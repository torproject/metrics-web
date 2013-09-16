<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
  <title>Tor Metrics Portal: Users</title>
  <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">
  <link href="/css/stylesheet-ltr.css" type="text/css" rel="stylesheet">
  <link href="/images/favicon.ico" type="image/x-icon" rel="shortcut icon">
</head>
<body>
  <div class="center">
    <%@ include file="banner.jsp"%>
    <div class="main-column">
<h2>Tor Metrics Portal: Users</h2>
<br>

<a name="direct-users"></a>
<h3><a href="#direct-users" class="anchor">Directly connecting Tor
users</a></h3>
<br>
<p>After being connected to the Tor network, users need to refresh their
list of running relays on a regular basis. They send their requests to one
out of a few hundred directory mirrors to save bandwidth of the directory
authorities. The following graphs show an estimate of recurring Tor users
based on the requests seen by a few dozen directory mirrors.</p>
<p><b>Daily directly connecting users:</b></p>
<img src="direct-users.png${direct_users_url}"
     width="576" height="360" alt="Direct users graph">
<form action="users.html#direct-users">
  <div class="formrow">
    <input type="hidden" name="graph" value="direct-users">
    <p>
    <label>Start date (yyyy-mm-dd):</label>
      <input type="text" name="start" size="10"
             value="<c:choose><c:when test="${fn:length(direct_users_start) == 0}">${default_start_date}</c:when><c:otherwise>${direct_users_start[0]}</c:otherwise></c:choose>">
    <label>End date (yyyy-mm-dd):</label>
      <input type="text" name="end" size="10"
             value="<c:choose><c:when test="${fn:length(direct_users_end) == 0}">${default_end_date}</c:when><c:otherwise>${direct_users_end[0]}</c:otherwise></c:choose>">
    </p><p>
      Source: <select name="country">
        <option value="all"<c:if test="${direct_users_country[0] eq 'all'}"> selected</c:if>>All users</option>
        <c:forEach var="country" items="${countries}" >
          <option value="${country[0]}"<c:if test="${direct_users_country[0] eq country[0]}"> selected</c:if>>${country[1]}</option>
        </c:forEach>
      </select>
    </p><p>
      Show possible censorship events if available (<a
      href="http://research.torproject.org/techreports/detector-2011-09-09.pdf">BETA</a>)
      <select name="events">
        <option value="off">Off</option>
        <option value="on"<c:if test="${direct_users_events[0] eq 'on'}"> selected</c:if>>On: both points and expected range</option>
        <option value="points"<c:if test="${direct_users_events[0] eq 'points'}"> selected</c:if>>On: points only, no expected range</option>
      </select>
    </p><p>
    <input class="submit" type="submit" value="Update graph">
    </p>
  </div>
</form>
<p>Download graph as
<a href="direct-users.pdf${direct_users_url}">PDF</a> or
<a href="direct-users.svg${direct_users_url}">SVG</a>.</p>
<hr>
<a name="direct-users-table"></a>
<p><b>Top-10 countries by directly connecting users:</b></p>
<form action="users.html#direct-users-table">
  <div class="formrow">
    <input type="hidden" name="table" value="direct-users">
    <p>
    <label>Start date (yyyy-mm-dd):</label>
      <input type="text" name="start" size="10"
             value="<c:choose><c:when test="${fn:length(direct_users_start) == 0}">${default_start_date}</c:when><c:otherwise>${direct_users_start[0]}</c:otherwise></c:choose>">
    <label>End date (yyyy-mm-dd):</label>
      <input type="text" name="end" size="10"
             value="<c:choose><c:when test="${fn:length(direct_users_end) == 0}">${default_end_date}</c:when><c:otherwise>${direct_users_end[0]}</c:otherwise></c:choose>">
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
  <c:forEach var="row" items="${direct_users_tabledata}">
    <tr>
      <td><a href="users.html?graph=direct-users&country=${row['cc']}#direct-users">${row['country']}</a>&emsp;</td>
      <td>${row['abs']} (<fmt:formatNumber type="number" minFractionDigits="2" value="${row['rel']}" /> %)</td>
    </tr>
  </c:forEach>
</table>
<hr>
<a name="censorship-events"></a>
<p><b>Top-10 countries by possible censorship events (<a
      href="http://research.torproject.org/techreports/detector-2011-09-09.pdf">BETA</a>):</b></p>
