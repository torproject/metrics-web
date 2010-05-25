import java.io.*;
import java.text.*;
import java.util.*;
import java.util.logging.*;
import org.apache.commons.codec.digest.*;

public class BridgeDescriptorParser {
  private ConsensusStatsFileHandler csfh;
  private BridgeStatsFileHandler bsfh;
  private SanitizedBridgesWriter sbw;
  private SortedSet<String> countries;
  private Logger logger;
  public BridgeDescriptorParser(ConsensusStatsFileHandler csfh,
      BridgeStatsFileHandler bsfh, SanitizedBridgesWriter sbw,
      SortedSet<String> countries) {
    this.csfh = csfh;
    this.bsfh = bsfh;
    this.sbw = sbw;
    this.countries = countries;
    this.logger =
        Logger.getLogger(BridgeDescriptorParser.class.getName());
  }
  public void parse(byte[] allData, String dateTime, boolean sanitized) {
    try {
      BufferedReader br = new BufferedReader(new StringReader(
          new String(allData, "US-ASCII")));
      SimpleDateFormat timeFormat = new SimpleDateFormat(
          "yyyy-MM-dd HH:mm:ss");
      timeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
      String hashedIdentity = null, platformLine = null,
          publishedLine = null, geoipStartTimeLine = null,
          bridgeStatsEndLine = null;
      boolean skip = false;
      String line = null;
      while ((line = br.readLine()) != null) {
        if (line.startsWith("r ")) {
          if (this.sbw != null) {
            if (sanitized) {
              this.sbw.storeSanitizedNetworkStatus(allData, dateTime);
            } else {
              this.sbw.sanitizeAndStoreNetworkStatus(allData, dateTime);
            }
          }
          int runningBridges = 0;
          while ((line = br.readLine()) != null) {
            if (line.startsWith("s ") && line.contains(" Running")) {
              runningBridges++;
            }
          }
          if (this.csfh != null) {
            this.csfh.addBridgeConsensusResults(dateTime, runningBridges);
          }
        } else if (line.startsWith("router ")) {
          if (this.sbw != null) {
            if (sanitized) {
              this.sbw.storeSanitizedServerDescriptor(allData);
            } else {
              this.sbw.sanitizeAndStoreServerDescriptor(allData);
            }
          }
        } else if (line.startsWith("extra-info ")) {
          if (this.sbw != null) {
            if (sanitized) {
              this.sbw.storeSanitizedExtraInfoDescriptor(allData);
            } else {
              this.sbw.sanitizeAndStoreExtraInfoDescriptor(allData);
            }
          }
          hashedIdentity = sanitized ? line.split(" ")[2]
              : DigestUtils.shaHex(line.split(" ")[2]).toUpperCase();
          if (this.bsfh != null) {
            skip = this.bsfh.isKnownRelay(hashedIdentity);
          }
        } else if (!skip && line.startsWith("platform ")) {
          platformLine = line;
        } else if (!skip && line.startsWith("published ")) {
          publishedLine = line;
        } else if (line.startsWith("opt fingerprint") ||
            line.startsWith("fingerprint")) {
          String identity = line.substring(line.startsWith("opt ") ?
              "opt fingerprint".length() : "fingerprint".length()).
              replaceAll(" ", "").toLowerCase();
          hashedIdentity = sanitized ? identity
              : DigestUtils.shaHex(identity).toUpperCase();
        } else if (!skip && line.startsWith("geoip-start-time ")) {
          geoipStartTimeLine = line;
        } else if (!skip && line.startsWith("geoip-client-origins")
            && line.split(" ").length > 1) {
          if (publishedLine == null ||
              geoipStartTimeLine == null) {
            this.logger.warning("Either published line or "
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
          if (this.bsfh != null) {
            this.bsfh.addObs(hashedIdentity, date, time, obs);
          }
        } else if (!skip && line.startsWith("bridge-stats-end ")) {
          bridgeStatsEndLine = line;
        } else if (!skip && line.startsWith("bridge-ips")
            && line.split(" ").length > 1) {
          if (bridgeStatsEndLine == null) {
            this.logger.warning("bridge-ips line without preceding "
                + "bridge-stats-end line in "
                + (sanitized ? "sanitized" : "non-sanitized")
                + " bridge descriptor.");
            break;
          }
          Map<String, String> obs = new HashMap<String, String>();
          String[] parts = line.split(" ")[1].split(",");
          for (String p : parts) {
            for (String c : countries) {
              if (p.startsWith(c)) {
                obs.put(c, String.format("%.2f",
                    (double) Long.parseLong(p.substring(3)) - 4L));
              }
            }
          }
          String date = bridgeStatsEndLine.split(" ")[1];
          String time = bridgeStatsEndLine.split(" ")[2];
          if (this.bsfh != null) {
            this.bsfh.addObs(hashedIdentity, date, time, obs);
          }
        }
      }
      if (this.bsfh != null && platformLine != null &&
          platformLine.startsWith("platform Tor 0.2.2")) {
        String date = publishedLine.split(" ")[1];
        String time = publishedLine.split(" ")[2];
        this.bsfh.addZeroTwoTwoDescriptor(hashedIdentity, date, time);
      }
    } catch (IOException e) {
      this.logger.log(Level.WARNING, "Could not parse bridge descriptor.",
          e);
      return;
    } catch (ParseException e) {
      this.logger.log(Level.WARNING, "Could not parse bridge descriptor.",
          e);
      return;
    }
  }
}
