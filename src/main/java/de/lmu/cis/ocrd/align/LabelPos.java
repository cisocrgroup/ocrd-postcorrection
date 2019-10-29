package de.lmu.cis.ocrd.align;

import java.util.Objects;

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
			this.label = this.label.next(this.id);
			this.pos = 0;
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, label, pos);
	}

	// Compare LabelPos based on the label and the position.
	// Does *not* compare them based on the id.
	@Override
	public boolean equals(Object other) {
		if (other == null || !(other instanceof LabelPos)) {
			return false;
		}
		final LabelPos pos = (LabelPos) other;
		// check if both point to the same label with the same position
		return label == pos.label && this.pos == pos.pos;

	}

	public char getChar() {
		return label.getLabel().charAt(pos);
	}

	public int getID() {
		return id;
	}

	public boolean isSynchronization() {
		return label == null ? false : label.isSynchronization();
	}

	public LabelPos next() {
		return new LabelPos(pos + 1, label, id);
	}

	public boolean valid() {
		return label != null;
	}
}