<form action="users.html#censorship-events">
  <div class="formrow">
    <input type="hidden" name="table" value="censorship-events">
    <p>
    <label>Start date (yyyy-mm-dd):</label>
      <input type="text" name="start" size="10"
             value="<c:choose><c:when test="${fn:length(censorship_events_start) == 0}">${default_start_date}</c:when><c:otherwise>${censorship_events_start[0]}</c:otherwise></c:choose>">
    <label>End date (yyyy-mm-dd):</label>
      <input type="text" name="end" size="10"
             value="<c:choose><c:when test="${fn:length(censorship_events_end) == 0}">${default_end_date}</c:when><c:otherwise>${censorship_events_end[0]}</c:otherwise></c:choose>">
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
  <c:forEach var="row" items="${censorship_events_tabledata}">
    <tr>
      <td><a href="users.html?graph=direct-users&country=${row['cc']}&events=on#direct-users">${row['country']}</a>&emsp;</td>
      <td>${row['downturns']}</td>
      <td>${row['upturns']}</td>
    </tr>
  </c:forEach>
</table>
<hr>
<p><a href="csv/direct-users.csv">CSV</a> file containing daily directly
connecting users by country.</p>
<p><a href="csv/monthly-users-peak.csv">CSV</a> file containing peak daily
Tor users (direct and bridge) per month by country.</p>
<p><a href="csv/monthly-users-average.csv">CSV</a> file containing average
daily Tor users (direct and bridge) per month by country.</p>
<br>

<a name="bridge-users"></a>
<h3><a href="#bridge-users" class="anchor">Tor users via bridges</a></h3>
<br>
<p>Users who cannot connect directly to the Tor network instead connect
via bridges, which are non-public relays. The following graphs display an
estimate of Tor users via bridges based on the unique IP addresses as seen
by a few hundred bridges.</p>
<img src="bridge-users.png${bridge_users_url}"
     width="576" height="360" alt="Bridge users graph">
<form action="users.html#bridge-users">
  <div class="formrow">
    <input type="hidden" name="graph" value="bridge-users">
    <p>
    <label>Start date (yyyy-mm-dd):</label>
      <input type="text" name="start" size="10"
             value="<c:choose><c:when test="${fn:length(bridge_users_start) == 0}">${default_start_date}</c:when><c:otherwise>${bridge_users_start[0]}</c:otherwise></c:choose>">
    <label>End date (yyyy-mm-dd):</label>
      <input type="text" name="end" size="10"
             value="<c:choose><c:when test="${fn:length(bridge_users_end) == 0}">${default_end_date}</c:when><c:otherwise>${bridge_users_end[0]}</c:otherwise></c:choose>">
    </p><p>
      Source: <select name="country">
        <option value="all"<c:if test="${bridge_users_country[0] eq 'all'}"> selected</c:if>>All users</option>
        <c:forEach var="country" items="${countries}" >
          <option value="${country[0]}"<c:if test="${bridge_users_country[0] eq country[0]}"> selected</c:if>>${country[1]}</option>
        </c:forEach>
      </select>
    </p><p>
    <input class="submit" type="submit" value="Update graph">
    </p>
  </div>
</form>
<p>Download graph as
<a href="bridge-users.pdf${bridge_users_url}">PDF</a> or
<a href="bridge-users.svg${bridge_users_url}">SVG</a>.</p>
<hr>
<a name="bridge-users-table"></a>
<p><b>Top-10 countries by bridge users:</b></p>
<form action="users.html#bridge-users-table">
  <div class="formrow">
    <input type="hidden" name="table" value="bridge-users">
    <p>
    <label>Start date (yyyy-mm-dd):</label>
      <input type="text" name="start" size="10"
             value="<c:choose><c:when test="${fn:length(bridge_users_start) == 0}">${default_start_date}</c:when><c:otherwise>${bridge_users_start[0]}</c:otherwise></c:choose>">
    <label>End date (yyyy-mm-dd):</label>
      <input type="text" name="end" size="10"
             value="<c:choose><c:when test="${fn:length(bridge_users_end) == 0}">${default_end_date}</c:when><c:otherwise>${bridge_users_end[0]}</c:otherwise></c:choose>">
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
  <c:forEach var="row" items="${bridge_users_tabledata}">
    <tr>
      <td><a href="users.html?graph=bridge-users&country=${row['cc']}#bridge-users">${row['country']}</a>&emsp;</td>
      <td>${row['abs']} (<fmt:formatNumber type="number" minFractionDigits="2" value="${row['rel']}" /> %)</td>
    </tr>
  </c:forEach>
