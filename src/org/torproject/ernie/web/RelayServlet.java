package org.torproject.ernie.web;

import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;

public class RelayServlet extends HttpServlet {

  public void doGet(HttpServletRequest request,
      HttpServletResponse response) throws IOException, ServletException {

    /* Forward the request to the JSP. */
    request.getRequestDispatcher("WEB-INF/relay.jsp").forward(
        request, response);
  }
}

