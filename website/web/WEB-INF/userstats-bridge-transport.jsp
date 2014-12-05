<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
  <title>Tor Metrics &mdash; Bridge users by transport</title>
  <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">
  <link href="/css/stylesheet-ltr.css" type="text/css" rel="stylesheet">
  <link href="/images/favicon.ico" type="image/x-icon" rel="shortcut icon">
</head>
<body>
  <div class="center">
    <%@ include file="banner.jsp"%>
    <div class="main-column">

<h2><a href="/">Tor Metrics</a> &mdash; Bridge users by transport</h2>
<br>
<p>The following graph shows the estimated number of
<a href="about.html#client">clients</a> connecting via
<a href="about.html#bridge">bridges</a>.
These numbers are derived from directory requests counted on bridges.
Bridges distinguish connecting clients by transport protocol, which may
include <a href="about.html#pluggable-transport">pluggable transports</a>,
so that graphs are available for different transports.</p>
<img src="userstats-bridge-transport.png${userstats_bridge_transport_url}"
     width="576" height="360" alt="Bridge users by transport graph">
<form action="userstats-bridge-transport.html">
  <div class="formrow">
    <input type="hidden" name="graph" value="userstats-bridge-transport">
    <p>
    <label>Start date (yyyy-mm-dd):</label>
      <input type="text" name="start" size="10"
             value="<c:choose><c:when test="${fn:length(userstats_bridge_transport_start) == 0}">${default_start_date}</c:when><c:otherwise>${userstats_bridge_transport_start[0]}</c:otherwise></c:choose>">
    <label>End date (yyyy-mm-dd):</label>
      <input type="text" name="end" size="10"
             value="<c:choose><c:when test="${fn:length(userstats_bridge_transport_end) == 0}">${default_end_date}</c:when><c:otherwise>${userstats_bridge_transport_end[0]}</c:otherwise></c:choose>">
    </p><p>
      <label>Source: </label>
      <input type="checkbox" name="transport" value="<OR>"<c:if test="${fn:length(userstats_bridge_transport_transport) == 0 or fn:contains(fn:join(userstats_bridge_transport_transport, ','), '<OR>')}"> checked</c:if>> Default OR protocol
      <input type="checkbox" name="transport" value="obfs2"<c:if test="${fn:length(userstats_bridge_transport_transport) > 0 and fn:contains(fn:join(userstats_bridge_transport_transport, ','), 'obfs2')}"> checked</c:if>> obfs2
      <input type="checkbox" name="transport" value="obfs3"<c:if test="${fn:length(userstats_bridge_transport_transport) > 0 and fn:contains(fn:join(userstats_bridge_transport_transport, ','), 'obfs3')}"> checked</c:if>> obfs3
      <input type="checkbox" name="transport" value="obfs4"<c:if test="${fn:length(userstats_bridge_transport_transport) > 0 and fn:contains(fn:join(userstats_bridge_transport_transport, ','), 'obfs4')}"> checked</c:if>> obfs4
      <input type="checkbox" name="transport" value="websocket"<c:if test="${fn:length(userstats_bridge_transport_transport) > 0 and fn:contains(fn:join(userstats_bridge_transport_transport, ','), 'websocket')}"> checked</c:if>> Flash proxy/websocket
      <input type="checkbox" name="transport" value="fte"<c:if test="${fn:length(userstats_bridge_transport_transport) > 0 and fn:contains(fn:join(userstats_bridge_transport_transport, ','), 'fte')}"> checked</c:if>> FTE
      <input type="checkbox" name="transport" value="meek"<c:if test="${fn:length(userstats_bridge_transport_transport) > 0 and fn:contains(fn:join(userstats_bridge_transport_transport, ','), 'meek')}"> checked</c:if>> meek
      <input type="checkbox" name="transport" value="scramblesuit"<c:if test="${fn:length(userstats_bridge_transport_transport) > 0 and fn:contains(fn:join(userstats_bridge_transport_transport, ','), 'scramblesuit')}"> checked</c:if>> scramblesuit
      <input type="checkbox" name="transport" value="<??>"<c:if test="${fn:length(userstats_bridge_transport_transport) > 0 and fn:contains(fn:join(userstats_bridge_transport_transport, ','), '<??>')}"> checked</c:if>> Unknown pluggable transport(s)
      <input type="checkbox" name="transport" value="!<OR>"<c:if test="${fn:length(userstats_bridge_transport_transport) > 0 and fn:contains(fn:join(userstats_bridge_transport_transport, ','), '!<OR>')}"> checked</c:if>> Any pluggable transport
    </p><p>
    <input class="submit" type="submit" value="Update graph">
    </p>
  </div>
</form>
<p>Download graph as
<a href="userstats-bridge-transport.pdf${userstats_bridge_transport_url}">PDF</a> or
<a href="userstats-bridge-transport.svg${userstats_bridge_transport_url}">SVG</a>.
<a href="https://gitweb.torproject.org/metrics-web.git/tree/doc/users-q-and-a.txt">Questions
and answers about users statistics</a></p>

<h4>Related metrics</h4>
<ul>
<li><a href="userstats-bridge-country.html">Graph: Bridge users by country</a></li>
<li><a href="userstats-bridge-table.html">Table: Top-10 countries by bridge users</a></li>
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
