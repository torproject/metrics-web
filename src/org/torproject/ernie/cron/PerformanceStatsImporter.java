/* Copyright 2012 The Tor Project
 * See LICENSE for licensing information */
package org.torproject.ernie.cron;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.torproject.descriptor.Descriptor;
import org.torproject.descriptor.DescriptorFile;
import org.torproject.descriptor.DescriptorReader;
import org.torproject.descriptor.DescriptorSourceFactory;
import org.torproject.descriptor.ExtraInfoDescriptor;

public class PerformanceStatsImporter {

  /**
   * How many records to commit with each database transaction.
   */
  private final long autoCommitCount = 500;

  /**
   * Keep track of the number of records committed before each transaction
   */
  private int rbsCount = 0;

  /**
   * Relay descriptor database connection.
   */
  private Connection conn;

  /**
   * Prepared statement to check whether a given conn-bi-direct stats
   * string has been imported into the database before.
   */
  private PreparedStatement psBs;

  /**
   * Prepared statement to insert a conn-bi-direct stats string into the
   * database.
   */
  private PreparedStatement psB;

  /**
   * Logger for this class.
   */
  private Logger logger;

  /**
   * Directory for writing raw import files.
   */
  private String rawFilesDirectory;

  /**
   * Raw import file containing conn-bi-direct stats strings.
   */
  private BufferedWriter connBiDirectOut;

  /**
   * Date format to parse timestamps.
   */
  private SimpleDateFormat dateTimeFormat;

  private boolean importIntoDatabase;
  private boolean writeRawImportFiles;

  private File archivesDirectory;
  private File statsDirectory;
  private boolean keepImportHistory;

  /**
   * Initialize database importer by connecting to the database and
   * preparing statements.
   */
  public PerformanceStatsImporter(String connectionURL,
      String rawFilesDirectory, File archivesDirectory,
      File statsDirectory, boolean keepImportHistory) {

    if (archivesDirectory == null ||
        statsDirectory == null) {
      throw new IllegalArgumentException();
    }
    this.archivesDirectory = archivesDirectory;
    this.statsDirectory = statsDirectory;
    this.keepImportHistory = keepImportHistory;

    /* Initialize logger. */
    this.logger = Logger.getLogger(
        RelayDescriptorDatabaseImporter.class.getName());

    if (connectionURL != null) {
      try {
        /* Connect to database. */
        this.conn = DriverManager.getConnection(connectionURL);

        /* Turn autocommit off */
        this.conn.setAutoCommit(false);

        /* Prepare statements. */
        this.psBs = conn.prepareStatement("SELECT COUNT(*) "
            + "FROM connbidirect WHERE source = ? AND statsend = ?");
        this.psB = conn.prepareStatement("INSERT INTO connbidirect "
            + "(source, statsend, seconds, belownum, readnum, writenum, "
            + "bothnum) VALUES (?, ?, ?, ?, ?, ?, ?)");
        this.importIntoDatabase = true;
      } catch (SQLException e) {
        this.logger.log(Level.WARNING, "Could not connect to database or "
            + "prepare statements.", e);
      }
    }

    /* Remember where we want to write raw import files. */
    if (rawFilesDirectory != null) {
      this.rawFilesDirectory = rawFilesDirectory;
      this.writeRawImportFiles = true;
    }

    /* Initialize date format, so that we can format timestamps. */
    this.dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    this.dateTimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
  }

