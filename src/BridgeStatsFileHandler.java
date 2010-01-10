import java.io.*;
import java.util.*;

/**
 *
 */
public class BridgeStatsFileHandler {
  private String statsDir;
  private File bridgeStatsFile;
  private File hashedRelayIdentitiesFile;
  private SortedSet<String> hashedRelays = new TreeSet<String>();
  public BridgeStatsFileHandler(String statsDir) throws IOException {
    this.statsDir = statsDir;
    this.bridgeStatsFile = new File(statsDir + "/bridge-stats");
    if (this.bridgeStatsFile.exists()) {
      System.out.print("Reading existing file " + statsDir
          + "/bridge-stats... ");
      BufferedReader br = new BufferedReader(new FileReader(
          this.bridgeStatsFile));
      String line = null;
      while ((br.readLine()) != null) {
        // TODO read bridge-stats
      }
      System.out.println("done");
      br.close();
    }
    this.hashedRelayIdentitiesFile = new File(statsDir
        + "/hashed-relay-identities");
    if (this.hashedRelayIdentitiesFile.exists()) {
      System.out.print("Reading existing file " + statsDir
          + "/hashed-relay-identities... ");
      BufferedReader br = new BufferedReader(new FileReader(
          this.hashedRelayIdentitiesFile));
      String line = null;
      while ((line = br.readLine()) != null) {
        hashedRelays.add(line);
      }
      br.close();
      System.out.println("done");
    }
  }
  public void addHashedRelay(String hashedRelayIdentity) {
    hashedRelays.add(hashedRelayIdentity);
  }
  public boolean isKnownRelay(String hashedBridgeIdentity) {
    return hashedRelays.contains(hashedBridgeIdentity);
  }
  public void addStats(String date, String time, String hashedIdentity,
      Map<String, Double> obs) {
/*
          bwBridgeStats.write(publishedLine.split(" ")[2] + ","
              + publishedLine.split(" ")[1] + "," + hashedRelay);
          for (String c : countries) {
            bwBridgeStats.append(String.format(",%.2f",
                obs.containsKey(c) ? obs.get(c) : 0.0D));
          }
          bwBridgeStats.append("\n");
*/
  }

  public void writeFile() throws IOException {
    System.out.print("Writing file " + this.statsDir
        + "/hashed-relay-identities... ");
    BufferedWriter bwRelayIdentities = new BufferedWriter(
        new FileWriter(this.hashedRelayIdentitiesFile));
    for (String hashedRelay : hashedRelays) {
      bwRelayIdentities.append(hashedRelay + "\n");
    }
    bwRelayIdentities.close();
    System.out.println("done");
    // TODO write bridge-stats, too
    // TODO filter out those that are known relays:
    /*
if (!hashedRelays.contains(line.split(",")[2])) {
    */
  }
}

