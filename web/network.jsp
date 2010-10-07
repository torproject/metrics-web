<jsp:useBean id="template" class="org.torproject.ernie.web.TemplateController" scope="request" />
<jsp:setProperty name="template" property="template" value="graphs_network"/>
<jsp:setProperty name="template" property="title" value="Tor Metrics Portal: Network"/>
<%@ include file="/WEB-INF/templates/main.tpl.jsp" %>
