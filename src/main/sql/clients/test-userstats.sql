BEGIN;
SET search_path TO tap, public;
SELECT plan(152);
SET client_min_messages = warning;

-- Make sure enums are as expected.
SELECT has_enum('node');
SELECT enum_has_labels('node', ARRAY['relay', 'bridge']);
SELECT has_enum('metric');
SELECT enum_has_labels('metric', ARRAY['responses', 'bytes', 'status']);

-- Make sure that the imported table is exactly as the importer expects
-- it.
SELECT has_table('imported');
SELECT has_column('imported', 'fingerprint');
SELECT col_type_is('imported', 'fingerprint', 'CHARACTER(40)');
SELECT col_not_null('imported', 'fingerprint');
SELECT has_column('imported', 'node');
SELECT col_type_is('imported', 'node', 'node');
SELECT col_not_null('imported', 'node');
SELECT has_column('imported', 'metric');
SELECT col_type_is('imported', 'metric', 'metric');
SELECT col_not_null('imported', 'metric');
SELECT has_column('imported', 'country');
SELECT col_type_is('imported', 'country', 'CHARACTER VARYING(2)');
SELECT col_not_null('imported', 'country');
SELECT has_column('imported', 'transport');
SELECT col_type_is('imported', 'transport', 'CHARACTER VARYING(20)');
SELECT col_not_null('imported', 'transport');
SELECT has_column('imported', 'version');
SELECT col_type_is('imported', 'version', 'CHARACTER VARYING(2)');
SELECT col_not_null('imported', 'version');
SELECT has_column('imported', 'stats_start');
SELECT col_type_is('imported', 'stats_start',
  'TIMESTAMP WITHOUT TIME ZONE');
SELECT col_not_null('imported', 'stats_start');
SELECT has_column('imported', 'stats_end');
SELECT col_type_is('imported', 'stats_end',
  'TIMESTAMP WITHOUT TIME ZONE');
SELECT col_not_null('imported', 'stats_end');
SELECT has_column('imported', 'val');
SELECT col_type_is('imported', 'val', 'DOUBLE PRECISION');
SELECT col_not_null('imported', 'val');
SELECT hasnt_pk('imported');

-- Make sure that the internally-used merged table is exactly as merge()
-- expects it.
SELECT has_table('merged');
SELECT has_column('merged', 'id');
SELECT col_type_is('merged', 'id', 'INTEGER');
SELECT col_is_pk('merged', 'id');
SELECT has_column('merged', 'fingerprint');
SELECT col_type_is('merged', 'fingerprint', 'CHARACTER(40)');
SELECT col_not_null('merged', 'fingerprint');
SELECT has_column('merged', 'node');
SELECT col_type_is('merged', 'node', 'node');
SELECT col_not_null('merged', 'node');
SELECT has_column('merged', 'metric');
SELECT col_type_is('merged', 'metric', 'metric');
SELECT col_not_null('merged', 'metric');
SELECT has_column('merged', 'country');
SELECT col_type_is('merged', 'country', 'CHARACTER VARYING(2)');
SELECT col_not_null('merged', 'country');
SELECT has_column('merged', 'transport');
SELECT col_type_is('merged', 'transport', 'CHARACTER VARYING(20)');
SELECT col_not_null('merged', 'transport');
SELECT has_column('merged', 'version');
SELECT col_type_is('merged', 'version', 'CHARACTER VARYING(2)');
SELECT col_not_null('merged', 'version');
SELECT has_column('merged', 'stats_start');
SELECT col_type_is('merged', 'stats_start',
  'TIMESTAMP WITHOUT TIME ZONE');
SELECT col_not_null('merged', 'stats_start');
SELECT has_column('merged', 'stats_end');
SELECT col_type_is('merged', 'stats_end',
  'TIMESTAMP WITHOUT TIME ZONE');
SELECT col_not_null('merged', 'stats_end');
SELECT has_column('merged', 'val');
SELECT col_type_is('merged', 'val', 'DOUBLE PRECISION');
SELECT col_not_null('merged', 'val');

