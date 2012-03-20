/* Copyright 2011, 2012 The Tor Project
 * See LICENSE for licensing information */
package org.torproject.ernie.web;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpServletResponse;

import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

public class RObjectGenerator implements ServletContextListener {

  /* Host and port where Rserve is listening. */
  private String rserveHost;
  private int rservePort;

  /* Some parameters for our cache of graph images. */
  private String cachedGraphsDirectory;
  private long maxCacheAge;

  private SortedSet<String> availableCsvFiles;
  private Map<String, String> availableTables;
  private Map<String, String> availableGraphs;

  public void contextInitialized(ServletContextEvent event) {

    /* Initialize using context parameters. */
    ServletContext servletContext = event.getServletContext();
    this.rserveHost = servletContext.getInitParameter("rserveHost");
    this.rservePort = Integer.parseInt(servletContext.getInitParameter(
        "rservePort"));
    this.maxCacheAge = Long.parseLong(servletContext.getInitParameter(
        "maxCacheAge"));
    this.cachedGraphsDirectory = servletContext.getInitParameter(
        "cachedGraphsDir");

    /* Initialize map of available CSV files. */
    this.availableCsvFiles = new TreeSet<String>();
    this.availableCsvFiles.add("bandwidth");
    this.availableCsvFiles.add("bridge-users");
    this.availableCsvFiles.add("bwhist-flags");
    this.availableCsvFiles.add("connbidirect");
    this.availableCsvFiles.add("direct-users");
    this.availableCsvFiles.add("dirreq-stats");
    this.availableCsvFiles.add("dirbytes");
    this.availableCsvFiles.add("gettor");
    this.availableCsvFiles.add("monthly-users-average");
    this.availableCsvFiles.add("monthly-users-peak");
    this.availableCsvFiles.add("networksize");
    this.availableCsvFiles.add("platforms");
    this.availableCsvFiles.add("relaycountries");
    this.availableCsvFiles.add("relayflags");
    this.availableCsvFiles.add("relayflags-hour");
    this.availableCsvFiles.add("torperf");
    this.availableCsvFiles.add("torperf-failures");
    this.availableCsvFiles.add("versions");

    this.availableTables = new HashMap<String, String>();
    this.availableTables.put("direct-users", "start,end,filename");
    this.availableTables.put("censorship-events", "start,end,filename");
    TableParameterChecker.getInstance().setAvailableTables(
        availableTables);

    this.availableGraphs = new HashMap<String, String>();
    this.availableGraphs.put("networksize", "start,end,filename,dpi");
    this.availableGraphs.put("relaycountries",
        "start,end,country,filename,dpi");
    this.availableGraphs.put("relayflags", "start,end,flag,granularity,"
        + "filename,dpi");
    this.availableGraphs.put("versions", "start,end,filename,dpi");
    this.availableGraphs.put("platforms", "start,end,filename,dpi");
    this.availableGraphs.put("bandwidth", "start,end,filename,dpi");
    this.availableGraphs.put("bwhist-flags", "start,end,filename,dpi");
    this.availableGraphs.put("dirbytes", "start,end,filename,dpi");
    this.availableGraphs.put("direct-users",
        "start,end,country,events,filename,nocutoff,dpi");
    this.availableGraphs.put("bridge-users",
         "start,end,country,filename,dpi");
    this.availableGraphs.put("gettor", "start,end,language,filename,dpi");
    this.availableGraphs.put("torperf",
         "start,end,source,filesize,filename,dpi");
    this.availableGraphs.put("torperf-failures",
         "start,end,source,filesize,filename,dpi");
    this.availableGraphs.put("connbidirect", "start,end,filename,dpi");
    GraphParameterChecker.getInstance().setAvailableGraphs(
        availableGraphs);

    /* Register ourself, so that servlets can use us. */
    servletContext.setAttribute("RObjectGenerator", this);
  }

  public void contextDestroyed(ServletContextEvent event) {
    /* Nothing to do. */
  }

