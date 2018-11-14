/* Copyright 2017--2018 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.stats.servers;

/** Configuration options parsed from Java properties with reasonable hard-coded
 * defaults. */
class Configuration {
  static String descriptors = System.getProperty("servers.descriptors",
      "../../shared/in/");
  static String database = System.getProperty("servers.database",
      "jdbc:postgresql:ipv6servers");
  static String history = System.getProperty("servers.history",
      "status/read-descriptors");
  static String output = System.getProperty("servers.output",
      "stats/");
}

