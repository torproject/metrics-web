<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
  <title>Tor Metrics &mdash; Data flow in the Tor network</title>
  <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">
  <link href="/css/stylesheet-ltr.css" type="text/css" rel="stylesheet">
  <link href="/images/favicon.ico" type="image/x-icon" rel="shortcut icon">
</head>
<body>
  <div class="center">
    <%@ include file="banner.jsp"%>
    <div class="main-column">

<h2><a href="/">Tor Metrics</a> &mdash; Data flow in the Tor network</h2>
<br>
<p>Uncharted made a visualization of data flow in the Tor network where
they place each <a href="about.html#relay">relay</a> on a world map and
illustrate traffic exchanged between relays as animated dots. More details
can be found on the <a
href="https://torflow.uncharted.software/">Uncharted website</a>.</p>

<a href="https://torflow.uncharted.software/">
<img src="images/uncharted-data-flow.png"
     alt="Data flow in the Tor network">
</a>

    </div>
  </div>
  <div class="bottom" id="bottom">
    <%@ include file="footer.jsp"%>
  </div>
</body>
</html>
