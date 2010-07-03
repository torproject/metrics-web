        <h2>Tor Metrics Portal: Graphs</h2>
        <br/>
        <h3>New or returning, directly connecting Tor users</h3>
        <br/>
        <p>Users connecting to the Tor network for the first time request
        a list of running relays from one of currently seven directory
        authorities. Likewise, returning users whose network information is
        out of date connect to one of the directory authorities to
        download a fresh list of relays. The following graphs display an
        estimate of new or returning Tor users based on the requests as
        seen by gabelmoo, one of the directory authorities.</p>
        <ul>
          <li><a href="#bahrain">Bahrain</a></li>
          <li><a href="#china">China</a></li>
          <li><a href="#cuba">Cuba</a></li>
          <li><a href="#ethiopia">Ethiopia</a></li>
          <li><a href="#iran">Iran</a></li>
          <li><a href="#burma">Burma</a></li>
          <li><a href="#saudi">Saudi</a></li>
          <li><a href="#syria">Syria</a></li>
          <li><a href="#tunisia">Tunisia</a></li>
          <li><a href="#turkmenistan">Turkmenistan</a></li>
          <li><a href="#uzbekistan">Uzbekistan</a></li>
          <li><a href="#vietnam">Vietnam</a></li>
          <li><a href="#yemen">Yemen</a></li>
        </ul>
        <ul>
          <li><a href="csv/new-users.csv">CSV</a> file containing all
          data.</li>
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
      out.print("        <p><a id=\"" + country + "\"/>\n");
      for (String suffix : suffixes) {
        out.print("        <img src=\"graphs/new-users/" + country
            + "-new-" + suffix + ".png\"/>\n");
      }
      out.print("        </p>");
%>
        <br/>
