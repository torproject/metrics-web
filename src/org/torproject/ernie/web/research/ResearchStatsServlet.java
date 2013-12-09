/* Copyright 2013 The Tor Project
 * See LICENSE for licensing information */
package org.torproject.ernie.web.research;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
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

  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    this.statsDir = new File(config.getInitParameter("statsDir"));
    this.availableStatisticsFiles = new TreeSet<String>();
    this.availableStatisticsFiles.add("servers");
    this.availableStatisticsFiles.add("bandwidth");
    this.availableStatisticsFiles.add("fast-exits");
    this.availableStatisticsFiles.add("clients");
    this.availableStatisticsFiles.add("torperf");
    this.availableStatisticsFiles.add("connbidirect");
  }

  public long getLastModified(HttpServletRequest request) {
    File statsFile = this.determineStatsFile(request);
    if (statsFile == null || !statsFile.exists()) {
      return -1L;
    } else {
      return statsFile.lastModified();
    }
  }

  public void doGet(HttpServletRequest request,
      HttpServletResponse response) throws IOException, ServletException {
    String requestURI = request.getRequestURI();
    if (requestURI.equals("/ernie/stats/")) {
      this.writeDirectoryListing(request, response);
    } else if (requestURI.equals("/ernie/stats.html")) {
      this.writeStatisticsPage(request, response);
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
    request.setAttribute("directory", "/stats");
    request.setAttribute("extension", ".csv");
    request.setAttribute("files", this.availableStatisticsFiles);
    request.getRequestDispatcher("/WEB-INF/dir.jsp").forward(request,
        response);
  }

  private void writeStatisticsPage(HttpServletRequest request,
      HttpServletResponse response) throws IOException, ServletException {
    request.getRequestDispatcher("/WEB-INF/stats.jsp").forward(request,
        response);
  }

  private File determineStatsFile(HttpServletRequest request) {
    String requestedStatsFile = request.getRequestURI();
    if (requestedStatsFile.equals("/ernie/stats/") ||
        requestedStatsFile.equals("/ernie/stats.html")) {
      return null;
    }
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
    byte[] statsFileBytes;
    try {
      BufferedInputStream bis = new BufferedInputStream(
          new FileInputStream(statsFile), 1024);
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      byte[] buffer = new byte[1024];
      int length;
      while ((length = bis.read(buffer)) > 0) {
        baos.write(buffer, 0, length);
      }
      bis.close();
      statsFileBytes = baos.toByteArray();
    } catch (IOException e) {
      return false;
    }
    String statsFileContent = new String(statsFileBytes);
    response.setContentType("text/csv");
    response.setHeader("Content-Length", String.valueOf(
        statsFileContent.length()));
    response.setHeader("Content-Disposition",
        "inline; filename=\"" + statsFile.getName() + "\"");
    response.getWriter().print(statsFileContent);
    return true;
  }
}

