<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<jsp:include page="top.jsp">
  <jsp:param name="pageTitle" value="Research &ndash; Tor Metrics"/>
  <jsp:param name="navActive" value="Research"/>
</jsp:include>

    <div class="container">
      <ul class="breadcrumb">
        <li><a href="index.html">Home</a></li>
        <li class="active">Research</li>
      </ul>
    </div>

    <div class="container">
      <h1>Research <a href="#research" name="research" class="anchor">#</a></h1>
      <p>You're a researcher and want to write a paper related to the public, deployed Tor network?  Here you'll find other relevant papers to cite, technical reports to understand all the details, and existing tools to conduct research on the Tor network.</p>
    </div>

    <div class="container">
      <h2>Citation <a href="#cite" name="cite" class="anchor">#</a></h2>
      <p>Feel free to use our data for your research.  If you do, please cite <a href="https://metrics.torproject.org/" target="_self">https://metrics.torproject.org/</a> or the following <a href="http://freehaven.net/anonbib/#wecsr10measuring-tor" target="_blank">paper</a>:</p>
      <p><pre>
@inproceedings{wecsr10measuring-tor,
  title = {A Case Study on Measuring Statistical Data in the {T}or Anonymity Network},
  author = {Karsten Loesing and Steven J. Murdoch and Roger Dingledine},
  booktitle = {Proceedings of the Workshop on Ethics in Computer Security Research (WECSR 2010)},
  year = {2010},
  month = {January},
  location = {Tenerife, Canary Islands, Spain},
  publisher = {Springer},
  series = {LNCS},
}</pre></p>
      <p>Thank you for acknowledging this work through a citation.</p>
    </div>

    <div class="container">
      <h2>Technical reports <a href="#techreports" name="techreports" class="anchor">#</a></h2>
      <p>From time to time we're writing <a href="https://research.torproject.org/techreports.html" target="_blank">technical reports</a> with further details on how we collect statistics in the Tor network or how we use them to learn new interesting facts.  Be sure to take a look!</p>
    </div>

    <div class="container">
      <h2>Research tools <a href="#tools" name="tools" class="anchor">#</a></h2>
      <p>The following tools are primarily useful if you want to do research using Tor network data.</p>
      <ul>
        <li><a href="https://torps.github.io/" target="_blank">TorPS</a> simulates changes to Tor's path selection algorithm using archived data.</li>
        <li><a href="https://shadow.github.io/" target="_blank">Shadow</a> uses archived Tor directory data to generate realistic network topologies.</li>
      </ul>
    </div>

<jsp:include page="bottom.jsp"/>

