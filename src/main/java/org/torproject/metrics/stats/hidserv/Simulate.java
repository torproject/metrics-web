/* Copyright 2016--2020 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.stats.hidserv;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/* NOTE: This class is not required for running the Main class!  (It
 * contains its own main method.) */
public class Simulate {

  private static Logger log = LoggerFactory.getLogger(Simulate.class);

  private static File simCellsCsvFile =
      new File("out/csv/sim-cells.csv");

  private static File simOnionsCsvFile =
      new File("out/csv/sim-onions.csv");

  /** Runs two simulations to evaluate this data-processing module. */
  public static void main(String[] args) throws Exception {
    log.info("Simulating extrapolation of rendezvous cells");
    simulateManyCells();
    log.info("Simulating extrapolation of .onions");
    simulateManyOnions();
    log.info("Terminating.");
  }

  private static Random rnd = new Random();

  private static void simulateManyCells() throws Exception {
    simCellsCsvFile.getParentFile().mkdirs();
    BufferedWriter bw = new BufferedWriter(new FileWriter(
        simCellsCsvFile));
    bw.write("run,frac,wmean,wmedian,wiqm\n");
    final int numberOfExtrapolations = 1000;
    for (int i = 0; i < numberOfExtrapolations; i++) {
      bw.write(simulateCells(i));
      log.info(".");
    }
    bw.close();
  }

  private static void simulateManyOnions() throws Exception {
    simOnionsCsvFile.getParentFile().mkdirs();
    BufferedWriter bw = new BufferedWriter(new FileWriter(
        simOnionsCsvFile));
    bw.write("run,frac,wmean,wmedian,wiqm\n");
    final int numberOfExtrapolations = 1000;
    for (int i = 0; i < numberOfExtrapolations; i++) {
      bw.write(simulateOnions(i));
      log.info(".");
    }
    bw.close();
  }

