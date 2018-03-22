/* Copyright 2015--2018 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.stats.collectdescs;

import org.torproject.descriptor.DescriptorCollector;
import org.torproject.descriptor.DescriptorSourceFactory;

import java.io.File;

public class Main {

  /** Executes this data-processing module. */
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
            "/recent/torperf/",
            "/recent/webstats/"
        }, 0L, new File("../../shared/in"), true);
  }
}

