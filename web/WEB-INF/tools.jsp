<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
  <title>Tor Metrics Portal: Tools</title>
  <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">
  <link href="/css/stylesheet-ltr.css" type="text/css" rel="stylesheet">
  <link href="/images/favicon.ico" type="image/x-icon" rel="shortcut icon">
</head>
<body>
  <div class="center">
    <%@ include file="banner.jsp"%>
    <div class="main-column">
        <h2>Tor Metrics Portal: Tools</h2>
        <br>
        <p>The metrics website and related websites depend on a collection
        of services that measure, archive, process, and finally present Tor
        network data.
        This page gives an overview of these services.</p>
        <img src="/images/tor-metrics-overview.png"/>
        <br>
        <a name="measure"></a>
        <h3><a href="#measure" class="anchor">Measuring Tor network
        data</a></h3>
        <br>
        <p>Tor network data is measured at various places:
        <ul>
          <li><a href="https://gitweb.torproject.org/torperf.git">Torperf</a>
          is a set of utilities for testing Tor performance from a client
          perspective.</li>
          <li><a href="https://gitweb.torproject.org/tor.git">tor</a>
          relays and bridges gather aggregate usage statistics and publish
          descriptors containing data about Tor network structure and
          usage.</li>
          <li><a href="https://gitweb.torproject.org/tordnsel.git">TorDNSEL</a>
          is a Tor DNS-based exit list that runs periodic checks whether
          relays use different IP addresses for exiting to the Internet
          than they use to register in the Tor network.</li>
          <li><a href="https://gitweb.torproject.org/bridgedb.git">BridgeDB</a>
          assigns bridges to distributors and gives them out via HTTPS or
          email.</li>
        </ul>
        <br>
        <a name="archive"></a>
        <h3><a href="#archive" class="anchor">Archiving Tor network
        data</a></h3>
        <br>
        <p>All Tor network data is downloaded, possibly sanitized, and
        then archived by a single tool:</p>
        <ul>
          <li><a href="https://gitweb.torproject.org/metrics-db.git">metrics-db</a>
          contains five components for archiving relay descriptors, bridge
          descriptors, Torperf results, TorDNSEL exit lists, and BridgeDB
          pool assignments.</li>
        </ul>
        <br>
        <a name="process"></a>
        <h3><a href="#process" class="anchor">Processing Tor network
        data</a></h3>
        <br>
        <p>In some cases, processing and presenting Tor network data is
        separated for maximum flexibility.
        In particular, there are currently two main tools that process Tor
        network data and write an intermediate data format, but don't
        directly present results:</p>
        <ul>
          <li><a href="https://gitweb.torproject.org/metrics-web.git">metrics-web</a>
          is the software behind this website, including aggregation code
          that produces statistics files.</li>
          <li><a href="https://gitweb.torproject.org/metrics-tasks.git/tree/HEAD:/task-6498">task-6498</a>
          is a submodule of metric-web that
          aggregates data to visualize fast exits in the Tor network.</li>
          <li><a href="https://gitweb.torproject.org/metrics-tasks.git/tree/HEAD:/task-8462">task-8462</a>
          is another submodule of metric-web that
          estimates daily users from reported directory request
          statistics.</li>
          <li><a href="https://gitweb.torproject.org/onionoo.git">Onionoo</a>
          provides Tor network status information in JSON format via a
          RESTful web service.</li>
        </ul>
        <br>
        <a name="present"></a>
        <h3><a href="#present" class="anchor">Presenting Tor network
        data</a></h3>
        <br>
        <p>There are a few websites and additional tools presenting Tor
        network data:
        <ul>
          <li><a href="https://gitweb.torproject.org/metrics-web.git">metrics-web</a>
          also contains the code that presents aggregate statistics on
          this website.</li>
          <li><a href="https://gitweb.torproject.org/exonerator.git">ExoneraTor</a>
          is a website that tells you whether a given IP address was a Tor
          relay.</li>
          <li><a href="https://gitweb.torproject.org/atlas.git">Atlas</a>
          is a web application to discover relays that uses Onionoo as its
          data back-end.</li>
          <li><a href="https://gitweb.torproject.org/globe.git">Globe</a>
          is a Tor relay and bridge explorer that also uses Onionoo as its
          data back-end.</li>
          <li><a href="https://gitweb.torproject.org/compass.git">Compass</a>
          is a web application that uses Onionoo's data to display
          information about fast exits in the Tor network.</li>
        </ul>
    </div>
  </div>
  <div class="bottom" id="bottom">
    <%@ include file="footer.jsp"%>
  </div>
</body>
</html>
