package de.lmu.cis.iba;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

public class Node {

	public int id;
	public int pathlength;
	public int end;
	public int start;
	public Node suffixLink;
	public Node prev;
	public int stringnr;
	public HashMap<Integer, Node> children;
	public HashMap<Integer, Node> children_left;

	public HashMap<Integer, EdgeInfo> children_new;
	public HashMap<Integer, EdgeInfo> children_left_new;

	public ArrayList<Integer> stringnumbers = new ArrayList<Integer>();

	public boolean is_endNode = false;

	public Multimap<Integer, Integer> end_positions;
	public Multimap<Integer, Integer> start_positions;

	public boolean right_end = false;

	// public org.neo4j.graphdb.Node dB_Node;

	public Node(int _start, int _end, int _pathlength, int _stringnr, int _id_cnt) {
		id = _id_cnt++;
		end = _end;
		start = _start;
		pathlength = _pathlength;
		stringnr = _stringnr;

		suffixLink = null;

		children = Maps.newHashMap();
		children_left = Maps.newHashMap();

		children_new = Maps.newHashMap();
		children_left_new = Maps.newHashMap();

		// children_pos = Maps.newHashMap();
		// children_left_pos = Maps.newHashMap();

	} // Node()

	public Node() {
		id = -2;
		children = Maps.newHashMap();

	}

}
