/* Copyright 2011, 2012 The Tor Project
 * See LICENSE for licensing information */
package org.torproject.ernie.cron;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.postgresql.util.PGbytea;

/**
 * Parse directory data.
 */

public final class RelayDescriptorDatabaseImporter {

  /**
   * How many records to commit with each database transaction.
   */
  private final long autoCommitCount = 500;

  /**
   * Keep track of the number of records committed before each transaction
   */
  private int rdsCount = 0;
  private int resCount = 0;
  private int rhsCount = 0;
  private int rrsCount = 0;
  private int rcsCount = 0;
  private int rvsCount = 0;
  private int rbsCount = 0;
  private int rqsCount = 0;

  /**
   * Relay descriptor database connection.
   */
  private Connection conn;

  /**
   * Prepared statement to check whether any network status consensus
   * entries matching a given valid-after time have been imported into the
   * database before.
   */
  private PreparedStatement psSs;

  /**
   * Prepared statement to check whether a given network status consensus
   * entry has been imported into the database before.
   */
  private PreparedStatement psRs;

  /**
   * Prepared statement to check whether a given extra-info descriptor has
   * been imported into the database before.
   */
  private PreparedStatement psEs;

  /**
   * Prepared statement to check whether a given server descriptor has
   * been imported into the database before.
   */
  private PreparedStatement psDs;

  /**
   * Prepared statement to check whether a given network status consensus
   * has been imported into the database before.
   */
  private PreparedStatement psCs;

  /**
   * Prepared statement to check whether a given network status vote has
   * been imported into the database before.
   */
  private PreparedStatement psVs;

  /**
   * Prepared statement to check whether a given conn-bi-direct stats
   * string has been imported into the database before.
   */
  private PreparedStatement psBs;

  /**
   * Prepared statement to check whether a given dirreq stats string has
   * been imported into the database before.
   */
  private PreparedStatement psQs;

  /**
   * Set of dates that have been inserted into the database for being
   * included in the next refresh run.
   */
  private Set<Long> scheduledUpdates;

  /**
   * Prepared statement to insert a date into the database that shall be
   * included in the next refresh run.
   */
  private PreparedStatement psU;

  /**
   * Prepared statement to insert a network status consensus entry into
   * the database.
   */
  private PreparedStatement psR;

  /**
   * Prepared statement to insert a server descriptor into the database.
   */
  private PreparedStatement psD;

  /**
   * Prepared statement to insert an extra-info descriptor into the
   * database.
   */
  private PreparedStatement psE;

  /**
   * Callable statement to insert the bandwidth history of an extra-info
   * descriptor into the database.
   */
  private CallableStatement csH;

  /**
   * Prepared statement to insert a network status consensus into the
   * database.
   */
  private PreparedStatement psC;

  /**
   * Prepared statement to insert a network status vote into the
   * database.
   */
  private PreparedStatement psV;

  /**
   * Prepared statement to insert a conn-bi-direct stats string into the
   * database.
   */
  private PreparedStatement psB;

  /**
   * Prepared statement to insert a given dirreq stats string into the
   * database.
   */
  private PreparedStatement psQ;

  /**
   * Logger for this class.
   */
  private Logger logger;

  /**
   * Directory for writing raw import files.
   */
  private String rawFilesDirectory;

  /**
   * Raw import file containing status entries.
   */
  private BufferedWriter statusentryOut;

  /**
   * Raw import file containing server descriptors.
   */
  private BufferedWriter descriptorOut;

  /**
   * Raw import file containing extra-info descriptors.
   */
  private BufferedWriter extrainfoOut;

  /**
   * Raw import file containing bandwidth histories.
   */
  private BufferedWriter bwhistOut;

  /**
   * Raw import file containing consensuses.
   */
  private BufferedWriter consensusOut;

  /**
   * Raw import file containing votes.
   */
  private BufferedWriter voteOut;

  /**
   * Raw import file containing conn-bi-direct stats strings.
   */
  private BufferedWriter connBiDirectOut;

  /**
   * Raw import file containing dirreq stats.
   */
  private BufferedWriter dirReqOut;

  /**
   * Date format to parse timestamps.
   */
  private SimpleDateFormat dateTimeFormat;

  /**
   * The last valid-after time for which we checked whether they have been
   * any network status entries in the database.
   */
  private long lastCheckedStatusEntries;

  /**
   * Set of fingerprints that we imported for the valid-after time in
   * <code>lastCheckedStatusEntries</code>.
   */
  private Set<String> insertedStatusEntries;

  /**
   * Flag that tells us whether we need to check whether a network status
   * entry is already contained in the database or not.
   */
  private boolean separateStatusEntryCheckNecessary;

  private boolean importIntoDatabase;
  private boolean writeRawImportFiles;

