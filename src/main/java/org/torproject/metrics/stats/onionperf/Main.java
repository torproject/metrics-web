package org.torproject.metrics.stats.onionperf;

import org.torproject.descriptor.Descriptor;
import org.torproject.descriptor.DescriptorReader;
import org.torproject.descriptor.DescriptorSourceFactory;
import org.torproject.descriptor.TorperfResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

public class Main {

  /** Logger for this class. */
  private static Logger log = LoggerFactory.getLogger(Main.class);

  private static final String jdbcString = String.format(
      "jdbc:postgresql://localhost/onionperf?user=%s&password=%s",
      System.getProperty("metrics.dbuser", "metrics"),
      System.getProperty("metrics.dbpass", "password"));

  private static final File baseDir = new File(
      org.torproject.metrics.stats.main.Main.modulesDir, "onionperf");

  /** Executes this data-processing module. */
  public static void main(String[] args) throws Exception {
    log.info("Starting onionperf module.");
    Connection connection = connectToDatabase();
    importOnionPerfFiles(connection);
    writeStatistics(new File(baseDir, "stats/torperf-1.1.csv").toPath(),
        queryOnionPerf(connection));
    writeStatistics(new File(baseDir, "stats/buildtimes.csv").toPath(),
        queryBuildTimes(connection));
    writeStatistics(new File(baseDir, "stats/latencies.csv").toPath(),
        queryLatencies(connection));
    disconnectFromDatabase(connection);
    log.info("Terminated onionperf module.");
  }

  private static Connection connectToDatabase()
      throws SQLException {
    log.info("Connecting to database.");
    Connection connection = DriverManager.getConnection(jdbcString);
    connection.setAutoCommit(false);
    log.info("Successfully connected to database.");
    return connection;
  }

