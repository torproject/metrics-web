/* Copyright 2016--2018 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ErrorServlet extends AnyServlet {

  private static final long serialVersionUID = 6850919895248020945L;

  @Override
  public void init() throws ServletException {
    super.init();
  }

  @Override
  public void doGet(HttpServletRequest request,
      HttpServletResponse response) throws IOException, ServletException {

    /* Forward the request to the JSP that does all the hard work. */
    request.setAttribute("categories", this.categories);
    request.getRequestDispatcher("WEB-INF/error.jsp").forward(request,
        response);
  }
}

