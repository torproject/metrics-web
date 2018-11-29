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

<h1>Statistics files used on this website <a href="#stats" name="stats" class="anchor">#</a></h1>

<p>This page contains specifications of statistics files used on this website.</p>

<h3>Parameters</h3>

<p>All per-graph statistics files are available for download via an URL of the form:</p>

<pre>
https://metrics.torproject.org/identifier.csv
</pre>

<p>These URLs all support a set of <em>optional</em> parameters that can be used to further customize their content. Typically, these are <b>start</b> and <b>end</b> plus additional parameters as specified below. Including a parameter in a URL typically filters the resulting statistics file by the given parameter value. In reverse, omitting a parameter produces a larger statistics file that is not filtered by that parameter.</p>

<h3>Columns</h3>

<p>Each per-graph statistics file starts with a comment section, followed by a header line and then the actual data lines. Columns are pre-defined and specified further down below. The rule of thumb for columns is that neither the choice of parameters nor availability of data should affect the set of columns, but that only a code change can add, change, or remove a column. This rule of thumb is not yet implemented for all per-graph statistics files.</p>

<p>Applications must not rely on the order of columns, as this order may change when columns are removed. Instead, applications should refer to columns by their name. Applications should be able to handle newly added columns and fail gracefully in case of removed columns.</p>

<h3>Changes</h3>

<p>Changes to columns will be announced on this page at least a couple weeks in advance as well as on the <a href="https://lists.torproject.org/cgi-bin/mailman/listinfo/tor-dev">tor-dev@ mailing list</a>.</p>

<p>The following changes have been made in the past or are scheduled to become effective in the near future:</p>

<ul>
<li><b>February 28, 2018:</b> Added per-graph CSV files to eventually replace pre-aggregated CSV files.</li>
<li><b>May 29, 2018:</b> Made all parameters of per-graph CSV files optional to support providing both pre-filtered and complete data sets.</li>
<li><b>July 31, 2018:</b> Announced pending changes to per-graph CSV files to become effective on August 15 and pre-aggregated CSV files to be removed by September 15.</li>
<li><b>August 15, 2018:</b> Made the first batch of changes to per-graph CSV files.</li>
<li><b>September 15, 2018:</b> Removed all pre-aggregated CSV files.</li>
<li><b>October 28, 2018:</b> Added and/or removed columns to <a href="#webstats-tb-platform">Tor Browser downloads and updates by platform</a> and <a href="#webstats-tb-locale">Tor Browser downloads and updates by locale</a> graphs.</li>
<li><b>December 20, 2018 (scheduled):</b> Remove source parameters and output rows with aggregates over all sources from <a href="#torperf">Time to download files over Tor</a>, <a href="#torperf-failures">Timeouts and failures of downloading files over Tor</a>, <a href="#onionperf-buildtimes">Circuit build times</a>, <a href="#onionperf-latencies">Circuit round-trip latencies</a> graphs.</li>
<li><b>December 20, 2018 (scheduled):</b> Remove two graphs <a href="#bandwidth">Total relay bandwidth</a> and <a href="#bwhist-flags">Consumed bandwidth by Exit/Guard flag combination</a>, and update the data format of the <a href="#bandwidth-flags">Advertised and consumed bandwidth by relay flag</a> graph to cover all data previously contained in the first two graphs.</li>
</ul>

</div>

<div class="container">
<h2><i class="fa fa-users fa-fw" aria-hidden="true"></i>
Users <a href="#users" name="users" class="anchor">#</a></h2>

<h3>Relay users
<a href="/userstats-relay-country.html" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> graph</a>
<a href="/userstats-relay-country.csv" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> data</a>
<a href="#userstats-relay-country" name="userstats-relay-country" class="anchor">#</a></h3>

<h4>Parameters</h4>

<ul>
<li><b>start:</b> First UTC date (YYYY-MM-DD) to include in the file.</li>
<li><b>end:</b> Last UTC date (YYYY-MM-DD) to include in the file.</li>
<li><b>country:</b> Two-letter lower-case country code of clients to include in the file, or <b>"all"</b> for all clients.</li>
<li><b>events:</b> Ignored; only present for compatibility reasons with the corresponding graph.</li>
</ul>

<h4>Columns</h4>

<ul>
<li><b>date:</b> UTC date (YYYY-MM-DD) for which user numbers are estimated.</li>
<li><b>country:</b> Two-letter lower-case country code as found in a GeoIP database by resolving clients' IP addresses, or <b>"??"</b> if client IP addresses could not be resolved. If this column contains the empty string, all clients are included, regardless of their country code.</li>
<li><b>users:</b> Estimated number of clients.</li>
<li><b>lower:</b> Lower number of expected clients under the assumption that there has been no censorship event. If <b>users &lt; lower</b>, a censorship-related event might have happened in this country on the given day. If this column contains the empty string, there are no expectations on the number of clients.</li>
<li><b>upper:</b> Upper number of expected clients under the assumption that there has been no release of censorship. If <b>users &gt; upper</b>, a censorship-related event might have happened in this country on the given day. If this column contains the empty string, there are no expectations on the number of clients.</li>
<li><b>frac:</b> Fraction of relays in percent that the estimate is based on.</li>
</ul>

<h3>Bridge users by country
<a href="/userstats-bridge-country.html" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> graph</a>
<a href="/userstats-bridge-country.csv" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> data</a>
<a href="#userstats-bridge-country" name="userstats-bridge-country"class="anchor">#</a></h3>

<h4>Parameters</h4>

