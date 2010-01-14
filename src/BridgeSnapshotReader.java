import java.io.*;
import java.text.*;
import java.util.*;
import org.apache.commons.compress.compressors.gzip.*;
import org.apache.commons.compress.archivers.tar.*;

/**
 * Reads the half-hourly snapshots of bridge descriptors from Tonga.
 */
public class BridgeSnapshotReader {
  public BridgeSnapshotReader(BridgeDescriptorParser bdp,
      String bridgeDirectoriesDir, String parsedBridgeDirectories,
      Set<String> countries) throws IOException, ParseException {
    SortedSet<String> parsed = new TreeSet<String>();
    File pbdFile = new File(parsedBridgeDirectories);
    if (pbdFile.exists()) {
      System.out.print("Reading existing file " + parsedBridgeDirectories
          + "... ");
      BufferedReader br = new BufferedReader(new FileReader(pbdFile));
      String line = null;
      while ((line = br.readLine()) != null) {
        parsed.add(line);
      }
      br.close();
      System.out.println("done");
    }
    File bdDir = new File(bridgeDirectoriesDir);
    if (bdDir.exists()) {
      System.out.print("Importing files in directory "
          + bridgeDirectoriesDir + "/... ");
      Stack<File> filesInInputDir = new Stack<File>();
      filesInInputDir.add(bdDir);
      while (!filesInInputDir.isEmpty()) {
        File pop = filesInInputDir.pop();
        if (pop.isDirectory()) {
          for (File f : pop.listFiles()) {
            filesInInputDir.add(f);
          }
        } else if (!parsed.contains(pop.getName())) {
          FileInputStream in = new FileInputStream(pop);
          GzipCompressorInputStream gcis =
              new GzipCompressorInputStream(in);
          TarArchiveInputStream tais = new TarArchiveInputStream(gcis);
          InputStreamReader isr = new InputStreamReader(tais);
          BufferedReader br = new BufferedReader(isr);
          TarArchiveEntry en = null;
          String fn = pop.getName();
          String dateTime = fn.substring(11, 21) + " "
                + fn.substring(22, 24) + ":" + fn.substring(24, 26) + ":"
                + fn.substring(26, 28);
          while ((en = tais.getNextTarEntry()) != null) {
            bdp.parse(br, dateTime, false);
          }
          br.close();
          parsed.add(pop.getName());
        }
      }
      System.out.print("done\nWriting file " + pbdFile + "... ");
      BufferedWriter bw = new BufferedWriter(new FileWriter(pbdFile));
      for (String f : parsed) {
        bw.append(f + "\n");
      }
      bw.close();
      System.out.println("done");
    }
  }
}

