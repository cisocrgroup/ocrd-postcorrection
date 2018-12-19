package de.lmu.cis.ocrd.train;

import de.lmu.cis.ocrd.ml.Token;

public class TrainSetSplitter {
	private final int n;
	private final Tokenizer tokenizer;
	public TrainSetSplitter(Tokenizer tokenizer, int n) {
		this.tokenizer = tokenizer;
		this.n = n;
	}

	public void eachToken(Visitor v) throws Exception {
		Counter c = new Counter(n);
		tokenizer.eachToken((token) -> {
			c.inc(token);
			v.visit(token, c.isTrain());
		});
	}

	public interface Visitor {
		void visit(Token token, boolean isTrain) throws Exception;
	}

	private class Counter {
		private final int n;
		private int i, lastPageID;

		Counter(int n) {
			this.i = 0;
			this.n = n;
			this.lastPageID = -1;
		}

		void inc(Token token) {
			if (token.getMasterOCR().getLine().getPageId() != lastPageID) {
				i++;
				lastPageID = token.getMasterOCR().getLine().getPageId();
			}
		}

		boolean isTrain() {
			if (n < 1) {
				return true;
			}
			return ((i + 1) % n) == 0;
		}
	}
}
