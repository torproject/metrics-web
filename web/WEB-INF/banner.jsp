<table class="banner" border="0" cellpadding="0" cellspacing="0" summary="">
<tr>
  <td class="banner-left">
    <a href="/index.html">
      <img src="/images/top-left.png" alt="Click to go to home page"
           width="193" height="79"></a></td>
  <td class="banner-middle">
    <% String currentPage = request.getRequestURI(); %>
    <a <% if (currentPage.endsWith("index.jsp")) {
        %>class="current"<%} else {%>href="/index.html"<%}%>>Home</a>
    <a <% if (currentPage.endsWith("graphs.jsp")) {
        %>class="current"<%} else {%>href="/graphs.html"<%}%>>Graphs</a>
    <a <% if (currentPage.endsWith("research.jsp")) {
        %>class="current"<%} else {%>href="/research.html"<%}%>>Research</a>
    <a <% if (currentPage.endsWith("status.jsp")) {
        %>class="current"<%} else {%>href="/status.html"<%}%>>Status</a>
    <%if (currentPage.endsWith("graphs.jsp") ||
         currentPage.endsWith("network.jsp") ||
         currentPage.endsWith("users.jsp") ||
         currentPage.endsWith("packages.jsp") ||
         currentPage.endsWith("performance.jsp")) {
     %><br>
      <font size="2">
        <a <%if (currentPage.endsWith("network.jsp")){
            %>class="current"<%} else {%>href="/network.html"<%}
            %>>Network</a>
        <a <%if (currentPage.endsWith("users.jsp")) {
            %>class="current"<%} else {%>href="/users.html"<%}
            %>>Users</a>
        <a <%if (currentPage.endsWith("packages.jsp")) {
            %>class="current"<%} else {%>href="/packages.html"<%}
            %>>Packages</a>
        <a <%if (currentPage.endsWith("performance.jsp")) {
            %>class="current"<%} else {%>href="/performance.html"<%}
            %>>Performance</a>
      </font>
    <%} else if (currentPage.endsWith("status.jsp") ||
                 currentPage.endsWith("networkstatus.jsp") ||
                 currentPage.endsWith("exonerator.jsp") ||
                 currentPage.endsWith("relay-search.jsp") ||
                 currentPage.endsWith("consensus-health.jsp")) {
     %><br>
      <font size="2">
        <a <%if (currentPage.endsWith("networkstatus.jsp")){
            %>class="current"<%} else {%>href="/networkstatus.html"<%}
            %>>Network Status</a>
        <a <%if (currentPage.endsWith("exonerator.jsp")){
            %>class="current"<%} else {%>href="/exonerator.html"<%}
            %>>ExoneraTor</a>
        <a <%if (currentPage.endsWith("relay-search.jsp")){
            %>class="current"<%} else {%>href="/relay-search.html"<%}
            %>>Relay Search</a>
        <a <%if (currentPage.endsWith("consensus-health.jsp")){
            %>class="current"<%} else {%>href="/consensus-health.html"<%}
            %>>Consensus Health</a>
      </font>
    <%} else if (currentPage.endsWith("research.jsp") ||
                 currentPage.endsWith("papers.jsp") ||
                 currentPage.endsWith("data.jsp") ||
                 currentPage.endsWith("tools.jsp")) {
     %><br>
      <font size="2">
        <a <%if (currentPage.endsWith("papers.jsp")) {
            %>class="current"<%} else {%> href="/papers.html"<%}
            %>>Papers</a>
        <a <%if (currentPage.endsWith("data.jsp")) {
            %>class="current"<%} else {%> href="/data.html"<%}
            %>>Data</a>
        <a <%if (currentPage.endsWith("tools.jsp")) {
            %>class="current"<%} else {%> href="/tools.html"<%}
            %>>Tools</a>
      </font>
    <%}%>
  </td>
  <td class="banner-right"></td>
</tr>
</table>
