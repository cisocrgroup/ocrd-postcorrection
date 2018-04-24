package de.lmu.cis.ocrd.graph;

public class Tokenizer {
	public interface Visitor {
		public void visit(String a, String b);
	}

	private final Node node;

	public Tokenizer(Node node) {
		this.node = node;
	}

	public void eachPair(Visitor v) {

	}
}
