<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<jsp:include page="top.jsp">
  <jsp:param name="pageTitle" value="News &ndash; Tor Metrics"/>
  <jsp:param name="navActive" value="News"/>
</jsp:include>

    <div class="container">
      <ul class="breadcrumb">
        <li><a href="index.html">Home</a></li>
        <li class="active">News</li>
      </ul>
    </div>

    <div class="container">
      <h1>News</h1>
      <p>We collect reports of events and aggregate them here for your convenience.  The process is usually pretty informal.  Someone tells us of an event, reports it to us, and we aggregate them here.  If you know of any event that may have caused a measurement anomaly, help us add it to this list.</p>
    </div>
    <div class="container">

    <c:forEach var="category" items="${news}" varStatus="status">
      <c:if test="${not status.first}"><hr></c:if>
      <a name="#${category.key[1]}" id="anchor-${category.key[1]}"></a>
      <h2>${category.key[0]}</h2>
      <c:forEach var="entry" items="${category.value}">
      <p>${entry[0]}</p>
      <br>
      </c:forEach>
    </c:forEach>

    </div>

<jsp:include page="bottom.jsp"/>

