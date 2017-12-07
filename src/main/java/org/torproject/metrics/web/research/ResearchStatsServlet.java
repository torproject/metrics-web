/* Copyright 2013--2017 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.web.research;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ResearchStatsServlet extends HttpServlet {

  private static final long serialVersionUID = 3346710354297653810L;

  private File statsDir;

  private SortedSet<String> availableStatisticsFiles;

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    this.statsDir = new File(config.getInitParameter("statsDir"));
    this.availableStatisticsFiles = new TreeSet<>();
    this.availableStatisticsFiles.add("servers");
    this.availableStatisticsFiles.add("ipv6servers");
    this.availableStatisticsFiles.add("bandwidth");
    this.availableStatisticsFiles.add("clients");
    this.availableStatisticsFiles.add("userstats-combined");
    this.availableStatisticsFiles.add("torperf");
    this.availableStatisticsFiles.add("torperf-1.1");
    this.availableStatisticsFiles.add("connbidirect2");
    this.availableStatisticsFiles.add("advbwdist");
    this.availableStatisticsFiles.add("hidserv");
    this.availableStatisticsFiles.add("webstats");
  }

  @Override
  public long getLastModified(HttpServletRequest request) {
    File statsFile = this.determineStatsFile(request);
    if (statsFile == null || !statsFile.exists()) {
      return -1L;
    } else {
      return statsFile.lastModified();
    }
  }

  @Override
  public void doGet(HttpServletRequest request,
      HttpServletResponse response) throws IOException, ServletException {
    String requestUri = request.getRequestURI();
    if (requestUri.equals("/metrics/stats/")) {
      this.writeDirectoryListing(request, response);
    } else {
      File statsFile = this.determineStatsFile(request);
      if (statsFile == null) {
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
        return;
      } else if (!this.writeStatsFile(statsFile, response)) {
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      }
    }
  }

  private void writeDirectoryListing(HttpServletRequest request,
      HttpServletResponse response) throws IOException, ServletException {
    response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
    response.setHeader("Location", "/?type=dt&level=ad");
  }

  private File determineStatsFile(HttpServletRequest request) {
    String requestedStatsFile = request.getRequestURI();
    if (requestedStatsFile.endsWith(".csv")) {
      requestedStatsFile = requestedStatsFile.substring(0,
          requestedStatsFile.length() - ".csv".length());
    }
    if (requestedStatsFile.contains("/")) {
      requestedStatsFile = requestedStatsFile.substring(
          requestedStatsFile.lastIndexOf("/") + 1);
    }
    if (!availableStatisticsFiles.contains(requestedStatsFile)) {
      return null;
    } else {
      return new File(this.statsDir, requestedStatsFile + ".csv");
    }
  }

  private boolean writeStatsFile(File statsFile,
      HttpServletResponse response) throws IOException, ServletException {
    if (!statsFile.exists()) {
      return false;
    }
    response.setContentType("text/csv");
    response.setHeader("Content-Length", String.valueOf(
        statsFile.length()));
    response.setHeader("Content-Disposition",
        "inline; filename=\"" + statsFile.getName() + "\"");
    try (BufferedInputStream bis = new BufferedInputStream(
        new FileInputStream(statsFile), 8192);
        BufferedOutputStream bos = new BufferedOutputStream(
        response.getOutputStream())) {
      byte[] buffer = new byte[8192];
      int length;
      while ((length = bis.read(buffer)) > 0) {
        bos.write(buffer, 0, length);
      }
    } catch (IOException e) {
      return false;
    }
    return true;
  }
}

