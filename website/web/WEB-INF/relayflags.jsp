<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
  <title>Tor Metrics: Relays with Exit, Fast, Guard, Stable, and HSDir flags</title>
  <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">
  <link href="/css/stylesheet-ltr.css" type="text/css" rel="stylesheet">
  <link href="/images/favicon.ico" type="image/x-icon" rel="shortcut icon">
</head>
<body>
  <div class="center">
    <%@ include file="banner.jsp"%>
    <div class="main-column">

<h3>Tor Metrics: Relays with Exit, Fast, Guard, Stable, and HSDir flags</h3>
<br>
<p>The directory authorities assign certain flags to relays that clients
use for their path selection decisions. The following graph shows the
average number of relays with these flags assigned.</p>
<img src="relayflags.png${relayflags_url}"
     width="576" height="360" alt="Relay flags graph">
<form action="relayflags.html">
  <div class="formrow">
    <input type="hidden" name="graph" value="relayflags">
    <p>
    <label>Start date (yyyy-mm-dd):</label>
      <input type="text" name="start" size="10"
             value="<c:choose><c:when test="${fn:length(relayflags_start) == 0}">${default_start_date}</c:when><c:otherwise>${relayflags_start[0]}</c:otherwise></c:choose>">
    <label>End date (yyyy-mm-dd):</label>
      <input type="text" name="end" size="10"
             value="<c:choose><c:when test="${fn:length(relayflags_end) == 0}">${default_end_date}</c:when><c:otherwise>${relayflags_end[0]}</c:otherwise></c:choose>">
    </p><p>
      <label>Relay flags: </label>
      <input type="checkbox" name="flag" value="Running"<c:if test="${fn:length(relayflags_flag) == 0 or fn:contains(fn:join(relayflags_flag, ','), 'Running')}"> checked</c:if>> Running
      <input type="checkbox" name="flag" value="Exit"<c:if test="${fn:length(relayflags_flag) == 0 or fn:contains(fn:join(relayflags_flag, ','), 'Exit')}"> checked</c:if>> Exit
      <input type="checkbox" name="flag" value="Fast"<c:if test="${fn:length(relayflags_flag) == 0 or fn:contains(fn:join(relayflags_flag, ','), 'Fast')}"> checked</c:if>> Fast
      <input type="checkbox" name="flag" value="Guard"<c:if test="${fn:length(relayflags_flag) == 0 or fn:contains(fn:join(relayflags_flag, ','), 'Guard')}"> checked</c:if>> Guard
      <input type="checkbox" name="flag" value="Stable"<c:if test="${fn:length(relayflags_flag) == 0 or fn:contains(fn:join(relayflags_flag, ','), 'Stable')}"> checked</c:if>> Stable
      <input type="checkbox" name="flag" value="HSDir"<c:if test="${fn:length(relayflags_flag) > 0 and fn:contains(fn:join(relayflags_flag, ','), 'HSDir')}"> checked</c:if>> HSDir
    </p><p>
    <input class="submit" type="submit" value="Update graph">
    </p>
  </div>
</form>
<p>Download graph as
<a href="relayflags.pdf${relayflags_url}">PDF</a> or
<a href="relayflags.svg${relayflags_url}">SVG</a>.</p>
<p><a href="stats/servers.csv">CSV</a> file containing all data.</p>
<br>

    </div>
  </div>
  <div class="bottom" id="bottom">
    <%@ include file="footer.jsp"%>
  </div>
</body>
</html>
