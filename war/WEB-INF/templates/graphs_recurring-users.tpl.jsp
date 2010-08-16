<%@ page import="java.util.*" %>
        <h2>Tor Metrics Portal: Graphs</h2>
        <br/>
        <h3>Recurring, directly connecting Tor users</h3>
        <br/>
        <p>After being connected to the Tor network, users need to refresh
        their list of running relays on a regular basis. They send their
        requests to one out of a few hundred directory mirrors to save
        bandwidth of the directory authorities. The following graphs show
        an estimate of recurring Tor users based on the requests as seen
        by trusted, a particularly fast directory mirror.</p>
        <ul>
          <li><a href="#total">All users</a></li>
          <li><a href="#australia">Australia</a></li>
          <li><a href="#bahrain">Bahrain</a></li>
          <li><a href="#brazil">Brazil</a></li>
          <li><a href="#burma">Burma</a></li>
          <li><a href="#canada">Canada</a></li>
          <li><a href="#china">China</a></li>
          <li><a href="#cuba">Cuba</a></li>
          <li><a href="#ethiopia">Ethiopia</a></li>
          <li><a href="#france">France</a></li>
          <li><a href="#germany">Germany</a></li>
          <li><a href="#iran">Iran</a></li>
          <li><a href="#italy">Italy</a></li>
          <li><a href="#japan">Japan</a></li>
          <li><a href="#poland">Poland</a></li>
          <li><a href="#russia">Russia</a></li>
          <li><a href="#saudi">Saudi</a></li>
          <li><a href="#southkorea">South Korea</a></li>
          <li><a href="#sweden">Sweden</a></li>
          <li><a href="#syria">Syria</a></li>
          <li><a href="#tunisia">Tunisia</a></li>
          <li><a href="#turkmenistan">Turkmenistan</a></li>
          <li><a href="#uk">U.K.</a></li>
          <li><a href="#usa">U.S.A.</a></li>
          <li><a href="#uzbekistan">Uzbekistan</a></li>
          <li><a href="#vietnam">Vietnam</a></li>
          <li><a href="#yemen">Yemen</a></li>
        </ul>
        <ul>
          <li><a href="csv/recurring-users.csv">CSV</a> file containing
          all data.</li>
          <li><a href="csv/monthly-users-peak.csv">CSV</a> file containing
          peak daily Tor users (recurring and bridge) per month by
          country.</li>
          <li><a href="csv/monthly-users-average.csv">CSV</a> file
          containing average daily Tor users (recurring and bridge) per
          month by country.</li>
        </ul>
<%
    List<String> countries = Arrays.asList(("total,australia,bahrain,"
        + "brazil,burma,canada,china,cuba,ethiopia,france,germany,iran,"
        + "italy,japan,poland,russia,saudi,southkorea,sweden,syria,"
        + "tunisia,turkmenistan,uk,usa,uzbekistan,vietnam,yemen").
        split(","));
    List<String> suffixes = new ArrayList<String>(Arrays.asList(
        "30d,90d,180d,all".split(",")));
    Calendar now = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    suffixes.add(String.format("%tY", now));
    suffixes.add(String.format("%1$tY-q%2$d", now,
        1 + now.get(Calendar.MONTH) / 3));
    suffixes.add(String.format("%1$tY-%1$tm", now));
    for (String country : countries) {
      out.print("        <p><a id=\"" + country + "\"/>\n");
      for (String suffix : suffixes) {
        out.print("        <img src=\"graphs/direct-users/" + country
            + "-direct-" + suffix + ".png\"/>\n");
      }
      out.print("        </p>\n");
    }
%>
        <br/>
