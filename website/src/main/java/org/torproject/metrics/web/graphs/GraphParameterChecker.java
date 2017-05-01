/* Copyright 2011--2017 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.web.graphs;

import org.torproject.metrics.web.ContentProvider;
import org.torproject.metrics.web.Metric;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

/**
 * Checks request parameters passed to graph-generating servlets.
 */
public class GraphParameterChecker {

  /**
   * Singleton instance of this class.
   */
  private static GraphParameterChecker instance =
      new GraphParameterChecker();

  /**
   * Returns the singleton instance of this class.
   */
  public static GraphParameterChecker getInstance() {
    return instance;
  }

  /* Date format for parsing start and end dates. */
  private SimpleDateFormat dateFormat;

  /* Available graphs with corresponding parameter lists. */
  private Map<String, String[]> availableGraphs;

  /* Known parameters and parameter values. */
  private Map<String, String> knownParameterValues;

  /**
   * Initializes map with valid parameters for each of the graphs.
   */
  public GraphParameterChecker() {
    this.dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    this.dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    this.availableGraphs = new HashMap<>();
    for (Metric metric : ContentProvider.getInstance().getMetricsList()) {
      if ("Graph".equals(metric.getType())) {
        this.availableGraphs.put(metric.getId(), metric.getParameters());
      }
    }
    this.knownParameterValues = new HashMap<>();
    this.knownParameterValues.put("flag",
        "Running,Exit,Guard,Fast,Stable,HSDir");
    StringBuilder sb = new StringBuilder("all");
    for (String[] country : Countries.getInstance().getCountryList()) {
      sb.append("," + country[0]);
    }
    this.knownParameterValues.put("country", sb.toString());
    this.knownParameterValues.put("events", "on,off,points");
    this.knownParameterValues.put("source", "all,siv,moria,torperf,op-hk,"
        + "op-nl,op-us");
    this.knownParameterValues.put("server", "public,onion");
    this.knownParameterValues.put("filesize", "50kb,1mb,5mb");
    this.knownParameterValues.put("transport", "obfs2,obfs3,obfs4,"
        + "websocket,fte,meek,scramblesuit,snowflake,<OR>,<??>,!<OR>");
    this.knownParameterValues.put("version", "v4,v6");
    this.knownParameterValues.put("p", "100,99,98,97,95,91,90,80,75,70,"
        + "60,50,40,30,25,20,10,9,5,3,2,1,0");
    this.knownParameterValues.put("n", "1,2,3,5,10,20,30,50,100,200,300,"
        + "500,1000,2000,3000,5000");
  }

