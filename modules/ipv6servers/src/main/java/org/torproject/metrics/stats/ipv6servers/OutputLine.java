/* Copyright 2017 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.stats.ipv6servers;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Data object holding all parts of an output line. */
class OutputLine {

  /** Column names used in the database and in the first line of the output
   * file. */
  enum Column {
    VALID_AFTER_DATE, SERVER, GUARD_RELAY, EXIT_RELAY, ANNOUNCED_IPV6,
    EXITING_IPV6_RELAY, REACHABLE_IPV6_RELAY, SERVER_COUNT_SUM_AVG,
    ADVERTISED_BANDWIDTH_BYTES_SUM_AVG
  }

  /** Column headers joined together with the given delimiter. */
  static String getColumnHeaders(String delimiter) {
    List<String> columnHeaders = new ArrayList<>();
    for (Column column : Column.values()) {
      columnHeaders.add(column.toString());
    }
    return String.join(delimiter, columnHeaders).toLowerCase();
  }

  /** Date. */
  LocalDate date;

  /** Server type, which can be "relay" or "bridge". */
  String server;

  /** Whether relays had the Guard flag ("t") or not ("f"). */
  String guard;

  /** Whether relays had the Exit flag ("t") or not ("f"). */
  String exit;

  /** Whether relays or bridges have announced an IPv6 address in their server
   * descriptor ("t") or not ("f"). */
  String announced;

  /** Whether relays have announced a non-reject-all IPv6 exit policy in their
   * server descriptor ("t") or not ("f"). */
  String exiting;

  /** Whether the directory authorities have confirmed IPv6 OR reachability by
   * including an "a" line for a relay containing an IPv6 address. */
  String reachable;

  /** Number of relays or bridges matching the previous criteria. */
  long count;

  /** Total advertised bandwidth of all relays matching the previous
   * criteria. */
  Long advertisedBandwidth;

  /** Format all fields in a single output line for inclusion in a CSV
   * file. */
  @Override
  public String toString() {
    return String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s",
        date, server, emptyNull(guard), emptyNull(exit), emptyNull(announced),
        emptyNull(exiting), emptyNull(reachable), emptyNull(count),
        emptyNull(advertisedBandwidth));
  }

  private static String emptyNull(Object text) {
    return null == text ? "" : text.toString();
  }
}

