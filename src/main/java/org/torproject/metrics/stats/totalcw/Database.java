/* Copyright 2018 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.stats.totalcw;

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
class Database implements AutoCloseable {

  /** Database connection string. */
  private String jdbcString;

  /** Connection object for all interactions with the database. */
  private Connection connection;

  /** Prepared statement for finding out whether a given authority is already
   * contained in the authority table. */
  private PreparedStatement psAuthoritySelect;

  /** Prepared statement for inserting an authority into the authority table. */
  private PreparedStatement psAuthorityInsert;

  /** Prepared statement for checking whether a vote has been inserted into the
   * vote table before. */
  private PreparedStatement psVoteSelect;

  /** Prepared statement for inserting a vote into the vote table. */
  private PreparedStatement psVoteInsert;

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
    this.psAuthoritySelect = this.connection.prepareStatement(
        "SELECT authority_id FROM authority "
            + "WHERE nickname = ? AND identity_hex = ?");
    this.psAuthorityInsert = this.connection.prepareStatement(
        "INSERT INTO authority (nickname, identity_hex) VALUES (?, ?)",
        Statement.RETURN_GENERATED_KEYS);
    this.psVoteSelect = this.connection.prepareStatement(
        "SELECT EXISTS (SELECT 1 FROM vote "
            + "WHERE valid_after = ? AND authority_id = ?)");
    this.psVoteInsert = this.connection.prepareStatement(
        "INSERT INTO vote (valid_after, authority_id, measured_count, "
            + "measured_sum, measured_mean, measured_min, measured_q1, "
            + "measured_median, measured_q3, measured_max) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
        Statement.RETURN_GENERATED_KEYS);
  }

  /** Insert a parsed vote into the vote table. */
  void insertVote(TotalcwRelayNetworkStatusVote vote) throws SQLException {
    if (null == vote) {
      /* Nothing to insert. */
      return;
    }
    int authorityId = -1;
    this.psAuthoritySelect.clearParameters();
    this.psAuthoritySelect.setString(1, vote.nickname);
    this.psAuthoritySelect.setString(2, vote.identityHex);
    try (ResultSet rs = this.psAuthoritySelect.executeQuery()) {
      if (rs.next()) {
        authorityId = rs.getInt(1);
      }
    }
    if (authorityId < 0) {
      this.psAuthorityInsert.clearParameters();
      this.psAuthorityInsert.setString(1, vote.nickname);
      this.psAuthorityInsert.setString(2, vote.identityHex);
      this.psAuthorityInsert.execute();
      try (ResultSet rs = this.psAuthorityInsert.getGeneratedKeys()) {
        if (rs.next()) {
          authorityId = rs.getInt(1);
        }
      }
      if (authorityId < 0) {
        throw new SQLException("Could not retrieve auto-generated key for new "
            + "authority entry.");
      }
    }
    Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"),
        Locale.US);
    this.psVoteSelect.clearParameters();
    this.psVoteSelect.setTimestamp(1,
        Timestamp.from(ZonedDateTime.of(vote.validAfter,
        ZoneId.of("UTC")).toInstant()), calendar);
    this.psVoteSelect.setInt(2, authorityId);
    try (ResultSet rs = this.psVoteSelect.executeQuery()) {
      if (rs.next()) {
        if (rs.getBoolean(1)) {
          /* Vote is already contained. */
          return;
        }
      }
    }
    int voteId = -1;
    this.psVoteInsert.clearParameters();
    this.psVoteInsert.setTimestamp(1,
        Timestamp.from(ZonedDateTime.of(vote.validAfter,
            ZoneId.of("UTC")).toInstant()), calendar);
    this.psVoteInsert.setInt(2, authorityId);
    this.psVoteInsert.setLong(3, vote.measuredCount);
    this.psVoteInsert.setLong(4, vote.measuredSum);
    this.psVoteInsert.setLong(5, vote.measuredMean);
    this.psVoteInsert.setLong(6, vote.measuredMin);
    this.psVoteInsert.setLong(7, vote.measuredQ1);
    this.psVoteInsert.setLong(8, vote.measuredMedian);
    this.psVoteInsert.setLong(9, vote.measuredQ3);
    this.psVoteInsert.setLong(10, vote.measuredMax);
    this.psVoteInsert.execute();
    try (ResultSet rs = this.psVoteInsert.getGeneratedKeys()) {
      if (rs.next()) {
        voteId = rs.getInt(1);
      }
    }
    if (voteId < 0) {
      throw new SQLException("Could not retrieve auto-generated key for new "
          + "vote entry.");
    }
  }

  /** Roll back any changes made in this execution. */
  void rollback() throws SQLException {
    this.connection.rollback();
  }

  /** Commit all changes made in this execution. */
  void commit() throws SQLException {
    this.connection.commit();
  }

  /** Query the totalcw view to obtain aggregated statistics. */
  Iterable<OutputLine> queryTotalcw() throws SQLException {
    List<OutputLine> statistics = new ArrayList<>();
    Statement st = this.connection.createStatement();
    Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"),
        Locale.US);
    String queryString = "SELECT " + OutputLine.columnHeadersDelimitedBy(", ")
        + " FROM totalcw";
    try (ResultSet rs = st.executeQuery(queryString)) {
      while (rs.next()) {
        OutputLine outputLine = new OutputLine();
        outputLine.validAfterDate = rs.getDate(
            OutputLine.Column.VALID_AFTER_DATE.name(), calendar).toLocalDate();
        outputLine.nickname = rs.getString(OutputLine.Column.NICKNAME.name());
        outputLine.measuredSumAvg = rs.getLong(
            OutputLine.Column.MEASURED_SUM_AVG.name());
        statistics.add(outputLine);
      }
    }
    return statistics;
  }

  /** Release database connection. */
  public void close() throws SQLException {
    this.connection.close();
  }
}

