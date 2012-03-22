-- Copyright 2010 The Tor Project
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
    rawdesc BYTEA NOT NULL,
    CONSTRAINT descriptor_pkey PRIMARY KEY (descriptor)
);

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

CREATE OR REPLACE FUNCTION statusentry_insert_trigger()
RETURNS TRIGGER AS $$

DECLARE
  tablename TEXT;
  selectresult TEXT;
  nextmonth TIMESTAMP WITHOUT TIME ZONE;
  v_year INTEGER;
  v_month INTEGER;
  n_year INTEGER;
  n_month INTEGER;

BEGIN
  v_year := extract(YEAR FROM NEW.validafter);
  v_month := extract(MONTH FROM NEW.validafter);
  tablename := 'statusentry_y' || v_year || 'm' ||
      TO_CHAR(NEW.validafter, 'mm');
  EXECUTE 'SELECT relname FROM pg_class WHERE relname = '''|| tablename ||
    '''' INTO selectresult;
  IF selectresult IS NULL THEN
    nextmonth := new.validafter + interval '1 month';
    n_year := extract(YEAR FROM nextmonth);
    n_month := extract(MONTH FROM nextmonth);
    EXECUTE 'CREATE TABLE ' || tablename ||
      ' ( CHECK ( validafter >= ''' || v_year || '-' ||
      TO_CHAR(NEW.validafter, 'mm') || '-01 00:00:00'' ' ||
      'AND validafter < ''' || n_year || '-' ||
      TO_CHAR(nextmonth, 'mm') ||
      '-01 00:00:00'') ) INHERITS (statusentry)';
    EXECUTE 'ALTER TABLE ' || tablename || ' ADD CONSTRAINT ' ||
      tablename || '_pkey PRIMARY KEY (validafter, fingerprint)';
    EXECUTE 'CREATE INDEX ' || tablename || '_address ON ' ||
      tablename || ' (address)';
    EXECUTE 'CREATE INDEX ' || tablename || '_fingerprint ON ' ||
      tablename || ' (fingerprint)';
    EXECUTE 'CREATE INDEX ' || tablename || '_nickname ON ' ||
      tablename || ' (LOWER(nickname))';
    EXECUTE 'CREATE INDEX ' || tablename || '_validafter ON ' ||
      tablename || ' (validafter)';
    EXECUTE 'CREATE INDEX ' || tablename || '_descriptor ON ' ||
      tablename || ' (descriptor)';
    EXECUTE 'CREATE INDEX ' || tablename || '_validafter_date ON ' ||
      tablename || ' (DATE(validafter))';
  END IF;
  EXECUTE 'INSERT INTO ' || tablename || ' SELECT ($1).*' USING NEW;
  RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER insert_statusentry_trigger
  BEFORE INSERT ON statusentry
  FOR EACH ROW EXECUTE PROCEDURE statusentry_insert_trigger();

-- TABLE consensus
-- Contains all of the consensuses published by the directories.
CREATE TABLE consensus (
    validafter TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    rawdesc BYTEA NOT NULL,
    CONSTRAINT consensus_pkey PRIMARY KEY (validafter)
);

-- TABLE connbidirect
-- Contain conn-bi-direct stats strings
CREATE TABLE connbidirect (
    source CHARACTER(40) NOT NULL,
    statsend TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    seconds INTEGER NOT NULL,
    belownum BIGINT NOT NULL,
    readnum BIGINT NOT NULL,
    writenum BIGINT NOT NULL,
    bothnum BIGINT NOT NULL,
    CONSTRAINT connbidirect_pkey PRIMARY KEY (source, statsend)
);

-- TABLE network_size
CREATE TABLE network_size (
    date DATE NOT NULL,
    avg_running INTEGER NOT NULL,
    avg_exit INTEGER NOT NULL,
    avg_guard INTEGER NOT NULL,
    avg_fast INTEGER NOT NULL,
    avg_stable INTEGER NOT NULL,
    CONSTRAINT network_size_pkey PRIMARY KEY(date)
);

