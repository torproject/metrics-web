<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
  <title>Tor Metrics Portal: Data Formats</title>
  <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">
  <link href="/css/stylesheet-ltr.css" type="text/css" rel="stylesheet">
  <link href="/images/favicon.ico" type="image/x-icon" rel="shortcut icon">
</head>
<body>
<div class="center">
<%@ include file="banner.jsp"%>
<div class="main-column">
<h2>Tor Metrics Portal: Data Formats</h2>
<br>

<p>
Statistical analysis in the Tor network can be performed using various
kinds of data.
This page gives an overview of three major data sources for
statistics in the Tor network:</p>

<ol>
<li>First, we recap measuring the Tor network from public directory
information (<a href="https://research.torproject.org/techreports/metrics-2009-08-07.pdf">PDF</a>)
by describing the data format of
<a href="#serverdesc">server descriptors and network statuses</a>,
and we explain the sanitzation process of (non-public)
<a href="#bridgedesc">bridge directory information</a>.</li>
<li>Second, we describe the numerous aggregate statistics that relays
publish about their usage (<a href="http://freehaven.net/anonbib/cache/wecsr10measuring-tor.pdf">PDF</a>),
including
<a href="#bytehist">byte histories</a>,
<a href="#dirreqstats">directory request statistics</a>,
<a href="#entrystats">connecting client statistics</a>,
<a href="#bridgestats">bridge user statistics</a>,
<a href="#cellstats">cell-queue statistics</a>,
<a href="#exitstats">exit-port statistics</a>, and
<a href="#bidistats">bidirectional connection use</a>.</li>
<li>Third, we delineate the output of various Tor services like
<a href="#bridgepool">BridgeDB</a>, or
<a href="#exitlist">Tor Check</a> as well as specific measurement tools like
<a href="#torperf">Torperf</a>.</li>
</ol>

<p>
All data described on this page are available for download on the
<a href="data.html">data</a> page.
This page is based on a technical report
(<a href="https://research.torproject.org/techreports/data-2011-03-14.pdf">PDF</a>)
and is very likely more recent than the report.
</p>
<hr>
<br>

<a name="descriptortypes"></a>
<h3><a href="#descriptortypes" class="anchor">Descriptor types</a></h3>
<br>
<p>
Any file containing descriptors described on this page may contain meta
data in its first text line using the format
<tt>@type $descriptortype $major.$minor</tt>.
Any tool that processes these descriptors may parse files without meta
data or with an unknown descriptor type at its own risk, can safely parse
files with known descriptor type and same major version number, and should
not parse files with known descriptor type and higher major version
number.
</p>

<p>
The following descriptor types and versions are known.
Gray entries are either not yet implemented or deprecated, black entries
are recent:
</p>

<ul>
<li><tt>@type server-descriptor 1.0</tt></li>
<li><tt>@type extra-info 1.0</tt></li>
<li><tt>@type directory 1.0</tt></li>
<li><tt>@type network-status-2 1.0</tt></li>
<li><tt>@type dir-key-certificate-3 1.0</tt></li>
<li><tt>@type network-status-consensus-3 1.0</tt></li>
<li><tt>@type network-status-vote-3 1.0</tt></li>
<li><tt>@type network-status-microdesc-consensus-3 1.0</tt></li>
<li><tt>@type bridge-network-status 1.0</tt></li>
<li><tt>@type bridge-server-descriptor 1.0</tt></li>
<li><tt><font color="gray">@type bridge-extra-info 1.0</font></tt></li>
<li><tt>@type bridge-extra-info 1.1</tt> contain sanitized
  <tt>transport</tt> lines</li>
<li><tt>@type torperf 1.0</tt></li>
<li><tt>@type bridge-pool-assignment 1.0</tt></li>
<li><tt>@type tordnsel 1.0</tt></li>
</ul>

<hr>
<br>

<a name="serverdesc"></a>
<h3><a href="#serverdesc" class="anchor">Server descriptors and network
statuses</a></h3>
<br>
<p>
Relays in the Tor network report their capabilities by publishing server
descriptors to the directory authorities.
The directory authorities confirm reachability of relays and assign flags
to help clients make good path selections.
Every hour, the directory authorities publish a network status consensus
with all known running relays at the time.
Both server descriptors and network statuses constitute a solid data basis
for statistical analysis in the Tor network.
We described the approach to measure the Tor network from public directory
information in the HotPETS 2009 paper
(<a href="https://research.torproject.org/techreports/metrics-2009-08-07.pdf">PDF</a>)
and provide interactive
<a href="graphs.html">graphs</a> on the metrics
website.
We briefly describe the most interesting pieces of the
two descriptor formats that can be used for statistics.
</p>

<p>
The server descriptors published by relays at least once every 18 hours
contain the necessary information for clients to build circuits using a
given relay.
These server descriptors can also be useful for statistical analysis of
the Tor network infrastructure.
</p>

<p>
We assume that the majority of server descriptors are correct.
But when performing statistical analysis on server descriptors, one has to
keep in mind that only a small subset of the information written to server
descriptors is confirmed by the trusted directory authorities.
In theory, relays can provide false information in their server
descriptors, even though the incentive to do so is probably low.
</p>

