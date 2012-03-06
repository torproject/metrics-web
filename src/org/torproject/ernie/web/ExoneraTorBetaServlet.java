package org.torproject.ernie.web;

import java.io.*;
import java.math.*;
import java.sql.*;
import java.text.*;
import java.util.*;
import java.util.logging.*;
import java.util.regex.*;

import javax.naming.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.sql.*;

import org.apache.commons.codec.binary.*;
import org.apache.commons.lang.*;

public class ExoneraTorBetaServlet extends HttpServlet {

  private DataSource ds;

  private Logger logger;

  public void init() {

    /* Initialize logger. */
    this.logger = Logger.getLogger(
        ExoneraTorBetaServlet.class.toString());

    /* Look up data source. */
    try {
      Context cxt = new InitialContext();
      this.ds = (DataSource) cxt.lookup("java:comp/env/jdbc/exonerator");
      this.logger.info("Successfully looked up data source.");
    } catch (NamingException e) {
      this.logger.log(Level.WARNING, "Could not look up data source", e);
    }
  }

  private void writeHeader(PrintWriter out) throws IOException {
    out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 "
          + "Transitional//EN\">\n"
        + "<html>\n"
        + "  <head>\n"
        + "    <title>Tor Metrics Portal: ExoneraTor</title>\n"
        + "    <meta http-equiv=\"content-type\" content=\"text/html; "
          + "charset=ISO-8859-1\">\n"
        + "    <link href=\"/css/stylesheet-ltr.css\" type=\"text/css\" "
          + "rel=\"stylesheet\">\n"
        + "    <link href=\"/images/favicon.ico\" "
          + "type=\"image/x-icon\" rel=\"shortcut icon\">\n"
        + "  </head>\n"
        + "  <body>\n"
        + "    <div class=\"center\">\n"
        + "      <table class=\"banner\" border=\"0\" cellpadding=\"0\" "
          + "cellspacing=\"0\" summary=\"\">\n"
        + "        <tr>\n"
        + "          <td class=\"banner-left\"><a "
          + "href=\"/index.html\"><img src=\"/images/top-left.png\" "
          + "alt=\"Click to go to home page\" width=\"193\" "
          + "height=\"79\"></a></td>\n"
        + "          <td class=\"banner-middle\">\n"
        + "            <a href=\"/\">Home</a>\n"
        + "            <a href=\"graphs.html\">Graphs</a>\n"
        + "            <a href=\"research.html\">Research</a>\n"
        + "            <a href=\"status.html\">Status</a>\n"
        + "            <br>\n"
        + "            <font size=\"2\">\n"
        + "              <a class=\"current\">ExoneraTor</a>\n"
        + "              <a href=\"relay-search.html\">Relay Search</a>\n"
        + "              <a href=\"consensus-health.html\">Consensus "
          + "Health</a>\n"
        + "            </font>\n"
        + "          </td>\n"
        + "          <td class=\"banner-right\"></td>\n"
        + "        </tr>\n"
        + "      </table>\n"
        + "      <div class=\"main-column\" style=\"margin:5; "
          + "Padding:0;\">\n"
        + "        <h2>ExoneraTor</h2>\n"
        + "        <h3>or: a website that tells you whether some IP "
          + "address was a Tor relay</h3>\n"
        + "        <p>ExoneraTor tells you whether there was a Tor relay "
          + "running on a given IP address at a given time. ExoneraTor "
          + "can further find out whether this relay permitted exiting "
          + "to a given server and/or TCP port. ExoneraTor learns about "
          + "these facts from parsing the public relay lists and relay "
          + "descriptors that are collected from the Tor directory "
          + "authorities and the exit lists collected by TorDNSEL.</p>\n"
        + "        <br>\n"
        + "        <p>This is a <b>BETA</b> version of ExoneraTor.  "
          + "Beware of bugs.  The stable version of ExoneraTor is still "
          + "available <a href=\"exonerator.html\">here</a>.  The "
          + "visible changes in this BETA version are:</p>\n"
        + "        <ul>\n"
        + "        <li>Results now include IP addresses from exit "
          + "lists, too.</li>\n"
        + "        <li>It's not required anymore to specify an exact "
          + "timestamp, but now a date is enough.</li>\n"
        + "        </ul><br>\n"
        + "        <p><font color=\"red\"><b>Notice:</b> Note that the "
          + "information you are providing below may be leaked to anyone "
          + "who can read the network traffic between you and this web "
          + "server or who has access to this web server. If you need to "
          + "keep the IP addresses and incident times confidential, you "
          + "should download the <a href=\"tools.html#exonerator\">Java "
          + "or Python version of ExoneraTor</a> and run it on your "
          + "local machine.</font></p>\n"
        + "        <br>\n");
  }

  private void writeFooter(PrintWriter out) throws IOException {
    out.println("        <br>\n"
        + "      </div>\n"
        + "    </div>\n"
        + "    <div class=\"bottom\" id=\"bottom\">\n"
        + "      <p>This material is supported in part by the National "
          + "Science Foundation under Grant No. CNS-0959138. Any "
          + "opinions, finding, and conclusions or recommendations "
          + "expressed in this material are those of the author(s) and "
          + "do not necessarily reflect the views of the National "
          + "Science Foundation.</p>\n"
        + "      <p>\"Tor\" and the \"Onion Logo\" are <a "
          + "href=\"https://www.torproject.org/docs/trademark-faq.html.en"
          + "\">registered trademarks</a> of The Tor Project, Inc.</p>\n"
        + "      <p>Data on this site is freely available under a <a "
          + "href=\"http://creativecommons.org/publicdomain/zero/1.0/\">"
          + "CC0 no copyright declaration</a>: To the extent possible "
          + "under law, the Tor Project has waived all copyright and "
          + "related or neighboring rights in the data. Graphs are "
          + "licensed under a <a "
          + "href=\"http://creativecommons.org/licenses/by/3.0/us/\">"
          + "Creative Commons Attribution 3.0 United States "
          + "License</a>.</p>\n"
        + "    </div>\n"
        + "  </body>\n"
        + "</html>");
    out.close();
  }

  public void doGet(HttpServletRequest request,
      HttpServletResponse response) throws IOException,
      ServletException {

    /* Start writing response. */
    PrintWriter out = response.getWriter();
    writeHeader(out);

    /* Open a database connection that we'll use to handle the whole
     * request. */
    Connection conn = null;
    long requestedConnection = System.currentTimeMillis();
    try {
      conn = this.ds.getConnection();
    } catch (SQLException e) {
      out.println("<p><font color=\"red\"><b>Warning: </b></font>Unable "
          + "to connect to the database. If this problem persists, "
          + "please <a href=\"mailto:tor-assistants@torproject.org\">let "
          + "us know</a>!</p>\n");
      writeFooter(out);
      return;
    }

    /* Look up first and last consensus in the database. */
    long firstValidAfter = -1L, lastValidAfter = -1L;
    try {
      Statement statement = conn.createStatement();
      String query = "SELECT MIN(validafter) AS first, "
          + "MAX(validafter) AS last FROM consensus";
      ResultSet rs = statement.executeQuery(query);
      if (rs.next()) {
        firstValidAfter = rs.getTimestamp(1).getTime();
        lastValidAfter = rs.getTimestamp(2).getTime();
      }
      rs.close();
      statement.close();
    } catch (SQLException e) {
      /* Looks like we don't have any consensuses. */
    }
    if (firstValidAfter < 0L || lastValidAfter < 0L) {
      out.println("<p><font color=\"red\"><b>Warning: </b></font>This "
          + "server doesn't have any relay lists available. If this "
          + "problem persists, please "
          + "<a href=\"mailto:tor-assistants@torproject.org\">let us "
          + "know</a>!</p>\n");
      writeFooter(out);
      try {
        conn.close();
        this.logger.info("Returned a database connection to the pool "
            + "after " + (System.currentTimeMillis()
            - requestedConnection) + " millis.");
      } catch (SQLException e) {
      }
      return;
    }

    out.println("<a name=\"relay\"></a><h3>Was there a Tor relay running "
        + "on this IP address?</h3>");

    /* Parse IP parameter. */
    /* TODO Extend the parsing code to accept IPv6 addresses, too. */
    Pattern ipAddressPattern = Pattern.compile(
        "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
        "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
        "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
        "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
    String ipParameter = request.getParameter("ip");
    String relayIP = "", ipWarning = "";
    if (ipParameter != null && ipParameter.length() > 0) {
      Matcher ipParameterMatcher = ipAddressPattern.matcher(ipParameter);
      if (ipParameterMatcher.matches()) {
        String[] ipParts = ipParameter.split("\\.");
        relayIP = Integer.parseInt(ipParts[0]) + "."
            + Integer.parseInt(ipParts[1]) + "."
            + Integer.parseInt(ipParts[2]) + "."
            + Integer.parseInt(ipParts[3]);
      } else {
        ipWarning = "\"" + (ipParameter.length() > 20 ?
            StringEscapeUtils.escapeHtml(ipParameter.substring(0, 20))
            + "[...]" : StringEscapeUtils.escapeHtml(ipParameter))
            + "\" is not a valid IP address.";
      }
    }

    /* Parse timestamp parameter. */
    String timestampParameter = request.getParameter("timestamp");
    long timestamp = 0L;
    boolean timestampIsDate = false;
    String timestampStr = "", timestampWarning = "";
    SimpleDateFormat shortDateTimeFormat = new SimpleDateFormat(
        "yyyy-MM-dd HH:mm");
    shortDateTimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    if (timestampParameter != null && timestampParameter.length() > 0) {
      try {
        if (timestampParameter.split(" ").length == 1) {
          timestamp = dateFormat.parse(timestampParameter).getTime();
          timestampStr = dateFormat.format(timestamp);
          timestampIsDate = true;
        } else {
          timestamp = shortDateTimeFormat.parse(timestampParameter).
              getTime();
          timestampStr = shortDateTimeFormat.format(timestamp);
        }
        if (timestamp < firstValidAfter || timestamp > lastValidAfter) {
          timestampWarning = "Please pick a date or timestamp between \""
              + shortDateTimeFormat.format(firstValidAfter) + "\" and \""
              + shortDateTimeFormat.format(lastValidAfter) + "\".";
          timestamp = 0L;
        }
      } catch (ParseException e) {
        /* We have no way to handle this exception, other than leaving
           timestampStr at "". */
        timestampWarning = "\"" + (timestampParameter.length() > 20 ?
            StringEscapeUtils.escapeHtml(timestampParameter.
            substring(0, 20)) + "[...]" :
            StringEscapeUtils.escapeHtml(timestampParameter))
            + "\" is not a valid date or timestamp.";
      }
    }

    /* If either IP address or timestamp is provided, the other one must
     * be provided, too. */
    if (relayIP.length() < 1 && timestampStr.length() > 0 &&
        ipWarning.length() < 1) {
      ipWarning = "Please provide an IP address.";
    }
    if (relayIP.length() > 0 && timestamp < 1 &&
        timestampWarning.length() < 1) {
      timestampWarning = "Please provide a date or timestamp.";
    }

    /* Parse target IP parameter. */
    String targetIP = "", targetPort = "", target = "";
    String[] targetIPParts = null;
    String targetAddrParameter = request.getParameter("targetaddr");
    String targetAddrWarning = "";
    if (targetAddrParameter != null && targetAddrParameter.length() > 0) {
      Matcher targetAddrParameterMatcher =
          ipAddressPattern.matcher(targetAddrParameter);
      if (targetAddrParameterMatcher.matches()) {
        String[] targetAddrParts = targetAddrParameter.split("\\.");
        targetIP = Integer.parseInt(targetAddrParts[0]) + "."
            + Integer.parseInt(targetAddrParts[1]) + "."
            + Integer.parseInt(targetAddrParts[2]) + "."
            + Integer.parseInt(targetAddrParts[3]);
        target = targetIP;
        targetIPParts = targetIP.split("\\.");
      } else {
        targetAddrWarning = "\"" + (targetAddrParameter.length() > 20 ?
            StringEscapeUtils.escapeHtml(targetAddrParameter.substring(
            0, 20)) + "[...]" : StringEscapeUtils.escapeHtml(
            targetAddrParameter)) + "\" is not a valid IP address.";
      }
    }

    /* Parse target port parameter. */
    String targetPortParameter = request.getParameter("targetport");
    String targetPortWarning = "";
    if (targetPortParameter != null && targetPortParameter.length() > 0) {
      Pattern targetPortPattern = Pattern.compile("\\d+");
      if (targetPortParameter.length() < 5 &&
          targetPortPattern.matcher(targetPortParameter).matches() &&
          !targetPortParameter.equals("0") &&
          Integer.parseInt(targetPortParameter) < 65536) {
        targetPort = targetPortParameter;
        if (target != null) {
          target += ":" + targetPort;
        } else {
          target = targetPort;
        }
      } else {
        targetPortWarning = "\"" + (targetPortParameter.length() > 8 ?
            StringEscapeUtils.escapeHtml(targetPortParameter.
            substring(0, 8)) + "[...]" :
            StringEscapeUtils.escapeHtml(targetPortParameter))
            + "\" is not a valid TCP port.";
      }
    }

    /* If target port is provided, a target address must be provided,
     * too. */
    /* TODO Relax this requirement. */
    if (targetPort.length() > 0 && targetIP.length() < 1 &&
        targetAddrWarning.length() < 1) {
      targetAddrWarning = "Please provide an IP address.";
    }

    /* Write form with IP address and timestamp. */
    out.println("        <form action=\"#relay\">\n"
        + "          <input type=\"hidden\" name=\"targetaddr\" "
        + (targetIP.length() > 0 ? " value=\"" + targetIP + "\"" : "")
        + ">\n"
        + "          <input type=\"hidden\" name=\"targetPort\""
        + (targetPort.length() > 0 ? " value=\"" + targetPort + "\"" : "")
        + ">\n"
        + "          <table>\n"
        + "            <tr>\n"
        + "              <td align=\"right\">IP address in question:"
          + "</td>\n"
        + "              <td><input type=\"text\" name=\"ip\""
          + (relayIP.length() > 0 ? " value=\"" + relayIP + "\""
            : "")
          + ">"
          + (ipWarning.length() > 0 ? "<br><font color=\"red\">"
          + ipWarning + "</font>" : "")
        + "</td>\n"
        + "              <td><i>(Ex.: 1.2.3.4)</i></td>\n"
        + "            </tr>\n"
        + "            <tr>\n"
        + "              <td align=\"right\">Date or timestamp, in "
          + "UTC:</td>\n"
        + "              <td><input type=\"text\" name=\"timestamp\""
          + (timestampStr.length() > 0 ? " value=\"" + timestampStr + "\""
            : "")
          + ">"
          + (timestampWarning.length() > 0 ? "<br><font color=\"red\">"
              + timestampWarning + "</font>" : "")
        + "</td>\n"
        + "              <td><i>(Ex.: 2010-01-01 or 2010-01-01 12:00)"
          + "</i></td>\n"
        + "            </tr>\n"
        + "            <tr>\n"
        + "              <td></td>\n"
        + "              <td>\n"
        + "                <input type=\"submit\">\n"
        + "                <input type=\"reset\">\n"
        + "              </td>\n"
        + "              <td></td>\n"
        + "            </tr>\n"
        + "          </table>\n"
        + "        </form>\n");

    if (relayIP.length() < 1 || timestamp < 1) {
      writeFooter(out);
      try {
        conn.close();
        this.logger.info("Returned a database connection to the pool "
            + "after " + (System.currentTimeMillis()
            - requestedConnection) + " millis.");
      } catch (SQLException e) {
      }
      return;
    }

    out.printf("<p>Looking up IP address %s in the relay lists "
        + "published ", relayIP);
    long timestampFrom, timestampTo;
    if (timestampIsDate) {
      /* If we only have a date, consider all consensuses published on the
       * given date, plus the ones published 3 hours before the given date
       * and until 23:59:59. */
      timestampFrom = timestamp - 3L * 60L * 60L * 1000L;
      timestampTo = timestamp + (24L * 60L * 60L - 1L) * 1000L;
      out.printf("on %s", timestampStr);
    } else {
      /* If we have an exact timestamp, consider the consensuses published
       * in the 3 hours preceding the UTC timestamp. */
      timestampFrom = timestamp - 3L * 60L * 60L * 1000L;
      timestampTo = timestamp;
      out.printf("between %s and %s UTC",
        shortDateTimeFormat.format(timestampFrom),
        shortDateTimeFormat.format(timestampTo));
    }
    /* If we don't find any relays in the given time interval, also look
     * at consensuses published 12 hours before and 12 hours after the
     * interval, in case the user got the "UTC" bit wrong. */
    long timestampTooOld = timestampFrom - 12L * 60L * 60L * 1000L;
    long timestampTooNew = timestampTo + 12L * 60L * 60L * 1000L;
    out.print(" as well as in the relevant exit lists. Clients could "
        + "have selected any of these relays to build circuits. "
        + "You may follow the links to relay lists and relay descriptors "
        + "to grep for the lines printed below and confirm that results "
        + "are correct.<br>");
    SimpleDateFormat validAfterTimeFormat = new SimpleDateFormat(
        "yyyy-MM-dd HH:mm:ss");
    validAfterTimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    String fromValidAfter = validAfterTimeFormat.format(timestampTooOld);
    String toValidAfter = validAfterTimeFormat.format(timestampTooNew);
    SortedSet<Long> tooOldConsensuses = new TreeSet<Long>();
    SortedSet<Long> relevantConsensuses = new TreeSet<Long>();
    SortedSet<Long> tooNewConsensuses = new TreeSet<Long>();
    try {
      Statement statement = conn.createStatement();
      String query = "SELECT validafter FROM consensus "
          + "WHERE validafter >= '" + fromValidAfter
          + "' AND validafter <= '" + toValidAfter + "'";
      ResultSet rs = statement.executeQuery(query);
      while (rs.next()) {
        long consensusTime = rs.getTimestamp(1).getTime();
        if (consensusTime < timestampFrom) {
          tooOldConsensuses.add(consensusTime);
        } else if (consensusTime > timestampTo) {
          tooNewConsensuses.add(consensusTime);
        } else {
          relevantConsensuses.add(consensusTime);
        }
      }
      rs.close();
      statement.close();
    } catch (SQLException e) {
      /* Looks like we don't have any consensuses in the requested
       * interval. */
    }
    SortedSet<Long> allConsensuses = new TreeSet<Long>();
    allConsensuses.addAll(tooOldConsensuses);
    allConsensuses.addAll(relevantConsensuses);
    allConsensuses.addAll(tooNewConsensuses);
    if (allConsensuses.isEmpty()) {
      out.println("        <p>No relay lists found!</p>\n"
          + "        <p>Result is INDECISIVE!</p>\n"
          + "        <p>We cannot make any statement whether there was "
          + "a Tor relay running on IP address " + relayIP
          + (timestampIsDate ? " on " : " at ") + timestampStr + "! We "
          + "did not find any relevant relay lists at the given time. If "
          + "you think this is an error on our side, please "
          + "<a href=\"mailto:tor-assistants@torproject.org\">contact "
          + "us</a>!</p>\n");
      writeFooter(out);
      try {
        conn.close();
        this.logger.info("Returned a database connection to the pool "
            + "after " + (System.currentTimeMillis()
            - requestedConnection) + " millis.");
      } catch (SQLException e) {
      }
      return;
    }

    /* Search for status entries with the given IP address as onion
     * routing address, plus status entries of relays having an exit list
     * entry with the given IP address as exit address. */
    SortedMap<Long, SortedMap<String, String>> statusEntries =
        new TreeMap<Long, SortedMap<String, String>>();
    SortedSet<Long> positiveConsensusesNoTarget = new TreeSet<Long>();
    SortedMap<String, Set<Long>> relevantDescriptors =
        new TreeMap<String, Set<Long>>();
    try {
      CallableStatement cs = conn.prepareCall(
          "{call search_statusentries_by_address_date(?, ?)}");
      cs.setString(1, relayIP);
      cs.setDate(2, new java.sql.Date(timestamp));
      ResultSet rs = cs.executeQuery();
      while (rs.next()) {
        byte[] rawstatusentry = rs.getBytes(1);
        String descriptor = rs.getString(2);
        long validafter = rs.getTimestamp(3).getTime();
        positiveConsensusesNoTarget.add(validafter);
        if (!relevantDescriptors.containsKey(descriptor)) {
          relevantDescriptors.put(descriptor, new HashSet<Long>());
        }
        relevantDescriptors.get(descriptor).add(validafter);
        String fingerprint = rs.getString(4);
        boolean orAddressMatches = rs.getString(5).equals(relayIP);
        String exitaddress = rs.getString(6);
        String rLine = new String(rawstatusentry);
        rLine = rLine.substring(0, rLine.indexOf("\n"));
        String[] parts = rLine.split(" ");
        String htmlString = "r " + parts[1] + " " + parts[2] + " "
            + "<a href=\"serverdesc?desc-id=" + descriptor + "\" "
            + "target=\"_blank\">" + parts[3] + "</a> " + parts[4]
            + " " + parts[5] + " " + (orAddressMatches ? "<b>" : "")
            + parts[6] + (orAddressMatches ? "</b>" : "") + " " + parts[7]
            + " " + parts[8] + "\n";
        if (exitaddress != null && exitaddress.length() > 0) {
          long scanned = rs.getTimestamp(7).getTime();
          htmlString += "  [ExitAddress <b>" + exitaddress
              + "</b> " + validAfterTimeFormat.format(scanned)
              + "]\n";
        }
        if (!statusEntries.containsKey(validafter)) {
          statusEntries.put(validafter, new TreeMap<String, String>());
        }
        statusEntries.get(validafter).put(fingerprint, htmlString);
      }
      rs.close();
      cs.close();
    } catch (SQLException e) {
      /* Nothing found. */
    }

    /* Print out what we found. */
    SimpleDateFormat validAfterUrlFormat = new SimpleDateFormat(
        "yyyy-MM-dd-HH-mm-ss");
    validAfterUrlFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    out.print("<pre><code>");
    for (long consensus : allConsensuses) {
      if (relevantConsensuses.contains(consensus)) {
        String validAfterDatetime = validAfterTimeFormat.format(
            consensus);
        String validAfterString = validAfterUrlFormat.format(consensus);
        out.print("valid-after <b>"
            + "<a href=\"consensus?valid-after="
            + validAfterString + "\" target=\"_blank\">"
            + validAfterDatetime + "</b></a>\n");
        if (statusEntries.containsKey(consensus)) {
          for (String htmlString :
              statusEntries.get(consensus).values()) {
            out.print(htmlString);
          }
        }
        out.print("\n");
      }
    }
    out.print("</code></pre>");
    if (relevantDescriptors.isEmpty()) {
      out.printf("        <p>None found!</p>\n"
          + "        <p>Result is NEGATIVE with high certainty!</p>\n"
          + "        <p>We did not find IP "
          + "address " + relayIP + " in any of the relay or exit lists "
          + "that were published between %s and %s.</p>\n",
          dateFormat.format(timestampTooOld),
          dateFormat.format(timestampTooNew));
      /* Run another query to find out if there are relays running on
       * other IP addresses in the same /24 network and tell the user
       * about it. */
      SortedSet<String> addressesInSameNetwork = new TreeSet<String>();
      String[] relayIPParts = relayIP.split("\\.");
      byte[] address24Bytes = new byte[3];
      address24Bytes[0] = (byte) Integer.parseInt(relayIPParts[0]);
      address24Bytes[1] = (byte) Integer.parseInt(relayIPParts[1]);
      address24Bytes[2] = (byte) Integer.parseInt(relayIPParts[2]);
      String address24 = Hex.encodeHexString(address24Bytes);
      try {
        CallableStatement cs = conn.prepareCall(
            "{call search_addresses_in_same_24 (?, ?)}");
        cs.setString(1, address24);
        cs.setDate(2, new java.sql.Date(timestamp));
        ResultSet rs = cs.executeQuery();
        while (rs.next()) {
          Map<String, String> resultEntry = new HashMap<String, String>();
          String address = rs.getString(1);
          addressesInSameNetwork.add(address);
        }
        rs.close();
        cs.close();
      } catch (SQLException e) {
        /* No other addresses in the same /24 found. */
      }
      if (!addressesInSameNetwork.isEmpty()) {
        out.print("        <p>The following other IP addresses of Tor "
            + "relays in the same /24 network were found in relay and/or "
            + "exit lists around the time that could be related to IP "
            + "address " + relayIP + ":</p>\n");
        out.print("        <ul>\n");
        for (String s : addressesInSameNetwork) {
          out.print("        <li>" + s + "</li>\n");
        }
        out.print("        </ul>\n");
      }
      writeFooter(out);
      try {
        conn.close();
        this.logger.info("Returned a database connection to the pool "
            + "after " + (System.currentTimeMillis()
            - requestedConnection) + " millis.");
      } catch (SQLException e) {
      }
      return;
    }

    /* Print out result. */
    boolean inMostRelevantConsensuses = false,
        inOtherRelevantConsensus = false,
        inTooOldConsensuses = false,
        inTooNewConsensuses = false;
    for (long match : positiveConsensusesNoTarget) {
      if (timestampIsDate &&
          dateFormat.format(match).equals(timestampStr)) {
        inMostRelevantConsensuses = true;
      } else if (!timestampIsDate &&
          match == relevantConsensuses.last()) {
        inMostRelevantConsensuses = true;
      } else if (relevantConsensuses.contains(match)) {
        inOtherRelevantConsensus = true;
      } else if (tooOldConsensuses.contains(match)) {
        inTooOldConsensuses = true;
      } else if (tooNewConsensuses.contains(match)) {
        inTooNewConsensuses = true;
      }
    }
    if (inMostRelevantConsensuses) {
      out.print("        <p>Result is POSITIVE with high certainty!"
            + "</p>\n"
          + "        <p>We found one or more relays on IP address "
          + relayIP + " in ");
      if (timestampIsDate) {
        out.print("relay list published on " + timestampStr);
      } else {
        out.print("the most recent relay list preceding " + timestampStr);
      }
      out.print(" that clients were likely to know.</p>\n");
    } else {
      if (inOtherRelevantConsensus) {
        out.println("        <p>Result is POSITIVE "
            + "with moderate certainty!</p>\n");
        out.println("<p>We found one or more relays on IP address "
            + relayIP + ", but not in ");
        if (timestampIsDate) {
          out.print("a relay list published on " + timestampStr);
        } else {
          out.print("the most recent relay list preceding " + timestampStr);
        }
        out.print(". A possible reason for the relay being missing in a "
            + "relay list might be that some of the directory "
            + "authorities had difficulties connecting to the relay. "
            + "However, clients might still have used the relay.</p>\n");
      } else {
        out.println("        <p>Result is NEGATIVE "
            + "with high certainty!</p>\n");
        out.println("        <p>We did not find any relay on IP address "
            + relayIP
            + " in the relay lists 3 hours preceding " + timestampStr
            + ".</p>\n");
        if (inTooOldConsensuses || inTooNewConsensuses) {
          if (inTooOldConsensuses && !inTooNewConsensuses) {
            out.println("        <p>Note that we found a matching relay "
                + "in relay lists that were published between 15 and 3 "
                + "hours before " + timestampStr + ".</p>\n");
          } else if (!inTooOldConsensuses && inTooNewConsensuses) {
            out.println("        <p>Note that we found a matching relay "
                + "in relay lists that were published up to 12 hours "
                + "after " + timestampStr + ".</p>\n");
          } else {
            out.println("        <p>Note that we found a matching relay "
                + "in relay lists that were published between 15 and 3 "
                + "hours before and in relay lists that were published "
                + "up to 12 hours after " + timestampStr + ".</p>\n");
          }
          if (timestampIsDate) {
            out.println("<p>Be sure to try out the previous/next day or "
                + "provide an exact timestamp in UTC.</p>");
          } else {
            out.println("<p>Make sure that the timestamp you "
                + "provided is correctly converted to the UTC "
                + "timezone.</p>");
          }
        }
        /* We didn't find any descriptor.  No need to look up targets. */
        writeFooter(out);
        try {
          conn.close();
          this.logger.info("Returned a database connection to the pool "
              + "after " + (System.currentTimeMillis()
              - requestedConnection) + " millis.");
        } catch (SQLException e) {
        }
        return;
      }
    }

    /* Second part: target */
    out.println("<br><a name=\"exit\"></a><h3>Was this relay configured "
        + "to permit exiting to a given target?</h3>");

    out.println("        <form action=\"#exit\">\n"
        + "              <input type=\"hidden\" name=\"timestamp\"\n"
        + "                         value=\"" + timestampStr + "\">\n"
        + "              <input type=\"hidden\" name=\"ip\" "
          + "value=\"" + relayIP + "\">\n"
        + "          <table>\n"
        + "            <tr>\n"
        + "              <td align=\"right\">Target address:</td>\n"
        + "              <td><input type=\"text\" name=\"targetaddr\""
          + (targetIP.length() > 0 ? " value=\"" + targetIP + "\"" : "")
          + "\">"
          + (targetAddrWarning.length() > 0 ? "<br><font color=\"red\">"
              + targetAddrWarning + "</font>" : "")
        + "</td>\n"
        + "              <td><i>(Ex.: 4.3.2.1)</i></td>\n"
        + "            </tr>\n"
        + "            <tr>\n"
        + "              <td align=\"right\">Target port:</td>\n"
        + "              <td><input type=\"text\" name=\"targetport\""
          + (targetPort.length() > 0 ? " value=\"" + targetPort + "\""
            : "")
          + ">"
          + (targetPortWarning.length() > 0 ? "<br><font color=\"red\">"
              + targetPortWarning + "</font>" : "")
        + "</td>\n"
        + "              <td><i>(Ex.: 80)</i></td>\n"
        + "            </tr>\n"
        + "            <tr>\n"
        + "              <td></td>\n"
        + "              <td>\n"
        + "                <input type=\"submit\">\n"
        + "                <input type=\"reset\">\n"
        + "              </td>\n"
        + "              <td></td>\n"
        + "            </tr>\n"
        + "          </table>\n"
        + "        </form>\n");

    if (targetIP.length() < 1) {
      writeFooter(out);
      try {
        conn.close();
        this.logger.info("Returned a database connection to the pool "
            + "after " + (System.currentTimeMillis()
            - requestedConnection) + " millis.");
      } catch (SQLException e) {
      }
      return;
    }

    /* Parse router descriptors to check exit policies. */
    out.println("<p>Searching the relay descriptors published by the "
        + "relay on IP address " + relayIP + " to find out whether this "
        + "relay permitted exiting to " + target + ". You may follow the "
        + "links above to the relay descriptors and grep them for the "
        + "lines printed below to confirm that results are correct.</p>");
    SortedSet<Long> positiveConsensuses = new TreeSet<Long>();
    Set<String> missingDescriptors = new HashSet<String>();
    Set<String> descriptors = relevantDescriptors.keySet();
    for (String descriptor : descriptors) {
      byte[] rawDescriptor = null;
      try {
        String query = "SELECT rawdescriptor FROM descriptor "
            + "WHERE descriptor = '" + descriptor + "'";
        Statement statement = conn.createStatement();
        ResultSet rs = statement.executeQuery(query);
        if (rs.next()) {
          rawDescriptor = rs.getBytes(1);
        }
        rs.close();
        statement.close();
      } catch (SQLException e) {
        /* Consider this descriptors as 'missing'. */
        continue;
      }
      if (rawDescriptor != null && rawDescriptor.length > 0) {
        missingDescriptors.remove(descriptor);
        String rawDescriptorString = new String(rawDescriptor,
            "US-ASCII");
        try {
          BufferedReader br = new BufferedReader(
              new StringReader(rawDescriptorString));
          String line = null, routerLine = null, publishedLine = null;
          StringBuilder acceptRejectLines = new StringBuilder();
          boolean foundMatch = false;
          while ((line = br.readLine()) != null) {
            if (line.startsWith("router ")) {
              routerLine = line;
            } else if (line.startsWith("published ")) {
              publishedLine = line;
            } else if (line.startsWith("reject ") ||
                line.startsWith("accept ")) {
              if (foundMatch) {
                out.println(line);
                continue;
              }
              boolean ruleAccept = line.split(" ")[0].equals("accept");
              String ruleAddress = line.split(" ")[1].split(":")[0];
              if (!ruleAddress.equals("*")) {
                if (!ruleAddress.contains("/") &&
                    !ruleAddress.equals(targetIP)) {
                  /* IP address does not match. */
                  acceptRejectLines.append(line + "\n");
                  continue;
                }
                String[] ruleIPParts = ruleAddress.split("/")[0].
                    split("\\.");
                int ruleNetwork = ruleAddress.contains("/") ?
                    Integer.parseInt(ruleAddress.split("/")[1]) : 32;
                for (int i = 0; i < 4; i++) {
                  if (ruleNetwork == 0) {
                    break;
                  } else if (ruleNetwork >= 8) {
                    if (ruleIPParts[i].equals(targetIPParts[i])) {
                      ruleNetwork -= 8;
                    } else {
                      break;
                    }
                  } else {
                    int mask = 255 ^ 255 >>> ruleNetwork;
                    if ((Integer.parseInt(ruleIPParts[i]) & mask) ==
                        (Integer.parseInt(targetIPParts[i]) & mask)) {
                      ruleNetwork = 0;
                    }
                    break;
                  }
                }
                if (ruleNetwork > 0) {
                  /* IP address does not match. */
                  acceptRejectLines.append(line + "\n");
                  continue;
                }
              }
              String rulePort = line.split(" ")[1].split(":")[1];
              if (targetPort.length() < 1 && !ruleAccept &&
                  !rulePort.equals("*")) {
                /* With no port given, we only consider reject :* rules as
                   matching. */
                acceptRejectLines.append(line + "\n");
                continue;
              }
              if (targetPort.length() > 0 && !rulePort.equals("*") &&
                  rulePort.contains("-")) {
                int fromPort = Integer.parseInt(rulePort.split("-")[0]);
                int toPort = Integer.parseInt(rulePort.split("-")[1]);
                int targetPortInt = Integer.parseInt(targetPort);
                if (targetPortInt < fromPort ||
                    targetPortInt > toPort) {
                  /* Port not contained in interval. */
                  continue;
                }
              }
              if (targetPort.length() > 0) {
                if (!rulePort.equals("*") &&
                    !rulePort.contains("-") &&
                    !targetPort.equals(rulePort)) {
                  /* Ports do not match. */
                  acceptRejectLines.append(line + "\n");
                  continue;
                }
              }
              boolean relevantMatch = false;
              for (long match : relevantDescriptors.get(descriptor)) {
                if (relevantConsensuses.contains(match)) {
                  relevantMatch = true;
                }
              }
              if (relevantMatch) {
                String[] routerParts = routerLine.split(" ");
                out.println("<pre><code>" + routerParts[0] + " "
                    + routerParts[1] + " <b>" + routerParts[2] + "</b> "
                    + routerParts[3] + " " + routerParts[4] + " "
                    + routerParts[5]);
                String[] publishedParts = publishedLine.split(" ");
                out.println(publishedParts[0] + " <b>"
                    + publishedParts[1] + " " + publishedParts[2]
                    + "</b>");
                out.print(acceptRejectLines.toString());
                out.println("<b>" + line + "</b>");
                foundMatch = true;
              }
              if (ruleAccept) {
                positiveConsensuses.addAll(
                    relevantDescriptors.get(descriptor));
              }
            }
          }
          br.close();
          if (foundMatch) {
            out.println("</code></pre>");
          }
        } catch (IOException e) {
          /* Could not read descriptor string. */
          continue;
        }
      }
    }

    /* Print out result. */
    inMostRelevantConsensuses = false;
    inOtherRelevantConsensus = false;
    inTooOldConsensuses = false;
    inTooNewConsensuses = false;
    for (long match : positiveConsensuses) {
      if (timestampIsDate &&
          dateFormat.format(match).equals(timestampStr)) {
        inMostRelevantConsensuses = true;
      } else if (!timestampIsDate && match == relevantConsensuses.last()) {
        inMostRelevantConsensuses = true;
      } else if (relevantConsensuses.contains(match)) {
        inOtherRelevantConsensus = true;
      } else if (tooOldConsensuses.contains(match)) {
        inTooOldConsensuses = true;
      } else if (tooNewConsensuses.contains(match)) {
        inTooNewConsensuses = true;
      }
    }
    if (inMostRelevantConsensuses) {
      out.print("        <p>Result is POSITIVE with high certainty!"
            + "</p>\n"
          + "        <p>We found one or more relays on IP address "
          + relayIP + " permitting exit to " + target + " in ");
      if (timestampIsDate) {
        out.print("relay list published on " + timestampStr);
      } else {
        out.print("the most recent relay list preceding " + timestampStr);
      }
      out.print(" that clients were likely to know.</p>\n");
      writeFooter(out);
      try {
        conn.close();
        this.logger.info("Returned a database connection to the pool "
            + "after " + (System.currentTimeMillis()
            - requestedConnection) + " millis.");
      } catch (SQLException e) {
      }
      return;
    }
    boolean resultIndecisive = target.length() > 0
        && !missingDescriptors.isEmpty();
    if (resultIndecisive) {
      out.println("        <p>Result is INDECISIVE!</p>\n"
          + "        <p>At least one referenced descriptor could not be "
          + "found. This is a rare case, but one that (apparently) "
          + "happens. We cannot make any good statement about exit "
          + "relays without these descriptors. The following descriptors "
          + "are missing:</p>");
      for (String desc : missingDescriptors)
        out.println("        <p>" + desc + "</p>\n");
    }
    if (inOtherRelevantConsensus) {
      if (!resultIndecisive) {
        out.println("        <p>Result is POSITIVE "
            + "with moderate certainty!</p>\n");
      }
      out.println("<p>We found one or more relays on IP address "
          + relayIP + " permitting exit to " + target + ", but not in ");
      if (timestampIsDate) {
        out.print("a relay list published on " + timestampStr);
      } else {
        out.print("the most recent relay list preceding " + timestampStr);
      }
      out.print(". A possible reason for the relay being missing in a "
          + "relay list might be that some of the directory authorities "
          + "had difficulties connecting to the relay. However, clients "
          + "might still have used the relay.</p>\n");
    } else {
      if (!resultIndecisive) {
        out.println("        <p>Result is NEGATIVE "
            + "with high certainty!</p>\n");
      }
      out.println("        <p>We did not find any relay on IP address "
          + relayIP + " permitting exit to " + target
          + " in the relay list 3 hours preceding " + timestampStr
          + ".</p>\n");
      if (inTooOldConsensuses || inTooNewConsensuses) {
        if (inTooOldConsensuses && !inTooNewConsensuses) {
          out.println("        <p>Note that we found a matching relay in "
              + "relay lists that were published between 15 and 3 "
              + "hours before " + timestampStr + ".</p>\n");
        } else if (!inTooOldConsensuses && inTooNewConsensuses) {
          out.println("        <p>Note that we found a matching relay in "
              + "relay lists that were published up to 12 hours after "
              + timestampStr + ".</p>\n");
        } else {
          out.println("        <p>Note that we found a matching relay in "
              + "relay lists that were published between 15 and 3 "
              + "hours before and in relay lists that were published up "
              + "to 12 hours after " + timestampStr + ".</p>\n");
        }
        if (timestampIsDate) {
          out.println("<p>Be sure to try out the previous/next day or "
              + "provide an exact timestamp in UTC.</p>");
        } else {
          out.println("<p>Make sure that the timestamp you provided is "
              + "correctly converted to the UTC timezone.</p>");
        }
      }
    }
    if (target != null) {
      if (positiveConsensuses.isEmpty() &&
          !positiveConsensusesNoTarget.isEmpty()) {
        out.println("        <p>Note that although the found relay(s) did "
            + "not permit exiting to " + target + ", there have been one "
            + "or more relays running at the given time.</p>");
      }
    }
    try {
      conn.close();
      this.logger.info("Returned a database connection to the pool "
          + "after " + (System.currentTimeMillis()
          - requestedConnection) + " millis.");
    } catch (SQLException e) {
    }
    writeFooter(out);
  }
}

