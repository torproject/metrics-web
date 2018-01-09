/* Copyright 2017--2018 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.stats.ipv6servers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Data object holding all relevant parts parsed from a (relay or bridge)
 * network status. */
class Ipv6NetworkStatus {

  /** Whether this is a relay network status as opposed to a bridge network
   * status. */
  boolean isRelay;

  /** Valid-after time in case of relay network status and published time in
   * case of bridge network status. */
  LocalDateTime timestamp;

  /** Number of relays or bridges with the Running flag. */
  int running = 0;

  /** Contained status entries. */
  List<Entry> entries = new ArrayList<>();

  /** Data object holding all relevant parts from a network status entry. */
  static class Entry {

    /** Hex-encoded SHA-1 server descriptor digest. */
    String digest;

    /** Whether this relay has the Guard flag; false for bridges. */
    boolean guard;

    /** Whether this relay has the Exit flag (and not the BadExit flag at the
     * same time); false for bridges. */
    boolean exit;

    /** Whether the directory authorities include an IPv6 address in this
     * entry's "a" line, confirming the relay's reachability via IPv6; false for
     * bridges. */
    boolean reachable;
  }
}

