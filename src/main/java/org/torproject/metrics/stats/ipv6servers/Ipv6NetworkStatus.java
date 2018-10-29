/* Copyright 2017--2018 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.stats.ipv6servers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

/** Data object holding all relevant parts parsed from a (relay or bridge)
 * network status. */
class Ipv6NetworkStatus {

  /** Whether this is a relay network status as opposed to a bridge network
   * status. */
  boolean isRelay;

  /** Valid-after time in case of relay network status and published time in
   * case of bridge network status. */
  LocalDateTime timestamp;

  /** List of recommended (server) versions, or null in case of bridge network
   * status. */
  List<String> recommendedVersions;

  /** Number of relays or bridges with the Running flag. */
  int running = 0;

  /** Total consensus weight of all status entries, or null if the status does
   * not contain entries with consensus weights. */
  Float totalConsensusWeight;

  /** Total guard-weighted consensus weight of all status entries, or null if
   * the status either does not contain entries with consensus weight or no Wxx
   * values. */
  Float totalGuardWeight;

  /** Total middle-weighted consensus weight of all status entries, or null if
   * the status either does not contain entries with consensus weight or no Wxx
   * values. */
  Float totalMiddleWeight;

  /** Total exit-weighted consensus weight of all status entries, or null if
   * the status either does not contain entries with consensus weight or no Wxx
   * values. */
  Float totalExitWeight;

  /** Contained status entries. */
  List<Entry> entries = new ArrayList<>();

  /** Data object holding all relevant parts from a network status entry. */
  static class Entry {

    /** Hex-encoded SHA-1 server descriptor digest. */
    String digest;

    /** Relay flags assigned to this relay or bridge. */
    SortedSet<String> flags;

    /** Whether the directory authorities include an IPv6 address in this
     * entry's "a" line, confirming the relay's reachability via IPv6; false for
     * bridges. */
    boolean reachable;

    /** Consensus weight of this entry, or null if the entry does not have a "w"
     * line. */
    Float consensusWeight;

    /** Guard-weighted consensus weight of this entry, or null if either the
     * entry does not have a "w" line or the consensus has no Wxx values. */
    Float guardWeight;

    /** Middle-weighted consensus weight of this entry, or null if either the
     * entry does not have a "w" line or the consensus has no Wxx values. */
    Float middleWeight;

    /** Exit-weighted consensus weight of this entry, or null if either the
     * entry does not have a "w" line or the consensus has no Wxx values. */
    Float exitWeight;
  }
}

