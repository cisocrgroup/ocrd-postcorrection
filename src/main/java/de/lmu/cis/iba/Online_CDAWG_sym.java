package de.lmu.cis.iba;

import java.awt.List;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

// import indexstructure.Neo4J_Handler;

public class Online_CDAWG_sym extends IndexStructure {

  ActivePoint ap;

  public Node root, sink, suffixstate, old_suffixstate, split;

  public ArrayList<Node> all_nodes;
  public ArrayList<Node> sinks = new ArrayList();

  ArrayList<HashMap> quasi_candidates = new ArrayList();

  int id_cnt;
  int split_cnt = 0;

  public String input_text;
  boolean letters_available;

  int stringcount;

  int pos = 0;

  boolean print = false;
  public Online_CDAWG_sym(ArrayList<String> _stringset, boolean print) {

    super();

    id_cnt = 0;
    stringset = _stringset;
    input_text = "";
    letters_available = true;

    this.print = print;

    all_nodes = new ArrayList();

  } // Online_CDAWG()

  public void build_cdawg() {

    long startTime = System.currentTimeMillis();
    System.out.println(" ..building CDAWG ");

    // 1. Create a state root

    root = create_node(-1, 0, 0, 0);

    stringcount = 0;

  main_loop:
    while (stringcount < stringset.size()) {

      quasi_candidates.add(new HashMap<Integer, Node>());

      input_text = stringset.get(stringcount);

      // 1. Create a state sink

      for (int i = 0; i < sinks.size(); i++) {
        if (this.get_node_label(sinks.get(i)).equals(input_text)) {
          // sink.stringnumbers.add(stringcount);
          sinks.get(i).stringnumbers.add(stringcount);
          stringcount++;
          continue main_loop;
        }
      }

      sink = create_node(0, 0, 0, stringcount);
      sink.stringnumbers.add(stringcount);

      sinks.add(sink);

      ap = new ActivePoint(root, -1, 0);

      pos = -1;

      // 2. For each letter a of w do:

      letters_available = true;
      while (letters_available) {
        update();
        if (pos >= input_text.length() - 1)
          letters_available = false;
      } // letters_available

      sink.suffixLink = ap.active_node;

      stringcount++;
    }

    long duration = System.currentTimeMillis() - startTime;
    System.out.println(" ... took " + duration + " millieseconds");
  }

  boolean canonize(Node node, int pos) {
    int edgelength =
        this.get_edge_length(ap.active_edge, ap.active_node,
                             ap.active_node.children.get(ap.active_edge));
    if (ap.active_length >= edgelength) {

      if (print)
        System.out.println("CANONIZE");
      ap.active_length -= edgelength;

      int next_pos = pos - ap.active_length;

      if (ap.active_length > 0) {

        ap.active_edge = this.get_letter(next_pos, stringcount);
      }

      ap.active_node = node;

      return true;
    }
    return false;
  }

  private void add_suffixlink(Node node) {
    if (print)
      System.out.println("SUFFIX START <" + this.get_node_label(node) + ">");
    if (suffixstate != null) {
      if (print)
        System.out.println("ADDDDD SUFFIXLINK!!!!!!");
      suffixstate.suffixLink = node;
      old_suffixstate = suffixstate;

      create_edge_left(
          node, suffixstate,
          this.get_letter(
              suffixstate.end - this.get_node_length(node),
              suffixstate.stringnr)); // create left_edge from split to next
    }
    suffixstate = node;
  }