<blockquote>
<p>
<i>Server descriptor published by relay <tt>blutmagie</tt> (without
cryptographic keys and hashes):</i>
</p>
<p>
<tt>router blutmagie 192.251.226.206 443 0 80</tt><br>
<tt>platform Tor 0.2.2.20-alpha on Linux x86_64</tt><br>
<tt>opt protocols Link 1 2 Circuit 1</tt><br>
<tt>published 2010-12-27 14:35:27</tt><br>
<tt>opt fingerprint 6297 B13A 687B 521A 59C6 BD79 188A 2501 EC03 A065</tt><br>
<tt>uptime 445412</tt><br>
<tt>bandwidth 14336000 18432000 15905178</tt><br>
<tt>opt extra-info-digest 5C1D5D6F8B243304079BC15CD96C7FCCB88322D4</tt><br>
<tt>opt caches-extra-info</tt><br>
<tt>onion-key</tt><br>
<tt>[...]</tt><br>
<tt>signing-key</tt><br>
<tt>[...]</tt><br>
<tt>family $66CA87E164F1CFCE8C3BB5C095217A28578B8BAF
  $67EC84376D9C4C467DCE8621AACA109160B5264E
  $7B698D327F1695590408FED95CDEE1565774D136</tt><br>
<tt>opt hidden-service-dir</tt><br>
<tt>contact abuse@blutmagie.de</tt><br>
<tt>reject 0.0.0.0/8:*</tt><br>
<tt>reject 169.254.0.0/16:*</tt><br>
<tt>reject 127.0.0.0/8:*</tt><br>
<tt>reject 192.168.0.0/16:*</tt><br>
<tt>reject 10.0.0.0/8:*</tt><br>
<tt>reject 172.16.0.0/12:*</tt><br>
<tt>reject 192.251.226.206:*</tt><br>
<tt>reject *:25</tt><br>
<tt>reject *:119</tt><br>
<tt>reject *:135-139</tt><br>
<tt>reject *:445</tt><br>
<tt>reject *:465</tt><br>
<tt>reject *:563</tt><br>
<tt>reject *:587</tt><br>
<tt>reject *:1214</tt><br>
<tt>reject *:4661-4666</tt><br>
<tt>reject *:6346-6429</tt><br>
<tt>reject *:6660-6999</tt><br>
<tt>accept *:*</tt><br>
<tt>router-signature</tt><br>
<tt>[...]</tt><br>
</p>
</blockquote>

<p>
The document above shows an example server descriptor.
The following data fields in server descriptors may be relevant to
statistical analysis:
</p>

<ul>
<li><b>IP address and ports:</b> Relays provide their IP address
and ports where they accept requests to build circuits and directory
requests.
These data fields are contained in the first line of a server descriptor
starting with <tt>router</tt>.
Note that in rare cases, the IP address provided here can be different
from the IP address used for exiting to the Internet.
The latter can be found in the exit lists produced by Tor Check as
described in the <a href="#exitlist">Tor Check exit lists</a> section below.
</li>
<li><b>Operating system and Tor software version:</b> Relays include
their operating system and Tor software version in their server
descriptors in the <tt>platform</tt> line.
While this information is very likely correct in most cases, a few relay
operators may try to impede hacking attempts by providing false platform
strings.</li>
<li><b>Uptime:</b> Relays include the number of seconds since the
last restart in their server descriptor in the <tt>uptime</tt> line.</li>
<li><b>Own measured bandwidth:</b> Relays report the bandwidth that
they are willing to provide on average and for short periods of time.
Relays also perform periodic bandwidth self-tests and report their actual
available bandwidth.
The latter was used by clients to weight relays in the path selection
algorithm and was sometimes subject to manipulation by malicious relays.
All three bandwidth values can be found in a server descriptor's
<tt>bandwidth</tt> line.
With the introduction of
<a href="http://gitweb.torproject.org/torflow.git/">bandwidth scanners</a>, the self-reported relay
bandwidth in server descriptors has become less
relevant.</li>
<li><b>Relay family:</b> Some relay operators who run more than one
relay organize their relays in relay families, so that clients don't pick
more than one of these relays for a single circuit.
Each relay belonging to a relay family lists the members of that family
either by nickname or fingerprint in its server descriptor in the
<tt>family</tt> line.</li>
<li><b>Exit policy:</b> Relays define their exit policy by including
firewall-like rules which outgoing connections they reject or accept in
the <tt>reject</tt> and <tt>accept</tt> lines.</li>
</ul>

<p>
These are just a subset of the fields in a server descriptor that seem
relevant for statistical analysis.
For a complete list of fields in server descriptors, see the <a href="https://gitweb.torproject.org/torspec.git/blob/HEAD:/dir-spec.txt">directory
protocol specification</a>.
</p>

<p>
Every hour, the directory authorities publish a new network status that
contains a list of all running relays.
The directory authorities confirm reachability of the contained relays and
assign flags based on the relays' characteristics.
The entries in a network status reference the last published server
descriptor of a relay.
</p>

<p>
The network statuses are relevant for statistical analysis, because they
constitute trusted snapshots of the Tor network.
Anyone can publish as many server descriptors as they want, but only the
directory authorities can confirm that a relay was running at a given
time.
Most statistics on the Tor network infrastructure rely on network statuses
and possibly combine them with the referenced server descriptors.
The document below shows the network status entry referencing
the server descriptor above.
In addition to the reachability information, network statuses contain the
following fields that may be relevant for statistical analysis:
</p>

<blockquote>
<p>
<i>Network status entry of relay <tt>blutmagie</tt>:</i>
</p>
<p>
<tt>r blutmagie YpexOmh7UhpZxr15GIolAewDoGU
  lFY7WmD/yvVFp9drmZzNeTxZ6dw 2010-12-27 14:35:27 192.251.226.206
  443 80</tt><br>
<tt>s Exit Fast Guard HSDir Named Running Stable V2Dir Valid</tt><br>
<tt>v Tor 0.2.2.20-alpha</tt><br>
<tt>w Bandwidth=30800</tt><br>
<tt>p reject 25,119,135-139,445,465,563,587,1214,4661-4666,6346-6429,6660-6999</tt><br>
</p>
</blockquote>

