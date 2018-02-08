<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<jsp:include page="top.jsp">
  <jsp:param name="pageTitle" value="Sources &ndash; Tor Metrics"/>
  <jsp:param name="navActive" value="Sources"/>
</jsp:include>

    <div class="container">
      <ul class="breadcrumb">
        <li><a href="/">Home</a></li>
        <li><a href="sources.html">Sources</a></li>
        <li class="active">Statistics</li>
      </ul>
    </div>

    <div class="container">
      <h1>Pre-aggregated statistics files used on this website <a href="#stats" name="stats" class="anchor">#</a></h1>
      <p>This page contains specifications and links to pre-aggregated statistics files used on this website.</p>
    </div>

<div class="container">
<h2>Number of relays and bridges <a href="#servers" name="servers" class="anchor">#</a></h2>

<p>The following data file contains the number of running <a
href="glossary.html#relay">relays</a> and <a href="glossary.html#bridge">bridges</a>
in the network.  Statistics include subsets of relays or bridges by <a
href="glossary.html#relay-flag">relay flag</a> (relays only), country code (relays
only, and only until February 2013), tor software version (relays only),
operating system (relays only), and by whether or not they are running in the
EC2 cloud (bridges only).  The data file contains daily (mean) averages of relay
and bridge numbers.</p>

<p><b>Download as <a href="stats/servers.csv">CSV file</a>.</b></p>

<p>The statistics file contains the following columns:</p>
<ul>

<li><b>date:</b> UTC date (YYYY-MM-DD) when relays or bridges have been listed
as running.</li>

<li><b>flag:</b> Relay flag assigned by the directory authorities.  Examples are
<b>"Exit"</b>, <b>"Guard"</b>, <b>"Fast"</b>, <b>"Stable"</b>, and
<b>"HSDir"</b>.  Relays can have none, some, or all these relay flags assigned.
Relays that don't have the <b>"Running"</b> flag are not included in these
statistics regardless of their other flags.  If this column contains the empty
string, all running relays are included, regardless of assigned flags.  There
are no statistics on the number of bridges by relay flag.</li>

<li><b>country:</b> Two-letter lower-case country code as found in a GeoIP
database by resolving the relay's first onion-routing IP address, or <b>"??"</b>
if an IP addresses could not be resolved.  If this column contains the empty
string, all running relays are included, regardless of their resolved country
code.  Statistics on relays by country code are only available until January 31,
2013.  There are no statistics on the number of bridges by country code.</li>

<li><b>version:</b> First three dotted numbers of the Tor software version as
reported by the relay.  An example is <b>"0.2.5"</b>.  If this column contains
the empty string, all running relays are included, regardless of the Tor
software version they run.  There are no statistics on the number of bridges by
Tor software version.</li>

<li><b>platform:</b> Operating system as reported by the relay.  Examples are
<b>"Linux"</b>, <b>"Darwin"</b> (macOS), <b>"BSD"</b>, <b>"Windows"</b>, and
<b>"Other"</b>.  If this column contains the empty string, all running relays
are included, regardless of the operating system they run on.  There are no
statistics on the number of bridges by operating system.</li>

<li><b>ec2bridge:</b> Whether bridges are running in the EC2 cloud or not.  More
precisely, bridges in the EC2 cloud running an image provided by Tor by default
set their nickname to <b>"ec2bridger"</b> plus 8 random hex characters.  This
column either contains <b>"t"</b> for bridges matching this naming scheme, or
the empty string for all bridges regardless of their nickname.  There are no
statistics on the number of relays running in the EC2 cloud.</li>

<li><b>relays:</b> The average number of relays matching the criteria in the
previous columns.  If the values in previous columns are specific to bridges
only, this column contains the empty string.</li>

<li><b>bridges:</b> The average number of bridges matching the criteria in the
previous columns.  If the values in previous columns are specific to relays
only, this column contains the empty string.</li>

</ul>

</div>

<div class="container">
<h2>Bandwidth provided and consumed by relays <a href="#bandwidth" name="bandwidth" class="anchor">#</a></h2>

