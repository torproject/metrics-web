<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<jsp:include page="top.jsp">
  <jsp:param name="pageTitle" value="Welcome to Tor Metrics"/>
  <jsp:param name="navActive" value="Home"/>
</jsp:include>

    <!-- empty breadcrumb, just to keep everything in line... -->
    <div class="container">
      <ul class="breadcrumb">
        <li class="active">&nbsp;</li>
      </ul>
    </div>

  <div class="container">
    <h1>Welcome!</h1>
    <p>
      What would you like to know about the <a href="https://www.torproject.org/" target="_blank">Tor</a> network?
    </p>
  </div>

  <div class="container dashboard">
    <div class="row">
      <c:forEach var="category" items="${categories}">
      <div class="col-sm-4">
        <a<c:if test="${fn:length(category[0]) > 0}"> href="${category[0]}.html"</c:if>><i class="fa ${category[3]} fa-fw fa-4x" aria-hidden="true"></i> <h2>${category[1]}</h2> <p>${category[2]}</p></a>
      </div>
      </c:forEach>
    </div>
  </div>

  <div class="container">
    <p><a href="about.html#contact">Let us know</a> if we're missing anything, or if we should measure something else.</p>
  </div>

<jsp:include page="bottom.jsp"/>

