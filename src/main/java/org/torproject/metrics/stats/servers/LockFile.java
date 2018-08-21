/* Copyright 2011--2018 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.stats.servers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class LockFile {

  private File lockFile;

  private static Logger log = LoggerFactory.getLogger(LockFile.class);

  public LockFile() {
    this.lockFile = new File("lock");
  }

  /** Acquires the lock by checking whether a lock file already exists,
   * and if not, by creating one with the current system time as
   * content. */
  public boolean acquireLock() {
    log.debug("Trying to acquire lock...");
    try {
      if (this.lockFile.exists()) {
        BufferedReader br = new BufferedReader(new FileReader("lock"));
        long runStarted = Long.parseLong(br.readLine());
        br.close();
        if (System.currentTimeMillis() - runStarted
            < 23L * 60L * 60L * 1000L) {
          return false;
        }
      }
      BufferedWriter bw = new BufferedWriter(new FileWriter("lock"));
      bw.append("").append(String.valueOf(System.currentTimeMillis()))
          .append("\n");
      bw.close();
      log.debug("Acquired lock.");
      return true;
    } catch (IOException e) {
      log.warn("Caught exception while trying to acquire "
          + "lock!");
      return false;
    }
  }

  /** Releases the lock by deleting the lock file, if present. */
  public void releaseLock() {
    log.debug("Releasing lock...");
    this.lockFile.delete();
    log.debug("Released lock.");
  }
}

