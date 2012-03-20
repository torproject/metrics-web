/* Copyright 2011 The Tor Project
 * See LICENSE for licensing information */
package org.torproject.ernie.cron;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Generates statistics on the average number of relays and bridges per
 * day. Accepts parse results from <code>RelayDescriptorParser</code> and
 * <code>BridgeDescriptorParser</code> and stores them in intermediate
 * result files <code>stats/consensus-stats-raw</code> and
 * <code>stats/bridge-consensus-stats-raw</code>. Writes final results to
 * <code>stats/consensus-stats</code> for all days for which at least half
 * of the expected consensuses or statuses are known.
 */
public class ConsensusStatsFileHandler {

  /**
   * Intermediate results file holding the number of running bridges per
   * bridge status.
   */
  private File bridgeConsensusStatsRawFile;

  /**
   * Number of running bridges in a given bridge status. Map keys are
   * bridge status times formatted as "yyyy-MM-dd HH:mm:ss", map values
   * are lines as read from <code>stats/bridge-consensus-stats-raw</code>.
   */
  private SortedMap<String, String> bridgesRaw;

  /**
   * Average number of running bridges per day. Map keys are dates
   * formatted as "yyyy-MM-dd", map values are the last column as written
   * to <code>stats/consensus-stats</code>.
   */
  private SortedMap<String, String> bridgesPerDay;

  /**
   * Logger for this class.
   */
  private Logger logger;

  private int bridgeResultsAdded = 0;

  /* Database connection string. */
  private String connectionURL = null;

 /**
  * Initializes this class, including reading in intermediate results
  * files <code>stats/consensus-stats-raw</code> and
  * <code>stats/bridge-consensus-stats-raw</code> and final results file
  * <code>stats/consensus-stats</code>.
  */
  public ConsensusStatsFileHandler(String connectionURL) {

    /* Initialize local data structures to hold intermediate and final
     * results. */
    this.bridgesPerDay = new TreeMap<String, String>();
    this.bridgesRaw = new TreeMap<String, String>();

    /* Initialize file names for intermediate and final results files. */
    this.bridgeConsensusStatsRawFile = new File(
        "stats/bridge-consensus-stats-raw");

    /* Initialize database connection string. */
    this.connectionURL = connectionURL;

    /* Initialize logger. */
    this.logger = Logger.getLogger(
        ConsensusStatsFileHandler.class.getName());

    /* Read in number of running bridges per bridge status. */
    if (this.bridgeConsensusStatsRawFile.exists()) {
      try {
        this.logger.fine("Reading file "
            + this.bridgeConsensusStatsRawFile.getAbsolutePath() + "...");
        BufferedReader br = new BufferedReader(new FileReader(
            this.bridgeConsensusStatsRawFile));
        String line = null;
        while ((line = br.readLine()) != null) {
          if (line.startsWith("date")) {
            /* Skip headers. */
            continue;
          }
          String[] parts = line.split(",");
          if (parts.length != 2) {
            this.logger.warning("Corrupt line '" + line + "' in file "
                + this.bridgeConsensusStatsRawFile.getAbsolutePath()
                + "! Aborting to read this file!");
            break;
          }
          String dateTime = parts[0];
          this.bridgesRaw.put(dateTime, line);
        }
        br.close();
        this.logger.fine("Finished reading file "
            + this.bridgeConsensusStatsRawFile.getAbsolutePath() + ".");
      } catch (IOException e) {
        this.logger.log(Level.WARNING, "Failed to read file "
            + this.bridgeConsensusStatsRawFile.getAbsolutePath() + "!",
            e);
      }
    }
  }

  /**
   * Adds the intermediate results of the number of running bridges in a
   * given bridge status to the existing observations.
   */
  public void addBridgeConsensusResults(String published, int running) {
    String line = published + "," + running;
    if (!this.bridgesRaw.containsKey(published)) {
      this.logger.finer("Adding new bridge numbers: " + line);
      this.bridgesRaw.put(published, line);
      this.bridgeResultsAdded++;
    } else if (!line.equals(this.bridgesRaw.get(published))) {
      this.logger.warning("The numbers of running bridges we were just "
        + "given (" + line + ") are different from what we learned "
        + "before (" + this.bridgesRaw.get(published) + ")! "
        + "Overwriting!");
      this.bridgesRaw.put(published, line);
    }
  }

