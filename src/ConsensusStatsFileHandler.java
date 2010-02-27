import java.io.*;
import java.text.*;
import java.util.*;
import java.util.logging.*;

/**
 * Generates statistics on the average number of relays and bridges per
 * day. Accepts parse results from <code>RelayDescriptorParser</code> and
 * <code>BridgeDescriptorParser</code> and stores them in intermediate
 * result files <code>stats/consensus-stats-raw</code> and
 * <code>stats/bridge-consensus-stats-raw</code>. Writes final results to
 * <code>stats/consensus-stats</code> for all days for which at least half
 * of the expected consensuses or statuses are known.
 */
public class ConsensusStatsFileHandler {

  /**
   * Intermediate results file holding the number of relays with Exit,
   * Fast, Guard, Running, and Stable flags per consensus.
   */
  private File consensusStatsRawFile;

  /**
   * Number of relays in a given consensus with Exit, Fast, Guard,
   * Running, and Stable flags set. Map keys are consensus valid-after
   * times formatted as "yyyy-MM-dd HH:mm:ss", map values are lines as
   * read from <code>stats/consensus-stats-raw</code>.
   */
  private SortedMap<String, String> relaysRaw;

  /**
   * Modification flag for <code>relaysRaw</code>. This flag is used to
   * decide whether the contents of <code>relaysRaw</code> need to be
   * written to disk during <code>writeFiles</code>.
   */
  private boolean relaysRawModified;

  /**
   * Intermediate results file holding the number of running bridges per
   * bridge status.
   */
  private File bridgeConsensusStatsRawFile;

  /**
   * Number of running bridges in a given bridge status. Map keys are
   * bridge status times formatted as "yyyy-MM-dd HH:mm:ss", map values
   * are lines as read from <code>stats/bridge-consensus-stats-raw</code>.
   */
  private SortedMap<String, String> bridgesRaw;

  /**
   * Modification flag for <code>bridgesRaw</code>. This flag is used to
   * decide whether the contents of <code>bridgesRaw</code> need to be
   * written to disk during <code>writeFiles</code>.
   */
  private boolean bridgesRawModified;

  /**
   * Final results file holding the average number of relays with Exit,
   * Fast, Guard, Running, and Stable flags set and the number of running
   * bridges per day.
   */
  private File consensusStatsFile;

  /**
   * Average number of relays with Exit, Fast, Guard, Running, and Stable
   * flags set per day. Map keys are dates formatted as "yyyy-MM-dd", map
   * values are lines as written to <code>stats/consensus-stats</code>
   * without the last column that contains the number of running bridges.
   */
  private SortedMap<String, String> relaysPerDay;

  /**
   * Average number of running bridges per day. Map keys are dates
   * formatted as "yyyy-MM-dd", map values are the last column as written
   * to <code>stats/consensus-stats</code>.
   */
  private SortedMap<String, String> bridgesPerDay;

  /**
   * Logger for this class.
   */
  private Logger logger;

