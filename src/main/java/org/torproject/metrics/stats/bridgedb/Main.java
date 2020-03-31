/* Copyright 2019--2020 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.stats.bridgedb;

import org.torproject.descriptor.BridgedbMetrics;
import org.torproject.descriptor.Descriptor;
import org.torproject.descriptor.DescriptorSourceFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class Main {

  private static final Logger logger = LoggerFactory.getLogger(Main.class);

  private static final Path bridgedbStatsCsvFile
      = org.torproject.metrics.stats.main.Main.modulesDir.toPath()
      .resolve("bridgedb/stats/bridgedb-stats.csv");

  private static File[] descriptorsDirectories = new File[] {
      new File(org.torproject.metrics.stats.main.Main.descriptorsDir,
          "archive/bridgedb-metrics"),
      new File(org.torproject.metrics.stats.main.Main.descriptorsDir,
          "recent/bridgedb-metrics") };

  /** Executes this data-processing module. */
  public static void main(String[] args) throws IOException {
    SortedMap<String, Long> previousStatistics
        = readBridgedbStatsFile(bridgedbStatsCsvFile);
    SortedMap<String, Long> currentStatistics = parseStatistics(
        previousStatistics, descriptorsDirectories);
    writeBridgedbStatsFile(bridgedbStatsCsvFile, currentStatistics);
  }

  static SortedMap<String, Long>
      readBridgedbStatsFile(Path bridgedbStatsCsvFile) throws IOException {
    SortedMap<String, Long> readStatistics = new TreeMap<>();
    if (Files.exists(bridgedbStatsCsvFile)) {
      for (String line : Files.readAllLines(bridgedbStatsCsvFile)) {
        if (line.startsWith("date")) {
          continue;
        }
        String[] lineParts = line.split(",");
        if (lineParts.length != 4) {
          logger.warn("Skipping unrecognized line '{}' in {}.", line,
              bridgedbStatsCsvFile.toAbsolutePath());
          continue;
        }
        String key = String.format("%s,%s,%s", lineParts[0], lineParts[1],
            lineParts[2]);
        long value = Long.parseLong(lineParts[3]);
        readStatistics.put(key, value);
      }
      logger.debug("Read {} containing {} non-header lines.",
          bridgedbStatsCsvFile, readStatistics.size());
    }
    return readStatistics;
  }

  static SortedMap<String, Long> parseStatistics(
      SortedMap<String, Long> previousStatistics,
      File[] descriptorsDirectories) {
    SortedMap<String, Long> currentStatistics
        = new TreeMap<>(previousStatistics);
    for (Descriptor descriptor : DescriptorSourceFactory
        .createDescriptorReader().readDescriptors(descriptorsDirectories)) {
      if (!(descriptor instanceof BridgedbMetrics)) {
        continue;
      }
      BridgedbMetrics bridgedbMetrics = (BridgedbMetrics) descriptor;
      if (!"1".equals(bridgedbMetrics.bridgedbMetricsVersion())) {
        logger.warn("Unable to process BridgeDB metrics version {} != 1.",
            bridgedbMetrics.bridgedbMetricsVersion());
        continue;
      }
      if (!bridgedbMetrics.bridgedbMetricCounts().isPresent()) {
        continue;
      }
      String bridgedbMetricsEndDate = bridgedbMetrics.bridgedbMetricsEnd()
          .toLocalDate().toString();
      SortedMap<String, Long> parsedStatistics = new TreeMap<>();
      for (Map.Entry<String, Long> bridgedbMetricCount
          : bridgedbMetrics.bridgedbMetricCounts().get().entrySet()) {
        String[] keyParts = bridgedbMetricCount.getKey().split("\\.");
        if (keyParts.length < 3) {
          /* Unable to extract relevant key parts. */
          continue;
        }
        if (bridgedbMetricCount.getValue() < 10) {
          logger.warn("Skipping too small BridgeDB metric count {} < 10 in {}.",
              bridgedbMetricCount.getValue(),
              descriptor.getDescriptorFile().getAbsolutePath());
          continue;
        }
        String distributor = keyParts[0];
        String transport = keyParts[1];
        String ccOrEmail = keyParts[2];
        if (ccOrEmail.equals("zz")) {
          /* Skip requests coming in over Tor exits. */
          continue;
        }
        String key = String.format("%s,%s,%s", bridgedbMetricsEndDate,
            distributor, transport);
        long countsSoFar = parsedStatistics.getOrDefault(key, 0L);
        countsSoFar += bridgedbMetricCount.getValue() - 5L;
        parsedStatistics.put(key, countsSoFar);
      }
      if (!Collections.disjoint(currentStatistics.keySet(),
          parsedStatistics.keySet())) {
        /* Statististics for this date (and any combination of distributor and
         * transport) are already contained. */
        continue;
      }
      currentStatistics.putAll(parsedStatistics);
    }
    return currentStatistics;
  }

  static void writeBridgedbStatsFile(Path bridgedbStatsCsvFile,
      SortedMap<String, Long> currentStatistics) throws IOException {
    if (!Files.exists(bridgedbStatsCsvFile.getParent())) {
      Files.createDirectories(bridgedbStatsCsvFile.getParent());
    }
    List<String> lines = new ArrayList<>();
    lines.add("date,distributor,transport,requests");
    for (Map.Entry<String, Long> statistic : currentStatistics.entrySet()) {
      lines.add(String.format("%s,%d", statistic.getKey(),
          statistic.getValue()));
    }
    Files.write(bridgedbStatsCsvFile, lines, StandardOpenOption.CREATE);
    logger.debug("Wrote {} containing {} non-header lines.",
        bridgedbStatsCsvFile, lines.size() - 1);
  }
}

