/* Copyright 2016--2017 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.hidserv;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/** Main class for updating extrapolated network totals of hidden-service
 * statistics.  The main method of this class can be executed as often as
 * new statistics are needed, though callers must ensure that executions
 * do not overlap. */
public class Main {

  /** Parses new descriptors, extrapolate contained statistics using
   * computed network fractions, aggregate results, and writes results to
   * disk. */
  public static void main(String[] args) {

    /* Initialize directories and file paths. */
    Set<File> inDirectories = new HashSet<File>();
    inDirectories.add(
        new File("../../shared/in/recent/relay-descriptors/consensuses"));
    inDirectories.add(
        new File("../../shared/in/recent/relay-descriptors/extra-infos"));
    File statusDirectory = new File("status");

    /* Initialize parser and read parse history to avoid parsing
     * descriptor files that haven't changed since the last execution. */
    System.out.println("Initializing parser and reading parse "
        + "history...");
    DocumentStore<ReportedHidServStats> reportedHidServStatsStore =
        new DocumentStore<ReportedHidServStats>(
        ReportedHidServStats.class);
    DocumentStore<ComputedNetworkFractions>
        computedNetworkFractionsStore =
        new DocumentStore<ComputedNetworkFractions>(
        ComputedNetworkFractions.class);
    Parser parser = new Parser(inDirectories, statusDirectory,
        reportedHidServStatsStore, computedNetworkFractionsStore);
    parser.readParseHistory();

    /* Parse new descriptors and store their contents using the document
     * stores. */
    System.out.println("Parsing descriptors...");
    if (!parser.parseDescriptors()) {
      System.err.println("Could not store parsed descriptors.  "
          + "Terminating.");
      return;
    }

    /* Write the parse history to avoid parsing descriptor files again
     * next time.  It's okay to do this now and not at the end of the
     * execution, because even if something breaks apart below, it's safe
     * not to parse descriptor files again. */
    System.out.println("Writing parse history...");
    parser.writeParseHistory();

    /* Extrapolate reported statistics using computed network fractions
     * and write the result to disk using a document store.  The result is
     * a single file with extrapolated network totals based on reports by
     * single relays. */
    System.out.println("Extrapolating statistics...");
    DocumentStore<ExtrapolatedHidServStats> extrapolatedHidServStatsStore
        = new DocumentStore<ExtrapolatedHidServStats>(
        ExtrapolatedHidServStats.class);
    Extrapolator extrapolator = new Extrapolator(statusDirectory,
        reportedHidServStatsStore, computedNetworkFractionsStore,
        extrapolatedHidServStatsStore);
    if (!extrapolator.extrapolateHidServStats()) {
      System.err.println("Could not extrapolate statistics.  "
          + "Terminating.");
      return;
    }

    /* Go through all extrapolated network totals and aggregate them.
     * This includes calculating daily weighted interquartile means, among
     * other statistics.  Write the result to a .csv file that can be
     * processed by other tools. */
    System.out.println("Aggregating statistics...");
    File hidservStatsExtrapolatedCsvFile = new File("stats/hidserv.csv");
    Aggregator aggregator = new Aggregator(statusDirectory,
        extrapolatedHidServStatsStore, hidservStatsExtrapolatedCsvFile);
    aggregator.aggregateHidServStats();

    /* End this execution. */
    System.out.println("Terminating.");
  }
}

