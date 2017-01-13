/* Copyright 2016--2017 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.hidserv;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/** Utility class to store serialized objects implementing the Document
 * interface to a file and later to retrieve them. */
public class DocumentStore<T extends Document> {

  /** Document class, needed to create new instances when retrieving
   * documents. */
  private Class<T> clazz;

  /** Initializes a new store object for the given type of documents. */
  DocumentStore(Class<T> clazz) {
    this.clazz = clazz;
  }

  /** Stores the provided documents in the given file and returns whether
   * the storage operation was successful.
   *
   * <p>If the file already existed and if it contains documents, merge
   * the new documents with the existing ones.</p> */
  public boolean store(File documentFile, Set<T> documentsToStore) {

    /* Retrieve existing documents. */
    Set<T> retrievedDocuments = this.retrieve(documentFile);
    if (retrievedDocuments == null) {
      System.err.printf("Unable to read and update %s.  Not storing "
          + "documents.%n", documentFile.getAbsoluteFile());
      return false;
    }

    /* Merge new documents with existing ones. */
    retrievedDocuments.addAll(documentsToStore);

    /* Serialize documents. */
    SortedMap<String, SortedSet<String>> formattedDocuments =
        new TreeMap<String, SortedSet<String>>();
    for (T retrieveDocument : retrievedDocuments) {
      String[] formattedDocument = retrieveDocument.format();
      if (!formattedDocuments.containsKey(formattedDocument[0])) {
        formattedDocuments.put(formattedDocument[0],
            new TreeSet<String>());
      }
      formattedDocuments.get(formattedDocument[0]).add(
          formattedDocument[1]);
    }

    /* Check if a temporary file exists from the previous execution. */
    File documentTempFile = new File(documentFile.getAbsoluteFile()
        + ".tmp");
    if (documentTempFile.exists()) {
      System.err.printf("Temporary document file %s still exists, "
          + "indicating that a previous execution did not terminate "
          + "cleanly.  Not storing documents.%n",
          documentTempFile.getAbsoluteFile());
      return false;
    }

    /* Write to a new temporary file, then move it into place, possibly
     * overwriting an existing file. */
    try {
      documentTempFile.getParentFile().mkdirs();
      BufferedWriter bw = new BufferedWriter(new FileWriter(
          documentTempFile));
      for (Map.Entry<String, SortedSet<String>> e
          : formattedDocuments.entrySet()) {
        bw.write(e.getKey() + "\n");
        for (String s : e.getValue()) {
          bw.write(" " + s + "\n");
        }
      }
      bw.close();
      documentFile.delete();
      documentTempFile.renameTo(documentFile);
    } catch (IOException e) {
      System.err.printf("Unable to write %s.  Not storing documents.%n",
          documentFile.getAbsolutePath());
      return false;
    }

    /* Return success. */
    return true;
  }

  /** Retrieves all previously stored documents from the given file. */
  public Set<T> retrieve(File documentFile) {
    return this.retrieve(documentFile, "");
  }

  /** Retrieves previously stored documents from the given file that start
   * with the given prefix. */
  public Set<T> retrieve(File documentFile, String prefix) {

    /* Check if the document file exists, and if not, return an empty set.
     * This is not an error case. */
    Set<T> result = new HashSet<T>();
    if (!documentFile.exists()) {
      return result;
    }

    /* Parse the document file line by line and de-serialize contained
     * documents. */
    try {
      LineNumberReader lnr = new LineNumberReader(new BufferedReader(
          new FileReader(documentFile)));
      String line;
      String formattedString0 = null;
      while ((line = lnr.readLine()) != null) {
        if (!line.startsWith(" ")) {
          formattedString0 = line;
        } else if (formattedString0 == null) {
          System.err.printf("First line in %s must not start with a "
              + "space.  Not retrieving any previously stored "
              + "documents.%n", documentFile.getAbsolutePath());
          lnr.close();
          return null;
        } else if (prefix.length() > formattedString0.length()
            && !(formattedString0 + line.substring(1))
            .startsWith(prefix)) {
          /* Skip combined line not starting with prefix. */
          continue;
        } else if (prefix.length() > 0
            && !formattedString0.startsWith(prefix)) {
          /* Skip line not starting with prefix. */
          continue;
        } else {
          T document = this.clazz.newInstance();
          if (!document.parse(new String[] { formattedString0,
              line.substring(1) })) {
            System.err.printf("Unable to read line %d from %s.  Not "
                + "retrieving any previously stored documents.%n",
                lnr.getLineNumber(), documentFile.getAbsolutePath());
            lnr.close();
            return null;
          }
          result.add(document);
        }
      }
      lnr.close();
    } catch (IOException e) {
      System.err.printf("Unable to read %s.  Not retrieving any "
          + "previously stored documents.%n",
          documentFile.getAbsolutePath());
      e.printStackTrace();
      return null;
    } catch (InstantiationException e) {
      System.err.printf("Unable to read %s.  Cannot instantiate document "
          + "object.%n", documentFile.getAbsolutePath());
      e.printStackTrace();
      return null;
    } catch (IllegalAccessException e) {
      System.err.printf("Unable to read %s.  Cannot instantiate document "
          + "object.%n", documentFile.getAbsolutePath());
      e.printStackTrace();
      return null;
    }
    return result;
  }
}

