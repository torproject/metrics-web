package org.torproject.ernie.web;

import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.commons.lang.time.*;

public class NetworkStatusServlet extends HttpServlet {

  public void doGet(HttpServletRequest request,
      HttpServletResponse response) throws IOException, ServletException {

    /* Forward the request to the JSP. */
    request.getRequestDispatcher("WEB-INF/networkstatus.jsp").forward(
        request, response);
  }
}

