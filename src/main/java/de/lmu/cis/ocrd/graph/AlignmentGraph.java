package de.lmu.cis.ocrd.graph;

import java.util.ArrayList;

import de.lmu.cis.iba.Pairwise_LCS_Alignment.AlignmentPair;

public class AlignmentGraph {
  private final String s1, s2;
  private Node start;
  private Node _final;

  public AlignmentGraph(ArrayList<AlignmentPair> ps, String s1, String s2) {
    this.s1 = '#' + s1 + '$';
    this.s2 = '#' + s2 + '$';
    build(ps);
  }

  private void build(ArrayList<AlignmentPair> ps) {
    if (ps.isEmpty()) {
      return;
    }
    // System.out.println(new Gson().toJson(ps.get(0)));
    start = new Node(ps.get(0).label, this);
    Node prevn = start;
    AlignmentPair prevp = ps.get(0);
    for (int i = 1; i < ps.size(); i++) {
      final AlignmentPair curp = handleOverlap(prevp, ps.get(i));
      final Node curn = new Node(curp.label, this);
      // System.out.println(new Gson().toJson(curp));
      final String s1gap = getGapLabel(prevp.epos1, curp.spos1, s1);
      // System.out.println("s1gap: " + s1gap);
      final String s2gap = getGapLabel(prevp.epos2, curp.spos2, s2);
      // System.out.println("s2gap: " + s2gap);
      Gap g1 = new Gap(0, s1gap, curn);
      Gap g2 = new Gap(1, s2gap, curn);
      prevn.gaps.add(g1);
      prevn.gaps.add(g2);
      prevp = curp;
      prevn = curn;
    }
    _final = prevn;
  }

  private String getGapLabel(int s, int e, String str) {
    // System.out.println("getGapLabel(" + s + ", " + e + ", " + str + ")");
    s += 1;
    e += 1;
    if (s > e) {  // overlaps
      return "";
    }
    return str.substring(s, e);
  }

  private AlignmentPair handleOverlap(AlignmentPair p, AlignmentPair c) {
    if (p.epos1 > c.spos1) {
      String label = c.label.substring(p.epos1 - c.spos1);
      return new AlignmentPair(label, c.epos1, c.epos2);
    }
    if (p.epos2 > c.spos2) {
      String label = c.label.substring(p.epos2 - c.spos2);
      return new AlignmentPair(label, c.epos1, c.epos2);
    }
    return c;
  }

  public Node getStartNode() {
    return this.start;
  }

  public static class Node {
    private final String label;
    private final AlignmentGraph graph;
    private final ArrayList<Gap> gaps;
    private Node(String label, AlignmentGraph graph) {
      this.label = label;
      this.graph = graph;
      this.gaps = new ArrayList<Gap>();
    }
    public String traverse(int id) {
      StringBuilder builder = new StringBuilder();
      this.traverse(id, builder);
      return builder.toString();
    }
    private void traverse(int id, StringBuilder builder) {
      builder.append(label);
      if (!gaps.isEmpty()) {
        builder.append(gaps.get(id).o);
        gaps.get(id).target.traverse(id, builder);
      }
    }
    public String toDot() {
      StringBuilder builder = new StringBuilder();
      builder.append("digraph g { // dotcode\n");
      builder.append("rankdir=LR; // dotcode\n");
      this.appendDot(builder);
      builder.append("} // dotcode\n");
      return builder.toString();
    }

    private void appendDot(StringBuilder builder) {
      builder.append("\"" + label + "\"");
      builder.append(" [label=\"" + label + "\"] // dotcode\n");
      for (Gap g : gaps) {
        builder.append("\"" + label + "\"");
        builder.append(" -> ");
        builder.append("\"" + g.target.label + "\"");
        builder.append(" [label=\"" + g.toString() + "\"] // dotcode\n");
      }
      if (!gaps.isEmpty()) {
        gaps.get(0).target.appendDot(builder);
      }
    }
  }

  private static class Gap {
    final int id;
    final String o;
    final Node target;
    Gap(int id, String o, Node t) {
      this.id = id;
      this.o = o;
      this.target = t;
    }

    @Override
	public String toString() {
      String output = this.o;
      if (output.length() == 0) {
        output = "ε";
      }
      if (" ".equals(output)) {
        output = "<SP>";
      }
      return id + ":" + output;
    }
  }
}
