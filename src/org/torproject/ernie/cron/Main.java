/* Copyright 2011, 2012 The Tor Project
 * See LICENSE for licensing information */
package org.torproject.ernie.cron;

import java.io.File;
import java.util.logging.Logger;

/**
 * Coordinate downloading and parsing of descriptors and extraction of
 * statistically relevant data for later processing with R.
 */
public class Main {
  public static void main(String[] args) {

    /* Initialize logging configuration. */
    new LoggingConfiguration();

    Logger logger = Logger.getLogger(Main.class.getName());
    logger.info("Starting ERNIE.");

    // Initialize configuration
    Configuration config = new Configuration();

    // Use lock file to avoid overlapping runs
    LockFile lf = new LockFile();
    if (!lf.acquireLock()) {
      logger.severe("Warning: ERNIE is already running or has not exited "
          + "cleanly! Exiting!");
      System.exit(1);
    }

    // Define stats directory for temporary files
    File statsDirectory = new File("stats");

    // Prepare bridge stats file handler
    BridgeStatsFileHandler bsfh = config.getWriteBridgeStats() ?
        new BridgeStatsFileHandler(
        config.getRelayDescriptorDatabaseJDBC(),
        new File(config.getSanitizedBridgesDirectory()),
        statsDirectory, config.getKeepSanitizedBridgesImportHistory(),
        new File(config.getDirectoryArchivesDirectory()),
        config.getKeepDirectoryArchiveImportHistory()) :
        null;

    // Import relay descriptors
    if (config.getImportDirectoryArchives()) {
      RelayDescriptorDatabaseImporter rddi =
          config.getWriteRelayDescriptorDatabase() ||
          config.getWriteRelayDescriptorsRawFiles() ?
          new RelayDescriptorDatabaseImporter(
          config.getWriteRelayDescriptorDatabase() ?
          config.getRelayDescriptorDatabaseJDBC() : null,
          config.getWriteRelayDescriptorsRawFiles() ?
          config.getRelayDescriptorRawFilesDirectory() : null,
          new File(config.getDirectoryArchivesDirectory()),
          statsDirectory,
          config.getKeepDirectoryArchiveImportHistory()) : null;
      if (rddi != null) {
        rddi.importRelayDescriptors();
      }
      if (bsfh != null) {
        bsfh.importRelayDescriptors();
      }
      rddi.closeConnection();

      // Import conn-bi-direct statistics.
      PerformanceStatsImporter psi = new PerformanceStatsImporter(
          config.getWriteRelayDescriptorDatabase() ?
          config.getRelayDescriptorDatabaseJDBC() : null,
          config.getWriteRelayDescriptorsRawFiles() ?
          config.getRelayDescriptorRawFilesDirectory() : null,
          new File(config.getDirectoryArchivesDirectory()),
          statsDirectory,
          config.getKeepDirectoryArchiveImportHistory());
      psi.importRelayDescriptors();
      psi.closeConnection();
    }

    // Import sanitized bridges and write updated stats files to disk
    if (bsfh != null) {
      if (config.getImportSanitizedBridges()) {
        bsfh.importSanitizedBridges();
      }
      bsfh.writeFiles();
      bsfh = null;
    }

    // Prepare consensus stats file handler (used for stats on running
    // bridges only)
    ConsensusStatsFileHandler csfh = config.getWriteBridgeStats() ?
        new ConsensusStatsFileHandler(
        config.getRelayDescriptorDatabaseJDBC(),
        new File(config.getSanitizedBridgesDirectory()),
        statsDirectory, config.getKeepSanitizedBridgesImportHistory()) :
        null;

    // Import sanitized bridges and write updated stats files to disk
    if (csfh != null) {
      if (config.getImportSanitizedBridges()) {
        csfh.importSanitizedBridges();
      }
      csfh.writeFiles();
      csfh = null;
    }

    // Import and process torperf stats
    if (config.getImportWriteTorperfStats()) {
      new TorperfProcessor(new File(config.getTorperfDirectory()),
          statsDirectory, config.getRelayDescriptorDatabaseJDBC());
    }

    // Remove lock file
    lf.releaseLock();

    logger.info("Terminating ERNIE.");
  }
}

