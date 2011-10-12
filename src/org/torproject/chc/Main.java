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

    /* Load last-known consensus and votes from disk and parse them. */
    Archiver archiver = new Archiver();
    archiver.loadLastFromDisk();
    Parser parser = new Parser();
    Status parsedCachedConsensus = parser.parse(
        archiver.getConsensusString(), archiver.getVoteStrings());
    if (parsedCachedConsensus != null) {
      for (Report report : reports) {
        report.processCachedConsensus(parsedCachedConsensus);
      }
    }

    /* Download consensus and corresponding votes from the directory
     * authorities and parse them, too. */
    Downloader downloader = new Downloader();
    if (parsedCachedConsensus != null) {
      downloader.setLastKnownValidAfterMillis(
          parsedCachedConsensus.getValidAfterMillis());
    }
    downloader.downloadFromAuthorities();
    Status parsedDownloadedConsensus = parser.parse(
        downloader.getConsensusString(), downloader.getVoteStrings());
    if (parsedDownloadedConsensus != null) {
      for (Report report : reports) {
        report.processDownloadedConsensus(parsedDownloadedConsensus);
      }
    }

    /* Save the new consensus and corresponding votes to disk and delete
     * all previous ones. */
    if (parsedDownloadedConsensus != null) {
      archiver.saveStatusStringToDisk(
          parsedDownloadedConsensus.getUnparsedString(),
          parsedDownloadedConsensus.getFileName());
      for (Status vote : parsedDownloadedConsensus.getVotes()) {
        archiver.saveStatusStringToDisk(vote.getUnparsedString(),
            vote.getFileName());
      }
      archiver.deleteAllButLast();
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

