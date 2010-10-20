package org.torproject.ernie.web;

import java.text.*;
import java.util.*;
import java.util.regex.*;

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
  private Map<String, String> availableGraphs;

  /* Known parameters and parameter values. */
  private Map<String, String> knownParameterValues;

  /**
   * Initializes map with valid parameters for each of the graphs.
   */
  public GraphParameterChecker() {
    this.dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    this.dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

    this.availableGraphs = new HashMap<String, String>();
    this.availableGraphs.put("networksize", "start,end,filename");
    this.availableGraphs.put("relayflags", "start,end,flag,filename");
    this.availableGraphs.put("relayflags-hour",
        "start,end,flag,filename");
    this.availableGraphs.put("versions", "start,end,filename");
    this.availableGraphs.put("platforms", "start,end,filename");
    this.availableGraphs.put("bandwidth", "start,end,filename");
    this.availableGraphs.put("dirbytes", "start,end,filename");
    this.availableGraphs.put("new-users", "start,end,country,filename");
    this.availableGraphs.put("direct-users",
        "start,end,country,filename");
    this.availableGraphs.put("bridge-users",
         "start,end,country,filename");
    this.availableGraphs.put("gettor", "start,end,bundle,filename");
    this.availableGraphs.put("torperf",
         "start,end,source,filesize,filename");
    this.availableGraphs.put("routerdetail",
         "start,end,fingerprint,filename");

    this.knownParameterValues = new HashMap<String, String>();
    this.knownParameterValues.put("flag",
        "Running,Exit,Guard,Fast,Stable");
    this.knownParameterValues.put("country", "all,au,bh,br,ca,cn,cu,de,"
        + "et,fr,gb,ir,it,jp,kr,mm,pl,ru,sa,se,sy,tn,tm,us,uz,vn,ye");
    this.knownParameterValues.put("bundle", "all,en,zh_CN,fa");
    this.knownParameterValues.put("source", "siv,moria,torperf");
    this.knownParameterValues.put("filesize", "50kb,1mb,5mb");
    this.knownParameterValues.put("fingerprint", "[0-9a-f]{40}");
  }

  /**
   * Checks request parameters for the given graph type and returns a map
   * of recognized parameters, or null if the graph type doesn't exist or
   * the parameters are invalid.
   */
  public Map<String, String[]> checkParameters(String graphType,
      Map requestParameters) {

    /* Check if the graph type exists. */
    if (graphType == null ||
        !this.availableGraphs.containsKey(graphType)) {
      return null;
    }

    /* Find out which other parameters are supported by this graph type
     * and parse them if they are given. */
    Set<String> supportedGraphParameters = new HashSet<String>(Arrays.
        asList(this.availableGraphs.get(graphType).split(",")));
    Map<String, String[]> recognizedGraphParameters =
        new HashMap<String, String[]>();

    /* Parse start and end dates if supported by the graph type. If
     * neither start nor end date are given, set the default date range to
     * the past 90 days. */
    if (supportedGraphParameters.contains("start") ||
        supportedGraphParameters.contains("end")) {
      String[] startParameter = (String[]) requestParameters.get("start");
      String[] endParameter = (String[]) requestParameters.get("end");
      if ((startParameter == null || startParameter.length < 1 ||
          startParameter[0].length() < 1) &&
          (endParameter == null || endParameter.length < 1 ||
          endParameter[0].length() < 1)) {
        /* If no start and end parameters are given, set default date
         * range to the past 90 days. */
        long now = System.currentTimeMillis();
        startParameter = new String[] {
            dateFormat.format(now - 90L * 24L * 60L * 60L * 1000L) };
        endParameter = new String[] { dateFormat.format(now) };
      } else if (startParameter != null && startParameter.length == 1 &&
          endParameter != null && endParameter.length == 1) {
        long startTimestamp = -1L, endTimestamp = -1L;
        try {
          startTimestamp = dateFormat.parse(startParameter[0]).getTime();
          endTimestamp = dateFormat.parse(endParameter[0]).getTime();
        } catch (ParseException e)  {
          return null;
        }
        /* The parameters are dates. Good. Does end not precede start? */
        if (startTimestamp > endTimestamp) {
          return null;
        }
        /* And while we're at it, make sure both parameters lie in this
         * century. */
        if (!startParameter[0].startsWith("20") ||
            !endParameter[0].startsWith("20")) {
          return null;
        }
        /* Looks like sane parameters. Re-format them to get a canonical
         * version, not something like 2010-1-1, 2010-01-1, etc. */
        startParameter = new String[] {
            dateFormat.format(startTimestamp) };
        endParameter = new String[] { dateFormat.format(endTimestamp) };
      } else {
        /* Either none or both of start and end need to be set. */
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
          if (flag == null || flag.length() == 0 ||
              !knownFlags.contains(flag)) {
            return null;
          }
        }
      } else {
        flagParameters = this.knownParameterValues.get("flag").split(",");
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
        for (String country : countryParameters) {
          if (country == null || country.length() == 0 ||
              !knownCountries.contains(country)) {
            return null;
          }
        }
      } else {
        countryParameters = new String[] { "all" };
      }
      recognizedGraphParameters.put("country", countryParameters);
    }

    /* Parse GetTor bundle if supported by the graph type. Only a single
     * bundle can be passed. If no bundle is passed, use "all" as
     * default. */
    if (supportedGraphParameters.contains("bundle")) {
      String[] bundleParameter = (String[]) requestParameters.get(
          "bundle");
      List<String> knownBundles = Arrays.asList(
          this.knownParameterValues.get("bundle").split(","));
      if (bundleParameter != null) {
        if (bundleParameter.length != 1 ||
            bundleParameter[0].length() == 0 ||
            !knownBundles.contains(bundleParameter[0])) {
          return null;
        }
      } else {
        bundleParameter = new String[] { "all" };
      }
      recognizedGraphParameters.put("bundle", bundleParameter);
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
        if (sourceParameter[0].length() == 0 ||
            !knownSources.contains(sourceParameter[0])) {
          return null;
        }
      } else {
        sourceParameter = new String[] { "torperf" };
      }
      recognizedGraphParameters.put("source", sourceParameter);
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
        if (filesizeParameter[0].length() == 0 ||
            !knownFilesizes.contains(filesizeParameter[0])) {
          return null;
        }
      } else {
        filesizeParameter = new String[] { "50kb" };
      }
      recognizedGraphParameters.put("filesize", filesizeParameter);
    }

    /* Parse fingerprint field for the torstatus graph. Match it against
     * a hexadecimal regular expression and make sure it is 40 characters
     * long. */
    if (supportedGraphParameters.contains("fingerprint")) {
      String[] fingerprint = (String[])requestParameters.get("fingerprint");
      if (fingerprint != null) {
        if (!Pattern.matches(this.knownParameterValues.get("fingerprint"),
            fingerprint[0]) || fingerprint[0].length() != 40) {
          return null;
        }
      } else {
        return null;
      }

      /* Set "mandatory" start and end parameters to this hour so it stays up to
       * date (the start and end parameters aren't needed for this graph).
       * Round the timestamp to the lowest hour */
      long msDay = 1000 * 60 * 60;
      long now = System.currentTimeMillis();
      long nearestHour = now - (now % msDay);

      String startParameter[] = { Long.toString(nearestHour) };
      String endParameter[] = { "" };
      recognizedGraphParameters.put("start", startParameter);
      recognizedGraphParameters.put("end", endParameter);
      recognizedGraphParameters.put("fingerprint", fingerprint);
    }

    /* We now have a map with all required graph parameters. Return it. */
    return recognizedGraphParameters;
  }
}

