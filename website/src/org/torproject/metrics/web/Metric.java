/* Copyright 2016 The Tor Project
 * See LICENSE for licensing information */
package org.torproject.metrics.web;

public class Metric {
  private String id;
  private String title;
  private String[] tags;
  private String type;
  private String level;
  private String description;
  private String function;
  private String[] parameters;
  private String[] data;
  private String[] related;
  private String[] table_headers;
  private String[] table_cell_formats;
  private String data_file;
  private String[] data_column_spec;
  public String getId() {
    return this.id;
  }
  public String getTitle() {
    return this.title;
  }
  public String[] getTags() {
    return this.tags;
  }
  public String getType() {
    return this.type;
  }
  public String getLevel() {
    return this.level;
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
  public String[] getRelated() {
    return this.related;
  }
}

