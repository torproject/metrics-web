<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<jsp:include page="top.jsp">
  <jsp:param name="pageTitle" value="Services &ndash; Tor Metrics"/>
  <jsp:param name="navActive" value="Services"/>
</jsp:include>

    <div class="container">
      <ul class="breadcrumb">
        <li><a href="/">Home</a></li>
        <li class="active">Services</li>
      </ul>
    </div>

    <div class="container">
      <h1>Services <a href="#services" name="services" class="anchor">#</a></h1>
      <p>You're running a Tor relay or bridge, or you're affected in some way by somebody else running one?  With the following services you can learn everything about currently running Tor nodes or even about nodes that have been running in the past.</p>
    </div>

    <div class="container">
      <h2>Network archive <a href="#archive" name="archive" class="anchor">#</a></h2>
      <p>The following tool lets you browse the network archive for relays running in the past.</p>
      <ul>
        <li><a href="https://exonerator.torproject.org/" target="_blank">ExoneraTor</a> tells you if an IP was used by a Tor relay on a given date.</li>
      </ul>
    </div>

    <div class="container">
      <h2>Network status <a href="#status" name="status" class="anchor">#</a></h2>
      <p>The following tools let you explore currently running relays and bridges.</p>
      <ul>
        <li><a href="/rs.html">Relay Search</a> displays data about single relays and bridges in the Tor Network.</li>
        <li><a href="https://consensus-health.torproject.org/" target="_blank">Consensus Health</a> displays information about the current directory consensus and votes.</li>
        <li><a href="https://tormap.void.gr/" target="_blank">Tor Map</a> displays an interactive map of Tor relays and provides KML files for relay locations.</li>
        <li><a href="https://nusenu.github.io/OrNetStats/" target="_blank">OrNetStats</a> displays statistics for monitoring diversity in the Tor network.</li>
        <li><a href="https://duckduckgo.com/" target="_blank">DuckDuckGo</a> displays Tor node details when including the keywords "tor node" in a search.</li>
        <li><a href="https://onionite.now.sh/" target="_blank">Onionite</a> is a Progressive Web App to view information on the individual nodes that make up the Tor network.</li>
      </ul>
    </div>

    <div class="container">
      <h2>Network health notifications <a href="#health" name="health" class="anchor">#</a></h2>
      <p>The following tools inform you of any problems with relays and bridges.</p>
      <ul>
        <li><a href="https://lists.torproject.org/cgi-bin/mailman/listinfo/tor-consensus-health" target="_blank">Consensus Issues</a> emails directory authority operators about consensus problems.</li>
        <li><a href="http://lists.infolabe.net/lists/listinfo/infolabe-anomalies" target="_blank">OII's anomaly detection system</a> ranks countries by how anomalous their Tor usage is.</li>
      </ul>
    </div>

<jsp:include page="bottom.jsp"/>

