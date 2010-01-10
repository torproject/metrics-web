import java.io.*;
import java.util.*;

/**
 *
 */
public class ConsensusStatsFileHandler {
  private String statsDir;
  private File consensusStatsRawFile;
  private File consensusStatsFile;
  private SortedMap<String, String> consensusResults;
  public ConsensusStatsFileHandler(String statsDir) throws IOException {
    this.statsDir = statsDir;
    this.consensusResults = new TreeMap<String, String>();
    this.consensusStatsRawFile = new File(statsDir + "/consensus-stats-raw");
    this.consensusStatsFile = new File(statsDir + "/consensus-stats");
    if (this.consensusStatsRawFile.exists()) {
      System.out.print("Reading existing file " + statsDir
          + "/consensus-stats-raw... ");
      BufferedReader br = new BufferedReader(new FileReader(
          this.consensusStatsRawFile));
      String line = null;
      while ((line = br.readLine()) != null) {
        consensusResults.put(line.split(",")[0], line);
      }
      System.out.println("done");
      br.close();
    }
  }
  public void addConsensusResults(String validAfter, int exit, int fast,
      int guard, int running, int stable) {
    consensusResults.put(validAfter, validAfter + "," + exit + "," + fast
        + "," + guard + "," + running + "," + stable);
  }
  public void writeFile() throws IOException {
    System.out.print("Writing files " + this.statsDir
        + "/consensus-stats-raw and " + this.statsDir
        + "/consensus-stats... ");
    BufferedWriter bwConsensusStatsRaw = new BufferedWriter(
        new FileWriter(this.consensusStatsRawFile));
    BufferedWriter bwConsensusStats = new BufferedWriter(
        new FileWriter(this.consensusStatsFile));
    bwConsensusStats.append("date,exit,fast,guard,running,stable\n");
    String tempDate = null;
    int exitDay = 0, fastDay = 0, guardDay = 0, runningDay = 0,
        stableDay = 0, consensusesDay = 0;
    Iterator<String> it = consensusResults.values().iterator();
    boolean haveWrittenFinalLine = false;
    while (it.hasNext() || !haveWrittenFinalLine) {
      String next = it.hasNext() ? it.next()
          : null;
      if (tempDate != null && (next == null ||
          !next.substring(0, 10).equals(tempDate))) {
        bwConsensusStats.append(tempDate + ","
            + (exitDay / consensusesDay) + ","
            + (fastDay / consensusesDay) + ","
            + (guardDay / consensusesDay) + ","
            + (runningDay / consensusesDay) + ","
            + (stableDay / consensusesDay) + "\n");
        exitDay = 0;
        fastDay = 0;
        guardDay = 0;
        runningDay = 0;
        stableDay = 0;
        consensusesDay = 0;
        if (next == null) {
          haveWrittenFinalLine = true;
        }
      }
      if (next != null) {
        bwConsensusStatsRaw.append(next + "\n");
        String[] parts = next.split(",");
        tempDate = next.substring(0, 10);
        consensusesDay++;
        exitDay += Integer.parseInt(parts[1]);
        fastDay += Integer.parseInt(parts[2]);
        guardDay += Integer.parseInt(parts[3]);
        runningDay += Integer.parseInt(parts[4]);
        stableDay += Integer.parseInt(parts[5]);
      }
    }
    bwConsensusStatsRaw.close();
    bwConsensusStats.close();
    System.out.println("done");
  }
}

