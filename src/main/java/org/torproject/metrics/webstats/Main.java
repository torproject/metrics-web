/* Copyright 2016--2018 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.webstats;

import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Main class of the webstats module that downloads log files from the server,
 * imports them into a database, and exports aggregate statistics to a CSV
 * file. */
public class Main {

  /** Logger for this class. */
  private static Logger log = LoggerFactory.getLogger(Main.class);

  /** Pattern for links contained in directory listings. */
  static final Pattern URL_STRING_PATTERN =
      Pattern.compile(".*<a href=\"([^\"]+)\">.*");

  static final Pattern LOG_FILE_URL_PATTERN =
      Pattern.compile("^.*/([^/]+)/([^/]+)-access.log-(\\d{8}).xz$");

  private static DateFormat logDateFormat;

  static {
    logDateFormat = new SimpleDateFormat("yyyyMMdd");
    logDateFormat.setLenient(false);
    logDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
  }

  static final Pattern LOG_LINE_PATTERN = Pattern.compile(
      "^0.0.0.[01] - - \\[\\d{2}/\\w{3}/\\d{4}:00:00:00 \\+0000\\] "
      + "\"(GET|HEAD) ([^ ]{1,2048}) HTTP[^ ]+\" (\\d+) (-|\\d+) \"-\" \"-\" "
      + "-$");

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

  /** Executes this data-processing module. */
  public static void main(String[] args) throws Exception {
    log.info("Starting webstats module.");
    String dbUrlString = "jdbc:postgresql:webstats";
    Connection connection = connectToDatabase(dbUrlString);
    SortedSet<String> previouslyImportedLogFileUrls =
        queryImportedFiles(connection);
    String baseUrl = "https://webstats.torproject.org/out/";
    SortedSet<String> newLogFileUrls = downloadDirectoryListings(baseUrl,
        previouslyImportedLogFileUrls);
    importLogFiles(connection, newLogFileUrls);
    SortedSet<String> statistics = queryWebstats(connection);
    writeStatistics(Paths.get("stats", "webstats.csv"), statistics);
    disconnectFromDatabase(connection);
    log.info("Terminated webstats module.");
  }

  private static Connection connectToDatabase(String jdbcString)
      throws SQLException {
    log.info("Connecting to database.");
    Connection connection = DriverManager.getConnection(jdbcString);
    connection.setAutoCommit(false);
    log.info("Successfully connected to database.");
    return connection;
  }

  static SortedSet<String> queryImportedFiles(Connection connection)
      throws SQLException {
    log.info("Querying URLs of previously imported log files.");
    SortedSet<String> importedLogFileUrls = new TreeSet<>();
    Statement st = connection.createStatement();
    String queryString = "SELECT url FROM files";
    try (ResultSet rs = st.executeQuery(queryString)) {
      while (rs.next()) {
        importedLogFileUrls.add(rs.getString(1));
      }
    }
    log.info("Found {} URLs of previously imported log files.",
        importedLogFileUrls.size());
    return importedLogFileUrls;
  }

  static SortedSet<String> downloadDirectoryListings(String baseUrl,
      SortedSet<String> importedLogFileUrls) throws IOException {
    log.info("Downloading directory listings from {}.", baseUrl);
    List<String> directoryListings = new ArrayList<>();
    directoryListings.add(baseUrl);
    SortedSet<String> newLogFileUrls = new TreeSet<>();
    while (!directoryListings.isEmpty()) {
      String urlString = directoryListings.remove(0);
      if (urlString.endsWith("/")) {
        directoryListings.addAll(downloadDirectoryListing(urlString));
      } else if (!urlString.endsWith(".xz")) {
        log.debug("Skipping unrecognized URL {}.", urlString);
      } else if (!importedLogFileUrls.contains(urlString)) {
        newLogFileUrls.add(urlString);
      }
    }
    log.info("Found {} URLs of log files that have not yet been imported.",
        newLogFileUrls.size());
    return newLogFileUrls;
  }

  static List<String> downloadDirectoryListing(String urlString)
      throws IOException {
    log.debug("Downloading directory listing from {}.", urlString);
    List<String> urlStrings = new ArrayList<>();
    try (BufferedReader br = new BufferedReader(new InputStreamReader(
        new URL(urlString).openStream()))) {
      String line;
      while ((line = br.readLine()) != null) {
        Matcher matcher = URL_STRING_PATTERN.matcher(line);
        if (matcher.matches() && !matcher.group(1).startsWith("/")) {
          urlStrings.add(urlString + matcher.group(1));
        }
      }
    }
    return urlStrings;
  }

