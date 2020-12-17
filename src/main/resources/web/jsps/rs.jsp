<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<jsp:include page="top.jsp">
  <jsp:param name="pageTitle" value="Relay Search"/>
  <jsp:param name="navActive" value="Services"/>
</jsp:include>

<div class="container">
  <ul class="breadcrumb">
    <li><a href="/">Home</a></li>
    <li><a href="/services.html">Services</a></li>
    <li class="active">Relay Search</li>
  </ul>
  <form class="hidden-xs navbar-form pull-right" role="search" id="secondary-search">
    <div class="input-group add-on">
      <input class="form-control" placeholder="Search" name="secondary-search-query" id="secondary-search-query" type="text" autocorrect="off" autocapitalize="none">
      <div class="input-group-btn">
        <button class="btn btn-danger" id="secondary-search-clear" type="button" title="Clear Search Query"><i class="glyphicon glyphicon-remove-circle"></i></button>
        <button class="btn btn-primary" id="secondary-search-submit" type="submit" title="Perform Search"><i class="glyphicon glyphicon-search"></i></button>
        <button class="btn btn-secondary" id="secondary-search-aggregate" type="button" title="Perform Aggregated Search"><i class="fa fa-compress"></i></button>
      </div>
    </div>
  </form>
  <h1>Relay Search</h1>
  <div class="progress progress-info progress-striped active">
    <div class="progress-bar">Rendering results...</div>
  </div>
  <div id="content">
    <noscript>
      <div class="alert alert-warning">
      <p><strong>JavaScript required</strong><br>
      Please enable JavaScript to use this service. If you are using Tor Browser on Safest mode, you'll have to switch to Safer or Standard mode. Relay Search only uses JavaScript resources that are hosted by the Tor Metrics team.</p>
    </noscript>
  </div>
</div> <!-- /container -->

<script>
  var require = {
    urlArgs: "v29"
  };
</script>
<script data-main="/js/rs/main" src="/js/rs/libs/require/require.js"></script>

<jsp:include page="bottom.jsp"/>
