package org.torproject.metrics.disagreement;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.TreeMap;

import org.torproject.descriptor.Descriptor;
import org.torproject.descriptor.DescriptorFile;
import org.torproject.descriptor.DescriptorReader;
import org.torproject.descriptor.DescriptorSourceFactory;
import org.torproject.descriptor.NetworkStatusEntry;
import org.torproject.descriptor.RelayNetworkStatusVote;

/* Read all relay network status votes from the in/ subdirectory with a
 * valid-after time of 12:00:00, extract attributes like relay flags or
 * bandwidth measurements that the directory authorities assigned to
 * relays, and output aggregate statistics on disagreement among the
 * directory authorities.
 *
 * When initializing from descriptor archives, put tarballs in the in/
 * subdirectory, run this code, and then move the tarballs away.
 * Otherwise, tarballs will be re-processed in each subsequent execution.
 *
 * Recent descriptors can stay in the in/ subdirectory and be re-processed
 * in each execution. */
public class Main {

  public static void main(String[] args) throws Exception {
    new Main().run();
  }

  public void run() throws Exception {
    readResults();
    readDescriptors();
    aggregate();
    writeResults();
  }

  /* We're processing a lot of strings, including authority identities,
   * relay fingerprints, and attributes like relay flags.  It would be
   * wasteful to store more than one instance of these strings in memory.
   * We also want to store them as part of long integers below.
   *
   * That's why we're resolving all strings to integers and keeping maps
   * from string to integers.  In case of authority identities and relay
   * fingerprints we don't need to resolve integers back to strings, but
   * in case of attributes we need to put attribute strings into the
   * output file, so we're also keeping a list of attribute strings in
   * insertion order. */
  private Map<String, Integer> authorityIndexes =
      new HashMap<String, Integer>();
  private Map<String, Integer> fingerprintIndexes =
      new HashMap<String, Integer>();
  private Map<String, Integer> attributeIndexes =
      new HashMap<String, Integer>();
  private List<String> attributeStrings = new ArrayList<String>();

  public Main() {

    /* Initialize maps from strings to integers and back by adding the
     * empty string as 0-th element.  This is necessary, because we want
     * to be able to treat 0 as special case below.  Once we add non-empty
     * strings to maps, they'll be indexed starting at 1. */
    this.getAuthorityIndexForString("");
    this.getFingerprintIndexForString("");
    this.getAttributeIndexForString("");
  }

  /* Resolve the given authority string to its authority index, possibly
   * after adding it to the mapping if it wasn't contained before. */
  protected int getAuthorityIndexForString(String authorityString) {
    return putToMapsAndReturnIndex(this.authorityIndexes, null,
        authorityString);
  }

  /* Resolve the given fingerprint string to its fingerprint index,
   * possibly after adding it to the mapping if it wasn't contained
   * before. */
  protected int getFingerprintIndexForString(String fingerprintString) {
    return putToMapsAndReturnIndex(this.fingerprintIndexes, null,
        fingerprintString);
  }

  /* Resolve the given attribute string to its attribute index, possibly
   * after adding it to the mapping if it wasn't contained before. */
  protected int getAttributeIndexForString(String attributeString) {
    return putToMapsAndReturnIndex(this.attributeIndexes,
        this.attributeStrings, attributeString);
  }

  /* Resolve the given attribute index to its attribute string, or return
   * null if this attribute index doesn't exist. */
  protected String getAttributeStringForIndex(int attributeIndex) {
    if (attributeIndex < 0 || attributeIndex >= attributeStrings.size()) {
      return null;
    } else {
      return this.attributeStrings.get(attributeIndex);
    }
  }

  /* Helper method: Return an index for a given string and possibly put
   * the string into the map and corresponding list if it was not
   * contained before. */
  protected int putToMapsAndReturnIndex(
      Map<String, Integer> stringToIntMap, List<String> strings,
      String string) {
    Integer index = stringToIntMap.get(string);
    if (index == null) {
      if (strings != null) {
        strings.add(string);
      }
      stringToIntMap.put(string, stringToIntMap.size());
    }
    return stringToIntMap.get(string);
  }

