import java.io.*;
import java.text.*;
import java.util.*;
import java.util.logging.*;
import org.apache.commons.codec.binary.*;


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
        "yyyy/MM/dd/yyyy-MM-dd-HH-mm-ss");
    printFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    String filename = "directory-archive/consensus/"
        + printFormat.format(new Date(validAfter)) + "-consensus";
    this.store(data, filename);
  }

  public void storeVote(byte[] data, long validAfter,
      String fingerprint, String digest) {
    SimpleDateFormat printFormat = new SimpleDateFormat(
        "yyyy/MM/dd/yyyy-MM-dd-HH-mm-ss");
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

  private SortedSet<String> getFileNames(File dir) {
    SortedSet<String> files = new TreeSet<String>();
    Stack<File> leftToParse = new Stack<File>();
    leftToParse.add(dir);
    while (!leftToParse.isEmpty()) {
      File pop = leftToParse.pop();
      if (pop.isDirectory()) {
        for (File f : pop.listFiles()) {
          leftToParse.add(f);
        }
      } else if (pop.length() > 0) {
        String absPath = pop.getAbsolutePath().replaceAll(":", "-");
        String relPath = absPath.substring(absPath.indexOf(
            "directory-archive/"));
        files.add(relPath);
      }
    }
    return files;
  }

  /**
   * Dump some statistics on the completeness of descriptors to the logs
   * on level INFO.
   */
  public void dumpStats() {
    try {
      SortedSet<String> votes = getFileNames(
          new File("directory-archive/vote"));
      SortedSet<String> serverDescs = getFileNames(
          new File("directory-archive/server-descriptor"));
      SortedSet<String> extraInfos = getFileNames(
          new File("directory-archive/extra-info"));
      SortedSet<String> consensuses = getFileNames(
          new File("directory-archive/consensus"));
      SimpleDateFormat validAfterFormat =
          new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      validAfterFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
      SimpleDateFormat consensusVoteFormat =
          new SimpleDateFormat("yyyy/MM/dd/yyyy-MM-dd-HH-mm-ss");
      consensusVoteFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
      SimpleDateFormat descriptorFormat =
          new SimpleDateFormat("yyyy/MM/");
      descriptorFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
      StringBuilder sb = new StringBuilder();
      sb.append("  valid-after          votes         "
          + "server descriptors  extra-infos\n");
      SortedSet<String> lastConsensuses = new TreeSet<String>();
      for (int i = 0; !consensuses.isEmpty() && i < 12; i++) {
        String last = consensuses.last();
        lastConsensuses.add(last);
        consensuses.remove(last);
      }
      for (String f : lastConsensuses) {
        BufferedReader br = new BufferedReader(new FileReader(new File(f)));
        String line = null, validAfterTime = null, votePrefix = null;
        int allVotes = 0, foundVotes = 0,
            allServerDescs = 0, foundServerDescs = 0,
            allExtraInfos = 0, foundExtraInfos = 0;
        while ((line = br.readLine()) != null) {
          if (line.startsWith("valid-after ")) {
            validAfterTime = line.substring("valid-after ".length());
            long validAfter = validAfterFormat.parse(
                validAfterTime).getTime();
            votePrefix = "directory-archive/vote/"
                + consensusVoteFormat.format(new Date(validAfter))
                + "-vote-";
          } else if (line.startsWith("dir-source ")) {
            allVotes++;
            String pattern = votePrefix + line.split(" ")[2];
            String votefilename = null;
            for (String v : votes) {
              if (v.startsWith(pattern)) {
                votefilename = v;
                break;
              }
            }
            if (votefilename != null) {
              foundVotes++;
              BufferedReader vbr = new BufferedReader(new FileReader(
                  new File(votefilename)));
              String line3 = null;
              int voteAllServerDescs = 0, voteFoundServerDescs = 0,
                  voteAllExtraInfos = 0, voteFoundExtraInfos = 0;
              while ((line3 = vbr.readLine()) != null) {
                if (line3.startsWith("r ")) {
                  voteAllServerDescs++;
                  String digest = Hex.encodeHexString(Base64.decodeBase64(
                      line3.split(" ")[3] + "=")).toLowerCase();
                  long published = validAfterFormat.parse(
                      line3.split(" ")[4] + " "
                      + line3.split(" ")[5]).getTime();
                  String filename = "directory-archive/server-descriptor/"
                      + descriptorFormat.format(new Date(published))
                      + digest.substring(0, 1) + "/"
                      + digest.substring(1, 2) + "/" + digest;
                  if (serverDescs.contains(filename)) {
                    BufferedReader sbr = new BufferedReader(new FileReader(
                        new File(filename)));
                    String line2 = null;
                    while ((line2 = sbr.readLine()) != null) {
                      if (line2.startsWith("opt extra-info-digest ") ||
                          line2.startsWith("extra-info-digest ")) {
                        voteAllExtraInfos++;
                        String extraInfoDigest = line2.startsWith("opt ") ?
                            line2.split(" ")[2].toLowerCase() :
                            line2.split(" ")[1].toLowerCase();
                        String filename2 = "directory-archive/extra-info/"
                            + descriptorFormat.format(new Date(published))
                            + extraInfoDigest.substring(0, 1) + "/"
                            + extraInfoDigest.substring(1, 2) + "/"
                            + extraInfoDigest;
                        if (extraInfos.contains(filename2)) {
                          voteFoundExtraInfos++;
                        }
                      }
                    }
                    sbr.close();
                    voteFoundServerDescs++;
                  }
                }
              }
              vbr.close();
              sb.append(String.format("V %s               "
                  + " %d/%d (%5.1f%%)  %d/%d (%5.1f%%)%n",
                  validAfterTime,
                  voteFoundServerDescs, voteAllServerDescs,
                  100.0D * (double) voteFoundServerDescs /
                    (double) voteAllServerDescs,
                  voteFoundExtraInfos, voteAllExtraInfos,
                  100.0D * (double) voteFoundExtraInfos /
                    (double) voteAllExtraInfos));
            }
          } else if (line.startsWith("r ")) {
            allServerDescs++;
            String digest = Hex.encodeHexString(Base64.decodeBase64(
                line.split(" ")[3] + "=")).toLowerCase();
            long published = validAfterFormat.parse(
                line.split(" ")[4] + " " + line.split(" ")[5]).getTime();
            String filename = "directory-archive/server-descriptor/"
                + descriptorFormat.format(new Date(published))
                + digest.substring(0, 1) + "/"
                + digest.substring(1, 2) + "/" + digest;
            if (serverDescs.contains(filename)) {
              BufferedReader sbr = new BufferedReader(new FileReader(
                  new File(filename)));
              String line2 = null;
              while ((line2 = sbr.readLine()) != null) {
                if (line2.startsWith("opt extra-info-digest ") ||
                    line2.startsWith("extra-info-digest ")) {
                  allExtraInfos++;
                  String extraInfoDigest = line2.startsWith("opt ") ?
                      line2.split(" ")[2].toLowerCase() :
                      line2.split(" ")[1].toLowerCase();
                  String filename2 = "directory-archive/extra-info/"
                      + descriptorFormat.format(new Date(published))
                      + extraInfoDigest.substring(0, 1) + "/"
                      + extraInfoDigest.substring(1, 2) + "/"
                      + extraInfoDigest;
                  if (extraInfos.contains(filename2)) {
                    foundExtraInfos++;
                  }
                }
              }
              sbr.close();
              foundServerDescs++;
            }
          }
        }
        sb.append(String.format("C %s  %d/%d (%5.1f%%)  %d/%d (%5.1f%%)  "
            + "%d/%d (%5.1f%%)%n",
            validAfterTime, foundVotes, allVotes,
            100.0D * (double) foundVotes / (double) allVotes,
            foundServerDescs, allServerDescs,
            100.0D * (double) foundServerDescs / (double) allServerDescs,
            foundExtraInfos, allExtraInfos,
            100.0D * (double) foundExtraInfos / (double) allExtraInfos));
      }
      this.logger.info("Statistics on relay descriptors from the last 12 "
          + "known consensuses:\n" + sb.toString());
    } catch (IOException e) {
      this.logger.log(Level.WARNING, "Could not dump statistics to disk.",
          e);
    } catch (ParseException e) {
      this.logger.log(Level.WARNING, "Could not dump statistics to disk.",
          e);
    }
  }
}
