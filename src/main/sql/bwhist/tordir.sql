-- Copyright 2010, 2018--2020 The Tor Project
-- See LICENSE for licensing information

CREATE LANGUAGE plpgsql;

-- Contains bandwidth histories reported by relays in extra-info
-- descriptors. Each row contains the reported bandwidth in 15-minute
-- intervals for each relay and date.
CREATE TABLE bwhist (
    fingerprint CHARACTER(40) NOT NULL,
    date DATE NOT NULL,
    read BIGINT[],
    read_sum BIGINT,
    written BIGINT[],
    written_sum BIGINT,
    dirread BIGINT[],
    dirread_sum BIGINT,
    dirwritten BIGINT[],
    dirwritten_sum BIGINT,
    CONSTRAINT bwhist_pkey PRIMARY KEY (fingerprint, date)
);

CREATE INDEX bwhist_date ON bwhist (date);

CREATE OR REPLACE FUNCTION delete_old_bwhist()
RETURNS INTEGER AS $$
    BEGIN
    DELETE FROM bwhist WHERE date < current_date - 14;
    RETURN 1;
    END;
$$ LANGUAGE plpgsql;

-- TABLE statusentry
-- Contains all of the consensus entries published by the directories.
-- Each statusentry references a valid descriptor.
CREATE TABLE statusentry (
    validafter TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    fingerprint CHARACTER(40) NOT NULL,
    isauthority BOOLEAN DEFAULT FALSE NOT NULL,
    isexit BOOLEAN DEFAULT FALSE NOT NULL,
    isguard BOOLEAN DEFAULT FALSE NOT NULL,
    isrunning BOOLEAN DEFAULT FALSE NOT NULL
);

CREATE OR REPLACE FUNCTION delete_old_statusentry()
RETURNS INTEGER AS $$
    BEGIN
    DELETE FROM statusentry WHERE DATE(validafter) < current_date - 14;
    RETURN 1;
    END;
$$ LANGUAGE plpgsql;

-- TABLE bwhist_flags
CREATE TABLE bwhist_flags (
    date DATE NOT NULL,
    isexit BOOLEAN NOT NULL,
    isguard BOOLEAN NOT NULL,
    read BIGINT,
    written BIGINT,
    CONSTRAINT bwhist_flags_pkey PRIMARY KEY(date, isexit, isguard)
);

-- TABLE user_stats
-- Aggregate statistics on directory requests and byte histories that we
-- use to estimate user numbers.
CREATE TABLE user_stats (
    date DATE NOT NULL,
    dw BIGINT,
    dr BIGINT,
    daw BIGINT,
    dar BIGINT
);

-- Dates to be included in the next refresh run.
CREATE TABLE scheduled_updates (
    id SERIAL,
    date DATE NOT NULL
);

-- Dates in the current refresh run.  When starting a refresh run, we copy
-- the rows from scheduled_updates here in order to delete just those
-- lines after the refresh run.  Otherwise we might forget scheduled dates
-- that have been added during a refresh run.  If this happens we're going
-- to update these dates in the next refresh run.
CREATE TABLE updates (
    id INTEGER,
    date DATE
);

-- Return an array as the result of merging two arrays: if an array index is
-- only contained in one array, that array element is included in the result;
-- if an array index is contained in both arrays, the greater of the two
-- elements is included.
CREATE OR REPLACE FUNCTION array_merge(first BIGINT[], second BIGINT[])
RETURNS BIGINT[] AS $$
DECLARE
  merged BIGINT[];
BEGIN
  FOR i IN LEAST(array_lower(first, 1), array_lower(second, 1))..
      GREATEST(array_upper(first, 1), array_upper(second, 1)) LOOP
    merged[i] := GREATEST(first[i], second[i]);
  END LOOP;
RETURN merged;
END;
$$ LANGUAGE plpgsql
STABLE RETURNS NULL ON NULL INPUT;

