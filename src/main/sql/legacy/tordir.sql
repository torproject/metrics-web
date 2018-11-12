-- Copyright 2010, 2018 The Tor Project
-- See LICENSE for licensing information

CREATE LANGUAGE plpgsql;

-- TABLE descriptor
-- Contains all of the descriptors published by routers.
CREATE TABLE descriptor (
    descriptor CHARACTER(40) NOT NULL,
    nickname CHARACTER VARYING(19) NOT NULL,
    address CHARACTER VARYING(15) NOT NULL,
    orport INTEGER NOT NULL,
    dirport INTEGER NOT NULL,
    fingerprint CHARACTER(40) NOT NULL,
    bandwidthavg BIGINT NOT NULL,
    bandwidthburst BIGINT NOT NULL,
    bandwidthobserved BIGINT NOT NULL,
    platform CHARACTER VARYING(256),
    published TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    uptime BIGINT,
    extrainfo CHARACTER(40),
    CONSTRAINT descriptor_pkey PRIMARY KEY (descriptor)
);

CREATE OR REPLACE FUNCTION delete_old_descriptor()
RETURNS INTEGER AS $$
    BEGIN
    DELETE FROM descriptor WHERE DATE(published) < current_date - 14;
    RETURN 1;
    END;
$$ LANGUAGE plpgsql;

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
    nickname CHARACTER VARYING(19) NOT NULL,
    fingerprint CHARACTER(40) NOT NULL,
    descriptor CHARACTER(40) NOT NULL,
    published TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    address CHARACTER VARYING(15) NOT NULL,
    orport INTEGER NOT NULL,
    dirport INTEGER NOT NULL,
    isauthority BOOLEAN DEFAULT FALSE NOT NULL,
    isbadexit BOOLEAN DEFAULT FALSE NOT NULL,
    isbaddirectory BOOLEAN DEFAULT FALSE NOT NULL,
    isexit BOOLEAN DEFAULT FALSE NOT NULL,
    isfast BOOLEAN DEFAULT FALSE NOT NULL,
    isguard BOOLEAN DEFAULT FALSE NOT NULL,
    ishsdir BOOLEAN DEFAULT FALSE NOT NULL,
    isnamed BOOLEAN DEFAULT FALSE NOT NULL,
    isstable BOOLEAN DEFAULT FALSE NOT NULL,
    isrunning BOOLEAN DEFAULT FALSE NOT NULL,
    isunnamed BOOLEAN DEFAULT FALSE NOT NULL,
    isvalid BOOLEAN DEFAULT FALSE NOT NULL,
    isv2dir BOOLEAN DEFAULT FALSE NOT NULL,
    isv3dir BOOLEAN DEFAULT FALSE NOT NULL,
    version CHARACTER VARYING(50),
    bandwidth BIGINT,
    ports TEXT,
    rawdesc BYTEA NOT NULL
);

CREATE OR REPLACE FUNCTION delete_old_statusentry()
RETURNS INTEGER AS $$
    BEGIN
    DELETE FROM statusentry WHERE DATE(validafter) < current_date - 14;
    RETURN 1;
    END;
$$ LANGUAGE plpgsql;

-- TABLE consensus
-- Contains all of the consensuses published by the directories.
CREATE TABLE consensus (
    validafter TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT consensus_pkey PRIMARY KEY (validafter)
);

-- TABLE bandwidth_flags
CREATE TABLE bandwidth_flags (
    date DATE NOT NULL,
    isexit BOOLEAN NOT NULL,
    isguard BOOLEAN NOT NULL,
    bwadvertised BIGINT NOT NULL,
    CONSTRAINT bandwidth_flags_pkey PRIMARY KEY(date, isexit, isguard)
);

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
    country CHARACTER(2) NOT NULL,
    r BIGINT,
    dw BIGINT,
    dr BIGINT,
    drw BIGINT,
    drr BIGINT,
    bw BIGINT,
    br BIGINT,
    bwd BIGINT,
    brd BIGINT,
    bwr BIGINT,
    brr BIGINT,
    bwdr BIGINT,
    brdr BIGINT,
    bwp BIGINT,
    brp BIGINT,
    bwn BIGINT,
    brn BIGINT,
    CONSTRAINT user_stats_pkey PRIMARY KEY(date, country)
);

