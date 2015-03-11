package org.torproject.metrics.hidserv;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.torproject.descriptor.Descriptor;
import org.torproject.descriptor.DescriptorFile;
import org.torproject.descriptor.DescriptorReader;
import org.torproject.descriptor.DescriptorSourceFactory;
import org.torproject.descriptor.ExtraInfoDescriptor;
import org.torproject.descriptor.NetworkStatusEntry;
import org.torproject.descriptor.RelayNetworkStatusConsensus;

/* Parse hidden-service statistics from extra-info descriptors, compute
 * network fractions from consensuses, and write parsed contents to
 * document files for later use. */
public class Parser {

  /* File containing tuples of last-modified times and file names of
   * descriptor files parsed in the previous execution. */
  private File parseHistoryFile;

  /* Descriptor reader to provide parsed extra-info descriptors and
   * consensuses. */
  private DescriptorReader descriptorReader;

  /* Document file containing previously parsed reported hidden-service
   * statistics. */
  private File reportedHidServStatsFile;

  /* Document store for storing and retrieving reported hidden-service
   * statistics. */
  private DocumentStore<ReportedHidServStats> reportedHidServStatsStore;

  /* Directory containing document files with previously computed network
   * fractions. */
  private File computedNetworkFractionsDirectory;

  /* Document store for storing and retrieving computed network
   * fractions. */
  private DocumentStore<ComputedNetworkFractions>
      computedNetworkFractionsStore;

