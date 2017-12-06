/* Copyright 2017 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.stats.ipv6servers;

/** Configuration options parsed from Java properties with reasonable hard-coded
 * defaults. */
class Configuration {
  static String descriptors = System.getProperty("descriptors",
      "../../shared/in/");
  static String database = System.getProperty("database",
      "jdbc:postgresql:ipv6servers");
  static String history = System.getProperty("history",
      "status/read-descriptors");
  static String output = System.getProperty("output",
      "stats/ipv6servers.csv");
}

