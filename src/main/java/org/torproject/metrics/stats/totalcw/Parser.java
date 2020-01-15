/* Copyright 2018--2020 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.stats.totalcw;

import org.torproject.descriptor.NetworkStatusEntry;
import org.torproject.descriptor.RelayNetworkStatusConsensus;
import org.torproject.descriptor.RelayNetworkStatusVote;

import java.time.Instant;
import java.time.ZoneId;

/** Parser that extracts bandwidth measurement statistics from votes and creates
 * data objects for them. */
class Parser {

  /** Parse and return a consensus, but return {@code null} if the
   * consensus did not contain any bandwidth values. */
  TotalcwRelayNetworkStatus parseRelayNetworkStatusConsensus(
      RelayNetworkStatusConsensus consensus) {
    boolean containsBandwidthValues = false;
    long[] measuredSums = new long[4];
    for (NetworkStatusEntry entry : consensus.getStatusEntries().values()) {
      if (null == entry.getFlags() || !entry.getFlags().contains("Running")
          || entry.getBandwidth() < 0L) {
        continue;
      }
      containsBandwidthValues = true;
      /* Encode flags as sum of Guard = 1 and (Exit and !BadExit) = 2. */
      int measuredSumsIndex = (entry.getFlags().contains("Guard") ? 1 : 0)
          + (entry.getFlags().contains("Exit")
          && !entry.getFlags().contains("BadExit") ? 2 : 0);
      measuredSums[measuredSumsIndex] += entry.getBandwidth();
    }
    if (!containsBandwidthValues) {
      /* Return null, because we wouldn't want to add this consensus to the
       * database anyway. */
      return null;
    }
    TotalcwRelayNetworkStatus parsedStatus = new TotalcwRelayNetworkStatus();
    parsedStatus.validAfter = Instant.ofEpochMilli(
        consensus.getValidAfterMillis())
        .atZone(ZoneId.of("UTC")).toLocalDateTime();
    parsedStatus.measuredSums = measuredSums;
    return parsedStatus;
  }

  /** Parse and return a vote, but return {@code null} if the vote did not
   * contain any bandwidth measurements. */
  TotalcwRelayNetworkStatus parseRelayNetworkStatusVote(
      RelayNetworkStatusVote vote) {
    boolean containsMeasuredBandwidths = false;
    long[] measuredSums = new long[4];
    for (NetworkStatusEntry entry : vote.getStatusEntries().values()) {
      if (null == entry.getFlags() || !entry.getFlags().contains("Running")
          || entry.getMeasured() < 0L) {
        continue;
      }
      containsMeasuredBandwidths = true;
      /* Encode flags as sum of Guard = 1 and (Exit and !BadExit) = 2. */
      int measuredSumsIndex = (entry.getFlags().contains("Guard") ? 1 : 0)
          + (entry.getFlags().contains("Exit")
          && !entry.getFlags().contains("BadExit") ? 2 : 0);
      measuredSums[measuredSumsIndex] += entry.getMeasured();
    }
    if (!containsMeasuredBandwidths) {
      /* Return null, because we wouldn't want to add this vote to the database
       * anyway. */
      return null;
    }
    TotalcwRelayNetworkStatus parsedStatus = new TotalcwRelayNetworkStatus();
    parsedStatus.validAfter = Instant.ofEpochMilli(vote.getValidAfterMillis())
        .atZone(ZoneId.of("UTC")).toLocalDateTime();
    parsedStatus.identityHex = vote.getIdentity();
    parsedStatus.nickname = vote.getNickname();
    parsedStatus.measuredSums = measuredSums;
    return parsedStatus;
  }
}

