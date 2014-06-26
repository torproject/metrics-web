<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
  <title>Tor Metrics: Network bubble graphs</title>
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
