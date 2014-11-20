/* Copyright 2014 The Tor Project
 * See LICENSE for licensing information */
package org.torproject.metrics.web.graphs;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class LegacyGraphsSubpagesServlet extends HttpServlet {

  /* Available graphs subpages with corresponding JSP to which requests
   * are forwarded. */
  private Map<String, String> availableGraphsSubpages;

  public LegacyGraphsSubpagesServlet() {
    this.availableGraphsSubpages = new HashMap<String, String>();
    this.availableGraphsSubpages.put("network.html",
        "WEB-INF/network.jsp");
    this.availableGraphsSubpages.put("users.html", "WEB-INF/users.jsp");
    this.availableGraphsSubpages.put("performance.html",
        "WEB-INF/performance.jsp");
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

    /* Forward the request to the JSP that does all the hard work. */
    request.getRequestDispatcher(jsp).forward(request, response);
  }
}