  private static String simulateCells(int run) {

    /* Generate consensus weights following an exponential distribution
     * with lambda = 1 for 3000 potential rendezvous points. */
    final int numberRendPoints = 3000;
    double[] consensusWeights = new double[numberRendPoints];
    double totalConsensusWeight = 0.0;
    for (int i = 0; i < numberRendPoints; i++) {
      double consensusWeight = -Math.log(1.0 - rnd.nextDouble());
      consensusWeights[i] = consensusWeight;
      totalConsensusWeight += consensusWeight;
    }

    /* Compute probabilities for being selected as rendezvous point. */
    double[] probRendPoint = new double[numberRendPoints];
    for (int i = 0; i < numberRendPoints; i++) {
      probRendPoint[i] = consensusWeights[i] / totalConsensusWeight;
    }

    /* Generate 10,000,000,000 cells (474 Mbit/s) in chunks following an
     * exponential distribution with lambda = 0.0001, so on average
     * 10,000 cells per chunk, and randomly assign them to a rendezvous
     * point to report them later. */
    long cellsLeft = 10000000000L;
    final double cellsLambda = 0.0001;
    long[] observedCells = new long[numberRendPoints];
    while (cellsLeft > 0) {
      long cells = Math.min(cellsLeft,
          (long) (-Math.log(1.0 - rnd.nextDouble()) / cellsLambda));
      double selectRendPoint = rnd.nextDouble();
      for (int i = 0; i < probRendPoint.length; i++) {
        selectRendPoint -= probRendPoint[i];
        if (selectRendPoint <= 0.0) {
          observedCells[i] += cells;
          break;
        }
      }
      cellsLeft -= cells;
    }

    /* Obfuscate reports using binning and Laplace noise, and then attempt
     * to remove noise again. */
    final long binSize = 1024L;
    final double b = 2048.0 / 0.3;
    long[] reportedCells = new long[numberRendPoints];
    long[] removedNoiseCells = new long[numberRendPoints];
    for (int i = 0; i < numberRendPoints; i++) {
      long observed = observedCells[i];
      long afterBinning = ((observed + binSize - 1L) / binSize) * binSize;
      double randomDouble = rnd.nextDouble();
      double laplaceNoise = -b * (randomDouble > 0.5 ? 1.0 : -1.0)
          * Math.log(1.0 - 2.0 * Math.abs(randomDouble - 0.5));
      long reported = afterBinning + (long) laplaceNoise;
      reportedCells[i] = reported;
      long roundedToNearestRightSideOfTheBin =
          ((reported + binSize / 2) / binSize) * binSize;
      long subtractedHalfOfBinSize =
          roundedToNearestRightSideOfTheBin - binSize / 2;
      removedNoiseCells[i] = subtractedHalfOfBinSize;
    }

    /* Perform extrapolations from random fractions of reports by
     * probability to be selected as rendezvous point. */
    StringBuilder sb = new StringBuilder();
    double[] fractions = new double[] { 0.01, 0.02, 0.03, 0.04, 0.05, 0.1,
        0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 0.99 };
    for (double fraction : fractions) {
      SortedSet<Integer> nonReportingRelays = new TreeSet<>();
      for (int j = 0; j < numberRendPoints; j++) {
        nonReportingRelays.add(j);
      }
      List<Integer> shuffledRelays = new ArrayList<>(nonReportingRelays);
      Collections.shuffle(shuffledRelays);
      SortedSet<Integer> reportingRelays = new TreeSet<>();
      for (int j = 0; j < (int) ((double) numberRendPoints * fraction);
          j++) {
        reportingRelays.add(shuffledRelays.get(j));
        nonReportingRelays.remove(shuffledRelays.get(j));
      }
      List<double[]> singleRelayExtrapolations;
      double totalReportingProbability;
      do {
        singleRelayExtrapolations = new ArrayList<>();
        totalReportingProbability = 0.0;
        for (int reportingRelay : reportingRelays) {
          double probability = probRendPoint[reportingRelay];
          if (probability > 0.0) {
            singleRelayExtrapolations.add(
                new double[] {
                    removedNoiseCells[reportingRelay] / probability,
                    removedNoiseCells[reportingRelay],
                    probability });
          }
          totalReportingProbability += probability;
        }
        if (totalReportingProbability < fraction - 0.001) {
          int addRelay = new ArrayList<>(nonReportingRelays).get(
              rnd.nextInt(nonReportingRelays.size()));
          nonReportingRelays.remove(addRelay);
          reportingRelays.add(addRelay);
        } else if (totalReportingProbability > fraction + 0.001) {
          int removeRelay = new ArrayList<>(reportingRelays).get(
              rnd.nextInt(reportingRelays.size()));
          reportingRelays.remove(removeRelay);
          nonReportingRelays.add(removeRelay);
        }
      } while (totalReportingProbability < fraction - 0.001
          || totalReportingProbability > fraction + 0.001);
      singleRelayExtrapolations.sort(Comparator.comparingDouble(o -> o[0]));
      double totalProbability = 0.0;
      double totalValues = 0.0;
      double totalInterquartileProbability = 0.0;
      double totalInterquartileValues = 0.0;
      Double weightedMedian = null;
      for (double[] extrapolation : singleRelayExtrapolations) {
        totalValues += extrapolation[1];
        totalProbability += extrapolation[2];
        if (weightedMedian == null
            && totalProbability > totalReportingProbability * 0.5) {
          weightedMedian = extrapolation[0];
        }
        if (totalProbability > totalReportingProbability * 0.25
            && totalProbability < totalReportingProbability * 0.75) {
          totalInterquartileValues += extrapolation[1];
          totalInterquartileProbability += extrapolation[2];
        }
      }
      sb.append(String.format(Locale.US, "%d,%.2f,%.0f,%.0f,%.0f%n", run,
          fraction, totalValues / totalProbability, weightedMedian,
          totalInterquartileValues / totalInterquartileProbability));
    }
    return sb.toString();
  }

