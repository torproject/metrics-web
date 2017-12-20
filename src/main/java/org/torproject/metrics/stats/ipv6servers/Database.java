/* Copyright 2017 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.stats.ipv6servers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/** Database wrapper to connect to the database, insert data, run the stored
 * procedure for aggregating data, and query aggregated data as output. */
class Database {

  /** Database connection string. */
  private String jdbcString;

  /** Connection object for all interactions with the database. */
  private Connection connection;

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
  Database(String jdbcString) throws SQLException {
    this.jdbcString = jdbcString;
    this.connect();
    this.prepareStatements();
  }

  private void connect() throws SQLException {
    this.connection = DriverManager.getConnection(this.jdbcString);
    this.connection.setAutoCommit(false);
  }

  private void prepareStatements() throws SQLException {
    this.psServerDescriptorsSelect = this.connection.prepareStatement(
        "SELECT EXISTS (SELECT 1 FROM server_descriptors "
            + "WHERE descriptor_digest_sha1 = decode(?, 'hex'))");
    this.psServerDescriptorsInsert = this.connection.prepareStatement(
        "INSERT INTO server_descriptors (descriptor_digest_sha1, "
        + "advertised_bandwidth_bytes, announced_ipv6, exiting_ipv6_relay) "
        + "VALUES (decode(?, 'hex'), ?, ?, ?)");
    this.psStatusesSelect = this.connection.prepareStatement(
        "SELECT EXISTS (SELECT 1 FROM statuses "
            + "WHERE server = CAST(? AS server_enum) AND valid_after = ?)");
    this.psStatusesInsert = this.connection.prepareStatement(
        "INSERT INTO statuses (server, valid_after, running_count) "
            + "VALUES (CAST(? AS server_enum), ?, ?)",
        Statement.RETURN_GENERATED_KEYS);
    this.psStatusEntriesInsert = this.connection.prepareStatement(
        "INSERT INTO status_entries (status_id, descriptor_digest_sha1, "
        + "guard_relay, exit_relay, reachable_ipv6_relay) "
        + "VALUES (?, decode(?, 'hex'), ?, ?, ?)");
  }

  /** Insert a server descriptor into the server_descriptors table. */
  void insertServerDescriptor(
      ParsedServerDescriptor parsedServerDescriptor) throws SQLException {
    this.psServerDescriptorsSelect.clearParameters();
    this.psServerDescriptorsSelect.setString(1,
        parsedServerDescriptor.digest);
    try (ResultSet rs = psServerDescriptorsSelect.executeQuery()) {
      if (rs.next()) {
        if (rs.getBoolean(1)) {
          /* Server descriptor is already contained. */
          return;
        }
      }
    }
    this.psServerDescriptorsInsert.clearParameters();
    this.psServerDescriptorsInsert.setString(1,
        parsedServerDescriptor.digest);
    this.psServerDescriptorsInsert.setInt(2,
        parsedServerDescriptor.advertisedBandwidth);
    this.psServerDescriptorsInsert.setBoolean(3,
        parsedServerDescriptor.announced);
    this.psServerDescriptorsInsert.setBoolean(4,
        parsedServerDescriptor.exiting);
    this.psServerDescriptorsInsert.execute();
  }

  /** Insert a status and all contained entries into the statuses and
   * status_entries table. */
  void insertStatus(ParsedNetworkStatus parsedNetworkStatus)
      throws SQLException {
    this.psStatusesSelect.clearParameters();
    this.psStatusesSelect.setString(1,
        parsedNetworkStatus.isRelay ? "relay" : "bridge");
    Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"),
        Locale.US);
    this.psStatusesSelect.setTimestamp(2,
        Timestamp.from(ZonedDateTime.of(parsedNetworkStatus.timestamp,
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
        parsedNetworkStatus.isRelay ? "relay" : "bridge");
    this.psStatusesInsert.setTimestamp(2,
        Timestamp.from(ZonedDateTime.of(parsedNetworkStatus.timestamp,
        ZoneId.of("UTC")).toInstant()), calendar);
    this.psStatusesInsert.setInt(3, parsedNetworkStatus.running);
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
    for (ParsedNetworkStatus.Entry entry : parsedNetworkStatus.entries) {
      this.psStatusEntriesInsert.clearParameters();
      this.psStatusEntriesInsert.setInt(1, statusId);
      this.psStatusEntriesInsert.setString(2, entry.digest);
      this.psStatusEntriesInsert.setBoolean(3, entry.guard);
      this.psStatusEntriesInsert.setBoolean(4, entry.exit);
      this.psStatusEntriesInsert.setBoolean(5, entry.reachable);
      this.psStatusEntriesInsert.addBatch();
    }
    this.psStatusEntriesInsert.executeBatch();
  }

  /** Call the aggregate() function to aggregate rows from the status_entries
   * and server_descriptors tables into the aggregated table. */
  void aggregate() throws SQLException {
    Statement st = this.connection.createStatement();
    st.executeQuery("SELECT aggregate_ipv6()");
  }

  /** Roll back any changes made in this execution. */
  void rollback() throws SQLException {
    this.connection.rollback();
  }

  /** Commit all changes made in this execution. */
  void commit() throws SQLException {
    this.connection.commit();
  }

  /** Query the servers_ipv6 view to obtain aggregated statistics. */
  Iterable<OutputLine> queryServersIpv6() throws SQLException {
    List<OutputLine> statistics = new ArrayList<>();
    Statement st = this.connection.createStatement();
    Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"),
        Locale.US);
    String queryString = "SELECT " + OutputLine.getColumnHeaders(", ")
        + " FROM ipv6servers";
    try (ResultSet rs = st.executeQuery(queryString)) {
      while (rs.next()) {
        OutputLine outputLine = new OutputLine();
        outputLine.date = rs.getDate(OutputLine.Column.VALID_AFTER_DATE.name(),
            calendar).toLocalDate();
        outputLine.server = rs.getString(OutputLine.Column.SERVER.name());
        outputLine.guard = rs.getString(OutputLine.Column.GUARD_RELAY.name());
        outputLine.exit = rs.getString(OutputLine.Column.EXIT_RELAY.name());
        outputLine.announced = rs.getString(
            OutputLine.Column.ANNOUNCED_IPV6.name());
        outputLine.exiting = rs.getString(
            OutputLine.Column.EXITING_IPV6_RELAY.name());
        outputLine.reachable = rs.getString(
            OutputLine.Column.REACHABLE_IPV6_RELAY.name());
        outputLine.count = rs.getLong(
            OutputLine.Column.SERVER_COUNT_SUM_AVG.name());
        outputLine.advertisedBandwidth = rs.getLong(
            OutputLine.Column.ADVERTISED_BANDWIDTH_BYTES_SUM_AVG.name());
        if (rs.wasNull()) {
          outputLine.advertisedBandwidth = null;
        }
        statistics.add(outputLine);
      }
    }
    return statistics;
  }

  /** Disconnect from the database. */
  void disconnect() throws SQLException {
    this.connection.close();
  }
}

