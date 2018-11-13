/* Copyright 2011--2018 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.stats.servers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Coordinate downloading and parsing of descriptors and extraction of
 * statistically relevant data for later processing with R.
 */
public class Main {

  private static Logger log = LoggerFactory.getLogger(Main.class);

  /** Executes this data-processing module. */
  public static void main(String[] args) {

    log.info("Starting ERNIE.");

    // Initialize configuration
    Configuration config = new Configuration();

    // Define stats directory for temporary files
    File statsDirectory = new File("stats");

    // Import relay descriptors
    RelayDescriptorDatabaseImporter rddi = new RelayDescriptorDatabaseImporter(
        config.getRelayDescriptorDatabaseJdbc(),
        config.getDirectoryArchivesDirectories(), statsDirectory);
    rddi.importRelayDescriptors();
    rddi.closeConnection();

    log.info("Terminating ERNIE.");
  }
}

