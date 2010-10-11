package org.torproject.ernie.web;

import java.io.*;
import java.text.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * Servlet that reads an HTTP request for a graph image, asks the
 * GraphController to generate this graph if it's not in the cache, and
 * returns the image bytes to the client.
 */
public class GraphImageServlet extends HttpServlet {

  private GraphController graphController;

  private SimpleDateFormat dateFormat;

  /* Available graphs with corresponding parameter lists. */
  private Map<String, String> availableGraphs;

  /* Known parameters and parameter values. */
  private Map<String, String> knownParameterValues;

  public GraphImageServlet()  {
    this.dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    this.dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

    this.availableGraphs = new HashMap<String, String>();
    this.availableGraphs.put("networksize", "start,end,filename");
    this.availableGraphs.put("relayflags", "start,end,flag,filename");
    this.availableGraphs.put("relayflags-hour",
        "start,end,flag,filename");
    this.availableGraphs.put("versions", "start,end,filename");
    this.availableGraphs.put("platforms", "start,end,filename");
    this.availableGraphs.put("bandwidth", "start,end,filename");
    this.availableGraphs.put("new-users", "start,end,country,filename");
    this.availableGraphs.put("direct-users",
        "start,end,country,filename");
    this.availableGraphs.put("bridge-users",
         "start,end,country,filename");
    this.availableGraphs.put("gettor", "start,end,bundle,filename");
    this.availableGraphs.put("torperf",
         "start,end,source,filesize,filename");

    this.knownParameterValues = new HashMap<String, String>();
    this.knownParameterValues.put("flag",
        "Running,Exit,Guard,Fast,Stable");
    this.knownParameterValues.put("country", "all,au,bh,br,ca,cn,cu,de,"
        + "et,fr,gb,ir,it,jp,kr,mm,pl,ru,sa,se,sy,tn,tm,us,uz,vn,ye");
    this.knownParameterValues.put("bundle", "all,en,zh_CN,fa");
    this.knownParameterValues.put("source", "siv,moria,torperf");
    this.knownParameterValues.put("filesize", "50kb,1mb,5mb");
  }

  public void init() {
    ServletConfig servletConfig = getServletConfig();
    String rserveHost = servletConfig.getInitParameter("rserveHost");
    String rservePort = servletConfig.getInitParameter("rservePort");
    String maxCacheAge = servletConfig.getInitParameter("maxCacheAge");
    String cachedGraphsDir = servletConfig.getInitParameter(
        "cachedGraphsDir");
    this.graphController = new GraphController(rserveHost, rservePort,
        maxCacheAge, cachedGraphsDir);
  }

  public void doGet(HttpServletRequest request,
      HttpServletResponse response) throws IOException,
      ServletException {

    /* Find out which graph type was requested and make sure we know this
     * graph type. */
    String requestedGraph = request.getRequestURI();
    if (requestedGraph == null || requestedGraph.length() < 6) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }
    requestedGraph = requestedGraph.substring(1,
        requestedGraph.length() - 4);
    if (!this.availableGraphs.containsKey(requestedGraph)) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    /* Find out which other parameters are supported by this graph type
     * and parse them if they are given. */
    Set<String> supportedGraphParameters = new HashSet<String>(Arrays.
        asList(this.availableGraphs.get(requestedGraph).split(",")));
    Map<String, String[]> recognizedGraphParameters =
        new HashMap<String, String[]>();

