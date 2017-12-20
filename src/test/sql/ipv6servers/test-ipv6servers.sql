-- Copyright 2017 The Tor Project
-- See LICENSE for licensing information

-- Hint: You'll need pgTAP in order to run these tests!

CREATE EXTENSION IF NOT EXISTS pgtap;

BEGIN;

SELECT plan(89);

-- Make sure that the server_descriptors table is as expected.
SELECT has_table('server_descriptors');
SELECT has_column('server_descriptors', 'descriptor_digest_sha1');
SELECT col_type_is('server_descriptors', 'descriptor_digest_sha1', 'bytea');
SELECT col_is_pk('server_descriptors', 'descriptor_digest_sha1');
SELECT has_column('server_descriptors', 'advertised_bandwidth_bytes');
SELECT col_type_is('server_descriptors', 'advertised_bandwidth_bytes', 'integer');
SELECT col_not_null('server_descriptors', 'advertised_bandwidth_bytes');
SELECT has_column('server_descriptors', 'announced_ipv6');
SELECT col_type_is('server_descriptors', 'announced_ipv6', 'boolean');
SELECT col_not_null('server_descriptors', 'announced_ipv6');
SELECT has_column('server_descriptors', 'exiting_ipv6_relay');
SELECT col_type_is('server_descriptors', 'exiting_ipv6_relay', 'boolean');
SELECT col_not_null('server_descriptors', 'exiting_ipv6_relay');

-- Make sure that the server enum is as expected.
SELECT has_enum('server_enum');
SELECT enum_has_labels('server_enum', ARRAY['relay', 'bridge']);

-- Make sure that the statuses table is as expected.
SELECT has_table('statuses');
SELECT has_column('statuses', 'status_id');
SELECT col_type_is('statuses', 'status_id', 'integer');
SELECT col_is_pk('statuses', 'status_id');
SELECT has_column('statuses', 'server');
SELECT col_type_is('statuses', 'server', 'server_enum');
SELECT col_not_null('statuses', 'server');
SELECT has_column('statuses', 'valid_after');
SELECT col_type_is('statuses', 'valid_after', 'timestamp without time zone');
SELECT col_not_null('statuses', 'valid_after');
SELECT has_column('statuses', 'running_count');
SELECT col_type_is('statuses', 'running_count', 'integer');
SELECT col_not_null('statuses', 'running_count');
SELECT col_is_unique('statuses', ARRAY['server', 'valid_after']);

-- Make sure that the status_entries table is as expected.
SELECT has_table('status_entries');
SELECT has_column('status_entries', 'status_id');
SELECT col_type_is('status_entries', 'status_id', 'integer');
SELECT fk_ok('status_entries', 'status_id', 'statuses', 'status_id');
SELECT col_not_null('status_entries', 'status_id');
SELECT has_column('status_entries', 'descriptor_digest_sha1');
SELECT col_type_is('status_entries', 'descriptor_digest_sha1', 'bytea');
SELECT col_not_null('status_entries', 'descriptor_digest_sha1');
SELECT has_column('status_entries', 'guard_relay');
SELECT col_type_is('status_entries', 'guard_relay', 'boolean');
SELECT col_not_null('status_entries', 'guard_relay');
SELECT has_column('status_entries', 'exit_relay');
SELECT col_type_is('status_entries', 'exit_relay', 'boolean');
SELECT col_not_null('status_entries', 'exit_relay');
SELECT has_column('status_entries', 'reachable_ipv6_relay');
SELECT col_type_is('status_entries', 'reachable_ipv6_relay', 'boolean');
SELECT col_not_null('status_entries', 'reachable_ipv6_relay');
SELECT col_is_unique('status_entries', ARRAY['status_id', 'descriptor_digest_sha1']);
SELECT hasnt_pk('status_entries');