  /* The following code is heavily optimized towards low memory usage,
   * because we're potentially processing a lot of data, especially when
   * initializing from descriptor archives.
   *
   * We need to be able to store weeks or even months of votes in memory,
   * which is really a lot of data.  The reason is that we need all votes
   * published at the same hour to be present when running aggregations,
   * but we cannot rely on the order of incoming votes.  That's why we
   * have to process votes in two steps: first, we extract everything we
   * need from votes and store it in memory, and second, we aggregate what
   * we have in memory and output aggregate results.
   *
   * We're going rather low-level here by converting each attribute that
   * an authority assigned to a relay at a certain vote valid-after time
   * into a single 64-bit signed long integer.  We're doing this by
   * subdividing (most of) the available bits into ranges for the
   * different parts we want to store.  As an added requirement we're
   * arranging ranges in a way that we can later process long values in
   * sorted order without keeping much state.  Ranges are:
   *  - 22 bits for the valid-after time in half hours since 1970-01-01
   *    00:00:00 (which won't overflow until year 2209),
   *  - 8 bits for the attribute index (which allows up to 253 different
   *    relay flags in addition to reserved 0, "Listed", and
   *    "Measured"),
   *  - 24 bits for the fingerprint index (which allows over 16 million
   *    different relay fingerprints in the current execution), and
   *  - 6 bits for the authority index (which allows up to 63 different
   *    authorities in addition to the reserved 0).
   *
   * If any of these numbers overflows during the execution, we'll detect
   * that, suggest to process less data at once, and exit with an error.
   * Still, it should be possible to process months of data in a single
   * execution.  For example, one set of votes published at the same
   * valid-after hour in December 2015 required to keep 420,000 long
   * values in memory, which is roughly 3.2 MiB plus list overhead. */
  protected List<Long> assignments = new ArrayList<Long>();
  protected final static int VALIDAFTER_LEN = 22, ATTRIBUTE_LEN = 8,
      FINGERPRINT_LEN = 24, AUTHORITY_LEN = 6;
  protected final static int
      VALIDAFTER_SHIFT = ATTRIBUTE_LEN + FINGERPRINT_LEN + AUTHORITY_LEN,
      ATTRIBUTE_SHIFT = FINGERPRINT_LEN + AUTHORITY_LEN,
      FINGERPRINT_SHIFT = AUTHORITY_LEN,
      AUTHORITY_SHIFT = 0;

  /* Define some constants for timestamp math. */
  protected final static long HALF_HOUR = 30L * 60L * 1000L,
      ONE_HOUR = 2L * HALF_HOUR, HALF_DAY = 12L * ONE_HOUR,
      ONE_DAY = 2L * HALF_DAY;

  /* Convert the given valid-after time in milliseconds, attribute index,
   * fingerprint index, and authority index to a long integer following
   * the conversion rules stated above.  Return -1 in case of
   * overflows. */
  protected static long convertToLongValue(long validAfterMillis,
      int attributeIndex, int fingerprintIndex, int authorityIndex) {
    long validAfterHalfHours = validAfterMillis / HALF_HOUR;
    if (validAfterHalfHours < 0L ||
        validAfterHalfHours >= (1L << VALIDAFTER_LEN)) {
      return -1;
    }
    if (attributeIndex < 0 || attributeIndex >= (1 << ATTRIBUTE_LEN)) {
      return -1;
    }
    if (fingerprintIndex < 0 ||
        fingerprintIndex >= (1 << FINGERPRINT_LEN)) {
      return -1;
    }
    if (authorityIndex < 0 || authorityIndex >= (1 << AUTHORITY_LEN)) {
      return -1;
    }
    long longValue = (validAfterHalfHours << VALIDAFTER_SHIFT)
        + ((long) attributeIndex << ATTRIBUTE_SHIFT)
        + ((long) fingerprintIndex << FINGERPRINT_SHIFT)
        + ((long) authorityIndex << AUTHORITY_SHIFT);
    return longValue;
  }

