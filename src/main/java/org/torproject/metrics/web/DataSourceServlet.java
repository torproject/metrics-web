/* Copyright 2017--2020 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.web;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DataSourceServlet extends AnyServlet {

  private static final long serialVersionUID = -8220752089313109128L;

  private Map<String, String[]> specFiles = new HashMap<>();

  @Override
  public void init() throws ServletException {
    super.init();
    this.specFiles.put("/bridge-descriptors.html",
        new String[] { "/bridge-descriptors.jsp", "Tor Bridge Descriptors" });
    this.specFiles.put("/web-server-logs.html",
        new String[] { "/web-server-logs.jsp", "Tor Web Server Logs" });
  }

  @Override
  public void doGet(HttpServletRequest request,
      HttpServletResponse response) throws IOException, ServletException {
    String requestedPage = request.getRequestURI();
    for (Map.Entry<String, String[]> specFile : this.specFiles.entrySet()) {
      if (requestedPage.endsWith(specFile.getKey())) {
        request.setAttribute("categories", this.categories);
        request.setAttribute("breadcrumb", specFile.getValue()[1]);
        request.getRequestDispatcher("WEB-INF" + specFile.getValue()[0])
            .forward(request, response);
        return;
      }
    }
    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
  }
}

