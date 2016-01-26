/* Copyright 2016 The Tor Project
 * See LICENSE for licensing information */
package org.torproject.metrics.web;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.text.StrSubstitutor;
import org.torproject.metrics.web.graphs.RObjectGenerator;
import org.torproject.metrics.web.graphs.TableParameterChecker;

@SuppressWarnings("serial")
public class TableServlet extends MetricServlet {

  private RObjectGenerator rObjectGenerator;

  public void init() throws ServletException {
    super.init();
    this.rObjectGenerator = (RObjectGenerator) getServletContext().
        getAttribute("RObjectGenerator");
  }

  protected void doGet(HttpServletRequest request,
      HttpServletResponse response) throws ServletException, IOException {
    String requestURI = request.getRequestURI();
    if (requestURI == null || !requestURI.endsWith(".html")) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }
    String requestedId = requestURI.substring(
        requestURI.contains("/") ? requestURI.lastIndexOf("/") + 1 : 0,
        requestURI.length() - 5);
    if (!this.idsByType.containsKey("Table") ||
        !this.idsByType.get("Table").contains(requestedId)) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }
    request.setAttribute("id", requestedId);
    request.setAttribute("title", this.titles.get(requestedId));
    request.setAttribute("description",
        this.descriptions.get(requestedId));
    request.setAttribute("tableheader",
        this.tableHeaders.get(requestedId));
    request.setAttribute("data", this.data.get(requestedId));
    request.setAttribute("related", this.related.get(requestedId));
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    Date defaultEndDate = new Date();
    Date defaultStartDate = new Date(defaultEndDate.getTime()
        - 90L * 24L * 60L * 60L * 1000L);
    if (this.parameters.containsKey(requestedId)) {
      Map<String, String[]> checkedParameters = TableParameterChecker.
          getInstance().checkParameters(requestedId,
          request.getParameterMap());
      for (String parameter : this.parameters.get(requestedId)) {
        if (parameter.equals("start") || parameter.equals("end")) {
          String[] requestParameter;
          if (checkedParameters != null &&
              checkedParameters.containsKey(parameter)) {
            requestParameter = checkedParameters.get(parameter);
          } else {
            requestParameter = new String[] {
                dateFormat.format(parameter.equals("start")
                ? defaultStartDate : defaultEndDate) };
          }
          request.setAttribute(parameter, requestParameter);
        }
      }
    }
    List<Map<String, String>> tableData = rObjectGenerator.
        generateTable(requestedId, request.getParameterMap(), true);
    List<List<String>> formattedTableData =
        new ArrayList<List<String>>();
    String[] contents = this.tableCellFormats.get(requestedId);
    for (Map<String, String> row : tableData) {
      List<String> formattedRow = new ArrayList<String>();
      StrSubstitutor sub = new StrSubstitutor(row);
      for (String con : contents) {
        formattedRow.add(sub.replace(con));
      }
      formattedTableData.add(formattedRow);
    }
    request.setAttribute("tabledata", formattedTableData);
    request.getRequestDispatcher("WEB-INF/table.jsp").forward(request,
        response);
  }
}