  /**
   * Initialize database importer by connecting to the database and
   * preparing statements.
   */
  public RelayDescriptorDatabaseImporter(String connectionURL,
      String rawFilesDirectory) {

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
        this.psSs = conn.prepareStatement("SELECT COUNT(*) "
            + "FROM statusentry WHERE validafter = ?");
        this.psRs = conn.prepareStatement("SELECT COUNT(*) "
            + "FROM statusentry WHERE validafter = ? AND "
            + "fingerprint = ?");
        this.psDs = conn.prepareStatement("SELECT COUNT(*) "
            + "FROM descriptor WHERE descriptor = ?");
        this.psEs = conn.prepareStatement("SELECT COUNT(*) "
            + "FROM extrainfo WHERE extrainfo = ?");
        this.psCs = conn.prepareStatement("SELECT COUNT(*) "
            + "FROM consensus WHERE validafter = ?");
        this.psVs = conn.prepareStatement("SELECT COUNT(*) "
            + "FROM vote WHERE validafter = ? AND dirsource = ?");
        this.psBs = conn.prepareStatement("SELECT COUNT(*) "
            + "FROM connbidirect WHERE source = ? AND statsend = ?");
        this.psQs = conn.prepareStatement("SELECT COUNT(*) "
            + "FROM dirreq_stats WHERE source = ? AND statsend = ?");
        this.psR = conn.prepareStatement("INSERT INTO statusentry "
            + "(validafter, nickname, fingerprint, descriptor, "
            + "published, address, orport, dirport, isauthority, "
            + "isbadexit, isbaddirectory, isexit, isfast, isguard, "
            + "ishsdir, isnamed, isstable, isrunning, isunnamed, "
            + "isvalid, isv2dir, isv3dir, version, bandwidth, ports, "
            + "rawdesc) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, "
            + "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        this.psD = conn.prepareStatement("INSERT INTO descriptor "
            + "(descriptor, nickname, address, orport, dirport, "
            + "fingerprint, bandwidthavg, bandwidthburst, "
            + "bandwidthobserved, platform, published, uptime, "
            + "extrainfo, rawdesc) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, "
            + "?, ?, ?, ?)");
        this.psE = conn.prepareStatement("INSERT INTO extrainfo "
            + "(extrainfo, nickname, fingerprint, published, rawdesc) "
            + "VALUES (?, ?, ?, ?, ?)");
        this.csH = conn.prepareCall("{call insert_bwhist(?, ?, ?, ?, ?, "
            + "?)}");
        this.psC = conn.prepareStatement("INSERT INTO consensus "
            + "(validafter, rawdesc) VALUES (?, ?)");
        this.psV = conn.prepareStatement("INSERT INTO vote "
            + "(validafter, dirsource, rawdesc) VALUES (?, ?, ?)");
        this.psB = conn.prepareStatement("INSERT INTO connbidirect "
            + "(source, statsend, seconds, belownum, readnum, writenum, "
            + "bothnum) VALUES (?, ?, ?, ?, ?, ?, ?)");
        this.psQ = conn.prepareStatement("INSERT INTO dirreq_stats "
            + "(source, statsend, seconds, country, requests) VALUES "
            + "(?, ?, ?, ?, ?)");
        this.psU = conn.prepareStatement("INSERT INTO scheduled_updates "
            + "(date) VALUES (?)");
        this.scheduledUpdates = new HashSet<Long>();
        this.importIntoDatabase = true;
      } catch (SQLException e) {
        this.logger.log(Level.WARNING, "Could not connect to database or "
            + "prepare statements.", e);
      }

      /* Initialize set of fingerprints to remember which status entries
       * we already imported. */
      this.insertedStatusEntries = new HashSet<String>();
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

  private void addDateToScheduledUpdates(long timestamp)
      throws SQLException {
    if (!this.importIntoDatabase) {
      return;
    }
    long dateMillis = 0L;
    try {
      dateMillis = this.dateTimeFormat.parse(
          this.dateTimeFormat.format(timestamp).substring(0, 10)
          + " 00:00:00").getTime();
    } catch (ParseException e) {
      this.logger.log(Level.WARNING, "Internal parsing error.", e);
      return;
    }
    if (!this.scheduledUpdates.contains(dateMillis)) {
      this.psU.setDate(1, new java.sql.Date(dateMillis));
      this.psU.execute();
      this.scheduledUpdates.add(dateMillis);
    }
  }

