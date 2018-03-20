/* Copyright 2016--2018 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.web;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class News {

  String start;

  String end;

  Boolean ongoing;

  List<String> places;

  List<String> protocols;

  String description;

  List<String> links;

  Boolean unknown;

  String getStart() {
    return this.start;
  }

  String getEnd() {
    return this.end;
  }

  /**
   * Returns whether or not the event is ongoing. If no value was set, it is
   * assumed that the event is not ongoing.
   */
  boolean getOngoing() {
    if (this.ongoing != null) {
      return this.ongoing;
    } else {
      return false;
    }
  }

  List<String> getPlaces() {
    return this.places;
  }

  String[] getProtocols() {
    return (String[]) this.protocols.toArray();
  }

  String getDescription() {
    return this.description;
  }

  String[] getLinks() {
    return (String[]) this.links.toArray();
  }

  /**
   * Returns whether or not the reason for an event is known. If no value was
   * set, it is assumed that the reason is known.
   */
  boolean isUnknown() {
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

  String formatAsTableRow() {
    StringBuilder sb = new StringBuilder();
    sb.append("<tr><td><span class=\"dates\">");
    if (null == this.start) {
      /* Invalid event without start date. */
      sb.append("N/A");
    } else if (this.getOngoing()) {
      /* Ongoing event. */
      sb.append(this.start).append(" to present");
    } else if (null == this.end || this.start.equals(this.end)) {
      /* Single-day event. */
      sb.append(this.start);
    } else {
      /* Multi-day event. */
      sb.append(this.start).append(" to ").append(this.end);
    }
    sb.append("</span></td><td>");
    if (null != this.places) {
      boolean appendUnknownCountry = false;
      for (String place : this.getPlaces()) {
        if (countries.containsKey(place)) {
          sb.append(" <span class=\"label label-warning\">")
              .append(countries.get(place)).append("</span>");
        } else {
          appendUnknownCountry = true;
        }
      }
      if (appendUnknownCountry) {
        sb.append(" <span class=\"label label-warning\">"
            + "Unknown country</span>");
      }
    }
    if (null != this.protocols) {
      for (String protocol : this.protocols) {
        switch (protocol) {
          case "relay":
            sb.append(" <span class=\"label label-success\">Relays</span>");
            break;
          case "bridge":
            sb.append(" <span class=\"label label-primary\">Bridges</span>");
            break;
          case "<OR>":
            sb.append(" <span class=\"label label-info\">&lt;OR&gt;</span>");
            break;
          default:
            sb.append(" <span class=\"label label-info\">").append(protocol)
                .append("</span>");
            break;
        }
      }
    }
    if (this.isUnknown()) {
      sb.append(" <span class=\"label label-default\">Unknown</span>");
    }
    sb.append("</td><td>");
    if (null != this.description) {
      sb.append(this.description).append("<br/>");
    }
    if (null != this.links) {
      for (String link : this.links) {
        int tagEnd = link.indexOf('>');
        if (tagEnd < 0 || tagEnd + 2 > link.length()) {
          continue;
        }
        sb.append(link, 0, tagEnd);
        sb.append(" class=\"link\"");
        if (!link.startsWith("<a href=\"https://metrics.torproject.org/")) {
          sb.append(" target=\"_blank\"");
        }
        sb.append('>')
            .append(link.substring(tagEnd + 1, tagEnd + 2).toUpperCase())
            .append(link.substring(tagEnd + 2));
      }
    }
    sb.append("</td></tr>");
    return sb.toString();
  }
}