CREATE OR REPLACE FUNCTION array_sum (BIGINT[]) RETURNS BIGINT AS $$
  SELECT SUM($1[i])::bigint
  FROM generate_series(array_lower($1, 1), array_upper($1, 1)) index(i);
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION insert_bwhist(
    insert_fingerprint CHARACTER(40), insert_date DATE,
    insert_read BIGINT[], insert_written BIGINT[],
    insert_dirread BIGINT[], insert_dirwritten BIGINT[])
    RETURNS INTEGER AS $$
  BEGIN
  IF (SELECT COUNT(*) FROM bwhist
      WHERE fingerprint = insert_fingerprint AND date = insert_date) = 0
      THEN
    INSERT INTO bwhist (fingerprint, date, read, written, dirread,
        dirwritten)
    VALUES (insert_fingerprint, insert_date, insert_read, insert_written,
        insert_dirread, insert_dirwritten);
  ELSE
    BEGIN
    UPDATE bwhist
    SET read = array_merge(read, insert_read),
        written = array_merge(written, insert_written),
        dirread = array_merge(dirread, insert_dirread),
        dirwritten = array_merge(dirwritten, insert_dirwritten)
    WHERE fingerprint = insert_fingerprint AND date = insert_date;
    END;
  END IF;
  UPDATE bwhist
  SET read_sum = array_sum(read),
      written_sum = array_sum(written),
      dirread_sum = array_sum(dirread),
      dirwritten_sum = array_sum(dirwritten)
  WHERE fingerprint = insert_fingerprint AND date = insert_date;
  RETURN 1;
  END;
$$ LANGUAGE plpgsql;

-- refresh_* functions
-- The following functions keep their corresponding aggregate tables
-- up-to-date. They should be called every time this module is run, or when new
-- data is finished being added to the statusentry tables.
-- They find what new data has been entered or updated based on the
-- updates table.

CREATE OR REPLACE FUNCTION refresh_bwhist_flags() RETURNS INTEGER AS $$
    DECLARE
        min_date TIMESTAMP WITHOUT TIME ZONE;
        max_date TIMESTAMP WITHOUT TIME ZONE;
    BEGIN

    min_date := (SELECT MIN(date) FROM updates);
    max_date := (SELECT MAX(date) + 1 FROM updates);

  DELETE FROM bwhist_flags WHERE date IN (SELECT date FROM updates);
  EXECUTE '
  INSERT INTO bwhist_flags (date, isexit, isguard, read, written)
  SELECT a.date, isexit, isguard, SUM(read_sum) as read,
      SUM(written_sum) AS written
  FROM
      (SELECT DATE(validafter) AS date,
             fingerprint,
             BOOL_OR(isexit) AS isexit,
             BOOL_OR(isguard) AS isguard
      FROM statusentry
      WHERE isrunning = TRUE
        AND validafter >= ''' || min_date || '''
        AND validafter < ''' || max_date || '''
        AND DATE(validafter) IN (SELECT date FROM updates)
      GROUP BY 1, 2) a
  JOIN bwhist
  ON a.date = bwhist.date
  AND a.fingerprint = bwhist.fingerprint
  GROUP BY 1, 2, 3';
  RETURN 1;
  END;
$$ LANGUAGE plpgsql;

