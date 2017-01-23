-- Copyright 2016--2017 The Tor Project
-- See LICENSE for licensing information

CREATE TYPE method AS ENUM ('GET', 'HEAD');

CREATE TABLE files (
  file_id SERIAL PRIMARY KEY,
  url CHARACTER VARYING(2048) UNIQUE NOT NULL,
  server CHARACTER VARYING(32) NOT NULL,
  site CHARACTER VARYING(128) NOT NULL,
  log_date DATE NOT NULL,
  UNIQUE (server, site, log_date)
);

CREATE TABLE resources (
  resource_id SERIAL PRIMARY KEY,
  resource_string CHARACTER VARYING(2048) UNIQUE NOT NULL
);

CREATE TABLE requests (
  file_id INTEGER REFERENCES files (file_id) NOT NULL,
  method METHOD NOT NULL,
  resource_id INTEGER REFERENCES resources (resource_id) NOT NULL,
  response_code SMALLINT NOT NULL,
  count INTEGER NOT NULL,
  UNIQUE (file_id, method, resource_id, response_code)
);

CREATE OR REPLACE VIEW webstats AS
  SELECT log_date,
    CASE WHEN resource_string LIKE '%.asc' THEN 'tbsd'
      ELSE 'tbid' END AS request_type,
    CASE WHEN resource_string LIKE '%.exe%' THEN 'w'
      WHEN resource_string LIKE '%.dmg%' THEN 'm'
      WHEN resource_string LIKE '%.tar.xz%' THEN 'l'
      ELSE 'o' END AS platform,
    CASE WHEN resource_string LIKE '%-hardened%' THEN 'h'
      WHEN resource_string LIKE '%/%.%a%/%' THEN 'a'
      ELSE 'r' END AS channel,
    COALESCE(SUBSTRING(resource_string
      FROM '.*_([a-zA-Z]{2}|[a-zA-Z]{2}-[a-zA-Z]{2})[\._-].*'), '??') AS locale,
    NULL::BOOLEAN AS incremental,
    SUM(count) AS count
  FROM files NATURAL JOIN requests NATURAL JOIN resources
  WHERE (resource_string LIKE '%/torbrowser/%.exe'
    OR resource_string LIKE '%/torbrowser/%.dmg'
    OR resource_string LIKE '%/torbrowser/%.tar.xz'
    OR resource_string LIKE '%/torbrowser/%.exe.asc'
    OR resource_string LIKE '%/torbrowser/%.dmg.asc'
    OR resource_string LIKE '%/torbrowser/%.tar.xz.asc')
  AND response_code = 200
  AND method = 'GET'
  GROUP BY log_date, request_type, platform, channel, locale, incremental
  UNION
  SELECT log_date,
    'tbup' AS request_type,
    CASE WHEN resource_string LIKE '%/WINNT%' THEN 'w'
      WHEN resource_string LIKE '%/Darwin%' THEN 'm'
      ELSE 'l' END AS platform,
    CASE WHEN resource_string LIKE '%/hardened/%' THEN 'h'
      WHEN resource_string LIKE '%/alpha/%' THEN 'a'
      WHEN resource_string LIKE '%/release/%' THEN 'r'
      ELSE 'o' END AS channel,
    COALESCE(SUBSTRING(resource_string
      FROM '.*/([a-zA-Z]{2}|[a-zA-Z]{2}-[a-zA-Z]{2})\??$'), '??') AS locale,
    NULL::BOOLEAN AS incremental,
    SUM(count) AS count
  FROM files NATURAL JOIN requests NATURAL JOIN resources
  WHERE resource_string LIKE '%/torbrowser/update_2/%'
  AND resource_string NOT LIKE '%.xml'
  AND response_code = 200
  AND method = 'GET'
  GROUP BY log_date, request_type, platform, channel, locale, incremental
  UNION
  SELECT log_date,
    'tbur' AS request_type,
    CASE WHEN resource_string LIKE '%-win32-%' THEN 'w'
      WHEN resource_string LIKE '%-osx%' THEN 'm'
      ELSE 'l' END AS platform,
    CASE WHEN resource_string LIKE '%-hardened%' THEN 'h'
      WHEN resource_string LIKE '%/%.%a%/%' THEN 'a'
      ELSE 'r' END AS channel,
    COALESCE(SUBSTRING(resource_string
      FROM '.*_([a-zA-Z]{2}|[a-zA-Z]{2}-[a-zA-Z]{2})[\._-].*'), '??') AS locale,
    CASE WHEN resource_string LIKE '%.incremental.%' THEN TRUE
      ELSE FALSE END AS incremental,
    SUM(count) AS count
  FROM files NATURAL JOIN requests NATURAL JOIN resources
  WHERE resource_string LIKE '%/torbrowser/%.mar'
  AND response_code = 302
  AND method = 'GET'
  GROUP BY log_date, request_type, platform, channel, locale, incremental
  UNION
  SELECT log_date,
    'tmid' AS request_type,
    CASE WHEN resource_string LIKE '%.exe' THEN 'w'
      WHEN resource_string LIKE '%.dmg' THEN 'm'
      WHEN resource_string LIKE '%.tar.xz' THEN 'l'
      ELSE 'o' END AS platform,
    NULL AS channel,
    COALESCE(SUBSTRING(resource_string
      FROM '.*_([a-zA-Z]{2}|[a-zA-Z]{2}-[a-zA-Z]{2})[\._-].*'), '??') AS locale,
    NULL::BOOLEAN AS incremental,
    SUM(count) AS count
  FROM files NATURAL JOIN requests NATURAL JOIN resources
  WHERE (resource_string LIKE '%/tormessenger/%.exe'
    OR resource_string LIKE '%/tormessenger/%.dmg'
    OR resource_string LIKE '%/tormessenger/%.tar.xz')
  AND response_code = 200
  AND method = 'GET'
  GROUP BY log_date, request_type, platform, channel, locale, incremental
  UNION
  SELECT log_date,
    'tmup' AS request_type,
    CASE WHEN resource_string LIKE '%/WINNT%' THEN 'w'
      WHEN resource_string LIKE '%/Darwin%' THEN 'm'
      WHEN resource_string LIKE '%/Linux%' THEN 'l'
      ELSE 'o' END AS platform,
    NULL AS channel,
    COALESCE(SUBSTRING(resource_string
      FROM '.*/([a-zA-Z]{2}|[a-zA-Z]{2}-[a-zA-Z]{2})\??$'), '??') AS locale,
    NULL::BOOLEAN AS incremental,
    SUM(count) AS count
  FROM files NATURAL JOIN requests NATURAL JOIN resources
  WHERE resource_string LIKE '%/tormessenger/update_2/%'
  AND resource_string NOT LIKE '%.xml'
  AND resource_string NOT LIKE '%/'
  AND resource_string NOT LIKE '%/?'
  AND response_code = 200
  AND method = 'GET'
  GROUP BY log_date, request_type, platform, channel, locale, incremental
  UNION
  SELECT log_date,
    'twhph' AS request_type,
    NULL AS platform,
    NULL AS channel,
    NULL AS locale,
    NULL::BOOLEAN AS incremental,
    SUM(count) AS count
  FROM files NATURAL JOIN requests NATURAL JOIN resources
  WHERE (resource_string = '/'
    OR resource_string LIKE '/index%')
  AND response_code = 200
  AND (site = 'torproject.org'
    OR site = 'www.torproject.org')
  AND method = 'GET'
  GROUP BY log_date, request_type, platform, channel, locale, incremental
  UNION
  SELECT log_date,
    'twdph' AS request_type,
    NULL AS platform,
    NULL AS channel,
    NULL AS locale,
    NULL::BOOLEAN AS incremental,
    SUM(count) AS count
  FROM files NATURAL JOIN requests NATURAL JOIN resources
  WHERE (resource_string LIKE '/download/download%'
    OR resource_string LIKE '/projects/torbrowser.html%')
  AND response_code = 200
  AND (site = 'torproject.org'
    OR site = 'www.torproject.org')
  AND method = 'GET'
  GROUP BY log_date, request_type, platform, channel, locale, incremental;

