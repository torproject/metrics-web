<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<jsp:include page="top.jsp">
  <jsp:param name="pageTitle" value="Sources &ndash; Tor Metrics"/>
  <jsp:param name="navActive" value="Sources"/>
</jsp:include>

    <div class="container">
      <ul class="breadcrumb">
        <li><a href="/">Home</a></li>
        <li><a href="sources.html">Sources</a></li>
        <li class="active">CollecTor</li>
      </ul>
    </div>

    <div class="container">
      <div class="row">
        <div class="col-xs-12">

          <br><br>

            <div class="jumbotron">
              <div class="text-center">

                <h2>
                  Welcome to CollecTor, your friendly data-collecting service in the Tor network
                </h2>
              <p>
              CollecTor fetches data from various
              nodes and services in the public Tor network and makes it
              available to the world.  If you're doing research on the Tor
              network, or if you're developing an application that uses
              Tor network data, this is your place to start.
              </p>
<a class="btn btn-primary btn-lg" style="margin: 10px" href="/collector/recent/"><i class="fa fa-chevron-right" aria-hidden="true"></i> Browse Recent Descriptors</a>
<a class="btn btn-primary btn-lg" style="margin: 10px" href="/collector/archive/"><i class="fa fa-chevron-right" aria-hidden="true"></i> Browse Archived Descriptors</a>
              </div><!-- text-center -->


            </div><!-- jumbotron -->

        </div><!-- col -->
      </div><!-- row -->

      <div class="row">
        <div class="col-xs-12">
        <br>
          <h1 id="available-descriptors" class="hover">Available Descriptors
<a href="#available-descriptors" class="anchor">#</a></h1>

<p>Descriptors are available in two different file formats: recent descriptors that were published in the last 72 hours are available as plain text, and archived descriptors covering over 10 years of Tor network history are available as compressed tarballs.</p>

<table class="table">
<thead>
<tr>
<th>Descriptor Type</th>
<th>Type Annotation</th>
<th class="thDescriptors">Descriptors</th>
</tr>
</thead>
<tbody>
<tr class="tableHeadline">
  <td colspan="3"><b><a href="#relay-descriptors">Tor Relay Descriptors</a></b></td>
</tr>
<tr>
  <td><a href="#type-server-descriptor">Relay Server Descriptors</a></td>
  <td><code>@type server-descriptor 1.0</code></td>
  <td><a href="/collector/recent/relay-descriptors/server-descriptors/" class="btn btn-primary btn-xs pull-left"><i class="fa fa-chevron-right" aria-hidden="true"></i> recent</a>
      <a href="/collector/archive/relay-descriptors/server-descriptors/" class="btn btn-primary btn-xs pull-right"><i class="fa fa-chevron-right" aria-hidden="true"></i> archive</a></td>
</tr>
<tr>
  <td><a href="#type-extra-info">Relay Extra-info Descriptors</a></td>
  <td><code>@type extra-info 1.0</code></td>
  <td><a href="/collector/recent/relay-descriptors/extra-infos/" class="btn btn-primary btn-xs pull-left"><i class="fa fa-chevron-right" aria-hidden="true"></i> recent</a>
      <a href="/collector/archive/relay-descriptors/extra-infos/" class="btn btn-primary btn-xs pull-right"><i class="fa fa-chevron-right" aria-hidden="true"></i> archive</a></td>
</tr>
<tr>
  <td><a href="#type-network-status-consensus-3">Network Status Consensuses</a></td>
  <td><code>@type network-status-consensus-3 1.0</code></td>
  <td><a href="/collector/recent/relay-descriptors/consensuses/" class="btn btn-primary btn-xs pull-left"><i class="fa fa-chevron-right" aria-hidden="true"></i> recent</a>
      <a href="/collector/archive/relay-descriptors/consensuses/" class="btn btn-primary btn-xs pull-right"><i class="fa fa-chevron-right" aria-hidden="true"></i> archive</a></td>
</tr>
<tr>
  <td><a href="#type-network-status-vote-3">Network Status Votes</a></td>
  <td><code>@type network-status-vote-3 1.0</code></td>
  <td><a href="/collector/recent/relay-descriptors/votes/" class="btn btn-primary btn-xs pull-left"><i class="fa fa-chevron-right" aria-hidden="true"></i> recent</a>
      <a href="/collector/archive/relay-descriptors/votes/" class="btn btn-primary btn-xs pull-right"><i class="fa fa-chevron-right" aria-hidden="true"></i> archive</a></td>
