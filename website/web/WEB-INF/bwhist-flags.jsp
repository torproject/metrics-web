<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
  <title>Tor Metrics: Relay bandwidth by Exit and/or Guard flags</title>
  <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">
  <link href="/css/stylesheet-ltr.css" type="text/css" rel="stylesheet">
  <link href="/images/favicon.ico" type="image/x-icon" rel="shortcut icon">
</head>
<body>
  <div class="center">
    <%@ include file="banner.jsp"%>
    <div class="main-column">

<h3>Tor Metrics: Relay bandwidth by Exit and/or Guard flags</h3>
<br>
<p>The following graph shows the
<a href="about.html#bandwidth-history">consumed bandwidth</a> reported by
relays, subdivided into four distinct subsets by assigned "Exit" and/or
"Guard" <a href="about.html#relay-flag">flags</a>.</p>
<img src="bwhist-flags.png${bwhist_flags_url}"
     width="576" height="360" alt="Relay bandwidth by flags graph">
<form action="bwhist-flags.html">
  <div class="formrow">
    <input type="hidden" name="graph" value="bwhist-flags">
    <p>
    <label>Start date (yyyy-mm-dd):</label>
      <input type="text" name="start" size="10"
             value="<c:choose><c:when test="${fn:length(bwhist_flags_start) == 0}">${default_start_date}</c:when><c:otherwise>${bwhist_flags_start[0]}</c:otherwise></c:choose>">
    <label>End date (yyyy-mm-dd):</label>
      <input type="text" name="end" size="10"
             value="<c:choose><c:when test="${fn:length(bwhist_flags_end) == 0}">${default_end_date}</c:when><c:otherwise>${bwhist_flags_end[0]}</c:otherwise></c:choose>">
    </p><p>
    <input class="submit" type="submit" value="Update graph">
    </p>
  </div>
</form>
<p>Download graph as
<a href="bwhist-flags.pdf${bwhist_flags_url}">PDF</a> or
<a href="bwhist-flags.svg${bwhist_flags_url}">SVG</a>.</p>
<p><a href="stats/bandwidth.csv">CSV</a> file containing all data.</p>
<br>

    </div>
  </div>
  <div class="bottom" id="bottom">
    <%@ include file="footer.jsp"%>
  </div>
</body>
</html>
