/* Copyright 2018 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.stats.totalcw;

import java.time.LocalDateTime;

/** Data object holding all relevant parts parsed from a vote. */
class TotalcwRelayNetworkStatusVote {

  /** Valid-after time of the vote. */
  LocalDateTime validAfter;

  /** The 1 to 19 character long alphanumeric nickname assigned to the authority
   * by its operator. */
  String nickname;

  /** Uppercase hex fingerprint of the authority's (v3 authority) identity
   * key. */
  String identityHex;

  /** Count of status entries containing bandwidth measurements. */
  long measuredCount;

  /** Sum of bandwidth measurements of all contained status entries. */
  long measuredSum;

  /** Mean value of bandwidth measurements of all contained status entries. */
  long measuredMean;

  /** Minimum value of bandwidth measurements of all contained status
   * entries. */
  long measuredMin;

  /** First quartile value of bandwidth measurements of all contained status
   * entries. */
  long measuredQ1;

  /** Median value of bandwidth measurements of all contained status entries. */
  long measuredMedian;

  /** Third quartile value of bandwidth measurements of all contained status
   * entries. */
  long measuredQ3;

  /** Maximum value of bandwidth measurements of all contained status
   * entries. */
  long measuredMax;
}

