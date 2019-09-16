/* Copyright 2016--2018 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.stats.hidserv;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/** Aggregate extrapolated network totals of hidden-service statistics by
 * calculating statistics like the daily weighted interquartile mean.
 * Also calculate simpler statistics like the number of reported
 * statistics and the total network fraction of reporting relays. */
public class Aggregator {

  private static Logger log = LoggerFactory.getLogger(Aggregator.class);

  /** Document file containing extrapolated hidden-service statistics. */
  private File extrapolatedHidServStatsFile;

  /** Document store for storing and retrieving extrapolated hidden-service
   * statistics. */
  private DocumentStore<ExtrapolatedHidServStats>
      extrapolatedHidServStatsStore;

  /** Output file for writing aggregated statistics. */
  private File hidservStatsCsvFile;

  /** Initializes a new aggregator object using the given directory,
   * document store, and output file for results. */
  public Aggregator(File statusDirectory,
      DocumentStore<ExtrapolatedHidServStats>
      extrapolatedHidServStatsStore, File hidservStatsCsvFile) {

    /* Create a File instance for the document file containing
     * extrapolated network totals. */
    this.extrapolatedHidServStatsFile = new File(statusDirectory,
        "extrapolated-hidserv-stats");

    /* Store references to the provided document store and output file. */
    this.extrapolatedHidServStatsStore = extrapolatedHidServStatsStore;
    this.hidservStatsCsvFile = hidservStatsCsvFile;
  }

