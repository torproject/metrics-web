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
<li class="active">Guidelines for getting your data into Tor Metrics</li>
</ul>
</div>

<div class="container">

<h1>Guidelines for getting your data into Tor Metrics
<a href="#metrics-guidelines" id="metrics-guidelines" class="anchor">#</a></h1>

<h2>Scope and preliminaries</h2>

<p>This document provides guidelines to authors and operators of tools that
 collect data about the publicly deployed Tor network that would like to
 contribute data, or allow data to be contributed easily by others using
 the tool, to Tor Metrics.</p>

<p>This document does not discuss how to ensure measurements are safe, for this
 refer to the Research Safety Board Guidelines [<a href="#ref-0">0</a>] and Guidelines for
 Performing Safe Measurement on the Internet [<a href="#ref-1">1</a>].</p>

<h2>What data belongs on Tor Metrics?</h2>

<ul>
<li>If it happens in the public deployed Tor network it likely belongs on Tor
 Metrics.</li>
<li>If it happens for a short term only, like for a research project, it's
   unlikely worth the effort to have Tor Metrics archive, publish, aggregate,
   and visualize it. In this case you should collect the data yourself (keeping
   in mind research ethics!), and we can later talk about linking to it or even
   using it as external data.</li>
<li>If your data is a combination of existing data on Tor Metrics plus maybe
   external data, we shouldn't add it, either. In such a case we should rather
   talk about extending our services towards what your service does, if that
   makes sense.</li>
</ul>

<h2>What data do you want to see on Tor Metrics?</h2>

<p>This section aims to help you organize your thoughts before making a request
 to the Metrics team. It might be that there are good reasons that something is
 not done in one of the preferred ways, but ideally data collection tools can
 be written with this guidelines in mind.</p>

<ul>
<li>What is your data about? Is it about servers or users or both? Is it
   passively gathered or actively measured or both?</li>
<li>This will help us to decide how we might present the data on Tor Metrics
   and perhaps which other datasets we have that might benefit from being
   combined with the new dataset.</li>
<li>Is there a way for you to aggregate the data before you hand it over to us?
   Of course this requires more thinking upfront, but it's a great way to ensure
   not to give out too sensitive data to us or anyone else. It's not always
   possible or even useful to aggregate data and discard the original data,
   though. Two examples:<ol>
   <li>Relays count how many clients download the consensus from them and from
     which country they connect. When 24 hours have passed, they include the
     count by country in their next extra-info descriptor. This is aggregated
     data. The obviously more sensitive, non-aggregated variant would be for
     relays to provide a log of clients downloading consensuses.</li>
   <li>The torproject.org webservers keep highly sanitized logs of web clients
     making requests to them that we sanitize even more before we archive them.
     This is non-aggregated data. The possibly less sensitive aggregated variant
     would be for webservers to count requests by requested URL or similar.</li>
  </ol></li>
<li>Is the data you're planning to give us too sensitive? If so, can you sanitize
   it yourself before giving it to us (we can help you with that), or does the
   sanitizing need to happen on our side (we should still involve you in this
   case)?
<p>There are currently cases where Tor Metrics performs the sanitization of
   data before archiving, but the preferred system would sanitize the data as
   close to the source as possible to minimize the possibility that sensitive
   data could be leaked.</li>
<li>How will you expect that Tor Metrics will fetch your data? For most data
   currently, CollecTor fetches from a web server secured with TLS. This is
   the easiest and quickest method to implement and so there should be a good
   reason to not use this method.</li>
<li>When is your data available and for how long? Ideally, we'd survive reboots
   or downtimes on our side for up to 72 hours without losing any of your data.
   Typically, you'd implement this using a cache. If that is hard or impossible
   to do on your side, we'll have to think about adding redundancy on our side.
   That's all possible and we did it before, it'll just make the process take
   longer.</li>
<li>Do you expect any difficulties on our side to write code that processes your
   data? If we only need to fetch and store your data, probably not. But if we
   have to inflate, parse, verify, combine, sanitize, split, and deflate your
   data, maybe. And if we need to include fancy crypto libraries in order to
   process your data, then for sure. Any intuitions you have about possible
   difficulties would be good to know, even if things turn out to be easier in
   the end.
<p>As far as possible, use simple formats for providing data. The Tor Directory
   Protocol meta-format [<a href="#ref-2">2</a>, §1.2] is a simple format for which we already have
   parsers. Without good reason, do not serialize to formats such as YAML, TOML,
   etc. as this would require adding a new parsing library into Tor Metrics just
   to parse the new data.</p></li>
<li>How much data do you think you'll give us over the next five years? A
   ballpark figure is fine, like the number of bytes as a power of ten.</li>
   </ul>

<h2>What belongs into the data format for the data to be archived?</h2>

<dl>
<dt>Timestamp</dt><dd>We're using the timestamp to place the data item into the right
   archive file, among other things. Exception: microdescriptors do not contain
   a timestamp, which makes them a pain to archive.</dd>
<dt>Source identifier</dt><dd>Ideally, we'd expect a cryptographic identifier of the
   source, but if that is not available, any identifier will do. Exception: exit
   lists do not contain a source identifier, because there happened to be just
   one exit list scanner in the network; you can see how this doesn't scale so
   well.</dd>
