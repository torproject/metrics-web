package org.torproject.ernie.web;

import java.io.*;
import java.text.*;
import java.util.*;
import java.util.logging.*;

import javax.servlet.*;
import javax.servlet.http.*;

/**
 * Servlet that reads an HTTP request for a comma-separated value file,
 * asks the GraphGenerator to generate this file, and returns it to the
 * client.
 */
public class CsvServlet extends HttpServlet {

  private RObjectGenerator rObjectGenerator;

  /* Available CSV files. */
  private SortedSet<String> availableCsvFiles;

  private Logger logger;

  public void init() {

    /* Initialize logger. */
    this.logger = Logger.getLogger(CsvServlet.class.toString());

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

    /* Get a reference to the R object generator that we need to generate
     * CSV files. */
    this.rObjectGenerator = (RObjectGenerator) getServletContext().
        getAttribute("RObjectGenerator");
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

    /* Prepare filename and R query string. */
    String rQuery = "export_" + requestedCsvFile.replaceAll("-", "_")
        + "(path = '%s')";
    String csvFilename = requestedCsvFile + ".csv";

    /* Request CSV file from R object generator, which asks Rserve to
     * generate it. */
    String csvFileContent = this.rObjectGenerator.generateCsv(rQuery,
        csvFilename);

    /* Make sure that we have a graph to return. */
    if (csvFileContent == null) {
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return;
    }

    /* Write CSV file to response. */
    response.setContentType("text/csv");
    response.setHeader("Content-Length", String.valueOf(
        csvFileContent.length()));
    response.setHeader("Content-Disposition",
        "inline; filename=\"" + csvFilename + "\"");
    response.getWriter().print(csvFileContent);
  }
}

