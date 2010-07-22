<jsp:useBean id="template" class="org.torproject.ernie.web.TemplateController"
    scope="request" />
<table class="banner" border="0" cellpadding="0" cellspacing="0" summary="">
<tr>
  <td class="banner-left">
    <a href="/index.html">
      <img src="/images/top-left.png" alt="Click to go to home page"
           width="193" heigth="79"></a></td>
  <td class="banner-middle">
    <a <% if (template.getTemplateName().equals("index")) {%>
        class="current" <%} else {%> href="/index.html" <%}%>>Home</a>
    <a <% if (template.getTemplateName().equals("graphs")) {%>
        class="current" <%} else {%> href="/graphs.html" <%}%>>Graphs</a>
    <a <% if (template.getTemplateName().equals("research")) {%>
        class="current" <%} else {%> href="/research.html" <%}%>>Research</a>
    <a <% if (template.getTemplateName().equals("status")) {%>
        class="current" <%} else {%> href="/status.html" <%}%>>Status</a>
    <%if (template.getTemplateName().startsWith("graphs")) {%>
      <br/>
      <font size="2">
        <a <%if (template.getTemplateName().contains("network-size")){%>
            class="current" <%} else {%>
            href="/consensus-graphs.html"<%}%>>Network Size</a>
        <a <%if (template.getTemplateName().contains("exit-relays")) {%>
            class="current" <%} else {%>
            href="/exit-relays-graphs.html"<%}%>>Exit Relays</a>
        <a <%if (template.getTemplateName().contains("new-users")) {%>
            class="current" <%} else {%>
            href="/new-users-graphs.html"<%}%>>New Users</a>
        <a <%if (template.getTemplateName().contains("recurring-users")) {%>
            class="current" <%} else {%>
            href="/recurring-users-graphs.html"<%}%>>Recurring Users</a>
        <a <%if (template.getTemplateName().contains("bridge-users")) {%>
            class="current" <%} else {%>
            href="/bridge-users-graphs.html"<%}%>>Bridge Users</a>
        <a <%if (template.getTemplateName().contains("torperf")) {%>
            class="current" <%} else {%>
            href="/torperf-graphs.html"<%}%>>torperf</a>
        <a <%if (template.getTemplateName().contains("gettor")) {%>
            class="current" <%} else {%>
            href="/gettor-graphs.html"<%}%>>GetTor</a>
        <a <%if (template.getTemplateName().contains("custom-graph")) {%>
            class="current" <%} else {%>
            href="/custom-graph.html"<%}%>>Custom Graph</a>
      </font>
    <%} else if (template.getTemplateName().startsWith("status")) {%>
      <br/>
      <font size="2">
        <a href="/exonerator.html">ExoneraTor</a>
        <a href="/relay-search.html">Relay Search</a>
        <a href="/consensus-health.html">Consensus Health</a>
        <a href="/log.html">Last Log</a>
      </font>
    <%} else if (template.getTemplateName().startsWith("research")) { %>
      <br/>
      <font size="2">
        <a <%if (template.getTemplateName().contains("papers")) {%>
          class="current" <%} else {%> href="/papers.html"<%}%>>Papers</a>
        <a <%if (template.getTemplateName().contains("data")) {%>
          class="current" <%} else {%> href="/data.html"<%}%>>Data</a>
        <a <%if (template.getTemplateName().contains("tools")) {%>
          class="current" <%} else {%> href="/tools.html"<%}%>>Tools</a>
      </font>
    <%}%>
  </td>
  <td class="banner-right"></td>
</tr>
</table>
