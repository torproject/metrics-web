import java.io.*;
import java.text.*;
import java.util.*;
import java.util.logging.*;

/**
 * Generates statistics about relays in the Tor network from data that
 * relays write to their server descriptors. Accepts lists of referenced
 * descriptors in network status consensuses and selected lines from
 * server descriptors from <code>RelayDescriptorParser</code>. Keeps two
 * intermediate results files <code>stats/consensuses-raw</code> and
 * <code>stats/descriptors-raw</code> and writes three final results files
 * <code>stats/version-stats</code>, <code>stats/platform-stats</code>,
 * and <code>stats/bandwidth-stats</code>.
 */
public class ServerDescriptorStatsFileHandler {

  /**
   * Intermediate results file <code>stats/consensuses-raw</code>
   * containing consensuses and the referenced descriptor identities of
   * relays with the Running flag set. The file format is
   * "valid-after,descid,descid,descid...\n" for each consensus. Lines are
   * ordered by valid-after time in ascending order.
   */
  private File consensusesFile;

  /**
   * Temporary file for writing <code>stats/consensuses-raw</code> while
   * reading that file at the same time. After read and write operations
   * are complete, the original file is deleted and the temporary file
   * renamed to be the new intermediate results file.
   */
  private File consensusesTempFile;

  /**
   * Intermediate results file <code>stats/descriptors-raw</code>
   * containing server descriptors with relevant fields for statistics.
   * The file format is "published,descid,version,platform,advbw\n" for
   * each server descriptors. Lines are first ordered by published time,
   * then by descid.
   */
  private File descriptorsFile;

  /**
   * Temporary file for writing <code>stats/descriptors-raw</code> while
   * reading that file at the same time. After read and write operations
   * are complete, the original file is deleted and the temporary file
   * renamed to be the new intermediate results file.
   */
  private File descriptorsTempFile;

  /**
   * Final results file <code>stats/version-stats</code> containing
   * statistics about Tor versions of relays in the network. The file
   * format is "date,version1,version2,...,other" with versions as
   * specified in config option RelayVersions.
   */
  private File versionStatsFile;

  /**
   * Final results file <code>stats/platform-stats</code> containing
   * statistics about operating systems of relays in the network. The
   * file format is "date,os1,os2,...,other" with operating systems as
   * specified in config option RelayPlatforms.
   */
  private File platformStatsFile;

  /**
   * Final results file <code>stats/bandwidth-stats</code> containing
   * statistics about the advertised bandwidth of relays in the network.
   * The file format is "date,advbw".
   */
  private File bandwidthStatsFile;

  /**
   * Consensuses and referenced descriptor identities of relays with the
   * Running flag set. This data structure only holds those consensuses
   * that were parsed in this execution, not the previously parsed
   * consensuses as read from disk. Map keys are valid-after times
   * formatted as "yyyy-MM-dd HH:mm:ss", map values are valid-after times
   * followed by a comma-separated list of base-64-formatted descriptor
   * identifiers.
   */
  private SortedMap<String, String> consensuses;

  /**
   * Server descriptors with relevant fields for statistics, ordered by
   * published time and descriptor identifier. Map keys are publication
   * times of descriptors formatted as "yyyy-MM-dd HH:mm:ss", a comma, and
   * base-64-formatted descriptor identifiers. An example key is
   * "2009-09-30 20:42:19,ZQZ5zq4q1U8Uynyk6lkUy5uAsdM" (length 47). Map
   * values are map keys plus version, platform, and advertised bandwidth
   * written as "published,descid,version,platform,advbw". Note that the
   * platform string may contain commas.
   */
  private SortedMap<String, String> descriptors;

  /**
   * Server descriptors as in <code>descriptors</code>, accessible by
   * descriptor identifiers only, without knowing the publication time.
   * Map keys are base-64-formatted descriptor identifiers, map values
   * are formatted as map values in <code>descriptors</code>.
   */
  private SortedMap<String, String> descById;

  /**
   * Tor relay versions that we care about.
   */
  private List<String> relayVersions;

  /**
   * Platforms (operating systems) that we care about.
   */
  private List<String> relayPlatforms;

  /**
   * Logger for this class.
   */
  private Logger logger;

  // TODO should there be a modified flag, too?

