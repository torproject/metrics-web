<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
  <title>Tor Metrics Portal: Tools</title>
  <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">
  <link href="/css/stylesheet-ltr.css" type="text/css" rel="stylesheet">
  <link href="/images/favicon.ico" type="image/x-icon" rel="shortcut icon">
</head>
<body>
  <div class="center">
    <%@ include file="banner.jsp"%>
    <div class="main-column">
        <h2>Tor Metrics Portal: Status</h2>
        <br>
        <p>The network data collected by the Tor Metrics Project can be
        used to analyze the Tor network status from a few years ago until
        an hour ago. There are currently two applications for this data:
        The <a href="exonerator.html">ExoneraTor</a> tells you whether
        some IP address was a Tor relay at a given time, the
        <a href="relay-search.html">Relay Search</a> lets you search the
        descriptor archive for a relay, and the
        <a href="consensus-health.html">Consensus Health</a> summarizes
        information about the latest network consensus voting process.
    </div>
  </div>
  <div class="bottom" id="bottom">
    <%@ include file="footer.jsp"%>
  </div>
</body>
</html>
