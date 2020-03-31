/* Copyright 2011--2020 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.stats.bwhist;

import org.torproject.descriptor.Descriptor;
import org.torproject.descriptor.DescriptorReader;
import org.torproject.descriptor.DescriptorSourceFactory;
import org.torproject.descriptor.ExtraInfoDescriptor;
import org.torproject.descriptor.NetworkStatusEntry;
import org.torproject.descriptor.RelayNetworkStatusConsensus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Parse directory data.
 */
public final class RelayDescriptorDatabaseImporter {

  /**
   * How many records to commit with each database transaction.
   */
  private final long autoCommitCount = 500;

  /* Counters to keep track of the number of records committed before
   * each transaction. */

  private int rhsCount = 0;

  private int rrsCount = 0;

  /**
   * Relay descriptor database connection.
   */
  private Connection conn;

  /**
   * Prepared statement to check whether any network status consensus
   * entries matching a given valid-after time have been imported into the
   * database before.
   */
  private PreparedStatement psSs;

  /**
   * Set of dates that have been inserted into the database for being
   * included in the next refresh run.
   */
  private Set<Long> scheduledUpdates;

  /**
   * Prepared statement to insert a date into the database that shall be
   * included in the next refresh run.
   */
  private PreparedStatement psU;

  /**
   * Prepared statement to insert a network status consensus entry into
   * the database.
   */
  private PreparedStatement psR;

  /**
   * Callable statement to insert the bandwidth history of an extra-info
   * descriptor into the database.
   */
  private CallableStatement csH;

  private static final Logger logger
      = LoggerFactory.getLogger(RelayDescriptorDatabaseImporter.class);

  /**
   * Date format to parse timestamps.
   */
  private SimpleDateFormat dateTimeFormat;

  /**
   * The last valid-after time for which we checked whether they have been
   * any network status entries in the database.
   */
  private long lastCheckedStatusEntries;

  /**
   * Set of fingerprints that we imported for the valid-after time in
   * {@code lastCheckedStatusEntries}.
   */
  private Set<String> insertedStatusEntries = new HashSet<>();

  private boolean importIntoDatabase = true;

  private File[] descriptorDirectories;

  private File historyFile;

  /**
   * Initialize database importer by connecting to the database and
   * preparing statements.
   */
  public RelayDescriptorDatabaseImporter(File[] descriptorDirectories,
      File historyFile, String connectionUrl) {

    this.descriptorDirectories = descriptorDirectories;
    this.historyFile = historyFile;

    if (connectionUrl != null) {
      try {
        /* Connect to database. */
        this.conn = DriverManager.getConnection(connectionUrl);

        /* Turn autocommit off */
        this.conn.setAutoCommit(false);

        /* Prepare statements. */
        this.psSs = conn.prepareStatement("SELECT fingerprint "
            + "FROM statusentry WHERE validafter = ?");
        this.psR = conn.prepareStatement("INSERT INTO statusentry "
            + "(validafter, fingerprint, isauthority, isexit, isguard, "
            + "isrunning) VALUES (?, ?, ?, ?, ?, ?)");
        this.csH = conn.prepareCall("{call insert_bwhist(?, ?, ?, ?, ?, "
            + "?)}");
        this.psU = conn.prepareStatement("INSERT INTO scheduled_updates "
            + "(date) VALUES (?)");
        this.scheduledUpdates = new HashSet<>();
      } catch (SQLException e) {
        logger.warn("Could not connect to database or prepare statements.", e);
      }
    }

    /* Initialize date format, so that we can format timestamps. */
    this.dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  }

  private void addDateToScheduledUpdates(long timestamp)
      throws SQLException {
    if (!this.importIntoDatabase) {
      return;
    }
    long dateMillis;
    try {
      dateMillis = this.dateTimeFormat.parse(
          this.dateTimeFormat.format(timestamp).substring(0, 10)
          + " 00:00:00").getTime();
    } catch (ParseException e) {
      logger.warn("Internal parsing error.", e);
      return;
    }
    if (!this.scheduledUpdates.contains(dateMillis)) {
      this.psU.setDate(1, new java.sql.Date(dateMillis));
      this.psU.execute();
      this.scheduledUpdates.add(dateMillis);
    }
  }

