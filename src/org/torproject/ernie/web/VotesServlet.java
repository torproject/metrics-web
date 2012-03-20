/* Copyright 2011, 2012 The Tor Project
 * See LICENSE for licensing information */
package org.torproject.ernie.web;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

public class VotesServlet extends HttpServlet {

  private static final long serialVersionUID = 246279501605993024L;

  private DataSource ds;

  private Logger logger;

  public void init() {

    /* Initialize logger. */
    this.logger = Logger.getLogger(VotesServlet.class.toString());

    /* Look up data source. */
    try {
      Context cxt = new InitialContext();
      this.ds = (DataSource) cxt.lookup("java:comp/env/jdbc/tordir");
      this.logger.info("Successfully looked up data source.");
    } catch (NamingException e) {
      this.logger.log(Level.WARNING, "Could not look up data source", e);
    }
  }

  public void doGet(HttpServletRequest request,
      HttpServletResponse response) throws IOException,
      ServletException {

    /* Check valid-after parameter. */
    String validAfterParameter = request.getParameter("valid-after");
    if (validAfterParameter == null ||
        validAfterParameter.length() != "yyyy-MM-dd-HH-mm-ss".length()) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }
    SimpleDateFormat parameterFormat = new SimpleDateFormat(
        "yyyy-MM-dd-HH-mm-ss");
    parameterFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    long parsedTimestamp = -1L;
    try {
      parsedTimestamp = parameterFormat.parse(validAfterParameter).
          getTime();
    } catch (ParseException e) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }
    if (parsedTimestamp < 0L) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    /* Look up consensus in the database. */
    SimpleDateFormat databaseFormat = new SimpleDateFormat(
        "yyyy-MM-dd HH:mm:ss");
    databaseFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    String databaseParameter = databaseFormat.format(parsedTimestamp);
    List<byte[]> rawDescriptors = new ArrayList<byte[]>();
    try {
      long requestedConnection = System.currentTimeMillis();
      Connection conn = this.ds.getConnection();
      Statement statement = conn.createStatement();
      String query = "SELECT rawdesc FROM vote "
          + "WHERE validafter = '" + databaseParameter + "'";
      ResultSet rs = statement.executeQuery(query);
      while (rs.next()) {
        rawDescriptors.add(rs.getBytes(1));
      }
      rs.close();
      statement.close();
      conn.close();
      this.logger.info("Returned a database connection to the pool after "
          + (System.currentTimeMillis() - requestedConnection)
          + " millis.");
    } catch (SQLException e) {
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return;
    }

    /* Write response. */
    if (rawDescriptors.size() == 0) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
      return;
    }
    try {
      response.setContentType("text/plain");
      int responseLength = 0;
      for (byte[] rawDescriptor : rawDescriptors) {
        responseLength += rawDescriptor.length;
      }
      response.setHeader("Content-Length", String.valueOf(
          responseLength));
      response.setHeader("Content-Disposition", "inline; filename=\""
          + validAfterParameter + "-votes\"");
      BufferedOutputStream output = new BufferedOutputStream(
          response.getOutputStream());
      for (byte[] rawDescriptor : rawDescriptors) {
        output.write(rawDescriptor);
      }
      output.flush();
      output.close();
    } finally {
      /* Nothing to do here. */
    }
  }
}

