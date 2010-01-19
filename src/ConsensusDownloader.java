import java.io.*;
import java.net.*;

/**
 * Download the current consensus and hand it to the consensus parser.
 */
public class ConsensusDownloader {
  public ConsensusDownloader(RelayDescriptorParser rdp, String authority)
      throws IOException {
    System.out.print("Downloading current consensus from " + authority
        + "... ");
    URL u = new URL("http://" + authority
        + "/tor/status-vote/current/consensus");
    HttpURLConnection huc = (HttpURLConnection) u.openConnection();
    huc.setRequestMethod("GET");
    huc.connect();
    int response = huc.getResponseCode();
    if (response == 200) {
      BufferedInputStream in = new BufferedInputStream(
          huc.getInputStream());
      StringBuilder sb = new StringBuilder();
      int len;
      byte[] data = new byte[1024];
      while ((len = in.read(data, 0, 1024)) >= 0) {
        sb.append(new String(data, 0, len));
      }
      in.close();
      String consensus = sb.toString();
      rdp.parse(new BufferedReader(new StringReader(consensus)));
    }
    System.out.println("done");
  }
}

