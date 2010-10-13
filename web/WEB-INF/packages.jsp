<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
  <title>Tor Metrics Portal: Packages</title>
  <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">
  <link href="/css/stylesheet-ltr.css" type="text/css" rel="stylesheet">
  <link href="/images/favicon.ico" type="image/x-icon" rel="shortcut icon">
</head>
<body>
  <div class="center">
    <%@ include file="banner.jsp"%>
    <div class="main-column">
<%@page import="java.util.*" %>
<h2>Tor Metrics Portal: Packages</h2>
<br>
<h3>Packages requested from GetTor</h3>
<br>
<p>GetTor allows users to fetch the Tor software via email. The following
graph shows the number of packages requested from GetTor per day.</p>
<p>
<a name="gettor"></a>
<%
if ("gettor".equals(request.getParameter("graph"))) {
  List<String> parameters = new ArrayList<String>();
// TODO check values here!
  String startParameter = request.getParameter("start"),
      endParameter = request.getParameter("end"),
      bundleParameter = request.getParameter("bundle");
  if (startParameter != null && startParameter.length() > 0) {
    parameters.add("start=" + startParameter);
  }
  if (endParameter != null && endParameter.length() > 0) {
    parameters.add("end=" + endParameter);
  }
  if (bundleParameter != null && bundleParameter.length() > 0) {
    parameters.add("bundle=" + bundleParameter);
  }
  StringBuilder url = new StringBuilder("gettor.png");
  if (parameters.size() > 0) {
    url.append("?" + parameters.get(0));
    if (parameters.size() > 1) {
      for (int i = 1; i < parameters.size(); i++) {
        url.append("&" + parameters.get(i));
      }
    }
  }
  out.println("<img src=\"" + url.toString() + "\" width=\"576\" "
      + "height=\"360\" alt=\"GetTor graph\">");
} else {%>
  <img src="gettor.png" width="576" height="360" alt="GetTor graph">
<%
}
%><form action="packages.html#gettor">
  <div class="formrow">
    <input type="hidden" name="graph" value="gettor">
    <p>
    <label>Start date (yyyy-mm-dd):</label>
      <input type="text" name="start" size="10"
        value="<%=("gettor".equals(request.getParameter("graph")) &&
                   request.getParameter("start") != null) ?
                      request.getParameter("start") : ""%>">
    <label>End date (yyyy-mm-dd):</label>
      <input type="text" name="end" size="10"
        value="<%=("gettor".equals(request.getParameter("graph")) &&
                   request.getParameter("end") != null) ?
                      request.getParameter("end") : ""%>">
    </p><p>
      Packages:
      <input type="radio" name="bundle" value="all"> Total packages
      <input type="radio" name="bundle" value="en"> TBB (en)
      <input type="radio" name="bundle" value="zh_CN"> TBB (zh_CN)
      <input type="radio" name="bundle" value="fa"> TBB (fa)
    </p><p>
    <input class="submit" type="submit" value="Update graph">
    </p>
  </div>
</form>

<p><a href="csv/gettor.csv">CSV</a> file containing all data.</p>
<br>
    </div>
  </div>
  <div class="bottom" id="bottom">
    <%@ include file="footer.jsp"%>
  </div>
</body>
</html>
