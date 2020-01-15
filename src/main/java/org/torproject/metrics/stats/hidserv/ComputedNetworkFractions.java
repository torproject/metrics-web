/* Copyright 2016--2020 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.stats.hidserv;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** Computed fraction of hidden-service activity that a single relay is
 * assumed to observe in the network.  These fractions are computed from
 * status entries and bandwidth weights in a network status consensus. */
public class ComputedNetworkFractions implements Document {

  private static Logger log
      = LoggerFactory.getLogger(ComputedNetworkFractions.class);

  /** Relay fingerprint consisting of 40 upper-case hex characters. */
  private String fingerprint;

  public String getFingerprint() {
    return this.fingerprint;
  }

  /** Valid-after timestamp of the consensus in milliseconds. */
  private long validAfterMillis;

  public long getValidAfterMillis() {
    return this.validAfterMillis;
  }

  /** Fraction of cells on rendezvous circuits that this relay is assumed
   * to observe in the network. */
  private double fractionRendRelayedCells;

  public void setFractionRendRelayedCells(
      double fractionRendRelayedCells) {
    this.fractionRendRelayedCells = fractionRendRelayedCells;
  }

  public double getFractionRendRelayedCells() {
    return this.fractionRendRelayedCells;
  }

  /** Fraction of descriptors that this relay is assumed to observe in the
   * network.  This is calculated as the fraction of descriptors
   * identifiers that this relay was responsible for, divided by 3,
   * because each descriptor that is published to this directory is also
   * published to two other directories. */
  private double fractionDirOnionsSeen;

  public void setFractionDirOnionsSeen(double fractionDirOnionsSeen) {
    this.fractionDirOnionsSeen = fractionDirOnionsSeen;
  }

  public double getFractionDirOnionsSeen() {
    return this.fractionDirOnionsSeen;
  }

  /** Instantiates a new fractions object using fingerprint and consensus
   * valid-after time which together uniquely identify the object. */
  public ComputedNetworkFractions(String fingerprint,
      long validAfterMillis) {
    this.fingerprint = fingerprint;
    this.validAfterMillis = validAfterMillis;
  }

  /** Returns whether this object contains the same fingerprint and
   * consensus valid-after time as the passed object. */
  @Override
  public boolean equals(Object otherObject) {
    if (!(otherObject instanceof ComputedNetworkFractions)) {
      return false;
    }
    ComputedNetworkFractions other =
        (ComputedNetworkFractions) otherObject;
    return this.fingerprint.equals(other.fingerprint)
        && this.validAfterMillis == other.validAfterMillis;
  }

  /** Returns a (hopefully unique) hash code based on this object's
   * fingerprint and consensus valid-after time. */
  @Override
  public int hashCode() {
    return this.fingerprint.hashCode()
        + (int) this.validAfterMillis;
  }

  private static Map<Long, String> previouslyFormattedDates =
      Collections.synchronizedMap(new HashMap<>());

  /** Returns a string representation of this object, consisting of two
   * strings: the first string contains fingerprint and valid-after date,
   * the second string contains the concatenation of all other
   * attributes. */
  @Override
  public String[] format() {
    long validAfterDateMillis = (this.validAfterMillis
        / DateTimeHelper.ONE_DAY) * DateTimeHelper.ONE_DAY;
    String validAfterDate;
    if (previouslyFormattedDates.containsKey(validAfterDateMillis)) {
      validAfterDate = previouslyFormattedDates.get(validAfterDateMillis);
    } else {
      validAfterDate = DateTimeHelper.format(validAfterDateMillis,
          DateTimeHelper.ISO_DATE_FORMAT);
      previouslyFormattedDates.put(validAfterDateMillis, validAfterDate);
    }
    long validAfterHourMillis = this.validAfterMillis
        % DateTimeHelper.ONE_DAY;
    String validAfterHour = String.format("%02d",
        validAfterHourMillis / DateTimeHelper.ONE_HOUR);
    String first = String.format("%s,%s", this.fingerprint,
        validAfterDate);
    String second = validAfterHour
        + (this.fractionRendRelayedCells == 0.0 ? ","
            : String.format(Locale.US, ",%f", this.fractionRendRelayedCells))
        + (this.fractionDirOnionsSeen == 0.0 ? ","
            : String.format(Locale.US, ",%f", this.fractionDirOnionsSeen));
    return new String[] { first, second };
  }

  private static Map<String, Long> previouslyParsedDates =
      Collections.synchronizedMap(new HashMap<>());

  /** Instantiates an empty fractions object that will be initialized more
   * by the parse method.
   *
   * <p>Invoked by {@link DocumentStore#retrieve} via reflection.</p> */
  ComputedNetworkFractions() {}

  /** Initializes this fractions object using the two provided strings
   * that have been produced by the format method earlier and returns
   * whether this operation was successful. */
  @Override
  public boolean parse(String[] formattedStrings) {
    if (formattedStrings.length != 2) {
      log.warn("Invalid number of formatted strings. Skipping.");
      return false;
    }
    String[] firstParts = formattedStrings[0].split(",", 2);
    if (firstParts.length != 2) {
      log.warn("Invalid number of comma-separated values. Skipping.");
      return false;
    }
    String fingerprint = firstParts[0];
    String[] secondParts = formattedStrings[1].split(",", 3);
    if (secondParts.length != 3) {
      log.warn("Invalid number of comma-separated values. Skipping.");
      return false;
    }
    String validAfterDate = firstParts[1];
    String validAfterHour = secondParts[0];
    long validAfterDateMillis;
    if (previouslyParsedDates.containsKey(validAfterDate)) {
      validAfterDateMillis = previouslyParsedDates.get(validAfterDate);
    } else {
      validAfterDateMillis = DateTimeHelper.parse(validAfterDate,
          DateTimeHelper.ISO_DATE_FORMAT);
      previouslyParsedDates.put(validAfterDate, validAfterDateMillis);
    }
    long validAfterTimeMillis = Long.parseLong(validAfterHour)
        * DateTimeHelper.ONE_HOUR;
    if (validAfterDateMillis == DateTimeHelper.NO_TIME_AVAILABLE
        || validAfterTimeMillis < 0L
        || validAfterTimeMillis >= DateTimeHelper.ONE_DAY) {
      log.warn("Invalid date/hour format. Skipping.");
      return false;
    }
    long validAfterMillis = validAfterDateMillis + validAfterTimeMillis;
    try {
      this.fingerprint = fingerprint;
      this.validAfterMillis = validAfterMillis;
      this.fractionRendRelayedCells = secondParts[1].equals("")
          ? 0.0 : Double.parseDouble(secondParts[1]);
      this.fractionDirOnionsSeen = secondParts[2].equals("")
          ? 0.0 : Double.parseDouble(secondParts[2]);
      return true;
    } catch (NumberFormatException e) {
      log.warn("Invalid number format. Skipping.");
      return false;
    }
  }
}

