<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
  <title>Tor Metrics Portal: Network</title>
  <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">
  <link href="/css/stylesheet-ltr.css" type="text/css" rel="stylesheet">
  <link href="/images/favicon.ico" type="image/x-icon" rel="shortcut icon">
</head>
<body>
  <div class="center">
    <%@ include file="banner.jsp"%>
    <div class="main-column">
<h2>Tor Metrics Portal: Network</h2>
<br>
<h3>Relays and bridges in the network</h3>
<br>
<p>The following graph shows the average daily number of relays and
bridges in the network.</p>
<a name="networksize"></a>
<img src="networksize.png${networksize_url}"
     width="576" height="360" alt="Network size graph">
<form action="network.html#networksize">
  <div class="formrow">
    <input type="hidden" name="graph" value="networksize">
    <p>
    <label>Start date (yyyy-mm-dd):</label>
      <input type="text" name="start" size="10"
             value="${networksize_start[0]}">
    <label>End date (yyyy-mm-dd):</label>
      <input type="text" name="end" size="10"
             value="${networksize_end[0]}">
    </p><p>
      Resolution: <select name="dpi">
        <option value="72"<c:if test="${networksize_dpi[0] eq '72'}"> selected</c:if>>Screen - 576x360</option>
        <option value="150"<c:if test="${networksize_dpi[0] eq '150'}"> selected</c:if>>Print low - 1200x750</option>
        <option value="300"<c:if test="${networksize_dpi[0] eq '300'}"> selected</c:if>>Print high - 2400x1500</option>
      </select>
    </p><p>
    <input class="submit" type="submit" value="Update graph">
    </p>
  </div>
</form>
<p><a href="csv/networksize.csv">CSV</a> file containing all data.</p>
<br>

<h3>Relays with Exit, Fast, Guard, and Stable flags</h3>
<br>
<p>The directory authorities assign certain flags to relays that clients
use for their path selection decisions. The following graph shows the
average number of relays with these flags assigned.</p>
<a name="relayflags"></a>
<img src="relayflags.png${relayflags_url}"
     width="576" height="360" alt="Relay flags graph">
<form action="network.html#relayflags">
  <div class="formrow">
    <input type="hidden" name="graph" value="relayflags">
    <p>
    <label>Start date (yyyy-mm-dd):</label>
      <input type="text" name="start" size="10"
             value="${relayflags_start[0]}">
    <label>End date (yyyy-mm-dd):</label>
      <input type="text" name="end" size="10"
             value="${relayflags_end[0]}">
    </p><p>
      <label>Relay flags: </label>
      <input type="checkbox" name="flag" value="Running"> Running
      <input type="checkbox" name="flag" value="Exit"> Exit
      <input type="checkbox" name="flag" value="Fast"> Fast
      <input type="checkbox" name="flag" value="Guard"> Guard
      <input type="checkbox" name="flag" value="Stable"> Stable
    </p><p>
      Resolution: <select name="dpi">
        <option value="72"<c:if test="${relayflags_dpi[0] eq '72'}"> selected</c:if>>Screen - 576x360</option>
        <option value="150"<c:if test="${relayflags_dpi[0] eq '150'}"> selected</c:if>>Print low - 1200x750</option>
        <option value="300"<c:if test="${relayflags_dpi[0] eq '300'}"> selected</c:if>>Print high - 2400x1500</option>
      </select>
    </p><p>
    <input class="submit" type="submit" value="Update graph">
    </p>
  </div>
</form>
<p><a href="csv/relayflags.csv">CSV</a> file containing all data.</p>
<br>

<h3>Relays with Exit, Fast, Guard, and Stable flags on 1-hour detail</h3>
<br>
<p>The same graph on the average number of relays with flags assigned is
available on 1-hour detail.</p>
<a name="relayflags-hour"></a>
<img src="relayflags-hour.png${relayflags_hour_url}"
     width="576" height="360" alt="Relay flags graph">
<form action="network.html#relayflags-hour">
  <div class="formrow">
    <input type="hidden" name="graph" value="relayflags-hour">
    <p>
    <label>Start date (yyyy-mm-dd):</label>
      <input type="text" name="start" size="10"
             value="${relayflags_hour_start[0]}">
    <label>End date (yyyy-mm-dd):</label>
      <input type="text" name="end" size="10"
             value="${relayflags_hour_end[0]}">
    </p><p>
      <label>Relay flags: </label>
      <input type="checkbox" name="flag" value="Running"> Running
      <input type="checkbox" name="flag" value="Exit"> Exit
      <input type="checkbox" name="flag" value="Fast"> Fast
      <input type="checkbox" name="flag" value="Guard"> Guard
      <input type="checkbox" name="flag" value="Stable"> Stable
    </p><p>
      Resolution: <select name="dpi">
        <option value="72"<c:if test="${relayflags_hour_dpi[0] eq '72'}"> selected</c:if>>Screen - 576x360</option>
        <option value="150"<c:if test="${relayflags_hour_dpi[0] eq '150'}"> selected</c:if>>Print low - 1200x750</option>
        <option value="300"<c:if test="${relayflags_hour_dpi[0] eq '300'}"> selected</c:if>>Print high - 2400x1500</option>
      </select>
    </p><p>
    <input class="submit" type="submit" value="Update graph">
    </p>
  </div>
</form>
<p><a href="csv/relayflags-hour.csv">CSV</a> file containing all data.</p>
<br>

<h3>Relays by version</h3>
<br>
<p>Relays report the Tor version that they are running to the directory
authorities. The following graph shows the number of relays by
version.</p>
<a name="versions"></a>
<img src="versions.png${versions_url}"
     width="576" height="360" alt="Relay versions graph">
