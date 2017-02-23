/* Copyright 2016--2017 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.web;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ContentProvider {

  private static ContentProvider instance = new ContentProvider();

  public static ContentProvider getInstance() {
    return ContentProvider.instance;
  }

  private List<Metric> metricsList;

  private List<Category> categoriesList;

  private List<News> newsList;

  private ContentProvider() {
    Gson gson = new GsonBuilder().create();
    this.metricsList = Arrays.asList(gson.fromJson(new InputStreamReader(
        this.getClass().getClassLoader().getResourceAsStream("metrics.json")),
        Metric[].class));
    this.categoriesList = Arrays.asList(gson.fromJson(new InputStreamReader(
        this.getClass().getClassLoader().getResourceAsStream(
        "categories.json")), Category[].class));
    this.newsList = Arrays.asList(gson.fromJson(new InputStreamReader(
        this.getClass().getClassLoader().getResourceAsStream(
        "news.json")), News[].class));
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

