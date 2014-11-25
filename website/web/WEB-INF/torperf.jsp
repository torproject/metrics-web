<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
  <title>Tor Metrics: Time to download files over Tor</title>
  <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">
  <link href="/css/stylesheet-ltr.css" type="text/css" rel="stylesheet">
  <link href="/images/favicon.ico" type="image/x-icon" rel="shortcut icon">
</head>
<body>
  <div class="center">
    <%@ include file="banner.jsp"%>
    <div class="main-column">

<h3>Tor Metrics: Time to download files over Tor</h3>
<br>
<p>The following graph shows overall performance when downloading static
files of different sizes over Tor.
The graph shows the range of measurements from first to third quartile,
and highlights the median.
The slowest and fastest quarter of measurements are omitted from the
graph.</p>
<img src="torperf.png${torperf_url}"
     width="576" height="360" alt="Torperf graph">
<form action="torperf.html">
  <div class="formrow">
    <input type="hidden" name="graph" value="torperf">
    <p>
    <label>Start date (yyyy-mm-dd):</label>
      <input type="text" name="start" size="10"
             value="<c:choose><c:when test="${fn:length(torperf_start) == 0}">${default_start_date}</c:when><c:otherwise>${torperf_start[0]}</c:otherwise></c:choose>">
    <label>End date (yyyy-mm-dd):</label>
      <input type="text" name="end" size="10"
             value="<c:choose><c:when test="${fn:length(torperf_end) == 0}">${default_end_date}</c:when><c:otherwise>${torperf_end[0]}</c:otherwise></c:choose>">
    </p><p>
      Source:
      <input type="radio" name="source" value="all"<c:if test="${fn:length(torperf_source) == 0 or torperf_source[0] eq 'all'}"> checked</c:if>> all
      <input type="radio" name="source" value="torperf"<c:if test="${torperf_source[0] eq 'torperf'}"> checked</c:if>> torperf
      <input type="radio" name="source" value="moria"<c:if test="${torperf_source[0] eq 'moria'}"> checked</c:if>> moria
      <input type="radio" name="source" value="siv"<c:if test="${torperf_source[0] eq 'siv'}"> checked</c:if>> siv
    </p><p>
      <label>File size: </label>
      <input type="radio" name="filesize" value="50kb"<c:if test="${fn:length(torperf_filesize) == 0 or torperf_filesize[0] eq '50kb'}"> checked</c:if>> 50 KiB
      <input type="radio" name="filesize" value="1mb"<c:if test="${torperf_filesize[0] eq '1mb'}"> checked</c:if>> 1 MiB
      <input type="radio" name="filesize" value="5mb"<c:if test="${torperf_filesize[0] eq '5mb'}"> checked</c:if>> 5 MiB
    </p><p>
    <input class="submit" type="submit" value="Update graph">
    </p>
  </div>
</form>
<p>Download graph as
<a href="torperf.pdf${torperf_url}">PDF</a> or
<a href="torperf.svg${torperf_url}">SVG</a>.</p>
<p><a href="stats/torperf.csv">CSV</a> file containing all data.</p>

    </div>
  </div>
  <div class="bottom" id="bottom">
    <%@ include file="footer.jsp"%>
  </div>
</body>
</html>
