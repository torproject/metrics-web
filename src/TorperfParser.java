/*
    // Remove corrupted lines from gabelmoo's torperf data
    File[] gtpFiles = new File[] { new File("gabelmoo-50kb.dat"),
        new File("gabelmoo-1mb.dat"), new File("gabelmoo-5mb.dat") };
    for (File g : gtpFiles) {
      BufferedReader br = new BufferedReader(new FileReader(g));
      BufferedWriter bw = new BufferedWriter(new FileWriter(g.getName()
          + "a"));
      String line = null;
      while ((line = br.readLine()) != null) {
        if (line.split(" ").length == 20) {
          bw.write(line + "\n");
        }
      }
      bw.close();
      br.close();
    }

*/
