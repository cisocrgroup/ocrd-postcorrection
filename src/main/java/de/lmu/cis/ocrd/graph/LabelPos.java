package de.lmu.cis.ocrd.graph;

public class LabelPos {
	private final int id;
	private Label label;
	private int pos;

	public LabelPos(int pos, Label label, int id) {
		this.id = id;
		this.pos = pos;
		this.label = label;
		while (valid()) {
			if (this.pos < this.label.getLabel().length()) {
				break;
			}
			this.label = label.next(id);
			this.pos = 0;
		}
	}

	public char getChar() {
		return label.getLabel().charAt(pos);
	}

	public int getID() {
		return id;
	}

	public boolean isSynchronization() {
		return label.isSynchronization();
	}

	public LabelPos next() {
		return new LabelPos(pos + 1, label, id);
	}

	public boolean valid() {
		return label != null;
	}
}