  /** Calculates aggregates for all extrapolated hidden-service statistics
   * and writes them to the output file. */
  public void aggregateHidServStats() {

    /* Retrieve previously extrapolated network totals. */
    Set<ExtrapolatedHidServStats> extrapolatedStats =
        this.extrapolatedHidServStatsStore.retrieve(
        this.extrapolatedHidServStatsFile);
    if (extrapolatedStats == null) {
      log.warn("Unable to retrieve extrapolated hidden-service "
          + "statistics from file {}. Skipping aggregation step.",
          this.extrapolatedHidServStatsFile.getAbsolutePath());
      return;
    }

    /* Exclude any statistics being as recent as yesterday or newer. */
    String yesterday = DateTimeHelper.format(System.currentTimeMillis()
        - DateTimeHelper.ONE_DAY, DateTimeHelper.ISO_DATE_FORMAT);

    /* Re-arrange extrapolated network totals by statistics interval end
     * date, and include the computed network total as weight for the
     * extrapolated value.  More precisely, map keys are ISO-formatted
     * dates, map values are double[] arrays with the extrapolated network
     * total as first element and the corresponding computed network
     * fraction as second element. */
    SortedMap<String, List<double[]>> extrapolatedCells = new TreeMap<>();
    SortedMap<String, List<double[]>> extrapolatedOnions = new TreeMap<>();
    for (ExtrapolatedHidServStats extrapolated : extrapolatedStats) {
      String date = DateTimeHelper.format(
          extrapolated.getStatsDateMillis(),
          DateTimeHelper.ISO_DATE_FORMAT);
      if (date.compareTo(yesterday) >= 0) {
        continue;
      }
      if (extrapolated.getFractionRendRelayedCells() > 0.0) {
        extrapolatedCells.putIfAbsent(date, new ArrayList<>());
        extrapolatedCells.get(date).add(new double[] {
            extrapolated.getExtrapolatedRendRelayedCells(),
            extrapolated.getFractionRendRelayedCells() });
      }
      if (extrapolated.getFractionDirOnionsSeen() > 0.0) {
        extrapolatedOnions.putIfAbsent(date, new ArrayList<>());
        extrapolatedOnions.get(date).add(new double[] {
            extrapolated.getExtrapolatedDirOnionsSeen(),
            extrapolated.getFractionDirOnionsSeen() });
      }
    }

    /* Write all results to a string builder that will later be written to
     * the output file.  Each line contains an ISO-formatted "date", a
     * string identifier for the "type" of statistic, the weighted mean
     * ("wmean"), weighted median ("wmedian"), weighted interquartile mean
     * ("wiqm"), the total network "frac"tion, and the number of reported
     * "stats" with non-zero computed network fraction. */
    StringBuilder sb = new StringBuilder();
    sb.append("date,type,wmean,wmedian,wiqm,frac,stats\n");

    /* Repeat all aggregation steps for both types of statistics. */
    for (int i = 0; i < 2; i++) {
      String type = i == 0 ? "rend-relayed-cells" : "dir-onions-seen";
      SortedMap<String, List<double[]>> extrapolated = i == 0
          ? extrapolatedCells : extrapolatedOnions;

      /* Go through all dates. */
      for (Map.Entry<String, List<double[]>> e
          : extrapolated.entrySet()) {
        List<double[]> weightedValues = e.getValue();

        /* Sort extrapolated network totals contained in the first array
         * element.  (The second array element contains the computed
         * network fraction as weight.) */
        weightedValues.sort(Comparator.comparingDouble(doubles -> doubles[0]));

        /* For the weighted mean, sum up all previously extrapolated
         * values weighted with their network fractions (which happens to
         * be the values that relays reported), and sum up all network
         * fractions.  Once we have those two sums, we can divide the sum
         * of weighted extrapolated values by the sum of network fractions
         * to obtain the weighted mean of extrapolated values. */
        double sumReported = 0.0;
        double sumFraction = 0.0;
        for (double[] d : weightedValues) {
          sumReported += d[0] * d[1];
          sumFraction += d[1];
        }
        double weightedMean = sumReported / sumFraction;

        /* For the weighted median and weighted interquartile mean, go
         * through all values once again.  The weighted median is the
         * first extrapolated value with weight interval end greater than
         * 50% of reported network fractions.  For the weighted
         * interquartile mean, sum up extrapolated values multiplied with
         * network fractions and network fractions falling into the 25% to
         * 75% range and later compute the weighted mean of those. */
        double weightIntervalEnd = 0.0;
        Double weightedMedian = null;
        double sumFractionInterquartile = 0.0;
        double sumReportedInterquartile = 0.0;
        for (double[] d : weightedValues) {
          double extrapolatedValue = d[0];
          double computedFraction = d[1];
          double weightIntervalStart = weightIntervalEnd;
          weightIntervalEnd += computedFraction;
          if (weightedMedian == null
              && weightIntervalEnd > sumFraction * 0.5) {
            weightedMedian = extrapolatedValue;
          }
          if (weightIntervalEnd >= sumFraction * 0.25
              && weightIntervalStart <= sumFraction * 0.75) {
            double fractionBetweenQuartiles =
                Math.min(weightIntervalEnd, sumFraction * 0.75)
                - Math.max(weightIntervalStart, sumFraction * 0.25);
            sumReportedInterquartile += extrapolatedValue
                * fractionBetweenQuartiles;
            sumFractionInterquartile += fractionBetweenQuartiles;
          }
        }
        double weightedInterquartileMean =
            sumReportedInterquartile / sumFractionInterquartile;

        /* Put together all aggregated values in a single line. */
        String date = e.getKey();
        int numStats = weightedValues.size();
        sb.append(String.format(Locale.US, "%s,%s,%.0f,%.0f,%.0f,%.8f,%d%n",
            date, type, weightedMean, weightedMedian, weightedInterquartileMean,
            sumFraction, numStats));
      }
    }

    /* Write all aggregated results to the output file. */
    this.hidservStatsCsvFile.getParentFile().mkdirs();
    try (BufferedWriter bw = new BufferedWriter(new FileWriter(
        this.hidservStatsCsvFile))) {
      bw.write(sb.toString());
    } catch (IOException e) {
      log.warn("Unable to write results to {}. Ignoring.",
          this.extrapolatedHidServStatsFile.getAbsolutePath());
    }
  }
}

