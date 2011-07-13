package org.torproject.ernie.web;

import java.io.*;
import java.sql.*;
import java.text.*;
import java.util.*;
import java.util.logging.*;

import javax.naming.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.sql.*;

public class VotesServlet extends HttpServlet {

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
    } finally {
      /* Nothing to do here. */
    }
  }
}

