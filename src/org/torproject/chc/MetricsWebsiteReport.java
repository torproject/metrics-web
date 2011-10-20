/* Copyright 2011 The Tor Project
 * See LICENSE for licensing information */
package org.torproject.chc;

import java.io.*;
import java.text.*;
import java.util.*;

/* Transform the most recent consensus and corresponding votes into an
 * HTML page showing possible irregularities. */
public class MetricsWebsiteReport implements Report {

  /* Date-time format to format timestamps. */
  private static SimpleDateFormat dateTimeFormat;
  static {
    dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    dateTimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
  }

  /* Output file to write report to. */
  private File htmlOutputFile;

  /* Initialize this report. */
  public MetricsWebsiteReport(String htmlOutputFilename) {
    this.htmlOutputFile = new File(htmlOutputFilename);
  }

  /* Store the downloaded consensus and corresponding votes for later
   * processing. */
  private Status downloadedConsensus;
  private SortedSet<Status> downloadedVotes;
  public void processDownloadedConsensuses(
      SortedMap<String, Status> downloadedConsensuses) {
    long mostRecentValidAfterMillis = -1L;
    for (Status downloadedConsensus : downloadedConsensuses.values()) {
      if (downloadedConsensus.getValidAfterMillis() >
          mostRecentValidAfterMillis) {
        this.downloadedConsensus = downloadedConsensus;
        mostRecentValidAfterMillis =
            downloadedConsensus.getValidAfterMillis();
      }
    }
    if (this.downloadedConsensus != null) {
      this.downloadedVotes = this.downloadedConsensus.getVotes();
    }
  }

  /* Writer to write all HTML output to. */
  private BufferedWriter bw;

  /* Write HTML output file for the metrics website. */
  public void writeReport() {

    if (this.downloadedConsensus != null) {
      try {
        this.htmlOutputFile.getParentFile().mkdirs();
        this.bw = new BufferedWriter(new FileWriter(this.htmlOutputFile));
        writePageHeader();
        writeValidAfterTime();
        writeKnownFlags();
        writeNumberOfRelaysVotedAbout();
        writeConsensusMethods();
        writeRecommendedVersions();
        writeConsensusParameters();
        writeAuthorityKeys();
        writeBandwidthScannerStatus();
        writeAuthorityVersions();
        writeRelayFlagsTable();
        writeRelayFlagsSummary();
        writePageFooter();
        this.bw.close();
      } catch (IOException e) {
        System.err.println("Could not write HTML output file '"
            + this.htmlOutputFile.getAbsolutePath() + "'.  Ignoring.");
      }
    }
  }

  /* Write the HTML page header including the metrics website
   * navigation. */
  private void writePageHeader() throws IOException {
    this.bw.write("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 "
          + "Transitional//EN\">\n"
        + "<html>\n"
        + "  <head>\n"
        + "    <title>Tor Metrics Portal: Consensus health</title>\n"
        + "    <meta http-equiv=\"content-type\" content=\"text/html; "
          + "charset=ISO-8859-1\">\n"
        + "    <link href=\"/css/stylesheet-ltr.css\" type=\"text/css\" "
          + "rel=\"stylesheet\">\n"
        + "    <link href=\"/images/favicon.ico\" "
          + "type=\"image/x-icon\" rel=\"shortcut icon\">\n"
        + "  </head>\n"
        + "  <body>\n"
        + "    <div class=\"center\">\n"
        + "      <table class=\"banner\" border=\"0\" cellpadding=\"0\" "
          + "cellspacing=\"0\" summary=\"\">\n"
        + "        <tr>\n"
        + "          <td class=\"banner-left\"><a "
          + "href=\"/index.html\"><img src=\"/images/top-left.png\" "
          + "alt=\"Click to go to home page\" width=\"193\" "
          + "height=\"79\"></a></td>\n"
        + "          <td class=\"banner-middle\">\n"
        + "            <a href=\"/\">Home</a>\n"
        + "            <a href=\"graphs.html\">Graphs</a>\n"
        + "            <a href=\"research.html\">Research</a>\n"
        + "            <a href=\"status.html\">Status</a>\n"
        + "            <br>\n"
        + "            <font size=\"2\">\n"
        + "              <a href=\"networkstatus.html\">Network "
          + "Status</a>\n"
        + "              <a href=\"exonerator.html\">ExoneraTor</a>\n"
        + "              <a href=\"relay-search.html\">Relay Search</a>\n"
        + "              <a class=\"current\">Consensus Health</a>\n"
        + "            </font>\n"
        + "          </td>\n"
        + "          <td class=\"banner-right\"></td>\n"
        + "        </tr>\n"
        + "      </table>\n"
        + "      <div class=\"main-column\">\n"
        + "        <h2>Tor Metrics Portal: Consensus Health</h2>\n"
        + "        <br>\n"
        + "        <p>This page shows statistics about the current "
          + "consensus and votes to facilitate debugging of the "
          + "directory consensus process.</p>\n");
  }

