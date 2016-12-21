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
      <h1>Sources</h1>
      <p>You're a data person and only trust the statistics that you doctored yourself?  Here's all the data right from the source, doctor.</p>
    </div>

    <div class="container tools">
      <a name="archive" id="anchor-archive"></a>
      <h2>Network archives</h2>
      <p>We get our data from the network archives below.</p>
      <div class="row">

        <div class="col-md-3 col-sm-4 col-xs-6">
          <a href="https://collector.torproject.org/" target="_blank">
            <div class="logo" style="background-image:url(images/collector-logo.png);"></div>
            <h2>CollecTor</h2>
            <p>collects data from various nodes and services in the public Tor network.</p>
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

    <div class="container tools">
      <a name="measurement" id="anchor-measurement"></a>
      <h2>Measurement tools</h2>
      <p>The following tools perform active measurements in the Tor network.  (They don't have pretty logos yet, but they are all uniquely useful!)</p>
      <div class="row">

        <div class="col-md-2 col-sm-3 col-xs-4">
          <a href="https://www.torproject.org/projects/tordnsel.html.en" target="_blank">
            <div class="logo" style="background-image:url(images/default-logo.png);"></div>
            <h3>TorDNSEL</h3>
            <p>publishes lists of IP addresses of multi-homed Tor exits.</p>
          </a>
        </div>

        <div class="col-md-2 col-sm-3 col-xs-4">
          <a href="https://gitweb.torproject.org/torperf.git" target="_blank">
            <div class="logo" style="background-image:url(images/default-logo.png);"></div>
            <h3>Torperf</h3>
            <p>measures Tor performance with a set of utilities and Python scripts.</p>
          </a>
        </div>

        <div class="col-md-2 col-sm-3 col-xs-4">
          <a href="https://github.com/robgjansen/onionperf" target="_blank">
            <div class="logo" style="background-image:url(images/default-logo.png);"></div>
            <h3>OnionPerf</h3>
            <p>measures the performance of onion services.</p>
          </a>
        </div>

      </div>
    </div>

    <div class="container tools">
      <a name="aggregated" id="anchor-aggregated"></a>
      <h2>Pre-aggregated data</h2>
      <p>Sometimes the data from the original sources can be hard to process.  If you want to take a little shortcut, try out the following pre-aggregated statistics.</p>
      <div class="row">

        <div class="col-md-2 col-sm-3 col-xs-4">
          <a href="stats.html">
            <div class="logo" style="background-image:url(images/default-logo.png);"></div>
            <h3>CSV files</h3>
            <p>are available with aggregated statistics of visualizations on this site.</p>
          </a>
        </div>

        <div class="col-md-2 col-sm-3 col-xs-4">
          <a href="https://onionoo.torproject.org/" target="_blank">
            <div class="logo" style="background-image:url(images/default-logo.png);"></div>
            <h3>Onionoo</h3>
            <p>provides current and historical data about relays and bridges via a web-based API.</p>
          </a>
        </div>

      </div>
    </div>

<jsp:include page="bottom.jsp"/>

