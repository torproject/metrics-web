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
<p>The following data file contains statistics on the fraction of direct
connections between a <a href="about.html#relay">relay</a> and other nodes
in the network that are used uni- or bidirectionally.
Every 10 seconds, relays determine for every direct connection whether
they read and wrote less than a threshold of 20 KiB.
For the remaining connections, relays determine whether they read/wrote at
least 10 times as many bytes as they wrote/read.
If so, they classify a connection as "mostly reading" or "mostly writing",
respectively.
All other connections are classified as "both reading and writing".
After classifying connections, read and write counters are reset for the
next 10-second interval.
The data file contains the absolute number of 10-second intervals per
relay, aggregated over 24-hour periods.</p>

<p><b>Download as <a href="stats/connbidirect.csv">CSV file</a>.</b></p>

<p>The statistics file contains the following columns:</p>
<ul>
<li><b>date:</b> UTC date (YYYY-MM-DD) for which statistics on
uni-/bidirectional connection usage were reported.</li>
<li><b>source:</b> Fingerprint of the relay reporting statistics.</li>
<li><b>below:</b> Number of 10-second intervals of connections with less
than 20 KiB read and written data.</li>
<li><b>read:</b> Number of 10-second intervals of connections with 10
times as many read bytes as written bytes.</li>
<li><b>write:</b> Number of 10-second intervals of connections with 10
times as many written bytes as read bytes.</li>
<li><b>both:</b> Number of 10-second intervals of connections with less
than 10 times as many written or read bytes as in the other
direction.</li>
</ul>

    </div>
  </div>
  <div class="bottom" id="bottom">
    <%@ include file="footer.jsp"%>
  </div>
</body>
</html>

