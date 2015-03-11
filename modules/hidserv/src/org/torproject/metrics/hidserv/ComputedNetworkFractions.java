package org.torproject.metrics.hidserv;

/* Computed fraction of hidden-service activity that a single relay is
 * assumed to observe in the network.  These fractions are computed from
 * status entries and bandwidth weights in a network status consensus. */
public class ComputedNetworkFractions implements Document {

  /* Relay fingerprint consisting of 40 upper-case hex characters. */
  private String fingerprint;
  public String getFingerprint() {
    return this.fingerprint;
  }

  /* Valid-after timestamp of the consensus in milliseconds. */
  private long validAfterMillis;
  public long getValidAfterMillis() {
    return this.validAfterMillis;
  }

  /* Fraction of cells on rendezvous circuits that this relay is assumed
   * to observe in the network. */
  private double fractionRendRelayedCells;
  public void setFractionRendRelayedCells(
      double fractionRendRelayedCells) {
    this.fractionRendRelayedCells = fractionRendRelayedCells;
  }
  public double getFractionRendRelayedCells() {
    return this.fractionRendRelayedCells;
  }

  /* Fraction of descriptors that this relay is assumed to observe in the
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

  /* Instantiate a new fractions object using fingerprint and consensus
   * valid-after time which together uniquely identify the object. */
  public ComputedNetworkFractions(String fingerprint,
      long validAfterMillis) {
    this.fingerprint = fingerprint;
    this.validAfterMillis = validAfterMillis;
  }

  /* Return whether this object contains the same fingerprint and
   * consensus valid-after time as the passed object. */
  @Override
  public boolean equals(Object otherObject) {
    if (!(otherObject instanceof ComputedNetworkFractions)) {
      return false;
    }
    ComputedNetworkFractions other =
        (ComputedNetworkFractions) otherObject;
    return this.fingerprint.equals(other.fingerprint) &&
        this.validAfterMillis == other.validAfterMillis;
  }

  /* Return a (hopefully unique) hash code based on this object's
   * fingerprint and consensus valid-after time. */
  @Override
  public int hashCode() {
    return this.fingerprint.hashCode() +
        (int) this.validAfterMillis;
  }

  /* Return a string representation of this object, consisting of two
   * strings: the first string contains fingerprint and valid-after date,
   * the second string contains the concatenation of all other
   * attributes. */
  @Override
  public String[] format() {
    String first = String.format("%s,%s", this.fingerprint,
        DateTimeHelper.format(this.validAfterMillis,
        DateTimeHelper.ISO_DATE_FORMAT));
    String second = DateTimeHelper.format(this.validAfterMillis,
        DateTimeHelper.ISO_HOUR_FORMAT)
        + (this.fractionRendRelayedCells == 0.0 ? ","
            : String.format(",%f", this.fractionRendRelayedCells))
        + (this.fractionDirOnionsSeen == 0.0 ? ","
            : String.format(",%f", this.fractionDirOnionsSeen));
    return new String[] { first, second };
  }

  /* Instantiate an empty fractions object that will be initialized more
   * by the parse method. */
  ComputedNetworkFractions() {
  }

  /* Initialize this fractions object using the two provided strings that
   * have been produced by the format method earlier.  Return whether this
   * operation was successful. */
  @Override
  public boolean parse(String[] formattedStrings) {
    if (formattedStrings.length != 2) {
      System.err.printf("Invalid number of formatted strings.  "
          + "Skipping.%n", formattedStrings.length);
      return false;
    }
    String[] firstParts = formattedStrings[0].split(",", 2);
    if (firstParts.length != 2) {
      System.err.printf("Invalid number of comma-separated values.  "
          + "Skipping.%n");
      return false;
    }
    String fingerprint = firstParts[0];
    String[] secondParts = formattedStrings[1].split(",", 3);
    if (secondParts.length != 3) {
      System.err.printf("Invalid number of comma-separated values.  "
          + "Skipping.%n");
      return false;
    }
    long validAfterMillis = DateTimeHelper.parse(firstParts[1] + " "
        + secondParts[0], DateTimeHelper.ISO_DATE_HOUR_FORMAT);
    if (validAfterMillis == DateTimeHelper.NO_TIME_AVAILABLE) {
      System.err.printf("Invalid date/hour format.  Skipping.%n");
      return false;
    }
    try {
      double fractionRendRelayedCells = secondParts[1].equals("")
          ? 0.0 : Double.parseDouble(secondParts[1]);
      double fractionDirOnionsSeen = secondParts[2].equals("")
          ? 0.0 : Double.parseDouble(secondParts[2]);
      this.fingerprint = fingerprint;
      this.validAfterMillis = validAfterMillis;
      this.fractionRendRelayedCells = fractionRendRelayedCells;
      this.fractionDirOnionsSeen = fractionDirOnionsSeen;
      return true;
    } catch (NumberFormatException e) {
      System.err.printf("Invalid number format.  Skipping.%n");
      return false;
    }
  }
}

