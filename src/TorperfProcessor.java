import java.io.*;
import java.text.*;
import java.util.*;

public class TorperfProcessor {
  public TorperfProcessor(String statsDirectory, String torperfDirectory)
      throws IOException {
    File rawFile = new File(statsDirectory + "/torperf-raw");
    File statsFile = new File(statsDirectory + "/torperf-stats");
    File torperfDir = new File(torperfDirectory);
    SortedMap<String, String> rawObs = new TreeMap<String, String>();
    SortedMap<String, String> stats = new TreeMap<String, String>();
    if (rawFile.exists()) {
      System.out.print("Reading file " + statsDirectory
          + "/torperf-raw... ");
      BufferedReader br = new BufferedReader(new FileReader(rawFile));
      String line = br.readLine(); // ignore header
      while ((line = br.readLine()) != null) {
        String key = line.substring(0, line.lastIndexOf(","));
        rawObs.put(key, line);
      }
      br.close();
      System.out.println("done");
    }
    if (statsFile.exists()) {
      System.out.print("Reading file " + statsDirectory
          + "/torperf-stats... ");
      BufferedReader br = new BufferedReader(new FileReader(statsFile));
      String line = br.readLine(); // ignore header
      while ((line = br.readLine()) != null) {
        String key = line.split(",")[0] + "," + line.split(",")[1];
        stats.put(key, line);
      }
      br.close();
      System.out.println("done");
    }
    if (torperfDir.exists()) {
      System.out.print("Importing files in " + torperfDirectory
          + "/... ");
      Stack<File> filesInInputDir = new Stack<File>();
      filesInInputDir.add(torperfDir);
      while (!filesInInputDir.isEmpty()) {
        File pop = filesInInputDir.pop();
        if (pop.isDirectory()) {
          for (File f : pop.listFiles()) {
            filesInInputDir.add(f);
          } 
        } else {
          String source = pop.getName().substring(0,
              pop.getName().indexOf("."));
          BufferedReader br = new BufferedReader(new FileReader(pop));
          String line = null;
          Format formatter = new SimpleDateFormat("yyyy-MM-dd,HH:mm:ss");
          while ((line = br.readLine()) != null) {
            String[] parts = line.split(" ");
            // remove defective lines as they occurred on gabelmoo as well
            // as and incomplete downloads 
            if (parts.length == 20 && parts[0].length() == 10
                && !parts[16].equals("0")) {
              long startSec = Long.parseLong(parts[0]);
              String dateTime = formatter.format(
                  new Date(startSec * 1000L));
              long completeMillis = Long.parseLong(parts[16].substring(5))
                  * 1000L + Long.parseLong(parts[17]) / 1000L
                  - Long.parseLong(parts[0].substring(5)) * 1000L
                  + Long.parseLong(parts[1]) / 1000L;
              String key = source + "," + dateTime;
              String value = key + "," + completeMillis;
              if (!rawObs.containsKey(key)) {
                rawObs.put(key, value);
                // TODO if torperf-stats generation takes long, compile
                // list of dates that have changes for torperf-stats and
                // only re-generate those
              }
            }
          }
          br.close();
        }
      }
      System.out.println("done");
    }
    if (rawObs.size() > 0) {
      System.out.print("Writing file " + statsDirectory
          + "/torperf-raw... ");
      BufferedWriter bw = new BufferedWriter(new FileWriter(rawFile));
      bw.append("source,date,start,completemillis\n");
      String tempSourceDate = null;
      Iterator<Map.Entry<String, String>> it = rawObs.entrySet().iterator();
      List<Long> dlTimes = new ArrayList<Long>();
      boolean haveWrittenFinalLine = false;
      while (it.hasNext() || !haveWrittenFinalLine) {
        Map.Entry<String, String> next = it.hasNext() ? it.next() : null;
        if (tempSourceDate != null
            && (next == null || !(next.getValue().split(",")[0] + ","
            + next.getValue().split(",")[1]).equals(tempSourceDate))) {
          Collections.sort(dlTimes);
          long q1 = dlTimes.get(dlTimes.size() / 4 - 1);
          long md = dlTimes.get(dlTimes.size() / 2 - 1);
          long q3 = dlTimes.get(dlTimes.size() * 3 / 4 - 1);
          dlTimes.clear();
          stats.put(tempSourceDate, tempSourceDate + "," + q1 + "," + md
              + "," + q3);
          if (next == null) {
            haveWrittenFinalLine = true;
          }
        }
        if (next != null) {
          bw.append(next.getValue() + "\n");
          String[] parts = next.getValue().split(",");
          tempSourceDate = parts[0] + "," + parts[1];
          dlTimes.add(Long.parseLong(parts[3]));
        }
      }
      bw.close();
      System.out.println("done");
    }
    if (stats.size() > 0) {
      System.out.print("Writing file " + statsDirectory
          + "/torperf-stats... ");
      BufferedWriter bw = new BufferedWriter(new FileWriter(statsFile));
      bw.append("source,date,q1,md,q3\n");
      // TODO should we handle missing days?
      for (String s : stats.values()) {
        bw.append(s + "\n");
      }
      bw.close();
      System.out.println("done");
    }
  }
}

/*
    File tsFile = new File(torperfStatsFile);
    List<String> sizeStr = new ArrayList<String>();
    sizeStr.add("5mb");
    sizeStr.add("1mb");
    sizeStr.add("50kb");
    for (String size : sizeStr) {
      SortedMap<String, File> inFiles = new TreeMap<String, File>();
      inFiles.put("gabelmoo", new File("gabelmoo-" + size + ".data"));
      inFiles.put("moria", new File("moria-" + size + ".data"));
      inFiles.put("torperf", new File("torperf-" + size + ".data"));
      File out = new File(size + ".out");
      BufferedWriter bw = new BufferedWriter(new FileWriter(out));
      bw.append("date,complete,source\n");
      Format formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      for (Map.Entry<String, File> e : inFiles.entrySet()) {
        BufferedReader br = new BufferedReader(new FileReader(
            e.getValue()));
        String line = null;
        while ((line = br.readLine()) != null) {
          String[] parts = line.split(" ");
          Date date = new Date(Long.parseLong(parts[0]) * 1000L);
          long start = Long.parseLong(parts[0]);
          long complete = Long.parseLong(parts[16]);
          if (complete - start >= 0) {
            String s = formatter.format(date);
            bw.append(s + "," + (complete - start) + "," + e.getKey()
                + "\n");
          }
        }
        br.close();
      }
      bw.close();
    }
*/