-- TABLE network_size_hour
CREATE TABLE network_size_hour (
    validafter TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    avg_running INTEGER NOT NULL,
    avg_exit INTEGER NOT NULL,
    avg_guard INTEGER NOT NULL,
    avg_fast INTEGER NOT NULL,
    avg_stable INTEGER NOT NULL,
    CONSTRAINT network_size_hour_pkey PRIMARY KEY(validafter)
);

-- TABLE relay_countries
CREATE TABLE relay_countries (
    date DATE NOT NULL,
    country CHARACTER(2) NOT NULL,
    relays INTEGER NOT NULL,
    CONSTRAINT relay_countries_pkey PRIMARY KEY(date, country)
);

-- TABLE relay_platforms
CREATE TABLE relay_platforms (
    date DATE NOT NULL,
    avg_linux INTEGER NOT NULL,
    avg_darwin INTEGER NOT NULL,
    avg_bsd INTEGER NOT NULL,
    avg_windows INTEGER NOT NULL,
    avg_other INTEGER NOT NULL,
    CONSTRAINT relay_platforms_pkey PRIMARY KEY(date)
);

-- TABLE relay_versions
CREATE TABLE relay_versions (
    date DATE NOT NULL,
    version CHARACTER(5) NOT NULL,
    relays INTEGER NOT NULL,
    CONSTRAINT relay_versions_pkey PRIMARY KEY(date, version)
);

-- TABLE total_bandwidth
-- Contains information for the whole network's total bandwidth which is
-- used in the bandwidth graphs.
CREATE TABLE total_bandwidth (
    date DATE NOT NULL,
    bwavg BIGINT NOT NULL,
    bwburst BIGINT NOT NULL,
    bwobserved BIGINT NOT NULL,
    bwadvertised BIGINT NOT NULL,
    CONSTRAINT total_bandwidth_pkey PRIMARY KEY(date)
);

