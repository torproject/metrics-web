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
import java.nio.file.Paths;
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
import java.util.List;
import java.util.Locale;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeSet;

public class Main {

  /** Logger for this class. */
  private static Logger log = LoggerFactory.getLogger(Main.class);

  /** Executes this data-processing module. */
  public static void main(String[] args) throws Exception {
    log.info("Starting onionperf module.");
    String dbUrlString = "jdbc:postgresql:onionperf";
    Connection connection = connectToDatabase(dbUrlString);
    importOnionPerfFiles(connection);
    SortedSet<String> statistics = queryOnionPerf(connection);
    writeStatistics(Paths.get("stats", "torperf-1.1.csv"), statistics);
    disconnectFromDatabase(connection);
    log.info("Terminated onionperf module.");
  }

  private static Connection connectToDatabase(String jdbcString)
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

    Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    DescriptorReader dr = DescriptorSourceFactory.createDescriptorReader();
    for (Descriptor d : dr.readDescriptors(
        new File("../../shared/in/archive/torperf"),
        new File("../../shared/in/recent/torperf"))) {
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
      /* Could use measurementId to insert path. */
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

  static SortedSet<String> queryOnionPerf(Connection connection)
      throws SQLException {
    log.info("Querying statistics from database.");
    SortedSet<String> statistics = new TreeSet<>();
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
            emptyNull(rs.getString("source")),
            emptyNull(rs.getString("server")),
            rs.getDouble("q1"),
            rs.getDouble("md"),
            rs.getDouble("q3"),
            rs.getInt("timeouts"),
            rs.getInt("failures"),
            rs.getInt("requests")));
      }
    }
    return statistics;
  }

  private static String emptyNull(String text) {
    return null == text ? "" : text;
  }

  static void writeStatistics(Path webstatsPath,
      SortedSet<String> statistics) throws IOException {
    webstatsPath.toFile().getParentFile().mkdirs();
    List<String> lines = new ArrayList<>();
    lines
        .add("date,filesize,source,server,q1,md,q3,timeouts,failures,requests");
    lines.addAll(statistics);
    log.info("Writing {} lines to {}.", lines.size(),
        webstatsPath.toFile().getAbsolutePath());
    Files.write(webstatsPath, lines, StandardCharsets.UTF_8);
  }

  private static void disconnectFromDatabase(Connection connection)
      throws SQLException {
    log.info("Disconnecting from database.");
    connection.close();
  }
}

