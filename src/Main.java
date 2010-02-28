import java.io.*;
import java.text.*;
import java.util.*;
import java.util.logging.*;

/**
 * Coordinate downloading and parsing of descriptors and extraction of
 * statistically relevant data for later processing with R.
 */
public class Main {
  public static void main(String[] args) {

    Logger logger = Logger.getLogger(Main.class.getName());
    logger.info("Starting ERNIE...");

    // Initialize configuration
    Configuration config = new Configuration();

    // Use lock file to avoid overlapping runs
    LockFile lf = new LockFile();
    if (!lf.acquireLock()) {
      logger.severe("Warning: ERNIE is already running or has not exited "
          + "cleanly! Exiting!");
      System.exit(1);
    }

    // Define which stats we are interested in
    SortedSet<String> countries = config.getDirreqBridgeCountries();
    SortedSet<String> directories = config.getDirreqDirectories();

    // Prepare stats file handlers (only if we are writing stats)
    ConsensusStatsFileHandler csfh = config.getWriteConsensusStats() ?
        new ConsensusStatsFileHandler() : null;
    BridgeStatsFileHandler bsfh = config.getWriteBridgeStats() ?
        new BridgeStatsFileHandler(countries) : null;
    DirreqStatsFileHandler dsfh = config.getWriteDirreqStats() ?
        new DirreqStatsFileHandler(countries) : null;

    // Prepare relay descriptor parser (only if we are writing the
    // stats)
    RelayDescriptorParser rdp = config.getWriteConsensusStats() &&
        config.getWriteBridgeStats() && config.getWriteDirreqStats() ?
        new RelayDescriptorParser(csfh, bsfh, dsfh, countries,
        directories) : null;

    // Prepare writing relay descriptor archive to disk
    ArchiveWriter aw = config.getWriteDirectoryArchives() ?
        new ArchiveWriter(config.getV3DirectoryAuthorities()) : null;

    // Import/download relay descriptors from the various sources
    if (config.getImportCachedRelayDescriptors()) {
      new CachedRelayDescriptorReader(rdp, aw);
    }
    if (config.getImportDirectoryArchives()) {
      new ArchiveReader(rdp, "archives");
    }
    if (config.getDownloadRelayDescriptors()) {
      new RelayDescriptorDownloader(rdp, aw,
          config.getDownloadFromDirectoryAuthorities(),
          directories);
    }

    // Write output to disk that only depends on relay descriptors
    if (aw != null) {
      aw.writeFile();
      aw = null;
    }
    if (rdp != null) {
      rdp.writeFile();
      rdp = null;
    }
    if (dsfh != null) {
      dsfh.writeFile();
      dsfh = null;
    }

    // Prepare bridge descriptor parser
    BridgeDescriptorParser bdp = config.getWriteConsensusStats() &&
        config.getWriteBridgeStats() ? new BridgeDescriptorParser(
        csfh, bsfh, countries) : null;

    // Import bridge descriptors
    if (config.getImportSanitizedBridges()) {
      new SanitizedBridgesReader(bdp, "bridges", countries);
    }
    if (config.getImportBridgeSnapshots()) {
      new BridgeSnapshotReader(bdp, "bridge-directories", countries);
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
      new TorperfProcessor("torperf");
    }

    // Download and process GetTor stats
    if (config.getDownloadProcessGetTorStats()) {
      new GetTorProcessor(config.getGetTorStatsUrl());
    }

    // Download exit list and store it to disk
    if (config.getDownloadExitList()) {
      new ExitListDownloader();
    }

    // Remove lock file
    lf.releaseLock();

    logger.info("Terminating ERNIE.");
  }
}