-- Make sure that the internally-used aggregated table is exactly as
-- aggregate() expects it.
SELECT has_table('aggregated');
SELECT has_column('aggregated', 'date');
SELECT col_type_is('aggregated', 'date', 'DATE');
SELECT col_not_null('aggregated', 'date');
SELECT has_column('aggregated', 'node');
SELECT col_type_is('aggregated', 'node', 'node');
SELECT col_not_null('aggregated', 'node');
SELECT has_column('aggregated', 'country');
SELECT col_type_is('aggregated', 'country', 'CHARACTER VARYING(2)');
SELECT col_not_null('aggregated', 'country');
SELECT col_default_is('aggregated', 'country', '');
SELECT has_column('aggregated', 'transport');
SELECT col_type_is('aggregated', 'transport', 'CHARACTER VARYING(20)');
SELECT col_not_null('aggregated', 'transport');
SELECT col_default_is('aggregated', 'transport', '');
SELECT has_column('aggregated', 'version');
SELECT col_type_is('aggregated', 'version', 'CHARACTER VARYING(2)');
SELECT col_not_null('aggregated', 'version');
SELECT col_default_is('aggregated', 'version', '');
SELECT has_column('aggregated', 'rrx');
SELECT col_type_is('aggregated', 'rrx', 'DOUBLE PRECISION');
SELECT col_not_null('aggregated', 'rrx');
SELECT col_default_is('aggregated', 'rrx', 0);
SELECT has_column('aggregated', 'nrx');
SELECT col_type_is('aggregated', 'nrx', 'DOUBLE PRECISION');
SELECT col_not_null('aggregated', 'nrx');
SELECT col_default_is('aggregated', 'nrx', 0);
SELECT has_column('aggregated', 'hh');
SELECT col_type_is('aggregated', 'hh', 'DOUBLE PRECISION');
SELECT col_not_null('aggregated', 'hh');
SELECT col_default_is('aggregated', 'hh', 0);
SELECT has_column('aggregated', 'nn');
SELECT col_type_is('aggregated', 'nn', 'DOUBLE PRECISION');
SELECT col_not_null('aggregated', 'nn');
SELECT col_default_is('aggregated', 'nn', 0);
SELECT has_column('aggregated', 'hrh');
SELECT col_type_is('aggregated', 'hrh', 'DOUBLE PRECISION');
SELECT col_not_null('aggregated', 'hrh');
SELECT col_default_is('aggregated', 'hrh', 0);
SELECT has_column('aggregated', 'nh');
SELECT col_type_is('aggregated', 'nh', 'DOUBLE PRECISION');
SELECT col_not_null('aggregated', 'nh');
SELECT col_default_is('aggregated', 'nh', 0);
SELECT has_column('aggregated', 'nrh');
SELECT col_type_is('aggregated', 'nrh', 'DOUBLE PRECISION');
SELECT col_not_null('aggregated', 'nrh');
SELECT col_default_is('aggregated', 'nrh', 0);

-- Create temporary tables that hide the actual tables, so that we don't
-- have to care about existing data, not even in a transaction that we're
-- going to roll back.  Temporarily set log level to warning to avoid
-- messages about implicitly created sequences and indexes.
CREATE TEMPORARY TABLE imported (
  fingerprint CHARACTER(40) NOT NULL,
  node node NOT NULL,
  metric metric NOT NULL,
  country CHARACTER VARYING(2) NOT NULL,
  transport CHARACTER VARYING(20) NOT NULL,
  version CHARACTER VARYING(2) NOT NULL,
  stats_start TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  stats_end TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  val DOUBLE PRECISION NOT NULL
);
CREATE TEMPORARY TABLE merged (
  id SERIAL PRIMARY KEY,
  fingerprint CHARACTER(40) NOT NULL,
  node node NOT NULL,
  metric metric NOT NULL,
  country CHARACTER VARYING(2) NOT NULL,
  transport CHARACTER VARYING(20) NOT NULL,
  version CHARACTER VARYING(2) NOT NULL,
  stats_start TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  stats_end TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  val DOUBLE PRECISION NOT NULL
);
CREATE TEMPORARY TABLE aggregated (
  date DATE NOT NULL,
  node node NOT NULL,
  country CHARACTER VARYING(2) NOT NULL DEFAULT '',
  transport CHARACTER VARYING(20) NOT NULL DEFAULT '',
  version CHARACTER VARYING(2) NOT NULL DEFAULT '',
  rrx DOUBLE PRECISION NOT NULL DEFAULT 0,
  nrx DOUBLE PRECISION NOT NULL DEFAULT 0,
  hh DOUBLE PRECISION NOT NULL DEFAULT 0,
  nn DOUBLE PRECISION NOT NULL DEFAULT 0,
  hrh DOUBLE PRECISION NOT NULL DEFAULT 0,
  nh DOUBLE PRECISION NOT NULL DEFAULT 0,
  nrh DOUBLE PRECISION NOT NULL DEFAULT 0
);

