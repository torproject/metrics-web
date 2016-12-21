<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<jsp:include page="top.jsp">
  <jsp:param name="pageTitle" value="${categoryHeader} &ndash; Tor Metrics"/>
  <jsp:param name="navActive" value="${categoryHeader}"/>
</jsp:include>

    <div class="container">
      <ul class="breadcrumb">
        <li><a href="index.html">Home</a></li>
        <li class="active">${categoryHeader}</li>
      </ul>
    </div>

    <div class="container">
      <h1>${categoryHeader}</h1>
      <p>${categoryDescription}</p>
    </div>

    <div class="container">

      <!-- tabs -->
      <ul class="nav nav-tabs">
        <c:forEach var="tab" items="${categoryTabs}">
        <li role="presentation"<c:if test="${id.equals(tab[1])}"> class="active"</c:if>><a href="${tab[1]}.html" data-tab="${tab[1]}">${tab[0]}</a></li>
        </c:forEach>
      </ul>

      <!-- tab-content -->
      <div class="tab-content">
        <div class="tab-pane active" id="tab-${tab[1]}">

<p>The following graph visualizes diversity of currently running
<a href="about.html#relay">relays</a> in terms of their probability to be
selected for <a href="about.html#circuit">circuits</a>.
Fast relays with at least 100 Mbit/s bandwidth capacity, and which
therefore have a high probability of being selected for circuits, are
represented by an onion; smaller relays are shown as a simple dot; and the
slowest relays, which are almost never selected for circuits, are omitted
entirely.
Graphs in the "all relays" category use a relay's
<a href="about.html#consensus-weight">consensus weight</a> as probability,
whereas graphs in the "exits only" category use a value derived from a
relay's consensus weight that resembles the probability of selecting that
relay as exit node.
All graphs support grouping relays by same autonomous system, contact
information, country, or network family.</p>

      <p>
        All relays:
        <a href="#no-group" onclick="make_bubble_graph('no-group');">No group</a> |
        <a href="#as" onclick="make_bubble_graph('as');">Autonomous Systems</a> |
        <a href="#contact" onclick="make_bubble_graph('contact');">Contact</a>  |
        <a href="#country" onclick="make_bubble_graph('country');">Country</a> |
        <a href="#network-family" onclick="make_bubble_graph('network-family');">Network family (/16)</a>
      </p>
      <p>
        Exits only:
        <a href="#no-group-exits-only" onclick="make_bubble_graph('no-group-exits-only');">No group</a> |
        <a href="#as-exits-only" onclick="make_bubble_graph('as-exits-only');">Autonomous Systems</a> |
        <a href="#contact-exits-only" onclick="make_bubble_graph('contact-exits-only');">Contact</a>  |
        <a href="#country-exits-only" onclick="make_bubble_graph('country-exits-only');">Country</a> |
        <a href="#network-family-exits-only" onclick="make_bubble_graph('network-family-exits-only');">Network family (/16)</a>
      </p>
      <div id="bubble-graph-placeholder"></div>
      <script src="js/d3.min.js"></script>
      <script src="js/bubbles.js"></script>
      <script>make_bubble_graph();</script>
      <noscript>Sorry, you need to turn on JavaScript.</noscript>

        </div>
      </div><!-- tab-content -->
    </div><!-- container -->

<jsp:include page="bottom.jsp"/>

