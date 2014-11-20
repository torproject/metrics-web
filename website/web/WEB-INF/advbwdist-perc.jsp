<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
  <title>Tor Metrics: Advertised bandwidth distribution</title>
  <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">
  <link href="/css/stylesheet-ltr.css" type="text/css" rel="stylesheet">
  <link href="/images/favicon.ico" type="image/x-icon" rel="shortcut icon">
</head>
<body>
  <div class="center">
    <%@ include file="banner.jsp"%>
    <div class="main-column">

<h3>Tor Metrics: Advertised bandwidth distribution</h3>
<br>
<p>The following graph shows the distribution of advertised bandwidth in
the network. In contrast to the graphs above, the following graph contains
no sums of advertised bandwidths, but bandwidths of single relays.</p>
<img src="advbwdist-perc.png${advbwdist_perc_url}"
     width="576" height="360"
     alt="Advertised bandwidth distribution graph">
<form action="advbwdist-perc.html">
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

    </div>
  </div>
  <div class="bottom" id="bottom">
    <%@ include file="footer.jsp"%>
  </div>
</body>
</html>
