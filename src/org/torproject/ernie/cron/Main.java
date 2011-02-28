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

    // Prepare consensus health checker
    ConsensusHealthChecker chc = config.getWriteConsensusHealth() ?
        new ConsensusHealthChecker() : null;

    // Prepare relay descriptor parser (only if we are writing the
    // consensus-health page to disk)
    RelayDescriptorParser rdp = config.getWriteConsensusHealth() ?
        new RelayDescriptorParser(chc) : null;

    // Import relay descriptors
    if (rdp != null) {
      if (config.getImportDirectoryArchives()) {
        new ArchiveReader(rdp,
            new File(config.getDirectoryArchivesDirectory()),
            statsDirectory,
            config.getKeepDirectoryArchiveImportHistory());
      }
    }

    // Write consensus health website
    if (chc != null) {
      chc.writeStatusWebsite();
      chc = null;
    }

    // Remove lock file
    lf.releaseLock();

    logger.info("Terminating ERNIE.");
  }
}

