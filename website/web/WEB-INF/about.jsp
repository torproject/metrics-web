<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
  <title>Tor Metrics &mdash; About</title>
  <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">
  <link href="../css/stylesheet-ltr.css" type="text/css" rel="stylesheet">
  <link href="/images/favicon.ico" type="image/x-icon" rel="shortcut icon">
</head>
<body>
  <div class="center">
    <%@ include file="banner.jsp"%>
    <div class="main-column">
<h2><a href="/"><img src="/images/metrics-wordmark-small.png" width="138" height="18" alt="Metrics wordmark"></a> &mdash; About</h2>
<br>

<h3>Frequently used terms</h3>
<br>

<a name="advertised-bandwidth"></a>
<p><b><a href="#advertised-bandwidth">advertised bandwidth:</a></b> the
volume of traffic, both incoming and outgoing, that a
<a href="#relay">relay</a> is willing to sustain, as configured by the
operator and claimed to be observed from recent data transfers.</p>

<a name="bandwidth-history"></a>
<p><b><a href="#bandwidth-history">bandwidth history:</a></b> the volume
of incoming and/or outgoing traffic that a <a href="#relay">relay</a>
claims to have handled on behalf of <a href="#client">clients</a>.</p>

<a name="bridge"></a>
<p><b><a href="#bridge">bridge:</a></b> a <a href="#relay">relay</a> whose
existence is non-public and which can therefore provide access for blocked
<a href="#client">clients</a>, often in combination with
<a href="#pluggable-transport">pluggable transports</a>, which registers
itself with the <a href="#bridge-authority">bridge authority</a>.</p>

<a name="bridge-authority"></a>
<p><b><a href="#bridge-authority">bridge authority:</a></b> a
special-purpose <a href="#relay">relay</a> that maintains a list of
bridges as input for external bridge distribution mechanisms (for example,
<a href="https://bridges.torproject.org/">BridgeDB</a>).</p>

<a name="circuit"></a>
<p><b><a href="#circuit">circuit:</a></b> a path through the Tor network
built by <a href="#client">clients</a> consisting of at most one
<a href="#bridge">bridge</a> and at least one
<a href="#relay">relay</a>.</p>

<a name="client"></a>
<p><b><a href="#client">client:</a></b> a node in the Tor network,
typically running on behalf of one user, that routes application
connections over a series of <a href="#relay">relays</a>.</p>

<a name="consensus"></a>
<p><b><a href="#consensus">consensus:</a></b> a single document compiled
and voted on by the <a href="#directory-authority">directory
authorities</a> once per hour, ensuring that all
<a href="#client">clients</a> have the same information about the
<a href="#relay">relays</a> that make up the Tor network.</p>

<a name="consensus-weight"></a>
<p><b><a href="#consensus-weight">consensus weight:</a></b> a value
assigned to a <a href="#relay">relay</a> that is based on bandwidth
observed by the relay and bandwidth measured by the
<a href="#directory-authority">directory authorities</a>, included in the
hourly published <a href="#consensus">consensus</a>, and used by
<a href="#client">clients</a> to select relays for their
<a href="#circuit">circuits</a>.</p>

<a name="directory-authority"></a>
<p><b><a href="#directory-authority">directory authority:</a></b> a
special-purpose <a href="#relay">relay</a> that maintains a list of
currently-running relays and periodically publishes a
<a href="#consensus">consensus</a> together with the other directory
authorities.</p>

<a name="directory-mirror"></a>
<p><b><a href="#directory-mirror">directory mirror:</a></b> a
<a href="#relay">relay</a> that provides a recent copy of directory
information to clients, in order to reduce the load on
<a href="#directory-authority">directory authorities</a>.</p>

<a name="hidden-service"></a>
<p><b><a href="#hidden-service">hidden service:</a></b> a location-hidden
service (for example, a website or instant-messaging server) that is only
accessible via the Tor network.</p>

<a name="pluggable-transport"></a>
<p><b><a href="#pluggable-transport">pluggable transport:</a></b> an
alternative transport protocol provided by <a href="#bridge">bridges</a>
and used by <a href="#client">clients</a> to circumvent transport-level
blockings (for example, by ISPs or governments).</p>

<a name="relay"></a>
<p><b><a href="#relay">relay:</a></b> a publicly-listed node in the Tor
network that forwards traffic on behalf of <a href="#client">clients</a>,
and that registers itself with the
<a href="#directory-authority">directory authorities</a>.</p>

<a name="relay-flag"></a>
<p><b><a href="#relay-flag">relay flag:</a></b> a special
(dis-)qualification of <a href="#relay">relays</a> for circuit positions
(for example, "Guard", "Exit", "BadExit"), circuit properties (for
example, "Fast", "Stable"), or roles (for example, "Authority", "HSDir"),
as assigned by the <a href="#directory-authority">directory
authorities</a> and further defined in the
<a href="https://gitweb.torproject.org/torspec.git/tree/dir-spec.txt">directory
protocol specification</a>.</p>

<h3>Frequently asked questions</h3>
<br>

