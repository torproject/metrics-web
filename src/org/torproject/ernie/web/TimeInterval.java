package org.torproject.ernie.web;

/* The standard JDK lacks anything for processing time intervals */

public class TimeInterval {

  private static final int YEARS = 31536000;
  private static final int DAYS = 86400;
  private static final int HOURS = 3600;
  private static final int MINUTES = 60;

  /* Format an interval like YY'y' DD'd' HH:MM:SS */
  public static String format(int seconds) {

    String fmt = "";

    if (seconds / YEARS > 0)  {
      fmt += (seconds / YEARS) + "y ";
      seconds -= ((seconds / YEARS) * YEARS);
    }

    if (seconds / DAYS > 0)  {
      fmt += (seconds / DAYS) + "d ";
      seconds -= ((seconds / DAYS) * DAYS);
    }

    fmt += ((seconds / HOURS < 10) ? "0" : "")
        + (seconds / HOURS) + ":";
    seconds -= ((seconds / HOURS) * HOURS);

    fmt += ((seconds / MINUTES < 10) ? "0" : "")
        + (seconds / MINUTES) + ":";
    seconds -= ((seconds / MINUTES) * MINUTES);

    fmt += seconds + ((seconds < 10) ? "0" : "");

    return fmt;
  }

}
