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
          <li><a href="#metrics-db">Metrics data processor</a></li>
          <li><a href="#metrics-web">Metrics website</a></li>
          <li><a href="#dirarch">Directory-archive script</a></li>
          <li><a href="#torperf">Torperf</a></li>
          <li><a href="#exonerator">ExoneraTor</a></li>
          <li><a href="#visitor">VisiTor</a></li>
        </ul>
        <br>
        <a name="metrics-db"></a>
        <h3><a href="#metrics-db" class="anchor">Metrics data
        processor</a></h3>
        <br>
        <p>The metrics data processor is a Java application that parses
        Tor's directory data and the data from various other Tor services,
        possibly removes sensitive parts from them, and then outputs the
        <a href="data.html">data</a> in <a href="formats.html">formats</a>
        that are feasible for later analysis.</p>
        <ul>
          <li>Browse the
          <a href="https://gitweb.torproject.org/metrics-db.git/tree">Git
          repository</a></li>
          <li><tt>git clone git://git.torproject.org/metrics-db</tt></li>
        </ul>
        <br>
        <a name="metrics-web"></a>
        <h3><a href="#metrics-web" class="anchor">Metrics website</a></h3>
        <br>
        <p>The metrics website software consists of a Java database
        importer and Tomcat application that makes Tor's directory data
        easily accessible.  This website is run by the metrics website
        software.</p>
        <ul>
          <li>Download
          <a href="dist/metrics-web-0.0.1.tar">metrics-web 0.0.1</a>
          (<a href="dist/metrics-web-0.0.1.tar.asc">sig</a>)</li>
          <li>Browse the
          <a href="https://gitweb.torproject.org/metrics-web.git/tree">Git
          repository</a></li>
          <li><tt>git clone git://git.torproject.org/metrics-web</tt></li>
        </ul>
        <br>
        <a name="dirarch"></a>
        <h3><a href="#dirarch" class="anchor">Directory-archive
        script</a></h3>
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
        <a name="torperf"></a>
        <h3><a href="#torperf" class="anchor">Torperf</a></h3>
        <br>
        <p>Torperf is a little tool that measures Tor's performance as
        users experience it. Torperf uses a trivial SOCKS client to
        download files of various sizes over the Tor network and notes how
        long substeps take.</p>
        <ul>
          <li>Download
          <a href="dist/torperf-0.0.1.tar">Torperf 0.0.1</a>
          (<a href="dist/torperf-0.0.1.tar.asc">sig</a>)</li>
          <li>Browse the <a href="https://gitweb.torproject.org/torperf.git">Git repository</a></li>
          <li><tt>git clone git://git.torproject.org/torperf</tt></li>
        </ul>
        <br>
        <a name="exonerator"></a>
        <h3><a href="#exonerator" class="anchor">ExoneraTor</a></h3>
        <br>
        <p>ExoneraTor parses the relay descriptor archives to answer the
        question whether some IP address was a Tor relay. This script is
        available as a Python and a Java version with equivalent
        functionality. There is also a web version of
        <a href="/exonerator.html">ExoneraTor</a> available.</p>
        <ul>
          <li>Download
          <a href="dist/exonerator-0.0.2.tar">ExoneraTor 0.0.2</a>
          (<a href="dist/exonerator-0.0.2.tar.asc">sig</a>)</li>
          <li>Browse the <a href="https://gitweb.torproject.org/metrics-utils.git/tree/HEAD:/exonerator">Git repository</a>
          <li><tt>git clone git://git.torproject.org/metrics-utils</tt></li>
        </ul>
        <br>
        <a name="visitor"></a>
        <h3><a href="#visitor" class="anchor">VisiTor</a></h3>
        <br>
        <p>VisiTor is a script that parses a web server log and the exit
        list archives to tell how many of the requests come from Tor
        users. VisiTor expects exit lists in the format described in
        <a href="https://www.torproject.org/tordnsel/exitlist-spec.txt">exitlist-spec.txt</a>.</p>
        <ul>
          <li>Download
          <a href="dist/visitor-0.0.4.tar">VisiTor 0.0.4</a>
          (<a href="dist/visitor-0.0.4.tar.asc">sig</a>)</li>
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
