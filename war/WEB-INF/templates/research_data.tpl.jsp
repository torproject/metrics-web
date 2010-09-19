<%@page import="java.io.*" %>
<%@page import="java.util.*" %>
<%
    /* Read /srv/metrics.torproject.org/ernie/remote-files-for-data-page,
     * if that file exists, and add the contained URLs to this page.
     * TODO make file location configurable. */
    SortedMap<String, String> allFiles = new TreeMap<String, String>();
    File remoteFiles = new File("/srv/metrics.torproject.org/ernie/"
        + "remote-files-for-data-page");
    if (remoteFiles.exists() && !remoteFiles.isDirectory()) {
      BufferedReader br = new BufferedReader(new FileReader(remoteFiles));
      String line = null;
      while ((line = br.readLine()) != null) {
        if (line.startsWith("#") || !line.contains("/")) {
          continue;
        }
        allFiles.put(line.substring(line.lastIndexOf("/") + 1), line);
      }
    }
    /* Add files in /srv/metrics.torproject.org/ernie/website/data/ to
     * list of provided files on this page. TODO make dir location
     * configurable. */
    File localFiles =
        new File("/srv/metrics.torproject.org/ernie/website/data/");
    if (localFiles.exists() && localFiles.isDirectory()) {
      for (File file : localFiles.listFiles()) {
        allFiles.put(file.getName(), "/data/" + file.getName());
      }
    }
%>
        <h2>Tor Metrics Portal: Data</h2>
        <br/>
        <p>One of the main goals of the Tor Metrics Project is to make all
        gathered data available to the public. This approach enables
        privacy researchers to perform their own analyses using real data
        on the Tor network, and it acts as a safeguard to not gather data
        that are too sensitive to publish. All signatures can be
        <a href="https://www.torproject.org/verifying-signatures">verified</a>
        using Karsten's PGP key (0xF7C11265). The following data are
        available (see the <a href="tools.html">Tools</a> section for
        details on processing the files):</p>
        <ul>
          <li><a href="#relaydesc">Relay descriptor archives</a></li>
          <li><a href="#bridgedesc">Bridge descriptor archives</a></li>
          <li><a href="#stats">Statistics produced by relays</a></li>
          <li><a href="#performance">Performance data</a></li>
          <li><a href="#exitlist">Exit lists</a></li>
        </ul>
        <br/>
        <a id="relaydesc"/>
        <h3>Relay descriptor archives</h3>
        <br/>
        <p>The relay descriptor archives contain all documents that the
        directory authorities make available about the network of relays.
        These documents include network statuses, server (relay)
        descriptors, and extra-info descriptors:</p>
        <table width="100%" border="0" cellpadding="5" cellspacing="0" summary="">
<%
    String firstYearMonth = null, lastYearMonth = null;
    for (Map.Entry<String, String> e : allFiles.entrySet()) {
      String filename = e.getKey();
      if (!filename.endsWith(".asc") &&
          (filename.startsWith("tor-20") ||
          filename.startsWith("statuses-20") ||
          filename.startsWith("server-descriptors-20") ||
          filename.startsWith("extra-infos-20") ||
          filename.startsWith("votes-20") ||
          filename.startsWith("consensuses-20"))) {
        String yearMonth = filename.substring(filename.indexOf("20"));
        yearMonth = yearMonth.substring(0, 7);
        if (firstYearMonth == null ||
            yearMonth.compareTo(firstYearMonth) < 0) {
          firstYearMonth = yearMonth;
        }
        if (lastYearMonth == null ||
            yearMonth.compareTo(lastYearMonth) > 0) {
          lastYearMonth = yearMonth;
        }
      }
    }
    String currentYearMonth = firstYearMonth;
    String[] monthNames = new String[] { "January", "February", "March",
        "April", "May", "June", "July", "August", "September", "October",
        "November", "December" };
    String[] prefixes = new String[] { "tor-", "statuses-",
        "server-descriptors-", "extra-infos-", "votes-", "consensuses-" };
    String[] descriptions = new String[] { "v1 directories",
        "v2 statuses", "server descriptors", "extra-infos", "v3 votes",
        "v3 consensuses" };
    Set<String> printedFiles = new HashSet<String>();
    while (currentYearMonth.compareTo(lastYearMonth) <= 0) {
      int currentYear = Integer.parseInt(currentYearMonth.substring(
          0, 4));
      int currentMonth = Integer.parseInt(currentYearMonth.substring(
          5, 7));
      out.write("          <tr>\n            <td>"
          + monthNames[currentMonth - 1] + " " + currentYear + "</td>\n");
      for (int i = 0; i < prefixes.length; i++) {
        String prefix = prefixes[i];
        String description = descriptions[i];
        String file = prefix + currentYearMonth + ".tar.bz2";
        String sig = file + ".asc";
        if (allFiles.containsKey(file)) {
          out.write("            <td><a href=\"" + allFiles.get(file)
              + "\">" + description + "</a>");
          printedFiles.add(file);
          if (allFiles.containsKey(sig)) {
            out.write("\n              (<a href=\"" + allFiles.get(sig)
                + "\">sig</a>)</td>\n");
            printedFiles.add(sig);
          } else {
            out.write("</td>\n");
          }
        } else {
          out.write("            <td/>\n");
        }
      }
      out.write("          </tr>\n");
      if (currentMonth < 12) {
        currentMonth++;
      } else {
        currentYear++;
        currentMonth = 1;
      }
      currentYearMonth = String.format("%d-%02d", currentYear,
          currentMonth);
    }
