<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
  <title>Tor Metrics: Performance</title>
  <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">
  <link href="/css/stylesheet-ltr.css" type="text/css" rel="stylesheet">
  <link href="/images/favicon.ico" type="image/x-icon" rel="shortcut icon">
</head>
<body>
  <div class="center">
    <%@ include file="banner.jsp"%>
    <div class="main-column">

<h2>Tor Metrics: Performance</h2>
<br>
<p>The graphs on this page have moved to their own subpages.
This is part of an effort to make navigation on this website more intuitive.
In the future, this page will go away, and all graphs/tables can be searched more easily from the start page.
Sorry for any inconvenience caused by this.</p>

<ul>
<li><a href="torperf.html">Graph: Time to download files over Tor</a></li>
<li><a href="torperf-failures.html">Graph: Timeouts and failures of downloading files over Tor</a></li>
<li><a href="connbidirect.html">Graph: Fraction of connections used uni-/bidirectionally</a></li>
</ul>

    </div>
  </div>
  <div class="bottom" id="bottom">
    <%@ include file="footer.jsp"%>
  </div>
</body>
</html>
