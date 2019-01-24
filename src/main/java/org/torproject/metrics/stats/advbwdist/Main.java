/* Copyright 2016--2018 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.stats.advbwdist;

import org.torproject.descriptor.Descriptor;
import org.torproject.descriptor.DescriptorReader;
import org.torproject.descriptor.DescriptorSourceFactory;
import org.torproject.descriptor.NetworkStatusEntry;
import org.torproject.descriptor.RelayNetworkStatusConsensus;
import org.torproject.descriptor.ServerDescriptor;

import org.apache.commons.math3.stat.descriptive.rank.Median;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.TreeMap;

public class Main {

  private static final File baseDir = new File(
      org.torproject.metrics.stats.main.Main.modulesDir, "advbwdist");

  /** Executes this data-processing module. */
  public static void main(String[] args) throws IOException {

    /* Parse server descriptors, not keeping a parse history, and memorize
     * the advertised bandwidth for every server descriptor. */
    Map<String, Long> serverDescriptors = new HashMap<>();
    DescriptorReader descriptorReader =
        DescriptorSourceFactory.createDescriptorReader();
    for (Descriptor descriptor : descriptorReader.readDescriptors(new File(
        org.torproject.metrics.stats.main.Main.descriptorsDir,
        "recent/relay-descriptors/server-descriptors"))) {
      if (!(descriptor instanceof ServerDescriptor)) {
        continue;
      }
      ServerDescriptor serverDescriptor = (ServerDescriptor) descriptor;
      String digest = serverDescriptor.getDigestSha1Hex();
      long advertisedBandwidth = Math.min(Math.min(
          serverDescriptor.getBandwidthRate(),
          serverDescriptor.getBandwidthBurst()),
          serverDescriptor.getBandwidthObserved());
      serverDescriptors.put(digest.toUpperCase(), advertisedBandwidth);
    }

    /* Parse consensuses, keeping a parse history. */
    descriptorReader = DescriptorSourceFactory.createDescriptorReader();
    File historyFile = new File(baseDir, "status/parsed-consensuses");
    descriptorReader.setHistoryFile(historyFile);
    File resultsFile = new File(baseDir, "stats/advbwdist-validafter.csv");
    resultsFile.getParentFile().mkdirs();
    boolean writeHeader = !resultsFile.exists();
    BufferedWriter bw = new BufferedWriter(new FileWriter(resultsFile,
        true));
    if (writeHeader) {
      bw.write("valid_after,isexit,relay,percentile,advbw\n");
    }
    SimpleDateFormat dateTimeFormat = new SimpleDateFormat(
        "yyyy-MM-dd HH:mm:ss");
    dateTimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    for (Descriptor descriptor : descriptorReader.readDescriptors(new File(
        org.torproject.metrics.stats.main.Main.descriptorsDir,
        "recent/relay-descriptors/consensuses"))) {
      if (!(descriptor instanceof RelayNetworkStatusConsensus)) {
        continue;
      }

      /* Parse server descriptor digests from consensus and look up
       * advertised bandwidths. */
      RelayNetworkStatusConsensus consensus =
          (RelayNetworkStatusConsensus) descriptor;
      String validAfter = dateTimeFormat.format(
          consensus.getValidAfterMillis());
      List<Long> advertisedBandwidthsAllRelays = new ArrayList<>();
      List<Long> advertisedBandwidthsExitsOnly = new ArrayList<>();
      for (NetworkStatusEntry relay
          : consensus.getStatusEntries().values()) {
        if (!relay.getFlags().contains("Running")) {
          continue;
        }
        String serverDescriptorDigest = relay.getDescriptor()
            .toUpperCase();
        if (!serverDescriptors.containsKey(serverDescriptorDigest)) {
          continue;
        }
        long advertisedBandwidth = serverDescriptors.get(
            serverDescriptorDigest);
        advertisedBandwidthsAllRelays.add(advertisedBandwidth);
        if (relay.getFlags().contains("Exit")
            && !relay.getFlags().contains("BadExit")) {
          advertisedBandwidthsExitsOnly.add(advertisedBandwidth);
        }
      }

      /* Write advertised bandwidths of n-th fastest relays/exits. */
      advertisedBandwidthsAllRelays.sort(Collections.reverseOrder());
      advertisedBandwidthsExitsOnly.sort(Collections.reverseOrder());
      int[] fastestRelays = new int[] { 1, 2, 3, 5, 10, 20, 30, 50, 100,
          200, 300, 500, 1000, 2000, 3000, 5000 };
      for (int fastestRelay : fastestRelays) {
        if (advertisedBandwidthsAllRelays.size() >= fastestRelay) {
          bw.write(String.format("%s,,%d,,%d%n", validAfter,
              fastestRelay,
              advertisedBandwidthsAllRelays.get(fastestRelay - 1)));
        }
      }
      for (int fastestRelay : fastestRelays) {
        if (advertisedBandwidthsExitsOnly.size() >= fastestRelay) {
          bw.write(String.format("%s,TRUE,%d,,%d%n", validAfter,
              fastestRelay,
              advertisedBandwidthsExitsOnly.get(fastestRelay - 1)));
        }
      }

      /* Write advertised bandwidth percentiles of relays/exits. */
      int[] percentiles = new int[] { 0, 1, 2, 3, 5, 9, 10, 20, 25, 30,
          40, 50, 60, 70, 75, 80, 90, 91, 95, 97, 98, 99, 100 };
      if (!advertisedBandwidthsAllRelays.isEmpty()) {
        for (Map.Entry<Integer, Long> e : computePercentiles(
            advertisedBandwidthsAllRelays, percentiles).entrySet()) {
          bw.write(String.format("%s,,,%d,%d%n", validAfter,
              e.getKey(), e.getValue()));
        }
      }
      if (!advertisedBandwidthsExitsOnly.isEmpty()) {
        for (Map.Entry<Integer, Long> e : computePercentiles(
            advertisedBandwidthsExitsOnly, percentiles).entrySet()) {
          bw.write(String.format("%s,TRUE,,%d,%d%n", validAfter,
              e.getKey(), e.getValue()));
        }
      }
    }
    descriptorReader.saveHistoryFile(historyFile);
    bw.close();

    /* Aggregate statistics. */
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    String today = dateFormat.format(new Date());
    SortedMap<String, List<Long>> preAggregatedValues = new TreeMap<>();
    try (BufferedReader br = new BufferedReader(new FileReader(resultsFile))) {
      br.readLine(); /* Skip header. */
      String line;
      while (null != (line = br.readLine())) {
        String[] parts = line.split(",");
        String date = parts[0].substring(0, 10);
        if (date.compareTo(today) >= 0) {
          continue;
        }
        String isExit = parts[1].equals("TRUE") ? "t" : "";
        String keyWithoutTime = String.format("%s,%s,%s,%s",
            date, isExit, parts[2], parts[3]);
        long value = Long.parseLong(parts[4]);
        preAggregatedValues.putIfAbsent(keyWithoutTime, new ArrayList<>());
        preAggregatedValues.get(keyWithoutTime).add(value);
      }
    }
    File aggregateResultsFile = new File(baseDir, "stats/advbwdist.csv");
    aggregateResultsFile.getParentFile().mkdirs();
    try (BufferedWriter bw2 = new BufferedWriter(
        new FileWriter(aggregateResultsFile))) {
      bw2.write("date,isexit,relay,percentile,advbw\n");
      for (Map.Entry<String, List<Long>> e : preAggregatedValues.entrySet()) {
        bw2.write(String.format("%s,%.0f%n", e.getKey(),
            computeMedian(e.getValue())));
      }
    }
  }

  /** Compute percentiles (between 0 and 100) for the given list of values, and
   * return a map with percentiles as keys and computed values as values. If the
   * list of values is empty, the returned map contains all zeros. */
  static SortedMap<Integer, Long> computePercentiles(
      List<Long> valueList, int ... percentiles) {
    SortedMap<Integer, Long> computedPercentiles = new TreeMap<>();
    double[] valueArray = new double[valueList.size()];
    long minValue = Long.MAX_VALUE;
    for (int i = 0; i < valueList.size(); i++) {
      valueArray[i] = valueList.get(i).doubleValue();
      minValue = Math.min(minValue, valueList.get(i));
    }
    if (valueList.isEmpty()) {
      minValue = 0L;
    }
    Percentile percentile = new Percentile()
        .withEstimationType(Percentile.EstimationType.R_7);
    percentile.setData(valueArray);
    for (int p : percentiles) {
      if (0 == p) {
        computedPercentiles.put(p, minValue);
      } else {
        computedPercentiles.put(p,
            (long) Math.floor(percentile.evaluate((double) p)));
      }
    }
    return computedPercentiles;
  }

  /** Return the median for the given list of values, or <code>Double.NaN</code>
   * if the given list is empty. */
  static double computeMedian(List<Long> valueList) {
    Median median = new Median()
        .withEstimationType(Percentile.EstimationType.R_7);
    median.setData(valueList.stream().mapToDouble(Long::doubleValue).toArray());
    return Math.floor(median.evaluate());
  }
}

