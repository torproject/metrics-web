<jsp:useBean id="template" class="ernie.TemplateController" scope="request" />

<jsp:setProperty name="template" property="template" value="index"/>
<jsp:setProperty name="template" property="title" value="Tor Metrics Portal"/>

<%@ include file="/WEB-INF/templates/main.tpl.jsp" %>
