<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
  <title>Tor Metrics &mdash; Direct users by country</title>
  <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">
  <link href="/css/stylesheet-ltr.css" type="text/css" rel="stylesheet">
  <link href="/images/favicon.ico" type="image/x-icon" rel="shortcut icon">
</head>
<body>
  <div class="center">
    <%@ include file="banner.jsp"%>
    <div class="main-column">

<h2><a href="/">Tor Metrics</a> &mdash; Direct users by country</h2>
<br>
<p>The following graph shows the estimated number of directly-connecting
<a href="about.html#client">clients</a>; that is, it excludes clients
connecting via <a href="about.html#bridge">bridges</a>.
These estimates are derived from the number of directory requests counted
on <a href="about.html#directory-authority">directory authorities</a> and
<a href="about.html#directory-mirror">mirrors</a>.
Relays resolve client IP addresses to country codes, so that graphs are
available for most countries.
Furthermore, it is possible to display indications of censorship events as
obtained from an anomaly-based censorship-detection system (for more
details, see this
<a href="https://research.torproject.org/techreports/detector-2011-09-09.pdf">technical report</a>).</p>
<img src="userstats-relay-country.png${userstats_relay_country_url}"
     width="576" height="360" alt="Direct users by country graph">
<form action="userstats-relay-country.html">
  <div class="formrow">
    <input type="hidden" name="graph" value="userstats-relay-country">
    <p>
    <label>Start date (yyyy-mm-dd):</label>
      <input type="text" name="start" size="10"
             value="<c:choose><c:when test="${fn:length(userstats_relay_country_start) == 0}">${default_start_date}</c:when><c:otherwise>${userstats_relay_country_start[0]}</c:otherwise></c:choose>">
    <label>End date (yyyy-mm-dd):</label>
      <input type="text" name="end" size="10"
             value="<c:choose><c:when test="${fn:length(userstats_relay_country_end) == 0}">${default_end_date}</c:when><c:otherwise>${userstats_relay_country_end[0]}</c:otherwise></c:choose>">
    </p><p>
      Source: <select name="country">
        <option value="all"<c:if test="${userstats_relay_country_country[0] eq 'all'}"> selected</c:if>>All users</option>
        <c:forEach var="country" items="${countries}" >
          <option value="${country[0]}"<c:if test="${userstats_relay_country_country[0] eq country[0]}"> selected</c:if>>${country[1]}</option>
        </c:forEach>
      </select>
    </p><p>
      Show possible censorship events if available (<a
      href="http://research.torproject.org/techreports/detector-2011-09-09.pdf">BETA</a>)
      <select name="events">
        <option value="off">Off</option>
        <option value="on"<c:if test="${userstats_relay_country_events[0] eq 'on'}"> selected</c:if>>On: both points and expected range</option>
        <option value="points"<c:if test="${userstats_relay_country_events[0] eq 'points'}"> selected</c:if>>On: points only, no expected range</option>
      </select>
    </p><p>
    <input class="submit" type="submit" value="Update graph">
    </p>
  </div>
</form>
<p>Download graph as
<a href="userstats-relay-country.pdf${userstats_relay_country_url}">PDF</a> or
<a href="userstats-relay-country.svg${userstats_relay_country_url}">SVG</a>.
<a href="https://gitweb.torproject.org/metrics-web.git/tree/doc/users-q-and-a.txt">Questions
and answers about users statistics</a></p>

<h4>Related metrics</h4>
<ul>
<li><a href="userstats-relay-table.html">Table: Top-10 countries by directly connecting users</a></li>
<li><a href="userstats-censorship-events.html">Table: Top-10 countries by possible censorship events</a></li>
<li><a href="userstats-bridge-country.html">Graph: Bridge users by country</a></li>
<li><a href="userstats-bridge-table.html">Table: Top-10 countries by bridge users</a></li>
<li><a href="oxford-anonymous-internet.html">Link: Tor users as percentage of larger Internet population</a></li>
<li><a href="clients-data.html">Data: Estimated number of clients in the Tor network</a></li>
</ul>

    </div>
  </div>
  <div class="bottom" id="bottom">
    <%@ include file="footer.jsp"%>
  </div>
</body>
</html>
