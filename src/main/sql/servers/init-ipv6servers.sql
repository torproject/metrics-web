-- Copyright 2017--2020 The Tor Project
-- See LICENSE for licensing information

-- Table of all known flags, to match flag strings to bit positions in the flags
-- column in status_entries. If the number of known flags ever grows larger than
-- 31, we'll have to extend flag_id to accept values between 0 and 63 and alter
-- the flags column in status_entries from INTEGER to BIGINT. And if it grows
-- even larger, we'll have to do something even smarter.
CREATE TABLE flags (
  flag_id SMALLINT PRIMARY KEY CHECK (flag_id BETWEEN 0 AND 30),
  flag_string TEXT UNIQUE NOT NULL
);

-- Hard-wire the Guard and (Bad)Exit flags, so that we can implement
-- aggregate_ipv6() below without having to look up their IDs in the flags
-- table.
INSERT INTO flags (flag_id, flag_string) VALUES (0, 'Guard');
INSERT INTO flags (flag_id, flag_string) VALUES (1, 'Exit');
INSERT INTO flags (flag_id, flag_string) VALUES (2, 'BadExit');

-- Table of all known versions, either from platform lines in server descriptors
-- or recommended-server-versions lines in consensuses. Versions found in the
-- latter are marked as recommended, which enables us to only include major
-- versions in results that have been recommended at least once in a consensus.
CREATE TABLE versions (
  version_id SERIAL PRIMARY KEY,
  version_string TEXT UNIQUE NOT NULL,
  recommended BOOLEAN NOT NULL
);

-- Hard-wire a 0.1.0 version and a 0.1.1 version, because these major versions
-- were never recommended in a consensus, yet they are supposed to go into the
-- versions statistics.
INSERT INTO versions (version_string, recommended) VALUES ('0.1.0.17', TRUE);
INSERT INTO versions (version_string, recommended) VALUES ('0.1.1.26', TRUE);

-- Table of all known platforms from platform lines in server descriptors, that
-- is, without Tor software version information.
CREATE TABLE platforms (
  platform_id SERIAL PRIMARY KEY,
  platform_string TEXT UNIQUE NOT NULL
);

-- Table of all relevant parts contained in relay or bridge server descriptors.
-- We're not deleting from this table, because we can never be sure that we
-- won't import a previously missing status that we'll want to match against
-- existing server descriptors.
CREATE TABLE server_descriptors (
  descriptor_digest_sha1 BYTEA PRIMARY KEY,
  platform_id INTEGER REFERENCES platforms (platform_id),
  version_id INTEGER REFERENCES versions (version_id),
  advertised_bandwidth_bytes INTEGER NOT NULL,
  announced_ipv6 BOOLEAN NOT NULL,
  exiting_ipv6_relay BOOLEAN NOT NULL
);

-- Enumeration type for servers, which can be either relays or bridges.
CREATE TYPE server_enum AS ENUM ('relay', 'bridge');

-- Table of all relay or bridge statuses. We're not deleting from this table.
CREATE TABLE statuses (
  status_id SERIAL PRIMARY KEY,
  server server_enum NOT NULL,
  valid_after TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  running_count INTEGER NOT NULL,
  consensus_weight_sum REAL,
  guard_weight_sum REAL,
  middle_weight_sum REAL,
  exit_weight_sum REAL,
  UNIQUE (server, valid_after)
);

-- Table of relay or bridge status entries. Unlike previous tables, we're
-- deleting from this table after aggregating rows into the aggregated table.
-- Otherwise this table would grow too large over time.
CREATE TABLE status_entries (
  status_id INTEGER REFERENCES statuses (status_id) NOT NULL,
  descriptor_digest_sha1 BYTEA NOT NULL,
  flags INTEGER NOT NULL,
  reachable_ipv6_relay BOOLEAN NOT NULL,
  consensus_weight REAL,
  guard_weight REAL,
  middle_weight REAL,
  exit_weight REAL,
  UNIQUE (status_id, descriptor_digest_sha1)
);

