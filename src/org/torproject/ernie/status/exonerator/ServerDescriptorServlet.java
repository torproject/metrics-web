/* Copyright 2011, 2012 The Tor Project
 * See LICENSE for licensing information */
package org.torproject.ernie.status.exonerator;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
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

public class ServerDescriptorServlet extends HttpServlet {

  private static final long serialVersionUID = -7935883442750583462L;

  private DataSource ds;

  private Logger logger;

  public void init() {

    /* Initialize logger. */
    this.logger = Logger.getLogger(
        ServerDescriptorServlet.class.toString());

    /* Look up data source. */
    try {
      Context cxt = new InitialContext();
      this.ds = (DataSource) cxt.lookup("java:comp/env/jdbc/exonerator");
      this.logger.info("Successfully looked up data source.");
    } catch (NamingException e) {
      this.logger.log(Level.WARNING, "Could not look up data source", e);
    }
  }

  public void doGet(HttpServletRequest request,
      HttpServletResponse response) throws IOException,
      ServletException {

    /* Read desc-id parameter. */
    String descIdParameter = request.getParameter("desc-id");

    /* See if we were given a desc-id parameter.  If so, look up this
     * descriptor and return it. */
    List<byte[]> rawDescriptors = new ArrayList<byte[]>();
    String filename = null;
    if (descIdParameter != null) {
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
        long requestedConnection = System.currentTimeMillis();
        Connection conn = ds.getConnection();
        Statement statement = conn.createStatement();
        String query = "SELECT descriptor, rawdescriptor FROM descriptor "
            + "WHERE descriptor LIKE '" + descId + "%'";
        ResultSet rs = statement.executeQuery(query);
        if (rs.next()) {
          filename = rs.getString(1);
          rawDescriptors.add(rs.getBytes(2));
        }
        rs.close();
        statement.close();
        conn.close();
        this.logger.info("Returned a database connection to the pool "
            + "after " + (System.currentTimeMillis()
            - requestedConnection) + " millis.");
      } catch (SQLException e) {
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return;
      }

    /* Return an error if no desc-id parameter was given. */
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

