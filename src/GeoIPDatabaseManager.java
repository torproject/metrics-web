import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;
import java.util.logging.*;
import java.util.zip.*;

/**
 * Maintains multiple versions of GeoIP databases to resolve IP addresses
 * to country codes using the most recent database at a given time.
 * Supports importing CSV-formatted databases from disk and downloading
 * the most recent commercial Maxmind GeoIP database from their server
 * using a license key.
 */
public class GeoIPDatabaseManager {

  /**
   * Database entry of the combined GeoIP database consisting of start IP
   * address, end IP address, and countries of all contained database
   * versions.
   */
  private static class DatabaseEntry {
    
    /**
     * Start IP address.
     */
    long fromIP;

    /**
     * End IP address.
     */
    long toIP;

    /**
     * Countries of all contained database versions.
     */
    String countries;
  }

  /**
   * Mapping from an IP address in decimal form to a database entry.
   */
  private SortedMap<Long, DatabaseEntry> combinedDatabase;

  /**
   * Has the combined database been modified from importing database
   * versions from disk?
   */
  private boolean combinedDatabaseModified;

  /**
   * File holding the combined GeoIP database.
   */
  private File combinedDatabaseFile;

  /**
   * List of dates representing the GeoIP database versions.
   */
  private List<String> allDatabases;

  /**
   * Timestamp when we last downloaded the GeoIP database from the Maxmind
   * servers.
   */
  private String lastDownloadedTime;

  /**
   * Logger for this class.
   */
  private Logger logger;

  /**
   * Initializes this class by reading in the database versions known so
   * far.
   */
  public GeoIPDatabaseManager() {

    /* Initialize instance variables. */
    this.combinedDatabaseFile = new File("stats/geoip-database");
    this.combinedDatabase = new TreeMap<Long, DatabaseEntry>();
    this.allDatabases = new ArrayList<String>();
    this.combinedDatabaseModified = false;

    /* Initialize logger. */
    this.logger = Logger.getLogger(RelayDescriptorParser.class.getName());

    /* Read in combined GeoIP database. */
    if (this.combinedDatabaseFile.exists()) {
      try {
        this.logger.fine("Reading in "
            + this.combinedDatabaseFile.getAbsolutePath() + "...");
        BufferedReader br = new BufferedReader(new FileReader(
            this.combinedDatabaseFile));
        String line = null;
        while ((line = br.readLine()) != null) {
          if (line.startsWith("lastDownload")) {
            this.lastDownloadedTime = line.substring("lastDownload ".
                length());
          } else if (line.startsWith("beginIpNum,endIpNum")) {
            String[] parts = line.split(",");
            for (int i = 2; i < parts.length; i++) {
              this.allDatabases.add(parts[i]);
            }
          } else {
            String[] parts = line.split(",");
            DatabaseEntry e = new DatabaseEntry();
            e.fromIP = Long.parseLong(parts[0]);
            e.toIP = Long.parseLong(parts[1]);
            e.countries = line.substring(line.indexOf(",",
                line.indexOf(",") + 1));
            this.combinedDatabase.put(e.fromIP, e);
          }
        }
        br.close();
        this.logger.fine("Finished reading in "
            + this.combinedDatabaseFile.getAbsolutePath() + ".");
      } catch (IOException e) {
        this.logger.log(Level.WARNING, "Failed to read in "
            + this.combinedDatabaseFile.getAbsolutePath() + "!", e);
      }
    }
  }