  //*******************************************************************************
  // update()
  //*******************************************************************************
  private void update() {

    pos++;
    int a = get_letter(pos, stringcount);
    suffixstate = null;
    old_suffixstate = null;
    split = null;
    Node active_child = null;

    sink.end = pos;
    sink.pathlength = pos + 1;

    while (true) {

      if (ap.active_length == 0)
        ap.active_edge = a;

      if (print)
        System.out.println(" pos: " + pos + " " + input_text.charAt(pos) +
                           " (active_node: " + ap.active_node.id + " <" +
                           this.get_node_label(ap.active_node) +
                           "> active_edge: " + ap.active_edge + " [" +
                           this.get_letter_by_idx(ap.active_edge) +
                           "] active length: " + ap.active_length + ")");

      if (!(has_outgoing_edge(ap.active_node,
                              ap.active_edge))) { // if no edge with label of
                                                  // active edge from active
                                                  // point

        create_edge(ap.active_node, sink,
                    ap.active_edge); // create new edge from active_node to sink

        int active_edge_left = 0;
        if (ap.active_node == root) {
          active_edge_left = ap.active_edge;

          create_edge_left(
              ap.active_node, sink,
              active_edge_left); // create new edge from active_node to sink

        } else {
          int left_char_occ = pos - this.get_node_length(ap.active_node) - 1;

          if (!this.get_node_label(ap.active_node).startsWith("#")) {

            if (!(has_outgoing_left_edge(
                    ap.active_node,
                    this.get_letter(left_char_occ, sink.stringnr)))) {

              create_edge_left(ap.active_node, sink,
                               this.get_letter(left_char_occ, sink.stringnr));
            }
          }
        }

        add_suffixlink(ap.active_node); // rule 2

      }

      else {
        if (print)
          System.out.println("ELSE");

        Node next = ap.active_node.children.get(ap.active_edge);
        if (canonize(next, pos))
          continue; // observation 2
        String current_letter = get_letter_by_idx(a);

        String active_label =
            this.get_edge_label(ap.active_edge, ap.active_node, next);
        if (print)
          System.out.println("active label: " + active_label);
        Character last_char = active_label.charAt(ap.active_length);
        String last_suffix = last_char.toString();

        if (last_suffix.equals(current_letter)) { // observation 1 current auf
                                                  // active edge vorhanden =>
                                                  // kein neuer rechtskontext.
          if (print)
            System.out.println("ACTIVE LENGTH ++");
          ap.active_length++;
          add_suffixlink(ap.active_node); // observation 3
          break;
        }

        // Redirect
        if (active_child == ap.active_node.children.get(ap.active_edge)) {
          redirect_edge(ap.active_node, split, ap.active_edge);

          int active_edge_left = 0;
          if (ap.active_node == root) {
            active_edge_left = ap.active_edge;
            redirect_edge_left(ap.active_node, split, active_edge_left);
          } else if (this.get_edge_length(ap.active_edge, ap.active_node,
                                          split) +
                         this.get_node_length(ap.active_node) <
                     this.get_node_length(split)) {

            // länge start knoten + läng der kante < länge des zielknotens
            int left_letter_pos =
                split.start + this.get_node_length(split) -
                this.get_edge_length(ap.active_edge, ap.active_node, split) -
                this.get_node_length(ap.active_node) - 1;
            active_edge_left = this.get_letter(left_letter_pos, split.stringnr);
            Node next_left = ap.active_node.children_left.get(active_edge_left);

            if (print)
              System.out.println(this.get_node_label(ap.active_node) +
                                 " pos= " + left_letter_pos + " " +
                                 this.get_letter_by_idx(active_edge_left) +
                                 " " + split.stringnr);
            if (print)
              System.out.println(active_edge_left);
            if (print)
              System.out.println(this.get_node_label(next_left));

            if (this.get_edge_label_left(active_edge_left, ap.active_node,
                                         next_left)
                        .length() +
                    this.get_node_length(ap.active_node) <
                this.get_node_length(next_left)) {
              redirect_edge_left(ap.active_node, split, active_edge_left);
            }
          }

        }

        // Split
        else {
          if (print)
            System.out.println("SPLIT");

          active_child = ap.active_node.children.get(ap.active_edge);

          String rep_parent = this.get_node_label(ap.active_node);

          String x = this.get_letter_by_idx(ap.active_edge);

          int stringnr = next.stringnr;

          int start = stringset.get(stringnr).indexOf(rep_parent + x);
          split = create_node(
              start, start + ap.active_node.pathlength + ap.active_length - 1,
              ap.active_node.pathlength + ap.active_length, stringnr);

          create_edge(ap.active_node, split,
                      ap.active_edge); // create edge from active_node to split

          create_edge(split, sink, a); // create edge from split to sink

          int last_char_occ = stringset.get(next.stringnr).indexOf(last_char);

          create_edge(
              split, next,
              this.get_letter(last_char_occ,
                              next.stringnr)); // create edge from split to next
          add_suffixlink(split);               // rule 2

          // new sym edges
          //				 if(stringset.get(stringnr).startsWith(active_label.substring(0,
          // ap.active_length))){
          if (this.get_node_length(ap.active_node) + active_label.length() ==
              this.get_node_length(next)) {

            if (print)
              System.out.println("SYM PR�FIX");
            if (print)
              System.out.println("next id " + next.id);
            if (print)
              System.out.println("split id " + split.id);

            Iterator it = next.children_left.entrySet().iterator();
            while (it.hasNext()) {
              Map.Entry pair = (Map.Entry)it.next();
              int key = (int)pair.getKey();
              Node child = (Node)pair.getValue();
              split.children_left.put(key, child);
            }

          } else {
            if (print)
              System.out.println(this.get_node_label(split));
            if (print)
              System.out.println(this.get_node_label(next));
            if (print)
              System.out.println(
                  this.get_letter(split.start - 1, next.stringnr));
            create_edge_left(
                split, next,
                this.get_letter(
                    split.start - 1,
                    next.stringnr)); // create edge from split to next
          }

          int left_char_occ = stringset.get(stringnr).indexOf(
                                  active_label.substring(0, ap.active_length)) -
                              1;

          if (old_suffixstate == null || old_suffixstate.suffixLink != split) {
            if (print)
              System.out.println(pos + " POS");
            if (print)
              System.out.println(this.get_node_label(split));

            left_char_occ = pos - this.get_node_length(split) - 1;

            if (print)
              System.out.println(
                  "STELLE left cahr occ " + left_char_occ + "  " +
                  ((ap.active_length + this.get_node_length(ap.active_node)) -
                   1));

            if (split.start != 0)
              create_edge_left(
                  split, sink,
                  this.get_letter(
                      left_char_occ,
                      sink.stringnr)); // create left_edge from split to sink
          }

          if (ap.active_node == root)
            create_edge_left(
                ap.active_node, split,
                ap.active_edge); // create edge from active_node to split
        }
      }

      if (ap.active_node == root && ap.active_length > 0) { // rule 1
        if (print)
          System.out.println("RULE 1");
        ap.active_edge = get_letter(pos - ap.active_length + 1,
                                    stringcount); // find the next shortest
                                                  // suffix (e.g. after root ->
                                                  // ab ; root -> b)
        ap.active_length--;

      } else {
        if (ap.active_node.suffixLink != null) { // rule 3
          if (print)
            System.out.println("RULE 3");
          ap.active_node = ap.active_node.suffixLink;
        } else {
          ap.active_node = root;
          if (print)
            System.out.println("AP -> ROOT");

          break;
        }
      }

    } // while

    separate_node();

    Node target_ap = ap.active_node.children.get(ap.active_edge);

    if (target_ap != null) {
      if (print)
        System.out.println(this.get_node_label(target_ap) + " " +
                           ap.active_length);

      if (this.get_edge_length(ap.active_edge, ap.active_node, target_ap) ==
              ap.active_length &
          !this.get_node_label(target_ap).startsWith("#")) {

        int left_char_occ = pos - this.get_node_length(target_ap);

        if (print)
          System.out.println("LEFT CHAR OCC " + left_char_occ + " " +
                             sink.stringnr);
        int left_letter = this.get_letter(left_char_occ, sink.stringnr);

        if (!(this.has_outgoing_left_edge(target_ap, left_letter))) {
          this.create_edge_left(target_ap, sink, left_letter);
        }
      }
    }

    if (print)
      System.out.println(
          "-----------------------------------------------------------------------------------------");
  }

