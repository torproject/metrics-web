/* Copyright 2011 The Tor Project
 * See LICENSE for licensing information */
package org.torproject.ernie.cron;

import java.io.*;
import java.sql.*;
import java.text.*;
import java.util.*;
import org.apache.commons.codec.binary.*;
import org.apache.commons.codec.digest.*;

/* Import Tor descriptors into the ExoneraTor database. */
public class ExoneraTorDatabaseImporter {

  /* Main function controlling the parsing process. */
  public static void main(String[] args) {
    readConfiguration();
    openDatabaseConnection();
    prepareDatabaseStatements();
    createLockFile();
    readImportHistoryToMemory();
    parseDescriptors();
    writeImportHistoryToDisk();
    closeDatabaseConnection();
    deleteLockFile();
  }

  /* JDBC string of the ExoneraTor database. */
  private static String jdbcString;

  /* Directory from which to import descriptors. */
  private static String importDirString;

  /* Learn JDBC string and directory to parse descriptors from. */
  private static void readConfiguration() {
    Configuration config = new Configuration();
    jdbcString = config.getExoneraTorDatabaseJdbc();
    importDirString = config.getExoneraTorImportDirectory();
  }

  /* Database connection. */
  private static Connection connection;

  /* Open a database connection using the JDBC string in the config. */
  private static void openDatabaseConnection() {
    try {
      connection = DriverManager.getConnection(jdbcString);
    } catch (SQLException e) {
      System.out.println("Could not connect to database.  Exiting.");
      System.exit(1);
    }
  }

  /* Callable statements to import data into the database. */
  private static CallableStatement insertDescriptorStatement;
  private static CallableStatement insertStatusentryStatement;
  private static CallableStatement insertConsensusStatement;
  private static CallableStatement insertExitlistentryStatement;

  /* Prepare statements for importing data into the database. */
  private static void prepareDatabaseStatements() {
    try {
      insertDescriptorStatement = connection.prepareCall(
          "{call insert_descriptor(?, ?)}");
      insertStatusentryStatement = connection.prepareCall(
          "{call insert_statusentry(?, ?, ?, ?, ?, ?)}");
      insertConsensusStatement = connection.prepareCall(
          "{call insert_consensus(?, ?)}");
      insertExitlistentryStatement = connection.prepareCall(
          "{call insert_exitlistentry(?, ?, ?, ?, ?)}");
    } catch (SQLException e) {
      System.out.println("Could not prepare callable statements to "
          + "import data into the database.  Exiting.");
      System.exit(1);
    }
  }

  /* Create a local lock file to prevent other instances of this import
   * tool to run concurrently. */
  private static void createLockFile() {
    File lockFile = new File("exonerator-lock");
    try {
      if (lockFile.exists()) {
        BufferedReader br = new BufferedReader(new FileReader(lockFile));
        long runStarted = Long.parseLong(br.readLine());
        br.close();
        if (System.currentTimeMillis() - runStarted
            < 6L * 60L * 60L * 1000L) {
          System.out.println("File 'exonerator-lock' is less than 6 "
              + "hours old.  Exiting.");
          System.exit(1);
        } else {
          System.out.println("File 'exonerator-lock' is at least 6 hours "
              + "old.  Overwriting and executing anyway.");
        }
      }
      BufferedWriter bw = new BufferedWriter(new FileWriter(
          "exonerator-lock"));
      bw.append(String.valueOf(System.currentTimeMillis()) + "\n");
      bw.close();
    } catch (IOException e) {
      System.out.println("Could not create 'exonerator-lock' file.  "
          + "Exiting.");
      System.exit(1);
    }
  }

  /* Last and next parse histories containing paths of parsed files and
   * last modified times. */
  private static Map<String, Long>
      lastImportHistory = new HashMap<String, Long>(),
      nextImportHistory = new HashMap<String, Long>();

  /* Read stats/exonerator-import-history file from disk and remember
   * locally when files were last parsed. */
  private static void readImportHistoryToMemory() {
    File parseHistoryFile = new File("stats",
        "exonerator-import-history");
    if (parseHistoryFile.exists()) {
      try {
        BufferedReader br = new BufferedReader(new FileReader(
            parseHistoryFile));
        String line = null;
        int lineNumber = 0;
        while ((line = br.readLine()) != null) {
          lineNumber++;
          String[] parts = line.split(",");
          if (parts.length != 2) {
            System.out.println("File 'stats/exonerator-import-history' "
                + "contains a corrupt entry in line " + lineNumber
                + ".  Ignoring parse history file entirely.");
            lastImportHistory.clear();
            return;
          }
          long lastModified = Long.parseLong(parts[0]);
          String filename = parts[1];
          lastImportHistory.put(filename, lastModified);
        }
        br.close();
      } catch (IOException e) {
        System.out.println("Could not read import history.  Ignoring.");
        lastImportHistory.clear();
      }
    }
  }

