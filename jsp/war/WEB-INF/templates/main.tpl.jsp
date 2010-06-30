<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
    <title><jsp:getProperty name="template" property="title"/></title>
    <meta http-equiv=Content-Type content="text/html; charset=iso-8859-1">
    <link href="/css/stylesheet-ltr.css" type=text/css rel=stylesheet>
    <link href="/images/favicon.ico" type=image/x-icon rel="shortcut icon">
</head>
<body>
  <div class="center">
    <%--@ include file="/WEB-INF/templates/banner.tpl.jsp" --%>
    <jsp:include page='/WEB-INF/templates/banner.tpl.jsp' />
    <div class="main-column">
      <jsp:include page='<%=template.getTemplate()%>' />
    </div>
    </div>
    <div class="bottom" id="bottom">
      <%--@ include file="/WEB-INF/templates/footer.tpl.jsp" --%>
      <jsp:include page='/WEB-INF/templates/footer.tpl.jsp' />
    </div>
  </div>
</body>
</html>
