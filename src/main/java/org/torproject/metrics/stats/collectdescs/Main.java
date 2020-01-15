/* Copyright 2015--2020 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.stats.collectdescs;

import org.torproject.descriptor.DescriptorCollector;
import org.torproject.descriptor.DescriptorSourceFactory;

import java.io.File;

public class Main {

  private static final File baseDir = new File(
      org.torproject.metrics.stats.main.Main.modulesDir, "collectdescs");

  /** Executes this data-processing module. */
  public static void main(String[] args) {
    /* Fetch recent descriptors from CollecTor. */
    DescriptorCollector collector =
        DescriptorSourceFactory.createDescriptorCollector();
    collector.collectDescriptors(
        "https://collector.torproject.org", new String[] {
            "/recent/bridgedb-metrics/",
            "/recent/bridge-descriptors/extra-infos/",
            "/recent/bridge-descriptors/server-descriptors/",
            "/recent/bridge-descriptors/statuses/",
            "/recent/exit-lists/",
            "/recent/relay-descriptors/consensuses/",
            "/recent/relay-descriptors/extra-infos/",
            "/recent/relay-descriptors/server-descriptors/",
            "/recent/relay-descriptors/votes/",
            "/recent/torperf/",
            "/recent/webstats/"
        }, 0L, org.torproject.metrics.stats.main.Main.descriptorsDir, true);
  }
}

