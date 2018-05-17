package de.lmu.cis.ocrd.align;

import de.lmu.cis.iba.LCS_Alignment_Pairwise;
import de.lmu.cis.iba.LCS_Alignment_Pairwise.AlignmentPair;
import org.pmw.tinylog.Logger;

import java.util.ArrayList;

public class Graph {

	private final String s1, s2;

	private Node start;

	public Graph(LCS_Alignment_Pairwise algn, String a, String b) {
		s1 = a;
		s2 = b;
		build(algn.getAligmentPairs());
	}

	public Graph(String a, String b) {
		LCS_Alignment_Pairwise algn = new LCS_Alignment_Pairwise(a, b);
		algn.align();
		s1 = a;
		s2 = b;
		build(algn.getAligmentPairs());
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
        // explicitly handle cases where both strings are equal
		if (ps.isEmpty() || s1.equals(s2)) {
			start = new Node('#' + s1 + '$');
			return;
		}
		// System.out.println("s1: " + s1);
        // System.out.println("s2: " + s2);
        // for (AlignmentPair p : ps) {
        //     System.out.println("ALIGNMENT PAIR: " + p);
        // }
		start = new Node(ps.get(0).label);
		Node prevn = start;
		AlignmentPair prevp = ps.get(0);
		for (int i = 1; i < ps.size(); i++) {
			final AlignmentPair curp = handleOverlap(prevp, ps.get(i));
			final Node curn = new Node(curp.label);
			final Gap g1 = makeGap(prevp.epos1, curp.spos1, s1, curn);
			final Gap g2 = makeGap(prevp.epos2, curp.spos2, s2, curn);
            Logger.info("s1: '{}'\nINFO: s2: '{}'\nINFO: pl: '{}'\nINFO: g1: '{}'\nINFO: g2: '{}'\nINFO: cl: '{}'", s1, s2, prevn.getLabel(), g1.getLabel(), g2.getLabel(), curn.getLabel());
			Logger.info("previous: {}", prevp);
			Logger.info("current:  {}", curp);
			prevn.add(g1);
			prevn.add(g2);
			prevp = curp;
			prevn = curn;
		}
		System.out.println("s1:    " + s1);
        System.out.println("s2:    " + s2);
		System.out.println("START: " + start.toString());
	}

	private AlignmentPair handleOverlap(AlignmentPair previous, AlignmentPair current) {
		Logger.info("previous.epos1: {}, previous.epos2: {}, previous.label: '{}'", previous.epos1, previous.epos2, previous.label);
		Logger.info("current.spos1: {}, current.spos2: {}, current.label: '{}'", current.spos1, current.spos2, current.label);
		Logger.info("s1: '{}'\nINFO: s2: '{}'", s1, s2);
		Logger.info("sub1: '{}'\nINFO: sub2: '{}'", s1.substring(previous.epos1), s2.substring(previous.epos2));
	    Logger.info("previous: {}", previous);
        Logger.info("current:  {}", current);
		// if (previous.epos1 > current.spos1) {
		// 	String label = current.label.substring(previous.epos1 - current.spos1);
		// 	return new AlignmentPair(label, current.epos1, current.epos2);
		// }
		// if (previous.epos2 > current.spos2) {
		// 	String label = current.label.substring(previous.epos2 - current.spos2);
		// 	return new AlignmentPair(label, current.epos1, current.epos2);
		// }
		return current;
	}

	private Gap makeGap(int s, int e, String str, Node node) {
		// Logger.info("getGapLabel(" + s + ", " + e + ", " + str + ")");
		// s += 1;
		// e += 1;
		if (s > e) {
			s = e;
		}
		return new Gap(s, e, str, node);
	}
}
