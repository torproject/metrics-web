/* Copyright 2011--2018 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.stats.servers;

import org.torproject.descriptor.BridgeNetworkStatus;
import org.torproject.descriptor.Descriptor;
import org.torproject.descriptor.DescriptorReader;
import org.torproject.descriptor.DescriptorSourceFactory;
import org.torproject.descriptor.NetworkStatusEntry;

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
import java.util.Map;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Generates statistics on the average number of relays and bridges per
 * day. Accepts parse results from {@code RelayDescriptorParser} and
 * {@code BridgeDescriptorParser} and stores them in intermediate
 * result files {@code stats/consensus-stats-raw} and
 * {@code stats/bridge-consensus-stats-raw}. Writes final results to
 * {@code stats/consensus-stats} for all days for which at least half
 * of the expected consensuses or statuses are known.
 */
public class ConsensusStatsFileHandler {

  /**
   * Intermediate results file holding the number of running bridges per
   * bridge status.
   */
  private File bridgeConsensusStatsRawFile;

  /**
   * Number of running bridges in a given bridge status. Map keys are the bridge
   * status time formatted as "yyyy-MM-dd HH:mm:ss", a comma, and the bridge
   * authority nickname, map values are lines as read from
   * {@code stats/bridge-consensus-stats-raw}.
   */
  private SortedMap<String, String> bridgesRaw;

  /**
   * Average number of running bridges per day. Map keys are dates
   * formatted as "yyyy-MM-dd", map values are the remaining columns as written
   * to {@code stats/consensus-stats}.
   */
  private SortedMap<String, String> bridgesPerDay;

  /**
   * Logger for this class.
   */
  private Logger logger;

  private int bridgeResultsAdded = 0;

  /* Database connection string. */
  private String connectionUrl;

  private SimpleDateFormat dateTimeFormat;

  private File bridgesDir;

  private File statsDirectory;

  private boolean keepImportHistory;

