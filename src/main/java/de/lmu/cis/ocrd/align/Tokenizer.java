package de.lmu.cis.ocrd.align;

public class Tokenizer {
	public interface Visitor {
		public void visit(String a, String b);
	}

	private final Alignment a;

	public Tokenizer(AlignmentGraph g) {
		a = new Alignment().addAll(g.getStartNode());
	}

	public void eachPair(Visitor v) {
		int i1 = 0;
		int i2 = 0;
		StringBuilder s1 = new StringBuilder();
		StringBuilder s2 = new StringBuilder();
		while (i1 < a.size() && i2 < a.size()) {
			s1.setLength(0);
			s2.setLength(0);
			i1 = nextToken(i1, 0, s1);
			i2 = nextToken(i2, 1, s2);
			// System.out.println("in outer while: " + i1 + ", " + i2 + " (" +
			// a.get(i1).isAligned() + ", "
			// + a.get(i2).isAligned() + ")");
			if (s1.toString().isEmpty() && s2.toString().isEmpty()) {
				return;
			}
			v.visit(s1.toString(), s2.toString());
			while (i1 < a.size() && i2 < a.size() && i1 != i2) {
				if (!a.get(i1).isAligned() || i1 < i2) {
					s1.setLength(0);
					i1 = nextToken(i1, 0, s1);
				}
				if (!a.get(i2).isAligned() || i2 < i1) {
					s2.setLength(0);
					i2 = nextToken(i2, 1, s2);
				}
				// System.out.println("in inner while: " + i1 + ", " + i2 + " (" +
				// a.get(i1).isAligned() + ", "
				// + a.get(i2).isAligned() + ")");
				v.visit(s1.toString(), s2.toString());
			}
		}
	}

	private int nextToken(int i, int id, StringBuilder s) {
		assert (a.get(i).isEndOfToken(id));
		for (i++; i < a.size(); i++) {
			Alignment.Tuple tuple = a.get(i);
			if (tuple.isEndOfToken(id)) {
				return i;
			}
			if (!tuple.isSkip(id)) {
				s.append(tuple.get(id));
			}
		}
		return a.size();
	}
}
