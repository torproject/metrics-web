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
        Metrics &#124;
        <a href="about.html">About</a> &#124;
        <a href="news.html">News</a> &#124;
        <a href="tools.html">Tools</a> &#124;
        <a href="research.html">Research</a>
        <br>
        <br>
        <!-- Navigation end -->

        <h1>Welcome!</h1>
        <p>What would you like to know about the Tor network?</p>

<div>

<c:forEach var="category" items="${categories}">
<c:if test="${fn:length(category[0]) > 0}"><a href="${category[0]}.html"></c:if><h2>${category[1]}</h2><c:if test="${fn:length(category[0]) > 0}"></a></c:if>
<p>${category[2]}</p>
</c:forEach>
<br>

</div>

<p>Let us know if we're missing anything, or if we should measure something
else.</p>

    </div>
  </div>
  <div class="bottom" id="bottom">
    <%@ include file="footer.jsp"%>
  </div>
</body>
</html>

