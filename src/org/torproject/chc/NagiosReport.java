/* Copyright 2011 The Tor Project
 * See LICENSE for licensing information */
package org.torproject.chc;

import java.io.*;
import java.text.*;
import java.util.*;

/* Check a given consensus and votes for irregularities and writes results
 * to a local text file for Nagios to print out warnings. */
public class NagiosReport implements Report {

  /* Output file to write report to. */
  private File nagiosOutputFile;

  /* Initialize this report. */
  public NagiosReport(String nagiosOutputFilename) {
    this.nagiosOutputFile = new File(nagiosOutputFilename);
  }

  /* Store the cached consensus for processing. */
  private Status cachedConsensus;
  public void processCachedConsensus(Status cachedConsensus) {
    this.cachedConsensus = cachedConsensus;
  }

  /* Store the current consensus and corresponding votes for
   * processing. */
  private Status downloadedConsensus;
  private SortedSet<Status> downloadedVotes;
  public void processDownloadedConsensus(Status downloadedConsensus) {
    this.downloadedConsensus = downloadedConsensus;
    this.downloadedVotes = downloadedConsensus.getVotes();
  }

  /* Lists of output messages sorted by warnings, criticals, and
   * unknowns (increasing severity). */
  private List<String> nagiosWarnings = new ArrayList<String>(),
      nagiosCriticals = new ArrayList<String>(),
      nagiosUnknowns = new ArrayList<String>();

  /* Date-time format to format timestamps. */
  private static SimpleDateFormat dateTimeFormat;
  static {
    dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    dateTimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
  }

  /* Check consensus and votes and write any findings to the output
   * file. */
  public void writeReport() {
    if (this.downloadedConsensus != null) {
      if (this.isConsensusFresh(this.downloadedConsensus)) {
        this.checkConsensusMethods();
        this.checkRecommendedVersions();
        this.checkConsensusParameters();
        this.checkAuthorityKeys();
        this.checkMissingVotes();
        this.checkBandwidthScanners();
      }
    } else if (this.cachedConsensus != null) {
      this.checkConsensusValid(this.cachedConsensus);
    } else {
      this.nagiosUnknowns.add("No consensus known");
    }
    this.writeNagiosStatusFile();
  }

  /* Check if the most recent consensus is older than 1 hour. */
  private boolean isConsensusFresh(Status consensus) {
    if (consensus.getValidAfterMillis() <
        System.currentTimeMillis() - 60L * 60L * 1000L) {
      this.nagiosCriticals.add("The last known consensus published at "
          + dateTimeFormat.format(consensus.getValidAfterMillis())
          + " is more than 1 hour old and therefore not fresh anymore");
      return false;
    } else {
      return true;
    }
  }

  /* Check supported consensus methods of all votes. */
  private void checkConsensusMethods() {
    for (Status vote : this.downloadedVotes) {
      if (!this.downloadedConsensus.getConsensusMethods().contains(
          this.downloadedConsensus.getConsensusMethods().last())) {
        nagiosWarnings.add(vote.getNickname() + " does not support "
            + "consensus method "
            + this.downloadedConsensus.getConsensusMethods().last());
      }
    }
  }

  /* Check if the recommended client and server versions in a vote are
   * different from the recommended versions in the consensus. */
  private void checkRecommendedVersions() {
    for (Status vote : this.downloadedVotes) {
      if (vote.getRecommendedClientVersions() != null &&
          !downloadedConsensus.getRecommendedClientVersions().equals(
          vote.getRecommendedClientVersions())) {
        nagiosWarnings.add(vote.getNickname() + " recommends other "
            + "client versions than the consensus");
      }
      if (vote.getRecommendedServerVersions() != null &&
          !downloadedConsensus.getRecommendedServerVersions().equals(
          vote.getRecommendedServerVersions())) {
        nagiosWarnings.add(vote.getNickname() + " recommends other "
            + "server versions than the consensus");
      }
    }
  }

  /* Checks if a vote contains conflicting or invalid consensus
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
            message.append(vote.getNickname() + " sets conflicting "
                + "or invalid consensus parameters:");
            for (Map.Entry<String, String> p :
                voteConsensusParams.entrySet()) {
              message.append(" " + p.getKey() + "=" + p.getValue());
            }
            nagiosWarnings.add(message.toString());
            break;
          }
        }
      }
    }
  }

  /* Check whether authority keys expire in the next 14 days. */
  private void checkAuthorityKeys() {
    for (Status vote : this.downloadedVotes) {
      long voteDirKeyExpiresMillis = vote.getDirKeyExpiresMillis();
      if (voteDirKeyExpiresMillis - 14L * 24L * 60L * 60L * 1000L <
          System.currentTimeMillis()) {
        nagiosWarnings.add(vote.getNickname() + "'s certificate "
            + "expires in the next 14 days");
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
      nagiosWarnings.add("We're missing votes from the following "
          + "directory authorities: " + sb.toString().substring(2));
    }
  }

  /* Check if any bandwidth scanner results are missing. */
  private void checkBandwidthScanners() {
    SortedSet<String> missingBandwidthScanners = new TreeSet<String>(
        Arrays.asList("ides,urras,moria1,gabelmoo".split(",")));
    SortedSet<String> runningBandwidthScanners = new TreeSet<String>();
    for (Status vote : this.downloadedVotes) {
      if (vote.getBandwidthWeights() > 0) {
        missingBandwidthScanners.remove(vote.getNickname());
        runningBandwidthScanners.add(vote.getNickname());
      }
    }
    if (!missingBandwidthScanners.isEmpty()) {
      StringBuilder sb = new StringBuilder();
      for (String dir : missingBandwidthScanners) {
        sb.append(", " + dir);
      }
      String message = "The following directory authorities are not "
          + "reporting bandwidth scanner results: "
          + sb.toString().substring(2);
      if (runningBandwidthScanners.size() >= 3) {
        nagiosWarnings.add(message);
      } else {
        nagiosCriticals.add(message);
      }
    }
  }

  /* Check that the most recent consensus is not more than 3 hours
   * old. */
  public void checkConsensusValid(Status consensus) {
    if (consensus.getValidAfterMillis() <
        System.currentTimeMillis() - 3L * 60L * 60L * 1000L) {
      this.nagiosCriticals.add("The last known consensus published at "
          + dateTimeFormat.format(consensus.getValidAfterMillis())
          + " is more than 3 hours old and therefore not valid anymore");
    }
  }

  /* Write all output to the Nagios status file.  The most severe status
   * goes in the first line of the output file and the same status and all
   * log messages in the second line. */
  private void writeNagiosStatusFile() {
    File nagiosStatusFile = this.nagiosOutputFile;
    try {
      BufferedWriter bw = new BufferedWriter(new FileWriter(
          nagiosStatusFile));
      if (!nagiosUnknowns.isEmpty()) {
        bw.write("UNKNOWN\nUNKNOWN");
      } else if (!nagiosCriticals.isEmpty()) {
        bw.write("CRITICAL\nCRITICAL");
      } else if (!nagiosWarnings.isEmpty()) {
        bw.write("WARNING\nWARNING");
      } else {
        bw.write("OK\nOK");
      }
      for (String message : nagiosUnknowns) {
        bw.write(" " + message + ";");
      }
      for (String message : nagiosCriticals) {
        bw.write(" " + message + ";");
      }
      for (String message : nagiosWarnings) {
        bw.write(" " + message + ";");
      }
      bw.write("\n");
      bw.close();
    } catch (IOException e) {
      System.err.println("Could not write Nagios output file to '"
          + nagiosStatusFile.getAbsolutePath() + "'.  Ignoring.");
    }
  }
}

