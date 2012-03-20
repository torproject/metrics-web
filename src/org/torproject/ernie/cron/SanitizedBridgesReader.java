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
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SanitizedBridgesReader {
  public SanitizedBridgesReader(BridgeDescriptorParser bdp,
      File bridgesDir, File statsDirectory, boolean keepImportHistory) {

    if (bdp == null || bridgesDir == null || statsDirectory == null) {
      throw new IllegalArgumentException();
    }

    Logger logger =
        Logger.getLogger(SanitizedBridgesReader.class.getName());
    SortedSet<String> bridgesImportHistory = new TreeSet<String>();
    File bridgesImportHistoryFile =
        new File(statsDirectory, "bridges-import-history");
    if (keepImportHistory && bridgesImportHistoryFile.exists()) {
      try {
        BufferedReader br = new BufferedReader(new FileReader(
            bridgesImportHistoryFile));
        String line = null;
        while ((line = br.readLine()) != null) {
          bridgesImportHistory.add(line);
        }
        br.close();
      } catch (IOException e) {
        logger.log(Level.WARNING, "Could not read in bridge descriptor "
            + "import history file. Skipping.");
      }
    }
    if (bridgesDir.exists()) {
      logger.fine("Importing files in directory " + bridgesDir + "/...");
      Stack<File> filesInInputDir = new Stack<File>();
      filesInInputDir.add(bridgesDir);
      List<File> problems = new ArrayList<File>();
      while (!filesInInputDir.isEmpty()) {
        File pop = filesInInputDir.pop();
        if (pop.isDirectory()) {
          for (File f : pop.listFiles()) {
            filesInInputDir.add(f);
          }
          continue;
        } else if (keepImportHistory && bridgesImportHistory.contains(
            pop.getName())) {
          continue;
        } else {
          try {
            BufferedInputStream bis = new BufferedInputStream(
                new FileInputStream(pop));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int len;
            byte[] data = new byte[1024];
            while ((len = bis.read(data, 0, 1024)) >= 0) {
              baos.write(data, 0, len);
            }
            bis.close();
            byte[] allData = baos.toByteArray();
            String fn = pop.getName();
            // TODO dateTime extraction doesn't work for sanitized network
            // statuses!
            String dateTime = fn.substring(0, 4) + "-" + fn.substring(4, 6)
                + "-" + fn.substring(6, 8) + " " + fn.substring(9, 11)
                + ":" + fn.substring(11, 13) + ":" + fn.substring(13, 15);
            bdp.parse(allData, dateTime, true);
            if (keepImportHistory) {
              bridgesImportHistory.add(pop.getName());
            }
          } catch (IOException e) {
            problems.add(pop);
            if (problems.size() > 3) {
              break;
            }
          }
        }
      }
      if (problems.isEmpty()) {
        logger.fine("Finished importing files in directory " + bridgesDir
            + "/.");
      } else {
        StringBuilder sb = new StringBuilder("Failed importing files in "
            + "directory " + bridgesDir + "/:");
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
      if (keepImportHistory) {
        try {
          bridgesImportHistoryFile.getParentFile().mkdirs();
          BufferedWriter bw = new BufferedWriter(new FileWriter(
              bridgesImportHistoryFile));
          for (String line : bridgesImportHistory) {
            bw.write(line + "\n");
          }
          bw.close();
        } catch (IOException e) {
          logger.log(Level.WARNING, "Could not write bridge descriptor "
              + "import history file.");
        }
      }
    }
  }
}