  /* Write the valid-after time of the downloaded consensus. */
  private void writeValidAfterTime() throws IOException {
    this.bw.write("        <br>\n"
        + "        <h3>Valid-after time</h3>\n"
        + "        <br>\n"
        + "        <p>Consensus was published ");
    if (this.downloadedConsensus.getValidAfterMillis() <
        System.currentTimeMillis() - 3L * 60L * 60L * 1000L) {
      this.bw.write("<font color=\"red\">"
          + dateTimeFormat.format(
          this.downloadedConsensus.getValidAfterMillis()) + "</font>");
    } else {
      this.bw.write(dateTimeFormat.format(
          this.downloadedConsensus.getValidAfterMillis()));
    }
    this.bw.write(". <i>Note that it takes up to 15 to learn about new "
        + "consensus and votes and process them.</i></p>\n");
  }

  /* Write the lists of known flags. */
  private void writeKnownFlags() throws IOException {
    this.bw.write("        <br>\n"
        + "        <h3>Known flags</h3>\n"
        + "        <br>\n"
        + "        <table border=\"0\" cellpadding=\"4\" "
        + "cellspacing=\"0\" summary=\"\">\n"
        + "          <colgroup>\n"
        + "            <col width=\"160\">\n"
        + "            <col width=\"640\">\n"
        + "          </colgroup>\n");
    if (this.downloadedVotes.size() < 1) {
      this.bw.write("          <tr><td>(No votes.)</td><td></td></tr>\n");
    } else {
      for (Status vote : this.downloadedVotes) {
        this.bw.write("          <tr>\n"
            + "            <td>" + vote.getNickname() + "</td>\n"
            + "            <td>known-flags");
        for (String knownFlag : vote.getKnownFlags()) {
          this.bw.write(" " + knownFlag);
        }
        this.bw.write("</td>\n"
            + "          </tr>\n");
      }
    }
    this.bw.write("          <tr>\n"
        + "            <td><font color=\"blue\">consensus</font>"
          + "</td>\n"
        + "            <td><font color=\"blue\">known-flags");
    for (String knownFlag : this.downloadedConsensus.getKnownFlags()) {
      this.bw.write(" " + knownFlag);
    }
    this.bw.write("</font></td>\n"
        + "          </tr>\n"
        + "        </table>\n");
  }

  /* Write the number of relays voted about. */
  private void writeNumberOfRelaysVotedAbout() throws IOException {
    this.bw.write("        <br>\n"
        + "        <h3>Number of relays voted about</h3>\n"
        + "        <br>\n"
        + "        <table border=\"0\" cellpadding=\"4\" "
        + "cellspacing=\"0\" summary=\"\">\n"
        + "          <colgroup>\n"
        + "            <col width=\"160\">\n"
        + "            <col width=\"320\">\n"
        + "            <col width=\"320\">\n"
        + "          </colgroup>\n");
    if (this.downloadedVotes.size() < 1) {
      this.bw.write("          <tr><td>(No votes.)</td><td></td><td></td>"
            + "</tr>\n");
    } else {
      for (Status vote : this.downloadedVotes) {
        this.bw.write("          <tr>\n"
            + "            <td>" + vote.getNickname() + "</td>\n"
            + "            <td>" + vote.getStatusEntries().size()
              + " total</td>\n"
            + "            <td>" + vote.getRunningRelays()
              + " Running</td>\n"
            + "          </tr>\n");
      }
    }
    this.bw.write("          <tr>\n"
        + "            <td><font color=\"blue\">consensus</font>"
          + "</td>\n"
        + "            <td><font color=\"blue\">"
          + this.downloadedConsensus.getStatusEntries().size()
          + " total</font></td>\n"
        + "            <td><font color=\"blue\">"
          + this.downloadedConsensus.getRunningRelays()
          + " Running</font></td>\n"
        + "          </tr>\n"
        + "        </table>\n");
  }

