/* Copyright 2016--2017 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.hidserv;

import org.torproject.descriptor.Descriptor;
import org.torproject.descriptor.DescriptorReader;
import org.torproject.descriptor.DescriptorSourceFactory;
import org.torproject.descriptor.ExtraInfoDescriptor;
import org.torproject.descriptor.NetworkStatusEntry;
import org.torproject.descriptor.RelayNetworkStatusConsensus;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/** Parse hidden-service statistics from extra-info descriptors, compute
 * network fractions from consensuses, and write parsed contents to
 * document files for later use. */
public class Parser {

  /** File containing tuples of last-modified times and file names of
   * descriptor files parsed in the previous execution. */
  private File parseHistoryFile;

  /** Descriptor reader to provide parsed extra-info descriptors and
   * consensuses. */
  private DescriptorReader descriptorReader;

  /** Input directories containing descriptors to parse. */
  private File[] inDirectories;

  /** Document file containing previously parsed reported hidden-service
   * statistics. */
  private File reportedHidServStatsFile;

  /** Document store for storing and retrieving reported hidden-service
   * statistics. */
  private DocumentStore<ReportedHidServStats> reportedHidServStatsStore;

  /** Directory containing document files with previously computed network
   * fractions. */
  private File computedNetworkFractionsDirectory;

  /** Document store for storing and retrieving computed network
   * fractions. */
  private DocumentStore<ComputedNetworkFractions>
      computedNetworkFractionsStore;

  /** Initializes a new parser object using the given directories and
   * document stores. */
  public Parser(File[] inDirectories, File statusDirectory,
      DocumentStore<ReportedHidServStats> reportedHidServStatsStore,
      DocumentStore<ComputedNetworkFractions>
      computedNetworkFractionsStore) {

    /* Create a new descriptor reader for reading descriptors in the given
     * in directory.  Configure the reader to avoid having more than five
     * parsed descriptors in the queue, rather than the default one
     * hundred.  Five is a compromise between very large consensuses and
     * rather small extra-info descriptors. */
    this.descriptorReader =
        DescriptorSourceFactory.createDescriptorReader();
    this.inDirectories = inDirectories;
    this.descriptorReader.setMaxDescriptorsInQueue(5);

    /* Create File instances for the files and directories in the provided
     * status directory. */
    this.parseHistoryFile = new File(statusDirectory, "parse-history");
    this.reportedHidServStatsFile = new File(statusDirectory,
        "reported-hidserv-stats");
    this.computedNetworkFractionsDirectory =
        new File(statusDirectory, "computed-network-fractions");

    /* Store references to the provided document stores. */
    this.reportedHidServStatsStore = reportedHidServStatsStore;
    this.computedNetworkFractionsStore = computedNetworkFractionsStore;
  }

  /** Reads the parse history file to avoid parsing descriptor files that
   * have not changed since the previous execution. */
  public void readParseHistory() {
    if (this.parseHistoryFile.exists()
        && this.parseHistoryFile.isFile()) {
      SortedMap<String, Long> excludedFiles = new TreeMap<>();
      try (BufferedReader br = new BufferedReader(new FileReader(
          this.parseHistoryFile))) {
        String line;
        while ((line = br.readLine()) != null) {
          try {
            /* Each line is supposed to contain the last-modified time and
             * absolute path of a descriptor file. */
            String[] parts = line.split(" ", 2);
            excludedFiles.put(parts[1], Long.parseLong(parts[0]));
          } catch (NumberFormatException e) {
            System.err.printf("Illegal line '%s' in parse history.  "
                + "Skipping line.%n", line);
          }
        }
      } catch (IOException e) {
        System.err.printf("Could not read history file '%s'.  Not "
            + "excluding descriptors in this execution.",
            this.parseHistoryFile.getAbsolutePath());
      }

      /* Tell the descriptor reader to exclude the files contained in the
       * parse history file. */
      this.descriptorReader.setExcludedFiles(excludedFiles);
    }
  }

