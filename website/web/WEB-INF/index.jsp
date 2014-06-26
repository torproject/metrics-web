<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
  <title>Tor Metrics</title>
  <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">
  <link href="/css/stylesheet-ltr.css" type="text/css" rel="stylesheet">
  <link href="/images/favicon.ico" type="image/x-icon" rel="shortcut icon">
</head>
<body>
  <div class="center">
    <%@ include file="banner.jsp"%>
    <div class="main-column">
        <h2>Tor Metrics</h2>
        <br>
        <p>Welcome to Tor Metrics, the primary place to learn interesting
        facts about the Tor network, the largest deployed anonymity
        network to date.
        If something can be measured safely, you'll find it here.*</p>
        <p><small>*And if you come across something that is missing here,
        please let us know.</small></p>

        <a href="network.html"><h3>Servers</h3></a>
        <p>How many relays and bridges are in the network?
        How many of them permit exiting?</p>

        <a href="bandwidth.html"><h3>Bandwidth</h3></a>
        <p>How much bandwidth do relays advertise?
        And how much of that is actually consumed?</p>

        <a href="bubbles.html"><h3>Diversity</h3></a>
        <p>How diverse is the network?
        In which countries are relays located?</p>

        <a href="users.html"><h3>Users</h3></a>
        <p>Where do users come from?
        What transports and IP versions are they using?</p>

        <a href="performance.html"><h3>Performance</h3></a>
        <p>How long does it take to download a megabyte of data over Tor?
        How about five?</p>
    </div>
  </div>
  <div class="bottom" id="bottom">
    <%@ include file="footer.jsp"%>
  </div>
</body>
</html>
