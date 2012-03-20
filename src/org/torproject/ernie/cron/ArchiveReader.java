/* Copyright 2011 The Tor Project
 * See LICENSE for licensing information */
package org.torproject.ernie.cron;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.Stack;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Read in all files in a given directory and pass buffered readers of
 * them to the relay descriptor parser.
 */
public class ArchiveReader {
  public ArchiveReader(RelayDescriptorParser rdp, File archivesDirectory,
      File statsDirectory, boolean keepImportHistory) {

    if (rdp == null || archivesDirectory == null ||
        statsDirectory == null) {
      throw new IllegalArgumentException();
    }

    int parsedFiles = 0, ignoredFiles = 0;
    Logger logger = Logger.getLogger(ArchiveReader.class.getName());
    SortedMap<String, Long>
        lastArchivesImportHistory = new TreeMap<String, Long>(),
        newArchivesImportHistory = new TreeMap<String, Long>();
    File archivesImportHistoryFile = new File(statsDirectory,
        "archives-import-history");
    if (keepImportHistory && archivesImportHistoryFile.exists()) {
      try {
        BufferedReader br = new BufferedReader(new FileReader(
            archivesImportHistoryFile));
        String line = null;
        while ((line = br.readLine()) != null) {
          String[] parts = line.split(",");
          if (parts.length < 2) {
            logger.warning("Archives import history file does not "
                + "contain timestamps. Skipping.");
            break;
          }
          long lastModified = Long.parseLong(parts[0]);
          String filename = parts[1];
          lastArchivesImportHistory.put(filename, lastModified);
        }
        br.close();
      } catch (IOException e) {
        logger.log(Level.WARNING, "Could not read in archives import "
            + "history file. Skipping.");
      }
    }
    if (archivesDirectory.exists()) {
      logger.fine("Importing files in directory " + archivesDirectory
          + "/...");
      Stack<File> filesInInputDir = new Stack<File>();
      filesInInputDir.add(archivesDirectory);
      List<File> problems = new ArrayList<File>();
      while (!filesInInputDir.isEmpty()) {
        File pop = filesInInputDir.pop();
        if (pop.isDirectory()) {
          for (File f : pop.listFiles()) {
            filesInInputDir.add(f);
          }
        } else {
          try {
            long lastModified = pop.lastModified();
            String filename = pop.getName();
            if (keepImportHistory) {
              newArchivesImportHistory.put(filename, lastModified);
            }
            if (keepImportHistory &&
                lastArchivesImportHistory.containsKey(filename) &&
                lastArchivesImportHistory.get(filename) >= lastModified) {
              ignoredFiles++;
              continue;
            } else if (filename.endsWith(".tar.bz2")) {
              logger.warning("Cannot parse compressed tarball "
                  + pop.getAbsolutePath() + ". Skipping.");
              continue;
            }
            FileInputStream fis = new FileInputStream(pop);
            BufferedInputStream bis = new BufferedInputStream(fis);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int len;
            byte[] data = new byte[1024];
            while ((len = bis.read(data, 0, 1024)) >= 0) {
              baos.write(data, 0, len);
            }
            bis.close();
            byte[] allData = baos.toByteArray();
            rdp.parse(allData);
            parsedFiles++;
          } catch (IOException e) {
            problems.add(pop);
            if (problems.size() > 3) {
              break;
            }
          }
        }
      }
      if (problems.isEmpty()) {
        logger.fine("Finished importing files in directory "
            + archivesDirectory + "/.");
      } else {
        StringBuilder sb = new StringBuilder("Failed importing files in "
            + "directory " + archivesDirectory + "/:");
        int printed = 0;
        for (File f : problems) {
          sb.append("\n  " + f.getAbsolutePath());
          if (++printed >= 3) {
            sb.append("\n  ... more");
            break;
          }
        }
        logger.warning(sb.toString());
      }
    }
    if (keepImportHistory) {
      try {
        archivesImportHistoryFile.getParentFile().mkdirs();
        BufferedWriter bw = new BufferedWriter(new FileWriter(
            archivesImportHistoryFile));
        for (Map.Entry<String, Long> historyEntry :
            newArchivesImportHistory.entrySet()) {
          bw.write(String.valueOf(historyEntry.getValue()) + ","
              + historyEntry.getKey() + "\n");
        }
        bw.close();
      } catch (IOException e) {
        logger.log(Level.WARNING, "Could not write archives import "
            + "history file.");
      }
    }
    logger.info("Finished importing relay descriptors from local "
        + "directory:\nParsed " + parsedFiles + ", ignored "
        + ignoredFiles + " files.");
  }
}

