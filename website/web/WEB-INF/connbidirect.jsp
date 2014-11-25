<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
  <title>Tor Metrics: Fraction of connections used uni-/bidirectionally</title>
  <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">
  <link href="/css/stylesheet-ltr.css" type="text/css" rel="stylesheet">
  <link href="/images/favicon.ico" type="image/x-icon" rel="shortcut icon">
</head>
<body>
  <div class="center">
    <%@ include file="banner.jsp"%>
    <div class="main-column">

<h3>Tor Metrics: Fraction of connections used uni-/bidirectionally</h3>
<br>
<p>The following graph shows the fraction of direct connections between a
<a href="about.html#relay">relay</a> and other nodes in the network that
are used uni- or bi-directionally.
Every 10 seconds, relays determine for every direct connection whether
they read and wrote less than a threshold of 20 KiB.
Connections below this threshold are excluded from the graph.
For the remaining connections, relays determine whether they read/wrote at
least 10 times as many bytes as they wrote/read.
If so, they classify a connection as "Mostly reading" or "Mostly writing",
respectively.
All other connections are classified as "Both reading and writing".
After classifying connections, read and write counters are reset for the
next 10-second interval.
The graph shows daily medians and inter-quartile ranges of reported
fractions.</p>
<img src="connbidirect.png${connbidirect_url}"
     width="576" height="360"
     alt="Fraction of direct connections used uni-/bidirectionally">
<form action="connbidirect.html">
  <div class="formrow">
    <input type="hidden" name="graph" value="connbidirect">
    <p>
    <label>Start date (yyyy-mm-dd):</label>
      <input type="text" name="start" size="10"
             value="<c:choose><c:when test="${fn:length(connbidirect_start) == 0}">${default_start_date}</c:when><c:otherwise>${connbidirect_start[0]}</c:otherwise></c:choose>">
    <label>End date (yyyy-mm-dd):</label>
      <input type="text" name="end" size="10"
             value="<c:choose><c:when test="${fn:length(connbidirect_end) == 0}">${default_end_date}</c:when><c:otherwise>${connbidirect_end[0]}</c:otherwise></c:choose>">
    </p><p>
    <input class="submit" type="submit" value="Update graph">
    </p>
  </div>
</form>
<p>Download graph as
<a href="connbidirect.pdf${connbidirect_url}">PDF</a> or
<a href="connbidirect.svg${connbidirect_url}">SVG</a>.</p>
<p><a href="stats/connbidirect.csv">CSV</a> file containing all data.</p>
<br>

    </div>
  </div>
  <div class="bottom" id="bottom">
    <%@ include file="footer.jsp"%>
  </div>
</body>
</html>
