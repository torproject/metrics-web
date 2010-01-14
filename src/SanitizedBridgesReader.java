import java.io.*;
import java.text.*;
import java.util.*;
import org.apache.commons.codec.digest.*;

public class SanitizedBridgesReader {
  public SanitizedBridgesReader(BridgeDescriptorParser bdp,
      String bridgesDir, SortedSet<String> countries) throws IOException,
      ParseException {
    System.out.print("Importing files in directory " + bridgesDir
        + "/... ");
    Stack<File> filesInInputDir = new Stack<File>();
    filesInInputDir.add(new File(bridgesDir));
    while (!filesInInputDir.isEmpty()) {
      File pop = filesInInputDir.pop();
      if (pop.isDirectory()) {
        for (File f : pop.listFiles()) {
          filesInInputDir.add(f);
        }
        continue;
      } else {
        BufferedReader br = new BufferedReader(new FileReader(pop));
        String fn = pop.getName();
        String dateTime = fn.substring(0, 4) + "-" + fn.substring(4, 6)
            + "-" + fn.substring(6, 8) + " " + fn.substring(9, 11)
            + ":" + fn.substring(11, 13) + ":" + fn.substring(13, 15);
        bdp.parse(br, dateTime, true);
        br.close();
      }
    }
    System.out.println("done");
  }
}

