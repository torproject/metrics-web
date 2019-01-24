/* Copyright 2017--2018 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.stats.clients;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/** Database wrapper to connect to the database, insert data, run the stored
 * procedure for aggregating data, and query aggregated data as output. */
class Database implements AutoCloseable {

  /** Database connection string. */
  private static final String jdbcString = String.format(
      "jdbc:postgresql://localhost/userstats?user=%s&password=%s",
      System.getProperty("metrics.dbuser", "metrics"),
      System.getProperty("metrics.dbpass", "password"));

  /** Connection object for all interactions with the database. */
  private Connection connection;

  /** Prepared statement for inserting a platform string into the imported
   * table. */
  private PreparedStatement psImportedInsert;

  /** Create a new Database instance and prepare for inserting or querying
   * data. */
  Database() throws SQLException {
    this.connect();
    this.prepareStatements();
  }

  private void connect() throws SQLException {
    this.connection = DriverManager.getConnection(jdbcString);
    this.connection.setAutoCommit(false);
  }

  private void prepareStatements() throws SQLException {
    this.psImportedInsert = this.connection.prepareStatement(
        "INSERT INTO imported (fingerprint, node, metric, country, transport, "
        + "version, stats_start, stats_end, val) "
        + "VALUES (?, CAST(? AS node), CAST(? AS metric), ?, ?, ?, ?, ?, ?)");
  }

  /** Insert into the imported table. */
  void insertIntoImported(String fingerprint, String node, String metric,
      String country, String transport, String version, long fromMillis,
      long toMillis, double val) throws SQLException {
    if (fromMillis > toMillis) {
      return;
    }
    psImportedInsert.clearParameters();
    psImportedInsert.setString(1, fingerprint);
    psImportedInsert.setString(2, node);
    psImportedInsert.setString(3, metric);
    psImportedInsert.setString(4, country);
    psImportedInsert.setString(5, transport);
    psImportedInsert.setString(6, version);
    psImportedInsert.setTimestamp(7,
        Timestamp.from(Instant.ofEpochMilli(fromMillis)));
    psImportedInsert.setTimestamp(8,
        Timestamp.from(Instant.ofEpochMilli(toMillis)));
    psImportedInsert.setDouble(9, Math.round(val * 10.0) / 10.0);
    psImportedInsert.execute();
  }

  /** Process the newly imported data by calling the various stored procedures
   * and then truncating the imported table. */
  void processImported() throws SQLException {
    this.connection.createStatement().execute("SELECT merge()");
    this.connection.createStatement().execute("SELECT aggregate()");
    this.connection.createStatement().execute("SELECT combine()");
    this.connection.createStatement().execute("TRUNCATE imported");
  }

  /** Commit all changes made in this execution. */
  void commit() throws SQLException {
    this.connection.commit();
  }

  /** Query the estimated view. */
  List<String[]> queryEstimated() throws SQLException {
    List<String[]> statistics = new ArrayList<>();
    String columns = "date, node, country, transport, version, frac, users";
    statistics.add(columns.split(", "));
    Statement st = this.connection.createStatement();
    Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"),
        Locale.US);
    String queryString = "SELECT " + columns + " FROM estimated";
    try (ResultSet rs = st.executeQuery(queryString)) {
      while (rs.next()) {
        String[] outputLine = new String[7];
        outputLine[0] = rs.getDate("date", calendar).toLocalDate().toString();
        outputLine[1] = rs.getString("node");
        outputLine[2] = rs.getString("country");
        outputLine[3] = rs.getString("transport");
        outputLine[4] = rs.getString("version");
        outputLine[5] = getIntFromResultSet(rs, "frac");
        outputLine[6] = getIntFromResultSet(rs, "users");
        statistics.add(outputLine);
      }
    }
    return statistics;
  }

  /** Query the combined view. */
  List<String[]> queryCombined() throws SQLException {
    List<String[]> statistics = new ArrayList<>();
    String columns = "date, node, country, transport, version, frac, low, high";
    statistics.add(columns.split(", "));
    Statement st = this.connection.createStatement();
    Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"),
        Locale.US);
    String queryString = "SELECT " + columns + " FROM combined";
    try (ResultSet rs = st.executeQuery(queryString)) {
      while (rs.next()) {
        String[] outputLine = new String[8];
        outputLine[0] = rs.getDate("date", calendar).toLocalDate().toString();
        outputLine[1] = rs.getString("node");
        outputLine[2] = rs.getString("country");
        outputLine[3] = rs.getString("transport");
        outputLine[4] = rs.getString("version");
        outputLine[5] = getIntFromResultSet(rs, "frac");
        outputLine[6] = getIntFromResultSet(rs, "low");
        outputLine[7] = getIntFromResultSet(rs, "high");
        statistics.add(outputLine);
      }
    }
    return statistics;
  }

  /** Retrieve the <code>int</code> value of the designated column in the
   * current row of the given <code>ResultSet</code> object and format it as a
   * <code>String</code> object, or return <code>null</code> if the retrieved
   * value was <code>NULL</code>. */
  private static String getIntFromResultSet(ResultSet rs, String columnLabel)
      throws SQLException {
    int result = rs.getInt(columnLabel);
    return rs.wasNull() ? null : String.valueOf(result);
  }

  /** Release database connection. */
  public void close() throws SQLException {
    this.connection.close();
  }
}

