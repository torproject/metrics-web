<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
  <title>Tor Metrics &mdash; Bridge users by IP version</title>
  <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">
  <link href="/css/stylesheet-ltr.css" type="text/css" rel="stylesheet">
  <link href="/images/favicon.ico" type="image/x-icon" rel="shortcut icon">
</head>
<body>
  <div class="center">
    <%@ include file="banner.jsp"%>
    <div class="main-column">

<h2><a href="/"><img src="/images/metrics-wordmark-small.png" width="138" height="18" alt="Metrics wordmark"></a> &mdash; Bridge users by IP version</h2>
<br>
<p>The following graph shows the estimated number of
<a href="about.html#client">clients</a> connecting via
<a href="about.html#bridge">bridges</a>.
These numbers are derived from directory requests counted on bridges.
Bridges distinguish connecting clients by IP version, so that graphs are
available for both IP versions 4 and 6.</p>
<img src="userstats-bridge-version.png${userstats_bridge_version_url}"
     width="576" height="360" alt="Bridge users by IP version graph">
<form action="userstats-bridge-version.html">
  <div class="formrow">
    <input type="hidden" name="graph" value="userstats-bridge-version">
    <p>
    <label>Start date (yyyy-mm-dd):
      <input type="text" name="start" size="10"
             value="<c:choose><c:when test="${fn:length(userstats_bridge_version_start) == 0}">${default_start_date}</c:when><c:otherwise>${userstats_bridge_version_start[0]}</c:otherwise></c:choose>"></label>
    <label>End date (yyyy-mm-dd):
      <input type="text" name="end" size="10"
             value="<c:choose><c:when test="${fn:length(userstats_bridge_version_end) == 0}">${default_end_date}</c:when><c:otherwise>${userstats_bridge_version_end[0]}</c:otherwise></c:choose>"></label>
    </p><p>
      <label>Source: <select name="version">
        <option value="v4"<c:if test="${userstats_bridge_version_version[0] eq 'v4'}"> selected</c:if>>IPv4</option>
        <option value="v6"<c:if test="${userstats_bridge_version_version[0] eq 'v6'}"> selected</c:if>>IPv6</option>
      </select></label>
    </p><p>
    <input class="submit" type="submit" value="Update graph">
    </p>
  </div>
</form>
<p>Download graph as
<a href="userstats-bridge-version.pdf${userstats_bridge_version_url}">PDF</a> or
<a href="userstats-bridge-version.svg${userstats_bridge_version_url}">SVG</a>.
<a href="https://gitweb.torproject.org/metrics-web.git/tree/doc/users-q-and-a.txt">Questions
and answers about users statistics</a></p>

<h4>Related metrics</h4>
<ul>
<li><a href="userstats-bridge-country.html">Graph: Bridge users by country</a></li>
<li><a href="userstats-bridge-table.html">Table: Top-10 countries by bridge users</a></li>
<li><a href="userstats-bridge-transport.html">Graph: Bridge users by transport</a></li>
<li><a href="clients-data.html">Data: Estimated number of clients in the Tor network</a></li>
</ul>

    </div>
  </div>
  <div class="bottom" id="bottom">
    <%@ include file="footer.jsp"%>
  </div>
</body>
</html>
