<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<jsp:include page="top.jsp">
  <jsp:param name="pageTitle" value="Development &ndash; Tor Metrics"/>
  <jsp:param name="navActive" value="Development"/>
</jsp:include>

    <div class="container">
      <ul class="breadcrumb">
        <li><a href="/">Home</a></li>
        <li><a href="development.html">Development</a></li>
        <li class="active">metrics-lib</li>
      </ul>
    </div>

<div class="container">

<div class="jumbotron">
<div class="text-center">
<h2>Tor Metrics Library</h2>
<p>Tor Metrics Library is a Java API that facilitates processing Tor network data from the <a href="https://collector.torproject.org/">CollecTor</a> service for statistical analysis and for building services and applications.</p>
<a class="btn btn-primary btn-lg" style="margin: 10px" href="https://dist.torproject.org/descriptor/?C=M;O=D"><i class="fa fa-chevron-right" aria-hidden="true"></i> Download Release</a>
<a class="btn btn-primary btn-lg" style="margin: 10px" href="https://gitweb.torproject.org/metrics-lib.git/plain/CHANGELOG.md"><i class="fa fa-chevron-right" aria-hidden="true"></i> View Change Log</a>
<!--<a class="btn btn-primary btn-lg" style="margin: 10px" href="metrics-lib/javadoc/index.html"><i class="fa fa-chevron-right" aria-hidden="true"></i> Browse JavaDocs</a>-->
</div><!-- text-center -->
</div><!-- jumbotron -->

</div><!-- container -->
<br>

<br>

<div class="container language-java">
      <div class="row">
        <div class="col-xs-12">

<h1>metrics-lib</h1>

<p>Welcome to metrics-lib, a Java API that facilitates processing Tor network data from the <a href="https://collector.torproject.org/">CollecTor</a> service for statistical analysis and for building services and applications.</p>

<p>In the tutorials below we're explaining the basic steps to get you started with metrics-lib.</p>

<h2 id="prerequisites">Prerequisites and preparation <a href="#prerequisites" class="anchor">#</a></h2>

<p>The following tutorials are written with an audience in mind that knows Java and to a lesser extent how Tor works.  We explain all data used in the tutorials.  More and most up-to-date information about descriptors can be found in the <a href="https://gitweb.torproject.org/torspec.git/tree/dir-spec.txt">Tor directory protocol specification</a> and on the <a href="https://collector.torproject.org/">CollecTor website</a>.</p>

<p>All tutorials require you to <a href="https://dist.torproject.org/descriptor/?C=M;O=D">download</a> the latest release of metrics-lib, follow the instructions to <a href="https://gitweb.torproject.org/metrics-lib.git/tree/README.md">verify</a> its signature, extract the tarball locally, and copy the <code>lib/</code> and the <code>generated/</code> directories to your working directory for the tutorials.</p>

<h2 id="tutorial1">Tutorial 1: Download descriptors from CollecTor <a href="#tutorial1" class="anchor">#</a></h2>

<p>Let's start this tutorial series by doing something really simple.  We'll use metrics-lib to download <a href="https://collector.torproject.org/recent/relay-descriptors/consensuses/">recent consensuses from CollecTor</a> and write them to a local directory.  We're not doing anything with those consensuses yet, though we'll get back to that in a bit.</p>

<p>We'll need to tell metrics-lib five pieces of information for this:</p>

<ol>
<li>the CollecTor base URL without trailing slash (<code>"https://collector.torproject.org"</code>),</li>
<li>which remote directories to collect descriptors from (<code>new String[] { "/recent/relay-descriptors/consensuses/" }</code>),</li>
<li>the minimum last-modified time of files to be collected (<code>0L</code>),</li>
<li>the local directory to write files to (<code>new File("descriptors")</code>), and</li>
<li>whether to delete all local files that do not exist remotely anymore (<code>false</code>).</li>
</ol>

<p>Create a new file <code>DownloadConsensuses.java</code> with the following content:</p>

<pre><code>import org.torproject.descriptor.*;

import java.io.File;

public class DownloadConsensuses {
  public static void main(String[] args) {

    // Download consensuses published in the last 72 hours, which will take up to five minutes and require several hundred MB on the local disk.
    DescriptorCollector descriptorCollector = DescriptorSourceFactory.createDescriptorCollector();
    descriptorCollector.collectDescriptors(
        // Download from Tor's main CollecTor instance,
        "https://collector.torproject.org",
        // include only network status consensuses
        new String[] { "/recent/relay-descriptors/consensuses/" },
        // regardless of last-modified time,
        0L,
        // write to the local directory called descriptors/,
        new File("descriptors"),
        // and don't delete extraneous files that do not exist remotely anymore.
        false);
  }
}
</code></pre>

