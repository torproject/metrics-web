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

<h3>Relays by country</h3>
<br>
<p>The following graph shows the average daily number of relays by
country.</p>
<a name="relaycountries"></a>
<img src="relaycountries.png${relaycountries_url}"
     width="576" height="360" alt="Relay countries graph">
<form action="network.html#relaycountries">
  <div class="formrow">
    <input type="hidden" name="graph" value="relaycountries">
    <p>
    <label>Start date (yyyy-mm-dd):</label>
      <input type="text" name="start" size="10"
             value="${relaycountries_start[0]}">
    <label>End date (yyyy-mm-dd):</label>
      <input type="text" name="end" size="10"
             value="${relaycountries_end[0]}">
    </p><p>
      Source: <select name="country">
        <option value="all"<c:if test="${relaycountries_country[0] eq 'all'}"> selected</c:if>>All relays</option>
        <option value="dz"<c:if test="${relaycountries_country[0] eq 'dz'}"> selected</c:if>>Algeria</option>
        <option value="au"<c:if test="${relaycountries_country[0] eq 'au'}"> selected</c:if>>Australia</option>
        <option value="bh"<c:if test="${relaycountries_country[0] eq 'bh'}"> selected</c:if>>Bahrain</option>
        <option value="br"<c:if test="${relaycountries_country[0] eq 'br'}"> selected</c:if>>Brazil</option>
        <option value="mm"<c:if test="${relaycountries_country[0] eq 'mm'}"> selected</c:if>>Burma</option>
        <option value="ca"<c:if test="${relaycountries_country[0] eq 'ca'}"> selected</c:if>>Canada</option>
        <option value="cn"<c:if test="${relaycountries_country[0] eq 'cn'}"> selected</c:if>>China</option>
        <option value="cu"<c:if test="${relaycountries_country[0] eq 'cu'}"> selected</c:if>>Cuba</option>
        <option value="dj"<c:if test="${relaycountries_country[0] eq 'dj'}"> selected</c:if>>Djibouti</option>
        <option value="eg"<c:if test="${relaycountries_country[0] eq 'eg'}"> selected</c:if>>Egypt</option>
        <option value="et"<c:if test="${relaycountries_country[0] eq 'et'}"> selected</c:if>>Ethiopia</option>
        <option value="fr"<c:if test="${relaycountries_country[0] eq 'fr'}"> selected</c:if>>France</option>
        <option value="de"<c:if test="${relaycountries_country[0] eq 'de'}"> selected</c:if>>Germany</option>
        <option value="ir"<c:if test="${relaycountries_country[0] eq 'ir'}"> selected</c:if>>Iran</option>
        <option value="iq"<c:if test="${relaycountries_country[0] eq 'iq'}"> selected</c:if>>Iraq</option>
        <option value="il"<c:if test="${relaycountries_country[0] eq 'il'}"> selected</c:if>>Israel</option>
        <option value="it"<c:if test="${relaycountries_country[0] eq 'it'}"> selected</c:if>>Italy</option>
        <option value="jp"<c:if test="${relaycountries_country[0] eq 'jp'}"> selected</c:if>>Japan</option>
        <option value="jo"<c:if test="${relaycountries_country[0] eq 'jo'}"> selected</c:if>>Jordan</option>
        <option value="kw"<c:if test="${relaycountries_country[0] eq 'kw'}"> selected</c:if>>Kuwait</option>
        <option value="lb"<c:if test="${relaycountries_country[0] eq 'lb'}"> selected</c:if>>Lebanon</option>
        <option value="ly"<c:if test="${relaycountries_country[0] eq 'ly'}"> selected</c:if>>Libya</option>
        <option value="ma"<c:if test="${relaycountries_country[0] eq 'ma'}"> selected</c:if>>Morocco</option>
        <option value="kp"<c:if test="${relaycountries_country[0] eq 'kp'}"> selected</c:if>>North Korea</option>
        <option value="om"<c:if test="${relaycountries_country[0] eq 'om'}"> selected</c:if>>Oman</option>
        <option value="ps"<c:if test="${relaycountries_country[0] eq 'ps'}"> selected</c:if>>Palestinian territories</option>
        <option value="pl"<c:if test="${relaycountries_country[0] eq 'pl'}"> selected</c:if>>Poland</option>
        <option value="qa"<c:if test="${relaycountries_country[0] eq 'qa'}"> selected</c:if>>Qatar</option>
        <option value="ru"<c:if test="${relaycountries_country[0] eq 'ru'}"> selected</c:if>>Russia</option>
        <option value="sa"<c:if test="${relaycountries_country[0] eq 'sa'}"> selected</c:if>>Saudi Arabia</option>
        <option value="kr"<c:if test="${relaycountries_country[0] eq 'kr'}"> selected</c:if>>South Korea</option>
        <option value="sd"<c:if test="${relaycountries_country[0] eq 'sd'}"> selected</c:if>>Sudan</option>
        <option value="se"<c:if test="${relaycountries_country[0] eq 'se'}"> selected</c:if>>Sweden</option>
        <option value="sy"<c:if test="${relaycountries_country[0] eq 'sy'}"> selected</c:if>>Syria</option>
        <option value="tn"<c:if test="${relaycountries_country[0] eq 'tn'}"> selected</c:if>>Tunisia</option>
        <option value="tm"<c:if test="${relaycountries_country[0] eq 'tm'}"> selected</c:if>>Turkmenistan</option>
        <option value="ae"<c:if test="${relaycountries_country[0] eq 'ae'}"> selected</c:if>>U.A.E.</option>
        <option value="gb"<c:if test="${relaycountries_country[0] eq 'gb'}"> selected</c:if>>U.K.</option>
        <option value="us"<c:if test="${relaycountries_country[0] eq 'us'}"> selected</c:if>>U.S.A.</option>
        <option value="uz"<c:if test="${relaycountries_country[0] eq 'uz'}"> selected</c:if>>Uzbekistan</option>
        <option value="vn"<c:if test="${relaycountries_country[0] eq 'vn'}"> selected</c:if>>Vietnam</option>
        <option value="ye"<c:if test="${relaycountries_country[0] eq 'ye'}"> selected</c:if>>Yemen</option>
      </select>
    </p><p>
      Resolution: <select name="dpi">
        <option value="72"<c:if test="${relaycountries_dpi[0] eq '72'}"> selected</c:if>>Screen - 576x360</option>
        <option value="150"<c:if test="${relaycountries_dpi[0] eq '150'}"> selected</c:if>>Print low - 1200x750</option>
        <option value="300"<c:if test="${relaycountries_dpi[0] eq '300'}"> selected</c:if>>Print high - 2400x1500</option>
      </select>
    </p><p>
    <input class="submit" type="submit" value="Update graph">
    </p>
  </div>
</form>
<p><a href="csv/relaycountries.csv">CSV</a> file containing all data.</p>
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
      Granularity:
        <input type="radio" name="granularity" value="day"> 1 day
        <input type="radio" name="granularity" value="hour"> 1 hour
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
