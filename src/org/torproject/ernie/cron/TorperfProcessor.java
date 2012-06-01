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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.torproject.descriptor.Descriptor;
import org.torproject.descriptor.DescriptorFile;
import org.torproject.descriptor.DescriptorReader;
import org.torproject.descriptor.DescriptorSourceFactory;
import org.torproject.descriptor.TorperfResult;

public class TorperfProcessor {
  public TorperfProcessor(File torperfDirectory, File statsDirectory,
      String connectionURL) {

    if (torperfDirectory == null || statsDirectory == null) {
      throw new IllegalArgumentException();
    }

    Logger logger = Logger.getLogger(TorperfProcessor.class.getName());
    File rawFile = new File(statsDirectory, "torperf-raw");
    File statsFile = new File(statsDirectory, "torperf-stats");
    SortedMap<String, String> rawObs = new TreeMap<String, String>();
    SortedMap<String, String> stats = new TreeMap<String, String>();
    int addedRawObs = 0;
    SimpleDateFormat formatter =
        new SimpleDateFormat("yyyy-MM-dd,HH:mm:ss");
    formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
    try {
      if (rawFile.exists()) {
        logger.fine("Reading file " + rawFile.getAbsolutePath() + "...");
        BufferedReader br = new BufferedReader(new FileReader(rawFile));
        String line = br.readLine(); // ignore header
        while ((line = br.readLine()) != null) {
          if (line.split(",").length != 4) {
            logger.warning("Corrupt line in " + rawFile.getAbsolutePath()
                + "!");
            break;
          }
          String key = line.substring(0, line.lastIndexOf(","));
          rawObs.put(key, line);
        }
        br.close();
        logger.fine("Finished reading file " + rawFile.getAbsolutePath()
            + ".");
      }
      if (statsFile.exists()) {
        logger.fine("Reading file " + statsFile.getAbsolutePath()
            + "...");
        BufferedReader br = new BufferedReader(new FileReader(statsFile));
        String line = br.readLine(); // ignore header
        while ((line = br.readLine()) != null) {
          String key = line.split(",")[0] + "," + line.split(",")[1];
          stats.put(key, line);
        }
        br.close();
        logger.fine("Finished reading file " + statsFile.getAbsolutePath()
            + ".");
      }
      if (torperfDirectory.exists()) {
        logger.fine("Importing files in " + torperfDirectory + "/...");
        DescriptorReader descriptorReader =
            DescriptorSourceFactory.createDescriptorReader();
        descriptorReader.addDirectory(torperfDirectory);
        descriptorReader.setExcludeFiles(new File(statsDirectory,
            "torperf-history"));
        Iterator<DescriptorFile> descriptorFiles =
            descriptorReader.readDescriptors();
        while (descriptorFiles.hasNext()) {
          DescriptorFile descriptorFile = descriptorFiles.next();
          if (descriptorFile.getException() != null) {
            logger.log(Level.FINE, "Error parsing file.",
                descriptorFile.getException());
            continue;
          }
          for (Descriptor descriptor : descriptorFile.getDescriptors()) {
            if (!(descriptor instanceof TorperfResult)) {
              continue;
            }
            TorperfResult result = (TorperfResult) descriptor;
            String source = result.getSource();
            long fileSize = result.getFileSize();
            if (fileSize == 51200) {
              source += "-50kb";
            } else if (fileSize == 1048576) {
              source += "-1mb";
            } else if (fileSize == 5242880) {
              source += "-5mb";
            } else {
              logger.fine("Unexpected file size '" + fileSize
                  + "'.  Skipping.");
              continue;
            }
            String dateTime = formatter.format(result.getStartMillis());
            long completeMillis = result.getDataCompleteMillis()
                - result.getStartMillis();
            String key = source + "," + dateTime;
            String value = key;
            if ((result.didTimeout() == null &&
                result.getDataCompleteMillis() < 1) ||
                (result.didTimeout() != null && result.didTimeout())) {
              value += ",-2"; // -2 for timeout
            } else if (result.getReadBytes() < fileSize) {
              value += ",-1"; // -1 for failure
            } else {
              value += "," + completeMillis;
            }
            if (!rawObs.containsKey(key)) {
              rawObs.put(key, value);
              addedRawObs++;
            }
          }
        }
        logger.fine("Finished importing files in " + torperfDirectory
            + "/.");
      }
      if (rawObs.size() > 0) {
        logger.fine("Writing file " + rawFile.getAbsolutePath() + "...");
        rawFile.getParentFile().mkdirs();
        BufferedWriter bw = new BufferedWriter(new FileWriter(rawFile));
        bw.append("source,date,start,completemillis\n");
        String tempSourceDate = null;
        Iterator<Map.Entry<String, String>> it =
            rawObs.entrySet().iterator();
        List<Long> dlTimes = new ArrayList<Long>();
        boolean haveWrittenFinalLine = false;
        SortedMap<String, List<Long>> dlTimesAllSources =
            new TreeMap<String, List<Long>>();
        SortedMap<String, long[]> statusesAllSources =
            new TreeMap<String, long[]>();
        long failures = 0, timeouts = 0, requests = 0;
        while (it.hasNext() || !haveWrittenFinalLine) {
          Map.Entry<String, String> next = it.hasNext() ? it.next() : null;
          if (tempSourceDate != null
              && (next == null || !(next.getValue().split(",")[0] + ","
              + next.getValue().split(",")[1]).equals(tempSourceDate))) {
            if (dlTimes.size() > 4) {
              Collections.sort(dlTimes);
              long q1 = dlTimes.get(dlTimes.size() / 4 - 1);
              long md = dlTimes.get(dlTimes.size() / 2 - 1);
              long q3 = dlTimes.get(dlTimes.size() * 3 / 4 - 1);
              stats.put(tempSourceDate, tempSourceDate + "," + q1 + ","
                  + md + "," + q3 + "," + timeouts + "," + failures + ","
                  + requests);
              String allSourceDate = "all" + tempSourceDate.substring(
                  tempSourceDate.indexOf("-"));
              if (dlTimesAllSources.containsKey(allSourceDate)) {
                dlTimesAllSources.get(allSourceDate).addAll(dlTimes);
              } else {
                dlTimesAllSources.put(allSourceDate, dlTimes);
              }
              if (statusesAllSources.containsKey(allSourceDate)) {
                long[] status = statusesAllSources.get(allSourceDate);
                status[0] += timeouts;
                status[1] += failures;
                status[2] += requests;
              } else {
                long[] status = new long[3];
                status[0] = timeouts;
                status[1] = failures;
                status[2] = requests;
                statusesAllSources.put(allSourceDate, status);
              }
            }
            dlTimes = new ArrayList<Long>();
            failures = timeouts = requests = 0;
            if (next == null) {
              haveWrittenFinalLine = true;
            }
          }
          if (next != null) {
            bw.append(next.getValue() + "\n");
            String[] parts = next.getValue().split(",");
            tempSourceDate = parts[0] + "," + parts[1];
            long completeMillis = Long.parseLong(parts[3]);
            if (completeMillis == -2L) {
              timeouts++;
            } else if (completeMillis == -1L) {
              failures++;
            } else {
              dlTimes.add(Long.parseLong(parts[3]));
            }
            requests++;
          }
        }
        bw.close();
        for (Map.Entry<String, List<Long>> e :
            dlTimesAllSources.entrySet()) {
          String allSourceDate = e.getKey();
          dlTimes = e.getValue();
          Collections.sort(dlTimes);
          long q1 = dlTimes.get(dlTimes.size() / 4 - 1);
          long md = dlTimes.get(dlTimes.size() / 2 - 1);
          long q3 = dlTimes.get(dlTimes.size() * 3 / 4 - 1);
          long[] status = statusesAllSources.get(allSourceDate);
          timeouts = status[0];
          failures = status[1];
          requests = status[2];
          stats.put(allSourceDate, allSourceDate + "," + q1 + "," + md
              + "," + q3 + "," + timeouts + "," + failures + ","
              + requests);
        }
        logger.fine("Finished writing file " + rawFile.getAbsolutePath()
            + ".");
      }
      if (stats.size() > 0) {
        logger.fine("Writing file " + statsFile.getAbsolutePath()
            + "...");
        statsFile.getParentFile().mkdirs();
        BufferedWriter bw = new BufferedWriter(new FileWriter(statsFile));
        bw.append("source,date,q1,md,q3,timeouts,failures,requests\n");
        for (String s : stats.values()) {
          bw.append(s + "\n");
        }
        bw.close();
        logger.fine("Finished writing file " + statsFile.getAbsolutePath()
            + ".");
      }
    } catch (IOException e) {
      logger.log(Level.WARNING, "Failed writing "
          + rawFile.getAbsolutePath() + " or "
          + statsFile.getAbsolutePath() + "!", e);
    }

    /* Write stats. */
    StringBuilder dumpStats = new StringBuilder("Finished writing "
        + "statistics on torperf results.\nAdded " + addedRawObs
        + " new observations in this execution.\n"
        + "Last known obserations by source and file size are:");
    String lastSource = null;
    String lastLine = null;
    Level logLevel = Level.INFO;
    String cutoff = formatter.format(System.currentTimeMillis()
        - 12L * 60L * 60L * 1000L).replaceAll(",", " ");
    for (String s : rawObs.keySet()) {
      String[] parts = s.split(",");
      if (lastSource == null) {
        lastSource = parts[0];
      } else if (!parts[0].equals(lastSource)) {
        String lastKnownObservation = lastLine.split(",")[1] + " "
            + lastLine.split(",")[2];
        dumpStats.append("\n" + lastSource + " " + lastKnownObservation);
        lastSource = parts[0];
        if (cutoff.compareTo(lastKnownObservation) > 0) {
          logLevel = Level.WARNING;
        }
      }
      lastLine = s;
    }
    if (lastSource != null) {
      String lastKnownObservation = lastLine.split(",")[1] + " "
          + lastLine.split(",")[2];
      dumpStats.append("\n" + lastSource + " " + lastKnownObservation);
      if (cutoff.compareTo(lastKnownObservation) > 0) {
        logLevel = Level.WARNING;
      }
    }
    logger.log(logLevel, dumpStats.toString());

    /* Write results to database. */
    if (connectionURL != null) {
      try {
        Map<String, String> insertRows = new HashMap<String, String>();
        insertRows.putAll(stats);
        Set<String> updateRows = new HashSet<String>();
        Connection conn = DriverManager.getConnection(connectionURL);
        conn.setAutoCommit(false);
        Statement statement = conn.createStatement();
        ResultSet rs = statement.executeQuery(
            "SELECT date, source, q1, md, q3, timeouts, failures, "
            + "requests FROM torperf_stats");
        while (rs.next()) {
          String date = rs.getDate(1).toString();
          String source = rs.getString(2);
          String key = source + "," + date;
          if (insertRows.containsKey(key)) {
            String insertRow = insertRows.remove(key);
            String[] newStats = insertRow.split(",");
            long newQ1 = Long.parseLong(newStats[2]);
            long newMd = Long.parseLong(newStats[3]);
            long newQ3 = Long.parseLong(newStats[4]);
            long newTimeouts = Long.parseLong(newStats[5]);
            long newFailures = Long.parseLong(newStats[6]);
            long newRequests = Long.parseLong(newStats[7]);
            long oldQ1 = rs.getLong(3);
            long oldMd = rs.getLong(4);
            long oldQ3 = rs.getLong(5);
            long oldTimeouts = rs.getLong(6);
            long oldFailures = rs.getLong(7);
            long oldRequests = rs.getLong(8);
            if (newQ1 != oldQ1 || newMd != oldMd || newQ3 != oldQ3 ||
                newTimeouts != oldTimeouts ||
                newFailures != oldFailures ||
                newRequests != oldRequests) {
              updateRows.add(insertRow);
            }
          }
        }
        PreparedStatement psU = conn.prepareStatement(
            "UPDATE torperf_stats SET q1 = ?, md = ?, q3 = ?, "
            + "timeouts = ?, failures = ?, requests = ? "
            + "WHERE date = ? AND source = ?");
        for (String row : updateRows) {
          String[] newStats = row.split(",");
          String source = newStats[0];
          java.sql.Date date = java.sql.Date.valueOf(newStats[1]);
          long q1 = Long.parseLong(newStats[2]);
          long md = Long.parseLong(newStats[3]);
          long q3 = Long.parseLong(newStats[4]);
          long timeouts = Long.parseLong(newStats[5]);
          long failures = Long.parseLong(newStats[6]);
          long requests = Long.parseLong(newStats[7]);
          psU.clearParameters();
          psU.setLong(1, q1);
          psU.setLong(2, md);
          psU.setLong(3, q3);
          psU.setLong(4, timeouts);
          psU.setLong(5, failures);
          psU.setLong(6, requests);
          psU.setDate(7, date);
          psU.setString(8, source);
          psU.executeUpdate();
        }
        PreparedStatement psI = conn.prepareStatement(
            "INSERT INTO torperf_stats (q1, md, q3, timeouts, failures, "
            + "requests, date, source) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
        for (String row : insertRows.values()) {
          String[] newStats = row.split(",");
          String source = newStats[0];
          java.sql.Date date = java.sql.Date.valueOf(newStats[1]);
          long q1 = Long.parseLong(newStats[2]);
          long md = Long.parseLong(newStats[3]);
          long q3 = Long.parseLong(newStats[4]);
          long timeouts = Long.parseLong(newStats[5]);
          long failures = Long.parseLong(newStats[6]);
          long requests = Long.parseLong(newStats[7]);
          psI.clearParameters();
          psI.setLong(1, q1);
          psI.setLong(2, md);
          psI.setLong(3, q3);
          psI.setLong(4, timeouts);
          psI.setLong(5, failures);
          psI.setLong(6, requests);
          psI.setDate(7, date);
          psI.setString(8, source);
          psI.executeUpdate();
        }
        conn.commit();
        conn.close();
      } catch (SQLException e) {
        logger.log(Level.WARNING, "Failed to add torperf stats to "
            + "database.", e);
      }
    }
  }
}

