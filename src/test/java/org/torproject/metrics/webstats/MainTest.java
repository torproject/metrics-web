/* Copyright 2017 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.webstats;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.regex.Matcher;

public class MainTest {

  static final String SAMPLE_LOG_FILE_NAME =
      "metrics.torproject.org-access.log-20170117.xz";

  static final String SAMPLE_SUBDIRECTORY_NAME = "meronense.torproject.org/";

  static final String SAMPLE_LOG_FILE_URL =
      "https://webstats.torproject.org/out/meronense.torproject.org/"
      + "metrics.torproject.org-access.log-20170117.xz";

  static final String[] SAMPLE_LOG_LINES = new String[] {
      "0.0.0.0 - - [17/Jan/2017:00:00:00 +0000] "
      + "\"GET / HTTP/1.0\" 200 10532 \"-\" \"-\" -",
      "0.0.0.0 - - [17/Jan/2017:00:00:00 +0000] "
      + "\"HEAD /bubbles.html HTTP/1.1\" 200 - \"-\" \"-\" -"
  };

  @Test
  public void testUrlStringPatternComplete() {
    Matcher matcher = Main.URL_STRING_PATTERN.matcher(
        "<img src=\"/icons/unknown.gif\" alt=\"[   ]\"> "
        + "<a href=\"" + SAMPLE_LOG_FILE_NAME + "\">" + SAMPLE_LOG_FILE_NAME
        + "</a> 2017-01-19 19:43  5.6K  ");
    assertTrue(matcher.matches());
    assertEquals(SAMPLE_LOG_FILE_NAME, matcher.group(1));
  }

  @Test
  public void testUrlStringPatternOnlyATag() {
    Matcher matcher = Main.URL_STRING_PATTERN.matcher("<a href=\""
        + SAMPLE_LOG_FILE_NAME + "\">" + SAMPLE_LOG_FILE_NAME + "</a>");
    assertTrue(matcher.matches());
    assertEquals(SAMPLE_LOG_FILE_NAME, matcher.group(1));
  }

  @Test
  public void testUrlStringPatternSubdirectory() {
    Matcher matcher = Main.URL_STRING_PATTERN.matcher(
        "<a href=\"" + SAMPLE_SUBDIRECTORY_NAME + "\">"
        + SAMPLE_SUBDIRECTORY_NAME + "/</a>");
    assertTrue(matcher.matches());
    assertEquals(SAMPLE_SUBDIRECTORY_NAME, matcher.group(1));
  }

  @Test
  public void testUrlStringPatternAnythingBetweenDoubleQuotesHtml() {
    Matcher matcher = Main.URL_STRING_PATTERN.matcher(
        "<a href=\"anything-between-double-quotes.html\">Link/</a>");
    assertTrue(matcher.matches());
    assertEquals("anything-between-double-quotes.html", matcher.group(1));
  }

  @Test
  public void testLogFileUrlPatternComplete() {
    Matcher matcher = Main.LOG_FILE_URL_PATTERN.matcher(SAMPLE_LOG_FILE_URL);
    assertTrue(matcher.matches());
    assertEquals("meronense.torproject.org", matcher.group(1));
    assertEquals("metrics.torproject.org", matcher.group(2));
    assertEquals("20170117", matcher.group(3));
  }

  @Test
  public void testLogLinePatternGetSlash() {
    Matcher matcher = Main.LOG_LINE_PATTERN.matcher(SAMPLE_LOG_LINES[0]);
    assertTrue(matcher.matches());
    assertEquals("GET", matcher.group(1));
    assertEquals("/", matcher.group(2));
    assertEquals("200", matcher.group(3));
  }

  @Test
  public void testLogLinePatternHeadBubbles() {
    Matcher matcher = Main.LOG_LINE_PATTERN.matcher(SAMPLE_LOG_LINES[1]);
    assertTrue(matcher.matches());
    assertEquals("HEAD", matcher.group(1));
    assertEquals("/bubbles.html", matcher.group(2));
    assertEquals("200", matcher.group(3));
  }

  @Test
  public void testLogLinePatternMaxLength() {
    int maxLength = 2048;
    String pre = "0.0.0.0 - - [17/Jan/2017:00:00:00 +0000] \"GET ";
    String post = " HTTP/1.0\" 200 10532 \"-\" \"-\" -";
    StringBuilder sb = new StringBuilder();
    while (sb.length() <= maxLength) {
      sb.append("/https://www.torproject.org");
    }
    String tooLongLogLine = pre + sb.toString() + post;
    assertFalse(Main.LOG_LINE_PATTERN.matcher(tooLongLogLine).matches());
    String notTooLongLogLine = pre + sb.toString().substring(0, maxLength)
        + post;
    assertTrue(Main.LOG_LINE_PATTERN.matcher(notTooLongLogLine).matches());
  }
}

