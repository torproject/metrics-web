import java.io.*;
import java.util.*;

/**
 * Read in all files in a given directory and pass buffered readers of
 * them to the relay descriptor parser.
 */
public class ArchiveReader {
  public ArchiveReader(RelayDescriptorParser rdp, String archivesDir) {
    if (new File(archivesDir).exists()) {
      try {
        rdp.initialize();
      } catch (IOException e) {
        return;
      }
      System.out.print("Importing files in directory " + archivesDir
          + "/... ");
      Stack<File> filesInInputDir = new Stack<File>();
      filesInInputDir.add(new File(archivesDir));
      List<File> problems = new ArrayList<File>();
      while (!filesInInputDir.isEmpty()) {
        File pop = filesInInputDir.pop();
        if (pop.isDirectory()) {
          for (File f : pop.listFiles()) {
            filesInInputDir.add(f);
          }
        } else {
          try {
            BufferedReader br = new BufferedReader(new FileReader(pop));
            rdp.parse(br);
            br.close();
          } catch (IOException e) {
            problems.add(pop);
            if (problems.size() > 3) {
              break;
            }
          }
        }
      }
      if (problems.isEmpty()) {
        System.out.println("done");
      } else {
        System.out.println("failed");
        int printed = 0;
        for (File f : problems) {
          System.out.println("  " + f.getAbsolutePath());
          if (++printed >= 3) {
            System.out.println("  ... more");
            break;
          }
        }
      }
    }
  }
}

