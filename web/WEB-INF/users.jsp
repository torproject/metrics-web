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
        <option value="all"<c:if test="${direct_users_country[0] eq 'all'}"> selected</c:if>>All users</option>
        <option value="dz"<c:if test="${direct_users_country[0] eq 'dz'}"> selected</c:if>>Algeria</option>
        <option value="au"<c:if test="${direct_users_country[0] eq 'au'}"> selected</c:if>>Australia</option>
        <option value="bh"<c:if test="${direct_users_country[0] eq 'bh'}"> selected</c:if>>Bahrain</option>
        <option value="br"<c:if test="${direct_users_country[0] eq 'br'}"> selected</c:if>>Brazil</option>
        <option value="mm"<c:if test="${direct_users_country[0] eq 'mm'}"> selected</c:if>>Burma</option>
        <option value="ca"<c:if test="${direct_users_country[0] eq 'ca'}"> selected</c:if>>Canada</option>
        <option value="cn"<c:if test="${direct_users_country[0] eq 'cn'}"> selected</c:if>>China</option>
        <option value="cu"<c:if test="${direct_users_country[0] eq 'cu'}"> selected</c:if>>Cuba</option>
        <option value="dj"<c:if test="${direct_users_country[0] eq 'dj'}"> selected</c:if>>Djibouti</option>
        <option value="eg"<c:if test="${direct_users_country[0] eq 'eg'}"> selected</c:if>>Egypt</option>
        <option value="et"<c:if test="${direct_users_country[0] eq 'et'}"> selected</c:if>>Ethiopia</option>
        <option value="fr"<c:if test="${direct_users_country[0] eq 'fr'}"> selected</c:if>>France</option>
        <option value="de"<c:if test="${direct_users_country[0] eq 'de'}"> selected</c:if>>Germany</option>
        <option value="ir"<c:if test="${direct_users_country[0] eq 'ir'}"> selected</c:if>>Iran</option>
        <option value="iq"<c:if test="${direct_users_country[0] eq 'iq'}"> selected</c:if>>Iraq</option>
        <option value="il"<c:if test="${direct_users_country[0] eq 'il'}"> selected</c:if>>Israel</option>
        <option value="it"<c:if test="${direct_users_country[0] eq 'it'}"> selected</c:if>>Italy</option>
        <option value="jp"<c:if test="${direct_users_country[0] eq 'jp'}"> selected</c:if>>Japan</option>
        <option value="jo"<c:if test="${direct_users_country[0] eq 'jo'}"> selected</c:if>>Jordan</option>
        <option value="kw"<c:if test="${direct_users_country[0] eq 'kw'}"> selected</c:if>>Kuwait</option>
        <option value="lb"<c:if test="${direct_users_country[0] eq 'lb'}"> selected</c:if>>Lebanon</option>
        <option value="ly"<c:if test="${direct_users_country[0] eq 'ly'}"> selected</c:if>>Libya</option>
        <option value="ma"<c:if test="${direct_users_country[0] eq 'ma'}"> selected</c:if>>Morocco</option>
        <option value="kp"<c:if test="${direct_users_country[0] eq 'kp'}"> selected</c:if>>North Korea</option>
        <option value="om"<c:if test="${direct_users_country[0] eq 'om'}"> selected</c:if>>Oman</option>
        <option value="ps"<c:if test="${direct_users_country[0] eq 'ps'}"> selected</c:if>>Palestinian territories</option>
        <option value="pl"<c:if test="${direct_users_country[0] eq 'pl'}"> selected</c:if>>Poland</option>
        <option value="qa"<c:if test="${direct_users_country[0] eq 'qa'}"> selected</c:if>>Qatar</option>
        <option value="ru"<c:if test="${direct_users_country[0] eq 'ru'}"> selected</c:if>>Russia</option>
        <option value="sa"<c:if test="${direct_users_country[0] eq 'sa'}"> selected</c:if>>Saudi Arabia</option>
        <option value="kr"<c:if test="${direct_users_country[0] eq 'kr'}"> selected</c:if>>South Korea</option>
        <option value="sd"<c:if test="${direct_users_country[0] eq 'sd'}"> selected</c:if>>Sudan</option>
        <option value="se"<c:if test="${direct_users_country[0] eq 'se'}"> selected</c:if>>Sweden</option>
        <option value="sy"<c:if test="${direct_users_country[0] eq 'sy'}"> selected</c:if>>Syria</option>
        <option value="tn"<c:if test="${direct_users_country[0] eq 'tn'}"> selected</c:if>>Tunisia</option>
        <option value="tm"<c:if test="${direct_users_country[0] eq 'tm'}"> selected</c:if>>Turkmenistan</option>
        <option value="ae"<c:if test="${direct_users_country[0] eq 'ae'}"> selected</c:if>>U.A.E.</option>
        <option value="gb"<c:if test="${direct_users_country[0] eq 'gb'}"> selected</c:if>>U.K.</option>
        <option value="us"<c:if test="${direct_users_country[0] eq 'us'}"> selected</c:if>>U.S.A.</option>
        <option value="uz"<c:if test="${direct_users_country[0] eq 'uz'}"> selected</c:if>>Uzbekistan</option>
        <option value="vn"<c:if test="${direct_users_country[0] eq 'vn'}"> selected</c:if>>Vietnam</option>
        <option value="ye"<c:if test="${direct_users_country[0] eq 'ye'}"> selected</c:if>>Yemen</option>
      </select>
    </p><p>
      Resolution: <select name="dpi">
        <option value="72"<c:if test="${direct_users_dpi[0] eq '72'}"> selected</c:if>>Screen - 576x360</option>
        <option value="150"<c:if test="${direct_users_dpi[0] eq '150'}"> selected</c:if>>Print low - 1200x750</option>
        <option value="300"<c:if test="${direct_users_dpi[0] eq '300'}"> selected</c:if>>Print high - 2400x1500</option>
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
        <option value="all"<c:if test="${bridge_users_country[0] eq 'all'}"> selected</c:if>>All users</option>
        <option value="dz"<c:if test="${bridge_users_country[0] eq 'dz'}"> selected</c:if>>Algeria</option>
        <option value="au"<c:if test="${bridge_users_country[0] eq 'au'}"> selected</c:if>>Australia</option>
        <option value="bh"<c:if test="${bridge_users_country[0] eq 'bh'}"> selected</c:if>>Bahrain</option>
        <option value="br"<c:if test="${bridge_users_country[0] eq 'br'}"> selected</c:if>>Brazil</option>
        <option value="mm"<c:if test="${bridge_users_country[0] eq 'mm'}"> selected</c:if>>Burma</option>
        <option value="ca"<c:if test="${bridge_users_country[0] eq 'ca'}"> selected</c:if>>Canada</option>
        <option value="cn"<c:if test="${bridge_users_country[0] eq 'cn'}"> selected</c:if>>China</option>
        <option value="cu"<c:if test="${bridge_users_country[0] eq 'cu'}"> selected</c:if>>Cuba</option>
        <option value="dj"<c:if test="${bridge_users_country[0] eq 'dj'}"> selected</c:if>>Djibouti</option>
        <option value="eg"<c:if test="${bridge_users_country[0] eq 'eg'}"> selected</c:if>>Egypt</option>
        <option value="et"<c:if test="${bridge_users_country[0] eq 'et'}"> selected</c:if>>Ethiopia</option>
        <option value="fr"<c:if test="${bridge_users_country[0] eq 'fr'}"> selected</c:if>>France</option>
        <option value="de"<c:if test="${bridge_users_country[0] eq 'de'}"> selected</c:if>>Germany</option>
        <option value="ir"<c:if test="${bridge_users_country[0] eq 'ir'}"> selected</c:if>>Iran</option>
        <option value="iq"<c:if test="${bridge_users_country[0] eq 'iq'}"> selected</c:if>>Iraq</option>
        <option value="il"<c:if test="${bridge_users_country[0] eq 'il'}"> selected</c:if>>Israel</option>
        <option value="it"<c:if test="${bridge_users_country[0] eq 'it'}"> selected</c:if>>Italy</option>
        <option value="jp"<c:if test="${bridge_users_country[0] eq 'jp'}"> selected</c:if>>Japan</option>
        <option value="jo"<c:if test="${bridge_users_country[0] eq 'jo'}"> selected</c:if>>Jordan</option>
        <option value="kw"<c:if test="${bridge_users_country[0] eq 'kw'}"> selected</c:if>>Kuwait</option>
        <option value="lb"<c:if test="${bridge_users_country[0] eq 'lb'}"> selected</c:if>>Lebanon</option>
        <option value="ly"<c:if test="${bridge_users_country[0] eq 'ly'}"> selected</c:if>>Libya</option>
        <option value="ma"<c:if test="${bridge_users_country[0] eq 'ma'}"> selected</c:if>>Morocco</option>
        <option value="kp"<c:if test="${bridge_users_country[0] eq 'kp'}"> selected</c:if>>North Korea</option>
        <option value="om"<c:if test="${bridge_users_country[0] eq 'om'}"> selected</c:if>>Oman</option>
        <option value="ps"<c:if test="${bridge_users_country[0] eq 'ps'}"> selected</c:if>>Palestinian territories</option>
        <option value="pl"<c:if test="${bridge_users_country[0] eq 'pl'}"> selected</c:if>>Poland</option>
        <option value="qa"<c:if test="${bridge_users_country[0] eq 'qa'}"> selected</c:if>>Qatar</option>
        <option value="ru"<c:if test="${bridge_users_country[0] eq 'ru'}"> selected</c:if>>Russia</option>
        <option value="sa"<c:if test="${bridge_users_country[0] eq 'sa'}"> selected</c:if>>Saudi Arabia</option>
        <option value="kr"<c:if test="${bridge_users_country[0] eq 'kr'}"> selected</c:if>>South Korea</option>
        <option value="sd"<c:if test="${bridge_users_country[0] eq 'sd'}"> selected</c:if>>Sudan</option>
        <option value="se"<c:if test="${bridge_users_country[0] eq 'se'}"> selected</c:if>>Sweden</option>
        <option value="sy"<c:if test="${bridge_users_country[0] eq 'sy'}"> selected</c:if>>Syria</option>
        <option value="tn"<c:if test="${bridge_users_country[0] eq 'tn'}"> selected</c:if>>Tunisia</option>
        <option value="tm"<c:if test="${bridge_users_country[0] eq 'tm'}"> selected</c:if>>Turkmenistan</option>
        <option value="ae"<c:if test="${bridge_users_country[0] eq 'ae'}"> selected</c:if>>U.A.E.</option>
        <option value="gb"<c:if test="${bridge_users_country[0] eq 'gb'}"> selected</c:if>>U.K.</option>
        <option value="us"<c:if test="${bridge_users_country[0] eq 'us'}"> selected</c:if>>U.S.A.</option>
        <option value="uz"<c:if test="${bridge_users_country[0] eq 'uz'}"> selected</c:if>>Uzbekistan</option>
        <option value="vn"<c:if test="${bridge_users_country[0] eq 'vn'}"> selected</c:if>>Vietnam</option>
        <option value="ye"<c:if test="${bridge_users_country[0] eq 'ye'}"> selected</c:if>>Yemen</option>
      </select>
    </p><p>
      Resolution: <select name="dpi">
        <option value="72"<c:if test="${bridge_users_dpi[0] eq '72'}"> selected</c:if>>Screen - 576x360</option>
        <option value="150"<c:if test="${bridge_users_dpi[0] eq '150'}"> selected</c:if>>Print low - 1200x750</option>
        <option value="300"<c:if test="${bridge_users_dpi[0] eq '300'}"> selected</c:if>>Print high - 2400x1500</option>
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