<ul>
<li><b>start:</b> First UTC date (YYYY-MM-DD) to include in the file.</li>
<li><b>end:</b> Last UTC date (YYYY-MM-DD) to include in the file.</li>
<li><b>country:</b> Two-letter lower-case country code of clients to include in the file, or <b>"all"</b> for all clients.</li>
</ul>

<h4>Columns</h4>

<ul>
<li><b>date:</b> UTC date (YYYY-MM-DD) for which user numbers are estimated.</li>
<li><b>country:</b> Two-letter lower-case country code as found in a GeoIP database by resolving clients' IP addresses, or <b>"??"</b> if client IP addresses could not be resolved. If this column contains the empty string, all clients are included, regardless of their country code.</li>
<li><b>users:</b> Estimated number of clients.</li>
<li><b>frac:</b> Fraction of bridges in percent that the estimate is based on.</li>
</ul>

<h3>Bridge users by transport
<a href="/userstats-bridge-transport.html" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> graph</a>
<a href="/userstats-bridge-transport.csv" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> data</a>
<a href="#userstats-bridge-transport" name="userstats-bridge-transport" class="anchor">#</a></h3>

<h4>Parameters</h4>

<ul>
<li><b>start:</b> First UTC date (YYYY-MM-DD) to include in the file.</li>
<li><b>end:</b> Last UTC date (YYYY-MM-DD) to include in the file.</li>
<li><b>transport:</b> Lower-case transport name to include in the file. Possible values include <b>"obfs4"</b>, <b>"websocket"</b> for Flash proxy/websocket, <b>"fte"</b> for FTE, <b>"!%3COR%3E"</b> (percent encoding of <b>"!&lt;OR&gt;"</b>) for any pluggable transport, <b>"%3C??%3E"</b> (percent encoding of <b>"&lt;??&gt;"</b>) for unknown pluggable transport(s), or <b>"%3COR%3E"</b> (percent encoding of <b>"&lt;OR&gt;"</b>) for the default OR protocol. Can be given multiple times.</li>
</ul>

<h4>Columns</h4>

<ul>
<li><b>date:</b> UTC date (YYYY-MM-DD) for which user numbers are estimated.</li>
<li><b>transport:</b> Transport name used by clients to connect to the Tor network using bridges. Examples are <b>"obfs4"</b>, <b>"websocket"</b> for Flash proxy/websocket, <b>"fte"</b> for FTE, <b>"!&lt;OR&gt;"</b> for any pluggable transport, <b>"&lt;??&gt;"</b> for unknown pluggable transport(s), or <b>"&lt;OR&gt;"</b> for the default OR protocol.</li>
<li><b>users:</b> Estimated number of clients.</li>
<li><b>frac:</b> Fraction of bridges in percent that the estimate is based on.</li>
</ul>

<h3>Bridge users by country and transport
<a href="/userstats-bridge-combined.html" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> graph</a>
<a href="/userstats-bridge-combined.csv" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> data</a>
<a href="#userstats-bridge-combined" name="userstats-bridge-combined" class="anchor">#</a></h3>

<h4>Parameters</h4>

<ul>
<li><b>start:</b> First UTC date (YYYY-MM-DD) to include in the file.</li>
<li><b>end:</b> Last UTC date (YYYY-MM-DD) to include in the file.</li>
<li><b>country:</b> Two-letter lower-case country code of clients to include in the file, or <b>"all"</b> for all clients (which, however, produces the same file as the bridge users by country for <b>"all"</b> countries).</li>
</ul>

<h4>Columns</h4>

<ul>
<li><b>date:</b> UTC date (YYYY-MM-DD) for which user numbers are estimated.</li>
<li><b>country:</b> Two-letter lower-case country code as found in a GeoIP database by resolving clients' IP addresses, or <b>"??"</b> if client IP addresses could not be resolved.</li>
<li><b>transport:</b> Transport name used by clients to connect to the Tor network using bridges. Examples are <b>"obfs4"</b>, <b>"websocket"</b> for Flash proxy/websocket, <b>"fte"</b> for FTE, <b>"&lt;??&gt;"</b> for unknown pluggable transport(s), or <b>"&lt;OR&gt;"</b> for the default OR protocol.</li>
<li><b>high:</b> Upper bound of estimated users from the given country and transport.</li>
<li><b>low:</b> Lower bound of estimated users from the given country and transport.</li>
<li><b>frac:</b> Fraction of bridges in percent that the estimate is based on.</li>
</ul>

<h3>Bridge users by IP version
<a href="/userstats-bridge-version.html" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> graph</a>
<a href="/userstats-bridge-version.csv" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> data</a>
<a href="#userstats-bridge-version" name="userstats-bridge-version" class="anchor">#</a></h3>

<h4>Parameters</h4>

<ul>
<li><b>start:</b> First UTC date (YYYY-MM-DD) to include in the file.</li>
<li><b>end:</b> Last UTC date (YYYY-MM-DD) to include in the file.</li>
<li><b>version:</b> IP version used by clients to connect to the Tor network
using bridges, which can be either <b>"v4"</b> or <b>"v6"</b>.</li>
</ul>

<h4>Columns</h4>

<ul>
<li><b>date:</b> UTC date (YYYY-MM-DD) for which user numbers are estimated.</li>
<li><b>version:</b> IP version used by clients to connect to the Tor network using bridges, which can be either <b>"v4"</b> or <b>"v6"</b>. If this column contains the empty string, all clients are included, regardless of their IP version.</li>
<li><b>users:</b> Estimated number of clients.</li>
<li><b>frac:</b> Fraction of bridges in percent that the estimate is based on.</li>
</ul>

