<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
  <title>Tor Metrics: Advertised bandwidth and bandwidth history by relay flags</title>
  <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">
  <link href="/css/stylesheet-ltr.css" type="text/css" rel="stylesheet">
  <link href="/images/favicon.ico" type="image/x-icon" rel="shortcut icon">
</head>
<body>
  <div class="center">
    <%@ include file="banner.jsp"%>
    <div class="main-column">

<h3>Tor Metrics: Advertised bandwidth and bandwidth history by relay flags</h3>
<br>
<p>The following graph shows
<a href="about.html#advertised-bandwidth">advertised</a> and
<a href="about.html#bandwidth-history">consumed bandwidth</a> of relays
with either "Exit" or "Guard" <a href="about.html#relay-flag">flags</a>
assigned by the directory authorities.
These sets are not distinct, because a relay that has both the "Exit" and
"Guard" flags assigned will be included in both sets.</p>
<img src="bandwidth-flags.png${bandwidth_flags_url}"
     width="576" height="360" alt="Advertised bandwidth and bandwidth history by relay flags graph">
<form action="bandwidth-flags.html">
  <div class="formrow">
    <input type="hidden" name="graph" value="bandwidth-flags">
    <p>
    <label>Start date (yyyy-mm-dd):</label>
      <input type="text" name="start" size="10"
             value="<c:choose><c:when test="${fn:length(bandwidth_flags_start) == 0}">${default_start_date}</c:when><c:otherwise>${bandwidth_flags_start[0]}</c:otherwise></c:choose>">
    <label>End date (yyyy-mm-dd):</label>
      <input type="text" name="end" size="10"
             value="<c:choose><c:when test="${fn:length(bandwidth_flags_end) == 0}">${default_end_date}</c:when><c:otherwise>${bandwidth_flags_end[0]}</c:otherwise></c:choose>">
    </p><p>
    <input class="submit" type="submit" value="Update graph">
    </p>
  </div>
</form>
<p>Download graph as
<a href="bandwidth-flags.pdf${bandwidth_flags_url}">PDF</a> or
<a href="bandwidth-flags.svg${bandwidth_flags_url}">SVG</a>.</p>
<p><a href="stats/bandwidth.csv">CSV</a> file containing all data.</p>
<br>

    </div>
  </div>
  <div class="bottom" id="bottom">
    <%@ include file="footer.jsp"%>
  </div>
</body>
</html>
