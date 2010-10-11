<jsp:useBean id="template" class="org.torproject.ernie.web.TemplateController"
    scope="request" />
<table class="banner" border="0" cellpadding="0" cellspacing="0" summary="">
<tr>
  <td class="banner-left">
    <a href="/index.html">
      <img src="/images/top-left.png" alt="Click to go to home page"
           width="193" height="79"></a></td>
  <td class="banner-middle">
    <a <% if (template.getTemplateName().equals("index")) {
        %>class="current" <%} else {%>href="/index.html"<%}%>>Home</a>
    <a <% if (template.getTemplateName().equals("graphs")) {
        %>class="current" <%} else {%>href="/graphs.html"<%}%>>Graphs</a>
    <a <% if (template.getTemplateName().equals("research")) {
        %>class="current" <%} else {%>href="/research.html"<%}%>>Research</a>
    <a <% if (template.getTemplateName().equals("status")) {
        %>class="current" <%} else {%>href="/status.html"<%}%>>Status</a>
    <%if (template.getTemplateName().startsWith("graphs")) {
     %><br>
      <font size="2">
        <a <%if (template.getTemplateName().contains("network")){
            %>class="current"<%} else {%>href="/network.html"<%}
            %>>Network</a>
        <a <%if (template.getTemplateName().contains("users")) {
            %>class="current"<%} else {%>href="/users.html"<%}
            %>>Users</a>
        <a <%if (template.getTemplateName().contains("packages")) {
            %>class="current"<%} else {%>href="/packages.html"<%}
            %>>Packages</a>
        <a <%if (template.getTemplateName().contains("performance")) {
            %>class="current"<%} else {%>href="/performance.html"<%}
            %>>Performance</a>
      </font>
    <%} else if (template.getTemplateName().startsWith("status")) {%>
      <br>
      <font size="2">
        <a href="/exonerator.html">ExoneraTor</a>
        <a href="/relay-search.html">Relay Search</a>
        <a href="/consensus-health.html">Consensus Health</a>
      </font>
    <%} else if (template.getTemplateName().startsWith("research")) { %>
      <br>
      <font size="2">
        <a <%if (template.getTemplateName().contains("papers")) {
            %>class="current"<%} else {%> href="/papers.html"<%}
            %>>Papers</a>
        <a <%if (template.getTemplateName().contains("data")) {
            %>class="current"<%} else {%> href="/data.html"<%}
            %>>Data</a>
        <a <%if (template.getTemplateName().contains("tools")) {
            %>class="current"<%} else {%> href="/tools.html"<%}
            %>>Tools</a>
      </font>
    <%}%>
  </td>
  <td class="banner-right"></td>
</tr>
</table>
