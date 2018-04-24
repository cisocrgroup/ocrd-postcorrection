package de.lmu.cis.ocrd.graph;

class Gap {
	final int id;
	final String o;
	final Node target;

	Gap(int id, String o, Node t) {
		this.id = id;
		this.o = o;
		target = t;
	}

	@Override
	public String toString() {
		String output = o;
		if (output.length() == 0) {
			output = "ε";
		}
		if (" ".equals(output)) {
			output = "<SP>";
		}
		return id + ":" + output;
	}
}