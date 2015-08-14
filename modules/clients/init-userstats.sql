-- Copyright 2013 The Tor Project
-- See LICENSE for licensing information

-- Use enum types for dimensions that may only change if we write new code
-- to support them.  For example, if there's a new node type beyond relay
-- and bridge, we'll have to write code to support it.  This is in
-- contrast to dimensions like country, transport, or version which don't
-- have their possible values hard-coded anywhere.
CREATE TYPE node AS ENUM ('relay', 'bridge');
CREATE TYPE metric AS ENUM ('responses', 'bytes', 'status');

-- All new data first goes into the imported table.  The import tool
-- should do some trivial checks for invalid or duplicate data, but
-- ultimately, we're going to do these checks in the database.  For
-- example, the import tool could avoid importing data from the same
-- descriptor more than once, but it's fine to import the same history
-- string from distinct descriptors multiple times.  The import tool must,
-- however, make sure that stats_end is not greater than 00:00:00 of the
-- day following stats_start.  There are no constraints set on this table,
-- because importing data should be really, really fast.  Once the newly
-- imported data is successfully processed, the imported table is emptied.
CREATE TABLE imported (

  -- The 40-character upper-case hex string identifies a descriptor
  -- uniquely and is used to join metrics (responses, bytes, status)
  -- published by the same node (relay or bridge).
  fingerprint CHARACTER(40) NOT NULL,

  -- The node type is used to decide the statistics that this entry will
  -- be part of.
  node node NOT NULL,

  -- The metric of this entry describes the stored observation type.
  -- We'll want to store different metrics published by a node:
  -- - 'responses' are the number of v3 network status consensus requests
  --   that the node responded to;
  -- - 'bytes' are the number of bytes that the node wrote when answering
  --   directory requests;
  -- - 'status' are the intervals when the node was listed as running in
  --   the network status published by either the directory authorities or
  --   bridge authority.
  metric metric NOT NULL,

  -- The two-letter lower-case country code that the observation in this
  -- entry can be attributed to; can be '??' if no country information is
  -- known for this entry, or '' (empty string) if this entry summarizes
  -- observations for all countries.
  country CHARACTER VARYING(2) NOT NULL,

  -- The pluggable transport name that the observation in this entry can
  -- be attributed to; can be '<OR>' if no pluggable transport was used,
  -- '<??>' if an unknown pluggable transport was used, or '' (empty
  -- string) if this entry summarizes observations for all transports.
  transport CHARACTER VARYING(20) NOT NULL,

  -- The IP address version that the observation in this entry can be
  -- attributed to; can be 'v4' or 'v6' or '' (empty string) if this entry
  -- summarizes observations for all IP address versions.
  version CHARACTER VARYING(2) NOT NULL,

  -- The interval start of this observation.
  stats_start TIMESTAMP WITHOUT TIME ZONE NOT NULL,

  -- The interval end of this observation.  This timestamp must be greater
  -- than stats_start and must not be greater than 00:00:00 of the day
  -- following stats_start, which the import tool must make sure.
  stats_end TIMESTAMP WITHOUT TIME ZONE NOT NULL,

  -- Finally, the observed value.
  val DOUBLE PRECISION NOT NULL
);