-- Make sure that the aggregated_ipv6 table is as expected.
SELECT has_table('aggregated_ipv6');
SELECT has_column('aggregated_ipv6', 'status_id');
SELECT col_type_is('aggregated_ipv6', 'status_id', 'integer');
SELECT fk_ok('aggregated_ipv6', 'status_id', 'statuses', 'status_id');
SELECT col_not_null('aggregated_ipv6', 'status_id');
SELECT has_column('aggregated_ipv6', 'guard_relay');
SELECT col_type_is('aggregated_ipv6', 'guard_relay', 'boolean');
SELECT col_not_null('aggregated_ipv6', 'guard_relay');
SELECT has_column('aggregated_ipv6', 'exit_relay');
SELECT col_type_is('aggregated_ipv6', 'exit_relay', 'boolean');
SELECT col_not_null('aggregated_ipv6', 'exit_relay');
SELECT has_column('aggregated_ipv6', 'reachable_ipv6_relay');
SELECT col_type_is('aggregated_ipv6', 'reachable_ipv6_relay', 'boolean');
SELECT col_not_null('aggregated_ipv6', 'reachable_ipv6_relay');
SELECT has_column('aggregated_ipv6', 'announced_ipv6');
SELECT col_type_is('aggregated_ipv6', 'announced_ipv6', 'boolean');
SELECT col_not_null('aggregated_ipv6', 'announced_ipv6');
SELECT has_column('aggregated_ipv6', 'exiting_ipv6_relay');
SELECT col_type_is('aggregated_ipv6', 'exiting_ipv6_relay', 'boolean');
SELECT col_not_null('aggregated_ipv6', 'exiting_ipv6_relay');
SELECT has_column('aggregated_ipv6', 'server_count_sum');
SELECT col_type_is('aggregated_ipv6', 'server_count_sum', 'integer');
SELECT col_not_null('aggregated_ipv6', 'server_count_sum');
SELECT has_column('aggregated_ipv6', 'advertised_bandwidth_bytes_sum');
SELECT col_type_is('aggregated_ipv6', 'advertised_bandwidth_bytes_sum', 'bigint');
SELECT col_not_null('aggregated_ipv6', 'advertised_bandwidth_bytes_sum');
SELECT col_is_unique('aggregated_ipv6',
  ARRAY['status_id', 'guard_relay', 'exit_relay', 'announced_ipv6',
    'exiting_ipv6_relay', 'reachable_ipv6_relay']);

-- Truncate all tables for subsequent tests. This happens inside a transaction,
-- so we're not actually truncating anything.
TRUNCATE server_descriptors, statuses, status_entries, aggregated_ipv6;

-- Make sure that the aggregated_ipv6 table is empty.
SELECT set_eq('SELECT COUNT(*) FROM aggregated_ipv6;', 'SELECT 0;',
  'At the beginning, the aggregated_ipv6 table should be empty.');

-- And make sure that running the aggregate_ipv6() function does not change that.
SELECT aggregate_ipv6();
SELECT set_eq('SELECT COUNT(*) FROM aggregated_ipv6;', 'SELECT 0;',
  'Even after aggregating, the aggregated_ipv6 table should be empty.');

-- Insert a server descriptor, then try again.
INSERT INTO server_descriptors (descriptor_digest_sha1, advertised_bandwidth_bytes, announced_ipv6,
  exiting_ipv6_relay) VALUES ('\x00', 100, FALSE, TRUE);

-- Try to aggregate, though there's not much to aggregate without corresponding
-- entry in status_entries.
SELECT aggregate_ipv6();
SELECT set_eq('SELECT COUNT(*) FROM aggregated_ipv6;', 'SELECT 0;',
  'At the beginning, the aggregated_ipv6 table should be empty.');

-- Attempt to add an entry to status_entries, but without having inserted an
-- entry into statuses first.
SELECT throws_ok('INSERT INTO status_entries (status_id, descriptor_digest_sha1) '
  || 'VALUES (1, ''\x00'');');

