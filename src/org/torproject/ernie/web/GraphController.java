package org.torproject.ernie.web;

import org.torproject.ernie.util.ErnieProperties;
import org.apache.log4j.Logger;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import org.rosuda.REngine.Rserve.*;
import org.rosuda.REngine.*;

public class GraphController {

  private static final Logger log;
  private static final String baseDir;
  private static final int cacheSize;
  private final String graphName;
  private static int cacheClearRequests;
  private static int requests;

  private static final int rservePort;
  private static final String rserveHost;

  static {
    log = Logger.getLogger(GraphController.class.toString());
    ErnieProperties props = new ErnieProperties();
    cacheSize = props.getInt("max.cached.graphs");
    baseDir = props.getProperty("cached.graphs.dir");
    cacheClearRequests = props.getInt("cache.clear.requests");
    rservePort = props.getInt("rserve.port");
    rserveHost = props.getProperty("rserve.host");
    requests = 0;

    try {
      /* Create temp graphs directory if it doesn't exist. */
      File dir = new File(baseDir);
      if (!dir.exists())  {
        dir.mkdirs();
      }

      /* Change directory permissions to allow it to be written to
       * by Rserve. */
      Runtime rt = Runtime.getRuntime();
      rt.exec("chmod 777 " + baseDir).waitFor();
    } catch (InterruptedException e) {
    } catch (IOException e) {
      log.warn("Couldn't create temporary graphs directory. " + e);
    }
  }

  public GraphController (String graphName)  {
    this.graphName = graphName;
  }

  public void writeOutput(String imagePath, HttpServletRequest request,
      HttpServletResponse response) throws IOException {

    /* Read file from disk and write it to response. */
    BufferedInputStream input = null;
    BufferedOutputStream output = null;
    try {
      File imageFile = new File(imagePath);
      /* If there was an error when generating the graph,
       * set the header to 400 bad request. */
      if (!imageFile.exists())  {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST);
      } else {
        response.setContentType("image/png");
        response.setHeader("Content-Length", String.valueOf(
            imageFile.length()));
        response.setHeader("Content-Disposition",
            "inline; filename=\"" + graphName + ".png" + "\"");
        input = new BufferedInputStream(new FileInputStream(imageFile),
            1024);
        output = new BufferedOutputStream(response.getOutputStream(), 1024);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = input.read(buffer)) > 0) {
            output.write(buffer, 0, length);
        }
        requests++;
        if (requests % cacheClearRequests == 0) {
          deleteLRUgraph();
        }
      }
    }
    finally {
      if (output != null)
        output.close();
      if (input != null)
        input.close();
    }
  }

  public void generateGraph(String rquery, String path)  {
    try {
      File f = new File(path);
      if (!f.exists())  {
        RConnection rc = new RConnection(rserveHost, rservePort);
        rc.eval(rquery);
        rc.close();
      }
    } catch (Exception e) {
      log.warn("Internal Rserve error. Couldn't generate graph: " +
          e.toString());
    }
  }

  /* Caching mechanism to delete the least recently
   * used graph every X requests.
   * TODO We're not really deleting the least recently used graphs here,
   * but a random sample. Not the end of the world, but when we're bored,
   * let's fix this. */
  public void deleteLRUgraph()  {
    File dir = new File(baseDir);
    List<File> flist = Arrays.asList(dir.listFiles());
    if (flist.size() > (cacheSize + cacheClearRequests))  {
      Collections.sort(flist);
      for (int i = 0; i <= cacheClearRequests; i++) {
        flist.get(i).delete();
      }
    }
  }

  public String getBaseDir()  {
    return this.baseDir;
  }
}

