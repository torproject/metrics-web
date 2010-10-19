package org.torproject.ernie.web;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;

public class RedirectServlet extends HttpServlet {

  private Map<String, String> redirects;

  public void init() {

    /* Initialize map containing redirects. */
    redirects = new HashMap<String, String>();
    redirects.put("networksize.html", "/network.html");
    redirects.put("exit-relays-graphs.html", "/network.html");
    redirects.put("bridge-users-graphs.html", "/users.html");
    redirects.put("new-users-graphs.html", "/users.html");
    redirects.put("consensus-graphs.html", "/network.html");
    redirects.put("recurring-users-graphs.html", "/users.html");
    redirects.put("torperf-graphs.html", "/performance.html");
    redirects.put("gettor-graphs.html", "/packages.html");
    redirects.put("custom-graph.html", "/graphs.html");
  }

  public void doGet(HttpServletRequest request,
      HttpServletResponse response) throws IOException, ServletException {

    /* Find out which page was requested. */
    String requestedPage = request.getRequestURI();
    if (requestedPage.contains("/")) {
      requestedPage = requestedPage.substring(requestedPage.
          lastIndexOf("/") + 1);
    }

    /* Make sure we know where to redirect this request. */
    if (!redirects.containsKey(requestedPage)) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND, requestedPage);
      return;
    }

    /* Redirect the request. */
    response.sendRedirect(redirects.get(requestedPage));
  }
}

