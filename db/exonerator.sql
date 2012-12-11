-- Copyright 2011 The Tor Project
-- See LICENSE for licensing information

-- The descriptor table holds server descriptors that we use for display
-- purposes and to parse exit policies.
CREATE TABLE descriptor (

  -- The 40-character lower-case hex string identifies a descriptor
  -- uniquely and is used to join statusentry and this table.
  descriptor CHARACTER(40) NOT NULL PRIMARY KEY,

  -- The raw descriptor string is used for display purposes and to check
  -- whether the relay allowed exiting to a given target or not.
  rawdescriptor BYTEA NOT NULL
);

-- The consensus table stores network status consensuses to be looked up
-- by valid-after time and displayed upon request.  A second purpose is
-- to learn quickly whether the database contains status entries for a
-- given day or not.
CREATE TABLE consensus (

  -- The unique valid-after time of the consensus.
  validafter TIMESTAMP WITHOUT TIME ZONE NOT NULL PRIMARY KEY,

  -- The raw consensus string for display purposes only.
  rawconsensus BYTEA NOT NULL
);

-- The statusentry table stores network status consensus entries listing
-- a relay as running at a certain point in time.  Only relays with the
-- Running flag shall be inserted into this table.  If a relay advertises
-- more than one IP address, there is a distinct entry for each address in
-- this table.  If a relay advertises more than one TCP port on the same
-- IP address, there is only a single entry in this table.
CREATE TABLE statusentry (

  -- The valid-after time of the consensus that contains this entry.
  validafter TIMESTAMP WITHOUT TIME ZONE NOT NULL,

  -- The 40-character lower-case hex string uniquely identifying the
  -- relay.
  fingerprint CHARACTER(40) NOT NULL,

  -- The 40-character lower-case hex string that identifies the server
  -- descriptor published by the relay.
  descriptor CHARACTER(40) NOT NULL,

  -- The most significant 3 bytes of the relay's onion routing IPv4
  -- address in lower-case hex notation, or null if the relay's onion
  -- routing address in this status entry is IPv6.  The purpose is to
  -- quickly reduce query results for relays in the same /24 network.
  oraddress24 CHARACTER(6),

  -- The most significant 6 bytes of the relay's onion routing IPv6
  -- address in lower-case hex notation, or null if the relay's onion
  -- routing address in this status entry is IPv4.  The purpose is to
  -- quickly reduce query results for relays in the same /48 network.
  oraddress48 CHARACTER(12),

  -- The relay's onion routing address.  Can be an IPv4 or an IPv6
  -- address.  If a relay advertises more than one address, there are
  -- multiple entries in this table for the same status entry.
  oraddress INET NOT NULL,

  -- The raw status entry string as contained in the network status
  -- consensus for display purposes only.
  rawstatusentry BYTEA NOT NULL,

  -- A status entry is uniquely identified by its valid-after time, relay
  -- fingerprint, and onion routing address.
  CONSTRAINT statusentry_pkey
      PRIMARY KEY (validafter, fingerprint, oraddress)
);

-- The index on the exact onion routing address and on the valid-after
-- date is used to speed up ExoneraTor's query for status entries.
CREATE INDEX statusentry_oraddress_validafterdate
    ON statusentry (oraddress, DATE(validafter));

-- The index on the most significant 3 bytes of the relay's onion routing
-- address and on the valid-after date is used to speed up queries for
-- other relays in the same /24 network.
CREATE INDEX statusentry_oraddress24_validafterdate
    ON statusentry (oraddress24, DATE(validafter));

-- The index on the most significant 6 bytes of the relay's onion routing
-- address and on the valid-after date is used to speed up queries for
-- other relays in the same /48 network.
CREATE INDEX statusentry_oraddress48_validafterdate
    ON statusentry (oraddress48, DATE(validafter));

-- The exitlistentry table stores the results of the active testing,
-- DNS-based exit list for exit nodes.  An entry in this table means that
-- a relay was scanned at a given time and found to be exiting to the
-- Internet from a given IP address.  This IP address can be different
-- from the relay's onion routing address if the relay uses more than one
-- IP addresses.
CREATE TABLE exitlistentry (

  -- The 40-character lower-case hex string identifying the relay.
  fingerprint CHARACTER(40) NOT NULL,

  -- The most significant 3 bytes of the relay's exit IPv4 address in
  -- lower-case hex notation, or null if the relay's exit address in this
  -- entry is IPv6.  The purpose is to quickly reduce query results for
  -- relays exiting from the same /24 network.
  exitaddress24 CHARACTER(6),

  -- The IP address that the relay uses for exiting to the Internet.  If
  -- the relay uses more than one IP address, there are multiple entries
  -- in this table.
  exitaddress INET NOT NULL,

  -- The time when the relay was scanned to find out its exit IP
  -- address(es).
  scanned TIMESTAMP WITHOUT TIME ZONE NOT NULL,

  -- The raw exit list entry containing all scan results for a given relay
  -- for display purposes.
  rawexitlistentry BYTEA NOT NULL,

  -- An exit list entry is uniquely identified by its scan time, relay
  -- fingerprint, and exit address.
  CONSTRAINT exitlistentry_pkey
      PRIMARY KEY (scanned, fingerprint, exitaddress)
);