</tr>
<tr>
  <td><a href="#type-dir-key-certificate-3">Directory Key Certificates</a></td>
  <td><code>@type dir-key-certificate-3 1.0</code></td>
  <td><a href="/collector/archive/relay-descriptors/" class="btn btn-primary btn-xs pull-right"><i class="fa fa-chevron-right" aria-hidden="true"></i> archive</a></td>
</tr>
<tr>
  <td><a href="#type-network-status-microdesc-consensus-3">Microdescriptor Consensuses</a></td>
  <td><code>@type network-status-microdesc-consensus-3 1.0</code></td>
  <td><a href="/collector/recent/relay-descriptors/microdescs/consensus-microdesc/" class="btn btn-primary btn-xs pull-left"><i class="fa fa-chevron-right" aria-hidden="true"></i> recent</a>
      <a href="/collector/archive/relay-descriptors/microdescs/" class="btn btn-primary btn-xs pull-right"><i class="fa fa-chevron-right" aria-hidden="true"></i> archive</a></td>
</tr>
<tr>
  <td><a href="#type-microdescriptor">Microdescriptors</a></td>
  <td><code>@type microdescriptor 1.0</code></td>
  <td><a href="/collector/recent/relay-descriptors/microdescs/micro/" class="btn btn-primary btn-xs pull-left"><i class="fa fa-chevron-right" aria-hidden="true"></i> recent</a>
      <a href="/collector/archive/relay-descriptors/microdescs/" class="btn btn-primary btn-xs pull-right"><i class="fa fa-chevron-right" aria-hidden="true"></i> archive</a></td>
</tr>
<tr>
  <td><a href="#type-network-status-2">Version 2 Network Statuses</a></td>
  <td><code>@type network-status-2 1.0</code></td>
  <td><a href="/collector/archive/relay-descriptors/statuses/" class="btn btn-primary btn-xs pull-right"><i class="fa fa-chevron-right" aria-hidden="true"></i> archive</a></td>
</tr>
<tr>
  <td><a href="#type-directory">Version 1 Directories</a></td>
  <td><code>@type directory 1.0</code></td>
  <td><a href="/collector/archive/relay-descriptors/tor/" class="btn btn-primary btn-xs pull-right"><i class="fa fa-chevron-right" aria-hidden="true"></i> archive</a></td>
</tr>
<tr class="tableHeadline">
  <td colspan="3"><b><a href="#bridge-descriptors">Tor Bridge Descriptors</a></b></td>
</tr>
<tr>
  <td><a href="#type-bridge-network-status">Bridge Network Statuses</a></td>
  <td><code>@type bridge-network-status 1.2</code></td>
  <td><a href="/collector/recent/bridge-descriptors/statuses/" class="btn btn-primary btn-xs pull-left"><i class="fa fa-chevron-right" aria-hidden="true"></i> recent</a>
      <a href="/collector/archive/bridge-descriptors/statuses/" class="btn btn-primary btn-xs pull-right"><i class="fa fa-chevron-right" aria-hidden="true"></i> archive</a></td>
</tr>
<tr>
  <td><a href="#type-bridge-server-descriptor">Bridge Server Descriptors</a></td>
  <td><code>@type bridge-server-descriptor 1.2</code></td>
  <td><a href="/collector/recent/bridge-descriptors/server-descriptors/" class="btn btn-primary btn-xs pull-left"><i class="fa fa-chevron-right" aria-hidden="true"></i> recent</a>
      <a href="/collector/archive/bridge-descriptors/server-descriptors/" class="btn btn-primary btn-xs pull-right"><i class="fa fa-chevron-right" aria-hidden="true"></i> archive</a></td>
</tr>
<tr>
  <td><a href="#type-bridge-extra-info">Bridge Extra-info Descriptors</a></td>
  <td><code>@type bridge-extra-info 1.3</code></td>
  <td><a href="/collector/recent/bridge-descriptors/extra-infos/" class="btn btn-primary btn-xs pull-left"><i class="fa fa-chevron-right" aria-hidden="true"></i> recent</a>
      <a href="/collector/archive/bridge-descriptors/extra-infos/" class="btn btn-primary btn-xs pull-right"><i class="fa fa-chevron-right" aria-hidden="true"></i> archive</a></td>
</tr>
<tr class="tableHeadline">
  <td colspan="3"><b><a href="#tor-hidden-service-descriptors">Tor Hidden Service Descriptors</a></b></td>
</tr>
<tr>
  <td><a href="#type-hidden-service-descriptor">Hidden Service Descriptors</a></td>
  <td><code>@type hidden-service-descriptor 1.0</code></td>
  <td></td>
</tr>
<tr class="tableHeadline">
  <td colspan="3"><b><a href="#bridge-pool-assignments">BridgeDB's Bridge Pool Assignments</a></b></td>
