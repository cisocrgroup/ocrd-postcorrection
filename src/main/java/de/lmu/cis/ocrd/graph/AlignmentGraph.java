package de.lmu.cis.ocrd.graph;

import de.lmu.cis.ocrd.Document.OCRLine;
import java.util.ArrayList;
import de.lmu.cis.iba.Pairwise_LCS_Alignment.AlignmentPair;
import com.google.gson.Gson;
import java.util.HashSet;

public class AlignmentGraph {
  private final ArrayList<String> lines;
  private int n;
  private final Node start;
  private final Node _final;
  private final ArrayList<Node> main;

  public AlignmentGraph(ArrayList<AlignmentPair> as, String s1, String s2) {
    lines = new ArrayList<String>();
    lines.add(s1);
    this.n = 0;
    this.main = new ArrayList<Node>();
    this.start = newNode();
    this._final = newNode();
    setup(s1);
    addAlignment(as, s2);
    System.out.println(s1);
    System.out.println(s2);
  }

  private void setup(String s1) {
    Node current = this.start;
    final String normalized = '#' + s1;
    for (int i = 0; i < normalized.length(); i++) {
      Node n = newNode();
      main.add(n);
      Transition t = new Transition(0, normalized.charAt(i), n);
      current.transitions.add(t);
      current = n;
    }
    current.transitions.add(new Transition(0, '$', _final));
    main.add(_final);
  }

  public Node getStartNode() {
    return this.start;
  }

  private Node newNode() {
    return new Node(this.n++, this);
  }

  public void addAlignment(ArrayList<AlignmentPair> as, String s2) {
    final String normalized = '#' + s2 + '$';
    Node current = this.start;
    int pos = 0;
    for (AlignmentPair p : as) {
      System.out.println(new Gson().toJson(p));
      Node target = main.get(p.spos1 + 1);
      addNewPath(current, target, pos, p.spos2, normalized);
      current = followPath(current, p.epos1 - p.spos1);
      pos = p.epos2;
    }
    current.transitions.add(new Transition(1, '$', _final));
  }

  private void addNewPath(Node current, Node target, int spos, int epos,
                          String normalized) {
    System.out.println("add new spos: " + spos + " (" + normalized.length() +
                       ")");
    System.out.println("add new epos: " + epos + " (" + normalized.length() +
                       ")");
    System.out.println("add new epos: " + (spos + 1) + " -> " + epos);
    System.out.println(normalized);
    boolean newNode = false;
    for (int i = spos + 1; i <= epos; i++) {
      newNode = true;
      Node n = newNode();
      Transition t = new Transition(1, normalized.charAt(i), n);
      current.transitions.add(t);
      current = n;
    }
    if (newNode) {
      current.transitions.add(new Transition(1, 'X', target));
    }
  }

  private Node followPath(Node current, int n) {
    System.out.println("follow path: " + n);
    for (int i = 0; i < n; i++) {
      Node node = current.transitions.get(0).target;
      char c = current.transitions.get(0).c;
      current.transitions.add(new Transition(1, c, node));
      current = node;
    }
    return current;
  }

  public static class Node {
    private final int id;
    private final AlignmentGraph graph;
    private final ArrayList<Transition> transitions;
    private Node(int id, AlignmentGraph graph) {
      this.id = id;
      this.graph = graph;
      this.transitions = new ArrayList<Transition>();
    }

    public String toDot() {
      HashSet<Node> v = new HashSet<Node>();
      StringBuilder builder = new StringBuilder();
      builder.append("digraph g { // dotcode\n");
      builder.append("rankdir=LR; // dotcode\n");
      this.appendDot(builder, v);
      builder.append("} // dotcode\n");
      return builder.toString();
    }

    private void appendDot(StringBuilder builder, HashSet<Node> v) {
      if (v.contains(this)) {
        return;
      }
      v.add(this);
      builder.append(this.id);
      builder.append(" [label=\"" + this.id + "\"]; // dotcode\n");
      for (Transition t : transitions) {
        builder.append(this.id);
        builder.append(" -> ");
        builder.append(t.target.id);
        builder.append("[label=\"");
        builder.append(t.id);
        builder.append(':');
        builder.append(t.c);
        builder.append("\"]; // dotcode\n");
        t.target.appendDot(builder, v);
      }
    }
  }
  private static class Transition {
    final int id;
    final char c;
    final Node target;
    Transition(int id, char c, Node t) {
      this.id = id;
      this.c = c;
      this.target = t;
    }
  }
}
