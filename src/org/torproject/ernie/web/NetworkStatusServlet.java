package org.torproject.ernie.web;

import java.io.*;
import java.util.*;
import java.sql.*;
import java.util.logging.*;
import java.text.*;

import javax.naming.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.sql.*;

public class NetworkStatusServlet extends HttpServlet {

  private DataSource ds;

  private Logger logger;

  public void init() {

    /* Initialize logger. */
    this.logger = Logger.getLogger(NetworkStatusServlet.class.toString());

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
      HttpServletResponse response) throws IOException, ServletException {

    String sort, order;

    List<Map<String, Object>> status =
        new ArrayList<Map<String, Object>>();

    Set<String> validSort = new HashSet<String>(
        Arrays.asList(("nickname,bandwidth,orport,dirport,isbadexit,"
            + "uptime").split(",")));

    Set<String> validOrder = new HashSet<String>(
        Arrays.asList(("desc,asc").split(",")));

    /* Initialize sort and order parameters from GET */
    try {
      sort = request.getParameter("sort").toLowerCase();
      order = request.getParameter("order").toLowerCase();
    } catch (Exception e) {
      sort = "nickname";
      order = "asc";
    }

    /* Check and set default parameters in case of bad user data. */
    if (!validSort.contains(sort))    { sort = "nickname"; }
    if (!validOrder.contains(order))  { order = "desc"; }

    /* Connect to the database and retrieve data set */
    try {
      Connection conn = this.ds.getConnection();
      Statement statement = conn.createStatement();

      String dbsort = ((sort.equals("uptime") || sort.equals("platform"))
          ? "d." : "s.") + sort;
      String query = "SELECT s.*, "
          + "d.uptime AS uptime, d.platform AS platform "
          + "FROM statusentry s "
          + "JOIN descriptor d "
          + "ON d.descriptor=s.descriptor "
          + "WHERE s.validafter = "
              + "(SELECT MAX(validafter) FROM statusentry) "
          + "ORDER BY " + dbsort + " " + order;

      ResultSet rs = statement.executeQuery(query);

      while (rs.next()) {
        Map<String, Object> row = new HashMap<String, Object>();
        row.put("validafter", rs.getTimestamp(1));
        row.put("nickname", rs.getString(2));
        row.put("fingerprint", rs.getString(3));
        row.put("descriptor", rs.getString(4));
        row.put("published", rs.getTimestamp(5));
        row.put("address", rs.getString(6));
        row.put("orport", rs.getInt(7));
        row.put("dirport", rs.getInt(8));
        row.put("isauthority", rs.getBoolean(9));
        row.put("isbadexit", rs.getBoolean(10));
        row.put("isbaddirectory", rs.getBoolean(11));
        row.put("isexit", rs.getBoolean(12));
        row.put("isfast", rs.getBoolean(13));
        row.put("isguard", rs.getBoolean(14));
        row.put("ishsdir", rs.getBoolean(15));
        row.put("isnamed", rs.getBoolean(16));
        row.put("isstable", rs.getBoolean(17));
        row.put("isrunning", rs.getBoolean(18));
        row.put("isunnamed", rs.getBoolean(19));
        row.put("isvalid", rs.getBoolean(20));
        row.put("isv2dir", rs.getBoolean(21));
        row.put("isv3dir", rs.getBoolean(22));
        row.put("version", rs.getString(23));
        row.put("bandwidth", rs.getBigDecimal(24));
        row.put("ports", rs.getString(25));
        row.put("rawdesc", rs.getBytes(26));
        row.put("uptime", TimeInterval.format(
            rs.getBigDecimal(27).intValue()));
        row.put("platform", rs.getString(28));
        row.put("validafterts", rs.getTimestamp(1).getTime());

        status.add(row);
      }

      conn.close();
      request.setAttribute("status", status);
      request.setAttribute("sort", sort);
      request.setAttribute("order", (order.equals("desc")) ? "asc" : "desc");

    } catch (SQLException e) {
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      this.logger.log(Level.WARNING, "Database error", e);
      return;
    }

    /* Forward the request to the JSP that does all the hard work. */
    request.getRequestDispatcher("WEB-INF/networkstatus.jsp").forward(request,
        response);
  }
}