</div>

<div class="container">
<h2><i class="fa fa-server fa-fw" aria-hidden="true"></i>
Servers <a href="#servers" name="servers" class="anchor">#</a></h2>

<h3>Relays and bridges
<a href="/networksize.html" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> graph</a>
<a href="/networksize.csv" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> data</a>
<a href="#networksize" name="networksize" class="anchor">#</a></h3>

<h4>Parameters</h4>

<ul>
<li><b>start:</b> First UTC date (YYYY-MM-DD) to include in the file.</li>
<li><b>end:</b> Last UTC date (YYYY-MM-DD) to include in the file.</li>
</ul>

<h4>Columns</h4>

<ul>
<li><b>date:</b> UTC date (YYYY-MM-DD) when relays or bridges have been listed as running.</li>
<li><b>relays:</b> Average number of relays.</li>
<li><b>bridges:</b> Average number of bridges.</li>
</ul>

<h3>Relays by relay flag
<a href="/relayflags.html" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> graph</a>
<a href="/relayflags.csv" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> data</a>
<a href="#relayflags" name="relayflags" class="anchor">#</a></h3>

<h4>Parameters</h4>

<ul>
<li><b>start:</b> First UTC date (YYYY-MM-DD) to include in the file.</li>
<li><b>end:</b> Last UTC date (YYYY-MM-DD) to include in the file.</li>
<li><b>flag:</b> Relay flag to include in the file. Examples are <b>"Running"</b>, <b>"Exit"</b>, <b>"Fast"</b>, <b>"Guard"</b>, <b>"Stable"</b>, and <b>"HSDir"</b>. This parameter can be given multiple times with different parameter values to include more relay numbers in the file.</li>
</ul>

<h4>Columns</h4>

<ul>
<li><b>date:</b> UTC date (YYYY-MM-DD) when relays have been listed as running.</li>
<li><b>flag:</b> Relay flag, which can be <b>"Exit"</b>, <b>"Fast"</b>, <b>"Guard"</b>, <b>"HSDir"</b>, <b>"Fast"</b>, <b>"Running"</b>, or <b>"Stable"</b>.</li>
<li><b>relays:</b> Average number of relays.</li>
</ul>

<h3>Relays by tor version
<a href="/versions.html" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> graph</a>
<a href="/versions.csv" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> data</a>
<a href="#versions" name="versions" class="anchor">#</a></h3>

<h4>Parameters</h4>

<ul>
<li><b>start:</b> First UTC date (YYYY-MM-DD) to include in the file.</li>
<li><b>end:</b> Last UTC date (YYYY-MM-DD) to include in the file.</li>
</ul>

<h4>Columns</h4>

<ul>
<li><b>date:</b> UTC date (YYYY-MM-DD) when relays have been listed as running.</li>
<li><b>version:</b> First three dotted numbers of the Tor software version as reported by the relay. An example is <b>"0.3.4"</b>.</li>
<li><b>relays:</b> Average number of relays.</li>
</ul>

<h3>Relays by platform
<a href="/platforms.html" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> graph</a>
<a href="/platforms.csv" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> data</a>
<a href="#platforms" name="platforms" class="anchor">#</a></h3>

<h4>Parameters</h4>

<ul>
<li><b>start:</b> First UTC date (YYYY-MM-DD) to include in the file.</li>
<li><b>end:</b> Last UTC date (YYYY-MM-DD) to include in the file.</li>
</ul>

<h4>Columns</h4>

<ul>
<li><b>date:</b> UTC date (YYYY-MM-DD) when relays have been listed as running.</li>
<li><b>bsd:</b> Average number of relays on *BSD.</li>
<li><b>linux:</b> Average number of relays on Linux.</li>
<li><b>macos:</b> Average number of relays on macOS.</li>
<li><b>other:</b> Average number of relays on another platform than Linux, *BSD, Windows, or macOS.</li>
<li><b>windows:</b> Average number of relays on Windows.</li>
</ul>

<h3>Relays by IP version
<a href="/relays-ipv6.html" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> graph</a>
<a href="/relays-ipv6.csv" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> data</a>
<a href="#relays-ipv6" name="relays-ipv6" class="anchor">#</a></h3>

<h4>Parameters</h4>

<ul>
<li><b>start:</b> First UTC date (YYYY-MM-DD) to include in the file.</li>
<li><b>end:</b> Last UTC date (YYYY-MM-DD) to include in the file.</li>
</ul>

<h4>Columns</h4>

<ul>
<li><b>date:</b> UTC date (YYYY-MM-DD) when relays have been listed as running.</li>
<li><b>announced:</b> Average number of relays that have announced an IPv6 address in their server descriptor.</li>
<li><b>exiting:</b> Average number of relays that have announced an IPv6 exit policy other than <b>"reject 1-65535"</b> in their server descriptor.</li>
<li><b>reachable:</b> Average number of relays with an IPv6 address that was confirmed as reachable by the directory authorities.</li>
<li><b>total:</b> Average number of relays.</li>
</ul>

<h3>Bridges by IP version
<a href="/bridges-ipv6.html" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> graph</a>
<a href="/bridges-ipv6.csv" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> data</a>
<a href="#bridges-ipv6" name="bridges-ipv6" class="anchor">#</a></h3>

<h4>Parameters</h4>

