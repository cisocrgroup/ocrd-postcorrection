package de.lmu.cis.iba;

public class LCS_Triple {

	public int endpos_s1;
	public int endpos_s2;
	public int column_number;
	public Node node;
	public int idx_ancestor = 0;

	public LCS_Triple(int endpos_s1, int endpos_s2, Node node) {
		this.endpos_s1 = endpos_s1;
		this.endpos_s2 = endpos_s2;
		this.node = node;

	}

}
