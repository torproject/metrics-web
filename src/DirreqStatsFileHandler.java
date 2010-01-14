import java.io.*;
import java.util.*;

/**
 *
 */
public class DirreqStatsFileHandler {
  private String statsDir;
  private SortedSet<String> countries;
  private File dirreqStatsFile;
  private SortedMap<String, String> observations;
  public DirreqStatsFileHandler(String statsDir,
      SortedSet<String> countries) throws IOException {
    this.statsDir = statsDir;
    this.countries = countries;
    this.dirreqStatsFile = new File(statsDir + "/dirreq-stats");
    this.observations = new TreeMap<String, String>();
    if (this.dirreqStatsFile.exists()) {
      System.out.print("Reading file " + statsDir + "/dirreq-stats... ");
      BufferedReader br = new BufferedReader(new FileReader(
          this.dirreqStatsFile));
      String line = br.readLine();
      if (line != null) {
        String[] headers = line.split(",");
        for (int i = 2; i < headers.length - 1; i++) {
          this.countries.add(headers[i]);
        }
        while ((line = br.readLine()) != null) {
          String[] readData = line.split(",");
          String dirNickname = readData[0];
          String date = readData[1];
          Map<String, String> obs = new HashMap<String, String>();
          for (int i = 2; i < readData.length - 1; i++) {
            obs.put(headers[i], readData[i]);
          }
          String share = readData[readData.length - 1];
          this.addObs(dirNickname, date, obs, share);
        }
      }
      System.out.println("done");
      br.close();
    }

  }
  public void addObs(String dirNickname, String date,
      Map<String, String> obs, String share) {
    String obsKey = dirNickname + "," + date;
    StringBuilder sb = new StringBuilder(obsKey);
    for (String c : this.countries) {
      sb.append("," + (obs.containsKey(c) ? obs.get(c) : "0"));
    }
    sb.append("," + share);
    this.observations.put(obsKey, sb.toString());
  }
  public void writeFile() throws IOException {
    System.out.print("Writing file " + this.statsDir
        + "/dirreq-stats... ");
    BufferedWriter bwDirreqStats = new BufferedWriter(
        new FileWriter(this.dirreqStatsFile));
    bwDirreqStats.append("directory,date");
    for (String country : this.countries) {
      bwDirreqStats.append("," + country);
    }
    bwDirreqStats.append(",share\n");
    for (String observation : this.observations.values()) {
      bwDirreqStats.append(observation + "\n");
    }
    bwDirreqStats.close();
    System.out.println("done");
  }
}