</tr>
<tr>
  <td><a href="#type-bridge-pool-assignment">Bridge Pool Assignments</a></td>
  <td><code>@type bridge-pool-assignment 1.0</code></td>
  <td><a href="/collector/archive/bridge-pool-assignments/" class="btn btn-primary btn-xs pull-right"><i class="fa fa-chevron-right" aria-hidden="true"></i> archive</a></td>
</tr>
<tr class="tableHeadline">
  <td colspan="3"><b><a href="#exit-lists">TorDNSEL's Exit Lists</a></b></td>
</tr>
<tr>
  <td><a href="#type-tordnsel">Exit Lists</a></td>
  <td><code>@type tordnsel 1.0</code></td>
  <td><a href="/collector/recent/exit-lists/" class="btn btn-primary btn-xs pull-left"><i class="fa fa-chevron-right" aria-hidden="true"></i> recent</a>
      <a href="/collector/archive/exit-lists/" class="btn btn-primary btn-xs pull-right"><i class="fa fa-chevron-right" aria-hidden="true"></i> archive</a></td>
</tr>
<tr class="tableHeadline">
  <td colspan="3"><b><a href="#torperf">Torperf's and OnionPerf's Performance Data</a></b></td>
</tr>
<tr>
  <td><a href="#type-torperf">Torperf Measurement Results</a></td>
  <td><code>@type torperf 1.1</code></td>
  <td><a href="/collector/recent/torperf/" class="btn btn-primary btn-xs pull-left"><i class="fa fa-chevron-right" aria-hidden="true"></i> recent</a>
      <a href="/collector/archive/torperf/" class="btn btn-primary btn-xs pull-right"><i class="fa fa-chevron-right" aria-hidden="true"></i> archive</a></td>
</tr>
</tbody>
</table>

        </div><!-- col -->
      </div><!-- row -->
<br>




        <br>
          <h1 id="data-formats" class="hover">Data Formats <a href="#data-formats" class="anchor">#</a></h1>

<p>
Each descriptor provided here contains an <code>@type</code> annotation using
the format <code>@type $descriptortype $major.$minor</code>.
Any tool that processes these descriptors may parse files without meta
data or with an unknown descriptor type at its own risk, can safely parse
files with known descriptor type and same major version number, and should
not parse files with known descriptor type and higher major version
number.
</p>
</div>


  <div class="container">

<br>
<h2 id="relay-descriptors" class="hover">Tor Relay Descriptors
<a href="#relay-descriptors" class="anchor">#</a>
</h2>

<p>
Relays and directory authorities publish relay descriptors, so that
clients can select relays for their paths through the Tor network.
All these relay descriptors are specified in the
<a href="https://gitweb.torproject.org/torspec.git/tree/dir-spec.txt" target="_blank">Tor
directory protocol, version 3</a> specification document (or in the
earlier protocol
<a href="https://gitweb.torproject.org/torspec.git/tree/attic/dir-spec-v2.txt" target="_blank">version 2</a> or
<a href="https://gitweb.torproject.org/torspec.git/tree/attic/dir-spec-v1.txt" target="_blank">version 1</a>).
</p>

<h3 id="type-server-descriptor" class="hover">Relay Server Descriptors
<small><code>@type server-descriptor 1.0</code></small>
<a href="/collector/recent/relay-descriptors/server-descriptors/" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> recent</a>
<a href="/collector/archive/relay-descriptors/server-descriptors/" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> archive</a>
<a href="#type-server-descriptor" class="anchor">#</a>
</h3>

<p>
Server descriptors contain information that relays publish about
themselves.
Tor clients once downloaded this information, but now they use
microdescriptors instead.
The server descriptors in the descriptor archives
contain one descriptor per file, whereas the recently published files
contain all descriptors collected in an hour concatenated into a single
file.
</p>

<h3 id="type-extra-info" class="hover">Relay Extra-info Descriptors
<small><code>@type extra-info 1.0</code></small>
<a href="/collector/recent/relay-descriptors/extra-infos/" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> recent</a>
<a href="/collector/archive/relay-descriptors/extra-infos/" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> archive</a>
<a href="#type-extra-info" class="anchor">#</a>
</h3>

<p>
Extra-info descriptors contain relay information that Tor clients do not
need in order to function.
These are self-published, like server descriptors, but not downloaded by
clients by default.
The extra-info descriptors in the descriptor archives
contain one descriptor per file, whereas the recently published files
contain all descriptors collected in an hour concatenated into a single
file.
</p>

