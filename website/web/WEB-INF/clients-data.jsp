<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
  <title>Tor Metrics &mdash; Estimated number of clients in the Tor network</title>
  <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">
  <link href="/css/stylesheet-ltr.css" type="text/css" rel="stylesheet">
  <link href="/images/favicon.ico" type="image/x-icon" rel="shortcut icon">
</head>
<body>
  <div class="center">
    <%@ include file="banner.jsp"%>
    <div class="main-column">

<h2><a href="/">Tor Metrics</a> &mdash; Estimated number of clients in the Tor network</h2>
<br>
<p>The following data file contains estimates on the number of
<a href="about.html#client">clients</a> in the network.
These numbers are derived from directory requests counted on
<a href="about.html#directory-authority">directory authorities</a>,
<a href="about.html#directory-mirror">directory mirrors</a>, and
<a href="about.html#bridge">bridges</a>.
Statistics are available for clients connecting directly relays and
clients connecting via bridges.
There are statistics available by country (for both directly-connecting
clients and clients connecting via bridges), by transport protocol (only
for clients connecting via bridges), and by IP version (only for clients
connecting via bridges).
Statistics also include predicted client numbers from past observations,
which can be used to detect censorship events.</p>

<p><b>Download as <a href="stats/clients.csv">CSV file</a>.</b></p>

<p>The statistics file contains the following columns:</p>
<ul>
<li><b>date:</b> UTC date (YYYY-MM-DD) for which client numbers are
estimated.</li>
<li><b>node:</b> The node type to which clients connect first, which can
be either <b>"relay"</b> or <b>"bridge"</b>.</li>
<li><b>country:</b> Two-letter lower-case country code as found in a GeoIP
database by resolving clients' IP addresses, or <b>"??"</b> if client IP
addresses could not be resolved.
If this column contains the empty string, all clients are included,
regardless of their country code.</li>
<li><b>transport:</b> Transport name used by clients to connect to the Tor
network using bridges.
Examples are <b>"obfs2"</b>, <b>"obfs3"</b>, <b>"websocket"</b>, or
<b>"&lt;OR&gt;"</b> (original onion routing protocol).
If this column contains the empty string, all clients are included,
regardless of their transport.
There are no statistics on the number of clients by transport that connect
to the Tor network via relays.</li>
<li><b>version:</b> IP version used by clients to connect to the Tor
network using bridges.
Examples are <b>"v4"</b> and <b>"v6"</b>.
If this column contains the empty string, all clients are included,
regardless of their IP version.
There are no statistics on the number of clients by IP version that connect
directly to the Tor network using relays.</li>
<li><b>lower:</b> Lower number of expected clients under the assumption
that there has been no censorship event.
If this column contains the empty string, there are no expectations on the
number of clients.</li>
<li><b>upper:</b> Upper number of expected clients under the assumption
that there has been no release of censorship.
If this column contains the empty string, there are no expectations on the
number of clients.</li>
<li><b>clients:</b> Estimated number of clients.</li>
<li><b>frac:</b> Fraction of relays or bridges in percent that the
estimate is based on.
The higher this value, the more reliable is the estimate.
Values above 50 can be considered reliable enough for most purposes,
lower values should be handled with more care.</li>
</ul>

<h4>Related metrics</h4>
<ul>
<li><a href="userstats-relay-country.html">Graph: Direct users by country</a></li>
<li><a href="userstats-relay-table.html">Table: Top-10 countries by directly connecting users</a></li>
<li><a href="userstats-censorship-events.html">Table: Top-10 countries by possible censorship events</a></li>
<li><a href="userstats-bridge-country.html">Graph: Bridge users by country</a></li>
<li><a href="userstats-bridge-table.html">Table: Top-10 countries by bridge users</a></li>
<li><a href="userstats-bridge-transport.html">Graph: Bridge users by transport</a></li>
<li><a href="userstats-bridge-version.html">Graph: Bridge users by IP version</a></li>
<li><a href="oxford-anonymous-internet.html">Link: Tor users as percentage of larger Internet population</a></li>
</ul>

    </div>
  </div>
  <div class="bottom" id="bottom">
    <%@ include file="footer.jsp"%>
  </div>
</body>
</html>

