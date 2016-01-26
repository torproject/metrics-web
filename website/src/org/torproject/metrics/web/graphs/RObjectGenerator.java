/* Copyright 2011, 2012 The Tor Project
 * See LICENSE for licensing information */
package org.torproject.metrics.web.graphs;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;
import org.torproject.metrics.web.Metric;
import org.torproject.metrics.web.MetricsProvider;

public class RObjectGenerator implements ServletContextListener {

  /* Host and port where Rserve is listening. */
  private String rserveHost;
  private int rservePort;

  /* Some parameters for our cache of graph images. */
  private String cachedGraphsDirectory;
  private long maxCacheAge;

  private Map<String, Metric> availableGraphs, availableTables;

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

    this.availableGraphs = new LinkedHashMap<String, Metric>();
    this.availableTables = new LinkedHashMap<String, Metric>();
    for (Metric metric : MetricsProvider.getInstance().getMetricsList()) {
      String type = metric.getType(), id = metric.getId();
      if ("Graph".equals(type)) {
        this.availableGraphs.put(id, metric);
      } else if ("Table".equals(type)) {
        this.availableTables.put(id, metric);
      }
    }

    /* Register ourself, so that servlets can use us. */
    servletContext.setAttribute("RObjectGenerator", this);

    /* Periodically generate R objects with default parameters. */
    new Thread() {
      public void run() {
        long lastUpdated = 0L, sleep;
        while (true) {
          while ((sleep = maxCacheAge * 1000L / 2L + lastUpdated
              - System.currentTimeMillis()) > 0L) {
            try {
              Thread.sleep(sleep);
            } catch (InterruptedException e) {
            }
          }
          for (String tableId : availableTables.keySet()) {
            generateTable(tableId, new HashMap(), false);
          }
          for (String graphId : availableGraphs.keySet()) {
            generateGraph(graphId, "png", new HashMap(), false);
          }
          lastUpdated = System.currentTimeMillis();
        }
      };
    }.start();
  }

  public void contextDestroyed(ServletContextEvent event) {
    /* Nothing to do. */
  }

  public RObject generateGraph(String requestedGraph, String fileType,
      Map parameterMap, boolean checkCache) {
    if (!this.availableGraphs.containsKey(requestedGraph) ||
        this.availableGraphs.get(requestedGraph).getFunction() == null) {
      return null;
    }
    String function = this.availableGraphs.get(requestedGraph).
        getFunction();
    Map<String, String[]> checkedParameters = GraphParameterChecker.
        getInstance().checkParameters(requestedGraph, parameterMap);
    if (checkedParameters == null) {
      return null;
    }
    StringBuilder
        rQueryBuilder = new StringBuilder().append(function).append("("),
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
    imageFilenameBuilder.append("." + fileType);
    String imageFilename = imageFilenameBuilder.toString();
    rQueryBuilder.append("path = '%s')");
    String rQuery = rQueryBuilder.toString();
    File imageFile = new File(this.cachedGraphsDirectory + "/"
        + imageFilename);
    return this.generateRObject(rQuery, imageFile, imageFilename,
        checkCache);
  }

  public List<Map<String, String>> generateTable(String requestedTable,
      Map parameterMap, boolean checkCache) {
    if (!this.availableTables.containsKey(requestedTable) ||
        this.availableTables.get(requestedTable).getFunction() == null) {
      return null;
    }
    String function = this.availableTables.get(requestedTable).
        getFunction();
    Map<String, String[]> checkedParameters = TableParameterChecker.
        getInstance().checkParameters(requestedTable, parameterMap);
    if (checkedParameters == null) {
      return null;
    }
    StringBuilder
        rQueryBuilder = new StringBuilder().append(function).append("("),
        tableFilenameBuilder = new StringBuilder(requestedTable);
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
    return this.generateTable(rQuery, tableFilename, checkCache);
  }

  /* Generate table data using the given R query and filename or read
   * previously generated table data from disk if it's not too old and
   * return table data. */
  private List<Map<String, String>> generateTable(String rQuery,
      String tableFilename, boolean checkCache) {

    /* See if we need to generate this table. */
    File tableFile = new File(this.cachedGraphsDirectory + "/"
        + tableFilename);
    byte[] tableBytes = this.generateRObject(rQuery, tableFile,
        tableFilename, checkCache).getBytes();

    /* Write the table content to a map. */
    List<Map<String, String>> result = null;
    try {
      result = new ArrayList<Map<String, String>>();
      BufferedReader br = new BufferedReader(new InputStreamReader(
          new ByteArrayInputStream(tableBytes)));
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

  /* Generate an R object in a separate worker thread, or wait for an
   * already running worker thread to finish and get its result. */
  private RObject generateRObject(String rQuery, File rObjectFile,
      String fileName, boolean checkCache) {
    RObjectGeneratorWorker worker = null;
    synchronized (this.rObjectGeneratorThreads) {
      if (this.rObjectGeneratorThreads.containsKey(rQuery)) {
        worker = this.rObjectGeneratorThreads.get(rQuery);
      } else {
        worker = new RObjectGeneratorWorker(rQuery, rObjectFile,
            fileName, checkCache);
        this.rObjectGeneratorThreads.put(rQuery, worker);
        worker.start();
      }
    }
    try {
      worker.join();
    } catch (InterruptedException e) {
    }
    synchronized (this.rObjectGeneratorThreads) {
      if (this.rObjectGeneratorThreads.containsKey(rQuery) &&
          this.rObjectGeneratorThreads.get(rQuery) == worker) {
        this.rObjectGeneratorThreads.remove(rQuery);
      }
    }
    return worker.getRObject();
  }

  private Map<String, RObjectGeneratorWorker> rObjectGeneratorThreads =
      new HashMap<String, RObjectGeneratorWorker>();

  private class RObjectGeneratorWorker extends Thread {

    private String rQuery;
    private File rObjectFile;
    private String fileName;
    private boolean checkCache;
    private RObject result = null;

    public RObjectGeneratorWorker(String rQuery, File rObjectFile,
        String fileName, boolean checkCache) {
      this.rQuery = rQuery;
      this.rObjectFile = rObjectFile;
      this.fileName = fileName;
      this.checkCache = checkCache;
    }

    public void run() {

      /* See if we need to generate this R object. */
      long now = System.currentTimeMillis();
      if (!this.checkCache || !this.rObjectFile.exists() ||
          this.rObjectFile.lastModified() < now - maxCacheAge * 1000L) {

        /* We do. Update the R query to contain the absolute path to the
         * file to be generated, create a connection to Rserve, run the R
         * query, and close the connection. The generated object will be
         * on disk. */
        this.rQuery = String.format(this.rQuery,
            this.rObjectFile.getAbsolutePath());
        try {
          RConnection rc = new RConnection(rserveHost, rservePort);
          rc.eval(this.rQuery);
          rc.close();
        } catch (RserveException e) {
          return;
        }

        /* Check that we really just generated the R object. */
        if (!this.rObjectFile.exists() || this.rObjectFile.lastModified()
            < now - maxCacheAge * 1000L) {
          return;
        }
      }

      /* Read the R object from disk and write it to a byte array. */
      long lastModified = this.rObjectFile.lastModified();
      try {
        BufferedInputStream bis = new BufferedInputStream(
            new FileInputStream(this.rObjectFile), 1024);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = bis.read(buffer)) > 0) {
          baos.write(buffer, 0, length);
        }
        bis.close();
        this.result = new RObject(baos.toByteArray(), this.fileName,
            lastModified);
      } catch (IOException e) {
        return;
      }
    }

    public RObject getRObject() {
      return this.result;
    }
  }
}
