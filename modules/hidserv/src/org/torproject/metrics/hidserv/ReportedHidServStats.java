/* Copyright 2016--2017 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.hidserv;

/* Hidden-service statistics reported by a single relay covering a single
 * statistics interval of usually 24 hours.  These statistics are reported
 * by the relay in the "hidserv-" lines of its extra-info descriptor. */
public class ReportedHidServStats implements Document {

  /* Relay fingerprint consisting of 40 upper-case hex characters. */
  private String fingerprint;

  public String getFingerprint() {
    return this.fingerprint;
  }

  /* Hidden-service statistics end timestamp in milliseconds. */
  private long statsEndMillis;

  public long getStatsEndMillis() {
    return this.statsEndMillis;
  }

  /* Statistics interval length in seconds. */
  private long statsIntervalSeconds;

  public void setStatsIntervalSeconds(long statsIntervalSeconds) {
    this.statsIntervalSeconds = statsIntervalSeconds;
  }

  public long getStatsIntervalSeconds() {
    return this.statsIntervalSeconds;
  }

  /* Number of relayed cells on rendezvous circuits as reported by the
   * relay and adjusted by rounding to the nearest right side of a bin and
   * subtracting half of the bin size. */
  private long rendRelayedCells;

  public void setRendRelayedCells(long rendRelayedCells) {
    this.rendRelayedCells = rendRelayedCells;
  }

  public long getRendRelayedCells() {
    return this.rendRelayedCells;
  }

  /* Number of distinct .onion addresses as reported by the relay and
   * adjusted by rounding to the nearest right side of a bin and
   * subtracting half of the bin size. */
  private long dirOnionsSeen;

  public void setDirOnionsSeen(long dirOnionsSeen) {
    this.dirOnionsSeen = dirOnionsSeen;
  }

  public long getDirOnionsSeen() {
    return this.dirOnionsSeen;
  }

  /* Instantiate a new stats object using fingerprint and stats interval
   * end which together uniquely identify the object. */
  public ReportedHidServStats(String fingerprint, long statsEndMillis) {
    this.fingerprint = fingerprint;
    this.statsEndMillis = statsEndMillis;
  }

  /* Return whether this object contains the same fingerprint and stats
   * interval end as the passed object. */
  @Override
  public boolean equals(Object otherObject) {
    if (!(otherObject instanceof ReportedHidServStats)) {
      return false;
    }
    ReportedHidServStats other = (ReportedHidServStats) otherObject;
    return this.fingerprint.equals(other.fingerprint)
        && this.statsEndMillis == other.statsEndMillis;
  }

  /* Return a (hopefully unique) hash code based on this object's
   * fingerprint and stats interval end. */
  @Override
  public int hashCode() {
    return this.fingerprint.hashCode() + (int) this.statsEndMillis;
  }

  /* Return a string representation of this object, consisting of
   * fingerprint and the concatenation of all other attributes. */
  @Override
  public String[] format() {
    String first = this.fingerprint;
    String second = String.format("%s,%d,%d,%d",
        DateTimeHelper.format(this.statsEndMillis),
        this.statsIntervalSeconds, this.rendRelayedCells,
        this.dirOnionsSeen);
    return new String[] { first, second };
  }

  /* Instantiate an empty stats object that will be initialized more by
   * the parse method. */
  ReportedHidServStats() {
  }

  /* Initialize this stats object using the two provided strings that have
   * been produced by the format method earlier.  Return whether this
   * operation was successful. */
  @Override
  public boolean parse(String[] formattedStrings) {
    if (formattedStrings.length != 2) {
      System.err.printf("Invalid number of formatted strings.  "
          + "Skipping.%n", formattedStrings.length);
      return false;
    }
    String[] secondParts = formattedStrings[1].split(",", 4);
    if (secondParts.length != 4) {
      return false;
    }
    long statsEndMillis = DateTimeHelper.parse(secondParts[0]);
    if (statsEndMillis == DateTimeHelper.NO_TIME_AVAILABLE) {
      return false;
    }
    long statsIntervalSeconds = -1L;
    long rendRelayedCells = -1L;
    long dirOnionsSeen = -1L;
    try {
      statsIntervalSeconds = Long.parseLong(secondParts[1]);
      rendRelayedCells = Long.parseLong(secondParts[2]);
      dirOnionsSeen = Long.parseLong(secondParts[3]);
    } catch (NumberFormatException e) {
      return false;
    }
    this.fingerprint = formattedStrings[0];
    this.statsEndMillis = statsEndMillis;
    this.statsIntervalSeconds = statsIntervalSeconds;
    this.rendRelayedCells = rendRelayedCells;
    this.dirOnionsSeen = dirOnionsSeen;
    return true;
  }
}

