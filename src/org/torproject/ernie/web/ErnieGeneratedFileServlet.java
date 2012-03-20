package org.torproject.ernie.web;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ErnieGeneratedFileServlet extends HttpServlet {

  public void doGet(HttpServletRequest request,
      HttpServletResponse response) throws IOException,
      ServletException {

    /* Read file from disk and write it to response. */
    String requestedURL = request.getRequestURI();
    if (requestedURL.contains("/")) {
      requestedURL = requestedURL.substring(requestedURL.
          lastIndexOf("/"));
    }
    String fn = "/srv/metrics.torproject.org/ernie/website"
        + requestedURL;
    BufferedInputStream input = null;
    BufferedOutputStream output = null;
    try {
      File f = new File(fn);
      if (!f.exists()) {
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
        return;
      }
      response.setContentType(this.getServletContext().getMimeType(f.getName()));
      response.setHeader("Content-Length", String.valueOf(
          f.length()));
      response.setHeader("Content-Disposition",
          "inline; filename=\"" + f.getName() + "\"");
      input = new BufferedInputStream(new FileInputStream(f),
          1024);
      output = new BufferedOutputStream(response.getOutputStream(), 1024);
      byte[] buffer = new byte[1024];
      int length;
      while ((length = input.read(buffer)) > 0) {
          output.write(buffer, 0, length);
      }
    } finally {
      if (output != null) {
        output.close();
      }
      if (input != null) {
        input.close();
      }
    }
  }
}

