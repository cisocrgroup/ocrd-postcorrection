package de.lmu.cis.ocrd.align;

import java.util.ArrayList;

public class Node implements Label {
	private ArrayList<Gap> gaps;
	private final String label;

	public Node(String label) {
		this.label = label;
	}

	public Node add(Gap gap) {
		if (gaps == null) {
			gaps = new ArrayList<>();
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
	public Gap next(int id) {
		if (gaps == null) {
			return null;
		}
		return gaps.get(id);
	}

	@Override
    public String toString() {
	    StringBuilder builder = new StringBuilder(getLabel());
	    boolean first = true;
	    if (gaps == null)  {
	        return builder.toString();
        }
        builder.append('[');
	    for (Gap gap: gaps) {
	        if (!first) {
	            builder.append('|');
            }
            builder.append(gap.getLabel());
	        first = false;
        }
        builder.append(']');
	    if (gaps.isEmpty()) {
	        return builder.toString();
        }
	    builder.append(gaps.get(0).next(0).toString());
	    return builder.toString();
    }

	String toDot() {
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
		builder.append("\"").append(label).append("\"");
		builder.append(" [label=\"").append(label).append("\"] // dotcode\n");
		if (gaps == null) {
			return;
		}
		int id = 0;
		for (Gap g : gaps) {
			builder.append("\"").append(label).append("\"");
			builder.append(" -> ");
			builder.append("\"").append(g.next(0).getLabel()).append("\"");
			builder.append(" [label=\"").append(id).append(":").append(g.toString()).append("\"] // dotcode\n");
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