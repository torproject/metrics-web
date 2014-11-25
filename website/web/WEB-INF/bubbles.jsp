<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
  <title>Tor Metrics &mdash; Network bubble graphs</title>
  <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">
  <link href="/css/stylesheet-ltr.css" type="text/css" rel="stylesheet">
  <link href="/images/favicon.ico" type="image/x-icon" rel="shortcut icon">
  <script src="/js/d3.min.js"></script>
  <script src="/js/bubbles.js"></script>
</head>
<body>
  <div class="center">
    <%@ include file="banner.jsp"%>
    <div class="main-column">

<h2><a href="/">Tor Metrics</a> &mdash; Diversity</h2>
<br>
<p>The graph on this page will soon move to its own subpages.
This is part of an effort to make navigation on this website more intuitive.
In the future, this notice will go away, and all graphs/tables can be searched more easily from the start page.
Sorry for any inconvenience caused by this.</p>

<ul>
<li>Graph: Network bubble graphs (see below)</li>
</ul>
<br>
<hr>
<br>

<h2><a href="/">Tor Metrics</a> &mdash; Network bubble graphs</h2>
<br>
<p>The following graph visualizes diversity of currently running
<a href="about.html#relay">relays</a> in terms of their probability to be
selected for <a href="about.html#circuit">circuits</a>.
Fast relays with at least 100 Mbit/s bandwidth capacity, and which
therefore have a high probability of being selected for circuits, are
represented by an onion; smaller relays are shown as a simple dot; and the
slowest relays, which are almost never selected for circuits, are omitted
entirely.
Graphs in the "all relays" category use a relay's
<a href="about.html#consensus-weight">consensus weight</a> as probability,
whereas graphs in the "exits only" category use a value derived from a
relay's consensus weight that resembles the probability of selecting that
relay as exit node.
All graphs support grouping relays by same autonomous system, contact
information, country, or network family.</p>

      <p>
        All relays:
        <a href="#no-group" onclick="make_bubble_graph('no-group');">No group</a> |
        <a href="#as" onclick="make_bubble_graph('as');">Autonomous Systems</a> |
        <a href="#contact" onclick="make_bubble_graph('contact');">Contact</a>  |
        <a href="#country" onclick="make_bubble_graph('country');">Country</a> |
        <a href="#network-family" onclick="make_bubble_graph('network-family');">Network family (/16)</a>
      </p>
      <p>
        Exits only:
        <a href="#no-group-exits-only" onclick="make_bubble_graph('no-group-exits-only');">No group</a> |
        <a href="#as-exits-only" onclick="make_bubble_graph('as-exits-only');">Autonomous Systems</a> |
        <a href="#contact-exits-only" onclick="make_bubble_graph('contact-exits-only');">Contact</a>  |
        <a href="#country-exits-only" onclick="make_bubble_graph('country-exits-only');">Country</a> |
        <a href="#network-family-exits-only" onclick="make_bubble_graph('network-family-exits-only');">Network family (/16)</a>
      </p>
      <script>make_bubble_graph();</script>
      <noscript>Sorry, you need to turn on JavaScript.</script>

    </div>
  </div>
  <div class="bottom" id="bottom">
    <%@ include file="footer.jsp"%>
  </div>
</body>
</html>