<ul>
<li><b>start:</b> First UTC date (YYYY-MM-DD) to include in the file.</li>
<li><b>end:</b> Last UTC date (YYYY-MM-DD) to include in the file.</li>
</ul>

<h4>Columns</h4>

<ul>
<li><b>date:</b> UTC date (YYYY-MM-DD) when bridges have been listed as running.</li>
<li><b>announced:</b> Average number of bridges that have announced an IPv6 address in their server descriptor.</li>
<li><b>total:</b> Average number of bridges.</li>
</ul>

<h3>Total consensus weights across bandwidth authorities
<a href="/totalcw.html" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> graph</a>
<a href="/totalcw.csv" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> data</a>
<a href="#totalcw" name="totalcw" class="anchor">#</a></h3>

<h4>Parameters</h4>

<ul>
<li><b>start:</b> First UTC date (YYYY-MM-DD) to include in the file.</li>
<li><b>end:</b> Last UTC date (YYYY-MM-DD) to include in the file.</li>
</ul>

<h4>Columns</h4>

<ul>
<li><b>date:</b> UTC date (YYYY-MM-DD) when relays have been listed as running.</li>
<li><b>nickname:</b> Bandwidth authority nickname, or the empty string in case of the consensus.</li>
<li><b>totalcw:</b> Total consensus weight of all running relays measured by the bandwidth authority or contained in the consensus.</li>
</ul>

</div>

<div class="container">
<h2><i class="fa fa-road fa-fw" aria-hidden="true"></i>
Traffic <a href="#traffic" name="traffic" class="anchor">#</a></h2>

<h3>Total relay bandwidth
<a href="/bandwidth.html" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> graph</a>
<a href="/bandwidth.csv" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> data</a>
<a href="#bandwidth" name="bandwidth" class="anchor">#</a></h3>

<div class="bs-callout bs-callout-warning">
<h3>Deprecated</h3>
<p>This graph will disappear by December 20, 2018, because it won't contain anything new compared to the soon-to-be tweaked <a href="#bandwidth-flags">Advertised and consumed bandwidth by relay flags</a> graph.</p>
</div>

<h4>Parameters</h4>

<ul>
<li><b>start:</b> First UTC date (YYYY-MM-DD) to include in the file.</li>
<li><b>end:</b> Last UTC date (YYYY-MM-DD) to include in the file.</li>
</ul>

<h4>Columns</h4>

<ul>
<li><b>date:</b> UTC date (YYYY-MM-DD) that relays reported bandwidth data for.</li>
<li><b>advbw:</b> Total advertised bandwidth in Gbit/s that relays are capable to provide.</li>
<li><b>bwhist:</b> Total consumed bandwidth in Gbit/s as the average of written and read traffic of all relays.</li>
</ul>

<h3>Advertised and consumed bandwidth by relay flag
<a href="/bandwidth-flags.html" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> graph</a>
<a href="/bandwidth-flags.csv" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> data</a>
<a href="#bandwidth-flags" name="bandwidth-flags" class="anchor">#</a></h3>

<h4>Parameters</h4>

<ul>
<li><b>start:</b> First UTC date (YYYY-MM-DD) to include in the file.</li>
<li><b>end:</b> Last UTC date (YYYY-MM-DD) to include in the file.</li>
</ul>

<h4>Columns</h4>

<ul>
<li><b>date:</b> UTC date (YYYY-MM-DD) that relays reported bandwidth data for.</li>
<li><b>guard_advbw:</b> Total advertised bandwidth in Gbit/s that relays with the <b>"Guard"</b> relay flag are capable to provide. <span class="red">This column is going to be removed after December 20, 2018.</span></li>
<li><b>guard_bwhist:</b> Total consumed bandwidth in Gbit/s as the average of written and read traffic of relays with the <b>"Guard"</b> relay flag. <span class="red">This column is going to be removed after December 20, 2018.</span></li>
<li><b>exit_advbw:</b> Total advertised bandwidth in Gbit/s that relays with the <b>"Exit"</b> relay flag are capable to provide. <span class="red">This column is going to be removed after December 20, 2018.</span></li>
<li><b>exit_bwhist:</b> Total consumed bandwidth in Gbit/s as the average of written and read traffic of relays with the <b>"Exit"</b> relay flag. <span class="red">This column is going to be removed after December 20, 2018.</span></li>
<li><b>have_guard_flag:</b> Whether relays included in this row had the <code>"Guard"</code> relay flag assigned (<code>"t"</code>) or not (<code>"f"</code>). <span class="blue">This column is going to be added after December 20, 2018.</span></li>
<li><b>have_exit_flag:</b> Whether relays included in this row had the <code>"Exit"</code> relay flag assigned and at the same time the <code>"BadExit"</code> not assigned (<code>"t"</code>) or not (<code>"f"</code>). <span class="blue">This column is going to be added after December 20, 2018.</span></li>
<li><b>advbw:</b> Total advertised bandwidth in Gbit/s that relays are capable to provide. <span class="blue">This column is going to be added after December 20, 2018.</span></li>
<li><b>bwhist:</b> Total consumed bandwidth in Gbit/s as the average of written and read traffic. <span class="blue">This column is going to be added after December 20, 2018.</span></li>
</ul>

<h3>Advertised bandwidth by IP version
<a href="/advbw-ipv6.html" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> graph</a>
<a href="/advbw-ipv6.csv" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> data</a>
<a href="#advbw-ipv6" name="advbw-ipv6" class="anchor">#</a></h3>

<h4>Parameters</h4>

