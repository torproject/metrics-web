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
    <a <% if (currentPage.endsWith("about.jsp")) {
        %>class="current"<%} else {%>href="/about.html"<%}%>>About</a>
  </td>
  <td class="banner-right"></td>
</tr>
</table>
