package de.lmu.cis.ocrd.graph;

import java.util.Iterator;

public class LabelIterator implements Iterator<LabelPos> {

	private LabelPos pos;

	public LabelIterator(Label label, int id) {
		pos = new LabelPos(0, label, id);
	}

	@Override
	public boolean hasNext() {
		return pos.valid();
	}

	@Override
	public LabelPos next() {
		LabelPos current = pos;
		pos = pos.next();
		return current;
	}
}
