/* Copyright 2016 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.web;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MetricsProvider {

  private static MetricsProvider instance = new MetricsProvider();

  public static MetricsProvider getInstance() {
    return MetricsProvider.instance;
  }

  private List<Metric> metricsList;

  private MetricsProvider() {
    InputStream in = this.getClass().getClassLoader()
        .getResourceAsStream("metrics.json");
    Gson gson = new GsonBuilder().create();
    Metric[] metricsArray = gson.fromJson(new InputStreamReader(in),
        Metric[].class);
    this.metricsList = Arrays.asList(metricsArray);
  }

  public List<Metric> getMetricsList() {
    return new ArrayList<Metric>(this.metricsList);
  }
}

