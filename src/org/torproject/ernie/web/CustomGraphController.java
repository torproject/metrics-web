package org.torproject.ernie.web;

import java.util.*;
import java.text.*;

public class CustomGraphController  {

  private Map<String, String[]> parameterMap;
  private Set<String> error;
  private String graphURI;
  private static SimpleDateFormat simpledf;

  static {
    simpledf = new SimpleDateFormat("yyyy-MM-dd");
    simpledf.setTimeZone(TimeZone.getTimeZone("UTC"));
  }

  /* Default constructor. */
  public CustomGraphController() {
    this.error = new HashSet<String>();
  }

  public void setParameterMap(Map<String, String[]> parameterMap) {
    this.parameterMap = parameterMap;
  }

  /**
   * Build a copy of the parameters passed to custom-graph.jsp to be
   * passed to one of the graph image servlets.
   * */
  public String getGraphURL() {
    try {
      Map<String, String[]> mapCopy =
          new HashMap<String, String[]>(parameterMap);
      String graphURI = "/" + mapCopy.get("graph")[0] + ".png";

      /*Make sure we don't pass this key/value to the servlet. */
      mapCopy.remove("graph");

      /* Make sure the user entered a valid date */
      if (!mapCopy.containsKey("start") || !mapCopy.containsKey("end")) {
        error.add("Invalid date format.");
        return "";
      }
      simpledf.parse(mapCopy.get("start")[0]);
      simpledf.parse(mapCopy.get("end")[0]);

      int i = 0;
      String uriChar;

      for (Map.Entry<String, String[]> entry : mapCopy.entrySet()) {
        uriChar = (i == 0) ? "?" : "&";
        graphURI += (uriChar + entry.getKey() + "=" + entry.getValue()[0]);
        i++;
      }
      return graphURI;
      /* All of the forms are empty */
    } catch (ParseException e)  {
      error.add("Invalid date format.");
      return "";
    }
  }

  public String getGraphName()  {
    return parameterMap.containsKey("graph") ?
        parameterMap.get("graph")[0] : "error";
  }
  public String getGraphStart() {
    return parameterMap.containsKey("start") ?
        parameterMap.get("start")[0] : "error";
  }
  public String getGraphEnd() {
    return parameterMap.containsKey("end") ?
        parameterMap.get("end")[0] : "error";
  }
  public Set<String> getError() {
    return this.error;
  }
}
