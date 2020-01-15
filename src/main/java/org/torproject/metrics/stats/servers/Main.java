/* Copyright 2017--2020 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.stats.servers;

import org.torproject.descriptor.BridgeNetworkStatus;
import org.torproject.descriptor.Descriptor;
import org.torproject.descriptor.DescriptorReader;
import org.torproject.descriptor.DescriptorSourceFactory;
import org.torproject.descriptor.RelayNetworkStatusConsensus;
import org.torproject.descriptor.ServerDescriptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.SQLException;
import java.util.Arrays;

/** Main class of the servers module that imports relevant parts from server
 * descriptors and network statuses into a database, and exports aggregate
 * statistics to CSV files. */
public class Main {

  private static Logger log = LoggerFactory.getLogger(Main.class);

  private static final File baseDir = new File(
      org.torproject.metrics.stats.main.Main.modulesDir, "servers");

  private static String[] paths = {
      "recent/relay-descriptors/consensuses",
      "recent/relay-descriptors/server-descriptors",
      "recent/bridge-descriptors/statuses",
      "recent/bridge-descriptors/server-descriptors",
      "archive/relay-descriptors/consensuses",
      "archive/relay-descriptors/server-descriptors",
      "archive/bridge-descriptors/statuses",
      "archive/bridge-descriptors/server-descriptors" };

  /** Run the module. */
  public static void main(String[] args) throws Exception {

    log.info("Starting servers module.");

    log.info("Reading descriptors and inserting relevant parts into the "
        + "database.");
    DescriptorReader reader = DescriptorSourceFactory.createDescriptorReader();
    File historyFile = new File(baseDir, "status/read-descriptors");
    reader.setHistoryFile(historyFile);
    Parser parser = new Parser();
    try (Database database = new Database()) {
      try {
        for (Descriptor descriptor : reader.readDescriptors(
            Arrays.stream(paths).map((String path) -> new File(
                org.torproject.metrics.stats.main.Main.descriptorsDir, path))
                .toArray(File[]::new))) {
          if (descriptor instanceof ServerDescriptor) {
            database.insertServerDescriptor(parser.parseServerDescriptor(
                (ServerDescriptor) descriptor));
          } else if (descriptor instanceof RelayNetworkStatusConsensus) {
            database.insertStatus(parser.parseRelayNetworkStatusConsensus(
                (RelayNetworkStatusConsensus) descriptor));
          } else if (descriptor instanceof BridgeNetworkStatus) {
            database.insertStatus(parser.parseBridgeNetworkStatus(
                (BridgeNetworkStatus) descriptor));
          } else if (null != descriptor.getRawDescriptorBytes()) {
            log.debug("Skipping unknown descriptor of type {} starting with "
                + "'{}'.", descriptor.getClass(),
                new String(descriptor.getRawDescriptorBytes(), 0,
                Math.min(descriptor.getRawDescriptorLength(), 100)));
          } else {
            log.debug("Skipping unknown, empty descriptor of type {}.",
                descriptor.getClass());
          }
        }

        log.info("Aggregating database entries.");
        database.aggregate();

        log.info("Committing all updated parts in the database.");
        database.commit();
      } catch (SQLException sqle) {
        log.error("Cannot recover from SQL exception while inserting or "
            + "aggregating data. Rolling back and exiting.", sqle);
        database.rollback();
        return;
      }
      reader.saveHistoryFile(historyFile);

      log.info("Querying aggregated statistics from the database.");
      File outputDir = new File(baseDir, "stats");
      new Writer().write(new File(outputDir, "ipv6servers.csv").toPath(),
          database.queryServersIpv6());
      new Writer().write(new File(outputDir, "advbw.csv").toPath(),
          database.queryAdvbw());
      new Writer().write(new File(outputDir, "networksize.csv").toPath(),
          database.queryNetworksize());
      new Writer().write(new File(outputDir, "relayflags.csv").toPath(),
          database.queryRelayflags());
      new Writer().write(new File(outputDir, "versions.csv").toPath(),
          database.queryVersions());
      new Writer().write(new File(outputDir, "platforms.csv").toPath(),
          database.queryPlatforms());

      log.info("Terminating servers module.");
    } catch (SQLException sqle) {
      log.error("Cannot recover from SQL exception while querying. Not writing "
          + "output file.", sqle);
    }
  }
}

