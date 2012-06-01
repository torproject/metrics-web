/* Copyright 2011, 2012 The Tor Project
 * See LICENSE for licensing information */
package org.torproject.ernie.cron;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.torproject.descriptor.Descriptor;
import org.torproject.descriptor.DescriptorFile;
import org.torproject.descriptor.DescriptorReader;
import org.torproject.descriptor.DescriptorSourceFactory;
import org.torproject.descriptor.GetTorStatistics;

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

    logger.fine("Importing GetTor stats files in directory "
        + getTorDirectory + "/...");
    DescriptorReader reader =
        DescriptorSourceFactory.createDescriptorReader();
    reader.addDirectory(getTorDirectory);
    Iterator<DescriptorFile> descriptorFiles = reader.readDescriptors();
    while (descriptorFiles.hasNext()) {
      DescriptorFile descriptorFile = descriptorFiles.next();
      if (descriptorFile.getException() != null) {
        logger.log(Level.WARNING, "Could not parse descriptor file '"
            + descriptorFile.getFileName() + "'.  Skipping.",
            descriptorFile.getException());
        continue;
      }
      if (descriptorFile.getDescriptors() != null) {
        for (Descriptor descriptor : descriptorFile.getDescriptors()) {
          if (!(descriptor instanceof GetTorStatistics)) {
            continue;
          }
          GetTorStatistics stats = (GetTorStatistics) descriptor;
          String date = dateFormat.format(stats.getDateMillis());
          Map<String, Integer> obs = new HashMap<String, Integer>();
          for (Map.Entry<String, Integer> e :
              stats.getDownloadedPackages().entrySet()) {
            columns.add(e.getKey().toLowerCase());
            obs.put(e.getKey().toLowerCase(), e.getValue());
          }
          data.put(date, obs);
        }
      }
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

