<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
  <title>Tor Metrics &mdash; Top-10 countries by possible censorship events</title>
  <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">
  <link href="/css/stylesheet-ltr.css" type="text/css" rel="stylesheet">
  <link href="/images/favicon.ico" type="image/x-icon" rel="shortcut icon">
</head>
<body>
  <div class="center">
    <%@ include file="banner.jsp"%>
    <div class="main-column">

<h2><a href="/"><img src="/images/metrics-wordmark-small.png" width="138" height="18" alt="Metrics wordmark"></a> &mdash; Top-10 countries by possible censorship events</h2>
<br>
<p>The following table shows the top-10 countries by possible censorship
events, as obtained from an anomaly-based censorship-detection system (for
more details, see this
<a href="https://research.torproject.org/techreports/detector-2011-09-09.pdf">technical report</a>).</p>
<form action="userstats-censorship-events.html">
  <div class="formrow">
    <input type="hidden" name="table" value="userstats-censorship-events">
    <p>
    <label>Start date (yyyy-mm-dd):
      <input type="text" name="start" size="10"
             value="<c:choose><c:when test="${fn:length(userstats_censorship_events_start) == 0}">${default_start_date}</c:when><c:otherwise>${userstats_censorship_events_start[0]}</c:otherwise></c:choose>"></label>
    <label>End date (yyyy-mm-dd):
      <input type="text" name="end" size="10"
             value="<c:choose><c:when test="${fn:length(userstats_censorship_events_end) == 0}">${default_end_date}</c:when><c:otherwise>${userstats_censorship_events_end[0]}</c:otherwise></c:choose>"></label>
    </p><p>
    <input class="submit" type="submit" value="Update table">
    </p>
  </div>
</form>
<br>
<table>
  <tr>
    <th>Country</th>
    <th>Downturns</th>
    <th>Upturns</th>
  </tr>
  <c:forEach var="row" items="${userstats_censorship_events_tabledata}">
    <tr>
      <td><a href="userstats-relay-country.html?graph=userstats-relay-country&country=${row['cc']}&events=on">${row['country']}</a>&emsp;</td>
      <td>${row['downturns']}</td>
      <td>${row['upturns']}</td>
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
<li><a href="userstats-relay-table.html">Table: Top-10 countries by directly connecting users</a></li>
<li><a href="userstats-censorship-events.html">Table: Top-10 countries by possible censorship events</a></li>
</ul>

    </div>
  </div>
  <div class="bottom" id="bottom">
    <%@ include file="footer.jsp"%>
  </div>
</body>
</html>