  private static String simulateOnions(final int run) {

    /* Generate 3000 HSDirs with "fingerprints" between 0.0 and 1.0. */
    final int numberHsDirs = 3000;
    SortedSet<Double> hsDirFingerprints = new TreeSet<>();
    for (int i = 0; i < numberHsDirs; i++) {
      hsDirFingerprints.add(rnd.nextDouble());
    }

    /* Compute fractions of observed descriptor space. */
    SortedSet<Double> ring =
        new TreeSet<>(Collections.reverseOrder());
    for (double fingerprint : hsDirFingerprints) {
      ring.add(fingerprint);
      ring.add(fingerprint - 1.0);
    }
    SortedMap<Double, Double> hsDirFractions = new TreeMap<>();
    for (double fingerprint : hsDirFingerprints) {
      double start = fingerprint;
      int positionsToGo = 3;
      for (double prev : ring.tailSet(fingerprint)) {
        start = prev;
        if (positionsToGo-- <= 0) {
          break;
        }
      }
      hsDirFractions.put(fingerprint, fingerprint - start);
    }

    /* Generate 40000 .onions with 4 HSDesc IDs, store them on HSDirs. */
    final int numberOnions = 40000;
    final int replicas = 4;
    final int storeOnDirs = 3;
    SortedMap<Double, SortedSet<Integer>> storedDescs = new TreeMap<>();
    for (double fingerprint : hsDirFingerprints) {
      storedDescs.put(fingerprint, new TreeSet<>());
    }
    for (int i = 0; i < numberOnions; i++) {
      for (int j = 0; j < replicas; j++) {
        int leftToStore = storeOnDirs;
        for (double fingerprint
            : hsDirFingerprints.tailSet(rnd.nextDouble())) {
          storedDescs.get(fingerprint).add(i);
          if (--leftToStore <= 0) {
            break;
          }
        }
        if (leftToStore > 0) {
          for (double fingerprint : hsDirFingerprints) {
            storedDescs.get(fingerprint).add(i);
            if (--leftToStore <= 0) {
              break;
            }
          }
        }
      }
    }

    /* Obfuscate reports using binning and Laplace noise, and then attempt
     * to remove noise again. */
    final long binSize = 8L;
    final double b = 8.0 / 0.3;
    SortedMap<Double, Long> reportedOnions = new TreeMap<>();
    SortedMap<Double, Long> removedNoiseOnions = new TreeMap<>();
    for (Map.Entry<Double, SortedSet<Integer>> e
        : storedDescs.entrySet()) {
      double fingerprint = e.getKey();
      long observed = e.getValue().size();
      long afterBinning = ((observed + binSize - 1L) / binSize) * binSize;
      double randomDouble = rnd.nextDouble();
      double laplaceNoise = -b * (randomDouble > 0.5 ? 1.0 : -1.0)
          * Math.log(1.0 - 2.0 * Math.abs(randomDouble - 0.5));
      long reported = afterBinning + (long) laplaceNoise;
      reportedOnions.put(fingerprint, reported);
      long roundedToNearestRightSideOfTheBin =
          ((reported + binSize / 2) / binSize) * binSize;
      long subtractedHalfOfBinSize =
          roundedToNearestRightSideOfTheBin - binSize / 2;
      removedNoiseOnions.put(fingerprint, subtractedHalfOfBinSize);
    }

    /* Perform extrapolations from random fractions of reports by
     * probability to be selected as rendezvous point. */
    StringBuilder sb = new StringBuilder();
    double[] fractions = new double[] { 0.01, 0.02, 0.03, 0.04, 0.05, 0.1,
        0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 0.99 };
    for (double fraction : fractions) {
      SortedSet<Double> nonReportingRelays =
          new TreeSet<>(hsDirFractions.keySet());
      List<Double> shuffledRelays = new ArrayList<>(
          nonReportingRelays);
      Collections.shuffle(shuffledRelays);
      SortedSet<Double> reportingRelays = new TreeSet<>();
      for (int j = 0; j < (int) ((double) hsDirFractions.size()
          * fraction); j++) {
        reportingRelays.add(shuffledRelays.get(j));
        nonReportingRelays.remove(shuffledRelays.get(j));
      }
      List<double[]> singleRelayExtrapolations;
      double totalReportingProbability;
      do {
        singleRelayExtrapolations = new ArrayList<>();
        totalReportingProbability = 0.0;
        for (double reportingRelay : reportingRelays) {
          double probability = hsDirFractions.get(reportingRelay) / 3.0;
          if (probability > 0.0) {
            singleRelayExtrapolations.add(
                new double[] { removedNoiseOnions.get(reportingRelay)
                    / probability, removedNoiseOnions.get(reportingRelay),
                    probability });
          }
          totalReportingProbability += probability;
        }
        if (totalReportingProbability < fraction - 0.001) {
          double addRelay =
              new ArrayList<>(nonReportingRelays).get(
              rnd.nextInt(nonReportingRelays.size()));
          nonReportingRelays.remove(addRelay);
          reportingRelays.add(addRelay);
        } else if (totalReportingProbability > fraction + 0.001) {
          double removeRelay =
              new ArrayList<>(reportingRelays).get(
              rnd.nextInt(reportingRelays.size()));
          reportingRelays.remove(removeRelay);
          nonReportingRelays.add(removeRelay);
        }
      } while (totalReportingProbability < fraction - 0.001
          || totalReportingProbability > fraction + 0.001);
      singleRelayExtrapolations.sort(
          Comparator.comparingDouble(doubles -> doubles[0]));
      double totalProbability = 0.0;
      double totalValues = 0.0;
      double totalInterquartileProbability = 0.0;
      double totalInterquartileValues = 0.0;
      Double weightedMedian = null;
      for (double[] extrapolation : singleRelayExtrapolations) {
        totalValues += extrapolation[1];
        totalProbability += extrapolation[2];
        if (weightedMedian == null
            && totalProbability > totalReportingProbability * 0.5) {
          weightedMedian = extrapolation[0];
        }
        if (totalProbability > totalReportingProbability * 0.25
            && totalProbability < totalReportingProbability * 0.75) {
          totalInterquartileValues += extrapolation[1];
          totalInterquartileProbability += extrapolation[2];
        }
      }
      sb.append(String.format(Locale.US, "%d,%.2f,%.0f,%.0f,%.0f%n", run,
          fraction, totalValues / totalProbability, weightedMedian,
          totalInterquartileValues / totalInterquartileProbability));
    }
    return sb.toString();
  }
}
