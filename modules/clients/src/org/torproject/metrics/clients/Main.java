/* Copyright 2013--2017 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.clients;

import org.torproject.descriptor.BandwidthHistory;
import org.torproject.descriptor.BridgeNetworkStatus;
import org.torproject.descriptor.Descriptor;
import org.torproject.descriptor.DescriptorFile;
import org.torproject.descriptor.DescriptorReader;
import org.torproject.descriptor.DescriptorSourceFactory;
import org.torproject.descriptor.ExtraInfoDescriptor;
import org.torproject.descriptor.NetworkStatusEntry;
import org.torproject.descriptor.RelayNetworkStatusConsensus;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.TreeMap;

public class Main {

  /** Executes this data-processing module. */
  public static void main(String[] args) throws Exception {
    parseArgs(args);
    parseRelayDescriptors();
    parseBridgeDescriptors();
    closeOutputFiles();
  }

  private static boolean writeToSingleFile = true;
  private static boolean byStatsDateNotByDescHour = false;

  private static void parseArgs(String[] args) {
    if (args.length == 0) {
      writeToSingleFile = true;
    } else if (args.length == 1 && args[0].equals("--stats-date")) {
      writeToSingleFile = false;
      byStatsDateNotByDescHour = true;
    } else if (args.length == 1 && args[0].equals("--desc-hour")) {
      writeToSingleFile = false;
      byStatsDateNotByDescHour = false;
    } else {
      System.err.println("Usage: java " + Main.class.getName()
          + " [ --stats-date | --desc-hour ]");
      System.exit(1);
    }
  }

  private static final long ONE_HOUR_MILLIS = 60L * 60L * 1000L;

  private static final long ONE_DAY_MILLIS = 24L * ONE_HOUR_MILLIS;

  private static final long ONE_WEEK_MILLIS = 7L * ONE_DAY_MILLIS;

  private static void parseRelayDescriptors() throws Exception {
    DescriptorReader descriptorReader =
        DescriptorSourceFactory.createDescriptorReader();
    descriptorReader.setExcludeFiles(new File(
        "status/relay-descriptors"));
    descriptorReader.addDirectory(new File(
        "../../shared/in/recent/relay-descriptors/consensuses"));
    descriptorReader.addDirectory(new File(
        "../../shared/in/recent/relay-descriptors/extra-infos"));
    descriptorReader.addDirectory(new File(
        "../../shared/in/archive/relay-descriptors/consensuses"));
    descriptorReader.addDirectory(new File(
        "../../shared/in/archive/relay-descriptors/extra-infos"));
    Iterator<DescriptorFile> descriptorFiles =
        descriptorReader.readDescriptors();
    while (descriptorFiles.hasNext()) {
      DescriptorFile descriptorFile = descriptorFiles.next();
      for (Descriptor descriptor : descriptorFile.getDescriptors()) {
        if (descriptor instanceof ExtraInfoDescriptor) {
          parseRelayExtraInfoDescriptor((ExtraInfoDescriptor) descriptor);
        } else if (descriptor instanceof RelayNetworkStatusConsensus) {
          parseRelayNetworkStatusConsensus(
              (RelayNetworkStatusConsensus) descriptor);
        }
      }
    }
  }

  private static void parseRelayExtraInfoDescriptor(
      ExtraInfoDescriptor descriptor) throws IOException {
    long publishedMillis = descriptor.getPublishedMillis();
    String fingerprint = descriptor.getFingerprint()
        .toUpperCase();
    long dirreqStatsEndMillis = descriptor.getDirreqStatsEndMillis();
    long dirreqStatsIntervalLengthMillis =
        descriptor.getDirreqStatsIntervalLength() * 1000L;
    SortedMap<String, Integer> requests = descriptor.getDirreqV3Reqs();
    BandwidthHistory dirreqWriteHistory =
        descriptor.getDirreqWriteHistory();
    parseRelayDirreqV3Reqs(fingerprint, publishedMillis,
        dirreqStatsEndMillis, dirreqStatsIntervalLengthMillis, requests);
    parseRelayDirreqWriteHistory(fingerprint, publishedMillis,
        dirreqWriteHistory);
  }

  private static void parseRelayDirreqV3Reqs(String fingerprint,
      long publishedMillis, long dirreqStatsEndMillis,
      long dirreqStatsIntervalLengthMillis,
      SortedMap<String, Integer> requests) throws IOException {
    if (requests == null
        || publishedMillis - dirreqStatsEndMillis > ONE_WEEK_MILLIS
        || dirreqStatsIntervalLengthMillis != ONE_DAY_MILLIS) {
      /* Cut off all observations that are one week older than
       * the descriptor publication time, or we'll have to update
       * weeks of aggregate values every hour. */
      return;
    }
    long statsStartMillis = dirreqStatsEndMillis
        - dirreqStatsIntervalLengthMillis;
    long utcBreakMillis = (dirreqStatsEndMillis / ONE_DAY_MILLIS)
        * ONE_DAY_MILLIS;
    for (int i = 0; i < 2; i++) {
      long fromMillis = i == 0 ? statsStartMillis
          : utcBreakMillis;
      long toMillis = i == 0 ? utcBreakMillis : dirreqStatsEndMillis;
      if (fromMillis >= toMillis) {
        continue;
      }
      double intervalFraction =  ((double) (toMillis - fromMillis))
          / ((double) dirreqStatsIntervalLengthMillis);
      double sum = 0L;
      for (Map.Entry<String, Integer> e : requests.entrySet()) {
        String country = e.getKey();
        double reqs = ((double) e.getValue()) - 4.0;
        sum += reqs;
        writeOutputLine(fingerprint, "relay", "responses", country,
            "", "", fromMillis, toMillis, reqs * intervalFraction,
            publishedMillis);
      }
      writeOutputLine(fingerprint, "relay", "responses", "", "",
          "", fromMillis, toMillis, sum * intervalFraction,
          publishedMillis);
    }
  }

  private static void parseRelayDirreqWriteHistory(String fingerprint,
      long publishedMillis, BandwidthHistory dirreqWriteHistory)
      throws IOException {
    if (dirreqWriteHistory == null
        || publishedMillis - dirreqWriteHistory.getHistoryEndMillis()
        > ONE_WEEK_MILLIS) {
      return;
      /* Cut off all observations that are one week older than
       * the descriptor publication time, or we'll have to update
       * weeks of aggregate values every hour. */
    }
    long intervalLengthMillis =
        dirreqWriteHistory.getIntervalLength() * 1000L;
    for (Map.Entry<Long, Long> e
        : dirreqWriteHistory.getBandwidthValues().entrySet()) {
      long intervalEndMillis = e.getKey();
      long intervalStartMillis =
          intervalEndMillis - intervalLengthMillis;
      for (int i = 0; i < 2; i++) {
        long fromMillis = intervalStartMillis;
        long toMillis = intervalEndMillis;
        double writtenBytes = (double) e.getValue();
        if (intervalStartMillis / ONE_DAY_MILLIS
            < intervalEndMillis / ONE_DAY_MILLIS) {
          long utcBreakMillis = (intervalEndMillis
              / ONE_DAY_MILLIS) * ONE_DAY_MILLIS;
          if (i == 0) {
            toMillis = utcBreakMillis;
          } else if (i == 1) {
            fromMillis = utcBreakMillis;
          }
          double intervalFraction = ((double) (toMillis - fromMillis))
              / ((double) intervalLengthMillis);
          writtenBytes *= intervalFraction;
        } else if (i == 1) {
          break;
        }
        writeOutputLine(fingerprint, "relay", "bytes", "", "", "",
            fromMillis, toMillis, writtenBytes, publishedMillis);
      }
    }
  }

  private static void parseRelayNetworkStatusConsensus(
      RelayNetworkStatusConsensus consensus) throws IOException {
    long fromMillis = consensus.getValidAfterMillis();
    long toMillis = consensus.getFreshUntilMillis();
    for (NetworkStatusEntry statusEntry
        : consensus.getStatusEntries().values()) {
      String fingerprint = statusEntry.getFingerprint()
          .toUpperCase();
      if (statusEntry.getFlags().contains("Running")) {
        writeOutputLine(fingerprint, "relay", "status", "", "", "",
            fromMillis, toMillis, 0.0, fromMillis);
      }
    }
  }

  private static void parseBridgeDescriptors() throws Exception {
    DescriptorReader descriptorReader =
        DescriptorSourceFactory.createDescriptorReader();
    descriptorReader.setExcludeFiles(new File(
        "status/bridge-descriptors"));
    descriptorReader.addDirectory(new File(
        "../../shared/in/recent/bridge-descriptors"));
    descriptorReader.addDirectory(new File(
        "../../shared/in/archive/bridge-descriptors"));
    Iterator<DescriptorFile> descriptorFiles =
        descriptorReader.readDescriptors();
    while (descriptorFiles.hasNext()) {
      DescriptorFile descriptorFile = descriptorFiles.next();
      for (Descriptor descriptor : descriptorFile.getDescriptors()) {
        if (descriptor instanceof ExtraInfoDescriptor) {
          parseBridgeExtraInfoDescriptor(
              (ExtraInfoDescriptor) descriptor);
        } else if (descriptor instanceof BridgeNetworkStatus) {
          parseBridgeNetworkStatus((BridgeNetworkStatus) descriptor);
        }
      }
    }
  }

  private static void parseBridgeExtraInfoDescriptor(
      ExtraInfoDescriptor descriptor) throws IOException {
    String fingerprint = descriptor.getFingerprint().toUpperCase();
    long publishedMillis = descriptor.getPublishedMillis();
    long dirreqStatsEndMillis = descriptor.getDirreqStatsEndMillis();
    long dirreqStatsIntervalLengthMillis =
        descriptor.getDirreqStatsIntervalLength() * 1000L;
    parseBridgeDirreqV3Resp(fingerprint, publishedMillis,
        dirreqStatsEndMillis, dirreqStatsIntervalLengthMillis,
        descriptor.getDirreqV3Resp(),
        descriptor.getBridgeIps(),
        descriptor.getBridgeIpTransports(),
        descriptor.getBridgeIpVersions());

    parseBridgeDirreqWriteHistory(fingerprint, publishedMillis,
          descriptor.getDirreqWriteHistory());
  }

  private static void parseBridgeDirreqV3Resp(String fingerprint,
      long publishedMillis, long dirreqStatsEndMillis,
      long dirreqStatsIntervalLengthMillis,
      SortedMap<String, Integer> responses,
      SortedMap<String, Integer> bridgeIps,
      SortedMap<String, Integer> bridgeIpTransports,
      SortedMap<String, Integer> bridgeIpVersions) throws IOException {
    if (responses == null
        || publishedMillis - dirreqStatsEndMillis > ONE_WEEK_MILLIS
        || dirreqStatsIntervalLengthMillis != ONE_DAY_MILLIS) {
      /* Cut off all observations that are one week older than
       * the descriptor publication time, or we'll have to update
       * weeks of aggregate values every hour. */
      return;
    }
    long statsStartMillis = dirreqStatsEndMillis
        - dirreqStatsIntervalLengthMillis;
    long utcBreakMillis = (dirreqStatsEndMillis / ONE_DAY_MILLIS)
        * ONE_DAY_MILLIS;
    double resp = ((double) responses.get("ok")) - 4.0;
    if (resp > 0.0) {
      for (int i = 0; i < 2; i++) {
        long fromMillis = i == 0 ? statsStartMillis
            : utcBreakMillis;
        long toMillis = i == 0 ? utcBreakMillis : dirreqStatsEndMillis;
        if (fromMillis >= toMillis) {
          continue;
        }
        double intervalFraction = ((double) (toMillis - fromMillis))
            / ((double) dirreqStatsIntervalLengthMillis);
        writeOutputLine(fingerprint, "bridge", "responses", "", "",
            "", fromMillis, toMillis, resp * intervalFraction,
            publishedMillis);
        parseBridgeRespByCategory(fingerprint, fromMillis, toMillis, resp,
            dirreqStatsIntervalLengthMillis, "country", bridgeIps,
            publishedMillis);
        parseBridgeRespByCategory(fingerprint, fromMillis, toMillis, resp,
            dirreqStatsIntervalLengthMillis, "transport",
            bridgeIpTransports, publishedMillis);
        parseBridgeRespByCategory(fingerprint, fromMillis, toMillis, resp,
            dirreqStatsIntervalLengthMillis, "version", bridgeIpVersions,
            publishedMillis);
      }
    }
  }

  private static void parseBridgeRespByCategory(String fingerprint,
      long fromMillis, long toMillis, double resp,
      long dirreqStatsIntervalLengthMillis, String category,
      SortedMap<String, Integer> frequencies, long publishedMillis)
      throws IOException {
    double total = 0.0;
    SortedMap<String, Double> frequenciesCopy = new TreeMap<>();
    if (frequencies != null) {
      for (Map.Entry<String, Integer> e : frequencies.entrySet()) {
        if (e.getValue() < 4.0) {
          continue;
        }
        double frequency = ((double) e.getValue()) - 4.0;
        frequenciesCopy.put(e.getKey(), frequency);
        total += frequency;
      }
    }
    /* If we're not told any frequencies, or at least none of them are
     * greater than 4, put in a default that we'll attribute all responses
     * to. */
    if (total == 0) {
      if (category.equals("country")) {
        frequenciesCopy.put("??", 4.0);
      } else if (category.equals("transport")) {
        frequenciesCopy.put("<OR>", 4.0);
      } else if (category.equals("version")) {
        frequenciesCopy.put("v4", 4.0);
      }
      total = 4.0;
    }
    for (Map.Entry<String, Double> e : frequenciesCopy.entrySet()) {
      double intervalFraction = ((double) (toMillis - fromMillis))
          / ((double) dirreqStatsIntervalLengthMillis);
      double val = resp * intervalFraction * e.getValue() / total;
      if (category.equals("country")) {
        writeOutputLine(fingerprint, "bridge", "responses", e.getKey(),
            "", "", fromMillis, toMillis, val, publishedMillis);
      } else if (category.equals("transport")) {
        writeOutputLine(fingerprint, "bridge", "responses", "",
            e.getKey(), "", fromMillis, toMillis, val, publishedMillis);
      } else if (category.equals("version")) {
        writeOutputLine(fingerprint, "bridge", "responses", "", "",
            e.getKey(), fromMillis, toMillis, val, publishedMillis);
      }
    }
  }

  private static void parseBridgeDirreqWriteHistory(String fingerprint,
      long publishedMillis, BandwidthHistory dirreqWriteHistory)
      throws IOException {
    if (dirreqWriteHistory == null
        || publishedMillis - dirreqWriteHistory.getHistoryEndMillis()
        > ONE_WEEK_MILLIS) {
      /* Cut off all observations that are one week older than
       * the descriptor publication time, or we'll have to update
       * weeks of aggregate values every hour. */
      return;
    }
    long intervalLengthMillis =
        dirreqWriteHistory.getIntervalLength() * 1000L;
    for (Map.Entry<Long, Long> e
        : dirreqWriteHistory.getBandwidthValues().entrySet()) {
      long intervalEndMillis = e.getKey();
      long intervalStartMillis =
          intervalEndMillis - intervalLengthMillis;
      for (int i = 0; i < 2; i++) {
        long fromMillis = intervalStartMillis;
        long toMillis = intervalEndMillis;
        double writtenBytes = (double) e.getValue();
        if (intervalStartMillis / ONE_DAY_MILLIS
            < intervalEndMillis / ONE_DAY_MILLIS) {
          long utcBreakMillis = (intervalEndMillis
              / ONE_DAY_MILLIS) * ONE_DAY_MILLIS;
          if (i == 0) {
            toMillis = utcBreakMillis;
          } else if (i == 1) {
            fromMillis = utcBreakMillis;
          }
          double intervalFraction = ((double) (toMillis - fromMillis))
              / ((double) intervalLengthMillis);
          writtenBytes *= intervalFraction;
        } else if (i == 1) {
          break;
        }
        writeOutputLine(fingerprint, "bridge", "bytes", "",
            "", "", fromMillis, toMillis, writtenBytes, publishedMillis);
      }
    }
  }

  private static void parseBridgeNetworkStatus(BridgeNetworkStatus status)
      throws IOException {
    long publishedMillis = status.getPublishedMillis();
    long fromMillis = (publishedMillis / ONE_HOUR_MILLIS)
        * ONE_HOUR_MILLIS;
    long toMillis = fromMillis + ONE_HOUR_MILLIS;
    for (NetworkStatusEntry statusEntry
        : status.getStatusEntries().values()) {
      String fingerprint = statusEntry.getFingerprint()
          .toUpperCase();
      if (statusEntry.getFlags().contains("Running")) {
        writeOutputLine(fingerprint, "bridge", "status", "", "", "",
            fromMillis, toMillis, 0.0, publishedMillis);
      }
    }
  }

  private static Map<String, BufferedWriter> openOutputFiles = new HashMap<>();

  private static void writeOutputLine(String fingerprint, String node,
      String metric, String country, String transport, String version,
      long fromMillis, long toMillis, double val, long publishedMillis)
      throws IOException {
    if (fromMillis > toMillis) {
      return;
    }
    String fromDateTime = formatDateTimeMillis(fromMillis);
    String toDateTime = formatDateTimeMillis(toMillis);
    BufferedWriter bw = getOutputFile(fromDateTime, publishedMillis);
    bw.write(String.format("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%.1f\n",
        fingerprint, node, metric, country, transport, version,
        fromDateTime, toDateTime, val));
  }

  private static SimpleDateFormat dateTimeFormat = null;

  private static String formatDateTimeMillis(long millis) {
    if (dateTimeFormat == null) {
      dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      dateTimeFormat.setLenient(false);
      dateTimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }
    return dateTimeFormat.format(millis);
  }

  private static BufferedWriter getOutputFile(String fromDateTime,
      long publishedMillis) throws IOException {
    String outputFileName;
    if (writeToSingleFile) {
      outputFileName = "out/userstats.sql";
    } else if (byStatsDateNotByDescHour) {
      outputFileName = "out/userstats-" + fromDateTime.substring(0, 10)
          + ".sql";
    } else {
      String publishedHourDateTime = formatDateTimeMillis(
          (publishedMillis / ONE_HOUR_MILLIS) * ONE_HOUR_MILLIS);
      outputFileName = "out/userstats-"
          + publishedHourDateTime.substring(0, 10) + "-"
          + publishedHourDateTime.substring(11, 13) + ".sql";
    }
    BufferedWriter bw = openOutputFiles.get(outputFileName);
    if (bw == null) {
      bw = openOutputFile(outputFileName);
      openOutputFiles.put(outputFileName, bw);
    }
    return bw;
  }

  private static BufferedWriter openOutputFile(String outputFileName)
      throws IOException {
    File outputFile = new File(outputFileName);
    outputFile.getParentFile().mkdirs();
    BufferedWriter bw = new BufferedWriter(new FileWriter(
        outputFileName));
    bw.write("BEGIN;\n");
    bw.write("LOCK TABLE imported NOWAIT;\n");
    bw.write("COPY imported (fingerprint, node, metric, country, "
        + "transport, version, stats_start, stats_end, val) FROM "
        + "stdin;\n");
    return bw;
  }

  private static void closeOutputFiles() throws IOException {
    for (BufferedWriter bw : openOutputFiles.values()) {
      bw.write("\\.\n");
      bw.write("SELECT merge();\n");
      bw.write("SELECT aggregate();\n");
      bw.write("SELECT combine();\n");
      bw.write("TRUNCATE imported;\n");
      bw.write("COMMIT;\n");
      bw.close();
    }
  }
}

