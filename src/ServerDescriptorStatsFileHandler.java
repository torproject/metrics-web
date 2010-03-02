import java.io.*;
import java.text.*;
import java.util.*;
import java.util.logging.*;

  /**
   * two pieces of information: consensuses referencing N server
   * descriptors that are combined with relay flags (like Running) and
   * server descriptors containing information about tor
   * versions, platforms, and advertised bandwidth. we want stats that
   * combine information from consensuses and server descriptors. in
   * databases this is a n:m relation with n consensus referencing m
   * server descriptors. so, the straightforward way is to keep parse
   * results in 2 tables and join them for extracting statistics.
   * however, we don't want to use a database here. and even if we had
   * a database, the table join would be too expensive to perform after
   * adding new data every hour.
   *
   * the approach we take here is to de-normalize the data and write
   * the join of consensuses and server descriptors into one file that
   * is never kept in memory in the whole. this file has entries for
   * every consensus line referencing a server descriptor and the
   * information we want to use from the references server descriptor,
   * if available. in addition to that, we need a smaller file containing
   * unreferenced server descriptors that we were not able to write to
   * the first file, yet. by implementing the join operation manually,
   * we can make use of the fact that descriptors are not referenced for
   * longer than 24 hours.
   *
   * stats/relay-version-stats:
   * date,v011,v012,v020,v021,v022,other
   *
   * stats/relay-platform-stats:
   * date,windows,sunos,openbsd,netbsd,linux,freebsd,dragonfly,darwin,other
   *
   * stats/relay-bandwidth-stats:
   * date,q1,md,q3
   *
   * read largefile and merge our data in; also generate stats
   * datetime,descriptor,version,platform,advbw
   * 320095,aZ7mNo3lkjf2li34hlkvjsdru2,0.2.1,Darwin,1024
   *
   * TODO future extension: remove lines from server-descriptor-stats-raw
   * as soon as we have written a full day (all consensuses, all SDs).
   */
public class ServerDescriptorStatsFileHandler {

  private File consensusesFile;
  private File consensusesTempFile;
  private File descriptorsFile;
  private File descriptorsTempFile;
  private File versionStatsFile;
  private File platformStatsFile;
  private File bandwidthStatsFile;

  /**
   * map key "valid-after", map value "valid-after,descid,descid,descid.."
   */
  private SortedMap<String, String> consensuses;

  /**
   * map key "published,descid"
   * map value "published,descid,version,platform,bandwidth"
   */
  private SortedMap<String, String> descriptors;

  /**
   * map key "descid"
   * map value "published,descid,version,platform,bandwidth"
   */
  private SortedMap<String, String> descById;

  private Logger logger;

  /**
   * Initializes this class, including reading in results file
   * <code>stats/relay-version-stats</code> etc. Not that we don't read in
   * <code>stats/server-descriptors-raw</code>, because it can grow
   * really big!
   */
  public ServerDescriptorStatsFileHandler() {

    /* init files */
    this.versionStatsFile = new File("stats/version-stats");
    this.platformStatsFile = new File("stats/platform-stats");
    this.bandwidthStatsFile = new File("stats/bandwidth-stats");
    this.consensusesFile = new File("stats/consensuses-raw");
    this.consensusesTempFile = new File("stats/consensuses-raw.temp");
    this.descriptorsFile = new File("stats/descriptors-raw");
    this.descriptorsTempFile = new File("stats/descriptors-raw.temp");

    /* Initialize local data structures. */
    this.consensuses = new TreeMap<String, String>();
    this.descriptors = new TreeMap<String, String>();
    this.descById = new TreeMap<String, String>();

    /* Initialize logger. */
    this.logger =
        Logger.getLogger(ServerDescriptorStatsFileHandler.class.getName());
    this.logger.fine("Initialized.");
  }

  /* Just add to data structure. We cannot check whether we already got
   * it right now. The only thing we can check is whether we got this
   * consensus before in this run. */
  public void addConsensus(String validAfter,
      String descriptorIdentities) {
    // TODO should there be a modified flag, too?
    if (!this.consensuses.containsKey(validAfter)) {
      this.logger.finer("Adding");
    } else {
      this.logger.fine("We already learned about this consensus in this "
          + "run. Overwriting.");
    }
    this.consensuses.put(validAfter, validAfter + ","
        + descriptorIdentities);
    
    // force autosave if we have too many data; 240 cons ^= 10 days
    if (this.consensuses.size() > 240) {
      this.logger.fine("Autosave triggered by adding consensus: We have "
          + this.consensuses.size() + " consensuses and " + this.descriptors.size()
          + " descriptors in memory. Writing to disk now.");
      this.writeFiles();
    }
  }