%>
        </table>
        <br/>
        <a id="bridgedesc"/>
        <h3>Bridge descriptor archives</h3>
        <br/>
        <p>The bridge descriptor archives contain similar documents as the
        relay descriptor archives, but for the non-public bridges. The
        descriptors have been sanitized before publication to remove all
        information that could otherwise be used to locate bridges.
        Beginning with May 2010, we stopped resolving IP addresses to
        country codes and including those in the sanitized descriptors,
        because it was tough to maintain; if your research requires this
        or any other detail, contact us and we'll sort something out. The
        files below contain all documents of a given month:</p>
        <table width="100%" border="0" cellpadding="5" cellspacing="0" summary="">
<%
    firstYearMonth = lastYearMonth = null;
    for (Map.Entry<String, String> e : allFiles.entrySet()) {
      String filename = e.getKey();
      if (!filename.endsWith(".asc") &&
          filename.startsWith("bridge-descriptors-20")) {
        String yearMonth = filename.substring(filename.indexOf("20"));
        yearMonth = yearMonth.substring(0, 7);
        if (firstYearMonth == null ||
            yearMonth.compareTo(firstYearMonth) < 0) {
          firstYearMonth = yearMonth;
        }
        if (lastYearMonth == null ||
            yearMonth.compareTo(lastYearMonth) > 0) {
          lastYearMonth = yearMonth;
        }
      }
    }
    currentYearMonth = firstYearMonth;
    while (currentYearMonth.compareTo(lastYearMonth) <= 0) {
      int currentYear = Integer.parseInt(currentYearMonth.substring(
          0, 4));
      int currentMonth = Integer.parseInt(currentYearMonth.substring(
          5, 7));
      String file = "bridge-descriptors-" + currentYearMonth + ".tar.bz2";
      String sig = file + ".asc";
      if (allFiles.containsKey(file)) {
        out.write("          <tr><td><a href=\"" + allFiles.get(file)
            + "\">" + monthNames[currentMonth - 1] + " " + currentYear
            + "</a>");
        printedFiles.add(file);
        if (allFiles.containsKey(sig)) {
          out.write("\n              (<a href=\"" + allFiles.get(sig)
                + "\">sig</a>)</td></tr>\n");
          printedFiles.add(sig);
        } else {
          out.write("</td></tr>\n");
        }
      }
      if (currentMonth < 12) {
        currentMonth++;
      } else {
        currentYear++;
        currentMonth = 1;
      }
      currentYearMonth = String.format("%d-%02d", currentYear,
          currentMonth);
    }
%>
        </table>
        <p/>
        <br/>
        <a id="stats"/>
        <h3>Statistics produced by relays</h3>
        <br/>
        <p>Some of the relays are configured to gather statistics on the
        number of requests or connecting clients, the number of processed
        cells per queue, or the number of exiting bytes per port. Relays
        running version 0.2.2.4-alpha can include these statistics in
        extra-info descriptors, so that they are included in the relay
        descriptor archives. The following files contain the statistics
        produced by relays running earlier versions:</p>
        <table width="100%" border="0" cellpadding="5" cellspacing="0" summary="">