  //*******************************************************************************
  // separate_node()
  //*******************************************************************************

  void separate_node() {

    Node next = ap.active_node.children.get(ap.active_edge);
    int ap_rep_length = 0;
    ap_rep_length = this.get_node_length(ap.active_node);

    int next_rep_length = this.get_node_length(next);

    //		if(print) System.out.println(this.get_edge_label(ap.active_edge,
    // ap.active_node, next));
    int edgelength = this.get_edge_length(ap.active_edge, ap.active_node, next);
    if (print)
      System.out.println("edgelength " + edgelength);

    if (ap.active_length == edgelength &&
        next_rep_length > ap.active_length + ap_rep_length) {
      if (print)
        System.out.println("SEPARATE NODE" + next.pathlength);

      Node copy_node = this.create_node(
          next.end - (edgelength + this.get_node_length(ap.active_node)) + 1,
          next.end, this.get_node_length(ap.active_node) + edgelength,
          next.stringnr);

      Iterator it = next.children.entrySet().iterator();

      while (it.hasNext()) {
        Map.Entry pair = (Map.Entry)it.next();
        int key = (int)pair.getKey();
        Node child = (Node)pair.getValue();
        copy_node.children.put(key, child);
      }

      copy_node.suffixLink = next.suffixLink;
      next.suffixLink = copy_node;
      if (print)
        System.out.println("SEPARATE NODE ID " + copy_node.id + " REP " +
                           this.get_node_label(copy_node));

      redirect_edge(ap.active_node, copy_node, ap.active_edge);

      int left_char_occ = copy_node.start - 1;

      create_edge_left(
          copy_node, next,
          this.get_letter(
              left_char_occ,
              next.stringnr)); // create left_edge from split to next
      create_edge_left(
          copy_node, sink,
          this.get_letter(
              pos - this.get_node_length(copy_node),
              sink.stringnr)); // create left_edge from split to sink

      create_edge_left(
          copy_node.suffixLink, copy_node,
          this.get_letter(copy_node.start,
                          copy_node.stringnr)); // create left_edge from
                                                // copy_node_suffixlink to
                                                // copy_node

      Node old_next = next;

      int left_letter = 0;

      if (print)
        System.out.println("ANFANG ACTIVE LENGTH " + ap.active_length);

      while (true) {

        if (ap.active_node.suffixLink != null) { // rule 3
          if (print)
            System.out.println("RULE 3");
          left_char_occ = ap.active_node.end -
                          this.get_node_length(ap.active_node.suffixLink);
          left_letter = this.get_letter(left_char_occ, ap.active_node.stringnr);
          ap.active_node = ap.active_node.suffixLink;

        }

        else {

          if (print)
            System.out.println("RULE 1");
          ap.active_length--;
          left_char_occ--;

          if (ap.active_length == 0) {
            break;
          }

          ap.active_edge = get_letter(pos - ap.active_length + 1,
                                      stringcount); // find the next shortest
                                                    // suffix (e.g. after root
                                                    // -> ab ; root -> b)
          left_letter =
              this.get_letter(pos - ap.active_length + 1, stringcount);

          if (print)
            System.out.println("RULE1 suffix" + ap.active_edge);
        }

        next = ap.active_node.children.get(ap.active_edge);

        while (this.get_edge_length(ap.active_edge, ap.active_node, next) +
                   this.get_node_length(ap.active_node) >=
               this.get_node_length(next)) {
          canonize(next, pos + 1);

          if (this.get_edge_length(ap.active_edge, ap.active_node, copy_node) +
                  this.get_node_length(ap.active_node) <
              this.get_node_length(copy_node)) {
            // länge start knoten + läng der kante < länge des zielknotens
            int left_letter_pos =
                copy_node.start + this.get_node_length(copy_node) -
                this.get_edge_length(ap.active_edge, ap.active_node,
                                     copy_node) -
                this.get_node_length(ap.active_node) - 1;
            int active_edge_left = this.get_letter(
                left_letter_pos, copy_node.stringnr); // stimmt das????
            Node next_left = ap.active_node.children_left.get(active_edge_left);

            if (this.get_edge_label_left(active_edge_left, ap.active_node,
                                         next_left)
                        .length() +
                    this.get_node_length(ap.active_node) <=
                this.get_node_length(next_left)) {
              redirect_edge_left(ap.active_node, copy_node, active_edge_left);
            }
          }

          if (ap.active_length == 0) {
            break;
          }
          next = ap.active_node.children.get(ap.active_edge);
        }

        //			if(this.get_node_length(copy_node)>(this.get_edge_length(ap.active_edge,
        // ap.active_node,old_next)+this.get_node_length(ap.active_node))){
        if (old_next == next) {
          if (print)
            System.out.println("SEPARATE REDIRECT");
          redirect_edge(ap.active_node, copy_node, ap.active_edge);

          if (this.get_edge_length(ap.active_edge, ap.active_node, copy_node) +
                  this.get_node_length(ap.active_node) <
              this.get_node_length(copy_node)) {

            // länge start knoten + läng der kante < länge des zielknotens
            int left_letter_pos =
                copy_node.start + this.get_node_length(copy_node) -
                this.get_edge_length(ap.active_edge, ap.active_node,
                                     copy_node) -
                this.get_node_length(ap.active_node) - 1;
            int active_edge_left = this.get_letter(
                left_letter_pos, copy_node.stringnr); // stimmt das????
            Node next_left = ap.active_node.children_left.get(active_edge_left);

            if (this.get_edge_label_left(active_edge_left, ap.active_node,
                                         next_left)
                        .length() +
                    this.get_node_length(ap.active_node) <
                this.get_node_length(next_left)) {
              redirect_edge_left(ap.active_node, copy_node, active_edge_left);
            }
          }
        }
        if (print)
          System.out.println("APPPPP " + ap.active_node.id + " " +
                             ap.active_edge + " al " + ap.active_length);
        if (print)
          System.out.println(" pos: " + pos + " " + input_text.charAt(pos) +
                             " (active_node: " + ap.active_node.id + " <" +
                             this.get_node_label(ap.active_node) +
                             "> active_edge: " + ap.active_edge + " [" +
                             this.get_letter_by_idx(ap.active_edge) +
                             "] active length: " + ap.active_length + ")");

        if (ap.active_length == 0) {
          break;
        }
      }

      ap.active_node = copy_node;
    }
  }