-- Try again in the correct order.
INSERT INTO statuses (server, valid_after, running_count)
  VALUES ('relay'::server_enum, '2017-12-04 00:00:00'::TIMESTAMP, 1);
INSERT INTO status_entries
  SELECT status_id, '\x00', TRUE, FALSE, FALSE FROM statuses;

-- Now aggregate and see how the status_entries entry gets moved over to the
-- aggregated_ipv6 table. However, it's just one status, so it doesn't show in the
-- output view yet.
SELECT aggregate_ipv6();
SELECT set_eq('SELECT COUNT(*) FROM status_entries;', 'SELECT 0;',
  'status_entries should not contain aggregated row anymore.');
SELECT set_eq('SELECT COUNT(*) FROM aggregated_ipv6;', 'SELECT 1;',
  'aggregated_ipv6 table should contain exactly one row now.');
SELECT set_eq('SELECT COUNT(*) FROM ipv6servers;', 'SELECT 0;',
  'ipv6servers should not contain any results yet.');

-- Try to aggregate once more, but that shouldn't change anything.
SELECT aggregate_ipv6();
SELECT set_eq('SELECT COUNT(*) FROM status_entries;', 'SELECT 0;',
  'status_entries should still be empty.');
SELECT set_eq('SELECT COUNT(*) FROM aggregated_ipv6;', 'SELECT 1;',
  'aggregated_ipv6 table should still contain exactly one row.');

-- Insert statuses for 3 days, of which the last 2 will be cut off in the
-- output.
INSERT INTO statuses (server, valid_after, running_count)
  SELECT 'relay'::server_enum, GENERATE_SERIES('2017-12-04 01:00:00'::TIMESTAMP,
  '2017-12-06 23:00:00', '1 hour'), 1;

-- Insert the same relay as entries for all statuses except the one that we
-- added earlier and that is already contained in the aggregated_ipv6 table. (In the
-- actual import code we'd first check that we already inserted the status and
-- then not import any entries from it.)
INSERT INTO status_entries
  SELECT status_id, '\x00', TRUE, FALSE, FALSE FROM statuses
  WHERE valid_after > '2017-12-04 00:00:00'::TIMESTAMP;

-- Aggregate, then look at the output.
SELECT aggregate_ipv6();
SELECT set_eq('SELECT COUNT(*) FROM status_entries;', 'SELECT 0;',
  'status_entries should not contain anything anymore.');
SELECT set_eq('SELECT COUNT(*) FROM aggregated_ipv6;', 'SELECT 72;',
  'aggregated_ipv6 table should contain one row per status.');
SELECT set_eq('SELECT COUNT(*) FROM ipv6servers;', 'SELECT 1;',
  'ipv6servers should now contain a results line.');

-- Insert another status entry for which there is no corresponding server
-- descriptor to observe how the results line disappears again (because we
-- require 99.9% of server descriptors to be present). This is just a test case
-- that would not occur in practice, because we wouLdn't retroactively add new
-- status entries. It's just server descriptors that we might add later.
INSERT INTO status_entries
  SELECT status_id, '\x01', FALSE, FALSE, FALSE FROM statuses;
UPDATE statuses SET running_count = 2;
SELECT aggregate_ipv6();
SELECT set_eq('SELECT COUNT(*) FROM ipv6servers;', 'SELECT 0;',
  'ipv6servers should be empty, because of missing server descriptors.');

-- Okay, okay, provide the missing server descriptor.
INSERT INTO server_descriptors (descriptor_digest_sha1, advertised_bandwidth_bytes, announced_ipv6,
  exiting_ipv6_relay) VALUES ('\x01', 100, TRUE, TRUE);
SELECT aggregate_ipv6();
SELECT set_eq('SELECT COUNT(*) FROM ipv6servers;', 'SELECT 2;',
  'ipv6servers should be non-empty again.');

SELECT * FROM finish();

ROLLBACK;

