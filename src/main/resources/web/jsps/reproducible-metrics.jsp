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
<li class="active">Reproducible Metrics</li>
</ul>
</div>

<div class="container">

<h1>Reproducible Metrics
<a href="#reproducible-metrics" name="reproducible-metrics" class="anchor">#</a></h1>

<p>The graphs and tables on Tor Metrics are the result of aggregating data obtained from several points in the Tor network.
Some of these aggregations are straightforward, but some are not.</p>

<p>We want to make the graphs and tables on this site easier to access and reproduce, so on this page, we specify how you can reproduce the data behind them to create your own.
We also provide background for some of the design decisions behind our aggregations and link to <a href="https://research.torproject.org/techreports.html">technical reports</a> and other additional information.</p>

<p>This page is a living document that reflects the latest changes to graphs and tables on Tor Metrics.
Whenever we create new aggregations or visualizations, we may write down our thoughts in technical reports; but, if we later expand or change a statistic, we don't update the original technical reports.
Instead, we update the specification here.</p>

<p>While we may refer to technical reports for additional details, we do not assume their knowledge in order to make sense of the specifications here.
Knowledge of our source code is not needed, either.</p>
</div>

<div class="container">

<h2><i class="fa fa-users fa-fw" aria-hidden="true"></i>
Users <a href="#users" name="users" class="anchor">#</a></h2>

<p>The number of Tor users is one of our most important statistics. It is vital for us to know how many people use the Tor network on a daily basis, whether they connect via <a href="/glossary.html#relay">relays</a> or <a href="/glossary.html#bridge">bridges</a>, from which countries they connect, what <a href="/glossary.html#pluggable-transport">transports</a> they use, and whether they connect via IPv4 or IPv6.</p>

<p>Due to the nature of Tor being an anonymity network, we cannot collect identifying data to learn the number of users. That is why we actually don't count users, but we count requests to the directories or bridges that <a href="/glossary.html#client">clients</a> clients make periodically to update their list of relays and estimate user numbers indirectly from there.</p>

<p>The result is an average number of concurrent users, estimated from data collected over a day.
We can't say how many distinct users there are.
That is, we can't say whether the same set of users stays connected over the whole day, or whether that set leaves after a few hours and a new set of users arrives.
However, the main interest is finding out if usage changes, for which it is not critical to estimate exact absolute user numbers.</p>

<!-- TODO Maybe add more from the Users FAQ, but only if relevant. -->
</div>

<div class="container">

<h3 id="relay-users" class="hover">Relay users
<a href="#relay-users" class="anchor">#</a>
</h3>

<p>Relay users are users that connect directly to a relay in order to connect to the Tor network&mdash;as opposed to bridge users that connect to a bridge as entry point into the Tor network.
Many steps here are similar to the steps for estimating bridge users, which are specified further down <a href="#bridge-users">below</a>.</p>

<p>The following description applies to the following graph and tables:</p>

<ul>
<li>Relay users <a href="/userstats-relay-country.html" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> graph</a></li>
<li>Top-10 countries by relay users <a href="/userstats-relay-table.html" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> table</a></li>
<li>Top-10 countries by possible censorship events <a href="/userstats-censorship-events.html" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> table</a></li>
</ul>

<h4>Step 1: Parse consensuses to learn which relays have been running</h4>

<p>Obtain consensuses from <a href="/collector.html#type-network-status-consensus-3">CollecTor</a>.
Refer to the <a href="https://gitweb.torproject.org/torspec.git/tree/dir-spec.txt">Tor directory protocol, version 3</a> for details on the descriptor format.</p>

<p>From each consensus, parse the <code>"valid-after"</code> and <code>"fresh-until"</code> times from the header section.</p>

<p>From each consensus entry, parse the base64-encoded relay fingerprint from the <code>"r"</code> line. Also parse the <a href="/glossary.html#relay-flag">relay flags</a> from the <code>"s"</code> line. If there is no <code>"Running"</code> flag, skip this entry.
(Consensuses with consensus method 4, introduced in 2008, or later do not list non-running relays, so that checking relay flags in recent consensuses is mostly done as a precaution without actual effect on the parsed data.)</p>

<h4>Step 2: Parse relay extra-info descriptors to learn relevant statistics reported by relays</h4>

<p>Also obtain relay extra-info descriptors from <a href="/collector.html#type-extra-info">CollecTor</a>.
As above, refer to the <a href="https://gitweb.torproject.org/torspec.git/tree/dir-spec.txt">Tor directory protocol, version 3</a> for details on the descriptor format.</p>

<p>Parse the relay fingerprint from the <code>"extra-info"</code> line and the descriptor publication time from the <code>"published"</code> line.</p>

<p>Parse the <code>"dirreq-write-history"</code> line containing written bytes spent on answering directory requests. If the contained statistics end time is more than 1 week older than the descriptor publication time in the <code>"published"</code> line, skip this line to avoid including statistics in the aggregation that have very likely been reported in earlier descriptors and processed before. If a statistics interval spans more than 1 UTC date, split observations to the covered UTC dates by assuming a linear distribution of observations.</p>

<p>Parse the <code>"dirreq-stats-end"</code>, <code>"dirreq-v3-resp"</code>, and <code>"dirreq-v3-reqs"</code> lines containing directory-request statistics.
If the statistics end time in the <code>"dirreq-stats-end"</code> line is more than 1 week older than the descriptor publication time in the <code>"published"</code> line, skip these directory request statistics for the same reason as given above: to avoid including statistics in the aggregation that have very likely been reported in earlier descriptors and processed before.
Also skip statistics with an interval length other than 1 day.
Parse successful requests from the <code>"ok"</code> part of the <code>"dirreq-v3-resp"</code> line, subtract <code>4</code> to undo the binning operation that has been applied by the relay, and discard the resulting number if it's zero or negative.
Parse successful requests by country from the <code>"dirreq-v3-reqs"</code> line, subtract <code>4</code> from each number to undo the binning operation that has been applied by the relay, and discard the resulting number if it's zero or negative.
Split observations to the covered UTC dates by assuming a linear distribution of observations.</p>

<h4>Step 3: Approximate directory requests by country</h4>

<p>Relays report directory request numbers in two places: as a total number (<code>"dirreq-v3-resp"</code> line) and as numbers broken down by country (<code>"dirreq-v3-reqs"</code> line).
Rather than using numbers broken down by country directly we multiply total requests with the fraction of requests from a given country.
This has two reasons: it reduces the overall effect of binning, and it makes relay and bridge user estimates more comparable.
If a relay for some reason only reports total requests and not requests by country, we attribute all requests to "??" which stands for Unknown Country.</p>

<h4>Step 4: Estimate fraction of reported directory-request statistics</h4>

<p>The next step after parsing descriptors is to estimate the fraction of reported directory-request statistics on a given day.
This fraction will be used in the next step to extrapolate observed request numbers to expected network totals.
For further background on the following calculation method, refer to the technical report titled <a href="https://research.torproject.org/techreports/counting-daily-bridge-users-2012-10-24.pdf">"Counting daily bridge users"</a> which also applies to relay users.
In the following, we're using the term server instead of relay or bridge, because the estimation method is exactly the same for relays and bridges.</p>

<p>For each day in the time period, compute five variables:</p>

<ul>
<li>Compute <var>n(N)</var> as the total server uptime in hours on a given day, that is, the sum of all server uptime hours on that day. This is the sum of all intervals between <code>"valid-after"</code> and <code>"fresh-until"</code>, multiplied by the contained running servers, for all consensuses with a valid-after time on a given day.
A more intuitive interpretation of this variable is the average number of running servers&mdash;however, that interpretation only works as long as fresh consensuses are present for all hours of a day.</li>
<li>Compute <var>n(H)</var> as the total number of hours for which servers have reported written directory bytes on a given day.</li>
<li>Compute <var>n(R\H)</var> as the number of hours for which responses have been reported but no written directory bytes. This fraction is determined by summing up all interval lengths and then subtracting the written directory bytes interval length from the directory response interval length. Negative results are discarded.</li>
<li>Compute <var>h(H)</var> as the total number of written directory bytes on a given day.</li>
<li>Compute <var>h(R^H)</var> as the number of written directory bytes for the fraction of time when a server was reporting both written directory bytes and directory responses. As above, this fraction is determined by first summing up all interval lengths and then computing the minimum of both sums divided by the sum of reported written directory bytes.</li>
</ul>

<p>From these variables, compute the estimated fraction of reported directory-request statistics using the following formula:</p>

<pre>
       h(R^H) * n(H) + h(H) * n(R\H)
frac = -----------------------------
                h(H) * n(N)
</pre>

<h4>Step 5: Compute estimated relay users per country</h4>

<p>With the estimated fraction of reported directory-request statistics from the previous step it is now possible to compute estimates for relay users.
Similar to the previous step, the same approach described here also applies to estimating bridge users by country, transport, or IP version as described further down below.</p>

