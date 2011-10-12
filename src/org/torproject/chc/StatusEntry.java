/* Copyright 2011 The Tor Project
 * See LICENSE for licensing information */
package org.torproject.chc;

import java.util.*;

/* Contains the parsed data from a network status entry contained in a
 * network status consensus or vote. */
public class StatusEntry implements Comparable<StatusEntry> {

  /* Helper methods to implement the Comparable interface; StatusEntry
   * instances are compared by fingerprint. */
  public int compareTo(StatusEntry o) {
    return this.fingerprint.compareTo(o.fingerprint);
  }
  public boolean equals(Object o) {
    return (o instanceof StatusEntry &&
        this.fingerprint.equals(((StatusEntry) o).fingerprint));
  }

  /* Relay fingerprint. */
  private String fingerprint;
  public void setFingerprint(String fingerprint) {
    this.fingerprint = fingerprint;
  }
  public String getFingerprint() {
    return this.fingerprint;
  }

  /* Relay nickname. */
  private String nickname;
  public void setNickname(String nickname) {
    this.nickname = nickname;
  }
  public String getNickname() {
    return this.nickname;
  }

  /* Relay flags. */
  private SortedSet<String> flags;
  public void setFlags(SortedSet<String> flags) {
    this.flags = flags;
  }
  public SortedSet<String> getFlags() {
    return this.flags;
  }
}

