/* Copyright 2011 George Danezis <gdane@microsoft.com>
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted (subject to the limitations in the
 * disclaimer below) provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the
 *    distribution.
 *
 *  * Neither the name of <Owner Organization> nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE
 * GRANTED BY THIS LICENSE.  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT
 * HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
 * IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * (Clear BSD license:
 * http://labs.metacarta.com/license-explanation.html#license)
 *
 * Copyright 2018 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.stats.clients;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.PoissonDistribution;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

/** Censorship detector that reads a .csv file of the number of Tor clients and
 * finds anomalies that might be indicative of censorship. */
public class Detector {

  /** Input file. */
  private static final Path INPUT_PATH = new File(Main.baseDir,
      "stats/userstats.csv").toPath();

  /** Output file. */
  private static final Path OUTPUT_PATH = new File(Main.baseDir,
      "stats/clients.csv").toPath();

  /** Number of largest locations to be included in the detection algorithm. */
  private static final int NUM_LARGEST_LOCATIONS = 50;

  /** Time interval in days to model connection rates. */
  private static final int INTERV = 7;

  /** Compound key under which client estimates are stored in both input and
   * output files. */
  private static class ClientsKey implements Comparable<ClientsKey> {

    /** Date when clients connected to the Tor network. */
    private LocalDate date;

    /** Whether clients connected via relays (true) or bridges (false). */
    private boolean nodeIsRelay;

    /** Two-letter lower-case country code of the country from which clients
     * connected, "??" if the country could not be resolved, or left empty for
     * all countries together. */
    private String country;

    /** Name of the transport used by clients to connect using bridges, or left
     * empty for all transports together. */
    private String transport = "";

    /** IP version used by clients to connect using bridges, or left empty for
     * all IP versions together. */
    private String version = "";

    ClientsKey(LocalDate date, boolean nodeIsRelay, String country) {
      this.date = date;
      this.nodeIsRelay = nodeIsRelay;
      this.country = country;
    }

    ClientsKey(LocalDate date, boolean nodeIsRelay, String country,
        String transport, String version) {
      this(date, nodeIsRelay, country);
      this.transport = transport;
      this.version = version;
    }

    @Override
    public int compareTo(ClientsKey other) {
      if (!this.date.equals(other.date)) {
        return this.date.compareTo(other.date);
      } else if (!this.nodeIsRelay && other.nodeIsRelay) {
        return -1;
      } else if (this.nodeIsRelay && !other.nodeIsRelay) {
        return 1;
      } else if (!this.country.equals(other.country)) {
        return this.country.compareTo(other.country);
      } else if (!this.transport.equals(other.transport)) {
        return this.transport.compareTo(other.transport);
      } else if (!this.version.equals(other.version)) {
        return this.version.compareTo(other.version);
      } else {
        return 0;
      }
    }

    @Override
    public boolean equals(Object otherObject) {
      if (!(otherObject instanceof ClientsKey)) {
        return false;
      } else {
        ClientsKey other = (ClientsKey) otherObject;
        return this.date.equals(other.date)
            && this.nodeIsRelay == other.nodeIsRelay
            && this.country.equals(other.country)
            && this.transport.equals(other.transport)
            && this.version.equals(other.version);
      }
    }

    @Override
    public int hashCode() {
      return 3 * this.date.hashCode() + (this.nodeIsRelay ? 5 : 0)
          + 7 * this.country.hashCode() + 11 * this.transport.hashCode()
          + 13 * this.version.hashCode();
    }

    @Override
    public String toString() {
      return String.format("%s,%s,%s,%s,%s",
          this.date.toString(), this.nodeIsRelay ? "relay" : "bridge",
          this.country, this.transport, this.version);
    }
  }

  /** Value class that stores everything we already knew about a specific
   * subset of clients from the input file. */
  private static class ClientsEstimates {

    /** Estimated number of clients. */
    private int clients;

    /** Fraction of relays or bridges in percent that the estimate is based on,
     * between 0 and 100. */
    private int frac;

    ClientsEstimates(int clients, int frac) {
      this.clients = clients;
      this.frac = frac;
    }

    @Override
    public String toString() {
      return String.format("%d,%d", this.clients, this.frac);
    }
  }

  /** Value class that stores everything we're computing here about a specific
   * subset of clients from the input file. */
  private static class ClientsRanges {

    /** Lower number of expected clients under the assumption that there has
     * been no censorship event, as computed here. */
    private int lower;

    /** Upper number of expected clients under the assumption that there has
     * been no release of censorship, as computed here. */
    private int upper;

    ClientsRanges(int lower, int upper) {
      this.lower = lower;
      this.upper = upper;
    }

    @Override
    public String toString() {
      return String.format("%d,%d", this.lower, this.upper);
    }
  }

