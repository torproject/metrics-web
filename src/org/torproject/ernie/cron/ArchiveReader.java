/* Copyright 2011, 2012 The Tor Project
 * See LICENSE for licensing information */
package org.torproject.ernie.cron;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * Read in all files in a given directory and pass buffered readers of
 * them to the relay descriptor parser.
 */
public class ArchiveReader {

  /**
   * Stats file handler that accepts parse results for bridge statistics.
   */
  private BridgeStatsFileHandler bsfh;

  /**
   * Relay descriptor database importer that stores relay descriptor
   * contents for later evaluation.
   */
  private RelayDescriptorDatabaseImporter rddi;

  /**
   * Logger for this class.
   */
  private Logger logger;

  private SimpleDateFormat dateTimeFormat;

  public ArchiveReader(RelayDescriptorDatabaseImporter rddi,
      BridgeStatsFileHandler bsfh, File archivesDirectory,
      File statsDirectory, boolean keepImportHistory) {

    if (archivesDirectory == null ||
        statsDirectory == null) {
      throw new IllegalArgumentException();
    }

    this.rddi = rddi;
    this.bsfh = bsfh;

    this.dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    this.dateTimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

    int parsedFiles = 0, ignoredFiles = 0;
    this.logger = Logger.getLogger(ArchiveReader.class.getName());
    SortedMap<String, Long>
        lastArchivesImportHistory = new TreeMap<String, Long>(),
        newArchivesImportHistory = new TreeMap<String, Long>();
    File archivesImportHistoryFile = new File(statsDirectory,
        "archives-import-history");
    if (keepImportHistory && archivesImportHistoryFile.exists()) {
      try {
        BufferedReader br = new BufferedReader(new FileReader(
            archivesImportHistoryFile));
        String line = null;
        while ((line = br.readLine()) != null) {
          String[] parts = line.split(",");
          if (parts.length < 2) {
            logger.warning("Archives import history file does not "
                + "contain timestamps. Skipping.");
            break;
          }
          long lastModified = Long.parseLong(parts[0]);
          String filename = parts[1];
          lastArchivesImportHistory.put(filename, lastModified);
        }
        br.close();
      } catch (IOException e) {
        logger.log(Level.WARNING, "Could not read in archives import "
            + "history file. Skipping.");
      }
    }
    if (archivesDirectory.exists()) {
      logger.fine("Importing files in directory " + archivesDirectory
          + "/...");
      Stack<File> filesInInputDir = new Stack<File>();
      filesInInputDir.add(archivesDirectory);
      List<File> problems = new ArrayList<File>();
      while (!filesInInputDir.isEmpty()) {
        File pop = filesInInputDir.pop();
        if (pop.isDirectory()) {
          for (File f : pop.listFiles()) {
            filesInInputDir.add(f);
          }
        } else {
          try {
            long lastModified = pop.lastModified();
            String filename = pop.getName();
            if (keepImportHistory) {
              newArchivesImportHistory.put(filename, lastModified);
            }
            if (keepImportHistory &&
                lastArchivesImportHistory.containsKey(filename) &&
                lastArchivesImportHistory.get(filename) >= lastModified) {
              ignoredFiles++;
              continue;
            } else if (filename.endsWith(".tar.bz2")) {
              logger.warning("Cannot parse compressed tarball "
                  + pop.getAbsolutePath() + ". Skipping.");
              continue;
            }
            FileInputStream fis = new FileInputStream(pop);
            BufferedInputStream bis = new BufferedInputStream(fis);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int len;
            byte[] data = new byte[1024];
            while ((len = bis.read(data, 0, 1024)) >= 0) {
              baos.write(data, 0, len);
            }
            bis.close();
            byte[] allData = baos.toByteArray();
            this.parse(allData);
            parsedFiles++;
          } catch (IOException e) {
            problems.add(pop);
            if (problems.size() > 3) {
              break;
            }
          }
        }
      }
      if (problems.isEmpty()) {
        logger.fine("Finished importing files in directory "
            + archivesDirectory + "/.");
      } else {
        StringBuilder sb = new StringBuilder("Failed importing files in "
            + "directory " + archivesDirectory + "/:");
        int printed = 0;
        for (File f : problems) {
          sb.append("\n  " + f.getAbsolutePath());
          if (++printed >= 3) {
            sb.append("\n  ... more");
            break;
          }
        }
        logger.warning(sb.toString());
      }
    }
    if (keepImportHistory) {
      try {
        archivesImportHistoryFile.getParentFile().mkdirs();
        BufferedWriter bw = new BufferedWriter(new FileWriter(
            archivesImportHistoryFile));
        for (Map.Entry<String, Long> historyEntry :
            newArchivesImportHistory.entrySet()) {
          bw.write(String.valueOf(historyEntry.getValue()) + ","
              + historyEntry.getKey() + "\n");
        }
        bw.close();
      } catch (IOException e) {
        logger.log(Level.WARNING, "Could not write archives import "
            + "history file.");
      }
    }
    logger.info("Finished importing relay descriptors from local "
        + "directory:\nParsed " + parsedFiles + ", ignored "
        + ignoredFiles + " files.");
  }

