/* Copyright 2016--2018 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.web;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class News {

  public static class Link {
    String label;
    String target;

    public String getLabel() {
      return label;
    }

    public String getTarget() {
      return target;
    }
  }

  String start;

  String end;

  Boolean ongoing;

  List<String> places;

  List<String> protocols;

  String shortDescription;

  String description;

  List<Link> links;

  Boolean unknown;

  void addLink(String label, String target) {
    if (null == links) {
      links = new ArrayList<>();
    }
    Link link = new Link();
    link.label = label;
    link.target = target;
    links.add(link);
  }

  public String getStart() {
    return this.start;
  }

  public String getEnd() {
    return this.end;
  }

  /**
   * Returns whether or not the event is ongoing. If no value was set, it is
   * assumed that the event is not ongoing.
   */
  public boolean isOngoing() {
    if (this.ongoing != null) {
      return this.ongoing;
    } else {
      return false;
    }
  }

  public List<String> getPlaces() {
    return this.places;
  }

  /**
   * Returns an array of country names looked up from the country codes
   * associated with this news entry. If a country is unknown, that country
   * will be added to the list as "Unknown Country". There is no deduplication
   * of countries, including the output of "Unknown Country".
   */
  public List<String> getPlaceNames() {
    if (null == this.places) {
      return null;
    }
    List<String> placeNames = new ArrayList<>();
    for (String place : this.places) {
      if (countries.containsKey(place)) {
        placeNames.add(countries.get(place));
      } else {
        placeNames.add("Unknown Country");
      }
    }
    return placeNames;
  }

  public List<String> getProtocols() {
    return this.protocols;
  }

  public String getDescription() {
    return this.description;
  }

  public String getShortDescription() {
    return this.shortDescription;
  }

  public List<Link> getLinks() {
    return this.links;
  }

  /**
   * Returns whether or not the reason for an event is known. If no value was
   * set, it is assumed that the reason is known.
   */
  public boolean isUnknown() {
    if (this.unknown != null) {
      return this.unknown;
    } else {
      return false;
    }
  }

  static SortedMap<String, String> countries;

  static {
    countries = new TreeMap<>();
    for (String[] country : Countries.getInstance().getCountryList()) {
      countries.put(country[0], country[1]);
    }
  }

}
