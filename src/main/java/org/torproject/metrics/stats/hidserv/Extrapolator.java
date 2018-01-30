/* Copyright 2016--2018 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.stats.hidserv;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/** Extrapolate hidden-service statistics reported by single relays by
 * dividing them by the computed fraction of hidden-service activity
 * observed by the relay. */
public class Extrapolator {

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

  /** Document file containing extrapolated hidden-service statistics. */
  private File extrapolatedHidServStatsFile;

  /** Document store for storing and retrieving extrapolated hidden-service
   * statistics. */
  private DocumentStore<ExtrapolatedHidServStats>
      extrapolatedHidServStatsStore;

  /** Initializes a new extrapolator object using the given directory and
   * document stores. */
  public Extrapolator(File statusDirectory,
      DocumentStore<ReportedHidServStats> reportedHidServStatsStore,
      DocumentStore<ComputedNetworkFractions>
      computedNetworkFractionsStore,
      DocumentStore<ExtrapolatedHidServStats>
      extrapolatedHidServStatsStore) {

    /* Create File instances for the files and directories in the provided
     * status directory. */
    this.reportedHidServStatsFile = new File(statusDirectory,
        "reported-hidserv-stats");
    this.computedNetworkFractionsDirectory =
        new File(statusDirectory, "computed-network-fractions");
    this.extrapolatedHidServStatsFile = new File(statusDirectory,
        "extrapolated-hidserv-stats");

    /* Store references to the provided document stores. */
    this.reportedHidServStatsStore = reportedHidServStatsStore;
    this.computedNetworkFractionsStore = computedNetworkFractionsStore;
    this.extrapolatedHidServStatsStore = extrapolatedHidServStatsStore;
  }