  /**
   * Insert network status consensus entry into database.
   */
  public void addStatusEntryContents(long validAfter, String fingerprint,
      SortedSet<String> flags) {
    if (this.importIntoDatabase) {
      try {
        this.addDateToScheduledUpdates(validAfter);
        Timestamp validAfterTimestamp = new Timestamp(validAfter);
        if (lastCheckedStatusEntries != validAfter) {
          insertedStatusEntries.clear();
          this.psSs.setTimestamp(1, validAfterTimestamp);
          ResultSet rs = psSs.executeQuery();
          while (rs.next()) {
            String insertedFingerprint = rs.getString(1);
            insertedStatusEntries.add(insertedFingerprint);
          }
          rs.close();
          lastCheckedStatusEntries = validAfter;
        }
        if (!insertedStatusEntries.contains(fingerprint)) {
          this.psR.clearParameters();
          this.psR.setTimestamp(1, validAfterTimestamp);
          this.psR.setString(2, fingerprint);
          this.psR.setBoolean(3, flags.contains("Authority"));
          this.psR.setBoolean(4, flags.contains("Exit"));
          this.psR.setBoolean(5, flags.contains("Guard"));
          this.psR.setBoolean(6, flags.contains("Running"));
          this.psR.executeUpdate();
          rrsCount++;
          if (rrsCount % autoCommitCount == 0)  {
            this.conn.commit();
          }
          insertedStatusEntries.add(fingerprint);
        }
      } catch (SQLException e) {
        logger.warn("Could not add network status consensus entry. We won't "
            + "make any further SQL requests in this execution.", e);
        this.importIntoDatabase = false;
      }
    }
  }

  /**
   * Insert extra-info descriptor into database.
   */
  public void addExtraInfoDescriptorContents(String fingerprint, long published,
      List<String> bandwidthHistoryLines) {
    if (!bandwidthHistoryLines.isEmpty()) {
      this.addBandwidthHistory(fingerprint.toLowerCase(), published,
          bandwidthHistoryLines);
    }
  }

  private static class BigIntArray implements java.sql.Array {

    private final String stringValue;

    public BigIntArray(long[] array, int offset) {
      if (array == null) {
        this.stringValue = "[-1:-1]={0}";
      } else {
        StringBuilder sb = new StringBuilder("[" + offset + ":"
            + (offset + array.length - 1) + "]={");
        for (int i = 0; i < array.length; i++) {
          sb.append(i > 0 ? "," : "").append(array[i]);
        }
        sb.append('}');
        this.stringValue = sb.toString();
      }
    }

    public String toString() {
      return stringValue;
    }

    public String getBaseTypeName() {
      return "int8";
    }

    /* The other methods are never called; no need to implement them. */
    public void free() {
      throw new UnsupportedOperationException();
    }

    public Object getArray() {
      throw new UnsupportedOperationException();
    }

    public Object getArray(long index, int count) {
      throw new UnsupportedOperationException();
    }

    public Object getArray(long index, int count,
        Map<String, Class<?>> map) {
      throw new UnsupportedOperationException();
    }

    public Object getArray(Map<String, Class<?>> map) {
      throw new UnsupportedOperationException();
    }

    public int getBaseType() {
      throw new UnsupportedOperationException();
    }

    public ResultSet getResultSet() {
      throw new UnsupportedOperationException();
    }

    public ResultSet getResultSet(long index, int count) {
      throw new UnsupportedOperationException();
    }

    public ResultSet getResultSet(long index, int count,
        Map<String, Class<?>> map) {
      throw new UnsupportedOperationException();
    }

    public ResultSet getResultSet(Map<String, Class<?>> map) {
      throw new UnsupportedOperationException();
    }
  }

