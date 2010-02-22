import java.io.*;
import java.text.*;
import org.apache.commons.codec.digest.*;

/**
 * Parses all descriptors in local directory cacheddesc/ and sorts them
 * into directory structure in directory-archive/.
 */
public class CachedRelayDescriptorReader {
  public CachedRelayDescriptorReader(RelayDescriptorParser rdp,
      ArchiveWriter aw) {
    File cachedDescDir = new File("cacheddesc");
    if (cachedDescDir.exists()) {
      try {
        rdp.initialize();
      } catch (IOException e) {
        return;
      }
      for (File f : cachedDescDir.listFiles()) {
        try {
          if (f.getName().equals("cached-consensus") ||
              f.getName().startsWith("cached-descriptors") ||
              f.getName().startsWith("cached-extrainfo")) {
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line = null, validAfterTime = null, publishedTime = null,
                extraInfoDigest = null, digest = null;
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
                  if (f.getName().equals("cached-consensus")) {
                    aw.storeConsensus(storeBr, validAfterTime);
                    validAfterTime = null;
                  } else if (f.getName().startsWith("cached-descriptors")) {
                    aw.storeServerDescriptor(storeBr, digest,
                        publishedTime, extraInfoDigest);
                    digest = null;
                    publishedTime = null;
                    extraInfoDigest = null;
                  } else if (f.getName().startsWith("cached-extrainfo")) {
                    aw.storeExtraInfo(storeBr, digest, publishedTime);
                    digest = null;
                    publishedTime = null;
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
              if (line.startsWith("valid-after ")) {
                validAfterTime = line.substring("valid-after ".length());
              } else if (line.startsWith("published ")) {
                publishedTime = line.substring("published ".length());
              } else if (line.startsWith("router-signature")) {
                digest = DigestUtils.shaHex(sb.toString()
                    + "router-signature\n").toUpperCase();
              } else if (line.startsWith("opt extra-info-digest ")) {
                extraInfoDigest = line.split(" ")[2];
              } else if (line.startsWith("extra-info-digest ")) {
                extraInfoDigest = line.split(" ")[1];
              }
              if (!line.startsWith("@")) {
                sb.append(line + "\n");
              }
            }
            br.close();
          }
        } catch (IOException e) {
          // TODO handle
        } catch (ParseException e) {
          // TODO handle
        }
      }
    }
  }
}

