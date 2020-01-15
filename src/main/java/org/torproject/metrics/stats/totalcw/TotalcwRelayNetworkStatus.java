/* Copyright 2018--2020 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.stats.totalcw;

import java.time.LocalDateTime;

/** Data object holding all relevant parts parsed from a consensus or vote. */
class TotalcwRelayNetworkStatus {

  /** Valid-after time of the vote. */
  LocalDateTime validAfter;

  /** The 1 to 19 character long alphanumeric nickname assigned to the authority
   * by its operator, or {@code null} if this is a consensus. */
  String nickname;

  /** Uppercase hex fingerprint of the authority's (v3 authority) identity
   * key, or {@code null} if this is a consensus. */
  String identityHex;

  /** Sums of bandwidth measurements of all contained status entries with four
   * entries: 0 = neither Exit nor Guard, 1 = only Guard, 2 = only Exit, and
   * 3 = both Guard and Exit. */
  long[] measuredSums;
}