-- TABLE total_bwhist
-- Contains the total number of read/written and the number of dir bytes
-- read/written by all relays in the network on a given day. The dir bytes
-- are an estimate based on the subset of relays that count dir bytes.
CREATE TABLE total_bwhist (
    date DATE NOT NULL,
    read BIGINT,
    written BIGINT,
    CONSTRAINT total_bwhist_pkey PRIMARY KEY(date)
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

-- GeoIP database that helps resolve IP addresses to country codes,
-- latitudes, and longitudes.
CREATE TABLE geoipdb (
  id SERIAL,
  ipstart INET,
  ipend INET,
  country CHARACTER(2) NOT NULL,
  latitude NUMERIC(7, 4) NOT NULL,
  longitude NUMERIC(7, 4) NOT NULL
);

-- Indexes to speed up looking up IP addresses in the GeoIP database.
CREATE INDEX geoip_ipstart ON geoipdb (ipstart);
CREATE INDEX geoip_ipend ON geoipdb (ipend);

-- Result type for GeoIP lookups that encapsulates the country code,
-- latitude, and longitude.
CREATE TYPE geoip_result AS (country CHARACTER(2),
  latitude NUMERIC(7, 4), longitude NUMERIC(7, 4));

-- GeoIP database lookup function.
CREATE OR REPLACE FUNCTION geoip_lookup (CHARACTER VARYING)
RETURNS geoip_result AS $$
  SELECT country, latitude, longitude FROM geoipdb
  WHERE INET($1)
  BETWEEN geoipdb.ipstart AND geoipdb.ipend LIMIT 1;
$$ LANGUAGE SQL;

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

-- FUNCTION refresh_network_size()
CREATE OR REPLACE FUNCTION refresh_network_size() RETURNS INTEGER AS $$
    DECLARE
        min_date TIMESTAMP WITHOUT TIME ZONE;
        max_date TIMESTAMP WITHOUT TIME ZONE;
    BEGIN

    min_date := (SELECT MIN(date) FROM updates);
    max_date := (SELECT MAX(date) + 1 FROM updates);

    DELETE FROM network_size
    WHERE date IN (SELECT date FROM updates);

    EXECUTE '
        INSERT INTO network_size
        (date, avg_running, avg_exit, avg_guard, avg_fast, avg_stable)
        SELECT date,
            isrunning / count AS avg_running,
            isexit / count AS avg_exit,
            isguard / count AS avg_guard,
            isfast / count AS avg_fast,
            isstable / count AS avg_stable
        FROM (
            SELECT DATE(validafter) AS date,
                COUNT(*) AS isrunning,
                COUNT(NULLIF(isexit, FALSE)) AS isexit,
                COUNT(NULLIF(isguard, FALSE)) AS isguard,
                COUNT(NULLIF(isfast, FALSE)) AS isfast,
                COUNT(NULLIF(isstable, FALSE)) AS isstable
            FROM statusentry
            WHERE isrunning = TRUE
              AND validafter >= ''' || min_date || '''
              AND validafter < ''' || max_date || '''
              AND DATE(validafter) IN (SELECT date FROM updates)
            GROUP BY DATE(validafter)
            ) b
        NATURAL JOIN relay_statuses_per_day';

    RETURN 1;
    END;
$$ LANGUAGE plpgsql;

-- FUNCTION refresh_network_size_hour()
CREATE OR REPLACE FUNCTION refresh_network_size_hour() RETURNS INTEGER AS $$
    DECLARE
        min_date TIMESTAMP WITHOUT TIME ZONE;
        max_date TIMESTAMP WITHOUT TIME ZONE;
    BEGIN

    min_date := (SELECT MIN(date) FROM updates);
    max_date := (SELECT MAX(date) + 1 FROM updates);

    DELETE FROM network_size_hour
    WHERE DATE(validafter) IN (SELECT date FROM updates);

    EXECUTE '
    INSERT INTO network_size_hour
    (validafter, avg_running, avg_exit, avg_guard, avg_fast, avg_stable)
    SELECT validafter, COUNT(*) AS avg_running,
    COUNT(NULLIF(isexit, FALSE)) AS avg_exit,
    COUNT(NULLIF(isguard, FALSE)) AS avg_guard,
    COUNT(NULLIF(isfast, FALSE)) AS avg_fast,
    COUNT(NULLIF(isstable, FALSE)) AS avg_stable
    FROM statusentry
    WHERE isrunning = TRUE
    AND validafter >= ''' || min_date || '''
    AND validafter < ''' || max_date || '''
    AND DATE(validafter) IN (SELECT date FROM updates)
    GROUP BY validafter';

    RETURN 1;
    END;
$$ LANGUAGE plpgsql;

-- FUNCTION refresh_relay_countries()
CREATE OR REPLACE FUNCTION refresh_relay_countries() RETURNS INTEGER AS $$
    DECLARE
        min_date TIMESTAMP WITHOUT TIME ZONE;
        max_date TIMESTAMP WITHOUT TIME ZONE;
    BEGIN

    min_date := (SELECT MIN(date) FROM updates);
    max_date := (SELECT MAX(date) + 1 FROM updates);

    DELETE FROM relay_countries
    WHERE date IN (SELECT date FROM updates);

    EXECUTE '
    INSERT INTO relay_countries
    (date, country, relays)
    SELECT date, country, relays / count AS relays
    FROM (
        SELECT date,
               COALESCE(lower((geoip_lookup(address)).country), ''zz'')
                 AS country,
               SUM(relays) AS relays
        FROM (
            SELECT DATE(validafter) AS date,
                   fingerprint,
                   address,
                   COUNT(*) AS relays
            FROM statusentry
            WHERE isrunning = TRUE
                  AND validafter >= ''' || min_date || '''
                  AND validafter < ''' || max_date || '''
                  AND DATE(validafter) IN (SELECT date FROM updates)
            GROUP BY 1, 2, 3
            ) c
        GROUP BY 1, 2
        ) b
    NATURAL JOIN relay_statuses_per_day';

    RETURN 1;
    END;
$$ LANGUAGE plpgsql;

-- FUNCTION refresh_relay_platforms()
CREATE OR REPLACE FUNCTION refresh_relay_platforms() RETURNS INTEGER AS $$
    DECLARE
        min_date TIMESTAMP WITHOUT TIME ZONE;
        max_date TIMESTAMP WITHOUT TIME ZONE;
    BEGIN

    min_date := (SELECT MIN(date) FROM updates);
    max_date := (SELECT MAX(date) + 1 FROM updates);

    DELETE FROM relay_platforms
    WHERE date IN (SELECT date FROM updates);

    EXECUTE '
    INSERT INTO relay_platforms
    (date, avg_linux, avg_darwin, avg_bsd, avg_windows, avg_other)
    SELECT date,
        linux / count AS avg_linux,
        darwin / count AS avg_darwin,
        bsd / count AS avg_bsd,
        windows / count AS avg_windows,
        other / count AS avg_other
    FROM (
        SELECT DATE(validafter) AS date,
            SUM(CASE WHEN platform LIKE ''%Linux%'' THEN 1 ELSE 0 END)
                AS linux,
            SUM(CASE WHEN platform LIKE ''%Darwin%'' THEN 1 ELSE 0 END)
                AS darwin,
            SUM(CASE WHEN platform LIKE ''%BSD%'' THEN 1 ELSE 0 END)
                AS bsd,
            SUM(CASE WHEN platform LIKE ''%Windows%'' THEN 1 ELSE 0 END)
                AS windows,
            SUM(CASE WHEN platform NOT LIKE ''%Windows%''
                AND platform NOT LIKE ''%Darwin%''
                AND platform NOT LIKE ''%BSD%''
                AND platform NOT LIKE ''%Linux%'' THEN 1 ELSE 0 END)
                AS other
        FROM descriptor
        RIGHT JOIN statusentry
        ON statusentry.descriptor = descriptor.descriptor
        WHERE isrunning = TRUE
          AND validafter >= ''' || min_date || '''
          AND validafter < ''' || max_date || '''
          AND DATE(validafter) IN (SELECT date FROM updates)
        GROUP BY DATE(validafter)
        ) b
    NATURAL JOIN relay_statuses_per_day';

    RETURN 1;
    END;
$$ LANGUAGE plpgsql;

-- FUNCTION refresh_relay_versions()
CREATE OR REPLACE FUNCTION refresh_relay_versions() RETURNS INTEGER AS $$
    DECLARE
        min_date TIMESTAMP WITHOUT TIME ZONE;
        max_date TIMESTAMP WITHOUT TIME ZONE;
    BEGIN

    min_date := (SELECT MIN(date) FROM updates);
    max_date := (SELECT MAX(date) + 1 FROM updates);

    DELETE FROM relay_versions
    WHERE date IN (SELECT date FROM updates);

    EXECUTE '
    INSERT INTO relay_versions
    (date, version, relays)
    SELECT date, version, relays / count AS relays
    FROM (
        SELECT DATE(validafter), SUBSTRING(platform, 5, 5) AS version,
               COUNT(*) AS relays
        FROM descriptor RIGHT JOIN statusentry
        ON descriptor.descriptor = statusentry.descriptor
        WHERE isrunning = TRUE
              AND platform IS NOT NULL
              AND validafter >= ''' || min_date || '''
              AND validafter < ''' || max_date || '''
              AND DATE(validafter) IN (SELECT date FROM updates)
        GROUP BY 1, 2
        ) b
    NATURAL JOIN relay_statuses_per_day';

    RETURN 1;
    END;
$$ LANGUAGE plpgsql;

-- FUNCTION refresh_total_bandwidth()
-- This keeps the table total_bandwidth up-to-date when necessary.
CREATE OR REPLACE FUNCTION refresh_total_bandwidth() RETURNS INTEGER AS $$
    DECLARE
        min_date TIMESTAMP WITHOUT TIME ZONE;
        max_date TIMESTAMP WITHOUT TIME ZONE;
    BEGIN

    min_date := (SELECT MIN(date) FROM updates);
    max_date := (SELECT MAX(date) + 1 FROM updates);

    DELETE FROM total_bandwidth
    WHERE date IN (SELECT date FROM updates);

    EXECUTE '
    INSERT INTO total_bandwidth
    (bwavg, bwburst, bwobserved, bwadvertised, date)
    SELECT (SUM(bandwidthavg)
            / relay_statuses_per_day.count)::BIGINT AS bwavg,
        (SUM(bandwidthburst)
            / relay_statuses_per_day.count)::BIGINT AS bwburst,
        (SUM(bandwidthobserved)
            / relay_statuses_per_day.count)::BIGINT AS bwobserved,
        (SUM(LEAST(bandwidthavg, bandwidthobserved))
            / relay_statuses_per_day.count)::BIGINT AS bwadvertised,
        DATE(validafter)
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
    GROUP BY DATE(validafter), relay_statuses_per_day.count';

    RETURN 1;
    END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION refresh_total_bwhist() RETURNS INTEGER AS $$
  BEGIN
  DELETE FROM total_bwhist WHERE date IN (SELECT date FROM updates);
  INSERT INTO total_bwhist (date, read, written)
  SELECT date, SUM(read_sum) AS read, SUM(written_sum) AS written
  FROM bwhist
  WHERE date >= (SELECT MIN(date) FROM updates)
  AND date <= (SELECT MAX(date) FROM updates)
  AND date IN (SELECT date FROM updates)
  GROUP BY date;
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
  INSERT INTO user_stats (date, country, r, dw, dr, drw, drr, bw, br, bwd,
      brd, bwr, brr, bwdr, brdr, bwp, brp, bwn, brn)
  SELECT
         -- We want to learn about total requests by date and country.
         dirreq_stats_by_country.date AS date,
         dirreq_stats_by_country.country AS country,
         dirreq_stats_by_country.r AS r,
         -- In order to weight the reported directory requests, we are
         -- counting bytes of relays (except directory authorities)
         -- matching certain criteria: whether or not they are reporting
         -- directory requests, whether or not they are reporting
         -- directory bytes, and whether their directory port is open or
         -- closed.
         SUM(CASE WHEN authority IS NOT NULL
           THEN NULL ELSE dirwritten END) AS dw,
         SUM(CASE WHEN authority IS NOT NULL
           THEN NULL ELSE dirread END) AS dr,
         SUM(CASE WHEN requests IS NULL OR authority IS NOT NULL
           THEN NULL ELSE dirwritten END) AS dwr,
         SUM(CASE WHEN requests IS NULL OR authority IS NOT NULL
           THEN NULL ELSE dirread END) AS drr,
         SUM(CASE WHEN authority IS NOT NULL
           THEN NULL ELSE written END) AS bw,
         SUM(CASE WHEN authority IS NOT NULL
           THEN NULL ELSE read END) AS br,
         SUM(CASE WHEN dirwritten = 0 OR authority IS NOT NULL
           THEN NULL ELSE written END) AS bwd,
         SUM(CASE WHEN dirwritten = 0 OR authority IS NOT NULL
           THEN NULL ELSE read END) AS brd,
         SUM(CASE WHEN requests IS NULL OR authority IS NOT NULL
           THEN NULL ELSE written END) AS bwr,
         SUM(CASE WHEN requests IS NULL OR authority IS NOT NULL
           THEN NULL ELSE read END) AS brr,
         SUM(CASE WHEN dirwritten = 0 OR requests IS NULL
           OR authority IS NOT NULL THEN NULL ELSE written END) AS bwdr,
         SUM(CASE WHEN dirwritten = 0 OR requests IS NULL
           OR authority IS NOT NULL THEN NULL ELSE read END) AS brdr,
         SUM(CASE WHEN opendirport IS NULL OR authority IS NOT NULL
           THEN NULL ELSE written END) AS bwp,
         SUM(CASE WHEN opendirport IS NULL OR authority IS NOT NULL
           THEN NULL ELSE read END) AS brp,
         SUM(CASE WHEN opendirport IS NOT NULL OR authority IS NOT NULL
           THEN NULL ELSE written END) AS bwn,
         SUM(CASE WHEN opendirport IS NOT NULL OR authority IS NOT NULL
           THEN NULL ELSE read END) AS brn
  FROM (
    -- The first sub-select tells us the total number of directory
    -- requests per country reported by all directory mirrors.
    SELECT dirreq_stats_by_date.date AS date, country, SUM(requests) AS r
    FROM (
      SELECT fingerprint, date, country, SUM(requests) AS requests
      FROM (
        -- There are two selects here, because in most cases the directory
        -- request statistics cover two calendar dates.
        SELECT LOWER(source) AS fingerprint, DATE(statsend) AS date,
          country, FLOOR(requests * (CASE
          WHEN EXTRACT(EPOCH FROM DATE(statsend)) >
          EXTRACT(EPOCH FROM statsend) - seconds
          THEN EXTRACT(EPOCH FROM statsend) -
          EXTRACT(EPOCH FROM DATE(statsend))
          ELSE seconds END) / seconds) AS requests
        FROM dirreq_stats
        UNION
        SELECT LOWER(source) AS fingerprint, DATE(statsend) - 1 AS date,
          country, FLOOR(requests *
          (EXTRACT(EPOCH FROM DATE(statsend)) -
          EXTRACT(EPOCH FROM statsend) + seconds)
          / seconds) AS requests
        FROM dirreq_stats
        WHERE EXTRACT(EPOCH FROM DATE(statsend)) -
        EXTRACT(EPOCH FROM statsend) + seconds > 0
      ) dirreq_stats_split
      GROUP BY 1, 2, 3
    ) dirreq_stats_by_date
    -- We are only interested in requests by directory mirrors, not
    -- directory authorities, so we exclude all relays with the Authority
    -- flag.
    RIGHT JOIN (
      SELECT fingerprint, DATE(validafter) AS date
      FROM statusentry
      WHERE validafter >= ''' || min_date || '''
      AND validafter < ''' || max_date || '''
      AND DATE(validafter) IN (SELECT date FROM updates)
      AND isauthority IS FALSE
      GROUP BY 1, 2
    ) statusentry_dirmirrors
    ON dirreq_stats_by_date.fingerprint =
       statusentry_dirmirrors.fingerprint
    AND dirreq_stats_by_date.date = statusentry_dirmirrors.date
    GROUP BY 1, 2
  ) dirreq_stats_by_country
  LEFT JOIN (
    -- In the next step, we expand the result by bandwidth histories of
    -- all relays.
    SELECT fingerprint, date, read_sum AS read, written_sum AS written,
           dirread_sum AS dirread, dirwritten_sum AS dirwritten
    FROM bwhist
    WHERE date >= ''' || min_date || '''
    AND date < ''' || max_date || '''
    AND date IN (SELECT date FROM updates)
  ) bwhist_by_relay
  ON dirreq_stats_by_country.date = bwhist_by_relay.date
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
  LEFT JOIN (
    -- For each relay, tell if it has reported directory request
    -- statistics on a given date. Again, we have to take into account
    -- that statistics intervals cover more than one calendar date in most
    -- cases. The exact number of requests is not relevant here, but only
    -- whether the relay reported directory requests or not.
    SELECT fingerprint, date, 1 AS requests
    FROM (
      SELECT LOWER(source) AS fingerprint, DATE(statsend) AS date
      FROM dirreq_stats
      WHERE DATE(statsend) >= ''' || min_date || '''
      AND DATE(statsend) < ''' || max_date || '''
      AND DATE(statsend) IN (SELECT date FROM updates)
      AND country = ''zy''
      UNION
      SELECT LOWER(source) AS fingerprint, DATE(statsend) - 1 AS date
      FROM dirreq_stats
      WHERE DATE(statsend) - 1 >= ''' || min_date || '''
      AND DATE(statsend) - 1 < ''' || max_date || '''
      AND DATE(statsend) IN (SELECT date FROM updates)
      AND country = ''zy''
      AND EXTRACT(EPOCH FROM DATE(statsend)) -
      EXTRACT(EPOCH FROM statsend) + seconds > 0
    ) dirreq_stats_split
    GROUP BY 1, 2
  ) dirreq_stats_by_relay
  ON bwhist_by_relay.fingerprint = dirreq_stats_by_relay.fingerprint
  AND bwhist_by_relay.date = dirreq_stats_by_relay.date
  WHERE dirreq_stats_by_country.country IS NOT NULL
  -- Group by date, country, and total reported directory requests,
  -- summing up the bandwidth histories.
  GROUP BY 1, 2, 3';
  RETURN 1;
  END;
$$ LANGUAGE plpgsql;

-- non-relay statistics
-- The following tables contain pre-aggregated statistics that are not
-- based on relay descriptors or that are not yet derived from the relay
-- descriptors in the database.

-- TABLE bridge_network_size
-- Contains average number of running bridges.
CREATE TABLE bridge_network_size (
    "date" DATE NOT NULL,
    avg_running INTEGER NOT NULL,
    CONSTRAINT bridge_network_size_pkey PRIMARY KEY(date)
);

-- TABLE dirreq_stats
-- Contains daily users by country.
CREATE TABLE dirreq_stats (
    source CHARACTER(40) NOT NULL,
    statsend TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    seconds INTEGER NOT NULL,
    country CHARACTER(2) NOT NULL,
    requests INTEGER NOT NULL,
    CONSTRAINT dirreq_stats_pkey
    PRIMARY KEY (source, statsend, seconds, country)
);

-- TABLE bridge_stats
-- Contains daily bridge users by country.
CREATE TABLE bridge_stats (
    "date" DATE NOT NULL,
    country CHARACTER(2) NOT NULL,
    users INTEGER NOT NULL,
    CONSTRAINT bridge_stats_pkey PRIMARY KEY ("date", country)
);

-- TABLE torperf_stats
-- Quantiles and medians of daily torperf results.
CREATE TABLE torperf_stats (
    "date" DATE NOT NULL,
    source CHARACTER VARYING(32) NOT NULL,
    q1 INTEGER NOT NULL,
    md INTEGER NOT NULL,
    q3 INTEGER NOT NULL,
    timeouts INTEGER NOT NULL,
    failures INTEGER NOT NULL,
    requests INTEGER NOT NULL,
    CONSTRAINT torperf_stats_pkey PRIMARY KEY("date", source)
);

-- TABLE gettor_stats
-- Packages requested from GetTor
CREATE TABLE gettor_stats (
    "date" DATE NOT NULL,
    bundle CHARACTER VARYING(64) NOT NULL,
    downloads INTEGER NOT NULL,
    CONSTRAINT gettor_stats_pkey PRIMARY KEY("date", bundle)
);

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
    RAISE NOTICE '% Refreshing network size.', timeofday();
    PERFORM refresh_network_size();
    RAISE NOTICE '% Refreshing hourly network size.', timeofday();
    PERFORM refresh_network_size_hour();
    RAISE NOTICE '% Refreshing relays by country.', timeofday();
    PERFORM refresh_relay_countries();
    RAISE NOTICE '% Refreshing relay platforms.', timeofday();
    PERFORM refresh_relay_platforms();
    RAISE NOTICE '% Refreshing relay versions.', timeofday();
    PERFORM refresh_relay_versions();
    RAISE NOTICE '% Refreshing total relay bandwidth.', timeofday();
    PERFORM refresh_total_bandwidth();
    RAISE NOTICE '% Refreshing relay bandwidth history.', timeofday();
    PERFORM refresh_total_bwhist();
    RAISE NOTICE '% Refreshing bandwidth history by flags.', timeofday();
    PERFORM refresh_bwhist_flags();
    RAISE NOTICE '% Refreshing user statistics.', timeofday();
    PERFORM refresh_user_stats();
    RAISE NOTICE '% Deleting processed dates.', timeofday();
    DELETE FROM scheduled_updates WHERE id IN (SELECT id FROM updates);
    RAISE NOTICE '% Terminating refresh run.', timeofday();
  RETURN 1;
  END;
$$ LANGUAGE plpgsql;

