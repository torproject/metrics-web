/* Copyright 2017--2020 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.stats.servers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TimeZone;

/** Database wrapper to connect to the database, insert data, run the stored
 * procedure for aggregating data, and query aggregated data as output. */
class Database implements AutoCloseable {

  /** Database connection string. */
  private static final String jdbcString = String.format(
      "jdbc:postgresql://localhost/ipv6servers?user=%s&password=%s",
      System.getProperty("metrics.dbuser", "metrics"),
      System.getProperty("metrics.dbpass", "password"));

  /** Connection object for all interactions with the database. */
  private Connection connection;

  /** Cache for the mapping of platform strings to identifiers in the database.
   * Initialized at startup and kept in sync with the database. */
  private Map<String, Integer> platformsCache = new HashMap<>();

  /** Cache for the mapping of version strings to identifiers in the database.
   * Initialized at startup and kept in sync with the database. */
  private Map<String, Integer> versionsCache = new HashMap<>();

  /** Cache of version strings that have been recommended as server version at
   * least once in a consensus. Initialized at startup and kept in sync with the
   * database. */
  private Set<String> recommendedVersions = new HashSet<>();

  /** Cache for the mapping of relay flags to identifiers in the database.
   * Initialized at startup and kept in sync with the database. */
  private Map<String, Integer> flagsCache = new HashMap<>();

  /** Prepared statement for inserting a platform string into the platforms
   * table. */
  private PreparedStatement psPlatformsInsert;

  /** Prepared statement for inserting a version string into the versions
   * table. */
  private PreparedStatement psVersionsInsert;

  /** Prepared statement for updating a version in the versions table. */
  private PreparedStatement psVersionsUpdate;

  /** Prepared statement for inserting a flag into the flags table. */
  private PreparedStatement psFlagsInsert;

  /** Prepared statement for finding out whether a given server descriptor is
   * already contained in the server_descriptors table. */
  private PreparedStatement psServerDescriptorsSelect;

  /** Prepared statement for inserting a server descriptor into the
   * server_descriptors table. */
  private PreparedStatement psServerDescriptorsInsert;

  /** Prepared statement for checking whether a status has been inserted into
   * the statuses table before. */
  private PreparedStatement psStatusesSelect;

  /** Prepared statement for inserting a status (without entries, yet) into
   * the statuses table. */
  private PreparedStatement psStatusesInsert;

  /** Prepared statement for inserting a status entry into the status_entries
   * table. */
  private PreparedStatement psStatusEntriesInsert;

  /** Create a new Database instance and prepare for inserting or querying
   * data. */
  Database() throws SQLException {
    this.connect();
    this.prepareStatements();
    this.initializeCaches();
  }

  private void connect() throws SQLException {
    this.connection = DriverManager.getConnection(jdbcString);
    this.connection.setAutoCommit(false);
  }

  private void prepareStatements() throws SQLException {
    this.psPlatformsInsert = this.connection.prepareStatement(
        "INSERT INTO platforms (platform_string) VALUES (?)",
        Statement.RETURN_GENERATED_KEYS);
    this.psVersionsInsert = this.connection.prepareStatement(
        "INSERT INTO versions (version_string, recommended) VALUES (?, ?)",
        Statement.RETURN_GENERATED_KEYS);
    this.psVersionsUpdate = this.connection.prepareStatement(
        "UPDATE versions SET recommended = TRUE WHERE version_id = ?");
    this.psFlagsInsert = this.connection.prepareStatement(
        "INSERT INTO flags (flag_id, flag_string) VALUES (?, ?)");
    this.psServerDescriptorsSelect = this.connection.prepareStatement(
        "SELECT EXISTS (SELECT 1 FROM server_descriptors "
        + "WHERE descriptor_digest_sha1 = decode(?, 'hex'))");
    this.psServerDescriptorsInsert = this.connection.prepareStatement(
        "INSERT INTO server_descriptors (descriptor_digest_sha1, platform_id, "
        + "version_id, advertised_bandwidth_bytes, announced_ipv6, "
        + "exiting_ipv6_relay) VALUES (decode(?, 'hex'), ?, ?, ?, ?, ?)");
    this.psStatusesSelect = this.connection.prepareStatement(
        "SELECT EXISTS (SELECT 1 FROM statuses "
        + "WHERE server = CAST(? AS server_enum) AND valid_after = ?)");
    this.psStatusesInsert = this.connection.prepareStatement(
        "INSERT INTO statuses (server, valid_after, running_count, "
        + "consensus_weight_sum, guard_weight_sum, middle_weight_sum, "
        + "exit_weight_sum) VALUES (CAST(? AS server_enum), ?, ?, ?, ?, ?, ?)",
        Statement.RETURN_GENERATED_KEYS);
    this.psStatusEntriesInsert = this.connection.prepareStatement(
        "INSERT INTO status_entries (status_id, descriptor_digest_sha1, "
        + "flags, reachable_ipv6_relay, consensus_weight, guard_weight, "
        + "middle_weight, exit_weight) "
        + "VALUES (?, decode(?, 'hex'), ?, ?, ?, ?, ?, ?)");
  }

