/* Copyright 2016 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.web;

import java.util.List;

public class Category {

  private String id;

  private String icon;

  private String header;

  private String summary;

  private String description;

  private List<String> metrics;

  public String getId() {
    return this.id;
  }

  public String getIcon() {
    return this.icon;
  }

  public String getHeader() {
    return this.header;
  }

  public String getSummary() {
    return this.summary;
  }

  public String getDescription() {
    return this.description;
  }

  public List<String> getMetrics() {
    return this.metrics;
  }
}

