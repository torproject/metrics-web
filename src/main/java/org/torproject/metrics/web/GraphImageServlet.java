/* Copyright 2011--2018 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.web;

import java.io.BufferedOutputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet that reads an HTTP request for a graph image, asks the
 * RObjectGenerator to generate this graph if it's not in the cache, and
 * returns the image bytes to the client.
 */
public class GraphImageServlet extends HttpServlet {

  private static final long serialVersionUID = -7356818641689744288L;

  private RObjectGenerator objectGenerator;

  @Override
  public void init() {

    /* Get a reference to the R object generator that we need to generate
     * graph images. */
    this.objectGenerator = (RObjectGenerator) getServletContext()
        .getAttribute("RObjectGenerator");
  }

  @Override
  public void doGet(HttpServletRequest request,
      HttpServletResponse response) throws IOException,
      ServletException {

    /* Find out which graph type was requested and make sure we know this
     * graph type and file type. */
    String requestedGraph = request.getRequestURI();
    String fileType = null;
    if (requestedGraph.endsWith(".png")
        || requestedGraph.endsWith(".pdf")
        || requestedGraph.endsWith(".csv")) {
      fileType = requestedGraph.substring(requestedGraph.length() - 3);
      requestedGraph = requestedGraph.substring(0, requestedGraph.length()
          - 4);
    }
    if (requestedGraph.contains("/")) {
      requestedGraph = requestedGraph.substring(requestedGraph
          .lastIndexOf("/") + 1);
    }

    /* Request graph from R object generator, which either returns it from
     * its cache or asks Rserve to generate it. */
    RObject graph = objectGenerator.generateGraph(requestedGraph,
        fileType, request.getParameterMap(), true);

    /* Make sure that we have a graph to return. */
    if (graph == null || graph.getBytes() == null || fileType == null) {
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return;
    }

    /* Write graph bytes to response. */
    byte[] headerBytes = ("#\n"
        + "# The Tor Project\n"
        + "#\n"
        + "# URL: https://metrics.torproject.org"
          + request.getRequestURI()
          + (null == request.getQueryString() ? ""
          : "?" + request.getQueryString()) + "\n"
        + "#\n").getBytes();
    response.setContentType(
        ("csv".equals(fileType) ? "text/" : "image/") + fileType);
    response.setHeader("Content-Length", String.valueOf(
        ("csv".equals(fileType) ? headerBytes.length : 0)
        + graph.getBytes().length));
    response.setHeader("Content-Disposition",
        "inline; filename=\"" + graph.getFileName() + "\"");
    BufferedOutputStream output = new BufferedOutputStream(
        response.getOutputStream(), 1024);
    if ("csv".equals(fileType)) {
      output.write(headerBytes);
    }
    output.write(graph.getBytes(), 0, graph.getBytes().length);
    output.flush();
    output.close();
  }
}

