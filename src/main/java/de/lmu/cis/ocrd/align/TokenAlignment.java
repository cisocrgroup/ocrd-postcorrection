package de.lmu.cis.ocrd.align;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TokenAlignment implements Iterable<TokenAlignment.Token> {

	public class Token {
		private final ArrayList<ArrayList<String>> alignments;
		private final String master;

		public Token(String master) {
			this.master = master;
			alignments = new ArrayList<ArrayList<String>>();
		}

		public void addExistingAlignment(String b) {
			if (alignments.isEmpty()) {
				throw new IndexOutOfBoundsException("no earlier alignment exists");
			}
			alignments.get(alignments.size() - 1).add(b);
		}

		public void addNewAlignment(String b) {
			alignments.add(new ArrayList<String>());
			addExistingAlignment(b);
		}

		public List<String> getAlignment(int i) {
			return alignments.get(i);
		}

		public String getMaster() {
			return master;
		}
	}

	private class Counter {
		int i;

		Counter(int i) {
			this.i = i;
		}

		int get() {
			return i;
		}

		void inc() {
			i++;
		}
	}

	private final ArrayList<Token> alignments = new ArrayList<Token>();

	private final String master;

	public TokenAlignment(String master) {
		this.master = master;
		for (String token : this.master.split("\\s+")) {
			alignments.add(new Token(token));
		}
	}

	public TokenAlignment add(String str) {
		final Tokenizer t = new Graph(master, str).getTokenizer();
		Counter i = new Counter(-1); // really, java?
		t.eachPair((a, b, anew, bnew) -> {
			if (anew) {
				i.inc();
				alignments.get(i.get()).addNewAlignment(b);
			} else { // !anew; ignore bnew
				alignments.get(i.get()).addExistingAlignment(b);
			}
		});
		return this;
	}

	@Override
	public Iterator<Token> iterator() {
		return alignments.iterator();
	}
}