<ul>
<li><b>start:</b> First UTC date (YYYY-MM-DD) to include in the file.</li>
<li><b>end:</b> Last UTC date (YYYY-MM-DD) to include in the file.</li>
</ul>

<h4>Columns</h4>

<ul>
<li><b>date:</b> UTC date (YYYY-MM-DD) that relays reported bandwidth data for.</li>
<li><b>exiting:</b> Total advertised bandwidth in Gbit/s of all relays that have announced an IPv6 exit policy other than <b>"reject 1-65535"</b> in their server descriptor.</li>
<li><b>reachable_exit:</b> Total advertised bandwidth in Gbit/s of relays with the <b>"Exit"</b> relay flag and an IPv6 address that was confirmed as reachable by the directory authorities.</li>
<li><b>reachable_guard:</b> Total advertised bandwidth in Gbit/s of relays with the <b>"Guard"</b> relay flag and an IPv6 address that was confirmed as reachable by the directory authorities.</li>
<li><b>total:</b> Total advertised bandwidth in Gbit/s of all relays.</li>
<li><b>total_exit:</b> Total advertised bandwidth in Gbit/s of relays with the <b>"Exit"</b> relay flag.</li>
<li><b>total_guard:</b> Total advertised bandwidth in Gbit/s of relays with the <b>"Guard"</b> relay flag.</li>
</ul>

<h3>Advertised bandwidth distribution
<a href="/advbwdist-perc.html" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> graph</a>
<a href="/advbwdist-perc.csv" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> data</a>
<a href="#advbwdist-perc" name="advbwdist-perc" class="anchor">#</a></h3>

<h4>Parameters</h4>

<ul>
<li><b>start:</b> First UTC date (YYYY-MM-DD) to include in the file.</li>
<li><b>end:</b> Last UTC date (YYYY-MM-DD) to include in the file.</li>
<li><b>p:</b> Percentile to include in the file, with pre-defined possible values: 100, 99, 98, 97, 95, 91, 90, 80, 75, 70, 60, 50, 40, 30, 25, 20, 10, 9, 5, 3, 2, 1, 0. Can be given multiple times.</li>
</ul>

<h4>Columns</h4>

<ul>
<li><b>date:</b> UTC date (YYYY-MM-DD) that relays reported bandwidth data for.</li>
<li><b>p:</b> Percentile as value between 0 and 100.</li>
<li><b>all:</b> Advertised bandwidth in Gbit/s of the p-th percentile of all relays.</li>
<li><b>exits:</b> Advertised bandwidth in Gbit/s of the p-th percentile of relays with the <b>"Exit"</b> relay flag.</li>
</ul>

<h3>Advertised bandwidth of n-th fastest relays
<a href="/advbwdist-relay.html" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> graph</a>
<a href="/advbwdist-relay.csv" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> data</a>
<a href="#advbwdist-relay" name="advbwdist-relay" class="anchor">#</a></h3>

<h4>Parameters</h4>

<ul>
<li><b>start:</b> First UTC date (YYYY-MM-DD) to include in the file.</li>
<li><b>end:</b> Last UTC date (YYYY-MM-DD) to include in the file.</li>
<li><b>n:</b> Relay by advertised bandwidth to include in the file, with pre-defined possible values: 1, 2, 3, 5, 10, 20, 30, 50, 100, 200, 300, 500, 1000, 2000, 3000, 5000. Can be given multiple times.</li>
</ul>

<h4>Columns</h4>

<ul>
<li><b>date:</b> UTC date (YYYY-MM-DD) that relays reported bandwidth data for.</li>
<li><b>n:</b> Position of the relay in an ordered list of all advertised bandwidths, starting at 1 for the fastest relay in the network.</li>
<li><b>all:</b> Advertised bandwidth in Gbit/s of n-th fastest relay.</li>
<li><b>exits:</b> Advertised bandwidth in Gbit/s of n-th fastest relay with the <b>"Exit"</b> relay flag.</li>
</ul>

<h3>Consumed bandwidth by Exit/Guard flag combination
<a href="/bwhist-flags.html" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> graph</a>
<a href="/bwhist-flags.csv" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> data</a>
<a href="#bwhist-flags" name="bwhist-flags" class="anchor">#</a></h3>

<div class="bs-callout bs-callout-warning">
<h3>Deprecated</h3>
<p>This graph will disappear by December 20, 2018, because it won't contain anything new compared to the soon-to-be tweaked <a href="#bandwidth-flags">Advertised and consumed bandwidth by relay flags</a> graph.</p>
</div>

<h4>Parameters</h4>

<ul>
<li><b>start:</b> First UTC date (YYYY-MM-DD) to include in the file.</li>
<li><b>end:</b> Last UTC date (YYYY-MM-DD) to include in the file.</li>
</ul>

<h4>Columns</h4>

<ul>
<li><b>date:</b> UTC date (YYYY-MM-DD) that relays reported bandwidth data for.</li>
<li><b>exit_only:</b> Total consumed bandwidth in Gbit/s as the average of written and read traffic of relays without <b>"Guard"</b> and with <b>"Exit"</b> relay flag.</li>
<li><b>guard_and_exit:</b> Total consumed bandwidth in Gbit/s as the average of written and read traffic of relays with both <b>"Guard"</b> and <b>"Exit"</b> relay flag.</li>
<li><b>guard_only:</b> Total consumed bandwidth in Gbit/s as the average of written and read traffic of relays with <b>"Guard"</b> and without <b>"Exit"</b> relay flag.</li>
<li><b>middle_only:</b> Total consumed bandwidth in Gbit/s as the average of written and read traffic of relays with neither <b>"Guard"</b> nor <b>"Exit"</b> relay flag.</li>
</ul>

