import java.io.*;
import java.util.*;
import java.util.logging.*;

/**
 * Read in all files in a given directory and pass buffered readers of
 * them to the relay descriptor parser.
 */
public class ArchiveReader {
  public ArchiveReader(RelayDescriptorParser rdp, String archivesDir) {
    Logger logger = Logger.getLogger(ArchiveReader.class.getName());
    if (new File(archivesDir).exists()) {
      logger.fine("Importing files in directory " + archivesDir
          + "/...");
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
          if (rdp != null) {
            try {
              BufferedInputStream bis =
                  new BufferedInputStream(new FileInputStream(pop));
              ByteArrayOutputStream baos = new ByteArrayOutputStream();
              int len;
              byte[] data = new byte[1024];
              while ((len = bis.read(data, 0, 1024)) >= 0) {
                baos.write(data, 0, len);
              }
              bis.close();
              byte[] allData = baos.toByteArray();
              rdp.parse(allData);
            } catch (IOException e) {
              problems.add(pop);
              if (problems.size() > 3) {
                break;
              }
            }
          }
        }
      }
      if (problems.isEmpty()) {
        logger.fine("Finished importing files in directory " + archivesDir
            + "/.");
      } else {
        StringBuilder sb = new StringBuilder("Failed importing files in "
            + "directory " + archivesDir + "/:");
        int printed = 0;
        for (File f : problems) {
          sb.append("\n  " + f.getAbsolutePath());
          if (++printed >= 3) {
            sb.append("\n  ... more");
            break;
          }
        }
      }
    }
  }
}

