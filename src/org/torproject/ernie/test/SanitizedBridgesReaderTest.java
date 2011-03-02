/* Copyright 2011 The Tor Project
 * See LICENSE for licensing information */
package org.torproject.ernie.test;

import org.torproject.ernie.cron.*;

import java.io.*;

import org.junit.*;
import org.junit.rules.*;
import static org.junit.Assert.*;

public class SanitizedBridgesReaderTest {

  private File tempSanitizedBridgesDirectory;
  private File tempStatsDirectory;

  @Rule
  public TemporaryFolder folder = new TemporaryFolder();

  @Before
  public void createTempDirectories() {
    this.tempSanitizedBridgesDirectory = folder.newFolder("bridges");
    this.tempStatsDirectory = folder.newFolder("stats");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBridgeDescriptorParserNull() {
    new SanitizedBridgesReader(null, this.tempSanitizedBridgesDirectory,
        this.tempStatsDirectory, false);
  }
}