-- Table of joined and aggregated server_descriptors and status_entries rows.
-- For a given status and combination of flags and IPv6 capabilities, we count
-- the number of servers and advertised bandwidth bytes.
CREATE TABLE aggregated_ipv6 (
  status_id INTEGER REFERENCES statuses (status_id) NOT NULL,
  guard_relay BOOLEAN NOT NULL,
  exit_relay BOOLEAN NOT NULL,
  announced_ipv6 BOOLEAN NOT NULL,
  exiting_ipv6_relay BOOLEAN NOT NULL,
  reachable_ipv6_relay BOOLEAN NOT NULL,
  server_count_sum INTEGER NOT NULL,
  consensus_weight REAL,
  guard_weight REAL,
  middle_weight REAL,
  exit_weight REAL,
  advertised_bandwidth_bytes_sum BIGINT NOT NULL,
  CONSTRAINT aggregated_ipv6_unique
    UNIQUE (status_id, guard_relay, exit_relay, announced_ipv6,
    exiting_ipv6_relay, reachable_ipv6_relay)
);

-- Table of joined and aggregated server_descriptors and status_entries rows by
-- relay flag.
CREATE TABLE aggregated_flags (
  status_id INTEGER REFERENCES statuses (status_id) NOT NULL,
  flag_id INTEGER REFERENCES flags (flag_id) NOT NULL,
  server_count_sum INTEGER NOT NULL,
  consensus_weight REAL,
  guard_weight REAL,
  middle_weight REAL,
  exit_weight REAL,
  advertised_bandwidth_bytes_sum BIGINT NOT NULL,
  CONSTRAINT aggregated_flags_unique UNIQUE (status_id, flag_id)
);

-- Table of joined and aggregated server_descriptors and status_entries rows by
-- version.
CREATE TABLE aggregated_versions (
  status_id INTEGER REFERENCES statuses (status_id) NOT NULL,
  version_id INTEGER REFERENCES versions (version_id),
  server_count_sum INTEGER NOT NULL,
  consensus_weight REAL,
  guard_weight REAL,
  middle_weight REAL,
  exit_weight REAL,
  advertised_bandwidth_bytes_sum BIGINT NOT NULL,
  CONSTRAINT aggregated_versions_unique UNIQUE (status_id, version_id)
);

-- Table of joined and aggregated server_descriptors and status_entries rows by
-- platform.
CREATE TABLE aggregated_platforms (
  status_id INTEGER REFERENCES statuses (status_id) NOT NULL,
  platform_id INTEGER REFERENCES platforms (platform_id),
  server_count_sum INTEGER NOT NULL,
  consensus_weight REAL,
  guard_weight REAL,
  middle_weight REAL,
  exit_weight REAL,
  advertised_bandwidth_bytes_sum BIGINT NOT NULL,
  CONSTRAINT aggregated_platforms_unique UNIQUE (status_id, platform_id)
);

-- Function to aggregate server_descriptors and status_entries rows into the
-- aggregated_* tables and delete rows from status_entries that are then
-- contained in the aggregated_* tables. This function is supposed to be called
-- once after inserting new rows into server_descriptors and/or status_entries.
-- Subsequent calls won't have any effect.
CREATE OR REPLACE FUNCTION aggregate() RETURNS VOID AS $$
-- Aggregate by IPv6 support.
INSERT INTO aggregated_ipv6
SELECT status_id, flags & 1 > 0 AS guard_relay,
  flags & 2 > 0 AND flags & 4 = 0 AS exit_relay,
  announced_ipv6, exiting_ipv6_relay,
  reachable_ipv6_relay, COUNT(*) AS server_count_sum,
  SUM(consensus_weight) AS consensus_weight,
  SUM(guard_weight) AS guard_weight,
  SUM(middle_weight) AS middle_weight,
  SUM(exit_weight) AS exit_weight,
  SUM(advertised_bandwidth_bytes) AS advertised_bandwidth_bytes_sum
FROM status_entries
NATURAL JOIN server_descriptors
GROUP BY status_id, guard_relay, exit_relay, announced_ipv6, exiting_ipv6_relay,
  reachable_ipv6_relay
