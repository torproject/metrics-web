/* Copyright 2018 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.stats.totalcw;

import org.torproject.descriptor.NetworkStatusEntry;
import org.torproject.descriptor.RelayNetworkStatusVote;

import org.apache.commons.math3.stat.descriptive.rank.Percentile;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Parser that extracts bandwidth measurement statistics from votes and creates
 * data objects for them. */
class Parser {

  /** Parse and return a vote, but return <code>null</code> if the vote did not
   * contain any bandwidth measurements. */
  TotalcwRelayNetworkStatusVote parseRelayNetworkStatusVote(
      RelayNetworkStatusVote vote) {
    List<Long> measuredBandwidths = new ArrayList<>();
    for (NetworkStatusEntry entry : vote.getStatusEntries().values()) {
      if (entry.getMeasured() >= 0L) {
        measuredBandwidths.add(entry.getMeasured());
      }
    }
    if (measuredBandwidths.isEmpty()) {
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
    Collections.sort(measuredBandwidths);
    long totalValue = 0L;
    double[] values = new double[measuredBandwidths.size()];
    for (int i = 0; i < measuredBandwidths.size(); i++) {
      values[i] = (double) measuredBandwidths.get(i);
      totalValue += measuredBandwidths.get(i);
    }
    parsedVote.measuredCount = values.length;
    parsedVote.measuredSum = totalValue;
    parsedVote.measuredMean = totalValue / values.length;
    parsedVote.measuredMin = (long) Math.floor(values[0]);
    parsedVote.measuredMax = (long) Math.floor(values[values.length - 1]);
    Percentile percentile = new Percentile().withEstimationType(
        Percentile.EstimationType.R_7);
    percentile.setData(values);
    parsedVote.measuredQ1 = (long) Math.floor(percentile.evaluate(25.0));
    parsedVote.measuredMedian = (long) Math.floor(percentile.evaluate(50.0));
    parsedVote.measuredQ3 = (long) Math.floor(percentile.evaluate(75.0));
    return parsedVote;
  }
}

