<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
  <title>Tor Metrics: Bandwidth</title>
  <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">
  <link href="/css/stylesheet-ltr.css" type="text/css" rel="stylesheet">
  <link href="/images/favicon.ico" type="image/x-icon" rel="shortcut icon">
</head>
<body>
  <div class="center">
    <%@ include file="banner.jsp"%>
    <div class="main-column">
<h2>Tor Metrics: Bandwidth</h2>
<br>

<a name="bandwidth"></a>
<h3><a href="#bandwidth" class="anchor">Total relay bandwidth in the
network</a></h3>
<br>
<p>Relays report how much bandwidth they are willing to contribute and how
many bytes they have read and written in the past 24 hours. The following
graph shows total advertised bandwidth and bandwidth history of all relays
in the network.</p>
<img src="bandwidth.png${bandwidth_url}"
     width="576" height="360" alt="Relay bandwidth graph">
<form action="network.html#bandwidth">
  <div class="formrow">
    <input type="hidden" name="graph" value="bandwidth">
    <p>
    <label>Start date (yyyy-mm-dd):</label>
      <input type="text" name="start" size="10"
             value="<c:choose><c:when test="${fn:length(bandwidth_start) == 0}">${default_start_date}</c:when><c:otherwise>${bandwidth_start[0]}</c:otherwise></c:choose>">
    <label>End date (yyyy-mm-dd):</label>
      <input type="text" name="end" size="10"
             value="<c:choose><c:when test="${fn:length(bandwidth_end) == 0}">${default_end_date}</c:when><c:otherwise>${bandwidth_end[0]}</c:otherwise></c:choose>">
    </p><p>
    <input class="submit" type="submit" value="Update graph">
    </p>
  </div>
</form>
<p>Download graph as
<a href="bandwidth.pdf${bandwidth_url}">PDF</a> or
<a href="bandwidth.svg${bandwidth_url}">SVG</a>.</p>
<p><a href="stats/bandwidth.csv">CSV</a> file containing all data.</p>
<br>

<a name="bwhist-flags"></a>
<h3><a href="#bwhist-flags" class="anchor">Relay bandwidth by Exit and/or
Guard flags</a></h3>
<br>
<p>The following graph shows the relay bandwidth of all relays with the
Exit and/or Guard flags assigned by the directory authorities.</p>
<img src="bwhist-flags.png${bwhist_flags_url}"
     width="576" height="360" alt="Relay bandwidth by flags graph">
<form action="network.html#bwhist-flags">
  <div class="formrow">
    <input type="hidden" name="graph" value="bwhist-flags">
    <p>
    <label>Start date (yyyy-mm-dd):</label>
      <input type="text" name="start" size="10"
             value="<c:choose><c:when test="${fn:length(bwhist_flags_start) == 0}">${default_start_date}</c:when><c:otherwise>${bwhist_flags_start[0]}</c:otherwise></c:choose>">
    <label>End date (yyyy-mm-dd):</label>
      <input type="text" name="end" size="10"
             value="<c:choose><c:when test="${fn:length(bwhist_flags_end) == 0}">${default_end_date}</c:when><c:otherwise>${bwhist_flags_end[0]}</c:otherwise></c:choose>">
    </p><p>
    <input class="submit" type="submit" value="Update graph">
    </p>
  </div>
</form>
<p>Download graph as
<a href="bwhist-flags.pdf${bwhist_flags_url}">PDF</a> or
<a href="bwhist-flags.svg${bwhist_flags_url}">SVG</a>.</p>
<p><a href="stats/bandwidth.csv">CSV</a> file containing all data.</p>
<br>

<a name="bandwidth-flags"></a>
<h3><a href="#bandwidth-flags" class="anchor">Advertised bandwidth and
bandwidth history by relay flags</a></h3>
<br>
<p>The following graph shows the advertised bandwidth and bandwidth
history of all relays with the Exit and/or Guard flags assigned by the
directory authorities.
Note that these sets possibly overlap with relays having both Exit and
Guard flag.</p>
<img src="bandwidth-flags.png${bandwidth_flags_url}"
     width="576" height="360" alt="Advertised bandwidth and bandwidth history by relay flags graph">