  public void parse(byte[] data) {
    try {
      /* Remove any @ lines at the beginning of the file and parse the
       * first non-@ line to find out the descriptor type. */
      BufferedReader br = new BufferedReader(new StringReader(new String(
          data, "US-ASCII")));
      String line = br.readLine();
      while (line != null && line.startsWith("@")) {
        line = br.readLine();
      }
      if (line == null) {
        this.logger.fine("We were given a file that doesn't contain a "
            + "single descriptor for parsing. Ignoring.");
        return;
      }
      br.close();

      /* Split the byte[] possibly containing multiple descriptors into
       * byte[]'s with one descriptor each and parse them. */
      String startToken = null;
      if (line.equals("network-status-version 3")) {
        startToken = "network-status-version 3";
      } else if (line.startsWith("router ")) {
        startToken = "router ";
      } else if (line.startsWith("extra-info ")) {
        startToken = "extra-info ";
      } else if (line.equals("dir-key-certificate-version 3")) {
        this.logger.fine("Not parsing dir key certificate.");
        return;
      } else {
        this.logger.warning("Unknown descriptor type.  First line is '"
            + line + "'.  Ignoring.");
        return;
      }
      String splitToken = "\n" + startToken;
      String ascii = new String(data, "US-ASCII");
      int length = data.length, start = ascii.indexOf(startToken);
      while (start < length) {
        int end = ascii.indexOf(splitToken, start);
        if (end < 0) {
          end = length;
        } else {
          end += 1;
        }
        byte[] descBytes = new byte[end - start];
        System.arraycopy(data, start, descBytes, 0, end - start);
        parseSingleDescriptor(descBytes);
        start = end;
      }
    } catch (IOException e) {
      this.logger.log(Level.WARNING, "Could not parse descriptor. "
          + "Skipping.", e);
    }
  }