  private static void importOnionPerfFiles(Connection connection)
      throws SQLException {

    PreparedStatement psMeasurementsSelect = connection.prepareStatement(
        "SELECT measurement_id FROM measurements WHERE source = ? "
        + "AND filesize = ? AND start = ?");

    PreparedStatement psMeasurementsInsert = connection.prepareStatement(
        "INSERT INTO measurements (source, filesize, start, socket, connect, "
        + "negotiate, request, response, datarequest, dataresponse, "
        + "datacomplete, writebytes, readbytes, didtimeout, dataperc0, "
        + "dataperc10, dataperc20, dataperc30, dataperc40, dataperc50, "
        + "dataperc60, dataperc70, dataperc80, dataperc90, dataperc100, "
        + "launch, used_at, timeout, quantile, circ_id, used_by, "
        + "endpointlocal, endpointproxy, endpointremote, hostnamelocal, "
        + "hostnameremote, sourceaddress) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, "
        + "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, "
        + "?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);

    PreparedStatement psBuildTimesSelect = connection.prepareStatement(
        "SELECT position FROM buildtimes WHERE measurement_id = ?");

    PreparedStatement psBuildTimesInsert = connection.prepareStatement(
        "INSERT INTO buildtimes (measurement_id, position, buildtime, delta) "
            + "VALUES (?, ?, ?, ?)");

    Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    DescriptorReader dr = DescriptorSourceFactory.createDescriptorReader();
    for (Descriptor d : dr.readDescriptors(
        new File(org.torproject.metrics.stats.main.Main.descriptorsDir,
            "archive/torperf"),
        new File(org.torproject.metrics.stats.main.Main.descriptorsDir,
            "recent/torperf"))) {
      if (!(d instanceof TorperfResult)) {
        continue;
      }
      TorperfResult tr = (TorperfResult) d;
      int measurementId = -1;
      String truncatedSource = truncateString(tr.getSource(), 32);
      psMeasurementsSelect.clearParameters();
      psMeasurementsSelect.setString(1, truncatedSource);
      psMeasurementsSelect.setInt(2, tr.getFileSize());
      psMeasurementsSelect.setTimestamp(3,
          new Timestamp(tr.getStartMillis()), calendar);
      try (ResultSet rs = psMeasurementsSelect.executeQuery()) {
        if (rs.next()) {
          measurementId = rs.getInt(1);
        }
      }
      if (measurementId < 0) {
        psMeasurementsInsert.clearParameters();
        psMeasurementsInsert.setString(1, truncatedSource);
        psMeasurementsInsert.setInt(2, tr.getFileSize());
        psMeasurementsInsert.setTimestamp(3,
            new Timestamp(tr.getStartMillis()), calendar);
        long[] timestamps = new long[] { tr.getSocketMillis(),
            tr.getConnectMillis(), tr.getNegotiateMillis(),
            tr.getRequestMillis(), tr.getResponseMillis(),
            tr.getDataRequestMillis(), tr.getDataResponseMillis(),
            tr.getDataCompleteMillis() };
        for (int i = 4, j = 0; j < timestamps.length; i++, j++) {
          if (timestamps[j] == 0L) {
            psMeasurementsInsert.setNull(i, Types.INTEGER);
          } else {
            psMeasurementsInsert.setInt(i,
                (int) (timestamps[j] - tr.getStartMillis()));
          }
        }
        psMeasurementsInsert.setInt(12, tr.getWriteBytes());
        psMeasurementsInsert.setInt(13, tr.getReadBytes());
        if (null == tr.didTimeout()) {
          psMeasurementsInsert.setNull(14, Types.BOOLEAN);
        } else {
          psMeasurementsInsert.setBoolean(14, tr.didTimeout());
        }
        for (int i = 15, p = 0; i <= 25 && p <= 100; i++, p += 10) {
          if (null == tr.getDataPercentiles()
              || !tr.getDataPercentiles().containsKey(p)) {
            psMeasurementsInsert.setNull(i, Types.INTEGER);
          } else {
            psMeasurementsInsert.setInt(i,
                (int) (tr.getDataPercentiles().get(p) - tr.getStartMillis()));
          }
        }
        if (tr.getLaunchMillis() < 0L) {
          psMeasurementsInsert.setNull(26, Types.TIMESTAMP);
        } else {
          psMeasurementsInsert.setTimestamp(26,
              new Timestamp(tr.getLaunchMillis()), calendar);
        }
        if (tr.getUsedAtMillis() < 0L) {
          psMeasurementsInsert.setNull(27, Types.TIMESTAMP);
        } else {
          psMeasurementsInsert.setTimestamp(27,
              new Timestamp(tr.getUsedAtMillis()), calendar);
        }
        if (tr.getTimeout() < 0L) {
          psMeasurementsInsert.setNull(28, Types.INTEGER);
        } else {
          psMeasurementsInsert.setInt(28, (int) tr.getTimeout());
        }
        if (tr.getQuantile() < 0.0) {
          psMeasurementsInsert.setNull(29, Types.REAL);
        } else {
          psMeasurementsInsert.setDouble(29, tr.getQuantile());
        }
        if (tr.getCircId() < 0L) {
          psMeasurementsInsert.setNull(30, Types.INTEGER);
        } else {
          psMeasurementsInsert.setInt(30, tr.getCircId());
        }
        if (tr.getUsedBy() < 0L) {
          psMeasurementsInsert.setNull(31, Types.INTEGER);
        } else {
          psMeasurementsInsert.setInt(31, tr.getUsedBy());
        }
        String[] onionPerfStrings = new String[] {
            tr.getEndpointLocal(), tr.getEndpointProxy(),
            tr.getEndpointRemote(), tr.getHostnameLocal(),
            tr.getHostnameRemote(), tr.getSourceAddress() };
        for (int i = 32, j = 0; j < onionPerfStrings.length; i++, j++) {
          if (null == onionPerfStrings[j]) {
            psMeasurementsInsert.setNull(i, Types.VARCHAR);
          } else {
            psMeasurementsInsert.setString(i,
                truncateString(onionPerfStrings[j], 64));
          }
        }
        psMeasurementsInsert.execute();
        try (ResultSet rs = psMeasurementsInsert.getGeneratedKeys()) {
          if (rs.next()) {
            measurementId = rs.getInt(1);
          }
        }
      }
      if (null != tr.getBuildTimes()) {
        psBuildTimesSelect.clearParameters();
        psBuildTimesSelect.setInt(1, measurementId);
        Set<Integer> skipPositions = new HashSet<>();
        try (ResultSet rs = psBuildTimesSelect.executeQuery()) {
          while (rs.next()) {
            skipPositions.add(rs.getInt(1));
          }
        }
        int position = 1;
        long previousBuildTime = 0L;
        for (long buildtime : tr.getBuildTimes()) {
          if (!skipPositions.contains(position)) {
            psBuildTimesInsert.clearParameters();
            psBuildTimesInsert.setInt(1, measurementId);
            psBuildTimesInsert.setInt(2, position);
            psBuildTimesInsert.setInt(3, (int) buildtime);
            psBuildTimesInsert.setInt(4, (int) (buildtime - previousBuildTime));
            psBuildTimesInsert.execute();
          }
          position++;
          previousBuildTime = buildtime;
        }
      }
      connection.commit();
    }
  }

  private static String truncateString(String originalString,
      int truncateAfter) {
    if (originalString.length() > truncateAfter) {
      originalString = originalString.substring(0, truncateAfter);
    }
    return originalString;
  }

  static List<String> queryOnionPerf(Connection connection)
      throws SQLException {
    log.info("Querying statistics from database.");
    List<String> statistics = new ArrayList<>();
    statistics
        .add("date,filesize,source,server,q1,md,q3,timeouts,failures,requests");
    Statement st = connection.createStatement();
    String queryString = "SELECT date, filesize, source, server, q1, md, q3, "
        + "timeouts, failures, requests FROM onionperf";
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    try (ResultSet rs = st.executeQuery(queryString)) {
      while (rs.next()) {
        statistics.add(String.format("%s,%d,%s,%s,%.0f,%.0f,%.0f,%d,%d,%d",
            dateFormat.format(rs.getDate("date", calendar)),
            rs.getInt("filesize"),
            getStringFromResultSet(rs, "source"),
            getStringFromResultSet(rs, "server"),
            getDoubleFromResultSet(rs, "q1"),
            getDoubleFromResultSet(rs, "md"),
            getDoubleFromResultSet(rs, "q3"),
            rs.getInt("timeouts"),
            rs.getInt("failures"),
            rs.getInt("requests")));
      }
    }
    return statistics;
  }

  static List<String> queryBuildTimes(Connection connection)
      throws SQLException {
    log.info("Querying buildtime statistics from database.");
    List<String> statistics = new ArrayList<>();
    statistics.add("date,source,position,q1,md,q3");
    Statement st = connection.createStatement();
    String queryString = "SELECT date, source, position, q1, md, q3 "
        + "FROM buildtimes_stats";
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    try (ResultSet rs = st.executeQuery(queryString)) {
      while (rs.next()) {
        statistics.add(String.format("%s,%s,%d,%d,%d,%d",
            dateFormat.format(rs.getDate("date", calendar)),
            getStringFromResultSet(rs, "source"),
            rs.getInt("position"),
            rs.getInt("q1"),
            rs.getInt("md"),
            rs.getInt("q3")));
      }
    }
    return statistics;
  }

  static List<String> queryLatencies(Connection connection)
      throws SQLException {
    log.info("Querying latency statistics from database.");
    List<String> statistics = new ArrayList<>();
    statistics.add("date,source,server,q1,md,q3");
    Statement st = connection.createStatement();
    String queryString = "SELECT date, source, server, q1, md, q3 "
        + "FROM latencies_stats";
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    try (ResultSet rs = st.executeQuery(queryString)) {
      while (rs.next()) {
        statistics.add(String.format("%s,%s,%s,%d,%d,%d",
            dateFormat.format(rs.getDate("date", calendar)),
            getStringFromResultSet(rs, "source"),
            rs.getString("server"),
            rs.getInt("q1"),
            rs.getInt("md"),
            rs.getInt("q3")));
      }
    }
    return statistics;
  }

  /** Retrieves the <code>String</code> value of the designated column in the
   * current row of the given <code>ResultSet</code> object, or returns the
   * empty string if the retrieved value was <code>NULL</code>. */
  private static String getStringFromResultSet(ResultSet rs, String columnLabel)
      throws SQLException {
    String result = rs.getString(columnLabel);
    return null == result ? "" : result;
  }

  /** Retrieves the <code>double</code> value of the designated column in the
   * current row of the given <code>ResultSet</code> object as a
   * <code>Double</code> object, or <code>null</code> if the retrieved value was
   * <code>NULL</code>. */
  private static Double getDoubleFromResultSet(ResultSet rs, String columnLabel)
      throws SQLException {
    double result = rs.getDouble(columnLabel);
    return rs.wasNull() ? null : result;
  }

  static void writeStatistics(Path webstatsPath, List<String> statistics)
      throws IOException {
    webstatsPath.toFile().getParentFile().mkdirs();
    log.info("Writing {} lines to {}.", statistics.size(),
        webstatsPath.toFile().getAbsolutePath());
    Files.write(webstatsPath, statistics, StandardCharsets.UTF_8);
  }

  private static void disconnectFromDatabase(Connection connection)
      throws SQLException {
    log.info("Disconnecting from database.");
    connection.close();
  }
}

