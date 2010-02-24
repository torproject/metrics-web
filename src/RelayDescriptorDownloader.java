import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;
import java.util.logging.*;

/**
 * Download the current consensus and relevant extra-info descriptors and
 * hand them to the relay descriptor parser.
 */
public class RelayDescriptorDownloader {
  public RelayDescriptorDownloader(RelayDescriptorParser rdp,
      ArchiveWriter aw, List<String> authorities,
      SortedSet<String> directories) {
    Logger logger = Logger.getLogger(
        RelayDescriptorDownloader.class.getName());
    List<String> remainingAuthorities =
        new ArrayList<String>(authorities);
    if (rdp != null) {
      try {
        rdp.initialize(); // TODO get rid of this non-sense
      } catch (IOException e) {
        return;
      }
    }
    Set<String> urls = new HashSet<String>();
    Set<String> downloaded = new HashSet<String>();
    if (rdp != null) {
      urls.addAll(rdp.getMissingDescriptorUrls());
    }
    do {
      if (aw != null) {
        urls.addAll(aw.getMissingDescriptorUrls());
      }
      urls.removeAll(downloaded);
      SortedSet<String> sortedUrls = new TreeSet<String>(urls);
      while (!remainingAuthorities.isEmpty() && !sortedUrls.isEmpty()) {
        String authority = remainingAuthorities.get(0);
        String url = sortedUrls.first();
        try {
          URL u = new URL("http://" + authority + url);
          HttpURLConnection huc =
              (HttpURLConnection) u.openConnection();
          huc.setRequestMethod("GET");
          huc.connect();
          int response = huc.getResponseCode();
          logger.fine("Downloading http://" + authority + url + " -> "
              + response);
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
            String result = sb.toString();
            if (rdp != null) {
              BufferedReader br = new BufferedReader(new StringReader(
                  result));
              rdp.parse(br);
              br.close();
            }
            if (aw != null) {
              BufferedReader br = new BufferedReader(new StringReader(
                  result));
              try {
                aw.store(br);
              } catch (Exception e) {
                e.printStackTrace();
                //TODO find better way to handle this
              }
              br.close();
            }
          }
          sortedUrls.remove(url);
        } catch (IOException e) {
          remainingAuthorities.remove(authority);
          if (!remainingAuthorities.isEmpty()) {
            logger.log(Level.INFO, "Failed downloading from "
                + authority + "!", e);
          } else {
            logger.log(Level.WARNING, "Failed downloading from "
                + authority + "! We have no authorities left to download "
                + "from!", e);
          }
        }
      }
      downloaded.addAll(urls);
    } while (!urls.isEmpty());
  }
}

