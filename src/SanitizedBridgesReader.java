import java.io.*;
import java.util.*;
import java.util.logging.*;

public class SanitizedBridgesReader {
  public SanitizedBridgesReader(BridgeDescriptorParser bdp,
      String bridgesDir, SortedSet<String> countries,
      boolean keepImportHistory) {
    Logger logger =
        Logger.getLogger(SanitizedBridgesReader.class.getName());
    SortedSet<String> bridgesImportHistory = new TreeSet<String>();
    File bridgesImportHistoryFile =
        new File("stats/bridges-import-history");
    if (keepImportHistory && bridgesImportHistoryFile.exists()) {
      try {
        BufferedReader br = new BufferedReader(new FileReader(
            bridgesImportHistoryFile));
        String line = null;
        while ((line = br.readLine()) != null) {
          bridgesImportHistory.add(line);
        }
        br.close();
      } catch (IOException e) {
        logger.log(Level.WARNING, "Could not read in bridge descriptor "
            + "import history file. Skipping.");
      }
    }
    if (new File(bridgesDir).exists()) {
      logger.fine("Importing files in directory " + bridgesDir + "/...");
      Stack<File> filesInInputDir = new Stack<File>();
      filesInInputDir.add(new File(bridgesDir));
      List<File> problems = new ArrayList<File>();
      while (!filesInInputDir.isEmpty()) {
        File pop = filesInInputDir.pop();
        if (pop.isDirectory()) {
          for (File f : pop.listFiles()) {
            filesInInputDir.add(f);
          }
          continue;
        } else if (keepImportHistory && bridgesImportHistory.contains(
            pop.getName())) {
          continue;
        } else {
          try {
            BufferedInputStream bis = new BufferedInputStream(
                new FileInputStream(pop));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int len;
            byte[] data = new byte[1024];
            while ((len = bis.read(data, 0, 1024)) >= 0) {
              baos.write(data, 0, len);
            }
            bis.close();
            byte[] allData = baos.toByteArray();
            String fn = pop.getName();
            // TODO dateTime extraction doesn't work for sanitized network
            // statuses!
            String dateTime = fn.substring(0, 4) + "-" + fn.substring(4, 6)
                + "-" + fn.substring(6, 8) + " " + fn.substring(9, 11)
                + ":" + fn.substring(11, 13) + ":" + fn.substring(13, 15);
            bdp.parse(allData, dateTime, true);
            if (keepImportHistory) {
              bridgesImportHistory.add(pop.getName());
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
        logger.fine("Finished importing files in directory " + bridgesDir
            + "/.");
      } else {
        StringBuilder sb = new StringBuilder("Failed importing files in "
            + "directory " + bridgesDir + "/:");
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
      if (keepImportHistory) {
        try {
          bridgesImportHistoryFile.getParentFile().mkdirs();
          BufferedWriter bw = new BufferedWriter(new FileWriter(
              bridgesImportHistoryFile));
          for (String line : bridgesImportHistory) {
            bw.write(line + "\n");
          }
          bw.close();
        } catch (IOException e) {
          logger.log(Level.WARNING, "Could not write bridge descriptor "
              + "import history file.");
        }
      }
    }
  }
}

