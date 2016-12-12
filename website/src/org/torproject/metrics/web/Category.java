/* Copyright 2016 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.web;

import java.util.List;

public class Category {

  private String id;

  private String header;

  private String description;

  private List<String> metrics;

  public String getId() {
    return id;
  }

  public String getHeader() {
    return header;
  }

  public String getDescription() {
    return description;
  }

  public List<String> getMetrics() {
    return metrics;
  }
}