  private void initializeCaches() throws SQLException {
    Statement st = this.connection.createStatement();
    String queryString = "SELECT platform_id, platform_string FROM platforms";
    try (ResultSet rs = st.executeQuery(queryString)) {
      while (rs.next()) {
        this.platformsCache.put(
            rs.getString("platform_string"), rs.getInt("platform_id"));
      }
    }
    st = this.connection.createStatement();
    queryString = "SELECT version_id, version_string, recommended "
        + "FROM versions";
    try (ResultSet rs = st.executeQuery(queryString)) {
      while (rs.next()) {
        String version = rs.getString("version_string");
        int versionId = rs.getInt("version_id");
        boolean recommended = rs.getBoolean("recommended");
        this.versionsCache.put(version, versionId);
        if (recommended) {
          this.recommendedVersions.add(version);
        }
      }
    }
    st = this.connection.createStatement();
    queryString = "SELECT flag_id, flag_string FROM flags";
    try (ResultSet rs = st.executeQuery(queryString)) {
      while (rs.next()) {
        this.flagsCache.put(
            rs.getString("flag_string"), rs.getInt("flag_id"));
      }
    }
  }

  /** Insert a server descriptor into the server_descriptors table. */
  void insertServerDescriptor(
      Ipv6ServerDescriptor serverDescriptor) throws SQLException {
    this.psServerDescriptorsSelect.clearParameters();
    this.psServerDescriptorsSelect.setString(1,
        serverDescriptor.digest);
    try (ResultSet rs = psServerDescriptorsSelect.executeQuery()) {
      if (rs.next()) {
        if (rs.getBoolean(1)) {
          /* Server descriptor is already contained. */
          return;
        }
      }
    }
    this.psServerDescriptorsInsert.clearParameters();
    this.psServerDescriptorsInsert.setString(1, serverDescriptor.digest);
    if (null != serverDescriptor.platform) {
      this.psServerDescriptorsInsert.setInt(2,
          this.selectOrInsertPlatform(serverDescriptor.platform));
    } else {
      this.psServerDescriptorsInsert.setNull(2, Types.INTEGER);
    }
    if (null != serverDescriptor.version) {
      this.psServerDescriptorsInsert.setInt(3,
          this.selectOrInsertVersion(serverDescriptor.version, false));
    } else {
      this.psServerDescriptorsInsert.setNull(3, Types.INTEGER);
    }
    this.psServerDescriptorsInsert.setInt(4,
        serverDescriptor.advertisedBandwidth);
    this.psServerDescriptorsInsert.setBoolean(5, serverDescriptor.announced);
    this.psServerDescriptorsInsert.setBoolean(6, serverDescriptor.exiting);
    this.psServerDescriptorsInsert.execute();
  }

  /** Return the platform identifier for a given platform string, either from
   * our local cache, from a database query, or after inserting it into the
   * platforms table. */
  int selectOrInsertPlatform(String platform) throws SQLException {
    if (!this.platformsCache.containsKey(platform)) {
      int platformId = -1;
      this.psPlatformsInsert.clearParameters();
      this.psPlatformsInsert.setString(1, platform);
      this.psPlatformsInsert.execute();
      try (ResultSet rs = this.psPlatformsInsert.getGeneratedKeys()) {
        if (rs.next()) {
          platformId = rs.getInt(1);
        }
      }
      if (platformId < 0) {
        throw new SQLException("Could not retrieve auto-generated key for "
            + "new platforms entry.");
      }
      this.platformsCache.put(platform, platformId);
    }
    return this.platformsCache.get(platform);
  }