<h3 id="type-network-status-consensus-3" class="hover">Network Status Consensuses
<small><code>@type network-status-consensus-3 1.0</code></small>
<a href="/collector/recent/relay-descriptors/consensuses/" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> recent</a>
<a href="/collector/archive/relay-descriptors/consensuses/" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> archive</a>
<a href="#type-network-status-consensus-3" class="anchor">#</a>
</h3>

<p>
Though Tor relays are decentralized, the directories that track the
overall network are not.
These central points are called directory authorities, and every hour they
publish a document called a consensus, or network status document.
The consensus is made up of router status entries containing
flags, heuristics used for relay selection, etc.
</p>

<h3 id="type-network-status-vote-3" class="hover">Network Status Votes
<small><code>@type network-status-vote-3 1.0</code></small>
<a href="/collector/recent/relay-descriptors/votes/" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> recent</a>
<a href="/collector/archive/relay-descriptors/votes/" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> archive</a>
<a href="#type-network-status-vote-3" class="anchor">#</a>
</h3>

<p>
The directory authorities exchange votes every hour to come up with a
common consensus.
Vote documents are by far the largest documents provided here.
</p>

<h3 id="type-dir-key-certificate-3" class="hover">Directory Key Certificates
<small><code>@type dir-key-certificate-3 1.0</code></small>
<a href="/collector/archive/relay-descriptors/" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> archive</a>
<a href="#type-dir-key-certificate-3" class="anchor">#</a>
</h3>

<p>
The directory authorities sign votes and the consensus with their
key that they publish in a key certificate.
These key certificates change once every few months, so they are only
available in a single descriptor archive tarball.
</p>

<h3 id="type-network-status-microdesc-consensus-3" class="hover">Microdescriptor Consensuses
<small><code>@type network-status-microdesc-consensus-3 1.0</code></small>
<a href="/collector/recent/relay-descriptors/microdescs/" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> recent</a>
<a href="/collector/archive/relay-descriptors/microdescs/" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> archive</a>
<a href="#type-network-status-microdesc-consensus-3" class="anchor">#</a>
</h3>

<p>
Tor clients used to download all server descriptors of active relays, but
now they only download the smaller microdescriptors which are derived from
server descriptors.
The microdescriptor consensus lists all active relays and references their
currently used microdescriptor.
The descriptor archive tarballs
contain both microdescriptor consensuses and referenced microdescriptors
together.
</p>

<h3 id="type-microdescriptor" class="hover">Microdescriptors
<small><code>@type microdescriptor 1.0</code></small>
<a href="/collector/recent/relay-descriptors/microdescs/" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> recent</a>
<a href="/collector/archive/relay-descriptors/microdescs/" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> archive</a>
<a href="#type-microdescriptor" class="anchor">#</a>
</h3>

<p>
Microdescriptors are minimalistic documents that just includes the
information necessary for Tor clients to work.
The descriptor archive tarballs
contain both microdescriptor consensuses and referenced microdescriptors
together.
The microdescriptors in descriptor archive tarballs
contain one descriptor per file, whereas the recently published files
contain all descriptors collected in an hour concatenated into a single
file.
</p>

<h3 id="type-network-status-2" class="hover">Version 2 Network Statuses
<small><code>@type network-status-2 1.0</code></small>
<a href="/collector/archive/relay-descriptors/statuses/" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> archive</a>
<a href="#type-network-status-2" class="anchor">#</a>
</h3>

<p>
Version 2 network statuses have been published by the directory
authorities before consensuses have been introduced.
In contrast to consensuses, each directory authority published their own
authoritative view on the network, and clients combined these documents
locally.
We stopped archiving version 2 network statuses in 2012.
</p>

<h3 id="type-directory" class="hover">Version 1 Directories
<small><code>@type directory 1.0</code></small>
<a href="/collector/archive/relay-descriptors/tor/" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> archive</a>
<a href="#type-directory" class="anchor">#</a>
</h3>

<p>
The first directory protocol version combined the list of active relays
with server descriptors in a single directory document.
We stopped archiving version 1 directories in 2007.
</p>

<br>
<h2 id="bridge-descriptors" class="hover">Tor Bridge Descriptors
<a href="#bridge-descriptors" class="anchor">#</a>
</h2>

<p>
Bridges and the bridge authority publish bridge descriptors that are used
by censored clients to connect to the Tor network.
We cannot, however, make bridge descriptors available as we do with relay
descriptors, because that would defeat the purpose of making bridges hard
to enumerate for censors.
We therefore sanitize bridge descriptors by removing all potentially
identifying information and publish sanitized versions here.
The sanitizing steps are specified in detail on a separate
<a href="bridge-descriptors.html">page</a>.
</p>

