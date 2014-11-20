<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
  <title>Tor Metrics: Advertised bandwidth of n-th fastest relays</title>
  <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">
  <link href="/css/stylesheet-ltr.css" type="text/css" rel="stylesheet">
  <link href="/images/favicon.ico" type="image/x-icon" rel="shortcut icon">
</head>
<body>
  <div class="center">
    <%@ include file="banner.jsp"%>
    <div class="main-column">

<h3>Tor Metrics: Advertised bandwidth of n-th fastest relays</h3>
<br>
<p>The following graph shows the advertised bandwidth of the n-th fastest
relays in the network.</p>
<img src="advbwdist-relay.png${advbwdist_relay_url}"
     width="576" height="360"
     alt="Advertised bandwidth of n-th fastest relays graph">
<form action="advbwdist-relay.html">
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