  /** Writes parsed or skipped descriptor files with last-modified times
   * and absolute paths to the parse history file to avoid parsing these
   * files again, unless they change until the next execution. */
  public void writeParseHistory() {

    /* Obtain the list of descriptor files that were either parsed now or
     * that were skipped in this execution from the descriptor reader. */
    SortedMap<String, Long> excludedAndParsedFiles = new TreeMap<>();
    excludedAndParsedFiles.putAll(
        this.descriptorReader.getExcludedFiles());
    excludedAndParsedFiles.putAll(this.descriptorReader.getParsedFiles());
    this.parseHistoryFile.getParentFile().mkdirs();
    try (BufferedWriter bw = new BufferedWriter(new FileWriter(
        this.parseHistoryFile))) {
      for (Map.Entry<String, Long> e
          : excludedAndParsedFiles.entrySet()) {
        /* Each line starts with the last-modified time of the descriptor
         * file, followed by its absolute path. */
        String absolutePath = e.getKey();
        long lastModifiedMillis = e.getValue();
        bw.write(String.valueOf(lastModifiedMillis) + " " + absolutePath
            + "\n");
      }
    } catch (IOException e) {
      System.err.printf("Could not write history file '%s'.  Not "
          + "excluding descriptors in next execution.",
          this.parseHistoryFile.getAbsolutePath());
    }
  }

  /** Set of all reported hidden-service statistics.
   *
   * <p>To date, these objects are small, and keeping them all in memory
   * is easy.  But if this ever changes, e.g., when more and more
   * statistics are added, this may not scale.</p> */
  private Set<ReportedHidServStats> reportedHidServStats = new HashSet<>();

  /** Instructs the descriptor reader to parse descriptor files, and
   * handles the resulting parsed descriptors if they are either
   * extra-info descriptors or consensuses. */
  public boolean parseDescriptors() {
    for (Descriptor descriptor : descriptorReader.readDescriptors(
        this.inDirectories)) {
      if (descriptor instanceof ExtraInfoDescriptor) {
        this.parseExtraInfoDescriptor((ExtraInfoDescriptor) descriptor);
      } else if (descriptor instanceof RelayNetworkStatusConsensus) {
        if (!this.parseRelayNetworkStatusConsensus(
            (RelayNetworkStatusConsensus) descriptor)) {
          return false;
        }
      }
    }

    /* Store reported hidden-service statistics to their document file.
     * It's more efficient to only do this once after processing all
     * descriptors.  In contrast, sets of computed network fractions are
     * stored immediately after processing the consensus they are based
     * on. */
    return this.reportedHidServStatsStore.store(
        this.reportedHidServStatsFile, this.reportedHidServStats);
  }

  private static final String BIN_SIZE = "bin_size";

  /** Parses the given extra-info descriptor by extracting its fingerprint
   * and contained hidserv-* lines.
   *
   * <p>If a valid set of hidserv-stats can be extracted, create a new
   * stats object that will later be stored to a document file.</p> */
  private void parseExtraInfoDescriptor(
      ExtraInfoDescriptor extraInfoDescriptor) {

    /* Extract the fingerprint from the parsed descriptor. */
    String fingerprint = extraInfoDescriptor.getFingerprint();

    /* If the descriptor did not contain any of the expected hidserv-*
     * lines, don't do anything.  This applies to the majority of
     * descriptors, at least as long as only a minority of relays reports
     * these statistics. */
    if (extraInfoDescriptor.getHidservStatsEndMillis() < 0L
        && extraInfoDescriptor.getHidservRendRelayedCells() == null
        && extraInfoDescriptor.getHidservDirOnionsSeen() == null) {
      return;

    /* If the descriptor contained all expected hidserv-* lines, create a
     * new stats object and put it in the local map, so that it will later
     * be written to a document file. */
    } else if (extraInfoDescriptor.getHidservStatsEndMillis() >= 0L
        && extraInfoDescriptor.getHidservStatsIntervalLength() >= 0L
        && extraInfoDescriptor.getHidservRendRelayedCells() != null
        && extraInfoDescriptor.getHidservRendRelayedCellsParameters() != null
        && extraInfoDescriptor.getHidservRendRelayedCellsParameters()
        .containsKey(BIN_SIZE)
        && extraInfoDescriptor.getHidservDirOnionsSeen() != null
        && extraInfoDescriptor.getHidservDirOnionsSeenParameters() != null
        && extraInfoDescriptor.getHidservDirOnionsSeenParameters()
        .containsKey(BIN_SIZE)) {
      ReportedHidServStats reportedStats = new ReportedHidServStats(
          fingerprint, extraInfoDescriptor.getHidservStatsEndMillis());
      reportedStats.setStatsIntervalSeconds(extraInfoDescriptor
          .getHidservStatsIntervalLength());
      reportedStats.setRendRelayedCells(this.removeNoise(extraInfoDescriptor
          .getHidservRendRelayedCells().longValue(), extraInfoDescriptor
          .getHidservRendRelayedCellsParameters().get(BIN_SIZE).longValue()));
      reportedStats.setDirOnionsSeen(this.removeNoise(extraInfoDescriptor
          .getHidservDirOnionsSeen().longValue(), extraInfoDescriptor
          .getHidservDirOnionsSeenParameters().get(BIN_SIZE).longValue()));
      this.reportedHidServStats.add(reportedStats);

    /* If the descriptor contained some but not all hidserv-* lines, print
     * out a warning.  This case does not warrant any further action,
     * because relays can in theory write anything in their extra-info
     * descriptors.  But maybe we'll want to know. */
    } else {
      System.err.println("Relay " + fingerprint + " published "
          + "incomplete hidserv-stats.  Ignoring.");
    }
  }

