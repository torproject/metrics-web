package org.torproject.metrics.hidserv;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/* Utility class to format and parse dates and timestamps. */
public class DateTimeHelper {

  /* This class is not supposed to be instantiated, which is why its
   * constructor has private visibility. */
  private DateTimeHelper() {
  }

  /* Some useful time constant. */
  public static final long
      ONE_SECOND = 1000L,
      ONE_MINUTE = 60L * ONE_SECOND,
      ONE_HOUR = 60L * ONE_MINUTE,
      ONE_DAY = 24L * ONE_HOUR;

  /* Some useful date/time formats. */
  public static final String
      ISO_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss",
      ISO_DATE_HOUR_FORMAT = "yyyy-MM-dd HH",
      ISO_DATE_FORMAT = "yyyy-MM-dd",
      ISO_HOUR_FORMAT = "HH";

  /* Map of DateFormat instances for parsing and formatting dates and
   * timestamps, protected using ThreadLocal to ensure that each thread
   * uses its own instances. */
  private static ThreadLocal<Map<String, DateFormat>> dateFormats =
      new ThreadLocal<Map<String, DateFormat>> () {
    public Map<String, DateFormat> get() {
      return super.get();
    }
    protected Map<String, DateFormat> initialValue() {
      return new HashMap<String, DateFormat>();
    }
    public void remove() {
      super.remove();
    }
    public void set(Map<String, DateFormat> value) {
      super.set(value);
    }
  };

  /* Return an instance of DateFormat for the given format.  If no such
   * instance exists, create one and put it in the map. */
  private static DateFormat getDateFormat(String format) {
    Map<String, DateFormat> threadDateFormats = dateFormats.get();
    if (!threadDateFormats.containsKey(format)) {
      DateFormat dateFormat = new SimpleDateFormat(format);
      dateFormat.setLenient(false);
      dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
      threadDateFormats.put(format, dateFormat);
    }
    return threadDateFormats.get(format);
  }

  /* Format the given time in milliseconds using the given format. */
  public static String format(long millis, String format) {
    return getDateFormat(format).format(millis);
  }

  /* Format the given time in milliseconds using ISO date/time format. */
  public static String format(long millis) {
    return format(millis, ISO_DATETIME_FORMAT);
  }

  /* Default result of the parse methods if the provided time could not be
   * parsed. */
  public final static long NO_TIME_AVAILABLE = -1L;

  /* Parse the given string using the given format. */
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

  /* Parse the given string using ISO date/time format. */
  public static long parse(String string) {
    return parse(string, ISO_DATETIME_FORMAT);
  }
}

