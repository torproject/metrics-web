<%@page import="java.util.*" %>
<%@page import="java.io.*" %>
        <h2>Tor Metrics Portal: Graphs</h2>
        <br/>
        <h3>Relays and bridges in the Tor network</h3>
        <br/>
        <p>The number of relays and bridges in the Tor network can be
        extracted from the hourly published network status consensuses
        and sanitized bridge statuses.</p>
        <ul>
          <li>Past <a href=\"#networksize-30d\">30</a>,
              <a href=\"#networksize-90d\">90</a>,
              <a href=\"#networksize-180d\">180</a> days</li>
          <li><a href=\"#networksize-all\">All data</a> up to today</li>
<%
    Calendar now = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    Calendar lastQuarter = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    lastQuarter.add(Calendar.MONTH, -3);
    Calendar lastMonth = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    lastMonth.add(Calendar.MONTH, -1);
    out.print("          <li>Annual graphs of\n");
    for (int i = now.get(Calendar.YEAR); i > 2006; i--) {
      out.print("              <a href=\"#networksize-" + i + "\">"
          + i + "</a>,\n");
    }
    out.print("              <a href=\"#networksize-2006\">2006</a></li>\n");
    out.print("          <li>Quarterly graphs of\n");
    out.print(String.format("              <a href=\"#networksize-%1$tY-q%2$d\">"
        + "Q%2$d %1$tY</a>,%n", now, 1 + now.get(Calendar.MONTH) / 3));
    out.print(String.format("              <a href=\"#networksize-%1$tY-q%2$d\">"
        + "Q%2$d %1$tY</a></li>%n", lastQuarter, 1 + lastQuarter.get(Calendar.MONTH) / 3));
    out.print("          <li>Monthly graphs of\n");
    out.print(String.format("              <a href=\"#networksize-%1$tY-%1$tm\">"
        + "%1$tb %1$tY</a>,%n", now));
    out.print(String.format("              <a href=\"#networksize-%1$tY-%1$tm\">"
        + "%1$tb %1$tY</a></li>%n", lastMonth));
    out.print("          <li><a href=\"csv/networksize.csv\">CSV</a> file\n"
        + "              containing raw data</li>\n"
        + "        </ul>\n"
        + "        <p><a id=\"networksize-30d\"/>\n"
        + "          <img src=\"graphs/networksize/networksize-30d.png\"/>\n"
        + "        </p><p><a id=\"networksize-90d\"/>\n"
        + "          <img src=\"graphs/networksize/networksize-90d.png\"/>\n"
        + "        </p><p><a id=\"networksize-180d\"/>\n"
        + "          <img src=\"graphs/networksize/networksize-180d.png\"/>\n"
        + "        </p><p><a id=\"networksize-all\"/>\n"
        + "          <img src=\"graphs/networksize/networksize-all.png\"/>\n");
    for (int i = now.get(Calendar.YEAR); i > 2006; i--) {
      out.print("        </p><p><a id=\"networksize-" + i + "\"/>\n"
        + "          <img src=\"graphs/networksize/networksize-" + i + ".png\"/>\n");
    }
    out.print("        </p><p><a id=\"networksize-2006\"/>\n"
        + "          <img src=\"graphs/networksize/networksize-2006.png\"/>\n");
    out.print(String.format("        </p><p><a id=\"networksize-%1$tY-q%2$d\"/>\n"
        + "          <img src=\"graphs/networksize/networksize-%1$tY-q%2$d.png\"/>\n",
        now, 1 + now.get(Calendar.MONTH) / 3));
    out.print(String.format("        </p><p><a id=\"networksize-%1$tY-q%2$d\"/>\n"
        + "          <img src=\"graphs/networksize/networksize-%1$tY-q%2$d.png\"/>\n",
        lastQuarter, 1 + lastQuarter.get(Calendar.MONTH) / 3));
    out.print(String.format("        </p><p><a id=\"networksize-%1$tY-%1$tm\"/>\n"
        + "          <img src=\"graphs/networksize/networksize-%1$tY-%1$tm.png\"/>\n", now));
    out.print(String.format("        </p><p><a id=\"networksize-%1$tY-%1$tm\"/>\n"
        + "          <img src=\"graphs/networksize/networksize-%1$tY-%1$tm.png\"/>\n", lastMonth));
    out.print("        </p><br/>\n");
%>