<form action="network.html#bandwidth-flags">
  <div class="formrow">
    <input type="hidden" name="graph" value="bandwidth-flags">
    <p>
    <label>Start date (yyyy-mm-dd):</label>
      <input type="text" name="start" size="10"
             value="<c:choose><c:when test="${fn:length(bandwidth_flags_start) == 0}">${default_start_date}</c:when><c:otherwise>${bandwidth_flags_start[0]}</c:otherwise></c:choose>">
    <label>End date (yyyy-mm-dd):</label>
      <input type="text" name="end" size="10"
             value="<c:choose><c:when test="${fn:length(bandwidth_flags_end) == 0}">${default_end_date}</c:when><c:otherwise>${bandwidth_flags_end[0]}</c:otherwise></c:choose>">
    </p><p>
    <input class="submit" type="submit" value="Update graph">
    </p>
  </div>
</form>
<p>Download graph as
<a href="bandwidth-flags.pdf${bandwidth_flags_url}">PDF</a> or
<a href="bandwidth-flags.svg${bandwidth_flags_url}">SVG</a>.</p>
<p><a href="stats/bandwidth.csv">CSV</a> file containing all data.</p>
<br>

<a name="dirbytes"></a>
<h3><a href="#dirbytes" class="anchor">Number of bytes spent on answering
directory requests</a></h3>
<br>
<p>Relays running on 0.2.2.15-alpha or higher report the number of bytes
they spend on answering directory requests. The following graph shows
total written and read bytes as well as written and read dir bytes. The
dir bytes are extrapolated from those relays who report them to reflect
the number of written and read dir bytes by all relays.</p>
<img src="dirbytes.png${dirbytes_url}"
     width="576" height="360" alt="Dir bytes graph">
<form action="network.html#dirbytes">
  <div class="formrow">
    <input type="hidden" name="graph" value="dirbytes">
    <p>
    <label>Start date (yyyy-mm-dd):</label>
      <input type="text" name="start" size="10"
             value="<c:choose><c:when test="${fn:length(dirbytes_start) == 0}">${default_start_date}</c:when><c:otherwise>${dirbytes_start[0]}</c:otherwise></c:choose>">
    <label>End date (yyyy-mm-dd):</label>
      <input type="text" name="end" size="10"
             value="<c:choose><c:when test="${fn:length(dirbytes_end) == 0}">${default_end_date}</c:when><c:otherwise>${dirbytes_end[0]}</c:otherwise></c:choose>">
    </p><p>
    <input class="submit" type="submit" value="Update graph">
    </p>
  </div>
</form>
<p>Download graph as
<a href="dirbytes.pdf${dirbytes_url}">PDF</a> or
<a href="dirbytes.svg${dirbytes_url}">SVG</a>.</p>
<p><a href="stats/bandwidth.csv">CSV</a> file containing all data.</p>
<br>

<a name="advbwdist-perc"></a>
<h3><a href="#advbwdist-perc" class="anchor">Advertised bandwidth
distribution</a></h3>
<br>
<p>The following graph shows the distribution of advertised bandwidth in
the network. In contrast to the graphs above, the following graph contains
no sums of advertised bandwidths, but bandwidths of single relays.</p>
<img src="advbwdist-perc.png${advbwdist_perc_url}"
     width="576" height="360"
     alt="Advertised bandwidth distribution graph">
