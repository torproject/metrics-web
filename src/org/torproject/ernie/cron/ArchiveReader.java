/* Copyright 2011, 2012 The Tor Project
 * See LICENSE for licensing information */
package org.torproject.ernie.cron;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Logger;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.torproject.descriptor.Descriptor;
import org.torproject.descriptor.DescriptorFile;
import org.torproject.descriptor.DescriptorReader;
import org.torproject.descriptor.DescriptorSourceFactory;
import org.torproject.descriptor.ExtraInfoDescriptor;
import org.torproject.descriptor.NetworkStatusEntry;
import org.torproject.descriptor.RelayNetworkStatusConsensus;
import org.torproject.descriptor.RelayNetworkStatusVote;
import org.torproject.descriptor.ServerDescriptor;

/**
 * Read in all files in a given directory and pass buffered readers of
 * them to the relay descriptor parser.
 */
public class ArchiveReader {

  /**
   * Stats file handler that accepts parse results for bridge statistics.
   */
  private BridgeStatsFileHandler bsfh;

  /**
   * Relay descriptor database importer that stores relay descriptor
   * contents for later evaluation.
   */
  private RelayDescriptorDatabaseImporter rddi;

  /**
   * Logger for this class.
   */
  private Logger logger;

  private SimpleDateFormat dateTimeFormat;

  public ArchiveReader(RelayDescriptorDatabaseImporter rddi,
      BridgeStatsFileHandler bsfh, File archivesDirectory,
      File statsDirectory, boolean keepImportHistory) {

    if (archivesDirectory == null ||
        statsDirectory == null) {
      throw new IllegalArgumentException();
    }

    this.rddi = rddi;
    this.bsfh = bsfh;

    this.dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    this.dateTimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

    int parsedFiles = 0, ignoredFiles = 0;
    this.logger = Logger.getLogger(ArchiveReader.class.getName());
    if (archivesDirectory.exists()) {
      logger.fine("Importing files in directory " + archivesDirectory
          + "/...");
      DescriptorReader reader =
          DescriptorSourceFactory.createDescriptorReader();
      reader.addDirectory(archivesDirectory);
      if (keepImportHistory) {
        reader.setExcludeFiles(new File(statsDirectory,
            "relay-descriptor-history"));
      }
      Iterator<DescriptorFile> descriptorFiles = reader.readDescriptors();
      while (descriptorFiles.hasNext()) {
        DescriptorFile descriptorFile = descriptorFiles.next();
        if (descriptorFile.getDescriptors() != null) {
          for (Descriptor descriptor : descriptorFile.getDescriptors()) {
            if (descriptor instanceof RelayNetworkStatusConsensus) {
              this.addRelayNetworkStatusConsensus(
                  (RelayNetworkStatusConsensus) descriptor);
            } else if (descriptor instanceof RelayNetworkStatusVote) {
              this.addRelayNetworkStatusVote(
                  (RelayNetworkStatusVote) descriptor);
            } else if (descriptor instanceof ServerDescriptor) {
              this.addServerDescriptor((ServerDescriptor) descriptor);
            } else if (descriptor instanceof ExtraInfoDescriptor) {
              this.addExtraInfoDescriptor(
                  (ExtraInfoDescriptor) descriptor);
            }
          }
        }
      }
    }

    logger.info("Finished importing relay descriptors from local "
        + "directory:\nParsed " + parsedFiles + ", ignored "
        + ignoredFiles + " files.");
  }

  private void addRelayNetworkStatusConsensus(
      RelayNetworkStatusConsensus consensus) {
    for (NetworkStatusEntry statusEntry :
      consensus.getStatusEntries().values()) {
      this.rddi.addStatusEntry(consensus.getValidAfterMillis(),
          statusEntry.getNickname(), statusEntry.getFingerprint(),
          statusEntry.getDescriptor(), statusEntry.getPublishedMillis(),
          statusEntry.getAddress(), statusEntry.getOrPort(),
          statusEntry.getDirPort(), statusEntry.getFlags(),
          statusEntry.getVersion(), statusEntry.getBandwidth(),
          statusEntry.getPortList(), statusEntry.getStatusEntryBytes());
      try {
        this.bsfh.addHashedRelay(DigestUtils.shaHex(Hex.decodeHex(
            statusEntry.getFingerprint().toCharArray())).toUpperCase());
      } catch (DecoderException e) {
      }
    }
    this.rddi.addConsensus(consensus.getValidAfterMillis(),
        consensus.getRawDescriptorBytes());
  }

