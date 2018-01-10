/* Copyright 2016--2018 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.web;

import org.torproject.metrics.web.graphs.Countries;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class News {

  private String start;

  private String end;

  private boolean ongoing;

  private List<String> places;

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

  boolean getOngoing() {
    return this.ongoing;
  }

  List<String> getPlaces() {
    return this.places;
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
    } else if (this.ongoing) {
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
    if (this.unknown) {
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