  /* Write the supported consensus methods of directory authorities and
   * the resulting consensus method. */
  private void writeConsensusMethods() throws IOException {
    this.bw.write("        <br>\n"
        + "        <h3>Consensus methods</h3>\n"
        + "        <br>\n"
        + "        <table border=\"0\" cellpadding=\"4\" "
        + "cellspacing=\"0\" summary=\"\">\n"
        + "          <colgroup>\n"
        + "            <col width=\"160\">\n"
        + "            <col width=\"640\">\n"
        + "          </colgroup>\n");
    if (this.downloadedVotes.size() < 1) {
      this.bw.write("          <tr><td>(No votes.)</td><td></td></tr>\n");
    } else {
      for (Status vote : this.downloadedVotes) {
        SortedSet<Integer> consensusMethods =
            vote.getConsensusMethods();
        if (consensusMethods.contains(
            this.downloadedConsensus.getConsensusMethods().last())) {
          this.bw.write("          <tr>\n"
               + "            <td>" + vote.getNickname() + "</td>\n"
               + "            <td>consensus-methods");
          for (int consensusMethod : consensusMethods) {
            this.bw.write(" " + String.valueOf(consensusMethod));
          }
          this.bw.write("</td>\n"
               + "          </tr>\n");
        } else {
          this.bw.write("          <tr>\n"
              + "            <td><font color=\"red\">"
                + vote.getNickname() + "</font></td>\n"
              + "            <td><font color=\"red\">"
                + "consensus-methods");
          for (int consensusMethod : consensusMethods) {
            this.bw.write(" " + String.valueOf(consensusMethod));
          }
          this.bw.write("</font></td>\n"
            + "          </tr>\n");
        }
      }
    }
    this.bw.write("          <tr>\n"
        + "            <td><font color=\"blue\">consensus</font>"
          + "</td>\n"
        + "            <td><font color=\"blue\">consensus-method "
          + this.downloadedConsensus.getConsensusMethods().last()
          + "</font></td>\n"
        + "          </tr>\n"
        + "        </table>\n");
  }

  /* Write recommended versions. */
  private void writeRecommendedVersions() throws IOException {
    this.bw.write("        <br>\n"
        + "        <h3>Recommended versions</h3>\n"
        + "        <br>\n"
        + "        <table border=\"0\" cellpadding=\"4\" "
        + "cellspacing=\"0\" summary=\"\">\n"
        + "          <colgroup>\n"
        + "            <col width=\"160\">\n"
        + "            <col width=\"640\">\n"
        + "          </colgroup>\n");
    if (this.downloadedVotes.size() < 1) {
      this.bw.write("          <tr><td>(No votes.)</td><td></td></tr>\n");
    } else {
      for (Status vote : this.downloadedVotes) {
        SortedSet<String> voteRecommendedClientVersions =
            vote.getRecommendedClientVersions();
        if (voteRecommendedClientVersions != null) {
          if (downloadedConsensus.getRecommendedClientVersions().equals(
              voteRecommendedClientVersions)) {
            this.bw.write("          <tr>\n"
                + "            <td>" + vote.getNickname() + "</td>\n"
                + "            <td>client-versions ");
            int i = 0;
            for (String version : voteRecommendedClientVersions) {
              this.bw.write((i++ > 0 ? "," : "") + version);
            }
            this.bw.write("</td>\n"
                + "          </tr>\n");
          } else {
            this.bw.write("          <tr>\n"
                + "            <td><font color=\"red\">"
                  + vote.getNickname()
                  + "</font></td>\n"
                + "            <td><font color=\"red\">client-versions ");
            int i = 0;
            for (String version : voteRecommendedClientVersions) {
              this.bw.write((i++ > 0 ? "," : "") + version);
            }
            this.bw.write("</font></td>\n"
                + "          </tr>\n");
          }
        }
        SortedSet<String> voteRecommendedServerVersions =
            vote.getRecommendedServerVersions();
        if (voteRecommendedServerVersions != null) {
          if (downloadedConsensus.getRecommendedServerVersions().equals(
              voteRecommendedServerVersions)) {
            this.bw.write("          <tr>\n"
                + "            <td></td>\n"
                + "            <td>server-versions ");
            int i = 0;
            for (String version : voteRecommendedServerVersions) {
              this.bw.write((i++ > 0 ? "," : "") + version);
            }
            this.bw.write("</td>\n"
                + "          </tr>\n");
          } else {
            this.bw.write("          <tr>\n"
                + "            <td></td>\n"
                + "            <td><font color=\"red\">server-versions ");
            int i = 0;
            for (String version : voteRecommendedServerVersions) {
              this.bw.write((i++ > 0 ? "," : "") + version);
            }
            this.bw.write("</font></td>\n"
                + "          </tr>\n");
          }
        }
      }
    }
    this.bw.write("          <tr>\n"
        + "            <td><font color=\"blue\">consensus</font>"
        + "</td>\n"
        + "            <td><font color=\"blue\">client-versions ");
    int i = 0;
    for (String version :
        downloadedConsensus.getRecommendedClientVersions()) {
      this.bw.write((i++ > 0 ? "," : "") + version);
    }
    this.bw.write("</font></td>\n"
        + "          </tr>\n"
        + "          <tr>\n"
        + "            <td></td>\n"
        + "            <td><font color=\"blue\">server-versions ");
    i = 0;
    for (String version :
        downloadedConsensus.getRecommendedServerVersions()) {
      this.bw.write((i++ > 0 ? "," : "") + version);
    }
    this.bw.write("</font></td>\n"
      + "          </tr>\n"
      + "        </table>\n");
  }

