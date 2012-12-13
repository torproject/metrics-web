/* Copyright 2011, 2012 The Tor Project
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

import org.torproject.descriptor.BridgeNetworkStatus;
import org.torproject.descriptor.Descriptor;
import org.torproject.descriptor.DescriptorFile;
import org.torproject.descriptor.DescriptorReader;
import org.torproject.descriptor.DescriptorSourceFactory;
import org.torproject.descriptor.NetworkStatusEntry;

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

  private SimpleDateFormat dateTimeFormat;

  private File bridgesDir;

  private File statsDirectory;

  private boolean keepImportHistory;

 /**
  * Initializes this class, including reading in intermediate results
  * files <code>stats/consensus-stats-raw</code> and
  * <code>stats/bridge-consensus-stats-raw</code> and final results file
  * <code>stats/consensus-stats</code>.
  */
  public ConsensusStatsFileHandler(String connectionURL,
      File bridgesDir, File statsDirectory,
      boolean keepImportHistory) {

    if (bridgesDir == null || statsDirectory == null) {
      throw new IllegalArgumentException();
    }
    this.bridgesDir = bridgesDir;
    this.statsDirectory = statsDirectory;
    this.keepImportHistory = keepImportHistory;

    /* Initialize local data structures to hold intermediate and final
     * results. */
    this.bridgesPerDay = new TreeMap<String, String>();
    this.bridgesRaw = new TreeMap<String, String>();

    /* Initialize file names for intermediate and final results files. */
    this.bridgeConsensusStatsRawFile = new File(
        "stats/bridge-consensus-stats-raw");

    /* Initialize database connection string. */
    this.connectionURL = connectionURL;

    this.dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    this.dateTimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

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
          String dateTime = parts[0];
          if (parts.length == 2) {
            this.bridgesRaw.put(dateTime, line + ",0");
          } else if (parts.length == 3) {
            this.bridgesRaw.put(dateTime, line);
          } else {
            this.logger.warning("Corrupt line '" + line + "' in file "
                + this.bridgeConsensusStatsRawFile.getAbsolutePath()
                + "! Aborting to read this file!");
            break;
          }
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
  public void addBridgeConsensusResults(long publishedMillis, int running,
      int runningEc2Bridges) {
    String published = dateTimeFormat.format(publishedMillis);
    String line = published + "," + running + "," + runningEc2Bridges;
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

  public void importSanitizedBridges() {
    if (bridgesDir.exists()) {
      logger.fine("Importing files in directory " + bridgesDir + "/...");
      DescriptorReader reader =
          DescriptorSourceFactory.createDescriptorReader();
      reader.addDirectory(bridgesDir);
      if (keepImportHistory) {
        reader.setExcludeFiles(new File(statsDirectory,
            "consensus-stats-bridge-descriptor-history"));
      }
      Iterator<DescriptorFile> descriptorFiles = reader.readDescriptors();
      while (descriptorFiles.hasNext()) {
        DescriptorFile descriptorFile = descriptorFiles.next();
        if (descriptorFile.getDescriptors() != null) {
          for (Descriptor descriptor : descriptorFile.getDescriptors()) {
            if (descriptor instanceof BridgeNetworkStatus) {
              this.addBridgeNetworkStatus(
                  (BridgeNetworkStatus) descriptor);
            }
          }
        }
      }
      logger.info("Finished importing bridge descriptors.");
    }
  }

  private void addBridgeNetworkStatus(BridgeNetworkStatus status) {
    int runningBridges = 0, runningEc2Bridges = 0;
    for (NetworkStatusEntry statusEntry :
        status.getStatusEntries().values()) {
      if (statusEntry.getFlags().contains("Running")) {
        runningBridges++;
        if (statusEntry.getNickname().startsWith("ec2bridge")) {
          runningEc2Bridges++;
        }
      }
    }
    this.addBridgeConsensusResults(status.getPublishedMillis(),
        runningBridges, runningEc2Bridges);
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
      int brunning = 0, brunningEc2 = 0, statuses = 0;
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
            String line = "," + (brunning / statuses) + ","
                + (brunningEc2 / statuses);
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
          brunning = brunningEc2 = statuses = 0;
          haveWrittenFinalLine = (next == null);
        }
        /* Sum up number of running bridges. */
        if (next != null) {
          tempDate = next.substring(0, 10);
          statuses++;
          String[] parts = next.split(",");
          brunning += Integer.parseInt(parts[1]);
          brunningEc2 += Integer.parseInt(parts[2]);
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
      bw.append("datetime,brunning,brunningec2\n");
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
            "SELECT date, avg_running, avg_running_ec2 "
            + "FROM bridge_network_size");
        while (rs.next()) {
          String date = rs.getDate(1).toString();
          if (insertRows.containsKey(date)) {
            String insertRow = insertRows.remove(date);
            String[] parts = insertRow.substring(1).split(",");
            long newAvgRunning = Long.parseLong(parts[0]);
            long newAvgRunningEc2 = Long.parseLong(parts[1]);
            long oldAvgRunning = rs.getLong(2);
            long oldAvgRunningEc2 = rs.getLong(3);
            if (newAvgRunning != oldAvgRunning ||
                newAvgRunningEc2 != oldAvgRunningEc2) {
              updateRows.put(date, insertRow);
            }
          }
        }
        rs.close();
        PreparedStatement psU = conn.prepareStatement(
            "UPDATE bridge_network_size SET avg_running = ?, "
            + "avg_running_ec2 = ? WHERE date = ?");
        for (Map.Entry<String, String> e : updateRows.entrySet()) {
          java.sql.Date date = java.sql.Date.valueOf(e.getKey());
          String[] parts = e.getValue().substring(1).split(",");
          long avgRunning = Long.parseLong(parts[0]);
          long avgRunningEc2 = Long.parseLong(parts[1]);
          psU.clearParameters();
          psU.setLong(1, avgRunning);
          psU.setLong(2, avgRunningEc2);
          psU.setDate(3, date);
          psU.executeUpdate();
        }
        PreparedStatement psI = conn.prepareStatement(
            "INSERT INTO bridge_network_size (avg_running, "
            + "avg_running_ec2, date) VALUES (?, ?, ?)");
        for (Map.Entry<String, String> e : insertRows.entrySet()) {
          java.sql.Date date = java.sql.Date.valueOf(e.getKey());
          String[] parts = e.getValue().substring(1).split(",");
          long avgRunning = Long.parseLong(parts[0]);
          long avgRunningEc2 = Long.parseLong(parts[1]);
          psI.clearParameters();
          psI.setLong(1, avgRunning);
          psI.setLong(2, avgRunningEc2);
          psI.setDate(3, date);
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