  public byte[] generateGraph(String requestedGraph, Map parameterMap) {
    Map<String, String[]> checkedParameters = GraphParameterChecker.
        getInstance().checkParameters(requestedGraph, parameterMap);
    if (checkedParameters == null) {
      /* TODO We're going to take the blame by sending an internal server
       * error to the client, but really the user is to blame. */
      return null;
    }
    StringBuilder rQueryBuilder = new StringBuilder("plot_"
        + requestedGraph.replaceAll("-", "_") + "("),
        imageFilenameBuilder = new StringBuilder(requestedGraph);
    for (Map.Entry<String, String[]> parameter :
        checkedParameters.entrySet()) {
      String parameterName = parameter.getKey();
      String[] parameterValues = parameter.getValue();
      for (String param : parameterValues) {
        imageFilenameBuilder.append("-" + param);
      }
      if (parameterValues.length < 2) {
        rQueryBuilder.append(parameterName + " = '" + parameterValues[0]
            + "', ");
      } else {
        rQueryBuilder.append(parameterName + " = c(");
        for (int i = 0; i < parameterValues.length - 1; i++) {
          rQueryBuilder.append("'" + parameterValues[i] + "', ");
        }
        rQueryBuilder.append("'" + parameterValues[
            parameterValues.length - 1] + "'), ");
      }
    }
    imageFilenameBuilder.append(".png");
    String imageFilename = imageFilenameBuilder.toString();
    rQueryBuilder.append("path = '%s')");
    String rQuery = rQueryBuilder.toString();
    return this.generateGraph(rQuery, imageFilename);
  }

  /* Generate a graph using the given R query that has a placeholder for
   * the absolute path to the image to be created. */
  private byte[] generateGraph(String rQuery, String imageFilename) {

    /* See if we need to generate this graph. */
    File imageFile = new File(this.cachedGraphsDirectory + "/"
        + imageFilename);
    long now = System.currentTimeMillis();
    if (!imageFile.exists() || imageFile.lastModified() < now
        - this.maxCacheAge * 1000L) {

      /* We do. Update the R query to contain the absolute path to the
       * file to be generated, create a connection to Rserve, run the R
       * query, and close the connection. The generated graph will be on
       * disk. */
      rQuery = String.format(rQuery, imageFile.getAbsolutePath());
      try {
        RConnection rc = new RConnection(rserveHost, rservePort);
        rc.eval(rQuery);
        rc.close();
      } catch (RserveException e) {
        return null;
      }

      /* Check that we really just generated the file */
      if (!imageFile.exists() || imageFile.lastModified() < now
          - this.maxCacheAge * 1000L) {
        return null;
      }
    }

    /* Read the image from disk and write it to a byte array. */
    byte[] result = null;
    try {
      BufferedInputStream bis = new BufferedInputStream(
          new FileInputStream(imageFile), 1024);
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      byte[] buffer = new byte[1024];
      int length;
      while ((length = bis.read(buffer)) > 0) {
        baos.write(buffer, 0, length);
      }
      result = baos.toByteArray();
    } catch (IOException e) {
      return null;
    }

    /* Return the graph bytes. */
    return result;
  }

  public SortedSet<String> getAvailableCsvFiles() {
    return this.availableCsvFiles;
  }

  public String generateCsv(String requestedCsvFile) {
    /* Prepare filename and R query string. */
    String rQuery = "export_" + requestedCsvFile.replaceAll("-", "_")
        + "(path = '%s')";
    String csvFilename = requestedCsvFile + ".csv";
    return this.generateCsv(rQuery, csvFilename);
  }

  /* Generate a comma-separated value file using the given R query that
   * has a placeholder for the absolute path to the file to be created. */
  private String generateCsv(String rQuery, String csvFilename) {

    /* See if we need to generate this .csv file. */
    File csvFile = new File(this.cachedGraphsDirectory + "/"
        + csvFilename);
    long now = System.currentTimeMillis();
    if (!csvFile.exists() || csvFile.lastModified() < now
        - this.maxCacheAge * 1000L) {

      /* We do. Update the R query to contain the absolute path to the
       * file to be generated, create a connection to Rserve, run the R
       * query, and close the connection. The generated csv file will be
       * on disk in the same directory as the generated graphs. */
      rQuery = String.format(rQuery, csvFile.getAbsolutePath());
      try {
        RConnection rc = new RConnection(rserveHost, rservePort);
        rc.eval(rQuery);
        rc.close();
      } catch (RserveException e) {
        return null;
      }

      /* Check that we really just generated the file */
      if (!csvFile.exists() || csvFile.lastModified() < now
          - this.maxCacheAge * 1000L) {
        return null;
      }
    }

    /* Read the text file from disk and write it to a string. */
    String result = null;
    try {
      StringBuilder sb = new StringBuilder();
      BufferedReader br = new BufferedReader(new FileReader(csvFile));
      String line = null;
      while ((line = br.readLine()) != null) {
        sb.append(line + "\n");
      }
      result = sb.toString();
    } catch (IOException e) {
      return null;
    }

    /* Return the csv file. */
    return result;
  }

