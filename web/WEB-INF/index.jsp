<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
  <title>Tor Metrics Portal</title>
  <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">
  <link href="/css/stylesheet-ltr.css" type="text/css" rel="stylesheet">
  <link href="/images/favicon.ico" type="image/x-icon" rel="shortcut icon">
</head>
<body>
  <div class="center">
    <%@ include file="banner.jsp"%>
    <div class="main-column">
        <h2>Tor Metrics Portal</h2>
        <br>
        <p>The Tor Metrics Portal aggregates all kinds of interesting
        data about the Tor network and visualizes them in graphs and
        reports. This portal also provides easy access to the underlying
        data and documentation for performing own analyses based on these
        data. Find out more here:</p>
        <ul>
          <li>View daily updated <a href="graphs.html">graphs</a> on
          estimated client numbers, on network performance, and other
          statistics on the Tor network</li>
          <li>Read <a href="papers.html">papers</a> and technical reports
          on the measurement techniques and results of statistical
          analysis of metrics data</li>
          <li>Download the <a href="data.html">data</a> that is behind the
          graphs and reports to make your own evaluations</li>
          <li>Try out the <a href="tools.html">tools</a> to parse and
          evaluate the metrics data</li>
        </ul>
        <br>
        <h3>News</h3>
        <ul>
          <li>January 27, 2011: New <a href="performance.html">Torperf</a>
          graphs combining the download times of all sources and showing
          the fraction of timeouts and failures are now available.</li>
          <li>December 29, 2010: Tech report with an
          <a href="papers/data-2010-12-29.pdf">Overview of Statistical
          Data in the Tor Network</a> is available for download on the
          <a href="papers.html">Papers</a> page.</li>
          <li>December 16, 2010: Graph and raw data on
          <a href="performance.html">Fraction of connections used
          uni-/bidirectionally</a> is available.</li>
          <li>November 30, 2010: Tech report on
          <a href="papers/countingusers-2010-11-30.pdf">Privacy-preserving
          Ways to Estimate the Number of Tor Users</a> is available for
          download on the <a href="papers.html">Papers</a> page.
          <li>October 7, 2010: Custom graphs are now available for all
          <a href="graphs.html">graphs</a>. Based on work by Kevin
          Berry.</li>
          <li>September 9, 2010: Custom
          graphs on network size, relay platforms, versions, and
          observed bandwidth available. Implemented by Kevin Berry.</li>
          <li>September 2, 2010: New <a href="relay-search.html">relay
          search</a> feature available.</li>
        </ul>
    </div>
  </div>
  <div class="bottom" id="bottom">
    <%@ include file="footer.jsp"%>
  </div>
</body>
</html>
