<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
  <title>Tor Metrics Portal: Performance</title>
  <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">
  <link href="/css/stylesheet-ltr.css" type="text/css" rel="stylesheet">
  <link href="/images/favicon.ico" type="image/x-icon" rel="shortcut icon">
</head>
<body>
  <div class="center">
    <%@ include file="banner.jsp"%>
    <div class="main-column">
<h2>Tor Metrics Portal: Performance</h2>
<br>
<h3>Time to download files over Tor</h3>
<br>
<p>The following graphs show the performance of the Tor network as
experienced by its users. The graphs contain the average (median) time to
request files of three different sizes over Tor as well as first and third
quartile of request times.</p>
<a name="torperf"></a>
<img src="torperf.png${torperf_url}"
     width="576" height="360" alt="Torperf graph">
<form action="performance.html#torperf">
  <div class="formrow">
    <input type="hidden" name="graph" value="torperf">
    <p>
    <label>Start date (yyyy-mm-dd):</label>
      <input type="text" name="start" size="10"
             value="${torperf_start[0]}">
    <label>End date (yyyy-mm-dd):</label>
      <input type="text" name="end" size="10"
             value="${torperf_end[0]}">
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
      Resolution: <select name="dpi">
        <option value="72"<c:if test="${torperf_dpi[0] eq '72'}"> selected</c:if>>Screen - 576x360</option>
        <option value="150"<c:if test="${torperf_dpi[0] eq '150'}"> selected</c:if>>Print low - 1200x750</option>
        <option value="300"<c:if test="${torperf_dpi[0] eq '300'}"> selected</c:if>>Print high - 2400x1500</option>
      </select>
    </p><p>
    <input class="submit" type="submit" value="Update graph">
    </p>
  </div>
</form>
<p><a href="csv/torperf.csv">CSV</a> file containing all data.</p>

<br>
<h3>Timeouts and failures of downloading files over Tor</h3>
<br>
<p>The following graphs show the fraction of timeouts and failures of
downloading files over Tor as experienced by users.
A timeout occurs when a 50 KiB (1 MiB, 5 MiB) download does not complete
within 4:55 minutes (29:55 minutes, 59:55 minutes).
A failure occurs when the download completes, but the response is smaller
than 50 KiB (1 MiB, 5 MiB).</p>
<a name="torperf-failures"></a>
<img src="torperf-failures.png${torperf_failures_url}"
     width="576" height="360" alt="Torperf failures graph">
<form action="performance.html#torperf-failures">
  <div class="formrow">
    <input type="hidden" name="graph" value="torperf-failures">
    <p>
    <label>Start date (yyyy-mm-dd):</label>
      <input type="text" name="start" size="10"
             value="${torperf_failures_start[0]}">
    <label>End date (yyyy-mm-dd):</label>
      <input type="text" name="end" size="10"
             value="${torperf_failures_end[0]}">
    </p><p>
      Source:
      <input type="radio" name="source" value="all"<c:if test="${fn:length(torperf_failures_source) == 0 or torperf_failures_source[0] eq 'all'}"> checked</c:if>> all
      <input type="radio" name="source" value="torperf"<c:if test="${torperf_failures_source[0] eq 'torperf'}"> checked</c:if>> torperf
      <input type="radio" name="source" value="moria"<c:if test="${torperf_failures_source[0] eq 'moria'}"> checked</c:if>> moria
      <input type="radio" name="source" value="siv"<c:if test="${torperf_failures_source[0] eq 'siv'}"> checked</c:if>> siv
    </p><p>
      <label>File size: </label>
      <input type="radio" name="filesize" value="50kb"<c:if test="${fn:length(torperf_failures_filesize) == 0 or torperf_failures_filesize[0] eq '50kb'}"> checked</c:if>> 50 KiB
      <input type="radio" name="filesize" value="1mb"<c:if test="${torperf_failures_filesize[0] eq '1mb'}"> checked</c:if>> 1 MiB
      <input type="radio" name="filesize" value="5mb"<c:if test="${torperf_failures_filesize[0] eq '5mb'}"> checked</c:if>> 5 MiB
    </p><p>
      Resolution: <select name="dpi">
        <option value="72"<c:if test="${torperf_failures_dpi[0] eq '72'}"> selected</c:if>>Screen - 576x360</option>
        <option value="150"<c:if test="${torperf_failures_dpi[0] eq '150'}"> selected</c:if>>Print low - 1200x750</option>
        <option value="300"<c:if test="${torperf_failures_dpi[0] eq '300'}"> selected</c:if>>Print high - 2400x1500</option>
      </select>
    </p><p>
    <input class="submit" type="submit" value="Update graph">
    </p>
  </div>
</form>
<p><a href="csv/torperf-failures.csv">CSV</a> file containing all data.</p>

<br>
<h3>Fraction of connections used uni-/bidirectionally</h3>
<br>
<p>The following graph shows the fraction of connections that is used
uni- or bi-directionally.  Every 10 seconds, relays determine for every
connection whether they read and wrote less than a threshold of 20 KiB.
Connections below this threshold are excluded from these statistics.  For
the remaining connections, relays report whether they read/wrote at least
10 times as many bytes as they wrote/read.  If so, they classify a
connection as "Mostly reading" or "Mostly writing," respectively.  All
other connections are classified as "Both reading and writing."  After
classifying connections, read and write counters are reset for the next
10-second interval.  Statistics are aggregated over 24 hours.</p>
<a name="connbidirect"></a>
<img src="connbidirect.png${connbidirect_url}"
     width="576" height="360"
     alt="Fraction of direct connections used uni-/bidirectionally">
<form action="performance.html#connbidirect">
  <div class="formrow">
    <input type="hidden" name="graph" value="connbidirect">
    <p>
    <label>Start date (yyyy-mm-dd):</label>
      <input type="text" name="start" size="10"
             value="${connbidirect_start[0]}">
    <label>End date (yyyy-mm-dd):</label>
      <input type="text" name="end" size="10"
             value="${connbidirect_end[0]}">
    </p><p>
      Resolution: <select name="dpi">
        <option value="72"<c:if test="${connbidirect_dpi[0] eq '72'}"> selected</c:if>>Screen - 576x360</option>
        <option value="150"<c:if test="${connbidirect_dpi[0] eq '150'}"> selected</c:if>>Print low - 1200x750</option>
        <option value="300"<c:if test="${connbidirect_dpi[0] eq '300'}"> selected</c:if>>Print high - 2400x1500</option>
      </select>
    </p><p>
    <input class="submit" type="submit" value="Update graph">
    </p>
  </div>
</form>
<p><a href="csv/connbidirect.csv">CSV</a> file containing all data.</p>
<br>

    </div>
  </div>
  <div class="bottom" id="bottom">
    <%@ include file="footer.jsp"%>
  </div>
</body>
</html>