ON CONFLICT ON CONSTRAINT aggregated_ipv6_unique
DO UPDATE SET server_count_sum = aggregated_ipv6.server_count_sum
    + EXCLUDED.server_count_sum,
  consensus_weight = aggregated_ipv6.consensus_weight
    + EXCLUDED.consensus_weight,
  guard_weight = aggregated_ipv6.guard_weight + EXCLUDED.guard_weight,
  middle_weight = aggregated_ipv6.middle_weight + EXCLUDED.middle_weight,
  exit_weight = aggregated_ipv6.exit_weight + EXCLUDED.exit_weight,
  advertised_bandwidth_bytes_sum
    = aggregated_ipv6.advertised_bandwidth_bytes_sum
    + EXCLUDED.advertised_bandwidth_bytes_sum;
-- Aggregate by assigned relay flags.
INSERT INTO aggregated_flags
SELECT status_id, flag_id, COUNT(*) AS server_count_sum,
  SUM(consensus_weight) AS consensus_weight,
  SUM(guard_weight) AS guard_weight,
  SUM(middle_weight) AS middle_weight,
  SUM(exit_weight) AS exit_weight,
  SUM(advertised_bandwidth_bytes) AS advertised_bandwidth_bytes_sum
FROM status_entries
NATURAL JOIN server_descriptors
JOIN flags ON flags & (1 << flag_id) > 0
GROUP BY status_id, flag_id
ON CONFLICT ON CONSTRAINT aggregated_flags_unique
DO UPDATE SET server_count_sum = aggregated_flags.server_count_sum
    + EXCLUDED.server_count_sum,
  consensus_weight = aggregated_flags.consensus_weight
    + EXCLUDED.consensus_weight,
  guard_weight = aggregated_flags.guard_weight + EXCLUDED.guard_weight,
  middle_weight = aggregated_flags.middle_weight + EXCLUDED.middle_weight,
  exit_weight = aggregated_flags.exit_weight + EXCLUDED.exit_weight,
  advertised_bandwidth_bytes_sum
    = aggregated_flags.advertised_bandwidth_bytes_sum
    + EXCLUDED.advertised_bandwidth_bytes_sum;
-- Aggregate by version.
INSERT INTO aggregated_versions
SELECT status_id, version_id, COUNT(*) AS server_count_sum,
  SUM(consensus_weight) AS consensus_weight,
  SUM(guard_weight) AS guard_weight,
  SUM(middle_weight) AS middle_weight,
  SUM(exit_weight) AS exit_weight,
  SUM(advertised_bandwidth_bytes) AS advertised_bandwidth_bytes_sum
FROM status_entries
NATURAL JOIN server_descriptors
GROUP BY status_id, version_id
ON CONFLICT ON CONSTRAINT aggregated_versions_unique
DO UPDATE SET server_count_sum = aggregated_versions.server_count_sum
    + EXCLUDED.server_count_sum,
  consensus_weight = aggregated_versions.consensus_weight
    + EXCLUDED.consensus_weight,
  guard_weight = aggregated_versions.guard_weight + EXCLUDED.guard_weight,
  middle_weight = aggregated_versions.middle_weight + EXCLUDED.middle_weight,
  exit_weight = aggregated_versions.exit_weight + EXCLUDED.exit_weight,
  advertised_bandwidth_bytes_sum
    = aggregated_versions.advertised_bandwidth_bytes_sum
    + EXCLUDED.advertised_bandwidth_bytes_sum;
-- Aggregate by platform.
INSERT INTO aggregated_platforms
SELECT status_id, platform_id, COUNT(*) AS server_count_sum,
  SUM(consensus_weight) AS consensus_weight,
  SUM(guard_weight) AS guard_weight,
  SUM(middle_weight) AS middle_weight,
  SUM(exit_weight) AS exit_weight,
  SUM(advertised_bandwidth_bytes) AS advertised_bandwidth_bytes_sum
FROM status_entries
NATURAL JOIN server_descriptors
GROUP BY status_id, platform_id
ON CONFLICT ON CONSTRAINT aggregated_platforms_unique
DO UPDATE SET server_count_sum = aggregated_platforms.server_count_sum
    + EXCLUDED.server_count_sum,
  consensus_weight = aggregated_platforms.consensus_weight
    + EXCLUDED.consensus_weight,
  guard_weight = aggregated_platforms.guard_weight + EXCLUDED.guard_weight,
  middle_weight = aggregated_platforms.middle_weight + EXCLUDED.middle_weight,
  exit_weight = aggregated_platforms.exit_weight + EXCLUDED.exit_weight,
  advertised_bandwidth_bytes_sum
    = aggregated_platforms.advertised_bandwidth_bytes_sum
    + EXCLUDED.advertised_bandwidth_bytes_sum;
