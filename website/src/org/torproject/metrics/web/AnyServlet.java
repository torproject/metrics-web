package org.torproject.metrics.web;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

public abstract class AnyServlet extends HttpServlet {

  private static final long serialVersionUID = -3670417319698456329L;

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
}