  /* Write consensus parameters. */
  private void writeConsensusParameters() throws IOException {
    this.bw.write("        <br>\n"
        + "        <h3>Consensus parameters</h3>\n"
        + "        <br>\n"
        + "        <table border=\"0\" cellpadding=\"4\" "
        + "cellspacing=\"0\" summary=\"\">\n"
        + "          <colgroup>\n"
        + "            <col width=\"160\">\n"
        + "            <col width=\"640\">\n"
        + "          </colgroup>\n");
    if (this.downloadedVotes.size() < 1) {
      this.bw.write("          <tr><td>(No votes.)</td><td></td></tr>\n");
    } else {
      Set<String> validParameters = new HashSet<String>(Arrays.asList(
          ("circwindow,CircuitPriorityHalflifeMsec,refuseunknownexits,"
          + "cbtdisabled,cbtnummodes,cbtrecentcount,cbtmaxtimeouts,"
          + "cbtmincircs,cbtquantile,cbtclosequantile,cbttestfreq,"
          + "cbtmintimeout,cbtinitialtimeout").split(",")));
      Map<String, String> consensusConsensusParams =
          downloadedConsensus.getConsensusParams();
      for (Status vote : this.downloadedVotes) {
        Map<String, String> voteConsensusParams =
            vote.getConsensusParams();
        boolean conflictOrInvalid = false;
        if (voteConsensusParams == null) {
          for (Map.Entry<String, String> e :
              voteConsensusParams.entrySet()) {
            if (!consensusConsensusParams.containsKey(e.getKey()) ||
                !consensusConsensusParams.get(e.getKey()).equals(
                e.getValue()) ||
                !validParameters.contains(e.getKey())) {
              conflictOrInvalid = true;
              break;
            }
          }
        }
        if (conflictOrInvalid) {
          this.bw.write("          <tr>\n"
              + "            <td><font color=\"red\">"
                + vote.getNickname() + "</font></td>\n"
              + "            <td><font color=\"red\">params");
          for (Map.Entry<String, String> e :
              voteConsensusParams.entrySet()) {
            this.bw.write(" " + e.getKey() + "=" + e.getValue());
          }
          this.bw.write("</font></td>\n"
              + "          </tr>\n");
        } else {
          this.bw.write("          <tr>\n"
              + "            <td>" + vote.getNickname() + "</td>\n"
              + "            <td>params");
          for (Map.Entry<String, String> e :
              voteConsensusParams.entrySet()) {
            this.bw.write(" " + e.getKey() + "=" + e.getValue());
          }
          this.bw.write("</td>\n"
              + "          </tr>\n");
        }
      }
    }
    this.bw.write("          <tr>\n"
        + "            <td><font color=\"blue\">consensus</font>"
          + "</td>\n"
        + "            <td><font color=\"blue\">params");
    for (Map.Entry<String, String> e :
        this.downloadedConsensus.getConsensusParams().entrySet()) {
      this.bw.write(" " + e.getKey() + "=" + e.getValue());
    }
    this.bw.write("</font></td>\n"
        + "          </tr>\n"
        + "        </table>\n");
  }

