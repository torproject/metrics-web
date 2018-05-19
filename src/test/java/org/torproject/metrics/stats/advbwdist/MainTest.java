/* Copyright 2018 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.stats.advbwdist;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

public class MainTest {

  @Test
  public void testComputePercentilesZeroValues() {
    List<Long> valueList = new ArrayList<>();
    SortedMap<Integer, Long> computedPercentiles = Main.computePercentiles(
        valueList, 0, 25, 50, 75, 100);
    assertEquals(0L, (long) computedPercentiles.get(0));
    assertEquals(0L, (long) computedPercentiles.get(25));
    assertEquals(0L, (long) computedPercentiles.get(50));
    assertEquals(0L, (long) computedPercentiles.get(75));
    assertEquals(0L, (long) computedPercentiles.get(100));
  }

  @Test
  public void testComputePercentilesTenValues() {
    List<Long> valueList = new ArrayList<>();
    valueList.add(3L);
    valueList.add(6L);
    valueList.add(7L);
    valueList.add(8L);
    valueList.add(8L);
    valueList.add(10L);
    valueList.add(13L);
    valueList.add(15L);
    valueList.add(16L);
    valueList.add(20L);
    SortedMap<Integer, Long> computedPercentiles = Main.computePercentiles(
        valueList, 0, 25, 50, 75, 100);
    assertEquals(3L, (long) computedPercentiles.get(0));
    assertEquals(7L, (long) computedPercentiles.get(25));
    assertEquals(9L, (long) computedPercentiles.get(50));
    assertEquals(14L, (long) computedPercentiles.get(75));
    assertEquals(20L, (long) computedPercentiles.get(100));
  }

  @Test
  public void testComputePercentilesElevenValues() {
    List<Long> valueList = new ArrayList<>();
    valueList.add(3L);
    valueList.add(6L);
    valueList.add(7L);
    valueList.add(8L);
    valueList.add(8L);
    valueList.add(9L);
    valueList.add(10L);
    valueList.add(13L);
    valueList.add(15L);
    valueList.add(16L);
    valueList.add(20L);
    SortedMap<Integer, Long> computedPercentiles = Main.computePercentiles(
        valueList, 0, 25, 50, 75, 100);
    assertEquals(3L, (long) computedPercentiles.get(0));
    assertEquals(7L, (long) computedPercentiles.get(25));
    assertEquals(9L, (long) computedPercentiles.get(50));
    assertEquals(14L, (long) computedPercentiles.get(75));
    assertEquals(20L, (long) computedPercentiles.get(100));
  }
}
