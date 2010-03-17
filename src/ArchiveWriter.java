import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.*;

public class ArchiveWriter {
  private Logger logger;
  public ArchiveWriter() {
    this.logger = Logger.getLogger(RelayDescriptorParser.class.getName());
  }

  private void store(byte[] data, String filename) {
    try {
      File file = new File(filename);
      if (!file.exists()) {
        this.logger.finer("Storing " + filename);
        file.getParentFile().mkdirs();
        BufferedOutputStream bos = new BufferedOutputStream(
            new FileOutputStream(file));
        bos.write(data, 0, data.length);
        bos.close();
      }
    } catch (IOException e) {
      // TODO handle
    }
  }

  public void storeConsensus(byte[] data, long validAfter) {
    SimpleDateFormat printFormat = new SimpleDateFormat(
        "yyyy/MM/yyyy-MM-dd-HH-mm-ss");
    printFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    String filename = "directory-archive/consensus/"
        + printFormat.format(new Date(validAfter)) + "-consensus";
    this.store(data, filename);
  }

  public void storeVote(byte[] data, long validAfter,
      String fingerprint, String digest) {
    SimpleDateFormat printFormat = new SimpleDateFormat(
        "yyyy/MM/yyyy-MM-dd-HH-mm-ss");
    printFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    String filename = "directory-archive/vote/"
        + printFormat.format(new Date(validAfter)) + "-vote-"
        + fingerprint + "-" + digest;
    this.store(data, filename);
  }

  public void storeServerDescriptor(byte[] data, String digest,
      long published) {
    SimpleDateFormat printFormat = new SimpleDateFormat("yyyy/MM/");
    printFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    String filename = "directory-archive/server-descriptor/"
        + printFormat.format(new Date(published))
        + digest.substring(0, 1) + "/" + digest.substring(1, 2) + "/"
        + digest;
    this.store(data, filename);
  }

  public void storeExtraInfoDescriptor(byte[] data,
      String extraInfoDigest, long published) {
    SimpleDateFormat descriptorFormat = new SimpleDateFormat("yyyy/MM/");
    descriptorFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    String filename = "directory-archive/extra-info/"
        + descriptorFormat.format(new Date(published))
        + extraInfoDigest.substring(0, 1) + "/"
        + extraInfoDigest.substring(1, 2) + "/"
        + extraInfoDigest;
    this.store(data, filename);
  }
}