-- Test merging newly imported data.
PREPARE new_imported(TIMESTAMP WITHOUT TIME ZONE,
  TIMESTAMP WITHOUT TIME ZONE) AS INSERT INTO imported
  (fingerprint, node, metric, country, transport, version, stats_start,
  stats_end, val) VALUES ('1234567890123456789012345678901234567890',
  'relay', 'status', '', '', '', $1, $2, 0);
PREPARE new_merged(TIMESTAMP WITHOUT TIME ZONE,
  TIMESTAMP WITHOUT TIME ZONE) AS INSERT INTO merged
  (fingerprint, node, metric, country, transport, version, stats_start,
  stats_end, val) VALUES ('1234567890123456789012345678901234567890',
  'relay', 'status', '', '', '', $1, $2, 0);

EXECUTE new_imported('2013-04-11 14:00:00', '2013-04-11 15:00:00');
SELECT merge();
SELECT bag_eq('SELECT stats_end FROM merged',
  $$VALUES ('2013-04-11 15:00:00'::TIMESTAMP WITHOUT TIME ZONE)$$,
  'Should insert new entry into empty table as is');
DELETE FROM imported;
DELETE FROM merged;

EXECUTE new_imported('2013-04-11 13:00:00', '2013-04-11 14:00:00');
EXECUTE new_imported('2013-04-11 16:00:00', '2013-04-11 17:00:00');
SELECT merge();
SELECT bag_eq('SELECT stats_end FROM merged',
  $$VALUES ('2013-04-11 14:00:00'::TIMESTAMP WITHOUT TIME ZONE),
           ('2013-04-11 17:00:00'::TIMESTAMP WITHOUT TIME ZONE)$$,
  'Should insert two non-contiguous entries');
DELETE FROM imported;
DELETE FROM merged;

EXECUTE new_imported('2013-04-11 13:00:00', '2013-04-11 15:00:00');
EXECUTE new_imported('2013-04-11 15:00:00', '2013-04-11 17:00:00');
SELECT merge();
SELECT bag_eq('SELECT stats_end FROM merged',
  $$VALUES ('2013-04-11 17:00:00'::TIMESTAMP WITHOUT TIME ZONE)$$,
  'Should merge two contiguous entries');
DELETE FROM imported;
DELETE FROM merged;

EXECUTE new_imported('2013-04-11 13:00:00', '2013-04-11 16:00:00');
EXECUTE new_imported('2013-04-11 14:00:00', '2013-04-11 17:00:00');
SELECT merge();
SELECT bag_eq('SELECT stats_end FROM merged',
  $$VALUES ('2013-04-11 16:00:00'::TIMESTAMP WITHOUT TIME ZONE)$$,
  'Should skip entry that starts before and ends after the start of ' ||
  'another new entry');
DELETE FROM imported;
DELETE FROM merged;

EXECUTE new_imported('2013-04-11 13:00:00', '2013-04-11 15:00:00');
EXECUTE new_imported('2013-04-11 13:00:00', '2013-04-11 16:00:00');
SELECT merge();
SELECT bag_eq('SELECT stats_end FROM merged',
  $$VALUES ('2013-04-11 15:00:00'::TIMESTAMP WITHOUT TIME ZONE)$$,
  'Should skip entry that starts at and ends after the start of ' ||
  'another new entry');
DELETE FROM imported;
DELETE FROM merged;

EXECUTE new_imported('2013-04-11 13:00:00', '2013-04-11 16:00:00');
EXECUTE new_imported('2013-04-11 14:00:00', '2013-04-11 15:00:00');
SELECT merge();
SELECT bag_eq('SELECT stats_end FROM merged',
  $$VALUES ('2013-04-11 16:00:00'::TIMESTAMP WITHOUT TIME ZONE)$$,
  'Should skip entry that starts after another new entry starts and ' ||
  'ends before that entry ends');
DELETE FROM imported;
DELETE FROM merged;

