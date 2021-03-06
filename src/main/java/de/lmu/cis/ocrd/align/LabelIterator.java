package de.lmu.cis.ocrd.align;

import java.util.Iterator;

public class LabelIterator implements Iterator<LabelPos> {

	private LabelPos pos;

	public LabelIterator(Label label, int id) {
		pos = new LabelPos(0, label, id);
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof LabelIterator)) {
			return false;
		}
		return pos.equals(((LabelIterator) other).pos);
	}

	@Override
	public int hashCode() {
		return pos.hashCode();
	}

	@Override
	public boolean hasNext() {
		return pos.valid();
	}

	public boolean isSynchronization() {
		return pos.isSynchronization();
	}

	@Override
	public LabelPos next() {
		LabelPos current = pos;
		pos = pos.next();
		return current;
	}
}
