package de.lmu.cis.iba;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

// import cdawg_sym.Online_CDAWG_sym;
// import indexstructure.Node;
// import util.Util;

public class Pairwise_LCS_Alignment {
  Online_CDAWG_sym scdawg;
  ArrayList<ArrayList<LCS_Triple>> longest_common_subsequences =
      new ArrayList<ArrayList<LCS_Triple>>();

  public Pairwise_LCS_Alignment(ArrayList<String> stringset) {
    Online_CDAWG_sym scdawg = new Online_CDAWG_sym(stringset, false);
    scdawg.determineAlphabet(false);
    scdawg.build_cdawg();

    this.scdawg = scdawg;
  }

  public void align() {
    System.out.println("Searching quasi max nodes for s1 and s2 pairs...");
    ArrayList<Endpos_Pair> quasi_max_nodes = find_quasi_max_nodes_pairwise();

    System.out.println("Calculating LCS for s1 and s2 pairs...");

    for (Endpos_Pair pair : quasi_max_nodes) {
      longest_common_subsequences.add(
          calculate_LCS(pair.nodes_in_s1, pair.nodes_endpos_s2));
    }
  }

  // *******************************************************************************
  // calculate_LCS()
  // *******************************************************************************

  public ArrayList<LCS_Triple> calculate_LCS(
      Node[] nodes_in_s1, HashMap<Node, ArrayList> nodes_endpos_s2) {
    // build greedy cover

    ArrayList[] greedy_cover = new ArrayList[nodes_in_s1.length];

    ArrayList<LCS_Triple> lcs_triples = new ArrayList<LCS_Triple>();

    for (int i = 0; i < nodes_in_s1.length; i++)
      greedy_cover[i] = new ArrayList();

    boolean first_node_found = false;

    for (int i = 0; i < nodes_in_s1.length; i++) {
      if (nodes_in_s1[i] != null) {
        // System.out.println(scdawg.get_node_label(nodes_in_s1[i])+ i+
        // nodes_endpos_s2.get(nodes_in_s1[i]) );

        if (nodes_endpos_s2.get(nodes_in_s1[i]) == null)
          continue;  // Wenn keine Endpos im zweiten String dann nix machen

        for (int j = nodes_endpos_s2.get(nodes_in_s1[i]).size() - 1; j >= 0;
             j--) {  // iterieren über alle
                     // dec sequences ;//
                     // HIER EIN TRIPEL! //
                     // erstes paar abc;4;15
                     // abc;4;4 mn;7;8
                     // abc;11;15 abc;11;4
          int dec_elem = (int)nodes_endpos_s2.get(nodes_in_s1[i]).get(j);

          lcs_triples.add(new LCS_Triple(i, dec_elem, nodes_in_s1[i]));
          int lcs_index = lcs_triples.size() - 1;

          if ((j == nodes_endpos_s2.get(nodes_in_s1[i]).size() - 1) &
              !first_node_found) {  // erstes
                                    // element
                                    // immer
                                    // hinzufügen
                                    // // HIER
                                    // NUN DIE
                                    // INDIZES
                                    // ANSTATT
                                    // DER WERTE
                                    // ABER
                                    // IMMER MIT
                                    // TRIPEL
            greedy_cover[0].add(lcs_index);
            first_node_found = true;
          }

          else {
            for (int k = 0; k < greedy_cover.length;
                 k++) {  // alle cover listen durchgehen

              if (greedy_cover[k].size() == 0) {
                greedy_cover[k].add(lcs_index);
                break;
              }

              else if (dec_elem < lcs_triples
                                      .get((int)greedy_cover[k].get(
                                          greedy_cover[k].size() - 1))
                                      .endpos_s2) {  // wenn
                                                     // kleiner
                                                     // als
                                                     // das
                                                     // letzte
                                                     // dann
                                                     // hinzufügen
                greedy_cover[k].add(lcs_index);
                break;
              }
            }
          }
        }
      }
    }

    // calculate lis

    int i_count = 0;
    for (int i = 0; i < greedy_cover.length; i++) {
      if (greedy_cover[i].size() > 0) i_count++;
    }

    ArrayList<LCS_Triple> lis = new ArrayList<LCS_Triple>();
    int x = lcs_triples.get((int)greedy_cover[i_count - 1].get(0))
                .endpos_s2;  // pick any??
    lis.add(lcs_triples.get((int)greedy_cover[i_count - 1].get(0)));

    i_count -= 2;

    while (i_count >= 0) {
      for (int j = 0; j < greedy_cover[i_count].size(); j++) {
        if (lcs_triples.get((int)greedy_cover[i_count].get(j)).endpos_s2 < x) {
          lis.add(0, lcs_triples.get((int)greedy_cover[i_count].get(j)));
          x = lcs_triples.get((int)greedy_cover[i_count].get(j)).endpos_s2;
          break;
        }
      }

      i_count--;
    }
    return lis;
  }

