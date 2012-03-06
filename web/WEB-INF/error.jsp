<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page isErrorPage="true" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
  <title>Tor Metrics Portal: Error</title>
  <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">
  <link href="/css/stylesheet-ltr.css" type="text/css" rel="stylesheet">
  <link href="/images/favicon.ico" type="image/x-icon" rel="shortcut icon">
</head>
<body>
  <div class="center">
    <%@ include file="banner.jsp"%>
    <div class="main-column">
<h2>Tor Metrics Portal: Error</h2>
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

<p>
Maybe you find what you're looking for on our sitemap:
<ul>
<li><a href="index.html">Home</a></li>
<li><a href="graphs.html">Graphs</a>
<ul>
<li><a href="network.html">Network</a></li>
<li><a href="users.html">Users</a></li>
<li><a href="packages.html">Packages</a></li>
<li><a href="performance.html">Performance</a></li>
</ul></li>
<li><a href="research.html">Research</a>
<ul>
<li><a href="papers.html">Papers</a></li>
<li><a href="data.html">Data</a></li>
<li><a href="formats.html">Formats</a></li>
<li><a href="tools.html">Tools</a></li>
</ul></li>
<li><a href="status.html">Status</a>
<ul>
<li><a href="exonerator.html">ExoneraTor</a></li>
<li><a href="relay-search.html">Relay Search</a></li>
<li><a href="consensus-health.html">Consensus Health</a></li>
</ul></li>
</ul>
</p>

<p>If this problem persists, please
<a href="mailto:tor-assistants@torproject.org">let us know</a>!</p>

    </div>
  </div>
  <div class="bottom" id="bottom">
    <%@ include file="footer.jsp"%>
  </div>
</body>
</html>