<ul>
<li><b>Relay flags:</b> The directory authorities assign flags to
relays based on their characteristics to the line starting with <tt>s</tt>.
Examples are the <tt>Exit</tt> flag if a relay permits exiting to the
Internet and the <tt>Guard</tt> flag if a relay is stable enough to be
picked as guard node</li>
<li><b>Relay version:</b> The directory authorities include the
version part of the platform string written to server descriptors in the
network status in the line starting with <tt>v</tt>.</li>
<li><b>Bandwidth weights:</b> The network status contains a bandwidth
weight for every relay in the lines with <tt>w</tt> that clients shall use
for weighting relays in their path selection algorithm.
This bandwidth weight is either the self-reported bandwidth of the relay
or the bandwidth measured by the bandwidth scanners.</li>
<li><b>Exit policy summary:</b> Every entry in a network status
contains a summary version of a relay's exit policy in the line starting
with <tt>p</tt>.
This summary is a list of accepted or rejected ports for exit to most IP
addresses.</li>
</ul>
<hr>
<br>

<a name="bridgedesc"></a>
<h3><a href="#bridgedesc" class="anchor">Sanitized bridge
descriptors</a></h3>
<br>
Bridges in the Tor network publish server descriptors to the bridge
authority which in turn generates a bridge network status.
We cannot, however, make the bridge server descriptors and bridge network
statuses available for statistical analysis as we do with the relay server
descriptors and relay network statuses.
The problem is that bridge server descriptors and network statuses contain
bridge IP addresses and other sensitive information that shall not be made
publicly available.
We therefore sanitize bridge descriptors by removing all potentially
identifying information and publish sanitized versions of the descriptors.
The processing steps for sanitizing bridge descriptors are as follows:

<ol>
<li><b>Replace the bridge identity with its SHA1 value:</b> Clients
can request a bridge's current descriptor by sending its identity string
to the bridge authority.
This is a feature to make bridges on dynamic IP addresses useful.
Therefore, the original identities (and anything that could be used to
derive them) need to be removed from the descriptors.
The bridge identity is replaced with its SHA1 hash value.
The idea is to have a consistent replacement that remains stable over
months or even years (without keeping a secret for a keyed hash function).</li>
<li><b>Remove all cryptographic keys and signatures:</b> It would be
straightforward to learn about the bridge identity from the bridge's
public key.
Replacing keys by newly generated ones seemed to be unnecessary (and would
involve keeping a state over months/years), so that all cryptographic
objects have simply been removed.</li>
<li><b>Replace IP address with IP address hash:</b> Of course, IP
addresses need to be sanitized, too.
<ul><li>IPv4 addresses are replaced with <tt>10.x.x.x</tt> with
<tt>x.x.x</tt> being the 3 byte output of
<tt>H(IP address | bridge identity | secret)[:3]</tt>.
The input <tt>IP address</tt> is the 4-byte long binary representation of
the bridge's current IP address.
The <tt>bridge identity</tt> is the 20-byte long binary representation of
the bridge's long-term identity fingerprint.
The <tt>secret</tt> is a 31-byte long secure random string that changes once
per month for all descriptors and statuses published in that month.
<tt>H()</tt> is SHA-256.
The <tt>[:3]</tt> operator means that we pick the 3 most significant bytes
of the result.</li>
<li>IPv6 addresses are replaced with <tt>[fd9f:2e19:3bcf::xx:xxxx]</tt>
with <tt>xx:xxxx</tt> being the hex-formatted 3 byte output of a similar
hash function as described for IPv4 addresses.
The only differences are that the input <tt>IP address</tt> is 16 bytes
long and the <tt>secret</tt> is only 19 bytes long.</li></ul>
<li><b>Replace contact information:</b> If there is contact
information in a descriptor, the contact line is changed to
<tt>somebody</tt>.</li>
<li><b>Remove pluggable transport addresses and arguments:</b> Bridges may
provide transports in addition to the onion-routing protocol and include
information about these transports in their extra-info descriptors for
BridgeDB.  In that case, any IP addresses, TCP ports, or additional
arguments are removed, only leaving in the supported transport names.</li>
</ol>

<p>
The two documents below show an example bridge server
descriptor that is referenced from a bridge network status entry.
For more details about this process, see the
<a href="https://gitweb.torproject.org/metrics-db.git">metrics data processor</a> software.
</p>

<blockquote>
<p>
<i>Sanitized bridge server descriptor:</i>
</p>
<p>
<tt>router Unnamed 10.74.150.129 443 0 0</tt><br>
<tt>platform Tor 0.2.2.19-alpha (git-1988927edecce4c7) on Linux i686</tt><br>
<tt>opt protocols Link 1 2 Circuit 1</tt><br>
<tt>published 2010-12-27 18:55:01</tt><br>
<tt>opt fingerprint A5FA 7F38 B02A 415E 72FE 614C 64A1 E5A9 2BA9 9BBD</tt><br>
<tt>uptime 2347112</tt><br>
<tt>bandwidth 5242880 10485760 1016594</tt><br>
<tt>opt extra-info-digest 86E6E9E68707AF586FFD09A36FAC236ADA0D11CC</tt><br>
<tt>opt hidden-service-dir</tt><br>
<tt>contact somebody</tt><br>
<tt>reject *:*</tt><br>
</p>
</blockquote>

