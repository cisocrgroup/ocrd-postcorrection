package de.lmu.cis.ocrd.graph;

import java.util.ArrayList;

class Node implements Label {
	private ArrayList<Gap> gaps;
	private final String label;

	public Node(String label) {
		this.label = label;
	}

	public Node add(Gap gap) {
		if (gaps == null) {
			gaps = new ArrayList<Gap>();
		}
		gaps.add(gap);
		return this;
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public boolean isSynchronization() {
		return true;
	}

	@Override
	public Label next(int id) {
		if (gaps == null) {
			return null;
		}
		return gaps.get(id);
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
		if (gaps == null) {
			return;
		}
		int id = 0;
		for (Gap g : gaps) {
			builder.append("\"" + label + "\"");
			builder.append(" -> ");
			builder.append("\"" + g.next(0).getLabel() + "\"");
			builder.append(" [label=\"" + id + ":" + g.toString() + "\"] // dotcode\n");
			++id;
		}
		gaps.get(0).next(0).appendDot(builder);
	}

	private void traverse(int id, StringBuilder builder) {
		builder.append(label);
		if (gaps == null) {
			return;
		}
		builder.append(gaps.get(id).getLabel());
		gaps.get(id).next(id).traverse(id, builder);
	}
}