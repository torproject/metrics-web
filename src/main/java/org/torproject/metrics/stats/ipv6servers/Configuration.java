/* Copyright 2017--2018 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.stats.ipv6servers;

/** Configuration options parsed from Java properties with reasonable hard-coded
 * defaults. */
class Configuration {
  static String descriptors = System.getProperty("ipv6servers.descriptors",
      "../../shared/in/");
  static String database = System.getProperty("ipv6servers.database",
      "jdbc:postgresql:ipv6servers");
  static String history = System.getProperty("ipv6servers.history",
      "status/read-descriptors");
  static String output = System.getProperty("ipv6servers.output",
      "stats/ipv6servers.csv");
}