-- The index on the exact exit address and on the valid-after date is used
-- to speed up ExoneraTor's query for status entries referencing exit list
-- entries.
CREATE INDEX exitlistentry_exitaddress_scanneddate
    ON exitlistentry (exitaddress, DATE(scanned));

-- The index on the most significant 3 bytes of the relay's exit address
-- and on the valid-after date is used to speed up queries for other
-- relays in the same /24 network.
CREATE INDEX exitlistentry_exitaddress24_scanneddate
    ON exitlistentry (exitaddress24, DATE(scanned));

-- Create the plpgsql language, so that we can use it below.
CREATE LANGUAGE plpgsql;

-- Insert a server descriptor into the descriptor table.  Before doing so,
-- check that there is no descriptor with the same descriptor identifier
-- in the table yet.  Return 1 if the descriptor was inserted, 0
-- otherwise.
CREATE OR REPLACE FUNCTION insert_descriptor (
    insert_descriptor CHARACTER(40),
    insert_rawdescriptor BYTEA)
    RETURNS INTEGER AS $$
  BEGIN
    -- Look up if the descriptor is already contained in the descriptor
    -- table.
    IF (SELECT COUNT(*)
        FROM descriptor
        WHERE descriptor = insert_descriptor) = 0 THEN
      -- Insert the descriptor and remember the new descriptorid to update
      -- the foreign key in statusentry.
      INSERT INTO descriptor (descriptor, rawdescriptor)
          VALUES (insert_descriptor, insert_rawdescriptor);
      -- Return 1 for a successfully inserted descriptor.
      RETURN 1;
    ELSE
      -- Return 0 because we didn't change anything.
      RETURN 0;
    END IF;
  END;
$$ LANGUAGE 'plpgsql';

-- Insert a status entry into the statusentry table.  First check that
-- this status entry isn't contained in the table yet.  It's okay to
-- insert the same status entry multiple times for different IP addresses
-- though.  Return 1 if it was inserted, 0 otherwise.
CREATE OR REPLACE FUNCTION insert_statusentry (
    insert_validafter TIMESTAMP WITHOUT TIME ZONE,
    insert_fingerprint CHARACTER(40),
    insert_descriptor CHARACTER(40),
    insert_oraddress24 CHARACTER(6),
    insert_oraddress48 CHARACTER(12),
    insert_oraddress TEXT,
    insert_rawstatusentry BYTEA)
    RETURNS INTEGER AS $$
  BEGIN
    -- Look up if the status entry is already contained in the statusentry
    -- table.
    IF (SELECT COUNT(*)
        FROM statusentry
        WHERE validafter = insert_validafter
        AND fingerprint = insert_fingerprint
        AND oraddress = insert_oraddress::INET) = 0 THEN
      -- Insert the status entry.
      INSERT INTO statusentry (validafter, fingerprint, descriptor,
            oraddress24, oraddress48, oraddress, rawstatusentry)
          VALUES (insert_validafter, insert_fingerprint,
            insert_descriptor, insert_oraddress24, insert_oraddress48,
            insert_oraddress::INET, insert_rawstatusentry);
      -- Return 1 for a successfully inserted status entry.
      RETURN 1;
    ELSE
      -- Return 0 because we already had this status entry.
      RETURN 0;
    END IF;
  END;
$$ LANGUAGE 'plpgsql';

-- Insert a consensus into the consensus table.  Check that the same
-- consensus has not been imported before.  Return 1 if it was inserted, 0
-- otherwise.
CREATE OR REPLACE FUNCTION insert_consensus (
    insert_validafter TIMESTAMP WITHOUT TIME ZONE,
    insert_rawconsensus BYTEA)
    RETURNS INTEGER AS $$
  BEGIN
    -- Look up if the consensus is already contained in the consensus
    -- table.
    IF (SELECT COUNT(*)
        FROM consensus
        WHERE validafter = insert_validafter) = 0 THEN
      -- Insert the consensus.
      INSERT INTO consensus (validafter, rawconsensus)
          VALUES (insert_validafter, insert_rawconsensus);
      -- Return 1 for a successful insert operation.
      RETURN 1;
    ELSE
      -- Return 0 for not inserting the consensus.
      RETURN 0;
    END IF;
  END;
$$ LANGUAGE 'plpgsql';

