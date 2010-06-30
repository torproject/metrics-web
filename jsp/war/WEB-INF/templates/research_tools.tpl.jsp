      <div>
        <h2>Tor Metrics Portal: Tools</h2>
        <br/>
        <p>This page contains a collection of tools that can be used to
        gather statistics as provided on the <a href="data.html">data</a>
        page and to process the resulting files to generate
        <a href="graphs.html">graphs</a>.</p>
        <ul>
          <li><a href="#ernie">Metrics portal software</a></li>
          <li><a href="#dirarch">Directory-archive script</a></li>
          <li><a href="#bridgesan">Bridge descriptor sanitizer</a></li>
          <li><a href="#torperf">Torperf</a></li>
        </ul>
        <br/>
        <a id="ernie"/>
        <h3>Metrics portal software</h3>
        <br/>
        <p>The <a href="graphs.html">graphs</a> on this website are
        generated using ERNIE, the Enhanced R-based tor Network
        Intelligence Engine (why ERNIE? because nobody liked BIRT; sorry
        for misspelling Tor). ERNIE consists of Java code that parses
        the various input data formats and writes CSV files and R code
        that processes the CSV files to generate graphs. Of course, ERNIE
        can be used to generate customized graphs without the need to put
        them on a website. ERNIE can further import descriptors into a
        database for further analysis and aggregate descriptors to make
        tarballs.</p>
        <ul>
          <li>Browse the
          <a href="http://gitweb.torproject.org//ernie.git">Git repository</a></li>
          <li><tt>git clone git://git.torproject.org/git/ernie</tt></li>
        </ul>
        <br/>
        <h3>Directory-archive script</h3>
        <br/>
        <p>The directory-archive script consists of a bunch of shell
        scripts that periodically download relay descriptors, sort them
        into a directory structure, and compile monthly tarballs. The
        tarballs are quite similar to the ones provided on the
        <a href="data.html#relaydesc">data</a> page, with a few
        exceptions: the provided tarballs are the result of combining two
        directory-archive script outputs, splitting v3 votes and v3
        consensuses into separate tarballs and replacing all colons in
        filenames with dashes. The
        <a href="#ernie">metrics portal software</a> contains similar
        functionality.</p>
        <ul>
          <li>Browse the <a href="http://gitweb.torproject.org//tor.git?a=tree;f=contrib/directory-archive;h=095a4216c06b5afc6274eddd39f0238067377fe4;hb=HEAD">contrib/directory-archive/</a> subdirectory in the Tor sources</a></li>
          <li><tt>git clone git://git.torproject.org/git/tor</tt></li>
        </ul>
        <br/>
        <h3>Bridge descriptor sanitizer</h3>
        <br/>
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
        input, removesall possibly sensitive information from the
        descriptors, and puts out the sanitized bridge descriptors that
        are safe to be published. The
        <a href="#ernie">metrics portal software</a> contains similar
        functionality and will soon make this bridge descriptor sanitizer
        obsolete.</p>
        <ul>
          <li>Browse the
          <a href="https://svn.torproject.org/svn/projects/archives/trunk/bridge-desc-sanitizer/">SVN repository</a></li>
          <li><tt>svn co https://svn.torproject.org/svn/projects/archives/trunk/bridge-desc-sanitizer</tt></li>
        </ul>
        <br/>
        <a id="torperf"/>
        <h3>Torperf</h3>
        <br/>
        Torperf is a little tool that measures Tor's performance as users
        experience it. Torperf uses a trivial SOCKS client to download
        files of various sizes over the Tor network and notes how long
        substeps take.</p>
        <ul>
          <li>Browse the
          <a href="https://svn.torproject.org/svn/torperf/trunk/">SVN repository</a></li>
          <li><tt>svn co https://svn.torproject.org/svn/torperf/trunk torperf</tt></li>
        </ul>
      </div>
