/* Copyright 2018 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.stats.totalcw;

import org.torproject.descriptor.NetworkStatusEntry;
import org.torproject.descriptor.RelayNetworkStatusVote;

import java.time.Instant;
import java.time.ZoneId;

/** Parser that extracts bandwidth measurement statistics from votes and creates
 * data objects for them. */
class Parser {

  /** Parse and return a vote, but return <code>null</code> if the vote did not
   * contain any bandwidth measurements. */
  TotalcwRelayNetworkStatusVote parseRelayNetworkStatusVote(
      RelayNetworkStatusVote vote) {
    Long measuredSum = null;
    for (NetworkStatusEntry entry : vote.getStatusEntries().values()) {
      if (null == entry.getFlags() || !entry.getFlags().contains("Running")
          || entry.getMeasured() < 0L) {
        continue;
      }
      if (null == measuredSum) {
        measuredSum = 0L;
      }
      measuredSum += entry.getMeasured();
    }
    if (null == measuredSum) {
      /* Return null, because we wouldn't want to add this vote to the database
       * anyway. */
      return null;
    }
    TotalcwRelayNetworkStatusVote parsedVote
        = new TotalcwRelayNetworkStatusVote();
    parsedVote.validAfter = Instant.ofEpochMilli(vote.getValidAfterMillis())
        .atZone(ZoneId.of("UTC")).toLocalDateTime();
    parsedVote.identityHex = vote.getIdentity();
    parsedVote.nickname = vote.getNickname();
    parsedVote.measuredSum = measuredSum;
    return parsedVote;
  }
}