  /* Parse descriptors in the import directory and its subdirectories. */
  private static void parseDescriptors() {
    File file = new File(importDirString);
    if (!file.exists()) {
      System.out.println("File or directory " + importDirString + " does "
          + "not exist.  Exiting.");
      return;
    }
    Stack<File> files = new Stack<File>();
    files.add(file);
    while (!files.isEmpty()) {
      file = files.pop();
      if (file.isDirectory()) {
        for (File f : file.listFiles()) {
          files.add(f);
        }
      } else {
        parseFile(file);
      }
    }
  }

  /* Import a file if it wasn't imported before, and add it to the import
   * history for the next execution. */
  private static void parseFile(File file) {
    long lastModified = file.lastModified();
    String filename = file.getName();
    nextImportHistory.put(filename, lastModified);
    if (!lastImportHistory.containsKey(filename) ||
        lastImportHistory.get(filename) < lastModified) {
      try {
        FileInputStream fis = new FileInputStream(file);
        BufferedInputStream bis = new BufferedInputStream(fis);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int len;
        byte[] bytes = new byte[1024];
        while ((len = bis.read(bytes, 0, 1024)) >= 0) {
          baos.write(bytes, 0, len);
        }
        bis.close();
        byte[] allBytes = baos.toByteArray();
        splitFile(file, allBytes);
      } catch (IOException e) {
        System.out.println("Could not read '" + file + "' to memory.  "
            + "Skipping.");
        nextImportHistory.remove(filename);
      }
    }
  }

  /* Detect what descriptor type is contained in a file and split it to
   * parse the single descriptors. */
  private static void splitFile(File file, byte[] bytes) {
    try {
      String asciiString = new String(bytes, "US-ASCII");
      BufferedReader br = new BufferedReader(new StringReader(
          asciiString));
      String line = br.readLine();
      while (line != null && line.startsWith("@")) {
        line = br.readLine();
      }
      if (line == null) {
        return;
      }
      br.close();
      String startToken = null;
      if (line.startsWith("router ")) {
        startToken = "router ";
      } else if (line.equals("network-status-version 3")) {
        startToken = "network-status-version 3";
      } else if (line.startsWith("ExitNode ")) {
        startToken = "ExitNode ";
      } else {
        System.out.println("Unknown descriptor type in file '" + file
            + "'.  Ignoring.");
        return;
      }
      String splitToken = "\n" + startToken;
      int length = bytes.length, start = asciiString.indexOf(startToken);
      while (start < length) {
        int end = asciiString.indexOf(splitToken, start);
        if (end < 0) {
          end = length;
        } else {
          end += 1;
        }
        byte[] descBytes = new byte[end - start];
        System.arraycopy(bytes, start, descBytes, 0, end - start);
        if (startToken.equals("router ")) {
          parseServerDescriptor(file, descBytes);
        } else if (startToken.equals("network-status-version 3")) {
          parseConsensus(file, descBytes);
        } else if (startToken.equals("ExitNode ")) {
          parseExitList(file, descBytes);
        }
        start = end;
      }
    } catch (IOException e) {
      System.out.println("Could not parse descriptor '" + file + "'.  "
          + "Skipping.");
    }
  }

  /* Date format to parse UTC timestamps. */
  private static SimpleDateFormat parseFormat;
  static {
    parseFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    parseFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
  }

  /* Parse a single server descriptor. */
  private static void parseServerDescriptor(File file, byte[] bytes) {
    String ascii = "";
    try {
      ascii = new String(bytes, "US-ASCII");
    } catch (UnsupportedEncodingException e) {
      /* We know that US-ASCII is a supported encoding. */
    }
    String startToken = "router ";
    String sigToken = "\nrouter-signature\n";
    int start = ascii.indexOf(startToken);
    int sig = ascii.indexOf(sigToken) + sigToken.length();
    String descriptor = null;
    if (start >= 0 || sig >= 0 || sig > start) {
      byte[] forDigest = new byte[sig - start];
      System.arraycopy(bytes, start, forDigest, 0, sig - start);
      descriptor = DigestUtils.shaHex(forDigest);
    }
    if (descriptor == null) {
      System.out.println("Could not calculate descriptor digest.  "
          + "Skipping.");
      return;
    }
    importDescriptor(descriptor, bytes);
  }

