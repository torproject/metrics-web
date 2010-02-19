import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;

/**
 * Download the current consensus and relevant extra-info descriptors and
 * hand them to the relay descriptor parser.
 */
public class RelayDescriptorDownloader {
  public RelayDescriptorDownloader(RelayDescriptorParser rdp,
      String authority, SortedSet<String> directories) {
    Logger logger =
        Logger.getLogger(RelayDescriptorDownloader.class.getName());
    try {
      rdp.initialize();
    } catch (IOException e) {
      return;
    }
    try {
      logger.info("Downloading current consensus from " + authority
          + "...");
      URL u = new URL("http://" + authority
          + "/tor/status-vote/current/consensus");
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
        String consensus = sb.toString();
        rdp.parse(new BufferedReader(new StringReader(consensus)));
      }
      logger.info("Finished downloading current consensus from "
          + authority + ".");
      logger.info("Downloading extra-info descriptors from " + authority
          + "...");
      Stack<String> extraInfos = new Stack<String>();
      for (String fingerprint : directories) {
        u = new URL("http://" + authority + "/tor/extra/fp/"
            + fingerprint);
        huc = (HttpURLConnection) u.openConnection();
        huc.setRequestMethod("GET");
        huc.connect();
        response = huc.getResponseCode();
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
          String extraInfo = sb.toString();
          if (extraInfo.length() > 0) {
            BufferedReader br = new BufferedReader(
                new StringReader(extraInfo));
            rdp.parse(br);
          }
        }
      }
      logger.info("Finished downloading extra-info descriptors from "
          + authority + ".");
    } catch (IOException e) {
      logger.log(Level.WARNING, "Failed downloading either current "
          + "consensus or extra-info descriptors from " + authority
          + "!", e);
    }
  }
}