-- Insert an exit list entry into the exitlistentry table.  Check that
-- this entry hasn't been inserted before.  It's okay to insert the same
-- exit list entry multiple times for different exit addresses.  Return 1
-- if the entry was inserted, 0 otherwise.
CREATE OR REPLACE FUNCTION insert_exitlistentry (
    insert_fingerprint CHARACTER(40),
    insert_exitaddress24 CHARACTER(6),
    insert_exitaddress TEXT,
    insert_scanned TIMESTAMP WITHOUT TIME ZONE,
    insert_rawexitlistentry BYTEA)
    RETURNS INTEGER AS $$
  BEGIN
    IF (SELECT COUNT(*)
        FROM exitlistentry
        WHERE fingerprint = insert_fingerprint
        AND exitaddress = insert_exitaddress::INET
        AND scanned = insert_scanned) = 0 THEN
      -- This exit list entry is not in the database yet.  Add it.
      INSERT INTO exitlistentry (fingerprint, exitaddress24, exitaddress,
            scanned, rawexitlistentry)
          VALUES (insert_fingerprint, insert_exitaddress24,
            insert_exitaddress::INET, insert_scanned,
            insert_rawexitlistentry);
      -- Return 1 for a successfully inserted exit list entry.
      RETURN 1;
    ELSE
      -- Return 0 to show that we didn't add anything.
      RETURN 0;
    END IF;
  END;
$$ LANGUAGE 'plpgsql';

-- Search for status entries with the given IP address as onion routing
-- address, plus status entries of relays having an exit list entry with
-- the given IP address as exit address.
CREATE OR REPLACE FUNCTION search_statusentries_by_address_date (
    select_address TEXT,
    select_date DATE)
    RETURNS TABLE(rawstatusentry BYTEA,
          descriptor CHARACTER(40),
          validafter TIMESTAMP WITHOUT TIME ZONE,
          fingerprint CHARACTER(40),
          oraddress TEXT,
          exitaddress TEXT,
          scanned TIMESTAMP WITHOUT TIME ZONE) AS $$
  -- The first select finds all status entries of relays with the given
  -- IP address as onion routing address.
  SELECT rawstatusentry,
        descriptor,
        validafter,
        fingerprint,
        HOST(oraddress),
        NULL,
        NULL
      FROM statusentry
      WHERE oraddress = $1::INET
      AND DATE(validafter) >= $2 - 1
      AND DATE(validafter) <= $2 + 1
  UNION
  -- The second select finds status entries of relays having an exit list
  -- entry with the provided IP address as the exit address.
  SELECT statusentry.rawstatusentry,
        statusentry.descriptor,
        statusentry.validafter,
        statusentry.fingerprint,
        HOST(statusentry.oraddress),
        HOST(exitlistentry.exitaddress),
        -- Pick only the last scan result that took place in the 24 hours
        -- before the valid-after time.
        MAX(exitlistentry.scanned)
      FROM statusentry
      JOIN exitlistentry
      ON statusentry.fingerprint = exitlistentry.fingerprint
      WHERE exitlistentry.exitaddress = $1::INET
      -- Focus on a time period from 1 day before and 1 day after the
      -- given date.  Also include a second day before the given date
      -- for exit lists, because it can take up to 24 hours to scan a
      -- relay again.  We shouldn't miss exit list entries here.
      AND DATE(exitlistentry.scanned) >= $2 - 2
      AND DATE(exitlistentry.scanned) <= $2 + 1
      AND DATE(statusentry.validafter) >= $2 - 1
      AND DATE(statusentry.validafter) <= $2 + 1
      -- Consider only exit list scans that took place in the 24 hours
      -- before the relay was listed in a consensus.
      AND statusentry.validafter >= exitlistentry.scanned
      AND statusentry.validafter - exitlistentry.scanned <=
          '1 day'::INTERVAL
      GROUP BY 1, 2, 3, 4, 5, 6
  ORDER BY 3, 4, 6;
$$ LANGUAGE SQL;

-- Look up all IPv4 OR and exit addresses in the /24 network of a given
-- address to suggest other addresses the user may be looking for.
CREATE OR REPLACE FUNCTION search_addresses_in_same_24 (
    select_address24 CHARACTER(6),
    select_date DATE)
    RETURNS TABLE(address TEXT) AS $$
  SELECT HOST(oraddress)
      FROM statusentry
      WHERE oraddress24 = $1
      AND DATE(validafter) >= $2 - 1
      AND DATE(validafter) <= $2 + 1
  UNION
  SELECT HOST(exitaddress)
      FROM exitlistentry
      WHERE exitaddress24 = $1
      AND DATE(scanned) >= $2 - 2
      AND DATE(scanned) <= $2 + 1
  ORDER BY 1;
$$ LANGUAGE SQL;

-- Look up all IPv6 OR addresses in the /48 network of a given address to
-- suggest other addresses the user may be looking for.
CREATE OR REPLACE FUNCTION search_addresses_in_same_48 (
    select_address48 CHARACTER(12),
    select_date DATE)
    RETURNS TABLE(address TEXT) AS $$
  SELECT HOST(oraddress)
      FROM statusentry
      WHERE oraddress48 = $1
      AND DATE(validafter) >= $2 - 1
      AND DATE(validafter) <= $2 + 1
  ORDER BY 1;
$$ LANGUAGE SQL;

