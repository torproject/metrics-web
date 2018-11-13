/* Copyright 2011--2018 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.stats.servers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Initialize configuration with hard-coded defaults, overwrite with
 * configuration in config file, if exists, and answer Main.java about our
 * configuration.
 */
public class Configuration {

  private static Logger log = LoggerFactory.getLogger(Configuration.class);

  private List<File> directoryArchivesDirectories = new ArrayList<>();

  private String relayDescriptorDatabaseJdbc =
      "jdbc:postgresql://localhost/tordir?user=metrics&password=password";

  /** Initializes this configuration class. */
  public Configuration() {

    /* Read config file, if present. */
    File configFile = new File("config");
    if (!configFile.exists()) {
      log.warn("Could not find config file.");
      return;
    }
    String line = null;
    try (BufferedReader br = new BufferedReader(new FileReader(configFile))) {
      while ((line = br.readLine()) != null) {
        if (line.startsWith("DirectoryArchivesDirectory")) {
          this.directoryArchivesDirectories.add(new File(line.split(" ")[1]));
        } else if (line.startsWith("RelayDescriptorDatabaseJDBC")) {
          this.relayDescriptorDatabaseJdbc = line.split(" ")[1];
        } else if (!line.startsWith("#") && line.length() > 0) {
          log.error("Configuration file contains unrecognized "
              + "configuration key in line '{}'! Exiting!", line);
          System.exit(1);
        }
      }
    } catch (ArrayIndexOutOfBoundsException e) {
      log.warn("Configuration file contains configuration key without value in "
          + "line '{}'. Exiting!", line);
      System.exit(1);
    } catch (MalformedURLException e) {
      log.warn("Configuration file contains illegal URL or IP:port pair in "
          + "line '{}'. Exiting!", line);
      System.exit(1);
    } catch (NumberFormatException e) {
      log.warn("Configuration file contains illegal value in line '{}' with "
          + "legal values being 0 or 1. Exiting!", line);
      System.exit(1);
    } catch (IOException e) {
      log.error("Unknown problem while reading config file! Exiting!", e);
      System.exit(1);
    }
  }

  /** Returns directories containing archived descriptors. */
  public List<File> getDirectoryArchivesDirectories() {
    if (this.directoryArchivesDirectories.isEmpty()) {
      String prefix = "../../shared/in/recent/relay-descriptors/";
      return Arrays.asList(new File(prefix + "consensuses/"),
          new File(prefix + "extra-infos/"));
    } else {
      return this.directoryArchivesDirectories;
    }
  }

  public String getRelayDescriptorDatabaseJdbc() {
    return this.relayDescriptorDatabaseJdbc;
  }
}

