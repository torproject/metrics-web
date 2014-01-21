/* Copyright 2011, 2012 The Tor Project
 * See LICENSE for licensing information */
package org.torproject.metrics.web.graphs;

import java.io.IOException;
import java.util.SortedSet;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet that reads an HTTP request for a comma-separated value file,
 * asks the GraphGenerator to generate this file, and returns it to the
 * client.
 */
public class CsvServlet extends HttpServlet {

  private static final long serialVersionUID = 7501442926823719958L;

  private RObjectGenerator rObjectGenerator;

  /* Available CSV files. */
  private SortedSet<String> availableCsvFiles;

  private Logger logger;

  public void init() {

    /* Initialize logger. */
    this.logger = Logger.getLogger(CsvServlet.class.toString());

    /* Get a reference to the R object generator that we need to generate
     * CSV files. */
    this.rObjectGenerator = (RObjectGenerator) getServletContext().
        getAttribute("RObjectGenerator");
    this.availableCsvFiles = rObjectGenerator.getAvailableCsvFiles();
  }

  public void doGet(HttpServletRequest request,
      HttpServletResponse response) throws IOException,
      ServletException {

    /* Check if the directory listing was requested. */
    String requestURI = request.getRequestURI();
    if (requestURI.equals("/ernie/csv/")) {
      request.setAttribute("directory", "/csv");
      request.setAttribute("extension", ".csv");
      request.setAttribute("files", this.availableCsvFiles);
      request.getRequestDispatcher("/WEB-INF/dir.jsp").forward(request,
          response);
      return;
    }

    /* Find out which CSV file was requested and make sure we know this
     * CSV file type. */
    String requestedCsvFile = requestURI;
    if (requestedCsvFile.endsWith(".csv")) {
      requestedCsvFile = requestedCsvFile.substring(0,
          requestedCsvFile.length() - ".csv".length());
    }
    if (requestedCsvFile.contains("/")) {
      requestedCsvFile = requestedCsvFile.substring(requestedCsvFile.
          lastIndexOf("/") + 1);
    }
    if (!availableCsvFiles.contains(requestedCsvFile)) {
      logger.info("Did not recognize requested .csv file from request "
          + "URI: '" + requestURI + "'. Responding with 404 Not Found.");
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
      return;
    }
    logger.fine("CSV file '" + requestedCsvFile + ".csv' requested.");

    /* Request CSV file from R object generator, which asks Rserve to
     * generate it. */
    RObject csvFile = this.rObjectGenerator.generateCsv(
        requestedCsvFile, true);

    /* Make sure that we have a .csv file to return. */
    if (csvFile == null) {
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return;
    }

    /* Write CSV file to response. */
    String csvFileContent = new String(csvFile.getBytes());
    response.setContentType("text/csv");
    response.setHeader("Content-Length", String.valueOf(
        csvFileContent.length()));
    response.setHeader("Content-Disposition",
        "inline; filename=\"" + requestedCsvFile + ".csv\"");
    response.getWriter().print(csvFileContent);
  }
}

