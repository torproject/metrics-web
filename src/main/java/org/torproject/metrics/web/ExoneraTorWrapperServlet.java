/* Copyright 2018 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.web;

import org.torproject.metrics.exonerator.ExoneraTorServlet;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ExoneraTorWrapperServlet extends AnyServlet {

  private ExoneraTorServlet exoneraTorServlet;

  public ExoneraTorWrapperServlet() {
    this.exoneraTorServlet = new ExoneraTorServlet();
  }

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    this.exoneraTorServlet.init(config);
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    request.setAttribute("categories", this.categories);
    this.exoneraTorServlet.doGet(request, response);
  }
}