<blockquote>
<p>
<i>Sanitized bridge network status entry:</i>
</p>
<p>
<tt>r Unnamed pfp/OLAqQV5y/mFMZKHlqSupm70 dByzfWWLas9cen7PtZ3XGYIJHt4
  2010-12-27 18:55:01 10.74.150.129 443 0</tt><br>
<tt>s Fast Guard HSDir Running Stable Valid</tt><br>
</p>
</blockquote>
<hr>
<br>

<a name="bytehist"></a>
<h3><a href="#bytehist" class="anchor">Byte histories</a></h3>
<br>
<p>
Relays include aggregate statistics in their descriptors that they upload
to the directory authorities.
These aggregate statistics are contained in extra-info descriptors that
are published in companion with server descriptors.
Extra-info descriptors are not required for clients to build circuits.
An extra-info descriptor belonging to a server descriptor is referenced by
its SHA1 hash value.
</p>

<p>
Byte histories were the first statistical data that relays published about
their usage.
Relays report the number of written and read bytes in 15-minute intervals
throughout the last 24 hours.
The extra-info descriptor in the document below contains the byte
histories in the two lines starting with <tt>write-history</tt> and
<tt>read-history</tt>.
More details about these statistics can be found in the <a href="https://gitweb.torproject.org/torspec.git/blob/HEAD:/dir-spec.txt">directory protocol
specification</a>.
</p>

<blockquote>
<p>
<i>Extra-info descriptor published by relay <tt>blutmagie</tt>
(without cryptographic signature and with long lines being truncated):</i>
</p>
<p>
<tt>extra-info blutmagie 6297B13A687B521A59C6BD79188A2501EC03A065</tt><br>
<tt>published 2010-12-27 14:35:27</tt><br>
<tt>write-history 2010-12-27 14:34:05 (900 s) 12902389760,12902402048,12859373568,12894131200,[...]</tt><br>
<tt>read-history 2010-12-27 14:34:05 (900 s) 12770249728,12833485824,12661140480,12872439808,[...]</tt><br>
<tt>dirreq-write-history 2010-12-27 14:26:13 (900 s) 51731456,60808192,56740864,54948864,[...]</tt><br>
<tt>dirreq-read-history 2010-12-27 14:26:13 (900 s) 4747264,4767744,4511744,4752384,[...]</tt><br>
<tt>dirreq-stats-end 2010-12-27 10:51:09 (86400 s)</tt><br>
<tt>dirreq-v3-ips us=2000,de=1344,fr=744,kr=712,[...]</tt><br>
<tt>dirreq-v2-ips ??=8,au=8,cn=8,cz=8,[...]</tt><br>
<tt>dirreq-v3-reqs us=2368,de=1680,kr=1048,fr=800,[...]</tt><br>
<tt>dirreq-v2-reqs id=48,??=8,au=8,cn=8,[...]</tt><br>
<tt>dirreq-v3-resp ok=12504,not-enough-sigs=0,unavailable=0,not-found=0,not-modified=0,busy=128</tt><br>
<tt>dirreq-v2-resp ok=64,unavailable=0,not-found=8,not-modified=0,busy=8</tt><br>
<tt>dirreq-v2-share 1.03%</tt><br>
<tt>dirreq-v3-share 1.03%</tt><br>
<tt>dirreq-v3-direct-dl complete=316,timeout=4,running=0,min=4649,d1=36436,d2=68056,q1=76600,d3=87891,d4=131294,md=173579,d6=229695,d7=294528,q3=332053,d8=376301,d9=530252,max=2129698</tt><br>
<tt>dirreq-v2-direct-dl complete=16,timeout=52,running=0,min=9769,d1=9769,d2=9844,q1=9981,d3=9981,d4=27297,md=33640,d6=60814,d7=205884,q3=205884,d8=361137,d9=628256,max=956009</tt><br>
<tt>dirreq-v3-tunneled-dl complete=12088,timeout=92,running=4,min=534,d1=31351,d2=49166,q1=58490,d3=70774,d4=88192,md=109778,d6=152389,d7=203435,q3=246377,d8=323837,d9=559237,max=26601000</tt><br>
<tt>dirreq-v2-tunneled-dl complete=0,timeout=0,running=0</tt><br>
<tt>entry-stats-end 2010-12-27 10:51:09 (86400 s)</tt><br>
<tt>entry-ips de=11024,us=10672,ir=5936,fr=5040,[...]</tt><br>
<tt>exit-stats-end 2010-12-27 10:51:09 (86400 s)</tt><br>
<tt>exit-kibibytes-written 80=6758009,443=498987,4000=227483,5004=1182656,11000=22767,19371=1428809,31551=8212,41500=965584,51413=3772428,56424=1912605,other=175227777</tt><br>
<tt>exit-kibibytes-read 80=197075167,443=5954607,4000=1660990,5004=1808563,11000=1893893,19371=130360,31551=7588414,41500=756287,51413=2994144,56424=1646509,other=288412366</tt><br>
<tt>exit-streams-opened 80=5095484,443=359256,4000=4508,5004=22288,11000=124,19371=24,31551=40,41500=96,51413=16840,56424=28,other=1970964</tt><br>
</p>
</blockquote>
<hr>
<br>

<a name="dirreqstats"></a>
<h3><a href="#dirreqstats" class="anchor">Directory requests</a></h3>
<br>
<p>
The directory authorities and directory mirrors report statistical data
about processed directory requests.
Starting with Tor version 0.2.2.15-alpha, all directories report the
number of written and read bytes for answering directory requests.
The format is similar to the format of byte histories as described in the
previous section.
The relevant lines are <tt>dirreq-write-history</tt> and
<tt>dirreq-read-history</tt> in the document listed in the
<a href="#bytehist">Byte histories</a> section above.
These two lines contain the subset of total read and written bytes that
the directory mirror spent on responding to any kind of directory request,
including network statuses, server descriptors, extra-info descriptors,
authority certificates, etc.
</p>