  /* Write authority keys and their expiration dates. */
  private void writeAuthorityKeys() throws IOException {
    this.bw.write("        <br>\n"
        + "        <h3>Authority keys</h3>\n"
        + "        <br>\n"
        + "        <table border=\"0\" cellpadding=\"4\" "
        + "cellspacing=\"0\" summary=\"\">\n"
        + "          <colgroup>\n"
        + "            <col width=\"160\">\n"
        + "            <col width=\"640\">\n"
        + "          </colgroup>\n");
    if (this.downloadedVotes.size() < 1) {
      this.bw.write("          <tr><td>(No votes.)</td><td></td></tr>\n");
    } else {
      for (Status vote : this.downloadedVotes) {
        long voteDirKeyExpiresMillis = vote.getDirKeyExpiresMillis();
        if (voteDirKeyExpiresMillis - 14L * 24L * 60L * 60L * 1000L <
            System.currentTimeMillis()) {
          this.bw.write("          <tr>\n"
              + "            <td><font color=\"red\">"
                + vote.getNickname() + "</font></td>\n"
              + "            <td><font color=\"red\">dir-key-expires "
                + dateTimeFormat.format(voteDirKeyExpiresMillis)
                + "</font></td>\n"
              + "          </tr>\n");
        } else {
          this.bw.write("          <tr>\n"
              + "            <td>" + vote.getNickname() + "</td>\n"
              + "            <td>dir-key-expires "
                + dateTimeFormat.format(voteDirKeyExpiresMillis)
                + "</td>\n"
              + "          </tr>\n");
        }
      }
    }
    this.bw.write("        </table>\n"
        + "        <br>\n"
        + "        <p><i>Note that expiration dates of legacy keys are "
          + "not included in votes and therefore not listed here!</i>"
          + "</p>\n");
  }

  /* Write the status of bandwidth scanners and results being contained
   * in votes. */
  private void writeBandwidthScannerStatus() throws IOException {
    this.bw.write("        <br>\n"
         + "        <h3>Bandwidth scanner status</h3>\n"
        + "        <br>\n"
        + "        <table border=\"0\" cellpadding=\"4\" "
        + "cellspacing=\"0\" summary=\"\">\n"
        + "          <colgroup>\n"
        + "            <col width=\"160\">\n"
        + "            <col width=\"640\">\n"
        + "          </colgroup>\n");
    if (this.downloadedVotes.size() < 1) {
      this.bw.write("          <tr><td>(No votes.)</td><td></td></tr>\n");
    } else {
      for (Status vote : this.downloadedVotes) {
        if (vote.getBandwidthWeights() > 0) {
          this.bw.write("          <tr>\n"
              + "            <td>" + vote.getNickname() + "</td>\n"
              + "            <td>" + vote.getBandwidthWeights()
                + " Measured values in w lines</td>\n"
              + "          </tr>\n");
        }
      }
    }
    this.bw.write("        </table>\n");
  }

  /* Write directory authority versions. */
  private void writeAuthorityVersions() throws IOException {
    this.bw.write("        <br>\n"
         + "        <h3>Authority versions</h3>\n"
        + "        <br>\n");
    Map<String, String> authorityVersions =
        this.downloadedConsensus.getAuthorityVersions();
    if (authorityVersions.size() < 1) {
      this.bw.write("          <p>(No relays with Authority flag found.)"
            + "</p>\n");
    } else {
      this.bw.write("        <table border=\"0\" cellpadding=\"4\" "
            + "cellspacing=\"0\" summary=\"\">\n"
          + "          <colgroup>\n"
          + "            <col width=\"160\">\n"
          + "            <col width=\"640\">\n"
          + "          </colgroup>\n");
      for (Map.Entry<String, String> e : authorityVersions.entrySet()) {
        String nickname = e.getKey();
        String versionString = e.getValue();
        this.bw.write("          <tr>\n"
            + "            <td>" + nickname + "</td>\n"
            + "            <td>" + versionString + "</td>\n"
            + "          </tr>\n");
      }
      this.bw.write("        </table>\n"
          + "        <br>\n"
          + "        <p><i>Note that this list of relays with the "
            + "Authority flag may be different from the list of v3 "
            + "directory authorities!</i></p>\n");
    }
  }