<form action="network.html#advbwdist-perc">
  <div class="formrow">
    <input type="hidden" name="graph" value="advbwdist-perc">
    <p>
    <label>Start date (yyyy-mm-dd):</label>
      <input type="text" name="start" size="10"
             value="<c:choose><c:when test="${fn:length(advbwdist_perc_start) == 0}">${default_start_date}</c:when><c:otherwise>${advbwdist_perc_start[0]}</c:otherwise></c:choose>">
    <label>End date (yyyy-mm-dd):</label>
      <input type="text" name="end" size="10"
             value="<c:choose><c:when test="${fn:length(advbwdist_perc_end) == 0}">${default_end_date}</c:when><c:otherwise>${advbwdist_perc_end[0]}</c:otherwise></c:choose>">
    </p><p>
      <label>Percentiles: </label>
      <input type="checkbox" name="p" value="100"<c:if test="${fn:length(advbwdist_perc_p) == 0 or fn:contains(fn:join(advbwdist_perc_p, ','), '100')}"> checked</c:if>> 100 (maximum)
      <input type="checkbox" name="p" value="99"<c:if test="${fn:length(advbwdist_perc_p) > 0 and fn:contains(fn:join(advbwdist_perc_p, ','), '99')}"> checked</c:if>> 99
      <input type="checkbox" name="p" value="98"<c:if test="${fn:length(advbwdist_perc_p) > 0 and fn:contains(fn:join(advbwdist_perc_p, ','), '98')}"> checked</c:if>> 98
      <input type="checkbox" name="p" value="97"<c:if test="${fn:length(advbwdist_perc_p) > 0 and fn:contains(fn:join(advbwdist_perc_p, ','), '97')}"> checked</c:if>> 97
      <input type="checkbox" name="p" value="95"<c:if test="${fn:length(advbwdist_perc_p) > 0 and fn:contains(fn:join(advbwdist_perc_p, ','), '95')}"> checked</c:if>> 95
      <input type="checkbox" name="p" value="91"<c:if test="${fn:length(advbwdist_perc_p) > 0 and fn:contains(fn:join(advbwdist_perc_p, ','), '91')}"> checked</c:if>> 91
      <input type="checkbox" name="p" value="90"<c:if test="${fn:length(advbwdist_perc_p) > 0 and fn:contains(fn:join(advbwdist_perc_p, ','), '90')}"> checked</c:if>> 90
      <input type="checkbox" name="p" value="80"<c:if test="${fn:length(advbwdist_perc_p) > 0 and fn:contains(fn:join(advbwdist_perc_p, ','), '80')}"> checked</c:if>> 80
      <input type="checkbox" name="p" value="75"<c:if test="${fn:length(advbwdist_perc_p) > 0 and fn:contains(fn:join(advbwdist_perc_p, ','), '75')}"> checked</c:if>> 75 (3rd quartile)
      <input type="checkbox" name="p" value="70"<c:if test="${fn:length(advbwdist_perc_p) > 0 and fn:contains(fn:join(advbwdist_perc_p, ','), '70')}"> checked</c:if>> 70
      <input type="checkbox" name="p" value="60"<c:if test="${fn:length(advbwdist_perc_p) > 0 and fn:contains(fn:join(advbwdist_perc_p, ','), '60')}"> checked</c:if>> 60
      <input type="checkbox" name="p" value="50"<c:if test="${fn:length(advbwdist_perc_p) > 0 and fn:contains(fn:join(advbwdist_perc_p, ','), '50')}"> checked</c:if>> 50 (median)
      <input type="checkbox" name="p" value="40"<c:if test="${fn:length(advbwdist_perc_p) > 0 and fn:contains(fn:join(advbwdist_perc_p, ','), '40')}"> checked</c:if>> 40
      <input type="checkbox" name="p" value="30"<c:if test="${fn:length(advbwdist_perc_p) > 0 and fn:contains(fn:join(advbwdist_perc_p, ','), '30')}"> checked</c:if>> 30
      <input type="checkbox" name="p" value="25"<c:if test="${fn:length(advbwdist_perc_p) > 0 and fn:contains(fn:join(advbwdist_perc_p, ','), '25')}"> checked</c:if>> 25 (first quartile)
      <input type="checkbox" name="p" value="20"<c:if test="${fn:length(advbwdist_perc_p) > 0 and fn:contains(fn:join(advbwdist_perc_p, ','), '20')}"> checked</c:if>> 20
      <input type="checkbox" name="p" value="10"<c:if test="${fn:length(advbwdist_perc_p) > 0 and (fn:startsWith(fn:join(advbwdist_perc_p, ','), '10,') or fn:contains(fn:join(advbwdist_perc_p, ','), ',10,') or fn:endsWith(fn:join(advbwdist_perc_p, ','), ',10') or (advbwdist_perc_p[0] == '10' and fn:length(advbwdist_perc_p) == 1))}"> checked</c:if>> 10
      <input type="checkbox" name="p" value="9"<c:if test="${fn:length(advbwdist_perc_p) > 0 and (fn:startsWith(fn:join(advbwdist_perc_p, ','), '9,') or fn:contains(fn:join(advbwdist_perc_p, ','), ',9,') or fn:endsWith(fn:join(advbwdist_perc_p, ','), ',9') or (advbwdist_perc_p[0] == '9' and fn:length(advbwdist_perc_p) == 1))}"> checked</c:if>> 9
      <input type="checkbox" name="p" value="5"<c:if test="${fn:length(advbwdist_perc_p) > 0 and (fn:startsWith(fn:join(advbwdist_perc_p, ','), '5,') or fn:contains(fn:join(advbwdist_perc_p, ','), ',5,') or fn:endsWith(fn:join(advbwdist_perc_p, ','), ',5') or (advbwdist_perc_p[0] == '5' and fn:length(advbwdist_perc_p) == 1))}"> checked</c:if>> 5
      <input type="checkbox" name="p" value="3"<c:if test="${fn:length(advbwdist_perc_p) > 0 and (fn:startsWith(fn:join(advbwdist_perc_p, ','), '3,') or fn:contains(fn:join(advbwdist_perc_p, ','), ',3,') or fn:endsWith(fn:join(advbwdist_perc_p, ','), ',3') or (advbwdist_perc_p[0] == '3' and fn:length(advbwdist_perc_p) == 1))}"> checked</c:if>> 3
      <input type="checkbox" name="p" value="2"<c:if test="${fn:length(advbwdist_perc_p) > 0 and (fn:startsWith(fn:join(advbwdist_perc_p, ','), '2,') or fn:contains(fn:join(advbwdist_perc_p, ','), ',2,') or fn:endsWith(fn:join(advbwdist_perc_p, ','), ',2') or (advbwdist_perc_p[0] == '2' and fn:length(advbwdist_perc_p) == 1))}"> checked</c:if>> 2
      <input type="checkbox" name="p" value="1"<c:if test="${fn:length(advbwdist_perc_p) > 0 and (fn:startsWith(fn:join(advbwdist_perc_p, ','), '1,') or fn:contains(fn:join(advbwdist_perc_p, ','), ',1,') or fn:endsWith(fn:join(advbwdist_perc_p, ','), ',1') or (advbwdist_perc_p[0] == '1' and fn:length(advbwdist_perc_p) == 1))}"> checked</c:if>> 1
      <input type="checkbox" name="p" value="0"<c:if test="${fn:length(advbwdist_perc_p) > 0 and (fn:startsWith(fn:join(advbwdist_perc_p, ','), '0,') or fn:contains(fn:join(advbwdist_perc_p, ','), ',0,') or fn:endsWith(fn:join(advbwdist_perc_p, ','), ',0') or (advbwdist_perc_p[0] == '0' and fn:length(advbwdist_perc_p) == 1))}"> checked</c:if>> 0 (minimum)
    </p><p>
    <input class="submit" type="submit" value="Update graph">
    </p>
  </div>
