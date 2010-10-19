package org.torproject.ernie.web;

import java.io.*;
import java.text.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * Servlet that reads an HTTP request for a graph image, asks the
 * GraphGenerator to generate this graph if it's not in the cache, and
 * returns the image bytes to the client.
 */
public class GraphImageServlet extends HttpServlet {

  private GraphGenerator graphGenerator;

  public void init() {
    ServletConfig servletConfig = getServletConfig();
    String rserveHost = servletConfig.getInitParameter("rserveHost");
    String rservePort = servletConfig.getInitParameter("rservePort");
    String maxCacheAge = servletConfig.getInitParameter("maxCacheAge");
    String cachedGraphsDir = servletConfig.getInitParameter(
        "cachedGraphsDir");
    this.graphGenerator = new GraphGenerator(rserveHost, rservePort,
        maxCacheAge, cachedGraphsDir);
  }

  public void doGet(HttpServletRequest request,
      HttpServletResponse response) throws IOException,
      ServletException {

    /* Find out which graph type was requested and make sure we know this
     * graph type. */
    String requestedGraph = request.getRequestURI();
    if (requestedGraph.endsWith(".png")) {
      requestedGraph = requestedGraph.substring(0, requestedGraph.length()
          - ".png".length());
    }
    if (requestedGraph.contains("/")) {
      requestedGraph = requestedGraph.substring(requestedGraph.
          lastIndexOf("/") + 1);
    }

    /* Check parameters. */
    Map<String, String[]> checkedParameters = GraphParameterChecker.
        getInstance().checkParameters(requestedGraph,
        request.getParameterMap());
    if (checkedParameters == null) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    /* Prepare filename and R query string. */
    StringBuilder rQueryBuilder = new StringBuilder("plot_"
        + requestedGraph.replaceAll("-", "_") + "("),
        imageFilenameBuilder = new StringBuilder(requestedGraph);
    for (Map.Entry<String, String[]> parameter :
        checkedParameters.entrySet()) {
      String parameterName = parameter.getKey();
      String[] parameterValues = parameter.getValue();
      for (String param : parameterValues) {
        imageFilenameBuilder.append("-" + param);
      }
      if (parameterValues.length < 2) {
        rQueryBuilder.append(parameterName + " = '" + parameterValues[0]
            + "', ");
      } else {
        rQueryBuilder.append(parameterName + " = c(");
        for (int i = 0; i < parameterValues.length - 1; i++) {
          rQueryBuilder.append("'" + parameterValues[i] + "', ");
        }
        rQueryBuilder.append("'" + parameterValues[
            parameterValues.length - 1] + "'), ");
      }
    }
    imageFilenameBuilder.append(".png");
    String imageFilename = imageFilenameBuilder.toString();
    rQueryBuilder.append("path = '%s')");
    String rQuery = rQueryBuilder.toString();

    /* Request graph from graph controller, which either returns it from
     * its cache or asks Rserve to generate it. */
    byte[] graphBytes = graphGenerator.generateGraph(rQuery,
        imageFilename);

    /* Make sure that we have a graph to return. */
    if (graphBytes == null) {
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return;
    }

    /* Write graph bytes to response. */
    BufferedOutputStream output = null;
    response.setContentType("image/png");
    response.setHeader("Content-Length",
        String.valueOf(graphBytes.length));
    response.setHeader("Content-Disposition",
        "inline; filename=\"" + imageFilename + "\"");
    output = new BufferedOutputStream(response.getOutputStream(), 1024);
    output.write(graphBytes, 0, graphBytes.length);
    output.close();
  }
}

