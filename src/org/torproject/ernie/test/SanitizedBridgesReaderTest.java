/* Copyright 2011 The Tor Project
 * See LICENSE for licensing information */
package org.torproject.ernie.test;

import java.io.File;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.torproject.ernie.cron.SanitizedBridgesReader;

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

