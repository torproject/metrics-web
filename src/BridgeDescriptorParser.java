import java.io.*;
import java.text.*;
import java.util.*;
import org.apache.commons.codec.digest.*;

public class BridgeDescriptorParser {
  private ConsensusStatsFileHandler csfh;
  private BridgeStatsFileHandler bsfh;
  SortedSet<String> countries;
  public BridgeDescriptorParser(ConsensusStatsFileHandler csfh,
      BridgeStatsFileHandler bsfh, SortedSet<String> countries) {
    this.csfh = csfh;
    this.bsfh = bsfh;
    this.countries = countries;
  }
  public void parse(BufferedReader br, String dateTime, boolean sanitized)
      throws IOException, ParseException {
    SimpleDateFormat timeFormat = new SimpleDateFormat(
        "yyyy-MM-dd HH:mm:ss");
    timeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    String hashedIdentity = null, publishedLine = null,
        geoipStartTimeLine = null;
    boolean skip = false;
    String line = null;
    while ((line = br.readLine()) != null) {
      if (line.startsWith("r ")) {
        int runningBridges = 0;
        while ((line = br.readLine()) != null) {
          if (line.startsWith("s ") && line.contains(" Running")) {
            runningBridges++;
          }
        }
        csfh.addBridgeConsensusResults(dateTime, runningBridges);
      } else if (line.startsWith("extra-info ")) {
        hashedIdentity = sanitized ? line.split(" ")[2]
            : DigestUtils.shaHex(line.split(" ")[2]).toUpperCase();
        skip = bsfh.isKnownRelay(hashedIdentity);
      } else if (!skip && line.startsWith("published ")) {
        publishedLine = line;
      } else if (!skip && line.startsWith("geoip-start-time ")) {
        geoipStartTimeLine = line;
      } else if (!skip && line.startsWith("geoip-client-origins")
          && line.split(" ").length > 1) {
        if (publishedLine == null ||
            geoipStartTimeLine == null) {
          System.out.println("Either published line or "
              + "geoip-start-time line is not present in "
              + (sanitized ? "sanitized" : "non-sanitized")
              + " bridge descriptors from " + dateTime + ".");
          break;
        }
        long published = timeFormat.parse(publishedLine.
            substring("published ".length())).getTime();
        long started = timeFormat.parse(geoipStartTimeLine.
            substring("geoip-start-time ".length())).getTime();
        long seconds = (published - started) / 1000L;
        Map<String, String> obs = new HashMap<String, String>();
        String[] parts = line.split(" ")[1].split(",");
        for (String p : parts) {
          for (String c : countries) {
            if (p.startsWith(c)) {
              obs.put(c, String.format("%.2f",
                  ((double) Long.parseLong(p.substring(3)) - 4L)
                  * 86400.0D / ((double) seconds)));
            }
          }
        }
        String date = publishedLine.split(" ")[1];
        String time = publishedLine.split(" ")[2];
        bsfh.addObs(hashedIdentity, date, time, obs);
      }
    }
  }
}
