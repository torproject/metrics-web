<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<jsp:include page="top.jsp">
  <jsp:param name="pageTitle" value="Sources &ndash; Tor Metrics"/>
  <jsp:param name="navActive" value="Sources"/>
</jsp:include>

    <div class="container">
      <ul class="breadcrumb">
        <li><a href="/">Home</a></li>
        <li><a href="/sources.html">Sources</a></li>
        <li><a href="/collector.html">CollecTor</a></li>
      </ul>
    </div>

    <div class="container">
      <div class="row">
        <div class="col-xs-12">
          <table class="table">
            <thead>
              <tr>
                <th>Name</th>
                <th>Last modified</th>
                <th>Size</th>
              </tr>
            </thead>
            <tbody>
              <c:forEach var="file" items="${files}"><tr>
                <td><a href="${file[1]}">${file[0]}</a></td>
                <td>${file[2]}</td>
                <td>${file[3]}</td>
              </tr></c:forEach>
            </tbody>
          </table>
        </div><!-- col -->
      </div><!-- row -->
    </div><!-- container -->

<jsp:include page="bottom.jsp"/>

