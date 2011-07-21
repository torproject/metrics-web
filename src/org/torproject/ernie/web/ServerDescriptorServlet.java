package org.torproject.ernie.web;

import java.io.*;
import java.sql.*;
import java.text.*;
import java.util.*;
import java.util.logging.*;
import java.util.regex.*;

import javax.naming.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.sql.*;

public class ServerDescriptorServlet extends HttpServlet {

  private DataSource ds;

  private Logger logger;

  public void init() {

    /* Initialize logger. */
    this.logger = Logger.getLogger(
        ServerDescriptorServlet.class.toString());

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

    /* Read desc-id and/or valid-after parameters. */
    String validAfterParameter = request.getParameter("valid-after");
    String descIdParameter = request.getParameter("desc-id");

    /* See if we were given a desc-id parameter.  If so, look up this
     * descriptor and return it. */
    List<byte[]> rawDescriptors = new ArrayList<byte[]>();
    String filename = null;
    if (descIdParameter != null && validAfterParameter == null) {
      if (descIdParameter.length() < 8 ||
          descIdParameter.length() > 40) {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        return;
      }
      String descId = descIdParameter.toLowerCase();
      Pattern descIdPattern = Pattern.compile("^[0-9a-f]+$");
      Matcher descIdMatcher = descIdPattern.matcher(descId);
      if (!descIdMatcher.matches()) {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        return;
      }

      /* Look up descriptor in the database. */
      try {
        Connection conn = ds.getConnection();
        Statement statement = conn.createStatement();
        String query = "SELECT descriptor, rawdesc FROM descriptor "
            + "WHERE descriptor LIKE '" + descId + "%'";
        ResultSet rs = statement.executeQuery(query);
        if (rs.next()) {
          filename = rs.getString(1);
          rawDescriptors.add(rs.getBytes(2));
        }
        rs.close();
        statement.close();
        conn.close();
      } catch (SQLException e) {
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return;
      }

    /* See if we were given a valid-after parameter.  If so, return all
     * descriptors referenced from the consensus published at that
     * time. */
    } else if (descIdParameter == null && validAfterParameter != null) {
      if (validAfterParameter.length() !=
          "yyyy-MM-dd-HH-mm-ss".length()) {
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
      filename = validAfterParameter + "-descriptors";

      /* Look up descriptors in the database. */
      SimpleDateFormat databaseFormat = new SimpleDateFormat(
          "yyyy-MM-dd HH:mm:ss");
      databaseFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
      String databaseParameter = databaseFormat.format(parsedTimestamp);
      try {
        Connection conn = this.ds.getConnection();
        Statement statement = conn.createStatement();
        String query = "SELECT descriptor.rawdesc FROM statusentry "
            + "JOIN descriptor ON statusentry.descriptor = "
            + "descriptor.descriptor WHERE validafter = '"
            + databaseParameter + "'";
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

    /* Return an error if neither desc-id nor valid-after parameter was
     * given (or both of them). */
    } else {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
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
          + filename + "\"");
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