<h3 id="type-bridge-network-status" class="hover">Bridge Network Statuses
<small><code>@type bridge-network-status 1.2</code></small>
<a href="/collector/recent/bridge-descriptors/statuses/" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> recent</a>
<a href="/collector/archive/bridge-descriptors/" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> archive</a>
<a href="#type-bridge-network-status" class="anchor">#</a>
</h3>

<p>
Sanitized bridge network statuses are similar to version 2 relay network
statuses, but with only a <code>published</code> line and a
<code>fingerprint</code> line in the header, and
without any lines in the footer.
The format has changed over time to accomodate changes to the sanitizing
process, with earlier versions being:
</p>

<ul>
<li><code>@type bridge-network-status 1.0</code> was the first version.</li>
<li><code>@type bridge-network-status 1.1</code> introduced sanitized TCP
ports.</li>
<li><code>@type bridge-network-status 1.2</code> introduced the
<code>fingerprint</code> line, containing the fingerprint of the bridge
authority which produced the document, to the header.</li>
</ul>

<h3 id="type-bridge-server-descriptor" class="hover">Bridge Server descriptors
<small><code>@type bridge-server-descriptor 1.2</code></small>
<a href="/collector/recent/bridge-descriptors/server-descriptors/" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> recent</a>
<a href="/collector/archive/bridge-descriptors/" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> archive</a>
<a href="#type-bridge-server-descriptor" class="anchor">#</a>
</h3>

<p>
Bridge server descriptors follow the same format as relay server
descriptors, except for the sanitizing steps described above.
The bridge server descriptor archive tarballs contain one descriptor per
file, whereas recently published bridge server descriptor files
contain all descriptors collected in an hour concatenated into a single
file to reduce the number of files.
The format has changed over time to accomodate changes to the sanitizing
process, with earlier versions being:
</p>

<ul>
<li><code>@type bridge-server-descriptor 1.0</code> was the first version.</li>
<li>There was supposed to be a newer version indicating added
<code>ntor-onion-key</code> lines, but due to a mistake only the version number
of sanitized bridge extra-info descriptors was raised.
As a result, there may be sanitized bridge server descriptors with version
<code>@type bridge-server-descriptor 1.0</code> with and without those
lines.</li>
<li><code>@type bridge-server-descriptor 1.1</code> added
<code>master-key-ed25519</code> lines and <code>router-digest-sha256</code> to
server descriptors published by bridges using an Ed25519 master
key.</li>
<li><code>@type bridge-server-descriptor 1.2</code> introduced sanitized TCP
ports.</li>
</ul>

<h3 id="type-bridge-extra-info" class="hover">Bridge Extra-info Descriptors
<small><code>@type bridge-extra-info 1.3</code></small>
<a href="/collector/recent/bridge-descriptors/extra-infos/" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> recent</a>
<a href="/collector/archive/bridge-descriptors/" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> archive</a>
<a href="#type-bridge-extra-info" class="anchor">#</a>
</h3>

<p>
Bridge extra-info descriptors follow the same format as relay extra-info
descriptors, except for the sanitizing steps described above.
The format has changed over time to accomodate changes to the sanitizing
process, with earlier versions being:
</p>

<ul>
<li><code>@type bridge-extra-info 1.0</code> was the first version.</li>
<li><code>@type bridge-extra-info 1.1</code> added sanitized
<code>transport</code> lines.</li>
<li><code>@type bridge-extra-info 1.2</code> was supposed to indicate added
<code>ntor-onion-key</code> lines, but those changes only affect bridge server
descriptors, not extra-info descriptors.
So, nothing has changed as compared to version 1.1.</li>
<li><code>@type bridge-extra-info 1.3</code> added <code>master-key-ed25519</code>
lines and <code>router-digest-sha256</code> to extra-info descriptors
published by bridges using an Ed25519 master key.</li>
</ul>

<p>
The bridge extra-info descriptor archive tarballs contain one descriptor
per file, whereas recently published bridge extra-info descriptor
files contain all descriptors collected in an hour concatenated into a single
file to reduce the number of files.
</p>

<br>
<h2 id="tor-hidden-service-descriptors" class="hover">Tor Hidden Service Descriptors
<a href="#tor-hidden-service-descriptors" class="anchor">#</a>
</h2>

<p>
Tor hidden services make it possible for users to hide their locations
while offering various kinds of services, such as web publishing or an
instant messaging server.
A hidden service assembles a hidden service descriptor to make its service
available in the network.
This descriptor gets stored on hidden service directories and can be
retrieved by hidden service clients.
Hidden service descriptors are not formally archived, but some libraries
support parsing these descriptors when obtaining them from a locally
running Tor instance.
</p>