-- After importing new data into the imported table, they are merged into
-- the merged table using the merge() function.  The merged table contains
-- the same data as the imported table, except:
-- (1) there are no duplicate or overlapping entries in the merged table
--     with respect to stats_start and stats_end and the same fingerprint,
--     node, metric, country, transport, and version columns;
-- (2) all subsequent intervals with the same node, metric, country,
--     transport, version, and stats_start date are compressed into a
--     single entry.
CREATE TABLE merged (

  -- The unique key that is only used when merging newly imported data
  -- into this table.
  id SERIAL PRIMARY KEY,

  -- All other columns have the same meaning as in the imported table.
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

-- After merging new data into the merged table, they are aggregated to
-- daily user number estimates using the aggregate() function.  Only dates
-- with new data in the imported table will be recomputed in the
-- aggregated table.  The aggregated components follow the algorithm
-- proposed in Tor Tech Report 2012-10-001.
CREATE TABLE aggregated (

  -- The date of these aggregated observations.
  date DATE NOT NULL,

  -- The node, country, transport, and version columns all have the same
  -- meaning as in the imported table.
  node node NOT NULL,
  country CHARACTER VARYING(2) NOT NULL DEFAULT '',
  transport CHARACTER VARYING(20) NOT NULL DEFAULT '',
  version CHARACTER VARYING(2) NOT NULL DEFAULT '',

  -- Total number of reported responses, possibly broken down by country,
  -- transport, or version if either of them is not ''.  See r(R) in the
  -- tech report.
  rrx DOUBLE PRECISION NOT NULL DEFAULT 0,

  -- Total number of seconds of nodes reporting responses, possibly broken
  -- down by country, transport, or version if either of them is not ''.
  -- This would be referred to as n(R) in the tech report, though it's not
  -- used there.
  nrx DOUBLE PRECISION NOT NULL DEFAULT 0,

  -- Total number of reported bytes.  See h(H) in the tech report.
  hh DOUBLE PRECISION NOT NULL DEFAULT 0,

  -- Total number of seconds of nodes in the status.  See n(N) in the tech
  -- report.
  nn DOUBLE PRECISION NOT NULL DEFAULT 0,

  -- Number of reported bytes of nodes that reported both responses and
  -- bytes.  See h(R intersect H) in the tech report.
  hrh DOUBLE PRECISION NOT NULL DEFAULT 0,

  -- Number of seconds of nodes reporting bytes.  See n(H) in the tech
  -- report.
  nh DOUBLE PRECISION NOT NULL DEFAULT 0,

  -- Number of seconds of nodes reporting responses but no bytes.  See
  -- n(R \ H) in the tech report.
  nrh DOUBLE PRECISION NOT NULL DEFAULT 0
);

CREATE LANGUAGE plpgsql;

-- Merge new entries from the imported table into the merged table, and
-- compress them while doing so.  This function first executes a query to
-- match all entries in the imported table with adjacent or even
-- overlapping entries in the merged table.  It then loops over query
-- results and either inserts or updates entries in the merged table.  The
-- idea is to leave query optimization to the database and only touch
-- as few entries as possible while running this function.
CREATE OR REPLACE FUNCTION merge() RETURNS VOID AS $$
DECLARE

  -- The current record that we're handling in the loop body.
  cur RECORD;

  -- Various information about the last record we processed, so that we
  -- can merge the current record with the last one if possible.
  last_fingerprint CHARACTER(40) := NULL;
  last_node node;
  last_metric metric;
  last_country CHARACTER VARYING(2);
  last_transport CHARACTER VARYING(20);
  last_version CHARACTER VARYING(2);
  last_start TIMESTAMP WITHOUT TIME ZONE;
  last_end TIMESTAMP WITHOUT TIME ZONE;
  last_id INTEGER;
  last_val DOUBLE PRECISION;

  -- Interval end and value of the last record before updating them in the
  -- last loop step.  In a few edge cases, we may update an entry and
  -- learn in the next loop step that the updated entry overlaps with the
  -- subsequent entry.  In these cases we'll have to undo the update,
  -- which is why we're storing the updated values.
  undo_end TIMESTAMP WITHOUT TIME ZONE;
  undo_val DOUBLE PRECISION;

BEGIN
  RAISE NOTICE '% Starting to merge.', timeofday();

  -- TODO Maybe we'll have to materialize a merged_part table that only
  -- contains dates IN (SELECT DISTINCT DATE(stats_start) FROM imported)
  -- and use that in the query below.

  -- Loop over results from a query that joins new entries in the imported
  -- table with existing entries in the merged table.
  FOR cur IN SELECT DISTINCT

    -- Select id, interval start and end, and value of the existing entry
    -- in merged; all these fields may be null if the imported entry is
    -- not adjacent to an existing one.
    merged.id AS merged_id,
    merged.stats_start AS merged_start,
    merged.stats_end AS merged_end,
    merged.val AS merged_val,

    -- Select interval start and end and value of the newly imported
    -- entry.
    imported.stats_start AS imported_start,
    imported.stats_end AS imported_end,
    imported.val AS imported_val,

    -- Select columns that define the group of entries that can be merged
    -- in the merged table.
    imported.fingerprint AS fingerprint,
    imported.node AS node,
    imported.metric AS metric,
    imported.country AS country,
    imported.transport AS transport,
    imported.version AS version

    -- Select these columns from all entries in the imported table, plus
    -- do an outer join on the merged table to find adjacent entries that
    -- we might want to merge the new entries with.  It's possible that we
    -- handle the same imported entry twice, if it starts directly after
    -- one existing entry and ends directly before another existing entry.
    FROM imported LEFT JOIN merged

    -- First two join conditions are to find adjacent intervals.  In fact,
    -- we also include overlapping intervals here, so that we can skip the
    -- overlapping entry in the imported table.
    ON imported.stats_end >= merged.stats_start AND
       imported.stats_start <= merged.stats_end AND

       -- Further join conditions are same date, fingerprint, node, etc.,
       -- so that we don't merge entries that don't belong together.
       DATE(imported.stats_start) = DATE(merged.stats_start) AND
       imported.fingerprint = merged.fingerprint AND
       imported.node = merged.node AND
       imported.metric = merged.metric AND
       imported.country = merged.country AND
       imported.transport = merged.transport AND
       imported.version = merged.version

    -- Ordering is key, or our approach to merge subsequent entries is
    -- going to break.
    ORDER BY imported.fingerprint, imported.node, imported.metric,
             imported.country, imported.transport, imported.version,
             imported.stats_start, merged.stats_start, imported.stats_end

  -- Now go through the results one by one.
  LOOP

    -- Log that we're done with the query and about to start merging.
    IF last_fingerprint IS NULL THEN
      RAISE NOTICE '% Query returned, now merging entries.', timeofday();
    END IF;

    -- If we're processing the very first entry or if we have reached a
    -- new group of entries that belong together, (re-)set last_*
    -- variables.
    IF last_fingerprint IS NULL OR
        DATE(cur.imported_start) <> DATE(last_start) OR
        cur.fingerprint <> last_fingerprint OR
        cur.node <> last_node OR
        cur.metric <> last_metric OR
        cur.country <> last_country OR
        cur.transport <> last_transport OR
        cur.version <> last_version THEN
      last_id := -1;
      last_start := '1970-01-01 00:00:00';
      last_end := '1970-01-01 00:00:00';
      last_val := -1;
    END IF;

    -- Remember all fields that determine the group of which entries
    -- belong together.
    last_fingerprint := cur.fingerprint;
    last_node := cur.node;
    last_metric := cur.metric;
    last_country := cur.country;
    last_transport := cur.transport;
    last_version := cur.version;

    -- If the existing entry that we're currently looking at starts before
    -- the previous entry ends, we have created two overlapping entries in
    -- the last iteration, and that is not allowed.  Undo the previous
    -- change.
    IF cur.merged_start IS NOT NULL AND
        cur.merged_start < last_end AND
        undo_end IS NOT NULL AND undo_val IS NOT NULL THEN
      UPDATE merged SET stats_end = undo_end, val = undo_val
        WHERE id = last_id;
      undo_end := NULL;
      undo_val := NULL;

    -- If there is no adjacent entry to the one we're about to merge,
    -- insert it as new entry.
    ELSIF cur.merged_end IS NULL THEN
      IF cur.imported_start > last_end THEN
        last_start := cur.imported_start;
        last_end := cur.imported_end;
        last_val := cur.imported_val;
        INSERT INTO merged (fingerprint, node, metric, country, transport,
                            version, stats_start, stats_end, val)
          VALUES (last_fingerprint, last_node, last_metric, last_country,
                  last_transport, last_version, last_start, last_end,
                  last_val)
          RETURNING id INTO last_id;

      -- If there was no adjacent entry before starting to merge, but
      -- there is now one ending right before the new entry starts, merge
      -- the new entry into the existing one.
      ELSIF cur.imported_start = last_end THEN
        last_val := last_val + cur.imported_val;
        last_end := cur.imported_end;
        UPDATE merged SET stats_end = last_end, val = last_val
          WHERE id = last_id;
      END IF;

      -- There's no risk of this entry overlapping with the next.
      undo_end := NULL;
      undo_val := NULL;

    -- If the new entry ends right when an existing entry starts, but
    -- there's a gap between when the previously processed entry ends and
    -- when the new entry starts, merge the new entry with the existing
    -- entry we're currently looking at.
    ELSIF cur.imported_end = cur.merged_start THEN
      IF cur.imported_start > last_end THEN
        last_id := cur.merged_id;
        last_start := cur.imported_start;
        last_end := cur.merged_end;
        last_val := cur.imported_val + cur.merged_val;
        UPDATE merged SET stats_start = last_start, val = last_val
          WHERE id = last_id;

      -- If the new entry ends right when an existing entry starts and
      -- there's no gap between when the previousl processed entry ends
      -- and when the new entry starts, merge the new entry with the other
      -- two entries.  This happens by deleting the previous entry and
      -- expanding the subsequent entry to cover all three entries.
      ELSIF cur.imported_start = last_end THEN
        DELETE FROM merged WHERE id = last_id;
        last_id := cur.merged_id;
        last_end := cur.merged_end;
        last_val := last_val + cur.merged_val;
        UPDATE merged SET stats_start = last_start, val = last_val
          WHERE id = last_id;
      END IF;

      -- There's no risk of this entry overlapping with the next.
      undo_end := NULL;
      undo_val := NULL;

    -- If the new entry starts right when an existing entry ends, but
    -- there's a gap between the previously processed entry and the
    -- existing one, extend the existing entry.  There's a special case
    -- when this operation is false and must be undone, which is when the
    -- newly added entry overlaps with the subsequent entry.  That's why
    -- we have to store the old interval end and value, so that this
    -- operation can be undone in the next loop iteration.
    ELSIF cur.imported_start = cur.merged_end THEN
      IF last_end < cur.imported_start THEN
        undo_end := cur.merged_end;
        undo_val := cur.merged_val;
        last_id := cur.merged_id;
        last_start := cur.merged_start;
        last_end := cur.imported_end;
        last_val := cur.merged_val + cur.imported_val;
        UPDATE merged SET stats_end = last_end, val = last_val
          WHERE id = last_id;

      -- If the new entry starts right when an existing entry ends and
      -- there's no gap between the previously processed entry and the
      -- existing entry, extend the existing entry.  This is very similar
      -- to the previous case.  The same reasoning about possibly having
      -- to undo this operation applies.
      ELSE
        undo_end := cur.merged_end;
        undo_val := last_val;
        last_end := cur.imported_end;
        last_val := last_val + cur.imported_val;
        UPDATE merged SET stats_end = last_end, val = last_val
          WHERE id = last_id;
      END IF;

    -- If none of the cases above applies, there must have been an overlap
    -- between the new entry and an existing one.  Skip the new entry.
    ELSE
      last_id := cur.merged_id;
      last_start := cur.merged_start;
      last_end := cur.merged_end;
      last_val := cur.merged_val;
      undo_end := NULL;
      undo_val := NULL;
    END IF;
  END LOOP;

  -- That's it, we're done merging.
  RAISE NOTICE '% Finishing merge.', timeofday();
  RETURN;
END;
$$ LANGUAGE plpgsql;

-- Aggregate user estimates for all dates that have updated entries in the
-- merged table.  This function first creates a temporary table with
-- new or updated observations, then removes all existing estimates for
-- the dates to be updated, and finally inserts newly computed aggregates
-- for these dates.
CREATE OR REPLACE FUNCTION aggregate() RETURNS VOID AS $$
BEGIN
  RAISE NOTICE '% Starting aggregate step.', timeofday();

  -- Create a new temporary table containing all relevant information
  -- needed to update the aggregated table.  In this table, we sum up all
  -- observations of a given type by reporting node.  This query is
  -- (temporarily) materialized, because we need to combine its entries
  -- multiple times in various ways.  A (non-materialized) view would have
  -- meant to re-compute this query multiple times.
  CREATE TEMPORARY TABLE update AS
    SELECT fingerprint, node, metric, country, transport, version,
           DATE(stats_start), SUM(val) AS val,
           SUM(CAST(EXTRACT(EPOCH FROM stats_end - stats_start)
               AS DOUBLE PRECISION)) AS seconds
    FROM merged
    WHERE DATE(stats_start) IN (
          SELECT DISTINCT DATE(stats_start) FROM imported)
    GROUP BY fingerprint, node, metric, country, transport, version,
             DATE(stats_start);

  -- Delete all entries from the aggregated table that we're about to
  -- re-compute.
  DELETE FROM aggregated WHERE date IN (SELECT DISTINCT date FROM update);

  -- Insert partly empty results for all existing combinations of date,
  -- node ('relay' or 'bridge'), country, transport, and version.  Only
  -- the rrx and nrx fields will contain number and seconds of reported
  -- responses for the given combination of date, node, etc., while the
  -- other fields will be updated below.
  INSERT INTO aggregated (date, node, country, transport, version, rrx,
      nrx)
    SELECT date, node, country, transport, version, SUM(val) AS rrx,
    SUM(seconds) AS nrx
    FROM update WHERE metric = 'responses'
    GROUP BY date, node, country, transport, version;

  -- Create another temporary table with only those entries that aren't
  -- broken down by any dimension.  This table is much smaller, so the
  -- following operations are much faster.
  CREATE TEMPORARY TABLE update_no_dimensions AS
    SELECT fingerprint, node, metric, date, val, seconds FROM update
    WHERE country = ''
    AND transport = ''
    AND version = '';

  -- Update results in the aggregated table by setting aggregates based
  -- on reported directory bytes.  These aggregates are only based on
  -- date and node, so that the same values are set for all combinations
  -- of country, transport, and version.
  UPDATE aggregated
    SET hh = aggregated_bytes.hh, nh = aggregated_bytes.nh
    FROM (
      SELECT date, node, SUM(val) AS hh, SUM(seconds) AS nh
      FROM update_no_dimensions
      WHERE metric = 'bytes'
      GROUP BY date, node
    ) aggregated_bytes
    WHERE aggregated.date = aggregated_bytes.date
    AND aggregated.node = aggregated_bytes.node;

  -- Update results based on nodes being contained in the network status.
  UPDATE aggregated
    SET nn = aggregated_status.nn
    FROM (
      SELECT date, node, SUM(seconds) AS nn
      FROM update_no_dimensions
      WHERE metric = 'status'
      GROUP BY date, node
    ) aggregated_status
    WHERE aggregated.date = aggregated_status.date
    AND aggregated.node = aggregated_status.node;

  -- Update results based on nodes reporting both bytes and responses.
  UPDATE aggregated
    SET hrh = aggregated_bytes_responses.hrh
    FROM (
      SELECT bytes.date, bytes.node,
             SUM((LEAST(bytes.seconds, responses.seconds)
                 * bytes.val) / bytes.seconds) AS hrh
      FROM update_no_dimensions bytes
      LEFT JOIN update_no_dimensions responses
      ON bytes.date = responses.date
      AND bytes.fingerprint = responses.fingerprint
      AND bytes.node = responses.node
      WHERE bytes.metric = 'bytes'
      AND responses.metric = 'responses'
      GROUP BY bytes.date, bytes.node
    ) aggregated_bytes_responses
    WHERE aggregated.date = aggregated_bytes_responses.date
    AND aggregated.node = aggregated_bytes_responses.node;

  -- Update results based on notes reporting responses but no bytes.
  UPDATE aggregated
    SET nrh = aggregated_responses_bytes.nrh
    FROM (
      SELECT responses.date, responses.node,
             SUM(GREATEST(0, responses.seconds
                             - COALESCE(bytes.seconds, 0))) AS nrh
      FROM update_no_dimensions responses
      LEFT JOIN update_no_dimensions bytes
      ON responses.date = bytes.date
      AND responses.fingerprint = bytes.fingerprint
      AND responses.node = bytes.node
      WHERE responses.metric = 'responses'
      AND bytes.metric = 'bytes'
      GROUP BY responses.date, responses.node
    ) aggregated_responses_bytes
    WHERE aggregated.date = aggregated_responses_bytes.date
    AND aggregated.node = aggregated_responses_bytes.node;

  -- We're done aggregating new data.
  RAISE NOTICE '% Finishing aggregate step.', timeofday();
  RETURN;
END;
$$ LANGUAGE plpgsql;

-- User-friendly view on the aggregated table that implements the
-- algorithm proposed in Tor Tech Report 2012-10-001.  This view returns
-- user number estimates for both relay and bridge staistics, possibly
-- broken down by country or transport or version.
CREATE OR REPLACE VIEW estimated AS SELECT

  -- The date of this user number estimate.
  a.date,

  -- The node type, which is either 'relay' or 'bridge'.
  a.node,

  -- The two-letter lower-case country code of this estimate; can be '??'
  -- for an estimate of users that could not be resolved to any country,
  -- or '' (empty string) for an estimate of all users, regardless of
  -- country.
  a.country,

  -- The pluggable transport name of this estimate; can be '<OR>' for an
  -- estimate of users that did not use any pluggable transport, '<??>'
  -- for unknown pluggable transports, or '' (empty string) for an
  -- estimate of all users, regardless of transport.
  a.transport,

  -- The IP address version of this estimate; can be 'v4' or 'v6', or ''
  -- (empty string) for an estimate of all users, regardless of IP address
  -- version.
  a.version,

  -- Estimated fraction of nodes reporting directory requests, which is
  -- used to extrapolate observed requests to estimated total requests in
  -- the network.  The closer this fraction is to 1.0, the more precise
  -- the estimation.
  CAST(a.frac * 100 AS INTEGER) AS frac,

  -- Finally, the estimate number of users.
  CAST(a.rrx / (a.frac * 10) AS INTEGER) AS users

  -- Implement the estimation method in a subquery, so that the ugly
  -- formula only has to be written once.
  FROM (
    SELECT date, node, country, transport, version, rrx, nrx,
           (hrh * nh + hh * nrh) / (hh * nn) AS frac
    FROM aggregated WHERE hh * nn > 0.0) a

  -- Only include estimates with at least 10% of nodes reporting directory
  -- request statistics.
  WHERE a.frac BETWEEN 0.1 AND 1.0

  -- Order results.
  ORDER BY date DESC, node, version, transport, country;

