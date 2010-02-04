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

    // Use lock file to avoid overlapping runs
    LockFile lf = new LockFile();
    if (!lf.acquireLock()) {
      logger.severe("Warning: ERNIE is already running or has not exited "
          + "cleanly! Exiting!");
      System.exit(1);
    }

    // Should we only import from disk or only download descriptors?
    boolean importOnly = args.length > 0
        && args[0].equals("import");
    boolean downloadOnly = args.length > 0
        && args[0].equals("download");

    // Define which stats we are interested in
    SortedSet<String> countries = new TreeSet<String>();
    countries.add("bh");
    countries.add("cn");
    countries.add("cu");
    countries.add("et");
    countries.add("ir");
    countries.add("mm");
    countries.add("sa");
    countries.add("sy");
    countries.add("tn");
    countries.add("tm");
    countries.add("uz");
    countries.add("vn");
    countries.add("ye");
    SortedMap<String, String> directories = new TreeMap<String, String>();
    directories.put("8522EB98C91496E80EC238E732594D1509158E77",
        "trusted");
    directories.put("9695DFC35FFEB861329B9F1AB04C46397020CE31",
        "moria1");

    // Prepare stats file handlers which will be initialized by the
    // importing/downloading classes
    String statsDirectory = "stats";
    ConsensusStatsFileHandler csfh = new ConsensusStatsFileHandler(
        statsDirectory);
    BridgeStatsFileHandler bsfh = new BridgeStatsFileHandler(
        statsDirectory, countries);
    DirreqStatsFileHandler dsfh = new DirreqStatsFileHandler(
        statsDirectory, countries);

    // Prepare parsers
    RelayDescriptorParser rdp = new RelayDescriptorParser(csfh, bsfh,
        dsfh, countries, directories);
    BridgeDescriptorParser bdp = new BridgeDescriptorParser(csfh, bsfh,
        countries);

    // Read files in archives/ and bridges/ directory
    if (!downloadOnly) {
      logger.info("Importing data...");
      ArchiveReader ar = new ArchiveReader(rdp, "archives");
      SanitizedBridgesReader sbr = new SanitizedBridgesReader(bdp,
          "bridges", countries);
      BridgeSnapshotReader bsr = new BridgeSnapshotReader(bdp,
          "bridge-directories", statsDirectory, countries);
      TorperfProcessor tp = new TorperfProcessor(statsDirectory,
          "torperf");
      logger.info("Finished importing data.");
    }

    // Download current descriptors
    if (!importOnly) {
      logger.info("Downloading descriptors...");
      new RelayDescriptorDownloader(rdp, "86.59.21.38", directories);
      new RelayDescriptorDownloader(rdp, "194.109.206.212", directories);
      new RelayDescriptorDownloader(rdp, "80.190.246.100:8180",
          directories);
      logger.info("Finished downloading descriptors.");
    }

    // Write updated stats files to disk
    logger.info("Writing updated stats files to disk...");
    bsfh.writeFile();
    csfh.writeFile();
    dsfh.writeFile();
    logger.info("Finished writing updated stats files to disk.");

    // Remove lock file
    lf.releaseLock();

    logger.info("Terminating ERNIE.");
  }
}

