import java.io.*;
import java.text.*;
import java.util.logging.*;
import org.apache.commons.codec.digest.*;

/**
 * Parses all descriptors in local directory cacheddesc/ and sorts them
 * into directory structure in directory-archive/.
 */
public class CachedRelayDescriptorReader {
  public CachedRelayDescriptorReader(RelayDescriptorParser rdp,
      ArchiveWriter aw) {
    Logger logger = Logger.getLogger(
        CachedRelayDescriptorReader.class.getName());
    File cachedDescDir = new File("cacheddesc");
    if (cachedDescDir.exists()) {
      logger.info("Reading cacheddesc/ directory.");
      try {
        rdp.initialize(); // TODO get rid of this non-sense
      } catch (IOException e) {
        return;
      }
      for (File f : cachedDescDir.listFiles()) {
        try {
          if (f.getName().equals("cached-consensus")) {
            BufferedReader br = new BufferedReader(new FileReader(f));
            if (aw != null) {
              aw.store(br);
            }
            br.close();
            br = new BufferedReader(new FileReader(f));
            if (rdp != null) {
              rdp.parse(br);
            }
            br.close();
          } else if (f.getName().startsWith("cached-descriptors") ||
              f.getName().startsWith("cached-extrainfo")) {
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line = null;
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null || sb != null) {
              if (line == null && sb.length() < 1) {
                continue; // empty file?
              }
              if (line == null || line.startsWith("router ") ||
                  line.startsWith("extra-info ")) {
                if (sb.length() > 0) {
                  BufferedReader storeBr = new BufferedReader(
                      new StringReader(sb.toString()));
                  if (aw != null) {
                    aw.store(storeBr);
                  }
                  storeBr.close();
                  storeBr = new BufferedReader(
                      new StringReader(sb.toString()));
                  if (rdp != null) {
                    rdp.parse(storeBr);
                  }
                  storeBr.close();
                }
                if (line == null) {
                  sb = null;
                  break;
                } else {
                  sb = new StringBuilder();
                }
              }
              if (!line.startsWith("@")) {
                sb.append(line + "\n");
              }
            }
            br.close();
            logger.info("Finished reading cacheddesc/ directory.");
          }
        } catch (IOException e) {
          logger.log(Level.WARNING, "Failed reading cacheddesc/ "
              + "directory.", e);
        } catch (ParseException e) {
          logger.log(Level.WARNING, "Failed reading cacheddesc/ "
              + "directory.", e);
        }
      }
    }
  }
}

