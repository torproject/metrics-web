/* Copyright 2011, 2012 The Tor Project
 * See LICENSE for licensing information */
package org.torproject.ernie.web;

public class RObject {
  private byte[] bytes;
  private String fileName;
  public RObject(byte[] bytes, String fileName) {
    this.bytes = bytes;
    this.fileName = fileName;
  }
  public String getFileName() {
    return fileName;
  }
  public byte[] getBytes() {
    return bytes;
  }
}
