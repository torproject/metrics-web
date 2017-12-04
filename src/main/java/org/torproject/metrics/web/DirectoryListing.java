/* Copyright 2017 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.web;

import org.torproject.descriptor.index.DirectoryNode;
import org.torproject.descriptor.index.FileNode;
import org.torproject.descriptor.index.IndexNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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

  /** Extracts directory listing from an index node by visiting all nodes. */
  private void extractDirectoryListings() {
    Map<DirectoryNode, String> directoryNodes = new HashMap<>();
    this.put("/collector/",
        formatTableEntries("", "/", this.index.directories, this.index.files));
    for (DirectoryNode directory : this.index.directories) {
      directoryNodes.put(directory, "/");
    }
    while (!directoryNodes.isEmpty()) {
      DirectoryNode currentDirectoryNode =
          directoryNodes.keySet().iterator().next();
      String parentPath = directoryNodes.remove(currentDirectoryNode);
      if (null != currentDirectoryNode.directories) {
        for (DirectoryNode subDirectoryNode
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
      SortedSet<DirectoryNode> directories, SortedSet<FileNode> files) {
    List<String[]> tableEntries = new ArrayList<>();
    tableEntries.add(new String[] { "Parent Directory",
        String.format("/collector%s",
        parentPath.isEmpty() ? ".html" : parentPath), "", "" });
    if (null != directories) {
      for (DirectoryNode subDirectoryNode : directories) {
        tableEntries.add(new String[] { subDirectoryNode.path,
            String.format("/collector%s%s%s/", parentPath, path,
            subDirectoryNode.path), "", "" });
      }
    }
    if (null != files) {
      for (FileNode fileNode : new TreeSet<>(files).descendingSet()) {
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
        .format(Locale.US, "%.1f %siB", bytes / Math.pow(1024, exp), pre);
  }
}

