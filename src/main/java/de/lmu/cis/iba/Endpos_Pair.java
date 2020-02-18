package de.lmu.cis.iba;

import java.util.ArrayList;

public class Endpos_Pair {

	public Node node;
	public ArrayList<Integer> endpos_s2 = new ArrayList<Integer>();
	public ArrayList<Integer> endpos_s1 = new ArrayList<Integer>();

	public Endpos_Pair(Node node, ArrayList endpos_s1, ArrayList endpos_s2) {
		this.endpos_s1 = endpos_s1;
		this.endpos_s2 = endpos_s2;

		this.node = node;
	}


}
