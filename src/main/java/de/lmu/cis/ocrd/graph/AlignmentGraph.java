package de.lmu.cis.ocrd.graph;

import java.util.ArrayList;

import de.lmu.cis.iba.Pairwise_LCS_Alignment;
import de.lmu.cis.iba.Pairwise_LCS_Alignment.AlignmentPair;

public class AlignmentGraph {

	private final String s1, s2;

	private Node start;

	public AlignmentGraph(String a, String b) {
		Pairwise_LCS_Alignment algn = new Pairwise_LCS_Alignment(a, b);
		algn.align();
		s1 = a;
		s2 = b;
		build(algn.getAligmentPairs());
	}

	public Node getStartNode() {
		return start;
	}

	public Tokenizer getTokenizer() {
		return new Tokenizer(getTraverser());
	}

	public Traverser getTraverser() {
		return new Traverser(start);
	}

	public LabelIterator iterator(int id) {
		return new LabelIterator(start, id);
	}

	private void build(ArrayList<AlignmentPair> ps) {
		if (ps.isEmpty()) {
			return;
		}
		start = new Node(ps.get(0).label);
		Node prevn = start;
		AlignmentPair prevp = ps.get(0);
		for (int i = 1; i < ps.size(); i++) {
			final AlignmentPair curp = handleOverlap(prevp, ps.get(i));
			final Node curn = new Node(curp.label);
			final Gap g1 = makeGap(prevp.epos1, curp.spos1, s1, curn);
			final Gap g2 = makeGap(prevp.epos2, curp.spos2, s2, curn);
			prevn.add(g1);
			prevn.add(g2);
			prevp = curp;
			prevn = curn;
		}
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

	private Gap makeGap(int s, int e, String str, Node node) {
		// System.out.println("getGapLabel(" + s + ", " + e + ", " + str + ")");
		// s += 1;
		// e += 1;
		if (s > e) {
			s = e;
		}
		return new Gap(s, e, str, node);
	}
}
