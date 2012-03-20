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

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

public class RObjectGenerator implements ServletContextListener {

  /* Host and port where Rserve is listening. */
  private String rserveHost;
  private int rservePort;

  /* Some parameters for our cache of graph images. */
  private String cachedGraphsDirectory;
  private long maxCacheAge;

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

    /* Register ourself, so that servlets can use us. */
    servletContext.setAttribute("RObjectGenerator", this);
  }

  public void contextDestroyed(ServletContextEvent event) {
    /* Nothing to do. */
  }

  /* Generate a graph using the given R query that has a placeholder for
   * the absolute path to the image to be created. */
  public byte[] generateGraph(String rQuery, String imageFilename) {

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

  /* Generate a comma-separated value file using the given R query that
   * has a placeholder for the absolute path to the file to be created. */
  public String generateCsv(String rQuery, String csvFilename) {

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

  /* Generate table data using the given R query and filename or read
   * previously generated table data from disk if it's not too old and
   * return table data. */
  public List<Map<String, String>> generateTable(String rQuery,
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

