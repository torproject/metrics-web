<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
  <title>Tor Metrics &mdash; Bandwidth provided and consumed by relays</title>
  <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">
  <link href="/css/stylesheet-ltr.css" type="text/css" rel="stylesheet">
  <link href="/images/favicon.ico" type="image/x-icon" rel="shortcut icon">
</head>
<body>
  <div class="center">
    <%@ include file="banner.jsp"%>
    <div class="main-column">

<h2><a href="/"><img src="/images/metrics-wordmark-small.png" width="138" height="18" alt="Metrics wordmark"></a> &mdash; Bandwidth provided and consumed by relays</h2>
<br>
<p>The following data file contains statistics on
<a href="about.html#advertised-bandwidth">advertised</a> and
<a href="about.html#bandwidth-history">consumed bandwidth</a> of
<a href="about.html#relay">relays</a> in the network.
Statistics on advertised bandwidth include any kind of traffic handled by
a relay, whereas statistics on consumed bandwidth are available either for
all traffic combined, or specifically for directory traffic.
Some of the statistics are available for subsets of relays that have the
"Exit" and/or the "Guard" <a href="about.html#relay-flag">flag</a>.
The data file contains daily (mean) averages of bandwidth numbers.</p>

<p><b>Download as <a href="stats/bandwidth.csv">CSV file</a>.</b></p>

<p>The statistics file contains the following columns:</p>
<ul>
<li><b>date:</b> UTC date (YYYY-MM-DD) that relays reported bandwidth data
for.</li>
<li><b>isexit:</b> Whether relays included in this line have the
<b>"Exit"</b> relay flag or not, which can be <b>"t"</b> or <b>"f"</b>.
If this column contains the empty string, bandwidth data from all running
relays are included, regardless of assigned relay flags.</li>
<li><b>isguard:</b> Whether relays included in this line have the
<b>"Guard"</b> relay flag or not, which can be <b>"t"</b> or <b>"f"</b>.
If this column contains the empty string, bandwidth data from all running
relays are included, regardless of assigned relay flags.</li>
<li><b>advbw:</b> Total advertised bandwidth in bytes per second that
relays are capable to provide.</li>
<li><b>bwread:</b> Total bandwidth in bytes per second that relays have
read.
This metric includes any kind of traffic.</li>
<li><b>bwwrite:</b> Similar to <b>bwread</b>, but for traffic written by
relays.</li>
<li><b>dirread:</b> Bandwidth in bytes per second that relays have read
when serving directory data.
Not all relays report how many bytes they read when serving directory data
which is why this value is an estimate from the available data.
This metric is not available for subsets of relays with certain relay
flags, so that this column will contain the empty string if either
<b>isexit</b> or <b>isguard</b> is non-empty.</li>
<li><b>dirwrite:</b> Similar to <b>dirread</b>, but for traffic written by
relays when serving directory data.</li>
</ul>

<h4>Related metrics</h4>
<ul>
<li><a href="bandwidth.html">Graph: Total relay bandwidth in the network</a></li>
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

