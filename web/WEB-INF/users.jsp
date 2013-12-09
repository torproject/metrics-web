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

<a name="userstats-relay-country"></a>
<p><b>Direct users by country:</b></p>

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
<p><b>Top-10 countries by directly connecting users:</b></p>
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
<p><b>Top-10 countries by possible censorship events (<a
      href="http://research.torproject.org/techreports/detector-2011-09-09.pdf">BETA</a>):</b></p>
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
<p><b>Bridge users by country:</b></p>

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
<p><b>Top-10 countries by bridge users:</b></p>
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
<p><b>Bridge users by transport:</b></p>

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
<p><b>Bridge users by IP version:</b></p>

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
<a name="questions-and-answers"></a>
<p><b>Questions and answers</b></p>
<p>
Q: How is it even possible to count users in an anonymity network?<br/>
A: We actually don't count users, but we count requests to the directories
that clients make periodically to update their list of relays and estimate
user numbers indirectly from there.
</p>
<p>
Q: Do all directories report these directory request numbers?<br/>
A: No, but we can see what fraction of directories reported them, and then
we can extrapolate the total number in the network.
</p>

<p>
Q: How do you get from these directory requests to user numbers?<br/>
A: We put in the assumption that the average client makes 10 such requests
per day.  A tor client that is connected 24/7 makes about 15 requests per
day, but not all clients are connected 24/7, so we picked the number 10
for the average client.  We simply divide directory requests by 10 and
consider the result as the number of users.
</p>

<p>
Q: So, are these distinct users per day, average number of users connected
over the day, or what?<br/>
A: Average number of users connected over the day.  We can't say how many
distinct users there are.
</p>

<p>
Q: Are these tor clients or users?  What if there's more than one user
behind a tor client?<br/>
A: Then we count those users as one.  We really count clients, but it's
more intuitive for most people to think of users, that's why we say users
and not clients.
</p>

<p>
Q: What if a user runs tor on a laptop and changes their IP address a few
times per day?  Don't you overcount that user?<br/>
A: No, because that user updates their list of relays as often as a user
that doesn't change IP address over the day.
</p>

<p>
Q: How do you know which countries users come from?<br/>
A: The directories resolve IP addresses to country codes and report these
numbers in aggregate form.  This is one of the reasons why tor ships with
a GeoIP database.
</p>

<p>
Q: Why are there so few bridge users that are not using the default OR
protocol or that are using IPv6?<br/>
A: Very few bridges report data on transports or IP versions yet, and by
default we consider requests to use the default OR protocol and IPv4.
Once more bridges report these data, the numbers will become more
accurate.
</p>

<p>
Q: Why do the graphs end 2 days in the past and not today?<br/>
A: Relays and bridges report some of the data in 24-hour intervals which
may end at any time of the day.  And after such an interval is over relays
and bridges might take another 18 hours to report the data.  We cut off
the last two days from the graphs, because we want to avoid that the last
data point in a graph indicates a recent trend change which is in fact
just an artifact of the algorithm.
</p>

<p>
Q: But I noticed that the last data point went up/down a bit since I last
looked a few hours ago.  Why is that?<br/>
A: You're an excellent observer!  The reason is that we publish user
numbers once we're confident enough that they won't change significantly
anymore.  But it's always possible that a directory reports data a few
hours after we were confident enough, but which then slightly changed the
graph.
</p>

<p>
Q: Why are no numbers available before September 2011?<br/>
A: We do have descriptor archives from before that time, but those
descriptors didn't contain all the data we use to estimate user numbers.
We do have older user numbers from an earlier estimation approach here
(add link), but we believe the current approach is more accurate.
</p>

<p>
Q: Why do you believe the current approach to estimate user numbers is
more accurate?<br/>
A: For direct users, we include all directories which we didn't do in the
old approach.  We also use histories that only contain bytes written to
answer directory requests, which is more precise than using general byte
histories.
</p>

<p>
Q: And what about the advantage of the current approach over the old one
when it comes to bridge users?<br/>
A: Oh, that's a whole different story.  We wrote a 13 page long
<a href="https://research.torproject.org/techreports/counting-daily-bridge-users-2012-10-24.pdf">technical
report</a> explaining the reasons for retiring the old approach.  But the
old data is still <a href="/data/old-user-number-estimates.tar.gz">available</a>.
tl;dr: in the old approach we measured the wrong thing, and now we measure
the right thing.
</p>

<p>
Q: Are the data and the source code for estimating these user numbers
available?<br/>
A: Sure, <a href="/data.html">data</a> and
<a href="https://gitweb.torproject.org/metrics-tasks.git/tree/HEAD:/task-8462">source
code</a> are publicly available.
</p>

<p>
Q: What are these red and blue dots indicating possible censorship
events?<br/>
A: We run an anomaly-based censorship-detection system that looks at
estimated user numbers over a series of days and predicts the user number
in the next days.  If the actual number is higher or lower, this might
indicate a possible censorship event or release of censorship.  For more
details, see our
<a href="https://research.torproject.org/techreports/detector-2011-09-09.pdf">technical
report</a>.
</p>

    </div>
  </div>
  <div class="bottom" id="bottom">
    <%@ include file="footer.jsp"%>
  </div>
</body>
</html>
