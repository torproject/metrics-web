<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
  <title>Tor Metrics &mdash; ${title}</title>
  <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">
  <link href="css/stylesheet-ltr.css" type="text/css" rel="stylesheet">
  <link href="images/favicon.ico" type="image/x-icon" rel="shortcut icon">
</head>
<body>
  <div class="center">
    <div class="main-column">

<h2><a href="."><img src="images/metrics-wordmark-small.png" width="138" height="18" alt="Metrics wordmark"></a> &mdash; ${title}</h2>
<br>
${description}
<form action="${id}.html">
  <div class="formrow">
    <p>
    <label>Start date (yyyy-mm-dd):
      <input type="text" name="start" size="10" value="${start[0]}">
    </label>
    </p><p>
    <label>End date (yyyy-mm-dd):
      <input type="text" name="end" size="10" value="${end[0]}">
    </label>
    </p><p>
    <input class="submit" type="submit" value="Update table">
    </p>
  </div>
</form>
<br>
<table>
  <tr>
  <c:forEach var="row" items="${tableheader}">
    <th>${row}</th>
  </c:forEach>
  </tr>
  <c:forEach var="row" items="${tabledata}">
    <tr>
    <c:forEach var="col" items="${row}">
      <td>${col}</td>
    </c:forEach>
    </tr>
  </c:forEach>
</table>
<p>

<c:if test="${fn:length(data) > 0}">
<h4>Underlying data</h4>
<ul>
<c:forEach var="row" items="${data}">
<li><a href="${row[0]}">${row[1]}</a></li>
</c:forEach>
</ul>
</c:if>

<c:if test="${fn:length(related) > 0}">
<h4>Related metrics</h4>
<ul>
<c:forEach var="row" items="${related}">
<li><a href="${row[0]}">${row[1]}</a></li>
</c:forEach>
</ul>
</c:if>

    </div>
  </div>
  <div class="bottom" id="bottom">
    <%@ include file="footer.jsp"%>
  </div>
</body>
</html>
