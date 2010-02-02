import java.io.*;
import java.util.*;
import java.util.logging.*;

/**
 *
 */
public class BridgeStatsFileHandler {
  private String statsDir;
  private File bridgeStatsFile;
  private File bridgeStatsDateFile;
  private File hashedRelayIdentitiesFile;
  private SortedSet<String> countries;
  private SortedSet<String> hashedRelays = new TreeSet<String>();
  private SortedMap<String, String> observations;
  private boolean initialized;
  private boolean modified;
  private Logger logger;
  public BridgeStatsFileHandler(String statsDir,
      SortedSet<String> countries) {
    this.statsDir = statsDir;
    this.countries = countries;
    this.bridgeStatsFile = new File(statsDir + "/bridge-stats-raw");
    this.bridgeStatsDateFile = new File(statsDir + "/bridge-stats");
    this.observations = new TreeMap<String, String>();
    this.hashedRelayIdentitiesFile = new File(statsDir
        + "/hashed-relay-identities");
    this.logger =
        Logger.getLogger(BridgeStatsFileHandler.class.getName());
  }
  public void initialize() throws IOException {
    if (this.initialized) {
      return;
    }
    this.initialized = true;
    if (this.bridgeStatsFile.exists()) {
      this.logger.info("Reading file " + statsDir
          + "/bridge-stats-raw...");
      BufferedReader br = new BufferedReader(new FileReader(
          this.bridgeStatsFile));
      String line = br.readLine();
      if (line != null) {
        String[] headers = line.split(",");
        for (int i = 3; i < headers.length; i++) {
          this.countries.add(headers[i]);
        }
        while ((line = br.readLine()) != null) {
          String[] readData = line.split(",");
          String hashedBridgeIdentity = readData[0];
          String date = readData[1];
          String time = readData[2];
          SortedMap<String, String> obs = new TreeMap<String, String>();
          for (int i = 3; i < readData.length; i++) {
            obs.put(headers[i], readData[i]);
          }
          this.addObs(hashedBridgeIdentity, date, time, obs);
        }
      }
      this.logger.info("Finished reading file " + statsDir
          + "/bridge-stats-raw.");
      br.close();
    }
    if (this.hashedRelayIdentitiesFile.exists()) {
      this.logger.info("Reading file " + statsDir
          + "/hashed-relay-identities...");
      BufferedReader br = new BufferedReader(new FileReader(
          this.hashedRelayIdentitiesFile));
      String line = null;
      while ((line = br.readLine()) != null) {
        this.hashedRelays.add(line);
      }
      br.close();
      this.logger.info("Finished reading file " + statsDir
          + "/hashed-relay-identities.");
    }
  }
  public void addHashedRelay(String hashedRelayIdentity)
      throws IOException {
    if (!this.initialized) {
      throw new RuntimeException("Not initialized!");
    }
    this.hashedRelays.add(hashedRelayIdentity);
    this.modified = true;
  }
  public boolean isKnownRelay(String hashedBridgeIdentity)
      throws IOException {
    if (!this.initialized) {
      throw new RuntimeException("Not initialized!");
    }
    return this.hashedRelays.contains(hashedBridgeIdentity);
  }
  public void addObs(String hashedIdentity, String date,
      String time, Map<String, String> obs) throws IOException {
    if (!this.initialized) {
      throw new RuntimeException("Not initialized!");
    }
    String key = hashedIdentity + "," + date;
    StringBuilder sb = new StringBuilder(key + "," + time);
    for (String c : countries) {
      sb.append("," + (obs.containsKey(c) ? obs.get(c) : "0.0"));
    }
    String value = sb.toString();
    if (!this.observations.containsKey(key)
        || value.compareTo(this.observations.get(key)) > 0) {
      this.observations.put(key, value);
    }
    this.modified = true;
  }

  public void writeFile() {
    if (!this.modified) {
      return;
    }
    try {
      if (!this.hashedRelays.isEmpty()) {
        this.logger.info("Writing file " + this.statsDir
            + "/hashed-relay-identities...");
        new File(this.statsDir).mkdirs();
        BufferedWriter bwRelayIdentities = new BufferedWriter(
            new FileWriter(this.hashedRelayIdentitiesFile));
        for (String hashedRelay : this.hashedRelays) {
          bwRelayIdentities.append(hashedRelay + "\n");
        }
        bwRelayIdentities.close();
        this.logger.info("Finished writing file " + this.statsDir
            + "/hashed-relay-identities.");
      }
      if (!this.observations.isEmpty()) {
        this.logger.info("Writing file " + this.statsDir
            + "/bridge-stats-raw...");
        new File(this.statsDir).mkdirs();
        BufferedWriter bwBridgeStats = new BufferedWriter(
            new FileWriter(this.bridgeStatsFile));
        bwBridgeStats.append("bridge,date,time");
        for (String c : this.countries) {
          bwBridgeStats.append("," + c);
        }
        bwBridgeStats.append("\n");
        SortedMap<String, Set<double[]>> observationsPerDay =
            new TreeMap<String, Set<double[]>>();
        for (String observation : this.observations.values()) {
          String hashedBridgeIdentity = observation.split(",")[0];
          if (!this.hashedRelays.contains(hashedBridgeIdentity)) {
            bwBridgeStats.append(observation + "\n");
            String[] parts = observation.split(",");
            String date = parts[1];
            double[] users = new double[countries.size()];
            for (int i = 3; i < parts.length; i++) {
              users[i - 3] = Double.parseDouble(parts[i]);
            }
            Set<double[]> perDay = observationsPerDay.get(date);
            if (perDay == null) {
              perDay = new HashSet<double[]>();
              observationsPerDay.put(date, perDay);
            }
            perDay.add(users);
          }
        }
        bwBridgeStats.close();
        this.logger.info("Finished writing file " + this.statsDir
            + "/bridge-stats-raw.");
        this.logger.info("Writing file " + this.statsDir
            + "/bridge-stats...");
        BufferedWriter bwBridgeStatsDate = new BufferedWriter(
            new FileWriter(this.bridgeStatsDateFile));
        bwBridgeStatsDate.append("date");
        for (String c : this.countries) {
          bwBridgeStatsDate.append("," + c);
        }
        bwBridgeStatsDate.append("\n");
        for (Map.Entry<String, Set<double[]>> e :
            observationsPerDay.entrySet()) {
          String date = e.getKey();
          double[] sums = null;
          for (double[] users : e.getValue()) {
            if (sums == null) {
              sums = users;
            } else {
              for (int i = 0; i < sums.length; i++) {
                sums[i] += users[i];
              }
            }
          }
          bwBridgeStatsDate.append(date);
          for (int i = 0; i < sums.length; i++) {
            bwBridgeStatsDate.append(","
                + String.format("%.2f", sums[i]));
          }
          bwBridgeStatsDate.append("\n");
        }
        bwBridgeStatsDate.close();
        this.logger.info("Finished writing file " + this.statsDir
            + "/bridge-stats.");
      }
    } catch (IOException e) {
      this.logger.log(Level.WARNING, "Failed writing " + this.statsDir
          + "/{hashed-relay-identities|bridge-stats-raw|bridge-stats}!",
          e);
    }
  }
}

