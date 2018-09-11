<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<jsp:include page="top.jsp">
  <jsp:param name="pageTitle" value="About &ndash; Tor Metrics"/>
  <jsp:param name="navActive" value="About"/>
</jsp:include>

    <div class="container">
      <ul class="breadcrumb">
        <li><a href="/">Home</a></li>
        <li class="active">About</li>
      </ul>
    </div>

    <div class="container">
      <h1>About <a href="#about" name="about" class="anchor">#</a></h1>
      <p>Tor Metrics archives historical data about the Tor ecosystem, collects
      data from the public Tor network and related services, and assists in
      developing novel approaches to safe, privacy preserving data
      collection.</p>

      <h2>Philosophy <a href="#philosophy" name="philosophy" class="anchor">#</a></h2>
      <p>We only use public, non-sensitive data for metrics. Each metric goes
      through a rigorous review and discussion process before appearing here.
      We never publish statistics&mdash;or aggregate statistics&mdash;of
      sensitive data, such as unencrypted contents of traffic.</p>
      <p>The goals of a privacy and anonymity network like Tor are not easily
      combined with extensive data gathering, but at the same time data is needed for
      monitoring, understanding, and improving the network. Data can be used to
      detect possible censorship events or attacks against the network. Safety and
      privacy concerns regarding data collection by Tor Metrics are guided by the <a
      href="https://research.torproject.org/safetyboard.html#guidelines">Tor Research
      Safety Board's guidelines</a>.  Safety and privacy assessment is usually done
      openly by discussion during the proposal process for changes to the Tor source,
      and/or supported by closer analysis in form of <a
      href="https://research.torproject.org/techreports.html">Tor Technical
      Reports</a>.</p>
      <p>For data we collect from the public Tor network, we will always follow
      three main guidelines:</p>
      <dl class="dl-horizontal">
      <dt>Data minimalism</dt>
      <dd>The first and most important guideline is that only the minimum
      amount of statistical data should be gathered to solve a given problem.
      The level of detail of measured data should be as small as possible.</dd>
      <dt>Source aggregation</dt>
      <dd>Possibly sensitive data should exist for as short a time
      as possible. Data should be aggregated at its source, including
      categorizing single events and memorizing category counts only, summing
      up event counts over large time frames, and being imprecise regarding
      exact event counts.</dd>
      <dt>Transparency</dt>
      <dd>All algorithms to gather statistical data need to be discussed
      publicly before deploying them. All measured statistical data should be
      made publicly available as a safeguard to not gather data that is too
      sensitive.</dd>
      </dl>
      <p>You can read more about safety considerations when collecting data in
      the Tor network in "<a
      href="https://www.freehaven.net/anonbib/#wecsr10measuring-tor">A Case
      Study on Measuring Statistical Data in the Tor Anonymity Network</a>" by
      Karsten Loesing, Steven J. Murdoch, and Roger Dingledine.  <i>In the
      Proceedings of the Workshop on Ethics in Computer Security Research
      (WECSR 2010), Tenerife, Canary Islands, Spain, January 2010.</i></p>

      <h2>Ecosystem <a href="#ecosystem" name="ecosystem" class="anchor">#</a></h2>
      <p>Tor relays and bridges collect aggregated statistics about their usage
      including bandwidth and connecting clients per country. Source
      aggregation is used to protect the privacy of connecting
      users&mdash;discarding IP addresses and only reporting country
      information from a local database mapping IP address ranges to countries.
      These statistics are sent periodically to the directory authorities.</p>
      <p><a href="/collector.html">CollecTor</a>
      downloads the latest server descriptors, extra info descriptors
      containing the aggregated statistics, and consensus documents from the
      directory authorities and archives them. This archive is public and the
      <a href="/metrics-lib.html">metrics-lib</a>
      Java library can be used to parse the contents of the archive to perform
      analysis of the data.</p>
      <p>In order to provide easy access to visualizations of the historical
      data archived, the Tor Metrics website contains a number of customizable
      plots to show <a
      href="/userstats-relay-country.html">user</a>,
      <a href="/bandwidth.html">traffic</a>, <a
      href="/relayflags.html">relay</a>, <a
      href="/bridges-ipv6.html">bridge</a>, and
      <a href="/webstats-tb.html">application
      download</a> statistics over a requested time period and filtered to a
      particular country.</p>
      <p>In order to provide easy access to current information about the
      public Tor network, <a
      href="/onionoo.html">Onionoo</a> implements
      a protocol to serve JSON documents over HTTP that can be consumed by
      applications that would like to display information about relays along
      with historical bandwidth, uptime, and consensus weight information.</p>
      <p>An example of one such application is <a
      href="/rs.html">Relay Search</a> which is
      used by relay operators, those monitoring the health of the network, and
      developers of software using the Tor network. Another example of such an
      application is <a href="https://people.torproject.org/~irl/metrics-bot"
      target="_blank">metrics-bot</a> which posts regular snapshots to <a
      href="https://twitter.com/TorAtlas" target="_blank">Twitter</a> and <a
      href="https://botsin.space/@metricsbot" target="_blank">Mastodon</a>
      including country statistics and a world map plotting known relays.</p>
      <p>The diagram below shows how data is collected, archived, analyzed, and
      presented to users through services operated by Tor Metrics. The majority
      of our services use metrics-lib to parse the descriptors that have been
      collected by CollecTor as their source of raw data about the public Tor
      network.</p>
      <figure>
      <img alt="Data flows from the public Tor network to the end user tools
      Tor Metrics Graphs and Exonerator via CollecTor, and Relay Search and
      metrics-bot via CollecTor and Onionoo." src="/images/ecosystem.png"
      class="thumbnail img-responsive center-block">
      </figure>

      <h2>Contributing <a href="#contributing" name="contributing" class="anchor">#</a></h2>
      <p>Collecting and processing new data won't likely happen without your help! If you really want to see something measured here, we would be happy to work with you. Learn more about contributing on our <a href="https://trac.torproject.org/projects/tor/wiki/org/teams/MetricsTeam" target="_blank">team wiki page</a>.</p>

      <h2>Contact <a href="#contact" name="contact" class="anchor">#</a></h2>
      <p>If you have any questions or suggestions, contact us at <a href="mailto:metrics-team@lists.torproject.org">metrics-team@lists.torproject.org</a>, which is a <a href="https://lists.torproject.org/cgi-bin/mailman/listinfo/metrics-team" target="_blank">public mailing list</a>.</p>

      <p>If you have a question about a relay or operating a relay, please contact <a href="mailto:tor-relays@lists.torproject.org">tor-relays@lists.torproject.org</a>, which is a <a href="https://lists.torproject.org/cgi-bin/mailman/listinfo/tor-relays" target="_blank">public mailing list</a> or <a href="https://lists.torproject.org/pipermail/tor-relays/" target="_blank">search the list archives</a> for answers.</p>

      <p>Tor Metrics is a project of:</p>