  // *******************************************************************************
  // find_quasi_max_nodes_pairwise()
  // simple quasi-max definition = direct left + direct right edge to sink
  // for start and end two left or two right transitions
  // *******************************************************************************

  public ArrayList<Endpos_Pair> find_quasi_max_nodes_pairwise() {
    ArrayList<Endpos_Pair> result = new ArrayList();

    for (int u = 0; u < scdawg.stringset.size() - 1; u++) {
      // nur für ersten string??? Array länge aus allen positionen immer knoten

      // dann iterieren auf pos und wenn konten gefunden => pos in zweiten
      // string => paare in s2 (s1,s2)..

      Node[] nodes_in_s1 = new Node[scdawg.stringset.get(u).length()];

      HashMap<Node, ArrayList> nodes_endpos_s2 = new HashMap();

      for (int i = 0; i < scdawg.all_nodes.size(); i++) {
        Node node = scdawg.all_nodes.get(i);

        // all nodes except first and last alignment part

        Iterator it = node.children.entrySet().iterator();

        while (it.hasNext()) {
          Map.Entry pair = (Map.Entry)it.next();
          Node child = (Node)pair.getValue();

          int letter = (int)pair.getKey();

          for (int j = 0; j < scdawg.sinks.size(); j++) {
            if (scdawg.sinks.get(j) == child) {  // wenn rechtsübergang auf sink

              Iterator it2 = node.children_left.entrySet().iterator();

              while (it2.hasNext()) {
                Map.Entry pair_left = (Map.Entry)it2.next();
                Node child_left = (Node)pair_left.getValue();

                if (child_left == child) {
                  int endpos = scdawg.get_node_length(child) -
                               scdawg.get_edge_length(letter, node, child) - 1;

                  if (node.start == -1) continue;

                  if (scdawg.sinks.get(j).stringnr == u)
                    nodes_in_s1[endpos] = node;
                  if (scdawg.sinks.get(j).stringnr == u + 1) {
                    if (nodes_endpos_s2.containsKey(node)) {
                      if (!nodes_endpos_s2.get(node).contains(endpos)) {
                        nodes_endpos_s2.get(node).add(endpos);
                      }

                    } else {
                      nodes_endpos_s2.put(node, new ArrayList());

                      nodes_endpos_s2.get(node).add(endpos);
                    }
                  }
                }
              }
            }
          }
        }

        // last alignment part

        if (scdawg.get_node_label(node).endsWith("$") &
            !scdawg.get_node_label(node).startsWith("#")) {
          for (int j = 0; j < scdawg.sinks.size(); j++) {
            Iterator it2 = node.children_left.entrySet().iterator();

            while (it2.hasNext()) {
              Map.Entry pair_left = (Map.Entry)it2.next();
              Node child_left = (Node)pair_left.getValue();

              int letter = (int)pair_left.getKey();

              if (child_left == scdawg.sinks.get(j)) {
                int endpos =
                    scdawg.sinks.get(j).start +
                    scdawg.get_edge_label_left(letter, node, child_left)
                        .length() +
                    scdawg.get_node_length(node) - 1;

                if (scdawg.sinks.get(j).stringnr == u)
                  nodes_in_s1[endpos] = node;
                if (scdawg.sinks.get(j).stringnr == u + 1) {
                  if (nodes_endpos_s2.containsKey(node)) {
                    if (!nodes_endpos_s2.get(node).contains(endpos)) {
                      nodes_endpos_s2.get(node).add(endpos);
                    }

                  } else {
                    nodes_endpos_s2.put(node, new ArrayList());

                    nodes_endpos_s2.get(node).add(endpos);
                  }
                }
              }

            }  // while

          }  // for sinks

        }  // last alignment part

        // first alignment part

        if (scdawg.get_node_label(node).startsWith("#") &
            !scdawg.get_node_label(node).startsWith("$")) {
          for (int j = 0; j < scdawg.sinks.size(); j++) {
            Iterator it2 = node.children.entrySet().iterator();

            while (it2.hasNext()) {
              Map.Entry pair = (Map.Entry)it2.next();
              Node child = (Node)pair.getValue();

              int letter = (int)pair.getKey();

              if (child == scdawg.sinks.get(j)) {
                int endpos =
                    scdawg.get_node_length(scdawg.sinks.get(j)) -
                    scdawg.get_edge_length(letter, node, scdawg.sinks.get(j)) -
                    1;

                if (scdawg.sinks.get(j).stringnr == u)
                  nodes_in_s1[endpos] = node;
                if (scdawg.sinks.get(j).stringnr == u + 1) {
                  if (nodes_endpos_s2.containsKey(node)) {
                    if (!nodes_endpos_s2.get(node).contains(endpos)) {
                      nodes_endpos_s2.get(node).add(endpos);
                    }

                  } else {
                    nodes_endpos_s2.put(node, new ArrayList());

                    nodes_endpos_s2.get(node).add(endpos);
                  }
                }
              }

            }  // while

          }  // for sinks

        }  // first alignment part

      }  // all Nodes

      result.add(new Endpos_Pair(nodes_in_s1, nodes_endpos_s2));

    }  // for u

    return result;
  }

