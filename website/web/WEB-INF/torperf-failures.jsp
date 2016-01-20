<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
  <title>Tor Metrics &mdash; Timeouts and failures of downloading files over Tor</title>
  <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">
  <link href="/css/stylesheet-ltr.css" type="text/css" rel="stylesheet">
  <link href="/images/favicon.ico" type="image/x-icon" rel="shortcut icon">
</head>
<body>
  <div class="center">
    <%@ include file="banner.jsp"%>
    <div class="main-column">

<h2><a href="/"><img src="/images/metrics-wordmark-small.png" width="138" height="18" alt="Metrics wordmark"></a> &mdash; Timeouts and failures of downloading files over Tor</h2>
<br>
<p>The following graph shows the fraction of timeouts and failures when
downloading static files of different sizes over Tor.
A timeout occurs when a download does not complete within the scheduled
time, in which case it is aborted in order not to overlap with the next
scheduled download.
A failure occurs when the download completes, but the response is smaller
than expected.</p>
<img src="torperf-failures.png${torperf_failures_url}"
     width="576" height="360" alt="Torperf failures graph">
<form action="torperf-failures.html">
  <div class="formrow">
    <input type="hidden" name="graph" value="torperf-failures">
    <p>
    <label>Start date (yyyy-mm-dd):
      <input type="text" name="start" size="10"
             value="<c:choose><c:when test="${fn:length(torperf_failures_start) == 0}">${default_start_date}</c:when><c:otherwise>${torperf_failures_start[0]}</c:otherwise></c:choose>"></label>
    <label>End date (yyyy-mm-dd):
      <input type="text" name="end" size="10"
             value="<c:choose><c:when test="${fn:length(torperf_failures_end) == 0}">${default_end_date}</c:when><c:otherwise>${torperf_failures_end[0]}</c:otherwise></c:choose>"></label>
    </p><p>
      Source:
      <label><input type="radio" name="source" value="all"<c:if test="${fn:length(torperf_failures_source) == 0 or torperf_failures_source[0] eq 'all'}"> checked</c:if>> all</label>
      <label><input type="radio" name="source" value="torperf"<c:if test="${torperf_failures_source[0] eq 'torperf'}"> checked</c:if>> torperf</label>
      <label><input type="radio" name="source" value="moria"<c:if test="${torperf_failures_source[0] eq 'moria'}"> checked</c:if>> moria</label>
      <label><input type="radio" name="source" value="siv"<c:if test="${torperf_failures_source[0] eq 'siv'}"> checked</c:if>> siv</label>
    </p><p>
      <label>File size: </label>
      <input type="radio" name="filesize" value="50kb"<c:if test="${fn:length(torperf_failures_filesize) == 0 or torperf_failures_filesize[0] eq '50kb'}"> checked</c:if>> 50 KiB
      <input type="radio" name="filesize" value="1mb"<c:if test="${torperf_failures_filesize[0] eq '1mb'}"> checked</c:if>> 1 MiB
      <input type="radio" name="filesize" value="5mb"<c:if test="${torperf_failures_filesize[0] eq '5mb'}"> checked</c:if>> 5 MiB
    </p><p>
    <input class="submit" type="submit" value="Update graph">
    </p>
  </div>
</form>
<p>Download graph as
<a href="torperf-failures.pdf${torperf_failures_url}">PDF</a> or
<a href="torperf-failures.svg${torperf_failures_url}">SVG</a>.</p>
<br>

<h4>Related metrics</h4>
<ul>
<li><a href="torperf.html">Graph: Time to download files over Tor</a></li>
<li><a href="torperf-data.html">Data: Performance of downloading static files over Tor</a></li>
</ul>

    </div>
  </div>
  <div class="bottom" id="bottom">
    <%@ include file="footer.jsp"%>
  </div>
</body>
</html>
