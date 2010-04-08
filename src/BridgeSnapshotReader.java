import java.io.*;
import java.util.*;
import java.util.logging.*;
import org.apache.commons.compress.compressors.gzip.*;
import org.apache.commons.compress.archivers.tar.*;

/**
 * Reads the half-hourly snapshots of bridge descriptors from Tonga.
 */
public class BridgeSnapshotReader {
  public BridgeSnapshotReader(BridgeDescriptorParser bdp,
      String bridgeDirectoriesDir, Set<String> countries) {
    Logger logger =
        Logger.getLogger(BridgeSnapshotReader.class.getName());
    SortedSet<String> parsed = new TreeSet<String>();
    File bdDir = new File(bridgeDirectoriesDir);
    File pbdFile = new File("stats/parsed-bridge-directories");
    boolean modified = false;
    if (bdDir.exists()) {
      if (pbdFile.exists()) {
        logger.fine("Reading file " + pbdFile.getAbsolutePath() + "...");
        try {
          BufferedReader br = new BufferedReader(new FileReader(pbdFile));
          String line = null;
          while ((line = br.readLine()) != null) {
            parsed.add(line);
          }
          br.close();
          logger.fine("Finished reading file "
              + pbdFile.getAbsolutePath() + ".");
        } catch (IOException e) {
          logger.log(Level.WARNING, "Failed reading file "
              + pbdFile.getAbsolutePath() + "!", e);
          return;
        }
      }
      logger.fine("Importing files in directory " + bridgeDirectoriesDir
         + "/...");
      Stack<File> filesInInputDir = new Stack<File>();
      filesInInputDir.add(bdDir);
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
              BufferedInputStream bis = new BufferedInputStream(tais);
              String fn = pop.getName();
              String dateTime = fn.substring(11, 21) + " "
                    + fn.substring(22, 24) + ":" + fn.substring(24, 26)
                    + ":" + fn.substring(26, 28);
              while ((tais.getNextTarEntry()) != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int len;
                byte[] data = new byte[1024];
                while ((len = bis.read(data, 0, 1024)) >= 0) {
                  baos.write(data, 0, len);
                }
                byte[] allData = baos.toByteArray();
                if (allData.length == 0) {
                  continue;
                }
                String ascii = new String(allData, "US-ASCII");
                BufferedReader br3 = new BufferedReader(new StringReader(
                    ascii));
                String firstLine = null;
                while ((firstLine = br3.readLine()) != null) {
                  if (firstLine.startsWith("@")) {
                    continue;
                  } else {
                    break;
                  }
                }
                if (firstLine.startsWith("r ")) {
                  bdp.parse(allData, dateTime, false);
                } else {
                  int start = -1, sig = -1, end = -1;
                  String startToken =
                      firstLine.startsWith("router ") ?
                      "router " : "extra-info ";
                  String sigToken = "\nrouter-signature\n";
                  String endToken = "\n-----END SIGNATURE-----\n";
                  while (end < ascii.length()) {
                    start = ascii.indexOf(startToken, end);
                    if (start < 0) {
                      break;
                    }
                    sig = ascii.indexOf(sigToken, start);
                    if (sig < 0) {
                      break;
                    }
                    sig += sigToken.length();
                    end = ascii.indexOf(endToken, sig);
                    if (end < 0) {
                      break;
                    }
                    end += endToken.length();
                    byte[] descBytes = new byte[end - start];
                    System.arraycopy(allData, start, descBytes, 0,
                        end - start);
                    bdp.parse(descBytes, dateTime, false);
                  }
                }
              }
            }
            in.close();

            /* Let's give some memory back, or we'll run out of it. */
            System.gc();

            parsed.add(pop.getName());
            modified = true;
          } catch (IOException e) {
            logger.log(Level.WARNING, "Could not parse bridge snapshot!",
                e);
            continue;
          }
        }
      }
      logger.fine("Finished importing files in directory "
          + bridgeDirectoriesDir + "/.");
      if (!parsed.isEmpty() && modified) {
        logger.fine("Writing file " + pbdFile.getAbsolutePath() + "...");
        try {
          pbdFile.getParentFile().mkdirs();
          BufferedWriter bw = new BufferedWriter(new FileWriter(pbdFile));
          for (String f : parsed) {
            bw.append(f + "\n");
          }
          bw.close();
          logger.fine("Finished writing file " + pbdFile.getAbsolutePath()
              + ".");
        } catch (IOException e) {
          logger.log(Level.WARNING, "Failed writing file "
              + pbdFile.getAbsolutePath() + "!", e);
        }
      }
    }
  }
}

