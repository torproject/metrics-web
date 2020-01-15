/* Copyright 2017--2020 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.web;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/** Map with all directory listings for all directories and subdirectories
 * contained in an index.json file. */
public class DirectoryListing extends HashMap<String, List<String[]>>
    implements Map<String, List<String[]>> {

  private IndexNode index;

  DirectoryListing(IndexNode index) {
    this.index = index;
    extractDirectoryListings();
  }

  /**
   * Parsed {@code index.json} file, which can be the root node ("index node"),
   * an inner node ("directory node"), or a leaf node ("file node").
   */
  private static class IndexNode implements Comparable<IndexNode> {

    /**
     * Relative path from this node's parent node, or the CollecTor host's base
     * URL if this is the root node.
     */
    String path;

    /**
     * List of file nodes available in this directory, or {@code null} if this
     * is a leaf node.
     */
    SortedSet<IndexNode> files;

    /**
     * List of directory nodes in this directory, or {@code null} if this is a
     * leaf node.
     */
    SortedSet<IndexNode> directories;

    /**
     * Size of the file in bytes if this is a leaf node, or {@code null}
     * otherwise.
     */
    Long size;

    /**
     * Timestamp when this file was last modified using pattern
     * {@code "YYYY-MM-DD HH:MM"} in the UTC timezone if this is a leaf node, or
     * {@code null} otherwise.
     */
    String lastModified;

    /**
     * Compare two index nodes by their (relative) path in alphabetic order.
     *
     * @param other The other index node to compare to.
     * @return Comparison result of the two node's paths.
     */
    @Override
    public int compareTo(IndexNode other) {
      return this.path.compareTo(other.path);
    }
  }

  /**
   * Timeout in milliseconds for reading from remote CollecTor host.
   */
  private static final int READ_TIMEOUT = Integer.parseInt(System
      .getProperty("sun.net.client.defaultReadTimeout", "60000"));

  /**
   * Timeout in milliseconds for connecting to remote CollecTor host.
   */
  private static final int CONNECT_TIMEOUT = Integer.parseInt(System
      .getProperty("sun.net.client.defaultConnectTimeout", "60000"));

  /**
   * Object mapper for parsing {@code index.json} files.
   */
  private static ObjectMapper objectMapper = new ObjectMapper()
      .setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)
      .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
      .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
      .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  /**
   * Create a new instance by downloading an {@code index.json} file from the
   * given CollecTor host and parsing its contents.
   *
   * @param host CollecTor host to download the {@code index.json} file from.
   * @return Parsed directory listings.
   * @throws IOException Thrown if downloading or parsing the {@code index.json}
   *     file fails.
   */
  public static DirectoryListing ofHostString(String host) throws IOException {
    String urlString = host + "/index/index.json.gz";
    URLConnection connection = new URL(urlString).openConnection();
    connection.setReadTimeout(READ_TIMEOUT);
    connection.setConnectTimeout(CONNECT_TIMEOUT);
    connection.connect();
    try (InputStream inputStream
        = new GzipCompressorInputStream(connection.getInputStream())) {
      return ofInputStream(inputStream);
    }
  }

  /**
   * Create a new instance by parsing an {@code index.json} file from the
   * given stream.
   *
   * @param inputStream Input stream providing (uncompressed) {@code index.json}
   *     file contents.
   * @return Parsed directory listings.
   * @throws IOException Thrown if parsing the {@code index.json} file fails.
   */
  public static DirectoryListing ofInputStream(InputStream inputStream)
      throws IOException {
    IndexNode index = objectMapper.readValue(inputStream, IndexNode.class);
    return new DirectoryListing(index);
  }

  /** Extracts directory listing from an index node by visiting all nodes. */
  private void extractDirectoryListings() {
    Map<IndexNode, String> directoryNodes = new HashMap<>();
    this.put("/collector/",
        formatTableEntries("", "/", this.index.directories, this.index.files));
    for (IndexNode directory : this.index.directories) {
      directoryNodes.put(directory, "/");
    }
    while (!directoryNodes.isEmpty()) {
      IndexNode currentDirectoryNode =
          directoryNodes.keySet().iterator().next();
      String parentPath = directoryNodes.remove(currentDirectoryNode);
      if (null != currentDirectoryNode.directories) {
        for (IndexNode subDirectoryNode
            : currentDirectoryNode.directories) {
          directoryNodes.put(subDirectoryNode, String.format("%s%s/",
              parentPath, currentDirectoryNode.path));
        }
      }
      this.put(String
          .format("/collector%s%s/", parentPath, currentDirectoryNode.path),
          formatTableEntries(parentPath, currentDirectoryNode.path + "/",
          currentDirectoryNode.directories, currentDirectoryNode.files));
    }
  }

  /** Formats table entries for a given directory. */
  private List<String[]> formatTableEntries(String parentPath, String path,
      SortedSet<IndexNode> directories, SortedSet<IndexNode> files) {
    List<String[]> tableEntries = new ArrayList<>();
    tableEntries.add(new String[] { "Parent Directory",
        String.format("/collector%s",
        parentPath.isEmpty() ? ".html" : parentPath), "", "" });
    if (null != directories) {
      for (IndexNode subDirectoryNode : directories) {
        tableEntries.add(new String[] { subDirectoryNode.path,
            String.format("/collector%s%s%s/", parentPath, path,
            subDirectoryNode.path), "", "" });
      }
    }
    if (null != files) {
      for (IndexNode fileNode : new TreeSet<>(files).descendingSet()) {
        tableEntries.add(new String[] { fileNode.path,
            String.format("%s%s%s%s", this.index.path, parentPath,
            path, fileNode.path), fileNode.lastModified,
            formatBytes(fileNode.size) });
      }
    }
    return tableEntries;
  }

  /** Formats a number of bytes to units B, KiB, MiB, etc. */
  static String formatBytes(long bytes) {
    if (bytes < 1024) {
      return bytes + " B";
    }
    int exp = (int) (Math.log(bytes) / Math.log(1024));
    char pre = "KMGTPE".charAt(exp - 1);
    return String
        .format("%.1f %siB", bytes / Math.pow(1024, exp), pre);
  }
}

