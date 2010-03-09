import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;
/**
 * Initialize configuration with hard-coded defaults, overwrite with
 * configuration in config config file, if exists, and answer Main.java
 * about our configuration.
 */
public class Configuration {
  private boolean writeStats = true;
  private boolean writeConsensusStats = true;
  private boolean writeDirreqStats = true;
  private SortedSet<String> dirreqBridgeCountries = new TreeSet<String>(
      Arrays.asList("bh,cn,cu,et,ir,mm,sa,sy,tn,tm,uz,vn,ye".split(",")));
  private SortedSet<String> dirreqDirectories = new TreeSet<String>(
      Arrays.asList(("8522EB98C91496E80EC238E732594D1509158E77,"
      + "9695DFC35FFEB861329B9F1AB04C46397020CE31").split(",")));
  private boolean writeBridgeStats = true;
  private boolean writeServerDescriptorStats = true;
  private List<String> relayVersions = new ArrayList<String>(Arrays.asList(
      "0.1.2,0.2.0,0.2.1,0.2.2".split(",")));
  private List<String> relayPlatforms = new ArrayList<String>(Arrays.asList(
      "Linux,Windows,Darwin,FreeBSD".split(",")));
  private boolean writeDirectoryArchives = false;
  private SortedSet<String> v3DirectoryAuthorities = new TreeSet<String>(
      Arrays.asList(("14C131DFC5C6F93646BE72FA1401C02A8DF2E8B4,"
      + "E8A9C45EDE6D711294FADF8E7951F4DE6CA56B58,"
      + "D586D18309DED4CD6D57C18FDB97EFA96D330566,"
      + "585769C78764D58426B8B52B6651A5A71137189A,"
      + "27B6B5996C426270A5C95488AA5BCEB6BCC86956,"
      + "80550987E1D626E3EBA5E5E75A458DE0626D088C,"
      + "ED03BB616EB2F60BEC80151114BB25CEF515B226,"
      + "81349FC1F2DBA2C2C11B45CB9706637D480AB913,"
      + "E2A2AF570166665D738736D0DD58169CC61D8A8B").split(",")));
  private boolean importCachedRelayDescriptors = true;
  private boolean importDirectoryArchives = true;
  private boolean importSanitizedBridges = true;
  private boolean importBridgeSnapshots = true;
  private boolean importWriteTorperfStats = true;
  private boolean downloadRelayDescriptors = false;
  private List<String> downloadFromDirectoryAuthorities = Arrays.asList(
      "86.59.21.38,194.109.206.212,80.190.246.100:8180".split(","));
  private boolean downloadProcessGetTorStats = false;
  private String getTorStatsUrl = "http://gettor.torproject.org:8080/"
      + "~gettor/gettor_stats.txt";
  private boolean downloadExitList = false;
  public Configuration() {
    Logger logger = Logger.getLogger(Configuration.class.getName());
    File configFile = new File("config");
    if (!configFile.exists()) {
      return;
    }
    String line = null;
    try {
      BufferedReader br = new BufferedReader(new FileReader(configFile));
      while ((line = br.readLine()) != null) {
        if (line.startsWith("#") || line.length() < 1) {
          continue;
        } else if (line.startsWith("WriteConsensusStats")) {
          this.writeConsensusStats = Integer.parseInt(
              line.split(" ")[1]) != 0;
        } else if (line.startsWith("WriteDirreqStats")) {
          this.writeDirreqStats = Integer.parseInt(
              line.split(" ")[1]) != 0;
        } else if (line.startsWith("DirreqBridgeCountries")) {
          this.dirreqBridgeCountries = new TreeSet<String>();
          for (String country : line.split(" ")[1].split(",")) {
            if (country.length() != 2) {
              logger.severe("Configuration file contains illegal country "
                  + "code in line '" + line + "'! Exiting!");
              System.exit(1);
            }
            this.dirreqBridgeCountries.add(country);
          }
        } else if (line.startsWith("DirreqDirectories")) {
          this.dirreqDirectories = new TreeSet<String>(
              Arrays.asList(line.split(" ")[1].split(",")));
        } else if (line.startsWith("WriteBridgeStats")) {
          this.writeBridgeStats = Integer.parseInt(
              line.split(" ")[1]) != 0;
        } else if (line.startsWith("WriteServerDescriptorStats")) {
          this.writeServerDescriptorStats = Integer.parseInt(
              line.split(" ")[1]) != 0;
        } else if (line.startsWith("RelayVersions")) {
          this.relayVersions = new ArrayList<String>(
              Arrays.asList(line.split(" ")[1].split(",")));
        } else if (line.startsWith("RelayPlatforms")) {
          this.relayPlatforms = new ArrayList<String>(
              Arrays.asList(line.split(" ")[1].split(",")));
        } else if (line.startsWith("WriteDirectoryArchives")) {
          this.writeDirectoryArchives = Integer.parseInt(
              line.split(" ")[1]) != 0;
        } else if (line.startsWith("V3DirectoryAuthorities")) {
          this.v3DirectoryAuthorities = new TreeSet<String>(
              Arrays.asList(line.split(" ")[1].split(",")));
        } else if (line.startsWith("ImportCachedRelayDescriptors")) {
          this.importCachedRelayDescriptors = Integer.parseInt(
              line.split(" ")[1]) != 0;
        } else if (line.startsWith("ImportDirectoryArchives")) {
          this.importDirectoryArchives = Integer.parseInt(
              line.split(" ")[1]) != 0;
        } else if (line.startsWith("ImportSanitizedBridges")) {
          this.importSanitizedBridges = Integer.parseInt(
              line.split(" ")[1]) != 0;
        } else if (line.startsWith("ImportBridgeSnapshots")) {
          this.importBridgeSnapshots = Integer.parseInt(
              line.split(" ")[1]) != 0;
        } else if (line.startsWith("ImportWriteTorperfStats")) {
          this.importWriteTorperfStats = Integer.parseInt(
              line.split(" ")[1]) != 0;
        } else if (line.startsWith("DownloadRelayDescriptors")) {
          this.downloadRelayDescriptors = Integer.parseInt(
              line.split(" ")[1]) != 0;
        } else if (line.startsWith("DownloadFromDirectoryAuthorities")) {
          this.downloadFromDirectoryAuthorities = new ArrayList<String>();
          for (String dir : line.split(" ")[1].split(",")) {
            // test if IP:port pair has correct format
            if (dir.length() < 1) {
              logger.severe("Configuration file contains directory "
                  + "authority IP:port of length 0 in line '" + line
                  + "'! Exiting!");
              System.exit(1);
            }
            new URL("http://" + dir + "/");
            this.downloadFromDirectoryAuthorities.add(dir);
          }
        } else if (line.startsWith("DownloadProcessGetTorStats")) {
          this.downloadProcessGetTorStats = Integer.parseInt(
              line.split(" ")[1]) != 0;
        } else if (line.startsWith("GetTorStatsURL")) {
          String newUrl = line.split(" ")[1];
          // test if URL has correct format
          new URL(newUrl);
          this.getTorStatsUrl = newUrl;
        } else if (line.startsWith("DownloadExitList")) {
          this.downloadExitList = Integer.parseInt(
              line.split(" ")[1]) != 0;
        } else {
          logger.severe("Configuration file contains unrecognized "
              + "configuration key in line '" + line + "'! Exiting!");
          System.exit(1);
        }
      }
      br.close();
    } catch (ArrayIndexOutOfBoundsException e) {
      logger.severe("Configuration file contains configuration key "
          + "without value in line '" + line + "'. Exiting!");
      System.exit(1);
    } catch (MalformedURLException e) {
      logger.severe("Configuration file contains illegal URL or IP:port "
          + "pair in line '" + line + "'. Exiting!");
      System.exit(1);
    } catch (NumberFormatException e) {
      logger.severe("Configuration file contains illegal value in line '"
          + line + "' with legal values being 0 or 1. Exiting!");
      System.exit(1);
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Unknown problem while reading config "
          + "file! Exiting!", e);
      System.exit(1);
    }
  }
  public boolean getWriteStats() {
    return this.writeStats;
  }
  public boolean getWriteConsensusStats() {
    return this.writeConsensusStats;
  }
  public boolean getWriteDirreqStats() {
    return this.writeDirreqStats;
  }
  public SortedSet<String> getDirreqBridgeCountries() {
    return this.dirreqBridgeCountries;
  }
  public SortedSet<String> getDirreqDirectories() {
    return this.dirreqDirectories;
  }
  public boolean getWriteBridgeStats() {
    return this.writeBridgeStats;
  }
  public boolean getWriteServerDescriptorStats() {
    return this.writeServerDescriptorStats;
  }
  public List<String> getRelayVersions() {
    return this.relayVersions;
  }
  public List<String> getRelayPlatforms() {
    return this.relayPlatforms;
  }
  public boolean getWriteDirectoryArchives() {
    return this.writeDirectoryArchives;
  }
  public SortedSet<String> getV3DirectoryAuthorities() {
    return this.v3DirectoryAuthorities;
  }
  public boolean getImportCachedRelayDescriptors() {
    return this.importCachedRelayDescriptors;
  }
  public boolean getImportDirectoryArchives() {
    return this.importDirectoryArchives;
  }
  public boolean getImportSanitizedBridges() {
    return this.importSanitizedBridges;
  }
  public boolean getImportBridgeSnapshots() {
    return this.importBridgeSnapshots;
  }
  public boolean getImportWriteTorperfStats() {
    return this.importWriteTorperfStats;
  }
  public boolean getDownloadRelayDescriptors() {
    return this.downloadRelayDescriptors;
  }
  public List<String> getDownloadFromDirectoryAuthorities() {
    return this.downloadFromDirectoryAuthorities;
  }
  public boolean getDownloadProcessGetTorStats() {
    return this.downloadProcessGetTorStats;
  }
  public String getGetTorStatsUrl() {
    return this.getTorStatsUrl;
  }
  public boolean getDownloadExitList() {
    return this.downloadExitList;
  }
}

