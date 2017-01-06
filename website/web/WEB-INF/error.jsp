<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<jsp:include page="top.jsp">
  <jsp:param name="pageTitle" value="${pageContext.errorData.statusCode} &ndash; Tor Metrics"/>
</jsp:include>

    <div class="container">
      <ul class="breadcrumb">
        <li><a href="/">Home</a></li>
        <li class="active">${pageContext.errorData.statusCode}</li>
      </ul>
    </div>

    <div class="container">
      <h1>Error</h1>

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

      <p>Maybe start over from the <a href="/">home page</a>.</p>

      <p>If this problem persists, please <a href="about.html#contact">let us know</a>!</p>

    </div>

<jsp:include page="bottom.jsp"/>