  private void parseSingleDescriptor(byte[] data) {
    try {
      /* Convert descriptor to ASCII for parsing. This means we'll lose
       * the non-ASCII chars, but we don't care about them for parsing
       * anyway. */
      BufferedReader br = new BufferedReader(new StringReader(new String(
          data, "US-ASCII")));
      String line = br.readLine();
      SimpleDateFormat parseFormat =
          new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      parseFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
      if (line.equals("network-status-version 3")) {
        // TODO when parsing the current consensus, check the fresh-until
        // time to see when we switch from hourly to half-hourly
        // consensuses
        boolean isConsensus = true;
        String validAfterTime = null, nickname = null,
            relayIdentity = null, serverDesc = null, version = null,
            ports = null;
        String dirSource = null, address = null;
        long validAfter = -1L, published = -1L, bandwidth = -1L,
            orPort = 0L, dirPort = 0L;
        SortedSet<String> relayFlags = null;
        StringBuilder rawStatusEntry = null;
        SortedSet<String> hashedRelayIdentities = new TreeSet<String>();
        while ((line = br.readLine()) != null) {
          if (line.equals("vote-status vote")) {
            isConsensus = false;
          } else if (line.startsWith("valid-after ")) {
            validAfterTime = line.substring("valid-after ".length());
            validAfter = parseFormat.parse(validAfterTime).getTime();
          } else if (line.startsWith("dir-source ")) {
            dirSource = line.split(" ")[2];
          } else if (line.startsWith("r ")) {
            if (isConsensus && relayIdentity != null &&
                this.rddi != null) {
              byte[] rawDescriptor = rawStatusEntry.toString().getBytes();
              this.rddi.addStatusEntry(validAfter, nickname,
                  relayIdentity, serverDesc, published, address, orPort,
                  dirPort, relayFlags, version, bandwidth, ports,
                  rawDescriptor);
              relayFlags = null;
              version = null;
              bandwidth = -1L;
              ports = null;
            }
            rawStatusEntry = new StringBuilder(line + "\n");
            String[] parts = line.split(" ");
            if (parts.length < 9) {
              this.logger.log(Level.WARNING, "Could not parse r line '"
                  + line + "' in descriptor. Skipping.");
              break;
            }
            nickname = parts[1];
            relayIdentity = Hex.encodeHexString(
                Base64.decodeBase64(parts[2] + "=")).
                toLowerCase();
            hashedRelayIdentities.add(DigestUtils.shaHex(
                Base64.decodeBase64(parts[2] + "=")).
                toUpperCase());
            serverDesc = Hex.encodeHexString(Base64.decodeBase64(
                parts[3] + "=")).toLowerCase();
            published = parseFormat.parse(parts[4] + " " + parts[5]).
                getTime();
            address = parts[6];
            orPort = Long.parseLong(parts[7]);
            dirPort = Long.parseLong(parts[8]);
          } else if (line.startsWith("s ") || line.equals("s")) {
            rawStatusEntry.append(line + "\n");
            relayFlags = new TreeSet<String>();
            if (line.length() > 2) {
              for (String flag : line.substring(2).split(" ")) {
                relayFlags.add(flag);
              }
            }
          } else if (line.startsWith("v ")) {
            rawStatusEntry.append(line + "\n");
            version = line.substring(2);
          } else if (line.startsWith("w ")) {
            rawStatusEntry.append(line + "\n");
            String[] parts = line.split(" ");
            for (String part : parts) {
              if (part.startsWith("Bandwidth=")) {
                bandwidth = Long.parseLong(part.substring(
                    "Bandwidth=".length()));
              }
            }
          } else if (line.startsWith("p ")) {
            rawStatusEntry.append(line + "\n");
            ports = line.substring(2);
          }
        }
        if (isConsensus) {
          if (this.bsfh != null) {
            for (String hashedRelayIdentity : hashedRelayIdentities) {
              this.bsfh.addHashedRelay(hashedRelayIdentity);
            }
          }
          if (this.rddi != null) {
            this.rddi.addConsensus(validAfter, data);
            if (relayIdentity != null) {
              byte[] rawDescriptor = rawStatusEntry.toString().getBytes();
              this.rddi.addStatusEntry(validAfter, nickname,
                  relayIdentity, serverDesc, published, address, orPort,
                  dirPort, relayFlags, version, bandwidth, ports,
                  rawDescriptor);
            }
          }
        } else {
          if (this.rddi != null) {
            this.rddi.addVote(validAfter, dirSource, data);
          }
        }
      } else if (line.startsWith("router ")) {
        String platformLine = null, bandwidthLine = null,
            extraInfoDigest = null, relayIdentifier = null;
        String[] parts = line.split(" ");
        String nickname = parts[1];
        String address = parts[2];
        int orPort = Integer.parseInt(parts[3]);
        int dirPort = Integer.parseInt(parts[4]);
        long published = -1L, uptime = -1L;
        while ((line = br.readLine()) != null) {
          if (line.startsWith("platform ")) {
            platformLine = line;
          } else if (line.startsWith("published ")) {
            String publishedTime = line.substring("published ".length());
            published = parseFormat.parse(publishedTime).getTime();
          } else if (line.startsWith("opt fingerprint") ||
              line.startsWith("fingerprint")) {
            relayIdentifier = line.substring(line.startsWith("opt ") ?
                "opt fingerprint".length() : "fingerprint".length()).
                replaceAll(" ", "").toLowerCase();
          } else if (line.startsWith("bandwidth ")) {
            bandwidthLine = line;
          } else if (line.startsWith("opt extra-info-digest ") ||
              line.startsWith("extra-info-digest ")) {
            extraInfoDigest = line.startsWith("opt ") ?
                line.split(" ")[2].toLowerCase() :
                line.split(" ")[1].toLowerCase();
          } else if (line.startsWith("uptime ")) {
            uptime = Long.parseLong(line.substring("uptime ".length()));
          }
        }
        String ascii = new String(data, "US-ASCII");
        String startToken = "router ";
        String sigToken = "\nrouter-signature\n";
        int start = ascii.indexOf(startToken);
        int sig = ascii.indexOf(sigToken) + sigToken.length();
        String digest = null;
        if (start >= 0 || sig >= 0 || sig > start) {
          byte[] forDigest = new byte[sig - start];
          System.arraycopy(data, start, forDigest, 0, sig - start);
          digest = DigestUtils.shaHex(forDigest);
        }
        if (this.rddi != null && digest != null) {
          String[] bwParts = bandwidthLine.split(" ");
          long bandwidthAvg = Long.parseLong(bwParts[1]);
          long bandwidthBurst = Long.parseLong(bwParts[2]);
          long bandwidthObserved = Long.parseLong(bwParts[3]);
          String platform = platformLine.substring("platform ".length());
          this.rddi.addServerDescriptor(digest, nickname, address, orPort,
              dirPort, relayIdentifier, bandwidthAvg, bandwidthBurst,
              bandwidthObserved, platform, published, uptime,
              extraInfoDigest, data);
        }
      } else if (line.startsWith("extra-info ")) {
        String nickname = line.split(" ")[1];
        long published = -1L;
        String dir = line.split(" ")[2];
        String statsEnd = null;
        long seconds = -1L;
        List<String> bandwidthHistory = new ArrayList<String>();
        while ((line = br.readLine()) != null) {
          if (line.startsWith("published ")) {
            String publishedTime = line.substring("published ".length());
            published = parseFormat.parse(publishedTime).getTime();
          } else if (line.startsWith("read-history ") ||
              line.startsWith("write-history ") ||
              line.startsWith("dirreq-read-history ") ||
              line.startsWith("dirreq-write-history ")) {
            bandwidthHistory.add(line);
          } else if (line.startsWith("dirreq-stats-end ")) {
            String[] parts = line.split(" ");
            if (parts.length < 5) {
              this.logger.warning("Could not parse dirreq-stats-end "
                  + "line '" + line + "' in descriptor. Skipping.");
              break;
            }
            statsEnd = parts[1] + " " + parts[2];
            seconds = Long.parseLong(parts[3].substring(1));
          } else if (line.startsWith("dirreq-v3-reqs ")
              && line.length() > "dirreq-v3-reqs ".length()) {
            if (this.rddi != null) {
              try {
                int allUsers = 0;
                Map<String, String> obs = new HashMap<String, String>();
                String[] parts = line.substring("dirreq-v3-reqs ".
                    length()).split(",");
                for (String p : parts) {
                  String country = p.substring(0, 2);
                  int users = Integer.parseInt(p.substring(3)) - 4;
                  allUsers += users;
                  obs.put(country, "" + users);
                }
                obs.put("zy", "" + allUsers);
                this.rddi.addDirReqStats(dir, statsEnd, seconds, obs);
              } catch (NumberFormatException e) {
                this.logger.log(Level.WARNING, "Could not parse "
                    + "dirreq-v3-reqs line '" + line + "' in descriptor. "
                    + "Skipping.", e);
                break;
              }
            }
          } else if (line.startsWith("conn-bi-direct ")) {
            if (this.rddi != null) {
              String[] parts = line.split(" ");
              if (parts.length == 6 &&
                  parts[5].split(",").length == 4) {
                try {
                  String connBiDirectStatsEnd = parts[1] + " " + parts[2];
                  long connBiDirectSeconds = Long.parseLong(parts[3].
                      substring(1));
                  String[] parts2 = parts[5].split(",");
                  long below = Long.parseLong(parts2[0]);
                  long read = Long.parseLong(parts2[1]);
                  long write = Long.parseLong(parts2[2]);
                  long both = Long.parseLong(parts2[3]);
                  this.rddi.addConnBiDirect(dir, connBiDirectStatsEnd,
                      connBiDirectSeconds, below, read, write, both);
                } catch (NumberFormatException e) {
                  this.logger.log(Level.WARNING, "Number format "
                      + "exception while parsing conn-bi-direct stats "
                      + "string '" + line + "'. Skipping.", e);
                }
              } else {
                this.logger.warning("Skipping invalid conn-bi-direct "
                    + "stats string '" + line + "'.");
              }
            }
          }
        }
        String ascii = new String(data, "US-ASCII");
        String startToken = "extra-info ";
        String sigToken = "\nrouter-signature\n";
        String digest = null;
        int start = ascii.indexOf(startToken);
        int sig = ascii.indexOf(sigToken) + sigToken.length();
        if (start >= 0 || sig >= 0 || sig > start) {
          byte[] forDigest = new byte[sig - start];
          System.arraycopy(data, start, forDigest, 0, sig - start);
          digest = DigestUtils.shaHex(forDigest);
        }
        if (this.rddi != null && digest != null) {
          this.rddi.addExtraInfoDescriptor(digest, nickname,
              dir.toLowerCase(), published, data, bandwidthHistory);
        }
      }
    } catch (IOException e) {
      this.logger.log(Level.WARNING, "Could not parse descriptor. "
          + "Skipping.", e);
    } catch (ParseException e) {
      this.logger.log(Level.WARNING, "Could not parse descriptor. "
          + "Skipping.", e);
    }
  }
}