<h3 id="type-hidden-service-descriptor" class="hover">Hidden Service Descriptors
<small><code>@type hidden-service-descriptor 1.0</code></small>
<a href="#type-hidden-service-descriptor" class="anchor">#</a>
</h3>

<p>
Hidden service descriptors contain all details that are necessary for
clients to connect to a hidden service.
Despite the version number being 1.0, these descriptors are part of the
version 2 hidden service protocol.
</p>

<br>
<h2 id="bridge-pool-assignments" class="hover">BridgeDB's Bridge Pool Assignments
<a href="#bridge-pool-assignments" class="anchor">#</a>
</h2>

<p>
The bridge distribution service BridgeDB publishes bridge pool assignments
describing which bridges it has assigned to which distribution pool.
BridgeDB receives bridge network statuses from the bridge authority,
assigns these bridges to persistent distribution rings, and hands them out
to bridge users.
BridgeDB periodically dumps the list of running bridges with information
about the rings, subrings, and file buckets to which they are assigned to
a local file.
The sanitized versions of these lists containing SHA-1 hashes of bridge
fingerprints instead of the original fingerprints are available for
statistical analysis.
</p>

<h3 id="type-bridge-pool-assignment" class="hover">Bridge Pool Assignments
<small><code>@type bridge-pool-assignment 1.0</code></small>
<a href="/collector/archive/bridge-pool-assignments/" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> archive</a>
<a href="#type-bridge-pool-assignment" class="anchor">#</a>
</h3>

<p>
The document below shows a BridgeDB pool assignment file
from March 13, 2011.
Every such file begins with a line containing the timestamp when BridgeDB
wrote this file.
Subsequent lines start with the SHA-1 hash of a bridge fingerprint,
followed by ring, subring, and/or file bucket information.
There are currently three distributor ring types in BridgeDB:
</p>

<ol>
<li><b>unallocated:</b> These bridges are not distributed by BridgeDB,
but are either reserved for manual distribution or are written to file
buckets for distribution via an external tool.
If a bridge in the <code>unallocated</code> ring is assigned to a file bucket,
this is noted by <code>bucket=$bucketname</code>.</li>
<li><b>email:</b> These bridges are distributed via an e-mail
autoresponder.  Bridges can be assigned to subrings by their OR port or
relay flag which is defined by <code>port=$port</code> and/or <code>flag=$flag</code>.
</li>
<li><b>https:</b> These bridges are distributed via https server.
There are multiple https rings to further distribute bridges by IP address
ranges, which is denoted by <code>ring=$ring</code>.
Bridges in the <code>https</code> ring can also be assigned to subrings by
OR port or relay flag which is defined by <code>port=$port</code> and/or
<code>flag=$flag</code>.</li>
</ol>

<pre>
bridge-pool-assignment 2011-03-13 14:38:03
00b834117566035736fc6bd4ece950eace8e057a unallocated
00e923e7a8d87d28954fee7503e480f3a03ce4ee email port=443 flag=stable
0103bb5b00ad3102b2dbafe9ce709a0a7c1060e4 https ring=2 port=443 flag=stable
[...]
</pre>

<p>
As of December 8, 2014, bridge pool assignment files are no longer
archived.
</p>

<br>
<h2 id="exit-lists" class="hover">TorDNSEL's Exit Lists
<a href="#exit-lists" class="anchor">#</a>
</h2>

<p>
The exit list service
<a href="https://www.torproject.org/tordnsel/dist/" target="_blank">TorDNSEL</a>
publishes exit lists containing the IP addresses of relays that it found
when exiting through them.
</p>

<h3 id="type-tordnsel" class="hover">Exit Lists
<small><code>@type tordnsel 1.0</code></small>
<a href="/collector/recent/exit-lists/" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> recent</a>
<a href="/collector/archive/exit-lists/" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> archive</a>
<a href="#type-tordnsel" class="anchor">#</a>
</h3>

<p>
Tor Check makes the list of known exits and corresponding exit IP
addresses available in a specific format.
The document below shows an entry of the exit list written on
December 28, 2010 at 15:21:44 UTC.
This entry means that the relay with fingerprint <code>63BA..</code> which
published a descriptor at 07:35:55 and was contained in a version 2
network status from 08:10:11 uses two different IP addresses for exiting.
The first address <code>91.102.152.236</code> was found in a test performed at
07:10:30.
When looking at the corresponding server descriptor, one finds that this
is also the IP address on which the relay accepts connections from inside
the Tor network.
A second test performed at 10:35:30 reveals that the relay also uses IP
address <code>91.102.152.227</code> for exiting.
</p>

