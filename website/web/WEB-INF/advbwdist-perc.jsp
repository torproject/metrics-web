<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
  <title>Tor Metrics &mdash; Advertised bandwidth distribution</title>
  <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">
  <link href="/css/stylesheet-ltr.css" type="text/css" rel="stylesheet">
  <link href="/images/favicon.ico" type="image/x-icon" rel="shortcut icon">
</head>
<body>
  <div class="center">
    <%@ include file="banner.jsp"%>
    <div class="main-column">

<h2><a href="/"><img src="/images/metrics-wordmark-small.png" width="138" height="18" alt="Metrics wordmark"></a> &mdash; Advertised bandwidth distribution</h2>
<br>
<p>The following graph shows the distribution of the
<a href="about.html#advertised-bandwidth">advertised bandwidth</a> of
relays in the network.
Each percentile represents the advertised bandwidth that a given
percentage of relays does not exceed (and that in turn the remaining
relays either match or exceed).
For example, 99% of relays advertise at most the bandwidth value shown in
the 99th percentile line (and the remaining 1% advertise at least that
amount).
<font color="red">Note that the unit has recently changed from MiB/s to
Gbit/s.</font></p>
<img src="advbwdist-perc.png${advbwdist_perc_url}"
     width="576" height="360"
     alt="Advertised bandwidth distribution graph">
