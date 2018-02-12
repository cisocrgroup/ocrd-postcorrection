package de.lmu.cis.iba;

import de.lmu.cis.ocrd.Document;
import de.lmu.cis.ocrd.Config;
import de.lmu.cis.pocoweb.Client;
import de.lmu.cis.pocoweb.Token;

// import de.lmu.cis.ocrd.Line;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import de.lmu.cis.api.model.Page;
import de.lmu.cis.api.model.Project;

import de.lmu.cis.api.model.Book;
import de.lmu.cis.iba.Alignments.*;

class Main {
  private static final int N = 3;

  static ArrayList<Node> sinks = new ArrayList();

  private static String patternsToString(String[] patterns) {
    String prefix = "[";
    String res = "";
    if (patterns == null || patterns.length == 0) {
      res += "[]";
    } else {
      for (String p : patterns) {
        res += prefix + p;
        prefix = ",";
      }
      res += "]";
    }
    return res;
  }

  private static HashMap sort_by_values_desc(HashMap map) {
    List list = new LinkedList(map.entrySet());
    // Defined Custom Comparator here
    Collections.sort(list, new Comparator() {
      public int compare(Object o1, Object o2) {
        return ((Comparable)((Map.Entry)(o2)).getValue())
            .compareTo(((Map.Entry)(o1)).getValue());
      }
    });

    HashMap sortedHashMap = new LinkedHashMap();
    for (Iterator it = list.iterator(); it.hasNext();) {
      Map.Entry entry = (Map.Entry)it.next();
      sortedHashMap.put(entry.getKey(), entry.getValue());
    }
    return sortedHashMap;
  }

  private static HashSet<Integer>
  find_n_transitions_to_sinks(Node node, Online_CDAWG_sym scdawg,
                              HashSet<Integer> acc) {

    Iterator it = node.children.entrySet().iterator();
    HashSet<Integer> result = new HashSet<Integer>();
    while (it.hasNext()) {
      Map.Entry pair = (Map.Entry)it.next();
      Node child = (Node)pair.getValue();
      for (int j = 0; j < scdawg.sinks.size(); j++) {
        if (scdawg.sinks.get(j) == child) {
          if (!sinks.contains(scdawg.sinks.get(j))) {
            for (int k = 0; k < scdawg.sinks.get(j).stringnumbers.size(); k++) {
              acc.add(scdawg.sinks.get(j).stringnumbers.get(k));
              // sinks.add(scdawg.sinks.get(j));
            }
          }
        }
      }
    }

    Iterator it2 = node.children_left.entrySet().iterator();

    while (it2.hasNext()) {

      Map.Entry pair = (Map.Entry)it2.next();
      Node child = (Node)pair.getValue();

      for (int j = 0; j < scdawg.sinks.size(); j++) {
        if (scdawg.sinks.get(j) == child) {
          if (!sinks.contains(scdawg.sinks.get(j))) {
            for (int k = 0; k < scdawg.sinks.get(j).stringnumbers.size(); k++) {
              acc.add(scdawg.sinks.get(j).stringnumbers.get(k));
              // sinks.add(scdawg.sinks.get(j));
            }
          }
        }
      }
    }

    // REC AUFRUF der Funktion mit den Kindern
    Iterator it3 = node.children.entrySet().iterator();

    while (it3.hasNext()) {

      Map.Entry pair = (Map.Entry)it3.next();
      Node child = (Node)pair.getValue();
      find_n_transitions_to_sinks(child, scdawg, acc);
    }

    Iterator it4 = node.children_left.entrySet().iterator();

    while (it4.hasNext()) {
      Map.Entry pair = (Map.Entry)it4.next();
      Node child = (Node)pair.getValue();
      find_n_transitions_to_sinks(child, scdawg, acc);
    }
    return acc;
  }

  private static void count_nodes(Node node, Online_CDAWG_sym scdawg,
                                  HashMap<Node, Integer> result) {

    // Count all right transitions

    Iterator it = node.children.entrySet().iterator();

    while (it.hasNext()) {

      Map.Entry pair = (Map.Entry)it.next();
      Node child = (Node)pair.getValue();

      if (scdawg.sinks.contains(child))
        continue;

      if (result.containsKey(child))
        result.put(child, (int)result.get(child) + 1);
      else {
        result.put(child, 1);
      }
    }

    // Count all left transitions

    Iterator it2 = node.children_left.entrySet().iterator();

    while (it2.hasNext()) {

      Map.Entry pair = (Map.Entry)it2.next();
      Node child = (Node)pair.getValue();

      if (scdawg.sinks.contains(child))
        continue;

      if (result.containsKey(child))
        result.put(child, (int)result.get(child) + 1);
      else {
        result.put(child, 1);
      }
    }

    // REC AUFRUF der Funktion mit den Kindern
    Iterator it3 = node.children.entrySet().iterator();

    while (it3.hasNext()) {

      Map.Entry pair = (Map.Entry)it3.next();
      Node child = (Node)pair.getValue();
      count_nodes(child, scdawg, result);
    }
  }

  public static void main(String[] args) {
    try (Client client = Client.login(Config.getInstance().getPocowebURL(),
                                      Config.getInstance().getPocowebUser(),
                                      Config.getInstance().getPocowebPass());) {
      Book book = new Book()
                      .withOcrEngine("abbyy")
                      .withOcrUser("test-ocr-user")
                      .withAuthor("Grenzboten")
                      .withTitle("Die Grenzboten")
                      .withYear(1841);
      Project project;
      try (InputStream is = new FileInputStream(
               "src/test/resources/1841-DieGrenzboten-abbyy-small.zip");) {
        project = client.newProject(book, is);
      }
      book = new Book()
                 .withOcrEngine("tesseract")
                 .withOcrUser("test-ocr-user")
                 .withAuthor("Grenzboten")
                 .withTitle("Die Grenzboten")
                 .withYear(1841);
      try (
          InputStream is = new FileInputStream(
              "src/test/resources/1841-DieGrenzboten-tesseract-small-with-error.zip");) {
        project = client.addBook(project, book, is);
      }
      book = new Book()
                 .withOcrEngine("ocropus")
                 .withOcrUser("test-ocr-user")
                 .withAuthor("Grenzboten")
                 .withTitle("Die Grenzboten")
                 .withYear(1841);
      try (InputStream is = new FileInputStream(
               "src/test/resources/1841-DieGrenzboten-ocropus-small.zip");) {
        project = client.addBook(project, book, is);
      }
      Document doc = new Document(project, client);

      LineAlignment l_alignment = Alignments.alignLines(doc, 3);

      client.deleteProject(project);
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("error: " + e);
    }
  }
}
