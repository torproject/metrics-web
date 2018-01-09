/* Copyright 2017--2018 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.stats.ipv6servers;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.torproject.descriptor.Descriptor;
import org.torproject.descriptor.DescriptorSourceFactory;
import org.torproject.descriptor.ServerDescriptor;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class Ipv6ServerDescriptorTest {

  /** Provide test data. */
  @Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] {
        { "Relay server descriptor without or-address or ipv6-policy line.",
            "ipv6servers/0018ab4f2f28af683d52f06407edbf7ce1bd3b7d",
            819200, false, false },
        { "Relay server descriptor with or-address and ipv6-policy line.",
            "ipv6servers/01003df74972ce952ebfa390f468ef63c50efa25",
            6576128, true, true },
        { "Relay server descriptor with or-address line only.",
            "ipv6servers/018c1229d5f56eebfc1d709d4692673d098800e8",
            0, true, false },
        { "Bridge server descriptor without or-address or ipv6-policy line.",
            "ipv6servers/000a7fe20a17bf5d9839a126b1dff43f998aac6f",
            0, false, false },
        { "Bridge server descriptor with or-address line.",
            "ipv6servers/0041dbf9fe846f9765882f7dc8332f94b709e35a",
            0, true, false },
        { "Bridge server descriptor with (ignored) ipv6-policy accept line.",
            "ipv6servers/64dd486d89af14027c9a7b4347a94b74dddb5cdb",
            0, false, false }
    });
  }

  @Parameter
  public String description;

  @Parameter(1)
  public String fileName;

  @Parameter(2)
  public int advertisedBandwidth;

  @Parameter(3)
  public boolean announced;

  @Parameter(4)
  public boolean exiting;

  @Test
  public void testParseServerDescriptor() throws Exception {
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
      assertTrue(this.description, descriptor instanceof ServerDescriptor);
      Ipv6ServerDescriptor parsedServerDescriptor
          = new Parser().parseServerDescriptor((ServerDescriptor) descriptor);
      assertEquals(this.description, this.advertisedBandwidth,
          parsedServerDescriptor.advertisedBandwidth);
      assertEquals(this.description, this.announced,
          parsedServerDescriptor.announced);
      assertEquals(this.description, this.exiting,
          parsedServerDescriptor.exiting);
    }
  }
}

