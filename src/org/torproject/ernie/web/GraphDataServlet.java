/* Copyright 2011, 2012 The Tor Project
 * See LICENSE for licensing information */
package org.torproject.ernie.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet that reads an HTTP request for a JSON-formatted graph data
 * document, asks the RObjectGenerator to generate the CSV file behind it,
 * converts it to a JSON object, and returns it to the client.
 */
public class GraphDataServlet extends HttpServlet {

  private static final long serialVersionUID = 1799558498301134024L;

  private RObjectGenerator rObjectGenerator;

  /* Available graph data files. */
  private SortedMap<String, String> availableGraphDataFiles;

  /* Variable columns in CSV files that are in long form, not wide. */
  private SortedMap<String, String> variableColumns;

  /* Value columns in CSV files if only specific value columns shall be
   * included in results. */
  private SortedMap<String, String> valueColumns;

  private Logger logger;

  public void init() {

    /* Initialize logger. */
    this.logger = Logger.getLogger(GraphDataServlet.class.toString());

    /* Initialize map of available graph data files and corresponding CSV
     * files. */
    this.availableGraphDataFiles = new TreeMap<String, String>();
    this.availableGraphDataFiles.put("relays", "networksize");
    this.availableGraphDataFiles.put("bridges", "networksize");
    this.availableGraphDataFiles.put("cloudbridges", "cloudbridges");
    this.availableGraphDataFiles.put("relays-by-country",
        "relaycountries");
    this.availableGraphDataFiles.put("relays-by-flags", "relayflags");
    this.availableGraphDataFiles.put("relays-by-version", "versions");
    this.availableGraphDataFiles.put("relays-by-platform", "platforms");
    this.availableGraphDataFiles.put("relay-bandwidth", "bandwidth");
    this.availableGraphDataFiles.put("relay-dir-bandwidth", "dirbytes");
    this.availableGraphDataFiles.put("relay-bandwidth-history-by-flags",
        "bwhist-flags");
    this.availableGraphDataFiles.put("relay-bandwidth-by-flags",
        "bandwidth-flags");
    this.availableGraphDataFiles.put("direct-users-by-country",
        "direct-users");
    this.availableGraphDataFiles.put("bridge-users-by-country",
        "bridge-users");
    this.availableGraphDataFiles.put("torperf", "torperf");

    /* Initialize map of graphs with specific variable columns. */
    this.variableColumns = new TreeMap<String, String>();
    this.variableColumns.put("relays-by-country", "country");
    this.variableColumns.put("relay-bandwidth-history-by-flags",
        "isexit,isguard");
    this.variableColumns.put("torperf", "source");

    /* Initialize map of graphs with specific value columns. */
    this.valueColumns = new TreeMap<String, String>();
    this.valueColumns.put("relays", "relays");
    this.valueColumns.put("bridges", "bridges");

    /* Get a reference to the R object generator that we need to generate
     * CSV files. */
    this.rObjectGenerator = (RObjectGenerator) getServletContext().
        getAttribute("RObjectGenerator");
  }

  public void doGet(HttpServletRequest request,
      HttpServletResponse response) throws IOException,
      ServletException {

    /* Find out which JSON file was requested and make sure we know this
     * JSON file type. */
    String requestedJsonFile = request.getRequestURI();
    if (requestedJsonFile.contains("/")) {
      requestedJsonFile = requestedJsonFile.substring(requestedJsonFile.
          lastIndexOf("/") + 1);
    }
    if (!availableGraphDataFiles.containsKey(requestedJsonFile)) {
      logger.info("Did not recognize requested .csv file from request "
          + "URI: '" + request.getRequestURI() + "'. Responding with 404 "
          + "Not Found.");
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
      return;
    }
    String requestedCsvFile = this.availableGraphDataFiles.get(
        requestedJsonFile);
    logger.fine("CSV file '" + requestedCsvFile + ".csv' requested.");

    /* Request CSV file from R object generator, which may ask Rserve to
     * generate it. */
    RObject csvFile = this.rObjectGenerator.generateCsv(requestedCsvFile,
        true);

    /* Make sure that we have a CSV to convert into JSON. */
    if (csvFile == null) {
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return;
    }

    /* Look up if we converted this CSV to JSON format before.  If not,
     * convert it now. */
    String jsonString;
    if (!this.lastConvertedCsvFile.containsKey(requestedJsonFile) ||
        this.lastConvertedCsvFile.get(requestedJsonFile) <
        csvFile.getLastModified()) {
      jsonString = this.convertCsvToJson(requestedJsonFile,
          new String(csvFile.getBytes()));
      this.lastConvertedCsvFile.put(requestedJsonFile,
          csvFile.getLastModified());
      this.convertedCsvFiles.put(requestedJsonFile, jsonString);
    } else {
      jsonString = this.convertedCsvFiles.get(requestedJsonFile);
    }

    /* Make sure we have a JSON string to return. */
    if (jsonString == null) {
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return;
    }

    /* Write JSON string to response. */
    response.setHeader("Access-Control-Allow-Origin", "*");
    response.setContentType("application/json");
    response.setCharacterEncoding("utf-8");
    response.getWriter().print(jsonString);
  }

