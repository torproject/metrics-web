/* Copyright 2011--2017 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.ernie.cron.performance;

import org.torproject.descriptor.Descriptor;
import org.torproject.descriptor.DescriptorFile;
import org.torproject.descriptor.DescriptorReader;
import org.torproject.descriptor.DescriptorSourceFactory;
import org.torproject.descriptor.TorperfResult;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TorperfProcessor {

  /** Processes Torperf data from the given directory and writes
   * aggregates statistics to the given stats directory. */
  public TorperfProcessor(File torperfDirectory, File statsDirectory) {

    if (torperfDirectory == null || statsDirectory == null) {
      throw new IllegalArgumentException();
    }

    Logger logger = Logger.getLogger(TorperfProcessor.class.getName());
    File rawFile = new File(statsDirectory, "torperf-raw");
    File statsFile = new File(statsDirectory, "torperf.csv");
    SortedMap<String, String> rawObs = new TreeMap<String, String>();
    SortedMap<String, String> stats = new TreeMap<String, String>();
    int addedRawObs = 0;
    SimpleDateFormat formatter =
        new SimpleDateFormat("yyyy-MM-dd,HH:mm:ss");
    formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
    try {
      if (rawFile.exists()) {
        logger.fine("Reading file " + rawFile.getAbsolutePath() + "...");
        BufferedReader br = new BufferedReader(new FileReader(rawFile));
        String line = br.readLine(); // ignore header
        while ((line = br.readLine()) != null) {
          if (line.split(",").length != 4) {
            logger.warning("Corrupt line in " + rawFile.getAbsolutePath()
                + "!");
            break;
          }
          String key = line.substring(0, line.lastIndexOf(","));
          rawObs.put(key, line);
        }
        br.close();
        logger.fine("Finished reading file " + rawFile.getAbsolutePath()
            + ".");
      }
      if (statsFile.exists()) {
        logger.fine("Reading file " + statsFile.getAbsolutePath()
            + "...");
        BufferedReader br = new BufferedReader(new FileReader(statsFile));
        String line = br.readLine(); // ignore header
        while ((line = br.readLine()) != null) {
          String[] parts = line.split(",");
          String key = String.format("%s,%s,%s", parts[0], parts[1],
              parts[2]);
          stats.put(key, line);
        }
        br.close();
        logger.fine("Finished reading file " + statsFile.getAbsolutePath()
            + ".");
      }
      if (torperfDirectory.exists()) {
        logger.fine("Importing files in " + torperfDirectory + "/...");
        DescriptorReader descriptorReader =
            DescriptorSourceFactory.createDescriptorReader();
        descriptorReader.addDirectory(torperfDirectory);
        descriptorReader.setExcludeFiles(new File(statsDirectory,
            "torperf-history"));
        Iterator<DescriptorFile> descriptorFiles =
            descriptorReader.readDescriptors();
        while (descriptorFiles.hasNext()) {
          DescriptorFile descriptorFile = descriptorFiles.next();
          if (descriptorFile.getException() != null) {
            logger.log(Level.FINE, "Error parsing file.",
                descriptorFile.getException());
            continue;
          }
          for (Descriptor descriptor : descriptorFile.getDescriptors()) {
            if (!(descriptor instanceof TorperfResult)) {
              continue;
            }
            TorperfResult result = (TorperfResult) descriptor;
            String source = result.getSource();
            long fileSize = result.getFileSize();
            if (fileSize == 51200) {
              source += "-50kb";
            } else if (fileSize == 1048576) {
              source += "-1mb";
            } else if (fileSize == 5242880) {
              source += "-5mb";
            } else {
              logger.fine("Unexpected file size '" + fileSize
                  + "'.  Skipping.");
              continue;
            }
            String dateTime = formatter.format(result.getStartMillis());
            long completeMillis = result.getDataCompleteMillis()
                - result.getStartMillis();
            String key = source + "," + dateTime;
            String value = key;
            if ((result.didTimeout() == null
                && result.getDataCompleteMillis() < 1)
                || (result.didTimeout() != null && result.didTimeout())) {
              value += ",-2"; // -2 for timeout
            } else if (result.getReadBytes() < fileSize) {
              value += ",-1"; // -1 for failure
            } else {
              value += "," + completeMillis;
            }
            if (!rawObs.containsKey(key)) {
              rawObs.put(key, value);
              addedRawObs++;
            }
          }
        }
        logger.fine("Finished importing files in " + torperfDirectory
            + "/.");
      }
      if (rawObs.size() > 0) {
        logger.fine("Writing file " + rawFile.getAbsolutePath() + "...");
        rawFile.getParentFile().mkdirs();
        BufferedWriter bw = new BufferedWriter(new FileWriter(rawFile));
        bw.append("source,date,start,completemillis\n");
        String tempSourceDate = null;
        Iterator<Map.Entry<String, String>> it =
            rawObs.entrySet().iterator();
        List<Long> dlTimes = new ArrayList<Long>();
        boolean haveWrittenFinalLine = false;
        SortedMap<String, List<Long>> dlTimesAllSources =
            new TreeMap<String, List<Long>>();
        SortedMap<String, long[]> statusesAllSources =
            new TreeMap<String, long[]>();
        long failures = 0;
        long timeouts = 0;
        long requests = 0;
        while (it.hasNext() || !haveWrittenFinalLine) {
          Map.Entry<String, String> next =
              it.hasNext() ? it.next() : null;
          if (tempSourceDate != null
              && (next == null || !(next.getValue().split(",")[0] + ","
              + next.getValue().split(",")[1]).equals(tempSourceDate))) {
            if (dlTimes.size() > 4) {
              Collections.sort(dlTimes);
              long q1 = dlTimes.get(dlTimes.size() / 4 - 1);
              long md = dlTimes.get(dlTimes.size() / 2 - 1);
              long q3 = dlTimes.get(dlTimes.size() * 3 / 4 - 1);
              String[] tempParts = tempSourceDate.split("[-,]", 3);
              String tempDate = tempParts[2];
              int tempSize = Integer.parseInt(
                  tempParts[1].substring(0, tempParts[1].length() - 2))
                  * 1024 * (tempParts[1].endsWith("mb") ? 1024 : 1);
              String tempSource = tempParts[0];
              String tempDateSizeSource = String.format("%s,%d,%s",
                  tempDate, tempSize, tempSource);
              stats.put(tempDateSizeSource,
                  String.format("%s,%s,%s,%s,%s,%s,%s",
                  tempDateSizeSource, q1, md, q3, timeouts, failures,
                  requests));
              String allDateSizeSource = String.format("%s,%d,",
                  tempDate, tempSize);
              if (dlTimesAllSources.containsKey(allDateSizeSource)) {
                dlTimesAllSources.get(allDateSizeSource).addAll(dlTimes);
              } else {
                dlTimesAllSources.put(allDateSizeSource, dlTimes);
              }
              if (statusesAllSources.containsKey(allDateSizeSource)) {
                long[] status = statusesAllSources.get(allDateSizeSource);
                status[0] += timeouts;
                status[1] += failures;
                status[2] += requests;
              } else {
                long[] status = new long[3];
                status[0] = timeouts;
                status[1] = failures;
                status[2] = requests;
                statusesAllSources.put(allDateSizeSource, status);
              }
            }
            dlTimes = new ArrayList<Long>();
            failures = timeouts = requests = 0;
            if (next == null) {
              haveWrittenFinalLine = true;
            }
          }
          if (next != null) {
            bw.append(next.getValue() + "\n");
            String[] parts = next.getValue().split(",");
            tempSourceDate = parts[0] + "," + parts[1];
            long completeMillis = Long.parseLong(parts[3]);
            if (completeMillis == -2L) {
              timeouts++;
            } else if (completeMillis == -1L) {
              failures++;
            } else {
              dlTimes.add(Long.parseLong(parts[3]));
            }
            requests++;
          }
        }
        bw.close();
        for (Map.Entry<String, List<Long>> e
            : dlTimesAllSources.entrySet()) {
          String allDateSizeSource = e.getKey();
          dlTimes = e.getValue();
          Collections.sort(dlTimes);
          long[] status = statusesAllSources.get(allDateSizeSource);
          timeouts = status[0];
          failures = status[1];
          requests = status[2];
          long q1 = dlTimes.get(dlTimes.size() / 4 - 1);
          long md = dlTimes.get(dlTimes.size() / 2 - 1);
          long q3 = dlTimes.get(dlTimes.size() * 3 / 4 - 1);
          stats.put(allDateSizeSource,
              String.format("%s,%s,%s,%s,%s,%s,%s",
              allDateSizeSource, q1, md, q3, timeouts, failures,
              requests));
        }
        logger.fine("Finished writing file " + rawFile.getAbsolutePath()
            + ".");
      }
      if (stats.size() > 0) {
        logger.fine("Writing file " + statsFile.getAbsolutePath()
            + "...");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        String yesterday = dateFormat.format(System.currentTimeMillis()
            - 86400000L);
        statsFile.getParentFile().mkdirs();
        BufferedWriter bw = new BufferedWriter(new FileWriter(statsFile));
        bw.append("date,size,source,q1,md,q3,timeouts,failures,"
            + "requests\n");
        for (String s : stats.values()) {
          if (s.compareTo(yesterday) < 0) {
            bw.append(s + "\n");
          }
        }
        bw.close();
        logger.fine("Finished writing file " + statsFile.getAbsolutePath()
            + ".");
      }
    } catch (IOException e) {
      logger.log(Level.WARNING, "Failed writing "
          + rawFile.getAbsolutePath() + " or "
          + statsFile.getAbsolutePath() + "!", e);
    }

    /* Write stats. */
    StringBuilder dumpStats = new StringBuilder("Finished writing "
        + "statistics on torperf results.\nAdded " + addedRawObs
        + " new observations in this execution.\n"
        + "Last known obserations by source and file size are:");
    String lastSource = null;
    String lastLine = null;
    for (String s : rawObs.keySet()) {
      String[] parts = s.split(",");
      if (lastSource == null) {
        lastSource = parts[0];
      } else if (!parts[0].equals(lastSource)) {
        String lastKnownObservation = lastLine.split(",")[1] + " "
            + lastLine.split(",")[2];
        dumpStats.append("\n" + lastSource + " " + lastKnownObservation);
        lastSource = parts[0];
      }
      lastLine = s;
    }
    if (lastSource != null) {
      String lastKnownObservation = lastLine.split(",")[1] + " "
          + lastLine.split(",")[2];
      dumpStats.append("\n" + lastSource + " " + lastKnownObservation);
    }
    logger.info(dumpStats.toString());
  }
}