-- FUNCTION refresh_user_stats()
-- This function refreshes our user statistics by weighting reported
-- directory request statistics of directory mirrors with bandwidth
-- histories.
CREATE OR REPLACE FUNCTION refresh_user_stats() RETURNS INTEGER AS $$
    DECLARE
        min_date TIMESTAMP WITHOUT TIME ZONE;
        max_date TIMESTAMP WITHOUT TIME ZONE;
    BEGIN

    min_date := (SELECT MIN(date) FROM updates);
    max_date := (SELECT MAX(date) + 1 FROM updates);

  -- Start by deleting user statistics of the dates we're about to
  -- regenerate.
  DELETE FROM user_stats WHERE date IN (SELECT date FROM updates);
  -- Now insert new user statistics.
  EXECUTE '
  INSERT INTO user_stats (date, dw, dr, daw, dar)
  SELECT
         bwhist_by_relay.date AS date,
         SUM(CASE WHEN authority IS NOT NULL
           THEN NULL ELSE dirwritten END) AS dw,
         SUM(CASE WHEN authority IS NOT NULL
           THEN NULL ELSE dirread END) AS dr,
         SUM(CASE WHEN authority IS NULL
           THEN NULL ELSE dirwritten END) AS daw,
         SUM(CASE WHEN authority IS NULL
           THEN NULL ELSE dirread END) AS dar
  FROM (
    -- Retrieve aggregate bandwidth histories of all relays in the given
    -- time frame.
    SELECT fingerprint, date, read_sum AS read, written_sum AS written,
           dirread_sum AS dirread, dirwritten_sum AS dirwritten
    FROM bwhist
    WHERE date >= ''' || min_date || '''
    AND date < ''' || max_date || '''
    AND date IN (SELECT date FROM updates)
  ) bwhist_by_relay
  LEFT JOIN (
    -- For each relay, tell how often it had an open directory port and
    -- how often it had the Authority flag assigned on a given date.
    SELECT fingerprint, DATE(validafter) AS date,
      SUM(CASE WHEN isauthority IS TRUE THEN 1 ELSE NULL END) AS authority
    FROM statusentry
    WHERE validafter >= ''' || min_date || '''
    AND validafter < ''' || max_date || '''
    AND DATE(validafter) IN (SELECT date FROM updates)
    GROUP BY 1, 2
  ) statusentry_by_relay
  ON bwhist_by_relay.fingerprint = statusentry_by_relay.fingerprint
  AND bwhist_by_relay.date = statusentry_by_relay.date
  -- Group by date, summing up the bandwidth histories.
  GROUP BY 1';
  RETURN 1;
  END;
$$ LANGUAGE plpgsql;

-- Refresh all statistics in the database.
CREATE OR REPLACE FUNCTION refresh_all() RETURNS INTEGER AS $$
  BEGIN
    RAISE NOTICE '% Starting refresh run.', timeofday();
    RAISE NOTICE '% Deleting old dates from updates.', timeofday();
    DELETE FROM updates;
    RAISE NOTICE '% Copying scheduled dates.', timeofday();
    INSERT INTO updates SELECT * FROM scheduled_updates;
    RAISE NOTICE '% Refreshing bandwidth history.', timeofday();
    PERFORM refresh_bwhist_flags();
    RAISE NOTICE '% Refreshing directory bytes history.', timeofday();
    PERFORM refresh_user_stats();
    RAISE NOTICE '% Deleting processed dates.', timeofday();
    DELETE FROM scheduled_updates WHERE id IN (SELECT id FROM updates);
    RAISE NOTICE '% Deleting old bandwidth histories.', timeofday();
    PERFORM delete_old_bwhist();
    RAISE NOTICE '% Deleting old status entries.', timeofday();
    PERFORM delete_old_statusentry();
    RAISE NOTICE '% Terminating refresh run.', timeofday();
  RETURN 1;
  END;
$$ LANGUAGE plpgsql;

-- View for exporting bandwidth statistics.
CREATE VIEW stats_bandwidth AS
  (SELECT date, isexit, isguard,
  read / 86400 AS bwread,
  written / 86400 AS bwwrite,
  NULL AS dirread, NULL AS dirwrite, NULL AS dirauthread, NULL AS dirauthwrite
  FROM bwhist_flags
  WHERE date < current_date - 2)
UNION ALL
  (SELECT date, NULL AS isexit, NULL AS isguard,
  NULL AS bwread, NULL AS bwwrite,
  FLOOR(CAST(dr AS NUMERIC) / CAST(86400 AS NUMERIC)) AS dirread,
  FLOOR(CAST(dw AS NUMERIC) / CAST(86400 AS NUMERIC)) AS dirwrite,
  FLOOR(CAST(dar AS NUMERIC) / CAST(86400 AS NUMERIC)) AS dirauthread,
  FLOOR(CAST(daw AS NUMERIC) / CAST(86400 AS NUMERIC)) AS dirauthwrite
  FROM user_stats
  WHERE date < current_date - 2)
ORDER BY date, isexit, isguard;