<p>The following data file contains statistics on <a
href="glossary.html#advertised-bandwidth">advertised</a> and <a
href="glossary.html#bandwidth-history">consumed bandwidth</a> of <a
href="glossary.html#relay">relays</a> in the network.  Statistics on advertised
bandwidth include any kind of traffic handled by a relay, whereas statistics on
consumed bandwidth are available either for all traffic combined, or
specifically for directory traffic.  Some of the statistics are available for
subsets of relays that have the "Exit" and/or the "Guard" <a
href="glossary.html#relay-flag">flag</a>.  The data file contains daily (mean)
averages of bandwidth numbers.</p>

<p><b>Download as <a href="stats/bandwidth.csv">CSV file</a>.</b></p>

<p>The statistics file contains the following columns:</p>
<ul>

<li><b>date:</b> UTC date (YYYY-MM-DD) that relays reported bandwidth data
for.</li>

<li><b>isexit:</b> Whether relays included in this line have the <b>"Exit"</b>
relay flag or not, which can be <b>"t"</b> or <b>"f"</b>.  If this column
contains the empty string, bandwidth data from all running relays are included,
regardless of assigned relay flags.</li>

<li><b>isguard:</b> Whether relays included in this line have the <b>"Guard"</b>
relay flag or not, which can be <b>"t"</b> or <b>"f"</b>.  If this column
contains the empty string, bandwidth data from all running relays are included,
regardless of assigned relay flags.</li>

<li><b>advbw:</b> Total advertised bandwidth in bytes per second that relays are
capable to provide.</li>

<li><b>bwread:</b> Total bandwidth in bytes per second that relays have read.
This metric includes any kind of traffic.</li>

<li><b>bwwrite:</b> Similar to <b>bwread</b>, but for traffic written by
relays.</li>

<li><b>dirread:</b> Bandwidth in bytes per second that relays have read when
serving directory data.  Not all relays report how many bytes they read when
serving directory data which is why this value is an estimate from the available
data.  This metric is not available for subsets of relays with certain relay
flags, so that this column will contain the empty string if either <b>isexit</b>
or <b>isguard</b> is non-empty.</li>

<li><b>dirwrite:</b> Similar to <b>dirread</b>, but for traffic written by
relays when serving directory data.</li>

</ul>

</div>

<div class="container">
<h2>Relays and bridges supporting IPv6 <a href="#ipv6servers" name="ipv6servers" class="anchor">#</a></h2>

<p>The following data file contains statistics on <a href="glossary.html#relay">relays</a> and <a href="glossary.html#bridge">bridges</a> supporting IPv6.
A relay can support IPv6 by announcing an IPv6 address and port for the OR protocol, which may then be confirmed as reachable by the <a href="glossary.html#directory-authority">directory authorities</a>.
It can further permit exiting to IPv6 targets.
A bridge can support IPv6 by announcing an IPv6 address and port for the OR protocol.
The <a href="glossary.html#bridge-authority">bridge authority</a> may test
reachability of IPv6 OR addresses similar to directory authorities, however it
does not explicitly list reachable IPv6 OR addresses of bridges.
Some of the statistics are available for subsets of relays that got the "Guard" and/or "Exit" <a href="glossary.html#relay-flag">relay flags</a>.
The data file contains daily (mean) averages of relay or server numbers and advertised bandwidth numbers.</p>

<p><b>Download as <a href="stats/ipv6servers.csv">CSV file</a>.</b></p>

<p>The statistics file contains the following columns:</p>
<ul>

<li><b>valid_after_date:</b> UTC date (YYYY-MM-DD) when relays or bridges have
been listed as running.</li>

<li><b>server:</b> Server type, which can be either <b>"relay"</b> or
<b>"bridge"</b>.</li>

<li><b>guard_relay:</b> Whether relays included in this line have the
<b>"Guard"</b> relay flag (<b>"t"</b>) or not (<b>"f"</b>).
If this column contains the empty string, all running relays are included,
regardless of assigned relay flags.
Always the empty string for bridges.</li>

<li><b>exit_relay:</b> Whether relays included in this line have the
<b>"Exit"</b> relay flag (<b>"t"</b>) or not (<b>"f"</b>).
If this column contains the empty string, all running relays are included,
regardless of assigned relay flags.
Always the empty string for bridges.</li>

<li><b>announced_ipv6:</b> Whether relays or bridges have announced an IPv6
address in their server descriptor (<b>"t"</b>) or not (<b>"f"</b>).
If this column contains the empty string, all running relays or bridges are
included, regardless of whether they have announced an IPv6 address.</li>