  /** Removes noise from a reported stats value by rounding to the nearest
   * right side of a bin and subtracting half of the bin size. */
  private long removeNoise(long reportedNumber, long binSize) {
    long roundedToNearestRightSideOfTheBin =
        ((reportedNumber + binSize / 2) / binSize) * binSize;
    long subtractedHalfOfBinSize =
        roundedToNearestRightSideOfTheBin - binSize / 2;
    return subtractedHalfOfBinSize;
  }

  /** Parses the given consensus. */
  public boolean parseRelayNetworkStatusConsensus(
      RelayNetworkStatusConsensus consensus) {

    /* Make sure that the consensus contains Wxx weights. */
    SortedMap<String, Integer> bandwidthWeights =
        consensus.getBandwidthWeights();
    if (bandwidthWeights == null) {
      System.err.printf("Consensus with valid-after time %s doesn't "
          + "contain any Wxx weights.  Skipping.%n",
          DateTimeHelper.format(consensus.getValidAfterMillis()));
      return false;
    }

    /* More precisely, make sure that it contains Wmx weights, and then
     * parse them. */
    SortedSet<String> expectedWeightKeys =
        new TreeSet<>(Arrays.asList("Wmg,Wmm,Wme,Wmd".split(",")));
    expectedWeightKeys.removeAll(bandwidthWeights.keySet());
    if (!expectedWeightKeys.isEmpty()) {
      System.err.printf("Consensus with valid-after time %s doesn't "
          + "contain expected Wmx weights.  Skipping.%n",
          DateTimeHelper.format(consensus.getValidAfterMillis()));
      return false;
    }
    double wmg = ((double) bandwidthWeights.get("Wmg")) / 10000.0;
    double wmm = ((double) bandwidthWeights.get("Wmm")) / 10000.0;
    double wme = ((double) bandwidthWeights.get("Wme")) / 10000.0;
    double wmd = ((double) bandwidthWeights.get("Wmd")) / 10000.0;

    /* Keep a sorted set with the fingerprints of all hidden-service
     * directories, in reverse order, so that we can later determine the
     * fingerprint distance between a directory and the directory
     * preceding it by three positions in the descriptor ring. */
    SortedSet<String> hsDirs = new TreeSet<>(Collections.reverseOrder());

    /* Prepare for computing the weights of all relays with the Fast flag
     * for being selected in the middle position. */
    double totalWeightsRendezvousPoint = 0.0;
    SortedMap<String, Double> weightsRendezvousPoint = new TreeMap<>();

    /* Go through all status entries contained in the consensus. */
    for (Map.Entry<String, NetworkStatusEntry> e
        : consensus.getStatusEntries().entrySet()) {
      String fingerprint = e.getKey();
      NetworkStatusEntry statusEntry = e.getValue();
      SortedSet<String> flags = statusEntry.getFlags();

      /* Add the relay to the set of hidden-service directories if it has
       * the HSDir flag. */
      if (flags.contains("HSDir")) {
        hsDirs.add(statusEntry.getFingerprint());
      }

      /* Compute the probability for being selected as rendezvous point.
       * If the relay has the Fast flag, multiply its consensus weight
       * with the correct Wmx weight, depending on whether the relay has
       * the Guard and/or Exit flag. */
      double weightRendezvousPoint = 0.0;
      if (flags.contains("Fast")) {
        weightRendezvousPoint = (double) statusEntry.getBandwidth();
        if (flags.contains("Guard") && flags.contains("Exit")) {
          weightRendezvousPoint *= wmd;
        } else if (flags.contains("Guard")) {
          weightRendezvousPoint *= wmg;
        } else if (flags.contains("Exit")) {
          weightRendezvousPoint *= wme;
        } else {
          weightRendezvousPoint *= wmm;
        }
      }
      weightsRendezvousPoint.put(fingerprint, weightRendezvousPoint);
      totalWeightsRendezvousPoint += weightRendezvousPoint;
    }

    /* Store all computed network fractions based on this consensus in a
     * set, which will then be written to disk in a single store
     * operation. */
    Set<ComputedNetworkFractions> computedNetworkFractions = new HashSet<>();

    /* Remove all previously added directory fingerprints and re-add them
     * twice, once with a leading "0" and once with a leading "1".  The
     * purpose is to simplify the logic for moving from one fingerprint to
     * the previous one, even if that would mean traversing the ring
     * start.  For example, the fingerprint preceding "1""00..0000" with
     * the first "1" being added here could be "0""FF..FFFF". */
    SortedSet<String> hsDirsCopy = new TreeSet<>(hsDirs);
    hsDirs.clear();
    for (String fingerprint : hsDirsCopy) {
      hsDirs.add("0" + fingerprint);
      hsDirs.add("1" + fingerprint);
    }

    /* Define the total ring size to compute fractions below.  This is
     * 16^40 or 2^160. */
    final double ringSize = new BigInteger(
        "10000000000000000000000000000000000000000",
        16).doubleValue();

    /* Go through all status entries again, this time computing network
     * fractions. */
    for (Map.Entry<String, NetworkStatusEntry> e
        : consensus.getStatusEntries().entrySet()) {
      String fingerprint = e.getKey();
      NetworkStatusEntry statusEntry = e.getValue();
      double fractionRendRelayedCells = 0.0;
      double fractionDirOnionsSeen = 0.0;
      if (statusEntry != null) {

        /* Check if the relay is a hidden-service directory by looking up
         * its fingerprint, preceded by "1", in the sorted set that we
         * populated above. */
        String fingerprintPrecededByOne = "1" + fingerprint;
        if (hsDirs.contains(fingerprintPrecededByOne)) {

          /* Move three positions in the sorted set, which is in reverse
           * order, to learn the fingerprint of the directory preceding
           * this directory by three positions. */
          String startResponsible = fingerprint;
          int positionsToGo = 3;
          for (String hsDirFingerprint
              : hsDirs.tailSet(fingerprintPrecededByOne)) {
            startResponsible = hsDirFingerprint;
            if (positionsToGo-- <= 0) {
              break;
            }
          }

          /* Compute the fraction of descriptor space that this relay is
           * responsible for as difference between the two fingerprints
           * divided by the ring size. */
          fractionDirOnionsSeen =
              new BigInteger(fingerprintPrecededByOne, 16).subtract(
              new BigInteger(startResponsible, 16)).doubleValue()
              / ringSize;

          /* Divide this fraction by three to obtain the fraction of
           * descriptors that this directory has seen.  This step is
           * necessary, because each descriptor that is published to this
           * directory is also published to two other directories. */
          fractionDirOnionsSeen /= 3.0;
        }

        /* Compute the fraction of cells on rendezvous circuits that this
         * relay has seen by dividing its previously calculated weight by
         * the sum of all such weights. */
        fractionRendRelayedCells = weightsRendezvousPoint.get(fingerprint)
            / totalWeightsRendezvousPoint;
      }

      /* If at least one of the computed fractions is non-zero, create a
       * new fractions object. */
      if (fractionRendRelayedCells > 0.0 || fractionDirOnionsSeen > 0.0) {
        ComputedNetworkFractions fractions = new ComputedNetworkFractions(
            fingerprint, consensus.getValidAfterMillis());
        fractions.setFractionRendRelayedCells(fractionRendRelayedCells);
        fractions.setFractionDirOnionsSeen(fractionDirOnionsSeen);
        computedNetworkFractions.add(fractions);
      }
    }

    /* Store all newly computed network fractions to a documents file.
     * The same file also contains computed network fractions from other
     * consensuses that were valid on the same day.  This is in contrast
     * to the other documents which are all stored in a single file, which
     * would not scale for computed network fractions. */
    String date = DateTimeHelper.format(consensus.getValidAfterMillis(),
        DateTimeHelper.ISO_DATE_FORMAT);
    File documentFile = new File(this.computedNetworkFractionsDirectory,
        date);
    if (!this.computedNetworkFractionsStore.store(documentFile,
        computedNetworkFractions)) {
      return false;
    }
    return true;
  }
}

