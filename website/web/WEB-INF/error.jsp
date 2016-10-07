<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page isErrorPage="true" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
  <title>Tor Metrics &mdash; Error</title>
  <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">
  <link href="css/stylesheet-ltr.css" type="text/css" rel="stylesheet">
  <link href="images/favicon.ico" type="image/x-icon" rel="shortcut icon">
</head>
<body>
  <div class="center">
    <div class="main-column">
<h2><a href="/"><img src="images/metrics-wordmark-small.png" width="138" height="18" alt="Metrics wordmark"></a> &mdash; Error</h2>
<br>
<p>
Oops! Something went wrong here! We encountered a
<b>
<c:choose>
<c:when test="${pageContext.errorData.statusCode eq 400}">
400 Bad Request
</c:when>
<c:when test="${pageContext.errorData.statusCode eq 404}">
404 Not Found
</c:when>
<c:when test="${pageContext.errorData.statusCode eq 500}">
500 Internal Server Error
</c:when>
<c:when test="${not empty pageContext.errorData.throwable}">
${pageContext.exception}
</c:when>
<c:otherwise>
Unknown Error
</c:otherwise>
</c:choose>
</b>
when processing your request!</p>

<p>Maybe start over from the <a href="/">home page</a>.</p>

<p>If this problem persists, please
<a href="https://www.torproject.org/about/contact">let us know</a>!</p>

    </div>
  </div>
  <div class="bottom" id="bottom">
    <%@ include file="footer.jsp"%>
  </div>
</body>
</html>
