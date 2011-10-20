/* Copyright 2011 The Tor Project
 * See LICENSE for licensing information */
package org.torproject.chc;

import java.util.*;

/* Transform findings from parsing consensuses and votes into a report of
 * some form. */
public interface Report {

  /* Process the downloaded current consensus and corresponding votes to
   * find irregularities between them. */
  public abstract void processDownloadedConsensuses(
      SortedMap<String, Status> downloadedConsensuses);

  /* Finish writing report. */
  public abstract void writeReport();
}

