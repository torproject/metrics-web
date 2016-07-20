/* Copyright 2011--2016 The Tor Project
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

  private List<Metric> availableMetrics;

  @Override
  public void init() throws ServletException {
    this.availableMetrics = new ArrayList<Metric>();
    for (org.torproject.metrics.web.Metric metric
        : MetricsProvider.getInstance().getMetricsList()) {
      this.availableMetrics.add(new Metric(metric.getId() + ".html",
          metric.getTitle(), metric.getTags(), metric.getType(),
          metric.getLevel()));
    }
  }

  @Override
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
    if (unparsedValues == null || unparsedValues.length == 0
        || unparsedValues.length > knownValues.length) {
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
      int index = -1;
      while ((index = bitSet.nextSetBit(index + 1)) >= 0) {
        sb.append(", " + knownKeysAndValues[index][1]);
      }
      return sb.substring(Math.min(sb.length(), 2));
    }
  }

  private List<Metric> filterMetrics(BitSet requestedTags,
      BitSet requestedTypes, BitSet requestedLevels) {
    List<Metric> filteredMetrics = new ArrayList<Metric>();
    for (Metric metric : availableMetrics) {
      if (requestedTags.intersects(metric.tags)
          && requestedTypes.intersects(metric.type)
          && requestedLevels.intersects(metric.level)) {
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
          public int compare(Metric first, Metric second) {
            return first.name.compareTo(second.name);
          }
        });
        break;
      case 1:
        Collections.sort(resultMetrics, new Comparator<Metric>() {
          public int compare(Metric first, Metric second) {
            return compareTwoBitSets(first.tags, second.tags);
          }
        });
        break;
      case 2:
        Collections.sort(resultMetrics, new Comparator<Metric>() {
          public int compare(Metric first, Metric second) {
            return compareTwoBitSets(first.type, second.type);
          }
        });
        break;
      case 3:
        Collections.sort(resultMetrics, new Comparator<Metric>() {
          public int compare(Metric first, Metric second) {
            return compareTwoBitSets(first.level, second.level);
          }
        });
        break;
      default:
        Collections.shuffle(resultMetrics);
        break;
    }
  }

  private int compareTwoBitSets(BitSet first, BitSet second) {
    if (first.equals(second)) {
      return 0;
    }
    BitSet xor = (BitSet) first.clone();
    xor.xor(second);
    return xor.length() == second.length() ? -1 : 1;
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
