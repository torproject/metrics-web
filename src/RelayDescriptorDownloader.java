import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;
import java.util.logging.*;
import org.apache.commons.codec.digest.*;

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
      SortedSet<String> sortedAuthorities =
          new TreeSet<String>(remainingAuthorities);
      SortedSet<String> sortedUrls = new TreeSet<String>(urls);
      SortedSet<String> retryUrls = new TreeSet<String>();
      while (!sortedAuthorities.isEmpty() && !sortedUrls.isEmpty()) {
        String authority = sortedAuthorities.first();
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
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int len;
            byte[] data = new byte[1024];
            while ((len = in.read(data, 0, 1024)) >= 0) {
              // we need to write the result to a byte array in order
              // to get a sane digest; otherwise, descriptors with
              // non-ASCII chars lead to different digests.
              baos.write(data, 0, len);
            }
            in.close();
            String digest = null;
            byte[] allData = baos.toByteArray();
            int beforeSig = new String(allData).indexOf(
                "\nrouter-signature\n")
                + "\nrouter-signature\n".length();
            byte[] noSig = new byte[beforeSig];
            System.arraycopy(allData, 0, noSig, 0, beforeSig);
            digest = DigestUtils.shaHex(noSig);
            // TODO UTF-8 may be wrong, but we don't care about the fields
            // containing non-ASCII
            String result = new String(allData, "UTF-8");
            boolean verified = false;
            if (url.contains("/tor/server/d/") ||
                url.contains("/tor/extra/d/")) {
              if (url.endsWith(digest)) {
                verified = true;
              } else {
                logger.warning("Downloaded descriptor digest (" + digest
                    + " doesn't match what we asked for (" + url + ")! "
                    + "Retrying.");
                retryUrls.add(url);
              }
            } else {
              verified = true;
              // TODO verify downloaded consensuses and votes, too
            }
            if (verified) {
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
                  aw.store(allData);
                } catch (Exception e) {
                  e.printStackTrace();
                  //TODO find better way to handle this
                }
                br.close();
              }
            }
          } else {
            retryUrls.add(url);
          }
          sortedUrls.remove(url);
          if (sortedUrls.isEmpty()) {
            sortedAuthorities.remove(authority);
            sortedUrls.addAll(retryUrls);
            retryUrls.clear();
          }
        } catch (IOException e) {
          remainingAuthorities.remove(authority);
          sortedAuthorities.remove(authority);
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

