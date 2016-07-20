/* Copyright 2011--2016 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.ernie.cron;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Initialize configuration with hard-coded defaults, overwrite with
 * configuration in config file, if exists, and answer Main.java about our
 * configuration.
 */
public class Configuration {

  private boolean importDirectoryArchives = false;

  private List<String> directoryArchivesDirectories =
      new ArrayList<String>();

  private boolean keepDirectoryArchiveImportHistory = false;

  private boolean importSanitizedBridges = false;

  private String sanitizedBridgesDirectory = "in/bridge-descriptors/";

  private boolean keepSanitizedBridgesImportHistory = false;

  private boolean writeRelayDescriptorDatabase = false;

  private String relayDescriptorDatabaseJdbc =
      "jdbc:postgresql://localhost/tordir?user=metrics&password=password";

  private boolean writeRelayDescriptorsRawFiles = false;

  private String relayDescriptorRawFilesDirectory = "pg-import/";

  private boolean writeBridgeStats = false;

  private boolean importWriteTorperfStats = false;

  private String torperfDirectory = "in/torperf/";

  private String exoneraTorDatabaseJdbc = "jdbc:postgresql:"
      + "//localhost/exonerator?user=metrics&password=password";

  private String exoneraTorImportDirectory = "exonerator-import/";

  /** Initializes this configuration class. */
  public Configuration() {

    /* Initialize logger. */
    Logger logger = Logger.getLogger(Configuration.class.getName());

    /* Read config file, if present. */
    File configFile = new File("config");
    if (!configFile.exists()) {
      logger.warning("Could not find config file.");
      return;
    }
    String line = null;
    try {
      BufferedReader br = new BufferedReader(new FileReader(configFile));
      while ((line = br.readLine()) != null) {
        if (line.startsWith("#") || line.length() < 1) {
          continue;
        } else if (line.startsWith("ImportDirectoryArchives")) {
          this.importDirectoryArchives = Integer.parseInt(
              line.split(" ")[1]) != 0;
        } else if (line.startsWith("DirectoryArchivesDirectory")) {
          this.directoryArchivesDirectories.add(line.split(" ")[1]);
        } else if (line.startsWith("KeepDirectoryArchiveImportHistory")) {
          this.keepDirectoryArchiveImportHistory = Integer.parseInt(
              line.split(" ")[1]) != 0;
        } else if (line.startsWith("ImportSanitizedBridges")) {
          this.importSanitizedBridges = Integer.parseInt(
              line.split(" ")[1]) != 0;
        } else if (line.startsWith("SanitizedBridgesDirectory")) {
          this.sanitizedBridgesDirectory = line.split(" ")[1];
        } else if (line.startsWith("KeepSanitizedBridgesImportHistory")) {
          this.keepSanitizedBridgesImportHistory = Integer.parseInt(
              line.split(" ")[1]) != 0;
        } else if (line.startsWith("WriteRelayDescriptorDatabase")) {
          this.writeRelayDescriptorDatabase = Integer.parseInt(
              line.split(" ")[1]) != 0;
        } else if (line.startsWith("RelayDescriptorDatabaseJDBC")) {
          this.relayDescriptorDatabaseJdbc = line.split(" ")[1];
        } else if (line.startsWith("WriteRelayDescriptorsRawFiles")) {
          this.writeRelayDescriptorsRawFiles = Integer.parseInt(
              line.split(" ")[1]) != 0;
        } else if (line.startsWith("RelayDescriptorRawFilesDirectory")) {
          this.relayDescriptorRawFilesDirectory = line.split(" ")[1];
        } else if (line.startsWith("WriteBridgeStats")) {
          this.writeBridgeStats = Integer.parseInt(
              line.split(" ")[1]) != 0;
        } else if (line.startsWith("ImportWriteTorperfStats")) {
          this.importWriteTorperfStats = Integer.parseInt(
              line.split(" ")[1]) != 0;
        } else if (line.startsWith("TorperfDirectory")) {
          this.torperfDirectory = line.split(" ")[1];
        } else if (line.startsWith("ExoneraTorDatabaseJdbc")) {
          this.exoneraTorDatabaseJdbc = line.split(" ")[1];
        } else if (line.startsWith("ExoneraTorImportDirectory")) {
          this.exoneraTorImportDirectory = line.split(" ")[1];
        } else {
          logger.severe("Configuration file contains unrecognized "
              + "configuration key in line '" + line + "'! Exiting!");
          System.exit(1);
        }
      }
      br.close();
    } catch (ArrayIndexOutOfBoundsException e) {
      logger.severe("Configuration file contains configuration key "
          + "without value in line '" + line + "'. Exiting!");
      System.exit(1);
    } catch (MalformedURLException e) {
      logger.severe("Configuration file contains illegal URL or IP:port "
          + "pair in line '" + line + "'. Exiting!");
      System.exit(1);
    } catch (NumberFormatException e) {
      logger.severe("Configuration file contains illegal value in line '"
          + line + "' with legal values being 0 or 1. Exiting!");
      System.exit(1);
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Unknown problem while reading config "
          + "file! Exiting!", e);
      System.exit(1);
    }
  }

  public boolean getImportDirectoryArchives() {
    return this.importDirectoryArchives;
  }

  /** Returns directories containing archived descriptors. */
  public List<String> getDirectoryArchivesDirectories() {
    if (this.directoryArchivesDirectories.isEmpty()) {
      String prefix = "../../shared/in/recent/relay-descriptors/";
      return Arrays.asList(
          (prefix + "consensuses/," + prefix + "server-descriptors/,"
          + prefix + "extra-infos/").split(","));
    } else {
      return this.directoryArchivesDirectories;
    }
  }

  public boolean getKeepDirectoryArchiveImportHistory() {
    return this.keepDirectoryArchiveImportHistory;
  }

  public boolean getWriteRelayDescriptorDatabase() {
    return this.writeRelayDescriptorDatabase;
  }

  public boolean getImportSanitizedBridges() {
    return this.importSanitizedBridges;
  }

  public String getSanitizedBridgesDirectory() {
    return this.sanitizedBridgesDirectory;
  }

  public boolean getKeepSanitizedBridgesImportHistory() {
    return this.keepSanitizedBridgesImportHistory;
  }

  public String getRelayDescriptorDatabaseJdbc() {
    return this.relayDescriptorDatabaseJdbc;
  }

  public boolean getWriteRelayDescriptorsRawFiles() {
    return this.writeRelayDescriptorsRawFiles;
  }

  public String getRelayDescriptorRawFilesDirectory() {
    return this.relayDescriptorRawFilesDirectory;
  }

  public boolean getWriteBridgeStats() {
    return this.writeBridgeStats;
  }

  public boolean getImportWriteTorperfStats() {
    return this.importWriteTorperfStats;
  }

  public String getTorperfDirectory() {
    return this.torperfDirectory;
  }

  public String getExoneraTorDatabaseJdbc() {
    return this.exoneraTorDatabaseJdbc;
  }

  public String getExoneraTorImportDirectory() {
    return this.exoneraTorImportDirectory;
  }
}