  //*******************************************************************************
  // redirect_edge()
  //*******************************************************************************

  void redirect_edge(Node start, Node target, int edge) {

    start.children.put(edge, target);

    /// **  # # # ** ## ** #'**** ##** NEU NEU

    // KEY BLEIBT BUCHTSTABE dann Object Edge mit Position und Zielknoten.
  }

  //*******************************************************************************
  // redirect_edge_left()
  //*******************************************************************************

  void redirect_edge_left(Node start, Node target, int edge) {

    start.children_left.put(edge, target);
  }

  //*******************************************************************************
  // get_letter()
  //*******************************************************************************

  int get_letter(int pos, int stringnr) {

    Character letter = stringset.get(stringnr).charAt(pos);
    return utf8_sequence_map.get(letter.toString());
  }

  //*******************************************************************************
  // get_node_label()
  //*******************************************************************************

  public String get_node_label(Node node) {

    String result;
    //	System.out.println(stringset.get(0).substring(node.end-node.pathlength,node.end)+"
    // pathlength: "+node.pathlength);

    try {

      if (node == root)
        return "λ";

      else {
        return stringset.get(node.stringnr).substring(node.start, node.end + 1);
      }

    } catch (Exception e) {

      return "X";
    }

  } // get_node_label()

  //*******************************************************************************
  // get_node_length()
  //*******************************************************************************

