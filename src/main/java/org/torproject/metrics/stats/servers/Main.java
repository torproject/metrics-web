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

    // Use lock file to avoid overlapping runs
    LockFile lf = new LockFile();
    if (!lf.acquireLock()) {
      log.error("Warning: ERNIE is already running or has not exited "
          + "cleanly! Exiting!");
      System.exit(1);
    }

    // Define stats directory for temporary files
    File statsDirectory = new File("stats");

    // Import relay descriptors
    if (config.getImportDirectoryArchives()) {
      RelayDescriptorDatabaseImporter rddi =
          config.getWriteRelayDescriptorDatabase()
          || config.getWriteRelayDescriptorsRawFiles()
          ? new RelayDescriptorDatabaseImporter(
          config.getWriteRelayDescriptorDatabase()
          ? config.getRelayDescriptorDatabaseJdbc() : null,
          config.getWriteRelayDescriptorsRawFiles()
          ? config.getRelayDescriptorRawFilesDirectory() : null,
          config.getDirectoryArchivesDirectories(),
          statsDirectory,
          config.getKeepDirectoryArchiveImportHistory()) : null;
      if (null != rddi) {
        rddi.importRelayDescriptors();
        rddi.closeConnection();
      }
    }

    // Remove lock file
    lf.releaseLock();

    log.info("Terminating ERNIE.");
  }
}