<li><b>exiting_ipv6_relay:</b> Whether relays have announced an IPv6 exit policy
other than <b>"reject 1-65535"</b> in their server descriptor (<b>"t"</b>) or
not (<b>"f"</b>).
If this column contains the empty string, all running relays are included,
regardless of IPv6 exit policy.
Always the empty string for bridges.</li>

<li><b>reachable_ipv6_relay:</b> Whether enough directory authorities have
confirmed reachability of an IPv6 OR address announced by a relay, by including
an "a" line in the consensus (<b>"t"</b>) or not (<b>"f"</b>).
If this column contains the empty string, all running relays are included,
regardless of whether their IPv6 address was found reachable.
Always the empty string for bridges.</li>

<li><b>server_count_sum_avg:</b> Number of relays or bridges matching the
criteria in the previous columns, computed as the mean value over all statuses
published on the given date.</li>

<li><b>advertised_bandwidth_bytes_sum_avg:</b> Total advertised bandwidth of all
relays matching the previous criteria, computed as the mean value over all
statuses published on the given date.
Always the empty string for bridges.</li>

</ul>

</div>

<div class="container">
<h2>Advertised bandwidth distribution and n-th fastest relays <a href="#advbwdist" name="advbwdist" class="anchor">#</a></h2>

<p>The following data file contains statistics on the distribution of <a
href="glossary.html#advertised-bandwidth">advertised bandwidth</a> of relays in the
network.  These statistics include advertised bandwidth percentiles and
advertised bandwidth values of the n-th fastest relays.  All values are obtained
from advertised bandwidths of running relays in a <a
href="glossary.html#consensus">network status consensus</a>.  The data file
contains daily (median) averages of percentiles and n-th largest values.</p>

<p><b>Download as <a href="stats/advbwdist.csv">CSV file</a>.</b></p>

<p>The statistics file contains the following columns:</p>
<ul>

<li><b>date:</b> UTC date (YYYY-MM-DD) when relays have been listed as
running.</li>

<li><b>isexit:</b> Whether relays included in this line have the <b>"Exit"</b>
relay flag, which would be indicated as <b>"t"</b>.  If this column contains the
empty string, advertised bandwidths from all running relays are included,
regardless of assigned relay flags.</li>

<li><b>relay:</b> Position of the relay in an ordered list of all advertised
bandwidths, starting at 1 for the fastest relay in the network.  May be the
empty string if this line contains advertised bandwidth by percentile.</li>

<li><b>percentile:</b> Advertised bandwidth percentile given in this line.  May
be the empty string if this line contains advertised bandwidth by fastest
relays.</li>

<li><b>advbw:</b> Advertised bandwidth in B/s.</li>

</ul>

</div>

<div class="container">
<h2>Estimated number of clients in the Tor network <a href="#clients" name="clients" class="anchor">#</a></h2>

<p>The following data file contains estimates on the number of <a
href="glossary.html#client">clients</a> in the network.  These numbers are derived
from directory requests counted on <a
href="glossary.html#directory-authority">directory authorities</a>, <a
href="glossary.html#directory-mirror">directory mirrors</a>, and <a
href="glossary.html#bridge">bridges</a>.  Statistics are available for clients
connecting directly relays and clients connecting via bridges.  There are
statistics available by country (for both directly-connecting clients and
clients connecting via bridges), by transport protocol (only for clients
connecting via bridges), and by IP version (only for clients connecting via
bridges).  Statistics also include predicted client numbers from past
observations, which can be used to detect censorship events.</p>

<p><b>Download as <a href="stats/clients.csv">CSV file</a>.</b></p>

<p>The statistics file contains the following columns:</p>
<ul>

<li><b>date:</b> UTC date (YYYY-MM-DD) for which client numbers are
estimated.</li>

<li><b>node:</b> The node type to which clients connect first, which can be
either <b>"relay"</b> or <b>"bridge"</b>.</li>

<li><b>country:</b> Two-letter lower-case country code as found in a GeoIP
database by resolving clients' IP addresses, or <b>"??"</b> if client IP
addresses could not be resolved.  If this column contains the empty string, all
clients are included, regardless of their country code.</li>

