package de.lmu.cis.ocrd.align;

import java.util.ArrayList;
import java.util.Iterator;

public class Alignment implements Iterable<Alignment.Tuple> {

	private final ArrayList<Tuple> tuples = new ArrayList<Tuple>();

	public Alignment addAll(Node node) {
		while (node != null) {
			add(node);
			Gap g1 = node.next(0);
			Gap g2 = node.next(1);
			if (g1 == null || g2 == null) {
				break;
			}
			add(g1, g2);
			node = g1.next(0);
		}
		return this;
	}

	public Tuple get(int i) {
		return tuples.get(i);
	}

	@Override
	public Iterator<Tuple> iterator() {
		return tuples.iterator();
	}

	public int size() {
		return tuples.size();
	}

	private void add(Gap a, Gap b) {
		final String l1 = a.getLabel();
		final String l2 = b.getLabel();
		final int n1 = l1.length();
		final int n2 = l2.length();
		final int max = Math.max(n1, n2);
		final int min = Math.min(n1, n2);

		int i;
		for (i = 0; i < min; i++) {
			tuples.add(new Tuple(l1.charAt(i), l2.charAt(i), false));
		}
		for (; i < max; i++) {
			if (i < n1) {
				tuples.add(new Tuple(l1.charAt(i), '\0', false));
			} else {
				tuples.add(new Tuple('\0', l2.charAt(i), false));
			}
		}
	}

	private void add(Node node) {
		for (char c : node.getLabel().toCharArray()) {
			tuples.add(new Tuple(c, c, true));
		}
	}

	public class Tuple {
		private final char[] chars;
		private final boolean isAligned;

		private Tuple(char first, char second, boolean isAligned) {
			chars = new char[2];
			chars[0] = first;
			chars[1] = second;
			this.isAligned = isAligned;
		}

		public char get(int i) {
			return chars[i];
		}

		public boolean isAligned() {
			return isAligned;
		}

		public boolean isEndOfToken(int i) {
			switch (chars[i]) {
				case '$':
				case '#':
				case ' ':
					return true;
				default:
					return false;
			}
		}

		public boolean isSkip(int i) {
			return get(i) == 0;
		}
	}
}