<pre>The Tor Project, Inc.
217 1st Ave South #4903
Seattle, WA 98194 USA</pre>

      <h2>Testimonials <a href="#testimonials" name="testimonials" class="anchor">#</a></h2>
      <blockquote>
        <p class="mb-0">
          Metrics are a critical part of any security technology. If you don't know how the technology works in practice, you can't find and fix problems. You can't improve the security. You can't make it work better. This isn't glamorous or sexy work, but it's essential. This is especially true for security and privacy, where our preconceived notions of threats and usage are regularly wrong&mdash;and knowing what's really going on is the difference between security and insecurity.<br><br>
          Tor is doing cutting-edge work in the anonymity space, and Tor metrics are already proven to provide critical information for research and development. It's one of the few open data sets available for how, why, where, and when people use anonymizing technologies.<br><br>
          Tor's metrics project increases the transparency of Tor's work. This helps users understand how Tor works. With good network metrics, you can look back for indicators and anomalies at the time a privacy issue was reported. You can also extrapolate and look forward to prevent related issues in the future. This helps alleviate users' security concerns, and helps others contribute to security issues in the network and browser.<br><br>
          Finally, Tor metrics are the ammunition that lets Tor and other security advocates argue for a more private and secure  Internet from a position of data, rather than just dogma or perspective. It's where the real world influences Tor.&rdquo;
        </p>
        <footer class="blockquote-footer">Bruce Schneier (June 1, 2016)</footer>
      </blockquote>
    </div>

<jsp:include page="bottom.jsp"/>

