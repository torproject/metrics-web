/* Copyright 2016 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.web;

import org.torproject.metrics.web.graphs.RObjectGenerator;
import org.torproject.metrics.web.graphs.TableParameterChecker;

import org.apache.commons.lang.text.StrSubstitutor;

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

@SuppressWarnings("serial")
public class TableServlet extends MetricServlet {

  private RObjectGenerator objectGenerator;

  @Override
  public void init() throws ServletException {
    super.init();
    this.objectGenerator = (RObjectGenerator) getServletContext()
        .getAttribute("RObjectGenerator");
  }

  @Override
  protected void doGet(HttpServletRequest request,
      HttpServletResponse response) throws ServletException, IOException {
    String requestUri = request.getRequestURI();
    if (requestUri == null || !requestUri.endsWith(".html")) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }
    String requestedId = requestUri.substring(
        requestUri.contains("/") ? requestUri.lastIndexOf("/") + 1 : 0,
        requestUri.length() - 5);
    if (!this.idsByType.containsKey("Table")
        || !this.idsByType.get("Table").contains(requestedId)) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }
    request.setAttribute("id", requestedId);
    request.setAttribute("title", this.titles.get(requestedId));
    if (this.categoriesById.containsKey(requestedId)) {
      Category category = this.categoriesById.get(requestedId);
      request.setAttribute("categoryHeader", category.getHeader());
      request.setAttribute("categoryDescription", category.getDescription());
      List<String[]> categoryTabs = new ArrayList<String[]>();
      for (String metricId : category.getMetrics()) {
        categoryTabs.add(new String[] { this.titles.get(metricId), metricId });
      }
      request.setAttribute("categoryTabs", categoryTabs);
    }
    request.setAttribute("description",
        this.descriptions.get(requestedId));
    request.setAttribute("tableheader",
        this.tableHeaders.get(requestedId));
    request.setAttribute("data", this.data.get(requestedId));
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    Date defaultEndDate = new Date();
    Date defaultStartDate = new Date(defaultEndDate.getTime()
        - 90L * 24L * 60L * 60L * 1000L);
    if (this.parameters.containsKey(requestedId)) {
      Map<String, String[]> checkedParameters = TableParameterChecker
          .getInstance().checkParameters(requestedId,
          request.getParameterMap());
      for (String parameter : this.parameters.get(requestedId)) {
        if (parameter.equals("start") || parameter.equals("end")) {
          String[] requestParameter;
          if (checkedParameters != null
              && checkedParameters.containsKey(parameter)) {
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
    List<Map<String, String>> tableData = objectGenerator
        .generateTable(requestedId, request.getParameterMap(), true);
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

