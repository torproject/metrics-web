package org.torproject.ernie.web;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;

public class ExitRelaysGraphsServlet extends HttpServlet {

  public void doGet(HttpServletRequest request,
      HttpServletResponse response) throws IOException,
      ServletException {

    PrintWriter out = response.getWriter();
    out.print("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\">\n"
        + "<html>\n"
        + "  <head>\n"
        + "    <title>Tor Metrics Portal: Exit relays in the Tor network</title>\n"
        + "    <meta http-equiv=Content-Type content=\"text/html; charset=iso-8859-1\">\n"
        + "    <link href=\"http://www.torproject.org/stylesheet-ltr.css\" type=text/css rel=stylesheet>\n"
        + "    <link href=\"http://www.torproject.org/favicon.ico\" type=image/x-icon rel=\"shortcut icon\">\n"
        + "  </head>\n"
        + "  <body>\n"
        + "    <div class=\"center\">\n"
        + "      <table class=\"banner\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" summary=\"\">\n"
        + "        <tr>\n"
        + "          <td class=\"banner-left\"><a href=\"https://www.torproject.org/\"><img src=\"http://www.torproject.org/images/top-left.png\" alt=\"Click to go to home page\" width=\"193\" height=\"79\"></a></td>\n"
        + "          <td class=\"banner-middle\">\n"
        + "            <a href=\"/\">Home</a>\n"
        + "            <a href=\"graphs.html\">Graphs</a>\n"
        + "            <a href=\"research.html\">Research</a>\n"
        + "            <a href=\"status.html\">Status</a>\n"
        + "            <br/>\n"
        + "            <font size=\"2\">\n"
        + "              <a href=\"consensus-graphs.html\">Network size</a>\n"
        + "              <a class=\"current\">Exit relays</a>\n"
        + "              <a href=\"new-users-graphs.html\">New users</a>\n"
        + "              <a href=\"recurring-users-graphs.html\">Recurring users</a>\n"
        + "              <a href=\"bridge-users-graphs.html\">Bridge users</a>\n"
        + "              <a href=\"torperf-graphs.html\">torperf</a>\n"
        + "              <a href=\"gettor-graphs.html\">GetTor</a>\n"
        + "            </font>\n"
        + "          </td>\n"
        + "          <td class=\"banner-right\"></td>\n"
        + "        </tr>\n"
        + "      </table>\n"
        + "      <div class=\"main-column\">\n"
        + "        <h2>Tor Metrics Portal: Graphs</h2>\n"
        + "        <br/>\n"
        + "        <h3>Exit relays in the Tor network</h3>\n"
        + "        <br/>\n"
        + "        <p>The number of exit relays in the Tor network can be extracted from\n"
        + "        the hourly published network status consensuses.</p>\n"
        + "        <ul>\n"
        + "          <li>Past <a href=\"#exit-72h\">72 hours</a> up to\n"
        + "              now</li>\n"
        + "          <li>Past <a href=\"#exit-30d\">30</a>,\n"
        + "              <a href=\"#exit-90d\">90</a>,\n"
        + "              <a href=\"#exit-180d\">180</a> days</li>\n"
        + "          <li><a href=\"#exit-all\">All data</a> up to today</li>\n"
        + "          <li>Annual graphs of\n");
    Calendar now = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    Calendar lastQuarter = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    lastQuarter.add(Calendar.MONTH, -3);
    Calendar lastMonth = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    lastMonth.add(Calendar.MONTH, -1);
    for (int i = now.get(Calendar.YEAR); i > 2006; i--) {
      out.print("              <a href=\"#exit-" + i + "\">"
          + i + "</a>,\n");
    }
    out.print("              <a href=\"#exit-2006\">2006</a></li>\n");
    out.print("          <li>Quarterly graphs of\n");
    out.printf("              <a href=\"#exit-%1$tY-q%2$d\">"
        + "Q%2$d %1$tY</a>,%n", now, 1 + now.get(Calendar.MONTH) / 3);
    out.printf("              <a href=\"#exit-%1$tY-q%2$d\">"
        + "Q%2$d %1$tY</a></li>%n", lastQuarter, 1 + lastQuarter.get(Calendar.MONTH) / 3);
    out.print("          <li>Monthly graphs of\n");
    out.printf("              <a href=\"#exit-%1$tY-%1$tm\">"
        + "%1$tb %1$tY</a>,%n", now);
    out.printf("              <a href=\"#exit-%1$tY-%1$tm\">"
        + "%1$tb %1$tY</a></li>%n", lastMonth);
    out.print("          <li><a href=\"csv/exit.csv\">CSV</a> file\n"
        + "              containing raw data</li>\n"
        + "        </ul>\n");
    out.print("        </p><p><a id=\"exit-72h\"/><img src=\"graphs/exit/exit-72h.png\"/>\n"
        + "        </p><p><a id=\"exit-30d\"/><img src=\"graphs/exit/exit-30d.png\"/>\n"
        + "        </p><p><a id=\"exit-90d\"/><img src=\"graphs/exit/exit-90d.png\"/>\n"
        + "        </p><p><a id=\"exit-180d\"/><img src=\"graphs/exit/exit-180d.png\"/>\n"
        + "        </p><p><a id=\"exit-all\"/><img src=\"graphs/exit/exit-all.png\"/>\n");
    for (int i = now.get(Calendar.YEAR); i > 2006; i--) {
      out.print("        </p><p><a id=\"exit-" + i + "\"/><img src=\"graphs/exit/exit-" + i + ".png\"/>\n");
    }
    out.print("        </p><p><a id=\"exit-2006\"/><img src=\"graphs/exit/exit-2006.png\"/>\n");
    out.printf("        </p><p><a id=\"exit-%1$tY-q%2$d\"/><img src=\"graphs/exit/exit-%1$tY-q%2$d.png\"/>\n",
        now, 1 + now.get(Calendar.MONTH) / 3);
    out.printf("        </p><p><a id=\"exit-%1$tY-q%2$d\"/><img src=\"graphs/exit/exit-%1$tY-q%2$d.png\"/>\n",
        lastQuarter, 1 + lastQuarter.get(Calendar.MONTH) / 3);
    out.printf("        </p><p><a id=\"exit-%1$tY-%1$tm\"/><img src=\"graphs/exit/exit-%1$tY-%1$tm.png\"/>\n", now);
    out.printf("        </p><p><a id=\"exit-%1$tY-%1$tm\"/><img src=\"graphs/exit/exit-%1$tY-%1$tm.png\"/>\n", lastMonth);
    out.print("        </p><br/>\n"
        + "      </div>\n"
        + "    </div>\n"
        + "    <div class=\"bottom\" id=\"bottom\">\n"
        + "      <p>\"Tor\" and the \"Onion Logo\" are <a href=\"https://www.torproject.org/trademark-faq.html.en\">registered trademarks</a> of The Tor Project, Inc.</p>\n"
        + "    </div>\n"
        + "  </body>\n"
        + "</html>\n");
    out.close();
  }
}

