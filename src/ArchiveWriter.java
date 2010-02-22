import java.io.*;
import java.text.*;
import java.util.*;
import org.apache.commons.compress.archivers.tar.*;

public class ArchiveWriter {
  public ArchiveWriter() {
  }
  public void storeConsensus(BufferedReader br, String validAfterTime)
      throws IOException, ParseException {
    SimpleDateFormat parseFormat =
        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    parseFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    long validAfter = parseFormat.parse(validAfterTime).getTime();
    SimpleDateFormat printFormat =
        new SimpleDateFormat("yyyy/MM/dd/yyyy-MM-dd-HH-mm-ss");
    printFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    File consensusFile = new File("directory-archive/consensus/"
        + printFormat.format(new Date(validAfter)) + "-consensus");
    consensusFile.getParentFile().mkdirs();
    if (!consensusFile.exists()) {
      BufferedWriter bw = new BufferedWriter(new FileWriter(
          consensusFile));
      String line = null;
      while ((line = br.readLine()) != null) {
        bw.write(line + "\n");
        if (line.startsWith("r ")) {
          // TODO compile list of server descriptors that we might want to
          // learn about
        }
      }
      bw.close();
    }
  }
  public void storeVote(BufferedReader br, String validAfterTime,
      String authorityFingerprint) throws IOException {
    // TODO implement me
  }
  public void storeServerDescriptor(BufferedReader br, String digest,
      String publishedTime, String extraInfoDigest) throws IOException,
      ParseException {
    SimpleDateFormat parseFormat =
        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    parseFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    long published = parseFormat.parse(publishedTime).getTime();
    SimpleDateFormat printFormat = new SimpleDateFormat("yyyy/MM/");
    printFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    File descriptorFile = new File("directory-archive/server-descriptor/"
        + printFormat.format(new Date(published))
        + digest.substring(0, 1) + "/" + digest.substring(1, 2) + "/"
        + digest);
    descriptorFile.getParentFile().mkdirs();
    if (!descriptorFile.exists()) {
      BufferedWriter bw = new BufferedWriter(new FileWriter(
          descriptorFile));
      String line = null;
      while ((line = br.readLine()) != null) {
        bw.write(line + "\n");
      }
      bw.close();
    }
    // TODO if extraInfoDigest != null, add digest to extra-info
    // descriptors we want to download
  }
  public void storeExtraInfo(BufferedReader br, String digest,
      String publishedTime) throws IOException, ParseException {
    SimpleDateFormat parseFormat =
        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    parseFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    long published = parseFormat.parse(publishedTime).getTime();
    SimpleDateFormat printFormat = new SimpleDateFormat("yyyy/MM/");
    printFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    File descriptorFile = new File("directory-archive/extra-info/"
        + printFormat.format(new Date(published))
        + digest.substring(0, 1) + "/" + digest.substring(1, 2) + "/"
        + digest);
    descriptorFile.getParentFile().mkdirs();
    if (!descriptorFile.exists()) {
      BufferedWriter bw = new BufferedWriter(new FileWriter(
          descriptorFile));
      String line = null, extraInfoDigest = null;
      while ((line = br.readLine()) != null) {
        bw.write(line + "\n");
      }
      bw.close();
    }
  }
}

