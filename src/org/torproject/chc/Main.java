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
    reports.add(new StatusFileReport());

    /* Download consensus and corresponding votes from the directory
     * authorities. */
    Downloader downloader = new Downloader();
    downloader.downloadFromAuthorities();

    /* Parse consensus and votes. */
    Parser parser = new Parser();
    SortedMap<String, Status> parsedDownloadedConsensuses = parser.parse(
        downloader.getConsensusStrings(), downloader.getVoteStrings());

    /* Check consensus and votes for possible problems. */
    Checker checker = new Checker();
    checker.processDownloadedConsensuses(parsedDownloadedConsensuses);
    SortedMap<Warning, String> warnings = checker.getWarnings();

    /* Pass warnings, consensuses, and votes to the reports, and finish
     * writing them. */
    for (Report report : reports) {
      report.processWarnings(warnings);
      report.processDownloadedConsensuses(parsedDownloadedConsensuses);
      report.writeReport();
    }

    /* Terminate the program including any download threads that may still
     * be running. */
    System.exit(0);
  }
}