 /**
  * Initializes <code>ConsensusStatsFileHandler</code>, including reading
  * in intermediate results files <code>stats/consensus-stats-raw</code>
  * and <code>stats/bridge-consensus-stats-raw</code> and final results
  * file <code>stats/consensus-stats</code>.
  */
  public ConsensusStatsFileHandler() {

    /* Initialize local data structures to hold intermediate and final
     * results. */
    this.relaysPerDay = new TreeMap<String, String>();
    this.bridgesPerDay = new TreeMap<String, String>();
    this.relaysRaw = new TreeMap<String, String>();
    this.bridgesRaw = new TreeMap<String, String>();

    /* Initialize file names for intermediate and final results files. */
    this.consensusStatsRawFile = new File("stats/consensus-stats-raw");
    this.bridgeConsensusStatsRawFile = new File(
        "stats/bridge-consensus-stats-raw");
    this.consensusStatsFile = new File("stats/consensus-stats");

    /* Initialize logger. */
    this.logger = Logger.getLogger(
        ConsensusStatsFileHandler.class.getName());

    /* Read in number of relays with flags set per consensus. */
    if (this.consensusStatsRawFile.exists()) {
      try {
        this.logger.info("Reading file "
            + this.consensusStatsRawFile.getAbsolutePath() + "...");
        BufferedReader br = new BufferedReader(new FileReader(
            this.consensusStatsRawFile));
        String line = null;
        while ((line = br.readLine()) != null) {
          if (line.startsWith("#") || line.startsWith("date")) {
            continue;
          }
          String[] parts = line.split(",");
          if (parts.length != 6) {
            this.logger.warning("Corrupt line '" + line + "' in file "
                + this.consensusStatsRawFile.getAbsolutePath()
                + "! Aborting to read this file!");
            break;
          }
          String dateTime = parts[0];
          this.relaysRaw.put(dateTime, line);
        }
        br.close();
        this.logger.info("Finished reading file "
            + this.consensusStatsRawFile.getAbsolutePath() + ".");
      } catch (IOException e) {
        this.logger.log(Level.WARNING, "Failed to read file "
            + this.consensusStatsRawFile.getAbsolutePath() + "!", e);
      }
    }

    /* Read in number of running bridges per bridge status. */
    if (this.bridgeConsensusStatsRawFile.exists()) {
      try {
        this.logger.info("Reading file "
            + this.bridgeConsensusStatsRawFile.getAbsolutePath() + "...");
        BufferedReader br = new BufferedReader(new FileReader(
            this.bridgeConsensusStatsRawFile));
        String line = null;
        while ((line = br.readLine()) != null) {
          if (line.startsWith("#") || line.startsWith("date")) {
            continue;
          }
          String[] parts = line.split(",");
          if (parts.length != 2) {
            this.logger.warning("Corrupt line '" + line + "' in file "
                + this.bridgeConsensusStatsRawFile.getAbsolutePath()
                + "! Aborting to read this file!");
            break;
          }
          String dateTime = parts[0];
          this.bridgesRaw.put(dateTime, line);
        }
        br.close();
        this.logger.info("Finished reading file "
            + this.bridgeConsensusStatsRawFile.getAbsolutePath() + ".");
      } catch (IOException e) {
        this.logger.log(Level.WARNING, "Failed to read file "
            + this.bridgeConsensusStatsRawFile.getAbsolutePath() + "!",
            e);
      }
    }

    /* Read in previous results on average numbers of relays and running
     * bridges per day. */
    if (this.consensusStatsFile.exists()) {
      try {
        this.logger.info("Reading file "
            + this.consensusStatsFile.getAbsolutePath() + "...");
        BufferedReader br = new BufferedReader(new FileReader(
            this.consensusStatsFile));
        String line = null;
        while ((line = br.readLine()) != null) {
          if (line.startsWith("#") || line.startsWith("date")) {
            continue;
          }
          String[] parts = line.split(",");
          if (parts.length != 7) {
            this.logger.warning("Corrupt line '" + line + "' in file "
                + this.consensusStatsFile.getAbsolutePath()
                + "! Aborting to read this file!");
            break;
          }
          String date = parts[0];
          /* Split line into relay and bridge part; the relay part ends
           * with the last comma (excluding) and the bridge part starts at
           * that comma (including). */
          String relayPart = line.substring(0, line.lastIndexOf(","));
          String bridgePart = line.substring(line.lastIndexOf(","));
          if (!relayPart.endsWith(",NA,NA,NA,NA,NA")) {
            this.relaysPerDay.put(date, relayPart);
          }
          if (!bridgePart.equals(",NA")) {
            this.bridgesPerDay.put(date, bridgePart);
          }
        }
        br.close();
        this.logger.info("Finished reading file "
            + this.consensusStatsFile.getAbsolutePath() + ".");
      } catch (IOException e) {
        this.logger.log(Level.WARNING, "Failed to write file "
            + this.consensusStatsFile.getAbsolutePath() + "!", e);
      }
    }

    /* Set modification flags to false. */
    this.relaysRawModified = this.bridgesRawModified = false;
  }

  /**
   * Adds the intermediate results of the number of relays with certain
   * flags in a given consensus to the existing observations.
   */
  public void addConsensusResults(String validAfter, int exit, int fast,
      int guard, int running, int stable) throws IOException {
    String line = validAfter + "," + exit + "," + fast + "," + guard + ","
        + running + "," + stable;
    if (!this.relaysRaw.containsKey(validAfter)) {
      this.logger.fine("Adding new relay numbers: " + line);
      this.relaysRaw.put(validAfter, line);
      this.relaysRawModified = true;
    } else if (!line.equals(this.relaysRaw.get(validAfter))) {
      this.logger.warning("The numbers of relays with Exit, Fast, "
        + "Guard, Running, and Stable flag we were just given (" + line
        + ") are different from what we learned before ("
        + this.relaysRaw.get(validAfter) + ")! Overwriting!");
      this.relaysRaw.put(validAfter, line);
      this.relaysRawModified = true;
    }
  }

