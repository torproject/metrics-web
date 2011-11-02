/* Copyright 2011 The Tor Project
 * See LICENSE for licensing information */
package org.torproject.ernie.cron;

import java.io.*;
import java.sql.*;
import java.text.*;
import java.util.*;
import java.util.logging.*;

public class GetTorProcessor {
  public GetTorProcessor(File getTorDirectory, String connectionURL) {

    Logger logger = Logger.getLogger(GetTorProcessor.class.getName());

    /* Parse stats file. */
    File getTorFile = new File(getTorDirectory, "gettor_stats.txt");
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    if (!getTorFile.exists() || getTorFile.isDirectory()) {
      logger.warning("Could not read GetTor stats");
      return;
    }
    SortedSet<String> columns = new TreeSet<String>();
    SortedMap<String, Map<String, Integer>> data =
        new TreeMap<String, Map<String, Integer>>();
    try {
      logger.fine("Parsing GetTor stats...");
      BufferedReader br = new BufferedReader(new FileReader(getTorFile));
      String line = null;
      while ((line = br.readLine()) != null) {
        String[] parts = line.split(" ");
        String date = parts[0];
        try {
          dateFormat.parse(date);
        } catch (ParseException e) {
          logger.warning("Illegal line in GetTor stats file: '" + line
              + "'.  Skipping.");
          continue;
        }
        Map<String, Integer> obs = new HashMap<String, Integer>();
        data.put(date, obs);
        for (int i = 2; i < parts.length; i++) {
          String key = parts[i].split(":")[0].toLowerCase();
          Integer value = new Integer(parts[i].split(":")[1]);
          columns.add(key);
          obs.put(key, value);
        }
      }
      br.close();
    } catch (IOException e) {
      logger.log(Level.WARNING, "Failed parsing GetTor stats!", e);
      return;
    } catch (NumberFormatException e) {
      logger.log(Level.WARNING, "Failed parsing GetTor stats!", e);
      return;
    }

    /* Write results to database. */
    if (connectionURL != null) {
      try {
        Map<String, Integer> updateRows = new HashMap<String, Integer>(),
            insertRows = new HashMap<String, Integer>();
        for (Map.Entry<String, Map<String, Integer>> e :
            data.entrySet()) {
          String date = e.getKey();
          Map<String, Integer> obs = e.getValue();
          for (String column : columns) {
            if (obs.containsKey(column)) {
              Integer value = obs.get(column);
              String key = date + "," + column;
              insertRows.put(key, value);
            }
          }
        }
        Connection conn = DriverManager.getConnection(connectionURL);
        PreparedStatement psI = conn.prepareStatement(
            "INSERT INTO gettor_stats (downloads, date, bundle) "
            + "VALUES (?, ?, ?)");
        PreparedStatement psU = conn.prepareStatement(
            "UPDATE gettor_stats SET downloads = ? "
            + "WHERE date = ? AND bundle = ?");
        conn.setAutoCommit(false);
        Statement statement = conn.createStatement();
        ResultSet rs = statement.executeQuery(
            "SELECT date, bundle, downloads FROM gettor_stats");
        while (rs.next()) {
          String date = rs.getDate(1).toString();
          String bundle = rs.getString(2);
          String key = date + "," + bundle;
          if (insertRows.containsKey(key)) {
            int insertRow = insertRows.remove(key);
            int oldCount = rs.getInt(3);
            if (insertRow != oldCount) {
              updateRows.put(key, insertRow);
            }
          }
        }
        for (Map.Entry<String, Integer> e : updateRows.entrySet()) {
          String[] keyParts = e.getKey().split(",");
          java.sql.Date date = java.sql.Date.valueOf(keyParts[0]);
          String bundle = keyParts[1];
          int downloads = e.getValue();
          psU.clearParameters();
          psU.setLong(1, downloads);
          psU.setDate(2, date);
          psU.setString(3, bundle);
          psU.executeUpdate();
        }
        for (Map.Entry<String, Integer> e : insertRows.entrySet()) {
          String[] keyParts = e.getKey().split(",");
          java.sql.Date date = java.sql.Date.valueOf(keyParts[0]);
          String bundle = keyParts[1];
          int downloads = e.getValue();
          psI.clearParameters();
          psI.setLong(1, downloads);
          psI.setDate(2, date);
          psI.setString(3, bundle);
          psI.executeUpdate();
        }
        conn.commit();
        conn.close();
      } catch (SQLException e) {
        logger.log(Level.WARNING, "Failed to add GetTor stats to "
            + "database.", e);
      }
    }

    logger.info("Finished processing statistics on Tor packages "
        + "delivered by GetTor.\nLast date in statistics is "
        + data.lastKey() + ".");
  }
}