<li><b>transport:</b> Transport name used by clients to connect to the Tor
network using bridges.  Examples are <b>"obfs2"</b>, <b>"obfs3"</b>,
<b>"websocket"</b>, or <b>"&lt;OR&gt;"</b> (original onion routing protocol).
If this column contains the empty string, all clients are included, regardless
of their transport.  There are no statistics on the number of clients by
transport that connect to the Tor network via relays.</li>

<li><b>version:</b> IP version used by clients to connect to the Tor network
using bridges.  Examples are <b>"v4"</b> and <b>"v6"</b>.  If this column
contains the empty string, all clients are included, regardless of their IP
version.  There are no statistics on the number of clients by IP version that
connect directly to the Tor network using relays.</li>

<li><b>lower:</b> Lower number of expected clients under the assumption that
there has been no censorship event.  If this column contains the empty string,
there are no expectations on the number of clients.</li>

<li><b>upper:</b> Upper number of expected clients under the assumption that
there has been no release of censorship.  If this column contains the empty
string, there are no expectations on the number of clients.</li>

<li><b>clients:</b> Estimated number of clients.</li>

<li><b>frac:</b> Fraction of relays or bridges in percent that the estimate is
based on.  The higher this value, the more reliable is the estimate.  Values
above 50 can be considered reliable enough for most purposes, lower values
should be handled with more care.</li>

</ul>

</div>

<div class="container">
<h2>Estimated number of clients by country and transport <a href="#userstats-combined" name="userstats-combined" class="anchor">#</a></h2>

<p>The following data file contains additional statistics on the number of <a
href="glossary.html#client">clients</a> in the network.  This data file is related
to the <a href="stats.html#clients">clients-data file</a> that contains estimates
on the number of clients by country and by transport protocol.  This data file
enhances these statistics by containing estimates of clients connecting to <a
href="glossary.html#bridge">bridges</a> by a given country and using a given <a
href="glossary.html#pluggable-transport">transport protocol</a>.  Even though
bridges don't report a combination of clients by country and transport, it's
possible to derive lower and upper bounds from existing usage statistics.</p>

<p><b>Download as <a href="stats/userstats-combined.csv">CSV file</a>.</b></p>

<p>The statistics file contains the following columns:</p>
<ul>

<li><b>date:</b> UTC date (YYYY-MM-DD) for which client numbers are
estimated.</li>

<li><b>node:</b> The node type to which clients connect first, which is always
<b>"bridge"</b>, because relays don't report responses by transport.</li>

<li><b>country:</b> Two-letter lower-case country code as found in a GeoIP
database by resolving clients' IP addresses, or <b>"??"</b> if client IP
addresses could not be resolved.</li>

<li><b>transport:</b> Transport name used by clients to connect to the Tor
network using bridges.  Examples are <b>"obfs2"</b>, <b>"obfs3"</b>,
<b>"websocket"</b>, or <b>"&lt;OR&gt;"</b> (original onion routing
protocol).</li>

<li><b>version:</b> IP version used by clients to connect to the Tor network
using bridges.  This column always contains the empty string and is only
included for compatibility reasons.</li>

<li><b>frac:</b> Fraction of relays or bridges in percent that the estimate is
based on.  The higher this value, the more reliable is the estimate.  Values
above 50 can be considered reliable enough for most purposes, lower values
should be handled with more care.</li>

<li><b>low:</b> Lower bound of users by country and transport, calculated as sum
over all bridges having reports for the given country and transport, that is,
the sum of <b>M(b)</b>, where for each bridge <b>b</b> define <b>M(b) := max(0,
C(b) + T(b) - S(b))</b> using the following definitions: <b>C(b)</b> is the
number of users from a given country reported by <b>b</b>; <b>T(b)</b> is the
number of users using a given transport reported by <b>b</b>; and <b>S(b)</b> is
the total numbers of users reported by <b>b</b>.  Reasoning: If the sum <b>C(b)
+ T(b)</b> exceeds the total number of users from all countries and transports
<b>S(b)</b>, there must be users from that country and transport.  And if that
is not the case, <b>0</b> is the lower limit.</li>

<li><b>high:</b> Upper bound of users by country and transport, calculated as
sum over all bridges having reports for the given country and transport, that
is, the sum of <b>m(b)</b>, where for each bridge <b>b</b> define
<b>m(b):=min(C(b), T(b))</b> where we use the definitions from <b>low</b>
(above).  Reasoning: there cannot be more users by country and transport than
there are users by either of the two numbers.</li>

</ul>

</div>

