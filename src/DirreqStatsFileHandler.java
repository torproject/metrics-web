import java.io.*;
import java.util.*;
import java.util.logging.*;
import java.text.*;

/**
 *
 */
public class DirreqStatsFileHandler {
  private String statsDir;
  private SortedSet<String> countries;
  private File dirreqStatsFile;
  private SortedMap<String, String> observations;
  private boolean modified;
  private Logger logger;
  public DirreqStatsFileHandler(String statsDir,
      SortedSet<String> countries) {
    this.statsDir = statsDir;
    this.countries = countries;
    this.dirreqStatsFile = new File(statsDir + "/dirreq-stats");
    this.observations = new TreeMap<String, String>();
    this.logger =
        Logger.getLogger(DirreqStatsFileHandler.class.getName());
    if (this.dirreqStatsFile.exists()) {
      this.logger.info("Reading file " + statsDir + "/dirreq-stats...");
      try {
        BufferedReader br = new BufferedReader(new FileReader(
            this.dirreqStatsFile));
        String line = br.readLine();
        if (line != null) {
          String[] headers = line.split(",");
          for (int i = 2; i < headers.length - 1; i++) {
            this.countries.add(headers[i]);
          }
          while ((line = br.readLine()) != null) {
            String[] readData = line.split(",");
            String dirNickname = readData[0];
            String date = readData[1];
            if (!readData[readData.length - 1].equals("NA")) {
              Map<String, String> obs = new HashMap<String, String>();
              for (int i = 2; i < readData.length - 1; i++) {
                obs.put(headers[i], readData[i]);
              }
              String share = readData[readData.length - 1];
              this.addObs(dirNickname, date, obs, share);
            }
          }
        }
        br.close();
        this.logger.info("Finished reading file " + statsDir
            + "/dirreq-stats...");
      } catch (IOException e) {
        this.logger.log(Level.WARNING, "Failed reading file " + statsDir
            + "/dirreq-stats!", e);
      }
    }
  }
  public void addObs(String dirNickname, String date,
      Map<String, String> obs, String share) throws IOException {
    String obsKey = dirNickname + "," + date;
    StringBuilder sb = new StringBuilder(obsKey);
    for (String c : this.countries) {
      sb.append("," + (obs.containsKey(c) ? obs.get(c) : "0"));
    }
    sb.append("," + share);
    this.observations.put(obsKey, sb.toString());
    this.modified = true;
  }
  public void writeFile() {
    if (!this.modified) {
      return;
    }
    try {
      if (!this.observations.isEmpty()) {
        this.logger.info("Writing file " + this.statsDir
            + "/dirreq-stats...");
        new File(this.statsDir).mkdirs();
        BufferedWriter bwDirreqStats = new BufferedWriter(
            new FileWriter(this.dirreqStatsFile));
        bwDirreqStats.append("directory,date");
        for (String country : this.countries) {
          bwDirreqStats.append("," + country);
        }
        bwDirreqStats.append(",share\n");
        long lastDate = 0L;
        String lastDirectory = null;
        SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd");
        timeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        for (String observation : this.observations.values()) {
          String currentDirectory = observation.split(",")[0];
          long currentDate = timeFormat.parse(observation.split(",")[1]).
              getTime();
          while (currentDirectory.equals(lastDirectory)
              && lastDate > 0L && currentDate - 86400000L > lastDate) {
            lastDate += 86400000L;
            bwDirreqStats.append(currentDirectory + ","
                + timeFormat.format(new Date(lastDate)));
            for (String country : this.countries) {
              bwDirreqStats.append(",NA");
            }
            bwDirreqStats.append(",NA\n");
          }
          lastDate = currentDate;
          lastDirectory = currentDirectory;
          bwDirreqStats.append(observation + "\n");
        }
        bwDirreqStats.close();
        this.logger.info("Finished writing file " + this.statsDir
            + "/dirreq-stats.");
      }
    } catch (IOException e) {
      this.logger.log(Level.WARNING, "Failed writing file "
          + this.statsDir + "/dirreq-stats!", e);
    } catch (ParseException e) {
      this.logger.log(Level.WARNING, "Failed writing file "
          + this.statsDir + "/dirreq-stats!", e);
    }
  }
}

