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
    <a <% if (currentPage.endsWith("network.jsp")) {
        %>class="current"<%} else {%>href="/network.html"<%}%>>Network</a>
    <a <% if (currentPage.endsWith("bubbles.jsp")) {
        %>class="current"<%} else {%>href="/bubbles.html"<%}%>>Bubbles</a>
    <a <% if (currentPage.endsWith("users.jsp")) {
        %>class="current"<%} else {%>href="/users.html"<%}%>>Users</a>
    <a <% if (currentPage.endsWith("performance.jsp")) {
        %>class="current"<%} else {%>href="/performance.html"<%}
        %>>Performance</a>
    <a <% if (currentPage.endsWith("stats.jsp")) {
        %>class="current"<%} else {%>href="/stats.html"<%}
        %>>Statistics</a>
  </td>
  <td class="banner-right"></td>
</tr>
</table>
