/* Copyright 2011 The Tor Project
 * See LICENSE for licensing information */
package org.torproject.chc;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.zip.*;

/* Download the latest network status consensus and corresponding
 * votes. */
public class Downloader {

  /* List of directory authorities to download consensuses and votes
   * from. */
  private static final List<String> AUTHORITIES =
      new ArrayList<String>(Arrays.asList(("212.112.245.170,86.59.21.38,"
      + "216.224.124.114:9030,213.115.239.118:443,193.23.244.244,"
      + "208.83.223.34:443,128.31.0.34:9131,194.109.206.212").
      split(",")));

  /* Set the last known valid-after time to avoid downloading a new
   * consensus if there cannot be a new one yet. */
  private long lastKnownValidAfterMillis;
  public void setLastKnownValidAfterMillis(
      long lastKnownValidAfterMillis) {
    this.lastKnownValidAfterMillis = lastKnownValidAfterMillis;
  }

  /* Download a new consensus and corresponding votes if we expect them to
   * be newer than the ones we have. */
  public void downloadFromAuthorities() {
    if (System.currentTimeMillis() - lastKnownValidAfterMillis <
        60L * 60L * 1000L) {
      return;
    }
    this.downloadConsensus();
    if (this.downloadedConsensus != null) {
      this.parseConsensusToFindReferencedVotes();
      this.downloadReferencedVotes();
    }
  }

  /* Download the most recent consensus. */
  private String downloadedConsensus;
  private void downloadConsensus() {
    List<String> authorities = new ArrayList<String>(AUTHORITIES);
    Collections.shuffle(authorities);
    for (String authority : authorities) {
      if (this.downloadedConsensus != null) {
        break;
      }
      String resource = "/tor/status-vote/current/consensus.z";
      String fullUrl = "http://" + authority + resource;
      String response = this.downloadFromAuthority(fullUrl);
      if (response != null) {
        this.downloadedConsensus = response;
      } else {
        System.err.println("Could not download consensus from directory "
            + "authority " + authority + ".  Ignoring.");
      }
    }
    if (this.downloadedConsensus == null) {
      System.err.println("Could not download consensus from any of the "
          + "directory authorities.  Ignoring.");
    }
  }

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

  /* Parse the downloaded consensus to find fingerprints of directory
   * authorities publishing the corresponding votes. */
  private List<String> fingerprints = new ArrayList<String>();
  private void parseConsensusToFindReferencedVotes() {
    if (this.downloadedConsensus != null) {
      try {
        BufferedReader br = new BufferedReader(new StringReader(
            this.downloadedConsensus));
        String line;
        while ((line = br.readLine()) != null) {
          if (line.startsWith("dir-source ")) {
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
      List<String> authorities = new ArrayList<String>(AUTHORITIES);
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

  /* Return the previously downloaded (unparsed) consensus string. */
  public String getConsensusString() {
    return this.downloadedConsensus;
  }

  /* Return the previously downloaded (unparsed) vote strings. */
  public List<String> getVoteStrings() {
    return this.downloadedVotes;
  }
}