  /**
   * Insert network status consensus entry into database.
   */
  public void addStatusEntry(long validAfter, String nickname,
      String fingerprint, String descriptor, long published,
      String address, long orPort, long dirPort,
      SortedSet<String> flags, String version, long bandwidth,
      String ports, byte[] rawDescriptor) {
    if (this.importIntoDatabase) {
      try {
        this.addDateToScheduledUpdates(validAfter);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        Timestamp validAfterTimestamp = new Timestamp(validAfter);
        if (lastCheckedStatusEntries != validAfter) {
          this.psSs.setTimestamp(1, validAfterTimestamp, cal);
          ResultSet rs = psSs.executeQuery();
          rs.next();
          if (rs.getInt(1) == 0) {
            separateStatusEntryCheckNecessary = false;
            insertedStatusEntries.clear();
          } else {
            separateStatusEntryCheckNecessary = true;
          }
          rs.close();
          lastCheckedStatusEntries = validAfter;
        }
        boolean alreadyContained = false;
        if (separateStatusEntryCheckNecessary ||
            insertedStatusEntries.contains(fingerprint)) {
          this.psRs.setTimestamp(1, validAfterTimestamp, cal);
          this.psRs.setString(2, fingerprint);
          ResultSet rs = psRs.executeQuery();
          rs.next();
          if (rs.getInt(1) > 0) {
            alreadyContained = true;
          }
          rs.close();
        } else {
          insertedStatusEntries.add(fingerprint);
        }
        if (!alreadyContained) {
          this.psR.clearParameters();
          this.psR.setTimestamp(1, validAfterTimestamp, cal);
          this.psR.setString(2, nickname);
          this.psR.setString(3, fingerprint);
          this.psR.setString(4, descriptor);
          this.psR.setTimestamp(5, new Timestamp(published), cal);
          this.psR.setString(6, address);
          this.psR.setLong(7, orPort);
          this.psR.setLong(8, dirPort);
          this.psR.setBoolean(9, flags.contains("Authority"));
          this.psR.setBoolean(10, flags.contains("BadExit"));
          this.psR.setBoolean(11, flags.contains("BadDirectory"));
          this.psR.setBoolean(12, flags.contains("Exit"));
          this.psR.setBoolean(13, flags.contains("Fast"));
          this.psR.setBoolean(14, flags.contains("Guard"));
          this.psR.setBoolean(15, flags.contains("HSDir"));
          this.psR.setBoolean(16, flags.contains("Named"));
          this.psR.setBoolean(17, flags.contains("Stable"));
          this.psR.setBoolean(18, flags.contains("Running"));
          this.psR.setBoolean(19, flags.contains("Unnamed"));
          this.psR.setBoolean(20, flags.contains("Valid"));
          this.psR.setBoolean(21, flags.contains("V2Dir"));
          this.psR.setBoolean(22, flags.contains("V3Dir"));
          this.psR.setString(23, version);
          this.psR.setLong(24, bandwidth);
          this.psR.setString(25, ports);
          this.psR.setBytes(26, rawDescriptor);
          this.psR.executeUpdate();
          rrsCount++;
          if (rrsCount % autoCommitCount == 0)  {
            this.conn.commit();
          }
        }
      } catch (SQLException e) {
        this.logger.log(Level.WARNING, "Could not add network status "
            + "consensus entry.  We won't make any further SQL requests "
            + "in this execution.", e);
        this.importIntoDatabase = false;
      }
    }
    if (this.writeRawImportFiles) {
      try {
        if (this.statusentryOut == null) {
          new File(rawFilesDirectory).mkdirs();
          this.statusentryOut = new BufferedWriter(new FileWriter(
              rawFilesDirectory + "/statusentry.sql"));
          this.statusentryOut.write(" COPY statusentry (validafter, "
              + "nickname, fingerprint, descriptor, published, address, "
              + "orport, dirport, isauthority, isbadExit, "
              + "isbaddirectory, isexit, isfast, isguard, ishsdir, "
              + "isnamed, isstable, isrunning, isunnamed, isvalid, "
              + "isv2dir, isv3dir, version, bandwidth, ports, rawdesc) "
              + "FROM stdin;\n");
        }
        this.statusentryOut.write(
            this.dateTimeFormat.format(validAfter) + "\t" + nickname
            + "\t" + fingerprint.toLowerCase() + "\t"
            + descriptor.toLowerCase() + "\t"
            + this.dateTimeFormat.format(published) + "\t" + address
            + "\t" + orPort + "\t" + dirPort + "\t"
            + (flags.contains("Authority") ? "t" : "f") + "\t"
            + (flags.contains("BadExit") ? "t" : "f") + "\t"
            + (flags.contains("BadDirectory") ? "t" : "f") + "\t"
            + (flags.contains("Exit") ? "t" : "f") + "\t"
            + (flags.contains("Fast") ? "t" : "f") + "\t"
            + (flags.contains("Guard") ? "t" : "f") + "\t"
            + (flags.contains("HSDir") ? "t" : "f") + "\t"
            + (flags.contains("Named") ? "t" : "f") + "\t"
            + (flags.contains("Stable") ? "t" : "f") + "\t"
            + (flags.contains("Running") ? "t" : "f") + "\t"
            + (flags.contains("Unnamed") ? "t" : "f") + "\t"
            + (flags.contains("Valid") ? "t" : "f") + "\t"
            + (flags.contains("V2Dir") ? "t" : "f") + "\t"
            + (flags.contains("V3Dir") ? "t" : "f") + "\t"
            + (version != null ? version : "\\N") + "\t"
            + (bandwidth >= 0 ? bandwidth : "\\N") + "\t"
            + (ports != null ? ports : "\\N") + "\t");
        this.statusentryOut.write(PGbytea.toPGString(rawDescriptor).
            replaceAll("\\\\", "\\\\\\\\") + "\n");
      } catch (SQLException e) {
        this.logger.log(Level.WARNING, "Could not write network status "
            + "consensus entry to raw database import file.  We won't "
            + "make any further attempts to write raw import files in "
            + "this execution.", e);
        this.writeRawImportFiles = false;
      } catch (IOException e) {
        this.logger.log(Level.WARNING, "Could not write network status "
            + "consensus entry to raw database import file.  We won't "
            + "make any further attempts to write raw import files in "
            + "this execution.", e);
        this.writeRawImportFiles = false;
      }
    }
  }

