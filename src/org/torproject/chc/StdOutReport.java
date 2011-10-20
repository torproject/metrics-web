/* Copyright 2011 The Tor Project
 * See LICENSE for licensing information */
package org.torproject.chc;

import java.io.*;
import java.text.*;
import java.util.*;

/* Check a given consensus and votes for irregularities and write results
 * to stdout while rate-limiting warnings based on severity. */
public class StdOutReport implements Report {

  /* Warning messages and the time in millis that should have passed
   * since sending them out. */
  private Map<String, Long> warnings = new HashMap<String, Long>();

  /* Date-time format to format timestamps. */
  private static SimpleDateFormat dateTimeFormat;
  static {
    dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    dateTimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
  }

  /* Downloaded consensus and corresponding votes for later
   * processing. */
  private Status downloadedConsensus;
  private SortedSet<Status> downloadedVotes;
  public void processDownloadedConsensus(Status downloadedConsensus) {
    this.downloadedConsensus = downloadedConsensus;
    this.downloadedVotes = downloadedConsensus.getVotes();
  }

  /* Check consensuses and votes for irregularities and write output to
   * stdout. */
  public void writeReport() {
    this.readLastWarned();
    if (this.downloadedConsensus != null) {
      if (this.isConsensusFresh(this.downloadedConsensus)) {
        this.checkConsensusMethods();
        this.checkRecommendedVersions();
        this.checkConsensusParameters();
        this.checkAuthorityKeys();
        this.checkMissingVotes();
        this.checkBandwidthScanners();
      }
    } else {
      this.warnings.put("No consensus known", 0L);
    }
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

  /* Check if the most recent consensus is older than 1 hour. */
  private boolean isConsensusFresh(Status consensus) {
    if (consensus.getValidAfterMillis() <
        System.currentTimeMillis() - 60L * 60L * 1000L) {
      this.warnings.put("The last known consensus published at "
          + dateTimeFormat.format(consensus.getValidAfterMillis())
          + " is more than 1 hour old and therefore not fresh anymore",
          0L);
      return false;
    } else {
      return true;
    }
  }

  /* Check supported consensus methods of all votes. */
  private void checkConsensusMethods() {
    for (Status vote : this.downloadedVotes) {
      if (!vote.getConsensusMethods().contains(
          this.downloadedConsensus.getConsensusMethods().last())) {
        this.warnings.put(vote.getNickname() + " does not "
            + "support consensus method "
            + this.downloadedConsensus.getConsensusMethods().last(),
            24L * 60L * 60L * 1000L);
      }
    }
  }

  /* Check if the recommended versions in a vote are different from the
   * recommended versions in the consensus. */
  private void checkRecommendedVersions() {
    for (Status vote : this.downloadedVotes) {
      if (vote.getRecommendedClientVersions() != null &&
          !downloadedConsensus.getRecommendedClientVersions().equals(
          vote.getRecommendedClientVersions())) {
        StringBuilder message = new StringBuilder();
        message.append(vote.getNickname() + " recommends other "
            + "client versions than the consensus:");
        for (String version : vote.getRecommendedClientVersions()) {
          message.append(" " + version);
        }
        this.warnings.put(message.toString(), 150L * 60L * 1000L);
      }
      if (vote.getRecommendedServerVersions() != null &&
          !downloadedConsensus.getRecommendedServerVersions().equals(
          vote.getRecommendedServerVersions())) {
        StringBuilder message = new StringBuilder();
        message.append(vote.getNickname() + " recommends other "
            + "server versions than the consensus:");
        for (String version : vote.getRecommendedServerVersions()) {
          message.append(" " + version);
        }
        this.warnings.put(message.toString(), 150L * 60L * 1000L);
      }
    }
  }

  /* Check if a vote contains conflicting or invalid consensus
   * parameters. */
  private void checkConsensusParameters() {
    Set<String> validParameters = new HashSet<String>(Arrays.asList(
        ("circwindow,CircuitPriorityHalflifeMsec,refuseunknownexits,"
        + "cbtdisabled,cbtnummodes,cbtrecentcount,cbtmaxtimeouts,"
        + "cbtmincircs,cbtquantile,cbtclosequantile,cbttestfreq,"
        + "cbtmintimeout,cbtinitialtimeout").split(",")));
    for (Status vote : this.downloadedVotes) {
      Map<String, String> voteConsensusParams =
          vote.getConsensusParams();
      boolean conflictOrInvalid = false;
      if (voteConsensusParams == null) {
        for (Map.Entry<String, String> e :
            voteConsensusParams.entrySet()) {
          if (!downloadedConsensus.getConsensusParams().containsKey(
              e.getKey()) ||
              !downloadedConsensus.getConsensusParams().get(e.getKey()).
              equals(e.getValue()) ||
              !validParameters.contains(e.getKey())) {
            StringBuilder message = new StringBuilder();
            message.append(vote.getNickname() + " sets conflicting or "
                + "invalid consensus parameters:");
            for (Map.Entry<String, String> p :
                voteConsensusParams.entrySet()) {
              message.append(" " + p.getKey() + "=" + p.getValue());
            }
            this.warnings.put(message.toString(), 150L * 60L * 1000L);
            break;
          }
        }
      }
    }
  }

  /* Check whether any of the authority keys expire in the next 14
   * days. */
  private void checkAuthorityKeys() {
    for (Status vote : this.downloadedVotes) {
      long voteDirKeyExpiresMillis = vote.getDirKeyExpiresMillis();
      if (voteDirKeyExpiresMillis - 14L * 24L * 60L * 60L * 1000L <
          System.currentTimeMillis()) {
        this.warnings.put(vote.getNickname() + "'s certificate "
            + "expires in the next 14 days: "
            + dateTimeFormat.format(voteDirKeyExpiresMillis),
            24L * 60L * 60L * 1000L);
      }
    }
  }

  /* Check if any votes are missing. */
  private void checkMissingVotes() {
    SortedSet<String> knownAuthorities = new TreeSet<String>(
        Arrays.asList(("dannenberg,dizum,gabelmoo,ides,maatuska,moria1,"
        + "tor26,urras").split(",")));
    SortedSet<String> missingVotes =
        new TreeSet<String>(knownAuthorities);
    for (Status vote : this.downloadedVotes) {
      missingVotes.remove(vote.getNickname());
    }
    if (!missingVotes.isEmpty()) {
      StringBuilder sb = new StringBuilder();
      for (String missingDir : missingVotes) {
        sb.append(", " + missingDir);
      }
      this.warnings.put("We're missing votes from the following "
          + "directory authorities: " + sb.toString().substring(2),
          150L * 60L * 1000L);
    }
  }

  /* Check if any bandwidth scanner results are missing. */
  private void checkBandwidthScanners() {
    SortedSet<String> missingBandwidthScanners = new TreeSet<String>(
        Arrays.asList("ides,urras,moria1,gabelmoo".split(",")));
    for (Status vote : this.downloadedVotes) {
      if (vote.getBandwidthWeights() > 0) {
        missingBandwidthScanners.remove(vote.getNickname());
      }
    }
    if (!missingBandwidthScanners.isEmpty()) {
      StringBuilder sb = new StringBuilder();
      for (String dir : missingBandwidthScanners) {
        sb.append(", " + dir);
      }
      this.warnings.put("The following directory authorities are not "
          + "reporting bandwidth scanner results: "
          + sb.toString().substring(2), 150L * 60L * 1000L);
    }
  }

  /* Prepare a report to be written to stdout. */
  private String preparedReport = null;
  private void prepareReport() {
    long now = System.currentTimeMillis();
    boolean writeReport = false;
    for (Map.Entry<String, Long> e : this.warnings.entrySet()) {
      String message = e.getKey();
      long warnInterval = e.getValue();
      if (!lastWarned.containsKey(message) ||
          lastWarned.get(message) + warnInterval < now) {
        writeReport = true;
      }
    }
    if (writeReport) {
      StringBuilder sb = new StringBuilder();
      for (String message : this.warnings.keySet()) {
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

