/* Copyright 2016--2020 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.stats.hidserv;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/** Utility class to format and parse dates and timestamps. */
public class DateTimeHelper {

  /** This class is not supposed to be instantiated, which is why its
   * constructor has private visibility. */
  private DateTimeHelper() {
  }

  /* Some useful time constant. */
  public static final long ONE_SECOND = 1000L;

  public static final long ONE_MINUTE = 60L * ONE_SECOND;

  public static final long ONE_HOUR = 60L * ONE_MINUTE;

  public static final long ONE_DAY = 24L * ONE_HOUR;

  /* Some useful date/time formats. */
  public static final String ISO_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

  public static final String ISO_DATE_HOUR_FORMAT = "yyyy-MM-dd HH";

  public static final String ISO_DATE_FORMAT = "yyyy-MM-dd";

  public static final String ISO_HOUR_FORMAT = "HH";

  /** Map of DateFormat instances for parsing and formatting dates and
   * timestamps, protected using ThreadLocal to ensure that each thread
   * uses its own instances. */
  private static ThreadLocal<Map<String, DateFormat>> dateFormats =
      ThreadLocal.withInitial(HashMap::new);

  /** Returns an instance of DateFormat for the given format, and if no
   * such instance exists, creates one and puts it in the map. */
  private static DateFormat getDateFormat(String format) {
    Map<String, DateFormat> threadDateFormats = dateFormats.get();
    if (!threadDateFormats.containsKey(format)) {
      DateFormat dateFormat = new SimpleDateFormat(format);
      dateFormat.setLenient(false);
      threadDateFormats.put(format, dateFormat);
    }
    return threadDateFormats.get(format);
  }

  /** Formats the given time in milliseconds using the given format. */
  public static String format(long millis, String format) {
    return getDateFormat(format).format(millis);
  }

  /** Formats the given time in milliseconds using ISO date/time
   * format. */
  public static String format(long millis) {
    return format(millis, ISO_DATETIME_FORMAT);
  }

  /** Default result of the parse methods if the provided time could not
   * be parsed. */
  public static final long NO_TIME_AVAILABLE = -1L;

  /** Parses the given string using the given format. */
  public static long parse(String string, String format) {
    if (null == string) {
      return NO_TIME_AVAILABLE;
    }
    try {
      return getDateFormat(format).parse(string).getTime();
    } catch (ParseException e) {
      return NO_TIME_AVAILABLE;
    }
  }

  /** Parses the given string using ISO date/time format. */
  public static long parse(String string) {
    return parse(string, ISO_DATETIME_FORMAT);
  }
}