<p>
The directories further report statistics on answering directory requests
for network statuses only.
For Tor versions before 0.2.3.x, relay operators had to manually enable
these statistics, which is why only a few directories report them.
The lines starting with <tt>dirreq-v3-</tt> all belong to the directory
request statistics (the lines starting with <tt>dirreq-v2-</tt> report
similar statistics for version 2 of the directory protocol which is
deprecated at the time of writing this report).
The following fields may be relevant for statistical analysis:
</p>

<ul>
<li><b>Unique IP addresses:</b> The numbers in <tt>dirreq-v3-ips</tt>
denote the unique IP addresses of clients requesting network statuses by
country.</li>
<li><b>Network status requests:</b> The numbers in
<tt>dirreq-v3-reqs</tt> constitute the total network status requests by
country.</li>
<li><b>Request share:</b> The percentage in <tt>dirreq-v3-share</tt> is
an estimate of the share of directory requests that the reporting relay
expects to see in the Tor network.
In a tech report (<a href="https://research.torproject.org/techreports/countingusers-2010-11-30.pdf">PDF</a>)
we found that this estimate isn't very useful
for statistical analysis because of the different approaches that clients
take to select directory mirrors.
The fraction of written directory bytes (<tt>dirreq-write-history</tt>) can
be used to derive a better metric for the share of directory requests.</li>
<li><b>Network status responses:</b> The directories also report
whether they could provide the requested network status to clients in
<tt>dirreq-v3-resp</tt>.
This information was mostly used to diagnose error rates in version 2 of
the directory protocol where a lot of directories replied to network
status requests with <tt>503 Busy</tt>.
In version 3 of the directory protocol, most responses contain the status
code <tt>200 OK</tt>.</li>
<li><b>Network status download times:</b> The line
<tt>dirreq-v3-direct-dl</tt> contains statistics on the download of network
statuses via the relay's directory port.
The line <tt>dirreq-v3-tunneled-dl</tt> contains similar statistics on
downloads via a 1-hop circuit between client and directory (which is the
common approach in version 3 of the directory protocol).
Relays report how many requests have been completed, have timed out, and
are still running at the end of a 24-hour time interval as well as the
minimum, maximum, median, quartiles, and deciles of download times.</li>
</ul>

<p>
More details about these statistics can be found in the <a href="https://gitweb.torproject.org/torspec.git/blob/HEAD:/dir-spec.txt">directory protocol
specification</a>.
</p>
<hr>
<br>

<a name="entrystats"></a>
<h3><a href="#entrystats" class="anchor">Connecting clients</a></h3>
<br>
<p>
Relays can be configured to report per-country statistics on directly
connecting clients.
This metric includes clients connecting to a relay in order to build
circuits and clients creating a 1-hop circuit to request directory
information.
In practice, the latter number outweighs the former number.
The <tt>entry-ips</tt> line in the document listed in the
<a href="#bytehist">Byte histories</a> section above
shows the number
of unique IP addresses connecting to the relay by country.
More details about these statistics can be found in the <a href="https://gitweb.torproject.org/torspec.git/blob/HEAD:/dir-spec.txt">directory protocol
specification</a>.
</p>
<hr>
<br>

<a name="bridgestats"></a>
<h3><a href="#bridgestats" class="anchor">Bridge users</a></h3>
<br>
<p>
Bridges report statistics on connecting bridge clients in their extra-info
descriptors.
The document below shows a bridge extra-info descriptor
with the bridge user statistics in the <tt>bridge-ips</tt> line.

<blockquote>
<p>
<i>Sanitized bridge extra-info descriptor:</i>
</p>
<p>
<tt>extra-info Unnamed A5FA7F38B02A415E72FE614C64A1E5A92BA99BBD</tt><br>
<tt>published 2010-12-27 18:55:01</tt><br>
<tt>write-history 2010-12-27 18:43:50 (900 s) 151712768,176698368,180030464,163150848,[...]</tt><br>
<tt>read-history 2010-12-27 18:43:50 (900 s) 148109312,172274688,172168192,161094656,[...]</tt><br>
<tt>bridge-stats-end 2010-12-27 14:56:29 (86400 s)</tt><br>
<tt>bridge-ips sa=48,us=40,de=32,ir=32,[...]</tt><br>
</p>
</blockquote>

<p>
Bridges running Tor version 0.2.2.3-alpha or earlier report bridge users
in a similar line starting with <tt>geoip-client-origins</tt>.
The reason for switching to <tt>bridge-ips</tt> was that the measurement
interval in <tt>geoip-client-origins</tt> had a variable length, whereas the
measurement interval in 0.2.2.4-alpha and later is set to exactly
24 hours.
In order to clearly distinguish the new measurement intervals from the old
ones, the new keywords have been introduced.
More details about these statistics can be found in the <a href="https://gitweb.torproject.org/torspec.git/blob/HEAD:/dir-spec.txt">directory protocol
specification</a>.
</p>
<hr>
<br>

<a name="cellstats"></a>
<h3><a href="#cellstats" class="anchor">Cell-queue statistics</a></h3>
<br>
<p>
Relays can be configured to report aggregate statistics on their cell
queues.
These statistics include average processed cells, average number of queued
cells, and average time that cells spend in circuits.
Circuits are split into deciles based on the number of processed cells.
The statistics are provided for circuit deciles from loudest to quietest
circuits.
The document below shows the cell statistics contained in an
extra-info descriptor by relay <tt>gabelmoo</tt>.
An early analysis of cell-queue statistics can be found in a tech report
(<a href="https://research.torproject.org/techreports/bufferstats-2009-08-25.pdf">PDF</a>).
More details about these statistics can be found in the <a href="https://gitweb.torproject.org/torspec.git/blob/HEAD:/dir-spec.txt">directory protocol
specification</a>.
</p>

