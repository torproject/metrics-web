<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<jsp:include page="top.jsp">
  <jsp:param name="pageTitle" value="Welcome to Tor Metrics"/>
  <jsp:param name="navActive" value="Home"/>
</jsp:include>

  <div class="container">
  <div class="jumbotron">
    <h1>Welcome to Tor Metrics!</h1>
    <div class="row">
      <div class="col-md-6">
        <p>The <a href="https://www.torproject.org/" target="_blank">Tor</a>
        network is one of the largest deployed anonymity networks, consisting of <a
            href="/networksize.html"><span
                id="latest_relay_count">thousands</span></a> of volunteer-run
        relays and <a
            href="/userstats-relay-country.html"><span
                id="latest_user_count">millions</span></a> of users.
        Users, advocates, relay operators, and journalists can better understand the Tor network
        through data and analysis made available by Tor Metrics.</p>
      </div>
      <div class="col-md-6">
        <p>Analyzing a live anonymity system must be performed with great care so that
        the users' privacy is not put at risk. Any metrics collected <i>must
        not</i> undermine the anonymity or security properties of the Tor
	network. <a href="/about.html">Read more &raquo;</a></p>
      </div>
    </div>
  </div>

  <div class="dashboard">
    <h2><i class="fa fa-line-chart"></i> Analysis</h2>
    <p class="lead">View visualizations of statistics collected from the public Tor network and from Tor Project infrastructure.</p>
    <div class="row">
      <div class="col-sm-4">
        <a href="userstats-relay-country.html"><i class="fa fa-users fa-fw fa-4x" aria-hidden="true"></i> <h3>Users</h3> <p>Where Tor users are from and how they connect to Tor.</p></a>
      </div>

      <div class="col-sm-4">
        <a href="networksize.html"><i class="fa fa-server fa-fw fa-4x" aria-hidden="true"></i> <h3>Servers</h3> <p>How many relays and bridges are online and what we know about them.</p></a>
      </div>

      <div class="col-sm-4">
        <a href="bandwidth.html"><i class="fa fa-road fa-fw fa-4x" aria-hidden="true"></i> <h3>Traffic</h3> <p>How much traffic the Tor network can handle and how much traffic there is.</p></a>
      </div>

      <div class="col-sm-4">
        <a href="torperf.html"><i class="fa fa-dashboard fa-fw fa-4x" aria-hidden="true"></i> <h3>Performance</h3> <p>How fast and reliable the Tor network is.</p></a>
      </div>

      <div class="col-sm-4">
        <a href="hidserv-dir-onions-seen.html"><i class="fa fa-map-signs fa-fw fa-4x" aria-hidden="true"></i> <h3>Onion Services</h3> <p>How many onion services there are and how much traffic they pull.</p></a>
      </div>

      <div class="col-sm-4">
        <a href="webstats-tb.html"><i class="fa fa-download fa-fw fa-4x" aria-hidden="true"></i> <h3>Applications</h3> <p>How many Tor applications, like Tor Browser, have been downloaded or updated.</p></a>
      </div>

    </div>
  </div>

  <div class="dashboard">
    <h2><i class="fa fa-cogs"></i> Services</h2>
    <p class="lead">Perform interactive queries for more detailed information relating to relays or bridges in the public Tor network.</p>
    <div class="row">
      <div class="col-sm-4">
        <a href="/rs.html#search"><i class="fa fa-search fa-fw fa-4x" aria-hidden="true"></i> <h3>Relay Search</h3> <p>Look up information on a particular Tor relay or bridge.</p></a>
      </div>

      <div class="col-sm-4">
        <a href="/rs.html#aggregate"><i class="fa fa-compress fa-fw fa-4x" aria-hidden="true"></i> <h3>Aggregated Relay Search</h3> <p>Look up aggregated statistics on groups of relays.</p></a>
      </div>

      <div class="col-sm-4">
        <a href="/exonerator.html"><i class="fa fa-history fa-fw fa-4x" aria-hidden="true"></i> <h3>Network Archive</h3> <p>Look up if a particular IP address was used as a Tor relay on a particular date.</p></a>
      </div>

    </div>
  </div>
  </div>

<jsp:include page="bottom.jsp"/>

