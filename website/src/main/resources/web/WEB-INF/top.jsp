<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE HTML>
<html lang="en" style="min-height:100%;">
<head>

  <title>${param.pageTitle}</title>

  <meta charset="utf-8">
  <link href="/images/favicon.ico" type="image/x-icon" rel="shortcut icon">

  <!-- yes, we are handheld friendly :) -->
  <meta name="HandheldFriendly" content="True">
  <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
  <meta name="apple-mobile-web-app-capable" content="yes">

  <!-- icons for mobile devices -->
  <link rel="apple-touch-icon" href="/images/apple-touch-icon-152x152.png">
  <link rel="shortcut icon" href="/images/android-icon.png" sizes="196x196">
  <meta name="msapplication-square70x70logo" content="/images/smalltile.png">
  <meta name="msapplication-square150x150logo" content="/images/mediumtile.png">
  <meta name="msapplication-wide310x150logo" content="/images/widetile.png">
  <meta name="msapplication-square310x310logo" content="/images/largetile.png">

  <!-- jQuery -->
  <script src="/js/jquery-3.2.1.min.js"></script>

  <!-- Bootstrap -->
  <link rel="stylesheet" href="/css/bootstrap.min.css">
  <script src="/js/bootstrap.min.js"></script>

  <!-- Fonts -->
  <link rel="stylesheet" href="/css/font-awesome.min.css">
  <link rel="stylesheet" href="/fonts/source-sans-pro.css">

  <!-- Prism -->
  <link rel="stylesheet" href="/css/prism.css">
  <script src="/js/prism.js"></script>

  <!-- custom styles and javascript -->
  <link rel="stylesheet" href="/css/style.css">
  <script src="/js/script.js"></script>

</head>

<body class="noscript" style="background-image:url(images/ajax-loader.gif);background-repeat:no-repeat;background-position:center center;">

<!-- Using evil inline stylesheets to hide the FOUC for slow connections... -->

<div id="wrapper" style="display:none;">

<!-- backToTop-Button and Anchor -->
<noscript>
<div class="topButton" style="display:block;"><a href="#top"><i class="fa fa-chevron-up" aria-hidden="true"></i></a></div>
</noscript>
<script type="text/javascript">
document.write('<div class="topButton" style="display:none;"><a href="#top"><i class="fa fa-chevron-up" aria-hidden="true"></i></a></div>');
</script>
<a id="top"></a>

<!-- secondary navigation -->
<nav class="navbar navbar-default navbar-secondary">
  <div class="container-fluid">
    <input type="checkbox" id="navbar-toggle-checkbox">
    <div class="navbar-header">
      <label for="navbar-toggle-checkbox" type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#bs-example-navbar-secondary" aria-expanded="false">
        <span class="sr-only">Toggle navigation</span>
        <span class="icon-bar"></span>
        <span class="icon-bar"></span>
        <span class="icon-bar"></span>
      </label>
      <a class="navbar-brand visible-xs" href="/"><img src="/images/tor-metrics-white.png" width="232" height="50" alt="Tor Metrics"></a>
    </div>

    <!-- Collect the nav links, forms, and other content for toggling -->
    <div class="collapse navbar-collapse" id="bs-example-navbar-secondary">
      <ul class="nav navbar-nav navbar-right">

        <!-- we have to copy the primary navigation items here for mobile accessibility -->
        <li class="visible-xs section-header">Metrics</li>
        <li class="visible-xs<c:if test="${'Home'.equals(param.navActive)}"> active</c:if>"><a href="/"><i class="fa fa-home fa-fw" aria-hidden="true"></i> Home</a></li>
        <c:forEach var="category" items="${categories}">
        <li class="visible-xs<c:if test="${category[1].equals(param.navActive)}"> active</c:if><c:if test="${fn:length(category[0]) == 0}"> disabled</c:if>"><a<c:if test="${fn:length(category[0]) > 0}"> href="/${category[0]}.html"</c:if>><i class="fa ${category[3]} fa-fw" aria-hidden="true"></i> ${category[1]}</a></li>
        </c:forEach>
        <!-- /end of primary copy -->

        <!-- secondary navigation items -->
        <li class="visible-xs section-header">More</li>
        <li <c:if test="${'News'.equals(param.navActive)}"> class="active"</c:if>><a href="/news.html"><i class="fa fa-newspaper-o fa-fw hidden-sm" aria-hidden="true"></i> News</a></li>
        <li <c:if test="${'Sources'.equals(param.navActive)}"> class="active"</c:if>><a href="/sources.html"><i class="fa fa-archive fa-fw hidden-sm" aria-hidden="true"></i> Sources</a></li>
        <li <c:if test="${'Operation'.equals(param.navActive)}"> class="active"</c:if>><a href="/operation.html"><i class="fa fa-cogs fa-fw hidden-sm" aria-hidden="true"></i> Operation</a></li>
        <li <c:if test="${'Development'.equals(param.navActive)}"> class="active"</c:if>><a href="/development.html"><i class="fa fa-code fa-fw hidden-sm" aria-hidden="true"></i> Development</a></li>
        <li <c:if test="${'Research'.equals(param.navActive)}"> class="active"</c:if>><a href="/research.html"><i class="fa fa-university fa-fw hidden-sm" aria-hidden="true"></i> Research</a></li>
        <li <c:if test="${'About'.equals(param.navActive)}"> class="active"</c:if>><a href="/about.html"><i class="fa fa-lightbulb-o fa-fw hidden-sm" aria-hidden="true"></i> About</a></li>
        <!-- /secondary navigation items -->

      </ul>
    </div><!-- /.navbar-collapse -->
  </div><!-- /.container-fluid -->
</nav>
<!-- /secondary navigation -->

<!-- page header for every single page -->
<div class="page-header hidden-xs">
  <a href="/"><img src="/images/tor-metrics-white@2x.png" width="232" height="50" alt="Tor Metrics" id="metrics-wordmark"></a>
  <div>
    <p>
      <i>&ldquo;Tor metrics are the ammunition that lets Tor and other security advocates argue for a more private and secure Internet from a position of data, rather than just dogma or perspective.&rdquo;<br><small>&mdash; Bruce Schneier (June 1, 2016)</small></i>
    </p>
  </div>
  <div class="clearfix"></div>
</div>
<!-- /page header -->

<!-- primary navigation -->
<nav class="navbar navbar-default hidden-xs">
  <div class="container-fluid">
    <div class="collapse navbar-collapse" id="bs-example-navbar-collapse-primary">
      <ul class="nav navbar-nav">
        <li <c:if test="${'Home'.equals(param.navActive)}"> class="active"</c:if>><a href="/"><i class="fa fa-home fa-fw hidden-sm" aria-hidden="true"></i> Home</a></li>
        <c:forEach var="category" items="${categories}">
        <li class="<c:if test="${category[1].equals(param.navActive)}"> active</c:if><c:if test="${fn:length(category[0]) == 0}"> disabled</c:if>"><a<c:if test="${fn:length(category[0]) > 0}"> href="/${category[0]}.html"</c:if>><i class="fa ${category[3]} fa-fw hidden-sm" aria-hidden="true"></i> ${category[1]}</a></li>
        </c:forEach>
      </ul>
    </div><!-- /.navbar-collapse -->
  </div><!-- /.container-fluid -->
</nav>
<!-- /primary navigation -->

