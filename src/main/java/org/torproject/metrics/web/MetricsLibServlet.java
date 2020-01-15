/* Copyright 2017--2020 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MetricsLibServlet extends AnyServlet {

  private static final long serialVersionUID = -6009422570527820853L;

  @Override
  public void doGet(HttpServletRequest request,
      HttpServletResponse response) throws IOException, ServletException {

    /* Forward the request to the JSP that does all the hard work. */
    request.setAttribute("categories", this.categories);
    request.getRequestDispatcher("WEB-INF/metrics-lib.jsp").forward(request,
        response);
  }
}

