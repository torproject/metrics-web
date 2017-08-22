<jsp:include page="top.jsp">
<jsp:param name="pageTitle" value="Sources &ndash; Tor Metrics"/>
<jsp:param name="navActive" value="Sources"/>
</jsp:include>
<div class="container">
<ul class="breadcrumb">
<li><a href="/">Home</a></li>
<li><a href="sources.html">Sources</a></li>
<li class="active">${breadcrumb}</li>
</ul>
</div>
<div class="container">
<header>
<div id="rfc.title">
<h1>Tor web server logs</h1>
</div>
</header>
</div> <!-- container -->
<div class="container">
<section id="n-purpose-of-this-document">
<h2 id="rfc.section.1" class="np"><a href=
"#rfc.section.1">1.</a>&nbsp;<a href=
"#n-purpose-of-this-document">Purpose of this document</a></h2>
<div id="rfc.section.1.p.1">
<p>BETA: As of November 8, 2017, this document is still under
discussion and subject to change without prior notice. Feel free to
<a href="/about.html#contact">contact us</a> for questions or
concerns regarding this document.</p>
</div>
<div id="rfc.section.1.p.2">
<p>Tor's web servers, like most web servers, keep request logs for
maintenance and informational purposes.</p>
</div>
<div id="rfc.section.1.p.3">
<p>However, unlike most other web servers, Tor's web servers use a
privacy-aware log format that avoids logging too sensitive data
about their users.</p>
</div>
<div id="rfc.section.1.p.4">
<p>Also unlike most other web server logs, Tor's logs are neither
archived nor analyzed before performing a number of post-processing
steps to further reduce any privacy-sensitive parts.</p>
</div>
<div id="rfc.section.1.p.5">
<p>This document describes 1) meta-data contained in log file names
written by Tor's web servers, 2) the privacy-aware log format used
in these files, and 3) subsequent sanitizing steps that are applied
before archiving and analyzing these log files.</p>
</div>
<div id="rfc.section.1.p.6">
<p>As a basis for our current implementation this document also
describes the naming conventions for the input log files, which is
just a description of the current state and subject to change.</p>
</div>
<div id="rfc.section.1.p.7">
<p>As a convention for this document, all format strings conform to
the format strings used by <a href=
"http://httpd.apache.org/docs/current/mod/mod_log_config.html">Apache's
mod_log_config module</a>.</p>
</div>
</section>
</div> <!-- container -->
<div class="container">
<section id="n-log-file-metadata">
<h2 id="rfc.section.2"><a href=
"#rfc.section.2">2.</a>&nbsp;<a href="#n-log-file-metadata">Log
file metadata</a></h2>
<div id="rfc.section.2.p.1">
<p>Log files have meta-data that is not part of the file's
contents, in particular, the names of the virtual and physical
hosts.</p>
</div>
<div id="rfc.section.2.p.2">
<p>All access log files written by Tor's web servers follow the
naming convention &lt;virtual-host&gt;-access.log-YYYYMMDD, where
"YYYYMMDD" is the date of the rotation and finalization of the log
file, which is not used in the further sanitizing process. The
"access.log" part serves as a marker for web server access
logs.</p>
</div>
<div id="rfc.section.2.p.3">
<p>The virtual hostname can be inferred from the input log's name,
whereas the physical hostname needs to be provided by other means.
Currently, log files are made available to the santizer in a
separate directory per physical web server host. Log files are
typically gz-compressed, which is indicated by appending ".gz" to
log file names, but this is subject to change. Files with unknown
compression type are discarded (currently ".xz", ".gz", and ".bz2"
are recognized). Overall, the sanitizer expects log files to use
the following path format:</p>
<ul class="empty">
<li>
&lt;physical-host&gt;/&lt;virtual-host&gt;-access.log-YYYYMMDD[.gz]</li>
</ul>
</div>
<div id="rfc.section.2.p.4">
<p>As first safeguard against publishing log files that are too
sensitive, we discard all files not matching the naming convention
for access logs. This is to prevent, for example, error logs from
slipping through.</p>
</div>
</section>
</div> <!-- container -->
<div class="container">
<section id="n-privacy-aware-log-format">
<h2 id="rfc.section.3"><a href=
"#rfc.section.3">3.</a>&nbsp;<a href="#n-privacy-aware-log-format">Privacy-aware
log format</a></h2>
<div id="rfc.section.3.p.1">
<p>Tor's Apache web servers are configured to write log files that
extend Apache's Combined Log Format with a couple tweaks towards
privacy. For example, the following Apache configuration lines were
in use at the time of writing (subject to change):</p>
<ul class="empty">
<li>LogFormat "0.0.0.0 - %u %{[%d/%b/%Y:00:00:00 %z]}t \"%r\"
%&gt;s %b \"%{Referer}i\" \"-\" %{Age}o" privacy</li>
<li>LogFormat "0.0.0.1 - %u %{[%d/%b/%Y:00:00:00 %z]}t \"%r\"
%&gt;s %b \"%{Referer}i\" \"-\" %{Age}o" privacyssl</li>
<li>LogFormat "0.0.0.2 - %u %{[%d/%b/%Y:00:00:00 %z]}t \"%r\"
%&gt;s %b \"%{Referer}i\" \"-\" %{Age}o" privacyhs</li>
</ul>
</div>
<div id="rfc.section.3.p.2">
<p>The main difference to Apache's Common Log Format is that
request IP addresses are removed and the field is instead used to
encode whether the request came in via http:// (0.0.0.0), via
https:// (0.0.0.1), or via the site's onion service (0.0.0.2).</p>
</div>
<div id="rfc.section.3.p.3">
<p>Tor's web servers are configured to use UTC as timezone, which
is also highly recommended when rewriting request times to
"00:00:00" in order for the subsequent sanitizing steps to work
correctly. Alternatively, if the system timezone is not set to UTC,
web servers should keep request times unchanged and let them be
handled by the subsequent sanitizing steps.</p>
</div>
<div id="rfc.section.3.p.4">
<p>Tor's web servers are configured to rotate logs at least once
per day, which does not necessarily happen at 00:00:00 UTC. As a
result, log files may contain requests from up to two UTC days and
several log files may contain requests that have been started on
the same UTC day.</p>
</div>
</section>
</div> <!-- container -->
<div class="container">
<section id="n-sanitizing-steps">
<h2 id="rfc.section.4"><a href=
"#rfc.section.4">4.</a>&nbsp;<a href="#n-sanitizing-steps">Sanitizing
steps</a></h2>
<div id="rfc.section.4.p.1">
<p>The request logs written by Tor's web servers still contain too
many details that we are uncomfortable publishing. Therefore, we
apply a couple of sanitizing steps on these log files before making
them public and analyzing them ourselves. Some of these steps could
as well be made directly by Apache, but others can only be made
with a delay.</p>
</div>
<div class="container">
<section id="n-discarding-non-matching-lines">
<h3 id="rfc.section.4.1"><a href=
"#rfc.section.4.1">4.1.</a>&nbsp;<a href=
"#n-discarding-non-matching-lines">Discarding non-matching
lines</a></h3>
<div id="rfc.section.4.1.p.1">
<p>Log files are expected to contain exactly one request per line.
We process these files line by line and discard any lines not
matching the following criteria:</p>
<ul class="empty">
<li>Lines begin with Apache's Common Log Format ("%h %l %u %t
\"%r\" %&gt;s %b") or a compatible format like one of Tor's privacy
formats. It is acceptable if lines start with a format that is
compatible to the Common Log Format and continue with additional
fields. Those additional fields will later be discarded, but the
line will not be discarded because of them.</li>
<li>The request protocol is HTTP.</li>
<li>The request method is either GET or HEAD.</li>
<li>The final status of the request is neither 400 ("Bad Request")
nor 404 ("Not Found").</li>
</ul>
</div>
<div id="rfc.section.4.1.p.2">
<p>Any lines not meeting all these criteria will be discarded, and
processing continues with the next line.</p>
</div>
<div id="rfc.section.4.1.p.3">
<p>In addition, log lines are treated differently according to the
date they contain:</p>
<ul class="empty">
<li>During an import process the sanitizer takes all log line dates
into account and determines the reference interval as stretching
from the oldest date to the youngest date encountered. Depending on
the reference interval log lines are not yet processed, if their
date is on the edges of the reference interval, i.e., the date is
not at least a day younger than the older endpoint or the date is
only LIMIT days older than the younger endpoint, where LIMIT is
initially set to two, but this might change if necessary.</li>
<li>If the younger endpoint of the reference interval coincides
with the current system date, the day before is used as the new
younger reference interval endpoint, which ensures that the
sanitizer won't publish logs prematurely, i.e., before there is a
chance that they are complete. Thus, processing of log lines
carrying such date is postponed.</li>
<li>All log lines with dates for which the sanitizer already
published a log file are discarded in order to avoid altering
published logs.</li>
</ul>
</div>
</section>
</div> <!-- container -->
<div class="container">
<section id="n-rewriting-matching-lines">
<h3 id="rfc.section.4.2"><a href=
"#rfc.section.4.2">4.2.</a>&nbsp;<a href=
"#n-rewriting-matching-lines">Rewriting matching lines</a></h3>
<div id="rfc.section.4.2.p.1">
<p>All matching lines, which are already checked to match Apache's
Common Log Format ("%h %l %u %t \"%r\" %&gt;s %b"), are rewritten
following these rules:</p>
<ul class="empty">
<li>%h: If the remote hostname starts with "0.0.0.", it is kept
unchanged, otherwise it's rewritten to "0.0.0.0".</li>
<li>%l: The remote logname, if present, is rewritten to "-".</li>
<li>%u: The remote user, if present, is rewritten to "-".</li>
<li>%t: The time the request was received is converted to UTC,
unless the time is already given in UTC, and time and time zone
components are rewritten to "00:00:00 +0000". Date components are
kept unchanged.</li>
<li>%r: If the first line of request contains a query string, that
query string is removed from "?" to the end of the request string.
Otherwise the first line of request is kept unchanged.</li>
<li>%&gt;s: The final status is kept unchanged.</li>
<li>%b: The size of response in bytes is kept unchanged.</li>
</ul>
</div>
<div id="rfc.section.4.2.p.2">
<p>Any columns exceeding Apache's Common Log Format are
discarded.</p>
</div>
<div id="rfc.section.4.2.p.3">
<p>The result is still supposed to be fully compatible with the
Common Log Format and can be processed by any tools being capable
of processing that format.</p>
</div>
</section>
</div> <!-- container -->
<div class="container">
<section id="n-re-assembling-log-files">
<h3 id="rfc.section.4.3"><a href=
"#rfc.section.4.3">4.3.</a>&nbsp;<a href=
"#n-re-assembling-log-files">Re-assembling log files</a></h3>
<div id="rfc.section.4.3.p.1">
<p>Rewritten log lines are re-assembled into sanitized log files
based on physical host, virtual host, and request start date.</p>
</div>
<div id="rfc.section.4.3.p.2">
<p>The naming convention for sanitized log files is:</p>
<ul class="empty">
<li>
&lt;virtual-host&gt;_&lt;physical-host&gt;_access.log_YYYYMMDD[.xz]</li>
</ul>
<p>The underscore is a separator symbol between the various parts
of the filename.</p>
</div>
<div id="rfc.section.4.3.p.3">
<p>Sanitized log files may additionally be sorted into directories
by virtual host and date as in:</p>
<ul class="empty">
<li>
&lt;virtual-host&gt;/YYYY/MM/&lt;virtual-host&gt;_&lt;physical-host&gt;_access.log_YYYYMMDD[.xz]</li>
</ul>
<p>The virtual hostnames, like 'metrics.torproject.org' or
'dist.torproject.org', are more familiar to the public and were
therefore chosen to be the first naming component.</p>
</div>
<div id="rfc.section.4.3.p.4">
<p>As last and certainly not least important sanitizing step, all
rewritten log lines are sorted alphabetically, so that request
order cannot be inferred from sanitized log files.</p>
</div>
<div id="rfc.section.4.3.p.5">
<p>Sanitized log files are typically compressed before publication.
In particular the sorting step allows for highly efficient
compression rates. We typically use XZ for compression, which is
indicated by appending ".xz" to log file names, but this is subject
to change.</p>
</div>
</section>
</div> <!-- container -->
</section>
</div> <!-- container -->
<jsp:include page="bottom.jsp"/>
