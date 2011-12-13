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
      href="papers/detector-2011-09-09.pdf">BETA</a>)
      <input type="checkbox" name="events" value="on"
        <c:if test="${direct_users_events[0] eq 'on'}"> checked</c:if>
      ></input>
    </p><p>
      Resolution: <select name="dpi">
        <option value="72"<c:if test="${direct_users_dpi[0] eq '72'}"> selected</c:if>>Screen - 576x360</option>
        <option value="150"<c:if test="${direct_users_dpi[0] eq '150'}"> selected</c:if>>Print low - 1200x750</option>
        <option value="300"<c:if test="${direct_users_dpi[0] eq '300'}"> selected</c:if>>Print high - 2400x1500</option>
      </select>
    </p><p>
    <input class="submit" type="submit" value="Update graph">
    </p>
  </div>
</form>
<hr>
<a name="direct-users-table"></a>
<p><b>Top-10 countries by directly connecting users:</b></p>
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
<br>
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
<hr>
<a name="censorship-events"></a>
<p><b>Top-10 countries by possible censorship events (<a
      href="papers/detector-2011-09-09.pdf">BETA</a>):</b></p>
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
<br>
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
      Resolution: <select name="dpi">
        <option value="72"<c:if test="${bridge_users_dpi[0] eq '72'}"> selected</c:if>>Screen - 576x360</option>
        <option value="150"<c:if test="${bridge_users_dpi[0] eq '150'}"> selected</c:if>>Print low - 1200x750</option>
        <option value="300"<c:if test="${bridge_users_dpi[0] eq '300'}"> selected</c:if>>Print high - 2400x1500</option>
      </select>
    </p><p>
    <input class="submit" type="submit" value="Update graph">
    </p>
  </div>
</form>
<hr>
<p><a href="csv/bridge-users.csv">CSV</a> file containing all data.</p>
<p><a href="csv/monthly-users-peak.csv">CSV</a> file containing peak daily
Tor users (direct and bridge) per month by country.</p>
<p><a href="csv/monthly-users-average.csv">CSV</a> file containing average
daily Tor users (direct and bridge) per month by country.</p>
<br>
    </div>
  </div>
  <div class="bottom" id="bottom">
    <%@ include file="footer.jsp"%>
  </div>
</body>
</html>
