/* Copyright 2011, 2012 The Tor Project
 * See LICENSE for licensing information */
package org.torproject.ernie.web.graphs;

public class RObject {
  private byte[] bytes;
  private String fileName;
  private long lastModified;
  public RObject(byte[] bytes, String fileName, long lastModified) {
    this.bytes = bytes;
    this.fileName = fileName;
    this.lastModified = lastModified;
  }
  public String getFileName() {
    return this.fileName;
  }
  public byte[] getBytes() {
    return this.bytes;
  }
  public long getLastModified() {
    return this.lastModified;
  }
}
