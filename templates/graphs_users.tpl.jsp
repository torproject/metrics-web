<%@page import="java.util.*" %>
<h2>Tor Metrics Portal: Users</h2>
<br/>
<h3>New or returning, directly connecting Tor users</h3>
<br/>
<p>Users connecting to the Tor network for the first time request a list
of running relays from one of currently seven directory authorities.
Likewise, returning users whose network information is out of date connect
to one of the directory authorities to download a fresh list of relays.
The following graphs display an estimate of new or returning Tor users
based on the requests as seen by gabelmoo, one of the directory
authorities.</p>
<a id="new-users" />
<%
StringBuilder newUsersUrl = new StringBuilder("new-users.png");
if ("new-users".equals(request.getParameter("graph"))) {
  List<String> parameters = new ArrayList<String>();
// TODO check values here!
  String startParameter = request.getParameter("start"),
      endParameter = request.getParameter("end");
  String[] countryParameters = request.getParameterValues("country");
  if (startParameter != null && startParameter.length() > 0) {
    parameters.add("start=" + startParameter);
  }
  if (endParameter != null && endParameter.length() > 0) {
    parameters.add("end=" + endParameter);
  }
  if (countryParameters != null && countryParameters.length > 0) {
    for (String country : countryParameters) {
      if (country != null && country.length() > 0) {
        parameters.add("country=" + country);
      }
    }
  }
  if (parameters.size() > 0) {
    newUsersUrl.append("?" + parameters.get(0));
    if (parameters.size() > 1) {
      for (int i = 1; i < parameters.size(); i++) {
        newUsersUrl.append("&" + parameters.get(i));
      }
    }
  }
}
out.println("<img src=\"" + newUsersUrl.toString() + "\" width=\"576\" "
    + "height=\"360\" />");
%>
</p>
<form action="users.html#new-users">
  <div class="formrow">
    <input type="hidden" name="graph" value="new-users"/>
    <p>
    <label class="startend" for="start">Start date (yyyy-mm-dd):</label>
      <input type="text" name="start" id="start" size="10"
        value="<%=("new-users".equals(request.getParameter("graph")) &&
                   request.getParameter("start") != null) ?
                      request.getParameter("start") : ""%>"/>
    <label class="startend" for="end">End date (yyyy-mm-dd):</label>
      <input type="text" name="end" id="end" size="10"
        value="<%=("new-users".equals(request.getParameter("graph")) &&
                   request.getParameter("end") != null) ?
                      request.getParameter("end") : ""%>"/>
    </p><p>
      <label>Source: </label>
      <input type="radio" name="country" value="all">All users</input>
      <input type="radio" name="country" value="au">Australia</input>
      <input type="radio" name="country" value="bh">Bahrain</input>
      <input type="radio" name="country" value="br">Brazil</input>
      <input type="radio" name="country" value="ca">Canada</input>
      <input type="radio" name="country" value="cn">China</input>
      <input type="radio" name="country" value="cu">Cuba</input>
      <input type="radio" name="country" value="de">Germany</input>
      <input type="radio" name="country" value="et">Ethiopia</input>
      <input type="radio" name="country" value="fr">France</input>
      <input type="radio" name="country" value="gb">U.K.</input>
      <input type="radio" name="country" value="ir">Iran</input>
      <input type="radio" name="country" value="it">Italy</input>
      <input type="radio" name="country" value="jp">Japan</input>
      <input type="radio" name="country" value="kr">South Korea</input>
      <input type="radio" name="country" value="mm">Burma</input>
      <input type="radio" name="country" value="pl">Poland</input>
      <input type="radio" name="country" value="ru">Russia</input>
      <input type="radio" name="country" value="sa">Saudi Arabia</input>
      <input type="radio" name="country" value="se">Sweden</input>
      <input type="radio" name="country" value="sy">Syria</input>
      <input type="radio" name="country" value="tn">Tunisia</input>
      <input type="radio" name="country" value="tm">Turkmenistan</input>
      <input type="radio" name="country" value="us">U.S.A.</input>
      <input type="radio" name="country" value="uz">Uzbekistan</input>
      <input type="radio" name="country" value="vn">Vietnam</input>
      <input type="radio" name="country" value="ye">Yemen</input>
    </p><p>
    <input class="submit" type="submit" value="Update graph"/>
    </p>
  </div>
</form>
<p><a href="csv/new-users.csv">CSV</a> file containing all data.</p>
<br/>