EXECUTE new_imported('2013-04-11 13:00:00', '2013-04-11 16:00:00');
EXECUTE new_imported('2013-04-11 13:00:00', '2013-04-11 16:00:00');
SELECT merge();
SELECT bag_eq('SELECT stats_end FROM merged',
  $$VALUES ('2013-04-11 16:00:00'::TIMESTAMP WITHOUT TIME ZONE)$$,
  'Should skip entry that has same start and end as another new entry');
DELETE FROM imported;
DELETE FROM merged;

EXECUTE new_imported('2013-04-11 13:00:00', '2013-04-11 16:00:00');
EXECUTE new_imported('2013-04-11 14:00:00', '2013-04-11 16:00:00');
SELECT merge();
SELECT bag_eq('SELECT stats_end FROM merged',
  $$VALUES ('2013-04-11 16:00:00'::TIMESTAMP WITHOUT TIME ZONE)$$,
  'Should skip entry that starts before and ends at the end of ' ||
  'another new entry');
DELETE FROM imported;
DELETE FROM merged;

EXECUTE new_merged('2013-04-11 16:00:00', '2013-04-11 17:00:00');
EXECUTE new_imported('2013-04-11 14:00:00', '2013-04-11 15:00:00');
SELECT merge();
SELECT bag_eq('SELECT stats_end FROM merged',
  $$VALUES ('2013-04-11 15:00:00'::TIMESTAMP WITHOUT TIME ZONE),
           ('2013-04-11 17:00:00'::TIMESTAMP WITHOUT TIME ZONE)$$,
  'Should insert entry that ends before existing entry starts');
DELETE FROM imported;
DELETE FROM merged;

EXECUTE new_merged('2013-04-11 15:00:00', '2013-04-11 16:00:00');
EXECUTE new_imported('2013-04-11 14:00:00', '2013-04-11 15:00:00');
SELECT merge();
SELECT bag_eq('SELECT stats_end FROM merged',
  $$VALUES ('2013-04-11 16:00:00'::TIMESTAMP WITHOUT TIME ZONE)$$,
  'Should merge entry that ends when existing entry starts');
DELETE FROM imported;
DELETE FROM merged;

EXECUTE new_merged('2013-04-11 14:00:00', '2013-04-11 15:00:00');
EXECUTE new_imported('2013-04-11 13:00:00', '2013-04-11 14:30:00');
SELECT merge();
SELECT bag_eq('SELECT stats_start FROM merged',
  $$VALUES ('2013-04-11 14:00:00'::TIMESTAMP WITHOUT TIME ZONE)$$,
  'Should skip entry that starts before but ends after existing entry ' ||
  'starts');
DELETE FROM imported;
DELETE FROM merged;

EXECUTE new_merged('2013-04-11 11:00:00', '2013-04-11 13:00:00');
EXECUTE new_merged('2013-04-11 14:00:00', '2013-04-11 16:00:00');
EXECUTE new_imported('2013-04-11 13:00:00', '2013-04-11 15:00:00');
SELECT merge();
SELECT bag_eq('SELECT stats_end FROM merged',
  $$VALUES ('2013-04-11 13:00:00'::TIMESTAMP WITHOUT TIME ZONE),
           ('2013-04-11 16:00:00'::TIMESTAMP WITHOUT TIME ZONE)$$,
  'Should skip entry that starts when existing entry ends but ' ||
  'ends before another entry starts');
DELETE FROM imported;
DELETE FROM merged;

EXECUTE new_merged('2013-04-11 14:00:00', '2013-04-11 17:00:00');
EXECUTE new_imported('2013-04-11 14:00:00', '2013-04-11 15:00:00');
SELECT merge();
SELECT bag_eq('SELECT stats_end FROM merged',
  $$VALUES ('2013-04-11 17:00:00'::TIMESTAMP WITHOUT TIME ZONE)$$,
  'Should skip entry that starts when existing entry starts');
DELETE FROM imported;
DELETE FROM merged;

EXECUTE new_merged('2013-04-11 14:00:00', '2013-04-11 17:00:00');
EXECUTE new_imported('2013-04-11 15:00:00', '2013-04-11 16:00:00');
SELECT merge();
SELECT bag_eq('SELECT stats_end FROM merged',
  $$VALUES ('2013-04-11 17:00:00'::TIMESTAMP WITHOUT TIME ZONE)$$,
  'Should skip entry that starts after and ends before existing entry');
DELETE FROM imported;
DELETE FROM merged;

