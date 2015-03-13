<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
  <title>Tor Metrics &mdash; Hidden-service statistics</title>
  <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">
  <link href="/css/stylesheet-ltr.css" type="text/css" rel="stylesheet">
  <link href="/images/favicon.ico" type="image/x-icon" rel="shortcut icon">
</head>
<body>
  <div class="center">
    <%@ include file="banner.jsp"%>
    <div class="main-column">

<h2><a href="/">Tor Metrics</a> &mdash; Hidden-service statistics</h2>
<br>
<p>
The following data file contains
<a href="about.html#hidden-service">hidden-service</a> statistics
gathered by a small subset of <a href="about.html#relay">relays</a> and
extrapolated to network totals.
Statistics include the amount of hidden-service traffic and the number
of hidden-service addresses in the network per day.
For more details on the extrapolation algorithm, see <a
href="https://blog.torproject.org/blog/some-statistics-about-onions">this
blog post</a> and <a
href="https://research.torproject.org/techreports/extrapolating-hidserv-stats-2015-01-31.pdf">this
technical report</a>.</p>

<p><b>Download as <a href="stats/hidserv.csv">CSV file</a>.</b></p>

<p>The statistics file contains the following columns:</p>
<ul>
<li><b>date:</b> UTC date (YYYY-MM-DD) when relays or bridges have been
listed as running.</li>
<li><b>type:</b> Type of hidden-service statistic reported by relays and
extrapolated to network totals.
Examples include <b>"rend-relayed-cells"</b> for the number of cells on
rendezvous circuits observed by rendezvous points and
<b>"dir-onions-seen"</b> for the number of unique .onion addresses
observed by hidden-service directories.</li>
<li><b>wmean:</b> Weighted mean of extrapolated network totals.</li>
<li><b>wmedian:</b> Weighted median of extrapolated network totals.</li>
<li><b>wiqm:</b> Weighted interquartile mean of extrapolated network
totals.</li>
<li><b>frac:</b> Total network fraction of reported statistics.</li>
<li><b>stats:</b> Number of reported statistics with non-zero computed
network fraction.</li>
</ul>

<h4>Related metrics</h4>
<ul>
<li><a href="hidserv-dir-onions-seen.html">Graph: Unique .onion addresses</a></li>
<li><a href="relayflags.html">Graph: Relays with Exit, Fast, Guard, Stable, and HSDir flags</a></li>
</ul>

    </div>
  </div>
  <div class="bottom" id="bottom">
    <%@ include file="footer.jsp"%>
  </div>
</body>
</html>

