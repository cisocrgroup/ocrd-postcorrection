package de.lmu.cis.ocrd.graph;

import java.util.ArrayList;

import de.lmu.cis.iba.Pairwise_LCS_Alignment.AlignmentPair;

public class AlignmentGraph {

	private final String s1, s2;

	private Node start;

	public AlignmentGraph(ArrayList<AlignmentPair> ps, String s1, String s2) {
		this.s1 = '#' + s1 + '$';
		this.s2 = '#' + s2 + '$';
		build(ps);
	}

	public Node getStartNode() {
		return start;
	}

	public Tokenizer getTokenizer() {
		return new Tokenizer(start);
	}

	public Traverser getTraverser() {
		return new Traverser(start);
	}

	private void build(ArrayList<AlignmentPair> ps) {
		if (ps.isEmpty()) {
			return;
		}
		// System.out.println(new Gson().toJson(ps.get(0)));
		start = new Node(ps.get(0).label);
		Node prevn = start;
		AlignmentPair prevp = ps.get(0);
		for (int i = 1; i < ps.size(); i++) {
			final AlignmentPair curp = handleOverlap(prevp, ps.get(i));
			final Node curn = new Node(curp.label);
			// System.out.println(new Gson().toJson(curp));
			final String s1gap = getGapLabel(prevp.epos1, curp.spos1, s1);
			// System.out.println("s1gap: " + s1gap);
			final String s2gap = getGapLabel(prevp.epos2, curp.spos2, s2);
			// System.out.println("s2gap: " + s2gap);
			Gap g1 = new Gap(s1gap, curn);
			Gap g2 = new Gap(s2gap, curn);
			prevn.add(g1);
			prevn.add(g2);
			prevp = curp;
			prevn = curn;
		}
	}

	private String getGapLabel(int s, int e, String str) {
		// System.out.println("getGapLabel(" + s + ", " + e + ", " + str + ")");
		s += 1;
		e += 1;
		if (s > e) { // overlaps
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
}
