/* Copyright 2017--2018 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.web;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/** Periodically fetches a remote index.json file and provides formatted
 * directory listings for all contained directories and subdirectories. */
public class CollectorDirectoryProvider implements Runnable {

  /** Host name of the host serving the remote index.json with trailing slash
   * omitted. */
  private String host;

  /** Scheduler for periodically downloading the remote index.json file. */
  private final ScheduledExecutorService scheduler =
      Executors.newScheduledThreadPool(1);

  /** Last known directory listings. */
  private final AtomicReference<Map<String, List<String[]>>> index
      = new AtomicReference<>(null);

  CollectorDirectoryProvider(String host) {
    this.host = host;
    this.scheduler.scheduleAtFixedRate(this, 0, 1, TimeUnit.MINUTES);
  }

  /** Returns the index object in a thread-safe way, blocking the invoking
   * thread at most 10 seconds if no index object is available. */
  Map<String, List<String[]>> getIndex() {
    if (null == this.index.get()) {
      long waitingSinceMillis = System.currentTimeMillis();
      do {
        try {
          Thread.sleep(200L);
        } catch (InterruptedException e) {
          /* Ignore. */
        }
      } while (null == this.index.get()
          && System.currentTimeMillis() < waitingSinceMillis + 10000L);
    }
    return this.index.get();
  }

  /** Fetch the remote index.json and extract all we need to know to later
   * produce directory listings as requested. */
  @Override
  public void run() {
    try {
      DirectoryListing directoryListing
          = DirectoryListing.ofHostString(this.host);
      this.index.set(directoryListing);
    } catch (Exception e) {
      /* If we failed to fetch the remote index.json this time, abort the
       * update and don't override what we possibly fetched last time. If this
       * is a temporary problem, one of the next runs will update the index. If
       * it's a permanent problem, we'll at least serve the last known files.
       * Unless it's a permanent problem right from when we started in which
       * case there's nothing we can do other than return 500. */
    }
  }

}

