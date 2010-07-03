<%@page import="java.io.*" %>
<%@page import="java.util.*" %>
        <h2>Tor Metrics Portal: Graphs</h2>
        <br/>
        <h3>Tor users via bridges</h3>
        <br/>
        <p>Users who cannot connect directly to the Tor network instead
        connect via bridges, which are non-public relays. The following
        graphs display an estimate of Tor users via bridges based on the
        unique IP addresses as seen by a few hundred bridges.</p>
        <ul>
          <li><a href=\"#bahrain\">Bahrain</a></li>
          <li><a href=\"#china\">China</a></li>
          <li><a href=\"#cuba\">Cuba</a></li>
          <li><a href=\"#ethiopia\">Ethiopia</a></li>
          <li><a href=\"#iran\">Iran</a></li>
          <li><a href=\"#burma\">Burma</a></li>
          <li><a href=\"#saudi\">Saudi</a></li>
          <li><a href=\"#syria\">Syria</a></li>
          <li><a href=\"#tunisia\">Tunisia</a></li>
          <li><a href=\"#turkmenistan\">Turkmenistan</a></li>
          <li><a href=\"#uzbekistan\">Uzbekistan</a></li>
          <li><a href=\"#vietnam\">Vietnam</a></li>
          <li><a href=\"#yemen\">Yemen</a></li>
        </ul>
        <ul>
          <li><a href=\"csv/bridge-users.csv\">CSV</a> file containing all
          data.</li>
          <li><a href=\"csv/monthly-users-peak.csv\">CSV</a> file
          containing peak daily Tor users (recurring and bridge) per month
          by country.</li>
          <li><a href=\"csv/monthly-users-average.csv\">CSV</a> file
          containing average daily Tor users (recurring and bridge) per
          month by country.</li>
        </ul>
<%
    List<String> countries = Arrays.asList((
        "bahrain,china,cuba,ethiopia,iran,burma,saudi,syria,tunisia,"
        + "turkmenistan,uzbekistan,vietnam,yemen").split(","));
    List<String> suffixes = new ArrayList<String>(Arrays.asList(
        "30d,90d,180d,all".split(",")));
    Calendar now = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    suffixes.add(String.format("%tY", now));
    suffixes.add(String.format("%1$tY-q%2$d", now,
        1 + now.get(Calendar.MONTH) / 3));
    suffixes.add(String.format("%1$tY-%1$tm", now));
    for (String country : countries) {
      out.print("<p><a id=\"" + country + "\"/>\n");
      for (String suffix : suffixes) {
        out.print("        <img src=\"graphs/bridge-users/" + country
            + "-bridges-" + suffix + ".png\"/>\n");
      }
      out.print("        </p>");
    }
    out.print("<br/>\n");
%>
