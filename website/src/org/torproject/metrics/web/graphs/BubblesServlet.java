/* Copyright 2013--2017 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.web.graphs;

import org.torproject.metrics.web.Category;
import org.torproject.metrics.web.MetricServlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class BubblesServlet extends MetricServlet {

  private static final long serialVersionUID = -6011833075497881033L;

  @Override
  public void init() throws ServletException {
    super.init();
  }

  @Override
  public void doGet(HttpServletRequest request,
      HttpServletResponse response) throws IOException, ServletException {
    String requestUri = request.getRequestURI();
    if (requestUri == null || !requestUri.endsWith(".html")) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }
    String requestedId = requestUri.substring(
        requestUri.contains("/") ? requestUri.lastIndexOf("/") + 1 : 0,
        requestUri.length() - 5);
    request.setAttribute("categories", this.categories);
    request.setAttribute("id", requestedId);
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
    request.getRequestDispatcher("WEB-INF/bubbles.jsp").forward(request,
        response);
  }
}