  /* Initialize a new parser object using the given directories and
   * document stores. */
  public Parser(Set<File> inDirectories, File statusDirectory,
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
    for (File inDirectory : inDirectories) {
      this.descriptorReader.addDirectory(inDirectory);
    }
    this.descriptorReader.setMaxDescriptorFilesInQueue(5);

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

  /* Read the parse history file to avoid parsing descriptor files that
   * have not changed since the previous execution. */
  public void readParseHistory() {
    if (this.parseHistoryFile.exists() &&
        this.parseHistoryFile.isFile()) {
      SortedMap<String, Long> excludedFiles =
          new TreeMap<String, Long>();
      try {
        BufferedReader br = new BufferedReader(new FileReader(
            this.parseHistoryFile));
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
        br.close();
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

  /* Write parsed or skipped descriptor files with last-modified times and
   * absolute paths to the parse history file to avoid parsing these files
   * again, unless they change until the next execution. */
  public void writeParseHistory() {

    /* Obtain the list of descriptor files that were either parsed now or
     * that were skipped in this execution from the descriptor reader. */
    SortedMap<String, Long> excludedAndParsedFiles =
        new TreeMap<String, Long>();
    excludedAndParsedFiles.putAll(
        this.descriptorReader.getExcludedFiles());
    excludedAndParsedFiles.putAll(this.descriptorReader.getParsedFiles());
    try {
      this.parseHistoryFile.getParentFile().mkdirs();
      BufferedWriter bw = new BufferedWriter(new FileWriter(
          this.parseHistoryFile));
      for (Map.Entry<String, Long> e :
          excludedAndParsedFiles.entrySet()) {
        /* Each line starts with the last-modified time of the descriptor
         * file, followed by its absolute path. */
        String absolutePath = e.getKey();
        long lastModifiedMillis = e.getValue();
        bw.write(String.valueOf(lastModifiedMillis) + " " + absolutePath
            + "\n");
      }
      bw.close();
    } catch (IOException e) {
      System.err.printf("Could not write history file '%s'.  Not "
          + "excluding descriptors in next execution.",
          this.parseHistoryFile.getAbsolutePath());
    }
  }

  /* Set of all reported hidden-service statistics.  To date, these
   * objects are small, and keeping them all in memory is easy.  But if
   * this ever changes, e.g., when more and more statistics are added,
   * this may not scale. */
  private Set<ReportedHidServStats> reportedHidServStats =
      new HashSet<ReportedHidServStats>();

  /* Instruct the descriptor reader to parse descriptor files, and handle
   * the resulting parsed descriptors if they are either extra-info
   * descriptors or consensuses. */
  public boolean parseDescriptors() {
    Iterator<DescriptorFile> descriptorFiles =
        this.descriptorReader.readDescriptors();
    while (descriptorFiles.hasNext()) {
      DescriptorFile descriptorFile = descriptorFiles.next();
      for (Descriptor descriptor : descriptorFile.getDescriptors()) {
        if (descriptor instanceof ExtraInfoDescriptor) {
          this.parseExtraInfoDescriptor((ExtraInfoDescriptor) descriptor);
        } else if (descriptor instanceof RelayNetworkStatusConsensus) {
          if (!this.parseRelayNetworkStatusConsensus(
              (RelayNetworkStatusConsensus) descriptor)) {
            return false;
          }
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

  /* Parse the given extra-info descriptor by extracting its fingerprint
   * and contained hidserv-* lines.  If a valid set of hidserv-stats can
   * be extracted, create a new stats object that will later be stored to
   * a document file. */
  private void parseExtraInfoDescriptor(
      ExtraInfoDescriptor extraInfoDescriptor) {

    /* Extract the fingerprint from the parsed descriptor. */
    String fingerprint = extraInfoDescriptor.getFingerprint();

    /* Parse the descriptor once more to extract any hidserv-* lines.
     * This is necessary, because these lines are not yet supported by the
     * descriptor-parsing library. */
    Scanner scanner = new Scanner(new ByteArrayInputStream(
        extraInfoDescriptor.getRawDescriptorBytes()));
    Long statsEndMillis = null, statsIntervalSeconds = null,
        rendRelayedCells = null, rendRelayedCellsBinSize = null,
        dirOnionsSeen = null, dirOnionsSeenBinSize = null;
    try {
      while (scanner.hasNext()) {
        String line = scanner.nextLine();
        if (line.startsWith("hidserv-")) {
          String[] parts = line.split(" ");
          if (parts[0].equals("hidserv-stats-end")) {
            /* Parse statistics end and statistics interval length. */
            if (parts.length != 5 || !parts[3].startsWith("(") ||
                !parts[4].equals("s)")) {
              /* Will warn below, because statsEndMillis is still null. */
              continue;
            }
            statsEndMillis = DateTimeHelper.parse(parts[1] + " "
                + parts[2]);
            statsIntervalSeconds = Long.parseLong(parts[3].substring(1));
          } else if (parts[0].equals("hidserv-rend-relayed-cells")) {
            /* Parse the reported number of cells on rendezvous circuits
             * and the bin size used by the relay to obfuscate that
             * number. */
            if (parts.length != 5 ||
                !parts[4].startsWith("bin_size=")) {
              /* Will warn below, because rendRelayedCells is still
               * null. */
              continue;
            }
            rendRelayedCells = Long.parseLong(parts[1]);
            rendRelayedCellsBinSize =
                Long.parseLong(parts[4].substring(9));
          } else if (parts[0].equals("hidserv-dir-onions-seen")) {
            /* Parse the reported number of distinct .onion addresses and
             * the bin size used by the relay to obfuscate that number. */
            if (parts.length != 5 ||
                !parts[4].startsWith("bin_size=")) {
              /* Will warn below, because dirOnionsSeen is still null. */
              continue;
            }
            dirOnionsSeen = Long.parseLong(parts[1]);
            dirOnionsSeenBinSize = Long.parseLong(parts[4].substring(9));
          }
        }
      }
    } catch (NumberFormatException e) {
      e.printStackTrace();
      return;
    }

    /* If the descriptor did not contain any of the expected hidserv-*
     * lines, don't do anything.  This applies to the majority of
     * descriptors, at least as long as only a minority of relays reports
     * these statistics. */
    if (statsEndMillis == null && rendRelayedCells == null &&
        dirOnionsSeen == null) {
      return;

    /* If the descriptor contained all expected hidserv-* lines, create a
     * new stats object and put it in the local map, so that it will later
     * be written to a document file. */
    } else if (statsEndMillis != null &&
        statsEndMillis != DateTimeHelper.NO_TIME_AVAILABLE &&
        statsIntervalSeconds != null && rendRelayedCells != null &&
        dirOnionsSeen != null) {
      ReportedHidServStats reportedStats = new ReportedHidServStats(
          fingerprint, statsEndMillis);
      reportedStats.setStatsIntervalSeconds(statsIntervalSeconds);
      reportedStats.setRendRelayedCells(this.removeNoise(rendRelayedCells,
          rendRelayedCellsBinSize));
      reportedStats.setDirOnionsSeen(this.removeNoise(dirOnionsSeen,
          dirOnionsSeenBinSize));
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

  /* Remove noise from a reported stats value by rounding to the nearest
   * right side of a bin and subtracting half of the bin size. */
  private long removeNoise(long reportedNumber, long binSize) {
    long roundedToNearestRightSideOfTheBin =
        ((reportedNumber + binSize / 2) / binSize) * binSize;
    long subtractedHalfOfBinSize =
        roundedToNearestRightSideOfTheBin - binSize / 2;
    return subtractedHalfOfBinSize;
  }

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
        new TreeSet<String>(Arrays.asList("Wmg,Wmm,Wme,Wmd".split(",")));
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
    SortedSet<String> hsDirs = new TreeSet<String>(
        Collections.reverseOrder());

    /* Prepare for computing the weights of all relays with the Fast flag
     * for being selected in the middle position. */
    double totalWeightsRendezvousPoint = 0.0;
    SortedMap<String, Double> weightsRendezvousPoint =
        new TreeMap<String, Double>();

    /* Go through all status entries contained in the consensus. */
    for (Map.Entry<String, NetworkStatusEntry> e :
        consensus.getStatusEntries().entrySet()) {
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
    Set<ComputedNetworkFractions> computedNetworkFractions =
        new HashSet<ComputedNetworkFractions>();

    /* Remove all previously added directory fingerprints and re-add them
     * twice, once with a leading "0" and once with a leading "1".  The
     * purpose is to simplify the logic for moving from one fingerprint to
     * the previous one, even if that would mean traversing the ring
     * start.  For example, the fingerprint preceding "1""00..0000" with
     * the first "1" being added here could be "0""FF..FFFF". */
    SortedSet<String> hsDirsCopy = new TreeSet<String>(hsDirs);
    hsDirs.clear();
    for (String fingerprint : hsDirsCopy) {
      hsDirs.add("0" + fingerprint);
      hsDirs.add("1" + fingerprint);
    }

    /* Define the total ring size to compute fractions below.  This is
     * 16^40 or 2^160. */
    final double RING_SIZE = new BigInteger(
        "10000000000000000000000000000000000000000",
        16).doubleValue();

    /* Go through all status entries again, this time computing network
     * fractions. */
    for (Map.Entry<String, NetworkStatusEntry> e :
        consensus.getStatusEntries().entrySet()) {
      String fingerprint = e.getKey();
      NetworkStatusEntry statusEntry = e.getValue();
      double fractionRendRelayedCells = 0.0,
          fractionDirOnionsSeen = 0.0;
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
          for (String hsDirFingerprint :
              hsDirs.tailSet(fingerprintPrecededByOne)) {
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
              / RING_SIZE;

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

