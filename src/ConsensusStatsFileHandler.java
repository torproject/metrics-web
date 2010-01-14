import java.io.*;
import java.util.*;

/**
 *
 */
public class ConsensusStatsFileHandler {
  private String statsDir;
  private File consensusStatsRawFile;
  private File bridgeConsensusStatsRawFile;
  private File consensusStatsFile;
  private SortedMap<String, String> consensusResults;
  private SortedMap<String, String> bridgeConsensusResults;
  public ConsensusStatsFileHandler(String statsDir) throws IOException {
    this.statsDir = statsDir;
    this.consensusResults = new TreeMap<String, String>();
    this.consensusStatsRawFile = new File(statsDir + "/consensus-stats-raw");
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
    this.bridgeConsensusResults = new TreeMap<String, String>();
    this.bridgeConsensusStatsRawFile = new File(statsDir
        + "/bridge-consensus-stats-raw");
    if (this.bridgeConsensusStatsRawFile.exists()) {
      System.out.print("Reading existing file " + statsDir
          + "/bridge-consensus-stats-raw... ");
      BufferedReader br = new BufferedReader(new FileReader(
          this.bridgeConsensusStatsRawFile));
      String line = null;
      while ((line = br.readLine()) != null) {
        bridgeConsensusResults.put(line.split(",")[0], line);
      }
      System.out.println("done");
      br.close();
    }
    this.consensusStatsFile = new File(statsDir + "/consensus-stats");
  }
  public void addConsensusResults(String validAfter, int exit, int fast,
      int guard, int running, int stable) {
    consensusResults.put(validAfter, validAfter + "," + exit + "," + fast
        + "," + guard + "," + running + "," + stable);
  }
  public void addBridgeConsensusResults(String published, int running) {
    bridgeConsensusResults.put(published, published + "," + running);
  }
  public void writeFile() throws IOException {
    System.out.print("Writing file " + this.statsDir
        + "/consensus-stats-raw... ");
    BufferedWriter bwConsensusStatsRaw = new BufferedWriter(
        new FileWriter(this.consensusStatsRawFile));
    String tempDate = null;
    int exitDay = 0, fastDay = 0, guardDay = 0, runningDay = 0,
        stableDay = 0, consensusesDay = 0;
    Iterator<String> it = consensusResults.values().iterator();
    boolean haveWrittenFinalLine = false;
    SortedMap<String, String> csAggr = new TreeMap<String, String>();
    while (it.hasNext() || !haveWrittenFinalLine) {
      String next = it.hasNext() ? it.next() : null;
      if (tempDate != null
          && (next == null || !next.substring(0, 10).equals(tempDate))) {
        if (consensusesDay > 11) {
          csAggr.put(tempDate, tempDate + ","
              + (exitDay / consensusesDay) + ","
              + (fastDay / consensusesDay) + ","
              + (guardDay / consensusesDay) + ","
              + (runningDay / consensusesDay) + ","
              + (stableDay / consensusesDay));
        }
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
    System.out.print("done\nWriting file " + this.statsDir
        + "/bridge-consensus-stats-raw... ");
    BufferedWriter bwBridgeConsensusStatsRaw = new BufferedWriter(
        new FileWriter(this.bridgeConsensusStatsRawFile));
    tempDate = null;
    int brunningDay = 0, bridgeStatusesDay = 0;
    it = bridgeConsensusResults.values().iterator();
    haveWrittenFinalLine = false;
    SortedMap<String, String> bcsAggr = new TreeMap<String, String>();
    while (it.hasNext() || !haveWrittenFinalLine) {
      String next = it.hasNext() ? it.next() : null;
      if (tempDate != null
          && (next == null || !next.substring(0, 10).equals(tempDate))) {
        if (bridgeStatusesDay > 23) {
          bcsAggr.put(tempDate, "" + (brunningDay / bridgeStatusesDay) + "\n");
        }
        brunningDay = 0;
        bridgeStatusesDay = 0;
        if (next == null) {
          haveWrittenFinalLine = true;
        }
      }
      if (next != null) {
        bwBridgeConsensusStatsRaw.append(next + "\n");
        tempDate = next.substring(0, 10);
        bridgeStatusesDay++;
        brunningDay += Integer.parseInt(next.split(",")[1]);
      }
    }
    bwBridgeConsensusStatsRaw.close();
    System.out.print("done\nWriting file " + this.statsDir
        + "/consensus-stats... ");
    BufferedWriter bwConsensusStats = new BufferedWriter(
        new FileWriter(this.consensusStatsFile));
    bwConsensusStats.append("date,exit,fast,guard,running,stable,brunning\n");
    SortedSet<String> allDates = new TreeSet<String>();
    allDates.addAll(csAggr.keySet());
    allDates.addAll(bcsAggr.keySet());
    for (String date : allDates) {
      if (csAggr.containsKey(date)) {
        bwConsensusStats.append(csAggr.get(date));
      } else {
        bwConsensusStats.append(date + ",NA,NA,NA,NA,NA");
      }
      if (bcsAggr.containsKey(date)) {
        bwConsensusStats.append("," + bcsAggr.get(date));
      } else {
        bwConsensusStats.append(",NA\n");
      }
    }
    bwConsensusStats.close();
    System.out.println("done");
  }
}

