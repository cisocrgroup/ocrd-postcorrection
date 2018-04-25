package de.lmu.cis.ocrd.align;

class Gap implements Label {
	private final int e;
	private final String label;
	private final int s;
	private final Node target;

	public Gap(int s, int e, String label, Node target) {
		this.s = s;
		this.e = e;
		this.label = label;
		this.target = target;
	}

	@Override
	public String getLabel() {
		return label.substring(s, e);
	}

	@Override
	public boolean isSynchronization() {
		return false;
	}

	@Override
	public Node next(int id) {
		return target;
	}

	@Override
	public String toString() {
		if (getLabel().isEmpty()) {
			return "ε";
		}
		if (" ".equals(getLabel())) {
			return "<SP>";
		}
		return getLabel();
	}
}