    /* Parse start and end dates if supported by the graph type. If
     * neither start nor end date are given, set the default date range to
     * the past 90 days. */
    if (supportedGraphParameters.contains("start") ||
        supportedGraphParameters.contains("end")) {
      String startParameter = request.getParameter("start");
      String endParameter = request.getParameter("end");
      if (startParameter == null && endParameter == null) {
        /* If no start and end parameters are given, set default date
         * range to the past 90 days. */
        long now = System.currentTimeMillis();
        startParameter = dateFormat.format(now
            - 90L * 24L * 60L * 60L * 1000L);
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
      recognizedGraphParameters.put("start",
          new String[] { startParameter });
      recognizedGraphParameters.put("end", new String[] { endParameter });
    }

    /* Parse relay flags if supported by the graph type. If no relay flags
     * are passed or none of them have been recognized, use the set of all
     * known flags as default. */
    if (supportedGraphParameters.contains("flag")) {
      String[] flagParameters = request.getParameterValues("flag");
      List<String> knownFlags = Arrays.asList(
          this.knownParameterValues.get("flag").split(","));
      if (flagParameters != null) {
        for (String flag : flagParameters) {
          if (flag == null || flag.length() == 0 ||
              !knownFlags.contains(flag)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
          }
        }
      } else {
        flagParameters = this.knownParameterValues.get("flag").split(",");
      }
      recognizedGraphParameters.put("flag", flagParameters);
    }

    /* Parse country codes if supported by the graph type. If no countries
     * are passed, use country code "all" (all countries) as default. */
    if (supportedGraphParameters.contains("country")) {
      String[] countryParameters = request.getParameterValues("country");
      List<String> knownCountries = Arrays.asList(
          this.knownParameterValues.get("country").split(","));
      if (countryParameters != null) {
        for (String country : countryParameters) {
          if (country == null || country.length() == 0 ||
              !knownCountries.contains(country)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
          }
        }
      } else {
        countryParameters = new String[] { "all" };
      }
      recognizedGraphParameters.put("country", countryParameters);
    }

    /* Parse GetTor bundle if supported by the graph type. Only a single
     * bundle can be passed. If no bundle is passed, use "all" as
     * default. */
    if (supportedGraphParameters.contains("bundle")) {
      String[] bundleParameter = request.getParameterValues("bundle");
      List<String> knownBundles = Arrays.asList(
          this.knownParameterValues.get("bundle").split(","));
      if (bundleParameter != null) {
        if (bundleParameter.length != 1) {
          response.sendError(HttpServletResponse.SC_BAD_REQUEST);
          return;
        }
        if (bundleParameter[0].length() == 0 ||
            !knownBundles.contains(bundleParameter[0])) {
          response.sendError(HttpServletResponse.SC_BAD_REQUEST);
          return;
        }
      } else {
        bundleParameter = new String[] { "all" };
      }
      recognizedGraphParameters.put("bundle", bundleParameter);
    }

    /* Parse torperf data source if supported by the graph type. Only a
     * single source can be passed. If no source is passed, use "torperf"
     * as default. */
    if (supportedGraphParameters.contains("source")) {
      String[] sourceParameter = request.getParameterValues("source");
      List<String> knownSources = Arrays.asList(
          this.knownParameterValues.get("source").split(","));
      if (sourceParameter != null) {
        if (sourceParameter.length != 1) {
          response.sendError(HttpServletResponse.SC_BAD_REQUEST);
          return;
        }
        if (sourceParameter[0].length() == 0 ||
            !knownSources.contains(sourceParameter[0])) {
          response.sendError(HttpServletResponse.SC_BAD_REQUEST);
          return;
        }
      } else {
        sourceParameter = new String[] { "torperf" };
      }
      recognizedGraphParameters.put("source", sourceParameter);
    }

    /* Parse torperf file size if supported by the graph type. Only a
     * single file size can be passed. If no file size is passed, use
     * "50kb" as default. */
    if (supportedGraphParameters.contains("filesize")) {
      String[] filesizeParameter = request.getParameterValues("filesize");
      List<String> knownFilesizes = Arrays.asList(
          this.knownParameterValues.get("filesize").split(","));
      if (filesizeParameter != null) {
        if (filesizeParameter.length != 1) {
          response.sendError(HttpServletResponse.SC_BAD_REQUEST);
          return;
        }
        if (filesizeParameter[0].length() == 0 ||
            !knownFilesizes.contains(filesizeParameter[0])) {
          response.sendError(HttpServletResponse.SC_BAD_REQUEST);
          return;
        }
      } else {
        filesizeParameter = new String[] { "50kb" };
      }
      recognizedGraphParameters.put("filesize", filesizeParameter);
    }

    /* Prepare filename and R query string. */
    StringBuilder rQueryBuilder = new StringBuilder("plot_"
        + requestedGraph.replaceAll("-", "_") + "("),
        imageFilenameBuilder = new StringBuilder(requestedGraph);
    List<String> requiredGraphParameters = Arrays.asList(
        this.availableGraphs.get(requestedGraph).split(","));
    for (String graphParameter : requiredGraphParameters) {
      if (graphParameter.equals("filename")) {
        break;
      }
      if (!recognizedGraphParameters.containsKey(graphParameter)) {
        /* We should have parsed this parameter above! */
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
            "Missing parameter: " + graphParameter);
        return;
      }
      String[] parameterValues = recognizedGraphParameters.get(
          graphParameter);
      if (parameterValues.length == 0) {
        /* We should not have added a zero-length array here! */
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
            "Missing parameter: " + graphParameter);
        return;
      }
      for (String param : parameterValues) {
        imageFilenameBuilder.append("-" + param);
      }
      if (parameterValues.length < 2) {
        rQueryBuilder.append("'" + parameterValues[0] + "', ");
      } else {
        rQueryBuilder.append("c(");
        for (int i = 0; i < parameterValues.length - 1; i++) {
          rQueryBuilder.append("'" + parameterValues[i] + "', ");
        }
        rQueryBuilder.append("'" + parameterValues[
            parameterValues.length - 1] + "'), ");
      }
    }
    imageFilenameBuilder.append(".png");
    String imageFilename = imageFilenameBuilder.toString();
    rQueryBuilder.append("'%s')");
    String rQuery = rQueryBuilder.toString();

    /* Request graph from graph controller, which either returns it from
     * its cache or asks Rserve to generate it. */
    byte[] graphBytes = graphController.generateGraph(rQuery,
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