<form action="network.html#versions">
  <div class="formrow">
    <input type="hidden" name="graph" value="versions">
    <p>
    <label>Start date (yyyy-mm-dd):</label>
      <input type="text" name="start" size="10"
             value="${versions_start[0]}">
    <label>End date (yyyy-mm-dd):</label>
      <input type="text" name="end" size="10"
             value="${versions_end[0]}">
    </p><p>
      Resolution: <select name="dpi">
        <option value="72"<c:if test="${versions_dpi[0] eq '72'}"> selected</c:if>>Screen - 576x360</option>
        <option value="150"<c:if test="${versions_dpi[0] eq '150'}"> selected</c:if>>Print low - 1200x750</option>
        <option value="300"<c:if test="${versions_dpi[0] eq '300'}"> selected</c:if>>Print high - 2400x1500</option>
      </select>
    </p><p>
    <input class="submit" type="submit" value="Update graph">
    </p>
  </div>
</form>
<p><a href="csv/versions.csv">CSV</a> file containing all data.</p>
<p><a href="csv/current-platform-strings.csv">CSV</a> file containing
platform strings of currently running relays.</p>
<br>

<h3>Relays by platform</h3>
<br>
<p>Relays report the operating system they are running to the directory
authorities. The following graph shows the number of relays by
platform.</p>
<a name="platforms"></a>
<img src="platforms.png${platforms_url}"
     width="576" height="360" alt="Relay platforms graph">
<form action="network.html#platforms">
  <div class="formrow">
    <input type="hidden" name="graph" value="platforms">
    <p>
    <label>Start date (yyyy-mm-dd):</label>
      <input type="text" name="start" size="10"
             value="${platforms_start[0]}">
    <label>End date (yyyy-mm-dd):</label>
      <input type="text" name="end" size="10"
             value="${platforms_end[0]}">
    </p><p>
      Resolution: <select name="dpi">
        <option value="72"<c:if test="${platforms_dpi[0] eq '72'}"> selected</c:if>>Screen - 576x360</option>
        <option value="150"<c:if test="${platforms_dpi[0] eq '150'}"> selected</c:if>>Print low - 1200x750</option>
        <option value="300"<c:if test="${platforms_dpi[0] eq '300'}"> selected</c:if>>Print high - 2400x1500</option>
      </select>
    </p><p>
    <input class="submit" type="submit" value="Update graph">
    </p>
  </div>
</form>
<p><a href="csv/platforms.csv">CSV</a> file containing all data.</p>
<p><a href="csv/current-platform-strings.csv">CSV</a> file containing
platform strings of currently running relays.</p>
<br>

<h3>Total relay bandwidth in the network</h3>
<br>
<p>Relays report how much bandwidth they are willing to contribute and how
many bytes they have read and written in the past 24 hours. The following
graph shows total advertised bandwidth and bandwidth history of all relays
in the network.</p>
<a name="bandwidth"></a>
<img src="bandwidth.png${bandwidth_url}"
     width="576" height="360" alt="Relay bandwidth graph">
<form action="network.html#bandwidth">
  <div class="formrow">
    <input type="hidden" name="graph" value="bandwidth">
    <p>
    <label>Start date (yyyy-mm-dd):</label>
      <input type="text" name="start" size="10"
             value="${bandwidth_start[0]}">
    <label>End date (yyyy-mm-dd):</label>
      <input type="text" name="end" size="10"
             value="${bandwidth_end[0]}">
    </p><p>
      Resolution: <select name="dpi">
        <option value="72"<c:if test="${bandwidth_dpi[0] eq '72'}"> selected</c:if>>Screen - 576x360</option>
        <option value="150"<c:if test="${bandwidth_dpi[0] eq '150'}"> selected</c:if>>Print low - 1200x750</option>
        <option value="300"<c:if test="${bandwidth_dpi[0] eq '300'}"> selected</c:if>>Print high - 2400x1500</option>
      </select>
    </p><p>
    <input class="submit" type="submit" value="Update graph">
    </p>
  </div>
</form>
<p><a href="csv/bandwidth.csv">CSV</a> file containing all data.</p>
<br>

<h3>Number of bytes spent on answering directory requests</h3>
<br>
<p>Relays running on 0.2.2.15-alpha or higher report the number of bytes
they spend on answering directory requests. The following graph shows
total written and read bytes as well as written and read dir bytes. The
dir bytes are extrapolated from those relays who report them to reflect
the number of written and read dir bytes by all relays.</p>
<a name="dirbytes"></a>
<img src="dirbytes.png${dirbytes_url}"
     width="576" height="360" alt="Dir bytes graph">
<form action="network.html#dirbytes">
  <div class="formrow">
    <input type="hidden" name="graph" value="dirbytes">
    <p>
    <label>Start date (yyyy-mm-dd):</label>
      <input type="text" name="start" size="10"
             value="${dirbytes_start[0]}">
    <label>End date (yyyy-mm-dd):</label>
      <input type="text" name="end" size="10"
             value="${dirbytes_end[0]}">
    </p><p>
      Resolution: <select name="dpi">
        <option value="72"<c:if test="${dirbytes_dpi[0] eq '72'}"> selected</c:if>>Screen - 576x360</option>
        <option value="150"<c:if test="${dirbytes_dpi[0] eq '150'}"> selected</c:if>>Print low - 1200x750</option>
        <option value="300"<c:if test="${dirbytes_dpi[0] eq '300'}"> selected</c:if>>Print high - 2400x1500</option>
      </select>
    </p><p>
    <input class="submit" type="submit" value="Update graph">
    </p>
  </div>
</form>
<p><a href="csv/dirbytes.csv">CSV</a> file containing all data.</p>
<br>
    </div>
  </div>
  <div class="bottom" id="bottom">
    <%@ include file="footer.jsp"%>
  </div>
</body>
</html>