  /* Extract the valid-after time in milliseconds from the given long
   * integer value. */
  protected static long extractValidAfterMillisFromLongValue(
      long longValue) {
    return (longValue >> VALIDAFTER_SHIFT) * HALF_HOUR;
  }

  /* Extract the attribute index from the given long integer value. */
  protected static int extractAttributeIndexFromLongValue(
      long longValue) {
    return (int) ((longValue >> ATTRIBUTE_SHIFT) % (1 << ATTRIBUTE_LEN));
  }

  /* Extract the fingerprint index from the given long integer value. */
  protected static int extractFingerprintIndexFromLongValue(
      long longValue) {
    return (int) ((longValue >> FINGERPRINT_SHIFT) %
        (1 << FINGERPRINT_LEN));
  }

  /* Extract the authority index from the given long integer value. */
  protected static int extractAuthorityIndexFromLongValue(
      long longValue) {
    return (int) ((longValue >> AUTHORITY_SHIFT) % (1 << AUTHORITY_LEN));
  }

  /* Keep all aggregated results in memory, so that we easily merge new
   * results obtained in the current execution.
   *
   * Another reason for keeping all results in memory is that we may have
   * processed some votes from a given valid-after time before, and now we
   * need to decide whether we keep old results or replace them with new
   * results.  We decide this by using results that are based on more
   * votes, which could be old or new results.
   *
   * Map keys are valid-after times formatted as strings, map values are
   * sorted lists of all subsequent columns starting with that valid-after
   * time. */
  protected SortedMap<String, List<String>> results =
      new TreeMap<String, List<String>>();

  /* Store all results in this .csv file. */
  protected File resultsFile = new File("stats/disagreement.csv");

  /* Use the following .csv header line for results. */
  protected String resultsHeaderLine =
      "validafter,attribute,votes,required,max,relays";

  /* Read results from the previous execution to memory and do some minor
   * validation of the file format while doing so.  Return immediately
   * without error if the file does not exist (yet). */
  private void readResults() throws Exception {
    if (!this.resultsFile.exists()) {
      return;
    }
    LineNumberReader lnr = new LineNumberReader(new BufferedReader(
        new FileReader(this.resultsFile)));
    String line;
    if ((line = lnr.readLine()) == null ||
        !line.equals(this.resultsHeaderLine)) {
      lnr.close();
      throw new IOException("Unexpected line " + lnr.getLineNumber()
          + " in " + this.resultsFile + ".");
    }
    while ((line = lnr.readLine()) != null) {
      if (!line.contains(",")) {
        lnr.close();
        throw new IOException("Cannot parse invalid line "
            + lnr.getLineNumber() + " in " + this.resultsFile + ".");
      }
      int indexOfFirstComma = line.indexOf(",");
      String validafter = line.substring(0, indexOfFirstComma);
      String otherColumns = line.substring(indexOfFirstComma);
      if (!this.results.containsKey(validafter)) {
        this.results.put(validafter, new ArrayList<String>());
      }
      this.results.get(validafter).add(otherColumns);
    }
    lnr.close();
  }

  /* Write results to the results file.  More precisely, write them to a
   * temporary file and rename it to the target file to avoid failing half
   * way through and losing the previous results file. */
  private void writeResults() throws Exception {
    this.resultsFile.getParentFile().mkdirs();
    File resultsTmpFile = new File(this.resultsFile + ".tmp");
    BufferedWriter bw = new BufferedWriter(new FileWriter(
        resultsTmpFile));
    bw.write(this.resultsHeaderLine + "\n");
    for (Map.Entry<String, List<String>> e : this.results.entrySet()) {
      String validafter = e.getKey();
      for (String otherColumns : e.getValue()) {
        bw.write(validafter + otherColumns + "\n");
      }
    }
    bw.close();
    resultsTmpFile.renameTo(this.resultsFile);
  }

