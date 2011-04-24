<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
  <title>Tor Metrics Portal: Packages</title>
  <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">
  <link href="/css/stylesheet-ltr.css" type="text/css" rel="stylesheet">
  <link href="/images/favicon.ico" type="image/x-icon" rel="shortcut icon">
</head>
<body>
  <div class="center">
    <%@ include file="banner.jsp"%>
    <div class="main-column">
<h2>Tor Metrics Portal: Packages</h2>
<br>
<h3>Packages requested from GetTor</h3>
<br>
<p>GetTor allows users to fetch the Tor software via email. The following
graph shows the number of packages requested from GetTor per day.</p>
<p>
<a name="gettor"></a>
<img src="gettor.png${gettor_url}"
     width="576" height="360" alt="GetTor graph">
<form action="packages.html#gettor">
  <div class="formrow">
    <input type="hidden" name="graph" value="gettor">
    <p>
    <label>Start date (yyyy-mm-dd):</label>
      <input type="text" name="start" size="10"
             value="<c:choose><c:when test="${fn:length(gettor_start) == 0}">${default_start_date}</c:when><c:otherwise>${gettor_start[0]}</c:otherwise></c:choose>">
    <label>End date (yyyy-mm-dd):</label>
      <input type="text" name="end" size="10"
             value="<c:choose><c:when test="${fn:length(gettor_end) == 0}">${default_end_date}</c:when><c:otherwise>${gettor_end[0]}</c:otherwise></c:choose>">
    </p><p>
      Packages:
      <input type="radio" name="bundle" value="all" <c:if test="${fn:length(gettor_bundle) == 0 or gettor_bundle[0] eq 'all'}"> checked</c:if>> Total packages
      <input type="radio" name="bundle" value="en" <c:if test="${gettor_bundle[0] eq 'en'}"> checked</c:if>> TBB (en)
      <input type="radio" name="bundle" value="zh_CN" <c:if test="${gettor_bundle[0] eq 'zh_CN'}"> checked</c:if>> TBB (zh_CN)
      <input type="radio" name="bundle" value="fa" <c:if test="${gettor_bundle[0] eq 'fa'}"> checked</c:if>> TBB (fa)
    </p><p>
      Resolution: <select name="dpi">
        <option value="72"<c:if test="${gettor_dpi[0] eq '72'}"> selected</c:if>>Screen - 576x360</option>
        <option value="150"<c:if test="${gettor_dpi[0] eq '150'}"> selected</c:if>>Print low - 1200x750</option>
        <option value="300"<c:if test="${gettor_dpi[0] eq '300'}"> selected</c:if>>Print high - 2400x1500</option>
      </select>
    </p><p>
    <input class="submit" type="submit" value="Update graph">
    </p>
  </div>
</form>

<p><a href="csv/gettor.csv">CSV</a> file containing all data.</p>
<br>
    </div>
  </div>
  <div class="bottom" id="bottom">
    <%@ include file="footer.jsp"%>
  </div>
</body>
</html>
