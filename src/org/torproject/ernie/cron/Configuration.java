/* Copyright 2011 The Tor Project
 * See LICENSE for licensing information */
package org.torproject.ernie.cron;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;

/**
 * Initialize configuration with hard-coded defaults, overwrite with
 * configuration in config file, if exists, and answer Main.java about our
 * configuration.
 */
public class Configuration {
  private boolean importDirectoryArchives = false;
  private String directoryArchivesDirectory = "archives/";
  private boolean keepDirectoryArchiveImportHistory = false;
  private boolean writeConsensusHealth = false;
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
          this.directoryArchivesDirectory = line.split(" ")[1];
        } else if (line.startsWith("KeepDirectoryArchiveImportHistory")) {
          this.keepDirectoryArchiveImportHistory = Integer.parseInt(
              line.split(" ")[1]) != 0;
        } else if (line.startsWith("WriteConsensusHealth")) {
          this.writeConsensusHealth = Integer.parseInt(
              line.split(" ")[1]) != 0;
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
  public String getDirectoryArchivesDirectory() {
    return this.directoryArchivesDirectory;
  }
  public boolean getKeepDirectoryArchiveImportHistory() {
    return this.keepDirectoryArchiveImportHistory;
  }
  public boolean getWriteConsensusHealth() {
    return this.writeConsensusHealth;
  }
}