  /**
   * Insert server descriptor into database.
   */
  public void addServerDescriptor(String descriptor, String nickname,
      String address, int orPort, int dirPort, String relayIdentifier,
      long bandwidthAvg, long bandwidthBurst, long bandwidthObserved,
      String platform, long published, long uptime,
      String extraInfoDigest, byte[] rawDescriptor) {
    if (this.importIntoDatabase) {
      try {
        this.addDateToScheduledUpdates(published);
        this.addDateToScheduledUpdates(
            published + 24L * 60L * 60L * 1000L);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        this.psDs.setString(1, descriptor);
        ResultSet rs = psDs.executeQuery();
        rs.next();
        if (rs.getInt(1) == 0) {
          this.psD.clearParameters();
          this.psD.setString(1, descriptor);
          this.psD.setString(2, nickname);
          this.psD.setString(3, address);
          this.psD.setInt(4, orPort);
          this.psD.setInt(5, dirPort);
          this.psD.setString(6, relayIdentifier);
          this.psD.setLong(7, bandwidthAvg);
          this.psD.setLong(8, bandwidthBurst);
          this.psD.setLong(9, bandwidthObserved);
          /* Remove all non-ASCII characters from the platform string, or
           * we'll make Postgres unhappy.  Sun's JDK and OpenJDK behave
           * differently when creating a new String with a given encoding.
           * That's what the regexp below is for. */
          this.psD.setString(10, new String(platform.getBytes(),
              "US-ASCII").replaceAll("[^\\p{ASCII}]",""));
          this.psD.setTimestamp(11, new Timestamp(published), cal);
          this.psD.setLong(12, uptime);
          this.psD.setString(13, extraInfoDigest);
          this.psD.setBytes(14, rawDescriptor);
          this.psD.executeUpdate();
          rdsCount++;
          if (rdsCount % autoCommitCount == 0)  {
            this.conn.commit();
          }
        }
      } catch (UnsupportedEncodingException e) {
        // US-ASCII is supported for sure
      } catch (SQLException e) {
        this.logger.log(Level.WARNING, "Could not add server "
            + "descriptor.  We won't make any further SQL requests in "
            + "this execution.", e);
        this.importIntoDatabase = false;
      }
    }
    if (this.writeRawImportFiles) {
      try {
        if (this.descriptorOut == null) {
          new File(rawFilesDirectory).mkdirs();
          this.descriptorOut = new BufferedWriter(new FileWriter(
              rawFilesDirectory + "/descriptor.sql"));
          this.descriptorOut.write(" COPY descriptor (descriptor, "
              + "nickname, address, orport, dirport, fingerprint, "
              + "bandwidthavg, bandwidthburst, bandwidthobserved, "
              + "platform, published, uptime, extrainfo, rawdesc) FROM "
              + "stdin;\n");
        }
        this.descriptorOut.write(descriptor.toLowerCase() + "\t"
            + nickname + "\t" + address + "\t" + orPort + "\t" + dirPort
            + "\t" + relayIdentifier + "\t" + bandwidthAvg + "\t"
            + bandwidthBurst + "\t" + bandwidthObserved + "\t"
            + (platform != null && platform.length() > 0
            ? new String(platform.getBytes(), "US-ASCII") : "\\N")
            + "\t" + this.dateTimeFormat.format(published) + "\t"
            + (uptime >= 0 ? uptime : "\\N") + "\t"
            + (extraInfoDigest != null ? extraInfoDigest : "\\N")
            + "\t");
        this.descriptorOut.write(PGbytea.toPGString(rawDescriptor).
            replaceAll("\\\\", "\\\\\\\\") + "\n");
      } catch (UnsupportedEncodingException e) {
        // US-ASCII is supported for sure
      } catch (SQLException e) {
        this.logger.log(Level.WARNING, "Could not write server "
            + "descriptor to raw database import file.  We won't make "
            + "any further attempts to write raw import files in this "
            + "execution.", e);
        this.writeRawImportFiles = false;
      } catch (IOException e) {
        this.logger.log(Level.WARNING, "Could not write server "
            + "descriptor to raw database import file.  We won't make "
            + "any further attempts to write raw import files in this "
            + "execution.", e);
        this.writeRawImportFiles = false;
      }
    }
  }