  /* Import a single server descriptor into the database. */
  private static void importDescriptor(String descriptor,
      byte[] rawDescriptor) {
    try {
      insertDescriptorStatement.clearParameters();
      insertDescriptorStatement.setString(1, descriptor);
      insertDescriptorStatement.setBytes(2, rawDescriptor);
      insertDescriptorStatement.execute();
    } catch (SQLException e) {
      System.out.println("Could not import descriptor into the "
          + "database.  Exiting.");
      System.exit(1);
    }
  }

  /* Parse a consensus. */
  private static void parseConsensus(File file, byte[] bytes) {
    try {
      BufferedReader br = new BufferedReader(new StringReader(new String(
          bytes, "US-ASCII")));
      String line, fingerprint = null, descriptor = null,
          orAddress24 = null, orAddress = null;
      long validAfterMillis = -1L;
      StringBuilder rawStatusentryBuilder = null;
      boolean isRunning = false;
      while ((line = br.readLine()) != null) {
        if (line.startsWith("vote-status ") &&
            !line.equals("vote-status consensus")) {
          System.out.println("File '" + file + "' contains network status "
              + "*votes*, not network status *consensuses*.  Skipping.");
          return;
        } else if (line.startsWith("valid-after ")) {
          String validAfterTime = line.substring("valid-after ".length());
          try {
            validAfterMillis = parseFormat.parse(validAfterTime).
                getTime();
          } catch (ParseException e) {
            System.out.println("Could not parse valid-after timestamp in "
                + "'" + file + "'.  Skipping.");
            return;
          }
          importConsensus(validAfterMillis, bytes);
        } else if (line.startsWith("r ") ||
            line.equals("directory-footer")) {
          if (isRunning) {
            byte[] rawStatusentry = rawStatusentryBuilder.toString().
                getBytes();
            importStatusentry(validAfterMillis, fingerprint, descriptor,
                orAddress24, orAddress, rawStatusentry);
          }
          if (line.equals("directory-footer")) {
            return;
          }
          rawStatusentryBuilder = new StringBuilder(line + "\n");
          String[] parts = line.split(" ");
          if (parts.length < 9) {
            System.out.println("Could not parse r line '" + line
                + "'.  Skipping.");
            return;
          }
          fingerprint = Hex.encodeHexString(Base64.decodeBase64(parts[2]
              + "=")).toLowerCase();
          descriptor = Hex.encodeHexString(Base64.decodeBase64(parts[3]
              + "=")).toLowerCase();
          orAddress = parts[6];
          /* TODO Extend the following code for IPv6 once Tor supports
           * it. */
          String[] orAddressParts = orAddress.split("\\.");
          byte[] orAddress24Bytes = new byte[3];
          orAddress24Bytes[0] = (byte) Integer.parseInt(
              orAddressParts[0]);
          orAddress24Bytes[1] = (byte) Integer.parseInt(
              orAddressParts[1]);
          orAddress24Bytes[2] = (byte) Integer.parseInt(
              orAddressParts[2]);
          orAddress24 = Hex.encodeHexString(orAddress24Bytes);
        } else if (line.startsWith("s ") || line.equals("s")) {
          rawStatusentryBuilder.append(line + "\n");
          isRunning = line.contains(" Running");
        } else if (rawStatusentryBuilder != null) {
          rawStatusentryBuilder.append(line + "\n");
        }
        /* TODO Extend this code to parse additional addresses once that's
         * implemented in Tor. */
      }
    } catch (IOException e) {
      System.out.println("Could not parse consensus.  Skipping.");
      return;
    }
  }

  /* UTC calendar for importing timestamps into the database. */
  private static Calendar calendarUTC = Calendar.getInstance(
      TimeZone.getTimeZone("UTC"));

  /* Import a single status entry into the database. */
  private static void importStatusentry(long validAfterMillis,
      String fingerprint, String descriptor, String orAddress24,
      String orAddress, byte[] rawStatusentry) {
    try {
      insertStatusentryStatement.clearParameters();
      insertStatusentryStatement.setTimestamp(1,
          new Timestamp(validAfterMillis), calendarUTC);
      insertStatusentryStatement.setString(2, fingerprint);
      insertStatusentryStatement.setString(3, descriptor);
      insertStatusentryStatement.setString(4, orAddress24);
      insertStatusentryStatement.setString(5, orAddress);
      insertStatusentryStatement.setBytes(6, rawStatusentry);
      insertStatusentryStatement.execute();
    } catch (SQLException e) {
      System.out.println("Could not import status entry.  Exiting.");
      System.exit(1);
    }
  }

