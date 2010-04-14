import java.io.*;
import java.util.*;
import java.util.logging.*;
import org.apache.commons.compress.compressors.bzip2.*;

/**
 * Read in all files in a given directory and pass buffered readers of
 * them to the relay descriptor parser.
 */
public class ArchiveReader {
  public ArchiveReader(RelayDescriptorParser rdp, String archivesDir,
      boolean keepImportHistory) {
    int parsedFiles = 0, ignoredFiles = 0;
    Logger logger = Logger.getLogger(ArchiveReader.class.getName());
    SortedSet<String> archivesImportHistory = new TreeSet<String>();
    File archivesImportHistoryFile =
        new File("stats/archives-import-history");
    if (keepImportHistory && archivesImportHistoryFile.exists()) {
      try {
        BufferedReader br = new BufferedReader(new FileReader(
            archivesImportHistoryFile));
        String line = null;
        while ((line = br.readLine()) != null) {
          archivesImportHistory.add(line);
        }
        br.close();
      } catch (IOException e) {
        logger.log(Level.WARNING, "Could not read in archives import "
            + "history file. Skipping.");
      }
    }
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
              BufferedInputStream bis = null;
              if (keepImportHistory &&
                  archivesImportHistory.contains(pop.getName())) {
                ignoredFiles++;
                continue;
              } else if (pop.getName().endsWith(".tar.bz2")) {
                logger.warning("Cannot parse compressed tarball "
                    + pop.getAbsolutePath() + ". Skipping.");
                continue;
              } else if (pop.getName().endsWith(".bz2")) {
                FileInputStream fis = new FileInputStream(pop);
                BZip2CompressorInputStream bcis =
                    new BZip2CompressorInputStream(fis);
                bis = new BufferedInputStream(bcis);
              } else {
                FileInputStream fis = new FileInputStream(pop);
                bis = new BufferedInputStream(fis);
              }
              if (keepImportHistory) {
                archivesImportHistory.add(pop.getName());
              }
              ByteArrayOutputStream baos = new ByteArrayOutputStream();
              int len;
              byte[] data = new byte[1024];
              while ((len = bis.read(data, 0, 1024)) >= 0) {
                baos.write(data, 0, len);
              }
              bis.close();
              byte[] allData = baos.toByteArray();
              rdp.parse(allData);
              parsedFiles++;
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
    if (keepImportHistory) {
      try {
        archivesImportHistoryFile.getParentFile().mkdirs();
        BufferedWriter bw = new BufferedWriter(new FileWriter(
            archivesImportHistoryFile));
        for (String line : archivesImportHistory) {
          bw.write(line + "\n");
        }
        bw.close();
      } catch (IOException e) {
        logger.log(Level.WARNING, "Could not write archives import "
            + "history file.");
      }
    }
    logger.info("Finished importing relay descriptors from local "
        + "directory:\nParsed " + parsedFiles + ", ignored "
        + ignoredFiles + " files.");
  }
}

