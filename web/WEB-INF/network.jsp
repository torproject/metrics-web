<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
  <title>Tor Metrics Portal: Network</title>
  <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">
  <link href="/css/stylesheet-ltr.css" type="text/css" rel="stylesheet">
  <link href="/images/favicon.ico" type="image/x-icon" rel="shortcut icon">
</head>
<body>
  <div class="center">
    <%@ include file="banner.jsp"%>
    <div class="main-column">
<%@page import="java.util.*" %>
<h2>Tor Metrics Portal: Network</h2>
<br>
<h3>Relays and bridges in the network</h3>
<br>
<p>The following graph shows the average daily number of relays and
bridges in the network.</p>
<a name="networksize"></a>
<%
StringBuilder networksizeUrl = new StringBuilder("networksize.png");
if ("networksize".equals(request.getParameter("graph"))) {
  List<String> parameters = new ArrayList<String>();
// TODO check values here!
  String startParameter = request.getParameter("start"),
      endParameter = request.getParameter("end");
  if (startParameter != null && startParameter.length() > 0) {
    parameters.add("start=" + startParameter);
  }
  if (endParameter != null && endParameter.length() > 0) {
    parameters.add("end=" + endParameter);
  }
  if (parameters.size() > 0) {
    networksizeUrl.append("?" + parameters.get(0));
    if (parameters.size() > 1) {
      for (int i = 1; i < parameters.size(); i++) {
        networksizeUrl.append("&" + parameters.get(i));
      }
    }
  }
}
out.println("<img src=\"" + networksizeUrl.toString() + "\" width=\"576\" "
    + "height=\"360\" alt=\"Network size graph\">");
%><form action="network.html#networksize">
  <div class="formrow">
    <input type="hidden" name="graph" value="networksize">
    <p>
    <label>Start date (yyyy-mm-dd):</label>
      <input type="text" name="start" size="10"
        value="<%=("networksize".equals(request.getParameter("graph")) &&
                   request.getParameter("start") != null) ?
                      request.getParameter("start") : ""%>">
    <label>End date (yyyy-mm-dd):</label>
      <input type="text" name="end" size="10"
        value="<%=("networksize".equals(request.getParameter("graph")) &&
                   request.getParameter("end") != null) ?
                      request.getParameter("end") : ""%>">
    </p><p>
    <input class="submit" type="submit" value="Update graph">
    </p>
  </div>
</form>
<p><a href="csv/networksize.csv">CSV</a> file containing all data.</p>
<br>

<h3>Relays with Exit, Fast, Guard, and Stable flags</h3>
<br>
<p>The directory authorities assign certain flags to relays that clients
use for their path selection decisions. The following graph shows the
average number of relays with these flags assigned.</p>
<a name="relayflags"></a>
<%
StringBuilder relayflagsUrl = new StringBuilder("relayflags.png");
if ("relayflags".equals(request.getParameter("graph"))) {
  List<String> parameters = new ArrayList<String>();
// TODO check values here!
  String startParameter = request.getParameter("start"),
      endParameter = request.getParameter("end");
  String[] flagParameters = request.getParameterValues("flag");
  if (startParameter != null && startParameter.length() > 0) {
    parameters.add("start=" + startParameter);
  }
  if (endParameter != null && endParameter.length() > 0) {
    parameters.add("end=" + endParameter);
  }
  if (flagParameters != null && flagParameters.length > 0) {
    for (String flag : flagParameters) {
      if (flag != null && flag.length() > 0) {
        parameters.add("flag=" + flag);
      }
    }
  }
  if (parameters.size() > 0) {
    relayflagsUrl.append("?" + parameters.get(0));
    if (parameters.size() > 1) {
      for (int i = 1; i < parameters.size(); i++) {
        relayflagsUrl.append("&" + parameters.get(i));
      }
    }
  }
}
out.println("<img src=\"" + relayflagsUrl.toString() + "\" width=\"576\" "
    + "height=\"360\" alt=\"Relay flags graph\">");
