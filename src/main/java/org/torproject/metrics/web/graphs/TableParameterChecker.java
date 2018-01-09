/* Copyright 2011--2018 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.web.graphs;

import org.torproject.metrics.web.ContentProvider;
import org.torproject.metrics.web.Metric;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

/**
 * Checks request parameters passed to generate tables.
 */
public class TableParameterChecker {

  /**
   * Singleton instance of this class.
   */
  private static TableParameterChecker instance =
      new TableParameterChecker();

  /**
   * Returns the singleton instance of this class.
   */
  public static TableParameterChecker getInstance() {
    return instance;
  }

  /* Date format for parsing start and end dates. */
  private SimpleDateFormat dateFormat;

  /* Available tables with corresponding parameter lists. */
  private Map<String, String[]> availableTables;

  /**
   * Initializes map with valid parameters for each of the graphs.
   */
  public TableParameterChecker() {
    this.dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    this.dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

    this.availableTables = new HashMap<>();
    for (Metric metric : ContentProvider.getInstance().getMetricsList()) {
      if ("Table".equals(metric.getType())) {
        this.availableTables.put(metric.getId(), metric.getParameters());
      }
    }
  }

  /**
   * Checks request parameters for the given table type and returns a map
   * of recognized parameters, or null if the table type doesn't exist or
   * the parameters are invalid.
   */
  public Map<String, String[]> checkParameters(String tableType,
      Map requestParameters) {

    /* Check if the table type exists. */
    if (tableType == null
        || !this.availableTables.containsKey(tableType)) {
      return null;
    }

    /* Find out which other parameters are supported by this table type
     * and parse them if they are given. */
    Set<String> supportedTableParameters = new HashSet<>(
        Arrays.asList(this.availableTables.get(tableType)));
    Map<String, String[]> recognizedTableParameters = new HashMap<>();

    /* Parse start and end dates if supported by the table type. If no end
     * date is provided, set it to today. If no start date is provided,
     * set it to 90 days before the end date. Make sure that start date
     * precedes end date. */
    if (supportedTableParameters.contains("start")
        || supportedTableParameters.contains("end")) {
      String[] startParameter = null;
      String[] endParameter = null;
      if (requestParameters != null) {
        startParameter = (String[]) requestParameters.get("start");
        endParameter = (String[]) requestParameters.get("end");
      }
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
      recognizedTableParameters.put("start", startParameter);
      recognizedTableParameters.put("end", endParameter);
    }

    /* We now have a map with all required table parameters. Return it. */
    return recognizedTableParameters;
  }
}

