import java.io.*;
import java.net.*;
import java.util.*;

public class ExtraInfoDownloader {
  public ExtraInfoDownloader(RelayDescriptorParser rdp,
      String authority, SortedMap<String, String> directories)
      throws IOException {
    System.out.print("Downloading extra-info descriptors from "
        + authority + "... ");
    Stack<String> extraInfos = new Stack<String>();
    for (String fingerprint : directories.keySet()) {
      BufferedInputStream in = new BufferedInputStream(new URL(
          "http://" + authority + "/tor/extra/fp/" + fingerprint).
          openStream());
      StringBuilder sb = new StringBuilder();
      int len;
      byte[] data = new byte[1024];
      while ((len = in.read(data, 0, 1024)) >= 0) {
        sb.append(new String(data, 0, len));
      }
      in.close();
      String extraInfo = sb.toString();
      if (extraInfo.length() > 0) {
        BufferedReader br = new BufferedReader(new StringReader(extraInfo));
        rdp.parse(br);
      }
    }
    System.out.println("done");
  }
}

