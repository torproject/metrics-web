/* Copyright 2011--2016 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.ernie.cron;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Logger;

public class LockFile {

  private File lockFile;
  private Logger logger;

  public LockFile() {
    this.lockFile = new File("lock");
    this.logger = Logger.getLogger(LockFile.class.getName());
  }

  /** Acquires the lock by checking whether a lock file already exists,
   * and if not, by creating one with the current system time as
   * content. */
  public boolean acquireLock() {
    this.logger.fine("Trying to acquire lock...");
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
      bw.append("" + System.currentTimeMillis() + "\n");
      bw.close();
      this.logger.fine("Acquired lock.");
      return true;
    } catch (IOException e) {
      this.logger.warning("Caught exception while trying to acquire "
          + "lock!");
      return false;
    }
  }

  /** Releases the lock by deleting the lock file, if present. */
  public void releaseLock() {
    this.logger.fine("Releasing lock...");
    this.lockFile.delete();
    this.logger.fine("Released lock.");
  }
}

