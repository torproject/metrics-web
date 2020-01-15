/* Copyright 2017--2020 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.stats.servers;

/** Data object holding all relevant parts parsed from a (relay or bridge)
 * server descriptor. */
class Ipv6ServerDescriptor {

  /** Hex-encoded SHA-1 server descriptor digest. */
  String digest;

  /** Platform obtained from the platform line without the Tor software
   * version. */
  String platform;

  /** Tor software version obtained from the platform line without the
   * platform. */
  String version;

  /** Advertised bandwidth bytes of this relay as the minimum of bandwidth rate,
   * bandwidth burst, and observed bandwidth (if reported); 0 for bridges. */
  int advertisedBandwidth;

  /** Whether the relay or bridge announced an IPv6 address in an "or-address"
   * line. */
  boolean announced;

  /** Whether the relay allows exiting via IPv6, which is the case if the
   * server descriptor contains an "ipv6-policy" line that is not
   * "ipv6-policy reject 1-65535"; false for bridges. */
  boolean exiting;
}

