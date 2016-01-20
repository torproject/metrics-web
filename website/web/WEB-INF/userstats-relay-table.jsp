<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
  <title>Tor Metrics &mdash; Top-10 countries by directly connecting users</title>
  <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">
  <link href="/css/stylesheet-ltr.css" type="text/css" rel="stylesheet">
  <link href="/images/favicon.ico" type="image/x-icon" rel="shortcut icon">
</head>
<body>
  <div class="center">
    <%@ include file="banner.jsp"%>
    <div class="main-column">

<h2><a href="/"><img src="/images/metrics-wordmark-small.png" width="138" height="18" alt="Metrics wordmark"></a> &mdash; Top-10 countries by directly connecting users</h2>
<br>
<p>The following table shows the top-10 countries by estimated number of
directly-connecting <a href="about.html#client">clients</a>.
These numbers are derived from directory requests counted on
<a href="about.html#directory-authority">directory authorities</a> and
<a href="about.html#directory-mirror">mirrors</a>.
Relays resolve client IP addresses to country codes, so that numbers are
available for most countries.</p>
<form action="userstats-relay-table.html">
  <div class="formrow">
    <input type="hidden" name="table" value="userstats-relay">
    <p>
    <label>Start date (yyyy-mm-dd):
      <input type="text" name="start" size="10"
             value="<c:choose><c:when test="${fn:length(userstats_relay_start) == 0}">${default_start_date}</c:when><c:otherwise>${userstats_relay_start[0]}</c:otherwise></c:choose>"></label>
    <label>End date (yyyy-mm-dd):
      <input type="text" name="end" size="10"
             value="<c:choose><c:when test="${fn:length(userstats_relay_end) == 0}">${default_end_date}</c:when><c:otherwise>${userstats_relay_end[0]}</c:otherwise></c:choose>"></label>
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
  <c:forEach var="row" items="${userstats_relay_tabledata}">
    <tr>
      <td><a href="userstats-relay-country.html?graph=userstats-relay-country&country=${row['cc']}">${row['country']}</a>&emsp;</td>
      <td>${row['abs']} <c:if test="${row['rel'] != 'NA'}"> (<fmt:formatNumber type="number" minFractionDigits="2" value="${row['rel']}" /> %)</c:if></td>
    </tr>
  </c:forEach>
</table>
<p>
<a href="https://gitweb.torproject.org/metrics-web.git/tree/doc/users-q-and-a.txt">Questions
and answers about users statistics</a></p>

<h4>Underlying data</h4>
<ul>
<li><a href="clients-data.html">Data: Estimated number of clients in the Tor network</a></li>
</ul>

<h4>Related metrics</h4>
<ul>
<li><a href="userstats-relay-country.html">Graph: Direct users by country</a></li>
<li><a href="userstats-censorship-events.html">Table: Top-10 countries by possible censorship events</a></li>
<li><a href="userstats-bridge-country.html">Graph: Bridge users by country</a></li>
<li><a href="userstats-bridge-table.html">Table: Top-10 countries by bridge users</a></li>
<li><a href="oxford-anonymous-internet.html">Link: Tor users as percentage of larger Internet population</a></li>
</ul>

    </div>
  </div>
  <div class="bottom" id="bottom">
    <%@ include file="footer.jsp"%>
  </div>
</body>
</html>
