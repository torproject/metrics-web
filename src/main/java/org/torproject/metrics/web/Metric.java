/* Copyright 2016--2017 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.web;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@SuppressWarnings("checkstyle:membername")
public class Metric {

  private String id;

  private String title;

  private String type;

  private String description;

  private String function;

  private String[] parameters;

  private String[] data;

  private String[] table_headers;

  private String[] table_cell_formats;

  private String data_file;

  private String[] data_column_spec;

  @Expose
  @SerializedName("include_related_events")
  private boolean includeRelatedEvents = false;

  public String getId() {
    return this.id;
  }

  public String getTitle() {
    return this.title;
  }

  public String getType() {
    return this.type;
  }

  public String getDescription() {
    return this.description;
  }

  public String getFunction() {
    return this.function;
  }

  public String[] getParameters() {
    return this.parameters;
  }

  public String[] getTableHeaders() {
    return this.table_headers;
  }

  public String[] getTableCellFormats() {
    return this.table_cell_formats;
  }

  public String getDataFile() {
    return this.data_file;
  }

  public String[] getDataColumnSpec() {
    return this.data_column_spec;
  }

  public String[] getData() {
    return this.data;
  }

  public boolean getIncludeRelatedEvents() {
    return this.includeRelatedEvents;
  }
}

