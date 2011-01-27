<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
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

<h3>Directly connecting Tor users</h3>
<br>
<p>After being connected to the Tor network, users need to refresh their
list of running relays on a regular basis. They send their requests to one
out of a few hundred directory mirrors to save bandwidth of the directory
authorities. The following graphs show an estimate of recurring Tor users
based on the requests seen by a few dozen directory mirrors.</p>
<a name="direct-users"></a>
<img src="direct-users.png${direct_users_url}"
     width="576" height="360" alt="Direct users graph">
<form action="users.html#direct-users">
  <div class="formrow">
    <input type="hidden" name="graph" value="direct-users">
    <p>
    <label>Start date (yyyy-mm-dd):</label>
      <input type="text" name="start" size="10"
             value="${direct_users_start[0]}">
    <label>End date (yyyy-mm-dd):</label>
      <input type="text" name="end" size="10"
             value="${direct_users_end[0]}">
    </p><p>
      Source: <select name="country">
        <option value="all" selected>All users</option>
        <option value="au">Australia</option>
        <option value="bh">Bahrain</option>
        <option value="br">Brazil</option>
        <option value="ca">Canada</option>
        <option value="cn">China</option>
        <option value="cu">Cuba</option>
        <option value="de">Germany</option>
        <option value="eg">Egypt</option>
        <option value="et">Ethiopia</option>
        <option value="fr">France</option>
        <option value="gb">U.K.</option>
        <option value="ir">Iran</option>
        <option value="it">Italy</option>
        <option value="jp">Japan</option>
        <option value="kr">South Korea</option>
        <option value="mm">Burma</option>
        <option value="pl">Poland</option>
        <option value="ru">Russia</option>
        <option value="sa">Saudi Arabia</option>
        <option value="se">Sweden</option>
        <option value="sy">Syria</option>
        <option value="tn">Tunisia</option>
        <option value="tm">Turkmenistan</option>
        <option value="us">U.S.A.</option>
        <option value="uz">Uzbekistan</option>
        <option value="vn">Vietnam</option>
        <option value="ye">Yemen</option>
      </select>
    </p><p>
    <input class="submit" type="submit" value="Update graph">
    </p>
  </div>
</form>
<p><a href="csv/direct-users.csv">CSV</a> file containing all data.</p>
<p><a href="csv/monthly-users-peak.csv">CSV</a> file containing peak daily
Tor users (direct and bridge) per month by country.</p>
<p><a href="csv/monthly-users-average.csv">CSV</a> file containing average
daily Tor users (direct and bridge) per month by country.</p>
<br>

<h3>Tor users via bridges</h3>
<br>
<p>Users who cannot connect directly to the Tor network instead connect
via bridges, which are non-public relays. The following graphs display an
estimate of Tor users via bridges based on the unique IP addresses as seen
by a few hundred bridges.</p>
<a name="bridge-users"></a>
<img src="bridge-users.png${bridge_users_url}"
     width="576" height="360" alt="Bridge users graph">
<form action="users.html#bridge-users">
  <div class="formrow">
    <input type="hidden" name="graph" value="bridge-users">
    <p>
    <label>Start date (yyyy-mm-dd):</label>
      <input type="text" name="start" size="10"
             value="${bridge_users_start[0]}">
    <label>End date (yyyy-mm-dd):</label>
      <input type="text" name="end" size="10"
             value="${bridge_users_end[0]}">
    </p><p>
      Source: <select name="country">
        <option value="all" selected>All users</option>
        <option value="au">Australia</option>
        <option value="bh">Bahrain</option>
        <option value="br">Brazil</option>
        <option value="ca">Canada</option>
        <option value="cn">China</option>
        <option value="cu">Cuba</option>
        <option value="de">Germany</option>
        <option value="eg">Egypt</option>
        <option value="et">Ethiopia</option>
        <option value="fr">France</option>
        <option value="gb">U.K.</option>
        <option value="ir">Iran</option>
        <option value="it">Italy</option>
        <option value="jp">Japan</option>
        <option value="kr">South Korea</option>
        <option value="mm">Burma</option>
        <option value="pl">Poland</option>
        <option value="ru">Russia</option>
        <option value="sa">Saudi Arabia</option>
        <option value="se">Sweden</option>
        <option value="sy">Syria</option>
        <option value="tn">Tunisia</option>
        <option value="tm">Turkmenistan</option>
        <option value="us">U.S.A.</option>
        <option value="uz">Uzbekistan</option>
        <option value="vn">Vietnam</option>
        <option value="ye">Yemen</option>
      </select>
    </p><p>
    <input class="submit" type="submit" value="Update graph">
    </p>
  </div>
</form>
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
