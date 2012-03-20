/* Copyright 2011, 2012 The Tor Project
 * See LICENSE for licensing information */
package org.torproject.ernie.cron;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.codec.digest.DigestUtils;

public class BridgeDescriptorParser {
  private ConsensusStatsFileHandler csfh;
  private BridgeStatsFileHandler bsfh;
  private Logger logger;
  public BridgeDescriptorParser(ConsensusStatsFileHandler csfh,
      BridgeStatsFileHandler bsfh) {
    this.csfh = csfh;
    this.bsfh = bsfh;
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
        } else if (line.startsWith("extra-info ")) {
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
          double allUsers = 0.0D;
          Map<String, String> obs = new HashMap<String, String>();
          String[] parts = line.split(" ")[1].split(",");
          for (String p : parts) {
            String country = p.substring(0, 2);
            double users = ((double) Long.parseLong(p.substring(3)) - 4L)
                    * 86400.0D / ((double) seconds);
            allUsers += users;
            obs.put(country, String.format("%.2f", users));
          }
          obs.put("zy", String.format("%.2f", allUsers));
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
          double allUsers = 0.0D;
          Map<String, String> obs = new HashMap<String, String>();
          String[] parts = line.split(" ")[1].split(",");
          for (String p : parts) {
            String country = p.substring(0, 2);
            double users = (double) Long.parseLong(p.substring(3)) - 4L;
            allUsers += users;
            obs.put(country, String.format("%.2f", users));
          }
          obs.put("zy", String.format("%.2f", allUsers));
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

