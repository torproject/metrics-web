/* Copyright 2015--2018 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.stats.connbidirect;

import org.torproject.descriptor.Descriptor;
import org.torproject.descriptor.DescriptorReader;
import org.torproject.descriptor.DescriptorSourceFactory;
import org.torproject.descriptor.ExtraInfoDescriptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.TreeSet;

public class Main {

  private static Logger log = LoggerFactory.getLogger(Main.class);

  static class RawStat implements Comparable<RawStat> {

    /* Date when the statistics interval ended in days since the epoch. */
    long dateDays;

    /* Relay fingerprint, or <code>null</code> if this entry only
     * indicates that outdated raw statistics have been discarded and
     * hence new raw statistics for this date should not be aggregated. */
    String fingerprint;

    /* Fraction of mostly reading connections as a value between 0 and
     * 100. */
    short fractionRead;

    /* Fraction of mostly writing connections as a value between 0 and
     * 100. */
    short fractionWrite;

    /* Fraction of both reading and writing connections as a value between
     * 0 and 100. */
    short fractionBoth;

    RawStat(long dateDays, String fingerprint, short fractionRead,
        short fractionWrite, short fractionBoth) {
      this.dateDays = dateDays;
      this.fingerprint = fingerprint;
      this.fractionRead = fractionRead;
      this.fractionWrite = fractionWrite;
      this.fractionBoth = fractionBoth;
    }

    static RawStat fromString(String string) {
      try {
        String[] parts = string.split(",");
        if (parts.length == 5) {
          long dateDays = Long.parseLong(parts[0]);
          String fingerprint = parts[1];
          short fractionRead = Short.parseShort(parts[2]);
          short fractionWrite = Short.parseShort(parts[3]);
          short fractionBoth = Short.parseShort(parts[4]);
          return new RawStat(dateDays, fingerprint, fractionRead,
              fractionWrite, fractionBoth);
        } else {
          log.warn("Could not deserialize raw statistic from string '{}'.",
              string);
          return null;
        }
      } catch (NumberFormatException e) {
        log.warn("Could not deserialize raw statistic from string '{}'.",
            string, e);
        return null;
      }
    }

    @Override
    public String toString() {
      if (this.fingerprint == null) {
        return String.valueOf(this.dateDays);
      } else {
        return String.format("%d,%s,%d,%d,%d", this.dateDays,
            this.fingerprint, this.fractionRead, this.fractionWrite,
            this.fractionBoth);
      }
    }

    @Override
    public int compareTo(RawStat other) {
      if (this.dateDays != other.dateDays) {
        return this.dateDays < other.dateDays ? -1 : 1;
      } else if (this.fingerprint != null && other.fingerprint != null) {
        return this.fingerprint.compareTo(other.fingerprint);
      } else if (this.fingerprint != null) {
        return -1;
      } else if (other.fingerprint != null) {
        return 1;
      } else {
        return 0;
      }
    }

    @Override
    public boolean equals(Object otherObject) {
      if (!(otherObject instanceof RawStat)) {
        return false;
      }
      RawStat other = (RawStat) otherObject;
      return this.dateDays == other.dateDays
          && this.fingerprint.equals(other.fingerprint);
    }
  }

  static final long ONE_DAY_IN_MILLIS = 86400000L;

  /** Executes this data-processing module. */
  public static void main(String[] args) throws IOException {
    File parseHistoryFile = new File("stats/parse-history");
    File aggregateStatsFile = new File("stats/connbidirect2.csv");
    File[] descriptorsDirectories = new File[] {
        new File("../../shared/in/archive/relay-descriptors/extra-infos"),
        new File("../../shared/in/recent/relay-descriptors/extra-infos")};
    SortedMap<String, Long> parseHistory = parseParseHistory(
        readStringFromFile(parseHistoryFile));
    if (parseHistory == null) {
      log.warn("Could not parse {}. Proceeding without parse history.",
          parseHistoryFile.getAbsolutePath());
    }
    SortedMap<String, Short> aggregateStats = parseAggregateStats(
        readStringFromFile(aggregateStatsFile));
    if (aggregateStats == null) {
      log.warn("Could not parse previously aggregated "
          + "statistics.  Not proceeding, because we would otherwise "
          + "lose previously aggregated values for which we don't have "
          + "raw statistics anymore.");
      return;
    }
    SortedSet<RawStat> newRawStats = new TreeSet<>();
    parseHistory = addRawStatsFromDescriptors(newRawStats,
        descriptorsDirectories, parseHistory);
    if (parseHistory == null) {
      log.warn("Could not parse raw statistics from "
          + "descriptors.  Not proceeding, because we would otherwise "
          + "leave out those descriptors in future runs.");
      return;
    }
    File rawStatsFile = new File("stats/raw-stats");
    SortedSet<RawStat> rawStats = parseRawStats(
        readStringFromFile(rawStatsFile));
    if (rawStats == null) {
      log.warn("Could not parse previously parsed raw "
          + "statistics.  Not proceeding, because we might otherwise "
          + "leave out previously parsed statistics in the aggregates.");
      return;
    }
    SortedSet<Long> conflictingDates = mergeRawStats(rawStats,
        newRawStats);
    if (!conflictingDates.isEmpty()) {
      StringBuilder sb = new StringBuilder(
          "Could not update aggregate statistics, because "
          + "we already aggregated statistics for at least one contained "
          + "date and discarded the underlying raw statistics.  Not "
          + "proceeding.  To fix this, you'll have to re-import "
          + "statistics for the following dates:");
      DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
      dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
      for (long conflictingDate : conflictingDates) {
        sb.append("\n ")
            .append(dateFormat.format(conflictingDate * ONE_DAY_IN_MILLIS));
      }
      log.warn(sb.toString());
      return;
    }
    updateAggregateStats(aggregateStats, rawStats);
    writeStringToFile(aggregateStatsFile, formatAggregateStats(
        aggregateStats));
    writeStringToFile(rawStatsFile, formatRawStats(rawStats));
    writeStringToFile(parseHistoryFile, formatParseHistory(parseHistory));
  }

  /* Read the string contained in <code>file</code> and return it. */
  private static String readStringFromFile(File file) throws IOException {
    StringBuilder sb = new StringBuilder();
    if (file.exists()) {
      BufferedReader br = new BufferedReader(new FileReader(file));
      String line;
      while ((line = br.readLine()) != null) {
        sb.append(line).append("\n");
      }
      br.close();
    }
    return sb.toString();
  }

  /* Write <code>string</code> to <code>file</code> by first creating its
   * parent directory if that doesn't exist yet, then writing to a
   * temporary file, and finally renaming that temporary file to the file
   * to be written. */
  private static void writeStringToFile(File file, String string)
      throws IOException {
    file.getParentFile().mkdirs();
    File tempFile = new File(file.getParentFile(), file.getName()
        + ".tmp");
    BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile));
    bw.write(string);
    bw.close();
    tempFile.renameTo(file);
  }

  /* Format a parse history containing paths and last-modified times. */
  static String formatParseHistory(SortedMap<String, Long> parseHistory) {
    StringBuilder sb = new StringBuilder();
    for (Map.Entry<String, Long> e : parseHistory.entrySet()) {
      sb.append(e.getKey()).append(",").append(e.getValue()).append("\n");
    }
    return sb.toString();
  }

  /* Parse a parse history. */
  static SortedMap<String, Long> parseParseHistory(
      String formattedParseHistory) {
    SortedMap<String, Long> parsedParseHistory = new TreeMap<>();
    LineNumberReader lnr = new LineNumberReader(new StringReader(
        formattedParseHistory));
    String line = "";
    try {
      while ((line = lnr.readLine()) != null) {
        String[] parts = line.split(",");
        if (parts.length < 2) {
          log.warn("Invalid line {} in parse history: '{}'.",
              lnr.getLineNumber(), line);
          return null;
        }
        parsedParseHistory.put(parts[0], Long.parseLong(parts[1]));
      }
    } catch (IOException e) {
      log.warn("Unexpected I/O exception while reading line {} from parse "
          + "history.", lnr.getLineNumber(), e);
      return null;
    } catch (NumberFormatException e) {
      log.warn("Invalid line {} in parse history: '{}'.", lnr.getLineNumber(),
          line, e);
      return null;
    }
    return parsedParseHistory;
  }

  private static final String AGGREGATE_STATS_HEADER =
      "date,direction,quantile,fraction";

  /* Format aggregate connbidirect stats containing a combined identifier
   * consisting of date (e.g., 2015-08-18), direction (both, read, write),
   * and quantile (0.25, 0.5, 0.75) and a fraction value (between 0.0 and
   * 1.0). */
  static String formatAggregateStats(
      SortedMap<String, Short> aggregateStats) {
    StringBuilder sb = new StringBuilder();
    sb.append(AGGREGATE_STATS_HEADER + "\n");
    for (Map.Entry<String, Short> e : aggregateStats.entrySet()) {
      sb.append(e.getKey()).append(",").append(e.getValue()).append("\n");
    }
    return sb.toString();
  }

  /* Parse aggregate connbidirect stats. */
  static SortedMap<String, Short> parseAggregateStats(
      String formattedAggregatedStats) {
    SortedMap<String, Short> parsedAggregateStats = new TreeMap<>();
    if (formattedAggregatedStats.length() < 1) {
      return parsedAggregateStats; /* Empty file. */
    }
    LineNumberReader lnr = new LineNumberReader(new StringReader(
        formattedAggregatedStats));
    String line = "";
    try {
      if (!AGGREGATE_STATS_HEADER.equals(lnr.readLine())) {
        log.warn("First line of aggregate statistics does not "
            + "contain the header line. Is this the correct file?");
        return null;
      }
      while ((line = lnr.readLine()) != null) {
        String[] parts = line.split(",");
        if (parts.length != 4) {
          log.warn("Invalid line {} in aggregate statistics: '{}'.",
              lnr.getLineNumber(), line);
          return null;
        }
        parsedAggregateStats.put(parts[0] + "," + parts[1] + ","
            + parts[2], Short.parseShort(parts[3]));
      }
    } catch (IOException e) {
      log.warn("Unexpected I/O exception while reading line {} from aggregate "
          + "statistics.", lnr.getLineNumber(), e);
      return null;
    } catch (NumberFormatException e) {
      log.warn("Invalid line {} in aggregate statistics: '{}'.",
          lnr.getLineNumber(), line, e);
      return null;
    }
    return parsedAggregateStats;
  }

  /* Format raw statistics separated by newlines using the formatter in
   * RawStats. */
  static String formatRawStats(SortedSet<RawStat> rawStats) {
    StringBuilder sb = new StringBuilder();
    for (RawStat rawStat : rawStats) {
      sb.append(rawStat.toString()).append("\n");
    }
    return sb.toString();
  }

  /* Parse raw statistics. */
  static SortedSet<RawStat> parseRawStats(String formattedRawStats) {
    SortedSet<RawStat> parsedRawStats = new TreeSet<>();
    LineNumberReader lnr = new LineNumberReader(new StringReader(
        formattedRawStats));
    String line = "";
    try {
      while ((line = lnr.readLine()) != null) {
        RawStat rawStat = RawStat.fromString(line);
        if (rawStat == null) {
          log.warn("Invalid line {} in raw statistics: '{}'.",
              lnr.getLineNumber(), line);
          return null;
        }
        parsedRawStats.add(rawStat);
      }
    } catch (IOException e) {
      log.warn("Unexpected I/O exception while reading line {} from raw "
          + "statistics.", lnr.getLineNumber(), e);
      return null;
    } catch (NumberFormatException e) {
      log.warn("Invalid line {} in raw statistics: '{}'.", lnr.getLineNumber(),
          line, e);
      return null;
    }
    return parsedRawStats;
  }

  private static SortedMap<String, Long> addRawStatsFromDescriptors(
      SortedSet<RawStat> rawStats, File[] descriptorsDirectories,
      SortedMap<String, Long> parseHistory) {
    DescriptorReader descriptorReader =
        DescriptorSourceFactory.createDescriptorReader();
    descriptorReader.setExcludedFiles(parseHistory);
    for (Descriptor descriptor : descriptorReader.readDescriptors(
        descriptorsDirectories)) {
      if (!(descriptor instanceof ExtraInfoDescriptor)) {
        continue;
      }
      RawStat rawStat = parseRawStatFromDescriptor(
          (ExtraInfoDescriptor) descriptor);
      if (rawStat != null) {
        rawStats.add(rawStat);
      }
    }
    parseHistory.clear();
    parseHistory.putAll(descriptorReader.getExcludedFiles());
    parseHistory.putAll(descriptorReader.getParsedFiles());
    return parseHistory;
  }

  private static RawStat parseRawStatFromDescriptor(
      ExtraInfoDescriptor extraInfo) {
    if (extraInfo.getConnBiDirectStatsEndMillis() <= 0L) {
      return null;
    }
    int below = extraInfo.getConnBiDirectBelow();
    int read = extraInfo.getConnBiDirectRead();
    int write = extraInfo.getConnBiDirectWrite();
    int both = extraInfo.getConnBiDirectBoth();
    if (below < 0 || read < 0 || write < 0 || both < 0) {
      log.debug("Could not parse incomplete conn-bi-direct statistics. "
          + "Skipping descriptor.");
      return null;
    }
    long statsEndMillis = extraInfo.getConnBiDirectStatsEndMillis();
    String fingerprint = extraInfo.getFingerprint();
    return parseRawStatFromDescriptorContents(statsEndMillis, fingerprint,
        below, read, write, both);
  }

  static RawStat parseRawStatFromDescriptorContents(long statsEndMillis,
      String fingerprint, int below, int read, int write, int both) {
    int total = read + write + both;
    if (below < 0 || read < 0 || write < 0 || both < 0 || total <= 0) {
      return null;
    }
    long dateDays = statsEndMillis / ONE_DAY_IN_MILLIS;
    short fractionRead = (short) ((read * 100) / total);
    short fractionWrite = (short) ((write * 100) / total);
    short fractionBoth = (short) ((both * 100) / total);
    return new RawStat(dateDays, fingerprint, fractionRead, fractionWrite,
        fractionBoth);
  }

  static SortedSet<Long> mergeRawStats(
      SortedSet<RawStat> rawStats, SortedSet<RawStat> newRawStats) {
    rawStats.addAll(newRawStats);
    SortedSet<Long> discardedRawStats = new TreeSet<>();
    SortedSet<Long> availableRawStats = new TreeSet<>();
    for (RawStat rawStat : rawStats) {
      if (rawStat.fingerprint != null) {
        availableRawStats.add(rawStat.dateDays);
      } else {
        discardedRawStats.add(rawStat.dateDays);
      }
    }
    discardedRawStats.retainAll(availableRawStats);
    return discardedRawStats;
  }

  static void updateAggregateStats(
      SortedMap<String, Short> aggregateStats,
      SortedSet<RawStat> rawStats) {
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    String yesterday = dateFormat.format(System.currentTimeMillis()
        - ONE_DAY_IN_MILLIS);
    SortedMap<String, List<Short>> fractionsByDateAndDirection
        = new TreeMap<>();
    final String[] directions = new String[] { "read", "write", "both" };
    for (RawStat rawStat : rawStats) {
      if (rawStat.fingerprint != null) {
        String date = dateFormat.format(rawStat.dateDays
            * ONE_DAY_IN_MILLIS);
        if (date.compareTo(yesterday) >= 0) {
          continue;
        }
        short[] fractions = new short[] { rawStat.fractionRead,
            rawStat.fractionWrite, rawStat.fractionBoth };
        for (int i = 0; i < directions.length; i++) {
          String dateAndDirection = date + "," + directions[i];
          if (!fractionsByDateAndDirection.containsKey(
              dateAndDirection)) {
            fractionsByDateAndDirection.put(dateAndDirection,
                new ArrayList<>());
          }
          fractionsByDateAndDirection.get(dateAndDirection).add(
              fractions[i]);
        }
      }
    }
    final String[] quantiles = new String[] { "0.25", "0.5", "0.75" };
    final int[] centiles = new int[] { 25, 50, 75 };
    for (Map.Entry<String, List<Short>> e
        : fractionsByDateAndDirection.entrySet()) {
      String dateAndDirection = e.getKey();
      List<Short> fractions = e.getValue();
      Collections.sort(fractions);
      for (int i = 0; i < quantiles.length; i++) {
        String dateDirectionAndQuantile = dateAndDirection + ","
            + quantiles[i];
        short fraction = fractions.get((centiles[i] * fractions.size())
            / 100);
        aggregateStats.put(dateDirectionAndQuantile, fraction);
      }
    }
  }
}