  /* Write the (huge) table containing relay flags contained in votes and
   * the consensus for each relay. */
  private void writeRelayFlagsTable() throws IOException {
    this.bw.write("        <br>\n"
        + "        <h3>Relay flags</h3>\n"
        + "        <br>\n"
        + "        <p>The semantics of flags written in the table is "
          + "as follows:</p>\n"
        + "        <ul>\n"
        + "          <li><b>In vote and consensus:</b> Flag in vote "
          + "matches flag in consensus, or relay is not listed in "
          + "consensus (because it doesn't have the Running "
          + "flag)</li>\n"
        + "          <li><b><font color=\"red\">Only in "
          + "vote:</font></b> Flag in vote, but missing in the "
          + "consensus, because there was no majority for the flag or "
          + "the flag was invalidated (e.g., Named gets invalidated by "
          + "Unnamed)</li>\n"
        + "          <li><b><font color=\"gray\"><s>Only in "
          + "consensus:</s></font></b> Flag in consensus, but missing "
          + "in a vote of a directory authority voting on this "
          + "flag</li>\n"
        + "          <li><b><font color=\"blue\">In "
          + "consensus:</font></b> Flag in consensus</li>\n"
        + "        </ul>\n"
        + "        <br>\n"
        + "        <p>See also the summary below the table.</p>\n"
        + "        <table border=\"0\" cellpadding=\"4\" "
        + "cellspacing=\"0\" summary=\"\">\n"
        + "          <colgroup>\n"
        + "            <col width=\"120\">\n"
        + "            <col width=\"80\">\n");
    for (int i = 0; i < this.downloadedVotes.size(); i++) {
      this.bw.write("            <col width=\""
          + (640 / this.downloadedVotes.size()) + "\">\n");
    }
    this.bw.write("          </colgroup>\n");
    SortedMap<String, String> allRelays = new TreeMap<String, String>();
    for (Status vote : this.downloadedVotes) {
      for (StatusEntry statusEntry : vote.getStatusEntries().values()) {
        allRelays.put(statusEntry.getFingerprint(),
            statusEntry.getNickname());
      }
    }
    for (StatusEntry statusEntry :
        this.downloadedConsensus.getStatusEntries().values()) {
      allRelays.put(statusEntry.getFingerprint(),
          statusEntry.getNickname());
    }
    int linesWritten = 0;
    for (Map.Entry<String, String> e : allRelays.entrySet()) {
      if (linesWritten++ % 10 == 0) {
        this.writeRelayFlagsTableHeader();
      }
      String fingerprint = e.getKey();
      String nickname = e.getValue();
      this.writeRelayFlagsTableRow(fingerprint, nickname);
    }
    this.bw.write("        </table>\n");
  }

  /* Write the table header that is repeated every ten relays and that
   * contains the directory authority names. */
  private void writeRelayFlagsTableHeader() throws IOException {
    this.bw.write("          <tr><td><br><b>Fingerprint</b></td>"
        + "<td><br><b>Nickname</b></td>\n");
    for (Status vote : this.downloadedVotes) {
      String shortDirName = vote.getNickname().length() > 6 ?
          vote.getNickname().substring(0, 5) + "." :
          vote.getNickname();
      this.bw.write("<td><br><b>" + shortDirName + "</b></td>");
    }
    this.bw.write("<td><br><b>consensus</b></td></tr>\n");
  }

