<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
  <title>Tor Metrics Portal: ExoneraTor</title>
  <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">
  <link href="/css/stylesheet-ltr.css" type="text/css" rel="stylesheet">
  <link href="/images/favicon.ico" type="image/x-icon" rel="shortcut icon">
</head>
<body>
  <div class="center">
    <%@ include file="banner.jsp"%>
  <div class="main-column" style="margin:5; Padding:0;">
    <h2>ExoneraTor</h2>
    <h3>or: a website that tells you whether a given IP address was a Tor
    relay</h3>
    <br>
    <p>Just because you see an Internet connection from a particular IP
    address does not mean you know <i>who</i> originated the traffic. Tor
    anonymizes Internet traffic by "<a href="https://www.torproject.org/about/overview#thesolution">onion
    routing</a>," sending packets through a series of encrypted hops
    before they reach their destination. Therefore, if you see traffic
    from a Tor node, you may be seeing traffic that originated from
    someone using Tor, rather than from the node operator itself. The Tor
    Project and Tor node operators have no records of the traffic that
    passes over the network, but we do maintain current and historical
    records of which IP addresses are part of the Tor network.</p>
    <br>
    <p>ExoneraTor tells you whether there was a Tor relay running on a
    given IP address at a given time. ExoneraTor can further indicate
    whether this relay permitted exiting to a given server and/or TCP
    port. ExoneraTor learns these facts by parsing the public relay lists
    and relay descriptors that are collected from the Tor directory
    authorities and the exit lists collected by TorDNSEL. By inputting an
    IP address and time, you can determine whether that IP was then a part
    of the Tor network.</p>
    <br>
    <p><font color="red"><b>Notice:</b> This service has moved to:
    <a href="https://exonerator.torproject.org/">https://exonerator.torproject.org/</a></font></p>
  </div>
  </div>
  <div class="bottom" id="bottom">
    <%@ include file="footer.jsp"%>
  </div>
</body>
</html>
