<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
  <title>Tor Metrics</title>
  <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">
  <link href="/css/stylesheet-ltr.css" type="text/css" rel="stylesheet">
  <link href="/images/favicon.ico" type="image/x-icon" rel="shortcut icon">
</head>
<body>
  <div class="center">
    <%@ include file="banner.jsp"%>
    <div class="main-column">
        <h2><a href="/">Tor Metrics</a></h2>
        <br>
        <p>Welcome to Tor Metrics, the primary place to learn interesting
        facts about the Tor network, the largest deployed anonymity
        network to date.
        If something can be measured safely, you'll find it here.*</p>
        <p><small>*And if you come across something that is missing here,
        please let us know.</small></p>

        <h3>Servers</h3>
        <p>How many relays and bridges are in the network?
        How many of them permit exiting?</p>
<ul>
<li><a href="networksize.html">Graph: Relays and bridges in the network</a></li>
<li><a href="relayflags.html">Graph: Relays with Exit, Fast, Guard, Stable, and HSDir flags</a></li>
<li><a href="versions.html">Graph: Relays by version</a></li>
<li><a href="platforms.html">Graph: Relays by platform</a></li>
<li><a href="cloudbridges.html">Graph: Tor Cloud bridges</a></li>
<li><a href="servers-data.html">Data: Number of relays and bridges</a></li>
</ul>

        <h3>Bandwidth</h3>
        <p>How much bandwidth do relays advertise?
        And how much of that is actually consumed?</p>

<ul>
<li><a href="bandwidth.html">Graph: Total relay bandwidth in the network</a></li>
<li><a href="bwhist-flags.html">Graph: Relay bandwidth by Exit and/or Guard flags</a></li>
<li><a href="bandwidth-flags.html">Graph: Advertised bandwidth and bandwidth history by relay flags</a></li>
<li><a href="dirbytes.html">Graph: Number of bytes spent on answering directory requests</a></li>
<li><a href="advbwdist-perc.html">Graph: Advertised bandwidth distribution</a></li>
<li><a href="advbwdist-relay.html">Graph: Advertised bandwidth of n-th fastest relays</a></li>
<li><a href="bandwidth-data.html">Data: Bandwidth provided and consumed by relays</a></li>
<li><a href="advbwdist-data.html">Data: Advertised bandwidth distribution and n-th fastest relays</a></li>
</ul>

        <h3>Diversity</h3>
        <p>How diverse is the network?
        In which countries are relays located?</p>

<ul>
<li><a href="bubbles.html">Graph: Network bubble graphs</a></li>
</ul>

        <h3>Users</h3>
        <p>Where do users come from?
        What transports and IP versions are they using?</p>

<ul>
<li><a href="userstats-relay-country.html">Graph: Direct users by country</a></li>
<li><a href="userstats-relay-table.html">Table: Top-10 countries by directly connecting users</a></li>
<li><a href="userstats-censorship-events.html">Table: Top-10 countries by possible censorship events</a></li>
<li><a href="userstats-bridge-country.html">Graph: Bridge users by country</a></li>
<li><a href="userstats-bridge-table.html">Table: Top-10 countries by bridge users</a></li>
<li><a href="userstats-bridge-transport.html">Graph: Bridge users by transport</a></li>
<li><a href="userstats-bridge-version.html">Graph: Bridge users by IP version</a></li>
<li><a href="oxford-anonymous-internet.html">Link: Tor users as percentage of larger Internet population</a></li>
<li><a href="clients-data.html">Data: Estimated number of clients in the Tor network</a></li>
</ul>

        <h3>Performance</h3>
        <p>How long does it take to download a megabyte of data over Tor?
        How about five?</p>

<ul>
<li><a href="torperf.html">Graph: Time to download files over Tor</a></li>
<li><a href="torperf-failures.html">Graph: Timeouts and failures of downloading files over Tor</a></li>
<li><a href="connbidirect.html">Graph: Fraction of connections used uni-/bidirectionally</a></li>
<li><a href="torperf-data.html">Data: Performance of downloading static files over Tor</a></li>
<li><a href="connbidirect-data.html">Data: Fraction of connections used uni-/bidirectionally</a></li>
</ul>

    </div>
  </div>
  <div class="bottom" id="bottom">
    <%@ include file="footer.jsp"%>
  </div>
</body>
</html>