<p>If you haven't already done so, prepare the working directory for this tutorial as described <a href="#prerequisites">above</a>.</p>

<p>Compile and run the Java file:</p>

<pre>
javac -cp lib/\*:generated/dist/signed/\* DownloadConsensuses.java
</pre>
<pre>
java -cp .:lib/\*:generated/dist/signed/\* DownloadConsensuses
</pre>

<p>This will take up to five minutes and require several hundred MB on the local disk.</p>

<p>If you want to play a bit with this code, you could extend it to also download recent bridge extra-info descriptors from CollecTor, which are stored in <code>/recent/bridge-descriptors/extra-infos/</code> and which we'll need for tutorial 3 below.  (If you're too <strike>impatient</strike> curious, scroll down to the bottom of this page for the diff.)</p>

<h2 id="tutorial2">Tutorial 2: Relay capacity by Tor version <a href="#tutorial2" class="anchor">#</a></h2>

<p>If you just followed tutorial 1 above, you now have a bunch of consensuses on your disk.  Let's do something with those and look at relay capacity by Tor version.  A possible use case could be that the Tor developers debate which of the older versions to turn into long-term supported versions, and you want to contribute more facts to that discussion by telling them how much relay capacity each version provides.</p>

<p>Consider the following snippet from a consensus document showing a single relay to get an idea of the underlying data:</p>

<pre>
[...]
r PrivacyRepublic0001 XOzFwwrMSz3kYnkjI5Zwh8xT2Uc WLlCQj3gVELkwIBh3EWxG74LZ2E 2017-03-04 08:16:22 178.32.181.96 443 80
s Exit Fast Guard HSDir Running Stable V2Dir Valid
v Tor 0.2.8.9
pr Cons=1-2 Desc=1-2 DirCache=1 HSDir=1 HSIntro=3 HSRend=1 Link=1-4 LinkAuth=1 Microdesc=1-2 Relay=1-2
w Bandwidth=136000
p reject 22,25,109-110,119,143,465,563,587,6881-6889
[...]
</pre>

<p>We're interested in the Tor version number without patch level (<code>0.2.8</code>) and the consensus weight (<code>136000</code>).</p>

<p>Create a new file <code>ConsensusWeightByVersion.java</code> with the following content:</p>

<pre><code class="language-java">import org.torproject.descriptor.*;

import java.io.File;
import java.util.*;

public class ConsensusWeightByVersion {
  public static void main(String[] args) {

    // Download consensuses.
    DescriptorCollector descriptorCollector = DescriptorSourceFactory.createDescriptorCollector();
    descriptorCollector.collectDescriptors("https://collector.torproject.org", new String[] { "/recent/relay-descriptors/consensuses/" }, 0L, new File("descriptors"), false);

    // Keep local counters for extracted descriptor data.
    long totalBandwidth = 0L;
    SortedMap&lt;String, Long&gt; bandwidthByVersion = new TreeMap&lt;&gt;();

    // Read descriptors from disk.
    DescriptorReader descriptorReader = DescriptorSourceFactory.createDescriptorReader();
    for (Descriptor descriptor : descriptorReader.readDescriptors(new File("descriptors/recent/relay-descriptors/consensuses"))) {
      if (!(descriptor instanceof RelayNetworkStatusConsensus)) {
        // We're only interested in consensuses.
        continue;
      }
      RelayNetworkStatusConsensus consensus = (RelayNetworkStatusConsensus) descriptor;
      for (NetworkStatusEntry entry : consensus.getStatusEntries().values()) {
        String version = entry.getVersion();
        if (!version.startsWith("Tor ") || version.length() &lt; 9) {
          // We're only interested in a.b.c type versions for this example.
          continue;
        }
        // Remove the 'Tor ' prefix and anything starting at the patch level.
        version = version.substring(4, 9);
        long bandwidth = entry.getBandwidth();
        totalBandwidth += bandwidth;
        if (bandwidthByVersion.containsKey(version)) {
          bandwidthByVersion.put(version, bandwidth + bandwidthByVersion.get(version));
        } else {
          bandwidthByVersion.put(version, bandwidth);
        }
      }
    }

    // Print out fractions of consensus weight by Tor version.
    if (totalBandwidth &gt; 0L) {
      for (Map.Entry&lt;String, Long&gt; e : bandwidthByVersion.entrySet()) {
        System.out.printf("%s -&gt; %4.1f%%%n", e.getKey(), (100.0 * (double) e.getValue() / (double) totalBandwidth));
      }
    }
  }
}
</code></pre>

