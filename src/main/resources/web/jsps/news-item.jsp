            <tr>
              <td>
                <span class="dates">
                  <c:choose>
                    <c:when test="${empty entry.start}">
                      N/A
                    </c:when>
                    <c:when test="${entry.ongoing}">
                      <c:out value="${entry.start}"/> to present
                    </c:when>
                    <c:when test="${(empty entry.end) || (entry.start == entry.end)}">
                      <c:out value="${entry.start}"/>
                    </c:when>
                    <c:otherwise>
                      <c:out value="${entry.start}"/> to
                      <c:out value="${entry.end}"/>
                    </c:otherwise>
                  </c:choose>
                </span>
              </td>
              <td>
                <c:forEach var="placeName" items="${entry.placeNames}">
                  <span class="label label-warning"><c:out value="${placeName}"/></span>
                </c:forEach>
                <c:forEach var="protocol" items="${entry.protocols}">
                  <c:choose>
                    <c:when test="${protocol == 'relay'}">
                      <span class="label label-success">Relays</span>
                    </c:when>
                    <c:when test="${protocol == 'bridge'}">
                      <span class="label label-primary">Bridges</span>
                    </c:when>
                    <c:when test="${protocol == '<OR>'}">
                      <span class="label label-info">&lt;OR&gt;</span>
                    </c:when>
                    <c:otherwise>
                      <span class="label label-info"><c:out value="${protocol}"/></span>
                    </c:otherwise>
                  </c:choose>
                </c:forEach>
                <c:if test="${entry.unknown}">
                  <span class="label label-default">Unknown</span>
                </c:if>
              </td>
              <td>
                ${entry.description}
                <br />
                <c:forEach var="link" items="${entry.links}">
                  <c:choose>
                    <c:when test="${fn:startsWith(link.target, 'https://metrics.torproject.org/')}">
                      <c:set value="" var="atarget"/>
                    </c:when>
                    <c:otherwise>
                      <c:set value="_blank" var="atarget"/>
                    </c:otherwise>
                  </c:choose>
                  <a href="${link.target}" class="link metrics-news-link" target="${atarget}">
                    <c:out value="${link.label}"/>
                  </a>
                </c:forEach>
              </td>
           </tr>