  private void addRelayNetworkStatusVote(RelayNetworkStatusVote vote) {
    this.rddi.addVote(vote.getValidAfterMillis(), vote.getIdentity(),
        vote.getRawDescriptorBytes());
  }

  private void addServerDescriptor(ServerDescriptor descriptor) {
    String digest = null;
    try {
      String ascii = new String(descriptor.getRawDescriptorBytes(),
          "US-ASCII");
      String startToken = "router ";
      String sigToken = "\nrouter-signature\n";
      int start = ascii.indexOf(startToken);
      int sig = ascii.indexOf(sigToken) + sigToken.length();
      if (start >= 0 || sig >= 0 || sig > start) {
        byte[] forDigest = new byte[sig - start];
        System.arraycopy(descriptor.getRawDescriptorBytes(), start,
            forDigest, 0, sig - start);
        digest = DigestUtils.shaHex(forDigest);
      }
    } catch (UnsupportedEncodingException e) {
    }
    if (digest != null) {
      this.rddi.addServerDescriptor(digest, descriptor.getNickname(),
          descriptor.getAddress(), descriptor.getOrPort(),
          descriptor.getDirPort(), descriptor.getFingerprint(),
          descriptor.getBandwidthRate(), descriptor.getBandwidthBurst(),
          descriptor.getBandwidthObserved(), descriptor.getPlatform(),
          descriptor.getPublishedMillis(), descriptor.getUptime(),
          descriptor.getExtraInfoDigest(),
          descriptor.getRawDescriptorBytes());
    }
  }

  private void addExtraInfoDescriptor(ExtraInfoDescriptor descriptor) {
    if (descriptor.getDirreqV3Reqs() != null) {
      int allUsers = 0;
      Map<String, String> obs = new HashMap<String, String>();
      for (Map.Entry<String, Integer> e :
          descriptor.getDirreqV3Reqs().entrySet()) {
        String country = e.getKey();
        int users = e.getValue() - 4;
        allUsers += users;
        obs.put(country, "" + users);
      }
      obs.put("zy", "" + allUsers);
      this.rddi.addDirReqStats(descriptor.getFingerprint(),
          descriptor.getDirreqStatsEndMillis(),
          descriptor.getDirreqStatsIntervalLength(), obs);
    }
    if (descriptor.getConnBiDirectStatsEndMillis() >= 0L) {
      this.rddi.addConnBiDirect(descriptor.getFingerprint(),
          descriptor.getConnBiDirectStatsEndMillis(),
          descriptor.getConnBiDirectStatsIntervalLength(),
          descriptor.getConnBiDirectBelow(),
          descriptor.getConnBiDirectRead(),
          descriptor.getConnBiDirectWrite(),
          descriptor.getConnBiDirectBoth());
    }
    List<String> bandwidthHistoryLines = new ArrayList<String>();
    if (descriptor.getWriteHistory() != null) {
      bandwidthHistoryLines.add(descriptor.getWriteHistory().getLine());
    }
    if (descriptor.getReadHistory() != null) {
      bandwidthHistoryLines.add(descriptor.getReadHistory().getLine());
    }
    if (descriptor.getDirreqWriteHistory() != null) {
      bandwidthHistoryLines.add(
          descriptor.getDirreqWriteHistory().getLine());
    }
    if (descriptor.getDirreqReadHistory() != null) {
      bandwidthHistoryLines.add(
          descriptor.getDirreqReadHistory().getLine());
    }
    String digest = null;
    try {
      String ascii = new String(descriptor.getRawDescriptorBytes(),
          "US-ASCII");
      String startToken = "extra-info ";
      String sigToken = "\nrouter-signature\n";
      int start = ascii.indexOf(startToken);
      int sig = ascii.indexOf(sigToken) + sigToken.length();
      if (start >= 0 || sig >= 0 || sig > start) {
        byte[] forDigest = new byte[sig - start];
        System.arraycopy(descriptor.getRawDescriptorBytes(), start,
            forDigest, 0, sig - start);
        digest = DigestUtils.shaHex(forDigest);
      }
    } catch (UnsupportedEncodingException e) {
    }
    if (digest != null) {
      this.rddi.addExtraInfoDescriptor(digest, descriptor.getNickname(),
          descriptor.getFingerprint().toLowerCase(),
          descriptor.getPublishedMillis(),
          descriptor.getRawDescriptorBytes(), bandwidthHistoryLines);
    }
  }
}

