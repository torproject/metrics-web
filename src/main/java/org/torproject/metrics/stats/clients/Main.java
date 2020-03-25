/* Copyright 2013--2020 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.stats.clients;

import org.torproject.descriptor.BandwidthHistory;
import org.torproject.descriptor.BridgeNetworkStatus;
import org.torproject.descriptor.Descriptor;
import org.torproject.descriptor.DescriptorReader;
import org.torproject.descriptor.DescriptorSourceFactory;
import org.torproject.descriptor.ExtraInfoDescriptor;
import org.torproject.descriptor.NetworkStatusEntry;
import org.torproject.descriptor.RelayNetworkStatusConsensus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.SQLException;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class Main {

  private static final Logger logger = LoggerFactory.getLogger(Main.class);

  private static Database database;

  static final File baseDir = new File(
      org.torproject.metrics.stats.main.Main.modulesDir, "clients");

  /** Executes this data-processing module. */
  public static void main(String[] args) throws Exception {

    logger.info("Starting clients module.");

    logger.info("Connecting to database.");
    database = new Database();

    logger.info("Reading relay descriptors and importing relevant parts into "
        + "the database.");
    parseRelayDescriptors();

    logger.info("Reading bridge descriptors and importing relevant parts into "
        + "the database.");
    parseBridgeDescriptors();

    logger.info("Processing newly imported data.");
    database.processImported();
    database.commit();

    logger.info("Querying aggregated statistics from the database.");
    new Writer().write(new File(baseDir, "stats/userstats.csv").toPath(),
        database.queryEstimated());
    new Writer().write(new File(baseDir, "stats/userstats-combined.csv")
        .toPath(), database.queryCombined());

    logger.info("Disconnecting from database.");
    database.close();

    logger.info("Running detector.");
    new Detector().detect();

    logger.info("Terminating clients module.");
  }

  private static final long ONE_HOUR_MILLIS = 60L * 60L * 1000L;

  private static final long ONE_DAY_MILLIS = 24L * ONE_HOUR_MILLIS;

  private static final long ONE_WEEK_MILLIS = 7L * ONE_DAY_MILLIS;

  private static void parseRelayDescriptors() throws Exception {
    DescriptorReader descriptorReader =
        DescriptorSourceFactory.createDescriptorReader();
    File historyFile = new File(baseDir, "status/relay-descriptors");
    descriptorReader.setHistoryFile(historyFile);
    for (Descriptor descriptor : descriptorReader.readDescriptors(
        new File(org.torproject.metrics.stats.main.Main.descriptorsDir,
            "recent/relay-descriptors/consensuses"),
        new File(org.torproject.metrics.stats.main.Main.descriptorsDir,
            "recent/relay-descriptors/extra-infos"),
        new File(org.torproject.metrics.stats.main.Main.descriptorsDir,
            "archive/relay-descriptors/consensuses"),
        new File(org.torproject.metrics.stats.main.Main.descriptorsDir,
            "archive/relay-descriptors/extra-infos"))) {
      if (descriptor instanceof ExtraInfoDescriptor) {
        parseRelayExtraInfoDescriptor((ExtraInfoDescriptor) descriptor);
      } else if (descriptor instanceof RelayNetworkStatusConsensus) {
        parseRelayNetworkStatusConsensus(
            (RelayNetworkStatusConsensus) descriptor);
      }
    }
    database.commit();
    descriptorReader.saveHistoryFile(historyFile);
  }

  private static void parseRelayExtraInfoDescriptor(
      ExtraInfoDescriptor descriptor) throws SQLException {
    long publishedMillis = descriptor.getPublishedMillis();
    String fingerprint = descriptor.getFingerprint()
        .toUpperCase();
    long dirreqStatsEndMillis = descriptor.getDirreqStatsEndMillis();
    long dirreqStatsIntervalLengthMillis =
        descriptor.getDirreqStatsIntervalLength() * 1000L;
    SortedMap<String, Integer> responses = descriptor.getDirreqV3Resp();
    SortedMap<String, Integer> requests = descriptor.getDirreqV3Reqs();
    BandwidthHistory dirreqWriteHistory =
        descriptor.getDirreqWriteHistory();
    parseRelayDirreqV3Resp(fingerprint, publishedMillis, dirreqStatsEndMillis,
        dirreqStatsIntervalLengthMillis, responses, requests);
    parseRelayDirreqWriteHistory(fingerprint, publishedMillis,
        dirreqWriteHistory);
  }

  private static void parseRelayDirreqV3Resp(String fingerprint,
      long publishedMillis, long dirreqStatsEndMillis,
      long dirreqStatsIntervalLengthMillis,
      SortedMap<String, Integer> responses,
      SortedMap<String, Integer> requests) throws SQLException {
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
        long fromMillis = i == 0 ? statsStartMillis : utcBreakMillis;
        long toMillis = i == 0 ? utcBreakMillis : dirreqStatsEndMillis;
        if (fromMillis >= toMillis) {
          continue;
        }
        double intervalFraction = ((double) (toMillis - fromMillis))
            / ((double) dirreqStatsIntervalLengthMillis);
        double total = 0L;
        SortedMap<String, Double> requestsCopy = new TreeMap<>();
        if (null != requests) {
          for (Map.Entry<String, Integer> e : requests.entrySet()) {
            if (e.getValue() < 4.0) {
              continue;
            }
            double frequency = ((double) e.getValue()) - 4.0;
            requestsCopy.put(e.getKey(), frequency);
            total += frequency;
          }
        }
        /* If we're not told any requests, or at least none of them are greater
         * than 4, put in a default that we'll attribute all responses to. */
        if (requestsCopy.isEmpty()) {
          requestsCopy.put("??", 4.0);
          total = 4.0;
        }
        for (Map.Entry<String, Double> e : requestsCopy.entrySet()) {
          String country = e.getKey();
          double val = resp * intervalFraction * e.getValue() / total;
          database.insertIntoImported(fingerprint, "relay", "responses",
              country, "", "", fromMillis, toMillis, val);
        }
        database.insertIntoImported(fingerprint, "relay", "responses", "", "",
            "", fromMillis, toMillis, resp * intervalFraction);
      }
    }
  }

  private static void parseRelayDirreqWriteHistory(String fingerprint,
      long publishedMillis, BandwidthHistory dirreqWriteHistory)
      throws SQLException {
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
        database.insertIntoImported(fingerprint, "relay", "bytes", "", "", "",
            fromMillis, toMillis, writtenBytes);
      }
    }
  }

  private static void parseRelayNetworkStatusConsensus(
      RelayNetworkStatusConsensus consensus) throws SQLException {
    long fromMillis = consensus.getValidAfterMillis();
    long toMillis = consensus.getFreshUntilMillis();
    for (NetworkStatusEntry statusEntry
        : consensus.getStatusEntries().values()) {
      String fingerprint = statusEntry.getFingerprint()
          .toUpperCase();
      if (statusEntry.getFlags().contains("Running")) {
        database.insertIntoImported(fingerprint, "relay", "status", "", "", "",
            fromMillis, toMillis, 0.0);
      }
    }
  }

  private static void parseBridgeDescriptors() throws Exception {
    DescriptorReader descriptorReader =
        DescriptorSourceFactory.createDescriptorReader();
    File historyFile = new File(baseDir, "status/bridge-descriptors");
    descriptorReader.setHistoryFile(historyFile);
    for (Descriptor descriptor : descriptorReader.readDescriptors(
        new File(org.torproject.metrics.stats.main.Main.descriptorsDir,
            "recent/bridge-descriptors"),
        new File(org.torproject.metrics.stats.main.Main.descriptorsDir,
            "archive/bridge-descriptors"))) {
      if (descriptor instanceof ExtraInfoDescriptor) {
        parseBridgeExtraInfoDescriptor(
            (ExtraInfoDescriptor) descriptor);
      } else if (descriptor instanceof BridgeNetworkStatus) {
        parseBridgeNetworkStatus((BridgeNetworkStatus) descriptor);
      }
    }
    database.commit();
    descriptorReader.saveHistoryFile(historyFile);
  }

  private static void parseBridgeExtraInfoDescriptor(
      ExtraInfoDescriptor descriptor) throws SQLException {
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
      SortedMap<String, Integer> bridgeIpVersions) throws SQLException {
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
        database.insertIntoImported(fingerprint, "bridge", "responses", "", "",
            "", fromMillis, toMillis, resp * intervalFraction);
        parseBridgeRespByCategory(fingerprint, fromMillis, toMillis, resp,
            dirreqStatsIntervalLengthMillis, "country", bridgeIps);
        parseBridgeRespByCategory(fingerprint, fromMillis, toMillis, resp,
            dirreqStatsIntervalLengthMillis, "transport",
            bridgeIpTransports);
        parseBridgeRespByCategory(fingerprint, fromMillis, toMillis, resp,
            dirreqStatsIntervalLengthMillis, "version", bridgeIpVersions);
      }
    }
  }

  private static void parseBridgeRespByCategory(String fingerprint,
      long fromMillis, long toMillis, double resp,
      long dirreqStatsIntervalLengthMillis, String category,
      SortedMap<String, Integer> frequencies)
      throws SQLException {
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
      switch (category) {
        case "country":
          frequenciesCopy.put("??", 4.0);
          break;
        case "transport":
          frequenciesCopy.put("<OR>", 4.0);
          break;
        case "version":
          frequenciesCopy.put("v4", 4.0);
          break;
        default:
          /* Ignore any other categories. */
      }
      total = 4.0;
    }
    for (Map.Entry<String, Double> e : frequenciesCopy.entrySet()) {
      double intervalFraction = ((double) (toMillis - fromMillis))
          / ((double) dirreqStatsIntervalLengthMillis);
      double val = resp * intervalFraction * e.getValue() / total;
      switch (category) {
        case "country":
          database.insertIntoImported(fingerprint, "bridge", "responses",
              e.getKey(), "", "", fromMillis, toMillis, val);
          break;
        case "transport":
          database.insertIntoImported(fingerprint, "bridge", "responses", "",
              e.getKey(), "", fromMillis, toMillis, val);
          break;
        case "version":
          database.insertIntoImported(fingerprint, "bridge", "responses", "",
              "", e.getKey(), fromMillis, toMillis, val);
          break;
        default:
          /* Ignore any other categories. */
      }
    }
  }

  private static void parseBridgeDirreqWriteHistory(String fingerprint,
      long publishedMillis, BandwidthHistory dirreqWriteHistory)
      throws SQLException {
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
        database.insertIntoImported(fingerprint, "bridge", "bytes", "",
            "", "", fromMillis, toMillis, writtenBytes);
      }
    }
  }

  private static void parseBridgeNetworkStatus(BridgeNetworkStatus status)
      throws SQLException {
    long publishedMillis = status.getPublishedMillis();
    long fromMillis = (publishedMillis / ONE_HOUR_MILLIS)
        * ONE_HOUR_MILLIS;
    long toMillis = fromMillis + ONE_HOUR_MILLIS;
    for (NetworkStatusEntry statusEntry
        : status.getStatusEntries().values()) {
      String fingerprint = statusEntry.getFingerprint()
          .toUpperCase();
      if (statusEntry.getFlags().contains("Running")) {
        database.insertIntoImported(fingerprint, "bridge", "status", "", "", "",
            fromMillis, toMillis, 0.0);
      }
    }
  }
}

