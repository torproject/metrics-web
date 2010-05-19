import java.sql.*;
import java.util.*;
import java.util.logging.*;

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
  private int rrsCount = 0;

  /**
   * Relay descriptor database connection.
   */
  private Connection conn;

  /**
   * Prepared statement to check whether a given network status consensus
   * entry has been imported into the database before.
   */
  private PreparedStatement psRs;

  /**
   * Prepared statement to check whether a given server descriptor has
   * been imported into the database before.
   */
  private PreparedStatement psDs;

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
   * Logger for this class.
   */
  private Logger logger;

  /**
   * Initialize database importer by connecting to the database and
   * preparing statements.
   */
  public RelayDescriptorDatabaseImporter(String connectionURL) {

    /* Initialize logger. */
    this.logger = Logger.getLogger(
        RelayDescriptorDatabaseImporter.class.getName());

    try {
      /* Connect to database. */
      this.conn = DriverManager.getConnection(connectionURL);

      /* Turn autocommit off */
      this.conn.setAutoCommit(false);

      /* Prepare statements. */
      this.psRs = conn.prepareStatement("SELECT COUNT(*) "
          + "FROM statusentry WHERE validafter = ? AND descriptor = ?");
      this.psDs = conn.prepareStatement("SELECT COUNT(*) "
          + "FROM descriptor WHERE descriptor = ?");
      this.psR = conn.prepareStatement("INSERT INTO statusentry "
          + "(validafter, descriptor, isauthority, isbadexit, "
          + "isbaddirectory, isexit, isfast, isguard, ishsdir, isnamed, "
          + "isstable, isrunning, isunnamed, isvalid, isv2dir, isv3dir) "
          + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
      this.psD = conn.prepareStatement("INSERT INTO descriptor "
          + "(descriptor, address, orport, dirport, bandwidthavg, "
          + "bandwidthburst, bandwidthobserved, platform, published, "
          + "uptime) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

    } catch (SQLException e) {
      this.logger.log(Level.WARNING, "Could not connect to database or "
          + "prepare statements.", e);
    }
  }

  /**
   * Insert network status consensus entry into database.
   */
  public void addStatusEntry(long validAfter, String descriptor,
      SortedSet<String> flags) {
    if (this.psRs == null || this.psR == null) {
      return;
    }
    try {
      Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
      Timestamp validAfterTimestamp = new Timestamp(validAfter);
      this.psRs.setTimestamp(1, validAfterTimestamp, cal);
      this.psRs.setString(2, descriptor);
      ResultSet rs = psRs.executeQuery();
      rs.next();
      if (rs.getInt(1) > 0) {
        return;
      }
      this.psR.clearParameters();
      this.psR.setTimestamp(1, validAfterTimestamp, cal);
      this.psR.setString(2, descriptor);
      this.psR.setBoolean(3, flags.contains("Authority"));
      this.psR.setBoolean(4, flags.contains("BadExit"));
      this.psR.setBoolean(5, flags.contains("BadDirectory"));
      this.psR.setBoolean(6, flags.contains("Exit"));
      this.psR.setBoolean(7, flags.contains("Fast"));
      this.psR.setBoolean(8, flags.contains("Guard"));
      this.psR.setBoolean(9, flags.contains("HSDir"));
      this.psR.setBoolean(10, flags.contains("Named"));
      this.psR.setBoolean(11, flags.contains("Stable"));
      this.psR.setBoolean(12, flags.contains("Running"));
      this.psR.setBoolean(13, flags.contains("Unnamed"));
      this.psR.setBoolean(14, flags.contains("Valid"));
      this.psR.setBoolean(15, flags.contains("V2Dir"));
      this.psR.setBoolean(16, flags.contains("V3Dir"));
      this.psR.executeUpdate();
      rrsCount++;
      if (rrsCount % autoCommitCount == 0)  {
        this.conn.commit();
        rrsCount = 0;
      }

    } catch (SQLException e) {
      this.logger.log(Level.WARNING, "Could not add network status "
          + "consensus entry.", e);
    }
  }

  /**
   * Insert server descriptor into database.
   */
  public void addServerDescriptor(String descriptor, String address,
      int orPort, int dirPort, long bandwidthAvg, long bandwidthBurst,
      long bandwidthObserved, String platform, long published,
      long uptime) {
    if (this.psDs == null || this.psD == null) {
      return;
    }
    try {
      Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
      this.psDs.setString(1, descriptor);
      ResultSet rs = psDs.executeQuery();
      rs.next();
      if (rs.getInt(1) > 0) {
        return;
      }
      this.psD.clearParameters();
      this.psD.setString(1, descriptor);
      this.psD.setString(2, address);
      this.psD.setInt(3, orPort);
      this.psD.setInt(4, dirPort);
      this.psD.setLong(5, bandwidthAvg);
      this.psD.setLong(6, bandwidthBurst);
      this.psD.setLong(7, bandwidthObserved);
      this.psD.setString(8, platform);
      this.psD.setTimestamp(9, new Timestamp(published), cal);
      this.psD.setLong(10, uptime);
      this.psD.executeUpdate();
      rdsCount++;
      if (rdsCount % autoCommitCount == 0)  {
        this.conn.commit();
        rdsCount = 0;
      }

    } catch (SQLException e) {
      this.logger.log(Level.WARNING, "Could not add server descriptor.",
          e);
    }
  }

  /**
   * Close the relay descriptor database connection.
   */
  public void closeConnection() {
    /* commit any stragglers before closing */
    try {
      this.conn.commit();
    }
    catch (SQLException e)  {
      this.logger.log(Level.WARNING, "Could not commit final records to database", e);
    }
    try {
      this.conn.close();
    } catch (SQLException e) {
      this.logger.log(Level.WARNING, "Could not close database "
          + "connection.", e);
    }
  }
}