<blockquote>
<p>
<i>Cell statistics in extra-info descriptor by relay <tt>gabelmoo</tt>:</i>
</p>
<p>
<tt>cell-stats-end 2010-12-27 09:59:50 (86400 s)</tt><br>
<tt>cell-processed-cells 4563,153,42,15,7,7,6,5,4,2</tt><br>
<tt>cell-queued-cells 9.39,0.98,0.09,0.01,0.00,0.00,0.00,0.01,0.00,
  0.01</tt><br>
<tt>cell-time-in-queue 2248,807,277,92,49,22,52,55,81,148</tt><br>
<tt>cell-circuits-per-decile 7233</tt><br>
</p>
</blockquote>
<hr>
<br>

<a name="exitstats"></a>
<h3><a href="#exitstats" class="anchor">Exit-port statistics</a></h3>
<br>
<p>
Exit relays running Tor version 0.2.1.1-alpha or higher can be configured
to report aggregate statistics on exiting connections.
These relays report the number of opened streams, written and read bytes
by exiting port.
Until version 0.2.2.19-alpha, relays reported all ports exceeding a
threshold of 0.01 % of all written and read exit bytes.
Starting with version 0.2.2.20-alpha, relays only report the top 10 ports
in exit-port statistics in order not to exceed the maximum extra-info
descriptor length of 50 KB.
The document listed in the
<a href="#bytehist">Byte histories</a> section above contains
exit-port statistics in the lines starting with <tt>exit-</tt>.
More details about these statistics can be found in the <a href="https://gitweb.torproject.org/torspec.git/blob/HEAD:/dir-spec.txt">directory protocol
specification</a>.
</p>
<hr>
<br>

<a name="bidistats"></a>
<h3><a href="#bidistats" class="anchor">Bidirectional connection
use</a></h3>
<br>
<p>
Relays running Tor version 0.2.3.1-alpha or higher can be configured to
report what fraction of connections is used uni- or bi-directionally.
Every 10 seconds, relays determine for every connection whether they read
and wrote less than a threshold of 20 KiB.
Connections below this threshold are labeled as "Below Threshold".
For the remaining connections, relays report whether they read/wrote at
least 10 times as many bytes as they wrote/read.
If so, they classify a connection as "Mostly reading" or "Mostly
writing," respectively.
All other connections are classified as "Both reading and writing."
After classifying connections, read and write counters are reset for the
next 10-second interval.
Statistics are aggregated over 24 hours.
The document below shows the bidirectional connection use
statistics in an extra-info descriptor by relay <tt>zweifaltigkeit</tt>.
The four numbers denote the number of connections "Below threshold,"
"Mostly reading," "Mostly writing," and "Both reading and writing."
More details about these statistics can be found in the <a href="https://gitweb.torproject.org/torspec.git/blob/HEAD:/dir-spec.txt">directory protocol
specification</a>.
</p>

<blockquote>
<p>
<i>Bidirectional connection use statistic in extra-info descriptor
by relay <tt>zweifaltigkeit</tt>:</i>
</p>
<p>
<tt>conn-bi-direct 2010-12-28 15:55:11 (86400 s) 387465,45285,55361,81786</tt>
</p>
</blockquote>
<hr>
<br>

<a name="torperf"></a>
<h3><a href="#torperf" class="anchor">Torperf output files</a></h3>
<br>
<p>
Torperf is a little tool that measures Tor's performance as users
experience it.
Torperf uses a trivial SOCKS client to download files of various sizes
over the Tor network and notes how long substeps take.
Torperf can be
<a href="https://metrics.torproject.org/tools.html">downloaded</a>
from the metrics
website.
A Torperf results file contains a single line per Torperf run with
<tt>key=value</tt> pairs.
Such a result line is sufficient to learn about 1) the Tor and Torperf
configuration, 2) measurement results, and 3) additional information that
might help explain the results.
Known keys are explained below.
</p>
<ul>
<li>Configuration
<ul>
<li><tt>SOURCE:</tt> Configured name of the data source; required.</li>
<li><tt>FILESIZE:</tt> Configured file size in bytes; required.</li>
<li>Other meta data describing the Tor or Torperf configuration, e.g.,
GUARD for a custom guard choice; optional.</li>
</ul>
<li>Measurement results
<ul>
<li><tt>START:</tt> Time when the connection process starts;
required.</li>
<li><tt>SOCKET:</tt> Time when the socket was created; required.</li>
<li><tt>CONNECT:</tt> Time when the socket was connected; required.</li>
<li><tt>NEGOTIATE:</tt> Time when SOCKS 5 authentication methods have been
negotiated; required.</li>
<li><tt>REQUEST:</tt> Time when the SOCKS request was sent; required.</li>
<li><tt>RESPONSE:</tt> Time when the SOCKS response was received;
required.</li>
<li><tt>DATAREQUEST:</tt> Time when the HTTP request was written;
required.</li>
<li><tt>DATARESPONSE:</tt> Time when the first response was received;
required.</li>
<li><tt>DATACOMPLETE:</tt> Time when the payload was complete;
required.</li>
<li><tt>WRITEBYTES:</tt> Total number of bytes written; required.</li>
<li><tt>READBYTES:</tt> Total number of bytes read; required.</li>
<li><tt>DIDTIMEOUT:</tt> 1 if the request timed out, 0 otherwise;
optional.</li>
<li><tt>DATAPERCx:</tt> Time when x% of expected bytes were read for
x = { 10, 20, 30, 40, 50, 60, 70, 80, 90 }; optional.</li>
<li>Other measurement results, e.g., START_RENDCIRC, GOT_INTROCIRC, etc.
for hidden-service measurements; optional.</li>
</ul>
<li>Additional information
<ul>
<li><tt>LAUNCH:</tt> Time when the circuit was launched; optional.</li>
<li><tt>USED_AT:</tt> Time when this circuit was used; optional.</li>
<li><tt>PATH:</tt> List of relays in the circuit, separated by commas;
optional.</li>
<li><tt>BUILDTIMES:</tt> List of times when circuit hops were built,
separated by commas; optional.</li>
<li><tt>TIMEOUT:</tt> Circuit build timeout that the Tor client used when
building this circuit; optional.</li>
<li><tt>QUANTILE:</tt> Circuit build time quantile that the Tor client
uses to determine its circuit-build timeout; optional.</li>
<li><tt>CIRC_ID:</tt> Circuit identifier of the circuit used for this
measurement; optional.</li>
<li><tt>USED_BY:</tt> Stream identifier of the stream used for this
measurement; optional.</li>
<li>Other fields containing additional information; optional.</li>
</ul>
</ul>

