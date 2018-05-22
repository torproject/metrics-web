/* Copyright 2016--2018 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.web;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ContentProvider {

  private static ObjectMapper objectMapper;

  private static ContentProvider instance;

  static {
    objectMapper = new ObjectMapper()
        .setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)
        .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
        .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
        .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
    instance = new ContentProvider();
  }

  public static ContentProvider getInstance() {
    return ContentProvider.instance;
  }

  private List<Metric> metricsList;

  private List<Category> categoriesList;

  private List<News> newsList;

  private ContentProvider() {
    try {
      this.metricsList = Arrays.asList(objectMapper.readValue(
          new InputStreamReader(this.getClass().getClassLoader()
              .getResourceAsStream("WEB-INF/json/metrics.json")),
          Metric[].class));
      this.categoriesList = Arrays.asList(objectMapper.readValue(
          new InputStreamReader(this.getClass().getClassLoader()
              .getResourceAsStream("WEB-INF/json/categories.json")),
          Category[].class));
      this.newsList = Arrays.asList(objectMapper.readValue(
          new InputStreamReader(this.getClass().getClassLoader()
              .getResourceAsStream("WEB-INF/json/news.json")),
          News[].class));
    } catch (IOException e) {
      /* Abort the web server start rather than continuing with broken provided
       * JSON content files. */
      throw new RuntimeException(e);
    }
  }

  public List<Metric> getMetricsList() {
    return new ArrayList<>(this.metricsList);
  }

  public List<Category> getCategoriesList() {
    return new ArrayList<>(this.categoriesList);
  }

  public List<News> getNewsList() {
    return new ArrayList<>(this.newsList);
  }
}

