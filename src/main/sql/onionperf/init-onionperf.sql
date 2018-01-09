-- Copyright 2017--2018 The Tor Project
-- See LICENSE for licensing information

CREATE TABLE IF NOT EXISTS measurements (
  measurement_id SERIAL PRIMARY KEY,
  source CHARACTER VARYING(32) NOT NULL,
  filesize INTEGER NOT NULL,
  start TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  socket INTEGER,
  connect INTEGER,
  negotiate INTEGER,
  request INTEGER,
  response INTEGER,
  datarequest INTEGER,
  dataresponse INTEGER,
  datacomplete INTEGER,
  writebytes INTEGER,
  readbytes INTEGER,
  didtimeout BOOLEAN,
  dataperc0 INTEGER,
  dataperc10 INTEGER,
  dataperc20 INTEGER,
  dataperc30 INTEGER,
  dataperc40 INTEGER,
  dataperc50 INTEGER,
  dataperc60 INTEGER,
  dataperc70 INTEGER,
  dataperc80 INTEGER,
  dataperc90 INTEGER,
  dataperc100 INTEGER,
  launch TIMESTAMP WITHOUT TIME ZONE,
  used_at TIMESTAMP WITHOUT TIME ZONE,
  timeout INTEGER,
  quantile REAL,
  circ_id INTEGER,
  used_by INTEGER,
  endpointlocal CHARACTER VARYING(64),
  endpointproxy CHARACTER VARYING(64),
  endpointremote CHARACTER VARYING(64),
  hostnamelocal CHARACTER VARYING(64),
  hostnameremote CHARACTER VARYING(64),
  sourceaddress CHARACTER VARYING(64),
  UNIQUE (source, filesize, start)
);

CREATE TYPE server AS ENUM ('public', 'onion');

CREATE OR REPLACE VIEW onionperf AS
SELECT date,
  filesize,
  source,
  server,
  q[1] AS q1,
  q[2] AS md,
  q[3] AS q3,
  timeouts,
  failures,
  requests
FROM (
SELECT DATE(start) AS date,
  filesize,
  source,
  CASE WHEN endpointremote LIKE '%.onion%' THEN 'onion'
    ELSE 'public' END AS server,
  PERCENTILE_CONT(ARRAY[0.25,0.5,0.75]) WITHIN GROUP(ORDER BY datacomplete) AS q,
  COUNT(CASE WHEN didtimeout OR datacomplete < 1 THEN 1 ELSE NULL END)
    AS timeouts,
  COUNT(CASE WHEN NOT didtimeout AND datacomplete >= 1
    AND readbytes < filesize THEN 1 ELSE NULL END) AS failures,
  COUNT(CASE WHEN NOT didtimeout AND datacomplete >= 1
    AND readbytes >= filesize then 1 else null end) AS requests
FROM measurements
GROUP BY date, filesize, source, server
UNION
SELECT DATE(start) AS date,
  filesize,
  '' AS source,
  CASE WHEN endpointremote LIKE '%.onion%' THEN 'onion'
    ELSE 'public' END AS server,
  PERCENTILE_CONT(ARRAY[0.25,0.5,0.75]) WITHIN GROUP(ORDER BY datacomplete) AS q,
  COUNT(CASE WHEN didtimeout OR datacomplete < 1 THEN 1 ELSE NULL END)
    AS timeouts,
  COUNT(CASE WHEN NOT didtimeout AND datacomplete >= 1
    AND readbytes < filesize THEN 1 ELSE NULL END) AS failures,
  COUNT(CASE WHEN NOT didtimeout AND datacomplete >= 1
    AND readbytes >= filesize then 1 else null end) AS requests
FROM measurements
GROUP BY date, filesize, 3, server) sub
ORDER BY date, filesize, source, server;

