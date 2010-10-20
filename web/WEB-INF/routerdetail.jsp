
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
  <title>Tor Metrics Portal: Status</title>
  <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">
  <link href="/css/stylesheet-ltr.css" type="text/css" rel="stylesheet">
  <link href="/images/favicon.ico" type="image/x-icon" rel="shortcut icon">
</head>
<body>
  <div class="center">
    <%@ include file="banner.jsp"%>
    <div class="main-column">
      <h2>Tor Metrics Portal: Router Detail</h2>
      <table>
        <tr>
         <td>validafter</td><td>${validafter}</td>
        </tr>
        <tr>
         <td>nickname</td><td>${nickname}</td>
        </tr>
        <tr>
         <td>bandwidth</td><td>${bandwidth * 0.0009765625} kBps</td>
        </tr>
        <tr>
         <td>fingerprint</td><td>${fingerprint}</td>
        </tr>
        <tr>
         <td>published</td><td>${published}</td>
        </tr>
        <tr>
         <td>address</td><td>${address}</td>
        </tr>
        <tr>
         <td>uptime</td><td>${uptime}</td>
        </tr>
        <tr>
         <td>orport</td><td>${orport}</td>
        </tr>
        <tr>
         <td>dirport</td><td>${dirport}</td>
        </tr>
        <tr>
         <td>isauthority</td><td>${isauthority}</td>
        </tr>
        <tr>
         <td>isbadexit</td><td>${isbadexit}</td>
        </tr>
        <tr>
         <td>isbaddirectory</td><td>${isbaddirectory}</td>
        </tr>
        <tr>
         <td>isexit</td><td>${isexit}</td>
        </tr>
        <tr>
         <td>isfast</td><td>${isfast}</td>
        </tr>
        <tr>
         <td>isguard</td><td>${isguard}</td>
        </tr>
        <tr>
         <td>ishsdir</td><td>${ishsdir}</td>
        </tr>
        <tr>
         <td>isnamed</td><td>${isnamed}</td>
        </tr>
        <tr>
         <td>isstable</td><td>${isstable}</td>
        </tr>
        <tr>
         <td>isrunning</td><td>${isrunning}</td>
        </tr>
        <tr>
         <td>isunnamed</td><td>${isunnamed}</td>
        </tr>
        <tr>
         <td>isvalid</td><td>${isvalid}</td>
        </tr>
        <tr>
         <td>isv2dir</td><td>${isv2dir}</td>
        </tr>
        <tr>
         <td>isv3dir</td><td>${isv3dir}</td>
        </tr>
        <tr>
         <td>version</td><td>${version}</td>
        </tr>
        <tr>
         <td>ports</td><td>${ports}</td>
        </tr>
        <tr>
         <td>platform</td><td>${platform}</td>
        </tr>
        <tr>
         <td>onion-key</td><td>${onion_key}</td>
        </tr>
        <tr>
         <td>signing-key</td><td>${signing_key}</td>
        </tr>
      </table>
      <img src="routerdetail.png?fingerprint=${fingerprint}"
           width="576"
           height="360"
           alt="Router detail bandwidth graph for ${fingerprint}"/>
    </div>
  </div>
  <div class="bottom" id="bottom">
    <%@ include file="footer.jsp"%>
  </div>
</body>
</html>
