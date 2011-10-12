/* Copyright 2011 The Tor Project
 * See LICENSE for licensing information */
package org.torproject.ernie.cron;

import java.io.*;
import java.util.*;
import java.util.logging.*;

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
        config.getRelayDescriptorDatabaseJDBC()) : null;

    // Prepare writing relay descriptors to database
    RelayDescriptorDatabaseImporter rddi =
        config.getWriteRelayDescriptorDatabase() ||
        config.getWriteRelayDescriptorsRawFiles() ?
        new RelayDescriptorDatabaseImporter(
        config.getWriteRelayDescriptorDatabase() ?
        config.getRelayDescriptorDatabaseJDBC() : null,
        config.getWriteRelayDescriptorsRawFiles() ?
        config.getRelayDescriptorRawFilesDirectory() : null) : null;

    // Prepare relay descriptor parser (only if we are writing the
    // consensus-health page to disk)
    RelayDescriptorParser rdp = rddi != null ?
        new RelayDescriptorParser(rddi, bsfh) : null;

    // Import relay descriptors
    if (rdp != null) {
      if (config.getImportDirectoryArchives()) {
        new ArchiveReader(rdp,
            new File(config.getDirectoryArchivesDirectory()),
            statsDirectory,
            config.getKeepDirectoryArchiveImportHistory());
      }
    }

    // Close database connection (if active)
    if (rddi != null)   {
      rddi.closeConnection();
    }

    // Prepare consensus stats file handler (used for stats on running
    // bridges only)
    ConsensusStatsFileHandler csfh = config.getWriteBridgeStats() ?
        new ConsensusStatsFileHandler(
        config.getRelayDescriptorDatabaseJDBC()) : null;

    // Prepare bridge descriptor parser
    BridgeDescriptorParser bdp = config.getWriteBridgeStats() ?
        new BridgeDescriptorParser(csfh, bsfh) : null;

    // Import bridge descriptors
    if (bdp != null && config.getImportSanitizedBridges()) {
      new SanitizedBridgesReader(bdp,
          new File(config.getSanitizedBridgesDirectory()),
          statsDirectory, config.getKeepSanitizedBridgesImportHistory());
    }

    // Write updated stats files to disk
    if (bsfh != null) {
      bsfh.writeFiles();
      bsfh = null;
    }
    if (csfh != null) {
      csfh.writeFiles();
      csfh = null;
    }

    // Import and process torperf stats
    if (config.getImportWriteTorperfStats()) {
      new TorperfProcessor(new File(config.getTorperfDirectory()),
          statsDirectory, config.getRelayDescriptorDatabaseJDBC());
    }

    // Download and process GetTor stats
    if (config.getProcessGetTorStats()) {
      new GetTorProcessor(
          new File(config.getGetTorDirectory()),
          config.getRelayDescriptorDatabaseJDBC());
    }

    // Remove lock file
    lf.releaseLock();

    logger.info("Terminating ERNIE.");
  }
}

