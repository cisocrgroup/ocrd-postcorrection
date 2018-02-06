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

class Main {

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

  private static void find_n_transitions_to_sinks(Node node,
                                                  Online_CDAWG_sym scdawg) {

    Iterator it = node.children.entrySet().iterator();

    while (it.hasNext()) {

      Map.Entry pair = (Map.Entry)it.next();
      Node child = (Node)pair.getValue();

      for (int j = 0; j < scdawg.sinks.size(); j++) {
        if (scdawg.sinks.get(j) == child) {
          if (!sinks.contains(scdawg.sinks.get(j)))
            sinks.add(scdawg.sinks.get(j));
        }
      }
    }

    Iterator it2 = node.children_left.entrySet().iterator();

    while (it2.hasNext()) {

      Map.Entry pair = (Map.Entry)it2.next();
      Node child = (Node)pair.getValue();

      for (int j = 0; j < scdawg.sinks.size(); j++) {
        if (scdawg.sinks.get(j) == child) {
          if (!sinks.contains(scdawg.sinks.get(j)))
            sinks.add(scdawg.sinks.get(j));
        }
      }
    }

    // REC AUFRUF der Funktion mit den Kindern
    Iterator it3 = node.children.entrySet().iterator();

    while (it3.hasNext()) {

      Map.Entry pair = (Map.Entry)it3.next();
      Node child = (Node)pair.getValue();
      find_n_transitions_to_sinks(child, scdawg);
    }

    Iterator it4 = node.children_left.entrySet().iterator();

    while (it4.hasNext()) {
      Map.Entry pair = (Map.Entry)it4.next();
      Node child = (Node)pair.getValue();
      find_n_transitions_to_sinks(child, scdawg);
    }
  }

  private static void count_nodes(int n, Node node, Online_CDAWG_sym scdawg,
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
      count_nodes(n, child, scdawg, result);
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

      ArrayList<String> stringset = new ArrayList<String>();
      ArrayList<String> ids = new ArrayList<String>();
      doc.eachLine(new Document.Visitor() {
        public void visit(Document.LineTriple t) throws Exception {
          System.out.println(String.format("[%9s,%1d,%2d] %s", t.ocrEngine,
                                           t.pageSeq, t.line.getLineId(),
                                           t.line.getNormalized()));
          for (Token token : t.line.getTokens()) {
            System.out.println(String.format(
                "[token %2d] %s", token.getTokenId(), token.getCor()));
          }
          stringset.add("#" + t.line.getNormalized() + "$");
          ids.add(String.format("[%d,%d,%s]", t.pageSeq, t.line.getLineId(),
                                t.ocrEngine));
        }
      });

      for (int i = 0; i < stringset.size(); i++) {
        System.out.println(stringset.get(i) + ": " + ids.get(i));
      }

      Online_CDAWG_sym scdawg = new Online_CDAWG_sym(stringset, false);
      scdawg.determineAlphabet(true);
      scdawg.build_cdawg();
      // scdawg.print_automaton("svgs/scdawg");

      HashMap nodes_count = new HashMap<Node, Integer>();

      count_nodes(3, scdawg.root, scdawg, nodes_count);

      HashMap count_nodes_sorted = sort_by_values_desc(nodes_count);
      ArrayList<ArrayList> nodes_sink_set = new ArrayList();

      Iterator it3 = count_nodes_sorted.entrySet().iterator();

      while (it3.hasNext()) {
        Map.Entry pair = (Map.Entry)it3.next();

        Node n = (Node)pair.getKey();

        // System.out.println(scdawg.get_node_label((Node)pair.getKey())+ "
        // "+pair.getValue());

        sinks = new ArrayList();

        find_n_transitions_to_sinks(n, scdawg);
        // System.out.println(scdawg.get_node_label((Node)pair.getKey())+"
        // "+sinks.size());

        if (sinks.size() != 3)
          continue;

        ArrayList<Node> sink_set = new ArrayList();

        for (int i = 0; i < sinks.size(); i++) {
          sink_set.add(sinks.get(i));
        }

        if (nodes_sink_set.size() == 0)
          nodes_sink_set.add(new ArrayList() {
            {
              add(n);
              add(sink_set);
            }
          });
        else {
          boolean node_found = false;
          for (int i = 0; i < nodes_sink_set.size(); i++) {

            ArrayList pair2 = nodes_sink_set.get(i);

            for (int j = 0; j < sink_set.size(); j++) {
              ArrayList<Node> known_sinks =
                  (ArrayList)nodes_sink_set.get(i).get(1);
              for (int k = 0; k < known_sinks.size(); k++)

                if (known_sinks.get(k).equals(sink_set.get(j))) {
                  node_found = true;
                }
            }
          }
          if (!node_found)
            nodes_sink_set.add(new ArrayList() {
              {
                add(n);
                add(sink_set);
              }
            });
        }
      }

      // ArrayList<String> xyz = new ArrayList<String>(stringset.size());
      String[] xyz = new String[stringset.size()];
      for (int i = 0; i < nodes_sink_set.size(); i++) {
        ArrayList pair = nodes_sink_set.get(i);
        System.out.println(scdawg.get_node_label((Node)pair.get(0)));
        for (int j = 0; j < ((ArrayList)pair.get(1)).size(); j++) {
          int idx = ((Node)((ArrayList)pair.get(1)).get(j)).stringnr;
          System.out.println(" - " + stringset.get(idx) + ": " + ids.get(idx));
          xyz[idx] = stringset.get(idx);
        }
        System.out.println();
      }
      for (int i = 0; i < xyz.length; i++) {
        if (xyz[i] != null) {
          System.out.print(xyz[i]);
        } else {
          System.out.print("NULL");
        }
        System.out.println(" " + stringset.get(i));
      }

      client.deleteProject(project);
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("error: " + e);
    }
  }
}
