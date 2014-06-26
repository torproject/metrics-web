<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
  <title>Tor Metrics: Users</title>
  <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">
  <link href="/css/stylesheet-ltr.css" type="text/css" rel="stylesheet">
  <link href="/images/favicon.ico" type="image/x-icon" rel="shortcut icon">
</head>
<body>
  <div class="center">
    <%@ include file="banner.jsp"%>
    <div class="main-column">
<h2>Tor Metrics: Users</h2>
<br>

<a name="userstats-relay-country"></a>
<h3><a href="#userstats-relay-country" class="anchor">
Direct users by country:
</a></h3>

<img src="userstats-relay-country.png${userstats_relay_country_url}"
     width="576" height="360" alt="Direct users by country graph">
<form action="users.html#userstats-relay-country">
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
<a href="userstats-relay-country.svg${userstats_relay_country_url}">SVG</a>.</p>
<hr>
<a name="userstats-relay-table"></a>
<h3><a href="#userstats-relay-table" class="anchor">
Top-10 countries by directly connecting users:
</a></h3>
<form action="users.html#userstats-relay-table">
  <div class="formrow">
    <input type="hidden" name="table" value="userstats-relay">
    <p>
    <label>Start date (yyyy-mm-dd):</label>
      <input type="text" name="start" size="10"
             value="<c:choose><c:when test="${fn:length(userstats_relay_start) == 0}">${default_start_date}</c:when><c:otherwise>${userstats_relay_start[0]}</c:otherwise></c:choose>">
    <label>End date (yyyy-mm-dd):</label>
      <input type="text" name="end" size="10"
             value="<c:choose><c:when test="${fn:length(userstats_relay_end) == 0}">${default_end_date}</c:when><c:otherwise>${userstats_relay_end[0]}</c:otherwise></c:choose>">
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
      <td><a href="users.html?graph=userstats-relay-country&country=${row['cc']}#userstats-relay-country">${row['country']}</a>&emsp;</td>
      <td>${row['abs']} (<fmt:formatNumber type="number" minFractionDigits="2" value="${row['rel']}" /> %)</td>
    </tr>
  </c:forEach>
</table>
<hr>
<a name="userstats-censorship-events"></a>
<h3><a href="#userstats-censorship-events" class="anchor">
Top-10 countries by possible censorship events (<a
href="http://research.torproject.org/techreports/detector-2011-09-09.pdf">BETA</a>):
</a></h3>
<form action="users.html#userstats-censorship-events">
  <div class="formrow">
    <input type="hidden" name="table" value="userstats-censorship-events">
    <p>
    <label>Start date (yyyy-mm-dd):</label>
      <input type="text" name="start" size="10"
             value="<c:choose><c:when test="${fn:length(userstats_censorship_events_start) == 0}">${default_start_date}</c:when><c:otherwise>${userstats_censorship_events_start[0]}</c:otherwise></c:choose>">
    <label>End date (yyyy-mm-dd):</label>
      <input type="text" name="end" size="10"
             value="<c:choose><c:when test="${fn:length(userstats_censorship_events_end) == 0}">${default_end_date}</c:when><c:otherwise>${userstats_censorship_events_end[0]}</c:otherwise></c:choose>">
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
      <td><a href="users.html?graph=userstats-relay-country&country=${row['cc']}&events=on#userstats-relay-country">${row['country']}</a>&emsp;</td>
      <td>${row['downturns']}</td>
      <td>${row['upturns']}</td>
    </tr>
  </c:forEach>
</table>
<hr>

<a name="userstats-bridge-country"></a>
<h3><a href="#userstats-bridge-country" class="anchor">
Bridge users by country:
</a></h3>

<img src="userstats-bridge-country.png${userstats_bridge_country_url}"
     width="576" height="360" alt="Bridge users by country graph">
<form action="users.html#userstats-bridge-country">
  <div class="formrow">
    <input type="hidden" name="graph" value="userstats-bridge-country">
    <p>
    <label>Start date (yyyy-mm-dd):</label>
      <input type="text" name="start" size="10"
             value="<c:choose><c:when test="${fn:length(userstats_bridge_country_start) == 0}">${default_start_date}</c:when><c:otherwise>${userstats_bridge_country_start[0]}</c:otherwise></c:choose>">
    <label>End date (yyyy-mm-dd):</label>
      <input type="text" name="end" size="10"
             value="<c:choose><c:when test="${fn:length(userstats_bridge_country_end) == 0}">${default_end_date}</c:when><c:otherwise>${userstats_bridge_country_end[0]}</c:otherwise></c:choose>">
    </p><p>
      Source: <select name="country">
        <option value="all"<c:if test="${userstats_bridge_country_country[0] eq 'all'}"> selected</c:if>>All users</option>
        <c:forEach var="country" items="${countries}" >
          <option value="${country[0]}"<c:if test="${userstats_bridge_country_country[0] eq country[0]}"> selected</c:if>>${country[1]}</option>
        </c:forEach>
      </select>
    </p><p>
    <input class="submit" type="submit" value="Update graph">
    </p>
  </div>
