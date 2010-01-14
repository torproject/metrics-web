import java.io.*;
import java.util.*;

/**
 * Read in all files in a given directory, decide what category they are
 * in, and pass them to the appropriate Parsers.
 */
public class ArchiveReader {
  public ArchiveReader(ConsensusParser cp, ServerDescriptorParser sdp,
      ExtraInfoParser eip, String archivesDir, Set<String> directoryKeys)
      throws IOException {
    System.out.print("Parsing all files in directory " + archivesDir
        + "/... ");
    Stack<File> filesInInputDir = new Stack<File>();
    filesInInputDir.add(new File(archivesDir));
    while (!filesInInputDir.isEmpty()) {
      BufferedReader br = null;
      File pop = filesInInputDir.pop();
      if (pop.isDirectory()) {
        for (File f : pop.listFiles()) {
          filesInInputDir.add(f);
        }
      } else {
        br = new BufferedReader(new FileReader(pop));
        String line = br.readLine();
        if (line.equals("network-status-version 3")) {
          cp.parse(br);
        } else if (line.startsWith("router ")) {
          sdp.parse(br);
        } else if (line.startsWith("extra-info ")
            && directoryKeys.contains(line.split(" ")[2])) {
          eip.parse(line.split(" ")[2], br);
        }
        br.close();
      }
    }
    System.out.println("done");
  }
}