  /* Import a consensus into the database. */
  private static void importConsensus(long validAfterMillis,
      byte[] rawConsensus) {
    try {
      insertConsensusStatement.clearParameters();
      insertConsensusStatement.setTimestamp(1,
          new Timestamp(validAfterMillis), calendarUTC);
      insertConsensusStatement.setBytes(2, rawConsensus);
      insertConsensusStatement.execute();
    } catch (SQLException e) {
      System.out.println("Could not import consensus.  Exiting.");
      System.exit(1);
    }
  }

  /* Parse an exit list. */
  private static void parseExitList(File file, byte[] bytes) {
    try {
      BufferedReader br = new BufferedReader(new StringReader(new String(
          bytes, "US-ASCII")));
      String fingerprint = null;
      Set<String> exitAddressLines = new HashSet<String>();
      StringBuilder rawExitlistentryBuilder = new StringBuilder();
      while (true) {
        String line = br.readLine();
        if ((line == null || line.startsWith("ExitNode ")) &&
            fingerprint != null) {
          for (String exitAddressLine : exitAddressLines) {
            String[] parts = exitAddressLine.split(" ");
            String exitAddress = parts[1];
            /* TODO Extend the following code for IPv6 once the exit list
             * format supports it. */
            String[] exitAddressParts = exitAddress.split("\\.");
            byte[] exitAddress24Bytes = new byte[3];
            exitAddress24Bytes[0] = (byte) Integer.parseInt(
                exitAddressParts[0]);
            exitAddress24Bytes[1] = (byte) Integer.parseInt(
                exitAddressParts[1]);
            exitAddress24Bytes[2] = (byte) Integer.parseInt(
                exitAddressParts[2]);
            String exitAddress24 = Hex.encodeHexString(
                exitAddress24Bytes);
            String scannedTime = parts[2] + " " + parts[3];
            long scannedMillis = -1L, publishedMillis = -1L;
            try {
              scannedMillis = parseFormat.parse(scannedTime).getTime();
            } catch (ParseException e) {
              System.out.println("Could not parse timestamp in "
                  + "'" + file + "'.  Skipping.");
              return;
            }
            byte[] rawExitlistentry = rawExitlistentryBuilder.toString().
                getBytes();
            importExitlistentry(fingerprint, exitAddress24, exitAddress,
                scannedMillis, rawExitlistentry);
          }
          exitAddressLines.clear();
          rawExitlistentryBuilder = new StringBuilder();
        }
        if (line == null) {
          break;
        }
        rawExitlistentryBuilder.append(line + "\n");
        if (line.startsWith("ExitNode ")) {
          fingerprint = line.substring("ExitNode ".length()).
              toLowerCase();
        } else if (line.startsWith("ExitAddress ")) {
          exitAddressLines.add(line);
        }
      }
      br.close();
    } catch (IOException e) {
      System.out.println("Could not parse exit list.  Skipping.");
      return;
    }
  }

  /* Import an exit list entry into the database. */
  private static void importExitlistentry(String fingerprint,
      String exitAddress24, String exitAddress, long scannedMillis,
      byte[] rawExitlistentry) {
    try {
      insertExitlistentryStatement.clearParameters();
      insertExitlistentryStatement.setString(1, fingerprint);
      insertExitlistentryStatement.setString(2, exitAddress24);
      insertExitlistentryStatement.setString(3, exitAddress);
      insertExitlistentryStatement.setTimestamp(4,
          new Timestamp(scannedMillis), calendarUTC);
      insertExitlistentryStatement.setBytes(5, rawExitlistentry);
      insertExitlistentryStatement.execute();
    } catch (SQLException e) {
      System.out.println("Could not import exit list entry.  Exiting.");
      System.exit(1);
    }
  }

  /* Write parse history from memory to disk for the next execution. */
  private static void writeImportHistoryToDisk() {
    File parseHistoryFile = new File("stats/exonerator-import-history");
    try {
      BufferedWriter bw = new BufferedWriter(new FileWriter(
          parseHistoryFile));
      for (Map.Entry<String, Long> historyEntry :
          nextImportHistory.entrySet()) {
        bw.write(String.valueOf(historyEntry.getValue()) + ","
            + historyEntry.getKey() + "\n");
      }
      bw.close();
    } catch (IOException e) {
      System.out.println("File 'stats/exonerator-import-history' could "
          + "not be written.  Ignoring.");
    }
  }

  /* Close the database connection. */
  private static void closeDatabaseConnection() {
    try {
      connection.close();
    } catch (SQLException e) {
      System.out.println("Could not close database connection.  "
          + "Ignoring.");
    }
  }

  /* Delete the exonerator-lock file to allow the next executing of this
   * tool. */
  private static void deleteLockFile() {
    new File("exonerator-lock").delete();
  }
}

