/* Copyright 2011 The Tor Project
 * See LICENSE for licensing information */
package org.torproject.chc;

import java.util.*;

/* Coordinate the process of downloading consensus and votes to check
 * Tor's consensus health. */
public class Main {
  public static void main(String[] args) {

    /* Initialize reports. */
    List<Report> reports = new ArrayList<Report>();
    reports.add(new MetricsWebsiteReport(
        "website/consensus-health.html"));
    reports.add(new NagiosReport("stats/consensus-health"));
    reports.add(new StdOutReport());

    /* Download consensus and corresponding votes from the directory
     * authorities. */
    Downloader downloader = new Downloader();
    downloader.downloadFromAuthorities();

    /* Parse consensus and votes and pass them to the reports. */
    Parser parser = new Parser();
    Status parsedDownloadedConsensus = parser.parse(
        downloader.getConsensusString(), downloader.getVoteStrings());
    if (parsedDownloadedConsensus != null) {
      for (Report report : reports) {
        report.processDownloadedConsensus(parsedDownloadedConsensus);
      }
    }

    /* Finish writing reports. */
    for (Report report : reports) {
      report.writeReport();
    }

    /* Terminate the program including any download threads that may still
     * be running. */
    System.exit(0);
  }
}

