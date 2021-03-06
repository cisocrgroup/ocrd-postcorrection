package de.lmu.cis.ocrd.align;

import de.lmu.cis.iba.LCS_Alignment_Pairwise;
import de.lmu.cis.iba.LCS_Alignment_Pairwise.AlignmentPair;

import java.util.ArrayList;

public class Graph {

	private final LCS_Alignment_Pairwise alignment;

	private Node start;

	public Graph(String a, String b) {
		this.alignment = new LCS_Alignment_Pairwise(a, b);
		this.alignment.align();
		build(alignment.getAligmentPairs());
	}

	private static boolean isOverlap(AlignmentPair previous, AlignmentPair current) {
		return previous.epos1 > current.spos1 || previous.epos2 > current.spos2;
	}

	private static Gap makeGap(int s, int e, String str, Node node) {
		// Logger.debug("getGapLabel(" + s + ", " + e + ", " + str + ")");
		// s += 1;
		// e += 1;
		if (s > e) {
			s = e;
		}
		return new Gap(s, e, str, node);
	}

	public Node getStartNode() {
		return start;
	}

	public Tokenizer getTokenizer() {
		return new Tokenizer(this);
	}

	public Traverser getTraverser() {
		return new Traverser(this);
	}

	public LabelIterator iterator(int id) {
		return new LabelIterator(start, id);
	}

	public String toDot() {
		return start.toDot();
	}

	private void build(ArrayList<AlignmentPair> ps) {
		final String s1 = alignment.getString(0);
		final String s2 = alignment.getString(1);
		// explicitly handle cases where both strings are equal
		if (ps.isEmpty() || s1.equals(s2)) {
			start = new Node(s1);
			return;
		}
		// Logger.debug("s1: {}", s1);
		// Logger.debug("s2: {}", s2);
		start = new Node(ps.get(0).label);
		Node prevn = start;
		AlignmentPair prevp = ps.get(0);
		for (int i = 1; i < ps.size(); i++) {
			final AlignmentPair curp = ps.get(i);
			if (isOverlap(prevp, curp)) {
				// Logger.debug("skipping {} {}", prevp, curp);
				continue;
			}
			final Node curn = new Node(curp.label);
			final Gap g1 = makeGap(prevp.epos1, curp.spos1, s1, curn);
			final Gap g2 = makeGap(prevp.epos2, curp.spos2, s2, curn);
			// Logger.debug("previous: {}", prevp);
			// Logger.debug("current:  {}", curp);
			// Logger.debug("gap1: {}", g1.getLabel());
			// Logger.debug("gap2: {}", g2.getLabel());
			prevn.add(g1);
			prevn.add(g2);
			prevp = curp;
			prevn = curn;
		}
		// check if last node is end node with $
		final int n = prevn.getLabel().length();
		if (n == 0 || prevn.getLabel().charAt(n - 1) != '$') {
			final Node end = new Node("$");
			final Gap g1 = makeGap(prevp.epos1, s1.length() - 1, s1, end);
			final Gap g2 = makeGap(prevp.epos1, s2.length() - 1, s2, end);
			prevn.add(g1);
			prevn.add(g2);
		}
	}
}
