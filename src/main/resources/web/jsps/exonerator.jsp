<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<jsp:include page="top.jsp">
  <jsp:param name="pageTitle" value="ExoneraTor &ndash; Tor Metrics"/>
  <jsp:param name="navActive" value="Services"/>
</jsp:include>

<div class="container">
  <ul class="breadcrumb">
    <li><a href="/">Home</a></li>
    <li><a href="/services.html">Services</a></li>
    <li class="active">ExoneraTor</li>
  </ul>
</div>

<div class="container">
  <h1>ExoneraTor</h1>
</div>

${body}

<jsp:include page="bottom.jsp"/>