<p>First compute <var>r(R)</var> as the sum of reported successful directory requests from a given country on a given day.
This approach also works with <var>r(R)</var> being the sum of requests from <em>all</em> countries or from any other subset of countries, if this is of interest.</p>

<p>Estimate the number of clients per country and day using the following formula:</p>

<pre>r(N) = floor(r(R) / frac / 10)</pre>

<p>A client that is connected 24/7 makes about 15 requests per day, but not all clients are connected 24/7, so we picked the number 10 for the average client. We simply divide directory requests by 10 and consider the result as the number of users. Another way of looking at it, is that we assume that each request represents a client that stays online for one tenth of a day, so 2 hours and 24 minutes.</p>
<p>Skip dates where <var>frac</var> is smaller than 10% and hence too low for a robust estimate. Also skip dates where <var>frac</var> is greater than 110%, which would indicate an issue in the previous step. We picked 110% as upper bound, not 100%, because there can be relays reporting statistics that temporarily didn't make it into the consensus, and we accept up to 10% of those additional statistics. However, there needs to be some upper bound to exclude obvious outliers with fractions of 120%, 150%, or even 200%.</p>

<h4>Step 6: Compute ranges of expected clients per day to detect potential censorship events</h4>

<p>As last step in reproducing relay user numbers, compute ranges of expected clients per day to detect potential censorship events.
For further details on the detection method, refer to the technical report titled <a href="https://research.torproject.org/techreports/detector-2011-09-09.pdf">"An anomaly-based censorship-detection system for Tor"</a>.
Unlike the previous two steps, this step only applies to relay users, not to bridge users.</p>

<ol>
<li>Start by finding the 50 largest countries by estimated relay users, excluding <code>"??"</code>, on the last day in the data set. (Note that, as new data gets available, the set of 50 largest countries may change, too, affecting ranges for the entire data set.)</li>
<li>For each of these largest countries and for each date in the data set, compute the ratio R_ij = C_ij / C_(i-7)j of estimated users on any given date divided by estimated users 1 week earlier. Exclude ratios for which there are no user estimates from 1 week earlier, or where that estimate is 0.</li>
<li>For the computed ratios on each date, remove outliers that fall outside four interquartile ranges of the median, and remove dates with less than 8 ratios remaining.</li>
<li>For each date, compute mean and standard variation in order to use these as parameters of a normal distribution.</li>
<li>For each date and country, compute a range of expected user numbers by using a normal distribution with parameters from the previous step and the ratio of estimated users divided by estimated users 1 week earlier as input. As previously, exclude ratios for which there are no user estimates from 1 week earlier, or where that estimate is 0. Compute the low and high ranges as:
<ul>
<li>low = max(0, NormalDistribution(mu, standard deviation, P = 0.0001) * PoissonDistribution(lambda = C_(i-7)j, P = 0.0001))</li>
<li>high = NormalDistribution(mu, standard deviation, P = 0.9999) * PoissonDistribution(lambda = C_(i-7)j, P = 0.9999)</li>
</ul>
</ol>

</div>

<div class="container">
<h3 id="bridge-users" class="hover">Bridge users
<a href="#bridge-users" class="anchor">#</a>
</h3>

<p>Bridge users are users that connect to a <a href="/glossary.html#bridge">bridge</a> as entry point into the Tor network as opposed to relay users that connect directly to a relay.
Many steps here are similar to the steps for estimating relay users, which are specified above.</p>

<ul>
<li>Bridge users by country <a href="/userstats-bridge-country.html" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> graph</a></li>
<li>Bridge users by transport <a href="/userstats-bridge-transport.html" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> graph</a></li>
<li>Bridge users by country and transport <a href="/userstats-bridge-combined.html" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> graph</a></li>
<li>Bridge users by IP version <a href="/userstats-bridge-version.html" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> graph</a></li>
<li>Top-10 countries by bridge users <a href="/userstats-bridge-table.html" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> table</a></li>
</ul>

<h4>Step 1: Parse bridge network statuses to learn which bridges have been running</h4>

<p>Obtain bridge network statuses from <a href="/collector.html#type-bridge-network-status">CollecTor</a>.
Refer to the <a href="/bridge-descriptors.html">Tor bridge descriptors page</a> for details on the descriptor format.</p>

<p>From each status, parse the <code>"published"</code> time from the header section.</p>

<p>From each status entry, parse the base64-encoded hashed bridge fingerprint from the <code>"r"</code> line. Also parse the <a href="/glossary.html#relay-flag">relay flags</a> from the <code>"s"</code> line. If there is no <code>"Running"</code> flag, skip this entry.</p>

<p>As opposed to relay consensuses, there are no <code>"valid-after"</code> or <code>"fresh-until"</code> times in the header of bridge network statuses.
To unify processing, we use the publication hour as valid-after time and one hour later as fresh-until time.
If we process multiple statuses published in the same hour, we take the union of contained running bridges as running bridges in that hour.</p>

<h4>Step 2: Parse bridge extra-info descriptors to learn relevant statistics reported by bridges</h4>

<p>Also obtain bridge extra-info descriptors from <a href="/collector.html#type-bridge-extra-info">CollecTor</a>.
As above, refer to the <a href="/bridge-descriptors.html">Tor bridge descriptors page</a> for details on the descriptor format.</p>

<p>Parse the hashed bridge fingerprint from the <code>"extra-info"</code> line and the descriptor publication time from the <code>"published"</code> line.</p>

<p>Parse the <code>"dirreq-write-history"</code> line containing written bytes spent on answering directory requests. If the contained statistics end time is more than 1 week older than the descriptor publication time in the <code>"published"</code> line, skip this line to avoid including statistics in the aggregation that have very likely been reported in earlier descriptors and processed before. If a statistics interval spans more than 1 UTC date, split observations to the covered UTC dates by assuming a linear distribution of observations.</p>

<p>Parse the <code>"dirreq-stats-end"</code>, <code>"dirreq-v3-resp"</code>, and <code>"dirreq-v3-reqs"</code> lines containing directory-request statistics.
If the statistics end time in the <code>"dirreq-stats-end"</code> line is more than 1 week older than the descriptor publication time in the <code>"published"</code> line, skip these directory request statistics for the same reason as given above: to avoid including statistics in the aggregation that have very likely been reported in earlier descriptors and processed before.
Also skip statistics with an interval length other than 1 day.
Parse successful requests from the <code>"ok"</code> part of the <code>"dirreq-v3-resp"</code> line, subtract <code>4</code> to undo the binning operation that has been applied by the bridge, and discard the resulting number if it's zero or negative.
Parse successful requests by country from the <code>"dirreq-v3-reqs"</code> line, subtract <code>4</code> from each number to undo the binning operation that has been applied by the bridge, and discard the resulting number if it's zero or negative.
Split observations to the covered UTC dates by assuming a linear distribution of observations.</p>

<p>Parse the <code>"bridge-ips"</code>, <code>"bridge-ip-versions"</code>, and <code>"bridge-ip-transports"</code> lines containing unique connecting IP addresses by country, IP version, and transport. From each number of unique IP addresses, subtract 4 to undo the binning operation that has been applied by the bridge. Discard the resulting number if it's zero or negative.</p>

<!-- Internal note: we do not compare the timestamp from the "bridge-stats-end" line with the one in the "dirreq-stats-end" line. If a bridge reports these two statistics for different periods of time, we're wrongly matching unique IP addresses with directory requests. Even worse, if a bridge reports different combinations of the two statistics, we'll use whichever combination we see first, which may not be the correct one. -->

<h4>Step 3: Approximate directory requests by country, transport, and IP version</h4>

<p>Older bridges did not report directory requests by country but only total requests and unique IP address counts by country.
In that case we approximate directory requests by country by multiplying the total number with the fraction of unique IP addresses from a given country.
For newer bridges that do report directory requests by country we still take total requests as starting point and multiply with the fraction of requests by country.
Otherwise, if we had used directory requests by country directly, totals by country, transport, and IP version would not match.
If a bridge reports neither directory requests by country nor unique IP addresses by country, we attribute all requests to "??" which stands for Unknown Country.</p>

<p>Bridges do not report directory requests by transport or IP version.
We approximate these numbers by multiplying the total number of requests with the fraction of unique IP addresses by transport or IP version.
If a bridge does not report unique IP addresses by transport or IP version, we attribute all requests to the default onion-routing protocol or to IPv4, respectively.</p>

<p>As a special case, we also approximate lower and upper bounds for directory requests by country <em>and</em> transport.
This approximation is based on the fact that most bridges only provide a small number of transports.
This allows us to combine unique IP address sets by country and by transport and obtain lower and upper bounds:</p>

<ul>
<li>We calculate the lower bound as <code>max(0, C(b) + T(b) - S(b))</code> using the following definitions: <code>C(b)</code> is the number of requests from a given country reported by bridge <code>b</code>; <code>T(b)</code> is the number of requests using a given transport reported by bridge <code>b</code>; and <code>S(b)</code> is the total numbers of requests reported by bridge <code>b</code>. Reasoning: If the sum <code>C(b) + T(b)</code> exceeds the total number of requests from all countries and transports <code>S(b)</code>, there must be requests from that country and transport. And if that is not the case, <code>0</code> is the lower limit.</li>
<li>We calculate the upper bound as <code>min(C(b), T(b))</code> with the definitions from above. Reasoning: There cannot be more requests by country and transport than there are requests by either of the two numbers.
</ul>

