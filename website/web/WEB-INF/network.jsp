<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
  <title>Tor Metrics: Servers</title>
  <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">
  <link href="/css/stylesheet-ltr.css" type="text/css" rel="stylesheet">
  <link href="/images/favicon.ico" type="image/x-icon" rel="shortcut icon">
</head>
<body>
  <div class="center">
    <%@ include file="banner.jsp"%>
    <div class="main-column">
<h2>Tor Metrics: Servers</h2>
<br>

<p>The graphs on this page have moved to their own subpages.
This is part of an effort to make navigation on this website more intuitive.
In the future, this page will go away, and all graphs/tables can be searched more easily from the start page.
Sorry for any inconvenience caused by this.</p>

<ul>
<li><a href="networksize.html">Graph: Relays and bridges in the network</a></li>
<li><a href="relayflags.html">Graph: Relays with Exit, Fast, Guard, Stable, and HSDir flags</a></li>
<li><a href="versions.html">Graph: Relays by version</a></li>
<li><a href="platforms.html">Graph: Relays by platform</a></li>
<li><a href="cloudbridges.html">Graph: Tor Cloud bridges</a></li>
<li><a href="servers-data.html">Data: Number of relays and bridges</a></li>
</ul>

    </div>
  </div>
  <div class="bottom" id="bottom">
    <%@ include file="footer.jsp"%>
  </div>
</body>
</html>
