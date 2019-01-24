/* Copyright 2016--2018 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.stats.webstats;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingByConcurrent;

import org.torproject.descriptor.Descriptor;
import org.torproject.descriptor.DescriptorParseException;
import org.torproject.descriptor.DescriptorSourceFactory;
import org.torproject.descriptor.WebServerAccessLog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeSet;

/** Main class of the webstats module that downloads log files from the server,
 * imports them into a database, and exports aggregate statistics to a CSV
 * file. */
public class Main {

  /** Logger for this class. */
  private static Logger log = LoggerFactory.getLogger(Main.class);

  private static final String jdbcString = String.format(
      "jdbc:postgresql://localhost/webstats?user=%s&password=%s",
      System.getProperty("metrics.dbuser", "metrics"),
      System.getProperty("metrics.dbpass", "password"));

  private static final String LOG_DATE = "log_date";

  private static final String REQUEST_TYPE = "request_type";

  private static final String PLATFORM = "platform";

  private static final String CHANNEL = "channel";

  private static final String LOCALE = "locale";

  private static final String INCREMENTAL = "incremental";

  private static final String COUNT = "count";

  private static final String ALL_COLUMNS = LOG_DATE + "," + REQUEST_TYPE + ","
      + PLATFORM + "," + CHANNEL + "," + LOCALE + "," + INCREMENTAL + ","
      + COUNT;

  private static final File baseDir = new File(
      org.torproject.metrics.stats.main.Main.modulesDir, "webstats");

  /** Executes this data-processing module. */
  public static void main(String[] args) throws Exception {
    log.info("Starting webstats module.");
    Connection connection = connectToDatabase();
    SortedSet<String> skipFiles = queryImportedFileNames(connection);
    importLogFiles(connection, skipFiles,
        new File(org.torproject.metrics.stats.main.Main.descriptorsDir,
            "recent/webstats"),
        new File(org.torproject.metrics.stats.main.Main.descriptorsDir,
            "archive/webstats"));
    SortedSet<String> statistics = queryWebstats(connection);
    writeStatistics(new File(baseDir, "stats/webstats.csv").toPath(),
        statistics);
    disconnectFromDatabase(connection);
    log.info("Terminated webstats module.");
  }

  private static Connection connectToDatabase()
      throws SQLException {
    log.info("Connecting to database.");
    Connection connection = DriverManager.getConnection(jdbcString);
    connection.setAutoCommit(false);
    log.info("Successfully connected to database.");
    return connection;
  }

  static SortedSet<String> queryImportedFileNames(Connection connection)
      throws SQLException {
    log.info("Querying previously imported log files.");
    SortedSet<String> importedLogFileUrls = new TreeSet<>();
    Statement st = connection.createStatement();
    String queryString = "SELECT server, site, log_date FROM files";
    DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyyMMdd");
    try (ResultSet rs = st.executeQuery(queryString)) {
      while (rs.next()) {
        importedLogFileUrls.add(String.format("%s_%s_access.log_%s.xz",
            rs.getString(2), rs.getString(1),
            rs.getDate(3).toLocalDate().format(dateFormat)));
      }
    }
    log.info("Found {} previously imported log files.",
        importedLogFileUrls.size());
    return importedLogFileUrls;
  }

  static void importLogFiles(Connection connection, SortedSet<String> skipFiles,
      File... inDirectories) {
    DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyyMMdd");
    for (Descriptor descriptor : DescriptorSourceFactory
        .createDescriptorReader().readDescriptors(inDirectories)) {
      if (!(descriptor instanceof WebServerAccessLog)) {
        continue;
      }
      WebServerAccessLog logFile = (WebServerAccessLog) descriptor;
      String logFileNameWithTruncatedParts = String.format(
          "%s_%s_access.log_%s.xz",
          truncateString(logFile.getVirtualHost(), 128),
          truncateString(logFile.getPhysicalHost(), 32),
          logFile.getLogDate().format(dateFormat));
      if (skipFiles.contains(logFileNameWithTruncatedParts)) {
        continue;
      }
      try {
        Map<String, Long> parsedLogLines = logFile.logLines().parallel()
            .collect(groupingByConcurrent(line
                -> String.format("%s %s %d", line.getMethod().name(),
                truncateString(line.getRequest(), 2048), line.getResponse()),
                counting()));
        importLogLines(connection, logFile.getDescriptorFile().getName(),
            logFile.getPhysicalHost(), logFile.getVirtualHost(),
            logFile.getLogDate(), parsedLogLines);
      } catch (DescriptorParseException exc) {
        log.warn("Cannot parse log file with file name {}.  Retrying in the "
            + "next run.", logFile.getDescriptorFile().getName(), exc);
      } catch (SQLException exc) {
        log.warn("Cannot import log file with file name {} into the database. "
            + "Rolling back and retrying in the next run.",
            logFile.getDescriptorFile().getName(), exc);
        try {
          connection.rollback();
        } catch (SQLException exceptionWhileRollingBack) {
          /* Ignore. */
        }
      }
    }
  }

