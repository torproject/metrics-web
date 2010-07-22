<jsp:useBean id="template" class="org.torproject.ernie.web.TemplateController" scope="request" />
<jsp:useBean id="customgraph" class="org.torproject.ernie.web.CustomGraphController" scope="request" />
<jsp:setProperty name="customgraph" property="parameterMap" value="<%=request.getParameterMap()%>" />
<jsp:setProperty name="template" property="template" value="graphs_custom-graph"/>
<jsp:setProperty name="template" property="title" value="Tor Metrics Portal: Custom Graph"/>
<%@ include file="/WEB-INF/templates/main.tpl.jsp" %>
