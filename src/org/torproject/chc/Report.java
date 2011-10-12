/* Copyright 2011 The Tor Project
 * See LICENSE for licensing information */
package org.torproject.chc;

import java.util.*;

/* Transform findings from parsing consensuses and votes into a report of
 * some form. */
public interface Report {

  /* Process the cached consensus and corresponding votes to compare them
   * to the downloaded ones. */
  public abstract void processCachedConsensus(Status cachedConsensus);

  /* Process the downloaded current consensus and corresponding votes to
   * find irregularities between them. */
  public abstract void processDownloadedConsensus(
      Status downloadedConsensus);

  /* Finish writing report. */
  public abstract void writeReport();
}

