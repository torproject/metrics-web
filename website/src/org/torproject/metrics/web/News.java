/* Copyright 2016--2017 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.web;

public class News {

  private String start;

  private String end;

  private String place;

  private String[] protocols;

  private String description;

  private String[] links;

  private boolean unknown;

  String getStart() {
    return this.start;
  }

  String getEnd() {
    return this.end;
  }

  String getPlace() {
    return this.place;
  }

  String[] getProtocols() {
    return this.protocols;
  }

  String getDescription() {
    return this.description;
  }

  String[] getLinks() {
    return this.links;
  }

  boolean isUnknown() {
    return this.unknown;
  }
}

