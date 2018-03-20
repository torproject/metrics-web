package org.torproject.metrics.web;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class UpdateNews {
  /** Update news. */
  public static void main(String[] args) throws Exception {
    URL textFile = new URL(
        "https://trac.torproject.org/projects/tor/wiki/doc/"
        + "MetricsTimeline?format=txt");
    Gson gson = new GsonBuilder()
        .disableHtmlEscaping()
        .setPrettyPrinting()
        .create();
    List<News> news = new ArrayList<>();
    try (BufferedReader br = new BufferedReader(new InputStreamReader(
        textFile.openStream()))) {
      String line;
      Boolean unknown = null;
      while ((line = br.readLine()) != null) {
        if (line.startsWith("== Unknown")) {
          unknown = true;
          continue;
        }
        if (!line.startsWith("||") || line.startsWith("||=start")) {
          continue;
        }
        line = line.trim();
        String[] parts = line.split("\\|\\|");
        News entry = new News();
        entry.start = parts[1].replaceAll("~", "").trim();
        if (entry.start.contains(" ")) {
          entry.start = entry.start.substring(0, entry.start.indexOf(" "));
        } else if (entry.start.isEmpty()) {
          entry.start = null;
        }
        entry.end = parts[2].replaceAll("~", "").trim();
        if ("ongoing".equalsIgnoreCase(entry.end)) {
          entry.end = null;
          entry.ongoing = true;
        } else if (entry.end.contains(" ")) {
          entry.end = entry.end.substring(0, entry.end.indexOf(" "));
        } else if (entry.end.isEmpty()) {
          entry.end = null;
        }
        for (String place : parts[3].trim().split(" +")) {
          if (!place.isEmpty()) {
            if (null == entry.places) {
              entry.places = new ArrayList<>();
            }
            entry.places.add(place);
          }
        }
        for (String protocol : parts[4].trim().split(" +")) {
          if (!protocol.isEmpty()) {
            if (null == entry.protocols) {
              entry.protocols = new ArrayList<>();
            }
            entry.protocols.add(protocol);
          }
        }
        String desc = parts[5].trim();
        while (desc.contains("[")) {
          int open = desc.indexOf("[");
          int space = desc.indexOf(" ", open);
          int close = desc.indexOf("]", open);
          if (open < 0 || space < 0 || close < 0) {
            System.err.println("Cannot convert link.");
            System.exit(1);
          }
          desc = desc.substring(0, open) + "<a href=\""
              + desc.substring(open + 1, space) + "\">"
              + desc.substring(space + 1, close) + "</a>"
              + desc.substring(close + 1);
        }
        while (desc.contains("`")) {
          int open = desc.indexOf("`");
          int close = desc.indexOf("`", open + 1);
          if (open < 0 || close < 0) {
            System.err.println("Cannot convert code fragment.");
            System.exit(1);
          }
          desc = desc.substring(0, open) + "<code>"
              + desc.substring(open + 1, close) + "</code>"
              + desc.substring(close + 1);
        }
        entry.description = desc
            .replaceAll("&", "&amp;")
            .replaceAll("×", "&times;")
            .replaceAll("§", "&sect;")
            .replaceAll("–", "&ndash;")
            .replaceAll("“", "&ldquo;")
            .replaceAll("”", "&rdquo;");
        String shortDesc = desc
            .replaceAll("\\<.*?\\>", "")
            .replaceAll("&.*;", "");
        if (shortDesc.indexOf(". ") != -1) {
          shortDesc = shortDesc.substring(0, shortDesc.indexOf(". "));
        }
        if (shortDesc.indexOf(" (") != -1) {
          shortDesc = shortDesc.substring(0, shortDesc.indexOf(" ("));
        }
        entry.shortDescription = shortDesc;
        if (parts.length >= 7) {
          for (String link : parts[6].split("[\\[\\]]")) {
            link = link.trim();
            if (link.isEmpty()) {
              continue;
            }
            entry.addLink(link.substring(link.indexOf(" ") + 1),
                link.substring(0, link.indexOf(" ")));
          }
        }
        entry.unknown = unknown;
        news.add(entry);
      }
    }
    try (FileWriter fw = new FileWriter(args[0])) {
      fw.write(gson.toJson(news));
    }
  }
}