  /**
   * Insert extra-info descriptor into database.
   */
  public void addExtraInfoDescriptor(String extraInfoDigest,
      String nickname, String fingerprint, long published,
      byte[] rawDescriptor, List<String> bandwidthHistoryLines) {
    if (this.importIntoDatabase) {
      try {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        this.psEs.setString(1, extraInfoDigest);
        ResultSet rs = psEs.executeQuery();
        rs.next();
        if (rs.getInt(1) == 0) {
          this.psE.clearParameters();
          this.psE.setString(1, extraInfoDigest);
          this.psE.setString(2, nickname);
          this.psE.setString(3, fingerprint);
          this.psE.setTimestamp(4, new Timestamp(published), cal);
          this.psE.setBytes(5, rawDescriptor);
          this.psE.executeUpdate();
          resCount++;
          if (resCount % autoCommitCount == 0)  {
            this.conn.commit();
          }
        }
      } catch (SQLException e) {
        this.logger.log(Level.WARNING, "Could not add extra-info "
            + "descriptor.  We won't make any further SQL requests in "
            + "this execution.", e);
        this.importIntoDatabase = false;
      }
    }
    if (this.writeRawImportFiles) {
      try {
        if (this.extrainfoOut == null) {
          new File(rawFilesDirectory).mkdirs();
          this.extrainfoOut = new BufferedWriter(new FileWriter(
              rawFilesDirectory + "/extrainfo.sql"));
          this.extrainfoOut.write(" COPY extrainfo (extrainfo, nickname, "
              + "fingerprint, published, rawdesc) FROM stdin;\n");
        }
        this.extrainfoOut.write(extraInfoDigest.toLowerCase() + "\t"
            + nickname + "\t" + fingerprint.toLowerCase() + "\t"
            + this.dateTimeFormat.format(published) + "\t");
        this.extrainfoOut.write(PGbytea.toPGString(rawDescriptor).
            replaceAll("\\\\", "\\\\\\\\") + "\n");
      } catch (IOException e) {
        this.logger.log(Level.WARNING, "Could not write extra-info "
            + "descriptor to raw database import file.  We won't make "
            + "any further attempts to write raw import files in this "
            + "execution.", e);
        this.writeRawImportFiles = false;
      } catch (SQLException e) {
        this.logger.log(Level.WARNING, "Could not write extra-info "
            + "descriptor to raw database import file.  We won't make "
            + "any further attempts to write raw import files in this "
            + "execution.", e);
        this.writeRawImportFiles = false;
      }
    }
    if (!bandwidthHistoryLines.isEmpty()) {
      this.addBandwidthHistory(fingerprint.toLowerCase(), published,
          bandwidthHistoryLines);
    }
  }

  private static class BigIntArray implements java.sql.Array {

    private final String stringValue;

    public BigIntArray(long[] array, int offset) {
      if (array == null) {
        this.stringValue = "[-1:-1]={0}";
      } else {
        StringBuilder sb = new StringBuilder("[" + offset + ":"
            + (offset + array.length - 1) + "]={");
        for (int i = 0; i < array.length; i++) {
          sb.append((i > 0 ? "," : "") + array[i]);
        }
        sb.append('}');
        this.stringValue = sb.toString();
      }
    }

    public String toString() {
      return stringValue;
    }

    public String getBaseTypeName() {
      return "int8";
    }

    /* The other methods are never called; no need to implement them. */
    public void free() {
      throw new UnsupportedOperationException();
    }
    public Object getArray() {
      throw new UnsupportedOperationException();
    }
    public Object getArray(long index, int count) {
      throw new UnsupportedOperationException();
    }
    public Object getArray(long index, int count,
        Map<String, Class<?>> map) {
      throw new UnsupportedOperationException();
    }
    public Object getArray(Map<String, Class<?>> map) {
      throw new UnsupportedOperationException();
    }
    public int getBaseType() {
      throw new UnsupportedOperationException();
    }
    public ResultSet getResultSet() {
      throw new UnsupportedOperationException();
    }
    public ResultSet getResultSet(long index, int count) {
      throw new UnsupportedOperationException();
    }
    public ResultSet getResultSet(long index, int count,
        Map<String, Class<?>> map) {
      throw new UnsupportedOperationException();
    }
    public ResultSet getResultSet(Map<String, Class<?>> map) {
      throw new UnsupportedOperationException();
    }
  }

