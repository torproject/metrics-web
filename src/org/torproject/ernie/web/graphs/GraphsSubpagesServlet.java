/* Copyright 2011, 2012 The Tor Project
 * See LICENSE for licensing information */
package org.torproject.ernie.web.graphs;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GraphsSubpagesServlet extends HttpServlet {

  private static final long serialVersionUID = -5959829347747628403L;

  /* Available graphs subpages with corresponding JSP to which requests
   * are forwarded. */
  private Map<String, String> availableGraphsSubpages;

  /* Available tables on graphs subpages. */
  private Map<String, Set<String>> availableGraphsSubpageTables;

  /* Country codes and names for per-country graphs. */
  private List<String[]> knownCountries;

  /* R object generator for generating table data. */
  private RObjectGenerator rObjectGenerator;

  public GraphsSubpagesServlet() {
    this.availableGraphsSubpages = new HashMap<String, String>();
    this.availableGraphsSubpages.put("network.html",
        "WEB-INF/network.jsp");
    this.availableGraphsSubpages.put("fast-exits.html",
        "WEB-INF/fast-exits.jsp");
    this.availableGraphsSubpages.put("users.html", "WEB-INF/users.jsp");
    this.availableGraphsSubpages.put("packages.html",
        "WEB-INF/packages.jsp");
    this.availableGraphsSubpages.put("performance.html",
        "WEB-INF/performance.jsp");

    this.availableGraphsSubpageTables =
        new HashMap<String, Set<String>>();
    this.availableGraphsSubpageTables.put("users.html",
        new HashSet<String>(Arrays.asList(
        "direct-users,censorship-events".split(","))));

    this.knownCountries = Countries.getInstance().getCountryList();
  }

  public void init() {
    /* Get a reference to the R object generator that we need to generate
     * table data. */
    this.rObjectGenerator = (RObjectGenerator) getServletContext().
        getAttribute("RObjectGenerator");
  }

  public void doGet(HttpServletRequest request,
      HttpServletResponse response) throws IOException, ServletException {

    /* Find out which graph subpage was requested and look up which JSP
     * handles this subpage. */
    String requestedPage = request.getRequestURI();
    if (requestedPage == null) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }
    if (requestedPage.contains("/")) {
      requestedPage = requestedPage.substring(requestedPage.
          lastIndexOf("/") + 1);
    }
    if (!availableGraphsSubpages.containsKey(requestedPage)) {
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return;
    }
    String jsp = availableGraphsSubpages.get(requestedPage);

    /* Find out which graph or table type was requested, if any. */
    String requestedGraph = request.getParameter("graph");
    String requestedTable = request.getParameter("table");
    if (requestedGraph != null) {

      /* Check if the passed parameters are valid. */
      Map<String, String[]> checkedParameters = GraphParameterChecker.
          getInstance().checkParameters(requestedGraph,
          request.getParameterMap());
      if (checkedParameters != null) {

        /* Set the graph's attributes to the appropriate values, so that
         * we can display the correct graph and prepopulate the form. */
        StringBuilder urlBuilder = new StringBuilder();
        for (Map.Entry<String, String[]> param :
            checkedParameters.entrySet()) {
          request.setAttribute(requestedGraph.replaceAll("-", "_") + "_"
              + param.getKey(), param.getValue());
          for (String paramValue : param.getValue()) {
            urlBuilder.append("&" + param.getKey() + "=" + paramValue);
          }
        }
        String url = "?" + urlBuilder.toString().substring(1);
        request.setAttribute(requestedGraph.replaceAll("-", "_") + "_url",
            url);
      }
    }
    if (requestedTable != null) {

      /* Check if the passed parameters are valid. */
      Map<String, String[]> checkedParameters = TableParameterChecker.
          getInstance().checkParameters(requestedTable,
          request.getParameterMap());
      if (checkedParameters != null) {

        /* Set the table's attributes to the appropriate values, so that
         * we can prepopulate the form. */
        for (Map.Entry<String, String[]> param :
            checkedParameters.entrySet()) {
          request.setAttribute(requestedTable.replaceAll("-", "_") + "_"
              + param.getKey(), param.getValue());
        }
      }
    }

    /* Generate table data if the graphs subpage has any tables,
     * regardless of whether a table update was requested, and add the
     * table data as request attribute. */
    if (this.availableGraphsSubpageTables.containsKey(requestedPage)) {
      for (String tableName :
          this.availableGraphsSubpageTables.get(requestedPage)) {
        List<Map<String, String>> tableData = rObjectGenerator.
            generateTable(tableName, requestedTable,
            request.getParameterMap(), true);
        request.setAttribute(tableName.replaceAll("-", "_")
              + "_tabledata", tableData);
      }
    }

    /* Pass list of known countries in case we want to display them. */
    request.setAttribute("countries", this.knownCountries);

    /* Pass the default start and end dates. */
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    Date defaultEndDate = new Date();
    Date defaultStartDate = new Date(defaultEndDate.getTime()
        - 90L * 24L * 60L * 60L * 1000L);
    request.setAttribute("default_start_date",
        dateFormat.format(defaultStartDate));
    request.setAttribute("default_end_date",
        dateFormat.format(defaultEndDate));

    /* Forward the request to the JSP that does all the hard work. */
    request.getRequestDispatcher(jsp).forward(request, response);
  }
}

