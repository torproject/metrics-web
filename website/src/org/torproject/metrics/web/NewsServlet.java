/* Copyright 2016--2017 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.web;

import org.torproject.metrics.web.graphs.Countries;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class NewsServlet extends AnyServlet {

  private static final long serialVersionUID = -7696996243187241242L;

  protected List<News> sortedNews;

  protected SortedMap<String, String> countries;

  @Override
  public void init() throws ServletException {
    super.init();
    List<News> sortedNews = new ArrayList<News>();
    for (News news : ContentProvider.getInstance().getNewsList()) {
      if (news.getStart() != null) {
        sortedNews.add(news);
      }
    }
    Collections.sort(sortedNews, new Comparator<News>() {
      public int compare(News o1, News o2) {
        return o1.getStart().compareTo(o2.getStart()) * -1;
      }
    });
    this.sortedNews = sortedNews;
    SortedMap<String, String> countries = new TreeMap<String, String>();
    for (String[] country : Countries.getInstance().getCountryList()) {
      countries.put(country[0], country[1]);
    }
    this.countries = countries;
  }

  @Override
  public void doGet(HttpServletRequest request,
      HttpServletResponse response) throws IOException, ServletException {

    /* Create categories based on current system time. */
    Map<String, String[]> cutOffDates = new LinkedHashMap<String, String[]>();
    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
    cal.set(Calendar.DAY_OF_WEEK, 1);
    cutOffDates.put(String.format("%tF", cal),
        new String[] { "This week", "week" });
    cal.set(Calendar.DAY_OF_MONTH, 1);
    cutOffDates.put(String.format("%tF", cal),
        new String[] { "This month", "month" });
    cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) /3 * 3);
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
    } while (!this.sortedNews.isEmpty() &&
        yearStart.compareTo(this.sortedNews.get(0).getStart()) > 0);

    /* Sort news into categories. */
    Map<String[], List<String[]>> newsByCategory =
        new LinkedHashMap<String[], List<String[]>>();
    for (String[] category : cutOffDates.values()) {
      newsByCategory.put(category, new ArrayList<String[]>());
    }
    for (News news : this.sortedNews) {
      StringBuilder sb = new StringBuilder();
      sb.append("<h3 class=\"media-heading\">" + news.getStart());
      if (news.getEnd() != null) {
        sb.append(" to " + news.getEnd());
      }
      if (news.getPlace() != null) {
        if (this.countries.containsKey(news.getPlace())) {
          sb.append(" <span class=\"label label-warning\">"
              + this.countries.get(news.getPlace()) + "</span>");
        } else {
          sb.append(" <span class=\"label label-warning\">"
              + "Unknown country</span>");
        }
      }
      if (news.getProtocols() != null) {
        for (String protocol : news.getProtocols()) {
          if (protocol.equals("relay")) {
            sb.append(" <span class=\"label label-success\">"
                + "Relays</span>");
          } else if (protocol.equals("bridge")) {
            sb.append(" <span class=\"label label-primary\">"
                + "Bridges</span>");
          } else if (protocol.equals("<OR>")) {
            sb.append(" <span class=\"label label-info\">"
                + "&lt;OR&gt;</span>");
          } else {
            sb.append(" <span class=\"label label-info\">"
                + protocol + "</span>");
          }
        }
      }
      if (news.isUnknown()) {
        sb.append(" <span class=\"label label-default\">"
            + "Unknown</span>");
      }
      sb.append("</h3><p>" + news.getDescription() + "</p>");
      if (news.getLinks() != null && news.getLinks().length > 0) {
        int written = 0;
        sb.append("<p class=\"links\">");
        for (String link : news.getLinks()) {
          if (written++ > 0) {
            sb.append(" ");
          }
          if (link.startsWith("https://metrics.torproject.org/")) {
            sb.append(link);
          } else {
            sb.append(link.replaceFirst(">", " target=\"_blank\">"));
          }
        }
        sb.append("</p>");
      }
      String[] formattedNews = new String[] { sb.toString() };
      for (Map.Entry<String, String[]> category : cutOffDates.entrySet()) {
        if (news.getStart().compareTo(category.getKey()) >= 0) {
          newsByCategory.get(category.getValue()).add(formattedNews);
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
}