<%
    SortedSet<String> statsSources = new TreeSet<String>();
    for (Map.Entry<String, String> e : allFiles.entrySet()) {
      String filename = e.getKey();
      if (!filename.endsWith(".asc") &&
          (filename.startsWith("buffer-") ||
          filename.startsWith("dirreq-") ||
          filename.startsWith("entry-") ||
          (filename.startsWith("exit-") &&
          !filename.startsWith("exit-list-")))) {
        statsSources.add(filename.substring(filename.indexOf("-") + 1));
      }
    }
    prefixes = new String[] { "buffer-", "dirreq-", "entry-", "exit-" };
    for (String source : statsSources) {
      String nickname = source.split("-")[0];
      String fingerprint = source.split("-")[1];
      fingerprint = fingerprint.substring(0, 8);
      out.write("          <tr>\n            <td>" + nickname + " ("
          + fingerprint + ")</td>\n");
      for (int i = 0; i < prefixes.length; i++) {
        String prefix = prefixes[i];
        String file = prefix + source;
        String sig = file + ".asc";
        if (allFiles.containsKey(file)) {
          out.write("            <td><a href=\"" + allFiles.get(file)
              + "\">" + prefix + "stats</a>");
          printedFiles.add(file);
          if (allFiles.containsKey(sig)) {
            out.write("\n              (<a href=\"" + allFiles.get(sig)
                + "\">sig</a>)</td>\n");
            printedFiles.add(sig);
          } else {
            out.write("</td>\n");
          }
        } else {
          out.write("            <td/>\n");
        }
      }
      out.write("          </tr>\n");
    }
%>
        </table>
        <br/>
        <a id="performance"/>
        <h3>Performance data</h3>
        <br/>
        <p>We are measuring the performance of the Tor network by
        periodically requesting files of different sizes and recording the
        time needed to do so. The files below contain the output of the
        torperf application and are updated every hour:</p>
        <table width="100%" border="0" cellpadding="5" cellspacing="0" summary="">
<%
    SortedSet<String> torperfSources = new TreeSet<String>();
    for (Map.Entry<String, String> e : allFiles.entrySet()) {
      String filename = e.getKey();
      if (filename.endsWith("b.data")) {
        torperfSources.add(filename.substring(0, filename.indexOf("-")));
      }
    }
    for (String source : torperfSources) {
      out.write("          <tr>\n            <td>" + source + "</td>\n");
      String file = source + "-50kb.data";
      if (allFiles.containsKey(file)) {
        out.write("            <td><a href=\"" + allFiles.get(file)
            + "\">50 KiB requests</a></td>\n");
        printedFiles.add(file);
      } else {
        out.write("            <td/>\n");
      }
      file = source + "-1mb.data";
      if (allFiles.containsKey(file)) {
        out.write("            <td><a href=\"" + allFiles.get(file)
            + "\">1 MiB requests</a></td>\n");
        printedFiles.add(file);
      } else {
        out.write("            <td/>\n");
      }
      file = source + "-5mb.data";
      if (allFiles.containsKey(file)) {
        out.write("            <td><a href=\"" + allFiles.get(file)
            + "\">5 MiB requests</a></td>\n");
        printedFiles.add(file);
      } else {
        out.write("            <td/>\n");
      }
      out.write("          </tr>\n");
    }
%>
        </table>
        <br/>
        <a id="exitlist"/>
        <h3>Exit lists</h3>
        <br/>
        <p>We are archiving the bulk exit lists used by
        <a href="https://check.torproject.org/">Tor Check</a> containing
        the IP addresses that exit relays exit from:</p>
        <table width="100%" border="0" cellpadding="5" cellspacing="0" summary="">
<%
    for (Map.Entry<String, String> e : allFiles.entrySet()) {
      String file = e.getKey();
      String url = e.getValue();
      if (file.startsWith("exit-list")) {
        String yearMonth = file.substring(file.indexOf("exit-list-")
            + "exit-list-".length());
        String year = yearMonth.substring(0, 4);
        String monthName = monthNames[Integer.parseInt(
            yearMonth.substring(5, 7)) - 1];
        out.write("          <tr><td><a href=\"" + url + "\">"
            + monthName + " " + year + "</a></td></tr>\n");
        printedFiles.add(file);
      }
    }
%>
        </table>