  /* Write a single row in the table of relay flags. */
  private void writeRelayFlagsTableRow(String fingerprint,
      String nickname) throws IOException {
    this.bw.write("          <tr>\n");
    if (this.downloadedConsensus.containsStatusEntry(fingerprint) &&
        this.downloadedConsensus.getStatusEntry(fingerprint).getFlags().
        contains("Named") &&
        !Character.isDigit(nickname.charAt(0))) {
      this.bw.write("            <td id=\"" + nickname
          + "\"><a href=\"relay.html?fingerprint="
          + fingerprint + "\" target=\"_blank\">"
          + fingerprint.substring(0, 8) + "</a></td>\n");
    } else {
      this.bw.write("            <td><a href=\"relay.html?fingerprint="
          + fingerprint + "\" target=\"_blank\">"
          + fingerprint.substring(0, 8) + "</a></td>\n");
    }
    this.bw.write("            <td>" + nickname + "</td>\n");
    SortedSet<String> relevantFlags = new TreeSet<String>();
    for (Status vote : this.downloadedVotes) {
      if (vote.containsStatusEntry(fingerprint)) {
        relevantFlags.addAll(vote.getStatusEntry(fingerprint).getFlags());
      }
    }
    SortedSet<String> consensusFlags = null;
    if (this.downloadedConsensus.containsStatusEntry(fingerprint)) {
      consensusFlags = this.downloadedConsensus.
          getStatusEntry(fingerprint).getFlags();
      relevantFlags.addAll(consensusFlags);
    }
    for (Status vote : this.downloadedVotes) {
      if (vote.containsStatusEntry(fingerprint)) {
        SortedSet<String> flags = vote.getStatusEntry(fingerprint).
            getFlags();
        this.bw.write("            <td>");
        int flagsWritten = 0;
        for (String flag : relevantFlags) {
          this.bw.write(flagsWritten++ > 0 ? "<br>" : "");
          if (flags.contains(flag)) {
            if (consensusFlags == null ||
              consensusFlags.contains(flag)) {
              this.bw.write(flag);
            } else {
              this.bw.write("<font color=\"red\">" + flag + "</font>");
            }
          } else if (consensusFlags != null &&
              vote.getKnownFlags().contains(flag) &&
              consensusFlags.contains(flag)) {
            this.bw.write("<font color=\"gray\"><s>" + flag
                + "</s></font>");
          }
        }
        this.bw.write("</td>\n");
      } else {
        this.bw.write("            <td></td>\n");
      }
    }
    if (consensusFlags != null) {
      this.bw.write("            <td>");
      int flagsWritten = 0;
      for (String flag : relevantFlags) {
        this.bw.write(flagsWritten++ > 0 ? "<br>" : "");
        if (consensusFlags.contains(flag)) {
          this.bw.write("<font color=\"blue\">" + flag + "</font>");
        }
      }
      this.bw.write("</td>\n");
    } else {
      this.bw.write("            <td></td>\n");
    }
    this.bw.write("          </tr>\n");
  }

