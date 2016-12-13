/* Copyright 2016 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.web;

public class News {

  private String start;

  private String end;

  private String place;

  private String[] protocols;

  private String description;

  private String[] links;

  String getStart() {
    return start;
  }

  String getEnd() {
    return end;
  }

  String getPlace() {
    return place;
  }

  String[] getProtocols() {
    return protocols;
  }

  String getDescription() {
    return description;
  }

  String[] getLinks() {
    return links;
  }
}