  public void addBandwidthHistory(String fingerprint, long published,
      List<String> bandwidthHistoryStrings) {

    /* Split history lines by date and rewrite them so that the date
     * comes first. */
    SortedSet<String> historyLinesByDate = new TreeSet<String>();
    for (String bandwidthHistoryString : bandwidthHistoryStrings) {
      String[] parts = bandwidthHistoryString.split(" ");
      if (parts.length != 6) {
        this.logger.finer("Bandwidth history line does not have expected "
            + "number of elements. Ignoring this line.");
        continue;
      }
      long intervalLength = 0L;
      try {
        intervalLength = Long.parseLong(parts[3].substring(1));
      } catch (NumberFormatException e) {
        this.logger.fine("Bandwidth history line does not have valid "
            + "interval length '" + parts[3] + " " + parts[4] + "'. "
            + "Ignoring this line.");
        continue;
      }
      if (intervalLength != 900L) {
        this.logger.fine("Bandwidth history line does not consist of "
            + "15-minute intervals. Ignoring this line.");
        continue;
      }
      String type = parts[0];
      String intervalEndTime = parts[1] + " " + parts[2];
      long intervalEnd, dateStart;
      try {
        intervalEnd = dateTimeFormat.parse(intervalEndTime).getTime();
        dateStart = dateTimeFormat.parse(parts[1] + " 00:00:00").
            getTime();
      } catch (ParseException e) {
        this.logger.fine("Parse exception while parsing timestamp in "
            + "bandwidth history line. Ignoring this line.");
        continue;
      }
      if (Math.abs(published - intervalEnd) >
          7L * 24L * 60L * 60L * 1000L) {
        this.logger.fine("Extra-info descriptor publication time "
            + dateTimeFormat.format(published) + " and last interval "
            + "time " + intervalEndTime + " in " + type + " line differ "
            + "by more than 7 days! Not adding this line!");
        continue;
      }
      long currentIntervalEnd = intervalEnd;
      StringBuilder sb = new StringBuilder();
      String[] values = parts[5].split(",");
      SortedSet<String> newHistoryLines = new TreeSet<String>();
      try {
        for (int i = values.length - 1; i >= -1; i--) {
          if (i == -1 || currentIntervalEnd < dateStart) {
            sb.insert(0, intervalEndTime + " " + type + " ("
                + intervalLength + " s) ");
            sb.setLength(sb.length() - 1);
            String historyLine = sb.toString();
            newHistoryLines.add(historyLine);
            sb = new StringBuilder();
            dateStart -= 24L * 60L * 60L * 1000L;
            intervalEndTime = dateTimeFormat.format(currentIntervalEnd);
          }
          if (i == -1) {
            break;
          }
          Long.parseLong(values[i]);
          sb.insert(0, values[i] + ",");
          currentIntervalEnd -= intervalLength * 1000L;
        }
      } catch (NumberFormatException e) {
        this.logger.fine("Number format exception while parsing "
            + "bandwidth history line. Ignoring this line.");
        continue;
      }
      historyLinesByDate.addAll(newHistoryLines);
    }

    /* Add split history lines to database. */
    String lastDate = null;
    historyLinesByDate.add("EOL");
    long[] readArray = null, writtenArray = null, dirreadArray = null,
        dirwrittenArray = null;
    int readOffset = 0, writtenOffset = 0, dirreadOffset = 0,
        dirwrittenOffset = 0;
    for (String historyLine : historyLinesByDate) {
      String[] parts = historyLine.split(" ");
      String currentDate = parts[0];
      if (lastDate != null && (historyLine.equals("EOL") ||
          !currentDate.equals(lastDate))) {
        BigIntArray readIntArray = new BigIntArray(readArray,
            readOffset);
        BigIntArray writtenIntArray = new BigIntArray(writtenArray,
            writtenOffset);
        BigIntArray dirreadIntArray = new BigIntArray(dirreadArray,
            dirreadOffset);
        BigIntArray dirwrittenIntArray = new BigIntArray(dirwrittenArray,
            dirwrittenOffset);
        if (this.importIntoDatabase) {
          try {
            long dateMillis = dateTimeFormat.parse(lastDate
                + " 00:00:00").getTime();
            this.addDateToScheduledUpdates(dateMillis);
            this.csH.setString(1, fingerprint);
            this.csH.setDate(2, new java.sql.Date(dateMillis));
            this.csH.setArray(3, readIntArray);
            this.csH.setArray(4, writtenIntArray);
            this.csH.setArray(5, dirreadIntArray);
            this.csH.setArray(6, dirwrittenIntArray);
            this.csH.addBatch();
            rhsCount++;
            if (rhsCount % autoCommitCount == 0)  {
              this.csH.executeBatch();
            }
          } catch (SQLException e) {
            this.logger.log(Level.WARNING, "Could not insert bandwidth "
                + "history line into database.  We won't make any "
                + "further SQL requests in this execution.", e);
            this.importIntoDatabase = false;
          } catch (ParseException e) {
            this.logger.log(Level.WARNING, "Could not insert bandwidth "
                + "history line into database.  We won't make any "
                + "further SQL requests in this execution.", e);
            this.importIntoDatabase = false;
          }
        }
        if (this.writeRawImportFiles) {
          try {
            if (this.bwhistOut == null) {
              new File(rawFilesDirectory).mkdirs();
              this.bwhistOut = new BufferedWriter(new FileWriter(
                  rawFilesDirectory + "/bwhist.sql"));
            }
            this.bwhistOut.write("SELECT insert_bwhist('" + fingerprint
                + "','" + lastDate + "','" + readIntArray.toString()
                + "','" + writtenIntArray.toString() + "','"
                + dirreadIntArray.toString() + "','"
                + dirwrittenIntArray.toString() + "');\n");
          } catch (IOException e) {
            this.logger.log(Level.WARNING, "Could not write bandwidth "
                + "history to raw database import file.  We won't make "
                + "any further attempts to write raw import files in "
                + "this execution.", e);
            this.writeRawImportFiles = false;
          }
        }
        readArray = writtenArray = dirreadArray = dirwrittenArray = null;
      }
      if (historyLine.equals("EOL")) {
        break;
      }
      long lastIntervalTime;
      try {
        lastIntervalTime = dateTimeFormat.parse(parts[0] + " "
            + parts[1]).getTime() - dateTimeFormat.parse(parts[0]
            + " 00:00:00").getTime();
      } catch (ParseException e) {
        continue;
      }
      String[] stringValues = parts[5].split(",");
      long[] longValues = new long[stringValues.length];
      for (int i = 0; i < longValues.length; i++) {
        longValues[i] = Long.parseLong(stringValues[i]);
      }

      int offset = (int) (lastIntervalTime / (15L * 60L * 1000L))
          - longValues.length + 1;
      String type = parts[2];
      if (type.equals("read-history")) {
        readArray = longValues;
        readOffset = offset;
      } else if (type.equals("write-history")) {
        writtenArray = longValues;
        writtenOffset = offset;
      } else if (type.equals("dirreq-read-history")) {
        dirreadArray = longValues;
        dirreadOffset = offset;
      } else if (type.equals("dirreq-write-history")) {
        dirwrittenArray = longValues;
        dirwrittenOffset = offset;
      }
      lastDate = currentDate;
    }
  }

