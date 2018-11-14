/* Copyright 2017--2018 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.stats.servers;

import org.torproject.descriptor.BridgeNetworkStatus;
import org.torproject.descriptor.NetworkStatusEntry;
import org.torproject.descriptor.RelayNetworkStatusConsensus;
import org.torproject.descriptor.RelayServerDescriptor;
import org.torproject.descriptor.ServerDescriptor;

import org.apache.commons.lang3.StringUtils;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Parser that extracts all relevant parts from (relay and bridge) server
 * descriptors and (relay and bridge) statuses and creates data objects for
 * them. */
class Parser {

  private Pattern platformPattern = Pattern.compile("^Tor (.+) on (.+)$");

  /** Parse a (relay or bridge) server descriptor. */
  Ipv6ServerDescriptor parseServerDescriptor(
      ServerDescriptor serverDescriptor) {
    Ipv6ServerDescriptor parsedDescriptor = new Ipv6ServerDescriptor();
    parsedDescriptor.digest = serverDescriptor.getDigestSha1Hex();
    if (null != serverDescriptor.getPlatform()) {
      Matcher platformMatcher = platformPattern.matcher(
          serverDescriptor.getPlatform());
      if (platformMatcher.matches() && platformMatcher.groupCount() == 2) {
        parsedDescriptor.version = platformMatcher.group(1);
        parsedDescriptor.platform = platformMatcher.group(2);
      }
    }
    for (String orAddress : serverDescriptor.getOrAddresses()) {
      /* Check whether the additional OR address is an IPv6 address containing
       * at least two colons as opposed to an IPv4 address and TCP port
       * containing only one colon as separator. */
      if (StringUtils.countMatches(orAddress, ":") >= 2) {
        parsedDescriptor.announced = true;
        break;
      }
    }
    if (serverDescriptor instanceof RelayServerDescriptor) {
      parsedDescriptor.advertisedBandwidth =
          Math.min(serverDescriptor.getBandwidthRate(),
              serverDescriptor.getBandwidthBurst());
      if (serverDescriptor.getBandwidthObserved() >= 0) {
        parsedDescriptor.advertisedBandwidth =
            Math.min(parsedDescriptor.advertisedBandwidth,
                serverDescriptor.getBandwidthObserved());
      }
      parsedDescriptor.exiting
          = null != serverDescriptor.getIpv6DefaultPolicy()
          && !("reject".equals(serverDescriptor.getIpv6DefaultPolicy())
          && "1-65535".equals(serverDescriptor.getIpv6PortList()));
    }
    return parsedDescriptor;
  }

