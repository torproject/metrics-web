<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
  <title>Tor Metrics: Tor Cloud bridges</title>
  <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">
  <link href="/css/stylesheet-ltr.css" type="text/css" rel="stylesheet">
  <link href="/images/favicon.ico" type="image/x-icon" rel="shortcut icon">
</head>
<body>
  <div class="center">
    <%@ include file="banner.jsp"%>
    <div class="main-column">

<a name="cloudbridges"></a>
<h3>Tor Metrics: Tor Cloud bridges</h3>
<br>
<p>The following graph shows the number of running
<a href="about.html#bridge">bridges</a> that are deployed in
<a href="http://aws.amazon.com/ec2/">Amazon's EC2 cloud</a> using images
provided by <a href="https://cloud.torproject.org/">Tor Cloud</a>.
These bridges have, by default, a nickname that starts with "ec2bridger"
which bridge operators usually leave unchanged.</p>
<img src="cloudbridges.png${cloudbridges_url}"
     width="576" height="360" alt="Tor Cloud bridges graph">
<form action="cloudbridges.html">
  <div class="formrow">
    <input type="hidden" name="graph" value="cloudbridges">
    <p>
    <label>Start date (yyyy-mm-dd):</label>
      <input type="text" name="start" size="10"
             value="<c:choose><c:when test="${fn:length(cloudbridges_start) == 0}">${default_start_date}</c:when><c:otherwise>${cloudbridges_start[0]}</c:otherwise></c:choose>">
    <label>End date (yyyy-mm-dd):</label>
      <input type="text" name="end" size="10"
             value="<c:choose><c:when test="${fn:length(cloudbridges_end) == 0}">${default_end_date}</c:when><c:otherwise>${cloudbridges_end[0]}</c:otherwise></c:choose>">
    </p><p>
    <input class="submit" type="submit" value="Update graph">
    </p>
  </div>
</form>
<p>Download graph as
<a href="cloudbridges.pdf${cloudbridges_url}">PDF</a> or
<a href="cloudbridges.svg${cloudbridges_url}">SVG</a>.</p>
<br>

<h4>Related metrics</h4>
<ul>
<li><a href="networksize.html">Graph: Relays and bridges in the network</a></li>
<li><a href="servers-data.html">Data: Number of relays and bridges</a></li>
</ul>

    </div>
  </div>
  <div class="bottom" id="bottom">
    <%@ include file="footer.jsp"%>
  </div>
</body>
</html>