  /**
   * Initializes this class, without reading in any files. We're only
   * reading in files when writing results to disk in
   * <code>writeFiles</code>.
   */
  public ServerDescriptorStatsFileHandler(List<String> relayVersions,
      List<String> relayPlatforms) {

    /* Memorize versions and platforms that we care about. */
    this.relayVersions = relayVersions;
    this.relayPlatforms = relayPlatforms;

    /* Initialize local data structures. */
    this.consensuses = new TreeMap<String, String>();
    this.descriptors = new TreeMap<String, String>();
    this.descById = new TreeMap<String, String>();

    /* Initialize file names for intermediate and final results files. */
    this.versionStatsFile = new File("stats/version-stats");
    this.platformStatsFile = new File("stats/platform-stats");
    this.bandwidthStatsFile = new File("stats/bandwidth-stats");
    this.consensusesFile = new File("stats/consensuses-raw");
    this.consensusesTempFile = new File("stats/consensuses-raw.temp");
    this.descriptorsFile = new File("stats/descriptors-raw");
    this.descriptorsTempFile = new File("stats/descriptors-raw.temp");

    /* Initialize logger. */
    this.logger =
        Logger.getLogger(ServerDescriptorStatsFileHandler.class.getName());
  }

  /**
   * Adds a consensus to the list with its valid-after time and a list of
   * descriptor identifiers of relays that have the Running flag set. If
   * the number of consensuses in memory exceeds a certain number, an
   * auto-save mechanism is triggered by calling <code>writeFiles</code>.
   */
  public void addConsensus(String validAfter,
      String descriptorIdentities) {

    /* Add consensus to the list. */
    if (!this.consensuses.containsKey(validAfter)) {
      this.logger.finer("Adding consensus published at " + validAfter
          + ".");
    } else {
      this.logger.fine("We already learned about a consensus published "
          + "at " + validAfter + " in this execution. Overwriting.");
    }
    this.consensuses.put(validAfter, validAfter + ","
        + descriptorIdentities);

    /* Check if we have more 240 consensuses in memory (covering 10 days).
     * If so, trigger the auto-save mechanism. */
    if (this.consensuses.size() > 240) {
      this.logger.fine("Autosave triggered by adding consensus: We have "
          + this.consensuses.size() + " consensuses and "
          + this.descriptors.size() + " descriptors in memory. Writing "
          + "to disk now.");
      this.writeFiles();
    }
  }

  /**
   * Adds a server descriptor to the list with its identity and the
   * platform, published, and bandwidth lines. Version and operating
   * system are parsed from the platform line. The parsed version consists
   * only of the dotted numbers part (e.g. "0.2.1.2") without any
   * additions like "-alpha". The operating system is the substring after
   * " on " up to the first encountered opening curly bracket ("{").
   * The publication time is extracted from the published line. The
   * advertised bandwidth is calculated from the bandwidth line by taking
   * the minimum of average and observed bandwidth, divided by 1024 to
   * obtain KiB/s.
   */
  public void addServerDescriptor(String descriptorIdentity,
      String platformLine, String publishedLine, String bandwidthLine) {

    /* Parse version, platform, and advertised bandwidth from the given
     * lines. */
    String version = "", platform = "", published = "", advBw = "";
    if (platformLine.contains(" Tor ")) {
      version = platformLine.substring(platformLine.indexOf(" Tor ") + 5).
        split(" ")[0];
    }
    if (platformLine.contains(" on ")) {
      platform = platformLine.substring(platformLine.indexOf(" on ") + 4);
      if (platform.contains("{")) {
        platform = platform.substring(0, platform.indexOf("{")).trim();
      }
    }
    published = publishedLine.substring("published ".length());
    String[] bwParts = bandwidthLine.split(" ");
    if (bwParts.length == 4) {
      try {
        advBw = "" + (Math.min(Long.parseLong(bwParts[1]),
            Long.parseLong(bwParts[3])) / 1024L);
      } catch (NumberFormatException e) {
        this.logger.log(Level.WARNING, "Exception while parsing average "
            + "and observed bandwidth from line '" + bandwidthLine
            + "'. Not adding server descriptor!", e);
        return;
      }
    }
    String key = published + "," + descriptorIdentity;
    String line = key + "," + version + "," + platform + "," + advBw;
    if (!this.descriptors.containsKey(key)) {
      this.logger.finer("Adding server descriptor with identifier "
          + descriptorIdentity + ".");
    } else {
      this.logger.fine("We already learned about a server descriptor "
          + "with identifier " + descriptorIdentity + ", published at "
          + published + " in this execution. Overwriting.");
    }
    this.descriptors.put(key, line);
    this.descById.put(descriptorIdentity, line);

    /* Check if we have more 50K server descriptors in memory (covering 10
     * days as of early 2010). If so, trigger the auto-save mechanism. */
    if (this.descriptors.size() > 50000) {
      this.logger.fine("Autosave triggered by adding server descriptor: "
          + "We have " + this.consensuses.size() + " consensuses and "
          + this.descriptors.size() + " descriptors in memory. Writing "
          + "to disk now.");
      this.writeFiles();
    }
  }