  /**
   * Adds the intermediate results of the number of running bridges in a
   * given bridge status to the existing observations.
   */
  public void addBridgeConsensusResults(String published, int running)
      throws IOException {
    String line = published + "," + running;
    if (!this.bridgesRaw.containsKey(published)) {
      this.logger.fine("Adding new bridge numbers: " + line);
      this.bridgesRaw.put(published, line);
      this.bridgesRawModified = true;
    } else if (!line.equals(this.bridgesRaw.get(published))) {
      this.logger.warning("The numbers of running bridges we were just "
        + "given (" + line + ") are different from what we learned "
        + "before (" + this.bridgesRaw.get(published) + ")! "
        + "Overwriting!");
      this.bridgesRaw.put(published, line);
      this.bridgesRawModified = true;
    }
  }

  /**
   * Aggregates the raw observations on relay and bridge numbers and
   * writes both raw and aggregate observations to disk.
   */
  public void writeFiles() {

    /* Did we learn anything new about average relay or bridge numbers in
     * this run? */
    boolean writeConsensusStats = false;

    /* Go through raw observations of numbers of relays in consensuses,
     * calculate averages per day, and add these averages to final
     * results. */
    if (!this.relaysRaw.isEmpty()) {
      String tempDate = null;
      int exit = 0, fast = 0, guard = 0, running = 0, stable = 0,
          consensuses = 0;
      Iterator<String> it = this.relaysRaw.values().iterator();
      boolean haveWrittenFinalLine = false;
      while (it.hasNext() || !haveWrittenFinalLine) {
        String next = it.hasNext() ? it.next() : null;
        /* Finished reading a day or even all lines? */
        if (tempDate != null && (next == null
            || !next.substring(0, 10).equals(tempDate))) {
          /* Only write results if we have seen at least half of all
           * consensuses. */
          if (consensuses >= 12) {
            String line = tempDate + "," + (exit / consensuses) + ","
                + (fast/ consensuses) + "," + (guard/ consensuses) + ","
                + (running/ consensuses) + "," + (stable/ consensuses);
            /* Are our results new? */
            if (!this.relaysPerDay.containsKey(tempDate)) {
              this.logger.fine("Adding new average relay numbers: "
                  + line);
              this.relaysPerDay.put(tempDate, line);
              writeConsensusStats = true;
            } else if (!line.equals(this.relaysPerDay.get(tempDate))) {
              this.logger.info("Replacing existing average relay numbers "
                  + "(" + this.relaysPerDay.get(tempDate) + " with new "
                  + "numbers: " + line);
              this.relaysPerDay.put(tempDate, line);
              writeConsensusStats = true;
            }
          }
          exit = fast = guard = running = stable = consensuses = 0;
          haveWrittenFinalLine = (next == null);
        }
        /* Sum up number of relays with given flags. */
        if (next != null) {
          String[] parts = next.split(",");
          tempDate = next.substring(0, 10);
          consensuses++;
          exit += Integer.parseInt(parts[1]);
          fast += Integer.parseInt(parts[2]);
          guard += Integer.parseInt(parts[3]);
          running += Integer.parseInt(parts[4]);
          stable += Integer.parseInt(parts[5]);
        }
      }
    }

    /* Go through raw observations of numbers of running bridges in bridge
     * statuses, calculate averages per day, and add these averages to
     * final results. */
    if (!this.bridgesRaw.isEmpty()) {
      String tempDate = null;
      int brunning = 0, statuses = 0;
      Iterator<String> it = this.bridgesRaw.values().iterator();
      boolean haveWrittenFinalLine = false;
      while (it.hasNext() || !haveWrittenFinalLine) {
        String next = it.hasNext() ? it.next() : null;
        /* Finished reading a day or even all lines? */
        if (tempDate != null && (next == null
            || !next.substring(0, 10).equals(tempDate))) {
          /* Only write results if we have seen at least half of all
           * statuses. */
          if (statuses >= 24) {
            String line = "," + (brunning / statuses);
            /* Are our results new? */
            if (!this.bridgesPerDay.containsKey(tempDate)) {
              this.logger.fine("Adding new average bridge numbers: "
                  + tempDate + line);
              this.bridgesPerDay.put(tempDate, line);
              writeConsensusStats = true;
            } else if (!line.equals(this.bridgesPerDay.get(tempDate))) {
              this.logger.info("Replacing existing average bridge "
                  + "numbers (" + this.bridgesPerDay.get(tempDate)
                  + " with new numbers: " + line);
              this.bridgesPerDay.put(tempDate, line);
              writeConsensusStats = true;
            }
          }
          brunning = statuses = 0;
          haveWrittenFinalLine = (next == null);
        }
        /* Sum up number of running bridges. */
        if (next != null) {
          tempDate = next.substring(0, 10);
          statuses++;
          brunning += Integer.parseInt(next.split(",")[1]);
        }
      }
    }

    /* Write raw numbers of relays with flags set to disk. */
    if (this.relaysRawModified) {
      try {
        this.logger.info("Writing file "
            + this.consensusStatsRawFile.getAbsolutePath() + "...");
        this.consensusStatsRawFile.getParentFile().mkdirs();
        BufferedWriter bw = new BufferedWriter(new FileWriter(
            this.consensusStatsRawFile));
        bw.append("datetime,exit,fast,guard,running,stable\n");
        for (String line : this.relaysRaw.values()) {
          bw.append(line + "\n");
        }
        bw.close();
        this.logger.info("Finished writing file "
            + this.consensusStatsRawFile.getAbsolutePath() + ".");
      } catch (IOException e) {
        this.logger.log(Level.WARNING, "Failed to write file "
            + this.consensusStatsRawFile.getAbsolutePath() + "!", e);
      }
    } else {
      this.logger.info("Not writing file "
          + this.consensusStatsRawFile.getAbsolutePath() + ", because "
          + "nothing has changed.");
    }

    /* Write raw numbers of running bridges to disk. */
    if (this.bridgesRawModified) {
      try {
        this.logger.info("Writing file "
            + this.bridgeConsensusStatsRawFile.getAbsolutePath() + "...");
        this.bridgeConsensusStatsRawFile.getParentFile().mkdirs();
        BufferedWriter bw = new BufferedWriter(
            new FileWriter(this.bridgeConsensusStatsRawFile));
        bw.append("datetime,brunning\n");
        for (String line : this.bridgesRaw.values()) {
          bw.append(line + "\n");
        }
        bw.close();
        this.logger.info("Finished writing file "
            + this.bridgeConsensusStatsRawFile.getAbsolutePath() + ".");
      } catch (IOException e) {
        this.logger.log(Level.WARNING, "Failed to write file "
            + this.bridgeConsensusStatsRawFile.getAbsolutePath() + "!",
            e);
      }
    } else {
      this.logger.info("Not writing file "
          + this.bridgeConsensusStatsRawFile.getAbsolutePath()
          + ", because nothing has changed.");
    }

    /* Write final results of relays with flags set and running bridges
     * to disk. */
    if (writeConsensusStats) {
      try {
        this.logger.info("Writing file "
            + this.consensusStatsFile.getAbsolutePath() + "...");
        this.consensusStatsFile.getParentFile().mkdirs();
        BufferedWriter bw = new BufferedWriter(new FileWriter(
            this.consensusStatsFile));
        bw.append("date,exit,fast,guard,running,stable,brunning\n");
        /* Iterate over all days, including those for which we don't have
         * observations for which we add NA's to all columns. */
        SortedSet<String> allDates = new TreeSet<String>();
        allDates.addAll(this.relaysPerDay.keySet());
        allDates.addAll(this.bridgesPerDay.keySet());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        long firstDateMillis = dateFormat.parse(allDates.first()).
            getTime();
        long lastDateMillis = dateFormat.parse(allDates.last()).getTime();
        long currentDateMillis = firstDateMillis;
        while (currentDateMillis <= lastDateMillis) {
          /* Write observations about relays, bridges, both, or none of
           * them. */
          String date = dateFormat.format(new Date(currentDateMillis));
          if (this.relaysPerDay.containsKey(date)) {
            bw.append(this.relaysPerDay.get(date));
          } else {
            bw.append(date + ",NA,NA,NA,NA,NA");
          }
          if (this.bridgesPerDay.containsKey(date)) {
            bw.append(this.bridgesPerDay.get(date) + "\n");
          } else {
            bw.append(",NA\n");
          }
          /* Advance by 1 day. */
          currentDateMillis += 24L * 60L * 60L * 1000L;
        }
        bw.close();
        this.logger.info("Finished writing file "
            + this.consensusStatsFile.getAbsolutePath() + ".");
      } catch (IOException e) {
        this.logger.log(Level.WARNING, "Failed to write file "
            + this.consensusStatsFile.getAbsolutePath() + "!", e);
      } catch (ParseException e) {
        this.logger.log(Level.WARNING, "Failed to write file "
            + this.consensusStatsFile.getAbsolutePath() + "!", e);
      }
    } else {
      this.logger.info("Not writing file "
          + this.consensusStatsFile.getAbsolutePath()
          + ", because nothing has changed.");
    }
  }
}

