package org.torproject.ernie.web;

import java.io.*;
import java.text.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

/* TODO This class shares a lot of code with the other *ImageServlet
 * classes. We should at some point try harder to reuse code. But let's
 * wait until we know what parameters besides start and end time will be
 * shared between these classes. We'll likely want to add more parameters
 * that reduce the code that can be re-used between servlets. */

public class RelayVersionsImageServlet extends HttpServlet {

  private GraphController graphController;

  private SimpleDateFormat dateFormat;

  public RelayVersionsImageServlet()  {
    this.graphController = GraphController.getInstance();
    this.dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    this.dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
  }

  public void doGet(HttpServletRequest request,
      HttpServletResponse response) throws IOException,
      ServletException {

    /* Check parameters. */
    String startParameter = request.getParameter("start");
    String endParameter = request.getParameter("end");
    if (startParameter == null && endParameter == null) {
      /* If no parameters are given, set default date range to the past 30
       * days. */
      long now = System.currentTimeMillis();
      startParameter = dateFormat.format(now
          - 30L * 24L * 60L * 60L * 1000L);
      endParameter = dateFormat.format(now);
    } else if (startParameter != null && endParameter != null) {
      long startTimestamp = -1L, endTimestamp = -1L;
      try {
        startTimestamp = dateFormat.parse(startParameter).getTime();
        endTimestamp = dateFormat.parse(endParameter).getTime();
      } catch (ParseException e)  {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        return;
      }
      /* The parameters are dates. Good. Does end not precede start? */
      if (startTimestamp > endTimestamp) {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        return;
      }
      /* And while we're at it, make sure both parameters lie in this
       * century. */
      if (!startParameter.startsWith("20") ||
          !endParameter.startsWith("20")) {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        return;
      }
      /* Looks like sane parameters. Re-format them to get a canonical
       * version, not something like 2010-1-1, 2010-01-1, etc. */
      startParameter = dateFormat.format(startTimestamp);
      endParameter = dateFormat.format(endTimestamp);
    } else {
      /* Either none or both of start and end need to be set. */
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    /* Request graph from graph controller, which either returns it from
     * its cache or asks Rserve to generate it. */
    String imageFilename = "versions-" + startParameter + "-"
        + endParameter + ".png";
    String rQuery = "plot_versions_line('" + startParameter + "', '"
        + endParameter + "', '%s')";
    byte[] graphBytes = graphController.generateGraph(rQuery,
        imageFilename);

    /* Make sure that we have a graph to return. */
    if (graphBytes == null) {
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
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

