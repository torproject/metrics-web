<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
  <title>Tor Metrics &mdash; Fraction of relays reporting hidden-service statistics</title>
  <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">
  <link href="/css/stylesheet-ltr.css" type="text/css" rel="stylesheet">
  <link href="/images/favicon.ico" type="image/x-icon" rel="shortcut icon">
</head>
<body>
  <div class="center">
    <%@ include file="banner.jsp"%>
    <div class="main-column">

<h2><a href="/"><img src="/images/metrics-wordmark-small.png" width="138" height="18" alt="Metrics wordmark"></a> &mdash; Fraction of relays reporting hidden-service statistics</h2>
<br>
<p>The following graph shows the fraction of
<a href="about.html#relay">relays</a> that report statistics on
<a href="about.html#hidden-service">hidden service</a> usage.
If at least 1% of relays report a statistic, it gets extrapolated towards
a network total, where higher fractions are produce more accurate results.
For more details on the extrapolation algorithm, see <a
href="https://blog.torproject.org/blog/some-statistics-about-onions">this
blog post</a> and <a
href="https://research.torproject.org/techreports/extrapolating-hidserv-stats-2015-01-31.pdf">this
technical report</a>.</p>

<img src="hidserv-frac-reporting.png${hidserv_frac_reporting_url}"
     width="576" height="360" alt="Fraction of relays reporting hidden-service statistics graph">
<form action="hidserv-frac-reporting.html">
  <div class="formrow">
    <input type="hidden" name="graph" value="hidserv-frac-reporting">
    <p>
    <label>Start date (yyyy-mm-dd):
      <input type="text" name="start" size="10"
             value="<c:choose><c:when test="${fn:length(hidserv_frac_reporting_start) == 0}">${default_start_date}</c:when><c:otherwise>${hidserv_frac_reporting_start[0]}</c:otherwise></c:choose>"></label>
    <label>End date (yyyy-mm-dd):
      <input type="text" name="end" size="10"
             value="<c:choose><c:when test="${fn:length(hidserv_frac_reporting_end) == 0}">${default_end_date}</c:when><c:otherwise>${hidserv_frac_reporting_end[0]}</c:otherwise></c:choose>"></label>
    </p><p>
    <input class="submit" type="submit" value="Update graph">
    </p>
  </div>
</form>
<p>Download graph as
<a href="hidserv-frac-reporting.pdf${hidserv_frac_reporting_url}">PDF</a> or
<a href="hidserv-frac-reporting.svg${hidserv_frac_reporting_url}">SVG</a>.</p>
<br>

<h4>Related metrics</h4>
<ul>
<li><a href="hidserv-dir-onions-seen.html">Graph: Unique .onion addresses</a></li>
<li><a href="hidserv-rend-relayed-cells.html">Graph: Hidden-service traffic</a></li>
<li><a href="hidserv-data.html">Data: Hidden-service statistics</a></li>
</ul>

    </div>
  </div>
  <div class="bottom" id="bottom">
    <%@ include file="footer.jsp"%>
  </div>
</body>
</html>