<blockquote>
<p>
<i>Torperf <tt>.tpf</tt> output lines for a single request to download a
50 KiB file (reformatted):</i>
</p>

<p>
<tt>BUILDTIMES=1.16901898384,1.86555600166,2.13295292854</tt><br>
<tt>CIRC_ID=9878</tt><br>
<tt>CONNECT=1338357901.42</tt><br>
<tt>DATACOMPLETE=1338357902.91</tt><br>
<tt>DATAPERC10=1338357902.48</tt><br>
<tt>DATAPERC20=1338357902.48</tt><br>
<tt>DATAPERC30=1338357902.61</tt><br>
<tt>DATAPERC40=1338357902.64</tt><br>
<tt>DATAPERC50=1338357902.65</tt><br>
<tt>DATAPERC60=1338357902.74</tt><br>
<tt>DATAPERC70=1338357902.74</tt><br>
<tt>DATAPERC80=1338357902.75</tt><br>
<tt>DATAPERC90=1338357902.79</tt><br>
<tt>DATAREQUEST=1338357901.83</tt><br>
<tt>DATARESPONSE=1338357902.25</tt><br>
<tt>DIDTIMEOUT=0</tt><br>
<tt>FILESIZE=51200</tt><br>
<tt>LAUNCH=1338357661.74</tt><br>
<tt>NEGOTIATE=1338357901.42</tt><br>
<tt>PATH=$980D326017CEF4CBBF4089FBABE767DC83D059AF,$03545609092A24C71CCAD2F4523F5CCC6714F159,$CAC3CF7154AE9C656C4096DC38B4EFA145905654</tt><br>
<tt>QUANTILE=0.800000</tt><br>
<tt>READBYTES=51442</tt><br>
<tt>REQUEST=1338357901.42</tt><br>
<tt>RESPONSE=1338357901.83</tt><br>
<tt>SOCKET=1338357901.42</tt><br>
<tt>SOURCE=torperf</tt><br>
<tt>START=1338357901.42</tt><br>
<tt>TIMEOUT=5049</tt><br>
<tt>USED_AT=1338357902.91</tt><br>
<tt>USED_BY=18869</tt><br>
<tt>WRITEBYTES=75</tt><br>
</p>
</blockquote>
<br>

<p>
Torperf can produce two output files: <tt>.data</tt> and
<tt>.extradata</tt>.
The <tt>.data</tt> file contains timestamps for request substeps and the
byte summaries for downloading a test file via Tor.
The document below shows an example output of a Torperf run.
The timestamps are seconds and microseconds since 1970-01-01
00:00:00.000000.
Torperf can be configured to write <tt>.extradata</tt> files by attaching
a Tor controller and writing certain controller events to disk.
The format of a <tt>.extradata</tt> line is similar to the combined format
as specified above, except that it can only contain "Additional
information" keywords.
</p>

<blockquote>
<p>
<i>Torperf <tt>.data</tt> and <tt>.extradata</tt> output lines for a
single request to download a 50 KiB file (reformatted and annotated with
comments):</i>
</p>

<p>
<tt># Timestamps and byte summaries contained in .data files:</tt><br>
<tt>1338357901 422336   # Connection process started</tt><br>
<tt>1338357901 422346   # After socket is created</tt><br>
<tt>1338357901 422521   # After socket is connected</tt><br>
<tt>1338357901 422604   # After authentication methods are negotiated (SOCKS 5 only)</tt><br>
<tt>1338357901 423550   # After SOCKS request is sent</tt><br>
<tt>1338357901 839639   # After SOCKS response is received</tt><br>
<tt>1338357901 839849   # After HTTP request is written</tt><br>
<tt>1338357902 258157   # After first response is received</tt><br>
<tt>1338357902 914263   # After payload is complete</tt><br>
<tt>75                  # Written bytes</tt><br>
<tt>51442               # Read bytes</tt><br>
<tt>0                   # Timeout (optional field)</tt><br>
<tt>1338357902 481591   # After 10% of expected bytes are read (optional field)</tt><br>
<tt>1338357902 482719   # After 20% of expected bytes are read (optional field)</tt><br>
<tt>1338357902 613169   # After 30% of expected bytes are read (optional field)</tt><br>
<tt>1338357902 647108   # After 40% of expected bytes are read (optional field)</tt><br>
<tt>1338357902 651764   # After 50% of expected bytes are read (optional field)</tt><br>
<tt>1338357902 743705   # After 60% of expected bytes are read (optional field)</tt><br>
<tt>1338357902 743876   # After 70% of expected bytes are read (optional field)</tt><br>
<tt>1338357902 757475   # After 80% of expected bytes are read (optional field)</tt><br>
<tt>1338357902 795100   # After 90% of expected bytes are read (optional field)</tt><br>
</p>