<div class="container">
<h2>Performance of downloading static files over Tor <a href="#torperf-1.1" name="torperf-1.1" class="anchor">#</a></h2>

<p>The following data file contains aggregate statistics on performance when
downloading static files of different sizes over Tor.  These statistics are
generated by <a href="https://github.com/robgjansen/onionperf">OnionPerf</a> and
its predecessor <a href="https://gitweb.torproject.org/torperf.git">Torperf</a>,
which both periodically fetch static files over Tor and record several
timestamps in the process.  The data file contains daily medians and quartiles
as well as total numbers of requests, timeouts, and failures.  Raw Onionperf and
Torperf measurement data is specified and available for download on the <a
href="/collector.html#torperf">CollecTor</a> page.</p>

<p><b>Download as <a href="stats/torperf-1.1.csv">CSV file</a>.</b></p>

<p>The statistics file contains the following columns:</p>
<ul>

<li><b>date:</b> UTC date (YYYY-MM-DD) when download performance was
measured.</li>

<li><b>filesize:</b> Size of the downloaded file in bytes.</li>

<li><b>source:</b> Name of the OnionPerf or Torperf service performing
measurements.  If this column contains the empty string, all measurements are
included, regardless of which service performed them.</li>

<li><b>server:</b> Either <b>"public"</b> if the request was made to a server on
the public internet, or <b>"onion"</b> if the request was made to a version 2
onion server.</li>

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

<li><b>requests:</b> Total number of requests made to download the static file
over Tor.</li>

</ul>

</div>

<div class="container">
<h2>Fraction of connections used uni-/bidirectionally <a href="#connbidirect2" name="connbidirect2" class="anchor">#</a></h2>

<p>The following data file contains statistics on the fraction of direct
connections between a <a href="glossary.html#relay">relay</a> and other nodes in
the network that are used uni- or bidirectionally.  Every 10 seconds, relays
determine for every direct connection whether they read and wrote less than a
threshold of 20 KiB.  Connections below this threshold are excluded from the
statistics file.  For the remaining connections, relays determine whether they
read/wrote at least 10 times as many bytes as they wrote/read.  If so, they
classify a connection as "mostly reading" or "mostly writing", respectively.
All other connections are classified as "both reading and writing".  After
classifying connections, read and write counters are reset for the next
10-second interval.  The data file contains daily medians and quartiles of
reported fractions.</p>

<p><b>Download as <a href="stats/connbidirect2.csv">CSV file</a>.</b></p>

<p>The statistics file contains the following columns:</p>
<ul>

<li><b>date:</b> UTC date (YYYY-MM-DD) for which statistics on
uni-/bidirectional connection usage were reported.</li>

<li><b>direction:</b> Direction of reported fraction, which can be
<b>"read"</b>, <b>"write"</b>, or <b>"both"</b> for connections classified as
"mostly reading", "mostly writing", or "both reading as writing".  Connections
below the threshold have been removed from this statistics file entirely.</li>

<li><b>quantile:</b> Quantile of the reported fraction when considering all
statistics reported for this date.  Examples are <b>"0.5"</b> for the median and
<b>"0.25"</b> and <b>"0.75"</b> for the lower and upper quartile.</li>

<li><b>fraction:</b> Fraction of connections in percent for the given date,
direction, and quantile.  For each daily statistic reported by a relay,
fractions for the three directions "read", "write", and "both" sum up to exactly
100.</li>

</ul>

</div>

<div class="container">
<h2>Onion-service statistics <a href="#hidserv" name="hidserv" class="anchor">#</a></h2>

<p>The following data file contains <a
href="glossary.html#onion-service">onion-service</a> statistics gathered by a
small subset of <a href="glossary.html#relay">relays</a> and extrapolated to
network totals.  Statistics include the amount of onion-service traffic and the
number of onion-service addresses in the network per day.  For more details on
the extrapolation algorithm, see <a
href="https://blog.torproject.org/blog/some-statistics-about-onions" target="_blank">this blog
post</a> and <a
href="https://research.torproject.org/techreports/extrapolating-hidserv-stats-2015-01-31.pdf" target="_blank">this
technical report</a>.</p>

<p><b>Download as <a href="stats/hidserv.csv">CSV file</a>.</b></p>

<p>The statistics file contains the following columns:</p>
<ul>

