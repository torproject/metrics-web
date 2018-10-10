/* Copyright 2018 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.stats.totalcw;

/** Configuration options parsed from Java properties with reasonable hard-coded
 * defaults. */
class Configuration {
  static String descriptors = System.getProperty("totalcw.descriptors",
      "../../shared/in/");
  static String database = System.getProperty("totalcw.database",
      "jdbc:postgresql:totalcw");
  static String history = System.getProperty("totalcw.history",
      "status/read-descriptors");
  static String output = System.getProperty("totalcw.output",
      "stats/totalcw.csv");
}

