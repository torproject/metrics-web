import java.io.*;
import java.text.*;
import java.util.*;
import java.util.logging.*;
import org.apache.commons.codec.digest.*;
import org.apache.commons.codec.binary.*;

public class ArchiveWriter {
  private String statsDir;
  private SortedSet<String> v3DirectoryAuthorities;
  private File archiveWriterParseHistory;
  private SortedSet<String> missingDescriptors;
  private String lastParsedConsensus;
  private boolean initialized = false;
  private boolean archiveWriterParseHistoryModified = false;
  private Logger logger;
  private String parseTime;
  public ArchiveWriter(String statsDir,
      SortedSet<String> v3DirectoryAuthorities) {
    this.statsDir = statsDir;
    this.v3DirectoryAuthorities = v3DirectoryAuthorities;
    this.archiveWriterParseHistory = new File(statsDir
        + "/archive-writer-parse-history");
    this.logger = Logger.getLogger(RelayDescriptorParser.class.getName());
    SimpleDateFormat parseFormat =
        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    parseFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    this.parseTime = parseFormat.format(new Date());
    this.missingDescriptors = new TreeSet<String>();
    SimpleDateFormat consensusVoteFormat =
        new SimpleDateFormat("yyyy/MM/dd/yyyy-MM-dd-HH-mm-ss");
    consensusVoteFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    SimpleDateFormat descriptorFormat =
        new SimpleDateFormat("yyyy/MM/");
    descriptorFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    if (this.archiveWriterParseHistory.exists()) {
      this.logger.info("Reading file " + statsDir
          + "/archive-writer-parse-history...");
      try {
        BufferedReader br = new BufferedReader(new FileReader(
            this.archiveWriterParseHistory));
        String line = null;
        SimpleDateFormat publishedFormat =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        publishedFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        long now = System.currentTimeMillis();
        while ((line = br.readLine()) != null) {
          // only add to download list if descriptors are still available
          // on directories
          long published = publishedFormat.parse(line.split(",")[2]).
              getTime();
          if (line.startsWith("consensus") &&
              published + 55L * 60L * 1000L > now &&
              !new File("directory-archive/consensus/"
                + consensusVoteFormat.format(new Date(published))
                + "-consensus").exists()) {
            this.logger.fine("Initializing missing list with "
                + "consensus: valid-after=" + line.split(",")[2]
                + ", filename=directory-archive/consensus/"
                + consensusVoteFormat.format(new Date(published))
                + "-consensus");
            this.missingDescriptors.add(line);
          } else if (line.startsWith("vote") &&
              published + 55L * 60L * 1000L > now &&
              // TODO is vote even available for 55 minutes after its
              // publication?
              !new File("directory-archive/vote/"
                + consensusVoteFormat.format(new Date(published))
                + "-vote-" + line.split(",")[1]).exists()) {
            this.logger.fine("Initializing missing list with vote: "
                + "fingerprint=" + line.split(",")[1]
                + ", valid-after="
                + consensusVoteFormat.format(new Date(published))
                + ", filename=directory-archive/vote/"
                + consensusVoteFormat.format(new Date(published))
                + "-vote-" + line.split(",")[1]);
            this.missingDescriptors.add(line);
          } else if ((line.startsWith("server") ||
              line.startsWith("extra")) &&
              published + 24L * 60L * 60L * 1000L > now) {
              // TODO are 24 hours okay?
            boolean isServerDesc = line.startsWith("server");
            String digest = line.split(",")[1].toLowerCase();
            if (!new File("directory-archive/"
                + (isServerDesc ? "server-descriptor" : "extra-info")
                + "/" + descriptorFormat.format(new Date(published))
                + digest.substring(0, 1) + "/" + digest.substring(1, 2)
                + "/" + digest).exists()) {
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
        this.logger.info("Finished reading file " + statsDir
            + "/archive-writer-parse-history");
      } catch (ParseException e) {
        this.logger.log(Level.WARNING, "Failed reading file " + statsDir
            + "/archive-writer-parse-history! This means that we might "
            + "forget to dowload descriptors we are missing.", e);
      } catch (IOException e) {
        this.logger.log(Level.WARNING, "Failed reading file " + statsDir
            + "/archive-writer-parse-history! This means that we might "
            + "forget to dowload descriptors we are missing.", e);
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
      if (!new File("directory-archive/vote/"
            + consensusVoteFormat.format(new Date(nowConsensus))
            + "-vote-" + authority).exists()) {
        if (!this.missingDescriptors.contains("vote," + authority + ","
            + nowConsensusFormat)) {
          this.logger.fine("Adding vote to missing list: fingerprint="
              + authority + ", valid-after="
              + consensusVoteFormat.format(new Date(nowConsensus))
              + ", filename=directory-archive/vote/"
              + consensusVoteFormat.format(new Date(nowConsensus))
              + "-vote-" + authority);
          this.missingDescriptors.add("vote," + authority + ","
              + nowConsensusFormat);
          this.archiveWriterParseHistoryModified = true;
        }
      }
    }
    if (!new File("directory-archive/consensus/"
        + consensusVoteFormat.format(new Date(nowConsensus))
        + "-consensus").exists()) {
      if (!this.missingDescriptors.contains("consensus,NA,"
          + nowConsensusFormat)) {
        this.logger.fine("Adding consensus to missing list: valid-after="
            + nowConsensusFormat
            + ", filename=directory-archive/consensus/"
            + consensusVoteFormat.format(new Date(nowConsensus))
            + "-consensus");
        this.missingDescriptors.add("consensus,NA,"
            + nowConsensusFormat);
        this.archiveWriterParseHistoryModified = true;
      }
    }
  }
  public void store(BufferedReader br) throws IOException,
      ParseException {
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
            line.split(" ")[2]) && validAfter + 55L * 60L * 1000L <
            System.currentTimeMillis()) {
          this.logger.warning("Unknown v3 directory authority fingerprint "
              + "in consensus line '" + line + "'. You should update your "
              + "V3DirectoryAuthorities config option!");
          fingerprint = line.split(" ")[2];
          long nowConsensus = (now / (60L * 60L * 1000L))
              * (60L * 60L * 1000L);
          SimpleDateFormat consensusVoteFormat =
              new SimpleDateFormat("yyyy/MM/dd/yyyy-MM-dd-HH-mm-ss");
          consensusVoteFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
          if (!new File("directory-archive/vote/"
                + consensusVoteFormat.format(new Date(nowConsensus))
                + "-vote-" + fingerprint).exists()) {
            if (!this.missingDescriptors.contains("vote," + fingerprint
                + "," + parseFormat.format(new Date(nowConsensus)))) {
              this.logger.fine("Adding vote to missing list: fingerprint="
                  + fingerprint + ", valid-after="
                  + parseFormat.format(new Date(nowConsensus))
                  + ", filename=directory-archive/vote/"
                  + consensusVoteFormat.format(new Date(nowConsensus))
                  + "-vote-" + fingerprint);
              this.missingDescriptors.add("vote," + fingerprint + ","
                  + parseFormat.format(new Date(nowConsensus)));
              this.archiveWriterParseHistoryModified = true;
            }
          }
        } else if (line.startsWith("fingerprint ")) {
          fingerprint = line.split(" ")[1];
        } else if (line.startsWith("r ")) {
          String publishedTime = line.split(" ")[4] + " "
              + line.split(" ")[5];
          long published = parseFormat.parse(publishedTime).getTime();
          String digest = Hex.encodeHexString(Base64.decodeBase64(
              line.split(" ")[3] + "=")).toLowerCase();
          // TODO are 24 hours okay?
          if (published + 24L * 60L * 60L * 1000L > now &&
              !new File("directory-archive/server-descriptor/"
              + descriptorFormat.format(new Date(published))
              + digest.substring(0, 1) + "/" + digest.substring(1, 2)
              + "/" + digest).exists()) {
            if (!this.missingDescriptors.contains("server," + digest + ","
                + publishedTime)) {
              this.logger.fine("Adding server descriptor to missing list: "
                  + "digest=" + digest
                  + ", filename=directory-archive/server-descriptor/"
                  + descriptorFormat.format(new Date(published))
                  + digest.substring(0, 1) + "/" + digest.substring(1, 2)
                  + "/" + digest);
              this.missingDescriptors.add("server," + digest + ","
                  + publishedTime);
              this.archiveWriterParseHistoryModified = true;
            }
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
          BufferedReader br2 = new BufferedReader(new StringReader(
              sb.toString()));
          BufferedWriter bw = new BufferedWriter(new FileWriter(
              consensusFile));
          while ((line = br2.readLine()) != null) {
              bw.write(line + "\n");
          }
          bw.close();
          br2.close();
          this.logger.fine("Removing consensus from missing list: "
              + "valid-after=" + validAfterTime
              + ", filename=directory-archive/consensus/"
              + printFormat.format(new Date(validAfter)) + "-consensus");
          this.missingDescriptors.remove("consensus,NA,"
              + validAfterTime);
          this.archiveWriterParseHistoryModified = true;
        } else {
          this.logger.info("Not storing consensus, because we already "
              + "have it: valid-after=" + validAfterTime
              + ", filename=directory-archive/consensus/"
              + printFormat.format(new Date(validAfter)) + "-consensus");
        }
      } else {
        File voteFile = new File("directory-archive/vote/"
            + printFormat.format(new Date(validAfter)) + "-vote-"
            + fingerprint);
        if (!voteFile.exists()) {
          this.logger.fine("Storing vote: fingerprint=" + fingerprint
              + ", valid-after="
              + printFormat.format(new Date(validAfter))
              + ", filename=directory-archive/vote/"
              + printFormat.format(new Date(validAfter)) + "-vote-"
              + fingerprint);
          voteFile.getParentFile().mkdirs();
          BufferedReader br2 = new BufferedReader(new StringReader(
              sb.toString()));
          BufferedWriter bw = new BufferedWriter(new FileWriter(
              voteFile));
          while ((line = br2.readLine()) != null) {
              bw.write(line + "\n");
          }
          bw.close();
          br2.close();
          this.logger.fine("Removing vote from missing list: "
              + "fingerprint=" + fingerprint + ", valid-after="
              + printFormat.format(new Date(validAfter))
              + ", filename=directory-archive/vote/"
              + printFormat.format(new Date(validAfter)) + "-vote-"
              + fingerprint);
          this.missingDescriptors.remove("vote," + fingerprint + ","
              + validAfterTime);
          this.archiveWriterParseHistoryModified = true;
        } else {
          this.logger.info("Not storing vote, because we already have "
              + "it: fingerprint=" + fingerprint + ", valid-after="
              + printFormat.format(new Date(validAfter))
              + ", filename=directory-archive/vote/"
              + printFormat.format(new Date(validAfter)) + "-vote-"
              + fingerprint);
        }
      }
    } else if (line.startsWith("router ") ||
        line.startsWith("extra-info ")) {
      boolean isServerDescriptor = line.startsWith("router ");
      String publishedTime = null;
      long published = -1L;
      String digest = null;
      while ((line = br.readLine()) != null) {
        sb.append(line + "\n");
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
          if (!new File("directory-archive/extra-info/"
              + descriptorFormat.format(new Date(published))
              + extraInfoDigest.substring(0, 1) + "/"
              + extraInfoDigest.substring(1, 2) + "/"
              + extraInfoDigest).exists()) {
            if (!this.missingDescriptors.contains("extra,"
                + extraInfoDigest + "," + publishedTime)) {
              this.logger.fine("Adding extra-info descriptor to missing "
                  + "list: digest=" + extraInfoDigest
                  + ", filename=directory-archive/extra-info/"
                  + descriptorFormat.format(new Date(published))
                  + extraInfoDigest.substring(0, 1) + "/"
                  + extraInfoDigest.substring(1, 2) + "/"
                  + extraInfoDigest);
              this.missingDescriptors.add("extra," + extraInfoDigest + ","
                  + publishedTime);
              this.archiveWriterParseHistoryModified = true;
            }
          }
        } else if (line.equals("router-signature")) {
          digest = DigestUtils.shaHex(sb.toString()).toLowerCase();
        }
      }
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
        BufferedReader br2 = new BufferedReader(new StringReader(
            sb.toString()));
        BufferedWriter bw = new BufferedWriter(new FileWriter(
            descriptorFile));
        while ((line = br2.readLine()) != null) {
          bw.write(line + "\n");
        }
        bw.close();
        br2.close();
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
        this.archiveWriterParseHistoryModified = true;
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
    if (this.archiveWriterParseHistoryModified) {
      try {
        this.logger.info("Writing file " + this.statsDir
            + "/archive-writer-parse-history...");
        new File(this.statsDir).mkdirs();
        BufferedWriter bw = new BufferedWriter(new FileWriter(
            this.archiveWriterParseHistory));
        bw.write("type,source,published\n");
        for (String line : this.missingDescriptors) {
          bw.write(line + "\n");
        }
        bw.close();
        this.logger.info("Finished writing file " + this.statsDir
            + "/archive-writer-parse-history.");
      } catch (IOException e) {
        this.logger.log(Level.WARNING, "Failed writing " + this.statsDir
            + "/archive-writer-parse-history!", e);
      }
    }
  }
}

