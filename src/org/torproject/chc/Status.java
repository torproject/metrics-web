/* Copyright 2011 The Tor Project
 * See LICENSE for licensing information */
package org.torproject.chc;

import java.util.*;

/* Contains the unparsed string and parsed fields from a network status
 * consensus or vote. */
public class Status implements Comparable<Status> {

  /* Helper methods to implement the Comparable interface; Status
   * instances are compared by nickname of the publishing directory
   * authorities. */
  public int compareTo(Status o) {
    return this.nickname.compareTo(o.nickname);
  }
  public boolean equals(Object o) {
    return (o instanceof Status &&
        this.nickname.equals(((Status) o).nickname));
  }

  /* Unparsed string that was downloaded or read from disk and that can
   * be written to disk. */
  private String unparsedString;
  public void setUnparsedString(String unparsedString) {
    this.unparsedString = unparsedString;
  }
  public String getUnparsedString() {
    return this.unparsedString;
  }

  /* Votes published at the same time as this consensus; votes don't
   * reference any statuses. */
  private SortedSet<Status> votes = new TreeSet<Status>();
  public void addVote(Status vote) {
    this.votes.add(vote);
  }
  public SortedSet<Status> getVotes() {
    return this.votes;
  }

  /* Fingerprint of the directory authority publishing this vote; left
   * empty for consensuses. */
  private String fingerprint;
  public void setFingerprint(String fingerprint) {
    this.fingerprint = fingerprint;
  }
  public String getFingerprint() {
    return this.fingerprint;
  }

  /* Nickname of the directory authority publishing this vote; left empty
   * for consensuses. */
  private String nickname;
  public void setNickname(String nickname) {
    this.nickname= nickname;
  }
  public String getNickname() {
    return this.nickname;
  }

  /* Valid-after time in milliseconds. */
  private long validAfterMillis;
  public void setValidAfterMillis(long validAfterMillis) {
    this.validAfterMillis = validAfterMillis;
  }
  public long getValidAfterMillis() {
    return this.validAfterMillis;
  }

  /* Consensus parameters. */
  private SortedMap<String, String> consensusParams =
      new TreeMap<String, String>();
  public void addConsensusParam(String paramName, String paramValue) {
    this.consensusParams.put(paramName, paramValue);
  }
  public SortedMap<String, String> getConsensusParams() {
    return this.consensusParams;
  }

  /* Consensus methods supported by the directory authority sending a vote
   * or of the produced consensus. */
  private SortedSet<Integer> consensusMethods;
  public void setConsensusMethods(SortedSet<Integer> consensusMethods) {
    this.consensusMethods = consensusMethods;
  }
  public SortedSet<Integer> getConsensusMethods() {
    return this.consensusMethods;
  }

  /* Recommended server versions. */
  private SortedSet<String> recommendedServerVersions;
  public void setRecommendedServerVersions(
      SortedSet<String> recommendedServerVersions) {
    this.recommendedServerVersions = recommendedServerVersions;
  }
  public SortedSet<String> getRecommendedServerVersions() {
    return this.recommendedServerVersions;
  }

  /* Recommended client versions. */
  private SortedSet<String> recommendedClientVersions;
  public void setRecommendedClientVersions(
      SortedSet<String> recommendedClientVersions) {
    this.recommendedClientVersions = recommendedClientVersions;
  }
  public SortedSet<String> getRecommendedClientVersions() {
    return this.recommendedClientVersions;
  }

  /* Expiration times of directory signing keys. */
  private long dirKeyExpiresMillis;
  public void setDirKeyExpiresMillis(long dirKeyExpiresMillis) {
    this.dirKeyExpiresMillis = dirKeyExpiresMillis;
  }
  public long getDirKeyExpiresMillis() {
    return this.dirKeyExpiresMillis;
  }

  /* Known flags by the directory authority sending a vote or of the
   * produced consensus. */
  private SortedSet<String> knownFlags = new TreeSet<String>();
  public void addKnownFlag(String knownFlag) {
    this.knownFlags.add(knownFlag);
  }
  public SortedSet<String> getKnownFlags() {
    return this.knownFlags;
  }

  /* Number of status entries with the Running flag. */
  private int runningRelays;
  public void setRunningRelays(int runningRelays) {
    this.runningRelays = runningRelays;
  }
  public int getRunningRelays() {
    return this.runningRelays;
  }

  /* Number of status entries containing bandwidth weights (only relevant
   * in votes). */
  private int bandwidthWeights;
  public void setBandwidthWeights(int bandwidthWeights) {
    this.bandwidthWeights = bandwidthWeights;
  }
  public int getBandwidthWeights() {
    return this.bandwidthWeights;
  }

  /* Status entries contained in this status. */
  private SortedMap<String, StatusEntry> statusEntries =
      new TreeMap<String, StatusEntry>();
  public void addStatusEntry(StatusEntry statusEntry) {
    this.statusEntries.put(statusEntry.getFingerprint(), statusEntry);
  }
  public SortedMap<String, StatusEntry> getStatusEntries() {
    return this.statusEntries;
  }
  public boolean containsStatusEntry(String fingerprint) {
    return this.statusEntries.containsKey(fingerprint);
  }
  public StatusEntry getStatusEntry(String fingerprint) {
    return this.statusEntries.get(fingerprint);
  }

  /* Versions of directory authorities (only set in a consensus). */
  private SortedMap<String, String> authorityVersions =
      new TreeMap<String, String>();
  public void addAuthorityVersion(String fingerprint,
      String versionString) {
    this.authorityVersions.put(fingerprint, versionString);
  }
  public SortedMap<String, String> getAuthorityVersions() {
    return this.authorityVersions;
  }
}

