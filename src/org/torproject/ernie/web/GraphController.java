package org.torproject.ernie.web;

import java.io.*;
import java.util.*;

import org.rosuda.REngine.Rserve.*;
import org.rosuda.REngine.*;

public class GraphController {

  /* Singleton instance and getInstance method of this class. */
  private static GraphController instance = new GraphController();
  public static GraphController getInstance() {
    return instance;
  }

  /* Host and port where Rserve is listening. */
  private String rserveHost;
  private int rservePort;

  /* Some parameters for our cache of graph images. */
  private String cachedGraphsDirectory;
  private long maxCacheAge;

  protected GraphController ()  {

    /* Read properties from property file. */
    ErnieProperties props = new ErnieProperties();
    this.cachedGraphsDirectory = props.getProperty("cached.graphs.dir");
    this.maxCacheAge = (long) props.getInt("max.cache.age");
    this.rserveHost = props.getProperty("rserve.host");
    this.rservePort = props.getInt("rserve.port");
  }

  /* Generate a graph using the given R query that has a placeholder for
   * the absolute path to the image to be created. */
  public byte[] generateGraph(String rQuery, String imageFilename) {

    /* See if we need to generate this graph. */
    File imageFile = new File(this.cachedGraphsDirectory + "/"
        + imageFilename);
    long now = System.currentTimeMillis();
    if (!imageFile.exists() || imageFile.lastModified() < now
        - this.maxCacheAge * 1000L) {

      /* We do. Update the R query to contain the absolute path to the file
       * to be generated, create a connection to Rserve, run the R query,
       * and close the connection. The generated graph will be on disk. */
      rQuery = String.format(rQuery, imageFile.getAbsolutePath());
      try {
        RConnection rc = new RConnection(rserveHost, rservePort);
        rc.eval(rQuery);
        rc.close();
      } catch (RserveException e) {
        return null;
      }

      /* Check that we really just generated the file */
      if (!imageFile.exists() || imageFile.lastModified() < now
          - this.maxCacheAge * 1000L) {
        return null;
      }
    }

    /* Read the image from disk and write it to a byte array. */
    byte[] result = null;
    try {
      BufferedInputStream bis = new BufferedInputStream(
          new FileInputStream(imageFile), 1024);
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      byte[] buffer = new byte[1024];
      int length;
      while ((length = bis.read(buffer)) > 0) {
        baos.write(buffer, 0, length);
      }
      result = baos.toByteArray();
    } catch (IOException e) {
      return null;
    }

    /* Return the graph bytes. */
    return result;
  }
}

