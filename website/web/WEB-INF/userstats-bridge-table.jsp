<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
  <title>Tor Metrics &mdash; Top-10 countries by bridge users</title>
  <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">
  <link href="/css/stylesheet-ltr.css" type="text/css" rel="stylesheet">
  <link href="/images/favicon.ico" type="image/x-icon" rel="shortcut icon">
</head>
<body>
  <div class="center">
    <%@ include file="banner.jsp"%>
    <div class="main-column">

<h2><a href="/"><img src="/images/metrics-wordmark-small.png" width="138" height="18" alt="Metrics wordmark"></a> &mdash; Top-10 countries by bridge users</h2>
<br>
<p>The following table shows the top-10 countries by estimated number of
<a href="about.html#client">clients</a> connecting via
<a href="about.html#bridge">bridges</a>.
These numbers are derived from directory requests counted on bridges.
Bridges resolve client IP addresses of incoming directory requests to
country codes, so that numbers are available for most countries.</p>
<form action="userstats-bridge-table.html">
  <div class="formrow">
    <input type="hidden" name="table" value="userstats-bridge">
    <p>
    <label>Start date (yyyy-mm-dd):
      <input type="text" name="start" size="10"
             value="<c:choose><c:when test="${fn:length(userstats_bridge_start) == 0}">${default_start_date}</c:when><c:otherwise>${userstats_bridge_start[0]}</c:otherwise></c:choose>"></label>
    <label>End date (yyyy-mm-dd):
      <input type="text" name="end" size="10"
             value="<c:choose><c:when test="${fn:length(userstats_bridge_end) == 0}">${default_end_date}</c:when><c:otherwise>${userstats_bridge_end[0]}</c:otherwise></c:choose>"></label>
    </p><p>
    <input class="submit" type="submit" value="Update table">
    </p>
  </div>
</form>
<br>
<table>
  <tr>
    <th>Country</th>
    <th>Mean daily users</th>
  </tr>
  <c:forEach var="row" items="${userstats_bridge_tabledata}">
    <tr>
      <td><a href="userstats-bridge-country.html?graph=userstats-bridge-country&country=${row['cc']}">${row['country']}</a>&emsp;</td>
      <td>${row['abs']} <c:if test="${row['rel'] != 'NA'}"> (<fmt:formatNumber type="number" minFractionDigits="2" value="${row['rel']}" /> %)</c:if></td>
    </tr>
  </c:forEach>
</table>
<p>
<a href="https://gitweb.torproject.org/metrics-web.git/tree/doc/users-q-and-a.txt">Questions
and answers about users statistics</a></p>

<h4>Related metrics</h4>
<ul>
<li><a href="userstats-relay-country.html">Graph: Direct users by country</a></li>
<li><a href="userstats-relay-table.html">Table: Top-10 countries by directly connecting users</a></li>
<li><a href="userstats-bridge-country.html">Graph: Bridge users by country</a></li>
<li><a href="userstats-bridge-transport.html">Graph: Bridge users by transport</a></li>
<li><a href="userstats-bridge-version.html">Graph: Bridge users by IP version</a></li>
<li><a href="clients-data.html">Data: Estimated number of clients in the Tor network</a></li>
</ul>

    </div>
  </div>
  <div class="bottom" id="bottom">
    <%@ include file="footer.jsp"%>
  </div>
</body>
</html>
