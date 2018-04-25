package de.lmu.cis.ocrd.align;

public class Traverser {
	public interface Visitor {
		public void visit(Label label);
	}

	private final Node node;

	public Traverser(Graph g) {
		node = g.getStartNode();
	}

	public void eachLabel(int id, Visitor v) {
		Label label = node;
		while (label != null) {
			v.visit(label);
			label = label.next(id);
		}
	}
}
