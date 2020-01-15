/* Copyright 2011--2020 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.stats.bwhist;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;

/**
 * Coordinate downloading and parsing of descriptors and extraction of
 * statistically relevant data for later processing with R.
 */
public class Main {

  private static Logger log = LoggerFactory.getLogger(Main.class);

  private static String[] paths =  {
      "recent/relay-descriptors/consensuses",
      "recent/relay-descriptors/extra-infos",
      "archive/relay-descriptors/consensuses",
      "archive/relay-descriptors/extra-infos" };

  private static final String jdbcString = String.format(
      "jdbc:postgresql://localhost/tordir?user=%s&password=%s",
      System.getProperty("metrics.dbuser", "metrics"),
      System.getProperty("metrics.dbpass", "password"));

  private static final File baseDir = new File(
      org.torproject.metrics.stats.main.Main.modulesDir, "bwhist");

  /** Executes this data-processing module. */
  public static void main(String[] args) throws Exception {

    log.info("Starting bwhist module.");

    log.info("Reading descriptors and inserting relevant parts into the "
        + "database.");
    File[] descriptorDirectories = Arrays.stream(paths).map((String path)
        -> new File(org.torproject.metrics.stats.main.Main.descriptorsDir,
        path)).toArray(File[]::new);
    File historyFile = new File(baseDir, "status/read-descriptors");
    RelayDescriptorDatabaseImporter database
        = new RelayDescriptorDatabaseImporter(descriptorDirectories,
        historyFile, jdbcString);
    database.importRelayDescriptors();

    log.info("Aggregating database entries.");
    database.aggregate();

    log.info("Querying aggregated statistics from the database.");
    new Writer().write(new File(baseDir, "stats/bandwidth.csv").toPath(),
        database.queryBandwidth());

    log.info("Closing database connection.");
    database.closeConnection();

    log.info("Terminating bwhist module.");
  }
}

