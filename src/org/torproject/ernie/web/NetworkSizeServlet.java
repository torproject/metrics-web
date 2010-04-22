package org.torproject.ernie.web;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;

/**
 * Web page showing a generated network size graph and controls to
 * customize this graph.
 *
 * This servlet accepts three parameters that are used to customize the
 * generated network size graph and prepare the controls:
 * - xaxis: date interval to display on the X axis
 * - yaxis: metric to display on the Y axis
 * - category: categorization of values as colored lines
 *
 * The controls consist of HTML forms each having two of these three
 * parameters fixed and allowing to change the third parameter. Users
 * start with a non-parameterized graph and can change one parameter at a
 * time.
 *
 * TODO Populate x axis options with actual values we can generate graphs
 *      for. Right now, it's just constants.
 * TODO Implement graphs for bandwidth (y axis) and categories other than
 *      "Relays and bridges".
 * TODO Make code parts easier to re-use in other servlets.
 * TODO Maybe switch to JSP.
 */
public class NetworkSizeServlet extends HttpServlet {

  public void doGet(HttpServletRequest request,
      HttpServletResponse response) throws IOException,
      ServletException {

    /* Read parameters. Only consider the first value for a given name. */
    String xaxisParameter = request.getParameter("xaxis");
    String yaxisParameter = request.getParameter("yaxis");
    String categoryParameter = request.getParameter("category");

    /* Check parameters. */
    Set<String> validXaxisParameters = new HashSet<String>(
        Arrays.asList(("3d,10d,30d,90d,180d,2010-04,2010-03,2010-02,"
        + "2010-01,2010,2009,2008,2010-q2,2010-q1,2009-q4,all").
        split(",")));
    Set<String> validYaxisParameters = new HashSet<String>(
        Arrays.asList("number,bandwidth".split(",")));
    Set<String> validCategoryParameters = new HashSet<String>(
        Arrays.asList("rnb,flags,versions,platforms,countries".
        split(",")));
    if (!validXaxisParameters.contains(xaxisParameter)) {
      xaxisParameter = null;
    }
    if (!validYaxisParameters.contains(yaxisParameter)) {
      yaxisParameter = null;
    }
    if (!validCategoryParameters.contains(categoryParameter)) {
      categoryParameter = null;
    }

    /* Prepare HTML snippets depending on which parameters are given. If
     * a parameter is set, keep this parameter as hidden input in forms.
     * Also prepare the parameters for the contained image file. */
    String xaxisLine = "", yaxisLine = "", categoryLine = "",
        pngParameters = "";
    StringBuilder pngParametersBuilder = new StringBuilder();
    if (xaxisParameter != null) {
      xaxisLine = "                <input type=\"hidden\" name=\"xaxis\" "
          + "value=\"" + xaxisParameter + "\" />\n";
      pngParametersBuilder.append("&xaxis=" + xaxisParameter);
    }
    if (yaxisParameter != null) {
      yaxisLine = "                <input type=\"hidden\" name=\"yaxis\" "
          + "value=\"" + yaxisParameter + "\" />\n";
      pngParametersBuilder.append("&yaxis=" + yaxisParameter);
    }
    if (categoryParameter != null) {
      categoryLine = "                <input type=\"hidden\" "
          + "name=\"category\" value=\"" + categoryParameter + "\" />\n";
      pngParametersBuilder.append("&category=" + categoryParameter);
    }
    if (pngParametersBuilder.length() > 1) {
      pngParameters = "?" + pngParametersBuilder.toString().substring(1);
    }

    /* Print out a notice or warning that this is just a prototype with
     * limited functionality. */
    String prototypeNotice = "Please note that this page is an early "
        + "prototype with limited functionality. Right now, "
        + "customization is restricted to changing the \"Past days\" "
        + "value for the X axis. Other changes have no effect. Further, "
        + "the displayed data is not updated automatically.";
    if ((xaxisParameter == null || xaxisParameter.endsWith("d")) &&
        yaxisParameter == null && categoryParameter == null) {
      prototypeNotice = "        <p><font color=\"gray\">"
          + prototypeNotice + "</font></p>\n";
    } else {
      prototypeNotice = "        <p><font color=\"red\">"
          + prototypeNotice + "</font></p>\n";
    }

    /* Write result. */
    PrintWriter out = response.getWriter();
    out.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 "
          + "Transitional//EN\"\n"
        + "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n"
        + "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n"
        + "  <head>\n"
        + "    <meta content=\"text/html; charset=ISO-8859-1\"\n"
        + "          http-equiv=\"content-type\" />\n"
        + "    <title>Tor Metrics Portal: Graphs</title>\n"
        + "    <meta http-equiv=Content-Type content=\"text/html; "
          + "charset=iso-8859-1\">\n"
        + "    <link href=\"http://www.torproject.org/"
          + "stylesheet-ltr.css\" type=text/css rel=stylesheet>\n"
        + "    <link href=\"http://www.torproject.org/favicon.ico\" "
          + "type=image/x-icon rel=\"shortcut icon\">\n"
        + "  </head>\n"
        + "  <body>\n"
        + "    <div class=\"center\">\n"
        + "      <table class=\"banner\" border=\"0\" cellpadding=\"0\" "
          + "cellspacing=\"0\" summary=\"\">\n"
        + "        <tr>\n"
        + "          <td class=\"banner-left\"><a "
          + "href=\"https://www.torproject.org/\"><img "
          + "src=\"http://www.torproject.org/images/top-left.png\" "
          + "alt=\"Click to go to home page\" width=\"193\" "
          + "height=\"79\"></a></td>\n"
        + "          <td class=\"banner-middle\"></td>\n"
        + "          <td class=\"banner-right\"></td>\n"
        + "        </tr>\n"
        + "      </table>\n"
        + "      <div class=\"main-column\" style=\"margin:5; "
          + "Padding:0;\">\n"
        + "        <br/>\n"
        + prototypeNotice
        + "        <br/>\n"
        + "        <table>\n"
        + "          <tr>\n"
        + "            <td style=\"vertical-align:middle; "
          + "text-align:right\">\n"
        + "              <form action=\"networksize.html\">\n"
        + xaxisLine
        + "                <input type=\"hidden\" name=\"yaxis\" "
          + "value=\"number\" />\n"
        + categoryLine
        + "                <input type=\"submit\" value=\"Number of "
          + "nodes\" />\n"
        + "              </form>\n"
        + "              <form action=\"networksize.html\">\n"
        + xaxisLine
        + "                <input type=\"hidden\" name=\"yaxis\" "
          + "value=\"bandwidth\" />\n"
        + categoryLine
        + "                <input type=\"submit\" value=\"Advertised "
          + "bandwidth\" />\n"
        + "              </form>\n"
        + "            </td>\n"
        + "            <td>\n"
        + "              <img src=\"graphs/networksize.png"
          + pngParameters + "\" alt=\"Network size graph\"\n"
        + "                   width=\"576\" height=\"360\" />\n"
        + "            </td>\n"
        + "            <td style=\"vertical-align: middle; width: "
          + "25px;\">\n"
        + "              <form action=\"networksize.html\">\n"
        + xaxisLine
        + yaxisLine
        + "                <input type=\"hidden\" name=\"category\" "
          + "value=\"rnb\" />\n"
        + "                <input type=\"submit\" value=\"Relays and "
          + "bridges\" />\n"
        + "              </form>\n"
        + "              <form action=\"networksize.html\">\n"
        + xaxisLine
        + yaxisLine
        + "                <input type=\"hidden\" name=\"category\" "
          + "value=\"flags\" />\n"
        + "                <input type=\"submit\" value=\"Relays by "
          + "flag\" />\n"
        + "              </form>\n"
        + "              <form action=\"networksize.html\">\n"
        + xaxisLine
        + yaxisLine
        + "                <input type=\"hidden\" name=\"category\" "
          + "value=\"versions\" />\n"
        + "                <input type=\"submit\" value=\"Relays by "
          + "version\" />\n"
        + "              </form>\n"
        + "              <form action=\"networksize.html\">\n"
        + xaxisLine
        + yaxisLine
        + "                <input type=\"hidden\" name=\"category\" "
          + "value=\"platforms\" />\n"
        + "                <input type=\"submit\" value=\"Relays by "
          + "platform\" />\n"
        + "              </form>\n"
        + "              <form action=\"networksize.html\">\n"
        + xaxisLine
        + yaxisLine
        + "                <input type=\"hidden\" name=\"category\" "
          + "value=\"countries\" />\n"
        + "                <input type=\"submit\" value=\"Relays by "
          + "country\" />\n"
        + "              </form>\n"
        + "            </td>\n"
        + "          </tr>\n"
        + "          <tr>\n"
        + "            <td/>\n"
        + "            <td align=\"center\">\n"
        + "              <form action=\"networksize.html\">\n"
        + "                <select name=\"xaxis\">\n"
        + "                  <option value=\"3d\""
          + ("3d".equals(xaxisParameter) ? " selected" : "")
          + ">Past 3 days</option>\n"
        + "                  <option value=\"10d\""
          + ("10d".equals(xaxisParameter) ? " selected" : "")
          + ">Past 10 days</option>\n"
        + "                  <option value=\"30d\""
          + (xaxisParameter == null || "30d".equals(xaxisParameter) ||
             !xaxisParameter.endsWith("d") ? " selected" : "")
          + ">Past 30 days</option>\n"
        + "                  <option value=\"90d\""
          + ("90d".equals(xaxisParameter) ? " selected" : "")
          + ">Past 90 days</option>\n"
        + "                  <option value=\"180d\""
          + ("180d".equals(xaxisParameter) ? " selected" : "")
          + ">Past 180 days</option>\n"
        + "                </select>\n"
        + yaxisLine
        + categoryLine
        + "                <input type=\"submit\" value=\"Update\" />\n"
        + "              </form>\n"
        + "              <form action=\"networksize.html\">\n"
        + "                <select name=\"xaxis\">\n"
        + "                  <option value=\"2010-04\">April "
          + "2010</option>\n"
        + "                  <option value=\"2010-03\">March "
          + "2010</option>\n"
        + "                  <option value=\"2010-02\">February "
          + "2010</option>\n"
        + "                  <option value=\"2010-01\">January "
          + "2010</option>\n"
        + "                </select>\n"
        + yaxisLine
        + categoryLine
        + "                <input type=\"submit\" value=\"Update\" />\n"
        + "              </form>\n"
        + "              <form action=\"networksize.html\">\n"
        + "                <select name=\"xaxis\">\n"
        + "                  <option value=\"2010\">2010</option>\n"
        + "                  <option value=\"2009\">2009</option>\n"
        + "                  <option value=\"2008\">2008</option>\n"
        + "                </select>\n"
        + yaxisLine
        + categoryLine
        + "                <input type=\"submit\" value=\"Update\" />\n"
        + "              </form>\n"
        + "              <form action=\"networksize.html\">\n"
        + "                <select name=\"xaxis\">\n"
        + "                  <option value=\"2010-q1\">Q2 2010</option>\n"
        + "                  <option value=\"2010-q2\">Q1 2010</option>\n"
        + "                  <option value=\"2009-q4\">Q4 2009</option>\n"
        + "                </select>\n"
        + yaxisLine
        + categoryLine
        + "                <input type=\"submit\" value=\"Update\" />\n"
        + "              </form>\n"
        + "              <form action=\"networksize.html\">\n"
        + "                <input type=\"hidden\" name=\"xaxis\" "
          + "value=\"all\" />\n"
        + yaxisLine
        + categoryLine
        + "                <input type=\"submit\" value=\"Show all "
          + "data\" />\n"
        + "              </form>\n"
        + "            </td>\n"
        + "            <td/>\n"
        + "          </tr>\n"
        + "        </table>\n"
        + "        <br/>\n"
        + "      </div>\n"
        + "    </div>\n"
        + "    <div class=\"bottom\" id=\"bottom\">\n"
        + "      <p>\"Tor\" and the \"Onion Logo\" are <a "
          + "href=\"https://www.torproject.org/trademark-faq.html.en\">"
          + "registered trademarks</a> of The Tor Project, Inc.</p>\n"
        + "    </div>\n"
        + "  </body>\n"
        + "</html>");
    out.close();
  }
}

