<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
  <title>Tor Metrics Portal: Status</title>
  <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">
  <link href="/css/stylesheet-ltr.css" type="text/css" rel="stylesheet">
  <link href="/images/favicon.ico" type="image/x-icon" rel="shortcut icon">
</head>
<body>
  <div class="center">
    <%@ include file="banner.jsp"%>
    <div class="main-column">
      <h2>Tor Metrics Portal: Network Status</h2>
      <table>
        <tr>
          <th><a href="/networkstatus.html?sort=nickname&order=${sort=='nickname'?order:'desc'}">nickname</a></th>
          <th><a href="/networkstatus.html?sort=bandwidth&order=${sort=='bandwidth'?order:'desc'}">bandwidth</a></th>
          <th><a href="/networkstatus.html?sort=orport&order=${sort=='orport'?order:'desc'}">orport</a></th>
          <th><a href="/networkstatus.html?sort=dirport&order=${sort=='dirport'?order:'desc'}">dirport</a></th>
          <th><a href="/networkstatus.html?sort=isbadexit&order=${sort=='isbadexit'?order:'desc'}">isbadexit</a></th>
          <th><a href="/networkstatus.html?sort=uptime&order=${sort=='uptime'?order:'desc'}">uptime</a></th>
        </tr>
      <c:forEach var="row" items="${status}">
        <tr>
          <td><a href="/routerdetail.html?fingerprint=${row['fingerprint']}&validafter=${row['validafterts']}">${row['nickname']}</a></td>
          <td>${row['bandwidth']}</td>
          <td>${row['orport']}</td>
          <td>${row['dirport']}</td>
          <td>${row['isbadexit']}</td>
          <td>${row['uptime']}</td>
        </tr>
      </c:forEach>
      </table>
    </div>
  </div>
  <div class="bottom" id="bottom">
    <%@ include file="footer.jsp"%>
  </div>
</body>
</html>
