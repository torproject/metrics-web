<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<jsp:include page="top.jsp">
  <jsp:param name="pageTitle" value="${categoryHeader} &ndash; Tor Metrics"/>
  <jsp:param name="navActive" value="${categoryHeader}"/>
</jsp:include>

    <div class="container">
      <ul class="breadcrumb">
        <li><a href="index.html">Home</a></li>
        <li class="active">${categoryHeader}</li>
      </ul>
    </div>

    <div class="container">
      <h1>${categoryHeader}</h1>
      <p>${categoryDescription}</p>
    </div>

    <div class="container">

      <!-- tabs -->
      <ul class="nav nav-tabs">
        <c:forEach var="tab" items="${categoryTabs}">
        <li role="presentation"<c:if test="${id.equals(tab[1])}"> class="active"</c:if>><a href="${tab[1]}.html" data-tab="${tab[1]}">${tab[0]}</a></li>
        </c:forEach>
      </ul>

      <!-- tab-content -->
      <div class="tab-content">
        <div class="tab-pane active" id="tab-${tab[1]}">

          <div class="row">
            <div class="col-md-8">

              <img src="${id}.png${parameters}" width="576" height="360" alt="${title} graph">

              ${description}

            </div>
            <div class="col-md-4">

<form action="${id}.html">
    <c:if test="${fn:length(start) > 0}">
      <p>
        <label for="start">Start date:</label>
        <input type="text" name="start" size="10" value="${start[0]}" placeholder="yyyy-mm-dd" id="start">
      </p>
    </c:if>
    <c:if test="${fn:length(end) > 0}">
      <p>
        <label for="end">End date:</label>
        <input type="text" name="end" size="10" value="${end[0]}" placeholder="yyyy-mm-dd" id="end">
      </p>
    </c:if>
    <c:if test="${fn:length(p) > 0}">
      <p><b>Percentiles:</b>
      <c:forEach var="row" items="${p}">
        <label class="checkbox-label">
          <input type="checkbox" name="p" value="${row[0]}"${row[1]}> ${row[2]}
        </label>
      </c:forEach>
      </p>
    </c:if>
    <c:if test="${fn:length(n) > 0}">
      <p><b>n:</b>
      <c:forEach var="row" items="${n}">
        <label class="checkbox-label">
          <input type="checkbox" name="n" value="${row[0]}"${row[1]}> ${row[0]}
        </label>
      </c:forEach>
      </p>
    </c:if>
    <c:if test="${fn:length(flag) > 0}">
      <p>
        <b>Relay flags:</b>
        <c:forEach var="row" items="${flag}">
        <input type="checkbox" name="flag" value="${row[0]}" id="flag_${row[0]}"${row[1]}>
        <label class="checkbox-label" for="flag_${row[0]}">${row[0]}</label>
        </c:forEach>
      </p>
    </c:if>
    <c:if test="${fn:length(country) > 0}">
      <p>
        <label for="country"><b>Source:</b></label>
        <select name="country" id="country">
        <c:forEach var="row" items="${country}">
          <option value="${row[0]}"${row[1]}>${row[2]}</option>
        </c:forEach>
        </select>
      </p>
    </c:if>
    <c:if test="${fn:length(events) > 0}">
      <p>
        <label for="events"><b>Show possible censorship events if available (<a
               href="http://research.torproject.org/techreports/detector-2011-09-09.pdf">BETA</a>):</b></label>
        <select name="events" id="events">
        <c:forEach var="row" items="${events}">
          <option value="${row[0]}"${row[1]}>${row[2]}</option>
        </c:forEach>
        </select>
      </p>
    </c:if>
    <c:if test="${fn:length(transport) > 0}">
      <p>
        <b>Source:</b>
        <c:forEach var="row" items="${transport}">
        <input type="checkbox" name="transport" value="${row[0]}" id="transport_${row[0]}"${row[1]}>
        <label class="checkbox-label" for="transport_${row[0]}">${row[2]}</label>
        </c:forEach>
      </p>
    </c:if>
    <c:if test="${fn:length(version) > 0}">
      <p>
        <label for="version"><b>Source:</b></label>
        <select name="version" id="version">
        <c:forEach var="row" items="${version}">
          <option value="${row[0]}"${row[1]}>${row[2]}</option>
        </c:forEach>
        </select>
      </p>
    </c:if>
    <c:if test="${fn:length(source) > 0}">
      <p><b>Source:</b>
      <c:forEach var="row" items="${source}">
        <label class="radio-label">
          <input type="radio" name="source" value="${row[0]}"${row[1]}> ${row[0]}
        </label>
      </c:forEach>
      </p>
    </c:if>
    <c:if test="${fn:length(filesize) > 0}">
      <p><b>File size:</b>
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
</form>

<p>Download graph as
<a href="${id}.pdf${parameters}">PDF</a> or
<a href="${id}.svg${parameters}">SVG</a>.</p>
<br>

<c:if test="${fn:length(data) > 0}">
<h4>Underlying data</h4>
<ul>
<c:forEach var="row" items="${data}">
<li><a href="stats.html#${row}"><code>stats/${row}.csv</code></a></li>
</c:forEach>
</ul>
</c:if>

            </div><!-- col-md-4 -->
          </div><!-- row -->
        </div><!-- tab-pane -->
      </div><!-- tab-content -->
    </div><!-- container -->

<jsp:include page="bottom.jsp"/>

