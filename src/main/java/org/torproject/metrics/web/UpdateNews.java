package org.torproject.metrics.web;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class UpdateNews {

  private static final Logger logger
      = LoggerFactory.getLogger(UpdateNews.class);

  /** Update news. */
  public static void main(String[] args) throws Exception {
    Locale.setDefault(Locale.US);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    URL textFile = new URL(
        "https://trac.torproject.org/projects/tor/wiki/doc/"
        + "MetricsTimeline?format=txt");
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
            logger.warn("Cannot convert link in line {}. Exiting.", line);
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
            logger.warn("Cannot convert code fragment in line {}. Exiting.",
                line);
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
            .replaceAll("<.*?>", "")
            .replaceAll("&.*;", "");
        if (shortDesc.contains(". ")) {
          shortDesc = shortDesc.substring(0, shortDesc.indexOf(". "));
        }
        if (shortDesc.contains(" (")) {
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
    ObjectMapper objectMapper = new ObjectMapper()
        .setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)
        .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
        .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
        .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
    String newsString = objectMapper.writerWithDefaultPrettyPrinter()
        .writeValueAsString(news);
    try (FileWriter fw = new FileWriter(args[0])) {
      fw.write(newsString);
    }
  }
}
