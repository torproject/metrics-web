<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
  <title>Tor Metrics Portal: Statistics</title>
  <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">
  <link href="/css/stylesheet-ltr.css" type="text/css" rel="stylesheet">
  <link href="/images/favicon.ico" type="image/x-icon" rel="shortcut icon">
</head>
<body>
  <div class="center">
    <%@ include file="banner.jsp"%>
    <div class="main-column">
<h2>Tor Metrics Portal: Statistics</h2>
<br>

<p>The metrics portal aggregates large amounts of Tor network
<a href="data.html">data</a> and visualizes results in customizable
<a href="graphs.html">graphs</a> and tables.
All aggregated data are also available for download, so that people can
easily plot their own graphs or even develop a prettier metrics website
without writing their own data aggregation code.
Data formats of aggregate statistics are specified below.</p>


<a name="servers"></a>
<h3><a href="#servers" class="anchor">Number of relays and
bridges</a></h3>
<br>
<p>Statistics file <a href="stats/servers.csv">servers.csv</a> contains
the average number of relays and bridges in the Tor network.
All averages are calculated per day by evaluating the relay and bridge
lists published by the directory authorities.
Statistics include subsets of relays or bridges by relay flag (only
relays), country code (only relays, only until February 2013), Tor
software version (only relays), operating system (only relays), and EC2
cloud (only bridges).
The statistics file contains the following columns:</p>

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

<a name="bandwidth"></a>
<h3><a href="#bandwidth" class="anchor">Bandwidth provided and consumed by
relays</a></h3>
<br>
<p>Statistics on bandwidth provided and consumed by relays are contained
in file <a href="stats/bandwidth.csv">bandwidth.csv</a>.
This file contains three different bandwidth metrics:
(1) bandwidth that relays are capable to provide and bandwidth that relays
report to have consumed, either (2) for any traffic, or (3) only traffic
from serving directory data.
Relays providing bandwidth statistics are categorized by having the
<b>"Exit"</b> and <b>"Guard"</b> relay flag, having both, or not having
either.
The statistics file contains the following columns:</p>

<ul>
<li><b>date:</b> UTC date (YYYY-MM-DD) that relays reported bandwidth data
for.</li>
<li><b>isexit:</b> Whether relays included in this line have the
<b>"Exit"</b> relay flag or not, which can be <b>"t"</b> or <b>"f"</b>.
If this column contains the empty string, bandwidth data from all running
relays are included, regardless of assigned relay flags.</li>
<li><b>isguard:</b> Whether relays included in this line have the
<b>"Guard"</b> relay flag or not, which can be <b>"t"</b> or <b>"f"</b>.
If this column contains the empty string, bandwidth data from all running
relays are included, regardless of assigned relay flags.</li>
<li><b>advbw:</b> Total advertised bandwidth in bytes per second that
relays are capable to provide.</li>
<li><b>bwread:</b> Total bandwidth in bytes per second that relays have
read.
This metric includes any kind of traffic.</li>
<li><b>bwwrite:</b> Similar to <b>bwread</b>, but for traffic written by
relays.</li>
<li><b>dirread:</b> Bandwidth in bytes per second that relays have read
when serving directory data.
Not all relays report how many bytes they read when serving directory data
which is why this value is an estimate from the available data.
This metric is not available for subsets of relays with certain relay
flags, so that this column will contain the empty string if either
<b>isexit</b> or <b>isguard</b> is non-empty.</li>
<li><b>dirwrite:</b> Similar to <b>dirread</b>, but for traffic written by
relays when serving directory data.</li>
</ul>

<a name="fast-exits"></a>
<h3><a href="#fast-exits" class="anchor">Relays meeting or almost meeting
fast-exit requirements</a></h3>
<br>
<p>Statistics file <a href="stats/fast-exits.csv">fast-exits.csv</a>
contains the number of relays meeting or almost meeting fast-exit
requirements.
These requirements originate from a Tor sponsor contract and are defined as
follows:
a Tor relay is fast if it has at least 95 Mbit/s configured bandwidth
rate, at least 5000 KB/s advertised bandwidth capacity, and permits
exiting to ports 80, 443, 554, and 1755; furthermore, there may be at most
2 relays per /24 network in the set of fast exits.
Similarly, an almost fast exit is one that almost meets the fast-exit
requirements, but fails at least one of them.
In particular, an almost fast exit is one that has at least 80 Mbit/s
configured bandwidth rate, at least 2000 KB/s advertised bandwidth
capacity, and permits exiting to ports 80 and 443; also, if there are more
than 2 relays per /24 network meeting fast-exit requirements, all but two
are considered almost fast.
The statistics file contains the following columns:</p>

