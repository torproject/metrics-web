<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<jsp:include page="top.jsp">
  <jsp:param name="pageTitle" value="Sources &ndash; Tor Metrics"/>
  <jsp:param name="navActive" value="Sources"/>
</jsp:include>

    <div class="container">
      <ul class="breadcrumb">
        <li><a href="index.html">Home</a></li>
        <li class="active">Sources</li>
      </ul>
    </div>

    <div class="container">
      <h1>Sources <a href="#sources" name="sources" class="anchor">#</a></h1>
      <p>You're a data person and only trust the statistics that you doctored yourself?  Here's all the data right from the source, doctor.</p>
    </div>

    <div class="container tools">
      <h2>Network archives <a href="#archive" name="archive" class="anchor">#</a></h2>
      <p>We get our data from the network archives below.</p>
      <div class="row">
        <div class="col-md-3 col-sm-4 col-xs-6">
          <a href="https://collector.torproject.org/" target="_blank">
            <div class="logo" style="background-image:url(images/collector-logo.png);"></div>
            <h2>CollecTor</h2>
            <p>collects and archives data from various nodes and services in the public Tor network.</p>
          </a>
        </div>
        <div class="col-md-3 col-sm-4 col-xs-6">
          <a href="https://ooni.torproject.org/" target="_blank">
            <div class="logo" style="background-image:url(images/ooni-logo.png);"></div>
            <h2>OONI</h2>
            <p>detects censorship, surveillance, and traffic manipulation on the internet.</p>
          </a>
        </div>
      </div>
    </div>

    <div class="container">
      <h2>Measurement tools <a href="#measurement" name="measurement" class="anchor">#</a></h2>
      <p>The following tools perform active measurements in the Tor network.</p>
      <ul>
        <li><a href="https://gitweb.torproject.org/user/phw/exitmap.git" target="_blank">Exitmap</a> is a fast and extensible scanner for Tor exit relays.</li>
        <li><a href="https://www.torproject.org/projects/tordnsel.html.en" target="_blank">TorDNSEL</a> publishes lists of IP addresses of multi-homed Tor exits.</li>
        <li><a href="https://gitweb.torproject.org/torperf.git" target="_blank">Torperf</a> measures Tor performance with a set of utilities and Python scripts.</li>
        <li><a href="https://github.com/robgjansen/onionperf" target="_blank">OnionPerf</a> measures the performance of onion services.</li>
        <li><a href="https://gitweb.torproject.org/user/phw/sybilhunter.git/" target="_blank">Sybilhunter</a> attempts to detect Sybil attacks onthe Tor network.</li>
      </ul>
    </div>

    <div class="container">
      <h2>Pre-aggregated data <a href="#aggregated" name="aggregated" class="anchor">#</a></h2>
      <p>Sometimes the data from the original sources can be hard to process.  If you want to take a little shortcut, try out the following pre-aggregated statistics.</p>
      <ul>
        <li><a href="stats.html">CSV files</a> are available with aggregated statistics of visualizations on this site.</li>
        <li><a href="https://onionoo.torproject.org/" target="_blank">Onionoo</a> provides current and historical data about relays and bridges via a web-based API.</li>
      </ul>
    </div>

<jsp:include page="bottom.jsp"/>

