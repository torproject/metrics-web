<jsp:useBean id="customgraph"
    class="org.torproject.ernie.web.CustomGraphController"
    scope="request" />
<h2>Tor Metrics Portal: Custom Graph</h2>
<br/>
<p>Custom graphs can be requested based on a specific range of
dates and parameters available. Many similar graphs are available in the
other sections of the metrics portal, however, the need may arise
for tracking a more specific part of the Tor network.</p>
<p>Date format: yyyy-mm-dd</p>
<div id="graphmenu">
  <form action="<%=request.getRequestURI()%>">
    <div class="formrow">
      <label class="graphname">Network size</label>
      <input type="hidden" name="graph" value="networksize"/>
      <label class="startend" for="start">start</label>
        <input type="text" name="start" id="start"
          value="<%=("networksize".equals(request.getParameter("graph")) &&
                     request.getParameter("start") != null) ?
                        request.getParameter("start") : ""%>"/>
      <label class="startend" for="end">end</label>
        <input type="text" name="end" id="end"
          value="<%=("networksize".equals(request.getParameter("graph")) &&
                     request.getParameter("end") != null) ?
                        request.getParameter("end") : ""%>"/>
      <input class="submit" type="submit"/>
    </div>
  </form>
  <form action="<%=request.getRequestURI()%>">
    <div class="formrow">
      <label class="graphname">Platforms</label>
      <input type="hidden" name="graph" value="platforms"/>
      <label class="startend" for="start">start</label>
        <input type="text" name="start" id="start"
          value="<%=("platforms".equals(request.getParameter("graph"))  &&
                     request.getParameter("start") != null) ?
                        request.getParameter("start") : ""%>"/>
      <label class="startend" for="end">end</label>
        <input type="text" name="end" id="end"
          value="<%=("platforms".equals(request.getParameter("graph"))  &&
                     request.getParameter("end") != null) ?
                        request.getParameter("end") : ""%>"/>
      <input class="submit" type="submit"/>
    </div>
  </form>
  <form action="<%=request.getRequestURI()%>">
    <div class="formrow">
      <label class="graphname">Versions</label>
      <input type="hidden" name="graph" value="versions"/>
      <label class="startend" for="start">start</label>
        <input type="text" name="start" id="start"
          value="<%=("versions".equals(request.getParameter("graph")) &&
                     request.getParameter("start") != null) ?
                        request.getParameter("start") : ""%>"/>
      <label class="startend" for="end">end</label>
        <input type="text" name="end" id="end"
          value="<%=("versions".equals(request.getParameter("graph")) &&
                     request.getParameter("end") != null) ?
                        request.getParameter("end") : ""%>"/>
      <input class="submit" type="submit"/>
    </div>
  </form>
  <form action="<%=request.getRequestURI()%>">
    <div class="formrow">
      <label class="graphname">Bandwidth</label>
      <input type="hidden" name="graph" value="bandwidth"/>
      <label class="startend" for="start">start</label>
        <input type="text" name="start" id="start"
          value="<%=("bandwidth".equals(request.getParameter("graph")) &&
                     request.getParameter("start") != null) ?
                        request.getParameter("start") : ""%>"/>
      <label class="startend" for="end">end</label>
        <input type="text" name="end" id="end"
          value="<%=("bandwidth".equals(request.getParameter("graph")) &&
                     request.getParameter("end") != null) ?
                        request.getParameter("end") : ""%>"/>
      <input class="submit" type="submit"/>
    </div>
  </form>
  <br/>
</div>

<%if (request.getParameter("start") != null &&
      request.getParameter("end") != null &&
      request.getParameter("graph") != null) {
  customgraph.getGraphURL();
  if (!customgraph.getError().isEmpty()) {
    for(String err : customgraph.getError())  { %>
    <p class="error"><%=err%></p>
  <%} } else {%>
  <p><strong><jsp:getProperty name="customgraph" property="graphName"/> graph from
  <jsp:getProperty name="customgraph" property="graphStart"/> to
  <jsp:getProperty name="customgraph" property="graphEnd"/></strong></p>
  <img src="<jsp:getProperty name="customgraph" property="graphURL"/>"
       href="<jsp:getProperty name="customgraph" property="graphURL"/>"/>
  <%}
}%>
<div style="clear:both;"></div>