<pre>
ExitNode 63BA28370F543D175173E414D5450590D73E22DC
Published 2010-12-28 07:35:55
LastStatus 2010-12-28 08:10:11
ExitAddress 91.102.152.236 2010-12-28 07:10:30
ExitAddress 91.102.152.227 2010-12-28 10:35:30
</pre>

<br>
<h2 id="torperf" class="hover">Torperf's and OnionPerf's Performance Data
<a href="#torperf" class="anchor">#</a>
</h2>

<p>
The performance measurement services Torperf and OnionPerf publish performance data
from making simple HTTP requests over the Tor network.
Torperf/OnionPerf use a SOCKS client to download files of various sizes
over the Tor network and notes how long substeps take.
</p>

<h3 id="type-torperf" class="hover">Torperf and OnionPerf Measurement Results
<small><code>@type torperf 1.1</code></small>
<a href="/collector/recent/torperf/" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> recent</a>
<a href="/collector/archive/torperf/" class="btn btn-primary btn-xs"><i class="fa fa-chevron-right" aria-hidden="true"></i> archive</a>
<a href="#type-torperf" class="anchor">#</a>
</h3>

<p>
A Torperf or OnionPerf results file contains a single line per Torperf/OnionPerf run with
<code>key=value</code> pairs.
Such a result line is sufficient to learn about 1) the Tor and Torperf/OnionPerf
configuration, 2) measurement results, and 3) additional information that
might help explain the results.
Known keys in <code>@type torperf 1.0</code> are explained below.
</p>
<ul>
<li>Configuration
<ul>
<li><code>SOURCE:</code> Configured name of the data source; required.</li>
<li><code>FILESIZE:</code> Configured file size in bytes; required.</li>
<li>Other meta data describing the Tor or Torperf/OnionPerf configuration, e.g.,
GUARD for a custom guard choice; optional.</li>
</ul></li>
<li>Measurement results
<ul>
<li><code>START:</code> Time when the connection process starts;
required.</li>
<li><code>SOCKET:</code> Time when the socket was created; required.</li>
<li><code>CONNECT:</code> Time when the socket was connected; required.</li>
<li><code>NEGOTIATE:</code> Time when SOCKS 5 authentication methods have been
negotiated; required.</li>
<li><code>REQUEST:</code> Time when the SOCKS request was sent; required.</li>
<li><code>RESPONSE:</code> Time when the SOCKS response was received;
required.</li>
<li><code>DATAREQUEST:</code> Time when the HTTP request was written;
required.</li>
<li><code>DATARESPONSE:</code> Time when the first response was received;
required.</li>
<li><code>DATACOMPLETE:</code> Time when the payload was complete;
required.</li>
<li><code>WRITEBYTES:</code> Total number of bytes written; required.</li>
<li><code>READBYTES:</code> Total number of bytes read; required.</li>
<li><code>DIDTIMEOUT:</code> 1 if the request timed out, 0 otherwise;
optional.</li>
<li><code>DATAPERCx:</code> Time when x% of expected bytes were read for
x = { 10, 20, 30, 40, 50, 60, 70, 80, 90 }; optional.</li>
<li>Other measurement results, e.g., START_RENDCIRC, GOT_INTROCIRC, etc.
for hidden-service measurements; optional.</li>
</ul></li>
<li>Additional information
<ul>
<li><code>LAUNCH:</code> Time when the circuit was launched; optional.</li>
<li><code>USED_AT:</code> Time when this circuit was used; optional.</li>
<li><code>PATH:</code> List of relays in the circuit, separated by commas;
optional.</li>
<li><code>BUILDTIMES:</code> List of times when circuit hops were built,
separated by commas; optional.</li>
<li><code>TIMEOUT:</code> Circuit build timeout in milliseconds that the Tor
client used when building this circuit; optional.</li>
<li><code>QUANTILE:</code> Circuit build time quantile that the Tor client
uses to determine its circuit-build timeout; optional.</li>
<li><code>CIRC_ID:</code> Circuit identifier of the circuit used for this
measurement; optional.</li>
<li><code>USED_BY:</code> Stream identifier of the stream used for this
measurement; optional.</li>
<li>Other fields containing additional information; optional.</li>
</ul></li>
</ul>

