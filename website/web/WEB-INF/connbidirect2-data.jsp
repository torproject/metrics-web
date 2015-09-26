<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
  <title>Tor Metrics &mdash; Fraction of connections used uni-/bidirectionally</title>
  <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">
  <link href="/css/stylesheet-ltr.css" type="text/css" rel="stylesheet">
  <link href="/images/favicon.ico" type="image/x-icon" rel="shortcut icon">
</head>
<body>
  <div class="center">
    <%@ include file="banner.jsp"%>
    <div class="main-column">

<h2><a href="/"><img src="/images/metrics-wordmark-small.png" width="138" height="18" alt="Metrics wordmark"></a> &mdash; Fraction of connections used uni-/bidirectionally</h2>
<br>
<p>The following data file contains statistics on the fraction of direct
connections between a <a href="about.html#relay">relay</a> and other nodes
in the network that are used uni- or bidirectionally.
Every 10 seconds, relays determine for every direct connection whether
they read and wrote less than a threshold of 20 KiB.
Connections below this threshold are excluded from the statistics file.
For the remaining connections, relays determine whether they read/wrote at
least 10 times as many bytes as they wrote/read.
If so, they classify a connection as "mostly reading" or "mostly writing",
respectively.
All other connections are classified as "both reading and writing".
After classifying connections, read and write counters are reset for the
next 10-second interval.
The data file contains daily medians and quartiles of reported
fractions.</p>

<p><b>Download as <a href="stats/connbidirect2.csv">CSV file</a>.</b></p>

<p>The statistics file contains the following columns:</p>
<ul>
<li><b>date:</b> UTC date (YYYY-MM-DD) for which statistics on
uni-/bidirectional connection usage were reported.</li>
<li><b>direction:</b> Direction of reported fraction, which can be
<b>"read"</b>, <b>"write"</b>, or <b>"both"</b> for connections classified
as "mostly reading", "mostly writing", or "both reading as writing".
Connections below the threshold have been removed from this statistics
file entirely.</li>
<li><b>quantile:</b> Quantile of the reported fraction when considering
all statistics reported for this date.
Examples are <b>"0.5"</b> for the median and <b>"0.25"</b> and
<b>"0.75"</b> for the lower and upper quartile.</li>
<li><b>fraction:</b> Fraction of connections in percent for the given
date, direction, and quantile.
For each daily statistic reported by a relay, fractions for the three
directions "read", "write", and "both" sum up to exactly 100.</li>
</ul>

<h4>Related metrics</h4>
<ul>
<li><a href="connbidirect.html">Graph: Fraction of connections used uni-/bidirectionally</a></li>
</ul>

    </div>
  </div>
  <div class="bottom" id="bottom">
    <%@ include file="footer.jsp"%>
  </div>
</body>
</html>

