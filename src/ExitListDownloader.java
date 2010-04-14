import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;
import java.util.logging.*;

public class ExitListDownloader {
  public ExitListDownloader() {
    Logger logger = Logger.getLogger(TorperfProcessor.class.getName());
    try {
      logger.fine("Downloading exit list...");
      String exitAddressesUrl =
          "http://exitlist.torproject.org/exitAddresses";
      URL u = new URL(exitAddressesUrl);
      HttpURLConnection huc = (HttpURLConnection) u.openConnection();
      huc.setRequestMethod("GET");
      huc.connect();
      int response = huc.getResponseCode();
      if (response != 200) {
        logger.warning("Could not download exit list. Response code " + 
            response);
        return;
      }
      BufferedInputStream in = new BufferedInputStream(
          huc.getInputStream());
      SimpleDateFormat printFormat =
          new SimpleDateFormat("yyyy/MM/dd/yyyy-MM-dd-HH-mm-ss");
      printFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
      File exitListFile = new File("exitlist/" + printFormat.format(
          new Date()));
      exitListFile.getParentFile().mkdirs();
      BufferedWriter bw = new BufferedWriter(new FileWriter(
          exitListFile));
      int len;
      byte[] data = new byte[1024];
      while ((len = in.read(data, 0, 1024)) >= 0) {
        bw.write(new String(data, 0, len));
      }   
      in.close();
      bw.close();
      logger.fine("Finished downloading exit list.");
    } catch (IOException e) {
      logger.log(Level.WARNING, "Failed downloading exit list", e);
      return;
    }

    /* Write stats. */
    StringBuilder dumpStats = new StringBuilder("Finished downloading "
        + "exit list.\nLast three exit lists are:");
    Stack<File> filesInInputDir = new Stack<File>();
    filesInInputDir.add(new File("exitlist"));
    SortedSet<File> lastThreeExitLists = new TreeSet<File>();
    while (!filesInInputDir.isEmpty()) {
      File pop = filesInInputDir.pop();
      if (pop.isDirectory()) {
        SortedSet<File> lastThreeElements = new TreeSet<File>();
        for (File f : pop.listFiles()) {
          lastThreeElements.add(f);
        }
        while (lastThreeElements.size() > 3) {
          lastThreeElements.remove(lastThreeElements.first());
        }
        for (File f : lastThreeElements) {
          filesInInputDir.add(f);
        }
      } else {
        lastThreeExitLists.add(pop);
        while (lastThreeExitLists.size() > 3) {
          lastThreeExitLists.remove(lastThreeExitLists.first());
        }
      }
    }
    for (File f : lastThreeExitLists) {
      dumpStats.append("\n" + f.getName());
    }
    logger.info(dumpStats.toString());
  }
}

