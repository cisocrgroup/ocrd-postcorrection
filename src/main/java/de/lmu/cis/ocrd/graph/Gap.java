package de.lmu.cis.ocrd.graph;

class Gap implements Label {
	private final String label;
	private final Node target;

	public Gap(String label, Node target) {
		this.label = label;
		this.target = target;
	}

	@Override
	public String getLabel() {
		return label;
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
		if (label.isEmpty()) {
			return "ε";
		}
		if (" ".equals(label)) {
			return "<SP>";
		}
		return label;
	}
}