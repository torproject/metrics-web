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
    // TODO check if files are stale; print out warning that Tor process
    // might have died
    Logger logger = Logger.getLogger(
        CachedRelayDescriptorReader.class.getName());
    File cachedDescDir = new File("cacheddesc");
    if (cachedDescDir.exists()) {
      logger.info("Reading cacheddesc/ directory.");
      if (rdp != null) {
        try {
          rdp.initialize(); // TODO get rid of this non-sense
        } catch (IOException e) {
          return;
        }
      }
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
            BufferedReader br = new BufferedReader(new FileReader(f));
            if (aw != null) {
              aw.store(allData);
            }
            br.close();
            br = new BufferedReader(new FileReader(f));
            if (rdp != null) {
              rdp.parse(br);
            }
            br.close();
          } else if (f.getName().startsWith("cached-descriptors") ||
              f.getName().startsWith("cached-extrainfo")) {
            String ascii = new String(allData, "US-ASCII");
            int start = -1, sig = -1, end = -1;
            String startToken =
                f.getName().startsWith("cached-descriptors") ?
                "router " : "extra-info ";
            String sigToken = "\nrouter-signature\n";
            String endToken = "\n-----END SIGNATURE-----\n";
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
              String desc = ascii.substring(start, end);
              byte[] forDigest = new byte[sig - start];
              System.arraycopy(allData, start, forDigest, 0, sig - start);
              String digest = DigestUtils.shaHex(forDigest);
              byte[] descBytes = new byte[end - start];
              System.arraycopy(allData, start, descBytes, 0, end - start);
              if (aw != null) {
                aw.store(descBytes);
              }
              if (rdp != null) {
                BufferedReader storeBr = new BufferedReader(
                    new StringReader(desc));
                rdp.parse(storeBr);
                storeBr.close();
              }
            }
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

