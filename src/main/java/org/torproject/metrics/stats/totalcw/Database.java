/* Copyright 2018--2020 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.stats.totalcw;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/** Database wrapper to connect to the database, insert data, run the stored
 * procedure for aggregating data, and query aggregated data as output. */
class Database implements AutoCloseable {

  /** Database connection string. */
  private static final String jdbcString = String.format(
      "jdbc:postgresql://localhost/totalcw?user=%s&password=%s",
      System.getProperty("metrics.dbuser", "metrics"),
      System.getProperty("metrics.dbpass", "password"));

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
  Database() throws SQLException {
    this.connect();
    this.prepareStatements();
  }

  private void connect() throws SQLException {
    this.connection = DriverManager.getConnection(jdbcString);
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
        "SELECT EXISTS (SELECT 1 FROM status "
            + "WHERE valid_after = ? AND authority_id = ?)");
    this.psVoteInsert = this.connection.prepareStatement(
        "INSERT INTO status (valid_after, authority_id, have_guard_flag, "
        + "have_exit_flag, measured_sum) VALUES (?, ?, ?, ?, ?)");
  }

  /** Insert a parsed consensus into the status table. */
  void insertConsensus(TotalcwRelayNetworkStatus consensus)
      throws SQLException {
    if (null != consensus) {
      insertStatusIfAbsent(consensus.validAfter, null, consensus.measuredSums);
    }
  }

  /** Insert a parsed vote into the status table. */
  void insertVote(TotalcwRelayNetworkStatus vote) throws SQLException {
    if (null != vote) {
      int authorityId = insertAuthorityIfAbsent(vote.nickname,
          vote.identityHex);
      insertStatusIfAbsent(vote.validAfter, authorityId, vote.measuredSums);
    }
  }

  private int insertAuthorityIfAbsent(String nickname, String identityHex)
      throws SQLException {
    int authorityId = -1;
    this.psAuthoritySelect.clearParameters();
    this.psAuthoritySelect.setString(1, nickname);
    this.psAuthoritySelect.setString(2, identityHex);
    try (ResultSet rs = this.psAuthoritySelect.executeQuery()) {
      if (rs.next()) {
        authorityId = rs.getInt(1);
      }
    }
    if (authorityId < 0) {
      this.psAuthorityInsert.clearParameters();
      this.psAuthorityInsert.setString(1, nickname);
      this.psAuthorityInsert.setString(2, identityHex);
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
    return authorityId;
  }

  private void insertStatusIfAbsent(LocalDateTime validAfter,
      Integer authorityId, long[] measuredSums) throws SQLException {
    this.psVoteSelect.clearParameters();
    this.psVoteSelect.setTimestamp(1,
        Timestamp.from(ZonedDateTime.of(validAfter,
            ZoneId.of("UTC")).toInstant()));
    if (null == authorityId) {
      this.psVoteSelect.setNull(2, Types.INTEGER);
    } else {
      this.psVoteSelect.setInt(2, authorityId);
    }
    try (ResultSet rs = this.psVoteSelect.executeQuery()) {
      if (rs.next()) {
        if (rs.getBoolean(1)) {
          /* Vote is already contained. */
          return;
        }
      }
    }
    for (int measuredSumsIndex = 0; measuredSumsIndex < 4;
         measuredSumsIndex++) {
      this.psVoteInsert.clearParameters();
      this.psVoteInsert.setTimestamp(1,
          Timestamp.from(ZonedDateTime.of(validAfter,
              ZoneId.of("UTC")).toInstant()));
      if (null == authorityId) {
        this.psVoteInsert.setNull(2, Types.INTEGER);
      } else {
        this.psVoteInsert.setInt(2, authorityId);
      }
      this.psVoteInsert.setBoolean(3, 1 == (measuredSumsIndex & 1));
      this.psVoteInsert.setBoolean(4, 2 == (measuredSumsIndex & 2));
      this.psVoteInsert.setLong(5, measuredSums[measuredSumsIndex]);
      this.psVoteInsert.execute();
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
    String queryString = "SELECT " + OutputLine.columnHeadersDelimitedBy(", ")
        + " FROM totalcw";
    try (ResultSet rs = st.executeQuery(queryString)) {
      while (rs.next()) {
        OutputLine outputLine = new OutputLine();
        outputLine.validAfterDate = rs.getDate(
            OutputLine.Column.VALID_AFTER_DATE.name()).toLocalDate();
        outputLine.nickname = rs.getString(OutputLine.Column.NICKNAME.name());
        outputLine.haveGuardFlag = rs.getBoolean(
            OutputLine.Column.HAVE_GUARD_FLAG.name());
        outputLine.haveExitFlag = rs.getBoolean(
            OutputLine.Column.HAVE_EXIT_FLAG.name());
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

