package org.torproject.ernie.web;

import java.util.*;
import java.text.*;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.log4j.Logger;
import org.apache.commons.codec.digest.DigestUtils;

public class RelayPlatformsImageServlet extends HttpServlet {

  private static final Logger log;
  private final String rquery;
  private final String graphName;
  private final GraphController gcontroller;
  private SimpleDateFormat simpledf;

  static {
    log = Logger.getLogger(RelayPlatformsImageServlet.class);
  }

  public RelayPlatformsImageServlet()  {
    this.graphName = "platforms";
    this.gcontroller = new GraphController(graphName);
    this.rquery = "plot_platforms_line('%s', '%s', '%s')";
    this.simpledf = new SimpleDateFormat("yyyy-MM-dd");
    this.simpledf.setTimeZone(TimeZone.getTimeZone("UTC"));
  }

  public void doGet(HttpServletRequest request,
      HttpServletResponse response) throws IOException,
      ServletException {

    try {
      String md5file, start, end, path, query;

      start = request.getParameter("start");
      end = request.getParameter("end");

      /* Validate input */
      try {
        simpledf.parse(start);
        simpledf.parse(end);
      } catch (ParseException e)  {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        return;
      }

      md5file = DigestUtils.md5Hex(graphName + "-" + start + "-" + end);
      path = gcontroller.getBaseDir() + md5file + ".png";

      query = String.format(rquery, start, end, path);

      gcontroller.generateGraph(query, path);
      gcontroller.writeOutput(path, request, response);

    } catch (IOException e) {
      log.warn(e.toString());
    }
  }
}
