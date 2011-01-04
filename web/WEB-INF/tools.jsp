<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
  <title>Tor Metrics Portal: Tools</title>
  <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">
  <link href="/css/stylesheet-ltr.css" type="text/css" rel="stylesheet">
  <link href="/images/favicon.ico" type="image/x-icon" rel="shortcut icon">
</head>
<body>
  <div class="center">
    <%@ include file="banner.jsp"%>
    <div class="main-column">
        <h2>Tor Metrics Portal: Tools</h2>
        <br>
        <p>This page contains a collection of tools that can be used to
        gather statistics as provided on the <a href="data.html">Data</a>
        page and to process the resulting files to generate
        <a href="graphs.html">graphs</a>.</p>
        <ul>
          <li><a href="#metrics-db">Metrics database</a></li>
          <li><a href="#metrics-web">Metrics website</a></li>
          <li><a href="#dirarch">Directory-archive script</a></li>
          <li><a href="#bridgesan">Bridge descriptor sanitizer</a></li>
          <li><a href="#torperf">Torperf</a></li>
          <li><a href="#exonerator">ExoneraTor</a></li>
          <li><a href="#visitor">VisiTor</a></li>
        </ul>
        <br>
        <a name="metrics-db"></a>
        <h3>Metrics database</h3>
        <br>
        <p>The metrics database software is a Java application that parses
        Tor's directory data and imports it into a PostgreSQL database.
        This database can then be used to look up relays or perform
        statistical analysis on the Tor network. The metrics database is
        also used by this website.</p>
        <ul>
          <li>Browse the
          <a href="https://gitweb.torproject.org/metrics-db.git/tree">Git
          repository</a></li>
          <li><tt>git clone git://git.torproject.org/metrics-db</tt></li>
        </ul>
        <br>
        <a name="metrics-web"></a>
        <h3>Metrics website</h3>
        <br>
        <p>The metrics website software is a Tomcat application that
        accesses the metrics database to generate this website. The
        metrics website uses R to generate custom graphs on demand.</p>
        <ul>
          <li>Browse the
          <a href="https://gitweb.torproject.org/metrics-web.git/tree">Git
          repository</a></li>
          <li><tt>git clone git://git.torproject.org/metrics-web</tt></li>
        </ul>
        <br>
        <a name="dirarch"></a>
        <h3>Directory-archive script</h3>
        <br>
        <p>The directory-archive script consists of a bunch of shell
        scripts that periodically download relay descriptors, sort them
        into a directory structure, and compile monthly tarballs. The
        tarballs are quite similar to the ones provided on the
        <a href="data.html#relaydesc">Data</a> page, with a few
        exceptions: the provided tarballs are the result of combining two
        directory-archive script outputs, splitting v3 votes and v3
        consensuses into separate tarballs and replacing all colons in
        filenames with dashes. The <a href="#metrics-db">metrics database
        software</a> contains similar functionality to the
        directory-archive script.</p>
        <ul>
          <li>Browse the <a href="https://gitweb.torproject.org/tor.git/tree/HEAD:/contrib/directory-archive">contrib/directory-archive/</a>
          subdirectory in the Tor sources</li>
          <li><tt>git clone git://git.torproject.org/tor</tt></li>
        </ul>
        <br>
        <a name="bridgesan"></a>
        <h3>Bridge descriptor sanitizer</h3>
        <br>
        <p>The bridge authority Tonga maintains a list of bridges in order
        to serve bridge addresses and descriptors to its clients. Every
        half hour, Tonga takes a snapshot of the known bridge descriptors
        and copies them to byblos for later statistical analysis. As a
        guiding principle, the Tor project makes all data that it uses for
        statistical analysis available to the interested public, in order
        to maximize transparency towards the community. However, the
        bridge descriptors contain the IP addresses and other contact
        information of bridges that must not be made public, or the
        purpose of bridges as non-public entry points into the Tor network
        would be obsolete. This script takes the half-hourly snapshots as
        input, removes all possibly sensitive information from the
        descriptors, and puts out the sanitized bridge descriptors that
        are safe to be published. The <a href="#metrics-db">metrics
        database software</a> contains similar functionality to the bridge
        descriptor sanitizer.</p>
        <ul>
          <li>Browse the <a href="https://gitweb.torproject.org/metrics-utils.git/tree/HEAD:/bridge-desc-sanitizer">Git repository</a></li>
          <li><tt>git clone git://git.torproject.org/metrics-utils</tt></li>
        </ul>
        <br>
        <a name="torperf"></a>
        <h3>Torperf</h3>
        <br>
        <p>Torperf is a little tool that measures Tor's performance as
        users experience it. Torperf uses a trivial SOCKS client to
        download files of various sizes over the Tor network and notes how
        long substeps take.</p>
        <ul>
          <li>Browse the <a href="https://gitweb.torproject.org/torperf.git">Git repository</a></li>
          <li><tt>git clone git://git.torproject.org/torperf</tt></li>
        </ul>
        <br>
        <a name="exonerator"></a>
        <h3>ExoneraTor</h3>
        <br>
        <p>ExoneraTor parses the relay descriptor archives to answer the
        question whether some IP address was a Tor relay. This script is
        available as a Python and a Java version with equivalent
        functionality. There is also a web version of
        <a href="/exonerator.html">ExoneraTor</a> available.</p>
        <ul>
          <li>Download
          <a href="dist/exonerator-0.0.2.tar">ExoneraTor 0.0.2</a>
          (<a href="dist/exonerator-0.0.2.tar.asc">sig</a>)
          <li>Browse the <a href="https://gitweb.torproject.org/metrics-utils.git/tree/HEAD:/exonerator">Git repository</a>
          <li><tt>git clone git://git.torproject.org/metrics-utils</tt></li>
        </ul>
        <br>
        <a name="visitor"></a>
        <h3>VisiTor</h3>
        <br>
        <p>VisiTor is a script that parses a web server log and the exit
        list archives to tell how many of the requests come from Tor
        users.</p>
        <ul>
          <li>Download
          <a href="dist/visitor-0.0.4.tar">VisiTor 0.0.4</a>
          (<a href="dist/visitor-0.0.4.tar.asc">sig</a>)
          <li>Browse the <a href="https://gitweb.torproject.org/metrics-utils.git/tree/HEAD:/visitor">Git repository</a></li>
          <li><tt>git clone git://git.torproject.org/metrics-utils</tt></li>
        </ul>
    </div>
  </div>
  <div class="bottom" id="bottom">
    <%@ include file="footer.jsp"%>
  </div>
</body>
</html>
