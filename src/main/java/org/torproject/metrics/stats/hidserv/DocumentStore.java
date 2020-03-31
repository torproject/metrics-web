/* Copyright 2016--2020 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.stats.hidserv;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.lang.reflect.InvocationTargetException;
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

  private static final Logger logger
      = LoggerFactory.getLogger(DocumentStore.class);

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
      logger.warn("Unable to read and update {}. Not storing documents.",
          documentFile.getAbsoluteFile());
      return false;
    }

    /* Merge new documents with existing ones. */
    retrievedDocuments.addAll(documentsToStore);

    /* Serialize documents. */
    SortedMap<String, SortedSet<String>> formattedDocuments = new TreeMap<>();
    for (T retrieveDocument : retrievedDocuments) {
      String[] formattedDocument = retrieveDocument.format();
      formattedDocuments.putIfAbsent(formattedDocument[0], new TreeSet<>());
      formattedDocuments.get(formattedDocument[0]).add(
          formattedDocument[1]);
    }

    /* Check if a temporary file exists from the previous execution. */
    File documentTempFile = new File(documentFile.getAbsoluteFile()
        + ".tmp");
    if (documentTempFile.exists()) {
      logger.warn("Temporary document file {} still exists, "
          + "indicating that a previous execution did not terminate "
          + "cleanly.  Not storing documents.",
          documentTempFile.getAbsoluteFile());
      return false;
    }

    /* Write to a new temporary file, then move it into place, possibly
     * overwriting an existing file. */
    documentTempFile.getParentFile().mkdirs();
    try (BufferedWriter bw = new BufferedWriter(new FileWriter(
        documentTempFile))) {
      for (Map.Entry<String, SortedSet<String>> e
          : formattedDocuments.entrySet()) {
        bw.write(e.getKey() + "\n");
        for (String s : e.getValue()) {
          bw.write(" " + s + "\n");
        }
      }
      documentFile.delete();
      documentTempFile.renameTo(documentFile);
    } catch (IOException e) {
      logger.warn("Unable to write {}. Not storing documents.",
          documentFile.getAbsolutePath(), e);
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
    Set<T> result = new HashSet<>();
    if (!documentFile.exists()) {
      return result;
    }

    /* Parse the document file line by line and de-serialize contained
     * documents. */
    try (LineNumberReader lnr = new LineNumberReader(new BufferedReader(
        new FileReader(documentFile)))) {
      String line;
      String formattedString0 = null;
      while ((line = lnr.readLine()) != null) {
        if (!line.startsWith(" ")) {
          formattedString0 = line;
        } else if (formattedString0 == null) {
          logger.warn("First line in {} must not start with a space. Not "
              + "retrieving any previously stored documents.",
              documentFile.getAbsolutePath());
          return null;
        } else if (prefix.length() > formattedString0.length()
            && !(formattedString0 + line.substring(1))
            .startsWith(prefix)) {
          /* Skip combined line not starting with prefix. */
        } else if (prefix.length() > 0
            && !formattedString0.startsWith(prefix)) {
          /* Skip line not starting with prefix. */
        } else {
          T document = this.clazz.getDeclaredConstructor().newInstance();
          if (!document.parse(new String[] { formattedString0,
              line.substring(1) })) {
            logger.warn("Unable to read line {} from {}. Not retrieving any "
                + "previously stored documents.", lnr.getLineNumber(),
                documentFile.getAbsolutePath());
            return null;
          }
          result.add(document);
        }
      }
    } catch (IOException e) {
      logger.warn("Unable to read {}. Not retrieving any previously stored "
          + "documents.", documentFile.getAbsolutePath(), e);
      return null;
    } catch (InstantiationException | IllegalAccessException
        | NoSuchMethodException | InvocationTargetException e) {
      logger.warn("Unable to read {}. Cannot instantiate document object.",
          documentFile.getAbsolutePath(), e);
      return null;
    }
    return result;
  }
}