  /**
   * Checks request parameters for the given graph type and returns a map
   * of recognized parameters, or null if the graph type doesn't exist or
   * the parameters are invalid.
   */
  @SuppressWarnings("checkstyle:localvariablename")
  public Map<String, String[]> checkParameters(String graphType,
      Map requestParameters) {

    /* Check if the graph type exists. */
    if (graphType == null
        || !this.availableGraphs.containsKey(graphType)) {
      return null;
    }

    /* Find out which other parameters are supported by this graph type
     * and parse them if they are given. */
    Set<String> supportedGraphParameters = new HashSet<>(
        Arrays.asList(this.availableGraphs.get(graphType)));
    Map<String, String[]> recognizedGraphParameters = new HashMap<>();

    /* Parse start and end dates if supported by the graph type. If no end
     * date is provided, set it to today. If no start date is provided,
     * set it to 90 days before the end date. Make sure that start date
     * precedes end date. */
    if (supportedGraphParameters.contains("start")
        || supportedGraphParameters.contains("end")) {
      String[] startParameter = (String[]) requestParameters.get("start");
      String[] endParameter = (String[]) requestParameters.get("end");
      long endTimestamp = System.currentTimeMillis();
      if (endParameter != null && endParameter.length > 0
          && endParameter[0].length() > 0) {
        try {
          endTimestamp = dateFormat.parse(endParameter[0]).getTime();
        } catch (ParseException e)  {
          return null;
        }
        if (!endParameter[0].startsWith("20")) {
          return null;
        }
      }
      endParameter = new String[] { dateFormat.format(endTimestamp) };
      long startTimestamp = endTimestamp - 90L * 24L * 60L * 60L * 1000L;
      if (startParameter != null && startParameter.length > 0
          && startParameter[0].length() > 0) {
        try {
          startTimestamp = dateFormat.parse(startParameter[0]).getTime();
        } catch (ParseException e)  {
          return null;
        }
        if (!startParameter[0].startsWith("20")) {
          return null;
        }
      }
      startParameter = new String[] { dateFormat.format(startTimestamp) };
      if (startTimestamp > endTimestamp) {
        return null;
      }
      recognizedGraphParameters.put("start", startParameter);
      recognizedGraphParameters.put("end", endParameter);
    }

    /* Parse relay flags if supported by the graph type. If no relay flags
     * are passed or none of them have been recognized, use the set of all
     * known flags as default. */
    if (supportedGraphParameters.contains("flag")) {
      String[] flagParameters = (String[]) requestParameters.get("flag");
      List<String> knownFlags = Arrays.asList(
          this.knownParameterValues.get("flag").split(","));
      if (flagParameters != null) {
        for (String flag : flagParameters) {
          if (flag == null || flag.length() == 0
              || !knownFlags.contains(flag)) {
            return null;
          }
        }
      } else {
        flagParameters = "Running,Exit,Guard,Fast,Stable".split(",");
      }
      recognizedGraphParameters.put("flag", flagParameters);
    }

    /* Parse country codes if supported by the graph type. If no countries
     * are passed, use country code "all" (all countries) as default. */
    if (supportedGraphParameters.contains("country")) {
      String[] countryParameters = (String[]) requestParameters.get(
          "country");
      List<String> knownCountries = Arrays.asList(
          this.knownParameterValues.get("country").split(","));
      if (countryParameters != null) {
        if (countryParameters.length != 1) {
          return null;
        }
        for (String country : countryParameters) {
          if (country == null || country.length() == 0
              || !knownCountries.contains(country)) {
            return null;
          }
        }
      } else {
        countryParameters = new String[] { "all" };
      }
      recognizedGraphParameters.put("country", countryParameters);
    }

    /* Parse whether the estimated min/max range shall be displayed if
     * supported by the graph type. This parameter can either be "on" or
     * "off," where "off" is the default. */
    if (supportedGraphParameters.contains("events")) {
      String[] eventsParameter = (String[]) requestParameters.get(
          "events");
      List<String> knownRanges = Arrays.asList(
          this.knownParameterValues.get("events").split(","));
      if (eventsParameter != null) {
        if (eventsParameter.length != 1
            || eventsParameter[0].length() == 0
            || !knownRanges.contains(eventsParameter[0])) {
          return null;
        }
      } else {
        eventsParameter = new String[] { "off" };
      }
      recognizedGraphParameters.put("events", eventsParameter);
    }

    /* Parse torperf data source if supported by the graph type. Only a
     * single source can be passed. If no source is passed, use "torperf"
     * as default. */
    if (supportedGraphParameters.contains("source")) {
      String[] sourceParameter = (String[]) requestParameters.get(
          "source");
      List<String> knownSources = Arrays.asList(
          this.knownParameterValues.get("source").split(","));
      if (sourceParameter != null) {
        if (sourceParameter.length != 1) {
          return null;
        }
        if (sourceParameter[0].length() == 0
            || !knownSources.contains(sourceParameter[0])) {
          return null;
        }
      } else {
        sourceParameter = new String[] { "all" };
      }
      recognizedGraphParameters.put("source", sourceParameter);
    }

    /* Parse onionperf server if supported by the graph type. Only a single
     * server can be passed. If no server is passed, use "public" as default. */
    if (supportedGraphParameters.contains("server")) {
      String[] serverParameter = (String[]) requestParameters.get("server");
      List<String> knownServers = Arrays.asList(
          this.knownParameterValues.get("server").split(","));
      if (serverParameter != null) {
        if (serverParameter.length != 1) {
          return null;
        }
        if (serverParameter[0].length() == 0
            || !knownServers.contains(serverParameter[0])) {
          return null;
        }
      } else {
        serverParameter = new String[] { "public" };
      }
      recognizedGraphParameters.put("server", serverParameter);
    }

    /* Parse torperf file size if supported by the graph type. Only a
     * single file size can be passed. If no file size is passed, use
     * "50kb" as default. */
    if (supportedGraphParameters.contains("filesize")) {
      String[] filesizeParameter = (String[]) requestParameters.get(
          "filesize");
      List<String> knownFilesizes = Arrays.asList(
          this.knownParameterValues.get("filesize").split(","));
      if (filesizeParameter != null) {
        if (filesizeParameter.length != 1) {
          return null;
        }
        if (filesizeParameter[0].length() == 0
            || !knownFilesizes.contains(filesizeParameter[0])) {
          return null;
        }
      } else {
        filesizeParameter = new String[] { "50kb" };
      }
      recognizedGraphParameters.put("filesize", filesizeParameter);
    }

    /* Parse transports if supported by the graph type. If no transports
     * are passed, use "<OR>" as default. */
    if (supportedGraphParameters.contains("transport")) {
      String[] transportParameters = (String[]) requestParameters.get(
          "transport");
      List<String> knownTransports = Arrays.asList(
          this.knownParameterValues.get("transport").split(","));
      if (transportParameters != null) {
        for (String transport : transportParameters) {
          if (transport == null || transport.length() == 0
              || !knownTransports.contains(transport)) {
            return null;
          }
        }
      } else {
        transportParameters = new String[] { "!<OR>" };
      }
      recognizedGraphParameters.put("transport", transportParameters);
    }

    /* Parse versions if supported by the graph type. If no versions
     * are passed, use "v4" as default. */
    if (supportedGraphParameters.contains("version")) {
      String[] versionParameters = (String[]) requestParameters.get(
          "version");
      List<String> knownVersions = Arrays.asList(
          this.knownParameterValues.get("version").split(","));
      if (versionParameters != null) {
        if (versionParameters.length != 1) {
          return null;
        }
        for (String version : versionParameters) {
          if (version == null || version.length() == 0
              || !knownVersions.contains(version)) {
            return null;
          }
        }
      } else {
        versionParameters = new String[] { "v4" };
      }
      recognizedGraphParameters.put("version", versionParameters);
    }

    /* Parse p if supported by the graph type. If no p's are passed, use
     * "100" as default. */
    if (supportedGraphParameters.contains("p")) {
      String[] pParameters = (String[]) requestParameters.get("p");
      if (pParameters != null) {
        List<String> knownPs = Arrays.asList(
            this.knownParameterValues.get("p").split(","));
        for (String p : pParameters) {
          if (p == null || p.length() == 0 || !knownPs.contains(p)) {
            return null;
          }
        }
      } else {
        pParameters = new String[] { "100" };
      }
      recognizedGraphParameters.put("p", pParameters);
    }

    /* Parse n if supported by the graph type. If no n's are passed, use
     * "1" as default. */
    if (supportedGraphParameters.contains("n")) {
      String[] nParameters = (String[]) requestParameters.get("n");
      if (nParameters != null) {
        List<String> knownNs = Arrays.asList(
            this.knownParameterValues.get("n").split(","));
        for (String n : nParameters) {
          if (n == null || n.length() == 0 || !knownNs.contains(n)) {
            return null;
          }
        }
      } else {
        nParameters = new String[] { "1" };
      }
      recognizedGraphParameters.put("n", nParameters);
    }

    /* We now have a map with all required graph parameters. Return it. */
    return recognizedGraphParameters;
  }
}

