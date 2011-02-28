/* Copyright 2011 The Tor Project
 * See LICENSE for licensing information */
package org.torproject.ernie.cron;

import java.io.*;
import java.text.*;
import java.util.*;
import java.util.logging.*;
import org.apache.commons.codec.digest.*;
import org.apache.commons.codec.binary.*;

/**
 * Parses relay descriptors including network status consensuses and
 * votes, server and extra-info descriptors, and passes the results to the
 * stats handlers, to the archive writer, or to the relay descriptor
 * downloader.
 */
public class RelayDescriptorParser {

  private ConsensusHealthChecker chc;

  /**
   * Logger for this class.
   */
  private Logger logger;

  private SimpleDateFormat dateTimeFormat;

  /**
   * Initializes this class.
   */
  public RelayDescriptorParser(ConsensusHealthChecker chc) {
    this.chc = chc;

    /* Initialize logger. */
    this.logger = Logger.getLogger(RelayDescriptorParser.class.getName());

    this.dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    this.dateTimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
  }

  public void parse(byte[] data) {
    try {
      /* Convert descriptor to ASCII for parsing. This means we'll lose
       * the non-ASCII chars, but we don't care about them for parsing
       * anyway. */
      BufferedReader br = new BufferedReader(new StringReader(new String(
          data, "US-ASCII")));
      String line = br.readLine();
      if (line == null) {
        this.logger.fine("We were given an empty descriptor for "
            + "parsing. Ignoring.");
        return;
      }
      SimpleDateFormat parseFormat =
          new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      parseFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
      if (line.equals("network-status-version 3")) {
        // TODO when parsing the current consensus, check the fresh-until
        // time to see when we switch from hourly to half-hourly
        // consensuses
        boolean isConsensus = true;
        String validAfterTime = null;
        String dirSource = null;
        while ((line = br.readLine()) != null) {
          if (line.equals("vote-status vote")) {
            isConsensus = false;
          } else if (line.startsWith("valid-after ")) {
            validAfterTime = line.substring("valid-after ".length());
          } else if (line.startsWith("dir-source ")) {
            dirSource = line.split(" ")[2];
            break;
          }
        }
        if (isConsensus) {
          if (this.chc != null) {
            this.chc.processConsensus(validAfterTime, data);
          }
        } else {
          if (this.chc != null) {
            this.chc.processVote(validAfterTime, dirSource, data);
          }
        }
      }
    } catch (IOException e) {
      this.logger.log(Level.WARNING, "Could not parse descriptor. "
          + "Skipping.", e);
    }
  }
}