<p>If you haven't already done so, prepare the working directory for this tutorial as described <a href="#prerequisites">above</a>.</p>

<p>Compile and run the Java file:</p>

<pre>
javac -cp lib/\*:generated/dist/signed/\* ConsensusWeightByVersion.java
</pre>
<pre>
java -cp .:lib/\*:generated/dist/signed/\* ConsensusWeightByVersion
</pre>

<p>There will be some log statements, and the final output should now contain lines like the following:</p>

<pre>
0.2.4 -&gt;  3.2%
0.2.5 -&gt;  9.4%
0.2.6 -&gt;  3.2%
0.2.7 -&gt;  7.3%
0.2.8 -&gt;  6.4%
0.2.9 -&gt; 48.2%
0.3.0 -&gt; 20.8%
0.3.1 -&gt;  1.2%
0.3.2 -&gt;  0.3%
</pre>

<p>These are the numbers we were looking for.  Now you should know what to do to extract interesting data from consensuses.  Want to give that another try and filter relays with the <code>Exit</code> flag to learn about exit capacity by Tor version?  Hint: You'll want to check for <code>entry.getFlags().contains("Exit")</code>.  Of course, you could as well continue with the next tutorial below.  (Or you could scroll down to the bottom of this page to see the diff.)</p>

<h2 id="tutorial3">Tutorial 3: Frequency of transports <a href="#tutorial3" class="anchor">#</a></h2>

<p>In the previous tutorial we looked at relay descriptors, so let's now look a bit at bridge descriptors.</p>

<p>Every bridge publishes its transports in its extra-info descriptors that it periodically sends to the bridge authority.  Let's count the frequency of transports.  A possible use case could be that the Pluggable Transports developers debate which of the transport name is the least pronouncable, and you want to give them numbers to talk about something much more useful instead.</p>

<p>Consider this snippet from a bridge extra-info descriptor:</p>

<pre>
extra-info LeifEricson 3E0908F131AC417C48DDD835D78FB6887F4CD126
[...]
transport obfs2
transport scramblesuit
transport obfs3
transport obfs4
transport fte
</pre>

<p>What we need to do is extract the list of transport names (<code>obfs2</code>, <code>scramblesuit</code>, etc.) together with the bridge fingerprint (<code>3E0908F131AC417C48DDD835D78FB6887F4CD126</code>).  Considering the fingerprint is important, so that we avoid double-counting transports provided by the same bridge.</p>

<p>Create a new file <code>PluggableTransports.java</code> with the following content:</p>

<pre><code class="language-java">import org.torproject.descriptor.*;

import java.io.File;
import java.util.*;

public class PluggableTransports {
  public static void main(String[] args) {

    DescriptorCollector descriptorCollector = DescriptorSourceFactory.createDescriptorCollector();
    descriptorCollector.collectDescriptors("https://collector.torproject.org", new String[] { "/recent/bridge-descriptors/extra-infos/" }, 0L, new File("descriptors"), false);

    Set&lt;String&gt; observedFingerprints = new HashSet&lt;&gt;();
    SortedMap&lt;String, Integer&gt; countedTransports = new TreeMap&lt;&gt;();

    DescriptorReader descriptorReader = DescriptorSourceFactory.createDescriptorReader();
    for (Descriptor descriptor : descriptorReader.readDescriptors(new File("descriptors/recent/bridge-descriptors/extra-infos"))) {
      if (!(descriptor instanceof BridgeExtraInfoDescriptor)) {
        continue;
      }
      BridgeExtraInfoDescriptor extraInfo = (BridgeExtraInfoDescriptor) descriptor;
      String fingerprint = extraInfo.getFingerprint();
      if (observedFingerprints.add(fingerprint)) {
        for (String transport : extraInfo.getTransports()) {
          if (countedTransports.containsKey(transport)) {
            countedTransports.put(transport, 1 + countedTransports.get(transport));
          } else {
            countedTransports.put(transport, 1);
          }
        }
      }
    }

    if (!observedFingerprints.isEmpty()) {
      double totalObservedFingerprints = observedFingerprints.size();
      for (Map.Entry&lt;String, Integer&gt; e : countedTransports.entrySet()) {
        System.out.printf("%20s -&gt; %4.1f%%%n", e.getKey(), (100.0 * (double) e.getValue() / totalObservedFingerprints));
      }
    }
  }
}
</code></pre>

<p>If you haven't already done so, prepare the working directory for this tutorial as described <a href="#prerequisites">above</a>.</p>

<p>Compile and run the Java file:</p>