  Ipv6NetworkStatus parseRelayNetworkStatusConsensus(
      RelayNetworkStatusConsensus consensus) {
    Ipv6NetworkStatus parsedStatus = new Ipv6NetworkStatus();
    parsedStatus.isRelay = true;
    parsedStatus.timestamp = Instant.ofEpochMilli(
        consensus.getValidAfterMillis())
        .atZone(ZoneId.of("UTC")).toLocalDateTime();
    parsedStatus.recommendedVersions = consensus.getRecommendedServerVersions();
    boolean consensusContainsBandwidthWeights =
        null != consensus.getBandwidthWeights()
        && consensus.getBandwidthWeights().keySet().containsAll(Arrays.asList(
        "Wgg", "Wgd", "Wmg", "Wmm", "Wme", "Wmd", "Wee", "Wed"));
    float wgg = 0.0f;
    float wgd = 0.0f;
    float wmg = 0.0f;
    float wmm = 0.0f;
    float wme = 0.0f;
    float wmd = 0.0f;
    float wee = 0.0f;
    float wed = 0.0f;
    if (consensusContainsBandwidthWeights) {
      for (Map.Entry<String, Integer> e
          : consensus.getBandwidthWeights().entrySet()) {
        float weight = e.getValue().floatValue() / 10000.0f;
        switch (e.getKey()) {
          case "Wgg":
            wgg = weight;
            break;
          case "Wgd":
            wgd = weight;
            break;
          case "Wmg":
            wmg = weight;
            break;
          case "Wmm":
            wmm = weight;
            break;
          case "Wme":
            wme = weight;
            break;
          case "Wmd":
            wmd = weight;
            break;
          case "Wee":
            wee = weight;
            break;
          case "Wed":
            wed = weight;
            break;
          default:
            /* Ignore other weights. */
        }
      }
    }
    for (NetworkStatusEntry entry : consensus.getStatusEntries().values()) {
      if (!entry.getFlags().contains("Running")) {
        continue;
      }
      Ipv6NetworkStatus.Entry parsedEntry = new Ipv6NetworkStatus.Entry();
      parsedEntry.digest = entry.getDescriptor().toLowerCase();
      parsedEntry.flags = entry.getFlags();
      parsedEntry.reachable = false;
      for (String orAddress : entry.getOrAddresses()) {
        /* Check whether the additional OR address is an IPv6 address
         * containing at least two colons as opposed to an IPv4 address and
         * TCP port containing only one colon as separator. */
        if (StringUtils.countMatches(orAddress, ":") >= 2) {
          parsedEntry.reachable = true;
          break;
        }
      }
      parsedStatus.running++;
      boolean isExit = entry.getFlags().contains("Exit")
          && !entry.getFlags().contains("BadExit");
      boolean isGuard = entry.getFlags().contains("Guard");
      long consensusWeight = entry.getBandwidth();
      if (consensusWeight >= 0L) {
        parsedEntry.consensusWeight = (float) consensusWeight;
        if (consensusContainsBandwidthWeights) {
          if (isGuard && isExit) {
            parsedEntry.guardWeight = parsedEntry.consensusWeight * wgd;
            parsedEntry.middleWeight = parsedEntry.consensusWeight * wmd;
            parsedEntry.exitWeight = parsedEntry.consensusWeight * wed;
          } else if (isGuard) {
            parsedEntry.guardWeight = parsedEntry.consensusWeight * wgg;
            parsedEntry.middleWeight = parsedEntry.consensusWeight * wmg;
            parsedEntry.exitWeight = 0.0f;
          } else if (isExit) {
            parsedEntry.guardWeight = 0.0f;
            parsedEntry.middleWeight = parsedEntry.consensusWeight * wme;
            parsedEntry.exitWeight = parsedEntry.consensusWeight * wee;
          } else {
            parsedEntry.guardWeight = 0.0f;
            parsedEntry.middleWeight = parsedEntry.consensusWeight * wmm;
            parsedEntry.exitWeight = 0.0f;
          }
          if (null == parsedStatus.totalGuardWeight) {
            parsedStatus.totalGuardWeight = 0.0f;
            parsedStatus.totalMiddleWeight = 0.0f;
            parsedStatus.totalExitWeight = 0.0f;
          }
          parsedStatus.totalGuardWeight += parsedEntry.guardWeight;
          parsedStatus.totalMiddleWeight += parsedEntry.middleWeight;
          parsedStatus.totalExitWeight += parsedEntry.exitWeight;
        }
        if (null == parsedStatus.totalConsensusWeight) {
          parsedStatus.totalConsensusWeight = 0.0f;
        }
        parsedStatus.totalConsensusWeight += parsedEntry.consensusWeight;
      }
      parsedStatus.entries.add(parsedEntry);
    }
    return parsedStatus;
  }

  Ipv6NetworkStatus parseBridgeNetworkStatus(BridgeNetworkStatus status) {
    Ipv6NetworkStatus parsedStatus = new Ipv6NetworkStatus();
    parsedStatus.isRelay = false;
    parsedStatus.timestamp = Instant.ofEpochMilli(status.getPublishedMillis())
        .atZone(ZoneId.of("UTC")).toLocalDateTime();
    for (NetworkStatusEntry entry : status.getStatusEntries().values()) {
      if (!entry.getFlags().contains("Running")) {
        continue;
      }
      parsedStatus.running++;
      Ipv6NetworkStatus.Entry parsedEntry = new Ipv6NetworkStatus.Entry();
      parsedEntry.digest = entry.getDescriptor().toLowerCase();
      parsedEntry.flags = entry.getFlags();
      parsedStatus.entries.add(parsedEntry);
    }
    return parsedStatus;
  }
}