  /**
   * Downloads today's commercial Maxmind GeoIP database, if such a
   * database exists, and writes it to disk. This method should be called
   * before importing GeoIP databases from disk if the new database should
   * be included in the combined database.
   */
  public void downloadGeoIPDatabase(String licenseKey) {
    if (licenseKey == null || licenseKey.length() < 1) {
      logger.warning("Missing or invalid license key for downloading "
          + "GeoIP database!");
      return;
    }

    /* Find out when we tried downloading the last time to avoid making
     * too many download attempts. */
    SimpleDateFormat dateTimeFormat = new SimpleDateFormat(
        "yyyy-MM-dd HH:mm:ss");
    dateTimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    long now = System.currentTimeMillis();
    if (this.lastDownloadedTime != null) {
      long lastDownloaded = -1;
      try {
        lastDownloaded = dateTimeFormat.parse(this.lastDownloadedTime).
            getTime();
      } catch (ParseException e) {
        logger.log(Level.WARNING, "Could not parse last downloaded "
            + "time '" + this.lastDownloadedTime + "'. Ignoring.");
      }
      if (lastDownloaded + 8L * 60L * 60L * 1000L > now) {
        logger.finer("Last GeoIP database download not more than 8 "
            + "hours in the past. Not downloading.");
        return;
      }
    }

    /* Download GeoIP database. */
    try {
      logger.fine("Downloading GeoIP database...");
      this.lastDownloadedTime = dateTimeFormat.format(now);
      SimpleDateFormat urlDateFormat = new SimpleDateFormat("yyyyMMdd");
      urlDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
      String date = urlDateFormat.format(now);
      String url = "http://www.maxmind.com/app/download_new"
          + "?edition_id=108&date=" + date + "&suffix=zip&license_key="
          + licenseKey;
      URL u = new URL(url);
      HttpURLConnection huc = (HttpURLConnection) u.openConnection();
      huc.setRequestMethod("GET");
      huc.connect();
      int response = huc.getResponseCode();
      if (response != 200) {
        logger.fine("Could not download GeoIP database. Response code "
            + response);
        return;
      }
      BufferedInputStream bis = new BufferedInputStream(
          huc.getInputStream());
      ZipInputStream zis = new ZipInputStream(bis);
      BufferedInputStream bis2 = new BufferedInputStream(zis);
      ZipEntry entry = null;
      while ((entry = zis.getNextEntry()) != null) {
        if (!entry.isDirectory() &&
            entry.getName().endsWith("GeoIP-108.csv")) {
          String filename = "geoipdb/GeoIP-108_" + date + ".csv";
          OutputStream out = new BufferedOutputStream(
              new FileOutputStream(filename));
          byte[] buffer = new byte[1024];
          int len;
          while ((len = bis2.read(buffer)) >= 0) {
            out.write(buffer, 0, len);
          }
          out.close();
        }
      }
      zis.close();
    } catch (IOException e) {
      this.logger.log(Level.WARNING, "Could not download GeoIP database. "
          + "Exiting.", e);
      return;
    }
  }

  /**
   * Imports the GeoIP databases from <code>directory</code> to include
   * them in the combined GeoIP database.
   */
  public void importGeoIPDatabaseFromDisk(String directory) {
    File databaseDirectory = new File(directory);
    if (!databaseDirectory.exists()) {
      return;
    }
    for (File databaseFile : databaseDirectory.listFiles()) {
      String filename = databaseFile.getName();
      if (!filename.startsWith("GeoIP-108_") ||
          filename.length() != "GeoIP-108_xxxxxxxx.csv".length() ||
          !filename.endsWith(".csv")) {
        continue;
      }
      String date = filename.substring("GeoIP-108_".length(),
          "GeoIP-108_xxxxxxxx".length());
      if (allDatabases.contains(date)) {
        continue;
      }
      this.combinedDatabaseModified = true;
      this.logger.fine("Reading in " + filename);
      String emptyCountryString = "";
      for (int i = 0; i < this.allDatabases.size(); i++) {
        emptyCountryString += ",ZZ";
      }
      try {
        BufferedReader br = new BufferedReader(new FileReader(
            databaseFile));
        String line = null;
        while ((line = br.readLine()) != null) {
          if (line.startsWith("Copyright") ||
              line.startsWith("\"begin")) {
            continue;
          }
          String lineWithoutQuotes = line.replaceAll("\"", "");
          String[] parts = lineWithoutQuotes.split(",");
          lineWithoutQuotes = null; // does this help GC?
          long fromIP = Long.parseLong(parts[2]);
          long toIP = Long.parseLong(parts[3]);
          String countryCode = parts[4];
          SortedMap<Long, DatabaseEntry> submap =
              combinedDatabase.headMap(toIP + 1L);
          if (!submap.headMap(fromIP + 1L).isEmpty()) {
            submap = submap.tailMap(submap.headMap(fromIP + 1L).
                lastKey());
          }
          Set<DatabaseEntry> newEntries = new HashSet<DatabaseEntry>();
          for (DatabaseEntry e : submap.values()) {
            while (fromIP <= toIP && fromIP <= e.toIP &&
                toIP >= e.fromIP) {
              if (fromIP < e.fromIP) {
                // duplicate entry: new entry fromIP-e.fromIP, set fromIP
                // to e.fromIP
                DatabaseEntry e1 = new DatabaseEntry();
                e1.fromIP = fromIP;
                e1.toIP = e.fromIP - 1L;
                e1.countries = e.countries;
                newEntries.add(e1);
                fromIP = e.fromIP;
              } else if (fromIP > e.fromIP) {
                // split off existing entry; don't add yet
                DatabaseEntry e1 = new DatabaseEntry();
                e1.fromIP = e.fromIP;
                e1.toIP = fromIP - 1L;
                e1.countries = e.countries;
                newEntries.add(e1);
                e.fromIP = fromIP;
                newEntries.add(e);
              } else if (toIP < e.toIP) {
                // split and add to first half
                DatabaseEntry e1 = new DatabaseEntry();
                e1.fromIP = toIP + 1L;
                e1.toIP = e.toIP;
                e1.countries = e.countries;
                newEntries.add(e1);
                e.toIP = e1.fromIP - 1L;
                e.countries += "," + countryCode;
                fromIP = toIP + 1L;
              } else if (toIP >= e.toIP) {
                // add to this entry and done, right?
                e.countries += "," + countryCode;
                fromIP = toIP + 1L;
              }
            }
          }
          if (fromIP <= toIP) {
            DatabaseEntry entry = new DatabaseEntry();
            entry.fromIP = fromIP;
            entry.toIP = toIP;
            entry.countries = emptyCountryString + "," + countryCode;
            newEntries.add(entry);
            fromIP = toIP + 1L;
          }
          for (DatabaseEntry e : newEntries) {
            this.combinedDatabase.put(e.fromIP, e);
          }
        }
        this.allDatabases.add(date);
        for (DatabaseEntry e : this.combinedDatabase.values()) {
          if (e.countries.substring(1).split(",").length <
              this.allDatabases.size()) {
            e.countries += ",ZZ";
          }
        }
      } catch (IOException e) {
        this.logger.log(Level.WARNING, "Could not import GeoIP database "
            + "from file " + databaseFile.getAbsolutePath()
            + ". This might leave us with an inconsistent state!");
      }
    }
  }

