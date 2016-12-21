<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<jsp:include page="top.jsp">
  <jsp:param name="pageTitle" value="Operation &ndash; Tor Metrics"/>
  <jsp:param name="navActive" value="Operation"/>
</jsp:include>

    <div class="container">
      <ul class="breadcrumb">
        <li><a href="index.html">Home</a></li>
        <li class="active">Operation</li>
      </ul>
    </div>

    <div class="container">
      <h1>Operation</h1>
<ul>
<li><a href="https://exonerator.torproject.org/">ExoneraTor</a> tells you if an IP was used by a Tor relay on a given date.</li>
<li><a href="https://atlas.torproject.org/">Atlas</a> displays data about single relays and bridges in the Tor network.</li>
<li><a href="https://compass.torproject.org/">Compass</a> groups current relays in different ways to measure Tor's network diversity.</li>
<li><a href="https://oniontip.com/">OnionTip</a> distributes bitcoin donations to relays that can receive them.</li>
<li><a href="https://consensus-health.torproject.org/">Consensus Health</a> displays information about the current directory consensus and votes.</li>
<li><a href="https://lists.torproject.org/cgi-bin/mailman/listinfo/tor-consensus-health">Consensus Issues</a> emails directory authority operators about consensus problems.</li>
<li><a href="https://onionview.codeplex.com/">Check</a> uses<a href="https://www.torproject.org/projects/tordnsel.html.en">TorDNSEL</a> data to tell users whether they are using Tor or not.</li>
<li><a href="http://lists.infolabe.net/lists/listinfo/infolabe-anomalies">OII's anomaly detection system</a> ranks countries by how anomalous their Tor usage is.</li>
<li><a href="https://tor-explorer-10kapart2016.azurewebsites.net/">Tor Explorer</a> displays data on each individual Tor node.</li>
<li>A <a href="https://duckduckgo.com/">DuckDuckGo</a> search with "tor node" keywords displays Tor node details.</li>
<li><a href="https://onionview.codeplex.com/">OnionView</a> plots the location of active Tor nodes on an interactive map of the world.</li>
</ul>
    </div>

<jsp:include page="bottom.jsp"/>

