package de.lmu.cis.ocrd.align;

import org.pmw.tinylog.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TokenAlignment implements Iterable<TokenAlignment.Token> {

	private final ArrayList<Token> alignments = new ArrayList<>();
	private final String master;

	public TokenAlignment(String master) {
		this.master = master;
		for (String token : this.master.split("\\s+")) {
			alignments.add(new Token(token));
		}
	}

	public TokenAlignment add(String str) {
		final Graph graph = new Graph(master, str);
		Logger.debug("Alignment: {}", graph.getStartNode().toString());
		final Tokenizer t = graph.getTokenizer();

		Counter i = new Counter(-1); // really, java?
		t.eachPair((a, b, anew, bnew) -> {
			// System.out.printf("a: %s, b: %s, anew: %b, bnew: %b\n", a, b, anew, bnew);
			if (anew) {
				i.inc();
				alignments.get(i.get()).addNewAlignment(b);
			} else { // !anew; ignore bnew
				alignments.get(i.get()).addExistingAlignment(b);
			}
		});
		return this;
	}

	public Token get(int i) {
		return alignments.get(i);
	}

	@Override
	public Iterator<Token> iterator() {
		return alignments.iterator();
	}

	public int size() {
		return alignments.size();
	}

	public class Token {
		private final ArrayList<ArrayList<String>> alignments = new ArrayList<>();
		private final String master;

		public Token(String master) {
			this.master = master;
		}

		public void addExistingAlignment(String b) {
			if (alignments.isEmpty()) {
				throw new IndexOutOfBoundsException("no earlier alignment exists");
			}
			alignments.get(alignments.size() - 1).add(b);
		}

		public void addNewAlignment(String b) {
			alignments.add(new ArrayList<>());
			addExistingAlignment(b);
		}

		public List<String> getAlignment(int i) {
			if (!(i < alignments.size())) {
				return new ArrayList<>();
			}
			return alignments.get(i);
		}

		public String getMaster() {
			return master;
		}

		@Override
		public String toString() {
			StringBuilder str = new StringBuilder(master);
			for (ArrayList<String> als : alignments) {
				char pre = '|';
				for (String s : als) {
					str.append(pre);
					str.append(s);
					pre = ',';
				}
			}
			return str.toString();
		}
	}

	// counter to handle i++ in a closure.
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
}