  /** Iterates over all reported stats and extrapolate network totals for
   * those that have not been extrapolated before. */
  public boolean extrapolateHidServStats() {

    /* Retrieve previously extrapolated stats to avoid extrapolating them
     * again. */
    Set<ExtrapolatedHidServStats> extrapolatedStats =
        this.extrapolatedHidServStatsStore.retrieve(
        this.extrapolatedHidServStatsFile);

    /* Retrieve all reported stats, even including those that have already
     * been extrapolated. */
    Set<ReportedHidServStats> reportedStats =
        this.reportedHidServStatsStore.retrieve(
        this.reportedHidServStatsFile);

    /* Make sure that all documents could be retrieved correctly. */
    if (extrapolatedStats == null || reportedStats == null) {
      System.err.printf("Could not read previously parsed or "
          + "extrapolated hidserv-stats.  Skipping.");
      return false;
    }

    /* Re-arrange reported stats by fingerprint. */
    SortedMap<String, Set<ReportedHidServStats>> parsedStatsByFingerprint =
        new TreeMap<>();
    for (ReportedHidServStats stat : reportedStats) {
      String fingerprint = stat.getFingerprint();
      if (!parsedStatsByFingerprint.containsKey(fingerprint)) {
        parsedStatsByFingerprint.put(fingerprint, new HashSet<>());
      }
      parsedStatsByFingerprint.get(fingerprint).add(stat);
    }

    /* Go through reported stats by fingerprint. */
    for (Map.Entry<String, Set<ReportedHidServStats>> e
        : parsedStatsByFingerprint.entrySet()) {
      String fingerprint = e.getKey();

      /* Iterate over all stats reported by this relay and make a list of
       * those that still need to be extrapolated.  Also make a list of
       * all dates for which we need to retrieve computed network
       * fractions. */
      Set<ReportedHidServStats> newReportedStats = new HashSet<>();
      SortedSet<String> retrieveFractionDates = new TreeSet<>();
      for (ReportedHidServStats stats : e.getValue()) {

        /* Check whether extrapolated stats already contain an object with
         * the same statistics interval end date and fingerprint. */
        long statsDateMillis = (stats.getStatsEndMillis()
            / DateTimeHelper.ONE_DAY) * DateTimeHelper.ONE_DAY;
        if (extrapolatedStats.contains(
            new ExtrapolatedHidServStats(statsDateMillis, fingerprint))) {
          continue;
        }

        /* Add the reported stats to the list of stats we still need to
         * extrapolate. */
        newReportedStats.add(stats);

        /* Add all dates between statistics interval start and end to a
         * list. */
        long statsEndMillis = stats.getStatsEndMillis();
        long statsStartMillis = statsEndMillis
            - stats.getStatsIntervalSeconds() * DateTimeHelper.ONE_SECOND;
        for (long millis = statsStartMillis; millis <= statsEndMillis;
            millis += DateTimeHelper.ONE_DAY) {
          String date = DateTimeHelper.format(millis,
              DateTimeHelper.ISO_DATE_FORMAT);
          retrieveFractionDates.add(date);
        }
      }

      /* Retrieve all computed network fractions that might be needed to
       * extrapolate new statistics.  Keep a list of all known consensus
       * valid-after times, and keep a map of fractions also by consensus
       * valid-after time.  (It's not sufficient to only keep the latter,
       * because we need to count known consensuses even if the relay was
       * not contained in a consensus or had a network fraction of exactly
       * zero.) */
      SortedSet<Long> knownConsensuses = new TreeSet<>();
      SortedMap<Long, ComputedNetworkFractions> computedNetworkFractions =
          new TreeMap<>();
      for (String date : retrieveFractionDates) {
        File documentFile = new File(
            this.computedNetworkFractionsDirectory, date);
        Set<ComputedNetworkFractions> fractions
            = this.computedNetworkFractionsStore.retrieve(documentFile,
            fingerprint);
        for (ComputedNetworkFractions fraction : fractions) {
          knownConsensuses.add(fraction.getValidAfterMillis());
          if (fraction.getFingerprint().equals(fingerprint)) {
            computedNetworkFractions.put(fraction.getValidAfterMillis(),
                fraction);
          }
        }
      }

      /* Go through newly reported stats, match them with computed network
       * fractions, and extrapolate network totals. */
      for (ReportedHidServStats stats : newReportedStats) {
        long statsEndMillis = stats.getStatsEndMillis();
        long statsDateMillis = (statsEndMillis / DateTimeHelper.ONE_DAY)
            * DateTimeHelper.ONE_DAY;
        long statsStartMillis = statsEndMillis
            - stats.getStatsIntervalSeconds() * DateTimeHelper.ONE_SECOND;

        /* Sum up computed network fractions and count known consensus in
         * the relevant interval, so that we can later compute means of
         * network fractions. */
        double sumFractionRendRelayedCells = 0.0;
        double sumFractionDirOnionsSeen = 0.0;
        int consensuses = 0;
        for (long validAfterMillis : knownConsensuses) {
          if (statsStartMillis <= validAfterMillis
              && validAfterMillis < statsEndMillis) {
            if (computedNetworkFractions.containsKey(validAfterMillis)) {
              ComputedNetworkFractions frac =
                  computedNetworkFractions.get(validAfterMillis);
              sumFractionRendRelayedCells +=
                  frac.getFractionRendRelayedCells();
              sumFractionDirOnionsSeen +=
                  frac.getFractionDirOnionsSeen();
            }
            consensuses++;
          }
        }

        /* If we don't know a single consensus with valid-after time in
         * the statistics interval, skip this stat. */
        if (consensuses == 0) {
          continue;
        }

        /* Compute means of network fractions. */
        double fractionRendRelayedCells =
            sumFractionRendRelayedCells / consensuses;
        double fractionDirOnionsSeen =
            sumFractionDirOnionsSeen / consensuses;

        /* If at least one fraction is positive, extrapolate network
         * totals. */
        if (fractionRendRelayedCells > 0.0
            || fractionDirOnionsSeen > 0.0) {
          ExtrapolatedHidServStats extrapolated =
              new ExtrapolatedHidServStats(
              statsDateMillis, fingerprint);
          if (fractionRendRelayedCells > 0.0) {
            extrapolated.setFractionRendRelayedCells(
                fractionRendRelayedCells);
            /* Extrapolating cells on rendezvous circuits is as easy as
             * dividing the reported number by the computed network
             * fraction. */
            double extrapolatedRendRelayedCells =
                stats.getRendRelayedCells() / fractionRendRelayedCells;
            extrapolated.setExtrapolatedRendRelayedCells(
                extrapolatedRendRelayedCells);
          }
          if (fractionDirOnionsSeen > 0.0) {
            extrapolated.setFractionDirOnionsSeen(
                fractionDirOnionsSeen);
            /* Extrapolating reported unique .onion addresses to the
             * total number in the network is more difficult.  In short,
             * each descriptor is stored to 12 (likely) different
             * directories, so we'll have to divide the reported number by
             * 12 and then by the computed network fraction of this
             * directory. */
            double extrapolatedDirOnionsSeen =
                stats.getDirOnionsSeen() / (12.0 * fractionDirOnionsSeen);
            extrapolated.setExtrapolatedDirOnionsSeen(
                extrapolatedDirOnionsSeen);
          }
          extrapolatedStats.add(extrapolated);
        }
      }
    }

    /* Store all extrapolated network totals to disk with help of the
     * document store. */
    return this.extrapolatedHidServStatsStore.store(
        this.extrapolatedHidServStatsFile, extrapolatedStats);
  }
}