<ul>
<li><b>date:</b> UTC date (YYYY-MM-DD) when relays have been listed as
running.</li>
<li><b>fastnum:</b> Average number of relays matching fast-exit
requirements.</li>
<li><b>almostnum:</b> Average number of relays almost matching
fast-exit requirements.</li>
<li><b>fastprob:</b> Total exit probability of all relays matching
fast-exit requirements.</li>
<li><b>almostprob:</b> Total exit probability of all relays almost
matching fast-exit requirements.</li>
</li>
</ul>

<a name="clients"></a>
<h3><a href="#clients" class="anchor">Estimated number of clients in the
Tor network</a></h3>
<br>
<p>Statistics file <a href="stats/clients.csv">clients.csv</a> contains
estimates on the number of clients in the Tor network.
These estimates are based on the number of directory requests counted on
directory mirrors and bridges.
Statistics are available for clients connecting directly to the Tor
network and clients connecting via bridges.
For relays, there exist statistics on the number of clients by country,
and for bridges, statistics are available by country, by transport, and by
IP version.
Statistics further include expected client numbers from past observations
which can be used to detect censorship or release of censorship.
The statistics file contains the following columns:</p>

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

<a name="torperf"></a>
<h3><a href="#torperf" class="anchor">Performance of downloading static
files over Tor</a></h3>
<br>
<p>Statistics file <a href="stats/torperf.csv">torperf.csv</a> contains
aggregate statistics on download performance over time.
These statistics come from the Torperf service that periodically downloads
static files over Tor.
The statistics file contains the following columns:</p>

<ul>
<li><b>date:</b> UTC date (YYYY-MM-DD) when download performance was
measured.</li>
<li><b>size:</b> Size of the downloaded file in bytes.</li>
<li><b>source:</b> Name of the Torperf service performing measurements.
If this column contains the empty string, all measurements are included,
regardless of which Torperf service performed them.
Examples are <b>"moria"</b>, <b>"siv"</b>, and <b>"torperf"</b>.</li>
<li><b>q1:</b> First quartile of time until receiving the last byte in
milliseconds.</li>
<li><b>md:</b> Median of time until receiving the last byte in
milliseconds.</li>
<li><b>q3:</b> Third quartile of time until receiving the last byte in
milliseconds.</li>
<li><b>timeouts:</b> Number of timeouts that occurred when attempting to
download the static file over Tor.</li>
<li><b>failures:</b> Number of failures that occurred when attempting to
download the static file over Tor.</li>
<li><b>requests:</b> Total number of requests made to download the static
file over Tor.</li>
</ul>

<a name="connbidirect"></a>
<h3><a href="#connbidirect" class="anchor">Fraction of connections used
uni-/bidirectionally</a></h3>
<br>
<p>Statistics file <a href="stats/connbidirect.csv">connbidirect.csv</a>
contains statistics on the fraction of connections that is used uni- or
bidirectionally.
Every 10 seconds, relays determine for every connection whether they read
and wrote less than a threshold of 20 KiB.
For the remaining connections, relays report whether they read/wrote at
least 10 times as many bytes as they wrote/read.
If so, they classify a connection as "mostly reading" or "mostly writing,"
respectively.
All other connections are classified as "both reading and writing."
After classifying connections, read and write counters are reset for the
next 10-second interval.
Statistics are aggregated over 24 hours.
The statistics file contains the following columns:</p>

<ul>
<li><b>date:</b> UTC date (YYYY-MM-DD) for which statistics on
uni-/bidirectional connection usage were reported.</li>
<li><b>source:</b> Fingerprint of the relay reporting statistics.</li>
<li><b>below:</b> Number of 10-second intervals of connections with less
than 20 KiB read and written data.</li>
<li><b>read:</b> Number of 10-second intervals of connections with 10
times as many read bytes as written bytes.</li>
<li><b>write:</b> Number of 10-second intervals of connections with 10
times as many written bytes as read bytes.</li>
<li><b>both:</b> Number of 10-second intervals of connections with less
than 10 times as many written or read bytes as in the other
direction.</li>
</ul>
    </div>
  </div>
  <div class="bottom" id="bottom">
    <%@ include file="footer.jsp"%>
  </div>
</body>
</html>
</body>
</html>