  private Map<String, Long> lastConvertedCsvFile =
      new HashMap<String, Long>();
  private Map<String, String> convertedCsvFiles =
      new HashMap<String, String>();
  private String convertCsvToJson(String requestedJsonFile,
      String csvFileContent) {
    String jsonString = null;
    try {
      BufferedReader br = new BufferedReader(new StringReader(
          csvFileContent));
      String line;
      String[] columns = null;
      int dateCol = -1;
      SortedSet<Integer> variableCols = new TreeSet<Integer>();
      SortedSet<Integer> valueCols = new TreeSet<Integer>();
      if ((line = br.readLine()) != null) {
        columns = line.split(",");
        for (int i = 0; i < columns.length; i++) {
          if (columns[i].equals("date")) {
            dateCol = i;
          } else if (this.variableColumns.containsKey(requestedJsonFile)
              && this.variableColumns.get(requestedJsonFile).contains(
              columns[i])) {
            variableCols.add(i);
          } else if (!this.valueColumns.containsKey(requestedJsonFile) ||
              this.valueColumns.get(requestedJsonFile).contains(
              columns[i])) {
            valueCols.add(i);
          }
        }
      }
      if (columns == null || dateCol < 0 || valueCols.isEmpty()) {
        return null;
      }
      SortedMap<String, SortedSet<String>> graphs =
          new TreeMap<String, SortedSet<String>>();
      while ((line = br.readLine()) != null) {
        String[] elements = line.split(",");
        if (elements.length != columns.length) {
          return null;
        }
        String date = elements[dateCol];
        String variable = "";
        if (!variableCols.isEmpty()) {
          for (int variableCol : variableCols) {
            String variableString = elements[variableCol];
            if (variableString.equals("TRUE")) {
              variable += columns[variableCol] + "_";
            } else if (variableString.equals("FALSE")) {
              variable += "not" + columns[variableCol] + "_";
            } else {
              variable += variableString + "_";
            }
          }
        }
        for (int valueCol : valueCols) {
          if (elements[valueCol].equals("NA")) {
            continue;
          }
          String graphName = variable + columns[valueCol];
          if (!graphs.containsKey(graphName)) {
            graphs.put(graphName, new TreeSet<String>());
          }
          String dateAndValue = date + "=" + elements[valueCol];
          graphs.get(graphName).add(dateAndValue);
        }
      }
      StringBuilder sb = new StringBuilder();
      SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
      dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
      for (Map.Entry<String, SortedSet<String>> e : graphs.entrySet()) {
        String graphName = e.getKey();
        SortedSet<String> datesAndValues = e.getValue();
        if (datesAndValues.isEmpty()) {
          continue;
        }
        String[] firstDateAndValue = datesAndValues.first().split("=");
        String firstDate = firstDateAndValue[0];
        String lastDate = datesAndValues.last().split("=")[0];
        sb.append(",\n\"" + graphName + "\":{"
            + "\"first\":\"" + firstDate + "\","
            + "\"last\":\"" + lastDate + "\","
            + "\"values\":[");
        int written = 0;
        String previousDate = firstDate;
        long previousDateMillis = dateFormat.parse(previousDate).
            getTime();
        for (String dateAndValue : datesAndValues) {
          String parts[] = dateAndValue.split("=");
          String date = parts[0];
          long dateMillis = dateFormat.parse(date).getTime();
          String value = parts[1];
          while (dateMillis - 86400L * 1000L > previousDateMillis) {
            sb.append((written++ > 0 ? "," : "") + "null");
            previousDateMillis += 86400L * 1000L;
            previousDate = dateFormat.format(previousDateMillis);
          }
          sb.append((written++ > 0 ? "," : "") + value);
          previousDate = date;
          previousDateMillis = dateMillis;
        }
        sb.append("]}");
      }
      br.close();
      jsonString = "[" + sb.toString().substring(1) + "\n]";
    } catch (IOException e) {
      return null;
    } catch (ParseException e) {
      return null;
    }
    return jsonString;
  }
}

