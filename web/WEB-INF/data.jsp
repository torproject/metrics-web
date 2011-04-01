<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setLocale value="en_US"/>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
  <title>Tor Metrics Portal: Data</title>
  <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">
  <link href="/css/stylesheet-ltr.css" type="text/css" rel="stylesheet">
  <link href="/images/favicon.ico" type="image/x-icon" rel="shortcut icon">
</head>
<body>
  <div class="center">
    <%@ include file="banner.jsp"%>
    <div class="main-column">
        <h2>Tor Metrics Portal: Data</h2>
        <br>
        <p>One of the main goals of the Tor Metrics Project is to make all
        gathered data available to the public. This approach enables
        privacy researchers to perform their own analyses using real data
        on the Tor network, and it acts as a safeguard to not gather data
        that are too sensitive to publish. All signatures can be
        <a href="https://www.torproject.org/docs/verifying-signatures">verified</a>
        using Karsten's PGP key (0xF7C11265). The following data are
        available (see the <a href="tools.html">Tools</a> section for
        details on processing the files):</p>
        <ul>
          <li><a href="#relaydesc">Relay descriptor archives</a></li>
          <li><a href="#bridgedesc">Bridge descriptor archives</a></li>
          <li><a href="#bridgeassignments">Bridge pool assignments</a></li>
          <li><a href="#stats">Statistics produced by relays</a></li>
          <li><a href="#performance">Performance data</a></li>
          <li><a href="#exitlist">Exit lists</a></li>
        </ul>
        <br>
        <a name="relaydesc"></a>
        <h3>Relay descriptor archives</h3>
        <br>
        <p>The relay descriptor archives contain all documents that the
        directory authorities make available about the network of relays.
        These documents include network statuses, server (relay)
        descriptors, and extra-info descriptors:</p>
        <table width="100%" border="0" cellpadding="5" cellspacing="0" summary="">
          <c:forEach var="item" items="${relayDescriptors}" >
            <fmt:formatDate var="longDate" pattern="MMMM yyyy"
                            value="${item.key}"/>
            <tr>
              <td>${longDate}</td>
              <td>
                <c:if test="${item.value['tor'] ne null}" >
                  <a href="${item.value['tor'][0]}">v1 directories</a>
                  <c:if test="${item.value['tor'][1] ne null}">
                    (<a href="${item.value['tor'][1]}">sig</a>)
                  </c:if>
                </c:if>
              </td>
              <td>
                <c:if test="${item.value['statuses'] ne null}" >
                  <a href="${item.value['statuses'][0]}">v2 statuses</a>
                  <c:if test="${item.value['statuses'][1] ne null}">
                    (<a href="${item.value['statuses'][1]}">sig</a>)
                  </c:if>
                </c:if>
              </td>
              <td>
                <c:if test="${item.value['server-descriptors'] ne null}" >
                  <a href="${item.value['server-descriptors'][0]}">server descriptors</a>
                  <c:if test="${item.value['server-descriptors'][1] ne null}">
                    (<a href="${item.value['server-descriptors'][1]}">sig</a>)
                  </c:if>
                </c:if>
              </td>
              <td>
                <c:if test="${item.value['extra-infos'] ne null}" >
                  <a href="${item.value['extra-infos'][0]}">extra-infos</a>
                  <c:if test="${item.value['extra-infos'][1] ne null}">
                    (<a href="${item.value['extra-infos'][1]}">sig</a>)
                  </c:if>
                </c:if>
              </td>
              <td>
                <c:if test="${item.value['votes'] ne null}" >
                  <a href="${item.value['votes'][0]}">v3 votes</a>
                  <c:if test="${item.value['votes'][1] ne null}">
                    (<a href="${item.value['votes'][1]}">sig</a>)
                  </c:if>
                </c:if>
              </td>
              <td>
                <c:if test="${item.value['consensuses'] ne null}" >
                  <a href="${item.value['consensuses'][0]}">v3 consensuses</a>
                  <c:if test="${item.value['consensuses'][1] ne null}">
                    (<a href="${item.value['consensuses'][1]}">sig</a>)
                  </c:if>
                </c:if>
              </td>
            </tr>
          </c:forEach>
        </table>
        <c:if test="${certs[0] ne null}">
          <br>
          <p>In order to verify the v3 votes and v3 consensuses, download
          the tarball of <a href="${certs[0]}">v3 certificates</a>
          <c:if test="${certs[1] ne null}">
            (<a href="${certs[1]}">sig</a>)
          </c:if>
          which is updated whenever new v3 certificates become available.</p>
        </c:if>
        <br>
        <a name="bridgedesc"></a>
        <h3>Bridge descriptor archives</h3>
        <br>
        <p>The bridge descriptor archives contain similar documents as the
        relay descriptor archives, but for the non-public bridges. The
        descriptors have been sanitized before publication to remove all
        information that could otherwise be used to locate bridges.
        Beginning with May 2010, we stopped resolving IP addresses to
        country codes and including those in the sanitized descriptors,
        because it was tough to maintain; if your research requires this
        or any other detail, contact us and we'll sort something out. The
        files below contain all documents of a given month:</p>
        <table width="100%" border="0" cellpadding="5" cellspacing="0" summary="">
          <c:forEach var="item" items="${bridgeDescriptors}" >
            <fmt:formatDate var="longDate" pattern="MMMM yyyy"
                            value="${item.key}"/>
            <tr>
              <td>
                <a href="${item.value[0]}">${longDate}</a>
                <c:if test="${item.value[1] ne null}">
                    (<a href="${item.value[1]}">sig</a>)
                </c:if>
              </td>
            </tr>
          </c:forEach>
        </table>
        <p></p>
        <br>
        <a name="bridgeassignments"></a>
        <h3>Bridge pool assignments</h3>
        <br>
        <p>BridgeDB periodically dumps the list of running bridges with
        information about the rings, subrings, and file buckets to which
        they are assigned to a local file.  We are archiving sanitized
        versions of these files here to analyze how the pool assignment
        affects a bridge's usage.</p>
        <table width="100%" border="0" cellpadding="5" cellspacing="0" summary="">
          <c:forEach var="item" items="${bridgePoolAssignments}" >
            <fmt:formatDate var="longDate" pattern="MMMM yyyy"
                            value="${item.key}"/>
            <tr>
              <td>
                <a href="${item.value[0]}">${longDate}</a>
              </td>
            </tr>
          </c:forEach>
        </table>
        <br>
        <a name="stats"></a>
        <h3>Statistics produced by relays</h3>
        <br>
        <p>Some of the relays are configured to gather statistics on the
        number of requests or connecting clients, the number of processed
        cells per queue, or the number of exiting bytes per port. Relays
        running version 0.2.2.4-alpha can include these statistics in
        extra-info descriptors, so that they are included in the relay
        descriptor archives. The following files contain the statistics
        produced by relays running earlier versions:</p>
        <table width="100%" border="0" cellpadding="5" cellspacing="0" summary="">
          <c:forEach var="item" items="${relayStatistics}" >
            <tr>
              <td>${item.key}</td>
              <td>
                <c:if test="${item.value['buffer'] ne null}" >
                  <a href="${item.value['buffer'][0]}">buffer-stats</a>
                  <c:if test="${item.value['buffer'][1] ne null}">
                    (<a href="${item.value['buffer'][1]}">sig</a>)
                  </c:if>
                </c:if>
              </td>
              <td>
                <c:if test="${item.value['dirreq'] ne null}" >
                  <a href="${item.value['dirreq'][0]}">dirreq-stats</a>
                  <c:if test="${item.value['dirreq'][1] ne null}">
                    (<a href="${item.value['dirreq'][1]}">sig</a>)
                  </c:if>
                </c:if>
              </td>
              <td>
                <c:if test="${item.value['entry'] ne null}" >
                  <a href="${item.value['entry'][0]}">entry-stats</a>
                  <c:if test="${item.value['entry'][1] ne null}">
                    (<a href="${item.value['entry'][1]}">sig</a>)
                  </c:if>
                </c:if>
              </td>
              <td>
                <c:if test="${item.value['exit'] ne null}" >
                  <a href="${item.value['exit'][0]}">exit-stats</a>
                  <c:if test="${item.value['exit'][1] ne null}">
                    (<a href="${item.value['exit'][1]}">sig</a>)
                  </c:if>
                </c:if>
              </td>
            </tr>
          </c:forEach>
        </table>
        <br>
        <a name="performance"></a>
        <h3>Performance data</h3>
        <br>
        <p>We are measuring the performance of the Tor network by
        periodically requesting files of different sizes and recording the
        time needed to do so. The main measurements on moria, siv, and
        torperf use an unmodified Tor client. The four additional setups
        on torperf are configured to pick their guard nodes from sets of
        the a) absolute fastest, b) absolute slowest, c) best rated vs.
        advertised ratio or d) worst rated vs. advertised ratio nodes. The
        ratio mechanisms provide a way to select the nodes that the
        bandwidth authorities think stand out in their measurement. The
        files below contain the output of the torperf application and are
        updated every hour:</p>
        <table width="100%" border="0" cellpadding="5" cellspacing="0" summary="">
          <c:forEach var="item" items="${torperfData}" >
            <tr>
              <td>${item.key}</td>
              <td>
                <c:if test="${item.value['50kb'] ne null}" >
                  <c:if test="${item.value['50kb'][0] ne null}" >
                    <a href="${item.value['50kb'][0]}">50 KiB requests</a>
                  </c:if>
                  <c:if test="${item.value['50kb'][1] ne null}" >
                    <a href="${item.value['50kb'][1]}">50 KiB path info</a>
                  </c:if>
                </c:if>
              </td>
              <td>
                <c:if test="${item.value['1mb'] ne null}" >
                  <c:if test="${item.value['1mb'][0] ne null}" >
                    <a href="${item.value['1mb'][0]}">1 MiB requests</a>
                  </c:if>
                  <c:if test="${item.value['1mb'][1] ne null}" >
                    <a href="${item.value['1mb'][1]}">1 MiB path info</a>
                  </c:if>
                </c:if>
              </td>
              <td>
                <c:if test="${item.value['5mb'] ne null}" >
                  <c:if test="${item.value['5mb'][0] ne null}" >
                    <a href="${item.value['5mb'][0]}">5 MiB requests</a>
                  </c:if>
                  <c:if test="${item.value['5mb'][1] ne null}" >
                    <a href="${item.value['5mb'][1]}">5 MiB path info</a>
                  </c:if>
                </c:if>
              </td>
            </tr>
          </c:forEach>
        </table>
        <br>
        <a name="exitlist"></a>
        <h3>Exit lists</h3>
        <br>
        <p>We are archiving the bulk exit lists used by
        <a href="https://check.torproject.org/">Tor Check</a> (see
        <a href="https://www.torproject.org/tordnsel/exitlist-spec.txt">exitlist-spec.txt</a>)
        containing the IP addresses that exit relays exit from:</p>
        <table width="100%" border="0" cellpadding="5" cellspacing="0" summary="">
          <c:forEach var="item" items="${exitLists}" >
            <fmt:formatDate var="longDate" pattern="MMMM yyyy"
                            value="${item.key}"/>
            <tr>
              <td>
                <a href="${item.value[0]}">${longDate}</a>
              </td>
            </tr>
          </c:forEach>
        </table>
    </div>
  </div>
  <div class="bottom" id="bottom">
    <%@ include file="footer.jsp"%>
  </div>
</body>
</html>