  public int get_node_length(Node node) {

    return node.pathlength;

  } // get_node_label()

  //*******************************************************************************
  // get_edge_label()
  //*******************************************************************************

  public String get_edge_label(int letter_idx, Node parent, Node node) {

    String rep_parent = get_node_label(parent);
    String rep_child = get_node_label(node);
    if (rep_parent.equals("λ"))
      rep_parent = "";

    String letter = this.get_letter_by_idx(letter_idx);
    // System.out.println("rep_parent: "+rep_parent+" rep_child: "+rep_child+"
    // letter "+letter+" node_id "+node.id+ " node start "+node.start);

    try {

      int start = rep_child.indexOf(rep_parent + letter);

      return rep_child.substring(start + rep_parent.length(),
                                 rep_child.length());

    } catch (Exception e) {
      return "X";
    }

  } // get_edge_label()

  //*******************************************************************************
  // get_edge_label_left()
  //*******************************************************************************

  public String get_edge_label_left(int letter_idx, Node parent, Node node) {

    String rep_parent = new StringBuilder(get_node_label(parent)).toString();
    String rep_child = new StringBuilder(get_node_label(node)).toString();
    if (rep_parent.equals("λ"))
      rep_parent = "";

    String letter = this.get_letter_by_idx(letter_idx);
    // System.out.println("rep_parent: "+rep_parent+" rep_child: "+rep_child+"
    // letter "+letter+" node_id "+node.id+ " node start "+node.start);

    try {
      //
      //
      int end = rep_child.lastIndexOf(letter + rep_parent) + 1;
      //
      //
      String result =
          new StringBuilder(rep_child.substring(0, end)).reverse().toString();
      return result;
      // return this.get_letter_by_idx(letter_idx);
    } catch (Exception e) {
      return "X";
    }

  } // get_edge_label_left()

  //*******************************************************************************
  // get_edge_length()
  //*******************************************************************************

  public int get_edge_length(int letter_idx, Node parent, Node node) {

    String rep_parent = get_node_label(parent);
    String rep_child = get_node_label(node);
    if (rep_parent.equals("λ"))
      rep_parent = "";

    String letter = this.get_letter_by_idx(letter_idx);
    // System.out.println("rep_parent: "+rep_parent+" rep_child: "+rep_child+"
    // letter "+letter+" node_id "+node.id+ " node start "+node.start);

    try {

      int start = rep_child.indexOf(rep_parent + letter);

      return rep_child.length() - (start + rep_parent.length());

    } catch (Exception e) {
      return -2;
    }

  } // get_edge_label()

  private boolean has_outgoing_edge(Node node, int label) {
    Node n = node.children.get(label);
    if (n == null)
      return false;
    return true;
  }

  private boolean has_outgoing_left_edge(Node node, int label) {
    Node n = node.children_left.get(label);
    if (n == null)
      return false;
    return true;
  }

