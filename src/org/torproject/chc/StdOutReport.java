/* Copyright 2011 The Tor Project
 * See LICENSE for licensing information */
package org.torproject.chc;

import java.io.*;
import java.text.*;
import java.util.*;

/* Check a given consensus and votes for irregularities and write results
 * to stdout while rate-limiting warnings based on severity. */
public class StdOutReport implements Report {

  /* Date-time format to format timestamps. */
  private static SimpleDateFormat dateTimeFormat;
  static {
    dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    dateTimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
  }

  /* Downloaded consensus and corresponding votes for later
   * processing. */
  private SortedMap<String, Status> downloadedConsensuses;
  private Status downloadedConsensus;
  private SortedSet<Status> downloadedVotes;
  public void processDownloadedConsensuses(
      SortedMap<String, Status> downloadedConsensuses) {
    this.downloadedConsensuses = downloadedConsensuses;
  }

  /* Warnings obtained from checking the current consensus and votes. */
  private SortedMap<Warning, String> warnings;
  public void processWarnings(SortedMap<Warning, String> warnings) {
    this.warnings = warnings;
  }

  /* Check consensuses and votes for irregularities and write output to
   * stdout. */
  public void writeReport() {
    this.readLastWarned();
    this.prepareReport();
    this.writeReportToStdOut();
    this.writeLastWarned();
  }

  /* Warning messages of the last 24 hours that is used to implement
   * rate limiting. */
  private Map<String, Long> lastWarned = new HashMap<String, Long>();

  /* Read when we last emitted a warning to rate-limit some of them. */
  private void readLastWarned() {
    long now = System.currentTimeMillis();
    File lastWarnedFile = new File("stats/chc-last-warned");
    try {
      if (lastWarnedFile.exists()) {
        BufferedReader br = new BufferedReader(new FileReader(
            lastWarnedFile));
        String line;
        while ((line = br.readLine()) != null) {
          if (!line.contains(": ")) {
            System.err.println("Bad line in stats/chc-last-warned: '" + line
                + "'.  Ignoring this line.");
            continue;
          }
          long warnedMillis = Long.parseLong(line.substring(0,
              line.indexOf(": ")));
          if (warnedMillis < now - 24L * 60L * 60L * 1000L) {
            /* Remove warnings that are older than 24 hours. */
            continue;
          }
          String message = line.substring(line.indexOf(": ") + 2);
          lastWarned.put(message, warnedMillis);
        }
      }
    } catch (IOException e) {
      System.err.println("Could not read file '"
          + lastWarnedFile.getAbsolutePath() + "' to learn which "
          + "warnings have been sent out before.  Ignoring.");
    }
  }

  /* Prepare a report to be written to stdout. */
  private String preparedReport = null;
  private void prepareReport() {
    SortedMap<String, Long> warningStrings = new TreeMap<String, Long>();
    for (Map.Entry<Warning, String> e : this.warnings.entrySet()) {
      Warning type = e.getKey();
      String details = e.getValue();
      switch (type) {
        case NoConsensusKnown:
          break;
        case ConsensusDownloadTimeout:
          warningStrings.put("The following directory authorities did "
              + "not return a consensus within a timeout of 60 seconds: "
              + details, 150L * 60L * 1000L);
          break;
        case ConsensusNotFresh:
          warningStrings.put("The consensuses published by the following "
              + "directory authorities are more than 1 hour old and "
              + "therefore not fresh anymore: " + details,
              150L * 60L * 1000L);
          break;
        case ConsensusMethodNotSupported:
          warningStrings.put("The following directory authorities do not "
              + "support the consensus method that the consensus uses: "
              + details, 24L * 60L * 60L * 1000L);
          break;
        case DifferentRecommendedClientVersions:
          warningStrings.put("The following directory authorities "
              + "recommend other client versions than the consensus: "
              + details, 150L * 60L * 1000L);
          break;
        case DifferentRecommendedServerVersions:
          warningStrings.put("The following directory authorities "
              + "recommend other server versions than the consensus: "
              + details, 150L * 60L * 1000L);
          break;
        case ConflictingOrInvalidConsensusParams:
          warningStrings.put("The following directory authorities set "
              + "conflicting or invalid consensus parameters: " + details,
              150L * 60L * 1000L);
          break;
        case CertificateExpiresSoon:
          warningStrings.put("The certificates of the following "
              + "directory authorities expire within the next 14 days: "
              + details, 24L * 60L * 60L * 1000L);
          break;
        case VotesMissing:
          warningStrings.put("We're missing votes from the following "
              + "directory authorities: " + details, 150L * 60L * 1000L);
          break;
        case BandwidthScannerResultsMissing:
          warningStrings.put("The following directory authorities are "
              + "not reporting bandwidth scanner results: " + details,
              150L * 60L * 1000L);
          break;
      }
    }
    long now = System.currentTimeMillis();
    boolean writeReport = false;
    for (Map.Entry<String, Long> e : warningStrings.entrySet()) {
      String message = e.getKey();
      long warnInterval = e.getValue();
      if (!lastWarned.containsKey(message) ||
          lastWarned.get(message) + warnInterval < now) {
        writeReport = true;
      }
    }
    if (writeReport) {
      StringBuilder sb = new StringBuilder();
      for (String message : warningStrings.keySet()) {
        this.lastWarned.put(message, now);
        sb.append("\n\n" + message);
      }
      this.preparedReport = sb.toString().substring(2);
    }
  }

  /* Write report to stdout. */
  private void writeReportToStdOut() {
    if (this.preparedReport != null) {
      System.out.println(this.preparedReport);
    }
  }

  /* Write timestamps when warnings were last sent to disk. */
  private void writeLastWarned() {
    File lastWarnedFile = new File("stats/chc-last-warned");
    try {
      lastWarnedFile.getParentFile().mkdirs();
      BufferedWriter bw = new BufferedWriter(new FileWriter(
          lastWarnedFile));
      for (Map.Entry<String, Long> e : lastWarned.entrySet()) {
        bw.write(String.valueOf(e.getValue()) + ": " + e.getKey() + "\n");
      }
      bw.close();
    } catch (IOException e) {
      System.err.println("Could not write file '"
          + lastWarnedFile.getAbsolutePath() + "' to remember which "
          + "warnings have been sent out before.  Ignoring.");
    }
  }
}

