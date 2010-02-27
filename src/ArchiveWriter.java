import java.io.*;
import java.text.*;
import java.util.*;
import java.util.logging.*;
import org.apache.commons.codec.digest.*;
import org.apache.commons.codec.binary.*;

public class ArchiveWriter {
  private SortedSet<String> v3DirectoryAuthorities;
  private File missingDescriptorsFile;
  private SortedSet<String> missingDescriptors;
  private boolean missingDescriptorsFileModified = false;
  private Logger logger;
  public ArchiveWriter(SortedSet<String> v3DirectoryAuthorities) {
    this.v3DirectoryAuthorities = v3DirectoryAuthorities;
    this.missingDescriptorsFile = new File(
        "stats/archive-writer-parse-history");
    this.logger = Logger.getLogger(RelayDescriptorParser.class.getName());
    SimpleDateFormat parseFormat =
        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    parseFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    this.missingDescriptors = new TreeSet<String>();
    SimpleDateFormat consensusVoteFormat =
        new SimpleDateFormat("yyyy/MM/dd/yyyy-MM-dd-HH-mm-ss");
    consensusVoteFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    SimpleDateFormat descriptorFormat =
        new SimpleDateFormat("yyyy/MM/");
    descriptorFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    if (this.missingDescriptorsFile.exists()) {
      this.logger.info("Reading file "
          + this.missingDescriptorsFile.getAbsolutePath() + "...");
      try {
        BufferedReader br = new BufferedReader(new FileReader(
            this.missingDescriptorsFile));
        String line = null;
        long now = System.currentTimeMillis();
        while ((line = br.readLine()) != null) {
          // only add to download list if descriptors are still available
          // on directories
          long published = parseFormat.parse(line.split(",")[2]).
              getTime();
          if (line.startsWith("consensus") &&
              published + 55L * 60L * 1000L > now) {
            File consensusFile = new File("directory-archive/consensus/"
                + consensusVoteFormat.format(new Date(published))
                + "-consensus");
            if (!consensusFile.exists()) {
              this.logger.fine("Initializing missing list with "
                  + "consensus: valid-after=" + line.split(",")[2]
                  + ", filename=directory-archive/consensus/"
                  + consensusVoteFormat.format(new Date(published))
                  + "-consensus");
              this.missingDescriptors.add(line);
            }
          } else if (line.startsWith("vote") &&
              published + 55L * 60L * 1000L > now) {
              // TODO is vote even available for 55 minutes after its
              // publication?
            File voteFile = new File("directory-archive/vote/"
                + consensusVoteFormat.format(new Date(published))
                + "-vote-" + line.split(",")[1]);
            File voteFileDir = voteFile.getParentFile();
            String voteFileName = voteFile.getName();
            boolean voteFileFound = false;
            if (voteFileDir.exists()) {
              for (File f : Arrays.asList(voteFileDir.listFiles())) {
                if (f.getName().startsWith(voteFileName)) {
                  voteFileFound = true;
                  break;
                }
              }
            }
            if (!voteFileFound) {
              this.logger.fine("Initializing missing list with vote: "
                  + "fingerprint=" + line.split(",")[1]
                  + ", valid-after="
                  + consensusVoteFormat.format(new Date(published))
                  + ", filename=directory-archive/vote/"
                  + consensusVoteFormat.format(new Date(published))
                  + "-vote-" + line.split(",")[1] + "-*");
              this.missingDescriptors.add(line);
            }
          } else if ((line.startsWith("server") ||
              line.startsWith("extra")) &&
              published + 24L * 60L * 60L * 1000L > now) {
              // TODO are 24 hours okay?
            boolean isServerDesc = line.startsWith("server");
            String digest = line.split(",")[1].toLowerCase();
            File descriptorFile = new File("directory-archive/"
                + (isServerDesc ? "server-descriptor" : "extra-info")
                + "/" + descriptorFormat.format(new Date(published))
                + digest.substring(0, 1) + "/" + digest.substring(1, 2)
                + "/" + digest);
            if (!descriptorFile.exists()) {
              this.logger.fine("Initializing missing list with "
                  + (isServerDesc ? "server" : "extra-info")
                  + " descriptor: digest=" + digest
                  + ", filename=directory-archive/server-descriptor/"
                  + descriptorFormat.format(new Date(published))
                  + line.split(",")[1].substring(0, 1) + "/"
                  + line.split(",")[1].substring(1, 2) + "/"
                  + line.split(",")[1]);
              this.missingDescriptors.add(line);
            }
          }
        }
        br.close();
        this.logger.info("Finished reading file "
            + this.missingDescriptorsFile.getAbsolutePath() + ".");
      } catch (ParseException e) {
        this.logger.log(Level.WARNING, "Failed reading file "
            + this.missingDescriptorsFile.getAbsolutePath()
            + "! This means that we might forget to dowload descriptors "
            + "we are missing.", e);
      } catch (IOException e) {
        this.logger.log(Level.WARNING, "Failed reading file "
            + this.missingDescriptorsFile.getAbsolutePath()
            + "! This means that we might forget to dowload descriptors "
            + "we are missing.", e);
      }
    }
    // add current consensus and votes to list
    SimpleDateFormat consensusFormat =
        new SimpleDateFormat("yyyy-MM-dd HH");
    consensusFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    String nowConsensusFormat = consensusFormat.format(new Date())
        + ":00:00";
    long nowConsensus = (System.currentTimeMillis() / (60L * 60L * 1000L))
        * (60L * 60L * 1000L);
    for (String authority : this.v3DirectoryAuthorities) {
      File voteFile = new File("directory-archive/vote/"
          + consensusVoteFormat.format(new Date(nowConsensus))
          + "-vote-" + authority);
      if (!this.missingDescriptors.contains("vote," + authority + ","
          + nowConsensusFormat)) {
        File voteFileDir = voteFile.getParentFile();
        String voteFileName = voteFile.getName();
        boolean voteFileFound = false;
        if (voteFileDir.exists()) {
          for (File f : Arrays.asList(voteFileDir.listFiles())) {
            if (f.getName().startsWith(voteFileName)) {
              voteFileFound = true;
              break;
            }
          }
        }
        if (!voteFileFound) {
          this.logger.fine("Adding vote to missing list: fingerprint="
              + authority + ", valid-after="
              + consensusVoteFormat.format(new Date(nowConsensus))
              + ", filename=directory-archive/vote/"
              + consensusVoteFormat.format(new Date(nowConsensus))
              + "-vote-" + authority + "-*");
          this.missingDescriptors.add("vote," + authority + ","
              + nowConsensusFormat);
          this.missingDescriptorsFileModified = true;
        }
      }
    }
    File consensusFile = new File("directory-archive/consensus/"
        + consensusVoteFormat.format(new Date(nowConsensus))
        + "-consensus");
    if (!this.missingDescriptors.contains("consensus,NA,"
        + nowConsensusFormat) && !consensusFile.exists()) {
      this.logger.fine("Adding consensus to missing list: valid-after="
          + nowConsensusFormat
          + ", filename=directory-archive/consensus/"
          + consensusVoteFormat.format(new Date(nowConsensus))
          + "-consensus");
      this.missingDescriptors.add("consensus,NA,"
          + nowConsensusFormat);
      this.missingDescriptorsFileModified = true;
    }
  }
  public void store(byte[] data) throws IOException, ParseException {
    BufferedReader br = new BufferedReader(new StringReader(new String(
        data, "US-ASCII")));
    String line = br.readLine();
    if (line == null) {
      this.logger.warning("Someone gave us an empty file for storing!");
      return;
    }
    StringBuilder sb = new StringBuilder();
    sb.append(line + "\n");
    SimpleDateFormat parseFormat =
        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    parseFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    if (line.equals("network-status-version 3")) {
      // TODO when parsing the current consensus, check the fresh-until
      // time to see when we switch from hourly to half-hourly
      // consensuses; in that case, add next half-hourly consensus to
      // missing list and warn!
      boolean isConsensus = true;
      String validAfterTime = null;
      long validAfter = -1L;
      long now = System.currentTimeMillis();
      String fingerprint = null;
      SimpleDateFormat descriptorFormat =
          new SimpleDateFormat("yyyy/MM/");
      descriptorFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
      while ((line = br.readLine()) != null) {
        sb.append(line + "\n");
        if (line.equals("vote-status vote")) {
          isConsensus = false;
        } else if (line.startsWith("valid-after ")) {
          validAfterTime = line.substring("valid-after ".length());
          validAfter = parseFormat.parse(validAfterTime).getTime();
        } else if (line.startsWith("dir-source ") &&
            !this.v3DirectoryAuthorities.contains(
            line.split(" ")[2]) && validAfter + 55L * 60L * 1000L > now) {
          this.logger.warning("Unknown v3 directory authority fingerprint "
              + "in consensus line '" + line + "'. You should update your "
              + "V3DirectoryAuthorities config option!");
          fingerprint = line.split(" ")[2];
          long nowConsensus = (now / (60L * 60L * 1000L))
              * (60L * 60L * 1000L);
          SimpleDateFormat consensusVoteFormat =
              new SimpleDateFormat("yyyy/MM/dd/yyyy-MM-dd-HH-mm-ss");
          consensusVoteFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
          File voteFile = new File("directory-archive/vote/"
                + consensusVoteFormat.format(new Date(nowConsensus))
                + "-vote-" + fingerprint);
          if (!this.missingDescriptors.contains("vote," + fingerprint
              + "," + parseFormat.format(new Date(nowConsensus)))) {
            File voteFileDir = voteFile.getParentFile();
            String voteFileName = voteFile.getName();
            boolean voteFileFound = false;
            if (voteFileDir.exists()) {
              for (File f : Arrays.asList(voteFileDir.listFiles())) {
                if (f.getName().startsWith(voteFileName)) {
                  voteFileFound = true;
                  break;
                }
              }
            }
            if (!voteFileFound) {
              this.logger.fine("Adding vote to missing list: fingerprint="
                  + fingerprint + ", valid-after="
                  + parseFormat.format(new Date(nowConsensus))
                  + ", filename=directory-archive/vote/"
                  + consensusVoteFormat.format(new Date(nowConsensus))
                  + "-vote-" + fingerprint + "-*");
              this.missingDescriptors.add("vote," + fingerprint + ","
                  + parseFormat.format(new Date(nowConsensus)));
              this.missingDescriptorsFileModified = true;
            }
          }
        } else if (line.startsWith("fingerprint ")) {
          fingerprint = line.split(" ")[1];
        } else if (line.startsWith("r ")) {
          String publishedTime = line.split(" ")[4] + " "
              + line.split(" ")[5];
          long published = parseFormat.parse(publishedTime).getTime();
          String serverDesc = Hex.encodeHexString(Base64.decodeBase64(
              line.split(" ")[3] + "=")).toLowerCase();
          // TODO are 24 hours okay?
          File descriptorFile = new File(
              "directory-archive/server-descriptor/"
              + descriptorFormat.format(new Date(published))
              + serverDesc.substring(0, 1) + "/"
              + serverDesc.substring(1, 2) + "/" + serverDesc);
          if (published + 24L * 60L * 60L * 1000L > now &&
              !this.missingDescriptors.contains("server," + serverDesc
                + "," + publishedTime) && !descriptorFile.exists()) {
            this.logger.fine("Adding server descriptor to missing list: "
                + "digest=" + serverDesc
                + ", filename=directory-archive/server-descriptor/"
                + descriptorFormat.format(new Date(published))
                + serverDesc.substring(0, 1) + "/"
                + serverDesc.substring(1, 2) + "/" + serverDesc);
            this.missingDescriptors.add("server," + serverDesc + ","
                + publishedTime);
            this.missingDescriptorsFileModified = true;
          }
        }
      }
      SimpleDateFormat printFormat =
          new SimpleDateFormat("yyyy/MM/dd/yyyy-MM-dd-HH-mm-ss");
      printFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
      if (isConsensus) {
        File consensusFile = new File("directory-archive/consensus/"
            + printFormat.format(new Date(validAfter)) + "-consensus");
        if (!consensusFile.exists()) {
          this.logger.fine("Storing consensus: valid-after="
              + validAfterTime + ", filename=directory-archive/consensus/"
              + printFormat.format(new Date(validAfter)) + "-consensus");
          consensusFile.getParentFile().mkdirs();
          BufferedOutputStream bos = new BufferedOutputStream(
              new FileOutputStream(consensusFile));
          bos.write(data, 0, data.length);
          bos.close();
          this.logger.fine("Removing consensus from missing list: "
              + "valid-after=" + validAfterTime
              + ", filename=directory-archive/consensus/"
              + printFormat.format(new Date(validAfter)) + "-consensus");
          this.missingDescriptors.remove("consensus,NA,"
              + validAfterTime);
          this.missingDescriptorsFileModified = true;
        } else {
          this.logger.info("Not storing consensus, because we already "
              + "have it: valid-after=" + validAfterTime
              + ", filename=directory-archive/consensus/"
              + printFormat.format(new Date(validAfter)) + "-consensus");
        }
      } else {
        String ascii = new String(data, "US-ASCII");
        String startToken = "network-status-version ";
        String sigToken = "directory-signature ";
        int start = ascii.indexOf(startToken);
        int sig = ascii.indexOf(sigToken);
        if (start < 0 || sig < 0 || sig < start) {
          this.logger.info("Cannot determine vote digest! Skipping.");
          return;
        }
        sig += sigToken.length();
        byte[] forDigest = new byte[sig - start];
        System.arraycopy(data, start, forDigest, 0, sig - start);
        String digest = DigestUtils.shaHex(forDigest).toUpperCase();
        File voteFile = new File("directory-archive/vote/"
            + printFormat.format(new Date(validAfter)) + "-vote-"
            + fingerprint + "-" + digest);
        if (!voteFile.exists()) {
          this.logger.fine("Storing vote: fingerprint=" + fingerprint
              + ", valid-after="
              + printFormat.format(new Date(validAfter))
              + ", filename=directory-archive/vote/"
              + printFormat.format(new Date(validAfter)) + "-vote-"
              + fingerprint + "-" + digest);
          voteFile.getParentFile().mkdirs();
          BufferedOutputStream bos = new BufferedOutputStream(
              new FileOutputStream(voteFile));
          bos.write(data, 0, data.length);
          bos.close();
          this.logger.fine("Removing vote from missing list: "
              + "fingerprint=" + fingerprint + ", valid-after="
              + printFormat.format(new Date(validAfter))
              + ", filename=directory-archive/vote/"
              + printFormat.format(new Date(validAfter)) + "-vote-"
              + fingerprint + "-" + digest);
          this.missingDescriptors.remove("vote," + fingerprint + ","
              + validAfterTime);
          this.missingDescriptorsFileModified = true;
        } else {
          this.logger.info("Not storing vote, because we already have "
              + "it: fingerprint=" + fingerprint + ", valid-after="
              + printFormat.format(new Date(validAfter))
              + ", filename=directory-archive/vote/"
              + printFormat.format(new Date(validAfter)) + "-vote-"
              + fingerprint + "-" + digest);
        }
      }
    } else if (line.startsWith("router ") ||
        line.startsWith("extra-info ")) {
      boolean isServerDescriptor = line.startsWith("router ");
      String publishedTime = null;
      long published = -1L;
      while ((line = br.readLine()) != null) {
        if (line.startsWith("published ")) {
          publishedTime = line.substring("published ".length());
          published = parseFormat.parse(publishedTime).getTime();
        } else if (line.startsWith("opt extra-info-digest ") ||
            line.startsWith("extra-info-digest ")) {
          String extraInfoDigest = line.startsWith("opt ") ?
              line.split(" ")[2].toLowerCase() :
              line.split(" ")[1].toLowerCase();
          SimpleDateFormat descriptorFormat =
              new SimpleDateFormat("yyyy/MM/");
          descriptorFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
          File descriptorFile = new File("directory-archive/extra-info/"
              + descriptorFormat.format(new Date(published))
              + extraInfoDigest.substring(0, 1) + "/"
              + extraInfoDigest.substring(1, 2) + "/"
              + extraInfoDigest);
          if (!this.missingDescriptors.contains("extra,"
              + extraInfoDigest + "," + publishedTime) &&
              !descriptorFile.exists()) {
            this.logger.fine("Adding extra-info descriptor to missing "
                + "list: digest=" + extraInfoDigest
                + ", filename=directory-archive/extra-info/"
                + descriptorFormat.format(new Date(published))
                + extraInfoDigest.substring(0, 1) + "/"
                + extraInfoDigest.substring(1, 2) + "/"
                + extraInfoDigest);
            this.missingDescriptors.add("extra," + extraInfoDigest + ","
                + publishedTime);
            this.missingDescriptorsFileModified = true;
          }
        }
      }
      String ascii = new String(data, "US-ASCII");
      String startToken = isServerDescriptor ?
          "router " : "extra-info ";
      String sigToken = "\nrouter-signature\n";
      int start = ascii.indexOf(startToken);
      int sig = ascii.indexOf(sigToken) + sigToken.length();
      if (start < 0 || sig < 0 || sig < start) {
        this.logger.info("Cannot determine descriptor digest! Skipping.");
        return;
      }
      byte[] forDigest = new byte[sig - start];
      System.arraycopy(data, start, forDigest, 0, sig - start);
      String digest = DigestUtils.shaHex(forDigest);
      SimpleDateFormat printFormat = new SimpleDateFormat("yyyy/MM/");
      printFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
      File descriptorFile = new File("directory-archive/"
          + (isServerDescriptor ? "server-descriptor" : "extra-info") + "/"
          + printFormat.format(new Date(published))
          + digest.substring(0, 1) + "/" + digest.substring(1, 2) + "/"
          + digest);
      if (!descriptorFile.exists()) {
        this.logger.fine("Storing " + (isServerDescriptor ?
            "server descriptor" : "extra-info descriptor")
            + ": digest=" + digest + ", filename=directory-archive/"
            + (isServerDescriptor ? "server-descriptor" : "extra-info")
            + "/" + printFormat.format(new Date(published))
            + digest.substring(0, 1) + "/" + digest.substring(1, 2)
            + "/" + digest);
        descriptorFile.getParentFile().mkdirs();
        BufferedOutputStream bos = new BufferedOutputStream(
            new FileOutputStream(descriptorFile));
        bos.write(data, 0, data.length);
        bos.close();
        this.logger.fine("Removing " + (isServerDescriptor ?
            "server descriptor" : "extra-info descriptor")
            + " from missing list: digest=" + digest
            + ", filename=directory-archive/"
            + (isServerDescriptor ? "server-descriptor" : "extra-info")
            + "/" + printFormat.format(new Date(published))
            + digest.substring(0, 1) + "/" + digest.substring(1, 2) + "/"
            + digest);
        if (isServerDescriptor) {
          this.missingDescriptors.remove("server," + digest + ","
              + publishedTime);
        } else {
          this.missingDescriptors.remove("extra," + digest + ","
              + publishedTime);
        }
        this.missingDescriptorsFileModified = true;
      } else {
        this.logger.info("Not storing " + (isServerDescriptor ?
            "server descriptor" : "extra-info descriptor")
            + ", because we already have it: digest=" + digest
            + ", filename=directory-archive/"
            + (isServerDescriptor ? "server-descriptor" : "extra-info")
            + "/" + printFormat.format(new Date(published))
            + digest.substring(0, 1) + "/" + digest.substring(1, 2) + "/"
            + digest);
      }
    }
  }
  public Set<String> getMissingDescriptorUrls() {
    Set<String> urls = new HashSet<String>();
    for (String line : this.missingDescriptors) {
      if (line.startsWith("consensus,")) {
        urls.add("/tor/status-vote/current/consensus");
      } else if (line.startsWith("vote,")) {
        urls.add("/tor/status-vote/current/" + line.split(",")[1]);
      } else if (line.startsWith("server,")) {
        urls.add("/tor/server/d/" + line.split(",")[1]);
      } else if (line.startsWith("extra,")) {
        urls.add("/tor/extra/d/" + line.split(",")[1]);
      }
    }
    return urls;
  }
  public void writeFile() {
    if (this.missingDescriptorsFileModified) {
      try {
        this.logger.info("Writing file "
            + this.missingDescriptorsFile.getAbsolutePath() + "...");
        this.missingDescriptorsFile.getParentFile().mkdirs();
        BufferedWriter bw = new BufferedWriter(new FileWriter(
            this.missingDescriptorsFile));
        for (String line : this.missingDescriptors) {
          bw.write(line + "\n");
        }
        bw.close();
        this.logger.info("Finished writing file "
            + this.missingDescriptorsFile.getAbsolutePath() + ".");
      } catch (IOException e) {
        this.logger.log(Level.WARNING, "Failed writing "
            + this.missingDescriptorsFile.getAbsolutePath() + "!", e);
      }
    }
  }
}

