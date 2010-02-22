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

    // Prepare stats file handlers which will be initialized by the
    // importing/downloading classes
    String statsDirectory = "stats";
    ConsensusStatsFileHandler csfh = config.getWriteConsensusStats() ?
        new ConsensusStatsFileHandler(statsDirectory) : null;
    BridgeStatsFileHandler bsfh = config.getWriteBridgeStats() ?
        new BridgeStatsFileHandler(statsDirectory, countries) : null;
    DirreqStatsFileHandler dsfh = config.getWriteDirreqStats() ?
        new DirreqStatsFileHandler(statsDirectory, countries) : null;

    // Prepare parsers
    // TODO handle cases bsfh==NULL, csfh==NULL, dsfh==NULL
    RelayDescriptorParser rdp = new RelayDescriptorParser(statsDirectory,
        csfh, bsfh, dsfh, countries, directories);
    BridgeDescriptorParser bdp = new BridgeDescriptorParser(csfh, bsfh,
        countries);

    // Prepare writing relay descriptor archive to disk
    ArchiveWriter aw = config.getWriteDirectoryArchives() ?
        new ArchiveWriter() : null;
    // TODO handle case aw==NULL below

    // import and/or download relay and bridge descriptors
    if (config.getImportCachedRelayDescriptors()) {
      new CachedRelayDescriptorReader(rdp, aw);
    }
    if (config.getImportDirectoryArchives()) {
      new ArchiveReader(rdp, "archives");
    }
    if (config.getDownloadRelayDescriptors()) {
      // TODO make this smarter by letting rdd ask rdp which descriptors
      // are still missing and only download those
      // TODO move iteration over dirauths from main() to class code
      for (String directoryAuthority : 
          config.getDownloadFromDirectoryAuthorities()) {
        new RelayDescriptorDownloader(rdp, aw, directoryAuthority,
            directories);
      }
    }
    if (config.getImportSanitizedBridges()) {
      new SanitizedBridgesReader(bdp, "bridges", countries);
    }
    if (config.getImportBridgeSnapshots()) {
      new BridgeSnapshotReader(bdp, "bridge-directories",
          statsDirectory, countries);
    }

    // Write updated stats files to disk
    if (bsfh != null) {
      bsfh.writeFile();
    }
    if (csfh != null) {
      csfh.writeFile();
    }
    if (dsfh != null) {
      dsfh.writeFile();
    }

    // Import and process torperf stats
    if (config.getImportWriteTorperfStats()) {
      new TorperfProcessor(statsDirectory, "torperf");
    }

    // Download and process GetTor stats
    if (config.getDownloadProcessGetTorStats()) {
      new GetTorProcessor(statsDirectory, config.getGetTorStatsUrl());
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

