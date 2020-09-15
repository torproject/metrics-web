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
      <div class="pull-right">
        <a href="/news.atom" title="Subscribe">
          <i class="fa fa-2x fa-rss-square"></i>
        </a>
      </div>
      <h1>News <a href="#news" name="news" class="anchor">#</a></h1>
      <p>You're a journalist or more generally a person who wants to know what's going on in the Tor network?  We're collecting unusual events in the Tor network together with any insights we have into what we think has happened in the <a href="https://gitlab.torproject.org/tpo/metrics/timeline" target="_blank">metrics-timeline Git repository</a>.</p>
    </div>

    <c:forEach var="category" items="${news}" varStatus="status">
    <div class="container">
      <h2>${category.key[0]} <a href="#${category.key[1]}" name="${category.key[1]}" class="anchor">#</a></h2>
      <table class="table events">
        <thead>
          <tr>
            <th class="dates">Dates</th>
            <th class="tags">Places/Protocols</th>
            <th class="description">Description and Links</th>
          </tr>
        </thead>
        <tbody>
          <c:forEach var="entry" items="${category.value}">
            <%@ include file="news-item.jsp" %>
          </c:forEach>
        </tbody>
      </table>
    </div>
    </c:forEach>

<jsp:include page="bottom.jsp"/>

