/* Copyright 2013 The Tor Project
 * See LICENSE for licensing information */
package org.torproject.ernie.web.graphs;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class BubblesServlet extends HttpServlet {

  private static final long serialVersionUID = -6011833075497881033L;

  public void doGet(HttpServletRequest request,
      HttpServletResponse response) throws IOException, ServletException {

    /* Forward the request to the JSP that does all the hard work. */
    request.getRequestDispatcher("WEB-INF/bubbles.jsp").forward(request,
        response);
  }
}