<p>
<tt># Path information contained in .extradata files:</tt><br>
<tt>CIRC_ID=9878</tt><br>
<tt>LAUNCH=1338357661.74</tt><br>
<tt>PATH=$980D326017CEF4CBBF4089FBABE767DC83D059AF,$03545609092A24C71CCAD2F4523F5CCC6714F159,$CAC3CF7154AE9C656C4096DC38B4EFA145905654</tt><br>
<tt>BUILDTIMES=1.16901898384,1.86555600166,2.13295292854</tt><br>
<tt>USED_AT=1338357902.91</tt><br>
<tt>USED_BY=18869</tt><br>
<tt>TIMEOUT=5049</tt><br>
<tt>QUANTILE=0.800000</tt><br>
</p>
</blockquote>
<hr>
<br>

<a name="bridgepool"></a>
<h3><a href="#bridgepool" class="anchor">BridgeDB pool assignment
files</a></h3>
<br>
<p>
BridgeDB is the software that receives bridge network statuses containing
the information which bridges are running from the bridge authority,
assigns these bridges to persistent distribution rings, and hands them out
to bridge users.
BridgeDB periodically dumps the list of running bridges with information
about the rings, subrings, and file buckets to which they are assigned to
a local file.
The sanitized versions of these lists containing SHA-1 hashes of bridge
fingerprints instead of the original fingerprints are available for
statistical analysis.
</p>

<blockquote>
<p>
<i>BridgeDB pool assignment file from March 13, 2011:</i>
</p>
<p>
<tt>bridge-pool-assignment 2011-03-13 14:38:03</tt><br>
<tt>00b834117566035736fc6bd4ece950eace8e057a unallocated</tt><br>
<tt>00e923e7a8d87d28954fee7503e480f3a03ce4ee email port=443 flag=stable</tt><br>
<tt>0103bb5b00ad3102b2dbafe9ce709a0a7c1060e4 https ring=2 port=443 flag=stable</tt><br>
<tt>[...]</tt><br>
</p>
</blockquote>

<p>
The document above shows a BridgeDB pool assignment file
from March 13, 2011.
Every such file begins with a line containing the timestamp when BridgeDB
wrote this file.
Subsequent lines always start with the SHA-1 hash of a bridge fingerprint,
followed by ring, subring, and/or file bucket information.
There are currently three distributor ring types in BridgeDB:
</p>

<ol>
<li><b>unallocated:</b> These bridges are not distributed by BridgeDB,
but are either reserved for manual distribution or are written to file
buckets for distribution via an external tool.
If a bridge in the <tt>unallocated</tt> ring is assigned to a file bucket,
this is noted by <tt>bucket=$bucketname</tt>.</li>
<li><b>email:</b> These bridges are distributed via an e-mail
autoresponder.  Bridges can be assigned to subrings by their OR port or
relay flag which is defined by <tt>port=$port</tt> and/or <tt>flag=$flag</tt>.
</li>
<li><b>https:</b> These bridges are distributed via https server.
There are multiple https rings to further distribute bridges by IP address
ranges, which is denoted by <tt>ring=$ring</tt>.
Bridges in the <tt>https</tt> ring can also be assigned to subrings by
OR port or relay flag which is defined by <tt>port=$port</tt> and/or
<tt>flag=$flag</tt>.</li>
</ol>
<hr>
<br>

<a name="exitlist">
<h3><a href="#exitlist" class="anchor">Tor Check exit lists</a></h3>
<br>
<p>
<a href="https://www.torproject.org/tordnsel/dist/">TorDNSEL</a> is an
implementation of the active testing, DNS-based exit list
for Tor exit
nodes.
Tor Check makes the list of known exits and corresponding exit IP
addresses available in a specific format.
The document below shows an entry of the exit list written on
December 28, 2010 at 15:21:44 UTC.
This entry means that the relay with fingerprint <tt>63BA..</tt> which
published a descriptor at 07:35:55 and was contained in a version 2
network status from 08:10:11 uses two different IP addresses for exiting.
The first address <tt>91.102.152.236</tt> was found in a test performed at
07:10:30.
When looking at the corresponding server descriptor, one finds that this
is also the IP address on which the relay accepts connections from inside
the Tor network.
A second test performed at 10:35:30 reveals that the relay also uses IP
address <tt>91.102.152.227</tt> for exiting.
</p>

<blockquote>
<p>
<i>Exit list entry written on December 28, 2010 at 15:21:44 UTC:</i>
</p>
<p>
<tt>ExitNode 63BA28370F543D175173E414D5450590D73E22DC</tt><br>
<tt>Published 2010-12-28 07:35:55</tt><br>
<tt>LastStatus 2010-12-28 08:10:11</tt><br>
<tt>ExitAddress 91.102.152.236 2010-12-28 07:10:30</tt><br>
<tt>ExitAddress 91.102.152.227 2010-12-28 10:35:30</tt><br>
</p>
</blockquote>

</div>
</div>
<div class="bottom" id="bottom">
<%@ include file="footer.jsp"%>
</div>
</body>
</html>

