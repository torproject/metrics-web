<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<jsp:include page="top.jsp">
  <jsp:param name="pageTitle" value="Development &ndash; Tor Metrics"/>
  <jsp:param name="navActive" value="Development"/>
</jsp:include>

    <div class="container">
      <ul class="breadcrumb">
        <li><a href="/">Home</a></li>
        <li class="active">Development</li>
      </ul>
    </div>

    <div class="container">
      <h1>Development <a href="#development" name="development" class="anchor">#</a></h1>
      <p>You're a developer and want to write a tool that uses Tor network data?  Here we're collecting programming libraries, APIs, and links to other code bases to get inspiration from.</p>
    </div>

    <div class="container">
      <h2>Parsing libraries <a href="#libraries" name="libraries" class="anchor">#</a></h2>
      <p>The following libraries help you with parsing Tor network data from the <a href="https://collector.torproject.org/" target="_blank">CollecTor</a> service.</p>
      <ul>
        <li><a href="https://dist.torproject.org/descriptor/" target="_blank">metrics-lib</a> is a Java library to fetch and parse Tor descriptors.</li>
        <li><a href="https://stem.torproject.org/" target="_blank">Stem</a> is a Python library that parses Tor descriptors.</li>
        <li><a href="https://github.com/NullHypothesis/zoossh" target="_blank">Zoossh</a> is a parser written in Go for Tor-specific data formats.</li>
      </ul>
    </div>

    <div class="container">
      <h2>Query support <a href="#query" name="query" class="anchor">#</a></h2>
      <p>The following tools help you with querying Tor network data from the <a href="https://onionoo.torproject.org/" target="_blank">Onionoo</a> service.</p>
      <ul>
        <li><a href="https://savannah.nongnu.org/projects/koninoo/" target="_blank">koninoo</a> is a simple Java command line interface for querying Onionoo data.</li>
        <li><a href="https://github.com/duk3luk3/onion-py" target="_blank">OnionPy</a> provides memcached support to cache queried data.</li>
        <li><a href="https://github.com/lukechilds/onionoo-node-client" target="_blank">onionoo-node-client</a> is a Node.js client library for the <a href="https://onionoo.torproject.org/" target="_blank">Onionoo</a> API.</li>
      </ul>
    </div>

<jsp:include page="bottom.jsp"/>

