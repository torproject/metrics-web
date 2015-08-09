/* Copyright 2015 The Tor Project
 * See LICENSE for licensing information */
package org.torproject.metrics.collectdescs;

import java.io.File;

import org.torproject.descriptor.DescriptorCollector;
import org.torproject.descriptor.DescriptorSourceFactory;

public class Main {
  public static void main(String[] args) {
    /* Fetch recent descriptors from CollecTor. */
    DescriptorCollector collector =
        DescriptorSourceFactory.createDescriptorCollector();
    collector.collectDescriptors(
        "https://collector.torproject.org", new String[] {
        "/recent/bridge-descriptors/extra-infos/",
        "/recent/bridge-descriptors/server-descriptors/",
        "/recent/bridge-descriptors/statuses/",
        "/recent/exit-lists/",
        "/recent/relay-descriptors/consensuses/",
        "/recent/relay-descriptors/extra-infos/",
        "/recent/relay-descriptors/server-descriptors/",
        "/recent/torperf/" }, 0L, new File("../../shared/in"), true);
  }
}

