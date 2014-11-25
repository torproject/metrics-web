<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
  <title>Tor Metrics: Advertised bandwidth distribution and n-th fastest relays</title>
  <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">
  <link href="/css/stylesheet-ltr.css" type="text/css" rel="stylesheet">
  <link href="/images/favicon.ico" type="image/x-icon" rel="shortcut icon">
</head>
<body>
  <div class="center">
    <%@ include file="banner.jsp"%>
    <div class="main-column">

<h3>Tor Metrics: Advertised bandwidth distribution and n-th fastest
relays</h3>
<br>
<p>The following data file contains statistics on the distribution of
<a href="about.html#advertised-bandwidth">advertised bandwidth</a> of
relays in the network.
These statistics include advertised bandwidth percentiles and advertised
bandwidth values of the n-th fastest relays.
All values are obtained from advertised bandwidths of running relays in a
<a href="about.html#consensus">network status consensus</a>.
The data file contains daily (median) averages of percentiles and n-th
largest values.</p>

<p><b>Download as <a href="stats/advbwdist.csv">CSV file</a>.</b></p>

<p>The statistics file contains the following columns:</p>
<ul>
<li><b>date:</b> UTC date (YYYY-MM-DD) when relays have been listed as
running.</li>
<li><b>isexit:</b> Whether relays included in this line have the
<b>"Exit"</b> relay flag, which would be indicated as <b>"t"</b>.
If this column contains the empty string, advertised bandwidths from all
running relays are included, regardless of assigned relay flags.</li>
<li><b>relay:</b> Position of the relay in an ordered list of all
advertised bandwidths, starting at 1 for the fastest relay in the network.
May be the empty string if this line contains advertised bandwidth by
percentile.</li>
<li><b>percentile:</b> Advertised bandwidth percentile given in this line.
May be the empty string if this line contains advertised bandwidth by
fastest relays.</li>
<li><b>advbw:</b> Advertised bandwidth in B/s.</li>
</ul>

<h4>Related metrics</h4>
<ul>
<li><a href="advbwdist-perc.html">Graph: Advertised bandwidth distribution</a></li>
<li><a href="advbwdist-relay.html">Graph: Advertised bandwidth of n-th fastest relays</a></li>
</ul>

    </div>
  </div>
  <div class="bottom" id="bottom">
    <%@ include file="footer.jsp"%>
  </div>
</body>
</html>

