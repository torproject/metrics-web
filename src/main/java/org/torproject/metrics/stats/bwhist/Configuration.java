/* Copyright 2011--2018 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.stats.bwhist;

/** Configuration options parsed from Java properties with reasonable hard-coded
 * defaults. */
public class Configuration {
  static String descriptors = System.getProperty("bwhist.descriptors",
      "../../shared/in/");
  static String database = System.getProperty("bwhist.database",
      "jdbc:postgresql:tordir");
  static String history = System.getProperty("bwhist.history",
      "status/read-descriptors");
  static String output = System.getProperty("bwhist.output",
      "stats/");
}