<div style="line-height: 18pt;">
<p>
<b>Q: How do you obtain all these facts in an anonymity network without
hurting user privacy?</b><br>
A: The metrics on this website are based on different data sources in the
Tor network.
Some of these data sources are not sensitive at all, like properties and
capabilities of a relay.
Others are more sensitive, like statistics on fetched directory listings
by country.
But others are simply too sensitive to gather at all, like contents of
unencrypted connections leaving the Tor network, so we don't have metrics
on those.<br>
We wrote a
<a href="http://freehaven.net/anonbib/#wecsr10measuring-tor">research
paper</a> where we describe how we measure potentially sensitive data in
the Tor network.
Whenever we plan to add new data, this plan needs to go through a rigorous
process of writing a
<a href="https://gitweb.torproject.org/torspec.git/tree/proposals/001-process.txt">proposal
document</a>, which is usually discussed on the
<a href="https://lists.torproject.org/cgi-bin/mailman/listinfo/tor-dev">public
development list</a>, and publicly reviewing code patches on the
<a href="https://trac.torproject.org/projects/tor">bug tracker</a>.
Furthermore, as a core principle, we only use data for metrics that have
been made publicly available:
if the raw data are too sensitive to publish, then we shouldn't even
publish aggregate statistics of it.
See the <a href="https://collector.torproject.org/">CollecTor service</a>
that we use as single data source for all graphs and tables on this
website.
</p>
<p>
<b>Q: How do you know <a href="network.html">how many servers there are in
the network</a>, how many of them permit exiting, etc.?<br></b>
A: The servers in the Tor network, called relays and bridges, send a
document with properties and capabilities to a set of central directory
servers.
These directory servers perform some reachability tests and publish a list
of running servers.
All we have to do is throw these documents into a database and run
aggregation functions on it.
</p>
<p>
<b>Q: How do you know <a href="bandwidth.html">how much bandwidth is
advertised and consumed</a> in the network?</b>
<br>
A: Relays and bridges report bandwidth numbers to the central directory
servers, both how much bandwidth they advertise and how much is used up by
clients.
</p>
<p>
<b>Q: How do you measure <a href="bubbles.html">diversity of relays</a> in
the network?</b>
<br>
A: We resolve relay IP addresses to country codes and autonomous system
numbers using <a href="https://www.maxmind.com/en/opensource">MaxMind's
open source databases</a>.
That gives us a rough idea whether there are certain countries or Internet
providers running larger parts of the Tor network than others.
</p>
<p>
<b>Q: How do you know <a href="users.html">how many users</a> there are in
the network?</b>
<br>
A: We don't actually count users but directory traffic induced by Tor
clients.
Clients periodically need to update their view on the network, and by
counting those requests we can make some rough estimates how many users
there are.
If you want to learn more, there's a more detailed document available
dubbed
<a href="https://gitweb.torproject.org/metrics-web.git/tree/doc/users-q-and-a.txt">Questions
and answers about user statistics</a>.
We also wrote a technical report titled
<a href="https://research.torproject.org/techreports/counting-daily-bridge-users-2012-10-24.pdf">Counting
daily bridge users</a> which is very related.
</p>
<p>
<b>Q: How do you <a href="performance.html">measure performance</a> in the
network?</b>
<br>
A: We run our own measurements using a tool called
<a href="https://gitweb.torproject.org/torperf.git">Torperf</a>.
This tool fetches files of three different sizes over the Tor network and
measures how long that takes.
</p>
<p>
<b>Q: How often are graphs updated?</b>
<br>
A: The graphs and tables on this website are updated multiple times per
day.
However, some graphs have the last few days cut off, because we don't have
enough data available yet.
It simply takes time to report, collect, and process all the data.
</p>
<p>
<b>Q: Are the raw numbers behind graphs available for download?</b>
<br>
A: Yes, all raw numbers are available in <a href="stats/">comma-separated
value files (.csv)</a>, which are further explained on the "Data:" pages,
e.g., <a href="https://metrics.torproject.org/servers-data.html">"Data:
Number of relays and bridges"</a>.
In addition to that, the raw data behind those .csv files are available
via the <a href="https://collector.torproject.org/">CollecTor service</a>.
If you do something cool with either the .csv files or the raw data,
please drop us a note, so that we may add a link here.
</p>
<p>
<b>Q: How can I request a new graph or table?</b>
<br>
A: Please open a ticket in the
<a href="https://trac.torproject.org/projects/tor">bug tracker</a> using
component "Metrics Website".
But please understand that adding a new graph or table may be harder than
it seems.
The following categories of feature requests may help you assess how
likely it is that we implement your suggestion and how long it may take.
Of course, you can always influence both likelihood and time to get your
graph or table added by helping out!
<ol>
<li>Adding a link to a related project that does something cool with Tor
network data is easiest.
These links can be pretty useful, because somebody might pick up the idea
and write a patch to include a fully customizable graph or table for this
website.
Adding a link is usually done within a day or two.</li>
<li>Improving an existing graph or table is slightly more work.
For example, you might suggest to add a new parameter or put another line
on a graph.
If the data for that is already available, this can be done within a
couple of days, assuming somebody is free to do it.</li>
<li>Adding a new graph or table based on existing, already processed data
requires writing some graphing code in R and some HTML around it.
This may take a few weeks.</li>
<li>Adding a new graph or table based on raw data that needs processing
takes quite some more work.
This may require us to do some heavy database lifting, because we need to
be sure that the processing code scales in the next few years.
So, expect this to take a couple of months.</li>
<li>Finally, adding a new metric based on newly gathered raw data, that
is, new fields in descriptors or even new descriptors types, is most
time-consuming.
And to be honest, it is least likely to happen without your help.
In addition to the significant development work, we may have to wait one
or even two Tor release cycles to get the new code running on relays
and/or bridges.
Everything under one year is optimistic for this type of enhancement
request.</li>
</ol>
</p>
<p>
<b>Q: Are there open feature requests or other issues related to this
website, and where do I start writing code for it?</b>
<br>
A: The bug tracker has a list of
<a href="https://trac.torproject.org/projects/tor/query?status=!closed&component=Metrics+Website&order=priority">open
tickets in the Metrics Website component</a>.
The sources are
<a href="https://gitweb.torproject.org/metrics-web.git/">available via
Git</a>.
</p>
</div>

    </div>
  </div>
  <div class="bottom" id="bottom">
    <%@ include file="footer.jsp"%>
  </div>
</body>
</html>
