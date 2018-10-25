/* Copyright 2018 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.stats.totalcw;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNull;

import org.torproject.descriptor.Descriptor;
import org.torproject.descriptor.DescriptorSourceFactory;
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
public class TotalcwRelayNetworkStatusVoteTest {

  /** Provide test data. */
  @Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] {
        { "2018-10-15-00-00-00-vote-0232AF901C31A04EE9848595AF9BB7620D4C5B2E-"
            + "55A38ED50848BE1F13C6A35C3CA637B0D962C2EF.part",
            ZonedDateTime.parse("2018-10-15T00:00:00Z").toLocalDateTime(),
            "dannenberg", "0232AF901C31A04EE9848595AF9BB7620D4C5B2E",
            0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L },
        { "2018-10-15-00-00-00-vote-27102BC123E7AF1D4741AE047E160C91ADC76B21-"
            + "049AB3179B12DACC391F06A10C2A8904E4339D33.part",
            ZonedDateTime.parse("2018-10-15T00:00:00Z").toLocalDateTime(),
            "bastet", "27102BC123E7AF1D4741AE047E160C91ADC76B21",
            20L, 138803L, 6940L, 5L, 76L, 2490L, 9732L, 34800L },
        { "2018-10-15-00-00-00-vote-ED03BB616EB2F60BEC80151114BB25CEF515B226-"
            + "2669AD153408F88E416CE6206D1A75EC3324A2F4.part",
            ZonedDateTime.parse("2018-10-15T00:00:00Z").toLocalDateTime(),
            "gabelmoo", "ED03BB616EB2F60BEC80151114BB25CEF515B226",
            19, 133441L, 7023L, 2L, 153L, 3920L, 11030L, 31600L },
        { "2018-10-15-00-00-00-vote-EFCBE720AB3A82B99F9E953CD5BF50F7EEFC7B97-"
            + "38C6A19F78948B689345EE41D7119D76246C4D3E.part",
            ZonedDateTime.parse("2018-10-15T00:00:00Z").toLocalDateTime(),
            "Faravahar", "EFCBE720AB3A82B99F9E953CD5BF50F7EEFC7B97",
            20, 158534L, 7926L, 6L, 109L, 3215L, 9582L, 40700L }
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
  public long expectedMeasuredCount;

  @Parameter(5)
  public long expectedMeasuredSum;

  @Parameter(6)
  public long expectedMeasuredMean;

  @Parameter(7)
  public long expectedMeasuredMin;

  @Parameter(8)
  public long expectedMeasuredQ1;

  @Parameter(9)
  public long expectedMeasuredMedian;

  @Parameter(10)
  public long expectedMeasuredQ3;

  @Parameter(11)
  public long expectedMeasuredMax;

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
      TotalcwRelayNetworkStatusVote parsedVote = new Parser()
          .parseRelayNetworkStatusVote((RelayNetworkStatusVote) descriptor);
      if (0L == expectedMeasuredCount) {
        assertNull(parsedVote);
      } else {
        assertEquals(this.expectedValidAfter, parsedVote.validAfter);
        assertEquals(this.expectedNickname, parsedVote.nickname);
        assertEquals(this.expectedIdentityHex, parsedVote.identityHex);
        assertEquals(this.expectedMeasuredCount, parsedVote.measuredCount);
        assertEquals(this.expectedMeasuredSum, parsedVote.measuredSum);
        assertEquals(this.expectedMeasuredMean, parsedVote.measuredMean);
        assertEquals(this.expectedMeasuredMin, parsedVote.measuredMin);
        assertEquals(this.expectedMeasuredQ1, parsedVote.measuredQ1);
        assertEquals(this.expectedMeasuredMedian, parsedVote.measuredMedian);
        assertEquals(this.expectedMeasuredQ3, parsedVote.measuredQ3);
        assertEquals(this.expectedMeasuredMax, parsedVote.measuredMax);
      }
    }
  }
}