-- Delete obsolete rows from the status_entries table.
DELETE FROM status_entries WHERE EXISTS (
  SELECT 1 FROM server_descriptors
  WHERE descriptor_digest_sha1 = status_entries.descriptor_digest_sha1);
$$ LANGUAGE SQL;

-- View on previously aggregated IPv6 server statistics in a format that is
-- compatible for writing to an output CSV file. Statuses are only included in
-- the output if they have at least 1 relay or bridge with the Running flag and
-- if at least 99.9% of referenced server descriptors are present. Dates are
-- only included in the output if at least 12 statuses are known. The last two
-- dates are excluded to avoid statistics from flapping if missing descriptors
-- are provided late.
CREATE OR REPLACE VIEW ipv6servers AS
WITH included_statuses AS (
  SELECT status_id, server, valid_after
  FROM statuses NATURAL JOIN aggregated_ipv6
  GROUP BY status_id, server, valid_after
  HAVING running_count > 0
  AND 1000 * SUM(server_count_sum) > 999 * running_count
), included_dates AS (
  SELECT DATE(valid_after) AS valid_after_date, server
  FROM included_statuses
  GROUP BY DATE(valid_after), server
  HAVING COUNT(status_id) >= 12
  AND DATE(valid_after)
    < (SELECT MAX(DATE(valid_after)) FROM included_statuses)
), grouped_by_status AS (
  SELECT valid_after, server,
    CASE WHEN server = 'relay' THEN guard_relay ELSE NULL END
      AS guard_relay_or_null,
    CASE WHEN server = 'relay' THEN exit_relay ELSE NULL END
      AS exit_relay_or_null,
    announced_ipv6,
    CASE WHEN server = 'relay' THEN exiting_ipv6_relay ELSE NULL END
      AS exiting_ipv6_relay_or_null,
    CASE WHEN server = 'relay' THEN reachable_ipv6_relay ELSE NULL END
      AS reachable_ipv6_relay_or_null,
    SUM(server_count_sum) AS server_count_sum,
    CASE WHEN server = 'relay' THEN SUM(advertised_bandwidth_bytes_sum)
      ELSE NULL END AS advertised_bandwidth_bytes_sum
  FROM statuses NATURAL JOIN aggregated_ipv6
  WHERE status_id IN (SELECT status_id FROM included_statuses)
  AND DATE(valid_after) IN (
    SELECT valid_after_date FROM included_dates
    WHERE included_dates.server = statuses.server)
  GROUP BY status_id, valid_after, server, guard_relay_or_null,
    exit_relay_or_null, announced_ipv6, exiting_ipv6_relay_or_null,
    reachable_ipv6_relay_or_null
)
SELECT DATE(valid_after) AS valid_after_date, server,
  guard_relay_or_null AS guard_relay,
  exit_relay_or_null AS exit_relay,
  announced_ipv6,
  exiting_ipv6_relay_or_null AS exiting_ipv6_relay,
  reachable_ipv6_relay_or_null AS reachable_ipv6_relay,
  FLOOR(AVG(server_count_sum)) AS server_count_sum_avg,
  FLOOR(AVG(advertised_bandwidth_bytes_sum))
    AS advertised_bandwidth_bytes_sum_avg
FROM grouped_by_status
GROUP BY DATE(valid_after), server, guard_relay, exit_relay, announced_ipv6,
  exiting_ipv6_relay, reachable_ipv6_relay
ORDER BY valid_after_date, server, guard_relay, exit_relay, announced_ipv6,
  exiting_ipv6_relay, reachable_ipv6_relay;

-- View on advertised bandwidth by Exit/Guard flag combination.
CREATE OR REPLACE VIEW bandwidth_advbw AS
SELECT valid_after_date AS date,
  exit_relay AS isexit,
  guard_relay AS isguard,
  FLOOR(SUM(advertised_bandwidth_bytes_sum_avg)) AS advbw
FROM ipv6servers
WHERE server = 'relay'
GROUP BY date, isexit, isguard
ORDER BY date, isexit, isguard;