EXECUTE new_merged('2013-04-11 14:00:00', '2013-04-11 17:00:00');
EXECUTE new_imported('2013-04-11 14:00:00', '2013-04-11 17:00:00');
SELECT merge();
SELECT bag_eq('SELECT stats_end FROM merged',
  $$VALUES ('2013-04-11 17:00:00'::TIMESTAMP WITHOUT TIME ZONE)$$,
  'Should skip entry that is already contained');
DELETE FROM imported;
DELETE FROM merged;

EXECUTE new_merged('2013-04-11 14:00:00', '2013-04-11 17:00:00');
EXECUTE new_imported('2013-04-11 16:00:00', '2013-04-11 17:00:00');
SELECT merge();
SELECT bag_eq('SELECT stats_end FROM merged',
  $$VALUES ('2013-04-11 17:00:00'::TIMESTAMP WITHOUT TIME ZONE)$$,
  'Should skip entry that ends when existing entry ends');
DELETE FROM imported;
DELETE FROM merged;

EXECUTE new_merged('2013-04-11 14:00:00', '2013-04-11 17:00:00');
EXECUTE new_imported('2013-04-11 16:00:00', '2013-04-11 18:00:00');
SELECT merge();
SELECT bag_eq('SELECT stats_end FROM merged',
  $$VALUES ('2013-04-11 17:00:00'::TIMESTAMP WITHOUT TIME ZONE)$$,
  'Should skip entry that starts before but ends after existing entry ' ||
  'ends');
DELETE FROM imported;
DELETE FROM merged;

EXECUTE new_merged('2013-04-11 14:00:00', '2013-04-11 17:00:00');
EXECUTE new_merged('2013-04-11 18:00:00', '2013-04-11 19:00:00');
EXECUTE new_imported('2013-04-11 16:00:00', '2013-04-11 18:00:00');
SELECT merge();
SELECT bag_eq('SELECT stats_end FROM merged',
  $$VALUES ('2013-04-11 17:00:00'::TIMESTAMP WITHOUT TIME ZONE),
           ('2013-04-11 19:00:00'::TIMESTAMP WITHOUT TIME ZONE)$$,
  'Should skip entry that starts before existing entry ends and ends ' ||
  'when another entry starts');
DELETE FROM imported;
DELETE FROM merged;

EXECUTE new_merged('2013-04-11 11:00:00', '2013-04-11 13:00:00');
EXECUTE new_merged('2013-04-11 15:00:00', '2013-04-11 17:00:00');
EXECUTE new_imported('2013-04-11 12:00:00', '2013-04-11 16:00:00');
SELECT merge();
SELECT bag_eq('SELECT stats_end FROM merged',
  $$VALUES ('2013-04-11 13:00:00'::TIMESTAMP WITHOUT TIME ZONE),
           ('2013-04-11 17:00:00'::TIMESTAMP WITHOUT TIME ZONE)$$,
  'Should skip entry that starts before existing entry ends and ends ' ||
  'after another entry starts');
DELETE FROM imported;
DELETE FROM merged;

EXECUTE new_merged('2013-04-11 14:00:00', '2013-04-11 15:00:00');
EXECUTE new_imported('2013-04-11 15:00:00', '2013-04-11 16:00:00');
SELECT merge();
SELECT bag_eq('SELECT stats_end FROM merged',
  $$VALUES ('2013-04-11 16:00:00'::TIMESTAMP WITHOUT TIME ZONE)$$,
  'Should merge entry that ends when existing entry starts');
DELETE FROM imported;
DELETE FROM merged;

EXECUTE new_merged('2013-04-11 14:00:00', '2013-04-11 15:00:00');
EXECUTE new_imported('2013-04-11 16:00:00', '2013-04-11 17:00:00');
SELECT merge();
SELECT bag_eq('SELECT stats_end FROM merged',
  $$VALUES ('2013-04-11 15:00:00'::TIMESTAMP WITHOUT TIME ZONE),
           ('2013-04-11 17:00:00'::TIMESTAMP WITHOUT TIME ZONE)$$,
  'Should insert entry that starts after existing entry ends');
DELETE FROM imported;
DELETE FROM merged;

EXECUTE new_merged('2013-04-11 15:00:00', '2013-04-11 16:00:00');
EXECUTE new_imported('2013-04-11 14:00:00', '2013-04-11 17:00:00');
SELECT merge();
SELECT bag_eq('SELECT stats_end FROM merged',
  $$VALUES ('2013-04-11 16:00:00'::TIMESTAMP WITHOUT TIME ZONE)$$,
  'Should skip entry that starts before existing entry starts and ' ||
  'ends after that entry ends');
