/* Copyright 2017--2018 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.web;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet for CollecTor's "home" page and for CollecTor's directory listings
 * based on the periodically fetched index.json file. */
public class CollecTorServlet extends AnyServlet {

  private static final long serialVersionUID = -7054057945737357463L;

  /** Host name of the CollecTor host with trailing slash omitted. */
  private static final String COLLECTOR_HOST
      = "https://collector.torproject.org";

  /** Parsed and formatted directory listings. */
  private CollectorDirectoryProvider collectorDirectory;

  /** Initializes this servlet by retrieving the CollecTor host name from the
   * configuration file and starting the periodic index.json downloader. */
  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    this.collectorDirectory = new CollectorDirectoryProvider(COLLECTOR_HOST);
  }

  /** Handles requests for either CollecTor's "home" page or for directory
   * listings. */
  @Override
  public void doGet(HttpServletRequest request,
      HttpServletResponse response) throws IOException, ServletException {
    String requestedPath = request.getRequestURI();
    if (requestedPath.contains("/collector")) {
      /* Possibly truncate any path prefix (like "/metrics") added by deploying
       * this webapp as metrics.war rather than, say, ROOT.war. */
      requestedPath = requestedPath.substring(requestedPath.indexOf(
          "/collector"));
    }
    Map<String, List<String[]>> index;
    if ("/collector.html".equals(requestedPath)) {
      request.setAttribute("categories", this.categories);
      request.getRequestDispatcher("WEB-INF/collector.jsp").forward(request,
          response);
    } else if (null == (index = this.collectorDirectory.getIndex())) {
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
          "Index of CollecTor files unavailable.");
    } else if (!requestedPath.endsWith("/")
        && index.containsKey(requestedPath + "/")) {
      response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
      response.setHeader("Location", requestedPath + "/");
    } else if (index.containsKey(requestedPath)) {
      request.setAttribute("categories", this.categories);
      request.setAttribute("files", index.get(requestedPath));
      request.getRequestDispatcher("/WEB-INF/collector-files.jsp").forward(
          request, response);
    } else {
      response.sendError(HttpServletResponse.SC_NOT_FOUND,
          "Unknown directory: " + requestedPath);
    }
  }
}