-- View on the number of running servers by relay flag.
CREATE OR REPLACE VIEW servers_flags_complete AS
WITH included_statuses AS (
  SELECT status_id, server, valid_after
  FROM statuses NATURAL JOIN aggregated_flags
  GROUP BY status_id
  HAVING running_count > 0
  AND 1000 * SUM(server_count_sum) > 999 * running_count
), included_dates AS (
  SELECT DATE(valid_after) AS valid_after_date, server
  FROM included_statuses NATURAL JOIN aggregated_flags
  GROUP BY DATE(valid_after), server
  HAVING COUNT(status_id) >= 12
  AND DATE(valid_after)
    < (SELECT MAX(DATE(valid_after)) FROM included_statuses)
)
SELECT DATE(valid_after) AS valid_after_date, server, flag_string AS flag,
  FLOOR(AVG(server_count_sum)) AS server_count_sum_avg,
  AVG(consensus_weight / consensus_weight_sum) AS consensus_weight_fraction,
  AVG(guard_weight / guard_weight_sum) AS guard_weight_fraction,
  AVG(middle_weight / middle_weight_sum) AS middle_weight_fraction,
  AVG(exit_weight / exit_weight_sum) AS exit_weight_fraction
FROM statuses NATURAL JOIN aggregated_flags NATURAL JOIN flags
WHERE status_id IN (SELECT status_id FROM included_statuses)
AND DATE(valid_after) IN (
  SELECT valid_after_date FROM included_dates
  WHERE included_dates.server = statuses.server)
GROUP BY DATE(valid_after), server, flag
ORDER BY valid_after_date, server, flag;

-- View on the number of running relays and bridges.
CREATE OR REPLACE VIEW servers_networksize AS
SELECT valid_after_date AS date,
  FLOOR(AVG(CASE WHEN server = 'relay' THEN server_count_sum_avg
    ELSE NULL END)) AS relays,
  FLOOR(AVG(CASE WHEN server = 'bridge' THEN server_count_sum_avg
    ELSE NULL END)) AS bridges
FROM servers_flags_complete
WHERE flag = 'Running'
GROUP BY date
ORDER BY date;

-- View on the number of running relays by relay flag.
CREATE OR REPLACE VIEW servers_relayflags AS
SELECT valid_after_date AS date, flag, server_count_sum_avg AS relays
FROM servers_flags_complete
WHERE server = 'relay'
AND flag IN ('Running', 'Exit', 'Fast', 'Guard', 'Stable', 'HSDir')
ORDER BY date, flag;

-- View on the number of running servers by version.
CREATE OR REPLACE VIEW servers_versions_complete AS
WITH included_statuses AS (
  SELECT status_id, server, valid_after
  FROM statuses NATURAL JOIN aggregated_versions
  GROUP BY status_id, server, valid_after
  HAVING running_count > 0
  AND 1000 * SUM(server_count_sum) > 999 * running_count
), included_dates AS (
  SELECT DATE(valid_after) AS valid_after_date, server
  FROM included_statuses
  GROUP BY DATE(valid_after), server
  HAVING COUNT(status_id) >= 12
  AND DATE(valid_after)
    < (SELECT MAX(DATE(valid_after)) FROM included_statuses)
), included_versions AS (
  SELECT SUBSTRING(version_string FROM '^([^\.]+\.[^\.]+\.[^\.]+)') AS version
  FROM versions
  WHERE recommended IS TRUE
  GROUP BY version
), grouped_by_version AS (
  SELECT server, valid_after,
    CASE WHEN SUBSTRING(version_string FROM '^([^\.]+\.[^\.]+\.[^\.]+)')
      IN (SELECT version FROM included_versions)
      THEN SUBSTRING(version_string FROM '^([^\.]+\.[^\.]+\.[^\.]+)')
      ELSE 'Other' END AS version,
    SUM(server_count_sum) AS server_count_sum,
    SUM(consensus_weight) AS consensus_weight,
    SUM(guard_weight) AS guard_weight,
    SUM(middle_weight) AS middle_weight,
    SUM(exit_weight) AS exit_weight,
    consensus_weight_sum,
    guard_weight_sum,
    middle_weight_sum,
    exit_weight_sum
  FROM statuses NATURAL JOIN aggregated_versions LEFT JOIN versions
    ON aggregated_versions.version_id = versions.version_id
  WHERE status_id IN (SELECT status_id FROM included_statuses)
  AND DATE(valid_after) IN (
    SELECT valid_after_date FROM included_dates
    WHERE included_dates.server = statuses.server)
  GROUP BY status_id, server, valid_after, version
)
SELECT DATE(valid_after) AS valid_after_date, server, version,
  FLOOR(AVG(server_count_sum)) AS server_count_sum_avg,
  AVG(consensus_weight / consensus_weight_sum) AS consensus_weight_fraction,
  AVG(guard_weight / guard_weight_sum) AS guard_weight_fraction,
  AVG(middle_weight / middle_weight_sum) AS middle_weight_fraction,
  AVG(exit_weight / exit_weight_sum) AS exit_weight_fraction