  /**
   * Aggregates the raw observations on relay and bridge numbers and
   * writes both raw and aggregate observations to disk.
   */
  public void writeFiles() {

    /* Go through raw observations of numbers of running bridges in bridge
     * statuses, calculate averages per day, and add these averages to
     * final results. */
    if (!this.bridgesRaw.isEmpty()) {
      String tempDate = null;
      int brunning = 0, statuses = 0;
      Iterator<String> it = this.bridgesRaw.values().iterator();
      boolean haveWrittenFinalLine = false;
      while (it.hasNext() || !haveWrittenFinalLine) {
        String next = it.hasNext() ? it.next() : null;
        /* Finished reading a day or even all lines? */
        if (tempDate != null && (next == null
            || !next.substring(0, 10).equals(tempDate))) {
          /* Only write results if we have seen at least half of all
           * statuses. */
          if (statuses >= 24) {
            String line = "," + (brunning / statuses);
            /* Are our results new? */
            if (!this.bridgesPerDay.containsKey(tempDate)) {
              this.logger.finer("Adding new average bridge numbers: "
                  + tempDate + line);
              this.bridgesPerDay.put(tempDate, line);
            } else if (!line.equals(this.bridgesPerDay.get(tempDate))) {
              this.logger.finer("Replacing existing average bridge "
                  + "numbers (" + this.bridgesPerDay.get(tempDate)
                  + " with new numbers: " + line);
              this.bridgesPerDay.put(tempDate, line);
            }
          }
          brunning = statuses = 0;
          haveWrittenFinalLine = (next == null);
        }
        /* Sum up number of running bridges. */
        if (next != null) {
          tempDate = next.substring(0, 10);
          statuses++;
          brunning += Integer.parseInt(next.split(",")[1]);
        }
      }
    }

    /* Write raw numbers of running bridges to disk. */
    try {
      this.logger.fine("Writing file "
          + this.bridgeConsensusStatsRawFile.getAbsolutePath() + "...");
      this.bridgeConsensusStatsRawFile.getParentFile().mkdirs();
      BufferedWriter bw = new BufferedWriter(
          new FileWriter(this.bridgeConsensusStatsRawFile));
      bw.append("datetime,brunning\n");
      for (String line : this.bridgesRaw.values()) {
        bw.append(line + "\n");
      }
      bw.close();
      this.logger.fine("Finished writing file "
          + this.bridgeConsensusStatsRawFile.getAbsolutePath() + ".");
    } catch (IOException e) {
      this.logger.log(Level.WARNING, "Failed to write file "
          + this.bridgeConsensusStatsRawFile.getAbsolutePath() + "!",
          e);
    }

    /* Add average number of bridges per day to the database. */
    if (connectionURL != null) {
      try {
        Map<String, String> insertRows = new HashMap<String, String>(),
            updateRows = new HashMap<String, String>();
        insertRows.putAll(this.bridgesPerDay);
        Connection conn = DriverManager.getConnection(connectionURL);
        conn.setAutoCommit(false);
        Statement statement = conn.createStatement();
        ResultSet rs = statement.executeQuery(
            "SELECT date, avg_running FROM bridge_network_size");
        while (rs.next()) {
          String date = rs.getDate(1).toString();
          if (insertRows.containsKey(date)) {
            String insertRow = insertRows.remove(date);
            long newAvgRunning = Long.parseLong(insertRow.substring(1));
            long oldAvgRunning = rs.getLong(2);
            if (newAvgRunning != oldAvgRunning) {
              updateRows.put(date, insertRow);
            }
          }
        }
        rs.close();
        PreparedStatement psU = conn.prepareStatement(
            "UPDATE bridge_network_size SET avg_running = ? "
            + "WHERE date = ?");
        for (Map.Entry<String, String> e : updateRows.entrySet()) {
          java.sql.Date date = java.sql.Date.valueOf(e.getKey());
          long avgRunning = Long.parseLong(e.getValue().substring(1));
          psU.clearParameters();
          psU.setLong(1, avgRunning);
          psU.setDate(2, date);
          psU.executeUpdate();
        }
        PreparedStatement psI = conn.prepareStatement(
            "INSERT INTO bridge_network_size (avg_running, date) "
            + "VALUES (?, ?)");
        for (Map.Entry<String, String> e : insertRows.entrySet()) {
          java.sql.Date date = java.sql.Date.valueOf(e.getKey());
          long avgRunning = Long.parseLong(e.getValue().substring(1));
          psI.clearParameters();
          psI.setLong(1, avgRunning);
          psI.setDate(2, date);
          psI.executeUpdate();
        }
        conn.commit();
        conn.close();
      } catch (SQLException e) {
        logger.log(Level.WARNING, "Failed to add average bridge numbers "
            + "to database.", e);
      }
    }

    /* Write stats. */
    StringBuilder dumpStats = new StringBuilder("Finished writing "
        + "statistics on bridge network statuses to disk.\nAdded "
        + this.bridgeResultsAdded + " bridge network status(es) in this "
        + "execution.");
    long now = System.currentTimeMillis();
    SimpleDateFormat dateTimeFormat =
        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    dateTimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    if (this.bridgesRaw.isEmpty()) {
      dumpStats.append("\nNo bridge status known yet.");
    } else {
      dumpStats.append("\nLast known bridge status was published "
          + this.bridgesRaw.lastKey() + ".");
      try {
        if (now - 6L * 60L * 60L * 1000L > dateTimeFormat.parse(
            this.bridgesRaw.lastKey()).getTime()) {
          logger.warning("Last known bridge status is more than 6 hours "
              + "old: " + this.bridgesRaw.lastKey());
        }
      } catch (ParseException e) {
         /* Can't parse the timestamp? Whatever. */
      }
    }
    logger.info(dumpStats.toString());
  }
}