  /**
   * Merges the newly learned consensuses and server descriptors with the
   * ones we wrote to disk earlier and extracts new statistics about relay
   * version, platforms, and advertised bandwidth.
   *
   * This method is rather complex, because we can only store a limited
   * number of consensuses and serer descriptors in memory. Also, we want
   * to avoid going through the files twice, once for merging old and new
   * lines and another time for extracting statistics.
   */
  public void writeFiles() {

   try {

      /* Initialize readers for reading intermediate results files from
       * disk. */
      BufferedReader consensusesReader = null;
      if (this.consensusesFile.exists()) {
        consensusesReader = new BufferedReader(new FileReader(
            this.consensusesFile));
      }
      BufferedReader descriptorsReader = null;
      if (this.descriptorsFile.exists()) {
        descriptorsReader = new BufferedReader(new FileReader(
          this.descriptorsFile));
      }

      /* Prepare writing intermediate results. The idea is to write to
       * temporary files while reading from the originals, delete the
       * originals, and rename the temporary files to be the new
       * originals. */
      this.consensusesTempFile.getParentFile().mkdirs();
      BufferedWriter consensusesWriter = new BufferedWriter(
          new FileWriter(this.consensusesTempFile));
      BufferedWriter descriptorsWriter = new BufferedWriter(
          new FileWriter(this.descriptorsTempFile));

      /* Prepare date format parsers. */
      SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
      dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
      SimpleDateFormat dateTimeFormat =
          new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      dateTimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

      /* Prepare extracting statistics and writing them to disk. */
      String statsDate = null;
      int[] versionStats = new int[this.relayVersions.size() + 1];
      int[] platformStats = new int[this.relayPlatforms.size() + 1];
      long bandwidthStats = 0L;
      int consensusesAtThisDay = 0;
      BufferedWriter versionWriter = new BufferedWriter(new FileWriter(
          this.versionStatsFile));
      BufferedWriter platformWriter = new BufferedWriter(new FileWriter(
          this.platformStatsFile));
      BufferedWriter bandwidthWriter = new BufferedWriter(new FileWriter(
          this.bandwidthStatsFile));
      versionWriter.write("date");
      for (String v : this.relayVersions) {
        versionWriter.write("," + v);
      }
      versionWriter.write(",other\n");
      platformWriter.write("date");
      for (String p : this.relayPlatforms) {
        platformWriter.write("," + p);
      }
      platformWriter.write(",other\n");
      bandwidthWriter.write("date,advbw\n");

      /* Always keep one line of the consensuses and descriptors file in
       * memory. */
      String consensusLine = consensusesReader != null ?
          consensusesReader.readLine() : null;
      String descriptorLine = descriptorsReader != null ?
          descriptorsReader.readLine() : null;

      /* Iterate over both the consensus file and the consensus strings
       * that we have in memory at the same time. Whichever has an earlier
       * valid-after time gets processed. */
      while (consensusLine != null || !this.consensuses.isEmpty()) {

        /* Find out which line we want to process now, memorize it for
         * parsing below, advance the source from where we got the line,
         * and write the line to disk. Afterwards, variable line contains
         * the consensus line we want to parse in this iteration. */
        String line = null;
        if (consensusLine != null) {
          if (!this.consensuses.isEmpty()) {
            String fileKey = consensusLine.split(",")[0];
            String memKey = this.consensuses.firstKey();
            if (fileKey.equals(memKey)) {
              this.logger.finer("The consensus we read from disk has the "
                  + "same valid-after time (" + fileKey + ") time as a "
                  + "consensus we have in memory. Using the consensus "
                  + "from memory.");
              consensusLine = consensusesReader.readLine();
              continue;
            } else if (fileKey.compareTo(memKey) < 0) {
              line = consensusLine;
              consensusLine = consensusesReader.readLine();
            } else {
              line = this.consensuses.remove(memKey);
            }
          } else {
            line = consensusLine;
            consensusLine = consensusesReader.readLine();
          }
        } else {
          line = this.consensuses.remove(this.consensuses.firstKey());
        }
        consensusesWriter.write(line + "\n");

        /* Write all server descriptors to disk that were published more
         * than 24 hours before the consensus we're about to process. Also
         * remove those server descriptors from memory. The idea is that
         * those server descriptors cannot be referenced from the
         * consensus anyway and would only bloat our memory. */
        String minus24h = dateTimeFormat.format(new Date(
            dateTimeFormat.parse(line.split(",")[0]).getTime() -
            (24L * 60L * 60L * 1000L)));
        while ((descriptorLine != null &&
            descriptorLine.split(",")[0].compareTo(minus24h) < 0) ||
            (!this.descriptors.isEmpty() &&
            this.descriptors.firstKey().split(",")[0].
              compareTo(minus24h) < 0)) {
          if (descriptorLine != null) {
            if (!this.descriptors.isEmpty()) {
              /* The first 47 chars contain the publication time (19
               * chars), a comma (1 char), and the descriptor identifier
               * (27 chars). */
              String fileKey = descriptorLine.substring(0, 47);
              String memKey = this.descriptors.firstKey();
              if (fileKey.equals(memKey)) {
                this.logger.finer("The server descriptor we read from "
                    + "disk has the same publication time and identifier "
                    + "(" + fileKey + ") as a server descriptor we have "
                    + "in memory. Using the server descriptor from "
                    + "memory.");
                descriptorLine = descriptorsReader.readLine();
                continue;
              } else if (fileKey.compareTo(memKey) < 0) {
                descriptorsWriter.write(descriptorLine + "\n");
                descriptorLine = descriptorsReader.readLine();
              } else {
                String removed = this.descriptors.remove(memKey);
                this.descById.remove(removed.split(",")[1]);
                descriptorsWriter.write(removed + "\n");
              }
            } else {
              descriptorsWriter.write(descriptorLine + "\n");
              descriptorLine = descriptorsReader.readLine();
            }
          } else {
            String removed = this.descriptors.remove(
                this.descriptors.firstKey());
            this.descById.remove(removed.split(",")[1]);
            descriptorsWriter.write(removed + "\n");
          }
        }

        /* Read in all server descriptors that were published in the last
         * 24 hours before the consensus that we're just processing. These
         * server descriptors might be referenced from the consensus.
         * Store references to these server descriptors by identifier to
         * facilitate matching a consensus entry with the corresponding
         * server descriptor. */
        String validAfter = line.split(",")[0];
        while (descriptorsReader != null && descriptorLine != null &&
            descriptorLine.split(",")[0].compareTo(validAfter) < 0) {
          this.descriptors.put(descriptorLine.substring(0, 47),
              descriptorLine);
          this.descById.put(descriptorLine.split(",")[1], descriptorLine);
          descriptorLine = descriptorsReader.readLine();
        }

        /* Now we have a consensus line we want to parse and all possibly
         * referenced descriptors in descById. Let's write some stats. */
        String consensusDate = line.substring(0, 10);
        if (statsDate == null) {
          statsDate = consensusDate;
        }
        if (!statsDate.equals(consensusDate)) {
          /* We have finished one day of consensuses. If we have parsed at
           * least half of the possible 24 consensuses of that day, write
           * stats to disk. */
          if (consensusesAtThisDay >= 12) {
            versionWriter.write(statsDate);
            for (int i = 0; i < versionStats.length; i++) {
              versionWriter.write("," + (versionStats[i] /
                  consensusesAtThisDay));
            }
            versionWriter.write("\n");
            platformWriter.write(statsDate);
            for (int i = 0; i < platformStats.length; i++) {
              platformWriter.write("," + (platformStats[i] /
                  consensusesAtThisDay));
            }
            platformWriter.write("\n");
            bandwidthWriter.write(statsDate + ","
                + (bandwidthStats / consensusesAtThisDay) + "\n");
          } else {
            this.logger.fine("Not enough consensuses to write to stats.");
          }
          /* Fill in NA's for missing dates. */
          long writtenMillis = dateFormat.parse(statsDate).getTime();
          if (consensusesAtThisDay < 12) {
            writtenMillis -= 24L * 60L * 60L * 1000L;
          }
          long nextMillis = dateFormat.parse(consensusDate).getTime();
          while (writtenMillis + (24L * 60L * 60L * 1000L) < nextMillis) {
            writtenMillis += 24L * 60L * 60L * 1000L;
            String date = dateFormat.format(new Date(writtenMillis));
            versionWriter.write(date);
            for (int i = 0; i < versionStats.length; i++) {
              versionWriter.write(",NA");
            }
            versionWriter.write(",NA\n");
            platformWriter.write(date);
            for (int i = 0; i < platformStats.length; i++) {
              platformWriter.write(",NA");
            }
            platformWriter.write(",NA\n");
            bandwidthWriter.write(date + ",NA\n");
          }
          /* Clear counters to collect next day's statistics. */
          versionStats = new int[this.relayVersions.size() + 1];
          platformStats = new int[this.relayPlatforms.size() + 1];
          bandwidthStats = 0L;
          consensusesAtThisDay = 0;
          statsDate = consensusDate;
        }

        /* For the given consensus, parse all referenced server
         * descriptors to obtain statistics on versions, platforms, and
         * advertised bandwidth. Only include these values if we have at
         * least 90 % of all referenced server descriptors. */
        int[] versionStatsCons = new int[this.relayVersions.size() + 1];
        int[] platformStatsCons = new int[this.relayPlatforms.size() + 1];
        long bandwidthStatsCons = 0L;
        String[] ids = line.split(",");
        int seenDescs = 0;
        for (int i = 1; i < ids.length; i++) {
          if (this.descById.containsKey(ids[i])) {
            seenDescs++;
            String desc = this.descById.get(ids[i]);
            String[] parts = desc.split(",");
            String version = parts[2].substring(0,
                parts[2].lastIndexOf("."));
            if (this.relayVersions.contains(version)) {
              versionStatsCons[this.relayVersions.indexOf(version)]++;
            } else {
              versionStatsCons[versionStatsCons.length - 1]++;
            }
            String platform = parts[3].toLowerCase();
            boolean isOther = true;
            for (String p : this.relayPlatforms) {
              if (platform.contains(p.toLowerCase())) {
                platformStatsCons[this.relayPlatforms.indexOf(p)]++;
                isOther = false;
                break;
              }
            }
            if (isOther) {
              platformStatsCons[platformStatsCons.length - 1]++;
            }
            bandwidthStatsCons += Long.parseLong(desc.substring(
                desc.lastIndexOf(",") + 1));
          }
        }
        if (10 * seenDescs / (ids.length - 1) >= 9) {
          for (int i = 0; i < versionStatsCons.length; i++) {
            versionStats[i] += versionStatsCons[i];
          }
          for (int i = 0; i < platformStatsCons.length; i++) {
            platformStats[i] += platformStatsCons[i];
          }
          bandwidthStats += bandwidthStatsCons;
          consensusesAtThisDay++;
        } else {
          this.logger.fine("Not enough referenced server descriptors for "
              + "consensus with valid-after time " + line.substring(0, 19)
              + ". Not including this consensus in the statistics.");
        }

        /* We're done reading one consensus. */
      }

      /* We're done reading all consensuses, both from disk and from
       * memory. Write remaining server descriptors to disk. These are the
       * server descriptors that were published 24 hours before the last
       * parsed consensus and those server descriptors published
       * afterwards. */
      while (descriptorLine != null || !this.descriptors.isEmpty()) {
        if (descriptorLine != null) {
          if (!this.descriptors.isEmpty()) {
            String fileKey = descriptorLine.substring(0, 47);
            String memKey = this.descriptors.firstKey();
            if (fileKey.equals(memKey)) {
              this.logger.finer("The server descriptor we read from "
                    + "disk has the same publication time and identifier "
                    + "(" + fileKey + ") as a server descriptor we have "
                    + "in memory. Using the server descriptor from "
                    + "memory.");
              descriptorLine = descriptorsReader.readLine();
              continue;
            } else if (fileKey.compareTo(memKey) < 0) {
              descriptorsWriter.write(descriptorLine + "\n");
              descriptorLine = descriptorsReader.readLine();
            } else {
              descriptorsWriter.write(this.descriptors.remove(memKey)
                  + "\n");
            }
          } else {
            descriptorsWriter.write(descriptorLine + "\n");
            descriptorLine = descriptorsReader.readLine();
          }
        } else {
          descriptorsWriter.write(this.descriptors.remove(
              this.descriptors.firstKey()) + "\n");
        }
      }
      this.descById.clear();

      /* Close the files that we read from and wrote to. */
      if (consensusesReader != null) {
        consensusesReader.close();
      }
      if (descriptorsReader != null) {
        descriptorsReader.close();
      }
      consensusesWriter.close();
      descriptorsWriter.close();
      bandwidthWriter.close();
      versionWriter.close();
      platformWriter.close();

      /* Delete original files and rename temporary files to be the new
       * originals. */
      if (this.consensusesFile.exists()) {
        this.consensusesFile.delete();
      }
      this.consensusesTempFile.renameTo(this.consensusesFile);
      if (this.descriptorsFile.exists()) {
        this.descriptorsFile.delete();
      }
      this.descriptorsTempFile.renameTo(this.descriptorsFile);

      /* Done. Whee! */
      this.logger.fine("Finished writing.");

    } catch (Exception e) {
      this.logger.log(Level.WARNING, "Exception while writing files.", e);
    }
  }
}