  // *******************************************************************************
  // LCS_to_JSONString()
  // *******************************************************************************

  public String LCS_to_JSONString() {
    String result = "{";

    for (int u = 0; u < this.longest_common_subsequences.size(); u++) {
      ArrayList<LCS_Triple> lis = this.longest_common_subsequences.get(u);

      String alignment = "[";

      String d = "";

      for (int i = 0; i < lis.size(); i++) {
        // System.out.println(lis.get(i).endpos_s1+" "+lis.get(i).endpos_s2+"
        // "+scdawg.get_node_label(lis.get(i).node));
        String nodelabel =
            scdawg.get_node_label(lis.get(i).node).replace("\"", "\\\"");
        alignment += d + "{\"endpos_s1\":\"" + lis.get(i).endpos_s1 +
                     "\",\"endpos_s2\":\"" + lis.get(i).endpos_s2 +
                     "\",\"nodelabel\":\"" + nodelabel + "\"}";
        d = ",";
      }

      alignment += "]";

      result += "\"alignment" + u + "\":" + alignment;
      if (u < scdawg.stringset.size() - 2) result += ",";

    }  // for u

    return result + "}";
  }

  // *******************************************************************************
  // *******************************************************************************
  // New Methods find quasi maximal Nodes ::::=>
  // *******************************************************************************
  // *******************************************************************************

  public void align_new() {
    HashSet<Node> candidates = this.find_quasi_max_nodes();

    HashMap<Node, Integer> result = new HashMap<Node, Integer>();

    ArrayList<NavigableMap<Integer, Node>> filtered_candidates =
        this.filter_quasi_max_nodes(candidates);
    ArrayList<NavigableMap<Integer, Node>> reduced_candidates =
        this.remove_included_substrings(filtered_candidates);

    System.out.println("---------------------------------");

    for (int i = 0; i < reduced_candidates.size(); i++) {
      for (Map.Entry<Integer, Node> entry :
           reduced_candidates.get(i).entrySet()) {
        Node currentNode = entry.getValue();
        int endpos_n1 =
            scdawg.get_node_length(currentNode) + entry.getKey() - 1;
        System.out.println(
            scdawg.get_node_label((Node)entry.getValue()) + " " +
            entry.getKey() + " " +
            (scdawg.get_node_length(currentNode) + entry.getKey() - 1));
      }
      System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXX");
    }
  }

  public boolean has_transitions_to_sinks(Node node) {
    Iterator it = node.children.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry pair = (Map.Entry)it.next();
      Node child = (Node)pair.getValue();
      for (Node sink : scdawg.sinks) {
        if (child.equals(sink)) return true;
      }
    }

    Iterator it2 = node.children_left.entrySet().iterator();
    while (it2.hasNext()) {
      Map.Entry pair = (Map.Entry)it2.next();
      Node child = (Node)pair.getValue();
      for (Node sink : scdawg.sinks) {
        if (child.equals(sink)) return true;
      }
    }

