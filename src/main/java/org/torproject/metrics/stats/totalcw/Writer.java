/* Copyright 2018--2020 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.stats.totalcw;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Writer that takes output line objects and writes them to a file, preceded
 * by a column header line. */
class Writer {

  /** Write output lines to the given file. */
  void write(Path filePath, Iterable<OutputLine> outputLines)
      throws IOException {
    File parentFile = filePath.toFile().getParentFile();
    if (null != parentFile && !parentFile.exists()) {
      if (!parentFile.mkdirs()) {
        throw new IOException("Unable to create parent directory of output "
            + "file. Not writing this file.");
      }
    }
    List<String> formattedOutputLines = new ArrayList<>();
    formattedOutputLines.add(OutputLine.columnHeadersDelimitedBy(","));
    for (OutputLine line : outputLines) {
      formattedOutputLines.add(line.toString());
    }
    Files.write(filePath, formattedOutputLines, StandardCharsets.UTF_8);
  }
}

