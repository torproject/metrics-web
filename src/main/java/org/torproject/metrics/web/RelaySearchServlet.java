/* Copyright 2011--2018 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RelaySearchServlet extends AnyServlet {

  private static final long serialVersionUID = 1L;

  @Override
  public void doGet(HttpServletRequest request,
      HttpServletResponse response) throws IOException, ServletException {

    /* Forward the request to the JSP that does all the hard work. */
    String[] additionalStylesheets = {"/css/atlas.css"};
    request.setAttribute("additionalStylesheets", additionalStylesheets);
    request.setAttribute("categories", this.categories);
    request.getRequestDispatcher("WEB-INF/rs.jsp").forward(request,
        response);
  }
}

