import java.io.*;
import java.util.*;
import org.apache.commons.codec.digest.*;
import org.apache.commons.codec.binary.*;

/**
 * Parse the contents of a network status consensus and pass on the
 * relevant contents to the stats file handlers.
 */
public class RelayDescriptorParser {
  private DirreqStatsFileHandler dsfh;
  private ConsensusStatsFileHandler csfh;
  private BridgeStatsFileHandler bsfh;
  private SortedSet<String> countries;
  private SortedSet<String> directories;
  public RelayDescriptorParser(ConsensusStatsFileHandler csfh,
      BridgeStatsFileHandler bsfh, DirreqStatsFileHandler dsfh,
      SortedSet<String> countries, SortedSet<String> directories) {
    this.csfh = csfh;
    this.bsfh = bsfh;
    this.dsfh = dsfh;
    this.countries = countries;
    this.directories = directories;
  }
  public void initialize() throws IOException {
    this.csfh.initialize();
    this.bsfh.initialize();
    this.dsfh.initialize();
  }
  public void parse(BufferedReader br) throws IOException {
    String line = br.readLine();
    if (line.equals("network-status-version 3")) {
      int exit = 0, fast = 0, guard = 0, running = 0, stable = 0;
      String validAfter = null;
      while ((line = br.readLine()) != null) {
        if (line.startsWith("valid-after ")) {
          validAfter = line.substring("valid-after ".length());
        } else if (line.startsWith("r ")) {
          String hashedRelay = DigestUtils.shaHex(Base64.decodeBase64(
              line.split(" ")[2] + "=")).toUpperCase();
          bsfh.addHashedRelay(hashedRelay);
        } else if (line.startsWith("s ")) {
          if (line.contains(" Running")) {
            exit += line.contains(" Exit") ? 1 : 0;
            fast += line.contains(" Fast") ? 1 : 0;
            guard += line.contains(" Guard") ? 1 : 0;
            stable += line.contains(" Stable") ? 1 : 0;
            running++;
          }
        }
      }
      csfh.addConsensusResults(validAfter, exit, fast, guard, running,
          stable);
    } else if (line.startsWith("router ")) {
      // in case we want to parse server descriptors in the future
    } else if (line.startsWith("extra-info ")
        && directories.contains(line.split(" ")[2])) {
      String dir = line.split(" ")[2];
      String date = null, v3ips = null;
      boolean skip = false;
      while ((line = br.readLine()) != null) {
        if (line.startsWith("dirreq-stats-end ")) {
          date = line.split(" ")[1];
          // trusted had very strange dirreq-v3-shares here...
          skip = dir.equals("8522EB98C91496E80EC238E732594D1509158E77")
              && (date.equals("2009-09-10") || date.equals("2009-09-11"));
        } else if (line.startsWith("dirreq-v3-reqs ")
            && line.length() > "dirreq-v3-reqs ".length()) {
          v3ips = line.split(" ")[1];
        } else if (line.startsWith("dirreq-v3-share ")
            && v3ips != null && !skip) {
          Map<String, String> obs = new HashMap<String, String>();
          String[] parts = v3ips.split(",");
          for (String p : parts) {
            for (String c : this.countries) {
              if (p.startsWith(c)) {
                obs.put(c, p.substring(3));
              }
            }
          }
          String share = line.substring("dirreq-v3-share ".length(),
              line.length() - 1);
          dsfh.addObs(dir, date, obs, share);
        }
      }
    }
  }
}

