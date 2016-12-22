<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<jsp:include page="top.jsp">
  <jsp:param name="pageTitle" value="About &ndash; Tor Metrics"/>
  <jsp:param name="navActive" value="About"/>
</jsp:include>

    <div class="container">
      <ul class="breadcrumb">
        <li><a href="index.html">Home</a></li>
        <li class="active">About</li>
      </ul>
    </div>

    <div class="container">

      <h1>About <a href="#about" name="about" class="anchor">#</a></h1>

      <p>Tor metrics are updated daily to provide transparency for Tor users, give feedback for developers, and measure evidence of network anomalies.</p>
    </div>

    <div class="container">

      <br>

      <h2>Philosophy <a href="#philosophy" name="philosophy" class="anchor">#</a></h2>

      <p>We only use public, non-sensitive data for metrics. Each metric goes through a rigorous review and discussion process before appearing here. We never publish statistics&mdash;or aggregate statistics&mdash;of sensitive data, such as unencrypted contents of traffic.</p>

      <br>

      <h2>Contributing <a href="#contributing" name="contributing" class="anchor">#</a></h2>

      <p>Collecting and processing new data won't likely happen without your help! If you really want to see something measured here, we would be happy to work with you.  (Link to Trac wiki page)</p>

      <br>

      <h2>Contact <a href="#contact" name="contact" class="anchor">#</a></h2>

      <p>If you have any questions, contact us at <a href="mailto:metrics-team@lists.torproject.org">metrics-team@lists.torproject.org</a>.</p>

      <br>

      <h2>Testimonials <a href="#testimonials" name="testimonials" class="anchor">#</a></h2>
      <blockquote>
        <p class="mb-0">
          Metrics are a critical part of any security technology. If you don't know how the technology works in practice, you can't find and fix problems. You can't improve the security. You can't make it work better. This isn't glamorous or sexy work, but it's essential. This is especially true for security and privacy, where our preconceived notions of threats and usage are regularly wrong&mdash;and knowing what's really going on is the difference between security and insecurity.<br><br>
          Tor is doing cutting-edge work in the anonymity space, and Tor metrics are already proven to provide critical information for research and development. It's one of the few open data sets available for how, why, where, and when people use anonymizing technologies.<br><br>
          Tor's metrics project increases the transparency of Tor's work. This helps users understand how Tor works. With good network metrics, you can look back for indicators and anomalies at the time a privacy issue was reported. You can also extrapolate and look forward to prevent related issues in the future. This helps alleviate users' security concerns, and helps others contribute to security issues in the network and browser.<br><br>
          Finally, Tor metrics are the ammunition that lets Tor and other security advocates argue for a more private and secure  Internet from a position of data, rather than just dogma or perspective. It's where the real world influences Tor.
        </p>
        <footer class="blockquote-footer">Bruce Schneier (June 1, 2016)</footer>
      </blockquote>

    </div>

<jsp:include page="bottom.jsp"/>