<form action="advbwdist-perc.html">
  <div class="formrow">
    <input type="hidden" name="graph" value="advbwdist-perc">
    <p>
    <label>Start date (yyyy-mm-dd):
      <input type="text" name="start" size="10"
             value="<c:choose><c:when test="${fn:length(advbwdist_perc_start) == 0}">${default_start_date}</c:when><c:otherwise>${advbwdist_perc_start[0]}</c:otherwise></c:choose>"></label>
    <label>End date (yyyy-mm-dd):
      <input type="text" name="end" size="10"
             value="<c:choose><c:when test="${fn:length(advbwdist_perc_end) == 0}">${default_end_date}</c:when><c:otherwise>${advbwdist_perc_end[0]}</c:otherwise></c:choose>"></label>
    </p><p>
      <label>Percentiles: </label>
      <label class="checkbox-label"><input type="checkbox" name="p" value="100"<c:if test="${fn:length(advbwdist_perc_p) == 0 or fn:contains(fn:join(advbwdist_perc_p, ','), '100')}"> checked</c:if>> 100 (maximum)</label>
      <label class="checkbox-label"><input type="checkbox" name="p" value="99"<c:if test="${fn:length(advbwdist_perc_p) > 0 and fn:contains(fn:join(advbwdist_perc_p, ','), '99')}"> checked</c:if>> 99</label>
      <label class="checkbox-label"><input type="checkbox" name="p" value="98"<c:if test="${fn:length(advbwdist_perc_p) > 0 and fn:contains(fn:join(advbwdist_perc_p, ','), '98')}"> checked</c:if>> 98</label>
      <label class="checkbox-label"><input type="checkbox" name="p" value="97"<c:if test="${fn:length(advbwdist_perc_p) > 0 and fn:contains(fn:join(advbwdist_perc_p, ','), '97')}"> checked</c:if>> 97</label>
      <label class="checkbox-label"><input type="checkbox" name="p" value="95"<c:if test="${fn:length(advbwdist_perc_p) > 0 and fn:contains(fn:join(advbwdist_perc_p, ','), '95')}"> checked</c:if>> 95</label>
      <label class="checkbox-label"><input type="checkbox" name="p" value="91"<c:if test="${fn:length(advbwdist_perc_p) > 0 and fn:contains(fn:join(advbwdist_perc_p, ','), '91')}"> checked</c:if>> 91</label>
      <label class="checkbox-label"><input type="checkbox" name="p" value="90"<c:if test="${fn:length(advbwdist_perc_p) > 0 and fn:contains(fn:join(advbwdist_perc_p, ','), '90')}"> checked</c:if>> 90</label>
      <label class="checkbox-label"><input type="checkbox" name="p" value="80"<c:if test="${fn:length(advbwdist_perc_p) > 0 and fn:contains(fn:join(advbwdist_perc_p, ','), '80')}"> checked</c:if>> 80</label>
      <label class="checkbox-label"><input type="checkbox" name="p" value="75"<c:if test="${fn:length(advbwdist_perc_p) > 0 and fn:contains(fn:join(advbwdist_perc_p, ','), '75')}"> checked</c:if>> 75 (3rd quartile)</label>
      <label class="checkbox-label"><input type="checkbox" name="p" value="70"<c:if test="${fn:length(advbwdist_perc_p) > 0 and fn:contains(fn:join(advbwdist_perc_p, ','), '70')}"> checked</c:if>> 70</label>
      <label class="checkbox-label"><input type="checkbox" name="p" value="60"<c:if test="${fn:length(advbwdist_perc_p) > 0 and fn:contains(fn:join(advbwdist_perc_p, ','), '60')}"> checked</c:if>> 60</label>
      <label class="checkbox-label"><input type="checkbox" name="p" value="50"<c:if test="${fn:length(advbwdist_perc_p) > 0 and fn:contains(fn:join(advbwdist_perc_p, ','), '50')}"> checked</c:if>> 50 (median)</label>
      <label class="checkbox-label"><input type="checkbox" name="p" value="40"<c:if test="${fn:length(advbwdist_perc_p) > 0 and fn:contains(fn:join(advbwdist_perc_p, ','), '40')}"> checked</c:if>> 40</label>
      <label class="checkbox-label"><input type="checkbox" name="p" value="30"<c:if test="${fn:length(advbwdist_perc_p) > 0 and fn:contains(fn:join(advbwdist_perc_p, ','), '30')}"> checked</c:if>> 30</label>
      <label class="checkbox-label"><input type="checkbox" name="p" value="25"<c:if test="${fn:length(advbwdist_perc_p) > 0 and fn:contains(fn:join(advbwdist_perc_p, ','), '25')}"> checked</c:if>> 25 (first quartile)</label>
      <label class="checkbox-label"><input type="checkbox" name="p" value="20"<c:if test="${fn:length(advbwdist_perc_p) > 0 and fn:contains(fn:join(advbwdist_perc_p, ','), '20')}"> checked</c:if>> 20</label>
      <label class="checkbox-label"><input type="checkbox" name="p" value="10"<c:if test="${fn:length(advbwdist_perc_p) > 0 and (fn:startsWith(fn:join(advbwdist_perc_p, ','), '10,') or fn:contains(fn:join(advbwdist_perc_p, ','), ',10,') or fn:endsWith(fn:join(advbwdist_perc_p, ','), ',10') or (advbwdist_perc_p[0] == '10' and fn:length(advbwdist_perc_p) == 1))}"> checked</c:if>> 10</label>
      <label class="checkbox-label"><input type="checkbox" name="p" value="9"<c:if test="${fn:length(advbwdist_perc_p) > 0 and (fn:startsWith(fn:join(advbwdist_perc_p, ','), '9,') or fn:contains(fn:join(advbwdist_perc_p, ','), ',9,') or fn:endsWith(fn:join(advbwdist_perc_p, ','), ',9') or (advbwdist_perc_p[0] == '9' and fn:length(advbwdist_perc_p) == 1))}"> checked</c:if>> 9</label>
      <label class="checkbox-label"><input type="checkbox" name="p" value="5"<c:if test="${fn:length(advbwdist_perc_p) > 0 and (fn:startsWith(fn:join(advbwdist_perc_p, ','), '5,') or fn:contains(fn:join(advbwdist_perc_p, ','), ',5,') or fn:endsWith(fn:join(advbwdist_perc_p, ','), ',5') or (advbwdist_perc_p[0] == '5' and fn:length(advbwdist_perc_p) == 1))}"> checked</c:if>> 5</label>
      <label class="checkbox-label"><input type="checkbox" name="p" value="3"<c:if test="${fn:length(advbwdist_perc_p) > 0 and (fn:startsWith(fn:join(advbwdist_perc_p, ','), '3,') or fn:contains(fn:join(advbwdist_perc_p, ','), ',3,') or fn:endsWith(fn:join(advbwdist_perc_p, ','), ',3') or (advbwdist_perc_p[0] == '3' and fn:length(advbwdist_perc_p) == 1))}"> checked</c:if>> 3</label>
      <label class="checkbox-label"><input type="checkbox" name="p" value="2"<c:if test="${fn:length(advbwdist_perc_p) > 0 and (fn:startsWith(fn:join(advbwdist_perc_p, ','), '2,') or fn:contains(fn:join(advbwdist_perc_p, ','), ',2,') or fn:endsWith(fn:join(advbwdist_perc_p, ','), ',2') or (advbwdist_perc_p[0] == '2' and fn:length(advbwdist_perc_p) == 1))}"> checked</c:if>> 2</label>
      <label class="checkbox-label"><input type="checkbox" name="p" value="1"<c:if test="${fn:length(advbwdist_perc_p) > 0 and (fn:startsWith(fn:join(advbwdist_perc_p, ','), '1,') or fn:contains(fn:join(advbwdist_perc_p, ','), ',1,') or fn:endsWith(fn:join(advbwdist_perc_p, ','), ',1') or (advbwdist_perc_p[0] == '1' and fn:length(advbwdist_perc_p) == 1))}"> checked</c:if>> 1</label>
      <label class="checkbox-label"><input type="checkbox" name="p" value="0"<c:if test="${fn:length(advbwdist_perc_p) > 0 and (fn:startsWith(fn:join(advbwdist_perc_p, ','), '0,') or fn:contains(fn:join(advbwdist_perc_p, ','), ',0,') or fn:endsWith(fn:join(advbwdist_perc_p, ','), ',0') or (advbwdist_perc_p[0] == '0' and fn:length(advbwdist_perc_p) == 1))}"> checked</c:if>> 0 (minimum)</label>
    </p><p>
    <input class="submit" type="submit" value="Update graph">
    </p>
  </div>
</form>
<p>Download graph as
<a href="advbwdist-perc.pdf${advbwdist_perc_url}">PDF</a> or
<a href="advbwdist-perc.svg${advbwdist_perc_url}">SVG</a>.</p>
<br>

<h4>Related metrics</h4>
<ul>
<li><a href="advbwdist-relay.html">Graph: Advertised bandwidth of n-th fastest relays</a></li>
<li><a href="advbwdist-data.html">Data: Advertised bandwidth distribution and n-th fastest relays</a></li>
</ul>

    </div>
  </div>
  <div class="bottom" id="bottom">
    <%@ include file="footer.jsp"%>
  </div>
</body>
</html>
