<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
  <title>Tor Metrics &mdash; Relays by platform</title>
  <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">
  <link href="/css/stylesheet-ltr.css" type="text/css" rel="stylesheet">
  <link href="/images/favicon.ico" type="image/x-icon" rel="shortcut icon">
</head>
<body>
  <div class="center">
    <%@ include file="banner.jsp"%>
    <div class="main-column">

<h2><a href="/"><img src="/images/metrics-wordmark-small.png" width="138" height="18" alt="Metrics wordmark"></a> &mdash; Relays by platform</a></h2>
<br>
<p>The following graph shows the number of running
<a href="about.html#relay">relays</a> by operating system.
Relays report their operating system when they announce themselves in the
network.</p>
<img src="platforms.png${platforms_url}"
     width="576" height="360" alt="Relay platforms graph">
<form action="platforms.html">
  <div class="formrow">
    <input type="hidden" name="graph" value="platforms">
    <p>
    <label>Start date (yyyy-mm-dd):
      <input type="text" name="start" size="10"
             value="<c:choose><c:when test="${fn:length(platforms_start) == 0}">${default_start_date}</c:when><c:otherwise>${platforms_start[0]}</c:otherwise></c:choose>"></label>
    <label>End date (yyyy-mm-dd):
      <input type="text" name="end" size="10"
             value="<c:choose><c:when test="${fn:length(platforms_end) == 0}">${default_end_date}</c:when><c:otherwise>${platforms_end[0]}</c:otherwise></c:choose>"></label>
    </p><p>
    <input class="submit" type="submit" value="Update graph">
    </p>
  </div>
</form>
<p>Download graph as
<a href="platforms.pdf${platforms_url}">PDF</a> or
<a href="platforms.svg${platforms_url}">SVG</a>.</p>
<br>

<h4>Underlying data</h4>
<ul>
<li><a href="servers-data.html">Data: Number of relays and bridges</a></li>
</ul>

<h4>Related metrics</h4>
<ul>
<li><a href="networksize.html">Graph: Relays and bridges in the network</a></li>
<li><a href="relayflags.html">Graph: Relays with Exit, Fast, Guard, Stable, and HSDir flags</a></li>
<li><a href="versions.html">Graph: Relays by version</a></li>
</ul>

    </div>
  </div>
  <div class="bottom" id="bottom">
    <%@ include file="footer.jsp"%>
  </div>
</body>
</html>