  /* Read relay network status votes from this directory. */
  protected File[] inDirectories = new File[] {
      new File("../../shared/in/archive/relay-descriptors/votes"),
      new File("../../shared/in/recent/relay-descriptors/votes")
  };

  /* Read relay network status votes from disk and extract all relevant
   * pieces from them. */
  protected void readDescriptors() throws Exception {
    DescriptorReader descriptorReader =
        DescriptorSourceFactory.createDescriptorReader();
    descriptorReader.setMaxDescriptorFilesInQueue(5);
    for (File inDirectory : this.inDirectories) {
      descriptorReader.addDirectory(inDirectory);
    }
    Iterator<DescriptorFile> descriptorFiles =
        descriptorReader.readDescriptors();
    while (descriptorFiles.hasNext()) {
      DescriptorFile descriptorFile = descriptorFiles.next();
      for (Descriptor descriptor : descriptorFile.getDescriptors()) {
        if ((descriptor instanceof RelayNetworkStatusVote)) {
          RelayNetworkStatusVote vote =
              (RelayNetworkStatusVote) descriptor;
          processVote(vote);
        }
      }
    }
  }

  private static final String LISTED_ATTRIBUTE = "Listed",
      MEASURED_ATTRIBUTE = "Measured";

  /* Process a single relay network status vote. */
  private void processVote(RelayNetworkStatusVote vote) throws Exception {
    long validAfterMillis = vote.getValidAfterMillis();
    if (validAfterMillis % ONE_DAY != HALF_DAY) {
      /* Only process votes with a valid-after time of 12:00:00 as a means
       * to reduce the overall amount of data. */
      return;
    }

    /* Use the authority's identity to distinguish votes. */
    String authorityIdentity = vote.getIdentity();

    /* Collect a set of all attributes that the authority assigns in this
     * vote, which includes all known flags, the general "Listed"
     * attribute for listing relays, and possibly the "Measured" attribute
     * for bandwidth-measured relays. */
    Set<String> knownAttributes = new HashSet<String>(
        vote.getKnownFlags());
    knownAttributes.add(LISTED_ATTRIBUTE);

    /* Go through all status entries in this vote and remember which
     * attributes this authority assigns to which relays. */
    for (NetworkStatusEntry entry :
        vote.getStatusEntries().values()) {

      /* Use the relay's fingerprint to distinguish relays. */
      String fingerprint = entry.getFingerprint();

      /* Compile a set of all attributes assigned to this relay, including
       * all relay flags, "Listed", and possibly "Measured". */
      Set<String> attributes = new HashSet<String>(entry.getFlags());
      attributes.add(LISTED_ATTRIBUTE);
      if (entry.getMeasured() >= 0L) {
        attributes.add(MEASURED_ATTRIBUTE);
        knownAttributes.add(MEASURED_ATTRIBUTE);
      }

      /* Remember all attributes assigned to this relay. */
      this.addAssignedAttributes(validAfterMillis, attributes,
          fingerprint, authorityIdentity);
    }

    /* Remember all attributes assigned by the authority in this vote. */
    this.addKnownAttributes(validAfterMillis, knownAttributes,
        authorityIdentity);

    /* Remember all fingerprints voted on by the authority at the given
     * valid-after time. */
    this.addKnownFingerprints(validAfterMillis,
        vote.getStatusEntries().keySet());
  }

  /* Remember all attributes assigned to a given relay by an authority at
   * a given valid-after time.  These are converted to long integers
   * with all components being non-zero. */
  protected void addAssignedAttributes(long validAfterMillis,
      Set<String> attributes, String fingerprint,
      String authorityIdentity) throws Exception {
    int fingerprintIndex = this.getFingerprintIndexForString(fingerprint);
    int authorityIndex = this.getAuthorityIndexForString(
        authorityIdentity);
    for (String attribute : attributes) {
      int attributeIndex = this.getAttributeIndexForString(attribute);
      long longValue = convertToLongValue(validAfterMillis,
          attributeIndex, fingerprintIndex, authorityIndex);
      if (longValue < 0L) {
        throw new Exception("Could not convert vote data to the internal "
            + "format.  Try processing fewer votes at once.");
      }
      this.assignments.add(longValue);
    }
  }

