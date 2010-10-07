<%@page import="java.util.*" %>
<h2>Tor Metrics Portal: Performance</h2>
<br/>
<h3>Time to download files over Tor</h3>
<br/>
<p>The following graphs show the performance of the Tor network as
experienced by its users. The graphs contain the average (median) time to
request files of three different sizes over Tor as well as first and third
quartile of request times.</p>
<a id="torperf" />
<%
if ("torperf".equals(request.getParameter("graph"))) {
  List<String> parameters = new ArrayList<String>();
// TODO check values here!
  String startParameter = request.getParameter("start"),
      endParameter = request.getParameter("end"),
      sourceParameter = request.getParameter("source"),
      filesizeParameter = request.getParameter("filesize");
  if (startParameter != null && startParameter.length() > 0) {
    parameters.add("start=" + startParameter);
  }
  if (endParameter != null && endParameter.length() > 0) {
    parameters.add("end=" + endParameter);
  }
  if (sourceParameter != null && sourceParameter.length() > 0) {
    parameters.add("source=" + sourceParameter);
  }
  if (filesizeParameter != null && filesizeParameter.length() > 0) {
    parameters.add("filesize=" + filesizeParameter);
  }
  StringBuilder url = new StringBuilder("torperf.png");
  if (parameters.size() > 0) {
    url.append("?" + parameters.get(0));
    if (parameters.size() > 1) {
      for (int i = 1; i < parameters.size(); i++) {
        url.append("&" + parameters.get(i));
      }
    }
  }
  out.println("<img src=\"" + url.toString() + "\" width=\"576\" "
      + "height=\"360\" />");
} else {%>
  <img src="torperf.png" width="576" height="360" />
<%
}
%>

</p>
<form action="performance.html#torperf">
  <div class="formrow">
    <input type="hidden" name="graph" value="torperf"/>
    <p>
    <label class="startend" for="start">Start date (yyyy-mm-dd):</label>
      <input type="text" name="start" id="start" size="10"
        value="<%=("torperf".equals(request.getParameter("graph")) &&
                   request.getParameter("start") != null) ?
                      request.getParameter("start") : ""%>"/>
    <label class="startend" for="end">End date (yyyy-mm-dd):</label>
      <input type="text" name="end" id="end" size="10"
        value="<%=("torperf".equals(request.getParameter("graph")) &&
                   request.getParameter("end") != null) ?
                      request.getParameter("end") : ""%>"/>
    </p><p>
      <label>Source: </label>
      <input type="radio" name="source" value="torperf"> torperf</input>
      <input type="radio" name="source" value="moria"> moria</input>
      <input type="radio" name="source" value="siv"> siv</input>
    </p><p>
      <label>File size: </label>
      <input type="radio" name="filesize" value="50kb"> 50 KiB</input>
      <input type="radio" name="filesize" value="1mb"> 1 MiB</input>
      <input type="radio" name="filesize" value="5mb"> 5 MiB</input>
    </p><p>
    <input class="submit" type="submit" value="Update graph"/>
    </p>
  </div>
</form>

<p><a href="csv/torperf.csv">CSV</a> file containing all data.</p>
<br/>