<h3>Bandwidth spent on answering directory requests
<a href="/dirbytes.html" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> graph</a>
<a href="/dirbytes.csv" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> data</a>
<a href="#dirbytes" name="dirbytes" class="anchor">#</a></h3>

<h4>Parameters</h4>

<ul>
<li><b>start:</b> First UTC date (YYYY-MM-DD) to include in the file.</li>
<li><b>end:</b> Last UTC date (YYYY-MM-DD) to include in the file.</li>
</ul>

<h4>Columns</h4>

<ul>
<li><b>date:</b> UTC date (YYYY-MM-DD) that relays reported bandwidth data for.</li>
<li><b>dirread:</b> Bandwidth in Gbit/s that directory mirrors have read when serving directory data.</li>
<li><b>dirwrite:</b> Bandwidth in Gbit/s that directory mirrors have written when serving directory data.</li>
</ul>

<h3>Fraction of connections used uni-/bidirectionally
<a href="/connbidirect.html" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> graph</a>
<a href="/connbidirect.csv" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> data</a>
<a href="#connbidirect" name="connbidirect" class="anchor">#</a></h3>

<h4>Parameters</h4>

<ul>
<li><b>start:</b> First UTC date (YYYY-MM-DD) to include in the file.</li>
<li><b>end:</b> Last UTC date (YYYY-MM-DD) to include in the file.</li>
</ul>

<h4>Columns</h4>

<ul>
<li><b>date:</b> UTC date (YYYY-MM-DD) for which statistics on uni-/bidirectional connection usage were reported.</li>
<li><b>direction:</b> Direction of reported fraction, which can be <b>"read"</b>, <b>"write"</b>, or <b>"both"</b> for connections classified as "mostly reading", "mostly writing", or "both reading and writing". Connections below the threshold have been removed from this statistics file entirely.</li>
<li><b>q1:</b> First quartile of fraction of connections.</li>
<li><b>md:</b> Median of fraction of connections.</li>
<li><b>q3:</b> Third quartile of fraction of connections.</li>
</ul>

</div>

<div class="container">
<h2><i class="fa fa-dashboard fa-fw" aria-hidden="true"></i>
Performance <a href="#performance" name="performance" class="anchor">#</a></h2>

<h3>Time to download files over Tor
<a href="/torperf.html" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> graph</a>
<a href="/torperf.csv" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> data</a>
<a href="#torperf" name="torperf" class="anchor">#</a></h3>

<h4>Parameters</h4>

<ul>
<li><b>start:</b> First UTC date (YYYY-MM-DD) to include in the file.</li>
<li><b>end:</b> Last UTC date (YYYY-MM-DD) to include in the file.</li>
<li><b>source:</b> Name of the OnionPerf or Torperf service performing measurements, or <b>"all"</b> for measurements performed by any service. <span class="red">This parameter is going to be removed after December 20, 2018.</span></li>
<li><b>server:</b> Either <b>"public"</b> for requests to a server on the public internet, or <b>"onion"</b> for requests to a version 2 onion server.</li>
<li><b>filesize:</b> Size of the downloaded file in bytes, with pre-defined possible values: <b>"50kb"</b>, <b>"1mb"</b>, or <b>"5mb"</b>.</li>
</ul>

<h4>Columns</h4>

<ul>
<li><b>date:</b> UTC date (YYYY-MM-DD) when download performance was measured.</li>
<li><b>filesize:</b> Size of the downloaded file in bytes.</li>
<li><b>source:</b> Name of the OnionPerf or Torperf service performing measurements. If this column contains the empty string, all measurements are included, regardless of which service performed them. <span class="red">Output rows with aggregates over all sources are going to be removed after December 20, 2018.</span></li>
<li><b>server:</b> Either <b>"public"</b> if the request was made to a server on the public internet, or <b>"onion"</b> if the request was made to a version 2 onion server.</li>
<li><b>q1:</b> First quartile of time in milliseconds until receiving the last byte.</li>
<li><b>md:</b> Median of time in milliseconds until receiving the last byte.</li>
<li><b>q3:</b> Third quartile of time in milliseconds until receiving the last byte.</li>
</ul>

<h3>Timeouts and failures of downloading files over Tor
<a href="/torperf-failures.html" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> graph</a>
<a href="/torperf-failures.csv" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> data</a>
<a href="#torperf-failures" name="torperf-failures" class="anchor">#</a></h3>

<h4>Parameters</h4>

<ul>
<li><b>start:</b> First UTC date (YYYY-MM-DD) to include in the file.</li>
<li><b>end:</b> Last UTC date (YYYY-MM-DD) to include in the file.</li>
<li><b>source:</b> Name of the OnionPerf or Torperf service performing measurements, or <b>"all"</b> for measurements performed by any service. <span class="red">This parameter is going to be removed after December 20, 2018.</span></li>
<li><b>server:</b> Either <b>"public"</b> for requests to a server on the public internet, or <b>"onion"</b> for requests to a version 2 onion server.</li>
<li><b>filesize:</b> Size of the downloaded file in bytes, with pre-defined possible values: <b>"50kb"</b>, <b>"1mb"</b>, or <b>"5mb"</b>.</li>
</ul>

<h4>Columns</h4>