  /** Inserts a bandwidth history into database. */
  public void addBandwidthHistory(String fingerprint, long published,
      List<String> bandwidthHistoryStrings) {

    /* Split history lines by date and rewrite them so that the date
     * comes first. */
    SortedSet<String> historyLinesByDate = new TreeSet<>();
    for (String bandwidthHistoryString : bandwidthHistoryStrings) {
      String[] parts = bandwidthHistoryString.split(" ");
      if (parts.length != 6) {
        logger.debug("Bandwidth history line does not have expected "
            + "number of elements. Ignoring this line.");
        continue;
      }
      long intervalLength;
      try {
        intervalLength = Long.parseLong(parts[3].substring(1));
      } catch (NumberFormatException e) {
        logger.debug("Bandwidth history line does not have valid interval "
            + "length '{} {}'. Ignoring this line.", parts[3], parts[4]);
        continue;
      }
      String[] values = parts[5].split(",");
      if (intervalLength % 900L != 0L) {
        logger.debug("Bandwidth history line does not contain "
            + "multiples of 15-minute intervals. Ignoring this line.");
        continue;
      } else if (intervalLength != 900L) {
        /* This is a really dirty hack to support bandwidth history
         * intervals that are longer than 15 minutes by linearly
         * distributing reported bytes to 15 minute intervals.  The
         * alternative would have been to modify the database schema. */
        try {
          long factor = intervalLength / 900L;
          String[] newValues = new String[values.length * (int) factor];
          for (int i = 0; i < newValues.length; i++) {
            newValues[i] = String.valueOf(
                Long.parseLong(values[i / (int) factor]) / factor);
          }
          values = newValues;
          intervalLength = 900L;
        } catch (NumberFormatException e) {
          logger.debug("Number format exception while parsing "
              + "bandwidth history line. Ignoring this line.");
          continue;
        }
      }
      String type = parts[0];
      String intervalEndTime = parts[1] + " " + parts[2];
      long intervalEnd;
      long dateStart;
      try {
        intervalEnd = dateTimeFormat.parse(intervalEndTime).getTime();
        dateStart = dateTimeFormat.parse(parts[1] + " 00:00:00")
            .getTime();
      } catch (ParseException e) {
        logger.debug("Parse exception while parsing timestamp in "
            + "bandwidth history line. Ignoring this line.");
        continue;
      }
      if (Math.abs(published - intervalEnd)
          > 7L * 24L * 60L * 60L * 1000L) {
        logger.debug("Extra-info descriptor publication time {} and last "
            + "interval time {} in {} line differ by more than 7 days! Not "
            + "adding this line!", dateTimeFormat.format(published),
            intervalEndTime, type);
        continue;
      }
      long currentIntervalEnd = intervalEnd;
      StringBuilder sb = new StringBuilder();
      SortedSet<String> newHistoryLines = new TreeSet<>();
      try {
        for (int i = values.length - 1; i >= -1; i--) {
          if (i == -1 || currentIntervalEnd < dateStart) {
            sb.insert(0, intervalEndTime + " " + type + " ("
                + intervalLength + " s) ");
            sb.setLength(sb.length() - 1);
            String historyLine = sb.toString();
            newHistoryLines.add(historyLine);
            sb = new StringBuilder();
            dateStart -= 24L * 60L * 60L * 1000L;
            intervalEndTime = dateTimeFormat.format(currentIntervalEnd);
          }
          if (i == -1) {
            break;
          }
          Long.parseLong(values[i]);
          sb.insert(0, values[i] + ",");
          currentIntervalEnd -= intervalLength * 1000L;
        }
      } catch (NumberFormatException e) {
        logger.debug("Number format exception while parsing "
            + "bandwidth history line. Ignoring this line.");
        continue;
      }
      historyLinesByDate.addAll(newHistoryLines);
    }

    /* Add split history lines to database. */
    String lastDate = null;
    historyLinesByDate.add("EOL");
    long[] readArray = null;
    long[] writtenArray = null;
    long[] dirreadArray = null;
    long[] dirwrittenArray = null;
    int readOffset = 0;
    int writtenOffset = 0;
    int dirreadOffset = 0;
    int dirwrittenOffset = 0;
    for (String historyLine : historyLinesByDate) {
      String[] parts = historyLine.split(" ");
      String currentDate = parts[0];
      if (lastDate != null && (historyLine.equals("EOL")
          || !currentDate.equals(lastDate))) {
        BigIntArray readIntArray = new BigIntArray(readArray,
            readOffset);
        BigIntArray writtenIntArray = new BigIntArray(writtenArray,
            writtenOffset);
        BigIntArray dirreadIntArray = new BigIntArray(dirreadArray,
            dirreadOffset);
        BigIntArray dirwrittenIntArray = new BigIntArray(dirwrittenArray,
            dirwrittenOffset);
        if (this.importIntoDatabase) {
          try {
            long dateMillis = dateTimeFormat.parse(lastDate
                + " 00:00:00").getTime();
            this.addDateToScheduledUpdates(dateMillis);
            this.csH.setString(1, fingerprint);
            this.csH.setDate(2, new java.sql.Date(dateMillis));
            this.csH.setArray(3, readIntArray);
            this.csH.setArray(4, writtenIntArray);
            this.csH.setArray(5, dirreadIntArray);
            this.csH.setArray(6, dirwrittenIntArray);
            this.csH.addBatch();
            rhsCount++;
            if (rhsCount % autoCommitCount == 0)  {
              this.csH.executeBatch();
            }
          } catch (SQLException | ParseException e) {
            logger.warn("Could not insert bandwidth "
                + "history line into database.  We won't make any "
                + "further SQL requests in this execution.", e);
            this.importIntoDatabase = false;
          }
        }
        readArray = writtenArray = dirreadArray = dirwrittenArray = null;
      }
      if (historyLine.equals("EOL")) {
        break;
      }
      long lastIntervalTime;
      try {
        lastIntervalTime = dateTimeFormat.parse(parts[0] + " "
            + parts[1]).getTime() - dateTimeFormat.parse(parts[0]
            + " 00:00:00").getTime();
      } catch (ParseException e) {
        continue;
      }
      String[] stringValues = parts[5].split(",");
      long[] longValues = new long[stringValues.length];
      for (int i = 0; i < longValues.length; i++) {
        longValues[i] = Long.parseLong(stringValues[i]);
      }

      int offset = (int) (lastIntervalTime / (15L * 60L * 1000L))
          - longValues.length + 1;
      String type = parts[2];
      switch (type) {
        case "read-history":
          readArray = longValues;
          readOffset = offset;
          break;
        case "write-history":
          writtenArray = longValues;
          writtenOffset = offset;
          break;
        case "dirreq-read-history":
          dirreadArray = longValues;
          dirreadOffset = offset;
          break;
        case "dirreq-write-history":
          dirwrittenArray = longValues;
          dirwrittenOffset = offset;
          break;
        default:
          /* Ignore any other types. */
      }
      lastDate = currentDate;
    }
  }

