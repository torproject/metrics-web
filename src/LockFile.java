import java.io.*;

public class LockFile {

  private File lockFile;

  public LockFile() {
    this.lockFile = new File("lock");
  }

  public boolean acquireLock() {
    try {
      if (this.lockFile.exists()) {
        BufferedReader br = new BufferedReader(new FileReader("lock"));
        long runStarted = Long.parseLong(br.readLine());
        br.close();
        if (System.currentTimeMillis() - runStarted < 60L * 60L * 1000L) {
          return false;
        }
      }
      BufferedWriter bw = new BufferedWriter(new FileWriter("lock"));
      bw.append("" + System.currentTimeMillis() + "\n");
      bw.close();
      return true;
    } catch (IOException e) {
      return false;
    }
  }

  public void releaseLock() {
    this.lockFile.delete();
  }
}

