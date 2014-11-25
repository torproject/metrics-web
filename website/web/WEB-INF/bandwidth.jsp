<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
  <title>Tor Metrics &mdash; Total relay bandwidth in the network</title>
  <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">
  <link href="/css/stylesheet-ltr.css" type="text/css" rel="stylesheet">
  <link href="/images/favicon.ico" type="image/x-icon" rel="shortcut icon">
</head>
<body>
  <div class="center">
    <%@ include file="banner.jsp"%>
    <div class="main-column">

<h2><a href="/">Tor Metrics</a> &mdash; Bandwidth</h2>
<br>
<p>The graphs on this page (except one) have moved to their own subpages.
This is part of an effort to make navigation on this website more intuitive.
In the future, this notice will go away, and all graphs/tables can be searched more easily from the start page.
Sorry for any inconvenience caused by this.</p>

<ul>
<li>Graph: Total relay bandwidth in the network (see below)</li>
<li><a href="bwhist-flags.html">Graph: Relay bandwidth by Exit and/or Guard flags</a></li>
<li><a href="bandwidth-flags.html">Graph: Advertised bandwidth and bandwidth history by relay flags</a></li>
<li><a href="dirbytes.html">Graph: Number of bytes spent on answering directory requests</a></li>
<li><a href="advbwdist-perc.html">Graph: Advertised bandwidth distribution</a></li>
<li><a href="advbwdist-relay.html">Graph: Advertised bandwidth of n-th fastest relays</a></li>
<li><a href="bandwidth-data.html">Data: Bandwidth provided and consumed by relays</a></li>
<li><a href="advbwdist-data.html">Data: Advertised bandwidth distribution and n-th fastest relays</a></li>
</ul>
<br>
<hr>
<br>

<h2><a href="/">Tor Metrics</a> &mdash; Total relay bandwidth in the network</h2>
<br>
<p>The following graph shows the total
<a href="about.html#advertised-bandwidth">advertised</a> and
<a href="about.html#bandwidth-history">consumed bandwidth</a> of all
<a href="about.html#relay">relays</a> in the network.</p>
<img src="bandwidth.png${bandwidth_url}"
     width="576" height="360" alt="Relay bandwidth graph">
<form action="bandwidth.html">
  <div class="formrow">
    <input type="hidden" name="graph" value="bandwidth">
    <p>
    <label>Start date (yyyy-mm-dd):</label>
      <input type="text" name="start" size="10"
             value="<c:choose><c:when test="${fn:length(bandwidth_start) == 0}">${default_start_date}</c:when><c:otherwise>${bandwidth_start[0]}</c:otherwise></c:choose>">
    <label>End date (yyyy-mm-dd):</label>
      <input type="text" name="end" size="10"
             value="<c:choose><c:when test="${fn:length(bandwidth_end) == 0}">${default_end_date}</c:when><c:otherwise>${bandwidth_end[0]}</c:otherwise></c:choose>">
    </p><p>
    <input class="submit" type="submit" value="Update graph">
    </p>
  </div>
</form>
<p>Download graph as
<a href="bandwidth.pdf${bandwidth_url}">PDF</a> or
<a href="bandwidth.svg${bandwidth_url}">SVG</a>.</p>
<br>

<h4>Related metrics</h4>
<ul>
<li><a href="bwhist-flags.html">Graph: Relay bandwidth by Exit and/or Guard flags</a></li>
<li><a href="bandwidth-flags.html">Graph: Advertised bandwidth and bandwidth history by relay flags</a></li>
<li><a href="dirbytes.html">Graph: Number of bytes spent on answering directory requests</a></li>
<li><a href="bandwidth-data.html">Data: Bandwidth provided and consumed by relays</a></li>
</ul>

    </div>
  </div>
  <div class="bottom" id="bottom">
    <%@ include file="footer.jsp"%>
  </div>
</body>
</html>
