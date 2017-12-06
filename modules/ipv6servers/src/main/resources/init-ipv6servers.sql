-- Copyright 2017 The Tor Project
-- See LICENSE for licensing information

-- Table of all relevant parts contained in relay or bridge server descriptors.
-- We're not deleting from this table, because we can never be sure that we
-- won't import a previously missing status that we'll want to match against
-- existing server descriptors.
CREATE TABLE server_descriptors (
  descriptor_digest_sha1 BYTEA PRIMARY KEY,
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
  UNIQUE (server, valid_after)
);

-- Table of relay or bridge status entries. Unlike previous tables, we're
-- deleting from this table after aggregating rows into the aggregated table.
-- Otherwise this table would grow too large over time.
CREATE TABLE status_entries (
  status_id INTEGER REFERENCES statuses (status_id) NOT NULL,
  descriptor_digest_sha1 BYTEA NOT NULL,
  guard_relay BOOLEAN NOT NULL,
  exit_relay BOOLEAN NOT NULL,
  reachable_ipv6_relay BOOLEAN NOT NULL,
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
  advertised_bandwidth_bytes_sum BIGINT NOT NULL,
  CONSTRAINT aggregated_ipv6_unique
    UNIQUE (status_id, guard_relay, exit_relay, announced_ipv6,
    exiting_ipv6_relay, reachable_ipv6_relay)
);

-- Function to aggregate server_descriptors and status_entries rows into the
-- aggregated table and delete rows from status_entries that are then contained
-- in the aggregated table. This function is supposed to be called once after
-- inserting new rows into server_descriptors and/or status_entries. Subsequent
-- calls won't have any effect.
CREATE OR REPLACE FUNCTION aggregate_ipv6() RETURNS VOID AS $$
INSERT INTO aggregated_ipv6
SELECT status_id, guard_relay, exit_relay, announced_ipv6, exiting_ipv6_relay,
  reachable_ipv6_relay, COUNT(*) AS server_count_sum,
  SUM(advertised_bandwidth_bytes) AS advertised_bandwidth_bytes
FROM status_entries
NATURAL JOIN server_descriptors
NATURAL JOIN statuses
GROUP BY status_id, guard_relay, exit_relay, announced_ipv6, exiting_ipv6_relay,
  reachable_ipv6_relay
ON CONFLICT ON CONSTRAINT aggregated_ipv6_unique
DO UPDATE SET server_count_sum = aggregated_ipv6.server_count_sum
  + EXCLUDED.server_count_sum,
  advertised_bandwidth_bytes_sum
  = aggregated_ipv6.advertised_bandwidth_bytes_sum
  + EXCLUDED.advertised_bandwidth_bytes_sum;
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
    < (SELECT MAX(DATE(valid_after)) FROM included_statuses) - 1
)
SELECT DATE(valid_after) AS valid_after_date, server,
  CASE WHEN server = 'relay' THEN guard_relay ELSE NULL END AS guard_relay,
  CASE WHEN server = 'relay' THEN exit_relay ELSE NULL END AS exit_relay,
  announced_ipv6,
  CASE WHEN server = 'relay' THEN exiting_ipv6_relay ELSE NULL END
    AS exiting_ipv6_relay,
  CASE WHEN server = 'relay' THEN reachable_ipv6_relay ELSE NULL END
    AS reachable_ipv6_relay,
  FLOOR(AVG(server_count_sum)) AS server_count_sum_avg,
  CASE WHEN server = 'relay' THEN FLOOR(AVG(advertised_bandwidth_bytes_sum))
    ELSE NULL END AS advertised_bandwidth_bytes_sum_avg
FROM statuses NATURAL JOIN aggregated_ipv6
WHERE status_id IN (SELECT status_id FROM included_statuses)
AND DATE(valid_after) IN (
  SELECT valid_after_date FROM included_dates WHERE server = statuses.server)
GROUP BY DATE(valid_after), server, guard_relay, exit_relay, announced_ipv6,
  exiting_ipv6_relay, reachable_ipv6_relay
ORDER BY valid_after_date, server, guard_relay, exit_relay, announced_ipv6,
  exiting_ipv6_relay, reachable_ipv6_relay;

