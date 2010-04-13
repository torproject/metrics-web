import java.io.*;
import java.text.*;
import java.util.Date;
import java.util.logging.*;
/**
 * Initialize logging configuration.
 *
 * Log levels used by ERNIE:
 *
 * - SEVERE: An event made it impossible to continue program execution.
 * - WARNING: A potential problem occurred that requires the operator to
 *   look after the otherwise unattended setup
 * - INFO: Messages on INFO level are meant to help the operator in making
 *   sure that operation works as expected.
 * - FINE: Debug messages that are used to identify problems and which are
 *   turned on by default.
 * - FINER: More detailed debug messages to investigate problems in more
 *   detail. Not turned on by default. Increase log file limit when using
 *   FINER.
 * - FINEST: Most detailed debug messages. Not used.
 */
public class LoggingConfiguration {
  public LoggingConfiguration() {

    /* Remove default console handler. */
    for (Handler h : Logger.getLogger("").getHandlers()) {
      Logger.getLogger("").removeHandler(h);
    }

    /* Disable logging of internal Sun classes. */
    Logger.getLogger("sun").setLevel(Level.OFF);

    /* Create console logger to write messages on WARNING or higher to the
     * console. */
    final SimpleDateFormat dateTimeFormat =
        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    Formatter cf = new Formatter() {
      public String format(LogRecord record) {
        return dateTimeFormat.format(new Date(record.getMillis())) + " "
            + record.getMessage() + "\n";
      }
    };
    Handler ch = new ConsoleHandler();
    ch.setFormatter(cf);
    ch.setLevel(Level.WARNING);
    Logger.getLogger("").addHandler(ch);

    /* Initialize own logger for this class. */
    Logger logger = Logger.getLogger(
        LoggingConfiguration.class.getName());

    /* Create file logger that writes all messages on FINE or higher to a
     * local file. */
    Formatter ff = new Formatter() {
      public String format(LogRecord record) {
        return dateTimeFormat.format(new Date(record.getMillis())) + " "
            + record.getLevel() + " " + record.getSourceClassName() + " "
            + record.getSourceMethodName() + " " + record.getMessage()
            + "\n";
      }
    };
    try {
      FileHandler fh = new FileHandler("log", 5000000, 5, true);
      fh.setFormatter(ff);
      fh.setLevel(Level.FINE);
      Logger.getLogger("").addHandler(fh);
    } catch (SecurityException e) {
      logger.log(Level.WARNING, "No permission to create log file. "
          + "Logging to file is disabled.", e);
    } catch (IOException e) {
      logger.log(Level.WARNING, "Could not write to log file. Logging to "
          + "file is disabled.", e);
    }

    /* Initialize website logger that writes all message on INFO or higher
     * to a website. */
    Formatter wf = new Formatter() {
      private StringBuilder infos = new StringBuilder();
      private StringBuilder warnings = new StringBuilder();
      public String format(LogRecord record) {
        String msg = "          <tr>\n"
            + "            <td>"
            + dateTimeFormat.format(new Date(record.getMillis()))
            + "</td>\n"
            + "            <td>"
            + record.getMessage().replaceAll("\n", "<br/>")
            + "</td>\n"
            + "          </tr>\n";
        if (record.getLevel().equals(Level.INFO)) {
          this.infos.append(msg);
        } else {
          this.warnings.append(msg);
        }
        return "";
      }
      public String getTail(Handler h) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 "
              + "Transitional//EN\">\n"
            + "<html>\n"
            + "  <head>\n"
            + "    <title>Tor Metrics Portal: Last execution "
              + "logs</title>\n"
            + "    <meta http-equiv=Content-Type content=\"text/html; "
              + "charset=iso-8859-1\">\n"
            + "    <link href=\"http://www.torproject.org/stylesheet-ltr."
              + "css\" type=text/css rel=stylesheet>\n"
            + "    <link href=\"http://www.torproject.org/favicon.ico\" "
              + "type=image/x-icon rel=\"shortcut icon\">\n"
            + "  </head>\n"
            + "  <body>\n"
            + "    <div class=\"center\">\n"
            + "      <table class=\"banner\" border=\"0\" "
              + "cellpadding=\"0\" cellspacing=\"0\" summary=\"\">\n"
            + "        <tr>\n"
            + "          <td class=\"banner-left\"><a href=\"https://www."
              + "torproject.org/\"><img src=\"http://www.torproject.org/i"
              + "mages/top-left.png\" alt=\"Click to go to home page\" "
              + "width=\"193\" height=\"79\"></a></td>\n"
            + "          <td class=\"banner-middle\">\n"
            + "            <a href=\"/\">Home</a>\n"
            + "            <a href=\"graphs.html\">Graphs</a>\n"
            + "            <a href=\"reports.html\">Reports</a>\n"
            + "            <a href=\"papers.html\">Papers</a>\n"
            + "            <a href=\"data.html\">Data</a>\n"
            + "            <a href=\"tools.html\">Tools</a>\n"
            + "          </td>\n"
            + "          <td class=\"banner-right\"></td>\n"
            + "        </tr>\n"
            + "      </table>\n"
            + "      <div class=\"main-column\">\n"
            + "        <h2>Tor Metrics Portal: Last execution logs</h2>\n"
            + "        <br/>\n"
            + "        <h3>Warnings</h3>\n"
            + "        <br/>\n");
        if (this.warnings.length() < 1) {
          sb.append("        <p>(No messages.)</p>\n");
        } else {
          sb.append("        <table border=\"0\" cellpadding=\"0\" "
              + "cellspacing=\"0\" summary=\"\">\n"
              + "          <colgroup>\n"
              + "            <col width=\"160\">\n"
              + "            <col width=\"640\">\n"
              + "          </colgroup>\n");
          sb.append(warnings.toString());
          sb.append("        </table>\n");
        }

        sb.append("        <br/>\n"
            + "        <h3>Infos</h3>\n"
            + "        <br/>\n");
        if (this.infos.length() < 1) {
          sb.append("<p>(No messages.)</p>\n");
        } else {
          sb.append("        <table border=\"0\" cellpadding=\"0\" "
              + "cellspacing=\"0\" summary=\"\">\n"
            + "          <colgroup>\n"
            + "            <col width=\"160\">\n"
            + "            <col width=\"640\">\n"
            + "          </colgroup>\n");
          sb.append(this.infos.toString());
          sb.append("        </table>\n"
              + "      </div>\n"
              + "    </div>\n"
              + "    <div class=\"bottom\" id=\"bottom\">\n"
              + "      <p>\"Tor\" and the \"Onion Logo\" are <a "
                + "href=\"https://www.torproject.org/trademark-faq.html."
                + "en\">"
              + "registered trademarks</a> of The Tor Project, Inc.</p>\n"
              + "    </div>\n"
              + "  </body>\n"
              + "</html>");
        }
        return sb.toString();
      }
    };
    try {
      FileHandler wh = new FileHandler("website/log.html");
      wh.setFormatter(wf);
      wh.setLevel(Level.INFO);
      Logger.getLogger("").addHandler(wh);
    } catch (SecurityException e) {
      logger.log(Level.WARNING, "No permission to create website log "
          + "file. Logging to website is disabled.", e);
    } catch (IOException e) {
      logger.log(Level.WARNING, "Could not write to website log file. "
          + "Logging to website is disabled.", e);
    }
  }
}
