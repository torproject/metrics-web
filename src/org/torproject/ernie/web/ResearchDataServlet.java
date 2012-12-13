/* Copyright 2011, 2012 The Tor Project
 * See LICENSE for licensing information */
package org.torproject.ernie.web;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Controller servlet for the Data page. Prepares the various lists of
 * downloadable metrics data files by parsing a file with URLs on other
 * servers and looking at a local directory with files served by local
 * Apache HTTP server. The file with URLs on other servers may contain
 * comment lines starting with #. Recognizes metrics data file types from
 * the file names.
 */
public class ResearchDataServlet extends HttpServlet {

  private static final long serialVersionUID = -5168280373350515577L;

  public void doGet(HttpServletRequest request,
      HttpServletResponse response) throws IOException, ServletException {

    /* Read local directory with files served by the local Apache HTTP
     * server and add the URLs to the list. */
    List<String> dataFileUrls = new ArrayList<String>();
    String localDataDir = getServletConfig().getInitParameter(
        "localDataDir");
    if (localDataDir != null) {
      try {
        File localDataDirFile = new File(localDataDir);
        if (localDataDirFile.exists() && localDataDirFile.isDirectory()) {
          for (File localDataFile : localDataDirFile.listFiles()) {
            if (!localDataFile.isDirectory()) {
              dataFileUrls.add("/data/" + localDataFile.getName());
            }
          }
        }
      } catch (SecurityException e) {
        /* We're not permitted to read the directory with metrics data
         * files. Ignore. */
      }
    }

    /* Prepare data structures that we're going to pass to the JSP. All
     * data structures are (nested) maps with the map keys being used for
     * displaying the files in tables and map values being 2-element
     * arrays containing the file url and optional signature file. */
    SortedMap<Date, Map<String, String[]>> relayDescriptors =
        new TreeMap<Date, Map<String, String[]>>(
        java.util.Collections.reverseOrder());
    String[] certs = new String[2];
    SortedMap<Date, String[]> bridgeDescriptors =
        new TreeMap<Date, String[]>(java.util.Collections.reverseOrder());
    String[] relayStatistics = new String[2];
    SortedMap<Date, String[]> torperfTarballs =
        new TreeMap<Date, String[]>(java.util.Collections.reverseOrder());
    SortedMap<String, Map<String, String[]>> torperfData =
        new TreeMap<String, Map<String, String[]>>();
    SortedMap<Date, String[]> exitLists =
        new TreeMap<Date, String[]>(java.util.Collections.reverseOrder());
    SortedMap<Date, String[]> torperfExperiments =
        new TreeMap<Date, String[]>();
    SortedMap<Date, String[]> bridgePoolAssignments =
        new TreeMap<Date, String[]>(java.util.Collections.reverseOrder());

    /* Prepare rewriting Torperf sources. */
    Map<String, String> torperfSources = new HashMap<String, String>();
    torperfSources.put("torperffast", "torperf, fastest");
    torperfSources.put("torperffastratio", "torperf, best ratio");
    torperfSources.put("torperfslow", "torperf, slowest");
    torperfSources.put("torperfslowratio", "torperf, worst ratio");

    /* Go through the file list, decide for each file what metrics data
     * type it is, and put it in the appropriate map. */
    SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM");
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    List<String> torperfFilesizes = Arrays.asList("50kb,1mb,5mb".
        split(","));
    for (String url : dataFileUrls) {
      if (!url.contains("/")) {
        continue;
      }
      String filename = url.substring(url.lastIndexOf("/") + 1);

      /* URL contains relay descriptors. */
      if (filename.startsWith("tor-20") ||
          filename.startsWith("statuses-20") ||
          filename.startsWith("server-descriptors-20") ||
          filename.startsWith("extra-infos-20") ||
          filename.startsWith("votes-20") ||
          filename.startsWith("consensuses-20")) {
        String type = filename.substring(0, filename.indexOf("-20"));
        String yearMonth = filename.substring(filename.indexOf("20"));
        yearMonth = yearMonth.substring(0, 7);
        Date month = null;
        try {
          month = monthFormat.parse(yearMonth);
        } catch (ParseException e) {
          /* Ignore this URL. */
          continue;
        }
        int index = filename.endsWith(".asc") ? 1 : 0;
        if (!relayDescriptors.containsKey(month)) {
          relayDescriptors.put(month, new HashMap<String, String[]>());
        }
        if (!relayDescriptors.get(month).containsKey(type)) {
          relayDescriptors.get(month).put(type, new String[2]);
        }
        relayDescriptors.get(month).get(type)[index] = url;

      /* URL contains v3 certificates. */
      } else if (filename.startsWith("certs.tar")) {
        int index = filename.endsWith(".asc") ? 1 : 0;
        certs[index] = url;

      /* URL contains bridge descriptors. */
      } else if (filename.startsWith("bridge-descriptors-20")) {
        String yearMonth = filename.substring(filename.indexOf("20"));
        yearMonth = yearMonth.substring(0, 7);
        Date month = null;
        try {
          month = monthFormat.parse(yearMonth);
        } catch (ParseException e) {
          /* Ignore this URL. */
          continue;
        }
        int index = filename.endsWith(".asc") ? 1 : 0;
        if (!bridgeDescriptors.containsKey(month)) {
          bridgeDescriptors.put(month, new String[2]);
        }
        bridgeDescriptors.get(month)[index] = url;

      /* URL contains relay statistics. */
      } else if (filename.startsWith("relay-statistics.tar.bz2")) {
        int index = filename.endsWith(".asc") ? 1 : 0;
        relayStatistics[index] = url;

      /* URL contains Torperf tarball. */
      } else if (filename.startsWith("torperf-20")) {
        String yearMonth = filename.substring(filename.indexOf("20"));
        yearMonth = yearMonth.substring(0, 7);
        Date month = null;
        try {
          month = monthFormat.parse(yearMonth);
        } catch (ParseException e) {
          /* Ignore this URL. */
          continue;
        }
        if (!torperfTarballs.containsKey(month)) {
          torperfTarballs.put(month, new String[2]);
        }
        torperfTarballs.get(month)[0] = url;

      /* URL contains Torperf data file. */
      } else if (filename.endsWith("b.data") ||
          filename.endsWith("b.extradata")) {
        boolean isExtraData = filename.endsWith("b.extradata");
        String[] parts = filename.split("-");
        if (parts.length != 2) {
          continue;
        }
        String source = parts[0];
        if (torperfSources.containsKey(source)) {
          source = torperfSources.get(source);
        }
        String filesize = parts[1];
        filesize = filesize.substring(0, filesize.length()
            - (isExtraData ? 10 : 5));
        if (!torperfFilesizes.contains(filesize)) {
          continue;
        }
        if (!torperfData.containsKey(source)) {
          torperfData.put(source, new HashMap<String, String[]>());
        }
        if (!torperfData.get(source).containsKey(filesize)) {
          torperfData.get(source).put(filesize, new String[2]);
        }
        torperfData.get(source).get(filesize)[isExtraData ? 1 : 0] = url;

      /* URL contains Torperf experiment tarball. */
      } else if (filename.startsWith("torperf-experiment-20")) {
        String dateString = filename.substring(filename.indexOf("20"));
        dateString = dateString.substring(0, 10);
        Date date = null;
        try {
          date = dateFormat.parse(dateString);
        } catch (ParseException e) {
          /* Ignore this URL. */
          continue;
        }
        if (!torperfExperiments.containsKey(date)) {
          torperfExperiments.put(date, new String[2]);
        }
        torperfExperiments.get(date)[0] = url;

      /* URL contains exit list. */
      } else if (filename.startsWith("exit-list-20")) {
        String yearMonth = filename.substring(filename.indexOf("20"));
        yearMonth = yearMonth.substring(0, 7);
        Date month = null;
        try {
          month = monthFormat.parse(yearMonth);
        } catch (ParseException e) {
          /* Ignore this URL. */
          continue;
        }
        if (!exitLists.containsKey(month)) {
          exitLists.put(month, new String[2]);
        }
        exitLists.get(month)[0] = url;

      /* URL contains bridge pool assignments. */
      } else if (filename.startsWith("bridge-pool-assignments-20")) {
        String yearMonth = filename.substring(filename.indexOf("20"));
        yearMonth = yearMonth.substring(0, 7);
        Date month = null;
        try {
          month = monthFormat.parse(yearMonth);
        } catch (ParseException e) {
          /* Ignore this URL. */
          continue;
        }
        if (!bridgePoolAssignments.containsKey(month)) {
          bridgePoolAssignments.put(month, new String[2]);
        }
        bridgePoolAssignments.get(month)[0] = url;
      }
    }

    /* Add the maps to the request and forward it to the JSP to display
     * the page. */
    request.setAttribute("relayDescriptors", relayDescriptors);
    request.setAttribute("certs", certs);
    request.setAttribute("bridgeDescriptors", bridgeDescriptors);
    request.setAttribute("relayStatistics", relayStatistics);
    request.setAttribute("torperfData", torperfData);
    request.setAttribute("exitLists", exitLists);
    request.setAttribute("torperfTarballs", torperfTarballs);
    request.setAttribute("torperfExperiments", torperfExperiments);
    request.setAttribute("bridgePoolAssignments", bridgePoolAssignments);
    request.getRequestDispatcher("WEB-INF/data.jsp").forward(request,
        response);
  }
}

