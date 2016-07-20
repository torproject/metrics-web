/* Copyright 2016 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class LinkServlet extends MetricServlet {

  @Override
  public void doGet(HttpServletRequest request,
      HttpServletResponse response) throws IOException, ServletException {
    String requestUri = request.getRequestURI();
    if (requestUri == null || !requestUri.endsWith(".html")) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }
    String requestedId = requestUri.substring(
        requestUri.contains("/") ? requestUri.lastIndexOf("/") + 1 : 0,
        requestUri.length() - 5);
    if (!this.idsByType.containsKey("Link")
        || !this.idsByType.get("Link").contains(requestedId)) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }
    request.setAttribute("id", requestedId);
    request.setAttribute("title", this.titles.get(requestedId));
    request.setAttribute("description",
        this.descriptions.get(requestedId));
    request.setAttribute("data", this.data.get(requestedId));
    request.setAttribute("related", this.related.get(requestedId));
    request.getRequestDispatcher("WEB-INF/link.jsp").forward(request,
        response);
  }
}

