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

import org.apache.commons.lang.time.*;

public class RouterDetailServlet extends HttpServlet {

  private DataSource ds;

  private Logger logger;

  private Pattern fingerprintPattern;

  public void init() {

    /* Initialize logger. */
    this.logger = Logger.getLogger(RouterDetailServlet.class.toString());

    /* Look up data source. */
    try {
      Context cxt = new InitialContext();
      this.ds = (DataSource) cxt.lookup("java:comp/env/jdbc/tordir");
      this.logger.info("Successfully looked up data source.");
    } catch (NamingException e) {
      this.logger.log(Level.WARNING, "Could not look up data source", e);
    }

    /* Initialize fingerprint pattern. */
    this.fingerprintPattern = Pattern.compile("^[0-9a-f]{40}$");
  }

  public void doGet(HttpServletRequest request,
      HttpServletResponse response) throws IOException, ServletException {

    /* Check that we were given a valid fingerprint. */
    String fingerprintParameter = request.getParameter("fingerprint");
    if (fingerprintParameter != null) {
      fingerprintParameter = fingerprintParameter.toLowerCase();
    }
    if (fingerprintParameter == null ||
        fingerprintParameter.length() != 40 ||
        !fingerprintPattern.matcher(fingerprintParameter).matches()) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    String query = "SELECT statusentry.validafter, statusentry.nickname, "
        + "statusentry.fingerprint, statusentry.descriptor, "
        + "statusentry.published, statusentry.address, "
        + "statusentry.orport, statusentry.dirport, "
        + "statusentry.isauthority, statusentry.isbadexit, "
        + "statusentry.isbaddirectory, statusentry.isexit, "
        + "statusentry.isfast, statusentry.isguard, statusentry.ishsdir, "
        + "statusentry.isnamed, statusentry.isstable, "
        + "statusentry.isrunning, statusentry.isunnamed, "
        + "statusentry.isvalid, statusentry.isv2dir, "
        + "statusentry.isv3dir, statusentry.version, "
        + "statusentry.bandwidth, statusentry.ports, "
        + "descriptor.uptime, descriptor.platform, descriptor.rawdesc "
        + "FROM statusentry JOIN descriptor "
        + "ON descriptor.descriptor = statusentry.descriptor "
        + "WHERE statusentry.validafter = "
        + "(SELECT MAX(validafter) FROM statusentry) "
        + "AND statusentry.fingerprint = ?";

    try {
      Connection conn = this.ds.getConnection();
      PreparedStatement ps = conn.prepareStatement(query);
      ps.setString(1, fingerprintParameter);
      ResultSet rs = ps.executeQuery();
      if (rs.next()) {
        request.setAttribute("validafter", rs.getTimestamp(1));
        request.setAttribute("nickname", rs.getString(2));
        request.setAttribute("fingerprint", rs.getString(3));
        request.setAttribute("descriptor", rs.getString(4));
        request.setAttribute("published", rs.getTimestamp(5));
        request.setAttribute("address", rs.getString(6));
        request.setAttribute("orport", rs.getInt(7));
        request.setAttribute("dirport", rs.getInt(8));
        request.setAttribute("isauthority", rs.getBoolean(9));
        request.setAttribute("isbadexit", rs.getBoolean(10));
        request.setAttribute("isbaddirectory", rs.getBoolean(11));
        request.setAttribute("isexit", rs.getBoolean(12));
        request.setAttribute("isfast", rs.getBoolean(13));
        request.setAttribute("isguard", rs.getBoolean(14));
        request.setAttribute("ishsdir", rs.getBoolean(15));
        request.setAttribute("isnamed", rs.getBoolean(16));
        request.setAttribute("isstable", rs.getBoolean(17));
        request.setAttribute("isrunning", rs.getBoolean(18));
        request.setAttribute("isunnamed", rs.getBoolean(19));
        request.setAttribute("isvalid", rs.getBoolean(20));
        request.setAttribute("isv2dir", rs.getBoolean(21));
        request.setAttribute("isv3dir", rs.getBoolean(22));
        request.setAttribute("version", rs.getString(23));
        request.setAttribute("bandwidth", rs.getBigDecimal(24));
        request.setAttribute("ports", rs.getString(25));
        request.setAttribute("uptime", DurationFormatUtils.formatDuration(
            rs.getBigDecimal(26).longValue() * 1000L, "d'd' HH:mm:ss"));
        request.setAttribute("platform", rs.getString(27));
      } else {
        /* There were zero results in the set */
        /* TODO Handle this case in a more user-friendly way. */
        response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        this.logger.log(Level.WARNING, "Query with fingerprint = '"
            + fingerprintParameter + "' returned zero results.  "
            + "Returned BAD_REQUEST.");
        return;
      }
      conn.close();

    } catch (SQLException e)  {
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      this.logger.log(Level.WARNING, "Database error", e);
      return;
    }

    /* Forward the request to the JSP. */
    request.getRequestDispatcher("WEB-INF/routerdetail.jsp").forward(
        request, response);
  }
}

