<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<jsp:include page="top.jsp">
  <jsp:param name="pageTitle" value="Sources &ndash; Tor Metrics"/>
  <jsp:param name="navActive" value="Sources"/>
</jsp:include>

    <div class="container">
      <ul class="breadcrumb">
        <li><a href="/">Home</a></li>
        <li class="active">Sources</li>
      </ul>
    </div>

    <div class="container">
      <h1>Sources <a href="#sources" name="sources" class="anchor">#</a></h1>
      <p>You're a data person and only trust the statistics that you doctored yourself?  Here's all the data right from the source, doctor.</p>
    </div>

    <div class="container">
      <h2>Main sources <a href="#main" name="main" class="anchor">#</a></h2>
      <p>We get most of our data from the services below.</p>
      <ul>
        <li><a href="collector.html">CollecTor</a> collects and archives data from various nodes and services in the public Tor network.</li>
        <li><a href="stats.html">Statistics</a> used for visualizations on this site are available in the CSV format.</li>
        <li><a href="onionoo.html">Onionoo</a> provides current and historical data about relays and bridges via a web-based API.</li>
      </ul>
    </div>

    <div class="container">
      <h2>Other sources <a href="#other" name="other" class="anchor">#</a></h2>
      <p>The following tools perform active measurements in the Tor network or provide other Tor network data.</p>
      <ul>
        <li><a href="https://gitweb.torproject.org/user/phw/exitmap.git" target="_blank">Exitmap</a> is a fast and extensible scanner for Tor exit relays.</li>
        <li><a href="https://www.torproject.org/projects/tordnsel.html.en" target="_blank">TorDNSEL</a> publishes lists of IP addresses of multi-homed Tor exits.</li>
        <li><a href="https://gitweb.torproject.org/torperf.git" target="_blank">Torperf</a> measures Tor performance with a set of utilities and Python scripts.</li>
        <li><a href="https://github.com/robgjansen/onionperf" target="_blank">OnionPerf</a> measures the performance of onion services.</li>
        <li><a href="https://ooni.torproject.org/" target="_blank">OONI</a> detects censorship, surveillance, and traffic manipulation on the internet.</li>
        <li><a href="https://gitweb.torproject.org/user/phw/sybilhunter.git/" target="_blank">Sybilhunter</a> attempts to detect Sybil attacks on the Tor network.</li>
        <li><a href="https://webstats.torproject.org/" target="_blank">Webstats</a> collects logs from <code>torproject.org</code> web servers and provides them as a stripped-down version of Apache's "combined" log format without IP addresses, log times, HTTP parameters, referers, and user agent strings.</li>
      </ul>
    </div>

    <div class="container">
      <h2>Specifications <a href="#specifications" name="specifications" class="anchor">#</a></h2>
      <p>The following specification documents are available for Tor network data.</p>
      <ul>
        <li><a href="https://gitweb.torproject.org/torspec.git/tree/dir-spec.txt" target="_blank">Tor directory protocol, version 3</a></li>
        <li><a href="https://gitweb.torproject.org/torspec.git/tree/attic/dir-spec-v2.txt" target="_blank">Tor directory protocol, version 2</a></li>
        <li><a href="https://gitweb.torproject.org/torspec.git/tree/attic/dir-spec-v1.txt" target="_blank">Tor directory protocol, version 1</a></li>
        <li><a href="bridge-descriptors.html">Tor bridge descriptors</a></li>
      </ul>
    </div>

<jsp:include page="bottom.jsp"/>

