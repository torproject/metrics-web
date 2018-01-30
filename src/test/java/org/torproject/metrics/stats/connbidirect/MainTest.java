/* Copyright 2016--2018 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.stats.connbidirect;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

public class MainTest {

  private void assertParseHistoryCanBeSerializedAndDeserialized(
      SortedMap<String, Long> parseHistory) {
    String formattedParseHistory = Main.formatParseHistory(parseHistory);
    SortedMap<String, Long> parsedParseHistory = Main.parseParseHistory(
        formattedParseHistory);
    assertEquals("Parse histories are not equal", parseHistory,
        parsedParseHistory);
  }

  @Test
  public void testParseHistoryEmpty() {
    assertParseHistoryCanBeSerializedAndDeserialized(new TreeMap<>());
  }

  private final String pathA = "a";

  private final String pathB = "/b";

  private final long lastmodA = 1L;

  private final long lastmodB = 2L;

  @Test
  public void testParseHistoryOneEntry() {
    SortedMap<String, Long> parseHistory = new TreeMap<>();
    parseHistory.put(pathA, lastmodA);
    assertParseHistoryCanBeSerializedAndDeserialized(parseHistory);
  }

  @Test
  public void testParseHistoryTwoEntries() {
    SortedMap<String, Long> parseHistory = new TreeMap<>();
    parseHistory.put(pathA, lastmodA);
    parseHistory.put(pathB, lastmodB);
    assertParseHistoryCanBeSerializedAndDeserialized(parseHistory);
  }

  private void assertParseHistoryCannotBeDeserialized(
      String brokenParseHistory) {
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    System.setErr(new PrintStream(baos));
    SortedMap<String, Long> parsedParseHistory =
        Main.parseParseHistory(brokenParseHistory);
    assertNull("Parsed parse history is supposed to be null",
        parsedParseHistory);
  }

  @Test
  public void testParseHistoryNoLastModifiedTime() {
    assertParseHistoryCannotBeDeserialized(String.format("%s%n", pathA));
  }

  @Test
  public void testParseHistoryLastModifiedTimeNoNumber() {
    assertParseHistoryCannotBeDeserialized(String.format("%s%s%n",
        pathA, pathB));
  }

  private void assertAggregateStatsCanBeSerializedAndDeserialized(
      SortedMap<String, Short> aggregateStats) {
    String formattedAggregateStats = Main.formatAggregateStats(
        aggregateStats);
    SortedMap<String, Short> parsedParseHistory =
        Main.parseAggregateStats(formattedAggregateStats);
    assertEquals("Aggregate statistics are not equal", aggregateStats,
        parsedParseHistory);
  }

  @Test
  public void testAggregateStatsEmpty() {
    assertAggregateStatsCanBeSerializedAndDeserialized(new TreeMap<>());
  }

  @Test
  public void testAggregateStatsOneEntry() {
    SortedMap<String, Short> aggregateStats = new TreeMap<>();
    aggregateStats.put("2015-08-18,read,0.25", (short) 42);
    assertAggregateStatsCanBeSerializedAndDeserialized(aggregateStats);
  }

  @Test
  public void testAggregateStatsThreeEntries() {
    SortedMap<String, Short> aggregateStats = new TreeMap<>();
    aggregateStats.put("2015-08-18,read,0.25", (short) 12);
    aggregateStats.put("2015-08-18,read,0.5", (short) 24);
    aggregateStats.put("2015-08-18,read,0.75", (short) 42);
    assertAggregateStatsCanBeSerializedAndDeserialized(aggregateStats);
  }

  private void assertRawStatsCanBeSerializedAndDeserialized(
      SortedSet<Main.RawStat> rawStats) {
    String formattedRawStats = Main.formatRawStats(rawStats);
    SortedSet<Main.RawStat> parsedRawStats = Main.parseRawStats(
        formattedRawStats);
    assertEquals("Raw statistics are not equal", rawStats,
        parsedRawStats);
  }

  @Test
  public void testRawStatsEmpty() {
    assertRawStatsCanBeSerializedAndDeserialized(new TreeSet<>());
  }

  private static final long DATE_A = 16665; /* 2015-08-18 */

  private static final long DATE_B = 16680; /* 2015-09-02 */

  private static final String FPR_A =
      "1234567890123456789012345678901234567890";

  private static final String FPR_B =
      "2345678901234567890123456789012345678901";

  @Test
  public void testRawStatsOneEntry() {
    SortedSet<Main.RawStat> rawStats = new TreeSet<>();
    rawStats.add(new Main.RawStat(DATE_A, FPR_A, (short) 40, (short) 30,
        (short) 50));
    assertRawStatsCanBeSerializedAndDeserialized(rawStats);
  }

  private void assertRawStatsCanBeMerged(SortedSet<Main.RawStat> rawStats,
      SortedSet<Main.RawStat> newRawStats, boolean expectConflicts) {
    SortedSet<Long> conflictingDays = Main.mergeRawStats(rawStats,
        newRawStats);
    assertSame("Expected merge conflicts differ from observed conflicts",
        expectConflicts, !conflictingDays.isEmpty());
  }

  @Test
  public void testMergeRawStatsAddNothing() {
    SortedSet<Main.RawStat> rawStats = new TreeSet<>();
    rawStats.add(new Main.RawStat(DATE_A, FPR_A, (short) 40, (short) 30,
        (short) 50));
    assertRawStatsCanBeMerged(rawStats, new TreeSet<>(), false);
  }

  @Test
  public void testMergeRawStatsAddSame() {
    SortedSet<Main.RawStat> rawStats = new TreeSet<>();
    rawStats.add(new Main.RawStat(DATE_A, FPR_A, (short) 40, (short) 30,
        (short) 50));
    SortedSet<Main.RawStat> newRawStats = new TreeSet<>();
    newRawStats.add(new Main.RawStat(DATE_A, FPR_A, (short) 40,
        (short) 30, (short) 50));
    assertRawStatsCanBeMerged(rawStats, newRawStats, false);
  }

  @Test
  public void testMergeRawStatsAddOther() {
    SortedSet<Main.RawStat> rawStats = new TreeSet<>();
    rawStats.add(new Main.RawStat(DATE_A, FPR_A, (short) 40, (short) 30,
        (short) 50));
    SortedSet<Main.RawStat> newRawStats = new TreeSet<>();
    newRawStats.add(new Main.RawStat(DATE_B, FPR_B, (short) 40,
        (short) 30, (short) 50));
    assertRawStatsCanBeMerged(rawStats, newRawStats, false);
  }

  @Test
  public void testParseRawStatAllNegative() {
    Main.RawStat rawStat = Main.parseRawStatFromDescriptorContents(DATE_A,
        FPR_A, -1, -1, -1, -1);
    assertNull(rawStat);
  }

  @Test
  public void testParseRawStatOneNegative() {
    Main.RawStat rawStat = Main.parseRawStatFromDescriptorContents(DATE_A,
        FPR_A, -1, 1, 1, 1);
    assertNull(rawStat);
  }

  @Test
  public void testParseRawStatTotalZero() {
    Main.RawStat rawStat = Main.parseRawStatFromDescriptorContents(DATE_A,
        FPR_A, 0, 0, 0, 0);
    assertNull(rawStat);
  }

  @Test
  public void testParseRawStatOneOfEach() {
    Main.RawStat rawStat = Main.parseRawStatFromDescriptorContents(DATE_A,
        FPR_A, 1, 1, 1, 2);
    assertSame("Read fraction", (short) 25, rawStat.fractionRead);
    assertSame("Write fraction", (short) 25, rawStat.fractionWrite);
    assertSame("Both fraction", (short) 50, rawStat.fractionBoth);
  }

  private void assertStatsCanBeAggregated(
      SortedMap<String, Short> expectedAggregateStats,
      SortedSet<Main.RawStat> rawStats) {
    SortedMap<String, Short> updatedAggregateStats = new TreeMap<>();
    Main.updateAggregateStats(updatedAggregateStats, rawStats);
    assertEquals("Updated aggregate statistics don't match",
        expectedAggregateStats, updatedAggregateStats);
  }

  @Test
  public void testUpdateAggregateStatsEmpty() {
    assertStatsCanBeAggregated(new TreeMap<>(), new TreeSet<>());
  }

  @Test
  public void testUpdateAggregateStatsSingleRawStat() {
    SortedMap<String, Short> expectedAggregateStats = new TreeMap<>();
    expectedAggregateStats.put("2015-08-18,read,0.25", (short) 42);
    expectedAggregateStats.put("2015-08-18,read,0.5", (short) 42);
    expectedAggregateStats.put("2015-08-18,read,0.75", (short) 42);
    expectedAggregateStats.put("2015-08-18,write,0.25", (short) 32);
    expectedAggregateStats.put("2015-08-18,write,0.5", (short) 32);
    expectedAggregateStats.put("2015-08-18,write,0.75", (short) 32);
    expectedAggregateStats.put("2015-08-18,both,0.25", (short) 22);
    expectedAggregateStats.put("2015-08-18,both,0.5", (short) 22);
    expectedAggregateStats.put("2015-08-18,both,0.75", (short) 22);
    SortedSet<Main.RawStat> rawStats = new TreeSet<>();
    rawStats.add(new Main.RawStat(DATE_A, FPR_A, (short) 42, (short) 32,
        (short) 22));
    assertStatsCanBeAggregated(expectedAggregateStats, rawStats);
  }

  @Test
  public void testUpdateAggregateStatsTwoRawStat() {
    SortedMap<String, Short> expectedAggregateStats = new TreeMap<>();
    expectedAggregateStats.put("2015-08-18,read,0.25", (short) 32);
    expectedAggregateStats.put("2015-08-18,read,0.5", (short) 42);
    expectedAggregateStats.put("2015-08-18,read,0.75", (short) 42);
    expectedAggregateStats.put("2015-08-18,write,0.25", (short) 22);
    expectedAggregateStats.put("2015-08-18,write,0.5", (short) 32);
    expectedAggregateStats.put("2015-08-18,write,0.75", (short) 32);
    expectedAggregateStats.put("2015-08-18,both,0.25", (short) 12);
    expectedAggregateStats.put("2015-08-18,both,0.5", (short) 22);
    expectedAggregateStats.put("2015-08-18,both,0.75", (short) 22);
    SortedSet<Main.RawStat> rawStats = new TreeSet<>();
    rawStats.add(new Main.RawStat(DATE_A, FPR_A, (short) 32, (short) 22,
        (short) 12));
    rawStats.add(new Main.RawStat(DATE_A, FPR_B, (short) 42, (short) 32,
        (short) 22));
    assertStatsCanBeAggregated(expectedAggregateStats, rawStats);
  }
}
