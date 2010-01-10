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
      System.out.print("Reading existing file " + statsDir
          + "/dirreq-stats... ");
      BufferedReader br = new BufferedReader(new FileReader(
          this.dirreqStatsFile));
      String line = null;
      while ((br.readLine()) != null) {
        // TODO read dirreq-stats; also, take into account that headers might be different than the countries that we're interested in now!
      }
      System.out.println("done");
      br.close();
    }

  }
  public void addObs(String dirNickname, String date,
      Map<String, String> obs, String share) {
    String obsKey = dirNickname + "," + date;
    StringBuilder sb = new StringBuilder(obsKey);
    for (String c : countries) {
      sb.append("," + (obs.containsKey(c) ? obs.get(c) : "0"));
    }
    sb.append("," + share);
    observations.put(obsKey, sb.toString());
  }
  public void writeFile() throws IOException {
    System.out.print("Writing file " + this.statsDir
        + "/dirreq-stats... ");
    BufferedWriter bwDirreqStats = new BufferedWriter(
        new FileWriter(this.dirreqStatsFile));
// TODO write countries to header!
    for (String observation : observations.values()) {
      bwDirreqStats.append(observation + "\n");
    }
    bwDirreqStats.close();
    System.out.println("done");
  }
}

