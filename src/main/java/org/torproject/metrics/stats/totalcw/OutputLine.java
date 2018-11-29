/* Copyright 2018 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.stats.totalcw;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.stream.Collectors;

/** Data object holding all parts of an output line. */
class OutputLine {

  /** Column names used in the database and in the first line of the output
   * file. */
  enum Column {
    VALID_AFTER_DATE, NICKNAME, HAVE_GUARD_FLAG, HAVE_EXIT_FLAG,
    MEASURED_SUM_AVG
  }

  /** Column headers joined together with the given delimiter. */
  static String columnHeadersDelimitedBy(String delimiter) {
    return Arrays.stream(Column.values()).map(c -> c.toString().toLowerCase())
        .collect(Collectors.joining(delimiter));
  }

  /** Date. */
  LocalDate validAfterDate;

  /** Nickname of the authority generating votes, or <code>null</code> in case
   * of consensuses. */
  String nickname;

  /** Whether contained relays all have the "Guard" flag. */
  boolean haveGuardFlag;

  /** Whether contained relays all have the "Exit" flag. */
  boolean haveExitFlag;

  /** Mean value of total measured bandwidths of all relays over the day. */
  Long measuredSumAvg;

  /** Format all fields in a single output line for inclusion in a CSV
   * file. */
  @Override
  public String toString() {
    return String.format("%s,%s,%s,%s,%d", validAfterDate,
        null == nickname ? "" : nickname, haveGuardFlag ? "t" : "f",
        haveExitFlag ? "t" : "f", measuredSumAvg);
  }
}

