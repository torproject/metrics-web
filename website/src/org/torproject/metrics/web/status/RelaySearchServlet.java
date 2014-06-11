/* Copyright 2011, 2012 The Tor Project
 * See LICENSE for licensing information */
package org.torproject.metrics.web.status;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RelaySearchServlet extends HttpServlet {

  private static final long serialVersionUID = -1772662230310611806L;

  public void doGet(HttpServletRequest request,
      HttpServletResponse response) throws IOException,
      ServletException {

    /* Let the JSP display that we're out of service. */
    request.getRequestDispatcher("WEB-INF/relay-search.jsp").forward(
        request, response);
  }
}