  /* Remember all attributes voted on by an authority at a given
   * valid-after time.  These are converted to long integers with
   * fingerprint component being zero. */
  protected void addKnownAttributes(long validAfterMillis,
      Set<String> knownAttributes, String authorityIdentity)
      throws Exception {
    int authorityIndex = this.getAuthorityIndexForString(
        authorityIdentity);
    for (String attribute : knownAttributes) {
      int attributeIndex = this.getAttributeIndexForString(attribute);
      long longValue = convertToLongValue(validAfterMillis,
          attributeIndex, 0, authorityIndex);
      if (longValue < 0L) {
        throw new Exception("Could not convert vote data to the internal "
            + "format.  Try processing fewer votes at once.");
      }
      this.assignments.add(longValue);
    }
  }

  /* Remember all fingerprints known by an authority at a given
   * valid-after time.  These are converted to long integers with
   * attribute and authority component being zero. */
  protected void addKnownFingerprints(long validAfterMillis,
      Set<String> knownFingerprints) throws Exception {
    for (String fingerprint : knownFingerprints) {
      int fingerprintIndex = this.getFingerprintIndexForString(
          fingerprint);
      long longValue = convertToLongValue(validAfterMillis, 0,
          fingerprintIndex, 0);
      if (longValue < 0L) {
        throw new Exception("Could not convert vote data to the internal "
            + "format.  Try processing fewer votes at once.");
      }
      this.assignments.add(longValue);
    }
  }