<ul>
<li><b>date:</b> UTC date (YYYY-MM-DD) when download performance was measured.</li>
<li><b>filesize:</b> Size of the downloaded file in bytes.</li>
<li><b>source:</b> Name of the OnionPerf or Torperf service performing measurements. If this column contains the empty string, all measurements are included, regardless of which service performed them. <span class="red">Output rows with aggregates over all sources are going to be removed after December 20, 2018.</span></li>
<li><b>server:</b> Either <b>"public"</b> if the request was made to a server on the public internet, or <b>"onion"</b> if the request was made to a version 2 onion server.</li>
<li><b>timeouts:</b> Fraction of requests that timed out when attempting to download the static file over Tor.</li>
<li><b>failures:</b> Fraction of requests that failed when attempting to download the static file over Tor.</li>
</ul>

<h3>Circuit build times
<a href="/onionperf-buildtimes.html" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> graph</a>
<a href="/onionperf-buildtimes.csv" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> data</a>
<a href="#onionperf-buildtimes" name="onionperf-buildtimes" class="anchor">#</a></h3>

<h4>Parameters</h4>

<ul>
<li><b>start:</b> First UTC date (YYYY-MM-DD) to include in the file.</li>
<li><b>end:</b> Last UTC date (YYYY-MM-DD) to include in the file.</li>
<li><b>source:</b> Name of the OnionPerf or Torperf service performing measurements, or <b>"all"</b> for measurements performed by any service. <span class="red">This parameter is going to be removed after December 20, 2018.</span></li>
</ul>

<h4>Columns</h4>

<ul>
<li><b>date:</b> UTC date (YYYY-MM-DD) when download performance was measured.</li>
<li><b>source:</b> Name of the OnionPerf or Torperf service performing measurements. If this column contains the empty string, all measurements are included, regardless of which service performed them. <span class="red">Output rows with aggregates over all sources are going to be removed after December 20, 2018.</span></li>
<li><b>position:</b> Position in the circuit, from first to third hop.</li>
<li><b>q1:</b> First quartile of time in milliseconds until successfully extending the circuit to the given position.</li>
<li><b>md:</b> Median of time in milliseconds until successfully extending the circuit to the given position.</li>
<li><b>q3:</b> Third quartile of time in milliseconds until successfully extending the circuit to the given position.</li>
</ul>

<h3>Circuit round-trip latencies
<a href="/onionperf-latencies.html" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> graph</a>
<a href="/onionperf-latencies.csv" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> data</a>
<a href="#onionperf-latencies" name="onionperf-latencies" class="anchor">#</a></h3>

<h4>Parameters</h4>

<ul>
<li><b>start:</b> First UTC date (YYYY-MM-DD) to include in the file.</li>
<li><b>end:</b> Last UTC date (YYYY-MM-DD) to include in the file.</li>
<li><b>source:</b> Name of the OnionPerf or Torperf service performing measurements, or <b>"all"</b> for measurements performed by any service. <span class="red">This parameter is going to be removed after December 20, 2018.</span></li>
</ul>

<h4>Columns</h4>

<ul>
<li><b>date:</b> UTC date (YYYY-MM-DD) when download performance was measured.</li>
<li><b>source:</b> Name of the OnionPerf or Torperf service performing measurements. If this column contains the empty string, all measurements are included, regardless of which service performed them. <span class="red">Output rows with aggregates over all sources are going to be removed after December 20, 2018.</span></li>
<li><b>server:</b> Either <b>"public"</b> if the request was made to a server on the public internet, or <b>"onion"</b> if the request was made to a version 2 onion server.</li>
<li><b>q1:</b> First quartile of time in milliseconds between sending the HTTP request and receiving the HTTP response header.</li>
<li><b>md:</b> Median of time in milliseconds between sending the HTTP request and receiving the HTTP response header.</li>
<li><b>q3:</b> Third quartile of time in milliseconds between sending the HTTP request and receiving the HTTP response header.</li>
</ul>

</div>

<div class="container">
<h2><i class="fa fa-map-signs fa-fw" aria-hidden="true"></i>
Onion Services <a href="#onion-services" name="onion-services" class="anchor">#</a></h2>

<h3>Unique .onion addresses (version 2 only)
<a href="/hidserv-dir-onions-seen.html" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> graph</a>
<a href="/hidserv-dir-onions-seen.csv" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> data</a>
<a href="#hidserv-dir-onions-seen" name="hidserv-dir-onions-seen" class="anchor">#</a></h3>

<h4>Parameters</h4>

<ul>
<li><b>start:</b> First UTC date (YYYY-MM-DD) to include in the file.</li>
<li><b>end:</b> Last UTC date (YYYY-MM-DD) to include in the file.</li>
</ul>

<h4>Columns</h4>

<ul>
<li><b>date:</b> UTC date (YYYY-MM-DD) when relays have been listed as running.</li>
<li><b>onions:</b> Estimated number of unique .onion addresses observed by onion-service directories.</li>
<li><b>frac:</b> Total network fraction of statistics reported by onion-service directories.</li>
</ul>

<h3>Onion-service traffic (versions 2 and 3)
<a href="/hidserv-rend-relayed-cells.html" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> graph</a>
<a href="/hidserv-rend-relayed-cells.csv" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> data</a>
<a href="#hidserv-rend-relayed-cells" name="hidserv-rend-relayed-cells" class="anchor">#</a></h3>

<h4>Parameters</h4>

<ul>
<li><b>start:</b> First UTC date (YYYY-MM-DD) to include in the file.</li>
<li><b>end:</b> Last UTC date (YYYY-MM-DD) to include in the file.</li>
</ul>

