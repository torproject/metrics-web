<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
  <title>Tor Metrics Portal: Papers</title>
  <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">
  <link href="/css/stylesheet-ltr.css" type="text/css" rel="stylesheet">
  <link href="/images/favicon.ico" type="image/x-icon" rel="shortcut icon">
</head>
<body>
  <div class="center">
    <%@ include file="banner.jsp"%>
    <div class="main-column">
        <h2>Tor Metrics Portal: Papers</h2>
        <br>
        <p>The <a href="#papers">papers</a>,
        <a href="#techreports">technical reports</a>, and
        <a href="#blogposts">blog posts</a> listed on this page originate
        from, are based on, or are related to work performed in the Tor
        Metrics Project.</p>
        <br>
        <a name="papers"></a>
        <h3>Papers</h3>
        <br>
        These papers summarize some of the results of of the Tor Metrics
        Project and have been accepted for publication at academic
        conferences or workshops.
        <ul>
          <li>Karsten Loesing, Steven J. Murdoch, Roger Dingledine. A Case
          Study on Measuring Statistical Data in the Tor Anonymity
          Network. Accepted for publication at Workshop on Ethics in
          Computer Security Research (WECSR 2010), Tenerife, Spain,
          January 2010. (<a href="papers/wecsr10.pdf">PDF</a>, 160K)</li>
          <li>Karsten Loesing. Measuring the Tor Network from Public
          Directory Information. 2nd Hot Topics in Privacy Enhancing
          Technologies (HotPETs 2009), Seattle, WA, USA, August 2009.
          (<a href="papers/hotpets09.pdf">PDF</a>, 582K)</li>
        </ul>
        <br>
        <a name="techreports"></a>
        <h3>Technical reports</h3>
        <br>
        The technical reports listed here have been the first place to
        publish novel kinds of statistics on the Tor network. Some, but
        not all, of the results contained in these technical reports have
        been included in the <a href="#papers">papers</a> above or in the
        daily updated <a href="graphs.html">graphs</a>.
        <ul>
          <li>An anomaly-based censorship-detection system for Tor (<a
          href="papers/detector-2011-08-11.pdf">PDF</a>, 106K from August
          11, 2011).</li>
          <li>An Analysis of Tor Relay Stability (<a
          href="papers/relay-stability-2011-06-30.pdf">PDF</a>, 1.3M from
          June 30, 2011,
          <a href="https://gitweb.torproject.org/metrics-tasks.git/tree/HEAD:/task-2911">code</a>,
          <a href="data/running-relays-reverse.csv.bz2">data</a>,
          322M).</li>
          <li>Overview of Statistical Data in the Tor Network
          (<a href="papers/data-2011-03-14.pdf">PDF</a>,
          166K from March 14, 2011).</li>
          <li>Privacy-preserving Ways to Estimate the Number of Tor Users
          (<a href="papers/countingusers-2010-11-30.pdf">PDF</a>,
          307K from November 30, 2010).</li>
          <li>Comparison of GeoIP Databases for Tor
          (<a href="papers/geoipdbcomp-2009-10-23.pdf">PDF</a>,
          546K from October 23, 2009).</li>
          <li>Analysis of Circuit Queues in Tor
          (<a href="papers/bufferstats-2009-08-25.pdf">PDF</a>,
          196K from August 25, 2009).</li>
          <li>Performance of Requests over the Tor Network
          (<a href="papers/torperf-2009-09-22.pdf">PDF</a>,
          2.8M from September 22, 2009).</li>
          <li>Reducing the Circuit Window Size in Tor
          (<a href="papers/circwindow-2009-09-20.pdf">PDF</a>,
          137K from September 20, 2009).</li>
          <li>Simulation of the number of Fast, Stable, and Guard flags
          for changed requirements
          (<a href="papers/flagrequirements-2009-04-11.pdf">PDF</a>,
          229K from April 11, 2009).</li>
          <li>Possible problems of directory authorities assigning Stable
          and Guard flags
          (<a href="papers/relayflags-2009-04-01.pdf">PDF</a>,
          2M from April 1, 2009).</li>
          <li>Evaluation of Client Requests to the Directories to
          determine total numbers and countries of users
          (<a href="papers/directory-requests-2009-06-25.pdf">PDF</a>,
          207K, last updated on June 25, 2009).</li>
          <li>Analysis of Bridge Usage in Tor
          (<a href="papers/bridges-2009-06-22.pdf">PDF</a>,
          76K, last updated on June 22, 2009).</li>
          <li>Evaluation of Relays from Public Directory Data
          (<a href="papers/dirarch-2009-06-22.pdf">PDF</a>,
          558K, last updated on June 22, 2009).</li>
        </ul>
        <br>
        <a name="blogposts"></a>
        <h3>Blog posts</h3>
        <br>
        The following blog posts are either the results of metrics
        research or describe new interesting research questions that can
        (partly) be answered with metrics data.
        <ul>
          <li>Research problem: better guard rotation parameters
          (<a href="https://blog.torproject.org/blog/research-problem-better-guard-rotation-parameters">link</a>,
          August 20, 2011).</li>
          <li>Research problem: measuring the safety of the Tor network
          (<a href="https://blog.torproject.org/blog/research-problem-measuring-safety-tor-network">link</a>,
          February 5, 2011).</li>
        </ul>
    </div>
  </div>
  <div class="bottom" id="bottom">
    <%@ include file="footer.jsp"%>
  </div>
</body>
</html>