DELETE FROM imported;
DELETE FROM merged;

EXECUTE new_merged('2013-04-11 13:00:00', '2013-04-11 14:00:00');
EXECUTE new_merged('2013-04-11 15:00:00', '2013-04-11 16:00:00');
EXECUTE new_imported('2013-04-11 12:00:00', '2013-04-11 17:00:00');
SELECT merge();
SELECT bag_eq('SELECT stats_end FROM merged',
  $$VALUES ('2013-04-11 14:00:00'::TIMESTAMP WITHOUT TIME ZONE),
           ('2013-04-11 16:00:00'::TIMESTAMP WITHOUT TIME ZONE)$$,
  'Should skip entry that starts before and ends after multiple ' ||
  'existing entries');
DELETE FROM imported;
DELETE FROM merged;

EXECUTE new_imported('2013-04-11 23:00:00', '2013-04-12 00:00:00');
EXECUTE new_imported('2013-04-12 00:00:00', '2013-04-12 01:00:00');
SELECT merge();
SELECT bag_eq('SELECT stats_end FROM merged',
  $$VALUES ('2013-04-12 00:00:00'::TIMESTAMP WITHOUT TIME ZONE),
           ('2013-04-12 01:00:00'::TIMESTAMP WITHOUT TIME ZONE)$$,
  'Should insert two contiguous entries that end and start at midnight');
DELETE FROM imported;
DELETE FROM merged;

EXECUTE new_imported('2013-04-11 12:00:00', '2013-04-11 17:00:00');
INSERT INTO imported (fingerprint, node, metric, country, transport,
  version, stats_start, stats_end, val) VALUES
  ('9876543210987654321098765432109876543210', 'relay', 'status', '', '',
  '', '2013-04-11 12:00:00', '2013-04-11 17:00:00', 0);
SELECT merge();
SELECT bag_eq('SELECT stats_end FROM merged',
  $$VALUES ('2013-04-11 17:00:00'::TIMESTAMP WITHOUT TIME ZONE),
           ('2013-04-11 17:00:00'::TIMESTAMP WITHOUT TIME ZONE)$$,
  'Should import two entries with different fingerprints and same ' ||
  'start and end');
DELETE FROM imported;
DELETE FROM merged;

EXECUTE new_imported('2013-04-11 13:00:00', '2013-04-11 15:00:00');
INSERT INTO imported (fingerprint, node, metric, country, transport,
  version, stats_start, stats_end, val) VALUES
  ('9876543210987654321098765432109876543210', 'relay', 'status', '', '',
  '', '2013-04-11 14:00:00', '2013-04-11 16:00:00', 0);
SELECT merge();
SELECT bag_eq('SELECT stats_end FROM merged',
  $$VALUES ('2013-04-11 15:00:00'::TIMESTAMP WITHOUT TIME ZONE),
           ('2013-04-11 16:00:00'::TIMESTAMP WITHOUT TIME ZONE)$$,
  'Should import two entries with overlapping starts and ends and ' ||
  'different fingerprints');
DELETE FROM imported;
DELETE FROM merged;

-- TODO Test aggregating imported and merged data.

-- Make sure that the results view has the exact definition as expected
-- for the .csv export.
SELECT has_view('estimated');
SELECT has_column('estimated', 'date');
SELECT col_type_is('estimated', 'date', 'DATE');
SELECT has_column('estimated', 'node');
SELECT col_type_is('estimated', 'node', 'node');
SELECT has_column('estimated', 'country');
SELECT col_type_is('estimated', 'country', 'CHARACTER VARYING(2)');
SELECT has_column('estimated', 'transport');
SELECT col_type_is('estimated', 'transport', 'CHARACTER VARYING(20)');
SELECT has_column('estimated', 'version');
SELECT col_type_is('estimated', 'version', 'CHARACTER VARYING(2)');
SELECT has_column('estimated', 'frac');
SELECT col_type_is('estimated', 'frac', 'INTEGER');
SELECT has_column('estimated', 'users');
SELECT col_type_is('estimated', 'users', 'INTEGER');

-- TODO Test that frac and users are computed correctly in the view.

-- Finish tests.
SELECT * FROM finish();
RESET client_min_messages;
ROLLBACK;

