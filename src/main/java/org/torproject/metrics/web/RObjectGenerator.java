/* Copyright 2011--2020 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.web;

import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class RObjectGenerator implements ServletContextListener {

  /* Host and port where Rserve is listening. */
  private String rserveHost;

  private int rservePort;

  /* Some parameters for our cache of graph images. */
  private String cachedGraphsDirectory;

  private long maxCacheAge;

  private Map<String, Metric> availableGraphs;

  private Map<String, Metric> availableTables;

  @Override
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

    this.availableGraphs = new LinkedHashMap<>();
    this.availableTables = new LinkedHashMap<>();
    for (Metric metric : ContentProvider.getInstance().getMetricsList()) {
      String type = metric.getType();
      String id = metric.getId();
      switch (type) {
        case "Graph":
          this.availableGraphs.put(id, metric);
          break;
        case "Table":
          this.availableTables.put(id, metric);
          break;
        default:
          /* Just skip any other types. */
      }
    }

    /* Register ourself, so that servlets can use us. */
    servletContext.setAttribute("RObjectGenerator", this);

    /* Periodically generate R objects with default parameters. */
    new Thread(() -> {
      long lastUpdated = 0L;
      long sleep;
      while (true) {
        while ((sleep = maxCacheAge * 1000L / 2L + lastUpdated
            - System.currentTimeMillis()) > 0L) {
          try {
            Thread.sleep(sleep);
          } catch (InterruptedException e) {
            /* Nothing we can handle. */
          }
        }
        for (String tableId : availableTables.keySet()) {
          generateTable(tableId, new HashMap<>(), false);
        }
        for (String graphId : availableGraphs.keySet()) {
          generateGraph(graphId, "png", new HashMap<>(), false);
        }
        lastUpdated = System.currentTimeMillis();
      }
    }).start();
  }

  @Override
  public void contextDestroyed(ServletContextEvent event) {
    /* Nothing to do. */
  }

  /** Generates a graph of the given type, given image file type, and with
   * the given parameters, possibly after checking whether the cache
   * already contains that graph. */
  public RObject generateGraph(String requestedGraph, String fileType,
      Map<String, String[]> parameterMap, boolean checkCache) {
    if (!this.availableGraphs.containsKey(requestedGraph)
        || this.availableGraphs.get(requestedGraph).getFunction()
        == null) {
      return null;
    }
    Map<String, String[]> checkedParameters = GraphParameterChecker
        .getInstance().checkParameters(requestedGraph, parameterMap);
    if (checkedParameters == null) {
      return null;
    }
    StringBuilder queryBuilder = new StringBuilder();
    queryBuilder.append("robust_call(as.call(list(");
    if ("csv".equalsIgnoreCase(fileType)) {
      queryBuilder.append("write_data, prepare_");
      /* When we checked parameters above we also put in defaults for missing
       * parameters. This is okay for graphs, but we want to support CSV files
       * with empty parameters. Using the parameters we got here. */
      checkedParameters = parameterMap;
    } else {
      queryBuilder.append("plot_");
    }
    String function = this.availableGraphs.get(requestedGraph)
        .getFunction();
    queryBuilder.append(function).append(", ");
    StringBuilder imageFilenameBuilder =
        new StringBuilder(requestedGraph);
    for (Map.Entry<String, String[]> parameter
        : checkedParameters.entrySet()) {
      String parameterName = parameter.getKey();
      String[] parameterValues = parameter.getValue();
      for (String param : parameterValues) {
        imageFilenameBuilder.append("-").append(param);
      }
      if (parameterValues.length < 2) {
        queryBuilder.append(parameterName).append("_p = '")
            .append(parameterValues[0]).append("', ");
      } else {
        queryBuilder.append(parameterName).append("_p = c(");
        for (int i = 0; i < parameterValues.length - 1; i++) {
          queryBuilder.append("'").append(parameterValues[i]).append("', ");
        }
        queryBuilder.append("'").append(parameterValues[
            parameterValues.length - 1]).append("'), ");
      }
    }
    imageFilenameBuilder.append(".").append(fileType);
    String imageFilename = imageFilenameBuilder.toString();
    queryBuilder.append("path_p = '%1$s')), '%1$s')");
    String query = queryBuilder.toString();
    File imageFile = new File(this.cachedGraphsDirectory + "/"
        + imageFilename);
    return this.generateObject(query, imageFile, imageFilename,
        checkCache);
  }

  /** Generates a table of the given type and with the given parameters,
   * possibly after checking whether the cache already contains that
   * table. */
  public List<Map<String, String>> generateTable(String requestedTable,
      Map<String, String[]> parameterMap, boolean checkCache) {
    if (!this.availableTables.containsKey(requestedTable)
        || this.availableTables.get(requestedTable).getFunction()
        == null) {
      return null;
    }
    String function = this.availableTables.get(requestedTable)
        .getFunction();
    Map<String, String[]> checkedParameters = TableParameterChecker
        .getInstance().checkParameters(requestedTable, parameterMap);
    if (checkedParameters == null) {
      return null;
    }
    StringBuilder queryBuilder = new StringBuilder().append(function)
        .append("(");
    StringBuilder tableFilenameBuilder = new StringBuilder(
        requestedTable);
    for (Map.Entry<String, String[]> parameter
        : checkedParameters.entrySet()) {
      String parameterName = parameter.getKey();
      String[] parameterValues = parameter.getValue();
      for (String param : parameterValues) {
        tableFilenameBuilder.append("-").append(param);
      }
      if (parameterValues.length < 2) {
        queryBuilder.append(parameterName).append(" = '")
            .append(parameterValues[0]).append("', ");
      } else {
        queryBuilder.append(parameterName).append(" = c(");
        for (int i = 0; i < parameterValues.length - 1; i++) {
          queryBuilder.append("'").append(parameterValues[i]).append("', ");
        }
        queryBuilder.append("'").append(parameterValues[
            parameterValues.length - 1]).append("'), ");
      }
    }
    tableFilenameBuilder.append(".tbl");
    String tableFilename = tableFilenameBuilder.toString();
    queryBuilder.append("path = '%s')");
    String query = queryBuilder.toString();
    return this.generateTable(query, tableFilename, checkCache);
  }

  /* Generate table data using the given R query and filename or read
   * previously generated table data from disk if it's not too old and
   * return table data. */
  private List<Map<String, String>> generateTable(String query,
      String tableFilename, boolean checkCache) {

    /* See if we need to generate this table. */
    File tableFile = new File(this.cachedGraphsDirectory + "/"
        + tableFilename);
    RObject tableObject = this.generateObject(query, tableFile,
        tableFilename, checkCache);
    if (null == tableObject) {
      return null;
    }
    byte[] tableBytes = tableObject.getBytes();

    /* Write the table content to a map. */
    List<Map<String, String>> result = new ArrayList<>();
    try (BufferedReader br = new BufferedReader(new InputStreamReader(
        new ByteArrayInputStream(tableBytes), StandardCharsets.UTF_8))) {
      String line = br.readLine();
      if (line != null) {
        List<String> headers = new ArrayList<>(Arrays.asList(line.split(",")));
        while ((line = br.readLine()) != null) {
          String[] parts = line.split(",");
          if (headers.size() != parts.length) {
            return null;
          }
          Map<String, String> row = new HashMap<>();
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
  private RObject generateObject(String query, File objectFile,
      String fileName, boolean checkCache) {
    RObjectGeneratorWorker worker;
    synchronized (this.objectGeneratorThreads) {
      if (this.objectGeneratorThreads.containsKey(query)) {
        worker = this.objectGeneratorThreads.get(query);
      } else {
        worker = new RObjectGeneratorWorker(query, objectFile, fileName,
            checkCache);
        this.objectGeneratorThreads.put(query, worker);
        worker.start();
      }
    }
    try {
      worker.join();
    } catch (InterruptedException e) {
      /* Nothing we can handle here. */
    }
    synchronized (this.objectGeneratorThreads) {
      if (this.objectGeneratorThreads.containsKey(query)
          && this.objectGeneratorThreads.get(query) == worker) {
        this.objectGeneratorThreads.remove(query);
      }
    }
    return worker.getRObject();
  }

  private Map<String, RObjectGeneratorWorker> objectGeneratorThreads =
      new HashMap<>();

  private class RObjectGeneratorWorker extends Thread {

    private String query;
    private File objectFile;
    private String fileName;
    private boolean checkCache;
    private RObject result = null;

    public RObjectGeneratorWorker(String query, File objectFile,
        String fileName, boolean checkCache) {
      this.query = query;
      this.objectFile = objectFile;
      this.fileName = fileName;
      this.checkCache = checkCache;
    }

    public void run() {

      /* See if we need to generate this R object. */
      long now = System.currentTimeMillis();
      if (!this.checkCache || !this.objectFile.exists()
          || this.objectFile.lastModified()
          < now - maxCacheAge * 1000L) {

        /* We do. Update the R query to contain the absolute path to the
         * file to be generated, create a connection to Rserve, run the R
         * query, and close the connection. The generated object will be
         * on disk. */
        this.query = String.format(this.query,
            this.objectFile.getAbsolutePath());
        try {
          RConnection rc = new RConnection(rserveHost, rservePort);
          rc.eval(this.query);
          rc.close();
        } catch (RserveException e) {
          return;
        }

        /* Check that we really just generated the R object. */
        if (!this.objectFile.exists() || this.objectFile.lastModified()
            < now - maxCacheAge * 1000L) {
          return;
        }
      }

      /* Read the R object from disk and write it to a byte array. */
      long lastModified = this.objectFile.lastModified();
      try (BufferedInputStream bis = new BufferedInputStream(
          new FileInputStream(this.objectFile), 1024);
          ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
        byte[] buffer = new byte[1024];
        int length;
        while ((length = bis.read(buffer)) > 0) {
          baos.write(buffer, 0, length);
        }
        bis.close();
        this.result = new RObject(baos.toByteArray(), this.fileName,
            lastModified);
      } catch (IOException e) {
        /* Nothing to do here. */
      }
    }

    public RObject getRObject() {
      return this.result;
    }
  }
}