%><form action="network.html#relayflags">
  <div class="formrow">
    <input type="hidden" name="graph" value="relayflags">
    <p>
    <label>Start date (yyyy-mm-dd):</label>
      <input type="text" name="start" size="10"
        value="<%=("relayflags".equals(request.getParameter("graph")) &&
                   request.getParameter("start") != null) ?
                      request.getParameter("start") : ""%>">
    <label>End date (yyyy-mm-dd):</label>
      <input type="text" name="end" size="10"
        value="<%=("relayflags".equals(request.getParameter("graph")) &&
                   request.getParameter("end") != null) ?
                      request.getParameter("end") : ""%>">
    </p><p>
      <label>Relay flags: </label>
      <input type="checkbox" name="flag" value="Running"> Running
      <input type="checkbox" name="flag" value="Exit"> Exit
      <input type="checkbox" name="flag" value="Fast"> Fast
      <input type="checkbox" name="flag" value="Guard"> Guard
      <input type="checkbox" name="flag" value="Stable"> Stable
    </p><p>
    <input class="submit" type="submit" value="Update graph">
    </p>
  </div>
</form>
<br>

<h3>Relays with Exit, Fast, Guard, and Stable flags on 1-hour detail</h3>
<br>
<p>The same graph on the average number of relays with flags assigned is
available on 1-hour detail.</p>
<a name="relayflags-hour"></a>
<%
StringBuilder relayflagsHourUrl = new StringBuilder("relayflags-hour.png");
if ("relayflags-hour".equals(request.getParameter("graph"))) {
  List<String> parameters = new ArrayList<String>();
// TODO check values here!
  String startParameter = request.getParameter("start"),
      endParameter = request.getParameter("end");
  String[] flagParameters = request.getParameterValues("flag");
  if (startParameter != null && startParameter.length() > 0) {
    parameters.add("start=" + startParameter);
  }
  if (endParameter != null && endParameter.length() > 0) {
    parameters.add("end=" + endParameter);
  }
  if (flagParameters != null && flagParameters.length > 0) {
    for (String flag : flagParameters) {
      if (flag != null && flag.length() > 0) {
        parameters.add("flag=" + flag);
      }
    }
  }
  if (parameters.size() > 0) {
    relayflagsHourUrl.append("?" + parameters.get(0));
    if (parameters.size() > 1) {
      for (int i = 1; i < parameters.size(); i++) {
        relayflagsHourUrl.append("&" + parameters.get(i));
      }
    }
  }
}
out.println("<img src=\"" + relayflagsHourUrl.toString()
    + "\" width=\"576\" height=\"360\" alt=\"Relay flags graph\">");
%><form action="network.html#relayflags-hour">
  <div class="formrow">
    <input type="hidden" name="graph" value="relayflags-hour">
    <p>
    <label>Start date (yyyy-mm-dd):</label>
      <input type="text" name="start" size="10"
        value="<%=("relayflags-hour".equals(request.getParameter("graph")) &&
                   request.getParameter("start") != null) ?
                      request.getParameter("start") : ""%>">
    <label>End date (yyyy-mm-dd):</label>
      <input type="text" name="end" size="10"
        value="<%=("relayflags-hour".equals(request.getParameter("graph")) &&
                   request.getParameter("end") != null) ?
                      request.getParameter("end") : ""%>">
    </p><p>
      <label>Relay flags: </label>
      <input type="checkbox" name="flag" value="Running"> Running
      <input type="checkbox" name="flag" value="Exit"> Exit
      <input type="checkbox" name="flag" value="Fast"> Fast
      <input type="checkbox" name="flag" value="Guard"> Guard
      <input type="checkbox" name="flag" value="Stable"> Stable
    </p><p>
    <input class="submit" type="submit" value="Update graph">
    </p>
  </div>
</form>
<br>