FROM grouped_by_version
GROUP BY DATE(valid_after), server, version
ORDER BY valid_after_date, server, version;

-- View on the number of running relays by version.
CREATE OR REPLACE VIEW servers_versions AS
SELECT valid_after_date AS date, version, server_count_sum_avg AS relays
FROM servers_versions_complete
WHERE server = 'relay'
ORDER BY date, version;

-- View on the number of running servers by platform.
CREATE OR REPLACE VIEW servers_platforms_complete AS
WITH included_statuses AS (
  SELECT status_id, server, valid_after
  FROM statuses NATURAL JOIN aggregated_platforms
  GROUP BY status_id, server, valid_after
  HAVING running_count > 0
  AND 1000 * SUM(server_count_sum) > 999 * running_count
), included_dates AS (
  SELECT DATE(valid_after) AS valid_after_date, server
  FROM included_statuses
  GROUP BY DATE(valid_after), server
  HAVING COUNT(status_id) >= 12
  AND DATE(valid_after)
    < (SELECT MAX(DATE(valid_after)) FROM included_statuses)
), grouped_by_platform AS (
  SELECT server, valid_after,
    CASE WHEN platform_string LIKE 'Linux%' THEN 'Linux'
      WHEN platform_string LIKE 'Windows%' THEN 'Windows'
      WHEN platform_string LIKE 'Darwin%' THEN 'macOS'
      WHEN platform_string LIKE '%BSD%'
        -- Uncomment, if we ever want to count DragonFly as BSD
        -- OR platform_string = 'DragonFly%'
        THEN 'BSD'
      ELSE 'Other' END AS platform,
    SUM(server_count_sum) AS server_count_sum,
    SUM(consensus_weight) AS consensus_weight,
    SUM(guard_weight) AS guard_weight,
    SUM(middle_weight) AS middle_weight,
    SUM(exit_weight) AS exit_weight,
    consensus_weight_sum,
    guard_weight_sum,
    middle_weight_sum,
    exit_weight_sum
  FROM statuses NATURAL JOIN aggregated_platforms LEFT JOIN platforms
    ON aggregated_platforms.platform_id = platforms.platform_id
  WHERE status_id IN (SELECT status_id FROM included_statuses)
  AND DATE(valid_after) IN (
    SELECT valid_after_date FROM included_dates
    WHERE included_dates.server = statuses.server)
  GROUP BY status_id, server, valid_after, platform
)
SELECT DATE(valid_after) AS valid_after_date, server, platform,
  FLOOR(AVG(server_count_sum)) AS server_count_sum_avg,
  AVG(consensus_weight / consensus_weight_sum) AS consensus_weight_fraction,
  AVG(guard_weight / guard_weight_sum) AS guard_weight_fraction,
  AVG(middle_weight / middle_weight_sum) AS middle_weight_fraction,
  AVG(exit_weight / exit_weight_sum) AS exit_weight_fraction
FROM grouped_by_platform
GROUP BY DATE(valid_after), server, platform
ORDER BY valid_after_date, server, platform;

-- View on the number of running relays by platform.
CREATE OR REPLACE VIEW servers_platforms AS
SELECT valid_after_date AS date, platform, server_count_sum_avg AS relays
FROM servers_platforms_complete
WHERE server = 'relay'
ORDER BY date, platform;

