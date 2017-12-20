/* Copyright 2017 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.stats.ipv6servers;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertNotNull;

import org.torproject.descriptor.BridgeNetworkStatus;
import org.torproject.descriptor.Descriptor;
import org.torproject.descriptor.DescriptorSourceFactory;
import org.torproject.descriptor.RelayNetworkStatusConsensus;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class ParsedNetworkStatusTest {

  /** Provide test data. */
  @Parameters
  public static Collection<Object[]> data() {
    String relayFileName = "ipv6servers/2017-12-04-20-00-00-consensus.part";
    String bridgeFileName = "ipv6servers/"
        + "20171204-190507-1D8F3A91C37C5D1C4C19B1AD1D0CFBE8BF72D8E1.part";
    return Arrays.asList(new Object[][] {
        { "Relay status without Guard or Exit flag and without IPv6 address. ",
            relayFileName, true,
            ZonedDateTime.parse("2017-12-04T20:00:00Z").toLocalDateTime(), 3,
            "19bd830ae419b4c6ea1047370d0a5ac446f1748d", false, false, false },
        { "Relay status with Guard and Exit flag and without IPv6 address.",
            relayFileName, true,
            ZonedDateTime.parse("2017-12-04T20:00:00Z").toLocalDateTime(), 3,
            "600a614a5ee63f8cb55aa3d4d9e9a8dd8d748d77", true, true, false },
        { "Relay status with Guard flag only and with IPv6 address.",
            relayFileName, true,
            ZonedDateTime.parse("2017-12-04T20:00:00Z").toLocalDateTime(), 3,
            "d993e03f907f7cb302a877feb7608cbd6c4cfeb0", true, false, true },
        { "Bridge status with Running flag.",
            bridgeFileName, false,
            ZonedDateTime.parse("2017-12-04T19:05:07Z").toLocalDateTime(), 1,
            "01b2cadfbcc0ebe50f395863665ac376d25f08ed", false, false, false },
        { "Bridge status without Running flag (skipped!).",
            bridgeFileName, false,
            ZonedDateTime.parse("2017-12-04T19:05:07Z").toLocalDateTime(), 1,
            null, false, false, false }
    });
  }

  @Parameter
  public String description;

  @Parameter(1)
  public String fileName;

  @Parameter(2)
  public boolean isRelay;

  @Parameter(3)
  public LocalDateTime timestamp;

  @Parameter(4)
  public int running;

  @Parameter(5)
  public String digest;

  @Parameter(6)
  public boolean guard;

  @Parameter(7)
  public boolean exit;

  @Parameter(8)
  public boolean reachable;

  @Test
  public void testParseNetworkStatus() throws Exception {
    InputStream is = getClass().getClassLoader().getResourceAsStream(
        this.fileName);
    assertNotNull(this.description, is);
    StringBuilder sb = new StringBuilder();
    BufferedReader br = new BufferedReader(new InputStreamReader(is));
    String line = br.readLine();
    while (null != line) {
      sb.append(line).append('\n');
      line = br.readLine();
    }
    for (Descriptor descriptor
        : DescriptorSourceFactory.createDescriptorParser().parseDescriptors(
        sb.toString().getBytes(), new File(this.fileName), this.fileName)) {
      ParsedNetworkStatus parsedNetworkStatus;
      if (descriptor instanceof RelayNetworkStatusConsensus) {
        parsedNetworkStatus = new Parser().parseRelayNetworkStatusConsensus(
            (RelayNetworkStatusConsensus) descriptor);
      } else if (descriptor instanceof BridgeNetworkStatus) {
        parsedNetworkStatus = new Parser().parseBridgeNetworkStatus(
            (BridgeNetworkStatus) descriptor);
      } else {
        fail(this.description);
        return;
      }
      assertEquals(this.description, this.isRelay, parsedNetworkStatus.isRelay);
      assertEquals(this.description, this.timestamp,
          parsedNetworkStatus.timestamp);
      assertEquals(this.description, this.running, parsedNetworkStatus.running);
      if (null != this.digest) {
        boolean foundEntry = false;
        for (ParsedNetworkStatus.Entry parsedEntry
            : parsedNetworkStatus.entries) {
          if (this.digest.equals(parsedEntry.digest)) {
            assertEquals(this.description, this.guard, parsedEntry.guard);
            assertEquals(this.description, this.exit, parsedEntry.exit);
            assertEquals(this.description, this.reachable,
                parsedEntry.reachable);
            foundEntry = true;
            break;
          }
        }
        if (!foundEntry) {
          fail(this.description);
        }
      }
    }
  }
}

