/* Copyright 2016--2018 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.web;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class GraphServlet extends MetricServlet {

  private Map<String, String[][]> defaultParameters = new HashMap<>();

  @Override
  public void init() throws ServletException {
    super.init();
    this.defaultParameters.put("p", new String[][] {
        { "100", " checked", "100 (maximum)" },
        { "99", "", "99" },
        { "98", "", "98" },
        { "97", "", "97" },
        { "95", "", "95" },
        { "91", "", "91" },
        { "90", "", "90" },
        { "80", "", "80" },
        { "75", "", "75 (3rd quartile)" },
        { "70", "", "70" },
        { "60", "", "60" },
        { "50", "", "50 (median)" },
        { "40", "", "40" },
        { "30", "", "30" },
        { "25", "", "25 (first quartile)" },
        { "20", "", "20" },
        { "10", "", "10" },
        { "9", "", "9" },
        { "5", "", "5" },
        { "3", "", "3" },
        { "2", "", "2" },
        { "1", "", "1" },
        { "0", "", "0 (minimum)" } });
    this.defaultParameters.put("n", new String[][] {
        { "1", " checked" },
        { "2", "" },
        { "3", "" },
        { "5", "" },
        { "10", "" },
        { "20", "" },
        { "30", "" },
        { "50", "" },
        { "100", "" },
        { "200", "" },
        { "300", "" },
        { "500", "" },
        { "1000", "" },
        { "2000", "" },
        { "3000", "" },
        { "5000", "" } });
    this.defaultParameters.put("flag", new String[][] {
        { "Running", " checked" },
        { "Exit", " checked" },
        { "Fast", " checked" },
        { "Guard", " checked" },
        { "Stable", " checked" },
        { "HSDir", "" } });
    List<String[]> knownCountries =
        Countries.getInstance().getCountryList();
    String[][] countries = new String[knownCountries.size() + 1][];
    int index = 0;
    countries[index++] = new String[] { "all", " selected", "All users" };
    for (String[] country : knownCountries) {
      countries[index++] = new String[] { country[0], "", country[1] };
    }
    this.defaultParameters.put("country", countries);
    this.defaultParameters.put("events", new String[][] {
        { "off", " selected", "Off" },
        { "on", "", "On: both points and expected range" },
        { "points", "", "On: points only, no expected range" } });
    this.defaultParameters.put("transport", new String[][] {
        { "!<OR>", " checked", "Any pluggable transport" },
        { "obfs2", "", "obfs2" },
        { "obfs3", "", "obfs3" },
        { "obfs4", "", "obfs4" },
        { "websocket", "", "Flash proxy/websocket" },
        { "fte", "", "FTE" },
        { "meek", "", "meek" },
        { "scramblesuit", "", "scramblesuit" },
        { "snowflake", "", "snowflake" },
        { "<??>", "", "Unknown pluggable transport(s)" },
        { "<OR>", "", "Default OR protocol" } });
    this.defaultParameters.put("version", new String[][] {
        { "v4", " selected", "IPv4" },
        { "v6", "", "IPv6" } });
    this.defaultParameters.put("source", new String[][] {
        { "all", " checked" },
        { "torperf", "" },
        { "moria", "" },
        { "siv", "" },
        { "op-hk", "" },
        { "op-nl", "" },
        { "op-us", "" }});
    this.defaultParameters.put("server", new String[][] {
        { "public", " checked" },
        { "onion", "" }});
    this.defaultParameters.put("filesize", new String[][] {
        { "50kb", " checked", "50 KiB" },
        { "1mb", "", "1 MiB" },
        { "5mb", "", "5 MiB" } });
  }

  @Override
  protected void doGet(HttpServletRequest request,
      HttpServletResponse response) throws ServletException, IOException {
    String requestUri = request.getRequestURI();
    if (requestUri == null || !requestUri.endsWith(".html")) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }
    String requestedId = requestUri.substring(
        requestUri.contains("/") ? requestUri.lastIndexOf("/") + 1 : 0,
        requestUri.length() - 5);
    if (!this.idsByType.containsKey("Graph")
        || !this.idsByType.get("Graph").contains(requestedId)) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }
    request.setAttribute("categories", this.categories);
    request.setAttribute("id", requestedId);
    request.setAttribute("title", this.titles.get(requestedId));
    if (this.categoriesById.containsKey(requestedId)) {
      Category category = this.categoriesById.get(requestedId);
      request.setAttribute("categoryHeader", category.getHeader());
      request.setAttribute("categoryDescription", category.getDescription());
      List<String[]> categoryTabs = new ArrayList<>();
      for (String metricId : category.getMetrics()) {
        categoryTabs.add(new String[] { this.titles.get(metricId), metricId });
      }
      request.setAttribute("categoryTabs", categoryTabs);
    }
    request.setAttribute("description",
        this.descriptions.get(requestedId));
    request.setAttribute("data", this.data.get(requestedId));
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    Date defaultEndDate = new Date();
    Date defaultStartDate = new Date(defaultEndDate.getTime()
        - 90L * 24L * 60L * 60L * 1000L);
    if (this.parameters.containsKey(requestedId)) {
      Map<String, String[]> checkedParameters = GraphParameterChecker
          .getInstance().checkParameters(requestedId,
          request.getParameterMap());
      StringBuilder urlBuilder = new StringBuilder();
      for (String parameter : this.parameters.get(requestedId)) {
        switch (parameter) {
          case "start":
          case "end":
            String[] requestParameter;
            if (checkedParameters != null
                && checkedParameters.containsKey(parameter)) {
              requestParameter = checkedParameters.get(parameter);
            } else {
              requestParameter = new String[] {
                  dateFormat.format(parameter.equals("start")
                  ? defaultStartDate : defaultEndDate) };
            }
            urlBuilder.append(String.format("&amp;%s=%s", parameter,
                requestParameter[0]));
            request.setAttribute(parameter, requestParameter);
            break;
          default:
            String[][] defaultParameters =
                this.defaultParameters.get(parameter);
            String[][] requestParameters =
                new String[defaultParameters.length][];
            Set<String> checked = null;
            if (checkedParameters != null
                && checkedParameters.containsKey(parameter)) {
              checked = new HashSet<>(Arrays.asList(
                  checkedParameters.get(parameter)));
            }
            String checkedOrSelected = parameter.equals("country")
                || parameter.equals("events") || parameter.equals("version")
                ? " selected" : " checked";
            for (int i = 0; i < defaultParameters.length; i++) {
              requestParameters[i] =
                  new String[defaultParameters[i].length];
              System.arraycopy(defaultParameters[i], 0,
                  requestParameters[i], 0, defaultParameters[i].length);
              if (checked != null) {
                if (checked.contains(requestParameters[i][0])) {
                  requestParameters[i][1] = checkedOrSelected;
                  urlBuilder.append(String.format("&amp;%s=%s", parameter,
                      requestParameters[i][0]));
                } else {
                  requestParameters[i][1] = "";
                }
              }
            }
            request.setAttribute(parameter, requestParameters);
            break;
        }
      }
      if (urlBuilder.length() > 5) {
        String url = "?" + urlBuilder.toString().substring(5);
        request.setAttribute("parameters", url);
      }
      if (this.includeRelatedEvents.contains(requestedId)) {
        request.setAttribute("includeRelatedEvents", true);
        String startParameter = dateFormat.format(defaultStartDate);
        String endParameter = dateFormat.format(defaultEndDate);
        String countryParameter = "all";
        String eventsParameter = "off";
        if (null != checkedParameters) {
          for (Map.Entry<String, String[]> checkedParameter
              : checkedParameters.entrySet()) {
            switch (checkedParameter.getKey()) {
              case "start":
                startParameter = checkedParameter.getValue()[0];
                break;
              case "end":
                endParameter = checkedParameter.getValue()[0];
                break;
              case "country":
                countryParameter = checkedParameter.getValue()[0];
                break;
              case "events":
                eventsParameter = checkedParameter.getValue()[0];
                break;
              default: // don't care about other parameters
            }
          }
        }
        if (!"off".equals(eventsParameter)) {
          request.setAttribute("displayEventsNotice", true);
        }
        List<String> relatedEvents = new ArrayList<>();
        for (News event : this.sortedEvents) {
          if (null == event.getStart()) {
            /* Skip event without start date. */
            continue;
          }
          if (event.getStart().compareTo(endParameter) > 0) {
            /* Skip event starting after displayed time period. */
            continue;
          }
          if (null != event.getEnd()
              && event.getEnd().compareTo(startParameter) < 0) {
            /* Skip multi-day event ending before displayed time period. */
            continue;
          }
          if (null == event.getEnd()
              && event.getStart().compareTo(startParameter) < 0) {
            /* Skip single-day event happening before displayed time period. */
            continue;
          }
          if (!"all".equals(countryParameter) && null != event.getPlaces()
              && !event.getPlaces().contains(countryParameter)) {
            /* Skip country-specific event for another country than the
             * displayed one. */
            continue;
          }
          /* We could filter by transport or version here, but that's a
           * non-trivial task. */
          relatedEvents.add(event.formatAsTableRow());
        }
        request.setAttribute("relatedEvents", relatedEvents);
      }
    }
    request.getRequestDispatcher("WEB-INF/graph.jsp").forward(request,
        response);
  }
}

