<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
  <title>Tor Metrics: Tor users as percentage of larger Internet population</title>
  <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">
  <link href="/css/stylesheet-ltr.css" type="text/css" rel="stylesheet">
  <link href="/images/favicon.ico" type="image/x-icon" rel="shortcut icon">
</head>
<body>
  <div class="center">
    <%@ include file="banner.jsp"%>
    <div class="main-column">

<h3>Tor Metrics: Tor users as percentage of larger Internet population</h3>
<br>
<p>The Oxford Internet Institute made a cartogram visualization of Tor
users as compared to the overall Internet population per country.
They used the average number of Tor users from August 2012 to August 2013
and put it in relation to total Internet users per country.
Be sure to read up the
<a href="http://geography.oii.ox.ac.uk/?page=tor">details and their
conclusions</a>.</p>

<a href="http://geography.oii.ox.ac.uk/?page=tor">
<img src="images/oxford-anonymous-internet.png"
     alt="The anonymous Internet">
</a>

    </div>
  </div>
  <div class="bottom" id="bottom">
    <%@ include file="footer.jsp"%>
  </div>
</body>
</html>