<dt>Generator identifier</dt><dd>The name of software and its version (either release
   or a commit reference) that produced the result. If a bug is discovered in the
   software then this allows us to see which data may have been affected by it.</dd>
<dt>Network location</dt><dd>If performing active measurement, the network location of
   the vantage point (e.g. IP address, ASN, and/or country) can help to provide
   context when comparing between different vantage points.</dd>
<dt>Signature</dt><dd>The signature is the proof that the source produced the data item,
   not us. And even if we don't verify all signatures, others might want to do
   that. If you are using the Tor Directory Protocol meta-format to serialize
   your metrics then signing metrics using RSA or Ed25519 signatures can be
   done easily. Signatures should not be detached to keep fetching, archiving
   and validating simple. Exception: hello, exit list, you again!</dd>
</dl>

<h2>First steps</h2>

<p>You're still reading, so it seems that we caught your interest! How should we
 start?</p>

<ul>
<li>Is the data already publicly available somewhere and all you want is discuss
   a way to include it in Tor Metrics? That's easy then. Just share with us what
   you have and we can talk.</li>
<li>If the data is not public yet, do you maybe have a data format that we can
   discuss? Bonus points if it comes with samples, but only if you're absolutely
   certain that the data is safe to be published.</li>
<li>If you have none of the above, can you share logs with us, so that we can
   help you derive a possible data format? It doesn't need to be recent logs
   (even though time might not magically make your data safe to be published).
   You could edit the logs and take out any parts you think are too sensitive.
   And you should encrypt the data before sending it to us.</li>
<li>If you have nothing at all yet, let's talk anyway. Describe to us what you   think would be good to include in Tor Metrics, and we'll figure something
   out.</li>
</ul>

<h2>How will Tor Metrics include the data?</h2>

 <p>It's a process to get your data on Tor Metrics, and not a short one. Let's go
 through the necessary steps for doing it. After each step we should together
 decide whether we're ready to move forward, need to take a step back, or maybe
 even stop the project, because we found out that it's not what we wanted.</p>

<ul>
 <li>If you can, give us a few months as heads-up. Ideally, it won't take us that
   long to do this project, but we'd prefer to make room for it in our next
   six-month roadmap. Otherwise we might not be able to do it right away.</li>
<li> We discuss your data format with you and other Tor developers on the public
   tor-dev@ mailing list. Maybe you or we need to write a Tor proposal for this.</li>
<li>We write a documentation page for the data format plus any necessary
   sanitizing steps. See the Tor Metrics website and the tor-spec Git repository
   for a couple of examples.</li>
<li>We write code for metrics-lib and/or Stem to parse your data and verify the
   data format. At this point we'll find out if there are any misunderstandings
   regarding data types or data structure that we haven't seen before.</li>
<li>We write code for CollecTor to fetch and archive your data, but without
   publishing just yet. As part of this we also agree on file names and URLs
   where your data will later be available.</li>
<li>We make a one-time visualization using your data, mostly as a sanity check.
   You'd be surprised how many issues are hiding well enough that we would
   otherwise not find them.</li>
<li>At this point we can think about adding your data to our services like
   Onionoo, Relay Search, and ExoneraTor and our visualizations on Tor Metrics.
   Typically, we'd do that as a separate project, though.</li>
<li>Finally we make your data available for download on CollecTor and put the
   documentation on the Tor Metrics website. We announce that your data is now
   on Tor Metrics.</li>
</ul>

<h2>Maintenance</h2>

<p>Congratulations, your data is now on Tor Metrics. But that's not the end of the
 story! Here's what we need you to do as long as we have your data:</p>

<ul><li>Make sure that we always get the data by whatever means we came up with
   together. Avoid longer downtimes and fix any related issues in a timely
   fashion. We do care about this, because people will come to us and complain
   that "our" data is not up-to-date, when it may in fact be your fault.</li>
<li>If you're planning to make any changes that affect the data format or the way
   how the data comes to us, talk to us beforehand with enough time to make such
   changes. Several weeks in advance would be good, because we may have to
   inform our users about upcoming changes and give them some time to update
   their tools.</li>
   <li>Let's be honest: we had to remove data from Tor Metrics in the past, because
   the services providing them have become unreliable or unmaintained. In such a
   case we'd talk to you and try to improve the situation. But if that doesn't
   work, we'd remove your data from Tor Metrics with enough heads up time for
   you and others to prepare. We'd very likely archive your data and keep it
   around in such a case. Sorry, and thanks for understanding!</li>
</ul>

<h2>References</h2>

[<a id="ref-0">0</a>] Tor Project. Research Safety Board Guidelines.
    <a href="https://research.torproject.org/safetyboard/#guidelines">https://research.torproject.org/safetyboard/#guidelines</a><br>
[<a id="ref-1">1</a>] I. Learmonth. Guidelines for Performing Safe Measurement on the Internet.
    (Work-in-progress). <a href="https://datatracker.ietf.org/doc/draft-learmonth-pearg-safe-internet-measurement/">https://datatracker.ietf.org/doc/draft-learmonth-pearg-safe-internet-measurement/</a><br>
[<a id="ref-2">2</a>] Tor Project. Tor Directory Protocol, version 3.
    <a href="https://spec.torproject.org/dir-spec">https://spec.torproject.org/dir-spec</a>

</div><!-- .container -->

<jsp:include page="bottom.jsp"/>

