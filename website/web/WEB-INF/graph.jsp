<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
  <title>Tor Metrics &mdash; ${title}</title>
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
        <a href="sources.html">Sources</a> &#124;
        <a href="tools.html">Tools</a> &#124;
        <a href="research.html">Research</a>
        <br>
        <br>
        <!-- Navigation end -->

<c:forEach var="category" items="${categories}"><c:if test="${fn:length(category[0]) > 0}"><a href="${category[0]}.html"></c:if>${category[1]}<c:if test="${fn:length(category[0]) > 0}"></a></c:if> &#124;
</c:forEach>
<br>

<h2>${categoryHeader}</h2>

<p>${categoryDescription}</p>

<c:forEach var="tab" items="${categoryTabs}">
<c:if test="${fn:length(tab[1]) > 0}"><a href="${tab[1]}.html"></c:if>${tab[0]}<c:if test="${fn:length(tab[1]) > 0}"></a></c:if> &#124;
</c:forEach>
<br>

<br>
${description}
<img src="${id}.png${parameters}"
     width="576" height="360" alt="${title} graph">
<form action="${id}.html">
  <div class="formrow">
    <c:if test="${fn:length(start) > 0}">
      <p>
        <label>Start date (yyyy-mm-dd):
          <input type="text" name="start" size="10" value="${start[0]}">
        </label>
      </p>
    </c:if>
    <c:if test="${fn:length(end) > 0}">
      <p>
        <label>End date (yyyy-mm-dd):
          <input type="text" name="end" size="10" value="${end[0]}">
        </label>
      </p>
    </c:if>
    <c:if test="${fn:length(p) > 0}">
      <p>Percentiles:
      <c:forEach var="row" items="${p}">
        <label class="checkbox-label">
          <input type="checkbox" name="p" value="${row[0]}"${row[1]}> ${row[2]}
        </label>
      </c:forEach>
      </p>
    </c:if>
    <c:if test="${fn:length(n) > 0}">
      <p>n:
      <c:forEach var="row" items="${n}">
        <label class="checkbox-label">
          <input type="checkbox" name="n" value="${row[0]}"${row[1]}> ${row[0]}
        </label>
      </c:forEach>
      </p>
    </c:if>
    <c:if test="${fn:length(flag) > 0}">
      <p>Relay flags:
      <c:forEach var="row" items="${flag}">
        <label class="checkbox-label">
          <input type="checkbox" name="flag" value="${row[0]}"${row[1]}> ${row[0]}
        </label>
      </c:forEach>
      </p>
    </c:if>
    <c:if test="${fn:length(country) > 0}">
      <p>
        <label>Source: <select name="country">
        <c:forEach var="row" items="${country}">
          <option value="${row[0]}"${row[1]}>${row[2]}</option>
        </c:forEach>
        </select>
        </label>
      </p>
    </c:if>
    <c:if test="${fn:length(events) > 0}">
      <p>
        <label>Show possible censorship events if available (<a
               href="http://research.torproject.org/techreports/detector-2011-09-09.pdf">BETA</a>)
          <select name="events">
          <c:forEach var="row" items="${events}">
            <option value="${row[0]}"${row[1]}>${row[2]}</option>
          </c:forEach>
          </select>
        </label>
      </p>
    </c:if>
    <c:if test="${fn:length(transport) > 0}">
      <p>Source:
        <c:forEach var="row" items="${transport}">
        <label class="checkbox-label">
          <input type="checkbox" name="transport" value="${row[0]}"${row[1]}> ${row[2]}
        </label>
        </c:forEach>
      </p>
    </c:if>
    <c:if test="${fn:length(version) > 0}">
      <p>
        <label>Source:
          <select name="version">
          <c:forEach var="row" items="${version}">
            <option value="${row[0]}"${row[1]}>${row[2]}</option>
          </c:forEach>
          </select>
        </label>
      </p>
    </c:if>
    <c:if test="${fn:length(source) > 0}">
      <p>Source:
      <c:forEach var="row" items="${source}">
        <label class="radio-label">
          <input type="radio" name="source" value="${row[0]}"${row[1]}> ${row[0]}
        </label>
      </c:forEach>
      </p>
    </c:if>
    <c:if test="${fn:length(filesize) > 0}">
      <p>File size:
      <c:forEach var="row" items="${filesize}">
        <label class="radio-label">
          <input type="radio" name="filesize" value="${row[0]}"${row[1]}> ${row[2]}
        </label>
      </c:forEach>
      </p>
    </c:if>
    <p>
    <input class="submit" type="submit" value="Update graph">
    </p>
  </div>
</form>

<p>Download graph as
<a href="${id}.pdf${parameters}">PDF</a> or
<a href="${id}.svg${parameters}">SVG</a>.</p>
<br>

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