  public void writeCombinedDatabase() {
    if (!combinedDatabaseModified) {
      return;
    }
    try {
      this.logger.fine("Writing "
          + this.combinedDatabaseFile.getAbsolutePath() + "...");
      BufferedWriter bw = new BufferedWriter(new FileWriter(
          this.combinedDatabaseFile));
      bw.write("lastDownload " + this.lastDownloadedTime + "\n");
      bw.write("beginIpNum,endIpNum");
      for (String d : allDatabases) {
        bw.write("," + d);
      }
      bw.write("\n");
      for (DatabaseEntry e : this.combinedDatabase.values()) {
        bw.write(e.fromIP + "," + e.toIP + e.countries + "\n");
      }
      bw.close();
      this.logger.fine("Finished writing "
          + this.combinedDatabaseFile.getAbsolutePath() + ".");
    } catch (IOException e) {
      this.logger.log(Level.WARNING, "Failed to write "
          + this.combinedDatabaseFile.getAbsolutePath() + "!");
    }
  }

  /**
   * Returns the uppercase two-letter country code that was assigned to
   * <code>ipAddress</code> (in dotted notation) in the most recent
   * commercial Maxmind GeoIP database published at least 1 day before
   * <code>date</code> (in the format yyyy-MM-dd).
   */
  public String getCountryForIP(String ipAddress, String date) {
    String[] parts = ipAddress.split("\\.");
    long ipNum = Long.parseLong(parts[0]) * 256 * 256 * 256 +
        Long.parseLong(parts[1]) * 256 * 256 +
        Long.parseLong(parts[2]) * 256 + Long.parseLong(parts[3]);
    String countries = null;
    if (this.combinedDatabase.containsKey(ipNum)) {
      countries = this.combinedDatabase.get(ipNum).countries;
    } else if (!this.combinedDatabase.headMap(ipNum).isEmpty()) {
      countries = this.combinedDatabase.get(this.combinedDatabase.headMap(
          ipNum).lastKey()).countries;
    } else {
      return "ZZ";
    }
    String dateShort = date.substring(0, 4) + date.substring(5, 7)
        + date.substring(8, 10);
    SortedSet<String> subset = new TreeSet<String>(this.allDatabases).
        headSet(dateShort);
    if (subset.isEmpty()) {
      return "ZZ";
    }
    int index = allDatabases.indexOf(subset.last());
    return countries.substring(1).split(",")[index];
  }
}