  /** Run censorship detection. */
  public void detect() throws IOException {
    SortedMap<ClientsKey, ClientsEstimates> estimates = readInputFile();
    Set<String> largestLocations = findLargestLocations(estimates);
    Map<LocalDate, List<Double>> ratios = computeRatiosOfLargestLocations(
        estimates, largestLocations);
    Map<LocalDate, List<Double>> ratiosWithoutOutliers = removeOutliers(ratios);
    SortedMap<ClientsKey, ClientsRanges> ranges = computeRanges(estimates,
        ratiosWithoutOutliers);
    writeOutputFile(estimates, ranges);
  }

  /** Read and return the parsed input file containing comma-separated estimates
   * of client numbers. */
  private static SortedMap<ClientsKey, ClientsEstimates> readInputFile()
      throws IOException {
    SortedMap<ClientsKey, ClientsEstimates> estimates = new TreeMap<>();
    File inputFile = INPUT_PATH.toFile();
    if (!inputFile.exists()) {
      throw new IOException(String.format("Input file %s does not exist.",
          inputFile));
    }
    try (LineNumberReader lnr = new LineNumberReader(
        new FileReader(inputFile))) {
      String line = lnr.readLine();
      if (!"date,node,country,transport,version,frac,users".equals(line)) {
        throw new IOException(String.format("Unable to read input file %s with "
            + "unrecognized header line '%s'. Not running detector.", inputFile,
            line));
      }
      while ((line = lnr.readLine()) != null) {
        ClientsKey key = null;
        ClientsEstimates value = null;
        boolean invalidLine = false;
        String[] lineParts = line.split(",");
        if (lineParts.length == 7) {
          try {
            LocalDate date = LocalDate.parse(lineParts[0]);
            boolean nodeIsRelay = false;
            if ("relay".equals(lineParts[1])) {
              nodeIsRelay = true;
            } else if (!"bridge".equals(lineParts[1])) {
              invalidLine = true;
            }
            String country = lineParts[2].replaceAll("\"", "");
            String transport = lineParts[3].replaceAll("\"", "");
            String version = lineParts[4].replaceAll("\"", "");
            key = new ClientsKey(date, nodeIsRelay, country, transport,
                version);
          } catch (DateTimeParseException e) {
            invalidLine = true;
          }
          try {
            int frac = Integer.parseInt(lineParts[5]);
            int clients = Integer.parseInt(lineParts[6]);
            value = new ClientsEstimates(clients, frac);
          } catch (NumberFormatException e) {
            invalidLine = true;
          }
        } else {
          invalidLine = true;
        }
        if (invalidLine) {
          throw new IOException(String.format(
              "Invalid line %d '%s' in input file %s.", lnr.getLineNumber(),
              line, inputFile));
        } else {
          estimates.put(key, value);
        }
      }
    }
    return estimates;
  }

  /** Return the NUM_LARGEST_LOCATIONS countries (except for "??") with the
   * largest number of estimated clients on the last known date in the input
   * data set.
   *
   * <p>Note that this implies that lower/upper values are going to change,
   * depending on which countries had most clients on the last known date in the
   * input data set.</p> */
  private static Set<String> findLargestLocations(
      SortedMap<ClientsKey, ClientsEstimates> clients) throws IOException {
    LocalDate lastKnownDate = clients.keySet().stream()
        .filter(c -> c.nodeIsRelay)
        .map(c -> c.date)
        .max(LocalDate::compareTo)
        .orElseThrow(() -> new IOException("Unable to find maximum date. Was "
            + "the input file empty or otherwise corrupt?"));
    return clients.entrySet().stream()
        .filter(c -> lastKnownDate.equals(c.getKey().date))
        .filter(c -> c.getKey().nodeIsRelay)
        .filter(c -> !"".equals(c.getKey().country))
        .filter(c -> !"??".equals(c.getKey().country))
        .sorted((c1, c2) -> Integer.compare(c2.getValue().clients,
            c1.getValue().clients))
        .map(c -> c.getKey().country)
        .limit(NUM_LARGEST_LOCATIONS)
        .collect(Collectors.toSet());
  }

  /** Compute the ratio of the client number estimate for a given date and
   * country as compared to 1 week before, for all dates, for relay users, and
   * for the largest locations. */
  private static Map<LocalDate, List<Double>> computeRatiosOfLargestLocations(
      SortedMap<ClientsKey, ClientsEstimates> estimates,
      Set<String> largestLocations) {
    Map<LocalDate, List<Double>> ratios = new HashMap<>();
    for (Map.Entry<ClientsKey, ClientsEstimates> numerator
        : estimates.entrySet()) {
      if (!numerator.getKey().nodeIsRelay
          || !largestLocations.contains(numerator.getKey().country)) {
        continue;
      }
      ClientsEstimates denominator = estimates.get(new ClientsKey(
          numerator.getKey().date.minusDays(INTERV), true,
          numerator.getKey().country));
      if (null == denominator || denominator.clients == 0) {
        continue;
      }
      if (!ratios.containsKey(numerator.getKey().date)) {
        ratios.put(numerator.getKey().date, new ArrayList<>());
      }
      ratios.get(numerator.getKey().date).add(
          ((double) numerator.getValue().clients)
              / (double) denominator.clients);
    }
    return ratios;
  }

