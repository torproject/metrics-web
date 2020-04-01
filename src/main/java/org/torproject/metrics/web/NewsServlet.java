/* Copyright 2016--2020 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class NewsServlet extends AnyServlet {

  private static final long serialVersionUID = -7696996243187241242L;

  protected List<News> sortedNews;

  @Override
  public void init() throws ServletException {
    super.init();
    List<News> sortedNews = new ArrayList<>();
    for (News news : ContentProvider.getInstance().getNewsList()) {
      if (news.getStart() != null) {
        sortedNews.add(news);
      }
    }
    sortedNews.sort((o1, o2) -> o1.getStart().compareTo(o2.getStart()) * -1);
    this.sortedNews = sortedNews;
  }

  @Override
  public void doGet(HttpServletRequest request,
      HttpServletResponse response) throws IOException, ServletException {
    if (request.getRequestURI().endsWith("news.atom")) {
      doGetAtom(request, response);
    } else {
      doGetHtml(request, response);
    }
  }

  private void doGetHtml(HttpServletRequest request,
      HttpServletResponse response) throws IOException, ServletException {
    /* Create categories based on current system time. */
    Map<String, String[]> cutOffDates = new LinkedHashMap<>();
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.DAY_OF_WEEK, 1);
    cutOffDates.put(String.format("%tF", cal),
        new String[] { "This week", "week" });
    cal.set(Calendar.DAY_OF_MONTH, 1);
    cutOffDates.put(String.format("%tF", cal),
        new String[] { "This month", "month" });
    cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) / 3 * 3);
    cutOffDates.put(String.format("%tF", cal),
        new String[] { "This quarter", "quarter" });
    cal.set(Calendar.MONTH, 0);
    String yearStart = String.format("%tF", cal);
    cutOffDates.put(yearStart,
        new String[] { "This year", "year" });
    do {
      cal.add(Calendar.YEAR, -1);
      yearStart = String.format("%tF", cal);
      String year = String.format("%tY", cal);
      cutOffDates.put(yearStart, new String[] { year, year });
    } while (!this.sortedNews.isEmpty()
        && yearStart.compareTo(this.sortedNews.get(this.sortedNews.size() - 1)
        .getStart()) > 0);

    /* Sort news into categories. */
    Map<String[], List<News>> newsByCategory = new LinkedHashMap<>();
    for (String[] category : cutOffDates.values()) {
      newsByCategory.put(category, new ArrayList<>());
    }
    for (News news : this.sortedNews) {
      for (Map.Entry<String, String[]> category : cutOffDates.entrySet()) {
        if (news.getStart().compareTo(category.getKey()) >= 0) {
          newsByCategory.get(category.getValue()).add(news);
          break;
        }
      }
    }

    /* Remove categories without news. */
    for (String[] category : cutOffDates.values()) {
      if (newsByCategory.get(category).isEmpty()) {
        newsByCategory.remove(category);
      }
    }

    /* Pass navigation categories and the news to the JSP and let it do the rest
     * of the work. */
    request.setAttribute("categories", this.categories);
    request.setAttribute("news", newsByCategory);
    request.getRequestDispatcher("WEB-INF/news.jsp").forward(request,
        response);
  }

  private void doGetAtom(HttpServletRequest request,
      HttpServletResponse response) throws IOException, ServletException {
    request.setAttribute("news", this.sortedNews);
    request.setAttribute("updated", this.sortedNews.get(0).getStart());
    request.getRequestDispatcher("WEB-INF/news-atom.jsp").forward(request,
        response);
  }
}

