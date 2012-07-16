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
          <li>July 16, 2012: Added link to <a
          href="http://tigerpa.ws/tor_metrics/">interactive graphs</a> of
          network statistics.</li>
          <li>April 30, 2012: Added <a
          href="papers/bridge-report-usage-stats-2012-04-30.pdf">tech
          report</a>: "What fraction of our bridges are not reporting
          usage statistics?"</li>
          <li>March 12, 2012: Added
          <a href="papers/bridge-scaling-2012-03-09.pdf">tech report</a>:
          "What if the Tor network had 50,000 bridges?"</li>
          <li>January 17, 2012:
          <a href="https://svn.torproject.org/svn/projects/roadmaps/metrics-roadmap-2012-01-17.pdf">Tor
          Metrics Roadmap</a> published.</li>
        </ul>
    </div>
  </div>
  <div class="bottom" id="bottom">
    <%@ include file="footer.jsp"%>
  </div>
</body>
</html>
