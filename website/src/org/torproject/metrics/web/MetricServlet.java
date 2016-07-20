/* Copyright 2016 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.web;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

@SuppressWarnings("serial")
public abstract class MetricServlet extends HttpServlet {

  protected List<Metric> metrics;

  protected Map<String, Set<String>> idsByType =
      new HashMap<String, Set<String>>();

  protected Map<String, String> descriptions =
      new HashMap<String, String>();

  protected Map<String, String> titles = new HashMap<String, String>();

  protected Map<String, List<String>> parameters =
      new HashMap<String, List<String>>();

  protected Map<String, String[]> tableHeaders =
      new HashMap<String, String[]>();

  protected Map<String, String[]> tableCellFormats =
      new HashMap<String, String[]>();

  protected Map<String, String> dataFiles = new HashMap<String, String>();

  protected Map<String, String[]> dataColumnSpecs =
      new HashMap<String, String[]>();

  protected Map<String, List<String[]>> data =
      new HashMap<String, List<String[]>>();

  protected Map<String, List<String[]>> related =
      new HashMap<String, List<String[]>>();

  @Override
  public void init() throws ServletException {
    this.metrics = MetricsProvider.getInstance().getMetricsList();
    Map<String, String> allTypesAndTitles = new HashMap<String, String>();
    Map<String, String[]> dataIds = new HashMap<String, String[]>();
    Map<String, String[]> relatedIds = new HashMap<String, String[]>();
    for (Metric metric : this.metrics) {
      String id = metric.getId();
      String title = metric.getTitle();
      String type = metric.getType();
      allTypesAndTitles.put(id, String.format("%s: %s", type, title));
      if (!this.idsByType.containsKey(type)) {
        this.idsByType.put(type, new HashSet<String>());
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
      if (metric.getDataFile() != null) {
        this.dataFiles.put(id, metric.getDataFile());
      }
      if (metric.getDataColumnSpec() != null) {
        this.dataColumnSpecs.put(id, metric.getDataColumnSpec());
      }
      if (metric.getData() != null) {
        dataIds.put(id, metric.getData());
      }
      if (metric.getRelated() != null) {
        relatedIds.put(id, metric.getRelated());
      }
    }
    for (Set<String> ids : idsByType.values()) {
      for (String id : ids) {
        if (dataIds.containsKey(id)) {
          List<String[]> dataLinksTypesAndTitles =
              new ArrayList<String[]>();
          for (String dataId : dataIds.get(id)) {
            if (allTypesAndTitles.containsKey(dataId)) {
              dataLinksTypesAndTitles.add(new String[] { dataId + ".html",
                  allTypesAndTitles.get(dataId) } );
            }
          }
          this.data.put(id, dataLinksTypesAndTitles);
        }
        if (relatedIds.containsKey(id)) {
          List<String[]> relatedLinksTypesAndTitles =
              new ArrayList<String[]>();
          for (String relatedId : relatedIds.get(id)) {
            if (allTypesAndTitles.containsKey(relatedId)) {
              relatedLinksTypesAndTitles.add(new String[] {
                  relatedId + ".html",
                  allTypesAndTitles.get(relatedId) } );
            }
          }
          this.related.put(id, relatedLinksTypesAndTitles);
        }
      }
    }
  }
}

