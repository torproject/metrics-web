package org.torproject.ernie.web;

import java.io.*;
import java.util.*;
import java.util.logging.*;

import javax.servlet.*;

import org.rosuda.REngine.Rserve.*;
import org.rosuda.REngine.*;

public class RObjectGenerator implements ServletContextListener {

  /* Host and port where Rserve is listening. */
  private String rserveHost;
  private int rservePort;

  /* Some parameters for our cache of graph images. */
  private String cachedGraphsDirectory;
  private long maxCacheAge;

  private Logger logger;

  public void contextInitialized(ServletContextEvent event) {

    /* Initialize logger. */
    this.logger = Logger.getLogger(RObjectGenerator.class.toString());

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

      /* We do. Update the R query to contain the absolute path to the file
       * to be generated, create a connection to Rserve, run the R query,
       * and close the connection. The generated graph will be on disk. */
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

    /* Update the R query to contain the absolute path to the file to be
     * generated, create a connection to Rserve, run the R query, and
     * close the connection. The generated csv file will be on disk in the
     * same directory as the generated graphs. */
    File csvFile = new File(this.cachedGraphsDirectory + "/"
        + csvFilename);
    rQuery = String.format(rQuery, csvFile.getAbsolutePath());
    try {
      RConnection rc = new RConnection(rserveHost, rservePort);
      rc.eval(rQuery);
      rc.close();
    } catch (RserveException e) {
      return null;
    }

    /* Check that we really just generated the file */
    if (!csvFile.exists()) {
      return null;
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
}

