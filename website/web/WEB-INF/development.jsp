<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<jsp:include page="top.jsp">
  <jsp:param name="pageTitle" value="Development &ndash; Tor Metrics"/>
  <jsp:param name="navActive" value="Development"/>
</jsp:include>

    <div class="container">
      <ul class="breadcrumb">
        <li><a href="index.html">Home</a></li>
        <li class="active">Development</li>
      </ul>
    </div>

    <div class="container">
      <h1>Development</h1>
<ul>
<li><a href="https://play.google.com/store/apps/details?id=com.networksaremadeofstring.anonionooid">AnOnionooid</a> is an Android app that helps find and explore Tor relays and bridges.</li>
<li>Tor's <a href="https://gitweb.torproject.org/tor.git/tree/scripts/maint/updateFallbackDirs.py">fallback directories script</a> generates a list of stable directories.</li>
<li><a href="https://github.com/duk3luk3/onion-py">OnionPy</a> provides memcached support to cache queried data.</li>
<li><a href="https://nos-oignons.net/Services/index.en.html">Nos oignons</a> visualizes bandwidth histories of their relays.</li>
<li><a href="https://metrics.torproject.org/uncharted-data-flow.html">metrics-lib</a> is a Java library to fetch and pars
e Tor descriptors.</li>
<li><a href="https://stem.torproject.org/">Stem</a> is a Python library that parses Tor descriptors.</li>
<li><a href="https://github.com/meejah/txtorcon">Txtorcon</a> is an asynchronous Tor controller library written in Twiste
d Python.</li>
<li><a href="https://github.com/NullHypothesis/zoossh">Zoossh</a> is a parser written in Go for Tor-specific data formats
.</li>
<li><a href="https://savannah.nongnu.org/projects/koninoo/">koninoo</a> is a simple Java command line interface for query
ing Onionoo data.</li>
<li><a href="https://gitweb.torproject.org/user/phw/exitmap.git">Exitmap</a> is a fast and extensible scanner for Tor exi
t relays.</li>
<li><a href="https://gitweb.torproject.org/user/phw/sybilhunter.git/">Sybilhunter</a> attempts to detect Sybil attacks on
 the Tor network.</li>
</ul>
    </div>

<jsp:include page="bottom.jsp"/>