  /**
   * Insert network status consensus into database.
   */
  public void addConsensus(long validAfter, byte[] rawDescriptor) {
    if (this.importIntoDatabase) {
      try {
        this.addDateToScheduledUpdates(validAfter);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        Timestamp validAfterTimestamp = new Timestamp(validAfter);
        this.psCs.setTimestamp(1, validAfterTimestamp, cal);
        ResultSet rs = psCs.executeQuery();
        rs.next();
        if (rs.getInt(1) == 0) {
          this.psC.clearParameters();
          this.psC.setTimestamp(1, validAfterTimestamp, cal);
          this.psC.setBytes(2, rawDescriptor);
          this.psC.executeUpdate();
          rcsCount++;
          if (rcsCount % autoCommitCount == 0)  {
            this.conn.commit();
          }
        }
      } catch (SQLException e) {
        this.logger.log(Level.WARNING, "Could not add network status "
            + "consensus.  We won't make any further SQL requests in "
            + "this execution.", e);
        this.importIntoDatabase = false;
      }
    }
    if (this.writeRawImportFiles) {
      try {
        if (this.consensusOut == null) {
          new File(rawFilesDirectory).mkdirs();
          this.consensusOut = new BufferedWriter(new FileWriter(
              rawFilesDirectory + "/consensus.sql"));
          this.consensusOut.write(" COPY consensus (validafter, rawdesc) "
              + "FROM stdin;\n");
        }
        String validAfterString = this.dateTimeFormat.format(validAfter);
        this.consensusOut.write(validAfterString + "\t");
        this.consensusOut.write(PGbytea.toPGString(rawDescriptor).
            replaceAll("\\\\", "\\\\\\\\") + "\n");
      } catch (SQLException e) {
        this.logger.log(Level.WARNING, "Could not write network status "
            + "consensus to raw database import file.  We won't make "
            + "any further attempts to write raw import files in this "
            + "execution.", e);
        this.writeRawImportFiles = false;
      } catch (IOException e) {
        this.logger.log(Level.WARNING, "Could not write network status "
            + "consensus to raw database import file.  We won't make "
            + "any further attempts to write raw import files in this "
            + "execution.", e);
        this.writeRawImportFiles = false;
      }
    }
  }

  /**
   * Insert network status vote into database.
   */
  public void addVote(long validAfter, String dirSource,
      byte[] rawDescriptor) {
    if (this.importIntoDatabase) {
      try {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        Timestamp validAfterTimestamp = new Timestamp(validAfter);
        this.psVs.setTimestamp(1, validAfterTimestamp, cal);
        this.psVs.setString(2, dirSource);
        ResultSet rs = psVs.executeQuery();
        rs.next();
        if (rs.getInt(1) == 0) {
          this.psV.clearParameters();
          this.psV.setTimestamp(1, validAfterTimestamp, cal);
          this.psV.setString(2, dirSource);
          this.psV.setBytes(3, rawDescriptor);
          this.psV.executeUpdate();
          rvsCount++;
          if (rvsCount % autoCommitCount == 0)  {
            this.conn.commit();
          }
        }
      } catch (SQLException e) {
        this.logger.log(Level.WARNING, "Could not add network status "
            + "vote.  We won't make any further SQL requests in this "
            + "execution.", e);
        this.importIntoDatabase = false;
      }
    }
    if (this.writeRawImportFiles) {
      try {
        if (this.voteOut == null) {
          new File(rawFilesDirectory).mkdirs();
          this.voteOut = new BufferedWriter(new FileWriter(
              rawFilesDirectory + "/vote.sql"));
          this.voteOut.write(" COPY vote (validafter, dirsource, "
              + "rawdesc) FROM stdin;\n");
        }
        String validAfterString = this.dateTimeFormat.format(validAfter);
        this.voteOut.write(validAfterString + "\t" + dirSource + "\t");
        this.voteOut.write(PGbytea.toPGString(rawDescriptor).
            replaceAll("\\\\", "\\\\\\\\") + "\n");
      } catch (SQLException e) {
        this.logger.log(Level.WARNING, "Could not write network status "
            + "vote to raw database import file.  We won't make any "
            + "further attempts to write raw import files in this "
            + "execution.", e);
        this.writeRawImportFiles = false;
      } catch (IOException e) {
        this.logger.log(Level.WARNING, "Could not write network status "
            + "vote to raw database import file.  We won't make any "
            + "further attempts to write raw import files in this "
            + "execution.", e);
        this.writeRawImportFiles = false;
      }
    }
  }

