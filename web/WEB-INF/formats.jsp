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
information (<a href="papers/hotpets09.pdf">PDF</a>) by describing the
data format of
<a href="#serverdesc">server descriptors and network statuses</a>,
and we explain the sanitzation process of (non-public)
<a href="#bridgedesc">bridge directory information</a>.</li>
<li>Second, we describe the numerous aggregate statistics that relays
publish about their usage (<a href="papers/wecsr10.pdf">PDF</a>),
including
<a href="#bytehist">byte histories</a>,
<a href="#dirreqstats">directory request statistics</a>,
<a href="#entrystats">connecting client statistics</a>,
<a href="#bridgestats">bridge user statistics</a>,
<a href="#cellstats">cell-queue statistics</a>,
<a href="#exitstats">exit-port statistics</a>, and
<a href="#bidistats">bidirectional connection use</a>.</li>
<li>Third, we delineate the output of various Tor services like
<a href="#bridgepool">BridgeDB</a>,
<a href="#gettor">GetTor</a>, or
<a href="#exitlist">Tor Check</a> as well as specific measurement tools like
<a href="#torperf">Torperf</a>.</li>
</ol>

<p>
All data described in this report are available for download on the
<a href="data.html">data</a> page.
This page is based on a technical report
(<a href="papers/data-2011-03-14.pdf">PDF</a>)
and is very likely more recent than the report.
</p>
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
(<a href="papers/hotpets09.pdf">PDF</a>) and provide interactive
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
<li><b>Replace nickname with Unnamed:</b> The bridge nicknames might
give hints on the location of the bridge if chosen without care; e.g. a
bridge nickname might be very similar to the operators' relay nicknames
which might be located on adjacent IP addresses.
All bridge nicknames are therefore replaced with the string
<tt>Unnamed</tt>.</li>
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
In a tech report (<a href="papers/countingusers-2010-11-30.pdf">PDF</a>)
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
(<a href="papers/bufferstats-2009-08-25.pdf">PDF</a>).
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
</p>

<p>
Torperf can produce two output files: <tt>.data</tt> and <tt>.extradata</tt>.
The <tt>.data</tt> file contains timestamps for nine substeps and the byte
summaries for downloading a test file via Tor.
The document below shows an example output of a Torperf run.
The timestamps in the upper part of this output are seconds and
microseconds since 1970-01-01 00:00:00.000000.
</p>

<p>
Torperf can be configured to write <tt>.extradata</tt> files by attaching
a Tor controller and writing certain controller events to disk.
The content of a <tt>.extradata</tt> line is shown in the lower part of
the document below.
The first column indicates if this circuit was actually used to fetch
the data (<tt>ok</tt>) or if Tor chose a different circuit because this
circuit was problematic (<tt>error</tt>).
For every <tt>error</tt> entry there should be a following <tt>ok</tt> entry,
unless the network of the Torperf instance is dead or the resource is
unavailable.
The circuit build completion time in the <tt>.extradata</tt> line is the
time between Torperf sent a SOCKS request and received a SOCKS response in
the <tt>.data</tt> file.
The three or more hops of the circuit are listed by relay fingerprint and
nickname.
An <tt>=</tt> sign between the two means that a relay has the <tt>Named</tt>
flag, whereas the <tt>~</tt> sign means it doesn't.
</p>

<blockquote>
<p>
<i>Torperf output lines for a single request to download a 50 KiB
file (reformatted and annotated with comments):</i>
</p>

<p>
<tt># Timestamps and byte summaries contained in .data files:</tt><br>
<tt>1293543301 762678   # Connection process started</tt><br>
<tt>1293543301 762704   # After socket is created</tt><br>
<tt>1293543301 763074   # After socket is connected</tt><br>
<tt>1293543301 763190   # After authentication methods are (SOCKS 5 only)</tt><br>
<tt>1293543301 763816   # After SOCKS request is sent</tt><br>
<tt>1293543302 901783   # After SOCKS response is received</tt><br>
<tt>1293543302 901818   # After HTTP request is written</tt><br>
<tt>1293543304 445732   # After first response is received</tt><br>
<tt>1293543305 456664   # After payload is complete</tt><br>
<tt>75                  # Written bytes</tt><br>
<tt>51442               # Read bytes</tt><br>
<tt></tt><br>
<tt># Path information contained in .extradata files:</tt><br>
<tt>ok                  # Status code</tt><br>
<tt>1293543302          # Circuit build completion time</tt><br>
<tt>$2F265B37920BDFE474BF795739978EEFA4427510=fejk4        # 1st hop</tt><br>
<tt>$66CA87E164F1CFCE8C3BB5C095217A28578B8BAF=blutmagie3   # 2nd hop</tt><br>
<tt>$76997E6557828E8E57F70FDFBD93FB3AA470C620~Amunet8      # 3rd hop</tt><br>
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

<a name="gettor"></a>
<h3><a href="#gettor" class="anchor">GetTor statistics file</a></h3>
<br>
<p>
GetTor allows users to fetch the Tor software via email.
GetTor keeps internal statistics on the number of packages requested
every day and writes these statistics to a file.
The document below shows the statistics file for December 27, 2010.
The <tt>None</tt> entry stands for requests that don't ask for a specific
bundle, e.g. requests for the bundle list.
</p>

<blockquote>
<p>
<i>GetTor statistics file for December 27, 2010:</i>
</p>
<p>
<tt>2010-12-27 - None:167 macosx-i386-bundle:0 macosx-ppc-bundle:0
source-bundle:2 tor-browser-bundle:0 tor-browser-bundle_ar:0
tor-browser-bundle_de:0 tor-browser-bundle_en:39
tor-browser-bundle_es:0 tor-browser-bundle_fa:5
tor-browser-bundle_fr:0 tor-browser-bundle_it:0
tor-browser-bundle_nl:0 tor-browser-bundle_pl:0
tor-browser-bundle_pt:0 tor-browser-bundle_ru:0
tor-browser-bundle_zh_CN:77 tor-im-browser-bundle:0
tor-im-browser-bundle_ar:0 tor-im-browser-bundle_de:0
tor-im-browser-bundle_en:1 tor-im-browser-bundle_es:0
tor-im-browser-bundle_fa:0 tor-im-browser-bundle_fr:0
tor-im-browser-bundle_it:0 tor-im-browser-bundle_nl:0
tor-im-browser-bundle_pl:0 tor-im-browser-bundle_pt:0
tor-im-browser-bundle_ru:0 tor-im-browser-bundle_zh_CN:0</tt>
</p>
</blockquote>
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

