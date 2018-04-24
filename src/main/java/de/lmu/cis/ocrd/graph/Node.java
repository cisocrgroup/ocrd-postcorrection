package de.lmu.cis.ocrd.graph;

import java.util.ArrayList;

public class Node {
	final ArrayList<Gap> gaps;
	private final AlignmentGraph graph;
	private final String label;

	Node(String label, AlignmentGraph graph) {
		this.label = label;
		this.graph = graph;
		gaps = new ArrayList<Gap>();
	}

	public String toDot() {
		StringBuilder builder = new StringBuilder();
		builder.append("digraph g { // dotcode\n");
		builder.append("rankdir=LR; // dotcode\n");
		appendDot(builder);
		builder.append("} // dotcode\n");
		return builder.toString();
	}

	public String traverse(int id) {
		StringBuilder builder = new StringBuilder();
		this.traverse(id, builder);
		return builder.toString();
	}

	private void appendDot(StringBuilder builder) {
		builder.append("\"" + label + "\"");
		builder.append(" [label=\"" + label + "\"] // dotcode\n");
		for (Gap g : gaps) {
			builder.append("\"" + label + "\"");
			builder.append(" -> ");
			builder.append("\"" + g.target.label + "\"");
			builder.append(" [label=\"" + g.toString() + "\"] // dotcode\n");
		}
		if (!gaps.isEmpty()) {
			gaps.get(0).target.appendDot(builder);
		}
	}

	private void traverse(int id, StringBuilder builder) {
		builder.append(label);
		if (!gaps.isEmpty()) {
			builder.append(gaps.get(id).o);
			gaps.get(id).target.traverse(id, builder);
		}
	}
}