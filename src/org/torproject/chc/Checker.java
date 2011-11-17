/* Copyright 2011 The Tor Project
 * See LICENSE for licensing information */
package org.torproject.chc;

import java.io.*;
import java.text.*;
import java.util.*;

/* Check a given consensus and votes for irregularities and write results
 * to a warnings map consisting of warning type and details. */
public class Checker {

  /* Warning messages consisting of type and details. */
  private SortedMap<Warning, String> warnings =
      new TreeMap<Warning, String>();

  public SortedMap<Warning, String> getWarnings() {
    return this.warnings;
  }

  /* Date-time format to format timestamps. */
  private static SimpleDateFormat dateTimeFormat;
  static {
    dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    dateTimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
  }

  /* Downloaded consensus and corresponding votes for processing. */
  private SortedMap<String, Status> downloadedConsensuses;
  private Status downloadedConsensus;
  private SortedSet<Status> downloadedVotes;
  public void processDownloadedConsensuses(
      SortedMap<String, Status> downloadedConsensuses) {
    this.downloadedConsensuses = downloadedConsensuses;
    this.findMostRecentConsensus();
    this.checkMissingConsensuses();
    this.checkAllConsensusesFresh();
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
      this.warnings.put(Warning.NoConsensusKnown, "");
    }
  }

  /* Find most recent consensus and corresponding votes. */
  private void findMostRecentConsensus() {
    long mostRecentValidAfterMillis = -1L;
    for (Status downloadedConsensus : downloadedConsensuses.values()) {
      if (downloadedConsensus.getValidAfterMillis() >
          mostRecentValidAfterMillis) {
        this.downloadedConsensus = downloadedConsensus;
        mostRecentValidAfterMillis =
            downloadedConsensus.getValidAfterMillis();
      }
    }
    if (this.downloadedConsensus != null) {
      this.downloadedVotes = this.downloadedConsensus.getVotes();
    }
  }

  /* Check if any directory authority didn't tell us a consensus. */
  private void checkMissingConsensuses() {
    SortedSet<String> missingConsensuses = new TreeSet<String>(
        Arrays.asList(("gabelmoo,tor26,ides,maatuska,dannenberg,urras,"
        + "moria1,dizum").split(",")));
    missingConsensuses.removeAll(this.downloadedConsensuses.keySet());
    if (!missingConsensuses.isEmpty()) {
      StringBuilder sb = new StringBuilder();
      for (String nickname : missingConsensuses) {
        sb.append(", " + nickname);
      }
      this.warnings.put(Warning.ConsensusDownloadTimeout,
          sb.toString().substring(2));
    }
  }

  /* Check if all consensuses are fresh. */
  private void checkAllConsensusesFresh() {
    long fresh = System.currentTimeMillis() - 60L * 60L * 1000L;
    SortedSet<String> nonFresh = new TreeSet<String>();
    for (Map.Entry<String, Status> e : downloadedConsensuses.entrySet()) {
      String nickname = e.getKey();
      Status downloadedConsensus = e.getValue();
      if (downloadedConsensus.getValidAfterMillis() < fresh) {
        nonFresh.add(nickname);
      }
    }
    if (!nonFresh.isEmpty()) {
      StringBuilder sb = new StringBuilder();
      for (String nickname : nonFresh) {
        sb.append(", " + nickname);
      }
      this.warnings.put(Warning.ConsensusNotFresh,
          sb.toString().substring(2));
    }
  }

  /* Check if the most recent consensus is older than 1 hour. */
  private boolean isConsensusFresh(Status consensus) {
    return (consensus.getValidAfterMillis() >=
        System.currentTimeMillis() - 60L * 60L * 1000L);
  }

  /* Check supported consensus methods of all votes. */
  private void checkConsensusMethods() {
    SortedSet<String> dirs = new TreeSet<String>();
    for (Status vote : this.downloadedVotes) {
      if (!vote.getConsensusMethods().contains(
          this.downloadedConsensus.getConsensusMethods().last())) {
        dirs.add(vote.getNickname());
      }
    }
    if (!dirs.isEmpty()) {
      StringBuilder sb = new StringBuilder();
      for (String dir : dirs) {
        sb.append(", " + dir);
      }
      this.warnings.put(Warning.ConsensusMethodNotSupported,
          sb.toString().substring(2));
    }
  }

  /* Check if the recommended versions in a vote are different from the
   * recommended versions in the consensus. */
  private void checkRecommendedVersions() {
    SortedSet<String> unrecommendedClientVersions = new TreeSet<String>(),
        unrecommendedServerVersions = new TreeSet<String>();
    for (Status vote : this.downloadedVotes) {
      if (vote.getRecommendedClientVersions() != null &&
          !downloadedConsensus.getRecommendedClientVersions().equals(
          vote.getRecommendedClientVersions())) {
        StringBuilder message = new StringBuilder();
        message.append(vote.getNickname());
        for (String version : vote.getRecommendedClientVersions()) {
          message.append(" " + version);
        }
        unrecommendedClientVersions.add(message.toString());
      }
      if (vote.getRecommendedServerVersions() != null &&
          !downloadedConsensus.getRecommendedServerVersions().equals(
          vote.getRecommendedServerVersions())) {
        StringBuilder message = new StringBuilder();
        message.append(vote.getNickname());
        for (String version : vote.getRecommendedServerVersions()) {
          message.append(" " + version);
        }
        unrecommendedServerVersions.add(message.toString());
      }
    }
    if (!unrecommendedServerVersions.isEmpty()) {
      StringBuilder sb = new StringBuilder();
      for (String dir : unrecommendedServerVersions) {
        sb.append(", " + dir);
      }
      this.warnings.put(Warning.DifferentRecommendedServerVersions,
          sb.toString().substring(2));
    }
    if (!unrecommendedClientVersions.isEmpty()) {
      StringBuilder sb = new StringBuilder();
      for (String dir : unrecommendedClientVersions) {
        sb.append(", " + dir);
      }
      this.warnings.put(Warning.DifferentRecommendedClientVersions,
          sb.toString().substring(2));
    }
  }

  /* Check if a vote contains conflicting or invalid consensus
   * parameters. */
  private void checkConsensusParameters() {
    Set<String> validParameters = new HashSet<String>(Arrays.asList(
        ("circwindow,CircuitPriorityHalflifeMsec,refuseunknownexits,"
        + "cbtdisabled,cbtnummodes,cbtrecentcount,cbtmaxtimeouts,"
        + "cbtmincircs,cbtquantile,cbtclosequantile,cbttestfreq,"
        + "cbtmintimeout,cbtinitialtimeout,bwauthpid,bwauthbestratio,"
        + "bwauthcircs,bwauthkp,bwauthtd,bwauthti,bwauthtidecay,"
        + "bwauthdescbw").split(",")));
    SortedSet<String> conflicts = new TreeSet<String>();
    for (Status vote : this.downloadedVotes) {
      Map<String, String> voteConsensusParams =
          vote.getConsensusParams();
      boolean conflictOrInvalid = false;
      if (voteConsensusParams != null) {
        for (Map.Entry<String, String> e :
            voteConsensusParams.entrySet()) {
          if (!downloadedConsensus.getConsensusParams().containsKey(
              e.getKey()) ||
              !downloadedConsensus.getConsensusParams().get(e.getKey()).
              equals(e.getValue()) ||
              !validParameters.contains(e.getKey())) {
            StringBuilder message = new StringBuilder();
            message.append(vote.getNickname());
            for (Map.Entry<String, String> p :
                voteConsensusParams.entrySet()) {
              message.append(" " + p.getKey() + "=" + p.getValue());
            }
            conflicts.add(message.toString());
            break;
          }
        }
      }
    }
    if (!conflicts.isEmpty()) {
      StringBuilder sb = new StringBuilder();
      for (String dir : conflicts) {
        sb.append(", " + dir);
      }
      this.warnings.put(Warning.ConflictingOrInvalidConsensusParams,
          sb.toString().substring(2));
    }
  }

  /* Check whether any of the authority keys expire in the next 14
   * days. */
  private void checkAuthorityKeys() {
    SortedMap<String, String> expiringCertificates =
        new TreeMap<String, String>();
    long now = System.currentTimeMillis();
    for (Status vote : this.downloadedVotes) {
      long voteDirKeyExpiresMillis = vote.getDirKeyExpiresMillis();
      if (voteDirKeyExpiresMillis - 14L * 24L * 60L * 60L * 1000L < now) {
        expiringCertificates.put(vote.getNickname(),
            dateTimeFormat.format(voteDirKeyExpiresMillis));
      }
    }
    if (!expiringCertificates.isEmpty()) {
      StringBuilder sb = new StringBuilder();
      for (Map.Entry<String, String> e :
          expiringCertificates.entrySet()) {
        String dir = e.getKey();
        String timestamp = e.getValue();
        sb.append(", " + dir + " " + timestamp);
      }
      this.warnings.put(Warning.CertificateExpiresSoon,
          sb.toString().substring(2));
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
      this.warnings.put(Warning.VotesMissing,
          sb.toString().substring(2));
    }
  }

  /* Check if any bandwidth scanner results are missing. */
  private void checkBandwidthScanners() {
    SortedSet<String> missingBandwidthScanners = new TreeSet<String>(
        Arrays.asList("ides,urras,moria1,gabelmoo,maatuska".split(",")));
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
      this.warnings.put(Warning.BandwidthScannerResultsMissing,
          sb.toString().substring(2));
    }
  }
}

