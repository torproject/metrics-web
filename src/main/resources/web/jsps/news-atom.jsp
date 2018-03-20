<%@ page contentType="text/xml" %><?xml version="1.0"?>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<feed xmlns="http://www.w3.org/2005/Atom">
  <title>Tor Project</title>
  <subtitle>Metrics Timeline</subtitle>
  <updated><c:out value="${updated}"/>T12:00:00Z</updated>
  <link href="https://metrics.torproject.org/news.atom" rel="self" />
  <link href="https://metrics.torproject.org/news.html" />
  <id>https://metrics.torproject.org/news.atom</id>
  <c:forEach var="entry" items="${news}">
    <entry>
      <title><c:out value="${entry.shortDescription}"/></title>
      <updated><c:out value="${entry.start}"/>T12:00:00Z</updated>
      <id>https://metrics.torproject.org/news.atom#<c:out value="${entry.start}"/><c:out value="${fn:substringBefore(entry.shortDescription, ' ')}"/>${fn:length(entry.shortDescription)}</id>
      <content type="xhtml">
        <div xmlns="http://www.w3.org/1999/xhtml">
          <dl>
            <dt>Dates</dt>
            <dd>
              <c:choose>
                <c:when test="${empty entry.start}">
                  N/A
                </c:when>
                <c:when test="${entry.ongoing}">
                  <c:out value="${entry.start}"/> to present
                </c:when>
                <c:when test="${empty entry.end}">
                  <c:out value="${entry.start}"/>
                </c:when>
                <c:otherwise>
                  <c:out value="${entry.start}"/> to
                  <c:out value="${entry.end}"/>
                </c:otherwise>
              </c:choose>
            </dd>
            <c:if test="${not empty entry.placeNames}">
              <dt>Places</dt>
              <dd>
                <c:forEach var="placeName" items="${entry.placeNames}">
                  <c:out value="${placeName}"/>
                </c:forEach>
              </dd>
            </c:if>
            <c:if test="${not empty entry.protocols}">
              <dt>Protocols</dt>
              <dd>
                <c:forEach var="protocol" items="${entry.protocols}">
                  <c:out value="${protocol}"/>
                </c:forEach>
              </dd>
            </c:if>
            <c:if test="${entry.unknown}">
              <dt>Unknown</dt>
              <dd>Yes</dd>
            </c:if>
          </dl>
          <c:if test="${not empty entry.links}">
            <ul>
              <c:forEach var="link" items="${entry.links}">
                <li>
                  <a href="<c:out value='${link.target}'/>">
                    <c:out value="${link.label}"/>
                  </a>
                </li>
              </c:forEach>
            </ul>
          </c:if>
        </div>
      </content>
      <author>
        <name>Tor Metrics</name>
      </author>
    </entry>
  </c:forEach>
</feed>
