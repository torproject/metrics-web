/* Copyright 2011, 2012 The Tor Project
 * See LICENSE for licensing information */
package org.torproject.ernie.cron.network;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import org.torproject.ernie.cron.Configuration;

/**
 * Import a Maxmind GeoLite City database to resolve resolve IP addresses
 * to country codes, latitudes, and longitudes.
 */
public class GeoipDatabaseImporter {
  public static void main(String[] args) throws IOException,
      SQLException {

    /* Check if the GeoIP database files are in place. */
    File locationsFile = new File("GeoLiteCity-Location.csv"),
        blocksFile = new File("GeoLiteCity-Blocks.csv");
    if (!locationsFile.exists() || !blocksFile.exists()) {
      System.out.println("Could not find GeoLiteCity-Location.csv and/or "
          + "GeoLiteCity-Blocks.csv in the working directory! Exiting!");
      System.exit(1);
    }

    /* Initialize configuration to learn JDBC string. */
    Configuration config = new Configuration();
    String jdbcString = config.getRelayDescriptorDatabaseJDBC();

    /* Connect to database. */
    Connection c = DriverManager.getConnection(jdbcString);

    /* Start by reading location information to memory. */
    BufferedReader br = new BufferedReader(new FileReader(locationsFile));
    String line;
    Map<Integer, String> locations = new HashMap<Integer, String>();
    while ((line = br.readLine()) != null) {
      if (line.startsWith("Copyright") || line.startsWith("locId")) {
        continue;
      }
      String[] parts = line.split(",");
      int locId = Integer.parseInt(parts[0]);
      String country = parts[1].replaceAll("\"", "");
      String latitude = parts[5];
      String longitude = parts[6];
      locations.put(locId, country + "," + latitude + "," + longitude);
    }
    br.close();

    /* Parse block information and add it to the database together with
     * the location information. */
    PreparedStatement ps = c.prepareStatement("INSERT INTO geoipdb "
        + "(ipstart, ipend, country, latitude, longitude) VALUES "
        + "(?, ?, ?, ?, ?)");
    Statement s = c.createStatement();
    s.execute("DELETE FROM geoipdb");
    /* TODO The import takes 30+ minutes.  Perform the import in a single
     * transaction, or requests will return strange results in these 30+
     * minutes. */
    br = new BufferedReader(new FileReader(blocksFile));
    while ((line = br.readLine()) != null) {
      if (line.startsWith("Copyright") ||
          line.startsWith("startIpNum")) {
        continue;
      }
      String[] parts = line.replaceAll("\"", "").split(",");
      long startIpNum = Long.parseLong(parts[0]);
      String startIp = "" + startIpNum / 256 / 256 / 256 + "."
          + startIpNum / 256 / 256 % 256 + "." + startIpNum / 256 % 256
          + "." + startIpNum % 256;
      long endIpNum = Long.parseLong(parts[1]);
      String endIp = "" + endIpNum / 256 / 256 / 256 + "."
          + endIpNum / 256 / 256 % 256 + "." + endIpNum / 256 % 256 + "."
          + endIpNum % 256;
      int locId = Integer.parseInt(parts[2]);
      if (!locations.containsKey(locId)) {
        System.out.println("Cannot find locId=" + locId
            + " in locations file!");
        continue;
      }
      String[] locationParts = locations.get(locId).split(",");
      String country = locationParts[0];
      double latitude = Double.parseDouble(locationParts[1]);
      double longitude = Double.parseDouble(locationParts[2]);
      ps.setObject(1, startIp, Types.OTHER);
      ps.setObject(2, endIp, Types.OTHER);
      ps.setString(3, country);
      ps.setDouble(4, latitude);
      ps.setDouble(5, longitude);
      ps.execute();
    }
  }
}