  public Node get_parent(Node node) { return node.suffixLink; } // get_parent()

  public void save_graph_to_db() {

    long startTime = System.currentTimeMillis();
    System.out.println("\n ..saving graph to database ");

    //  Neo4J_Handler neo4j = new Neo4J_Handler("C:/Neo4j/Neo4CDAWG_test",this);

    //  neo4j.connect_and_clear_graphDb();
    //  neo4j.create_node_db(root);
    //
    //  neo4j.link_children_db(root);
    //  neo4j.link_suffixes_db(root);

    long duration = System.currentTimeMillis() - startTime;
    System.out.println(" ... took " + duration + " milliseconds \n");
  }

  //*******************************************************************************
  // create_node()
  //*******************************************************************************

  private Node create_node(int start, int end, int pathlength, int stringnr) {

    // if(pos==-2) node  = new Node();
    id_cnt = id_cnt + 1;
    int new_idcnt = id_cnt;

    Node node = new Node(start, end, pathlength, stringnr, id_cnt);

    //	   Iterator it = utf8_sequence_map.entrySet().iterator();
    //		  while (it.hasNext()) {
    //		  	    Map.Entry pair = (Map.Entry)it.next();
    //
    //			 node.children.put((Integer) pair.getValue(),null);
    //		  }

    all_nodes.add(node);

    return node;
  }

  //*******************************************************************************
  // create_edge()
  //*******************************************************************************

  public void create_edge(Node parent, Node child, int label) {

    // for(int i=0;i<sinks.size();i++) {
    //	Node sink= sinks.get(i);
    //	if(child==sink) parent.right_end=true;
    //}
    parent.children.put(label, child);

  } // create_edge()

  //*******************************************************************************
  // create_edge_left()
  //*******************************************************************************

  public void create_edge_left(Node parent, Node child, int label) {

    // for(int i=0;i<sinks.size();i++) {
    //		Node sink= sinks.get(i);
    //	if(child==sink&&parent.right_end) {
    //		quasi_candidates.get(sink.stringnr).put(parent, label);
    //	}
    //}
    parent.children_left.put(label, child);

  } // create_edge()

  //*******************************************************************************
  // create_edge()
  //*******************************************************************************

  public void create_edge_new(Node parent, Node child, int pos) {

    // for(int i=0;i<sinks.size();i++) {
    //	Node sink= sinks.get(i);
    //	if(child==sink) parent.right_end=true;
    //}
    parent.children_pos.put(pos, child);

  } // create_edge()

  //*******************************************************************************
  // create_edge_left_new()
  //*******************************************************************************

  public void create_edge_left_new(Node parent, Node child, int pos) {

    // for(int i=0;i<sinks.size();i++) {
    //		Node sink= sinks.get(i);
    //	if(child==sink&&parent.right_end) {
    //		quasi_candidates.get(sink.stringnr).put(parent, label);
    //	}
    //}
    parent.children_left_pos.put(pos, child);

  } // create_edge()

  public void print_automaton(String outputfile) {

    long startTime = System.currentTimeMillis();
    System.out.println(" ..printing CDAWG ");

    String filename = "cdawg.dot";

    StringBuilder sb = new StringBuilder();

    //  dotfile
    sb.append("digraph cdawg_graph { label=\"CDAWG input text: " + input_text +
              "\"");
    sb.append("\n"
              + "labeljust=l");
    sb.append("\n"
              + "fontname=Vera");
    sb.append("\n"
              + "fontsize=20");
    sb.append("\n"
              + "labelloc=top");
    sb.append("\n"
              + "margin=.5");
    sb.append("\n"
              + "size=\"15,7\"");
    sb.append("\n"
              + "nodesep=.3");
    sb.append(
        "\n"
        +
        "node [width=0.5,height=auto,shape=record,fontsize=12,fontcolor=black,style=filled,fillcolor=sandybrown];");
    sb.append("\n"
              +
              "edge [minlen=1,constraint=true,fontsize=11,labelfontsize=11];");

    sb.append("\n");
    sb.append("\n");

    //
    String edge_list = "", node_list = "";
    //  cout << endl << " Nodes" << endl << " -----" << endl;
    sb.append("/* Nodes */ \n \n");
    //
    String nodeslist = print_nodes();
    sb.append(nodeslist);
    //  cout << endl << " Edges" << endl << " -----" << endl;
    sb.append("\n /* Edges */ \n \n");
    //  print_edges(root,edge_list);
    String edgeslist = print_edges();
    sb.append(edgeslist + "}");

    String result = sb.toString();

    Util.writeFile(filename, result);

    String[] cmd = {"cmd.exe", "/c",
                    "dot -Tsvg cdawg.dot -o " + outputfile + "_CDAWG.svg"};

    try {

      Process p = Runtime.getRuntime().exec(cmd);

      BufferedReader input =
          new BufferedReader(new InputStreamReader(p.getInputStream()));

      String temp = "";

      while ((temp = input.readLine()) != null)
        System.out.println(temp);

      input.close();

    } catch (IOException e) {
    }

    long duration = System.currentTimeMillis() - startTime;
    System.out.println(" ... took " + duration + " milliseconds");

  } // print_automaton()