  static void importLogFiles(Connection connection,
      SortedSet<String> newLogFileUrls) {
    log.info("Downloading, parsing, and importing {} log files.",
        newLogFileUrls.size());
    for (String urlString : newLogFileUrls) {
      try {
        Object[] metaData = parseMetaData(urlString);
        if (metaData == null) {
          continue;
        }
        Map<String, Integer> parsedLogLines = downloadAndParseLogFile(
            urlString);
        importLogLines(connection, urlString, metaData, parsedLogLines);
      } catch (IOException | ParseException exc) {
        log.warn("Cannot download or parse log file with URL {}.  Retrying "
            + "in the next run.", urlString, exc);
      } catch (SQLException exc) {
        log.warn("Cannot import log file with URL {} into the database.  "
            + "Rolling back and retrying in the next run.", urlString, exc);
        try {
          connection.rollback();
        } catch (SQLException exceptionWhileRollingBack) {
          /* Ignore. */
        }
      }
    }
  }

  private static Object[] parseMetaData(String urlString)
      throws ParseException {
    log.debug("Importing log file {}.", urlString);
    if (urlString.contains("-ssl-access.log-")) {
      log.debug("Skipping log file containing SSL requests with URL {}.",
          urlString);
      return null;
    }
    Matcher logFileUrlMatcher = LOG_FILE_URL_PATTERN.matcher(urlString);
    if (!logFileUrlMatcher.matches()) {
      log.debug("Skipping log file with unrecognized URL {}.", urlString);
      return null;
    }
    String server = logFileUrlMatcher.group(1);
    String site = logFileUrlMatcher.group(2);
    long logDateMillis = logDateFormat.parse(logFileUrlMatcher.group(3))
        .getTime();
    return new Object[] { server, site, logDateMillis };
  }

  static Map<String, Integer> downloadAndParseLogFile(String urlString)
      throws IOException {
    int skippedLines = 0;
    Map<String, Integer> parsedLogLines = new HashMap<>();
    try (BufferedReader br = new BufferedReader(new InputStreamReader(
        new XZCompressorInputStream(new URL(urlString).openStream())))) {
      String line;
      while ((line = br.readLine()) != null) {
        if (!parseLogLine(line, parsedLogLines)) {
          skippedLines++;
        }
      }
    }
    if (skippedLines > 0) {
      log.debug("Skipped {} lines while parsing log file {}.", skippedLines,
          urlString);
    }
    return parsedLogLines;
  }

  static boolean parseLogLine(String logLine,
      Map<String, Integer> parsedLogLines) {
    Matcher logLineMatcher = LOG_LINE_PATTERN.matcher(logLine);
    if (!logLineMatcher.matches()) {
      return false;
    }
    String method = logLineMatcher.group(1);
    String resource = logLineMatcher.group(2);
    int responseCode = Integer.parseInt(logLineMatcher.group(3));
    String combined = String.format("%s %s %d", method, resource,
        responseCode);
    if (!parsedLogLines.containsKey(combined)) {
      parsedLogLines.put(combined, 1);
    } else {
      parsedLogLines.put(combined, parsedLogLines.get(combined) + 1);
    }
    return true;
  }

  private static void importLogLines(Connection connection, String urlString,
      Object[] metaData, Map<String, Integer> parsedLogLines)
      throws SQLException {
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
    String server = (String) metaData[0];
    String site = (String) metaData[1];
    long logDateMillis = (long) metaData[2];
    int fileId = insertFile(psFiles, urlString, server, site, logDateMillis);
    if (fileId < 0) {
      log.debug("Skipping previously imported log file {}.", urlString);
      return;
    }
    for (Map.Entry<String, Integer> requests : parsedLogLines.entrySet()) {
      String[] keyParts = requests.getKey().split(" ");
      String method = keyParts[0];
      String resource = keyParts[1];
      int responseCode = Integer.parseInt(keyParts[2]);
      int count = requests.getValue();
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
    log.debug("Finished importing log file with URL {} into database.",
        urlString);
  }

  private static int insertFile(PreparedStatement psFiles, String urlString,
      String server, String site, long logDateMillis) throws SQLException {
    int fileId = -1;
    psFiles.clearParameters();
    psFiles.setString(1, truncateString(urlString, 2048));
    psFiles.setString(2, truncateString(server, 32));
    psFiles.setString(3, truncateString(site, 128));
    psFiles.setDate(4, new Date(logDateMillis));
    psFiles.execute();
    try (ResultSet rs = psFiles.getGeneratedKeys()) {
      if (rs.next()) {
        fileId = rs.getInt(1);
      }
    }
    return fileId;
  }

  private static void insertRequest(PreparedStatement psRequests, int fileId,
      String method, int resourceId, int responseCode, int count)
      throws SQLException {
    psRequests.clearParameters();
    psRequests.setInt(1, fileId);
    psRequests.setString(2, method);
    psRequests.setInt(3, resourceId);
    psRequests.setInt(4, responseCode);
    psRequests.setInt(5, count);
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

