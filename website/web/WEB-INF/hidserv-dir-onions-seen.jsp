<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
  <title>Tor Metrics &mdash; Unique .onion addresses</title>
  <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">
  <link href="/css/stylesheet-ltr.css" type="text/css" rel="stylesheet">
  <link href="/images/favicon.ico" type="image/x-icon" rel="shortcut icon">
</head>
<body>
  <div class="center">
    <%@ include file="banner.jsp"%>
    <div class="main-column">

<h2><a href="/">Tor Metrics</a> &mdash; Unique .onion addresses</h2>
<br>
<p>The following graph shows the number of unique .onion addresses in
the network per day.
These numbers are extrapolated from aggregated statistics on unique
.onion addresses reported by single <a href="about.html#relay">relays</a>
acting as <a href="about.html#hidden-service">hidden-service</a>
directories, if at least 1% of relays reported these statistics.
For more details on the extrapolation algorithm, see <a
href="https://blog.torproject.org/blog/some-statistics-about-onions">this
blog post</a> and <a
href="https://research.torproject.org/techreports/extrapolating-hidserv-stats-2015-01-31.pdf">this
technical report</a>.</p>

<img src="hidserv-dir-onions-seen.png${hidserv_dir_onions_seen_url}"
     width="576" height="360" alt="Unique .onion addresses graph">
<form action="hidserv-dir-onions-seen.html">
  <div class="formrow">
    <input type="hidden" name="graph" value="hidserv-dir-onions-seen">
    <p>
    <label>Start date (yyyy-mm-dd):</label>
      <input type="text" name="start" size="10"
             value="<c:choose><c:when test="${fn:length(hidserv_dir_onions_seen_start) == 0}">${default_start_date}</c:when><c:otherwise>${hidserv_dir_onions_seen_start[0]}</c:otherwise></c:choose>">
    <label>End date (yyyy-mm-dd):</label>
      <input type="text" name="end" size="10"
             value="<c:choose><c:when test="${fn:length(hidserv_dir_onions_seen_end) == 0}">${default_end_date}</c:when><c:otherwise>${hidserv_dir_onions_seen_end[0]}</c:otherwise></c:choose>">
    </p><p>
    <input class="submit" type="submit" value="Update graph">
    </p>
  </div>
</form>
<p>Download graph as
<a href="hidserv-dir-onions-seen.pdf${hidserv_dir_onions_seen_url}">PDF</a> or
<a href="hidserv-dir-onions-seen.svg${hidserv_dir_onions_seen_url}">SVG</a>.</p>
<br>

<h4>Related metrics</h4>
<ul>
<li><a href="relayflags.html">Graph: Relays with Exit, Fast, Guard, Stable, and HSDir flags</a></li>
<li><a href="hidserv-data.html">Data: Hidden-service statistics</a></li>
</ul>

    </div>
  </div>
  <div class="bottom" id="bottom">
    <%@ include file="footer.jsp"%>
  </div>
</body>
</html>

