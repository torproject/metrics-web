package org.torproject.metrics.hidserv;

/* Common interface of documents that are supposed to be serialized and
 * stored in document files and later retrieved and de-serialized. */
public interface Document {

  /* Return an array of two strings with a string representation of this
   * document.  The first string will be used to start a group of
   * documents, the second string will be used to represent a single
   * document in that group.  Ideally, the first string is equivalent for
   * many documents stored in the same file, and the second string is
   * different for those documents. */
  public String[] format();

  /* Initialize an object using the given array of two strings.  These are
   * the same two strings that the format method provides. */
  public boolean parse(String[] formattedStrings);
}

