package org.torproject.ernie.web;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;

public class GraphsSubpagesServlet extends HttpServlet {

  /* Available graphs subpages with corresponding JSP to which requests
   * are forwarded. */
  private Map<String, String> availableGraphsSubpages;

  public GraphsSubpagesServlet() {
    this.availableGraphsSubpages = new HashMap<String, String>();
    this.availableGraphsSubpages.put("network.html",
        "WEB-INF/network.jsp");
    this.availableGraphsSubpages.put("users.html", "WEB-INF/users.jsp");
    this.availableGraphsSubpages.put("packages.html",
        "WEB-INF/packages.jsp");
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

    /* Find out which graph type was requested, if any. */
    String requestedGraph = request.getParameter("graph");
    if (requestedGraph != null) {

      /* Check if the passed parameters are valid. */
      Map<String, String[]> checkedParameters = GraphParameterChecker.
          getInstance().checkParameters(requestedGraph,
          request.getParameterMap());
      if (checkedParameters != null) {

        /* Set the graph's attributes to the appropriate values, so that
         * we can display the correct graph and prepopulate the form. */
        StringBuilder urlBuilder = new StringBuilder();
        for (Map.Entry<String, String[]> param :
            checkedParameters.entrySet()) {
          request.setAttribute(requestedGraph.replaceAll("-", "_") + "_"
              + param.getKey(), param.getValue());
          for (String paramValue : param.getValue()) {
            urlBuilder.append("&" + param.getKey() + "=" + paramValue);
          }
        }
        String url = "?" + urlBuilder.toString().substring(1);
        request.setAttribute(requestedGraph.replaceAll("-", "_") + "_url",
            url);
      }
    }

    /* Forward the request to the JSP that does all the hard work. */
    request.getRequestDispatcher(jsp).forward(request, response);
  }
}

