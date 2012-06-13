/* Copyright 2011, 2012 The Tor Project
 * See LICENSE for licensing information */
package org.torproject.ernie.cron;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

import org.torproject.descriptor.BridgeNetworkStatus;
import org.torproject.descriptor.Descriptor;
import org.torproject.descriptor.DescriptorFile;
import org.torproject.descriptor.DescriptorReader;
import org.torproject.descriptor.DescriptorSourceFactory;
import org.torproject.descriptor.ExtraInfoDescriptor;
import org.torproject.descriptor.NetworkStatusEntry;
import org.torproject.descriptor.ServerDescriptor;

public class SanitizedBridgesReader {
  private ConsensusStatsFileHandler csfh;
  private BridgeStatsFileHandler bsfh;
  private Logger logger;
  public SanitizedBridgesReader(ConsensusStatsFileHandler csfh,
      BridgeStatsFileHandler bsfh, File bridgesDir, File statsDirectory,
      boolean keepImportHistory) {

    if (csfh == null || bsfh == null || bridgesDir == null ||
        statsDirectory == null) {
      throw new IllegalArgumentException();
    }

    this.csfh = csfh;
    this.bsfh = bsfh;
    this.logger =
        Logger.getLogger(SanitizedBridgesReader.class.getName());

    if (bridgesDir.exists()) {
      logger.fine("Importing files in directory " + bridgesDir + "/...");
      DescriptorReader reader =
          DescriptorSourceFactory.createDescriptorReader();
      reader.addDirectory(bridgesDir);
      if (keepImportHistory) {
        reader.setExcludeFiles(new File(statsDirectory,
            "bridge-descriptor-history"));
      }
      Iterator<DescriptorFile> descriptorFiles = reader.readDescriptors();
      while (descriptorFiles.hasNext()) {
        DescriptorFile descriptorFile = descriptorFiles.next();
        if (descriptorFile.getDescriptors() != null) {
          for (Descriptor descriptor : descriptorFile.getDescriptors()) {
            if (descriptor instanceof BridgeNetworkStatus) {
              this.addBridgeNetworkStatus(
                  (BridgeNetworkStatus) descriptor);
            } else if (descriptor instanceof ServerDescriptor) {
              this.addServerDescriptor((ServerDescriptor) descriptor);
            } else if (descriptor instanceof ExtraInfoDescriptor) {
              this.addExtraInfoDescriptor(
                  (ExtraInfoDescriptor) descriptor);
            }
          }
        }
      }
      logger.info("Finished importing bridge descriptors.");
    }
  }

  private void addBridgeNetworkStatus(BridgeNetworkStatus status) {
    int runningBridges = 0, runningEc2Bridges = 0;
    for (NetworkStatusEntry statusEntry :
        status.getStatusEntries().values()) {
      if (statusEntry.getFlags().contains("Running")) {
        runningBridges++;
        if (statusEntry.getNickname().startsWith("ec2bridge")) {
          runningEc2Bridges++;
        }
      }
    }
    this.csfh.addBridgeConsensusResults(status.getPublishedMillis(),
        runningBridges, runningEc2Bridges);
  }

  private void addServerDescriptor(ServerDescriptor descriptor) {
    if (descriptor.getPlatform() != null &&
        descriptor.getPlatform().startsWith("Tor 0.2.2")) {
      this.bsfh.addZeroTwoTwoDescriptor(descriptor.getFingerprint(),
          descriptor.getPublishedMillis());
    }
  }

  private void addExtraInfoDescriptor(ExtraInfoDescriptor descriptor) {
    if (!this.bsfh.isKnownRelay(descriptor.getFingerprint())) {
      if (descriptor.getGeoipStartTimeMillis() >= 0 &&
          descriptor.getGeoipClientOrigins() != null) {
        long seconds = (descriptor.getPublishedMillis()
            - descriptor.getGeoipStartTimeMillis()) / 1000L;
        double allUsers = 0.0D;
        Map<String, String> obs = new HashMap<String, String>();
        for (Map.Entry<String, Integer> e :
            descriptor.getGeoipClientOrigins().entrySet()) {
          String country = e.getKey();
          double users = ((double) e.getValue() - 4) * 86400.0D
              / ((double) seconds);
          allUsers += users;
          obs.put(country, String.format("%.2f", users));
        }
        obs.put("zy", String.format("%.2f", allUsers));
        this.bsfh.addObs(descriptor.getFingerprint(),
            descriptor.getPublishedMillis(), obs);
      }
      if (descriptor.getBridgeStatsEndMillis() >= 0 &&
          descriptor.getBridgeIps() != null) {
        double allUsers = 0.0D;
        Map<String, String> obs = new HashMap<String, String>();
        for (Map.Entry<String, Integer> e :
            descriptor.getBridgeIps().entrySet()) {
          String country = e.getKey();
          double users = (double) e.getValue() - 4;
          allUsers += users;
          obs.put(country, String.format("%.2f", users));
        }
        obs.put("zy", String.format("%.2f", allUsers));
        this.bsfh.addObs(descriptor.getFingerprint(),
            descriptor.getBridgeStatsEndMillis(), obs);
      }

    }
  }
}

