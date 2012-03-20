package org.torproject.ernie.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RelayServlet extends HttpServlet {

  private static final long serialVersionUID = 2375986581752040224L;

  public void doGet(HttpServletRequest request,
      HttpServletResponse response) throws IOException, ServletException {

    /* Forward the request to the JSP. */
    request.getRequestDispatcher("WEB-INF/relay.jsp").forward(
        request, response);
  }
}