  public List<Map<String, String>> generateTable(String tableName,
      String requestedTable, Map parameterMap) {

    Map<String, String[]> checkedParameters = null;
    if (tableName.equals(requestedTable)) {
      checkedParameters = TableParameterChecker.
          getInstance().checkParameters(requestedTable,
          parameterMap);
    } else {
      checkedParameters = TableParameterChecker.
          getInstance().checkParameters(tableName, null);
    }
    if (checkedParameters == null) {
      /* TODO We're going to take the blame by sending an internal server
       * error to the client, but really the user is to blame. */
      return null;
    }
    StringBuilder rQueryBuilder = new StringBuilder("write_"
        + tableName.replaceAll("-", "_") + "("),
        tableFilenameBuilder = new StringBuilder(tableName);

    for (Map.Entry<String, String[]> parameter :
        checkedParameters.entrySet()) {
      String parameterName = parameter.getKey();
      String[] parameterValues = parameter.getValue();
      for (String param : parameterValues) {
        tableFilenameBuilder.append("-" + param);
      }
      if (parameterValues.length < 2) {
        rQueryBuilder.append(parameterName + " = '"
            + parameterValues[0] + "', ");
      } else {
        rQueryBuilder.append(parameterName + " = c(");
        for (int i = 0; i < parameterValues.length - 1; i++) {
          rQueryBuilder.append("'" + parameterValues[i] + "', ");
        }
        rQueryBuilder.append("'" + parameterValues[
            parameterValues.length - 1] + "'), ");
      }
    }
    tableFilenameBuilder.append(".tbl");
    String tableFilename = tableFilenameBuilder.toString();
    rQueryBuilder.append("path = '%s')");
    String rQuery = rQueryBuilder.toString();
    return this.generateTable(rQuery, tableFilename);
  }

  /* Generate table data using the given R query and filename or read
   * previously generated table data from disk if it's not too old and
   * return table data. */
  private List<Map<String, String>> generateTable(String rQuery,
      String tableFilename) {

    /* See if we need to generate this table. */
    File tableFile = new File(this.cachedGraphsDirectory + "/"
        + tableFilename);
    long now = System.currentTimeMillis();
    if (!tableFile.exists() || tableFile.lastModified() < now
        - this.maxCacheAge * 1000L) {

      /* We do. Update the R query to contain the absolute path to the
       * file to be generated, create a connection to Rserve, run the R
       * query, and close the connection. The generated csv file will be
       * on disk in the same directory as the generated graphs. */
      rQuery = String.format(rQuery, tableFile.getAbsolutePath());
      try {
        RConnection rc = new RConnection(rserveHost, rservePort);
        rc.eval(rQuery);
        rc.close();
      } catch (RserveException e) {
        return null;
      }

      /* Check that we really just generated the file */
      if (!tableFile.exists() || tableFile.lastModified() < now
          - this.maxCacheAge * 1000L) {
        return null;
      }
    }

    /* Read the text file from disk and write the table content to a
     * map. */
    List<Map<String, String>> result = null;
    try {
      result = new ArrayList<Map<String, String>>();
      BufferedReader br = new BufferedReader(new FileReader(tableFile));
      String line = br.readLine();
      if (line != null) {
        List<String> headers = new ArrayList<String>(Arrays.asList(
            line.split(",")));
        while ((line = br.readLine()) != null) {
          String[] parts = line.split(",");
          if (headers.size() != parts.length) {
            return null;
          }
          Map<String, String> row = new HashMap<String, String>();
          for (int i = 0; i < headers.size(); i++) {
            row.put(headers.get(i), parts[i]);
          }
          result.add(row);
        }
      }
    } catch (IOException e) {
      return null;
    }

    /* Return table values. */
    return result;
  }
}

