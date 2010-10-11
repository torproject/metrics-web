package org.torproject.ernie.web;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.math.*;
import java.sql.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;

import org.apache.commons.codec.*;
import org.apache.commons.codec.binary.*;

public class RelayServlet extends HttpServlet {

  private static SimpleDateFormat dayFormat =
      new SimpleDateFormat("yyyy-MM-dd");

  static {
    dayFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
  }

  private Connection conn = null;

  public void init() {

    /* Try to load the database driver. */
    try {
      Class.forName("org.postgresql.Driver");
    } catch (ClassNotFoundException e) {
      /* Don't initialize conn and always reply to all requests with
       * "500 internal server error". */
      return;
    }

    /* Read JDBC URL from deployment descriptor. */
    String connectionURL = getServletContext().
        getInitParameter("jdbcUrl");

    /* Try to connect to database. */
    try {
      conn = DriverManager.getConnection(connectionURL);
    } catch (SQLException e) {
      conn = null;
    }
  }

  private void writeHeader(PrintWriter out) throws IOException {
    out.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 "
          + "Transitional//EN\"\n"
        + "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n"
        + "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n"
        + "  <head>\n"
        + "    <meta content=\"text/html; charset=ISO-8859-1\"\n"
        + "          http-equiv=\"content-type\" />\n"
        + "    <title>Relay</title>\n"
        + "    <meta http-equiv=Content-Type content=\"text/html; "
          + "charset=iso-8859-1\">\n"
        + "    <link href=\"/css/stylesheet-ltr.css\" type=text/css "
          + "rel=stylesheet>\n"
        + "    <link href=\"/images/favicon.ico\" "
          + "type=image/x-icon rel=\"shortcut icon\">\n"
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
        + "            <br/>\n"
        + "            <font size=\"2\">\n"
        + "              <a href=\"exonerator.html\">ExoneraTor</a>\n"
        + "              <a class=\"current\">Relay Search</a>\n"
        + "              <a href=\"consensus-health.html\">Consensus "
          + "Health</a>\n"
        + "              <a href=\"log.html\">Last Log</a>\n"
        + "            </font>\n"
        + "          </td>\n"
        + "          <td class=\"banner-right\"></td>\n"
        + "        </tr>\n"
        + "      </table>\n"
        + "      <div class=\"main-column\" style=\"margin:5; "
          + "Padding:0;\">\n"
        + "        <h2>Relay</h2>\n");
  }

  private void writeFooter(PrintWriter out) throws IOException {
    out.println("        <br/>\n"
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
          + "href=\"https://www.torproject.org/docs/trademark-faq.html.en\">"
          + "registered trademarks</a> of The Tor Project, Inc.</p>\n"
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

    /* Measure how long it takes to process this request. */
    long started = System.currentTimeMillis();

    /* Get print writer and start writing response. */
    PrintWriter out = response.getWriter();
    writeHeader(out);

    /* Check if we have a database connection. */
    if (conn == null) {
      out.println("<br/><p><font color=\"red\"><b>Warning: </b></font>"
          + "This server doesn't have any relay descriptors available. "
          + "If this problem persists, please "
          + "<a href=\"mailto:tor-assistants@freehaven.net\">let us "
          + "know</a>!</p>\n");
      writeFooter(out);
      return;
    }

    /* Check fingerprint parameter. */
    String fingerprintParameter = request.getParameter("fingerprint");
    boolean validParameter = true;
    if (fingerprintParameter == null ||
        fingerprintParameter.length() < 8 ||
        fingerprintParameter.length() > 40) {
      validParameter = false;
    } else {
      Pattern fingerprintPattern = Pattern.compile("^[0-9a-f]{8,40}$");
      if (!fingerprintPattern.matcher(fingerprintParameter.toLowerCase()).
          matches()) {
        validParameter = false;
      }
    }
    if (!validParameter) {
      out.write("    <br/><p>Sorry, \"" + fingerprintParameter
          + "\" is not a valid relay fingerprint. Please provide at "
          + "least the first 8 hex characters of a relay "
          + "fingerprint.</p>\n");
      writeFooter(out);
      return;
    }

    /* If we were only given a partial fingerprint, look up all
     * fingerprints starting with that part to see if it's unique in the
     * last 30 days. */
    String fingerprint = fingerprintParameter.toLowerCase();
    if (fingerprint.length() < 40) {
      SortedSet<String> allFingerprints = new TreeSet<String>();
      try {
        Statement statement = conn.createStatement();
        String query = "SELECT DISTINCT fingerprint FROM statusentry "
            + "WHERE validafter >= '"
            + dayFormat.format(started - 30L * 24L * 60L * 60L * 1000L)
            + " 00:00:00' AND fingerprint LIKE '" + fingerprint + "%'";
        ResultSet rs = statement.executeQuery(query);
        while (rs.next()) {
          allFingerprints.add(rs.getString(1));
        }
        statement.close();
      } catch (SQLException e) {
        out.println("<p><font color=\"red\"><b>Warning: </b></font>We "
            + "experienced an unknown database problem while looking up "
            + "the relay with fingerprint starting with "
            + fingerprintParameter + ". If this problem persists, please "
            + "<a href=\"mailto:tor-assistants@freehaven.net\">let us "
            + "know</a>!</p>\n");
        writeFooter(out);
        return;
      }
      if (allFingerprints.size() == 0) {
        out.write("<p>No relay found with fingerprint starting with "
            + fingerprintParameter + " in the last 30 days.</p>");
        writeFooter(out);
        return;
      } else if (allFingerprints.size() > 1) {
        out.println("<p>The fingerprint part " + fingerprintParameter
            + " is not unique for relays running in the last 30 days. "
            + "Please choose one of the following fingerprints:</p><ul>");
        for (String f : allFingerprints) {
          out.println("<li><a href=\"relay.html?fingerprint=" + f + "\">"
              + f + "</a></li>");
        }
        out.write("</ul><br/>");
        writeFooter(out);
        return;
      } else {
        fingerprint = allFingerprints.first();
      }
    }

    /* Print out in which consensuses this relay was last contained. */
    boolean foundRelay = false;
    String lastDescriptor = null;
    try {
      Statement statement = conn.createStatement();
      String query = "SELECT validafter, rawdesc FROM statusentry WHERE "
          + "validafter >= '"
          + dayFormat.format(started - 30L * 24L * 60L * 60L * 1000L)
          + " 00:00:00' AND fingerprint = '" + fingerprint
          + "' ORDER BY validafter DESC LIMIT 3";
      ResultSet rs = statement.executeQuery(query);
      boolean printedDescription = false;
      while (rs.next()) {
        foundRelay = true;
        if (!printedDescription) {
          out.println("<p>The relay with fingerprint "
              + (fingerprintParameter.length() < 40 ? "starting " : "")
              + "with " + fingerprintParameter + " was last "
              + "referenced in the following relay lists:</p>");
          printedDescription = true;
        }
        String validAfter = rs.getTimestamp(1).toString().
            substring(0, 19);
        out.println("        <br/><tt>valid-after "
            + "<a href=\"consensus?valid-after="
            + validAfter.replaceAll(":", "-").replaceAll(" ", "-")
            + "\" target=\"_blank\">" + validAfter + "</a></tt><br/>");
        byte[] rawStatusEntry = rs.getBytes(2);
        try {
          String statusEntryLines = new String(rawStatusEntry,
              "US-ASCII");
          String[] lines = statusEntryLines.split("\n");
          for (String line : lines) {
            if (line.startsWith("r ")) {
              String[] parts = line.split(" ");
              String descriptor = String.format("%040x",
                  new BigInteger(1, Base64.decodeBase64(parts[3]
                  + "==")));
              if (lastDescriptor == null) {
                lastDescriptor = descriptor;
              }
              out.println("    <tt>r " + parts[1] + " " + parts[2] + " "
                  + "<a href=\"descriptor.html?desc-id=" + descriptor
                  + "\" target=\"_blank\">" + parts[3] + "</a> "
                  + parts[4] + " " + parts[5] + " " + parts[6] + " "
                  + parts[7] + " " + parts[8] + "</tt><br/>");
            } else {
              out.println("    <tt>" + line + "</tt><br/>");
            }
          }
        } catch (UnsupportedEncodingException e) {
          /* This shouldn't happen, because we know that ASCII is
           * supported. */
        }
      }
      statement.close();
    } catch (SQLException e) {
      out.println("<p><font color=\"red\"><b>Warning: </b></font>We "
          + "experienced an unknown database problem while looking up "
          + "the relay with fingerprint "
          + (fingerprintParameter.length() < 40 ? "starting with " : "")
          + fingerprintParameter + ". If this problem persists, please "
          + "<a href=\"mailto:tor-assistants@freehaven.net\">let us "
          + "know</a>!</p>\n");
      writeFooter(out);
      return;
    }

    /* If we didn't find this relay, stop here. */
    if (!foundRelay) {
      out.write("<p>No relay found with fingerprint "
          + (fingerprintParameter.length() < 40 ? "starting with " : "")
          + fingerprintParameter + " in the last 30 days.</p>");
      writeFooter(out);
      return;
    }

    /* Look up last server and extra-info descriptor in the database. */
    String query = null, descriptor = null, nickname = null,
        published = null, extrainfo = null;
    byte[] rawDescriptor = null, rawExtrainfo = null;
    if (lastDescriptor != null) {
      try {
        Statement statement = conn.createStatement();
        query = "SELECT descriptor, nickname, published, extrainfo, "
            + "rawdesc FROM descriptor WHERE descriptor = '"
            + lastDescriptor + "'";
        ResultSet rs = statement.executeQuery(query);
        if (rs.next()) {
          descriptor = rs.getString(1);
          nickname = rs.getString(2);
          published = rs.getTimestamp(3).toString().substring(0, 19);
          extrainfo = rs.getString(4);
          rawDescriptor = rs.getBytes(5);
          query = "SELECT rawdesc FROM extrainfo WHERE extrainfo = '"
              + extrainfo + "'";
          rs = statement.executeQuery(query);
          if (rs.next()) {
            rawExtrainfo = rs.getBytes(1);
          }
        }
      } catch (SQLException e) {
        out.write("<br/><p><font color=\"red\"><b>Warning: </b></font>"
            + "Internal server error when looking up descriptor. The "
            + "query was '" + query + "'. If this problem persists, "
            + "please "
            + "<a href=\"mailto:tor-assistants@freehaven.net\">let us "
            + "know</a>!</p>\n");
        writeFooter(out);
        return;
      }
    }

    /* If no descriptor was found, stop here. */
    if (descriptor == null) {
      out.write("<p>No descriptor found with identifier " + descriptor
          + " which was referenced in the last relay list.</p>");
      writeFooter(out);
      return;
    }

    /* Print out both server and extra-info descriptor. */
    out.write("<br/><p>The last referenced server descriptor published "
        + "by this relay is:</p>");
    BufferedReader br = new BufferedReader(new StringReader(new String(
        rawDescriptor, "US-ASCII")));
    String line = null;
    while ((line = br.readLine()) != null) {
      out.println("        <tt>" + line + "</tt><br/>");
    }
    br.close();
    if (rawExtrainfo != null) {
      out.println("<br/><p>Together with this server descriptor, the "
          + "relay published the following extra-info descriptor:</p>");
      br = new BufferedReader(new StringReader(new String(rawExtrainfo,
          "US-ASCII")));
      line = null;
      while ((line = br.readLine()) != null) {
        out.println("        <tt>" + line + "</tt><br/>");
      }
    }

    /* Provide links to raw descriptors, too. */
    out.println("<br/><p>Note that the descriptor" + (rawExtrainfo != null
        ? "s have" : " has") + " been converted to ASCII and reformatted "
        + "for display purposes. You may also download the raw "
        + "<a href=\"serverdesc?desc-id=" + descriptor
        + "\" target=\"_blank\">server " + "descriptor</a>"
        + (extrainfo != null ? " and <a href=\"extrainfodesc?desc-id="
        + extrainfo + "\" target=\"_blank\">extra-info descriptor</a>"
        : "") + " as " + (extrainfo != null ? "they were" : "it was")
        + " published to the directory authorities.</p>");

    /* Display total lookup time on the results page. */
    long searchTime = System.currentTimeMillis() - started;
    out.write("        <br/><p>Looking up this relay took us "
        + String.format("%d.%03d", searchTime / 1000, searchTime % 1000)
        + " seconds.</p>\n");

    /* Finish writing response. */
    writeFooter(out);
  }
}

