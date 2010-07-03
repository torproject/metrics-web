        <h2>Tor Metrics Portal</h2>
        <br/>
        <p>The Tor Metrics Portal aggregates all kinds of interesting
        data about the Tor network and visualizes them in graphs and
        reports. This portal also provides easy access to the underlying
        data and documentation for performing own analyses based on these
        data. Find out more here:</p>
        <ul>
          <li>View daily updated <a href="graphs.html">graphs</a> on
          estimated client numbers, on network performance, and other
          statistics on the Tor network</li>
          <li>Read <a href="papers.html">papers</a> and technical reports
          on the measurement techniques and results of statistical
          analysis of metrics data</li>
          <li>Download the <a href="/research/data.html">data</a> that is behind the
          graphs and reports to make your own evaluations</li>
          <li>Try out the <a href="/research/tools.html">tools</a> to parse and
          evaluate the metrics data</li>
        </ul>
        <br/>
        <h3>News</h3>
        <ul>
          <li>June 7, 2010: The <a href="exonerator.html">ExoneraTor</a>
          now uses the most recent network information as its data basis
          (as opposed to monthly snapshots). As a by-product,
          <a href="consensus?valid-after=2010-06-01-12-00-00">network
          status consensuses</a> and <a href="serverdesc?desc-id=b7461d591738b4c7d54393acde0efc6d66d1e7fc">server
          descriptors</a> are now browsable by valid-after time and
          descriptor digest, respectively. More sophisticated descriptor
          search functions will follow.</li>
          <li>June 7, 2010: The <a href="torperf-graphs.html">graphs on
          user-experienced download times</a> are now drawn using
          (a modified) <a href="http://had.co.nz/ggplot2/">ggplot2</a>.
          Modifications to ggplot2 include
          <a href="https://stat.ethz.ch/pipermail/r-help/2010-June/241559.html">drawing
          a ribbon only for intervals with non-NA values</a> and
          <a href="https://stat.ethz.ch/pipermail/r-help/2010-June/241618.html">positioning
          the legend at the top of a graph</a>. This concludes the
          transition to ggplot2 and will greatly facilitate dynamic graph
          generation in the future.</li>
          <li>May 26, 2010: The monthly
          <a href="/research/data.html#relaydesc">relay</a> and
          <a href="/research/data.html#bridgedesc">bridge descriptor tarballs</a>
          are now updated every day. Beginning with May 2010, the bridge
          descriptor tarballs do not contain country codes anymore,
          because it was tough to maintain. If your research requires this
          or any other detail, contact us and we'll sort something out.
          <li>May 25, 2010: The
          <a href="bridge-users-graphs.html">graphs on daily bridge users</a>
          are now more
          accurate by excluding broken "geoip-stats" data of 0.2.2.x
          versions and including "bridge-stats" data of bridges running
          0.2.2.7-alpha or higher.</li>
          <li>May 19, 2010: The graphs on
          <a href="recurring-users-graphs.html">recurring users</a> and
          <a href="bridge-users-graphs.html">bridge users</a> are now
          accompanied by CSV files containing
          <a href="csv/monthly-users-peak.csv">peak</a> and
          <a href="csv/monthly-users-average.csv">average</a>
          daily users per month by country.</li>
        </ul>