  /**
   * Insert a conn-bi-direct stats string into the database.
   */
  private void addConnBiDirect(String source, long statsEndMillis,
      long seconds, long below, long read, long write, long both) {
    String statsEnd = this.dateTimeFormat.format(statsEndMillis);
    if (this.importIntoDatabase) {
      try {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        Timestamp statsEndTimestamp = new Timestamp(statsEndMillis);
        this.psBs.setString(1, source);
        this.psBs.setTimestamp(2, statsEndTimestamp, cal);
        ResultSet rs = psBs.executeQuery();
        rs.next();
        if (rs.getInt(1) == 0) {
          this.psB.clearParameters();
          this.psB.setString(1, source);
          this.psB.setTimestamp(2, statsEndTimestamp, cal);
          this.psB.setLong(3, seconds);
          this.psB.setLong(4, below);
          this.psB.setLong(5, read);
          this.psB.setLong(6, write);
          this.psB.setLong(7, both);
          this.psB.executeUpdate();
          rbsCount++;
          if (rbsCount % autoCommitCount == 0)  {
            this.conn.commit();
          }
        }
      } catch (SQLException e) {
        this.logger.log(Level.WARNING, "Could not add conn-bi-direct "
            + "stats string. We won't make any further SQL requests in "
            + "this execution.", e);
        this.importIntoDatabase = false;
      }
    }
    if (this.writeRawImportFiles) {
      try {
        if (this.connBiDirectOut == null) {
          new File(rawFilesDirectory).mkdirs();
          this.connBiDirectOut = new BufferedWriter(new FileWriter(
              rawFilesDirectory + "/connbidirect.sql"));
          this.connBiDirectOut.write(" COPY connbidirect (source, "
              + "statsend, seconds, belownum, readnum, writenum, "
              + "bothnum) FROM stdin;\n");
        }
        this.connBiDirectOut.write(source + "\t" + statsEnd + "\t"
            + seconds + "\t" + below + "\t" + read + "\t" + write + "\t"
            + both + "\n");
      } catch (IOException e) {
        this.logger.log(Level.WARNING, "Could not write conn-bi-direct "
            + "stats string to raw database import file.  We won't make "
            + "any further attempts to write raw import files in this "
            + "execution.", e);
        this.writeRawImportFiles = false;
      }
    }
  }

  void importRelayDescriptors() {
    if (archivesDirectory.exists()) {
      logger.fine("Importing files in directory " + archivesDirectory
          + "/...");
      DescriptorReader reader =
          DescriptorSourceFactory.createDescriptorReader();
      reader.addDirectory(archivesDirectory);
      if (keepImportHistory) {
        reader.setExcludeFiles(new File(statsDirectory,
            "connbidirect-relay-descriptor-history"));
      }
      Iterator<DescriptorFile> descriptorFiles = reader.readDescriptors();
      while (descriptorFiles.hasNext()) {
        DescriptorFile descriptorFile = descriptorFiles.next();
        if (descriptorFile.getDescriptors() != null) {
          for (Descriptor descriptor : descriptorFile.getDescriptors()) {
            if (descriptor instanceof ExtraInfoDescriptor) {
              this.addExtraInfoDescriptor(
                  (ExtraInfoDescriptor) descriptor);
            }
          }
        }
      }
    }

    logger.info("Finished importing relay descriptors.");
  }

  private void addExtraInfoDescriptor(ExtraInfoDescriptor descriptor) {
    if (descriptor.getConnBiDirectStatsEndMillis() >= 0L) {
      this.addConnBiDirect(descriptor.getFingerprint(),
          descriptor.getConnBiDirectStatsEndMillis(),
          descriptor.getConnBiDirectStatsIntervalLength(),
          descriptor.getConnBiDirectBelow(),
          descriptor.getConnBiDirectRead(),
          descriptor.getConnBiDirectWrite(),
          descriptor.getConnBiDirectBoth());
    }
  }

  /**
   * Close the relay descriptor database connection.
   */
  void closeConnection() {

    /* Log stats about imported descriptors. */
    this.logger.info(String.format("Finished importing relay "
        + "descriptors: %d conn-bi-direct stats lines", rbsCount));

    /* Commit any stragglers before closing. */
    if (this.conn != null) {
      try {
        this.conn.commit();
      } catch (SQLException e)  {
        this.logger.log(Level.WARNING, "Could not commit final records "
            + "to database", e);
      }
      try {
        this.conn.close();
      } catch (SQLException e) {
        this.logger.log(Level.WARNING, "Could not close database "
            + "connection.", e);
      }
    }

    /* Close raw import files. */
    try {
      if (this.connBiDirectOut != null) {
        this.connBiDirectOut.write("\\.\n");
        this.connBiDirectOut.close();
      }
    } catch (IOException e) {
      this.logger.log(Level.WARNING, "Could not close one or more raw "
          + "database import files.", e);
    }
  }
}