  /**
   * Insert a conn-bi-direct stats string into the database.
   */
  public void addConnBiDirect(String source, long statsEndMillis,
      long seconds, long below, long read, long write, long both) {
    String statsEnd = this.dateTimeFormat.format(statsEndMillis);
    if (this.importIntoDatabase) {
      try {
        this.addDateToScheduledUpdates(statsEndMillis);
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

  /**
   * Adds observations on the number of directory requests by country as
   * seen on a directory at a given date to the database.
   */
  public void addDirReqStats(String source, long statsEndMillis,
      long seconds, Map<String, String> dirReqsPerCountry) {
    String statsEnd = this.dateTimeFormat.format(statsEndMillis);
    if (this.importIntoDatabase) {
      try {
        this.addDateToScheduledUpdates(statsEndMillis);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        Timestamp statsEndTimestamp = new Timestamp(statsEndMillis);
        this.psQs.setString(1, source);
        this.psQs.setTimestamp(2, statsEndTimestamp, cal);
        ResultSet rs = psQs.executeQuery();
        rs.next();
        if (rs.getInt(1) == 0) {
          for (Map.Entry<String, String> e :
              dirReqsPerCountry.entrySet()) {
            this.psQ.clearParameters();
            this.psQ.setString(1, source);
            this.psQ.setTimestamp(2, statsEndTimestamp, cal);
            this.psQ.setLong(3, seconds);
            this.psQ.setString(4, e.getKey());
            this.psQ.setLong(5, Long.parseLong(e.getValue()));
            this.psQ.executeUpdate();
            rqsCount++;
            if (rqsCount % autoCommitCount == 0)  {
              this.conn.commit();
            }
          }
        }
      } catch (SQLException e) {
        this.logger.log(Level.WARNING, "Could not add dirreq stats.  We "
            + "won't make any further SQL requests in this execution.",
            e);
        this.importIntoDatabase = false;
      }
    }
    if (this.writeRawImportFiles) {
      try {
        if (this.dirReqOut == null) {
          new File(rawFilesDirectory).mkdirs();
          this.dirReqOut = new BufferedWriter(new FileWriter(
              rawFilesDirectory + "/dirreq_stats.sql"));
          this.dirReqOut.write(" COPY dirreq_stats (source, statsend, "
              + "seconds, country, requests) FROM stdin;\n");
        }
        for (Map.Entry<String, String> e :
            dirReqsPerCountry.entrySet()) {
          this.dirReqOut.write(source + "\t" + statsEnd + "\t" + seconds
              + "\t" + e.getKey() + "\t" + e.getValue() + "\n");
        }
      } catch (IOException e) {
        this.logger.log(Level.WARNING, "Could not write dirreq stats to "
            + "raw database import file.  We won't make any further "
            + "attempts to write raw import files in this execution.", e);
        this.writeRawImportFiles = false;
      }
    }
  }

  /**
   * Close the relay descriptor database connection.
   */
  public void closeConnection() {

    /* Log stats about imported descriptors. */
    this.logger.info(String.format("Finished importing relay "
        + "descriptors: %d consensuses, %d network status entries, %d "
        + "votes, %d server descriptors, %d extra-info descriptors, %d "
        + "bandwidth history elements, %d dirreq stats elements, and %d "
        + "conn-bi-direct stats lines", rcsCount, rrsCount, rvsCount,
        rdsCount, resCount, rhsCount, rqsCount, rbsCount));

    /* Insert scheduled updates a second time, just in case the refresh
     * run has started since inserting them the first time in which case
     * it will miss the data inserted afterwards.  We cannot, however,
     * insert them only now, because if a Java execution fails at a random
     * point, we might have added data, but not the corresponding dates to
     * update statistics. */
    if (this.importIntoDatabase) {
      try {
        for (long dateMillis : this.scheduledUpdates) {
          this.psU.setDate(1, new java.sql.Date(dateMillis));
          this.psU.execute();
        }
      } catch (SQLException e) {
        this.logger.log(Level.WARNING, "Could not add scheduled dates "
            + "for the next refresh run.", e);
      }
    }

    /* Commit any stragglers before closing. */
    if (this.conn != null) {
      try {
        this.csH.executeBatch();

        this.conn.commit();
      } catch (SQLException e)  {
        this.logger.log(Level.WARNING, "Could not commit final records to "
            + "database", e);
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
      if (this.statusentryOut != null) {
        this.statusentryOut.write("\\.\n");
        this.statusentryOut.close();
      }
      if (this.descriptorOut != null) {
        this.descriptorOut.write("\\.\n");
        this.descriptorOut.close();
      }
      if (this.extrainfoOut != null) {
        this.extrainfoOut.write("\\.\n");
        this.extrainfoOut.close();
      }
      if (this.bwhistOut != null) {
        this.bwhistOut.write("\\.\n");
        this.bwhistOut.close();
      }
      if (this.consensusOut != null) {
        this.consensusOut.write("\\.\n");
        this.consensusOut.close();
      }
      if (this.voteOut != null) {
        this.voteOut.write("\\.\n");
        this.voteOut.close();
      }
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

