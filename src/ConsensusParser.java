import java.io.*;
import org.apache.commons.codec.digest.*;
import org.apache.commons.codec.binary.*;

/**
 * Parse the contents of a network status consensus and pass on the
 * relevant contents to the stats file handlers.
 */
public class ConsensusParser {
  private ConsensusStatsFileHandler csfh;
  private BridgeStatsFileHandler bsfh;
  public ConsensusParser(ConsensusStatsFileHandler csfh,
      BridgeStatsFileHandler bsfh) {
    this.csfh = csfh;
    this.bsfh = bsfh;
  }
  public void parse(BufferedReader br) throws IOException {
    int exit = 0, fast = 0, guard = 0, running = 0, stable = 0;
    String validAfter = null;
    String line = br.readLine();
    while ((line = br.readLine()) != null) {
      if (line.startsWith("valid-after ")) {
        validAfter = line.substring("valid-after ".length());
      } else if (line.startsWith("r ")) {
        String hashedRelay = DigestUtils.shaHex(Base64.decodeBase64(
            line.split(" ")[2] + "=")).toUpperCase();
        bsfh.addHashedRelay(hashedRelay);
      } else if (line.startsWith("s ")) {
        if (line.contains(" Running")) {
          exit += line.contains(" Exit") ? 1 : 0;
          fast += line.contains(" Fast") ? 1 : 0;
          guard += line.contains(" Guard") ? 1 : 0;
          stable += line.contains(" Stable") ? 1 : 0;
          running++;
        }
      }
    }
    csfh.addConsensusResults(validAfter, exit, fast, guard, running,
        stable);
  }
}

