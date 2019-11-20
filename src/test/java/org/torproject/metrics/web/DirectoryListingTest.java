/* Copyright 2017--2018 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.torproject.descriptor.index.IndexNode;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.util.Arrays;

public class DirectoryListingTest {

  @Test
  public void testFormatBytes() {
    long[] input = new long[] { -1024L, -1L, 0L, 1L, 1023L, // B
        1024L, 1025L, 1048575L, // KiB
        1048576L, 1048577L, 1073741823L, // MiB
        1073741824L, 1073741825L, 1099511627775L, // GiB
        32099511627776L, 1099511627777L, 1125899906842623L, // TiB
        1125899906842624L, 1125899906842625L }; // PiB
    String[] expectedOutput = new String[] { "-1024 B", "-1 B", "0 B", "1 B",
        "1023 B", "1.0 KiB", "1.0 KiB", "1024.0 KiB", "1.0 MiB", "1.0 MiB",
        "1024.0 MiB", "1.0 GiB", "1.0 GiB", "1024.0 GiB", "29.2 TiB", "1.0 TiB",
        "1.0 PiB", // <- Would have expected 1024.0 TiB, but who cares?
        "1.0 PiB", "1.0 PiB" };
    assertEquals(expectedOutput.length, input.length);
    for (int i = 0; i < input.length; i++) {
      assertEquals("Mismatch for input " + input[i], expectedOutput[i],
          DirectoryListing.formatBytes(input[i]));
    }
  }

  private static final String jsonIndex
      = "{\"index_created\":\"2016-02-02 00:02\","
      + "\"path\":\"https://some.collector.url\","
      + "\"directories\":[{\"path\":\"a1\","
      + "\"directories\":[{\"path\":\"p1\","
      + "\"files\":[{\"path\":\"file1\",\"size\":624156,"
      + "\"last_modified\":\"2012-01-01 13:13\"},{\"path\":\"file2\","
      + "\"size\":1010648,"
      + "\"last_modified\":\"2012-02-02 14:14\"}]},{\"path\":\"p2\","
      + "\"files\":[{\"path\":\"file3\",\"size\":624156,"
      + "\"last_modified\":\"2012-03-03 15:15\"}]}]}]}";

  @Test
  public void testListing() throws Exception {
    DirectoryListing dl = new DirectoryListing(IndexNode
        .fetchIndex(new ByteArrayInputStream(jsonIndex.getBytes())));
    assertEquals(4, dl.size());
    for (String key : new String[]{"/collector/a1/", "/collector/",
        "/collector/a1/p2/", "/collector/a1/p1/"}) {
      assertTrue("Missing: " + key, dl.containsKey(key));
    }
    assertEquals("[Parent Directory, /collector.html, , ]",
        Arrays.toString(dl.get("/collector/").get(0)));
    assertEquals(3, dl.get("/collector/a1/").size());
    assertEquals("[Parent Directory, /collector/, , ]",
        Arrays.toString(dl.get("/collector/a1/").get(0)));
    assertEquals("[p1, /collector/a1/p1/, , ]",
        Arrays.toString(dl.get("/collector/a1/").get(1)));
    assertEquals("[p2, /collector/a1/p2/, , ]",
        Arrays.toString(dl.get("/collector/a1/").get(2)));
    assertEquals("[Parent Directory, /collector/a1/, , ]",
        Arrays.toString(dl.get("/collector/a1/p1/").get(0)));
    assertEquals(2, dl.get("/collector/a1/p2/").size());
    assertEquals("[file3, https://some.collector.url/a1/p2/file3, "
        + "2012-03-03 15:15, 609.5 KiB]",
        Arrays.toString(dl.get("/collector/a1/p2/").get(1)));
  }
}

