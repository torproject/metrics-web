<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
  <title>Tor Metrics</title>
  <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">
  <link href="css/stylesheet-ltr.css" type="text/css" rel="stylesheet">
  <link href="css/bootstrap.min.css" type="text/css" rel="stylesheet">
  <link href="images/favicon.ico" type="image/x-icon" rel="shortcut icon">
</head>
<body>
  <div class="center">
    <div class="main-column">
        <h2><a href="/"><img src="images/metrics-logo.png" width="153" height="200" alt="Metrics logo"><img src="images/metrics-wordmark.png" width="384" height="50" alt="Metrics wordmark"></a></h2>
        <br>

<p>"Tor metrics are the ammunition that lets Tor and other security
advocates argue for a more private and secure Internet from a position
of data, rather than just dogma or perspective."
<i>- Bruce Schneier (June 1, 2016)</i></p>

        <!-- Navigation start -->
        <a href="index.html">Metrics</a> &#124;
        <a href="about.html">About</a> &#124;
        <a href="news.html">News</a> &#124;
        <a href="sources.html">Sources</a> &#124;
        Tools &#124;
        <a href="research.html">Research</a>
        <br>
        <br>
        <!-- Navigation end -->

<h1>Tools</h1>
<br>

<p>We list some tools that you can use to collect data about Tor. We don't use
all of the tools listed here. We use CollecTor to collect (relay data, bridge
data, etc), OONI to collect (data, data, data), and Onionoo to collect (data,
data, data).</p>

<p>With these other tools, you can measure different things about Tor that we
currently do not! We encourage you to do so if you are curious, want check up on
your relays, or conduct some research.</p>

<h2>Our sources</h2>

<ul>
<li><a href="https://collector.torproject.org/">CollecTor</a> collects data from
various nodes and services in the public Tor network.</li>
<li><a href="https://ooni.torproject.org/">OONI</a>, detects censorship,
surveillance and traffic manipulation on the internet.</li>
<li><a href="https://onionoo.torproject.org/">Onionoo</a> provides current and
past data on relays and bridges to other services.</li>
</ul>

<h2>Others</h2>

<ul>
<li><a href="https://gitweb.torproject.org/user/phw/exitmap.git">Exitmap</a> is
a fast and extensible scanner for Tor exit relays.</li>
<li><a href="https://github.com/robgjansen/onionperf">OnionPerf</a> measures the
performance of onion services.</li>
<li><a href="https://gitweb.torproject.org/torperf.git">Torperf</a> measures Tor
performance with a set of utilities and Python scripts.</li>
<li><a href="https://www.torproject.org/projects/tordnsel.html.en">TorDNSEL</a>
publishes lists of IP addresses of multi-homed Tor exits.</li>
<li><a
href="https://gitweb.torproject.org/user/phw/sybilhunter.git/">Sybilhunter</a>
attempts to detect Sybil attacks on the Tor network.</li>
<li><a href="https://exonerator.torproject.org/">ExoneraTor</a> tells you if an
IP was used by a Tor relay on a given date.</li>
<li><a href="https://torps.github.io/">TorPS</a> simulates changes to Tor's path
selection algorithm using archived data.</li>
<li><a
href="https://play.google.com/store/apps/details?id=com.networksaremadeofstring.anonionooid">AnOnionooid</a>
is an Android app that helps find and explore Tor relays and bridges.</li>
<li><a href="https://atlas.torproject.org/">Atlas</a> displays data about single
relays and bridges in the Tor network.</li>
<li><a href="https://compass.torproject.org/">Compass</a> groups current relays in
different ways to measure Tor's network diversity.</li>
<li><a href="https://oniontip.com/">OnionTip</a> distributes bitcoin donations
to relays that can receive them.</li>
<li><a href="https://onionview.codeplex.com/">OnionView</a> plots the location
of active Tor nodes on an interactive map of the world.</li>
<li><a href="https://consensus-health.torproject.org/">Consensus Health</a>
displays information about the current directory consensus and votes.</li>
<li><a
href="https://lists.torproject.org/cgi-bin/mailman/listinfo/tor-consensus-health">Consensus
Issues</a> emails directory authority operators about consensus problems.</li>
<li><a href="https://onionview.codeplex.com/">Check</a> uses<a
href="https://www.torproject.org/projects/tordnsel.html.en">TorDNSEL</a> data to
tell users whether they are using Tor or not.</li>
<li><a href="https://shadow.github.io/">Shadow</a> uses archived Tor directory
data to generate realistic network topologies.</li>
<li><a href="http://lists.infolabe.net/lists/listinfo/infolabe-anomalies">OII's
anomaly detection system</a> ranks countries by how anomalous their Tor usage
is.</li>
<li>Tor's <a
href="https://gitweb.torproject.org/tor.git/tree/scripts/maint/updateFallbackDirs.py">fallback
directories script</a> generates a list of stable directories.</li>
<li><a href="https://github.com/duk3luk3/onion-py">OnionPy</a> provides memcached support to cache queried data.</li>
</ul>

<h2>Things we took out for now</h2>

<ul>
<li><a href="http://tor2web.org/">Tor2web</a> is a web proxy for Tor Hidden
Services.</li>
<li><a href="https://tor-explorer-10kapart2016.azurewebsites.net/">Tor
Explorer</a> displays data on each individual Tor node.</li>
<li><a href="https://nos-oignons.net/Services/index.en.html">Nos oignons</a>
visualizes bandwidth histories of their relays.</li>
<li><a href="https://github.com/kloesing/challenger">challenger</a> aggregates
data from relays participating in EFF's 2014 Tor Challenge.</li>
<li>A <a href="https://duckduckgo.com/">DuckDuckGo</a> search with "tor node"
keywords displays Tor node details.</li>
<li><a
href="https://metrics.torproject.org/uncharted-data-flow.html">metrics-lib</a>
is a Java library to fetch and parse Tor descriptors.</li>
<li><a href="https://stem.torproject.org/">Stem</a> is a Python library that
parses Tor descriptors.</li>
<li><a href="https://github.com/meejah/txtorcon">Txtorcon</a> is an asynchronous
Tor controller library written in Twisted Python.</li>
<li><a href="https://github.com/NullHypothesis/zoossh">Zoossh</a> is a parser written in Go for Tor-specific data formats.</li>
<li><a href="https://savannah.nongnu.org/projects/koninoo/">koninoo</a> is a
simple Java command line interface for querying Onionoo data.</li>
</ul>

    </div>
  </div>
  <div class="bottom" id="bottom">
    <%@ include file="footer.jsp"%>
  </div>
</body>
</html>

