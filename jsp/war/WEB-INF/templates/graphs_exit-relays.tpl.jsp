<%@page import="java.io.*" %>
<%@page import="java.util.*" %>
<div>
<%
        out.print( "        <h2>Tor Metrics Portal: Graphs</h2>\n"
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
    out.print(String.format("              <a href=\"#exit-%1$tY-q%2$d\">"
        + "Q%2$d %1$tY</a>,%n", now, 1 + now.get(Calendar.MONTH) / 3));
    out.print(String.format("              <a href=\"#exit-%1$tY-q%2$d\">"
        + "Q%2$d %1$tY</a></li>%n", lastQuarter, 1 + lastQuarter.get(Calendar.MONTH) / 3));
    out.print("          <li>Monthly graphs of\n");
    out.print(String.format("              <a href=\"#exit-%1$tY-%1$tm\">"
        + "%1$tb %1$tY</a>,%n", now));
    out.print(String.format("              <a href=\"#exit-%1$tY-%1$tm\">"
        + "%1$tb %1$tY</a></li>%n", lastMonth));
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
    out.print(String.format("        </p><p><a id=\"exit-%1$tY-q%2$d\"/><img src=\"graphs/exit/exit-%1$tY-q%2$d.png\"/>\n",
        now, 1 + now.get(Calendar.MONTH) / 3));
    out.print(String.format("        </p><p><a id=\"exit-%1$tY-q%2$d\"/><img src=\"graphs/exit/exit-%1$tY-q%2$d.png\"/>\n",
        lastQuarter, 1 + lastQuarter.get(Calendar.MONTH) / 3));
    out.print(String.format("        </p><p><a id=\"exit-%1$tY-%1$tm\"/><img src=\"graphs/exit/exit-%1$tY-%1$tm.png\"/>\n", now));
    out.print(String.format("        </p><p><a id=\"exit-%1$tY-%1$tm\"/><img src=\"graphs/exit/exit-%1$tY-%1$tm.png\"/>\n", lastMonth));
    out.print("        </p><br/>\n");
%>
</div>
