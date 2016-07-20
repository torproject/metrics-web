/* Copyright 2016 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.web;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class RedirectServlet extends HttpServlet {

  /* Available permanent internal and external redirects. */
  private Map<String, String> redirects = new HashMap<String, String>();

  @Override
  public void init() throws ServletException {

    /* Internal redirects: */
    this.redirects.put("/metrics/graphs.html",
        "/?type=gr&level=bs&level=ad");
    this.redirects.put("/metrics/network.html",
        "/?tag=rl&tag=br&level=bs&level=ad");
    this.redirects.put("/metrics/performance.html",
        "/?tag=pf&level=bs&level=ad");
    this.redirects.put("/metrics/stats.html",
        "/?type=dt&level=bs&level=ad");
    this.redirects.put("/metrics/users.html",
        "/?tag=cl&level=bs&level=ad");

    /* External redirects: */
    this.redirects.put("/metrics/consensus-health.html",
        "https://consensus-health.torproject.org/");
    this.redirects.put("/metrics/data.html",
        "https://collector.torproject.org/");
    this.redirects.put("/metrics/exonerator.html",
        "https://exonerator.torproject.org/");
    this.redirects.put("/metrics/formats.html",
        "https://collector.torproject.org/#data-formats");
    this.redirects.put("/metrics/papers.html",
        "https://research.torproject.org/techreports.html");
    this.redirects.put("/metrics/relay-search.html",
        "https://atlas.torproject.org/");
    this.redirects.put("/metrics/research.html",
        "https://research.torproject.org/");
    this.redirects.put("/metrics/tools.html",
        "https://collector.torproject.org/#related-work");
  }

  @Override
  public void doGet(HttpServletRequest request,
      HttpServletResponse response) throws IOException, ServletException {
    String redirect = this.redirects.get(request.getRequestURI());
    if (redirect == null) {
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return;
    } else {
      response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
      response.setHeader("Location", redirect);
    }
  }
}

