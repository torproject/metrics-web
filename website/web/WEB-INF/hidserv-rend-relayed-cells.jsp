<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
  <title>Tor Metrics &mdash; Hidden-service traffic</title>
  <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">
  <link href="/css/stylesheet-ltr.css" type="text/css" rel="stylesheet">
  <link href="/images/favicon.ico" type="image/x-icon" rel="shortcut icon">
</head>
<body>
  <div class="center">
    <%@ include file="banner.jsp"%>
    <div class="main-column">

<h2><a href="/"><img src="/images/metrics-wordmark-small.png" width="138" height="18" alt="Metrics wordmark"></a> &mdash; Hidden-service traffic</h2>
<br>
<p>The following graph shows the amount of hidden-service traffic in the
network per day.
This number is extrapolated from aggregated statistics on hidden-service
traffic reported by single <a href="about.html#relay">relays</a>
acting as rendezvous points for
<a href="about.html#hidden-service">hidden services</a>, if at least 1% of
relays reported these statistics.
For more details on the extrapolation algorithm, see <a
href="https://blog.torproject.org/blog/some-statistics-about-onions">this
blog post</a> and <a
href="https://research.torproject.org/techreports/extrapolating-hidserv-stats-2015-01-31.pdf">this
technical report</a>.</p>

<img src="hidserv-rend-relayed-cells.png${hidserv_rend_relayed_cells_url}"
     width="576" height="360" alt="Hidden-service traffic graph">
<form action="hidserv-rend-relayed-cells.html">
  <div class="formrow">
    <input type="hidden" name="graph" value="hidserv-rend-relayed-cells">
    <p>
    <label>Start date (yyyy-mm-dd):
      <input type="text" name="start" size="10"
             value="<c:choose><c:when test="${fn:length(hidserv_rend_relayed_cells_start) == 0}">${default_start_date}</c:when><c:otherwise>${hidserv_rend_relayed_cells_start[0]}</c:otherwise></c:choose>"></label>
    <label>End date (yyyy-mm-dd):
      <input type="text" name="end" size="10"
             value="<c:choose><c:when test="${fn:length(hidserv_rend_relayed_cells_end) == 0}">${default_end_date}</c:when><c:otherwise>${hidserv_rend_relayed_cells_end[0]}</c:otherwise></c:choose>"></label>
    </p><p>
    <input class="submit" type="submit" value="Update graph">
    </p>
  </div>
</form>
<p>Download graph as
<a href="hidserv-rend-relayed-cells.pdf${hidserv_rend_relayed_cells_url}">PDF</a> or
<a href="hidserv-rend-relayed-cells.svg${hidserv_rend_relayed_cells_url}">SVG</a>.</p>
<br>

<h4>Underlying data</h4>
<ul>
<li><a href="hidserv-data.html">Data: Hidden-service statistics</a></li>
</ul>

<h4>Related metrics</h4>
<ul>
<li><a href="hidserv-dir-onions-seen.html">Graph: Unique .onion addresses</a></li>
<li><a href="hidserv-frac-reporting.html">Graph: Fraction of relays reporting hidden-service statistics</a></li>
</ul>

    </div>
  </div>
  <div class="bottom" id="bottom">
    <%@ include file="footer.jsp"%>
  </div>
</body>
</html>