<h3>Relays by version</h3>
<br>
<p>Relays report the Tor version that they are running to the directory
authorities. The following graph shows the number of relays by
version.</p>
<a name="versions"></a>
<%
StringBuilder versionsUrl = new StringBuilder("versions.png");
if ("versions".equals(request.getParameter("graph"))) {
  List<String> parameters = new ArrayList<String>();
// TODO check values here!
  String startParameter = request.getParameter("start"),
      endParameter = request.getParameter("end");
  if (startParameter != null && startParameter.length() > 0) {
    parameters.add("start=" + startParameter);
  }
  if (endParameter != null && endParameter.length() > 0) {
    parameters.add("end=" + endParameter);
  }
  if (parameters.size() > 0) {
    versionsUrl.append("?" + parameters.get(0));
    if (parameters.size() > 1) {
      for (int i = 1; i < parameters.size(); i++) {
        versionsUrl.append("&" + parameters.get(i));
      }
    }
  }
}
out.println("<img src=\"" + versionsUrl.toString() + "\" width=\"576\" "
    + "height=\"360\" alt=\"Relay versions graph\">");
%><form action="network.html#versions">
  <div class="formrow">
    <input type="hidden" name="graph" value="versions">
    <p>
    <label>Start date (yyyy-mm-dd):</label>
      <input type="text" name="start" size="10"
        value="<%=("versions".equals(request.getParameter("graph")) &&
                   request.getParameter("start") != null) ?
                      request.getParameter("start") : ""%>">
    <label>End date (yyyy-mm-dd):</label>
      <input type="text" name="end" size="10"
        value="<%=("versions".equals(request.getParameter("graph")) &&
                   request.getParameter("end") != null) ?
                      request.getParameter("end") : ""%>">
    </p><p>
    <input class="submit" type="submit" value="Update graph">
    </p>
  </div>
</form>
<br>

<h3>Relays by platform</h3>
<br>
<p>Relays report the operating system they are running to the directory
authorities. The following graph shows the number of relays by
platform.</p>
<a name="platforms"></a>
<%
StringBuilder platformsUrl = new StringBuilder("platforms.png");
if ("platforms".equals(request.getParameter("graph"))) {
  List<String> parameters = new ArrayList<String>();
// TODO check values here!
  String startParameter = request.getParameter("start"),
      endParameter = request.getParameter("end");
  if (startParameter != null && startParameter.length() > 0) {
    parameters.add("start=" + startParameter);
  }
  if (endParameter != null && endParameter.length() > 0) {
    parameters.add("end=" + endParameter);
  }
  if (parameters.size() > 0) {
    platformsUrl.append("?" + parameters.get(0));
    if (parameters.size() > 1) {
      for (int i = 1; i < parameters.size(); i++) {
        platformsUrl.append("&" + parameters.get(i));
      }
    }
  }
}
out.println("<img src=\"" + platformsUrl.toString() + "\" width=\"576\" "
    + "height=\"360\" alt=\"Relay platforms graph\">");
%><form action="network.html#platforms">
  <div class="formrow">
    <input type="hidden" name="graph" value="platforms">
    <p>
    <label>Start date (yyyy-mm-dd):</label>
      <input type="text" name="start" size="10"
        value="<%=("platforms".equals(request.getParameter("graph")) &&
                   request.getParameter("start") != null) ?
                      request.getParameter("start") : ""%>">
    <label>End date (yyyy-mm-dd):</label>
      <input type="text" name="end" size="10"
        value="<%=("platforms".equals(request.getParameter("graph")) &&
                   request.getParameter("end") != null) ?
                      request.getParameter("end") : ""%>">
    </p><p>
    <input class="submit" type="submit" value="Update graph">
    </p>
  </div>
</form>
<br>

