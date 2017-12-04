/* Copyright 2016--2017 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.web;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;

@SuppressWarnings("serial")
public abstract class MetricServlet extends AnyServlet {

  protected List<Metric> metrics;

  protected Map<String, Set<String>> idsByType = new HashMap<>();

  protected Map<String, String> descriptions = new HashMap<>();

  protected Map<String, String> titles = new HashMap<>();

  protected Map<String, List<String>> parameters = new HashMap<>();

  protected Map<String, String[]> tableHeaders = new HashMap<>();

  protected Map<String, String[]> tableCellFormats = new HashMap<>();

  protected Map<String, String[]> data = new HashMap<>();

  protected Map<String, Category> categoriesById = new HashMap<>();

  protected Set<String> includeRelatedEvents = new HashSet<>();

  protected List<News> sortedEvents = new ArrayList<>();

  @Override
  public void init() throws ServletException {
    super.init();
    this.metrics = ContentProvider.getInstance().getMetricsList();
    for (Metric metric : this.metrics) {
      String id = metric.getId();
      String title = metric.getTitle();
      String type = metric.getType();
      if (!this.idsByType.containsKey(type)) {
        this.idsByType.put(type, new HashSet<>());
      }
      this.idsByType.get(type).add(id);
      this.titles.put(id, title);
      this.descriptions.put(id, metric.getDescription());
      if (metric.getParameters() != null) {
        this.parameters.put(id, Arrays.asList(metric.getParameters()));
      }
      if (metric.getTableHeaders() != null) {
        this.tableHeaders.put(id, metric.getTableHeaders());
      }
      if (metric.getTableCellFormats() != null) {
        this.tableCellFormats.put(id, metric.getTableCellFormats());
      }
      if (metric.getData() != null) {
        this.data.put(id, metric.getData());
      }
      if (metric.getIncludeRelatedEvents()) {
        this.includeRelatedEvents.add(id);
      }
    }
    for (Category category :
        ContentProvider.getInstance().getCategoriesList()) {
      for (String id : category.getMetrics()) {
        this.categoriesById.put(id, category);
      }
    }
    this.sortedEvents.addAll(ContentProvider.getInstance().getNewsList());
    Collections.sort(this.sortedEvents,
        (o1, o2) -> o2.getStart().compareTo(o1.getStart()));
  }
}