<h4>Columns</h4>

<ul>
<li><b>date:</b> UTC date (YYYY-MM-DD) when relays have been listed as running.</li>
<li><b>relayed:</b> Estimated bandwidth in Gbit/s relayed on rendezvous circuits as observed by rendezvous points.</li>
<li><b>frac:</b> Total network fraction of statistics reported by rendezvous points.</li>
</ul>

</div>

<div class="container">
<h2><i class="fa fa-download fa-fw" aria-hidden="true"></i>
Applications <a href="#applications" name="applications" class="anchor">#</a></h2>

<h3>Tor Browser downloads and updates
<a href="/webstats-tb.html" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> graph</a>
<a href="/webstats-tb.csv" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> data</a>
<a href="#webstats-tb" name="webstats-tb" class="anchor">#</a></h3>

<h4>Parameters</h4>

<ul>
<li><b>start:</b> First UTC date (YYYY-MM-DD) to include in the file.</li>
<li><b>end:</b> Last UTC date (YYYY-MM-DD) to include in the file.</li>
</ul>

<h4>Columns</h4>

<ul>
<li><b>date:</b> UTC date (YYYY-MM-DD) when requests to <code>torproject.org</code> web servers have been logged.</li>
<li><b>initial_downloads:</b> Number of Tor Browser initial downloads: GET requests to all sites with resource strings <code>'%/torbrowser/%.exe'</code>, <code>'%/torbrowser/%.dmg'</code>, and <code>'%/torbrowser/%.tar.xz'</code> and response code 200.</li>
<li><b>signature_downloads:</b> Number of Tor Browser signature downloads: GET requests to all sites with resource strings <code>'%/torbrowser/%.exe.asc'</code>, <code>'%/torbrowser/%.dmg.asc'</code>, and <code>'%/torbrowser/%.tar.xz.asc'</code> and response code 200.</li>
<li><b>update_pings:</b> Number of Tor Browser update pings: GET requests to all sites with resource strings <code>'%/torbrowser/update\__/%'</code> and response code 200.</li>
<li><b>update_requests:</b> Number of Tor Browser update requests: GET requests to all sites with resource strings <code>'%/torbrowser/%.mar'</code> and response code 302.</li>
</ul>

<h3>Tor Browser downloads and updates by platform
<a href="/webstats-tb-platform.html" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> graph</a>
<a href="/webstats-tb-platform.csv" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> data</a>
<a href="#webstats-tb-platform" name="webstats-tb-platform" class="anchor">#</a></h3>

<h4>Parameters</h4>

<ul>
<li><b>start:</b> First UTC date (YYYY-MM-DD) to include in the file.</li>
<li><b>end:</b> Last UTC date (YYYY-MM-DD) to include in the file.</li>
</ul>

<h4>Columns</h4>

<ul>
<li><b>date:</b> UTC date (YYYY-MM-DD) when requests to <code>torproject.org</code> web servers have been logged.</li>
<li><b>platform:</b> Platform, like "Linux", "macOS", or "Windows".</li>
<li><b>initial_downloads:</b> Number of Tor Browser initial downloads.</li>
<li><b>update_pings:</b> Number of Tor Browser update pings.</li>
</ul>

<h3>Tor Browser downloads and updates by locale
<a href="/webstats-tb-locale.html" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> graph</a>
<a href="/webstats-tb-locale.csv" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> data</a>
<a href="#webstats-tb-locale" name="webstats-tb-locale" class="anchor">#</a></h3>

<h4>Parameters</h4>

<ul>
<li><b>start:</b> First UTC date (YYYY-MM-DD) to include in the file.</li>
<li><b>end:</b> Last UTC date (YYYY-MM-DD) to include in the file.</li>
</ul>

<h4>Columns</h4>

<ul>
<li><b>date:</b> UTC date (YYYY-MM-DD) when requests to <code>torproject.org</code> web servers have been logged.</li>
<li><b>locale:</b> Locale, like "en-US" for English (United States), "de" for German, etc., and "??" for unrecognized locales.</li>
<li><b>initial_downloads:</b> Number of Tor Browser initial downloads.</li>
<li><b>update_pings:</b> Number of Tor Browser update pings.</li>
</ul>

<h3>Tor Messenger downloads and updates
<a href="/webstats-tm.html" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> graph</a>
<a href="/webstats-tm.csv" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> data</a>
<a href="#webstats-tm" name="webstats-tm" class="anchor">#</a></h3>

<h4>Parameters</h4>

<ul>
<li><b>start:</b> First UTC date (YYYY-MM-DD) to include in the file.</li>
<li><b>end:</b> Last UTC date (YYYY-MM-DD) to include in the file.</li>
</ul>

<h4>Columns</h4>

<ul>
<li><b>log_date:</b> UTC date (YYYY-MM-DD) when requests to <code>torproject.org</code> web servers have been logged.</li>
<li><b>initial_downloads:</b> Number of Tor Messenger initial downloads: GET requests to all sites with resource strings <code>'%/tormessenger/%.exe'</code>, <code>'%/tormessenger/%.dmg'</code>, and <code>'%/tormessenger/%.tar.xz'</code> and response code 200.</li>
<li><b>update_pings:</b> Number of Tor Messenger update pings: GET requests to all sites with resource strings <code>'%/tormessenger/update\__/%'</code> and response code 200.</li>
</ul>

</div>

<jsp:include page="bottom.jsp"/>