</form>
<p>Download graph as
<a href="userstats-bridge-country.pdf${userstats_bridge_country_url}">PDF</a> or
<a href="userstats-bridge-country.svg${userstats_bridge_country_url}">SVG</a>.</p>
<hr>
<a name="userstats-bridge-table"></a>
<h3><a href="#userstats-bridge-table" class="anchor">
Top-10 countries by bridge users:
</a></h3>
<form action="users.html#userstats-bridge-table">
  <div class="formrow">
    <input type="hidden" name="table" value="userstats-bridge">
    <p>
    <label>Start date (yyyy-mm-dd):</label>
      <input type="text" name="start" size="10"
             value="<c:choose><c:when test="${fn:length(userstats_bridge_start) == 0}">${default_start_date}</c:when><c:otherwise>${userstats_bridge_start[0]}</c:otherwise></c:choose>">
    <label>End date (yyyy-mm-dd):</label>
      <input type="text" name="end" size="10"
             value="<c:choose><c:when test="${fn:length(userstats_bridge_end) == 0}">${default_end_date}</c:when><c:otherwise>${userstats_bridge_end[0]}</c:otherwise></c:choose>">
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
      <td><a href="users.html?graph=userstats-bridge-country&country=${row['cc']}#userstats-bridge-country">${row['country']}</a>&emsp;</td>
      <td>${row['abs']} (<fmt:formatNumber type="number" minFractionDigits="2" value="${row['rel']}" /> %)</td>
    </tr>
  </c:forEach>
</table>
<hr>

<a name="userstats-bridge-transport"></a>
<h3><a href="#userstats-bridge-transport" class="anchor">
Bridge users by transport:
</a></h3>

<img src="userstats-bridge-transport.png${userstats_bridge_transport_url}"
     width="576" height="360" alt="Bridge users by transport graph">
<form action="users.html#userstats-bridge-transport">
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
      Source: <select name="transport">
        <option value="<OR>"<c:if test="${userstats_bridge_transport_transport[0] eq '<OR>'}"> selected</c:if>>Default OR protocol</option>
        <option value="obfs2"<c:if test="${userstats_bridge_transport_transport[0] eq 'obfs2'}"> selected</c:if>>obfs2</option>
        <option value="obfs3"<c:if test="${userstats_bridge_transport_transport[0] eq 'obfs3'}"> selected</c:if>>obfs3</option>
        <option value="websocket"<c:if test="${userstats_bridge_transport_transport[0] eq 'websocket'}"> selected</c:if>>Flash proxy/websocket</option>
        <option value="fte"<c:if test="${userstats_bridge_transport_transport[0] eq 'fte'}"> selected</c:if>>FTE</option>
        <option value="meek"<c:if test="${userstats_bridge_transport_transport[0] eq 'meek'}"> selected</c:if>>meek</option>
        <option value="scramblesuit"<c:if test="${userstats_bridge_transport_transport[0] eq 'scramblesuit'}"> selected</c:if>>scramblesuit</option>
        <option value="<??>"<c:if test="${userstats_bridge_transport_transport[0] eq '<??>'}"> selected</c:if>>Unknown transport</option>
      </select>
    </p><p>
    <input class="submit" type="submit" value="Update graph">
    </p>
  </div>
</form>
<p>Download graph as
<a href="userstats-bridge-transport.pdf${userstats_bridge_transport_url}">PDF</a> or
<a href="userstats-bridge-transport.svg${userstats_bridge_transport_url}">SVG</a>.</p>
<hr>

<a name="userstats-bridge-version"></a>
<h3><a href="#userstats-bridge-version" class="anchor">
Bridge users by IP version:
</a></h3>

<img src="userstats-bridge-version.png${userstats_bridge_version_url}"
     width="576" height="360" alt="Bridge users by IP version graph">
<form action="users.html#userstats-bridge-version">
  <div class="formrow">
    <input type="hidden" name="graph" value="userstats-bridge-version">
    <p>
    <label>Start date (yyyy-mm-dd):</label>
      <input type="text" name="start" size="10"
             value="<c:choose><c:when test="${fn:length(userstats_bridge_version_start) == 0}">${default_start_date}</c:when><c:otherwise>${userstats_bridge_version_start[0]}</c:otherwise></c:choose>">
    <label>End date (yyyy-mm-dd):</label>
      <input type="text" name="end" size="10"
             value="<c:choose><c:when test="${fn:length(userstats_bridge_version_end) == 0}">${default_end_date}</c:when><c:otherwise>${userstats_bridge_version_end[0]}</c:otherwise></c:choose>">
    </p><p>
      Source: <select name="version">
        <option value="v4"<c:if test="${userstats_bridge_version_version[0] eq 'v4'}"> selected</c:if>>IPv4</option>
        <option value="v6"<c:if test="${userstats_bridge_version_version[0] eq 'v6'}"> selected</c:if>>IPv6</option>
      </select>
    </p><p>
    <input class="submit" type="submit" value="Update graph">
    </p>
  </div>
</form>
<p>Download graph as
<a href="userstats-bridge-version.pdf${userstats_bridge_version_url}">PDF</a> or
<a href="userstats-bridge-version.svg${userstats_bridge_version_url}">SVG</a>.</p>
<hr>

<p><a href="stats/clients.csv">CSV</a> file containing new user
estimates.</p>
<br>

<hr>

<p><a href="https://gitweb.torproject.org/metrics-web.git/blob/HEAD:/doc/users-q-and-a.txt">Questions
and answers about users statistics</a></p>

    </div>
  </div>
  <div class="bottom" id="bottom">
    <%@ include file="footer.jsp"%>
  </div>
</body>
</html>