  /* Aggregate everything we extracted from votes earlier into statistics
   * on disagreement among directory authorities. */
  protected void aggregate() {

    /* Initialize a date format for formatting valid-after times. */
    DateFormat dateTimeFormat = new SimpleDateFormat(
        "yyyy-MM-dd HH:mm:ss", Locale.US);
    dateTimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

    /* Sort long integer values and append the largest possible long
     * value, so that we can process values in the following order
     * (columns are valid-after time, attribute, fingerprint, authority;
     * + stands for a value > 0, 0 stands for 0):
     *  (+, 0, +, 0): all fingerprints known at a given valid-after time
     *  (+, +, 0, +): all attributes known by a given authority
     *  (+, +, +, +): attributes assigned to relays by authority
     *  ...
     *  (MAX_VALUE): end-of-list marker */
    Collections.sort(this.assignments);
    this.assignments.add(Long.MAX_VALUE);

    /* Remember long value and some of its components from the last
     * iteration. */
    long lastLongValue = -1L, lastValidAfterMillis = -1L;
    int lastAttributeIndex = -1, lastFingerprintIndex = -1;

    /* Keep a list of all output lines for a single valid-after time. */
    List<String> outputLines = new ArrayList<String>();

    /* Keep counters for the number of fingerprints seen at a valid-after
     * time, the number of authorities voting on an attribute, and the
     * number of votes that a relay received for a given attribute. */
    int knownFingerprintsByAllAuthorities = 0,
        authoritiesVotingOnAttribute = 0, votesForAttribute = 0;

    /* Keep counters of relays receiving a given number of votes on an
     * attribute.  The number at element i is the number of relays
     * receiving i votes. */
    int[] relays = new int[(1 << AUTHORITY_LEN)];

    /* Go through all long values in ascending order. */
    for (long longValue : this.assignments) {

      /* Skip duplicate values. */
      if (lastLongValue == longValue) {
        continue;
      }

      /* If we're looking at attributes assigned to relays and just moved
       * from one non-zero fingerprint to the next, we need to wrap up
       * results for the last fingerprint before moving on. */
      int fingerprintIndex = extractFingerprintIndexFromLongValue(
          longValue);
      if (lastAttributeIndex > 0 && lastFingerprintIndex > 0 &&
          lastFingerprintIndex != fingerprintIndex) {

        /* This relay received at least one vote for the given attribute,
         * or otherwise it wouldn't be contained in the list of long
         * values.  Increment the counter for the number of votes we
         * counted for this attribute, and then reset that counter. */
        relays[0]--;
        relays[votesForAttribute]++;
        votesForAttribute = 0;
      }

      /* If we're looking at attributes and just moved from one non-zero
       * attribute to the next, we need to wrap up results for the last
       * attribute before moving on.  And if we just moved to the first
       * attribute, initialize counters. */
      int attributeIndex = extractAttributeIndexFromLongValue(longValue);
      if (lastAttributeIndex >= 0 &&
          lastAttributeIndex != attributeIndex) {

        /* If we just finished a non-zero attribute, wrap it up.
         * Determine the number of votes required for getting into the
         * consensus, which is typically the majority of votes, except for
         * the "Measured" attribute, where it's set to 3.  Format one
         * output line for each possible number of votes, from 0 to the
         * total number of authorities voting on the attribute. */
        if (lastAttributeIndex > 0) {
          String lastAttribute = this.getAttributeStringForIndex(
              lastAttributeIndex);
          int required = lastAttribute.equals(MEASURED_ATTRIBUTE) ? 3
              : (authoritiesVotingOnAttribute / 2) + 1;
          for (int i = 0; i <= authoritiesVotingOnAttribute; i++) {
            outputLines.add(String.format(",%s,%d,%d,%d,%d",
                lastAttribute, i, required, authoritiesVotingOnAttribute,
                relays[i]));
          }
        }

        /* Reset counters and initialize the bucket at index 0 with the
         * total number of fingerprints known at this valid-after time. */
        authoritiesVotingOnAttribute = 0;
        relays = new int[(1 << AUTHORITY_LEN)];
        relays[0] = knownFingerprintsByAllAuthorities;
      }

      /* If we just moved from one valid-after time to the next, we need
       * to wrap up results for the last valid-after time before moving
       * on. */
      long validAfterMillis = extractValidAfterMillisFromLongValue(
          longValue);
      if (lastValidAfterMillis >= 0L &&
          lastValidAfterMillis < validAfterMillis) {

        /* Check if results already contain lines for this valid-after
         * time.  If so, only replace them with new results lines if there
         * are more new lines than old lines.  The rationale is that more
         * lines are very likely based on more votes, and we want to
         * include as many votes as possible in the aggregation. */
        String validAfterString = dateTimeFormat.format(
            lastValidAfterMillis);
        if (!this.results.containsKey(validAfterString) ||
            this.results.get(validAfterString).size() <
            outputLines.size()) {

          /* Sort results lines, and then put them in. */
          Collections.sort(outputLines);
          this.results.put(validAfterString, outputLines);
        }

        /* Prepare for aggregating votes from the next valid-after
         * time. */
        outputLines = new ArrayList<String>();
        knownFingerprintsByAllAuthorities = 0;
      }

      /* If we reached our end-of-list marker, stop here. */
      if (longValue == Long.MAX_VALUE) {
        break;
      }

      /* Look at the current indexes and increment one of the three
       * counters.  If this value doesn't contain an attribute index, it
       * was put in for counting all known fingerprints at this
       * valid-after time. */
      if (attributeIndex == 0) {
        knownFingerprintsByAllAuthorities++;
      }

      /* Otherwise, if this value doesn't contain a fingerprint index, it
       * was put in for counting authorities voting on a given attribute
       * at the current valid-after time. */
      else if (fingerprintIndex == 0) {
        authoritiesVotingOnAttribute++;
      }

      /* Otherwise, if both indexes are non-zero, this value was put in to
       * count how many authorities assign the attribute to this relay at
       * this valid-after time. */
      else {
        votesForAttribute++;
      }

      /* Prepare moving on to the next value. */
      lastLongValue = longValue;
      lastValidAfterMillis = validAfterMillis;
      lastAttributeIndex = attributeIndex;
      lastFingerprintIndex = fingerprintIndex;
    }
  }
}

