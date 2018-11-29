/* Copyright 2018 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.stats.totalcw;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNull;

import org.torproject.descriptor.Descriptor;
import org.torproject.descriptor.DescriptorSourceFactory;
import org.torproject.descriptor.RelayNetworkStatusConsensus;
import org.torproject.descriptor.RelayNetworkStatusVote;

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
public class TotalcwRelayNetworkStatusTest {

  /** Provide test data. */
  @Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] {
        { "2018-10-15-00-00-00-consensus.part",
            ZonedDateTime.parse("2018-10-15T00:00:00Z").toLocalDateTime(),
            null, null, new long[] { 16774L, 44820L, 26600L, 49500L } },
        { "2018-10-15-00-00-00-vote-0232AF901C31A04EE9848595AF9BB7620D4C5B2E-"
            + "55A38ED50848BE1F13C6A35C3CA637B0D962C2EF.part",
            ZonedDateTime.parse("2018-10-15T00:00:00Z").toLocalDateTime(),
            "dannenberg", "0232AF901C31A04EE9848595AF9BB7620D4C5B2E", null },
        { "2018-10-15-00-00-00-vote-27102BC123E7AF1D4741AE047E160C91ADC76B21-"
            + "049AB3179B12DACC391F06A10C2A8904E4339D33.part",
            ZonedDateTime.parse("2018-10-15T00:00:00Z").toLocalDateTime(),
            "bastet", "27102BC123E7AF1D4741AE047E160C91ADC76B21",
            new long[] { 13700L, 47220L, 17080L, 60700L } },
        { "2018-10-15-00-00-00-vote-ED03BB616EB2F60BEC80151114BB25CEF515B226-"
            + "2669AD153408F88E416CE6206D1A75EC3324A2F4.part",
            ZonedDateTime.parse("2018-10-15T00:00:00Z").toLocalDateTime(),
            "gabelmoo", "ED03BB616EB2F60BEC80151114BB25CEF515B226",
            new long[] { 18020L, 43200L, 26150L, 46000L } },
        { "2018-10-15-00-00-00-vote-EFCBE720AB3A82B99F9E953CD5BF50F7EEFC7B97-"
            + "38C6A19F78948B689345EE41D7119D76246C4D3E.part",
            ZonedDateTime.parse("2018-10-15T00:00:00Z").toLocalDateTime(),
            "Faravahar", "EFCBE720AB3A82B99F9E953CD5BF50F7EEFC7B97",
            new long[] { 17365L, 52030L, 35400L, 53600L } }
    });
  }

  @Parameter
  public String fileName;

  @Parameter(1)
  public LocalDateTime expectedValidAfter;

  @Parameter(2)
  public String expectedNickname;

  @Parameter(3)
  public String expectedIdentityHex;

  @Parameter(4)
  public long[] expectedMeasuredSums;

  @Test
  public void testParseVote() throws Exception {
    InputStream is = getClass().getClassLoader().getResourceAsStream(
        "totalcw/" + this.fileName);
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
      TotalcwRelayNetworkStatus parsedStatus;
      if (descriptor instanceof RelayNetworkStatusConsensus) {
        parsedStatus = new Parser().parseRelayNetworkStatusConsensus(
            (RelayNetworkStatusConsensus) descriptor);
      } else {
        parsedStatus = new Parser().parseRelayNetworkStatusVote(
            (RelayNetworkStatusVote) descriptor);
      }
      if (null == this.expectedMeasuredSums) {
        assertNull(parsedStatus);
      } else {
        assertEquals(this.expectedValidAfter, parsedStatus.validAfter);
        assertEquals(this.expectedNickname, parsedStatus.nickname);
        assertEquals(this.expectedIdentityHex, parsedStatus.identityHex);
        assertArrayEquals(this.expectedMeasuredSums, parsedStatus.measuredSums);
      }
    }
  }
}