<li><b>date:</b> UTC date (YYYY-MM-DD) when relays or bridges have been listed
as running.</li>

<li><b>type:</b> Type of onion-service statistic reported by relays and
extrapolated to network totals.  Examples include <b>"rend-relayed-cells"</b>
for the number of cells on rendezvous circuits observed by rendezvous points and
<b>"dir-onions-seen"</b> for the number of unique .onion addresses observed by
onion-service directories.</li>

<li><b>wmean:</b> Weighted mean of extrapolated network totals.</li>

<li><b>wmedian:</b> Weighted median of extrapolated network totals.</li>

<li><b>wiqm:</b> Weighted interquartile mean of extrapolated network
totals.</li>

<li><b>frac:</b> Total network fraction of reported statistics.</li>

<li><b>stats:</b> Number of reported statistics with non-zero computed network
fraction.</li>

</ul>

</div>

<div class="container">
<h2>Requests to <code>torproject.org</code> web servers <a href="#webstats" name="webstats" class="anchor">#</a></h2>

<p>The following data file contains aggregate statistics on requests to <code>torproject.org</code> web servers.</p>

<p><b>Download as <a href="stats/webstats.csv">CSV file</a>.</b></p>

<p>The statistics file contains the following columns:</p>
<ul>
<li><b>log_date:</b> UTC date (YYYY-MM-DD) when requests to <code>torproject.org</code> web servers have been logged.</li>
<li><b>request_type:</b> Request type with fixed identifiers as follows:
<ul>
<li><b>"tbid":</b> Tor Browser initial downloads: GET requests to all sites with resource strings <code>'%/torbrowser/%.exe'</code>, <code>'%/torbrowser/%.dmg'</code>, and <code>'%/torbrowser/%.tar.xz'</code> and response code 200.</li>
<li><b>"tbsd":</b> Tor Browser signature downloads: GET requests to all sites with resource strings <code>'%/torbrowser/%.exe.asc'</code>, <code>'%/torbrowser/%.dmg.asc'</code>, and <code>'%/torbrowser/%.tar.xz.asc'</code> and response code 200.</li>
<li><b>"tbup":</b> Tor Browser update pings: GET requests to all sites with resource strings <code>'%/torbrowser/update\__/%'</code> and response code 200.</li>
<li><b>"tbur":</b> Tor Browser update requests: GET requests to all sites with resource strings <code>'%/torbrowser/%.mar'</code> and response code 302.</li>
<li><b>"tmid":</b> Tor Messenger initial downloads: GET requests to all sites with resource strings <code>'%/tormessenger/%.exe'</code>, <code>'%/tormessenger/%.dmg'</code>, and <code>'%/tormessenger/%.tar.xz'</code> and response code 200.</li>
<li><b>"tmup":</b> Tor Messenger update pings: GET requests to all sites with resource strings <code>'%/tormessenger/update\__/%'</code> and response code 200.</li>
<li><b>"twhph":</b> Tor website home page hits: GET requests to sites <code>'torproject.org'</code> and <code>'www.torproject.org'</code> with resource strings <code>'/'</code> and <code>'/index%'</code> and response code 200.</li>
<li><b>"twdph":</b> Tor website download page hits: GET requests to sites <code>'torproject.org'</code> and <code>'www.torproject.org'</code> with resource strings <code>'/download/download%'</code> and <code>'/projects/torbrowser.html%'</code> and response code 200.</li>
</ul>
</li>
<li><b>platform:</b> Platform string, like <b>"w"</b> for Windows, <b>"m"</b> for macOS, or <b>"l"</b> for Linux, <b>"o"</b> for other platforms, and the empty string for all platforms.</li>
<li><b>channel:</b> Release channel, like <b>"r"</b> for stable releases, <b>"a"</b> for alpha releases, <b>"h"</b> for hardened releases, and the empty string for all channels.</li>
<li><b>locale:</b> Locale, like <b>"en-US"</b> for English (United States), <b>"de"</b> for German, etc., <b>"??"</b> for unrecognized locales, and the empty string for all locales.</li>
<li><b>incremental:</b> Incremental update, with <b>"t"</b> for incremental updates, <b>"f"</b> for non-incremental (full) updates, and the empty string for all updates types.</li>
<li><b>count:</b> Number of request for the given request type, platform, etc.</li>
</ul>

</div>

<jsp:include page="bottom.jsp"/>

