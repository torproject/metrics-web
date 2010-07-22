package org.torproject.ernie.util;

import org.torproject.ernie.util.ErnieProperties;
import org.apache.log4j.Logger;
import java.util.*;
import java.text.*;
import java.sql.*;
import java.util.Date; /* Use java date instead of sql.*/

public class DateRanges {

  private static final Logger log;
  private static SimpleDateFormat simpledf;
  private final static String jdbcURL;
  private static final ErnieProperties props;

  private Connection conn;
  private PreparedStatement psYearsRange;
  private PreparedStatement psAllRange;

  static {
    log = Logger.getLogger(DateRanges.class.toString());
    props = new ErnieProperties();
    simpledf = new SimpleDateFormat("yyyy-MM-dd");
    simpledf.setTimeZone(TimeZone.getTimeZone("UTC"));
    jdbcURL = props.getProperty("jdbc.url");
  }

  public DateRanges() {
    try {
      this.conn = DriverManager.getConnection(jdbcURL);

      /* Its much faster to get the year from the aggregate tables instead
       * of the large statusentry or descriptor tables. TODO do this more
       * robustly? */
      this.psYearsRange = conn.prepareStatement(
          "select min(extract('year' from date(date))) as min, " +
          "max(extract('year' from date(date))) as max " +
          "from network_size");
      this.psAllRange = conn.prepareStatement(
          "select min(date(date)) as min, " +
          "max(date(date)) as max " +
          "from network_size");
    } catch (SQLException e)  {
      log.warn("Couldn't connect to database or prepare statements. " + e);
    }
  }

  /**
   * Get a range for days in the past, which returns a tuple
   * that maps to (start, end) - (yyyy-mm-dd, yyyy-mm-dd)
   */
  public String[] getDayRange(int days)  {
    String[] dates = new String[2];
    Calendar today = Calendar.getInstance();
    today.setTimeZone(TimeZone.getTimeZone("UTC"));
    Calendar start = (Calendar)today.clone();
    start.add(Calendar.DATE, -days);

    dates[0] = simpledf.format(start.getTime());
    dates[1] = simpledf.format(today.getTime());
    return dates;
  }

  /**
   * Get the years range (of current data in the database), which returns a
   * simple numeric array of each year.
   */
  public int[] getYearsRange()  {
    int min = 0, max = 0;
    int yearsrange[] = null;
    try {
      ResultSet rsYearsRange = psYearsRange.executeQuery();
      if (rsYearsRange.next())  {
        min = rsYearsRange.getInt("min");
        max = rsYearsRange.getInt("max");
      }
      yearsrange = new int[max - min + 1];
      for (int i = 0; i <= max-min; i++) {
        yearsrange[i] = min+i;
      }
    } catch (SQLException e) {
      yearsrange = new int[0];
      log.warn("Couldn't get results from network_size table: " + e);
    }
    return yearsrange;
  }

  /**
   * Used in conjunction with getYearsRange, it accepts a year and will
   * return the start and end date.
   */
  public String[] getYearsRangeDates(int year)  {
    String[] dates = new String[2];
    dates[0] = year + "-01-01";
    dates[1] = year + "-12-31";
    return dates;
  }

  /**
   * Get the date range (of the current data in the database), which
   * returns a map with one row that contains the start and end dates
   * like (yyyy-mm-dd, yyyy-mm-dd).
   */
  public String[] getAllDataRange() {
    String[] range = new String[2];
    try {
      ResultSet rsAllRange = psAllRange.executeQuery();
      if (rsAllRange.next())  {
        range[0] = rsAllRange.getString("min");
        range[1] = rsAllRange.getString("max");
      }
    } catch (SQLException e) {
      log.warn("Couldn't get results from network_size table: " + e);
    }
    return range;
  }

  /**
   * Close database connection.
   */
  public void closeConnection() {
    try {
      this.conn.close();
    } catch (SQLException e)  {
      log.warn("Couldn't close database connection. " + e);
    }
  }
}
