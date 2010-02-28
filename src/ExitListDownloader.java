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
  }
}

