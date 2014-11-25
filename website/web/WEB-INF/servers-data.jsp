<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
  <title>Tor Metrics &mdash; Number of relays and bridges</title>
  <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">
  <link href="/css/stylesheet-ltr.css" type="text/css" rel="stylesheet">
  <link href="/images/favicon.ico" type="image/x-icon" rel="shortcut icon">
</head>
<body>
  <div class="center">
    <%@ include file="banner.jsp"%>
    <div class="main-column">

<h2><a href="/">Tor Metrics</a> &mdash; Number of relays and bridges</h2>
<br>
<p>The following data file contains the number of running
<a href="about.html#relay">relays</a> and
<a href="about.html#bridge">bridges</a> in the network.
Statistics include subsets of relays or bridges by
<a href="about.html#relay-flag">relay flag</a> (relays only), country code
(relays only, and only until February 2013), tor software version (relays
only), operating system (relays only), and by whether or not they are
running in the EC2 cloud (bridges only).
The data file contains daily (mean) averages of relay and bridge
numbers.</p>

<p><b>Download as <a href="stats/servers.csv">CSV file</a>.</b></p>

<p>The statistics file contains the following columns:</p>
<ul>
<li><b>date:</b> UTC date (YYYY-MM-DD) when relays or bridges have been
listed as running.</li>
<li><b>flag:</b> Relay flag assigned by the directory authorities.
Examples are <b>"Exit"</b>, <b>"Guard"</b>, <b>"Fast"</b>,
<b>"Stable"</b>, and <b>"HSDir"</b>.
Relays can have none, some, or all these relay flags assigned.
Relays that don't have the <b>"Running"</b> flag are not included in these
statistics regardless of their other flags.
If this column contains the empty string, all running relays are included,
regardless of assigned flags.
There are no statistics on the number of bridges by relay flag.</li>
<li><b>country:</b> Two-letter lower-case country code as found in a GeoIP
database by resolving the relay's first onion-routing IP address, or
<b>"??"</b> if an IP addresses could not be resolved.
If this column contains the empty string, all running relays are included,
regardless of their resolved country code.
Statistics on relays by country code are only available until January 31,
2013.
There are no statistics on the number of bridges by country code.</li>
<li><b>version:</b> First three dotted numbers of the Tor software version
as reported by the relay.
An example is <b>"0.2.5"</b>.
If this column contains the empty string, all running relays are included,
regardless of the Tor software version they run.
There are no statistics on the number of bridges by Tor software
version.</li>
<li><b>platform:</b> Operating system as reported by the relay.
Examples are <b>"Linux"</b>, <b>"Darwin"</b> (Mac OS X), <b>"FreeBSD"</b>,
<b>"Windows"</b>, and <b>"Other"</b>.
If this column contains the empty string, all running relays are included,
regardless of the operating system they run on.
There are no statistics on the number of bridges by operating system.</li>
<li><b>ec2bridge:</b> Whether bridges are running in the EC2 cloud or not.
More precisely, bridges in the EC2 cloud running an image provided by Tor
by default set their nickname to <b>"ec2bridger"</b> plus 8 random hex
characters.
This column either contains <b>"t"</b> for bridges matching this naming
scheme, or the empty string for all bridges regardless of their nickname.
There are no statistics on the number of relays running in the EC2
cloud.</li>
<li><b>relays:</b> The average number of relays matching the criteria in
the previous columns.
If the values in previous columns are specific to bridges only, this
column contains the empty string.</li>
<li><b>bridges:</b> The average number of bridges matching the criteria in
the previous columns.
If the values in previous columns are specific to relays only, this column
contains the empty string.</li>
</ul>

<h4>Related metrics</h4>
<ul>
<li><a href="networksize.html">Graph: Relays and bridges in the network</a></li>
<li><a href="relayflags.html">Graph: Relays with Exit, Fast, Guard, Stable, and HSDir flags</a></li>
<li><a href="versions.html">Graph: Relays by version</a></li>
<li><a href="platforms.html">Graph: Relays by platform</a></li>
<li><a href="cloudbridges.html">Graph: Tor Cloud bridges</a></li>
</ul>

    </div>
  </div>
  <div class="bottom" id="bottom">
    <%@ include file="footer.jsp"%>
  </div>
</body>
</html>

