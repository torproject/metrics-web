import java.io.*;
import java.text.*;
import java.util.*;
import java.util.logging.*;

/**
 * Parses all descriptors in local directory cacheddesc/ and sorts them
 * into directory structure in directory-archive/.
 */
public class CachedRelayDescriptorReader {
  public CachedRelayDescriptorReader(RelayDescriptorParser rdp,
      List<String> inputDirectories) {
    StringBuilder dumpStats = new StringBuilder("Finished importing "
        + "relay descriptors from local Tor data directories:");
    Logger logger = Logger.getLogger(
        CachedRelayDescriptorReader.class.getName());
    for (String inputDirectory : inputDirectories) {
      File cachedDescDir = new File(inputDirectory);
      if (!cachedDescDir.exists()) {
        logger.warning("Directory " + cachedDescDir.getAbsolutePath()
            + " does not exist. Skipping.");
        continue;
      }
      logger.fine("Reading " + cachedDescDir.getAbsolutePath()
          + " directory.");
      for (File f : cachedDescDir.listFiles()) {
        try {
          // descriptors may contain non-ASCII chars; read as bytes to
          // determine digests
          BufferedInputStream bis =
              new BufferedInputStream(new FileInputStream(f));
          ByteArrayOutputStream baos = new ByteArrayOutputStream();
          int len;
          byte[] data = new byte[1024];
          while ((len = bis.read(data, 0, 1024)) >= 0) {
            baos.write(data, 0, len);
          }
          bis.close();
          byte[] allData = baos.toByteArray();
          if (f.getName().equals("cached-consensus")) {
            /* Check if directory information is stale. */
            BufferedReader br = new BufferedReader(new StringReader(
                new String(allData, "US-ASCII")));
            String line = null;
            while ((line = br.readLine()) != null) {
              if (line.startsWith("valid-after ")) {
                dumpStats.append("\n" + f.getName() + ": " + line.substring(
                    "valid-after ".length()));
                SimpleDateFormat dateTimeFormat =
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                dateTimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                if (dateTimeFormat.parse(line.substring("valid-after ".
                    length())).getTime() < System.currentTimeMillis()
                    - 6L * 60L * 60L * 1000L) {
                  logger.warning("Cached descriptor files in "
                      + cachedDescDir.getAbsolutePath() + " are stale. "
                      + "The valid-after line in cached-consensus is '"
                      + line + "'.");
                  dumpStats.append(" (stale!)");
                }
                break;
              }
            }
            br.close();

            /* Parse the cached consensus (regardless of whether it's
             * stale or not. */
            if (rdp != null) {
              rdp.parse(allData);
            }
          } else if (f.getName().startsWith("cached-descriptors") ||
              f.getName().startsWith("cached-extrainfo")) {
            String ascii = new String(allData, "US-ASCII");
            int start = -1, sig = -1, end = -1;
            String startToken =
                f.getName().startsWith("cached-descriptors") ?
                "router " : "extra-info ";
            String sigToken = "\nrouter-signature\n";
            String endToken = "\n-----END SIGNATURE-----\n";
            int parsedNum = 0;
            while (end < ascii.length()) {
              start = ascii.indexOf(startToken, end);
              if (start < 0) {
                break;
              }
              sig = ascii.indexOf(sigToken, start);
              if (sig < 0) {
                break;
              }
              sig += sigToken.length();
              end = ascii.indexOf(endToken, sig);
              if (end < 0) {
                break;
              }
              end += endToken.length();
              byte[] descBytes = new byte[end - start];
              System.arraycopy(allData, start, descBytes, 0, end - start);
              if (rdp != null) {
                rdp.parse(descBytes);
                parsedNum++;
              }
            }
            dumpStats.append("\n" + f.getName() + ": " + parsedNum + " "
                + (f.getName().startsWith("cached-descriptors") ?
                "server" : "extra-info") + " descriptors");
            logger.fine("Finished reading "
                + cachedDescDir.getAbsolutePath() + " directory.");
          }
        } catch (IOException e) {
          logger.log(Level.WARNING, "Failed reading "
              + cachedDescDir.getAbsolutePath() + " directory.", e);
        } catch (ParseException e) {
          logger.log(Level.WARNING, "Failed reading "
              + cachedDescDir.getAbsolutePath() + " directory.", e);
        }
      }
    }
    logger.info(dumpStats.toString());
  }
}

