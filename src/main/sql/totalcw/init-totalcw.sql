-- Copyright 2018--2020 The Tor Project
-- See LICENSE for licensing information

-- Table of v3 authorities that stores nicknames and identity fingerprints and
-- assigns much shorter numeric identifiers for internal-only use.
CREATE TABLE authority (

  -- The auto-incremented numeric identifier for an authority.
  authority_id SERIAL PRIMARY KEY,

  -- The 1 to 19 character long alphanumeric nickname assigned to the authority by
  -- its operator.
  nickname CHARACTER VARYING(19) NOT NULL,

  -- Uppercase hex fingerprint of the authority's (v3 authority) identity key.
  identity_hex CHARACTER(40) NOT NULL,

  UNIQUE (nickname, identity_hex)
);

-- Table of all consensuses and votes with statistics on contained bandwidth
-- measurements. Only contains consensuses containing bandwidth values and votes
-- containing bandwidth measurements.
CREATE TABLE status (

  -- The auto-incremented numeric identifier for a status.
  status_id SERIAL PRIMARY KEY,

  -- Timestamp at which the consensus is supposed to become valid.
  valid_after TIMESTAMP WITHOUT TIME ZONE NOT NULL,

  -- Numeric identifier uniquely identifying the authority generating this vote,
  -- or NULL if this a consensus.
  authority_id INTEGER REFERENCES authority (authority_id),

  -- Whether contained relays had the Guard flag assigned.
  have_guard_flag BOOLEAN NOT NULL,

  -- Whether contained relays had the Exit flag assigned.
  have_exit_flag BOOLEAN NOT NULL,

  -- Sum of bandwidth measurements of all contained status entries.
  measured_sum BIGINT NOT NULL,

  UNIQUE (valid_after, authority_id, have_guard_flag, have_exit_flag)
);

-- View on aggregated total consensus weight statistics in a format that is
-- compatible for writing to an output CSV file. Votes are only included in the
-- output if at least 12 statuses are known for a given authority and day.
CREATE OR REPLACE VIEW totalcw AS
SELECT DATE(valid_after) AS valid_after_date, nickname, have_guard_flag,
  have_exit_flag, FLOOR(AVG(measured_sum)) AS measured_sum_avg
FROM status LEFT JOIN authority
ON status.authority_id = authority.authority_id
GROUP BY DATE(valid_after), nickname, have_guard_flag, have_exit_flag
HAVING COUNT(status_id) >= 12
  AND DATE(valid_after) < (SELECT MAX(DATE(valid_after)) FROM status)
ORDER BY DATE(valid_after), nickname, have_guard_flag, have_exit_flag;