<h4>Step 4: Estimate fraction of reported directory-request statistics</h4>

<p>The step for estimating the fraction of reported directory-request statistics is pretty much the same for bridges and for relays.
This is why we refer to Step 4 of the <a href="#relay-users">Relay users</a> description for this estimation.</p>

<h4>Step 5: Compute estimated bridge users per country, transport, or IP version</h4>

<p>Similar to the previous step, this step is equivalent for bridge users and relay users.
We therefore refer to Step 5 of the <a href="#relay-users">Relay users</a> description for transforming directory request numbers to user numbers.</p>

</div>

<div class="container">
<h3 id="bridgedb-requests" class="hover">BridgeDB requests
<a href="#bridgedb-requests" class="anchor">#</a>
</h3>

<p>BridgeDB metrics contain aggregated information about requests to the BridgeDB service.
BridgeDB keeps track of each request per distribution method (HTTPS, moat, email), per bridge type (e.g., <code>vanilla</code> or <code>obfs4</code>) per country code or email provider (e.g., <code>"ru"</code> or <code>"gmail"</code>) per request success (<code>"success"</code> or <code>"fail"</code>).
Every 24 hours, BridgeDB writes these metrics to disk and then begins a new measurement interval.</p>

<p>The following description applies to the following graphs:</p>

<ul>
<li>BridgeDB requests by requested transport <a href="/bridgedb-transport.html" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> graph</a></li>
<li>BridgeDB requests by distributor <a href="/bridgedb-distributor.html" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> graph</a></li>
</ul>

<h4>Step 1: Parse BridgeDB metrics to obtain reported request numbers</h4>

<p>Obtain BridgeDB metrics from <a href="/collector.html#type-bridgedb-metrics">CollecTor</a>.
Refer to the <a href="https://gitweb.torproject.org/bridgedb.git/tree/doc/bridgedb-metrics-spec.txt">BridgeDB metrics specification</a> for details on the descriptor format.</p>

<h4>Step 2: Skip requests coming in over Tor exits</h4>

<p>Skip any request counts with <code>"zz"</code> as their <code>CC/EMAIL</code> metrics key part.
We use the <code>"zz"</code> pseudo country code for requests originating from Tor exit relays.
We're discarding these requests because <a href="https://bugs.torproject.org/32117">bots use the Tor network to crawl BridgeDB</a>, and including bot requests would provide a
false sense of how users interact with BridgeDB.
Note that BridgeDB maintains a separate distribution pool for requests coming from Tor exit relays.</p>

<h4>Step 3: Aggregate requests by date, distributor, and transport</h4>

