/* Copyright 2018 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ReproducibleMetricsServlet extends AnyServlet {

  private static final long serialVersionUID = 6099009779662419291L;

  @Override
  public void doGet(HttpServletRequest request,
      HttpServletResponse response) throws IOException, ServletException {

    request.setAttribute("categories", this.categories);
    request.getRequestDispatcher("WEB-INF/reproducible-metrics.jsp")
        .forward(request, response);
  }
}

