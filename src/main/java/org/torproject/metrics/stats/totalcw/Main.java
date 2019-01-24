/* Copyright 2018 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.stats.totalcw;

import org.torproject.descriptor.Descriptor;
import org.torproject.descriptor.DescriptorReader;
import org.torproject.descriptor.DescriptorSourceFactory;
import org.torproject.descriptor.RelayNetworkStatusConsensus;
import org.torproject.descriptor.RelayNetworkStatusVote;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.SQLException;
import java.util.Arrays;

/** Main class of the totalcw module that imports bandwidth measurement
 * statistics from votes into a database and exports aggregate statistics to a
 * CSV file. */
public class Main {

  private static Logger log = LoggerFactory.getLogger(Main.class);

  private static final File baseDir = new File(
      org.torproject.metrics.stats.main.Main.modulesDir, "totalcw");

  private static String[] paths =  {
      "recent/relay-descriptors/consensuses",
      "archive/relay-descriptors/consensuses",
      "recent/relay-descriptors/votes",
      "archive/relay-descriptors/votes" };

  /** Run the module. */
  public static void main(String[] args) throws Exception {

    log.info("Starting totalcw module.");

    log.info("Reading consensuses and votes and inserting relevant parts into "
        + "the database.");
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
          if (descriptor instanceof RelayNetworkStatusConsensus) {
            database.insertConsensus(parser.parseRelayNetworkStatusConsensus(
                (RelayNetworkStatusConsensus) descriptor));
          } else if (descriptor instanceof RelayNetworkStatusVote) {
            database.insertVote(parser.parseRelayNetworkStatusVote(
                (RelayNetworkStatusVote) descriptor));
          } else {
            log.debug("Skipping unknown descriptor of type {}.",
                descriptor.getClass());
          }
        }

        log.info("Committing all updated parts in the database.");
        database.commit();
      } catch (SQLException sqle) {
        log.error("Cannot recover from SQL exception while inserting data. "
            + "Rolling back and exiting.", sqle);
        database.rollback();
        return;
      }
      reader.saveHistoryFile(historyFile);

      log.info("Querying aggregated statistics from the database.");
      Iterable<OutputLine> output = database.queryTotalcw();
      File outputFile = new File(baseDir, "stats/totalcw.csv");
      log.info("Writing aggregated statistics to {}.", outputFile);
      if (null != output) {
        new Writer().write(outputFile.toPath(), output);
      }

      log.info("Terminating totalcw module.");
    } catch (SQLException sqle) {
      log.error("Cannot recover from SQL exception while querying. Not writing "
          + "output file.", sqle);
    }
  }
}

