package org.torproject.ernie.web;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

public class ExtraInfoDescriptorServlet extends HttpServlet {

  private static final long serialVersionUID = -177072581949479416L;

  private DataSource ds;

  private Logger logger;

  public void init() {

    /* Initialize logger. */
    this.logger = Logger.getLogger(
        ExtraInfoDescriptorServlet.class.toString());

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
    if (descIdParameter == null || descIdParameter.length() < 8) {
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
    String extrainfo = null;
    byte[] rawDescriptor = null;
    try {
      long requestedConnection = System.currentTimeMillis();
      Connection conn = this.ds.getConnection();
      Statement statement = conn.createStatement();
      String query = "SELECT extrainfo, rawdesc FROM extrainfo "
          + "WHERE extrainfo LIKE '" + descId + "%'";
      ResultSet rs = statement.executeQuery(query);
      if (rs.next()) {
        extrainfo = rs.getString(1);
        rawDescriptor = rs.getBytes(2);
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
    if (rawDescriptor == null) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
      return;
    }
    try {
      response.setContentType("text/plain");
      response.setHeader("Content-Length", String.valueOf(
          rawDescriptor.length));
      response.setHeader("Content-Disposition", "inline; filename=\""
          + extrainfo + "\"");
      BufferedOutputStream output = new BufferedOutputStream(
          response.getOutputStream());
      output.write(rawDescriptor);
      output.flush();
      output.close();
    } finally {
      /* Nothing to do here. */
    }
  }
}

