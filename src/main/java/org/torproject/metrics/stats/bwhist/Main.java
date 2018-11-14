/* Copyright 2011--2018 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.stats.bwhist;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Coordinate downloading and parsing of descriptors and extraction of
 * statistically relevant data for later processing with R.
 */
public class Main {

  private static Logger log = LoggerFactory.getLogger(Main.class);

  private static String[][] paths =  {
      {"recent", "relay-descriptors", "consensuses"},
      {"recent", "relay-descriptors", "extra-infos"},
      {"archive", "relay-descriptors", "consensuses"},
      {"archive", "relay-descriptors", "extra-infos"}};

  /** Executes this data-processing module. */
  public static void main(String[] args) throws Exception {

    log.info("Starting bwhist module.");

    log.info("Reading descriptors and inserting relevant parts into the "
        + "database.");
    File[] descriptorDirectories = Arrays.stream(paths).map((String[] path)
        -> Paths.get(Configuration.descriptors, path).toFile())
        .toArray(File[]::new);
    File historyFile = new File(Configuration.history);
    RelayDescriptorDatabaseImporter database
        = new RelayDescriptorDatabaseImporter(descriptorDirectories,
        historyFile, Configuration.database);
    database.importRelayDescriptors();

    log.info("Aggregating database entries.");
    database.aggregate();

    log.info("Querying aggregated statistics from the database.");
    new Writer().write(Paths.get(Configuration.output, "bandwidth.csv"),
        database.queryBandwidth());

    log.info("Closing database connection.");
    database.closeConnection();

    log.info("Terminating bwhist module.");
  }
}