    return false;
  }

  public HashSet<Node> find_quasi_max_nodes() {
    HashSet<Node> candidates = new HashSet<Node>();

    for (int i = 0; i < scdawg.all_nodes.size(); i++) {
      Node node = scdawg.all_nodes.get(i);
      if (node == scdawg.root) continue;
      if (has_transitions_to_sinks(node)) candidates.add(node);
    }

    // for(Node n : candidates) {
    // System.out.println(scdawg.get_node_label(n));
    // }

    return candidates;
  }

  public ArrayList<NavigableMap<Integer, Node>> filter_quasi_max_nodes(
      HashSet<Node> candidates) {
    ArrayList<NavigableMap<Integer, Node>> result =
        new ArrayList<NavigableMap<Integer, Node>>();

    for (int i = 0; i < scdawg.stringset.size(); i++) {
      result.add(new TreeMap<Integer, Node>());
    }

    for (Node n : candidates) {
      Iterator it2 = n.children.entrySet().iterator();
      while (it2.hasNext()) {
        Map.Entry pair = (Map.Entry)it2.next();

        for (Node sink : scdawg.sinks) {
          Node child = (Node)pair.getValue();
          if (sink.equals(child)) {
            int pos = scdawg.stringset.get(child.stringnr).length() -
                      (scdawg.get_node_length(n) +
                       scdawg.get_edge_length((int)pair.getKey(), n, child));

            if (!result.get(child.stringnr).containsKey(pos)) {
              result.get(child.stringnr).put(pos, n);
            } else {
              if (scdawg.get_node_length(result.get(child.stringnr).get(pos)) <
                  scdawg.get_node_length(n)) {
                result.get(child.stringnr).put(pos, n);
              }
            }
          }
        }
      }

      // LEFT EDGES
      //
      Iterator it3 = n.children_left.entrySet().iterator();
      while (it3.hasNext()) {
        Map.Entry pair = (Map.Entry)it3.next();

        for (Node sink : scdawg.sinks) {
          Node child = (Node)pair.getValue();
          if (sink.equals(child)) {
            int pos = scdawg.get_edge_label_left((int)pair.getKey(), n, child)
                          .length();

            if (!result.get(child.stringnr).containsKey(pos)) {
              result.get(child.stringnr).put(pos, n);
            } else {
              if (scdawg.get_node_length(result.get(child.stringnr).get(pos)) <
                  scdawg.get_node_length(n)) {
                result.get(child.stringnr).put(pos, n);
              }
            }
          }
        }
      }

      for (NavigableMap<Integer, Node> map : result) {
        map = (NavigableMap<Integer, Node>)Util.sortByKeys(map);
      }
    }

    return result;
  }

  public ArrayList<NavigableMap<Integer, Node>> remove_included_substrings(
      ArrayList<NavigableMap<Integer, Node>> filtered_candidates) {
    ArrayList<NavigableMap<Integer, Node>> result =
        new ArrayList<NavigableMap<Integer, Node>>();

    for (int i = 0; i < scdawg.stringset.size(); i++) {
      result.add(new TreeMap<Integer, Node>());
    }

    for (int i = 0; i < filtered_candidates.size(); i++) {
      Node n1 = null;
      boolean found = false;

      Map.Entry<Integer, Node> currentPair = null;
      Map.Entry<Integer, Node> comparePair = null;

      for (Map.Entry<Integer, Node> entry :
           filtered_candidates.get(i).entrySet()) {
        Map.Entry<Integer, Node> prev =
            filtered_candidates.get(i).lowerEntry(entry.getKey());  // previous

        if (prev == null) {
          currentPair = entry;

          result.get(i).put(currentPair.getKey(), currentPair.getValue());
          found = false;
        }

        else {
          comparePair = entry;
          int endpos_n2 = scdawg.get_node_length(comparePair.getValue()) +
                          comparePair.getKey() - 1;
          int endpos_n1 = scdawg.get_node_length(currentPair.getValue()) +
                          currentPair.getKey() - 1;

          if (endpos_n2 > endpos_n1) {
            currentPair = entry;
            result.get(i).put(currentPair.getKey(), currentPair.getValue());
          }
        }
      }
    }
    return result;
  }
}
