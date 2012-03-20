/* Copyright 2011, 2012 The Tor Project
 * See LICENSE for licensing information */
package org.torproject.ernie.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ResearchFormatsServlet extends HttpServlet {

  private static final long serialVersionUID = 5666493868675314116L;

  public void doGet(HttpServletRequest request,
      HttpServletResponse response) throws IOException, ServletException {

    /* Forward the request to the JSP that does all the hard work. */
    request.getRequestDispatcher("WEB-INF/formats.jsp").forward(request,
        response);
  }
}