  // version string is the 0.2.1.23 part of the platform string
  // platform is platform string with all parts after { removed
  // advbw is in kibibytes
  public void addServerDescriptor(String descriptorIdentity,
      String platformLine, String publishedLine, String bandwidthLine) {
    // TODO should there be a modified flag, too?
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
      advBw = "" + (Math.min(Long.parseLong(bwParts[1]),
          Long.parseLong(bwParts[3])) / 1024L);
      // TODO can't trust input! verify
    }
    String key = published + "," + descriptorIdentity;
    String line = key + "," + version + "," + platform + "," + advBw;
    if (!this.descriptors.containsKey(key)) {
      this.logger.finer("Adding");
    } else {
      this.logger.fine("We already learned about this server descriptor "
          + "in this run. Overwriting.");
    }
    this.descriptors.put(key, line);
    this.descById.put(descriptorIdentity, line);

    // force autosave if we have too many data; 50K descs ^= 10 days in early 2010
    if (this.descriptors.size() > 50000) {
      this.logger.fine("Autosave triggered by adding descriptor: We have "
          + this.consensuses.size() + " consensuses and " + this.descriptors.size()
          + " descriptors in memory. Writing to disk now.");
      this.writeFiles();
    }
  }

  /**
   * Writes the newly learned consensuses and server descriptors to disk
   * and merges new findings about relay versions, platforms, and advertised
   * bandwidth with existing stats files.
   */
  /* why is this so complex? because the data doesn't fit into memory and
   * we want to avoid going through the file more than once (that is,
   * once for reading and once for writing) if at all possible. */
  public void writeFiles() {

   // TODO use separate try blocks?
   try {
      /* Initialize readers and writers for the two files. We are going to
       * write to temporary files, delete originals, and rename. */
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

      this.consensusesTempFile.getParentFile().mkdirs();
      BufferedWriter consensusesWriter = new BufferedWriter(new FileWriter(
          this.consensusesTempFile));
      BufferedWriter descriptorsWriter = new BufferedWriter(new FileWriter(
          this.descriptorsTempFile));
      BufferedWriter versionWriter = new BufferedWriter(new FileWriter(
          this.versionStatsFile));
      BufferedWriter platformWriter = new BufferedWriter(new FileWriter(
          this.platformStatsFile));
      BufferedWriter bandwidthWriter = new BufferedWriter(new FileWriter(
          this.bandwidthStatsFile));

      SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
      dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
      SimpleDateFormat dateTimeFormat =
          new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      dateTimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

      String statsDate = null;
      // TODO make these configurable
      List<String> versionKeys = new ArrayList<String>(Arrays.asList(
          "0.1.1,0.1.2,0.2.0,0.2.1,0.2.2".split(",")));
      List<String> platformKeys = new ArrayList<String>(Arrays.asList(
          "Windows,SunOS,OpenBSD,NetBSD,Linux,FreeBSD,DragonFly,Darwin".
          split(",")));
      versionWriter.write("date");
      for (String v : versionKeys) {
        versionWriter.write("," + v);
      }
      versionWriter.write(",other\n");
      platformWriter.write("date");
      for (String p : platformKeys) {
        platformWriter.write("," + p.toLowerCase());
      }
      platformWriter.write(",other\n");
      bandwidthWriter.write("date,advbw\n");

      int[] versionStats = new int[versionKeys.size() + 1];
      int[] platformStats = new int[platformKeys.size() + 1];
      long bandwidthStats = 0L;
      int consensusesAtThisDay = 0;

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
         * and write the line to disk. Afterwards, line contains
         * the consensus line we want to parse in this iteration. */
        String line = null; // TODO rename
        if (consensusLine != null) {
          if (!this.consensuses.isEmpty()) {
            String fileVA = consensusLine.split(",")[0];
            String memVA = this.consensuses.firstKey();
            if (fileVA.equals(memVA)) {
              this.logger.finer("We have a consensus line in memory that "
                  + "we already knew before. Skipping.");
              // TODO should we compare the two lines here?
              consensusLine = consensusesReader.readLine();
              continue; // TODO is this correct?
            } else if (fileVA.compareTo(memVA) < 0) {
              line = consensusLine; // TODO rename
              consensusLine = consensusesReader.readLine();
            } else {
              line = this.consensuses.remove(memVA);
            }
          } else {
            line = consensusLine;
            consensusLine = consensusesReader.readLine();
          }
        } else {
          line = this.consensuses.remove(this.consensuses.firstKey());
        }
        consensusesWriter.write(line + "\n");

        /* Write all descriptor to disk that were published more than 24
         * hours before this consensus. */
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
              String filePubl = descriptorLine.substring(0, 47);
              // 47 chars: 19 for datetime, 1 for comma, 27 for descid
              String memPubl = this.descriptors.firstKey();
              if (filePubl.equals(memPubl)) {
                this.logger.finer("same desc. skipping.");
                descriptorLine = descriptorsReader.readLine();
                continue; // TODO is this correct?
              } else if (filePubl.compareTo(memPubl) < 0) {
                descriptorsWriter.write(descriptorLine + "\n");
                descriptorLine = descriptorsReader.readLine();
              } else {
                String removed = this.descriptors.remove(memPubl);
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

        /* Read in all descriptors that were published in the last 24
         * hours before the consensus that we're just parsing. */
        String validAfter = line.split(",")[0];
        while (descriptorsReader != null && descriptorLine != null &&
            descriptorLine.split(",")[0].compareTo(validAfter) < 0) {
          this.descriptors.put(descriptorLine.substring(0, 47),
              descriptorLine);
          this.descById.put(descriptorLine.split(",")[1], descriptorLine);
          descriptorLine = descriptorsReader.readLine();
        }

        /* Now we have a consensus line we want to parse and all possibly
         * referenced descriptors in descById (rename). Let's write some
         * stats. */
        String consensusDate = line.substring(0, 10);
        if (statsDate == null) {
          statsDate = consensusDate;
        }
        if (!statsDate.equals(consensusDate)) {
          /* If we have parsed at least half of the consensuses of a day,
           * Write stats to disk. */ // TODO document this somewhere
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
          versionStats = new int[versionKeys.size() + 1];
          platformStats = new int[platformKeys.size() + 1];
          bandwidthStats = 0L;
          consensusesAtThisDay = 0;
          // fill in NA's for missing dates
          long writtenMillis = dateFormat.parse(statsDate).getTime();
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
          
          statsDate = consensusDate;
        }

        /* Parse all descriptors that are referenced from this consensus.
         * only add values if we have 90+ % of all ref. descriptors!!
         * TODO document this somewhere! */
        int[] versionStatsCons = new int[versionKeys.size() + 1];
        int[] platformStatsCons = new int[platformKeys.size() + 1];
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
            if (versionKeys.contains(version)) {
              versionStatsCons[versionKeys.indexOf(version)]++;
            } else {
              versionStatsCons[versionStatsCons.length - 1]++;
            }
            String platform = parts[3].toLowerCase();
            boolean isOther = true;
            // TODO document that order of platform strings in config
            // matters! if there are two OS, "DragonFly" and "Dragon",
            // put "DragonFly" first! capitalization doesn't matter, but
            // is only relevant for stats file headers
            for (String p : platformKeys) {
              if (platform.contains(p.toLowerCase())) {
                platformStatsCons[platformKeys.indexOf(p)]++;
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
          this.logger.fine("not enough server descriptors for consensus, "
              + "less than 90%. not including in stats.");
        }

        /* We're done reading one consensus. */
      }

      /* Write remaining server descriptors to disk. */
      while (descriptorLine != null || !this.descriptors.isEmpty()) {
        if (descriptorLine != null) {
          if (!this.descriptors.isEmpty()) {
            String filePubl = descriptorLine.substring(0, 47);
            // 47 chars: 19 for datetime, 1 for comma, 27 for descid
            String memPubl = this.descriptors.firstKey();
            if (filePubl.equals(memPubl)) {
              this.logger.finer("same desc. skipping.");
              descriptorLine = descriptorsReader.readLine();
              continue; // TODO is this correct?
            } else if (filePubl.compareTo(memPubl) < 0) {
              descriptorsWriter.write(descriptorLine + "\n");
              descriptorLine = descriptorsReader.readLine();
            } else {
              descriptorsWriter.write(this.descriptors.remove(memPubl) + "\n");
            }
          } else {
            descriptorsWriter.write(descriptorLine + "\n");
            descriptorLine = descriptorsReader.readLine();
          }
        } else {
          descriptorsWriter.write(this.descriptors.remove(this.descriptors.firstKey())
              + "\n");
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
      if (this.consensusesFile.exists()) {
        this.consensusesFile.delete();
      }
      this.consensusesTempFile.renameTo(this.consensusesFile);
      if (this.descriptorsFile.exists()) {
        this.descriptorsFile.delete();
      }
      this.descriptorsTempFile.renameTo(this.descriptorsFile);

      /* Done. Whee! */
    } catch (Exception e) {
      this.logger.log(Level.WARNING, "Exception while writing files.", e);
    }
    this.logger.fine("Finished writing.");
  }
}

