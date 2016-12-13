/* Copyright 2016 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class NewsServlet extends HttpServlet {

  private static final long serialVersionUID = -7696996243187241242L;

  protected SortedSet<News> sortedNews;

  @Override
  public void init() throws ServletException {
    SortedSet<News> sortedNews = new TreeSet<News>(new Comparator<News>() {
      public int compare(News o1, News o2) {
        return o1.getStart().compareTo(o2.getStart()) * -1;
      }
    });
    for (News news : ContentProvider.getInstance().getNewsList()) {
      if (news.getStart() != null) {
        sortedNews.add(news);
      }
    }
    this.sortedNews = sortedNews;
  }

  @Override
  public void doGet(HttpServletRequest request,
      HttpServletResponse response) throws IOException, ServletException {

    /* Create categories based on current system time. */
    Map<String, String> cutOffDates = new LinkedHashMap<String, String>();
    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
    cal.set(Calendar.DAY_OF_WEEK, 1);
    cutOffDates.put(String.format("%tF", cal), "This week");
    cal.set(Calendar.DAY_OF_MONTH, 1);
    cutOffDates.put(String.format("%tF", cal), "This month");
    cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) /3 * 3);
    cutOffDates.put(String.format("%tF", cal), "This quarter");
    cal.set(Calendar.MONTH, 0);
    String yearStart = String.format("%tF", cal);
    cutOffDates.put(yearStart, "This year");
    do {
      cal.add(Calendar.YEAR, -1);
      yearStart = String.format("%tF", cal);
      cutOffDates.put(yearStart, String.format("%tY", cal));
    } while (yearStart.compareTo(this.sortedNews.first().getStart()) > 0);

    /* Sort news into categories. */
    Map<String, List<String[]>> newsByCategory =
        new LinkedHashMap<String, List<String[]>>();
    for (String category : cutOffDates.values()) {
      newsByCategory.put(category, new ArrayList<String[]>());
    }
    for (News news : this.sortedNews) {
      StringBuilder sb = new StringBuilder();
      sb.append("<p>" + news.getStart());
      if (news.getEnd() != null) {
        sb.append("&ndash;" + news.getEnd());
      }
      sb.append(": ");
      if (news.getPlace() != null) {
        sb.append(news.getPlace() + ", ");
      }
      if (news.getProtocols() != null) {
        int written = 0;
        for (String protocol : news.getProtocols()) {
          sb.append((written++ > 0 ? ", " : "") + protocol);
        }
      }
      sb.append(", " + news.getDescription());
      if (news.getLinks() != null && news.getLinks().length > 0) {
        int written = 0;
        sb.append(" (");
        for (String link : news.getLinks()) {
          sb.append((written++ > 0 ? " " : "") + link);
        }
        sb.append(")");
      }
      sb.append("</p>");
      String[] formattedNews = new String[] { sb.toString() };
      for (Map.Entry<String, String> category : cutOffDates.entrySet()) {
        if (news.getStart().compareTo(category.getKey()) >= 0) {
          newsByCategory.get(category.getValue()).add(formattedNews);
          break;
        }
      }
    }

    /* Remove categories without news. */
    for (String category : cutOffDates.values()) {
      if (newsByCategory.get(category).isEmpty()) {
        newsByCategory.remove(category);
      }
    }

    /* Pass news by category to the JSP and let it do the rest of the work. */
    request.setAttribute("news", newsByCategory);
    request.getRequestDispatcher("WEB-INF/news.jsp").forward(request,
        response);
  }
}