<h3>Recurring, directly connecting Tor users</h3>
<br/>
<p>After being connected to the Tor network, users need to refresh their
list of running relays on a regular basis. They send their requests to one
out of a few hundred directory mirrors to save bandwidth of the directory
authorities. The following graphs show an estimate of recurring Tor users
based on the requests as seen by trusted, a particularly fast directory
mirror.</p>
<a id="direct-users" />
<%
StringBuilder directUsersUrl = new StringBuilder("direct-users.png");
if ("direct-users".equals(request.getParameter("graph"))) {
  List<String> parameters = new ArrayList<String>();
// TODO check values here!
  String startParameter = request.getParameter("start"),
      endParameter = request.getParameter("end");
  String[] countryParameters = request.getParameterValues("country");
  if (startParameter != null && startParameter.length() > 0) {
    parameters.add("start=" + startParameter);
  }
  if (endParameter != null && endParameter.length() > 0) {
    parameters.add("end=" + endParameter);
  }
  if (countryParameters != null && countryParameters.length > 0) {
    for (String country : countryParameters) {
      if (country != null && country.length() > 0) {
        parameters.add("country=" + country);
      }
    }
  }
  if (parameters.size() > 0) {
    directUsersUrl.append("?" + parameters.get(0));
    if (parameters.size() > 1) {
      for (int i = 1; i < parameters.size(); i++) {
        directUsersUrl.append("&" + parameters.get(i));
      }
    }
  }
}
out.println("<img src=\"" + directUsersUrl.toString() + "\" width=\"576\" "
    + "height=\"360\" />");
%>
</p>
<form action="users.html#direct-users">
  <div class="formrow">
    <input type="hidden" name="graph" value="direct-users"/>
    <p>
    <label class="startend" for="start">Start date (yyyy-mm-dd):</label>
      <input type="text" name="start" id="start" size="10"
        value="<%=("direct-users".equals(request.getParameter("graph")) &&
                   request.getParameter("start") != null) ?
                      request.getParameter("start") : ""%>"/>
    <label class="startend" for="end">End date (yyyy-mm-dd):</label>
      <input type="text" name="end" id="end" size="10"
        value="<%=("direct-users".equals(request.getParameter("graph")) &&
                   request.getParameter("end") != null) ?
                      request.getParameter("end") : ""%>"/>
    </p><p>
      <label>Source: </label>
      <input type="radio" name="country" value="all">All users</input>
      <input type="radio" name="country" value="au">Australia</input>
      <input type="radio" name="country" value="bh">Bahrain</input>
      <input type="radio" name="country" value="br">Brazil</input>
      <input type="radio" name="country" value="ca">Canada</input>
      <input type="radio" name="country" value="cn">China</input>
      <input type="radio" name="country" value="cu">Cuba</input>
      <input type="radio" name="country" value="de">Germany</input>
      <input type="radio" name="country" value="et">Ethiopia</input>
      <input type="radio" name="country" value="fr">France</input>
      <input type="radio" name="country" value="gb">U.K.</input>
      <input type="radio" name="country" value="ir">Iran</input>
      <input type="radio" name="country" value="it">Italy</input>
      <input type="radio" name="country" value="jp">Japan</input>
      <input type="radio" name="country" value="kr">South Korea</input>
      <input type="radio" name="country" value="mm">Burma</input>
      <input type="radio" name="country" value="pl">Poland</input>
      <input type="radio" name="country" value="ru">Russia</input>
      <input type="radio" name="country" value="sa">Saudi Arabia</input>
      <input type="radio" name="country" value="se">Sweden</input>
      <input type="radio" name="country" value="sy">Syria</input>
      <input type="radio" name="country" value="tn">Tunisia</input>
      <input type="radio" name="country" value="tm">Turkmenistan</input>
      <input type="radio" name="country" value="us">U.S.A.</input>
      <input type="radio" name="country" value="uz">Uzbekistan</input>
      <input type="radio" name="country" value="vn">Vietnam</input>
      <input type="radio" name="country" value="ye">Yemen</input>
    </p><p>
    <input class="submit" type="submit" value="Update graph"/>
    </p>
  </div>
</form>
<p><a href="csv/direct-users.csv">CSV</a> file containing all data.</p>
<p><a href="csv/monthly-users-peak.csv">CSV</a> file containing peak daily
Tor users (recurring and bridge) per month by country.</p>
<p><a href="csv/monthly-users-average.csv">CSV</a> file containing average
daily Tor users (recurring and bridge) per month by country.</p>
<br/>

