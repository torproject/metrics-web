/* Copyright 2011--2016 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class IndexServlet extends HttpServlet {

  private static final long serialVersionUID = -5156539049907533057L;

  protected List<String[]> categories;

  @Override
  public void init() throws ServletException {
    List<String[]> categories = new ArrayList<String[]>();
    for (Category category :
        ContentProvider.getInstance().getCategoriesList()) {
      categories.add(new String[] {
          category.getMetrics().isEmpty() ? "" : category.getMetrics().get(0),
          category.getHeader(), category.getSummary(), category.getIcon() });
    }
    this.categories = categories;
  }

  @Override
  public void doGet(HttpServletRequest request,
      HttpServletResponse response) throws IOException, ServletException {

    /* Forward the request to the JSP that does all the hard work. */
    request.setAttribute("categories", this.categories);
    request.getRequestDispatcher("WEB-INF/index.jsp").forward(request,
        response);
  }
}

