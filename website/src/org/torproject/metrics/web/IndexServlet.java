/* Copyright 2011--2015 The Tor Project
 * See LICENSE for licensing information */
package org.torproject.metrics.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class IndexServlet extends HttpServlet {

  private static final String[][] knownTags = new String[][] {
    { "cl", "Clients" },
    { "rl", "Relays" },
    { "br", "Bridges" },
    { "pt", "Pluggable transports" },
    { "hs", "Hidden services" },
    { "bw", "Bandwidth" },
    { "pf", "Performance" },
    { "dv", "Diversity" }
  };
  private static final String[] defaultTags =
      new String[] { "cl", "rl", "br", "pt", "hs", "bw", "pf", "dv" };

  private static final String[][] knownTypes = new String[][] {
    { "gr", "Graph" },
    { "tb", "Table" },
    { "ln", "Link" },
    { "dt", "Data" }
  };
  private static final String[] defaultTypes =
      new String[] { "gr", "tb", "ln", "dt" };

  private static final String[][] knownLevels = new String[][] {
    { "bs", "Basic" },
    { "ad", "Advanced" }
  };
  private static final String[] defaultLevels = new String[] { "bs" };

  private static final String[][] knownOrders = new String[][] {
    { "name", "Name" },
    { "tags", "Tags" },
    { "type", "Type" },
    { "level", "Level" },
    { "shuffle", "None (shuffle)" }
  };
  private static final String[] defaultOrders = new String[] { "type" };

  private final static List<Metric> availableMetrics;
  static {
    availableMetrics = new ArrayList<Metric>();
    availableMetrics.add(new Metric("networksize.html",
        "Relays and bridges in the network",
        new String[] { "Relays", "Bridges" }, "Graph", "Basic"));
    availableMetrics.add(new Metric("relayflags.html",
        "Relays with Exit, Fast, Guard, Stable, and HSDir flags",
        new String[] { "Relays" }, "Graph", "Basic"));
    availableMetrics.add(new Metric("versions.html",
        "Relays by version", new String[] { "Relays", "Diversity" },
        "Graph", "Basic"));
    availableMetrics.add(new Metric("platforms.html",
        "Relays by platform", new String[] { "Relays", "Diversity" },
        "Graph", "Basic"));
    availableMetrics.add(new Metric("cloudbridges.html",
        "Tor Cloud bridges", new String[] { "Bridges" }, "Graph",
        "Basic"));
    availableMetrics.add(new Metric("servers-data.html",
        "Number of relays and bridges",
        new String[] { "Relays", "Bridges", "Diversity" }, "Data",
        "Advanced"));
    availableMetrics.add(new Metric("bandwidth.html",
        "Total relay bandwidth in the network",
        new String[] { "Relays", "Bandwidth" }, "Graph", "Basic"));
    availableMetrics.add(new Metric("bwhist-flags.html",
        "Relay bandwidth by Exit and/or Guard flags",
        new String[] { "Relays", "Bandwidth" }, "Graph", "Basic"));
    availableMetrics.add(new Metric("bandwidth-flags.html",
        "Advertised bandwidth and bandwidth history by relay flags",
        new String[] { "Relays", "Bandwidth" }, "Graph", "Basic"));
    availableMetrics.add(new Metric("dirbytes.html",
        "Number of bytes spent on answering directory requests",
        new String[] { "Relays", "Bandwidth" }, "Graph", "Basic"));
    availableMetrics.add(new Metric("advbwdist-perc.html",
        "Advertised bandwidth distribution",
        new String[] { "Relays", "Bandwidth" }, "Graph", "Basic"));
    availableMetrics.add(new Metric("advbwdist-relay.html",
        "Advertised bandwidth of n-th fastest relays",
        new String[] { "Relays", "Bandwidth" }, "Graph", "Basic"));
    availableMetrics.add(new Metric("bandwidth-data.html",
        "Bandwidth provided and consumed by relays",
        new String[] { "Relays", "Bandwidth" }, "Data", "Advanced"));
    availableMetrics.add(new Metric("advbwdist-data.html",
        "Advertised bandwidth distribution and n-th fastest relays",
        new String[] { "Relays", "Bandwidth" }, "Data", "Advanced"));
    availableMetrics.add(new Metric("bubbles.html",
        "Network bubble graphs", new String[] { "Relays", "Diversity" },
        "Graph", "Basic"));
    availableMetrics.add(new Metric("userstats-relay-country.html",
        "Direct users by country", new String[] { "Clients" }, "Graph",
        "Basic"));
    availableMetrics.add(new Metric("userstats-relay-table.html",
        "Top-10 countries by directly connecting users",
        new String[] { "Clients" }, "Table", "Basic"));
    availableMetrics.add(new Metric("userstats-censorship-events.html",
        "Top-10 countries by possible censorship events",
        new String[] { "Clients" }, "Table", "Basic"));
    availableMetrics.add(new Metric("userstats-bridge-country.html",
        "Bridge users by country", new String[] { "Clients" }, "Graph",
        "Basic"));
    availableMetrics.add(new Metric("userstats-bridge-table.html",
        "Top-10 countries by bridge users", new String[] { "Clients" },
        "Table", "Basic"));
    availableMetrics.add(new Metric("userstats-bridge-transport.html",
        "Bridge users by transport",
        new String[] { "Clients", "Pluggable transports" }, "Graph",
        "Basic"));
    availableMetrics.add(new Metric("userstats-bridge-version.html",
        "Bridge users by IP version", new String[] { "Clients" }, "Graph",
        "Basic"));
    availableMetrics.add(new Metric("oxford-anonymous-internet.html",
        "Tor users as percentage of larger Internet population",
        new String[] { "Clients" }, "Link", "Basic"));
    availableMetrics.add(new Metric("clients-data.html",
        "Estimated number of clients in the Tor network",
        new String[] { "Clients", "Pluggable transports" }, "Data",
        "Advanced"));
    availableMetrics.add(new Metric("torperf.html",
        "Time to download files over Tor", new String[] { "Performance" },
        "Graph", "Basic"));
    availableMetrics.add(new Metric("torperf-failures.html",
        "Timeouts and failures of downloading files over Tor",
        new String[] { "Performance" }, "Graph", "Advanced"));
    availableMetrics.add(new Metric("connbidirect.html",
        "Fraction of connections used uni-/bidirectionally",
        new String[] { "Performance" }, "Graph", "Advanced"));
    availableMetrics.add(new Metric("torperf-data.html",
        "Performance of downloading static files over Tor",
        new String[] { "Performance" }, "Data", "Advanced"));
    availableMetrics.add(new Metric("connbidirect-data.html",
        "Fraction of connections used uni-/bidirectionally (deprecated)",
        new String[] { "Performance" }, "Data", "Advanced"));
    availableMetrics.add(new Metric("connbidirect2-data.html",
        "Fraction of connections used uni-/bidirectionally",
        new String[] { "Performance" }, "Data", "Advanced"));
    availableMetrics.add(new Metric("hidserv-dir-onions-seen.html",
        "Unique .onion addresses", new String[] { "Hidden services" },
        "Graph", "Basic"));
    availableMetrics.add(new Metric("hidserv-rend-relayed-cells.html",
        "Hidden-service traffic", new String[] { "Hidden services" },
        "Graph", "Basic"));
    availableMetrics.add(new Metric("hidserv-frac-reporting.html",
        "Fraction of relays reporting hidden-service statistics",
        new String[] { "Hidden services" }, "Graph", "Advanced"));
    availableMetrics.add(new Metric("hidserv-data.html",
        "Hidden-service statistics", new String[] { "Hidden services" },
        "Data", "Advanced"));
    availableMetrics.add(new Metric("uncharted-data-flow.html",
        "Data flow in the Tor network", new String[] { "Relays",
        "Hidden services", "Bandwidth" }, "Link", "Basic"));
  }

  public void doGet(HttpServletRequest request,
      HttpServletResponse response) throws IOException, ServletException {
    @SuppressWarnings("rawtypes")
    Map parameterMap = request.getParameterMap();
    BitSet requestedTags = this.parseParameter(
        (String[]) parameterMap.get("tag"), knownTags, defaultTags);
    BitSet requestedTypes = this.parseParameter(
        (String[]) parameterMap.get("type"), knownTypes, defaultTypes);
    BitSet requestedLevels = this.parseParameter(
        (String[]) parameterMap.get("level"), knownLevels, defaultLevels);
    BitSet requestedOrder = this.parseParameter(
        (String[]) parameterMap.get("order"), knownOrders, defaultOrders);
    request.setAttribute("tags", this.formatParameter(knownTags,
        requestedTags));
    request.setAttribute("types", this.formatParameter(knownTypes,
        requestedTypes));
    request.setAttribute("levels", this.formatParameter(knownLevels,
        requestedLevels));
    request.setAttribute("order", this.formatParameter(knownOrders,
        requestedOrder));
    List<Metric> filteredAndOrderedMetrics = this.filterMetrics(
        requestedTags, requestedTypes, requestedLevels);
    this.orderMetrics(filteredAndOrderedMetrics, requestedOrder);
    request.setAttribute("results", this.formatMetrics(
        filteredAndOrderedMetrics));
    request.getRequestDispatcher("WEB-INF/index.jsp").forward(request,
        response);
  }

  private BitSet parseParameter(String[] unparsedValues,
      String[][] knownValues, String[] defaultValues) {
    BitSet result = new BitSet();
    if (unparsedValues == null || unparsedValues.length == 0 ||
        unparsedValues.length > knownValues.length) {
      unparsedValues = defaultValues;
    }
    Set<String> requestedValues =
        new HashSet<String>(Arrays.asList(unparsedValues));
    for (int i = 0; i < knownValues.length; i++) {
      if (requestedValues.contains(knownValues[i][0])) {
        result.set(i);
      }
    }
    return result;
  }

  private String[][] formatParameter(String[][] strings, BitSet bitSet) {
    String[][] formattedParameter = new String[strings.length][];
    for (int i = 0; i < formattedParameter.length; i++) {
      String[] formatted = new String[] { strings[i][0], strings[i][1],
          "" };
      if (bitSet.get(i)) {
        formatted[2] = " checked";
      }
      formattedParameter[i] = formatted;
    }
    return formattedParameter;
  }

  private static class Metric {
    private String url;
    private String name;
    private BitSet tags;
    private BitSet type;
    private BitSet level;
    private Metric(String url, String name, String[] tagStrings,
        String typeString, String levelString) {
      this.url = url;
      this.name = name;
      this.tags = this.convertStringsToBitSet(knownTags, tagStrings);
      this.type = this.convertStringToBitSet(knownTypes, typeString);
      this.level = this.convertStringToBitSet(knownLevels, levelString);
    }
    private BitSet convertStringsToBitSet(String[][] knownKeysAndValues,
        String[] givenKeyStrings) {
      BitSet result = new BitSet(knownKeysAndValues.length);
      Set<String> keys = new HashSet<String>(Arrays.asList(
          givenKeyStrings));
      for (int i = 0; i < knownKeysAndValues.length; i++) {
        if (keys.contains(knownKeysAndValues[i][1])) {
          result.set(i);
        }
      }
      if (result.cardinality() != givenKeyStrings.length) {
        throw new RuntimeException("Unknown key(s): " + keys);
      }
      return result;
    }
    private BitSet convertStringToBitSet(String[][] knownKeysAndValues,
        String givenKeyString) {
      return this.convertStringsToBitSet(knownKeysAndValues,
          new String[] { givenKeyString });
    }
    private String[] toStrings() {
      return new String[] { this.url, this.name,
          this.convertBitSetToString(knownTags, this.tags),
          this.convertBitSetToString(knownTypes, this.type),
          this.convertBitSetToString(knownLevels, this.level) };
    }
    private String convertBitSetToString(String[][] knownKeysAndValues,
        BitSet bitSet) {
      StringBuilder sb = new StringBuilder();
      int i = -1;
      while ((i = bitSet.nextSetBit(i + 1)) >= 0) {
        sb.append(", " + knownKeysAndValues[i][1]);
      }
      return sb.substring(Math.min(sb.length(), 2));
    }
  }

  private List<Metric> filterMetrics(BitSet requestedTags,
      BitSet requestedTypes, BitSet requestedLevels) {
    List<Metric> filteredMetrics = new ArrayList<Metric>();
    for (Metric metric : availableMetrics) {
      if (requestedTags.intersects(metric.tags) &&
          requestedTypes.intersects(metric.type) &&
          requestedLevels.intersects(metric.level)) {
        filteredMetrics.add(metric);
      }
    }
    return filteredMetrics;
  }

  private void orderMetrics(List<Metric> resultMetrics,
      BitSet requestedOrder) {
    switch (requestedOrder.nextSetBit(0)) {
    case 0:
      Collections.sort(resultMetrics, new Comparator<Metric>() {
        public int compare(Metric a, Metric b) {
          return a.name.compareTo(b.name);
        }
      });
      break;
    case 1:
      Collections.sort(resultMetrics, new Comparator<Metric>() {
        public int compare(Metric a, Metric b) {
          return compareTwoBitSets(a.tags, b.tags);
        }
      });
      break;
    case 2:
      Collections.sort(resultMetrics, new Comparator<Metric>() {
        public int compare(Metric a, Metric b) {
          return compareTwoBitSets(a.type, b.type);
        }
      });
      break;
    case 3:
      Collections.sort(resultMetrics, new Comparator<Metric>() {
        public int compare(Metric a, Metric b) {
          return compareTwoBitSets(a.level, b.level);
        }
      });
      break;
    default:
      Collections.shuffle(resultMetrics);
      break;
    }
  }

  private int compareTwoBitSets(BitSet a, BitSet b) {
    if (a.equals(b)) {
      return 0;
    }
    BitSet xor = (BitSet) a.clone();
    xor.xor(b);
    return xor.length() == b.length() ? -1 : 1;
  }

  private String[][] formatMetrics(
      List<Metric> filteredAndOrderedMetrics) {
    String[][] formattedMetrics =
        new String[filteredAndOrderedMetrics.size()][];
    for (int i = 0; i < formattedMetrics.length; i++) {
      formattedMetrics[i] = filteredAndOrderedMetrics.get(i).toStrings();
    }
    return formattedMetrics;
  }
}
