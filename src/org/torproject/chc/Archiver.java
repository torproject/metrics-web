/* Copyright 2011 The Tor Project
 * See LICENSE for licensing information */
package org.torproject.chc;

import java.io.*;
import java.util.*;

/* Load the last network status consensus and corresponding votes from
 * disk and save newly downloaded ones to disk. */
public class Archiver {

  /* Local directory containing cached consensuses and votes. */
  private File cachedFilesDirectory = new File("chc-cache");

  /* Copy of the most recent cached consensus and votes. */
  private String loadedConsensus;
  private List<String> loadedVotes = new ArrayList<String>();

  /* Load the most recent cached consensus and votes to memory. */
  public void loadLastFromDisk() {
    if (!this.cachedFilesDirectory.exists()) {
      return;
    }
    String lastValidAfter = this.findLastConsensusPrefix();
    if (lastValidAfter != null) {
      for (File file : this.cachedFilesDirectory.listFiles()) {
        if (file.isDirectory() ||
            !file.getName().startsWith(lastValidAfter)) {
          continue;
        }
        String content = null;
        try {
          FileInputStream fis = new FileInputStream(file);
          BufferedInputStream bis = new BufferedInputStream(fis);
          ByteArrayOutputStream baos = new ByteArrayOutputStream();
          int len;
          byte[] data = new byte[1024];
          while ((len = bis.read(data, 0, 1024)) >= 0) {
            baos.write(data, 0, len);
          }
          bis.close();
          byte[] allData = baos.toByteArray();
          content = new String(allData);
        } catch (IOException e) {
          System.err.println("Could not read cached status from file '"
              + file.getAbsolutePath() + "'.  Skipping.");
          continue;
        }
        if (file.getName().contains("-consensus")) {
          this.loadedConsensus = content;
        } else if (file.getName().contains("-vote-")) {
          this.loadedVotes.add(content);
        }
      }
    }
  }

  /* Delete all cached consensuses and votes but the last consensus and
   * corresponding votes. */
  public void deleteAllButLast() {
    if (!this.cachedFilesDirectory.exists()) {
      return;
    }
    String lastValidAfter = this.findLastConsensusPrefix();
    if (lastValidAfter != null) {
      for (File file : this.cachedFilesDirectory.listFiles()) {
        if (!file.getName().startsWith(lastValidAfter)) {
          file.delete();
        }
      }
    }
  }

  /* Save a status to disk under the given file name. */
  public void saveStatusStringToDisk(String content, String fileName) {
    try {
      this.cachedFilesDirectory.mkdirs();
      BufferedWriter bw = new BufferedWriter(new FileWriter(new File(
          this.cachedFilesDirectory, fileName)));
      bw.write(content);
      bw.close();
    } catch (IOException e) {
      System.err.println("Could not save status to file '"
          + this.cachedFilesDirectory.getAbsolutePath() + "/" + fileName
          + "'.  Ignoring.");
    }
  }

  /* Find and return the timestamp prefix of the last published
   * consensus. */
  private String findLastConsensusPrefix() {
    String lastValidAfter = null;
    for (File file : this.cachedFilesDirectory.listFiles()) {
      if (file.isDirectory() ||
          file.getName().length() !=
          "yyyy-MM-dd-HH-mm-ss-consensus".length() ||
          !file.getName().endsWith("-consensus")) {
        continue;
      }
      String prefix = file.getName().substring(0,
          "yyyy-MM-dd-HH-mm-ss".length());
      if (lastValidAfter == null ||
          prefix.compareTo(lastValidAfter) > 0) {
        lastValidAfter = prefix;
      }
    }
    return lastValidAfter;
  }

  /* Return the previously loaded (unparsed) consensus string. */
  public String getConsensusString() {
    return this.loadedConsensus;
  }

  /* Return the previously loaded (unparsed) vote strings. */
  public List<String> getVoteStrings() {
    return this.loadedVotes;
  }
}