  /**
   * Initializes this class, including reading in intermediate results
   * files {@code stats/consensus-stats-raw} and
   * {@code stats/bridge-consensus-stats-raw} and final results file
   * {@code stats/consensus-stats}.
   */
  public ConsensusStatsFileHandler(String connectionUrl,
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
    this.bridgesPerDay = new TreeMap<>();
    this.bridgesRaw = new TreeMap<>();

    /* Initialize file names for intermediate and final results files. */
    this.bridgeConsensusStatsRawFile = new File(
        "stats/bridge-consensus-stats-raw");

    /* Initialize database connection string. */
    this.connectionUrl = connectionUrl;

    this.dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    this.dateTimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

    /* Initialize logger. */
    this.logger = Logger.getLogger(
        ConsensusStatsFileHandler.class.getName());

    /* Read in number of running bridges per bridge status. */
    if (this.bridgeConsensusStatsRawFile.exists()) {
      this.logger.fine("Reading file "
          + this.bridgeConsensusStatsRawFile.getAbsolutePath() + "...");
      try (BufferedReader br = new BufferedReader(new FileReader(
          this.bridgeConsensusStatsRawFile))) {
        String line;
        while ((line = br.readLine()) != null) {
          if (line.startsWith("date")) {
            /* Skip headers. */
            continue;
          }
          String[] parts = line.split(",");
          if (parts.length < 2 || parts.length > 4) {
            this.logger.warning("Corrupt line '" + line + "' in file "
                + this.bridgeConsensusStatsRawFile.getAbsolutePath()
                + "! Aborting to read this file!");
            break;
          }
          /* Assume that all lines without authority nickname are based on
           * Tonga's network status, not Bifroest's. */
          String key = parts[0] + "," + (parts.length < 4 ? "Tonga" : parts[1]);
          String value = null;
          if (parts.length == 2) {
            value = key + "," + parts[1] + ",0";
          } else if (parts.length == 3) {
            value = key + "," + parts[1] + "," + parts[2];
          } else if (parts.length == 4) {
            value = key + "," + parts[2] + "," + parts[3];
          } /* No more cases as we already checked the range above. */
          this.bridgesRaw.put(key, value);
        }
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
  public void addBridgeConsensusResults(long publishedMillis,
      String authorityNickname, int running, int runningEc2Bridges) {
    String publishedAuthority = dateTimeFormat.format(publishedMillis) + ","
        + authorityNickname;
    String line = publishedAuthority + "," + running + "," + runningEc2Bridges;
    if (!this.bridgesRaw.containsKey(publishedAuthority)) {
      this.logger.finer("Adding new bridge numbers: " + line);
      this.bridgesRaw.put(publishedAuthority, line);
      this.bridgeResultsAdded++;
    } else if (!line.equals(this.bridgesRaw.get(publishedAuthority))) {
      this.logger.warning("The numbers of running bridges we were just "
          + "given (" + line + ") are different from what we learned "
          + "before (" + this.bridgesRaw.get(publishedAuthority) + ")! "
          + "Overwriting!");
      this.bridgesRaw.put(publishedAuthority, line);
    }
  }

  /** Imports sanitized bridge descriptors. */
  public void importSanitizedBridges() {
    if (bridgesDir.exists()) {
      logger.fine("Importing files in directory " + bridgesDir + "/...");
      DescriptorReader reader =
          DescriptorSourceFactory.createDescriptorReader();
      File historyFile = new File(statsDirectory,
          "consensus-stats-bridge-descriptor-history");
      if (keepImportHistory) {
        reader.setHistoryFile(historyFile);
      }
      for (Descriptor descriptor : reader.readDescriptors(bridgesDir)) {
        if (descriptor instanceof BridgeNetworkStatus) {
          String descriptorFileName = descriptor.getDescriptorFile().getName();
          String authority = null;
          if (descriptorFileName.contains(
              "4A0CCD2DDC7995083D73F5D667100C8A5831F16D")) {
            authority = "Tonga";
          } else if (descriptorFileName.contains(
              "1D8F3A91C37C5D1C4C19B1AD1D0CFBE8BF72D8E1")) {
            authority = "Bifroest";
          } else if (descriptorFileName.contains(
              "BA44A889E64B93FAA2B114E02C2A279A8555C533")) {
            authority = "Serge";
          }
          if (authority == null) {
            this.logger.warning("Did not recognize the bridge authority "
                + "that generated " + descriptorFileName + ".  Skipping.");
            continue;
          }
          this.addBridgeNetworkStatus(
              (BridgeNetworkStatus) descriptor, authority);
        }
      }
      if (keepImportHistory) {
        reader.saveHistoryFile(historyFile);
      }
      logger.info("Finished importing bridge descriptors.");
    }
  }

  private void addBridgeNetworkStatus(BridgeNetworkStatus status,
      String authority) {
    int runningBridges = 0;
    int runningEc2Bridges = 0;
    for (NetworkStatusEntry statusEntry
        : status.getStatusEntries().values()) {
      if (statusEntry.getFlags().contains("Running")) {
        runningBridges++;
        if (statusEntry.getNickname().startsWith("ec2bridge")) {
          runningEc2Bridges++;
        }
      }
    }
    this.addBridgeConsensusResults(status.getPublishedMillis(), authority,
        runningBridges, runningEc2Bridges);
  }

  /**
   * Aggregates the raw observations on relay and bridge numbers and
   * writes both raw and aggregate observations to disk.
   */
  public void writeFiles() {

    /* Go through raw observations and put everything into nested maps by day
     * and bridge authority. */
    Map<String, Map<String, int[]>> bridgesPerDayAndAuthority = new HashMap<>();
    for (String bridgesRawLine : this.bridgesRaw.values()) {
      String[] parts = bridgesRawLine.split(",");
      int brunning = Integer.parseInt(parts[2]);
      if (brunning <= 0) {
        /* Skip this status which contains zero bridges with the Running
         * flag. */
        continue;
      }
      String date = bridgesRawLine.substring(0, 10);
      if (!bridgesPerDayAndAuthority.containsKey(date)) {
        bridgesPerDayAndAuthority.put(date, new TreeMap<>());
      }
      String authority = parts[1];
      if (!bridgesPerDayAndAuthority.get(date).containsKey(authority)) {
        bridgesPerDayAndAuthority.get(date).put(authority, new int[3]);
      }
      int[] bridges = bridgesPerDayAndAuthority.get(date).get(authority);
      bridges[0] += brunning;
      bridges[1] += Integer.parseInt(parts[3]);
      bridges[2]++;
    }

    /* Sum up average numbers of running bridges per day reported by all bridge
     * authorities and add these averages to final results. */
    for (Map.Entry<String, Map<String, int[]>> perDay
        : bridgesPerDayAndAuthority.entrySet()) {
      String date = perDay.getKey();
      int brunning = 0;
      int brunningEc2 = 0;
      for (int[] perAuthority : perDay.getValue().values()) {
        int statuses = perAuthority[2];
        if (statuses < 12) {
          /* Only write results if we have seen at least a dozen statuses. */
          continue;
        }
        brunning += perAuthority[0] / statuses;
        brunningEc2 += perAuthority[1] / statuses;
      }
      String line = "," + brunning + "," + brunningEc2;
      /* Are our results new? */
      if (!this.bridgesPerDay.containsKey(date)) {
        this.logger.finer("Adding new average bridge numbers: " + date + line);
        this.bridgesPerDay.put(date, line);
      } else if (!line.equals(this.bridgesPerDay.get(date))) {
        this.logger.finer("Replacing existing average bridge numbers ("
            + this.bridgesPerDay.get(date) + " with new numbers: " + line);
        this.bridgesPerDay.put(date, line);
      }
    }

    /* Write raw numbers of running bridges to disk. */
    this.logger.fine("Writing file "
        + this.bridgeConsensusStatsRawFile.getAbsolutePath() + "...");
    this.bridgeConsensusStatsRawFile.getParentFile().mkdirs();
    try (BufferedWriter bw = new BufferedWriter(
        new FileWriter(this.bridgeConsensusStatsRawFile))) {
      bw.append("datetime,authority,brunning,brunningec2");
      bw.newLine();
      for (String line : this.bridgesRaw.values()) {
        bw.append(line);
        bw.newLine();
      }
      this.logger.fine("Finished writing file "
          + this.bridgeConsensusStatsRawFile.getAbsolutePath() + ".");
    } catch (IOException e) {
      this.logger.log(Level.WARNING, "Failed to write file "
          + this.bridgeConsensusStatsRawFile.getAbsolutePath() + "!",
          e);
    }

    /* Add average number of bridges per day to the database. */
    if (connectionUrl != null) {
      try {
        Map<String, String> updateRows = new HashMap<>();
        Map<String, String> insertRows = new HashMap<>(this.bridgesPerDay);
        Connection conn = DriverManager.getConnection(connectionUrl);
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
            if (newAvgRunning != oldAvgRunning
                || newAvgRunningEc2 != oldAvgRunningEc2) {
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
      dumpStats.append("\nLast known bridge status was published ")
          .append(this.bridgesRaw.lastKey()).append(".");
      try {
        if (now - 6L * 60L * 60L * 1000L > dateTimeFormat.parse(
            this.bridgesRaw.lastKey()).getTime()) {
          logger.warning("Last known bridge status is more than 6 hours "
              + "old: " + this.bridgesRaw.lastKey());
        }
      } catch (ParseException e) {
        logger.warning("Can't parse the timestamp? Reason: " + e);
      }
    }
    logger.info(dumpStats.toString());
  }
}

