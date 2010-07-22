package org.torproject.ernie.util;

import java.util.Properties;

public class ErnieProperties  {

  private static final String PROPS_RESOURCE = "ernie.properties";
  private static Properties props;

  static {
    props = new Properties();
    try {
      props.load(ErnieProperties.class.getClassLoader()
          .getResourceAsStream(PROPS_RESOURCE));
    } catch (Exception e) {
    }
  }

  public String getProperty(String key) {
    return props.getProperty(key);
  }

  public String getProperty(String key, String defaultValue)  {
    return props.getProperty(key, defaultValue);
  }

  public int getInt(String key) {
    String value = getProperty(key);
    return value == null ? 0 : Integer.parseInt(value);
  }
}