<p>OnionPerf adds a few more keys in <code>@type torperf 1.1</code>:</p>
<ul>
<li><code>ENDPOINTLOCAL:</code> Hostname, IP address, and port that the TGen client used to connect to the local tor SOCKS port, formatted as <code>hostname:ip:port</code>, which may be <code>"NULL:0.0.0.0:0"</code> if TGen was not able to find this information; optional.</li>
<li><code>ENDPOINTPROXY:</code> Hostname, IP address, and port that the TGen client used to connect to the SOCKS proxy server that tor runs, formatted as <code>hostname:ip:port</code>, which may be <code>"NULL:0.0.0.0:0"</code> if TGen was not able to find this information; optional.</li>
<li><code>ENDPOINTREMOTE:</code> Hostname, IP address, and port that the TGen client used to connect to the remote server, formatted as <code>hostname:ip:port</code>, which may be <code>"NULL:0.0.0.0:0"</code> if TGen was not able to find this information; optional.</li>
<li><code>HOSTNAMELOCAL:</code> Client machine hostname, which may be <code>"(NULL)"</code> if the TGen client was not able to find this information; optional.</li>
<li><code>HOSTNAMEREMOTE:</code> Server machine hostname, which may be <code>"(NULL)"</code> if the TGen server was not able to find this information; optional.</li>
<li><code>SOURCEADDRESS:</code> Public IP address of the OnionPerf host obtained by connecting to well-known servers and finding the IP address in the result, which may be <code>"unknown"</code> if OnionPerf was not able to find this information; optional.</li>
</ul>

    </div>

    <br>


<div class="container">
      <div class="row">
        <div class="col-xs-12">
        <br>
          <h1 id="automated-downloads" class="hover">Automated Downloads
<a href="#automated-downloads" class="anchor">#</a>
</h1>
<p>
There are multiple ways to download descriptors from this site.
Of course, the obvious way is to browse the directories and download contained files using your browser.  However, this method cannot be automated very well.</p>

<h2 id="recursive-wget" class="hover">Recursive downloads using <code>wget</code>
<a href="#recursive-wget" class="anchor">#</a>
</h2>

<p>A more elaborate way to automatically download descriptors is to use Unix tools like <code>wget</code> which support recursively downloading files from this site.  Example:</p>
<pre>
wget --recursive \                     # turn on recursive retrieving
     --reject "index.html*" \          # don't retrieve directory listings
     --no-parent \                     # don't ascend to parent directory
     --no-host-directories \           # don't generate host-prefixed directories
     --directory-prefix descriptors \  # set directory prefix
     https://collector.torproject.org/recent/relay-descriptors/microdescs/
</pre>

<h2 id="index-json" class="hover">Custom downloaders using provided <code>index.json</code>
<a href="#index-json" class="anchor">#</a></h2>

<p>Another automated way to download descriptors is to develop a tool that uses the provided <a href="https://collector.torproject.org/index/index.json" target="_blank"><code>index.json</code></a> file or one of its compressed versions <a href="https://collector.torproject.org/index/index.json.gz" target="_blank"><code>index.json.gz</code></a>, <a href="https://collector.torproject.org/index/index.json.bz2" target="_blank"><code>index.json.bz2</code></a>, or <a href="https://collector.torproject.org/index/index.json.xz" target="_blank"><code>index.json.xz</code></a>.
These files contain a machine-readable representation of all descriptor files available on this site.
Index files use the following custom JSON data format that might still be extended at a later time:</p>
<ul>
<li>Index object: At the document root there is always an index object with the following fields:
<ul>
<li><code>"index_created"</code>: Timestamp when this index was created using pattern <code>"YYYY-MM-DD HH:MM"</code> in the UTC timezone.</li>
<li><code>"path"</code>: Base URL of this index file and all included resources.</li>
<li><code>"files"</code>: List of file objects of files available from the document root, which will be omitted if empty.
<li><code>"directories"</code>: List of directory objects of directories available from the document root, which will be omitted if empty.</li>
</ul></li>
<li>Directory object: There is one directory object for each directory or subdirectory in the document tree containing similar fields as the index object:
<ul>
<li><code>"path"</code>: Relative path of the directory.</li>
<li><code>"files"</code>: List of file objects of files available from this directory, which will be omitted if empty.
<li><code>"directories"</code>: List of directory objects of directories available from this directory, which will be omitted if empty.</li>
</ul></li>
<li>File object: Each file that is available in the document tree is represented by a file object with the following fields:
<ul>
<li><code>"path"</code>: Relative path of the file.</li>
<li><code>"size"</code>: Size of the file in bytes.</li>
<li><code>"last_modified"</code>: Timestamp when the file was last modified using pattern <code>"YYYY-MM-DD HH:MM"</code> in the UTC timezone.</li>
</ul></li>
</ul>

        </div><!-- col -->
      </div><!-- row -->

    <br>



</div>

<jsp:include page="bottom.jsp"/>
