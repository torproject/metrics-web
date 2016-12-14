<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
  <title>Tor Metrics</title>
  <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">
  <link href="css/stylesheet-ltr.css" type="text/css" rel="stylesheet">
  <link href="css/bootstrap.min.css" type="text/css" rel="stylesheet">
  <link href="images/favicon.ico" type="image/x-icon" rel="shortcut icon">
</head>
<body>
  <div class="center">
    <div class="main-column">
        <h2><a href="/"><img src="images/metrics-logo.png" width="153" height="200" alt="Metrics logo"><img src="images/metrics-wordmark.png" width="384" height="50" alt="Metrics wordmark"></a></h2>
        <br>

<p>"Tor metrics are the ammunition that lets Tor and other security
advocates argue for a more private and secure Internet from a position
of data, rather than just dogma or perspective."
<i>- Bruce Schneier (June 1, 2016)</i></p>

        <!-- Navigation start -->
        <a href="index.html">Metrics</a> &#124;
        <a href="about.html">About</a> &#124;
        News &#124;
        <a href="tools.html">Tools</a> &#124;
        <a href="research.html">Research</a>
        <br>
        <br>
        <!-- Navigation end -->

<h3>News</h3>
<br>

<p>We collect reports of events and aggregate them here for your convenience.
The process is usually pretty informal.
Someone tells us of an event, reports it to us, and we aggregate them here.
If you know of any event that may have caused a measurement anomaly, help us add
it to this list.</p>

<c:forEach var="category" items="${news}">
<a href="#${category.key[1]}">${category.key[0]}</a> &#124;
</c:forEach>
<br>

<c:forEach var="category" items="${news}">
<a name="${category.key[1]}"></a>
<h3>${category.key[0]}</h3>
<c:forEach var="entry" items="${category.value}">
<p>${entry[0]}</p>
</c:forEach>
</c:forEach>

    </div>
  </div>
  <div class="bottom" id="bottom">
    <%@ include file="footer.jsp"%>
  </div>
</body>
</html>

