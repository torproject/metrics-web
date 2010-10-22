package org.torproject.ernie.web;

import java.io.*;
import java.sql.*;
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

    /* Check desc-id parameter. */
    String descIdParameter = request.getParameter("desc-id");
    if (descIdParameter == null || descIdParameter.length() < 8 ||
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
    String descriptor = null;
    byte[] rawDescriptor = null;
    try {
      Connection conn = ds.getConnection();
      Statement statement = conn.createStatement();
      String query = "SELECT descriptor, rawdesc FROM descriptor "
          + "WHERE descriptor LIKE '" + descId + "%'";
      ResultSet rs = statement.executeQuery(query);
      if (rs.next()) {
        descriptor = rs.getString(1);
        rawDescriptor = rs.getBytes(2);
      }
      rs.close();
      statement.close();
      conn.close();
    } catch (SQLException e) {
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return;
    }

    /* Write response. */
    if (rawDescriptor == null) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
      return;
    }
    try {
      response.setContentType("text/plain");
      response.setHeader("Content-Length", String.valueOf(
          rawDescriptor.length));
      response.setHeader("Content-Disposition", "inline; filename=\""
          + descriptor + "\"");
      BufferedOutputStream output = new BufferedOutputStream(
          response.getOutputStream());
      output.write(rawDescriptor);
    } finally {
      /* Nothing to do here. */
    }
  }
}