-- TABLE relay_statuses_per_day
-- A helper table which is commonly used to update the tables above in the
-- refresh_* functions.
CREATE TABLE relay_statuses_per_day (
    date DATE NOT NULL,
    count INTEGER NOT NULL,
    CONSTRAINT relay_statuses_per_day_pkey PRIMARY KEY(date)
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

-- FUNCTION refresh_relay_statuses_per_day()
-- Updates helper table which is used to refresh the aggregate tables.
CREATE OR REPLACE FUNCTION refresh_relay_statuses_per_day()
RETURNS INTEGER AS $$
    BEGIN
    DELETE FROM relay_statuses_per_day
    WHERE date IN (SELECT date FROM updates);
    INSERT INTO relay_statuses_per_day (date, count)
    SELECT DATE(validafter) AS date, COUNT(*) AS count
    FROM consensus
    WHERE DATE(validafter) >= (SELECT MIN(date) FROM updates)
    AND DATE(validafter) <= (SELECT MAX(date) FROM updates)
    AND DATE(validafter) IN (SELECT date FROM updates)
    GROUP BY DATE(validafter);
    RETURN 1;
    END;
$$ LANGUAGE plpgsql;

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
    SET read[array_lower(insert_read, 1):
          array_upper(insert_read, 1)] = insert_read,
        written[array_lower(insert_written, 1):
          array_upper(insert_written, 1)] = insert_written,
        dirread[array_lower(insert_dirread, 1):
          array_upper(insert_dirread, 1)] = insert_dirread,
        dirwritten[array_lower(insert_dirwritten, 1):
          array_upper(insert_dirwritten, 1)] = insert_dirwritten
    WHERE fingerprint = insert_fingerprint AND date = insert_date;
    -- Updating twice is an ugly workaround for PostgreSQL bug 5840
    UPDATE bwhist
    SET read[array_lower(insert_read, 1):
          array_upper(insert_read, 1)] = insert_read,
        written[array_lower(insert_written, 1):
          array_upper(insert_written, 1)] = insert_written,
        dirread[array_lower(insert_dirread, 1):
          array_upper(insert_dirread, 1)] = insert_dirread,
        dirwritten[array_lower(insert_dirwritten, 1):
          array_upper(insert_dirwritten, 1)] = insert_dirwritten
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
-- up-to-date. They should be called every time ERNIE is run, or when new
-- data is finished being added to the descriptor or statusentry tables.
-- They find what new data has been entered or updated based on the
-- updates table.

CREATE OR REPLACE FUNCTION refresh_bandwidth_flags() RETURNS INTEGER AS $$
    DECLARE
        min_date TIMESTAMP WITHOUT TIME ZONE;
        max_date TIMESTAMP WITHOUT TIME ZONE;
    BEGIN

    min_date := (SELECT MIN(date) FROM updates);
    max_date := (SELECT MAX(date) + 1 FROM updates);

  DELETE FROM bandwidth_flags WHERE date IN (SELECT date FROM updates);
  EXECUTE '
  INSERT INTO bandwidth_flags (date, isexit, isguard, bwadvertised)
  SELECT DATE(validafter) AS date,
      BOOL_OR(isexit) AS isexit,
      BOOL_OR(isguard) AS isguard,
      (SUM(LEAST(bandwidthavg, bandwidthobserved))
      / relay_statuses_per_day.count)::BIGINT AS bwadvertised
    FROM descriptor RIGHT JOIN statusentry
    ON descriptor.descriptor = statusentry.descriptor
    JOIN relay_statuses_per_day
    ON DATE(validafter) = relay_statuses_per_day.date
    WHERE isrunning = TRUE
          AND validafter >= ''' || min_date || '''
          AND validafter < ''' || max_date || '''
          AND DATE(validafter) IN (SELECT date FROM updates)
          AND relay_statuses_per_day.date >= ''' || min_date || '''
          AND relay_statuses_per_day.date < ''' || max_date || '''
          AND DATE(relay_statuses_per_day.date) IN
              (SELECT date FROM updates)
    GROUP BY DATE(validafter), isexit, isguard, relay_statuses_per_day.count';
  RETURN 1;
  END;
$$ LANGUAGE plpgsql;

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
  INSERT INTO user_stats (date, country, dw, dr, bwd, brd, bwp, brp)
  SELECT
         bwhist_by_relay.date AS date,
         ''zy'' AS country,
         SUM(CASE WHEN authority IS NOT NULL
           THEN NULL ELSE dirwritten END) AS dw,
         SUM(CASE WHEN authority IS NOT NULL
           THEN NULL ELSE dirread END) AS dr,
         SUM(CASE WHEN dirwritten = 0 OR authority IS NOT NULL
           THEN NULL ELSE written END) AS bwd,
         SUM(CASE WHEN dirwritten = 0 OR authority IS NOT NULL
           THEN NULL ELSE read END) AS brd,
         SUM(CASE WHEN opendirport IS NULL OR authority IS NOT NULL
           THEN NULL ELSE written END) AS bwp,
         SUM(CASE WHEN opendirport IS NULL OR authority IS NOT NULL
           THEN NULL ELSE read END) AS brp
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
      SUM(CASE WHEN dirport > 0 THEN 1 ELSE NULL END) AS opendirport,
      SUM(CASE WHEN isauthority IS TRUE THEN 1 ELSE NULL END) AS authority
    FROM statusentry
    WHERE validafter >= ''' || min_date || '''
    AND validafter < ''' || max_date || '''
    AND DATE(validafter) IN (SELECT date FROM updates)
    GROUP BY 1, 2
  ) statusentry_by_relay
  ON bwhist_by_relay.fingerprint = statusentry_by_relay.fingerprint
  AND bwhist_by_relay.date = statusentry_by_relay.date
  -- Group by date and country, summing up the bandwidth histories.
  GROUP BY 1, 2';
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
    RAISE NOTICE '% Refreshing relay statuses per day.', timeofday();
    PERFORM refresh_relay_statuses_per_day();
    RAISE NOTICE '% Refreshing total relay bandwidth.', timeofday();
    PERFORM refresh_bandwidth_flags();
    RAISE NOTICE '% Refreshing bandwidth history.', timeofday();
    PERFORM refresh_bwhist_flags();
    RAISE NOTICE '% Refreshing user statistics.', timeofday();
    PERFORM refresh_user_stats();
    RAISE NOTICE '% Deleting processed dates.', timeofday();
    DELETE FROM scheduled_updates WHERE id IN (SELECT id FROM updates);
    RAISE NOTICE '% Deleting old descriptors.', timeofday();
    PERFORM delete_old_descriptor();
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
  (SELECT COALESCE(bandwidth_flags.date, bwhist_flags.date) AS date,
  COALESCE(bandwidth_flags.isexit, bwhist_flags.isexit) AS isexit,
  COALESCE(bandwidth_flags.isguard, bwhist_flags.isguard) AS isguard,
  bandwidth_flags.bwadvertised AS advbw,
  CASE WHEN bwhist_flags.read IS NOT NULL
  THEN bwhist_flags.read / 86400 END AS bwread,
  CASE WHEN bwhist_flags.written IS NOT NULL
  THEN bwhist_flags.written / 86400 END AS bwwrite,
  NULL AS dirread, NULL AS dirwrite
  FROM bandwidth_flags FULL OUTER JOIN bwhist_flags
  ON bandwidth_flags.date = bwhist_flags.date
  AND bandwidth_flags.isexit = bwhist_flags.isexit
  AND bandwidth_flags.isguard = bwhist_flags.isguard
  WHERE COALESCE(bandwidth_flags.date, bwhist_flags.date) <
  current_date - 2)
UNION ALL
  (SELECT date, NULL AS isexit, NULL AS isguard, NULL AS advbw,
  NULL AS bwread, NULL AS bwwrite,
  FLOOR(CAST(dr AS NUMERIC) / CAST(86400 AS NUMERIC)) AS dirread,
  FLOOR(CAST(dw AS NUMERIC) / CAST(86400 AS NUMERIC)) AS dirwrite
  FROM user_stats
  WHERE country = 'zy'
  AND date < current_date - 2)
ORDER BY date, isexit, isguard;