  /** Imports relay descriptors into the database. */
  public void importRelayDescriptors() {
    DescriptorReader reader =
        DescriptorSourceFactory.createDescriptorReader();
    reader.setMaxDescriptorsInQueue(10);
    reader.setHistoryFile(this.historyFile);
    for (Descriptor descriptor : reader.readDescriptors(
        this.descriptorDirectories)) {
      if (descriptor instanceof RelayNetworkStatusConsensus) {
        this.addRelayNetworkStatusConsensus(
            (RelayNetworkStatusConsensus) descriptor);
      } else if (descriptor instanceof ExtraInfoDescriptor) {
        this.addExtraInfoDescriptor((ExtraInfoDescriptor) descriptor);
      }
    }
    this.commit();
    reader.saveHistoryFile(this.historyFile);
  }

  private void addRelayNetworkStatusConsensus(
      RelayNetworkStatusConsensus consensus) {
    for (NetworkStatusEntry statusEntry
        : consensus.getStatusEntries().values()) {
      this.addStatusEntryContents(consensus.getValidAfterMillis(),
          statusEntry.getFingerprint().toLowerCase(), statusEntry.getFlags());
    }
  }

  private void addExtraInfoDescriptor(ExtraInfoDescriptor descriptor) {
    List<String> bandwidthHistoryLines = new ArrayList<>();
    if (descriptor.getWriteHistory() != null) {
      bandwidthHistoryLines.add(descriptor.getWriteHistory().getLine());
    }
    if (descriptor.getReadHistory() != null) {
      bandwidthHistoryLines.add(descriptor.getReadHistory().getLine());
    }
    if (descriptor.getDirreqWriteHistory() != null) {
      bandwidthHistoryLines.add(
          descriptor.getDirreqWriteHistory().getLine());
    }
    if (descriptor.getDirreqReadHistory() != null) {
      bandwidthHistoryLines.add(
          descriptor.getDirreqReadHistory().getLine());
    }
    this.addExtraInfoDescriptorContents(
        descriptor.getFingerprint().toLowerCase(),
        descriptor.getPublishedMillis(), bandwidthHistoryLines);
  }