</table>
<hr>
<p><a href="csv/bridge-users.csv">CSV</a> file containing all data.</p>
<p><a href="csv/monthly-users-peak.csv">CSV</a> file containing peak daily
Tor users (direct and bridge) per month by country.</p>
<p><a href="csv/monthly-users-average.csv">CSV</a> file containing average
daily Tor users (direct and bridge) per month by country.</p>
<br>

<a name="userstats"></a>
<h3><a href="#userstats" class="anchor">New approach to estimating daily
Tor users (BETA)</a></h3>
<br>
<p>As of April 2013, we are experimenting with a new approach to estimating
daily Tor users.
The new approach works very similar to the existing approach to estimate
directly connecting users, but can also be applied to bridge users.
This new approach can break down user numbers by country, pluggable
transport, and IP version.
See tech report on
<a href="https://research.torproject.org/techreports/counting-daily-bridge-users-2012-10-24.pdf">Counting daily bridge users</a>
and the
<a href="https://gitweb.torproject.org/metrics-tasks.git/tree/HEAD:/task-8462">source code</a>
for details.

<p><font color="red">Note that this approach should be considered
experimental and absolute numbers should be taken with care!</font></p>

<a name="userstats-relay-country"></a>
<p><b>Direct users by country (BETA):</b></p>

<p>
<font color="red">In contrast to the graphs above, this graph is based on
requests to directory mirrors <i>and</i> directory authorities.
That is why the numbers here are higher.
It's yet to be decided which approach is more correct.</font>
</p>

<img src="userstats-relay-country.png${userstats_relay_country_url}"
     width="576" height="360" alt="Direct users by country graph (BETA)">
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
    <input class="submit" type="submit" value="Update graph">
    </p>
  </div>
</form>
<p>Download graph as
<a href="userstats-relay-country.pdf${userstats_relay_country_url}">PDF</a> or
<a href="userstats-relay-country.svg${userstats_relay_country_url}">SVG</a>.</p>
<hr>
<a name="userstats-relay-table"></a>
<p><b>Top-10 countries by directly connecting users (BETA):</b></p>
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
      <td><a href="users.html?graph=userstats-relay-country&country=${row['cc']}#userstats-relay">${row['country']}</a>&emsp;</td>
      <td>${row['abs']} (<fmt:formatNumber type="number" minFractionDigits="2" value="${row['rel']}" /> %)</td>
    </tr>
  </c:forEach>
</table>
<hr>

<a name="userstats-bridge-country"></a>
<p><b>Bridge users by country (BETA):</b></p>

<p>
<font color="red">In contrast to the bridge-user graph above, this graph
uses directory requests to estimate user numbers, not unique IP address sets.
It's yet to be decided which approach is more correct.</font>
</p>

<img src="userstats-bridge-country.png${userstats_bridge_country_url}"
     width="576" height="360" alt="Bridge users by country graph (BETA)">
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
<p><b>Top-10 countries by bridge users (BETA):</b></p>
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
      <td><a href="users.html?graph=userstats-bridge-country&country=${row['cc']}#userstats-bridge">${row['country']}</a>&emsp;</td>
      <td>${row['abs']} (<fmt:formatNumber type="number" minFractionDigits="2" value="${row['rel']}" /> %)</td>
    </tr>
  </c:forEach>
</table>
<hr>

<a name="userstats-bridge-transport"></a>
<p><b>Bridge users by transport (BETA):</b></p>

<p>
<font color="red">Almost none of the currently running bridges report the
transport name of connecting users, which is why non-OR transport usage is
so low.
By default, we consider all users of a bridge OR transport users, unless told
otherwise.
Non-OR transport numbers will become more accurate over time.</font>
</p>

<img src="userstats-bridge-transport.png${userstats_bridge_transport_url}"
     width="576" height="360" alt="Bridge users by transport graph (BETA)">
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
<p><b>Bridge users by IP version (BETA):</b></p>

<p>
<font color="red">Not all of the currently running bridges report the
IP version of connecting users.
By default, we consider all users of a bridge IPv4 users, unless told
otherwise.
IPv6 numbers will become more accurate over time.</font>
</p>

<img src="userstats-bridge-version.png${userstats_bridge_version_url}"
     width="576" height="360" alt="Bridge users by IP version graph (BETA)">
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

<p><a href="csv/userstats.csv">CSV</a> file containing new user
estimates (BETA).</p>
<br>

    </div>
  </div>
  <div class="bottom" id="bottom">
    <%@ include file="footer.jsp"%>
  </div>
</body>
</html>