  public String get_dot() {

    long startTime = System.currentTimeMillis();

    StringBuilder sb = new StringBuilder();

    //
    String edge_list = "", node_list = "";
    //  cout << endl << " Nodes" << endl << " -----" << endl;
    //
    String nodeslist = print_nodes();
    sb.append(nodeslist);
    //  cout << endl << " Edges" << endl << " -----" << endl;
    //  print_edges(root,edge_list);
    String edgeslist = print_edges();
    sb.append(edgeslist + "}");

    String result = sb.toString();

    return result;

  } // get_dot()

  String print_nodes() {

    String result = "";

    String node_str;
    StringBuilder node_sstr = new StringBuilder();

    // String[] levels = new String[stringset.get(0).length()];
    // for (int i=0;i<levels.length;i++) levels[i] = "{rank=same;";

    for (int r = 0; r < all_nodes.size(); r++) {

      Node node = all_nodes.get(r);

      int node_number;
      if (node.end == -1)
        node_number = node.end + 1;
      else
        node_number = node.end;

      String node_id_string = " n_" + node.id + "_" + node_number + " ";

      node_sstr.append(node_id_string);

      // Multimap<Integer,Integer> equivalence_classes =
      // get_equivalence_classes(node);

      //
      // ArrayList eq_strings_map =
      // equivalence_classes_to_strings(equivalence_classes);
      //
      //
      String rep = this.get_node_label(node);
      if (rep.length() == 0)
        rep = "λ";

      // cout << " rep: " << rep << endl;
      // cout << " pathlength " << node->pathlength << endl;
      // cout << "-------------" << endl;
      //
      //// cout << node_str << " [label=" << node->end << " id=" << node->id  <<
      ///" pathlength=" << node->pathlength <<   "];" << endl;
      node_sstr.append("[label=< id=" + node.id + "<br/> Rep: \"" + rep +
                       "\"<br/>");
      //
      //
      //
      node_sstr.append("Start: " + node.start + " <br/> End: " + node.end +
                       "<br/> Pathlength: " + node.pathlength +
                       "<br/> Stringnr: " + node.stringnr + "<br/>>");

      // for (int i=0;i<eq_strings_map.size();i++){
      //    node_sstr.append("s" + (i+1) + ":" + eq_strings_map.get(i));
      //    if(i==0)node_sstr.append("<br/>");
      //    else node_sstr.append("<br/>>");
      // }
      node_sstr.append("]; \n");
      // levels[node.pathlength] += node_id_string+"; ";
    }

    // for (int i=0;i<levels.length;i++) node_sstr.append(levels[i]+"}\n");

    result = node_sstr.toString();

    return result;

  } // print_nodes()

  //*******************************************************************************
  // print_nodes_rec()
  //*******************************************************************************
  String print_nodes_rec(Node node) {

    String result = "";

    StringBuilder node_sstr = new StringBuilder();

    String node_str = "";

    int node_number;
    if (node.end == -1)
      node_number = node.end + 1;
    else
      node_number = node.end;

    node_sstr.append(" n_" + node.id + "_" + node_number + " ");

    String rep = "";
    if (node != root)
      rep = this.get_node_label(node);
    else
      rep = "λ";

    node_sstr.append("[label=< id=" + node.id + "<br/> Rep: \"" + rep +
                     "\"<br/>>");

    node_sstr.append("]; \n");

    StringBuilder childlevel = new StringBuilder();
    childlevel.append("{ rank= same; ");

    Iterator it = node.children.entrySet().iterator();
    while (it.hasNext()) {

      Map.Entry pair = (Map.Entry)it.next();

      Node child = (Node)pair.getValue();
      int i = (int)pair.getKey();

      String child_node_str = print_nodes_rec(child);
      node_sstr.append(child_node_str);

      int node_number_end;
      if (node.children.get(i).end == -1)
        node_number_end = 0;
      else
        node_number_end = node.children.get(i).end;

      childlevel.append(" n_" + node.children.get(i).id + "_" +
                        node_number_end + "; ");
    }

    childlevel.append("}");

    node_sstr.append(childlevel.toString() + "\n");

    result = node_sstr.toString();

    return result;

  } // print_nodes_rec()

