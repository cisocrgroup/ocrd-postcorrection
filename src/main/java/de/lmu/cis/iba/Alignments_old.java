package de.lmu.cis.iba;

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

import de.lmu.cis.ocrd.Document;
import de.lmu.cis.ocrd.Document.OCRLine;
import de.lmu.cis.pocoweb.Token;

public class Alignments_old {

  static ArrayList<Node> sinks = new ArrayList<Node>();

  private static class pair {
    public Node node;
    public HashSet<Integer> ids;
  }

  public static class LineAlignment extends ArrayList<ArrayList<OCRLine>> {
    public LineAlignment() { super(); }
  }

  public Alignments_old() {}

  private static HashMap sort_by_values_desc(HashMap<Node, Integer> map) {
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

  public static LineAlignment alignLines(Document doc, int nlines)
      throws Exception {
    if (nlines <= 0) {
      throw new Exception("cannot allign " + nlines + " lines");
    }

    ArrayList<String> stringset = new ArrayList<String>();
    ArrayList<String> strids = new ArrayList<String>();
    ArrayList<OCRLine> ocrlines = new ArrayList<OCRLine>();

    LineAlignment result = new LineAlignment();

    doc.eachLine(new Document.Visitor() {
      public void visit(Document.OCRLine l) throws Exception {
        System.out.println(String.format("[%9s,%1d,%2d] %s", l.ocrEngine,
                                         l.pageSeq, l.line.getLineId(),
                                         l.line.getNormalized()));
        for (Token token : l.line.getTokens()) {
          System.out.println(String.format("[token %2d] %s", token.getTokenId(),
                                           token.getCor()));
        }
        stringset.add("#" + l.line.getNormalized() + "$");
        strids.add(String.format("[%d,%d,%s]", l.pageSeq, l.line.getLineId(),
                                 l.ocrEngine));
        ocrlines.add(l);
      }
    });

    Online_CDAWG_sym scdawg = new Online_CDAWG_sym(stringset, false);
    scdawg.determineAlphabet(false);
    scdawg.build_cdawg();
    // scdawg.print_automaton("svgs/scdawg");

    HashMap<Node, Integer> nodes_count = new HashMap<Node, Integer>();

    count_nodes(scdawg.root, scdawg, nodes_count);

    HashMap count_nodes_sorted = sort_by_values_desc(nodes_count);
    ArrayList<pair> nodes_sink_set = new ArrayList<pair>();

    count_nodes_sorted.put(scdawg.root, null);
    Iterator it3 = count_nodes_sorted.entrySet().iterator();

    HashSet<Integer> usedIDs = new HashSet<Integer>();
  main_loop:
    while (it3.hasNext()) {
      Map.Entry pair = (Map.Entry)it3.next();

      Node n = (Node)pair.getKey();

      HashSet<Integer> ids =
          find_n_transitions_to_sinks(n, scdawg, new HashSet<Integer>());

      if (ids.size() != nlines) {
        continue;
      }
      for (Integer id : ids) {
        if (usedIDs.contains(id)) {
          continue main_loop;
        }
      }
      for (Integer id : ids) {
        usedIDs.add(id);
      }
      pair p = new pair();
      p.ids = ids;
      p.node = n;
      nodes_sink_set.add(p);
    }
    // handle final nodes (special case if all ocrs are identical)
    for (Node sink : scdawg.sinks) {
      if (sink.stringnumbers.size() == nlines) {
        // it is impossilbe (?) that this node was used before
        // System.out.println("got sink with " + N + " sinks");
        // System.out.println(sink.stringnumbers);
        pair p = new pair();
        p.ids = new HashSet<Integer>();
        for (Integer id : sink.stringnumbers) {
          p.ids.add(id);
        }
        p.node = scdawg.root;
        nodes_sink_set.add(p);
      }
    }

    // ArrayList<String> xyz = new ArrayList<String>(stringset.size());
    String[] xyz = new String[stringset.size()];
    for (pair p : nodes_sink_set) {
      System.out.println(scdawg.get_node_label(p.node));
      System.out.println(p.ids);
      ArrayList<Document.OCRLine> linetupel = new ArrayList<Document.OCRLine>();
      for (Integer id : p.ids) {
        int idx = id;

        linetupel.add(ocrlines.get(idx));

        System.out.println("- " + stringset.get(idx) + ": " + strids.get(idx));
        xyz[idx] = stringset.get(idx);
      }
      result.add(linetupel);
      System.out.println();
    }
    // for (int i = 0; i < xyz.length; i++) {
    // if (xyz[i] != null) {
    // System.out.print(xyz[i]);
    // } else {
    // System.out.print("NULL");
    // }
    // System.out.println(" " + stringset.get(i));
    // }

    return result;
  }
}
