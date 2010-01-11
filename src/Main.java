import java.io.*;
import java.text.*;
import java.util.*;

/**
 * Coordinate downloading and parsing of descriptors and extraction of
 * statistically relevant data for later processing with R.
 */
public class Main {
// TODO handle exceptions better!
  public static void main(String[] args) throws IOException,
      ParseException {

    // Should we only import from disk or only download descriptors?
    boolean importOnly = args.length > 0
        && args[0].equals("import");
    boolean downloadOnly = args.length > 0
        && args[0].equals("download");

    // Define which stats we are interested in
    String authority = "80.190.246.100";
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
    directories.put("FFCB46DB1339DA84674C70D7CB586434C4370441",
        "moria1");

    // Initialize stats file handlers
    String statsDirectory = "stats";
    ConsensusStatsFileHandler csfh = new ConsensusStatsFileHandler(
        statsDirectory);
    BridgeStatsFileHandler bsfh = new BridgeStatsFileHandler(
        statsDirectory);
    DirreqStatsFileHandler dsfh = new DirreqStatsFileHandler(
        statsDirectory, countries);

    // Initialize parsers
    ConsensusParser cp = new ConsensusParser(csfh, bsfh);
    ServerDescriptorParser sdp = new ServerDescriptorParser();
    ExtraInfoParser eip = new ExtraInfoParser(dsfh, countries,
        directories);

    // Read files in archives/ and bridges/ directory
    if (!downloadOnly) {
// TODO prevent overlapping runs by cron and manually!!
      ArchiveReader ar = new ArchiveReader(cp, sdp, eip, "archives",
          directories.keySet());
      BridgeReader br = new BridgeReader(csfh, bsfh, "bridges",
          countries);
    }

    // Download current descriptors
    if (!importOnly) {
      ConsensusDownloader cd = new ConsensusDownloader(cp, authority);
/* TODO no downloading until parsing files on disk works!
      ServerDescriptorDownloader sdd = new ServerDescriptorDownloader(sdp,
          authority);
*/
      ExtraInfoDownloader eid = new ExtraInfoDownloader(eip, authority,
          directories);
    }

    // Parse torperf files
    // TODO maybe convert them in a format that is easier to process for R than the current one?
    // TorperfParser tp = new TorperfParser(tsfh);

    // Write updated stats files to disk
    bsfh.writeFile();
    csfh.writeFile();
    dsfh.writeFile();
  }
}

