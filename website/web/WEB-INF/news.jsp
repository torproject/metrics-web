<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<jsp:include page="top.jsp">
  <jsp:param name="pageTitle" value="News &ndash; Tor Metrics"/>
  <jsp:param name="navActive" value="News"/>
</jsp:include>

    <div class="container">
      <ul class="breadcrumb">
        <li><a href="/">Home</a></li>
        <li class="active">News</li>
      </ul>
    </div>

    <div class="container">
      <h1>News <a href="#news" name="news" class="anchor">#</a></h1>
      <p>You're a journalist or more generally a person who wants to know what's going on in the Tor network?  We're collecting unusual events in the Tor network together with any insights we have into what we think has happened.</p>
    </div>

    <c:forEach var="category" items="${news}" varStatus="status">
    <div class="container">
      <h2>${category.key[0]} <a href="#${category.key[1]}" name="${category.key[1]}" class="anchor">#</a></h2>
      <c:forEach var="entry" items="${category.value}">
      <p>${entry[0]}<br></p>
      </c:forEach>
    </div>
    </c:forEach>

<jsp:include page="bottom.jsp"/>

