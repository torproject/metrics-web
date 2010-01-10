import java.io.*;
import java.net.*;

/**
 * Download the current consensus and hand it to the consensus parser.
 */
public class ConsensusDownloader {
  public ConsensusDownloader(ConsensusParser cp,
      String authority) throws IOException {
    System.out.print("Downloading current consensus from " + authority
        + "... ");
    BufferedInputStream in = new BufferedInputStream(new URL("http://"
        + authority + "/tor/status-vote/current/consensus").openStream());
    StringBuilder sb = new StringBuilder();
    int len;
    byte[] data = new byte[1024];
    while ((len = in.read(data, 0, 1024)) >= 0) {
      sb.append(new String(data, 0, len));
    }
    in.close();
    String consensus = sb.toString();
    System.out.println("done");
    cp.parse(new BufferedReader(new StringReader(consensus)));
  }
}

