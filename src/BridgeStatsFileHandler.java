import java.io.*;
import java.util.*;
import java.util.logging.*;

/**
 *
 */
public class BridgeStatsFileHandler {
  private File bridgeStatsRawFile;
  private File bridgeStatsFile;
  private File hashedRelayIdentitiesFile;
  private SortedSet<String> countries;
  private SortedSet<String> hashedRelays = new TreeSet<String>();
  private SortedMap<String, String> observations;
  private boolean hashedRelaysModified;
  private boolean observationsModified;
  private Logger logger;
  public BridgeStatsFileHandler(SortedSet<String> countries) {
    this.bridgeStatsRawFile = new File("stats/bridge-stats-raw");
    this.bridgeStatsFile = new File("stats/bridge-stats");
    this.hashedRelayIdentitiesFile = new File(
        "stats/hashed-relay-identities");
    this.countries = countries;
    this.observations = new TreeMap<String, String>();
    this.logger =
        Logger.getLogger(BridgeStatsFileHandler.class.getName());
    if (this.bridgeStatsRawFile.exists()) {
      this.logger.info("Reading file "
          + this.bridgeStatsRawFile.getAbsolutePath() + "...");
      try {
        BufferedReader br = new BufferedReader(new FileReader(
            this.bridgeStatsRawFile));
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
        br.close();
        this.observationsModified = false;
        this.logger.info("Finished reading file "
            + this.bridgeStatsRawFile.getAbsolutePath() + ".");
      } catch (IOException e) {
        this.logger.log(Level.WARNING, "Failed reading file "
            + this.bridgeStatsRawFile.getAbsolutePath() + "!", e);
      }
    }
    if (this.hashedRelayIdentitiesFile.exists()) {
      this.logger.info("Reading file "
          + this.hashedRelayIdentitiesFile.getAbsolutePath() + "...");
      try {
        BufferedReader br = new BufferedReader(new FileReader(
            this.hashedRelayIdentitiesFile));
        String line = null;
        while ((line = br.readLine()) != null) {
          this.hashedRelays.add(line);
        }
        br.close();
        this.hashedRelaysModified = false;
        this.logger.info("Finished reading file "
            + this.hashedRelayIdentitiesFile.getAbsolutePath() + ".");
      } catch (IOException e) {
        this.logger.log(Level.WARNING, "Failed reading file "
            + this.hashedRelayIdentitiesFile.getAbsolutePath() + "!", e);
      }
    }
  }
  public void addHashedRelay(String hashedRelayIdentity)
      throws IOException {
    this.hashedRelays.add(hashedRelayIdentity);
    this.hashedRelaysModified = true;
  }
  public boolean isKnownRelay(String hashedBridgeIdentity)
      throws IOException {
    return this.hashedRelays.contains(hashedBridgeIdentity);
  }
  public void addObs(String hashedIdentity, String date,
      String time, Map<String, String> obs) throws IOException {
    String key = hashedIdentity + "," + date;
    StringBuilder sb = new StringBuilder(key + "," + time);
    for (String c : countries) {
      sb.append("," + (obs.containsKey(c) ? obs.get(c) : "0.0"));
    }
    String value = sb.toString();
    if (!this.observations.containsKey(key)
        || value.compareTo(this.observations.get(key)) > 0) {
      this.observations.put(key, value);
      this.observationsModified = true;
    }
  }

  public void writeFile() {
    if (!this.hashedRelays.isEmpty() && this.hashedRelaysModified) {
      try {
        this.logger.info("Writing file "
            + this.hashedRelayIdentitiesFile.getAbsolutePath() + "...");
        this.hashedRelayIdentitiesFile.getParentFile().mkdirs();
        BufferedWriter bwRelayIdentities = new BufferedWriter(
            new FileWriter(this.hashedRelayIdentitiesFile));
        for (String hashedRelay : this.hashedRelays) {
          bwRelayIdentities.append(hashedRelay + "\n");
        }
        bwRelayIdentities.close();
        this.logger.info("Finished writing file "
            + this.hashedRelayIdentitiesFile.getAbsolutePath() + ".");
      } catch (IOException e) {
        this.logger.log(Level.WARNING, "Failed writing "
            + this.hashedRelayIdentitiesFile.getAbsolutePath() + "!", e);
      }
    }
    if (!this.observations.isEmpty() && this.observationsModified) {
      try {
        this.logger.info("Writing file "
            + this.bridgeStatsRawFile.getAbsolutePath() + "...");
        this.bridgeStatsRawFile.getParentFile().mkdirs();
        BufferedWriter bwBridgeStats = new BufferedWriter(
            new FileWriter(this.bridgeStatsRawFile));
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
        this.logger.info("Finished writing file "
            + this.bridgeStatsRawFile.getAbsolutePath() + ".");
        this.logger.info("Writing file "
            + this.bridgeStatsRawFile.getAbsolutePath() + "...");
        this.bridgeStatsFile.getParentFile().mkdirs();
        BufferedWriter bwBridgeStatsDate = new BufferedWriter(
            new FileWriter(this.bridgeStatsFile));
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
        this.logger.info("Finished writing file "
            + this.bridgeStatsFile.getAbsolutePath() + ".");
      } catch (IOException e) {
        this.logger.log(Level.WARNING, "Failed writing "
            + this.bridgeStatsRawFile.getAbsolutePath() + " or "
            + this.bridgeStatsFile.getAbsolutePath() + "!", e);
      }
    }
  }
}

