import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;

public class GetTorProcessor {
  public GetTorProcessor(String gettorStatsUrl) {
    Logger logger = Logger.getLogger(TorperfProcessor.class.getName());
    String unparsed = null;
    try {
      logger.fine("Downloading gettor stats...");
      URL u = new URL(gettorStatsUrl);
      HttpURLConnection huc = (HttpURLConnection) u.openConnection();
      huc.setRequestMethod("GET");
      huc.connect();
      int response = huc.getResponseCode();
      if (response == 200) {
        BufferedInputStream in = new BufferedInputStream(
            huc.getInputStream());
        StringBuilder sb = new StringBuilder();
        int len;
        byte[] data = new byte[1024];
        while ((len = in.read(data, 0, 1024)) >= 0) {
          sb.append(new String(data, 0, len));
        }   
        in.close();
        unparsed = sb.toString();
      }   
      logger.fine("Finished downloading gettor stats.");
    } catch (IOException e) {
      logger.log(Level.WARNING, "Failed downloading gettor stats", e);
      return;
    }

    SortedSet<String> columns = new TreeSet<String>();
    SortedMap<String, Map<String, Integer>> data =
        new TreeMap<String, Map<String, Integer>>();
    try {
      logger.fine("Parsing downloaded gettor stats...");
      BufferedReader br = new BufferedReader(new StringReader(unparsed));
      String line = null;
      while ((line = br.readLine()) != null) {
        String[] parts = line.split(" ");
        String date = parts[0];
        Map<String, Integer> obs = new HashMap<String, Integer>();
        data.put(date, obs);
        for (int i = 2; i < parts.length; i++) {
          String key = parts[i].split(":")[0].toLowerCase();
          Integer value = new Integer(parts[i].split(":")[1]);
          columns.add(key);
          obs.put(key, value);
        }
      }
      br.close();
    } catch (IOException e) {
      logger.log(Level.WARNING, "Failed parsing gettor stats!", e);
      return;
    } catch (NumberFormatException e) {
      logger.log(Level.WARNING, "Failed parsing gettor stats!", e);
      return;
    }

    File statsFile = new File("stats/gettor-stats");
    logger.fine("Writing file " + statsFile.getAbsolutePath() + "...");
    try {
      statsFile.getParentFile().mkdirs();
      BufferedWriter bw = new BufferedWriter(new FileWriter(statsFile));
      bw.write("date");
      for (String column : columns) {
        bw.write("," + column);
      }
      bw.write("\n");
      for (String date : data.keySet()) {
        bw.write(date);
        for (String column : columns) {
          Integer value = data.get(date).get(column);
          bw.write("," + (value == null ? "NA" : value));
        }
        bw.write("\n");
      }
      bw.close();
    } catch (IOException e) {
      logger.log(Level.WARNING, "Failed writing "
          + statsFile.getAbsolutePath() + "!", e);
    }
  }
}

