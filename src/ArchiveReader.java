import java.io.*;
import java.util.*;

/**
 * Read in all files in a given directory and pass buffered readers of
 * them to the relay descriptor parser.
 */
public class ArchiveReader {
  public ArchiveReader(RelayDescriptorParser rdp, String archivesDir)
      throws IOException {
    if (new File(archivesDir).exists()) {
      System.out.print("Importing files in directory " + archivesDir
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
          rdp.parse(br);
          br.close();
        }
      }
      System.out.println("done");
    }
  }
}

