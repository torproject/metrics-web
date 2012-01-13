<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
  <title>Tor Metrics Portal: Relay Search</title>
  <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">
  <link href="/css/stylesheet-ltr.css" type="text/css" rel="stylesheet">
  <link href="/images/favicon.ico" type="image/x-icon" rel="shortcut icon">
</head>
<body>
  <div class="center">
    <%@ include file="banner.jsp"%>
    <div class="main-column">
      <h2>Tor Metrics Portal: Relay Search</h2>
      <p>Search for a relay in the relay descriptor archive by typing
      (part of) a <b>nickname</b>, <b>fingerprint</b>, or <b>IP
      address</b> and optionally a <b>month (yyyy-mm)</b> or up to three
      <b>days (yyyy-mm-dd)</b> in the following search field and
      clicking Search. The search will stop after 30 hits or, unless you
      provide a month or a day, after parsing the last 30 days of relay
      lists.</p>
      <br>
      <form action="relay-search.html">
        <table>
          <tr>
            <td><input type="text" name="search"
                       value="<c:out value="${param.search}"/>"></td>
            <td><input type="submit" value="Search"></td>
          </tr>
        </table>
      </form>
      <br>
      <c:if test="${not empty invalidQuery}">
        <p>Sorry, I didn't understand your query. Please provide a
        nickname (e.g., "gabelmoo"), at least the first 8 hex characters
        of a fingerprint prefixed by $ (e.g., "$F2044413"), or at least
        the first two octets of an IPv4 address in dotted-decimal notation
        (e.g., "80.190"). You can also provide at most three months or
        days in ISO 8601 format (e.g., "2010-09" or "2010-09-17").</p>
      </c:if>
      <c:if test="${not empty outsideInterval}">
        <p>${outsideInterval}</p>
      </c:if>
      <c:if test="${not empty searchNotice}">
        <p>${searchNotice}</p>
      </c:if>
      <c:if test="${not empty query}">
        <!-- ${query} -->
      </c:if>
      <c:if test="${not empty queryTime}">
        <c:forEach var="consensus" items="${foundDescriptors}">
          ${rawValidAfterLines[consensus.key]}
          <c:forEach var="statusentry" items="${consensus.value}">
            ${rawStatusEntries[statusentry]}
          </c:forEach>
          <br>
        </c:forEach>
        <p>Found
        <c:choose>
          <c:when test="${matches > 30}">
            more than 30 relays (displaying only those in the last
            consensuses)
          </c:when>
          <c:otherwise>
            ${matches} relays
          </c:otherwise>
        </c:choose>
        in <fmt:formatNumber value="${queryTime / 1000}" pattern="#.###"/>
        seconds.</p>
        <c:if test="${queryTime > 10000}">
          <p>In theory, search time should not exceed 10 seconds. The
          query was '${query}'. If this or similar searches remain slow,
          please <a href="mailto:tor-assistants@torproject.org">let us
          know</a>!</p>
        </c:if>
      </c:if>
    </div>
  </div>
  <div class="bottom" id="bottom">
    <%@ include file="footer.jsp"%>
  </div>
</body>
</html>