  private static void importLogLines(Connection connection, String urlString,
      String server, String site, LocalDate logDate,
      Map<String, Long> parsedLogLines) throws SQLException {
    PreparedStatement psFiles = connection.prepareStatement(
        "INSERT INTO files (url, server, site, " + LOG_DATE + ") "
        + "VALUES (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
    PreparedStatement psResourcesSelect = connection.prepareStatement(
        "SELECT resource_id FROM resources WHERE resource_string = ?");
    PreparedStatement psResourcesInsert = connection.prepareStatement(
        "INSERT INTO resources (resource_string) VALUES (?)",
        Statement.RETURN_GENERATED_KEYS);
    PreparedStatement psRequests = connection.prepareStatement(
        "INSERT INTO requests (file_id, method, resource_id, response_code, "
        + COUNT + ") VALUES (?, CAST(? AS method), ?, ?, ?)");
    int fileId = insertFile(psFiles, urlString, server, site, logDate);
    if (fileId < 0) {
      log.debug("Skipping previously imported log file {}.", urlString);
      return;
    }
    for (Map.Entry<String, Long> requests : parsedLogLines.entrySet()) {
      String[] keyParts = requests.getKey().split(" ");
      String method = keyParts[0];
      String resource = keyParts[1];
      int responseCode = Integer.parseInt(keyParts[2]);
      long count = requests.getValue();
      int resourceId = insertResource(psResourcesSelect, psResourcesInsert,
          resource);
      if (resourceId < 0) {
        log.error("Could not retrieve auto-generated key for new resources "
            + "entry.");
        connection.rollback();
        return;
      }
      insertRequest(psRequests, fileId, method, resourceId, responseCode,
          count);
    }
    connection.commit();
    log.debug("Finished importing log file with file name {} into database.",
        urlString);
  }

  private static int insertFile(PreparedStatement psFiles, String urlString,
      String server, String site, LocalDate logDate) throws SQLException {
    int fileId = -1;
    psFiles.clearParameters();
    psFiles.setString(1, truncateString(urlString, 2048));
    psFiles.setString(2, truncateString(server, 32));
    psFiles.setString(3, truncateString(site, 128));
    psFiles.setDate(4, Date.valueOf(logDate));
    psFiles.execute();
    try (ResultSet rs = psFiles.getGeneratedKeys()) {
      if (rs.next()) {
        fileId = rs.getInt(1);
      }
    }
    return fileId;
  }

  private static void insertRequest(PreparedStatement psRequests, int fileId,
      String method, int resourceId, int responseCode, long count)
      throws SQLException {
    psRequests.clearParameters();
    psRequests.setInt(1, fileId);
    psRequests.setString(2, method);
    psRequests.setInt(3, resourceId);
    psRequests.setInt(4, responseCode);
    psRequests.setLong(5, count);
    psRequests.execute();
  }

  private static int insertResource(PreparedStatement psResourcesSelect,
      PreparedStatement psResourcesInsert, String resource)
      throws SQLException {
    int resourceId = -1;
    String truncatedResource = truncateString(resource, 2048);
    psResourcesSelect.clearParameters();
    psResourcesSelect.setString(1, truncatedResource);
    try (ResultSet rs = psResourcesSelect.executeQuery()) {
      if (rs.next()) {
        resourceId = rs.getInt(1);
      }
    }
    if (resourceId < 0) {
      /* There's a small potential for a race condition between the previous
       * SELECT and this INSERT INTO, but that will be resolved by the UNIQUE
       * constraint when committing the transaction. */
      psResourcesInsert.clearParameters();
      psResourcesInsert.setString(1, truncatedResource);
      psResourcesInsert.execute();
      try (ResultSet rs = psResourcesInsert.getGeneratedKeys()) {
        if (rs.next()) {
          resourceId = rs.getInt(1);
        }
      }
    }
    return resourceId;
  }

  private static String truncateString(String originalString,
      int truncateAfter) {
    if (originalString.length() > truncateAfter) {
      originalString = originalString.substring(0, truncateAfter);
    }
    return originalString;
  }

  static SortedSet<String> queryWebstats(Connection connection)
      throws SQLException {
    log.info("Querying statistics from database.");
    SortedSet<String> statistics = new TreeSet<>();
    Statement st = connection.createStatement();
    String queryString = "SELECT " + ALL_COLUMNS + " FROM webstats";
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"),
        Locale.US);
    try (ResultSet rs = st.executeQuery(queryString)) {
      while (rs.next()) {
        statistics.add(String.format("%s,%s,%s,%s,%s,%s,%d",
            dateFormat.format(rs.getDate(LOG_DATE, calendar)),
            emptyNull(rs.getString(REQUEST_TYPE)),
            emptyNull(rs.getString(PLATFORM)),
            emptyNull(rs.getString(CHANNEL)),
            emptyNull(rs.getString(LOCALE)),
            emptyNull(rs.getString(INCREMENTAL)),
            rs.getLong(COUNT)));
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
    lines.add(ALL_COLUMNS);
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