<h3>Tor users via bridges</h3>
<br/>
<p>Users who cannot connect directly to the Tor network instead connect
via bridges, which are non-public relays. The following graphs display an
estimate of Tor users via bridges based on the unique IP addresses as seen
by a few hundred bridges.</p>
<a id="bridge-users" />
<%
StringBuilder bridgeUsersUrl = new StringBuilder("bridge-users.png");
if ("bridge-users".equals(request.getParameter("graph"))) {
  List<String> parameters = new ArrayList<String>();
// TODO check values here!
  String startParameter = request.getParameter("start"),
      endParameter = request.getParameter("end");
  String[] countryParameters = request.getParameterValues("country");
  if (startParameter != null && startParameter.length() > 0) {
    parameters.add("start=" + startParameter);
  }
  if (endParameter != null && endParameter.length() > 0) {
    parameters.add("end=" + endParameter);
  }
  if (countryParameters != null && countryParameters.length > 0) {
    for (String country : countryParameters) {
      if (country != null && country.length() > 0) {
        parameters.add("country=" + country);
      }
    }
  }
  if (parameters.size() > 0) {
    bridgeUsersUrl.append("?" + parameters.get(0));
    if (parameters.size() > 1) {
      for (int i = 1; i < parameters.size(); i++) {
        bridgeUsersUrl.append("&" + parameters.get(i));
      }
    }
  }
}
out.println("<img src=\"" + bridgeUsersUrl.toString() + "\" width=\"576\" "
    + "height=\"360\" />");
%>
</p>
<form action="users.html#bridge-users">
  <div class="formrow">
    <input type="hidden" name="graph" value="bridge-users"/>
    <p>
    <label class="startend" for="start">Start date (yyyy-mm-dd):</label>
      <input type="text" name="start" id="start" size="10"
        value="<%=("bridge-users".equals(request.getParameter("graph")) &&
                   request.getParameter("start") != null) ?
                      request.getParameter("start") : ""%>"/>
    <label class="startend" for="end">End date (yyyy-mm-dd):</label>
      <input type="text" name="end" id="end" size="10"
        value="<%=("bridge-users".equals(request.getParameter("graph")) &&
                   request.getParameter("end") != null) ?
                      request.getParameter("end") : ""%>"/>
    </p><p>
      <label>Source: </label>
      <input type="radio" name="country" value="all">All users</input>
      <input type="radio" name="country" value="au">Australia</input>
      <input type="radio" name="country" value="bh">Bahrain</input>
      <input type="radio" name="country" value="br">Brazil</input>
      <input type="radio" name="country" value="ca">Canada</input>
      <input type="radio" name="country" value="cn">China</input>
      <input type="radio" name="country" value="cu">Cuba</input>
      <input type="radio" name="country" value="de">Germany</input>
      <input type="radio" name="country" value="et">Ethiopia</input>
      <input type="radio" name="country" value="fr">France</input>
      <input type="radio" name="country" value="gb">U.K.</input>
      <input type="radio" name="country" value="ir">Iran</input>
      <input type="radio" name="country" value="it">Italy</input>
      <input type="radio" name="country" value="jp">Japan</input>
      <input type="radio" name="country" value="kr">South Korea</input>
      <input type="radio" name="country" value="mm">Burma</input>
      <input type="radio" name="country" value="pl">Poland</input>
      <input type="radio" name="country" value="ru">Russia</input>
      <input type="radio" name="country" value="sa">Saudi Arabia</input>
      <input type="radio" name="country" value="se">Sweden</input>
      <input type="radio" name="country" value="sy">Syria</input>
      <input type="radio" name="country" value="tn">Tunisia</input>
      <input type="radio" name="country" value="tm">Turkmenistan</input>
      <input type="radio" name="country" value="us">U.S.A.</input>
      <input type="radio" name="country" value="uz">Uzbekistan</input>
      <input type="radio" name="country" value="vn">Vietnam</input>
      <input type="radio" name="country" value="ye">Yemen</input>
    </p><p>
    <input class="submit" type="submit" value="Update graph"/>
    </p>
  </div>
</form>
<p><a href="csv/bridge-users.csv">CSV</a> file containing all data.</p>
<p><a href="csv/monthly-users-peak.csv">CSV</a> file containing peak daily
Tor users (recurring and bridge) per month by country.</p>
<p><a href="csv/monthly-users-average.csv">CSV</a> file containing average
daily Tor users (recurring and bridge) per month by country.</p>
<br/>

