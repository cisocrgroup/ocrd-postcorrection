package de.lmu.cis.ocrd.graph;

public class Tokenizer {
	public interface Visitor {
		public void visit(String a, String b);
	}

	private final Traverser traverser;

	public Tokenizer(Traverser traverser) {
		this.traverser = traverser;
	}

	public void eachPair(Visitor v) {

	}
}