  /* Write the relay flag summary. */
  private void writeRelayFlagsSummary() throws IOException {
    this.bw.write("        <br>\n"
         + "        <h3>Overlap between votes and consensus</h3>\n"
        + "        <br>\n"
        + "        <p>The semantics of columns is similar to the "
          + "table above:</p>\n"
        + "        <ul>\n"
        + "          <li><b>In vote and consensus:</b> Flag in vote "
          + "matches flag in consensus, or relay is not listed in "
          + "consensus (because it doesn't have the Running "
          + "flag)</li>\n"
        + "          <li><b><font color=\"red\">Only in "
          + "vote:</font></b> Flag in vote, but missing in the "
          + "consensus, because there was no majority for the flag or "
          + "the flag was invalidated (e.g., Named gets invalidated by "
          + "Unnamed)</li>\n"
        + "          <li><b><font color=\"gray\"><s>Only in "
          + "consensus:</s></font></b> Flag in consensus, but missing "
          + "in a vote of a directory authority voting on this "
          + "flag</li>\n"
        + "        </ul>\n"
        + "        <br>\n"
        + "        <table border=\"0\" cellpadding=\"4\" "
        + "cellspacing=\"0\" summary=\"\">\n"
        + "          <colgroup>\n"
        + "            <col width=\"160\">\n"
        + "            <col width=\"210\">\n"
        + "            <col width=\"210\">\n"
        + "            <col width=\"210\">\n"
        + "          </colgroup>\n"
        + "          <tr><td></td><td><b>Only in vote</b></td>"
          + "<td><b>In vote and consensus</b></td>"
          + "<td><b>Only in consensus</b></td>\n");
    Set<String> allFingerprints = new HashSet<String>();
    for (Status vote : this.downloadedVotes) {
      allFingerprints.addAll(vote.getStatusEntries().keySet());
    }
    allFingerprints.addAll(this.downloadedConsensus.getStatusEntries().
        keySet());
    SortedMap<String, SortedMap<String, Integer>> flagsAgree =
        new TreeMap<String, SortedMap<String, Integer>>();
    SortedMap<String, SortedMap<String, Integer>> flagsLost =
        new TreeMap<String, SortedMap<String, Integer>>();
    SortedMap<String, SortedMap<String, Integer>> flagsMissing =
        new TreeMap<String, SortedMap<String, Integer>>();
    for (String fingerprint : allFingerprints) {
      SortedSet<String> consensusFlags =
          this.downloadedConsensus.containsStatusEntry(fingerprint) ?
          this.downloadedConsensus.getStatusEntry(fingerprint).getFlags() :
          null;
      for (Status vote : this.downloadedVotes) {
        String dir = vote.getNickname();
        if (vote.containsStatusEntry(fingerprint)) {
          SortedSet<String> flags = vote.getStatusEntry(fingerprint).
              getFlags();
          for (String flag : this.downloadedConsensus.getKnownFlags()) {
            SortedMap<String, SortedMap<String, Integer>> sums = null;
            if (flags.contains(flag)) {
              if (consensusFlags == null ||
                consensusFlags.contains(flag)) {
                sums = flagsAgree;
              } else {
                sums = flagsLost;
              }
            } else if (consensusFlags != null &&
                vote.getKnownFlags().contains(flag) &&
                consensusFlags.contains(flag)) {
              sums = flagsMissing;
            }
            if (sums != null) {
              SortedMap<String, Integer> sum = null;
              if (sums.containsKey(dir)) {
                sum = sums.get(dir);
              } else {
                sum = new TreeMap<String, Integer>();
                sums.put(dir, sum);
              }
              sum.put(flag, sum.containsKey(flag) ?
                  sum.get(flag) + 1 : 1);
            }
          }
        }
      }
    }
    for (Status vote : this.downloadedVotes) {
      String dir = vote.getNickname();
      int i = 0;
      for (String flag : vote.getKnownFlags()) {
        this.bw.write("          <tr>\n"
            + "            <td>" + (i++ == 0 ? dir : "")
              + "</td>\n");
        if (flagsLost.containsKey(dir) &&
            flagsLost.get(dir).containsKey(flag)) {
          this.bw.write("            <td><font color=\"red\"> "
                + flagsLost.get(dir).get(flag) + " " + flag
                + "</font></td>\n");
        } else {
          this.bw.write("            <td></td>\n");
        }
        if (flagsAgree.containsKey(dir) &&
            flagsAgree.get(dir).containsKey(flag)) {
          this.bw.write("            <td>" + flagsAgree.get(dir).get(flag)
                + " " + flag + "</td>\n");
        } else {
          this.bw.write("            <td></td>\n");
        }
        if (flagsMissing.containsKey(dir) &&
            flagsMissing.get(dir).containsKey(flag)) {
          this.bw.write("            <td><font color=\"gray\"><s>"
                + flagsMissing.get(dir).get(flag) + " " + flag
                + "</s></font></td>\n");
        } else {
          this.bw.write("            <td></td>\n");
        }
        this.bw.write("          </tr>\n");
      }
    }
    this.bw.write("        </table>\n");
  }

  /* Write the footer of the HTML page containing the blurb that is on
   * every page of the metrics website. */
  private void writePageFooter() throws IOException {
    this.bw.write("      </div>\n"
        + "    </div>\n"
        + "    <div class=\"bottom\" id=\"bottom\">\n"
        + "      <p>This material is supported in part by the "
          + "National Science Foundation under Grant No. "
          + "CNS-0959138. Any opinions, finding, and conclusions "
          + "or recommendations expressed in this material are "
          + "those of the author(s) and do not necessarily reflect "
          + "the views of the National Science Foundation.</p>\n"
        + "      <p>\"Tor\" and the \"Onion Logo\" are <a "
          + "href=\"https://www.torproject.org/docs/trademark-faq.html"
          + ".en\">"
        + "registered trademarks</a> of The Tor Project, "
          + "Inc.</p>\n"
        + "      <p>Data on this site is freely available under a "
          + "<a href=\"http://creativecommons.org/publicdomain/"
          + "zero/1.0/\">CC0 no copyright declaration</a>: To the "
          + "extent possible under law, the Tor Project has waived "
          + "all copyright and related or neighboring rights in "
          + "the data. Graphs are licensed under a <a "
          + "href=\"http://creativecommons.org/licenses/by/3.0/"
          + "us/\">Creative Commons Attribution 3.0 United States "
          + "License</a>.</p>\n"
        + "    </div>\n"
        + "  </body>\n"
        + "</html>");
  }
}

