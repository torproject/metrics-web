/* Copyright 2011 The Tor Project
 * See LICENSE for licensing information */
package org.torproject.chc;

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;
import java.util.zip.*;

/* Download the latest network status consensus and corresponding
 * votes. */
public class Downloader {

  /* List of directory authorities to download consensuses and votes
   * from. */
  private SortedMap<String, String> authorities =
      new TreeMap<String, String>();
  public Downloader() {
    this.authorities.put("gabelmoo", "212.112.245.170");
    this.authorities.put("tor26", "86.59.21.38");
    this.authorities.put("ides", "216.224.124.114:9030");
    this.authorities.put("maatuska", "213.115.239.118:443");
    this.authorities.put("dannenberg", "193.23.244.244");
    this.authorities.put("urras", "208.83.223.34:443");
    this.authorities.put("moria1", "128.31.0.34:9131");
    this.authorities.put("dizum", "194.109.206.212");
  }

  /* Download a new consensus and corresponding votes. */
  public void downloadFromAuthorities() {
    this.downloadConsensus();
    if (!this.downloadedConsensuses.isEmpty()) {
      this.parseConsensusToFindReferencedVotes();
      this.downloadReferencedVotes();
    }
  }

  /* Download the most recent consensus from all authorities. */
  private SortedMap<String, String> downloadedConsensuses =
      new TreeMap<String, String>();
  private void downloadConsensus() {
    for (Map.Entry<String, String> e : this.authorities.entrySet()) {
      String nickname = e.getKey();
      String address = e.getValue();
      String resource = "/tor/status-vote/current/consensus.z";
      String fullUrl = "http://" + address + resource;
      String response = this.downloadFromAuthority(fullUrl);
      if (response != null) {
        this.downloadedConsensuses.put(nickname, response);
      } else {
        System.err.println("Could not download consensus from directory "
            + "authority " + nickname + ".  Ignoring.");
      }
    }
    if (this.downloadedConsensuses.isEmpty()) {
      System.err.println("Could not download consensus from any of the "
          + "directory authorities.  Ignoring.");
    }
  }

  /* Downloads a consensus or vote in a separate thread that can be
   * interrupted after a timeout. */
  private static class DownloadRunnable implements Runnable {
    Thread mainThread;
    String url;
    String response;
    boolean interrupted = false;
    public DownloadRunnable(String url) {
      this.mainThread = Thread.currentThread();
      this.url = url;
    }
    public void run() {
      try {
        URL u = new URL(url);
        HttpURLConnection huc = (HttpURLConnection) u.openConnection();
        huc.setRequestMethod("GET");
        huc.connect();
        int responseCode = huc.getResponseCode();
        if (responseCode == 200) {
          BufferedInputStream in = new BufferedInputStream(
              new InflaterInputStream(huc.getInputStream()));
          ByteArrayOutputStream baos = new ByteArrayOutputStream();
          int len;
          byte[] data = new byte[1024];
          while (!this.interrupted &&
              (len = in.read(data, 0, 1024)) >= 0) {
            baos.write(data, 0, len);
          }
          if (this.interrupted) {
            return;
          }
          in.close();
          byte[] allData = baos.toByteArray();
          this.response = new String(allData);
          this.mainThread.interrupt();
        }
      } catch (IOException e) {
        /* Can't do much except leaving this.response at null. */
      }
    }
  }

  /* Download a consensus or vote from a directory authority using a
   * timeout of 60 seconds. */
  private String downloadFromAuthority(final String url) {
    DownloadRunnable downloadRunnable = new DownloadRunnable(url);
    new Thread(downloadRunnable).start();
    try {
      Thread.sleep(60L * 1000L);
    } catch (InterruptedException e) {
      /* Do nothing. */
    }
    String response = downloadRunnable.response;
    downloadRunnable.interrupted = true;
    return response;
  }

  /* Date-time formats to parse and format timestamps. */
  private static SimpleDateFormat dateTimeFormat;
  static {
    dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    dateTimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
  }

  /* Parse the downloaded consensus to find fingerprints of directory
   * authorities publishing the corresponding votes. */
  private List<String> fingerprints = new ArrayList<String>();
  private void parseConsensusToFindReferencedVotes() {
    for (String downloadedConsensus :
        this.downloadedConsensuses.values()) {
      try {
        BufferedReader br = new BufferedReader(new StringReader(
            downloadedConsensus));
        String line;
        while ((line = br.readLine()) != null) {
          if (line.startsWith("valid-after ")) {
            try {
              long validAfterMillis = dateTimeFormat.parse(line.substring(
                  "valid-after ".length())).getTime();
              if (validAfterMillis + 60L * 60L * 1000L <
                  System.currentTimeMillis()) {
                /* Consensus is more than 1 hour old.  We won't be able to
                 * download the corresponding votes anymore. */
                break;
              }
            } catch (ParseException e) {
              System.err.println("Could not parse valid-after timestamp "
                  + "in line '" + line + "' of a downloaded consensus.  "
                  + "Not downloading votes.");
              break;
            }
          } else if (line.startsWith("dir-source ")) {
            String[] parts = line.split(" ");
            if (parts.length < 3) {
              System.err.println("Bad dir-source line '" + line
                  + "' in downloaded consensus.  Skipping.");
              continue;
            }
            String nickname = parts[1];
            if (nickname.endsWith("-legacy")) {
              continue;
            }
            String fingerprint = parts[2];
            this.fingerprints.add(fingerprint);
          }
        }
        br.close();
      } catch (IOException e) {
        System.err.println("Could not parse consensus to find referenced "
            + "votes in it.  Skipping.");
      }
    }
  }

  /* Download the votes published by directory authorities listed in the
   * consensus. */
  private List<String> downloadedVotes = new ArrayList<String>();
  private void downloadReferencedVotes() {
    for (String fingerprint : this.fingerprints) {
      String downloadedVote = null;
      List<String> authorities = new ArrayList<String>(
          this.authorities.values());
      Collections.shuffle(authorities);
      for (String authority : authorities) {
        if (downloadedVote != null) {
          break;
        }
        String resource = "/tor/status-vote/current/" + fingerprint
            + ".z";
        String fullUrl = "http://" + authority + resource;
        downloadedVote = this.downloadFromAuthority(fullUrl);
        if (downloadedVote != null) {
          this.downloadedVotes.add(downloadedVote);
        }
      }
    }
  }

  /* Return the previously downloaded (unparsed) consensus string by
   * authority nickname. */
  public SortedMap<String, String> getConsensusStrings() {
    return this.downloadedConsensuses;
  }

  /* Return the previously downloaded (unparsed) vote strings. */
  public List<String> getVoteStrings() {
    return this.downloadedVotes;
  }
}