<h3>Total relay bandwidth in the network</h3>
<br>
<p>Relays report how much bandwidth they are willing to contribute and how
many bytes they have read and written in the past 24 hours. The following
graph shows total advertised bandwidth and bandwidth history of all relays
in the network.</p>
<a name="bandwidth"></a>
<%
StringBuilder bandwidthUrl = new StringBuilder("bandwidth.png");
if ("bandwidth".equals(request.getParameter("graph"))) {
  List<String> parameters = new ArrayList<String>();
// TODO check values here!
  String startParameter = request.getParameter("start"),
      endParameter = request.getParameter("end");
  if (startParameter != null && startParameter.length() > 0) {
    parameters.add("start=" + startParameter);
  }
  if (endParameter != null && endParameter.length() > 0) {
    parameters.add("end=" + endParameter);
  }
  if (parameters.size() > 0) {
    bandwidthUrl.append("?" + parameters.get(0));
    if (parameters.size() > 1) {
      for (int i = 1; i < parameters.size(); i++) {
        bandwidthUrl.append("&" + parameters.get(i));
      }
    }
  }
}
out.println("<img src=\"" + bandwidthUrl.toString() + "\" width=\"576\" "
    + "height=\"360\" alt=\"Relay bandwidth graph\">");
%><form action="network.html#bandwidth">
  <div class="formrow">
    <input type="hidden" name="graph" value="bandwidth">
    <p>
    <label>Start date (yyyy-mm-dd):</label>
      <input type="text" name="start" size="10"
        value="<%=("bandwidth".equals(request.getParameter("graph")) &&
                   request.getParameter("start") != null) ?
                      request.getParameter("start") : ""%>">
    <label>End date (yyyy-mm-dd):</label>
      <input type="text" name="end" size="10"
        value="<%=("bandwidth".equals(request.getParameter("graph")) &&
                   request.getParameter("end") != null) ?
                      request.getParameter("end") : ""%>">
    </p><p>
    <input class="submit" type="submit" value="Update graph">
    </p>
  </div>
</form>
<br>

<h3>Number of bytes spent on answering directory requests</h3>
<br>
<p>Relays running on 0.2.2.15-alpha or higher report the number of bytes
they spend on answering directory requests. The following graph shows
total written and read bytes as well as written and read dir bytes. The
dir bytes are extrapolated from those relays who report them to reflect
the number of written and read dir bytes by all relays.</p>
<a name="dirbytes"></a>
<%
StringBuilder dirbytesUrl = new StringBuilder("dirbytes.png");
if ("dirbytes".equals(request.getParameter("graph"))) {
  List<String> parameters = new ArrayList<String>();
// TODO check values here!
  String startParameter = request.getParameter("start"),
      endParameter = request.getParameter("end");
  if (startParameter != null && startParameter.length() > 0) {
    parameters.add("start=" + startParameter);
  }
  if (endParameter != null && endParameter.length() > 0) {
    parameters.add("end=" + endParameter);
  }
  if (parameters.size() > 0) {
    dirbytesUrl.append("?" + parameters.get(0));
    if (parameters.size() > 1) {
      for (int i = 1; i < parameters.size(); i++) {
        dirbytesUrl.append("&" + parameters.get(i));
      }
    }
  }
}
out.println("<img src=\"" + dirbytesUrl.toString() + "\" width=\"576\" "
    + "height=\"360\" alt=\"Dir bytes graph\">");
%><form action="network.html#dirbytes">
  <div class="formrow">
    <input type="hidden" name="graph" value="dirbytes">
    <p>
    <label>Start date (yyyy-mm-dd):</label>
      <input type="text" name="start" size="10"
        value="<%=("dirbytes".equals(request.getParameter("graph")) &&
                   request.getParameter("start") != null) ?
                      request.getParameter("start") : ""%>">
    <label>End date (yyyy-mm-dd):</label>
      <input type="text" name="end" size="10"
        value="<%=("dirbytes".equals(request.getParameter("graph")) &&
                   request.getParameter("end") != null) ?
                      request.getParameter("end") : ""%>">
    </p><p>
    <input class="submit" type="submit" value="Update graph">
    </p>
  </div>
</form>
<br>
    </div>
  </div>
  <div class="bottom" id="bottom">
    <%@ include file="footer.jsp"%>
  </div>
</body>
</html>