</form>
<p>Download graph as
<a href="advbwdist-perc.pdf${advbwdist_perc_url}">PDF</a> or
<a href="advbwdist-perc.svg${advbwdist_perc_url}">SVG</a>.</p>
<p><a href="stats/advbwdist.csv">CSV</a> file containing all data.</p>
<br>

<a name="advbwdist-relay"></a>
<h3><a href="#advbwdist-relay" class="anchor">Advertised bandwidth of
n-th fastest relays</a></h3>
<br>
<p>The following graph shows the advertised bandwidth of the n-th fastest
relays in the network.</p>
<img src="advbwdist-relay.png${advbwdist_relay_url}"
     width="576" height="360"
     alt="Advertised bandwidth of n-th fastest relays graph">
<form action="network.html#advbwdist-relay">
  <div class="formrow">
    <input type="hidden" name="graph" value="advbwdist-relay">
    <p>
    <label>Start date (yyyy-mm-dd):</label>
      <input type="text" name="start" size="10"
             value="<c:choose><c:when test="${fn:length(advbwdist_relay_start) == 0}">${default_start_date}</c:when><c:otherwise>${advbwdist_relay_start[0]}</c:otherwise></c:choose>">
    <label>End date (yyyy-mm-dd):</label>
      <input type="text" name="end" size="10"
             value="<c:choose><c:when test="${fn:length(advbwdist_relay_end) == 0}">${default_end_date}</c:when><c:otherwise>${advbwdist_relay_end[0]}</c:otherwise></c:choose>">
    </p><p>
      <label>n-th fastest relays: </label>
      <input type="checkbox" name="n" value="1"<c:if test="${fn:length(advbwdist_relay_n) == 0 or fn:contains(fn:join(advbwdist_relay_n, ','), '1,') or fn:endsWith(fn:join(advbwdist_relay_n, ','), '1')}"> checked</c:if>> 1
      <input type="checkbox" name="n" value="2"<c:if test="${fn:length(advbwdist_relay_n) > 0 and (fn:contains(fn:join(advbwdist_relay_n, ','), '2,') or fn:endsWith(fn:join(advbwdist_relay_n, ','), '2'))}"> checked</c:if>> 2
      <input type="checkbox" name="n" value="3"<c:if test="${fn:length(advbwdist_relay_n) > 0 and (fn:contains(fn:join(advbwdist_relay_n, ','), '3,') or fn:endsWith(fn:join(advbwdist_relay_n, ','), '3'))}"> checked</c:if>> 3
      <input type="checkbox" name="n" value="5"<c:if test="${fn:length(advbwdist_relay_n) > 0 and (fn:contains(fn:join(advbwdist_relay_n, ','), '5,') or fn:endsWith(fn:join(advbwdist_relay_n, ','), '5'))}"> checked</c:if>> 5
      <input type="checkbox" name="n" value="10"<c:if test="${fn:length(advbwdist_relay_n) > 0 and (fn:contains(fn:join(advbwdist_relay_n, ','), '10,') or fn:endsWith(fn:join(advbwdist_relay_n, ','), '10'))}"> checked</c:if>> 10
      <input type="checkbox" name="n" value="20"<c:if test="${fn:length(advbwdist_relay_n) > 0 and (fn:contains(fn:join(advbwdist_relay_n, ','), '20,') or fn:endsWith(fn:join(advbwdist_relay_n, ','), '20'))}"> checked</c:if>> 20
      <input type="checkbox" name="n" value="30"<c:if test="${fn:length(advbwdist_relay_n) > 0 and (fn:contains(fn:join(advbwdist_relay_n, ','), '30,') or fn:endsWith(fn:join(advbwdist_relay_n, ','), '30'))}"> checked</c:if>> 30
      <input type="checkbox" name="n" value="50"<c:if test="${fn:length(advbwdist_relay_n) > 0 and (fn:contains(fn:join(advbwdist_relay_n, ','), '50,') or fn:endsWith(fn:join(advbwdist_relay_n, ','), '50'))}"> checked</c:if>> 50
      <input type="checkbox" name="n" value="100"<c:if test="${fn:length(advbwdist_relay_n) > 0 and (fn:contains(fn:join(advbwdist_relay_n, ','), '100,') or fn:endsWith(fn:join(advbwdist_relay_n, ','), '100'))}"> checked</c:if>> 100
      <input type="checkbox" name="n" value="200"<c:if test="${fn:length(advbwdist_relay_n) > 0 and (fn:contains(fn:join(advbwdist_relay_n, ','), '200,') or fn:endsWith(fn:join(advbwdist_relay_n, ','), '200'))}"> checked</c:if>> 200
      <input type="checkbox" name="n" value="300"<c:if test="${fn:length(advbwdist_relay_n) > 0 and (fn:contains(fn:join(advbwdist_relay_n, ','), '300,') or fn:endsWith(fn:join(advbwdist_relay_n, ','), '300'))}"> checked</c:if>> 300
      <input type="checkbox" name="n" value="500"<c:if test="${fn:length(advbwdist_relay_n) > 0 and (fn:contains(fn:join(advbwdist_relay_n, ','), '500,') or fn:endsWith(fn:join(advbwdist_relay_n, ','), '500'))}"> checked</c:if>> 500
      <input type="checkbox" name="n" value="1000"<c:if test="${fn:length(advbwdist_relay_n) > 0 and fn:contains(fn:join(advbwdist_relay_n, ','), '1000')}"> checked</c:if>> 1000
      <input type="checkbox" name="n" value="2000"<c:if test="${fn:length(advbwdist_relay_n) > 0 and fn:contains(fn:join(advbwdist_relay_n, ','), '2000')}"> checked</c:if>> 2000
      <input type="checkbox" name="n" value="3000"<c:if test="${fn:length(advbwdist_relay_n) > 0 and fn:contains(fn:join(advbwdist_relay_n, ','), '3000')}"> checked</c:if>> 3000
      <input type="checkbox" name="n" value="5000"<c:if test="${fn:length(advbwdist_relay_n) > 0 and fn:contains(fn:join(advbwdist_relay_n, ','), '5000')}"> checked</c:if>> 5000
    </p><p>
    <input class="submit" type="submit" value="Update graph">
    </p>
  </div>
</form>
<p>Download graph as
<a href="advbwdist-relay.pdf${advbwdist_relay_url}">PDF</a> or
<a href="advbwdist-relay.svg${advbwdist_relay_url}">SVG</a>.</p>
<p><a href="stats/advbwdist.csv">CSV</a> file containing all data.</p>
<br>
    </div>
  </div>
  <div class="bottom" id="bottom">
    <%@ include file="footer.jsp"%>
  </div>
</body>
</html>
