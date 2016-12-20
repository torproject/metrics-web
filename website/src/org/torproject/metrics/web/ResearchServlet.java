/* Copyright 2016 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ResearchServlet extends AnyServlet {

  private static final long serialVersionUID = 7380914323836427960L;

  @Override
  public void init() throws ServletException {
    super.init();
  }

  @Override
  public void doGet(HttpServletRequest request,
      HttpServletResponse response) throws IOException, ServletException {

    /* Forward the request to the JSP that does all the hard work. */
    request.setAttribute("categories", this.categories);
    request.getRequestDispatcher("WEB-INF/research.jsp").forward(request,
        response);
  }
}

