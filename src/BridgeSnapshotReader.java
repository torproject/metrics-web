import java.io.*;
import java.text.*;
import java.util.*;
import java.util.logging.*;
import org.apache.commons.compress.compressors.gzip.*;
import org.apache.commons.compress.archivers.tar.*;

/**
 * Reads the half-hourly snapshots of bridge descriptors from Tonga.
 */
public class BridgeSnapshotReader {
  public BridgeSnapshotReader(BridgeDescriptorParser bdp,
      String bridgeDirectoriesDir, String statsDirectory,
      Set<String> countries) {
    Logger logger =
        Logger.getLogger(BridgeSnapshotReader.class.getName());
    SortedSet<String> parsed = new TreeSet<String>();
    File bdDir = new File(bridgeDirectoriesDir);
    File pbdFile = new File(statsDirectory
         + "/parsed-bridge-directories");
    boolean modified = false;
    if (bdDir.exists()) {
      if (pbdFile.exists()) {
        logger.info("Reading file " + statsDirectory
            + "/parsed-bridge-directories...");
        try {
          BufferedReader br = new BufferedReader(new FileReader(pbdFile));
          String line = null;
          while ((line = br.readLine()) != null) {
            parsed.add(line);
          }
          br.close();
          logger.info("Finished reading file " + statsDirectory
              + "/parsed-bridge-directories.");
        } catch (IOException e) {
          logger.log(Level.WARNING, "Failed reading file "
              + statsDirectory + "/parsed-bridge-directories!", e);
          return;
        }
      }
      logger.info("Importing files in directory " + bridgeDirectoriesDir
         + "/...");
      Stack<File> filesInInputDir = new Stack<File>();
      filesInInputDir.add(bdDir);
      List<File> problems = new ArrayList<File>();
      while (!filesInInputDir.isEmpty()) {
        File pop = filesInInputDir.pop();
        if (pop.isDirectory()) {
          for (File f : pop.listFiles()) {
            filesInInputDir.add(f);
          }
        } else if (!parsed.contains(pop.getName())) {
          try {
            FileInputStream in = new FileInputStream(pop);
            if (in.available() > 0) {
              GzipCompressorInputStream gcis =
                  new GzipCompressorInputStream(in);
              TarArchiveInputStream tais = new TarArchiveInputStream(gcis);
              InputStreamReader isr = new InputStreamReader(tais);
              BufferedReader br = new BufferedReader(isr);
              TarArchiveEntry en = null;
              String fn = pop.getName();
              String dateTime = fn.substring(11, 21) + " "
                    + fn.substring(22, 24) + ":" + fn.substring(24, 26)
                    + ":" + fn.substring(26, 28);
              while ((en = tais.getNextTarEntry()) != null) {
                bdp.parse(br, dateTime, false);
              }
            }
            in.close();
            parsed.add(pop.getName());
            modified = true;
          } catch (ParseException e) {
            problems.add(pop);
            if (problems.size() > 3) {
              break;
            }
          } catch (IOException e) {
            problems.add(pop);
            if (problems.size() > 3) {
              break;
            }
          }
        }
      }
      if (problems.isEmpty()) {
        logger.info("Finished importing files in directory "
            + bridgeDirectoriesDir + "/.");
      } else {
        StringBuilder sb = new StringBuilder("Failed importing files in "
            + "directory " + bridgeDirectoriesDir + "/:");
        int printed = 0;
        for (File f : problems) {
          sb.append("\n  " + f.getAbsolutePath());
          if (++printed >= 3) {
            sb.append("\n  ... more");
            break;
          }
        }
        logger.warning(sb.toString());
      }
      if (!parsed.isEmpty() && modified) {
        logger.info("Writing file " + pbdFile + "...");
        try {
          new File(statsDirectory).mkdirs();
          BufferedWriter bw = new BufferedWriter(new FileWriter(pbdFile));
          for (String f : parsed) {
            bw.append(f + "\n");
          }
          bw.close();
          logger.info("Finished writing file " + pbdFile + ".");
        } catch (IOException e) {
          logger.log(Level.WARNING, "Failed writing file "
              + pbdFile + "!", e);
        }
      }
    }
  }
}