  //*******************************************************************************
  // print_edges()
  //*******************************************************************************
  public String print_edges() {

    String result = "";
    StringBuilder edge_sstr = new StringBuilder();

    for (int r = 0; r < all_nodes.size(); r++) {

      Node node = all_nodes.get(r);
      //    if (node->ident_pointers.size()>0||node==root) {
      int label_count = 0;

      Iterator it = node.children.entrySet().iterator();
      while (it.hasNext()) {

        Map.Entry pair = (Map.Entry)it.next();

        int i = (int)pair.getKey();

        int node_number;
        if (node.end == -1)
          node_number = node.end + 1;
        else
          node_number = node.end;

        int node_number_end;
        if (node.children.get(i).end == -1)
          node_number_end = 0;
        else
          node_number_end = node.children.get(i).end;

        edge_sstr.append(" n_" + node.id + "_" + node_number + " -> n_" +
                         node.children.get(i).id + "_" + node_number_end + " ");

        String label = "";
        label = this.get_edge_label(i, node, node.children.get(i));

        String regex = "\\\"";

        edge_sstr.append(" [style="
                         + "solid"
                         + "  label=\"" + label + "\"];"
                         + "\n");

      } // for it node children

      it = node.children_left.entrySet().iterator();
      while (it.hasNext()) {

        Map.Entry pair = (Map.Entry)it.next();

        int i = (int)pair.getKey();

        int node_number;
        if (node.end == -1)
          node_number = node.end + 1;
        else
          node_number = node.end;

        int node_number_end;
        if (node.children_left.get(i).end == -1)
          node_number_end = 0;
        else
          node_number_end = node.children_left.get(i).end;

        edge_sstr.append(" n_" + node.id + "_" + node_number + " -> n_" +
                         node.children_left.get(i).id + "_" + node_number_end +
                         " ");

        String label = "";
        label = this.get_edge_label_left(i, node, node.children_left.get(i));

        String regex = "\\\"";

        edge_sstr.append(" [color=blue"
                         + "  label=\"" + label + "\"];"
                         + "\n");

      } // for it node children_left

      Node suffixLink = node.suffixLink;

      if (suffixLink != null) {

        int node_number;
        if (node.end == -1)
          node_number = node.end + 1;
        else
          node_number = node.end;

        int node_number_end;
        if (node.suffixLink.end == -1)
          node_number_end = node.suffixLink.end + 1;
        else
          node_number_end = node.suffixLink.end;

        edge_sstr.append(" n_" + node.id + "_" + node_number + " -> n_" +
                         node.suffixLink.id + "_" + node_number_end + " ");
        edge_sstr.append(" [color=red];\n");
      }

      //   edge_list+= edge_str;
      label_count++;

      // if (node.prefixLinks.size()>0)
      // {
      //
      //   for (int k=0;k<node->prefixLinks.size();k++){
      //   string edge_str;
      //   stringstream edge_sstr;
      //
      //   int node_number;
      //   if (node->end==-1) node_number=node->end+1;
      //   else node_number = node->end;
      //
      //   int node_number_end;
      //   if (node->prefixLinks[k]->end==-1)
      //   node_number_end=node->prefixLinks[k]->end+1; else node_number_end =
      //   node->prefixLinks[k]->end;
      //
      //   edge_sstr << " n_" << node->id << "_" << node_number << " -> n_" <<
      //   node->prefixLinks[k]->id << "_" << node_number_end << " "; edge_str =
      //   edge_sstr.str();
      //
      //   string label =  node->prefixlabels[k];
      //   string regex = "\\\"";
      //   label = ReplaceAll(label,std::string("\""), std::string(regex));
      //   // if ( edge_list.find(edge_str) != string::npos ) return;
      //   // cout << edge_str << " ["<< "prefix_link label=" <<
      //   node->prefixlabels[k] << "];" << endl; dotfile << edge_str << "
      //   [color=blue label=\"" << label << "\"];" << endl; edge_list+=
      //   edge_str;
      //
      //   }
      // }

      //}
    } // for all nodes

    result = edge_sstr.toString();

    return result;

  } // print_edges()
}