<p>BridgeDB metrics contain request numbers broken down by distributor, bridge type, and a few more dimensions.
For our purposes we only care about total request numbers by date and either distributor or transport.
Our total request number includes both successful (i.e., the user ended up getting bridge lines)
and unsuccessful (e.g., the user failed to solve the CAPTCHA) requests.
We're using request sums by these three dimensions as aggregates and we are subtracting <code>bin_size/2</code>
from each count to better approximate the count before binning.
As date we're using the date of the BridgeDB metrics interval end.
If we encounter more than one BridgeDB metrics interval end on the same UTC date (which shouldn't be possible with an interval length of 24 hours), we arbitrarily keep whichever we process first.</p>

</div>

<div class="container">
<h2><i class="fa fa-server fa-fw" aria-hidden="true"></i>
Servers <a href="#servers" name="servers" class="anchor">#</a></h2>

<p>Statistics on the number of servers&mdash;<a href="/glossary.html#relay">relays</a> and <a href="/glossary.html#bridge">bridges</a>&mdash;were among the first to appear on Tor Metrics.
Most of these statistics have one thing in common: they use the number of running servers as their metric.
Possible alternatives are to use <a href="/glossary.html#consensus-weight">consensus weight</a> totals/fractions or guard/middle/exit probabilities as metrics, but we only recently started doing that.
In the following, we describe how exactly we count servers.</p>

<h3 id="running-relays" class="hover">Running relays
<a href="#running-relays" class="anchor">#</a>
</h3>

<p>We start with statistics on the number of running relays in the network, broken down by criteria like assigned <a href="/glossary.html#relay-flag">relay flag</a>, self-reported tor version and operating system, or IPv6 capabilities.</p>

<p>The following description applies to the following graphs:</p>

<ul>
<li>Relays and bridges (just the relays part; for the bridges part <a href="#running-bridges">see below</a>) <a href="/networksize.html" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> graph</a></li>
<li>Relays by relay flag <a href="/relayflags.html" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> graph</a></li>
<li>Relays by tor version <a href="/versions.html" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> graph</a></li>
<li>Relays by platform <a href="/platforms.html" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> graph</a></li>
<li>Relays by IP version <a href="/relays-ipv6.html" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> graph</a></li>
</ul>

<h4>Step 1: Parse consensuses</h4>

<p>Obtain consensuses from <a href="/collector.html#type-network-status-consensus-3">CollecTor</a>.
Refer to the <a href="https://gitweb.torproject.org/torspec.git/tree/dir-spec.txt">Tor directory protocol, version 3</a> for details on the descriptor format.</p>

<p>Parse and memorize the <code>"valid-after"</code> time from the consensus header. We use this UTC timestamp to uniquely identify the consensus while processing, and to later aggregate by the UTC date of this UTC timestamp.</p>

<p>Repeat the following steps for each consensus entry:</p>

<ul>
<li>Server descriptor digest: Parse the server descriptor digest from the <code>"r"</code> line. This is only needed for statistics based on relay server descriptor contents.</li>
<li>IPv6 reachable OR: Parse any <code>"a"</code> lines, if present, and memorize whether at least one of them contains an IPv6 address. This indicates that at least one of the relay's IPv6 OR addresses is reachable.</li>
<li>Relay flags: Parse relay flags from the <code>"s"</code> line. If there is no <code>"Running"</code> flag, skip this consensus entry. This ensures that we only consider running relays. Also parse any other relay flags from the <code>"s"</code> line that the relay had assigned.</li>
</ul>

<p>If a consensus contains zero running relays, we skip it.
This is mostly to rule out a rare edge case when only a minority of <a href="/glossary.html#directory-authority">directory authorities</a> voted on the <code>"Running"</code> flag.
In those cases, such a consensus would skew the average, even though relays were likely running.</p>

<h4>Step 2: Parse relay server descriptors</h4>

<p>Obtain relay server descriptors from <a href="/collector.html#type-server-descriptor">CollecTor</a>.
Again, refer to the <a href="https://gitweb.torproject.org/torspec.git/tree/dir-spec.txt">Tor directory protocol, version 3</a> for details on the descriptor format.</p>

<p>Parse any or all of the following parts from each server descriptor:</p>

<ul>
<li>Tor version: Parse the tor software version from the <code>"platform"</code> line and memorize the first three dotted numbers from it.
If the <code>"platform"</code> line does not begin with "Tor" followed by a space character and a dotted version number, memorize the version as "Other".
<!--(note: the pattern we're using in PostgreSQL (<code>'Tor 0._._%'</code>) is a bit fragile, where neither "0.10.1" nor "1.0.1" would be considered as valid tor software versions; which is unfortunate, but which will probably not bite us in the near future; and when it bites us, we'll notice by suddenly seeing lots of "Other" relays.).-->
If the platform line is missing, we skip this descriptor, which later leads to not counting this relay at all rather than including it in the "Other" group, which is slightly wrong.
Note that consensus entries also contain a <code>"v"</code> line with the tor software version from the referenced descriptor, which we do not use, because it was not present in very old consensuses, but which should work just as well for recent consensus.</li>
<li>Operating system: Parse the <code>"platform"</code> line and memorize whether it contains either one of the substrings "Linux", "Darwin" (macOS), "BSD", or "Windows".
If the <code>"platform"</code> line contains neither of these substrings, memorize the platform as "Other".
If the platform line is missing, we skip this descriptor, which later leads to not counting this relay at all rather than including it in the "Other" group, which is slightly wrong.</li>
<li>IPv6 announced OR: Parse any <code>"or-address"</code> lines and memorize whether at least one of them contains an IPv6 address. This indicates that the relay announced an IPv6 address.</li>
<li>IPv6 exiting: Parse the <code>"ipv6-policy"</code> line, if present, and memorize whether it's different from "reject 1-65535". This indicates whether the relay permitted exiting to IPv6 targets. If the line is not present, memorize that the relay does not permit exiting to IPv6 targets.</li>
<li>Server descriptor digest: Compute the SHA-1 digest, or determine it from the file name in case of archived descriptor tarballs.</li>
</ul>

<h4>Step 3: Compute daily averages</h4>

<p>Match consensus entries with server descriptors by SHA-1 digest.
Every consensus entry references exactly one server descriptor, and a server descriptor may be referenced from an arbitrary number of consensus entries.
If at least 0.1% of referenced server descriptors are missing, we skip the consensus. We chose this threshold as low, because missing server descriptors may easily skew the results. However, a small number of missing server descriptors per consensus is acceptable and also unavoidable.</p>

<p>Go through all previously processed consensuses by valid-after UTC date.
Compute the arithmetic mean of running relays, possibly broken down by relay flag, tor version, platform, or IPv6 capabilities, as the sum of all running relays divided by the number of consensuses.
Round down to the next integer number.</p>

<p>Skip the last day of the results if it matches the current UTC date, because those averages may still change throughout the day.
Further skip days for which fewer than 12 consensuses are known. The goal is to avoid over-representing a few consensuses during periods when the directory authorities had trouble producing a consensus for at least half of the day.</p>

<h3 id="running-bridges" class="hover">Running bridges
<a href="#running-bridges" class="anchor">#</a>
</h3>

<p>After explaining our running <a href="/glossary.html#relay">relays</a> statistics we continue with our running <a href="/glossary.html#bridge">bridges</a> statistics.
The steps are quite similar, except for a couple differences in data formats that justify explaining these statistics in a separate subsection.</p>

<p>The following description applies to the following graphs:</p>

<ul>
<li>Relays and bridges (just the bridges part; for the relays part <a href="#running-relays">see above</a>) <a href="/networksize.html" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> graph</a></li>
<li>Bridges by IP version <a href="/bridges-ipv6.html" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> graph</a></li>
</ul>

<h4>Step 1: Parse bridge network statuses</h4>

<p>Obtain bridge network statuses from <a href="/collector.html#type-bridge-network-status">CollecTor</a>.
Refer to the <a href="/bridge-descriptors.html">Tor bridge descriptors page</a> for details on the descriptor format.</p>

<p>Parse the <a href="/glossary.html#bridge-authority">bridge authority</a> identify from the file name and memorize it.
This is only relevant for times when more than 1 bridge authority was running.
In those cases, bridges typically register at a single bridge authority only, so that taking the average of running bridges over all statuses on those day would be misleading.</p>

<p>Parse and memorize the <code>"published"</code> time either from the file name or from the status header.
This timestamp is used to uniquely identify the status while processing, and the UTC date of this timestamp is later used to aggregate by UTC date.</p>

<p>Repeat the following steps for each status entry:</p>

<ul>
<li>Server descriptor digest: Parse the server descriptor digest from the <code>"r"</code> line and memorize it.</li>
<li>Relay flags: Parse <a href="/glossary.html#relay-flag">relay flag</a> from the <code>"s"</code> line. If there is no <code>"Running"</code> flag, skip this entry. This ensures that we only consider running bridges.</li>
</ul>

<p>If a status contains zero running bridges, skip it. This may happen when there is a temporary issue with the bridge authority.</p>

<h4>Step 2: Parse bridge server descriptors.</h4>

<p>Obtain bridge server descriptors from <a href="/collector.html#type-bridge-server-descriptor">CollecTor</a>.
As above, refer to the <a href="/bridge-descriptors.html">Tor bridge descriptors page</a> for details on the descriptor format.</p>

<p>Parse the following parts from each server descriptor:</p>

<ul>
<li>IPv6 announced OR: Parse any <code>"or-address"</code> lines and memorize whether at least one of them contains an IPv6 address. This indicates that the bridge announced an IPv6 address.</li>
<li>Server descriptor digest: Parse the SHA-1 digest from the <code>"router-digest"</code> line, or determine it from the file name in case of archived descriptor tarballs.</li>
</ul>

<h4>Step 3: Compute daily averages</h4>

<p>Match status entries with server descriptors by SHA-1 digest.
Every status entry references exactly one server descriptor, and a server descriptor may be referenced from an arbitrary number of status entries.
If at least 0.1% of referenced server descriptors are missing, we skip the status.
We chose this threshold as low, because missing server descriptors may easily skew the results.
However, a small number of missing server descriptors per status is acceptable and also unavoidable.</p>

<p>Compute the arithmetic mean of running bridges as the sum of all running bridges divided by the number of statuses and round down to the next integer number. We are aware that this approach does not correctly reflect that bridges typically register at a single bridge authority only.</p>

<p>Skip the last day of the results if it matches the current UTC date, because those averages may still change throughout the day.
Further skip days for which fewer than 12 statuses are known.
The goal is to avoid over-representing a few statuses during periods when the bridge directory authority had trouble producing a status for at least half of the day.</p>

<h3 id="consensus-weight" class="hover">Consensus weight
<a href="#consensus-weight" class="anchor">#</a>
</h3>

<p>The following statistic uses measured bandwidth, also known as <a href="/glossary.html#consensus-weight">consensus weight</a>, as metric for relay statistics, rather than absolute relay counts.</p>

<p>The following description applies to the following graph:</p>

<ul>
<li>Total consensus weights across bandwidth authorities <a href="/totalcw.html" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> graph</a></li>
</ul>

<h4>Step 1: Parse consensuses.</h4>

<p>Obtain consensuses from <a href="/collector.html#type-network-status-consensus-3">CollecTor</a>.
Refer to the <a href="https://gitweb.torproject.org/torspec.git/tree/dir-spec.txt">Tor directory protocol, version 3</a> for details on the descriptor format.</p>

<p>Parse and memorize the <code>"valid-after"</code> time from the consensus header. We use this UTC timestamp to aggregate by the UTC date.</p>

<p>Parse the <code>"s"</code> lines of all status entries and skip entries without the <code>"Running"</code> flag. Optionally distinguish relays by assigned <code>"Guard"</code> and <code>"Exit"</code> flags.</p>

<p>Parse the (optional) <code>"w"</code> lines of all status entries and compute the total of all bandwidth values denoted by the <code>"Bandwidth="</code> keyword. If an entry does not contain such a value, skip the entry. If a consensus does not contain a single bandwidth value, skip the consensus.</code>

<h4>Step 2: Parse votes.</h4>

<p>Obtain votes from <a href="/collector.html#type-network-status-vote-3">CollecTor</a>.
Refer to the <a href="https://gitweb.torproject.org/torspec.git/tree/dir-spec.txt">Tor directory protocol, version 3</a> for details on the descriptor format.</p>

<p>Parse and memorize the <code>"valid-after"</code> time from the vote header. We use this UTC timestamp to aggregate by the UTC date.</p>

<p>Also parse the <code>"nickname"</code> and <code>"identity"</code> fields from the <code>"dir-source"</code> line. We use the identity to aggregate by authority and the nickname for display purposes.</p>

<p>Parse the <code>"s"</code> lines of all status entries and skip entries without the <code>"Running"</code> flag. Optionally distinguish relays by assigned <code>"Guard"</code> and <code>"Exit"</code> flags.</p>

<p>Parse the (optional) <code>"w"</code> lines of all status entries and compute the total of all measured bandwidth values denoted by the <code>"Measured="</code> keyword. If an entry does not contain such a value, skip the entry. If a vote does not contain a single measured bandwidth value, skip the vote.</p>

<h4>Step 3: Compute daily averages</h4>

<p>Go through all previously processed consensuses and votes by valid-after UTC date and authority.
If there are less than 12 consensuses known for a given UTC date, skip consensuses from this date.
If an authority published less than 12 votes on a given UTC date, skip this date and authority.
Also skip the last date of the results, because those averages may still change throughout the day.
For all remaining combinations of date and authority, compute the arithmetic mean of total measured bandwidth, rounded down to the next-smaller integer number.</p>

</div>

<div class="container">
<h2><i class="fa fa-road fa-fw" aria-hidden="true"></i>
Traffic <a href="#traffic" name="traffic" class="anchor">#</a></h2>

<p>Our traffic statistics have in common that their metrics are based on user-generated traffic.
This includes advertised and consumed bandwidth and connection usage statistics.</p>

<h3 id="advertised-bandwidth" class="hover">Advertised bandwidth
<a href="#advertised-bandwidth" class="anchor">#</a>
</h3>

<p><a href="/glossary.html#advertised-bandwidth">Advertised bandwidth</a> is the volume of traffic, both incoming and outgoing, that a relay is willing to sustain, as configured by the operator and claimed to be observed from recent data transfers.
Relays self-report their advertised bandwidth in their server descriptors which we evaluate together with consensuses.</p>

<p>The following description applies to the following graphs:</p>

<ul>
<li>Total relay bandwidth (just the advertised bandwidth part; for the consumed bandwidth part <a href="#consumed-bandwidth">see below</a>) <a href="/bandwidth.html" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> graph</a></li>
<li>Advertised and consumed bandwidth by relay flags (just the advertised bandwidth part; for the consumed bandwidth part <a href="#consumed-bandwidth">see below</a>) <a href="/bandwidth-flags.html" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> graph</a></li>
<li>Advertised bandwidth by IP version <a href="/advbw-ipv6.html" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> graph</a></li>
<li>Advertised bandwidth distribution <a href="/advbwdist-perc.html" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> graph</a></li>
<li>Advertised bandwidth of n-th fastest relays <a href="/advbwdist-relay.html" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> graph</a></li>
</ul>

<h4>Step 1: Parse relay server descriptors</h4>

<p>Obtain relay server descriptors from <a href="/collector.html#type-server-descriptor">CollecTor</a>.
Refer to the <a href="https://gitweb.torproject.org/torspec.git/tree/dir-spec.txt">Tor directory protocol, version 3</a> for details on the descriptor format.</p>

<p>Parse the following parts from each server descriptor:</p>

<ul>
<li>Advertised bandwidth: Parse the three values (or just two in very old descriptors) from the <code>"bandwidth"</code> line. These values stand for the average bandwidth, burst bandwidth, and observed bandwidth. The advertised bandwidth is the minimum of these values.</li>
<li>Server descriptor digest: Compute the SHA-1 digest, or determine it from the file name in case of archived descriptor tarballs.</li>
</ul>

<!-- Note: In the bandwidth and bandwidth-flags graph we calculate advertised bandwidth as minimum of first and third value and ignore the second. However, tor ensures in config.c that RelayBandwidthBurst is at least equal to RelayBandwidthRate. Hence, the result is the same. -->

<h4>Step 2: Parse consensuses</h4>

<p>Obtain consensuses from <a href="/collector.html#type-network-status-consensus-3">CollecTor</a>.
Refer to the <a href="https://gitweb.torproject.org/torspec.git/tree/dir-spec.txt">Tor directory protocol, version 3</a> for details on the descriptor format.</p>

<p>From each consensus, parse the <code>"valid-after"</code> time from the header section.</p>

<p>From each consensus entry, parse the base64-encoded server descriptor digest from the <code>"r"</code> line.
We are going to use this digest to match the entry with the advertised bandwidth value from server descriptors later on.</p>

<p>Also parse the <a href="/glossary.html#relay-flag">relay flags</a> from the <code>"s"</code> line. If there is no <code>"Running"</code> flag, skip this entry.
(Consensuses with consensus method 4, introduced in 2008, or later do not list non-running relays, so that checking relay flags in recent consensuses is mostly done as a precaution without actual effect on the parsed data.)
Further parse the <code>"Guard"</code>, <code>"Exit"</code>, and <code>"BadExit"</code> relay flags from this line.
We consider a relay with the <code>"Guard"</code> flag as guard and a relay with the <code>"Exit"</code> and without the <code>"BadExit"</code> flag as exit.</p>

<h4>Step 3: Compute daily averages</h4>

<p>The first three graphs described here, namely <a href="/bandwidth.html">Total relay bandwidth</a>, <a href="/bandwidth-flags.html">Advertised and consumed bandwidth by relay flags</a> and <a href="/advbw-ipv6.html">Advertised bandwidth by IP version</a>, have in common that they show daily averages of advertised bandwidth.</p>

<p>In order to compute these averages, first match consensus entries with server descriptors by SHA-1 digest.
Every consensus entry references exactly one server descriptor, and a server descriptor may be referenced from an arbitrary number of consensus entries.
If at least 0.1% of referenced server descriptors are missing, we skip the consensus. We chose this threshold as low, because missing server descriptors may easily skew the results. However, a small number of missing server descriptors per consensus is acceptable and also unavoidable.</p>

<p>Go through all previously processed consensuses by valid-after UTC date.
Compute the arithmetic mean of advertised bandwidth as the sum of all advertised bandwidth values divided by the number of consensuses.
Round down to the next integer number.</p>

<p>Break down numbers by guards and/or exits by taking into account which <a href="/glossary.html#relay-flag">relay flags</a> a consensus entry had that referenced a server descriptor.</p>

<p>Skip the last day of the results if it matches the current UTC date, because those averages may still change throughout the day.
Further skip days for which fewer than 12 consensuses are known.
The goal is to avoid over-representing a few consensuses during periods when the directory authorities had trouble producing a consensus for at least half of the day.</p>

<h4>Step 4: Compute ranks and percentiles</h4>

<p>The remaining two graphs described here, namely <a href="/advbwdist-perc.html">Advertised bandwidth distribution</a> and <a href="/advbwdist-relay.html">Advertised bandwidth of n-th fastest relays</a>, display advertised bandwidth ranks or percentiles.</p>

<p>Similar to the previous step, match consensus entries with server descriptors by SHA-1 digest.
We handle missing server descriptors by simply skipping the consensus entry, at the risk of over-representing available server descriptors in consensuses where most server descriptors are missing.</p>

<p>For the <a href="/advbwdist-perc.html">Advertised bandwidth distribution</a> graph, determine the i-th percentile value for each consensus.
We use a non-standard percentile definition that is loosely based on the nearest-rank method:
the P-th percentile (<code>0 &le; P &le; 100</code>) of a list of <code>N</code> ordered values (sorted from least to greatest) is the <em>largest</em> value in the list such that no more than <code>P</code> percent of the data is strictly less than the value and at least <code>P</code> percent of the data is less than or equal to that value.
We calculate the ordinal rank <code>n</code> using the following formula: <code>floor((P / 100) * (N - 1)) + 1</code></p>

<p>Calculate the median value over all consensus from a given day for each percentile value.</p>

<p>Consider the set of all running relays as well as the set of exit relays.</p>

<p>For the <a href="/advbwdist-relay.html">Advertised bandwidth of n-th fastest relays</a> graph, determine the n-th highest advertised bandwidth value for each consensus, and then calculate the median value over all consensus from a given day.
Again consider the set of all running relays as well as the set of exit relays.</p>

<h3 id="consumed-bandwidth" class="hover">Consumed bandwidth
<a href="#consumed-bandwidth" class="anchor">#</a>
</h3>

<p>Consumed bandwidth, or <a href="/glossary.html#bandwidth-history">bandwidth history</a>, is the volume of incoming and/or outgoing traffic that a relay claims to have handled on behalf of clients.
Relays self-report bandwidth histories as part of their extra-info descriptors, which we evaluate in combination with consensuses.</p>

<p>The following description applies to the following graphs:</p>

<ul>
<li>Total relay bandwidth (just the consumed bandwidth part; for the advertised bandwidth part <a href="#advertised-bandwidth">see above</a>) <a href="/bandwidth.html" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> graph</a></li>
<li>Advertised and consumed bandwidth by relay flags (just the consumed bandwidth part; for the advertised bandwidth part <a href="#advertised-bandwidth">see above</a>) <a href="/bandwidth-flags.html" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> graph</a></li>
<li>Bandwidth spent on answering directory requests <a href="/dirbytes.html" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> graph</a></li>
</ul>

<h4>Step 1: Parse extra-info descriptors</h4>

<p>Obtain extra-info descriptors from <a href="/collector.html#type-extra-info">CollecTor</a>.
Refer to the <a href="https://gitweb.torproject.org/torspec.git/tree/dir-spec.txt">Tor directory protocol, version 3</a> for details on the descriptor format.</p>

<p>Parse the fingerprint from the <code>"extra-info"</code> line.
We will use this fingerprint to deduplicate statistics included in other extra-info descriptor published by the same relay.
We may also use this fingerprint to attribute statistics to relays with the <code>"Exit"</code> and/or <code>"Guard"</code> flag.</p>

<p>Parse the <code>"write-history"</code>, <code>"read-history"</code>, <code>"dirreq-write-history"</code>, and <code>"dirreq-read-history</code> lines containing consumed bandwidth statistics.
The first two histories include all bytes written or read by the relay, whereas the last two include only bytes spent on answering directory requests.
If a statistics interval spans more than 1 UTC date, split observations to the covered UTC dates by assuming a linear distribution of observations.
As a simplification, we shift reported statistics intervals forward to fully align with multiples of 15 minutes since midnight.
We also discard reported statistics with intervals that are not multiples of 15 minutes.</p>

<h4>Step 2: Parse consensuses</h4>

<p>Obtain consensuses from <a href="/collector.html#type-network-status-consensus-3">CollecTor</a>.
Refer to the <a href="https://gitweb.torproject.org/torspec.git/tree/dir-spec.txt">Tor directory protocol, version 3</a> for details on the descriptor format.</p>

<p>From each consensus, parse the <code>"valid-after"</code> time from the header section.</p>

<p>From each consensus entry, parse the base64-encoded relay fingerprint from the <code>"r"</code> line.
We are going to use this fingerprint to match the entry with statistics from extra-info descriptors later on.</p>

<p>Also parse the <a href="/glossary.html#relay-flag">relay flag</a> from the <code>"s"</code> line. If there is no <code>"Running"</code> flag, skip this entry.
(Consensuses with consensus method 4, introduced in 2008, or later do not list non-running relays, so that checking relay flags in recent consensuses is mostly done as a precaution without actual effect on the parsed data.)
Further parse the <code>"Guard"</code>, <code>"Exit"</code>, and <code>"BadExit"</code> relay flags from this line.
We consider a relay with the <code>"Guard"</code> flag as guard and a relay with the <code>"Exit"</code> and without the <code>"BadExit"</code> flag as exit.</p>

<h4>Step 3: Compute daily totals</h4>

<p>The first two graphs described here, namely <a href="/bandwidth.html">Total relay bandwidth</a> and <a href="/bandwidth-flags.html">Advertised and consumed bandwidth by relay flag</a> show daily totals of all bytes written or read by relays.
For both graphs we sum up all read and written bytes on a given day and divide the result by 2.
However, we only include bandwidth histories for a given day if a relay was listed as running in a consensus at least once on that day.
We attribute bandwidth to guards and/or exits if a relay was a guard and/or exit at least in one consensus on a day.</p>

<p>The third graph, <a href="/dirbytes.html">Bandwidth spent on answering directory requests</a>, shows bytes spent by <a href="/glossary.html#directory-authority">directory authorities</a> and <a href="/glossary.html#directory-mirror">directory mirrors</a> on answering directory requests.
As opposed to the first two graphs, all bandwidth histories are included, regardless of whether a relay was listed as running in a consensus.
Also, we compute total read directory and total written directory bytes for this fourth graph, not an average of the two.</p>

<h3 id="connbidirect" class="hover">Connection usage
<a href="#connbidirect" class="anchor">#</a>
</h3>

<p>The last category of traffic statistics concerns statistics on the fraction of connections used uni- or bidirectionally.
A subset of relays reports these highly aggregated statistics in their extra-info descriptors.</p>

<p>The following description applies to the following graph:</p>

<ul>
<li>Fraction of connections used uni-/bidirectionally <a href="/connbidirect.html" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> graph</a></li>
</ul>

<h4>Step 1: Parse relay extra-info descriptors</h4>

<p>Obtain relay extra-info descriptors from <a href="/collector.html#type-extra-info">CollecTor</a>.</p>

<p>Parse the relay fingerprint from the <code>"extra-info"</code> line.
We deduplicate reported statistics by UTC date of reported statistics and relay fingerprint.
If a given relay publishes different statistics on a given UTC day, we pick the first encountered statistics and discard all subsequent statistics by that relay on that UTC day.</p>

<p>Parse the UTC date from the <code>"conn-bi-direct"</code> line.
We use this date to aggregate statistics, regardless of what period of time fell on the statistics end date as opposed to the previous date.</p>

<p>From the same line, parse the three counts <code>READ</code>, <code>WRITE</code>, and <code>BOTH</code>, but disregard the <code>BELOW</code> value.
Discard any statistics where the sum of these three values is 0.
Compute three fractions by dividing each of the three values <code>READ</code>, <code>WRITE</code>, and <code>BOTH</code> by the sum of all three.
Multiply results with 100 and truncate any decimal places, keeping a fraction value between 0 and 100.</p>

<h4>Step 2: Aggregate statistics</h4>

<p>For each date, compute the 25th, 50th, and 75th percentile of fractions computed in the previous step.</p>

<p>We use a non-standard percentile definition that is similar to the nearest-rank method:
the P-th percentile (<code>0 &lt; P &le; 100</code>) of a list of <code>N</code> ordered values (sorted from least to greatest) is the <em>largest</em> value in the list such that no more than <code>P</code> percent of the data is strictly less than the value and at least <code>P</code> percent of the data is less than or equal to that value.
We calculate the ordinal rank <code>n</code> using the following formula: <code>floor((P / 100) * N) + 1</code></p>

</div>

<div class="container">
<h2><i class="fa fa-dashboard fa-fw" aria-hidden="true"></i>
Performance <a href="#performance" name="performance" class="anchor">#</a></h2>

<p>We perform active measurements of Tor network performance by running several <a href="https://github.com/robgjansen/onionperf">OnionPerf</a> (previously: <a href="https://gitweb.torproject.org/torperf.git">Torperf</a>) instances from different vantage points.
Here we explain how we evaluate Torperf/OnionPerf measurement to obtain the same results as on Tor Metrics.</p>

<!-- commented out subsection header and paragraph as long as we only have 1 subsection, which may change in the future (also reconsider naming it torperf):
<h3 id="torperf" class="hover">Performance of static file downloads over Tor
<a href="#torperf" class="anchor">#</a>
</h3>

<p>The most basic questions we're trying to answer from Torperf/OnionPerf measurements are: what fraction of requests succeeded, timed out, or failed; and how long did it take for the successful requests to complete?</p>
-->

<p>The following description applies to the following graphs:</p>

<ul>
<li>Time to download files over Tor <a href="/torperf.html" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> graph</a></li>
<li>Timeouts and failures of downloading files over Tor <a href="/torperf-failures.html" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> graph</a></li>
<li>Circuit build times <a href="/onionperf-buildtimes.html" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> graph</a></li>
<li>Circuit round-trip latencies <a href="/onionperf-latencies.html" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> graph</a></li>
<li>Throughput <a href="/onionperf-throughput.html" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> graph</a></li>
</ul>

<h4>Step 1: Parse OnionPerf and/or Torperf measurement results</h4>

<p>Obtain OnionPerf/Torperf measurement results from <a href="/collector.html#type-torperf">CollecTor</a>.</p>

<p>From each measurement result, parse the following keys:</p>

<ul>
<li><code>SOURCE</code>: Configured name of the data source.</li>
<li><code>FILESIZE</code>: Configured file size in bytes.</li>
<li><code>START</code>: Download start time that we use for two purposes: to determine how long a request took and to aggregate measurements by date.</li>
<li><code>DATAREQUEST</code>: Time when the HTTP request was sent.</li>
<li><code>DATARESPONSE</code>: Time when the HTTP response header was received.</li>
<li><code>DATACOMPLETE</code>: Download end time that is only set if the request succeeded.</li>
<li><code>READBYTES</code>: Total number of bytes read, which indicates whether this request succeeded (if &ge; <code>FILESIZE</code>) or failed.</li>
<li><code>DIDTIMEOUT</code>: 1 if the request timed out, 0 otherwise.</li>
<li><code>PARTIAL51200</code> and <code>PARTIAL1048576</code>: Time when 51200 or 1048576 bytes were read.</li>
<li><code>DATAPERCx</code>: Time when x% of expected bytes were read for x = { 10, 20, 50, 100 }.</li>
<li><code>BUILDTIMES</code>: Comma-separated list of times when circuit hops were built, which includes all circuits used for making measurement requests, successful or not.</li>
<li><code>ENDPOINTREMOTE</code>: Hostname, IP address, and port that was used to connect to the remote server; we use this to distinguish a request to a public server (if <code>ENDPOINTREMOTE</code> is not present or does not contain <code>".onion"</code> as substring) or to an onion server.</li>
</ul>

<h4>Step 2: Aggregate measurement results</h4>

<p>Each of the measurement results parsed in the previous step constitutes a single measurement.
We're first interested in statistics on download times for the <a href="/torperf.html">Time to download files over Tor</a> graph.
Therefore we consider complete downloads as well as partial downloads.
For complete downloads we calculate the download time as <code>DATACOMPLETE - START</code> for measurements with <code>DATACOMPLETE &gt; START</code>.
For partial downloads of larger file sizes we calculate the download time as <code>PARTIAL51200 - START</code> for measurements with <code>PARTIAL51200 &gt; START</code> and <code>FILESIZE &gt; 51200</code>; and <code>PARTIAL1048576 - START</code> for measurements with <code>PARTIAL1048576 &gt; START</code> and <code>FILESIZE &gt; 1048576</code>.
We then compute the 25th, 50th, and 75th percentile of download times by sorting download times, determining the percentile rank, and using linear interpolation between adjacent ranks.</p>

<p>Next we're interested in the average throughput of measurements for the <a href="/onionperf-throughput.html">Throughput</a> graph.
We calculate throughput from the time between receiving 0.5 and 1 MiB of a response, which obviously excludes any measurements with responses smaller than 1 MiB.
From <code>DATAPERC50</code> and <code>DATAPERC100</code> (if <code>FILESIZE = 1048576</code>) or <code>DATAPERC10</code> and <code>DATAPERC20</code> (if <code>FILESIZE = 5242880</code>) we can compute the number of milliseconds that have elapsed between receiving bytes 524,288 and 1,048,576, which is a total of 524,288 bytes or 4,194,304 bits.
We divide the value 4,194,304 by this time difference to obtain throughput in bits per millisecond which happens to be the same value as the number of kilobits per second.</p>

<p>We're also interested in circuit round-trip latencies for the <a href="/onionperf-latencies.html">Circuit round-trip latencies</a> graph.
We measure circuit latency as the time between sending the HTTP request and receiving the HTTP response header.
We calculate latencies as <code>DATARESPONSE - DATAREQUEST</code> for measurements with non-zero values for both timestamps.
We then compute 25th, 50th, and 75th percentiles in the same way as for download times above.
We also compute the lowest latency within 1.5 IQR of the lower quartile and the highest latency within 1.5 IQR of the upper quartile.</p>

<p>Ideally, all measurements would succeed.
But it's also possible that some measurements did not complete within a pre-defined timeout or failed for some other reason.
We distinguish three cases for the <a href="/torperf-failures.html">Timeouts and failures of downloading files over Tor</a> graph and provide counts of each case per day:</p>

<ul>
<li>Timeouts: measurements that either timed out (with <code>DIDTIMEOUT = 1</code>) or that have an invalid measurement end time (<code>DATACOMPLETE &le; START</code>),</li>
<li>Failures: measurements that did not time out (with <code>DIDTIMEOUT = 0</code>), that had a valid measurement end time (<code>DATACOMPLETE &gt; START</code>), and that had fewer bytes read than expected (<code>READBYTES &lt; FILESIZE</code>), and</li>
<li>Requests: all measurements, including successes, timeouts, and failures.</li>
</ul>

<p>The fourth metric that we obtain from OnionPerf/Torperf measurements is <a href="/glossary.html#circuit">circuit</a> build time, which is shown in the <a href="/onionperf-buildtimes.html">Circuit build times</a> graph.
We extract circuit build times from the <code>BUILDTIMES</code> field included in measurement results.
We use the first value as build time for the first hop and deltas between subsequent values as build times for the second and third hop.
Again, we compute the 25th, 50th, and 75th percentiles of these build times in the same way as for download times and circuit round-trip latencies.</p>

</div>

<div class="container">
<h2><i class="fa fa-map-signs fa-fw" aria-hidden="true"></i>
Onion Services <a href="#onion-services" name="onion-services" class="anchor">#</a></h2>

<p>Our <a href="/glossary.html#onion-service">onion services</a> statistics are based on two statistics reported by relays that have been added in 2014 to give some first insights into onion-service usage.
For further background on the following steps, refer to the technical report titled <a href="https://research.torproject.org/techreports/extrapolating-hidserv-stats-2015-01-31.pdf">"Extrapolating network totals from hidden-service statistics"</a> that this description is based on (which was written before hidden services were renamed to onion services).</p>

<p>The following description applies to the following graphs:</p>

<ul>
<li>Unique .onion addresses (version 2 only) <a href="/hidserv-dir-onions-seen.html" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> graph</a></li>
<li>Onion-service traffic (versions 2 and 3) <a href="/hidserv-rend-relayed-cells.html" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> graph</a></li>
</ul>

<h4>Step 1: Parse reported statistics from extra-info descriptors</h4>

<p>Obtain relay extra-info descriptors from <a href="/collector.html#type-extra-info">CollecTor</a>.</p>

<p>Parse the following parts from each extra-info descriptor:</p>

<ul>
<li>Relay fingerprint: The <code>"extra-info"</code> line tells us which relay reported these statistics, which we need to know to match them with the expected fraction of onion-service activity throughout the statistics interval.</li>
<li>Onion service statistics interval end: The <code>"hidserv-stats-end"</code> line tells us when the statistics interval ended, and, together with the interval length, when it started.</li>
<li>Cells relayed as rendezvous point: The <code>"hidserv-rend-relayed-cells"</code> line tells us the number of cells that the relay handled on rendezvous circuits, and it tells us how this number has been obfuscated by the relay.
The value for <code>"bin_size"</code> is the bin size used for rounding up the originally observed cell number, and the values for <code>"delta_f"</code> and <code>"epsilon"</code> are inputs for the additive noise following a Laplace distribution.</li>
<li>.onion addresses observed as directory: Finally, the <code>"hidserv-dir-onions-seen"</code> line tells us the number of .onion addresses that the relay observed in published onion-service descriptors in its role as onion-service directory.</li>
</ul>

<p>Note: Unlike other statistics, we're not splitting statistics by UTC date. Instead, we're only accepting statistics intervals that are exactly 1 day long, and we're counting all reported values for the UTC date of the statistics end time.</p>

<h4>Step 2: Remove previously added noise</h4>

<p>When processing onion-service statistics, we need to handle the fact that they have been obfuscated by relays.
As first step, we're attempting to remove the additive Laplace-distributed noise by rounding up to the nearest multiple of <code>bin_size</code>.
The idea is that it's most likely that noise was added to the closest right side of a bin than to the right side of another bin.
In step two, we're subtracting half of <code>bin_size</code>, because the relay added between 0 and <code>bin_size - 1</code> to the originally observed value.
All in all, we're using the following formula to remove previously added noise:</p>

<pre>
halfBin = binSize / 2
floor((reported + halfBin) / binSize) * binSize - halfBin
</pre>

<h4>Step 3: Parse consensuses</h4>

<p>Obtain consensuses from <a href="/collector.html#type-network-status-consensus-3">CollecTor</a>.</p>

<p>From each consensus, parse the <code>"valid-after"</code> time from the header section.</p>

<p>From each consensus entry, parse the base64-encoded relay fingerprint from the <code>"r"</code> line.</p>

<p>Also parse the <a href="/glossary.html#relay-flag">relay flags</a> from the <code>"s"</code> line. If there is no <code>"Running"</code> flag, skip this entry.
(Consensuses with consensus method 4, introduced in 2008, or later do not list non-running relays, so that checking relay flags in recent consensuses is mostly done as a precaution without actual effect on the parsed data.)
Parse the remaining relay flags from this line.</p>

<p>Finally, parse the weights contained in the <code>"bandwidth-weights"</code> line from the footer section of the consensus.</p>

<h4>Step 4: Derive network fractions from consensuses</h4>

<p>The probability of choosing a relay as rendezvous point varies a lot between relays, and not all onion-service directories handle the same number of onion-service descriptors.
Fortunately, we can derive what fraction of rendezvous circuits a relay has handled and what fraction of descriptors a directory was responsible for.</p>

<p>The first fraction that we compute is the probability of a relay to be selected as rendezvous point.
<a href="/glossary.html#client">Clients</a> only select relays as rendezvous point that have the <code>"Fast"</code> flag.
They weight relays differently based on their bandwidth and depending on whether they have the <code>"Exit"</code> and/or <code>"Guard"</code> flags:
they weight the bandwidth value contained in the <code>"w"</code> line with the value of <code>"Wmg"</code>, <code>"Wme"</code>, <code>"Wmd"</code>, or <code>"Wmm"</code>, depending on whether the relay has only the <code>"Guard"</code> flag, only the <code>"Exit"</code> flag, both such flags, or neither of them.</p>

<p>The second fraction that we can derive from this consensus entry is the fraction of descriptor space that this relay was responsible for in its role as onion-service directory.
The <a href="https://gitweb.torproject.org/torspec.git/tree/rend-spec-v2.txt">Tor Rendezvous Specification</a> contains the following definition:
<em>"A[n onion] service directory is deemed responsible for a descriptor ID if it has the HSDir flag and its identity digest is one of the first three identity digests of HSDir relays following the descriptor ID in a circular list."</em></p>

<p>Based on the fraction of descriptor space that a directory was responsible for we can compute the fraction of descriptors that this directory has seen.
Intuitively, one might think that these fractions are the same.
However, this is not the case: each descriptor that is published to a directory is also published to two other directories.
As a result we need to divide the fraction of descriptor space by <em>three</em> to obtain the fraction of descriptors observed the directory.
Note that, without dividing by three, fractions of all directories would not add up to 100%.</p>

<p>We calculate network fraction per consensus.
When we extrapolate reported statistics, we compute the average (arithmetic mean) of all such network fractions with consensus valid-after times falling into the statistics interval.
In particular, we're <em>not</em> computing the average of network fractions from the UTC day when the statistics interval ends; even though we're attributing extrapolated statistics to the UTC date of the statistics interval end in the next step.</p>

<h4>Step 5: Extrapolate network totals</h4>

<p>We are now ready to extrapolate network totals from reported statistics.
We do this by dividing reported statistics by the calculated fraction of observations made by the reporting relay.
The underlying assumption is that statistics grow linearly with calculated fractions.
We only exclude relays from this step that have a calculated fraction of exactly zero, to avoid dividing by zero.</p>

<p>While we can expect this method to work as described for extrapolating cells on rendezvous circuits, we need to take another step for estimating the number of unique .onion addresses in the network.
The reason is that a .onion address is not only known to a single relay, but to a couple of relays, all of which include that .onion address in their statistics.
We need to subtract out the multiple counting of .onion addresses to come up with a network-wide number of unique .onion addresses.</p>

<p>As an approximation, we assume that an <a href="/glossary.html#onion-service">onion service</a> publishes its descriptor to <em>twelve</em> directories over a 24-hour period:
the service stores <em>two</em> replicas per descriptor using different descriptor identifiers, both descriptor replicas get stored to <em>three</em> different onion-service directories each, and the service changes descriptor identifiers once every 24 hours which leads to <em>two</em> different descriptor identifiers per replica.</p>

<p>To be clear, this approximation is not entirely accurate.
For example, the descriptors of roughly 1/24 of services are seen by 3 rather than 2 sets of onion-service directories, when a service changes descriptor identifiers once at the beginning of a relay's statistics interval and once again towards the end.
In some cases, the two replicas or the descriptors with changed descriptor identifiers could have been stored to the same directory.
As another example, onion-service directories might have joined or left the network and other directories might have become responsible for storing a descriptor which also include that .onion address in their statistics.
However, for the subsequent analysis, we assume that neither of these cases affects results substantially.</p>

<h4>Step 6: Compute daily averages</h4>

<p>As last step in the analysis, we aggregate extrapolated network totals from all reporting relays to obtain a daily average.
We're using the weighted interquartile mean as metric, because it is robust against noisy statistics and potentially lying relays and considers half of the reported statistics.
For this metric we order extrapolated network totals by their value, discard the lower and the upper quartile by weight, and compute the weighted mean of the remaining values.
</p>

<p>We further define a threshold of 1% for the total fraction of relays reporting statistics.
If less than these 1% of relays report statistics on a given day, we don't include that day in the end results.</p>

</div>

<div class="container">
<h2><i class="fa fa-download fa-fw" aria-hidden="true"></i>
Applications <a href="#applications" name="applications" class="anchor">#</a></h2>

<p>Our applications statistics are based on Tor web server requests where our users download applications initially and where they ask for updates.</p>

<p>The following description applies to the following graphs:</p>

<ul>
<li>Tor Browser downloads and updates <a href="/webstats-tb.html" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> graph</a></li>
<li>Tor Browser downloads and updates by platform <a href="/webstats-tb-platform.html" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> graph</a></li>
<li>Tor Browser downloads and updates by locale <a href="/webstats-tb-locale.html" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> graph</a></li>
<li>Tor Browser updates by release channel <a href="/webstats-tb-channel.html" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> graph</a></li>
<li>Tor Messenger downloads and updates <a href="/webstats-tm.html" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> graph</a></li>
</ul>

<h4>Step 1: Parse Tor web server logs</h4>

<p>Obtain Tor web server logs from <a href="/collector.html#type-webstats">CollecTor</a>.
Refer to the separate <a href="/web-server-logs.html">specification page</a> for details on the data format.</p>

<p>Each log file contains relevant meta data in its file name, including the site name, the server name, and the log date.
The log file itself contains sanitized requests to Tor web servers.</p>

<p>All patterns mentioned in the following are understood by PostgreSQL's <code>LIKE</code> operator.
An underscore (<code>_</code>) matches any single character; a percent sign (<code>%</code>) matches any string of zero or more characters.</p>

<h4>Step 2: Count Tor Browser initial downloads</h4>

<p>We count a request as Tor Browser initial download if it matches the following criteria:</p>

<ul>
<li>Request method: GET</li>
<li>Resource string: <code>'%/torbrowser/%.exe'</code>, <code>'%/torbrowser/%.dmg'</code>, or <code>'%/torbrowser/%.tar.xz'</code></li>
<li>Response code: 200</li>
</ul>

<p>We distinguish platforms based on the resource string: <code>'%.exe%'</code> for Windows, <code>'%.dmg%'</code> for macOS, and <code>'%.tar.xz%'</code> for Linux.</p>

<p>We distinguish release channels based on the resource string: <code>'%-hardened%'</code> for hardened releases, <code>'%/%.%a%/%'</code> for alpha releases, and stable releases otherwise.</p>

<p>We extract the locale (for example, <code>'en-US'</code> for English as used in the United States or <code>'de'</code> for German) from the resource string using regular expression <code>'.*_([a-zA-Z]{2}|[a-zA-Z]{2}-[a-zA-Z]{2})[\._-].*'</code>, falling back to <code>'??'</code> for unrecognized locales if the regular expression does not match.</p>

<h4>Step 3: Count Tor Browser signature downloads</h4>

<p>We count a request as Tor Browser signature download if it matches the following criteria:</p>

<ul>
<li>Request method: GET</li>
<li>Resource string: <code>'%/torbrowser/%.exe.asc'</code>, <code>'%/torbrowser/%.dmg.asc'</code>, or <code>'%/torbrowser/%.tar.xz.asc'</code></li>
<li>Response code: 200</li>
</ul>

<p>We break down requests by platform, channel, and locale in the exact same way as for Tor Browser initial downloads (see above).</p>

<h4>Step 4: Count Tor Browser update pings</h4>

<p>We count a request as Tor Browser update ping if it matches the following criteria:</p>

<ul>
<li>Request method: GET</li>
<li>Resource string: <code>'%/torbrowser/update\__/%'</code> but not <code>'%.xml'</code></li>
<li>Response code: 200</li>
</ul>

<p>We distinguish platforms based on the resource string: <code>'%/WINNT%'</code> for Windows, <code>'%/Darwin%'</code> for macOS, and Linux otherwise.</p>

<p>We distinguish release channels based on the resource string: <code>'%/hardened/%'</code> for hardened releases, <code>'%/alpha/%'</code> for alpha releases, and <code>'%/release/%'</code> for stable releases.</p>

<p>We extract the locale (for example, <code>'en-US'</code> for English as used in the United States or <code>'de'</code> for German) from the resource string using regular expression <code>'.*/([a-zA-Z]{2}|[a-zA-Z]{2}-[a-zA-Z]{2})\??$'</code>, falling back to <code>'??'</code> for unrecognized locales if the regular expression does not match.</p>

<h4>Step 5: Count Tor Browser update requests</h4>

<p>We count a request as Tor Browser update request if it matches the following criteria:</p>

<ul>
<li>Request method: GET</li>
<li>Resource string: <code>'%/torbrowser/%.mar'</code></li>
<li>Response code: 302</li>
</ul>

<p>We distinguish platforms based on the resource string: <code>'%-win32-%'</code> for Windows, <code>'%-osx%'</code> for macOS, and Linux otherwise.</p>

<p>We distinguish release channels based on the resource string: <code>'%-hardened%'</code> for hardened releases, <code>'%/%.%a%/%'</code> for alpha releases, and stable releases otherwise.</p>

<p>We extract the locale (for example, <code>'en-US'</code> for English as used in the United States or <code>'de'</code> for German) from the resource string using regular expression <code>'.*_([a-zA-Z]{2}|[a-zA-Z]{2}-[a-zA-Z]{2})[\._-].*'</code>, falling back to <code>'??'</code> for unrecognized locales if the regular expression does not match.</p>

<p>We distinguish incremental updates having <code>'%.incremental.%'</code> in the resource string from non-incremental (full) updates that don't contain this pattern in their resource string.</p>

<h4>Step 6: Count Tor Messenger initial downloads</h4>

<p>We count a request as Tor Messenger initial download if it matches the following criteria:</p>

<ul>
<li>Request method: GET</li>
<li>Resource string: <code>'%/tormessenger/%.exe'</code>, <code>'%/tormessenger/%.dmg'</code>, and <code>'%/tormessenger/%.tar.xz'</code></li>
<li>Response code: 200</li>
</ul>

<p>We distinguish platforms based on the resource string: <code>'%.exe'</code> for Windows, <code>'%.dmg'</code> for macOS, and <code>'%.tar.xz'</code> for Linux.</p>

<p>We extract the locale (for example, <code>'en-US'</code> for English as used in the United States or <code>'de'</code> for German) from the resource string using regular expression <code>'.*_([a-zA-Z]{2}|[a-zA-Z]{2}-[a-zA-Z]{2})[\._-].*'</code>, falling back to <code>'??'</code> for unrecognized locales if the regular expression does not match.</p>

<h4>Step 7: Count Tor Messenger update pings</h4>

<p>We count a request as Tor Messenger update ping if it matches the following criteria:</p>

<ul>
<li>Request method: GET</li>
<li>Resource string: <code>'%/tormessenger/update\__/%'</code> but none of <code>'%.xml'</code>, <code>'%/'</code> or <code>'%/?'</code></li>
<li>Response code: 200</li>
</ul>

<p>We distinguish platforms based on the resource string: <code>'%/WINNT%'</code> for Windows, <code>'%/Darwin%'</code> for macOS, and <code>'%/Linux%'</code> for Linux.</p>

<p>We extract the locale (for example, <code>'en-US'</code> for English as used in the United States or <code>'de'</code> for German) from the resource string using regular expression <code>'.*/([a-zA-Z]{2}|[a-zA-Z]{2}-[a-zA-Z]{2})\??$'</code>, falling back to <code>'??'</code> for unrecognized locales if the regular expression does not match.</p>

</div>

<!--<div class="container">
<h2>Glossary <a href="#glossary" name="glossary" class="anchor">#</a></h2>
<p>&hellip;</p>
</div>-->

<!--<div class="container">
<h2>To-do list <a href="#todo" name="todo" class="anchor">#</a></h2>
<ul>
<li>Make sure that all technical terms have links to our glossary when they are first mentioned in a section.</li>
</ul>
</div>-->

<jsp:include page="bottom.jsp"/>

