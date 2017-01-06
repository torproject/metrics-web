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

              <table>
                <tr>
                <c:forEach var="row" items="${tableheader}">
                  <th>${row}</th>
                </c:forEach>
                </tr>
                <c:forEach var="row" items="${tabledata}">
                  <tr>
                  <c:forEach var="col" items="${row}">
                    <td>${col}</td>
                  </c:forEach>
                  </tr>
                </c:forEach>
              </table>

              ${description}
            </div>
            <div class="col-md-4">

<form action="${id}.html">
    <p>
    <label for="start">Start date:</label>
    <input type="text" name="start" size="10" value="${start[0]}" placeholder="yyyy-mm-dd" id="start">
    </p><p>
    <label for="end">End date:</label>
    <input type="text" name="end" size="10" value="${end[0]}" placeholder="yyyy-mm-dd" id="end">
    </p><p>
    <input class="submit" type="submit" value="Update table">
    </p>
</form>

<c:if test="${fn:length(data) > 0}">
<p>Download underlying data:</p>
<ul>
<c:forEach var="row" items="${data}">
<li><a href="stats/${row}.csv">CSV</a> (<a href="stats.html#${row}">format</a>)</li>
</c:forEach>
</ul>
</c:if>

            </div><!-- col-md-4 -->
          </div><!-- row -->
        </div><!-- tab-pane -->
      </div><!-- tab-content -->
    </div><!-- container -->

<jsp:include page="bottom.jsp"/>

