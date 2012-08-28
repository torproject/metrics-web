<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
  <title>Tor Metrics Portal: Fast Exits</title>
  <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">
  <link href="/css/stylesheet-ltr.css" type="text/css" rel="stylesheet">
  <link href="/images/favicon.ico" type="image/x-icon" rel="shortcut icon">
</head>
<body>
  <div class="center">
    <%@ include file="banner.jsp"%>
    <div class="main-column">
<h2>Tor Metrics Portal: Fast Exits</h2>
<br>
<p>This page summarizes progress in operating 125 fast exits, which is a
deliverable for
<a href="https://trac.torproject.org/projects/tor/wiki/org/sponsors/SponsorJ">SponsorJ</a>.</p>
<br>

<a name="fastexits"></a>
<h3><a href="#fastexits" class="anchor">Relays meeting the fast-exit
requirements</a></h3>
<br>
<img src="fast-exits.png${fast_exits_url}"
     width="576" height="432" alt="Fast exits graph">
<form action="fast-exits.html#fastexits">
  <div class="formrow">
    <input type="hidden" name="graph" value="fast-exits">
    <p>
    <label>Start date (yyyy-mm-dd):</label>
      <input type="text" name="start" size="10"
             value="<c:choose><c:when test="${fn:length(fast_exits_start) == 0}">${default_start_date}</c:when><c:otherwise>${fast_exits_start[0]}</c:otherwise></c:choose>">
    <label>End date (yyyy-mm-dd):</label>
      <input type="text" name="end" size="10"
             value="<c:choose><c:when test="${fn:length(fast_exits_end) == 0}">${default_end_date}</c:when><c:otherwise>${fast_exits_end[0]}</c:otherwise></c:choose>">
    </p><p>
      Resolution: <select name="dpi">
        <option value="72"<c:if test="${fast_exits_dpi[0] eq '72'}"> selected</c:if>>Screen - 576x432</option>
        <option value="150"<c:if test="${fast_exits_dpi[0] eq '150'}"> selected</c:if>>Print low - 1200x900</option>
        <option value="300"<c:if test="${fast_exits_dpi[0] eq '300'}"> selected</c:if>>Print high - 2400x1800</option>
      </select>
    </p><p>
    <input class="submit" type="submit" value="Update graph">
    </p>
  </div>
</form>
<br>

<a name="almostfastexits"></a>
<h3><a href="#almostfastexits" class="anchor">Relays almost meeting the
fast-exit requirements</a></h3>
<br>
<img src="almost-fast-exits.png${almost_fast_exits_url}"
     width="576" height="432" alt="Almost fast exits graph">
<form action="fast-exits.html#almostfastexits">
  <div class="formrow">
    <input type="hidden" name="graph" value="almost-fast-exits">
    <p>
    <label>Start date (yyyy-mm-dd):</label>
      <input type="text" name="start" size="10"
             value="<c:choose><c:when test="${fn:length(almost_fast_exits_start) == 0}">${default_start_date}</c:when><c:otherwise>${almost_fast_exits_start[0]}</c:otherwise></c:choose>">
    <label>End date (yyyy-mm-dd):</label>
      <input type="text" name="end" size="10"
             value="<c:choose><c:when test="${fn:length(almost_fast_exits_end) == 0}">${default_end_date}</c:when><c:otherwise>${almost_fast_exits_end[0]}</c:otherwise></c:choose>">
    </p><p>
      Resolution: <select name="dpi">
        <option value="72"<c:if test="${almost_fast_exits_dpi[0] eq '72'}"> selected</c:if>>Screen - 576x432</option>
        <option value="150"<c:if test="${almost_fast_exits_dpi[0] eq '150'}"> selected</c:if>>Print low - 1200x900</option>
        <option value="300"<c:if test="${almost_fast_exits_dpi[0] eq '300'}"> selected</c:if>>Print high - 2400x1800</option>
      </select>
    </p><p>
    <input class="submit" type="submit" value="Update graph">
    </p>
  </div>
</form>
<br>

    </div>
  </div>
  <div class="bottom" id="bottom">
    <%@ include file="footer.jsp"%>
  </div>
</body>
</html>