  /** Return the version identifier for a given version string, either from our
   * local cache, from a database query, or after inserting it into the versions
   * table. */
  int selectOrInsertVersion(String version, boolean recommended)
      throws SQLException {
    if (!this.versionsCache.containsKey(version)) {
      int versionId = -1;
      this.psVersionsInsert.clearParameters();
      this.psVersionsInsert.setString(1, version);
      this.psVersionsInsert.setBoolean(2, recommended);
      this.psVersionsInsert.execute();
      try (ResultSet rs = this.psVersionsInsert.getGeneratedKeys()) {
        if (rs.next()) {
          versionId = rs.getInt(1);
        }
      }
      if (versionId < 0) {
        throw new SQLException("Could not retrieve auto-generated key for "
            + "new versions entry.");
      }
      this.versionsCache.put(version, versionId);
      if (recommended) {
        this.recommendedVersions.add(version);
      }
    }
    if (recommended && !this.recommendedVersions.contains(version)) {
      int versionId = this.versionsCache.get(version);
      this.psVersionsUpdate.clearParameters();
      this.psVersionsUpdate.setInt(1, versionId);
      this.psVersionsUpdate.execute();
      this.recommendedVersions.add(version);
    }
    return this.versionsCache.get(version);
  }

  /** Insert a status and all contained entries into the statuses and
   * status_entries table. */
  void insertStatus(Ipv6NetworkStatus networkStatus) throws SQLException {
    this.insertRecommendedVersions(networkStatus.recommendedVersions);
    this.psStatusesSelect.clearParameters();
    this.psStatusesSelect.setString(1,
        networkStatus.isRelay ? "relay" : "bridge");
    Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"),
        Locale.US);
    this.psStatusesSelect.setTimestamp(2,
        Timestamp.from(ZonedDateTime.of(networkStatus.timestamp,
        ZoneId.of("UTC")).toInstant()), calendar);
    try (ResultSet rs = this.psStatusesSelect.executeQuery()) {
      if (rs.next()) {
        if (rs.getBoolean(1)) {
          /* Status is already contained. */
          return;
        }
      }
    }
    int statusId = -1;
    this.psStatusesInsert.clearParameters();
    this.psStatusesInsert.setString(1,
        networkStatus.isRelay ? "relay" : "bridge");
    this.psStatusesInsert.setTimestamp(2,
        Timestamp.from(ZonedDateTime.of(networkStatus.timestamp,
        ZoneId.of("UTC")).toInstant()), calendar);
    this.psStatusesInsert.setInt(3, networkStatus.running);
    if (null != networkStatus.totalConsensusWeight) {
      this.psStatusesInsert.setFloat(4, networkStatus.totalConsensusWeight);
    } else {
      this.psStatusesInsert.setNull(4, Types.FLOAT);
    }
    if (null != networkStatus.totalGuardWeight) {
      this.psStatusesInsert.setFloat(5, networkStatus.totalGuardWeight);
    } else {
      this.psStatusesInsert.setNull(5, Types.FLOAT);
    }
    if (null != networkStatus.totalMiddleWeight) {
      this.psStatusesInsert.setFloat(6, networkStatus.totalMiddleWeight);
    } else {
      this.psStatusesInsert.setNull(6, Types.FLOAT);
    }
    if (null != networkStatus.totalExitWeight) {
      this.psStatusesInsert.setFloat(7, networkStatus.totalExitWeight);
    } else {
      this.psStatusesInsert.setNull(7, Types.FLOAT);
    }
    this.psStatusesInsert.execute();
    try (ResultSet rs = this.psStatusesInsert.getGeneratedKeys()) {
      if (rs.next()) {
        statusId = rs.getInt(1);
      }
    }
    if (statusId < 0) {
      throw new SQLException("Could not retrieve auto-generated key for new "
          + "statuses entry.");
    }
    for (Ipv6NetworkStatus.Entry entry : networkStatus.entries) {
      this.insertStatusEntry(statusId, entry);
    }
    this.psStatusEntriesInsert.executeBatch();
  }


  void insertRecommendedVersions(List<String> versions)
      throws SQLException {
    if (null != versions
        && !this.versionsCache.keySet().containsAll(versions)) {
      for (String version : versions) {
        this.selectOrInsertVersion(version, true);
      }
    }
  }

  /** Insert a status entry into the status_entries table. */
  void insertStatusEntry(int statusId, Ipv6NetworkStatus.Entry entry)
      throws SQLException {
    this.insertFlags(entry.flags);
    int flags = 0;
    if (null != entry.flags) {
      for (String flag : entry.flags) {
        int flagId = this.flagsCache.get(flag);
        flags |= 1 << flagId;
      }
    }
    this.psStatusEntriesInsert.clearParameters();
    this.psStatusEntriesInsert.setInt(1, statusId);
    this.psStatusEntriesInsert.setString(2, entry.digest);
    this.psStatusEntriesInsert.setInt(3, flags);
    this.psStatusEntriesInsert.setBoolean(4, entry.reachable);
    if (null != entry.consensusWeight) {
      this.psStatusEntriesInsert.setFloat(5, entry.consensusWeight);
    } else {
      this.psStatusEntriesInsert.setNull(5, Types.FLOAT);
    }
    if (null != entry.guardWeight) {
      this.psStatusEntriesInsert.setFloat(6, entry.guardWeight);
    } else {
      this.psStatusEntriesInsert.setNull(6, Types.FLOAT);
    }
    if (null != entry.middleWeight) {
      this.psStatusEntriesInsert.setFloat(7, entry.middleWeight);
    } else {
      this.psStatusEntriesInsert.setNull(7, Types.FLOAT);
    }
    if (null != entry.exitWeight) {
      this.psStatusEntriesInsert.setFloat(8, entry.exitWeight);
    } else {
      this.psStatusEntriesInsert.setNull(8, Types.FLOAT);
    }
    this.psStatusEntriesInsert.addBatch();
  }

  void insertFlags(SortedSet<String> flags) throws SQLException {
    if (null != flags && !this.flagsCache.keySet().containsAll(flags)) {
      for (String flag : flags) {
        if (!this.flagsCache.containsKey(flag)) {
          int flagId = this.flagsCache.size();
          this.psFlagsInsert.clearParameters();
          this.psFlagsInsert.setInt(1, flagId);
          this.psFlagsInsert.setString(2, flag);
          this.psFlagsInsert.execute();
          this.flagsCache.put(flag, flagId);
        }
      }
    }
  }

  /** Call the aggregate() function to aggregate rows from the status_entries
   * and server_descriptors tables into the aggregated_* tables. */
  void aggregate() throws SQLException {
    Statement st = this.connection.createStatement();
    st.executeQuery("SELECT aggregate()");
  }

  /** Roll back any changes made in this execution. */
  void rollback() throws SQLException {
    this.connection.rollback();
  }

  /** Commit all changes made in this execution. */
  void commit() throws SQLException {
    this.connection.commit();
  }

  /** Query the servers_ipv6 view. */
  List<String[]> queryServersIpv6() throws SQLException {
    List<String[]> statistics = new ArrayList<>();
    String columns = "valid_after_date, server, guard_relay, exit_relay, "
        + "announced_ipv6, exiting_ipv6_relay, reachable_ipv6_relay, "
        + "server_count_sum_avg, advertised_bandwidth_bytes_sum_avg";
    statistics.add(columns.split(", "));
    Statement st = this.connection.createStatement();
    Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"),
        Locale.US);
    String queryString = "SELECT " + columns + " FROM ipv6servers";
    try (ResultSet rs = st.executeQuery(queryString)) {
      while (rs.next()) {
        String[] outputLine = new String[9];
        outputLine[0] = rs.getDate("valid_after_date", calendar)
            .toLocalDate().toString();
        outputLine[1] = rs.getString("server");
        outputLine[2] = rs.getString("guard_relay");
        outputLine[3] = rs.getString("exit_relay");
        outputLine[4] = rs.getString("announced_ipv6");
        outputLine[5] = rs.getString("exiting_ipv6_relay");
        outputLine[6] = rs.getString("reachable_ipv6_relay");
        outputLine[7] = getLongFromResultSet(rs, "server_count_sum_avg");
        outputLine[8] = getLongFromResultSet(rs,
            "advertised_bandwidth_bytes_sum_avg");
        statistics.add(outputLine);
      }
    }
    return statistics;
  }

  /** Query the bandwidth_advbw view. */
  List<String[]> queryAdvbw() throws SQLException {
    List<String[]> statistics = new ArrayList<>();
    String columns = "date, isexit, isguard, advbw";
    statistics.add(columns.split(", "));
    Statement st = this.connection.createStatement();
    Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"),
        Locale.US);
    String queryString = "SELECT " + columns + " FROM bandwidth_advbw";
    try (ResultSet rs = st.executeQuery(queryString)) {
      while (rs.next()) {
        String[] outputLine = new String[4];
        outputLine[0] = rs.getDate("date", calendar).toLocalDate().toString();
        outputLine[1] = rs.getString("isexit");
        outputLine[2] = rs.getString("isguard");
        outputLine[3] = getLongFromResultSet(rs, "advbw");
        statistics.add(outputLine);
      }
    }
    return statistics;
  }

  /** Query the servers_networksize view. */
  List<String[]> queryNetworksize() throws SQLException {
    List<String[]> statistics = new ArrayList<>();
    String columns = "date, relays, bridges";
    statistics.add(columns.split(", "));
    Statement st = this.connection.createStatement();
    Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"),
        Locale.US);
    String queryString = "SELECT " + columns + " FROM servers_networksize";
    try (ResultSet rs = st.executeQuery(queryString)) {
      while (rs.next()) {
        String[] outputLine = new String[3];
        outputLine[0] = rs.getDate("date", calendar).toLocalDate().toString();
        outputLine[1] = getLongFromResultSet(rs, "relays");
        outputLine[2] = getLongFromResultSet(rs, "bridges");
        statistics.add(outputLine);
      }
    }
    return statistics;
  }

  /** Query the servers_relayflags view. */
  List<String[]> queryRelayflags() throws SQLException {
    List<String[]> statistics = new ArrayList<>();
    String columns = "date, flag, relays";
    statistics.add(columns.split(", "));
    Statement st = this.connection.createStatement();
    Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"),
        Locale.US);
    String queryString = "SELECT " + columns + " FROM servers_relayflags";
    try (ResultSet rs = st.executeQuery(queryString)) {
      while (rs.next()) {
        String[] outputLine = new String[3];
        outputLine[0] = rs.getDate("date", calendar).toLocalDate().toString();
        outputLine[1] = rs.getString("flag");
        outputLine[2] = getLongFromResultSet(rs, "relays");
        statistics.add(outputLine);
      }
    }
    return statistics;
  }

  /** Query the servers_versions view. */
  List<String[]> queryVersions() throws SQLException {
    List<String[]> statistics = new ArrayList<>();
    String columns = "date, version, relays";
    statistics.add(columns.split(", "));
    Statement st = this.connection.createStatement();
    Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"),
        Locale.US);
    String queryString = "SELECT " + columns + " FROM servers_versions";
    try (ResultSet rs = st.executeQuery(queryString)) {
      while (rs.next()) {
        String[] outputLine = new String[3];
        outputLine[0] = rs.getDate("date", calendar).toLocalDate().toString();
        outputLine[1] = rs.getString("version");
        outputLine[2] = getLongFromResultSet(rs, "relays");
        statistics.add(outputLine);
      }
    }
    return statistics;
  }

  /** Query the servers_platforms view. */
  List<String[]> queryPlatforms() throws SQLException {
    List<String[]> statistics = new ArrayList<>();
    String columns = "date, platform, relays";
    statistics.add(columns.split(", "));
    Statement st = this.connection.createStatement();
    Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"),
        Locale.US);
    String queryString = "SELECT " + columns + " FROM servers_platforms";
    try (ResultSet rs = st.executeQuery(queryString)) {
      while (rs.next()) {
        String[] outputLine = new String[3];
        outputLine[0] = rs.getDate("date", calendar).toLocalDate().toString();
        outputLine[1] = rs.getString("platform");
        outputLine[2] = getLongFromResultSet(rs, "relays");
        statistics.add(outputLine);
      }
    }
    return statistics;
  }

  /** Retrieve the {@code long} value of the designated column in the
   * current row of the given {@code ResultSet} object and format it as a
   * {@code String} object, or return {@code null} if the retrieved
   * value was {@code NULL}. */
  private static String getLongFromResultSet(ResultSet rs, String columnLabel)
      throws SQLException {
    long result = rs.getLong(columnLabel);
    return rs.wasNull() ? null : String.valueOf(result);
  }

  /** Release database connection. */
  public void close() throws SQLException {
    this.connection.close();
  }
}