  /**
   * Commit any non-commited parts.
   */
  public void commit() {

    /* Log stats about imported descriptors. */
    logger.info("Finished importing relay descriptors: {} network status "
        + "entries and {} bandwidth history elements", rrsCount, rhsCount);

    /* Insert scheduled updates a second time, just in case the refresh
     * run has started since inserting them the first time in which case
     * it will miss the data inserted afterwards.  We cannot, however,
     * insert them only now, because if a Java execution fails at a random
     * point, we might have added data, but not the corresponding dates to
     * update statistics. */
    if (this.importIntoDatabase) {
      try {
        for (long dateMillis : this.scheduledUpdates) {
          this.psU.setDate(1, new java.sql.Date(dateMillis));
          this.psU.execute();
        }
      } catch (SQLException e) {
        logger.warn("Could not add scheduled dates "
            + "for the next refresh run.", e);
      }
    }

    /* Commit any stragglers. */
    if (this.conn != null) {
      try {
        this.csH.executeBatch();

        this.conn.commit();
      } catch (SQLException e) {
        logger.warn("Could not commit final records to database", e);
      }
    }
  }

  /** Call the refresh_all() function to aggregate newly imported data. */
  void aggregate() throws SQLException {
    Statement st = this.conn.createStatement();
    st.executeQuery("SELECT refresh_all()");
    this.commit();
  }

  /** Query the servers_platforms view. */
  List<String[]> queryBandwidth() throws SQLException {
    List<String[]> statistics = new ArrayList<>();
    String columns = "date, isexit, isguard, bwread, bwwrite, dirread, "
        + "dirwrite, dirauthread, dirauthwrite";
    statistics.add(columns.split(", "));
    Statement st = this.conn.createStatement();
    String queryString = "SELECT " + columns + " FROM stats_bandwidth";
    try (ResultSet rs = st.executeQuery(queryString)) {
      while (rs.next()) {
        String[] outputLine = new String[9];
        outputLine[0] = rs.getDate("date").toLocalDate().toString();
        outputLine[1] = getBooleanFromResultSet(rs, "isexit");
        outputLine[2] = getBooleanFromResultSet(rs, "isguard");
        outputLine[3] = getLongFromResultSet(rs, "bwread");
        outputLine[4] = getLongFromResultSet(rs, "bwwrite");
        outputLine[5] = getLongFromResultSet(rs, "dirread");
        outputLine[6] = getLongFromResultSet(rs, "dirwrite");
        outputLine[7] = getLongFromResultSet(rs, "dirauthread");
        outputLine[8] = getLongFromResultSet(rs, "dirauthwrite");
        statistics.add(outputLine);
      }
    }
    return statistics;
  }

  /** Retrieve the {@code boolean} value of the designated column in the
   * current row of the given {@code ResultSet} object and format it as a
   * {@code String} object with {@code "t"} for {@code true} and
   * {@code "f"} for {@code false}, or return {@code null} if the
   * retrieved value was {@code NULL}. */
  private static String getBooleanFromResultSet(ResultSet rs,
      String columnLabel) throws SQLException {
    boolean result = rs.getBoolean(columnLabel);
    if (rs.wasNull()) {
      return null;
    } else {
      return result ? "t" : "f";
    }
  }

  /** Retrieve the {@code long} value of the designated column in the
   * current row of the given {@code ResultSet} object and format it as a
   * {@code String} object, or return {@code null} if the retrieved
   * value was {@code NULL}. */
  private static String getLongFromResultSet(ResultSet rs, String columnLabel)
      throws SQLException {
    long result = rs.getLong(columnLabel);
    return rs.wasNull() ? null : String.valueOf(result);
  }

  /**
   * Close the relay descriptor database connection.
   */
  public void closeConnection() {
    try {
      this.conn.close();
    } catch (SQLException e) {
      logger.warn("Could not close database connection.", e);
    }
  }
}