<pre>
javac -cp lib/\*:generated/dist/signed/\* PluggableTransports.java
</pre>
<pre>
java -cp .:lib/\*:generated/dist/signed/\* PluggableTransports
</pre>

<p>The output should contain lines like the following:</p>

<pre>
                 fte -&gt;  2.3%
                meek -&gt;  0.2%
               obfs2 -&gt;  0.7%
               obfs3 -&gt; 20.8%
     obfs3_websocket -&gt;  0.0%
               obfs4 -&gt; 77.0%
        scramblesuit -&gt; 17.3%
           snowflake -&gt;  0.1%
           websocket -&gt;  0.7%
</pre>

<p>As above, we'll leave it up to you to further expand this code.  For example, how does the result change if you count transport <i>combinations</i> rather than transports?  Hint: you won't need anything else from metrics-lib, but you'll need to add some code to order transport names and write them to a string.  (And if you'd rather look up the solution, scroll down a bit to see the diff.)</p>

<h2 id="nextsteps">Next steps <a href="#nextsteps" class="anchor">#</a></h2>

<p>Want to write more code that uses metrics-lib?  Be sure to read the JavaDocs while developing new services or applications using Tor network data.</p>

<p>Ran into a problem, found a bug, or came up with a cool new feature?  Feel free to <a href="https://metrics.torproject.org/about.html#contact">contact us</a>.  Alternatively, take a look at the <a href="https://trac.torproject.org/projects/tor">bug tracker</a> and open a ticket if there's none for your issue yet.</p>

<p>Interested in writing <a href="https://gitweb.torproject.org/metrics-lib.git/">code</a> for metrics-lib?  Please take a look at the Tor Metrics team <a href="https://trac.torproject.org/projects/tor/wiki/org/teams/MetricsTeam/Volunteers">wiki page</a> to find out how to contribute.</p>

<p>Scrolled down just to see where we're hiding the solutions of the three little riddles above?  Here are the diffs:</p>

<pre><code class="language-diff">diff -Nur DownloadConsensuses.java DownloadConsensuses.java
--- DownloadConsensuses.java        2017-03-07 17:48:35.000000000 +0100
+++ DownloadConsensuses.java        2017-03-10 23:02:51.000000000 +0100
@@ -11,7 +11,7 @@
         // Download from Tor's main CollecTor instance,
         "https://collector.torproject.org",
         // include only network status consensuses
-        new String[] { "/recent/relay-descriptors/consensuses/" },
+        new String[] { "/recent/bridge-descriptors/extra-infos/" },
         // regardless of last-modified time,
         0L,
         // write to the local directory called descriptors/,
</code></pre>

<pre><code class="language-diff">diff -Nur ConsensusWeightByVersion.java ConsensusWeightByVersion.java
--- ConsensusWeightByVersion.java   2017-03-10 23:00:40.000000000 +0100
+++ ConsensusWeightByVersion.java   2017-03-10 23:03:18.000000000 +0100
@@ -25,6 +25,9 @@
       }
       RelayNetworkStatusConsensus consensus = (RelayNetworkStatusConsensus) descriptor;
       for (NetworkStatusEntry entry : consensus.getStatusEntries().values()) {
+        if (!entry.getFlags().contains("Exit")) {
+          continue;
+        }
         String version = entry.getVersion();
         if (!version.startsWith("Tor ") || version.length() &lt; 9) {
           // We're only interested in a.b.c type versions for this example.
</code></pre>

<pre><code class="language-diff">diff -Nur PluggableTransports.java PluggableTransports.java
--- PluggableTransports.java        2017-03-10 23:01:43.000000000 +0100
+++ PluggableTransports.java        2017-03-10 23:03:43.000000000 +0100
@@ -20,12 +22,11 @@
       BridgeExtraInfoDescriptor extraInfo = (BridgeExtraInfoDescriptor) descriptor;
       String fingerprint = extraInfo.getFingerprint();
       if (observedFingerprints.add(fingerprint)) {
-        for (String transport : extraInfo.getTransports()) {
-          if (countedTransports.containsKey(transport)) {
-            countedTransports.put(transport, 1 + countedTransports.get(transport));
-          } else {
-            countedTransports.put(transport, 1);
-          }
+        String transports = new TreeSet&lt;&gt;(extraInfo.getTransports()).toString();
+        if (countedTransports.containsKey(transports)) {
+          countedTransports.put(transports, 1 + countedTransports.get(transports));
+        } else {
+          countedTransports.put(transports, 1);
         }
       }
     }
</code></pre>

</div> <!-- col -->
</div> <!-- row -->
</div> <!-- container -->

<jsp:include page="bottom.jsp"/>