  /** Exclude outliers from the given ratios by date that fall outside four
   * inter-quartile ranges of the median and make sure that at least 8 ratio
   * values remain. */
  private static SortedMap<LocalDate, List<Double>> removeOutliers(
      Map<LocalDate, List<Double>> ratios) {
    SortedMap<LocalDate, List<Double>> ratiosWithoutOutliers = new TreeMap<>();
    for (Map.Entry<LocalDate, List<Double>> e : ratios.entrySet()) {
      double[] values = e.getValue().stream().mapToDouble(Double::doubleValue)
          .toArray();
      Percentile percentile = new Percentile()
          .withEstimationType(Percentile.EstimationType.R_7);
      percentile.setData(values);
      double median = percentile.evaluate(50.0);
      double firstQuarter = percentile.evaluate(25.0);
      double thirdQuarter = percentile.evaluate(75.0);
      double interQuartileRange = thirdQuarter - firstQuarter;
      List<Double> valuesWithoutOutliers = new ArrayList<>();
      for (double value : values) {
        if (value > median - 4 * interQuartileRange
            && value < median + 4 * interQuartileRange) {
          valuesWithoutOutliers.add(value);
        }
      }
      if (valuesWithoutOutliers.size() < 8) {
        continue;
      }
      LocalDate date = e.getKey();
      ratiosWithoutOutliers.put(date, valuesWithoutOutliers);
    }
    return ratiosWithoutOutliers;
  }

  /** Compute ranges as the expected minimum and maximum number of users. */
  private static SortedMap<ClientsKey, ClientsRanges> computeRanges(
      SortedMap<ClientsKey, ClientsEstimates> estimates,
      Map<LocalDate, List<Double>> ratiosWithoutOutliers) {
    SortedMap<ClientsKey, ClientsRanges> ranges = new TreeMap<>();
    for (Map.Entry<ClientsKey, ClientsEstimates> estimatesEntry
        : estimates.entrySet()) {
      LocalDate date = estimatesEntry.getKey().date;
      if (!estimatesEntry.getKey().nodeIsRelay
          || "".equals(estimatesEntry.getKey().country)
          || "??".equals(estimatesEntry.getKey().country)
          || !ratiosWithoutOutliers.containsKey(date)) {
        continue;
      }
      ClientsEstimates referenceEstimate = estimates.get(
          new ClientsKey(date.minusDays(INTERV),
          true, estimatesEntry.getKey().country));
      if (null == referenceEstimate || referenceEstimate.clients == 0) {
        continue;
      }
      double[] values = ratiosWithoutOutliers.get(date).stream()
          .mapToDouble(Double::doubleValue).toArray();
      double mean = new Mean().evaluate(values);
      double std = new StandardDeviation(false).evaluate(values);
      NormalDistribution normalDistribution = new NormalDistribution(mean, std);
      PoissonDistribution poissonDistribution
          = new PoissonDistribution(referenceEstimate.clients);
      int lower = Math.max(0,
          (int) (normalDistribution.inverseCumulativeProbability(0.0001)
              * poissonDistribution.inverseCumulativeProbability(0.0001)));
      int upper =
          (int) (normalDistribution.inverseCumulativeProbability(0.9999)
              * poissonDistribution.inverseCumulativeProbability(0.9999));
      ranges.put(estimatesEntry.getKey(), new ClientsRanges(lower, upper));
    }
    return ranges;
  }

  /** Write client number estimates together with lower and upper bounds as
   * comma-separated values to the output file. */
  private static void writeOutputFile(
      SortedMap<ClientsKey, ClientsEstimates> estimates,
      SortedMap<ClientsKey, ClientsRanges> ranges) throws IOException {
    try (BufferedWriter bw = new BufferedWriter(
        new FileWriter(OUTPUT_PATH.toFile()))) {
      bw.write(
          "date,node,country,transport,version,lower,upper,clients,frac\n");
      for (Map.Entry<ClientsKey, ClientsEstimates> e : estimates.entrySet()) {
        String rangesString = ",";
        if (ranges.containsKey(e.getKey())) {
          rangesString = ranges.get(e.getKey()).toString();
        }
        bw.write(String.format("%s,%s,%s%n", e.getKey().toString(),
            rangesString, e.getValue().toString()));
      }
    }
  }
}

