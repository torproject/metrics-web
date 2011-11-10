/* Copyright 2011 The Tor Project
 * See LICENSE for licensing information */
package org.torproject.chc;

/* Warning about irregularities in parsed consensuses and votes. */
public enum Warning {

  /* No consensus is known that can be checked. */
  NoConsensusKnown,

  /* One or more directory authorities did not return a consensus within a
   * timeout of 60 seconds. */
  ConsensusDownloadTimeout,

  /* One or more directory authorities published a consensus that is more
   * than 1 hour old and therefore not fresh anymore. */
  ConsensusNotFresh,

  /* One or more directory authorities does not support the consensus
   * method that the consensus uses. */
  ConsensusMethodNotSupported,

  /* One or more directory authorities recommends different client
   * versions than the ones in the consensus. */
  DifferentRecommendedClientVersions,

  /* One or more directory authorities recommends different server
   * versions than the ones in the consensus. */
  DifferentRecommendedServerVersions,

  /* One or more directory authorities set conflicting or invalid
   * consensus parameters. */
  ConflictingOrInvalidConsensusParams,

  /* The certificate(s) of one or more directory authorities expire within
   * the next 14 days. */
  CertificateExpiresSoon,

  /* The vote(s) of one or more directory authorities are missing. */
  VotesMissing,

  /* One or more directory authorities are not reporting bandwidth scanner
   * results. */
  BandwidthScannerResultsMissing
}

