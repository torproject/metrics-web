/* Copyright 2016--2020 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.stats.hidserv;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Extrapolated network totals of hidden-service statistics reported by a
 * single relay.  Extrapolated values are based on reported statistics and
 * computed network fractions in the statistics interval. */
public class ExtrapolatedHidServStats implements Document {

  private static Logger log
      = LoggerFactory.getLogger(ExtrapolatedHidServStats.class);

  /** Date of statistics interval end in milliseconds. */
  private long statsDateMillis;

  public long getStatsDateMillis() {
    return this.statsDateMillis;
  }

  /** Relay fingerprint consisting of 40 upper-case hex characters. */
  private String fingerprint;

  public String getFingerprint() {
    return this.fingerprint;
  }

  /** Extrapolated number of cells on rendezvous circuits in the
   * network. */
  private double extrapolatedRendRelayedCells;

  public void setExtrapolatedRendRelayedCells(
      double extrapolatedRendRelayedCells) {
    this.extrapolatedRendRelayedCells = extrapolatedRendRelayedCells;
  }

  public double getExtrapolatedRendRelayedCells() {
    return this.extrapolatedRendRelayedCells;
  }

  /** Computed fraction of observed cells on rendezvous circuits in the
   * network, used to weight this relay's extrapolated network total in
   * the aggregation step. */
  private double fractionRendRelayedCells;

  public void setFractionRendRelayedCells(
      double fractionRendRelayedCells) {
    this.fractionRendRelayedCells = fractionRendRelayedCells;
  }

  public double getFractionRendRelayedCells() {
    return this.fractionRendRelayedCells;
  }

  /** Extrapolated number of .onions in the network. */
  private double extrapolatedDirOnionsSeen;

  public void setExtrapolatedDirOnionsSeen(
      double extrapolatedDirOnionsSeen) {
    this.extrapolatedDirOnionsSeen = extrapolatedDirOnionsSeen;
  }

  public double getExtrapolatedDirOnionsSeen() {
    return this.extrapolatedDirOnionsSeen;
  }

  /** Computed fraction of observed .onions in the network, used to weight
   * this relay's extrapolated network total in the aggregation step. */
  private double fractionDirOnionsSeen;

  public void setFractionDirOnionsSeen(double fractionDirOnionsSeen) {
    this.fractionDirOnionsSeen = fractionDirOnionsSeen;
  }

  public double getFractionDirOnionsSeen() {
    return this.fractionDirOnionsSeen;
  }

  /** Instantiates a new stats object using fingerprint and statistics
   * interval end date which together uniquely identify the object. */
  public ExtrapolatedHidServStats(long statsDateMillis,
      String fingerprint) {
    this.statsDateMillis = statsDateMillis;
    this.fingerprint = fingerprint;
  }

  /** Returns whether this object contains the same fingerprint and
   * statistics interval end date as the passed object. */
  @Override
  public boolean equals(Object otherObject) {
    if (!(otherObject instanceof ExtrapolatedHidServStats)) {
      return false;
    }
    ExtrapolatedHidServStats other =
        (ExtrapolatedHidServStats) otherObject;
    return this.fingerprint.equals(other.fingerprint)
        && this.statsDateMillis == other.statsDateMillis;
  }

  /** Returns a (hopefully unique) hash code based on this object's
   * fingerprint and statistics interval end date. */
  @Override
  public int hashCode() {
    return this.fingerprint.hashCode() + (int) this.statsDateMillis;
  }

  /** Returns a string representation of this object, consisting of the
   * statistics interval end date and the concatenation of all other
   * attributes. */
  @Override
  public String[] format() {
    String first = DateTimeHelper.format(this.statsDateMillis,
        DateTimeHelper.ISO_DATE_FORMAT);
    String second = this.fingerprint
        + (this.fractionRendRelayedCells == 0.0 ? ",,"
        : String.format(",%.0f,%f",
        this.extrapolatedRendRelayedCells, this.fractionRendRelayedCells))
        + (this.fractionDirOnionsSeen == 0.0 ? ",,"
        : String.format(",%.0f,%f",
        this.extrapolatedDirOnionsSeen, this.fractionDirOnionsSeen));
    return new String[] { first, second };
  }

  /** Instantiates an empty stats object that will be initialized more by
   * the parse method.
   *
   * <p>Invoked by {@link DocumentStore#retrieve} via reflection.</p> */
  ExtrapolatedHidServStats() {}

  /** Initializes this stats object using the two provided strings that
   * have been produced by the format method earlier and returns whether
   * this operation was successful. */
  @Override
  public boolean parse(String[] formattedStrings) {
    if (formattedStrings.length != 2) {
      log.warn("Invalid number of formatted strings: {}. Skipping.",
          formattedStrings.length);
      return false;
    }
    long statsDateMillis = DateTimeHelper.parse(formattedStrings[0],
        DateTimeHelper.ISO_DATE_FORMAT);
    String[] secondParts = formattedStrings[1].split(",", 5);
    if (secondParts.length != 5) {
      log.warn("Invalid number of comma-separated values: {}. Skipping.",
          secondParts.length);
      return false;
    }
    String fingerprint = secondParts[0];
    double extrapolatedRendRelayedCells;
    double fractionRendRelayedCells;
    double extrapolatedDirOnionsSeen;
    double fractionDirOnionsSeen;
    try {
      extrapolatedRendRelayedCells = secondParts[1].equals("") ? 0.0
          : Double.parseDouble(secondParts[1]);
      fractionRendRelayedCells = secondParts[2].equals("") ? 0.0
          : Double.parseDouble(secondParts[2]);
      extrapolatedDirOnionsSeen = secondParts[3].equals("") ? 0.0
          : Double.parseDouble(secondParts[3]);
      fractionDirOnionsSeen = secondParts[4].equals("") ? 0.0
          : Double.parseDouble(secondParts[4]);
    } catch (NumberFormatException e) {
      return false;
    }
    this.statsDateMillis = statsDateMillis;
    this.fingerprint = fingerprint;
    this.extrapolatedRendRelayedCells = extrapolatedRendRelayedCells;
    this.fractionRendRelayedCells = fractionRendRelayedCells;
    this.extrapolatedDirOnionsSeen = extrapolatedDirOnionsSeen;
    this.fractionDirOnionsSeen = fractionDirOnionsSeen;
    return true;
  }
}

