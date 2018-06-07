package de.lmu.cis.iba;

import org.pmw.tinylog.Logger;

import java.util.*;

public class LCS_Alignment_Pairwise {

	public static class AlignmentPair {
		public final int epos1, epos2, spos1, spos2;
		public final String label;

		public AlignmentPair(String label, int epos1, int epos2) {
			this.epos1 = epos1;
			this.epos2 = epos2;
			this.label = label;
			spos1 = epos1 - this.label.length();
			spos2 = epos2 - this.label.length();
		}

		@Override
		public String toString() {
			return String.format("{%s,%d,%d,%d,%d}", label, spos1, epos1, spos2, epos2);
		}
	}

	Online_CDAWG_sym scdawg;
	public ArrayList<ArrayList<LCS_Triple>> longest_common_subsequences = new ArrayList<>();

	Common_SCDAWG_Functions scdawg_functions;

	public LCS_Alignment_Pairwise(String n1, String n2) {

		ArrayList<String> stringset = new ArrayList<>();
		stringset.add("#" + n1 + "$");
		stringset.add("#" + n2 + "$");
		Online_CDAWG_sym scdawg = new Online_CDAWG_sym(stringset, false);
		scdawg.determineAlphabet(false);
		scdawg.build_cdawg();
		this.scdawg = scdawg;

		Common_SCDAWG_Functions scdawg_functions = new Common_SCDAWG_Functions(scdawg);
		this.scdawg_functions = scdawg_functions;

	}

	public String getString(int i) {
		return scdawg.stringset.get(i);
	}

	public void align() {

		Logger.debug("Searching quasi max nodes for s1 and s2 pairs...");

		ArrayList<Endpos_Pair> quasi_max_nodes = scdawg_functions.get_quasi_maximal_nodes_pairwise();
		if (quasi_max_nodes.size() == 0) {
			return;
		}
		Node[] nodes_in_s1 = new Node[scdawg.stringset.get(0).length() + 1];
		HashMap<Node, ArrayList> nodes_endpos_s2 = new HashMap();

		for (Endpos_Pair pair : quasi_max_nodes) {
			Node x = pair.node;
			Logger.debug("-----------------------------------");

			Logger.debug(scdawg.get_node_label(x) + ", s1=" + pair.endpos_s1 + " :: s2=" + pair.endpos_s2);
			for (int e1 : pair.endpos_s1) {
				nodes_in_s1[e1] = pair.node;
			}

			ArrayList e2 = pair.endpos_s2;
			Collections.sort(e2);
			nodes_endpos_s2.put(x, e2);
		}

		Logger.debug("Calculating LCS for s1 and s2 pairs...");

		ArrayList lcs = this.calculate_LCS_quadratic(nodes_in_s1, nodes_endpos_s2);
		this.longest_common_subsequences.add(lcs);
		this.printLCS();

	}

	public ArrayList<Endpos_Pair> reduce_nodes(ArrayList<Endpos_Pair> quasi_max_nodes) {

		final ArrayList<Endpos_Pair> result = new ArrayList<>();
		for (Endpos_Pair pair : quasi_max_nodes) {
			// Logger.debug("LABEL: '" + scdawg.get_node_label(pair.node) + "'");
			if (pair.endpos_s1.size() != pair.endpos_s2.size()) {
				// Logger.debug(" -> not the same lengtheses");
				continue;
			}
			if (pair.endpos_s1.size() != 1) {
				// Logger.debug(" -> not length 1");
				continue;
			}
			// #... and ...$ will always have exactly one alignment.
			// make sure that these two will not get filtered even if the according labels
			// are too short
			final int epos = pair.endpos_s1.get(0);
			final int spos = epos - scdawg.get_node_length(pair.node);
			// Logger.debug("PAIR: " + new
			// AlignmentPair(scdawg.get_node_label(pair.node), pair.endpos_s1.get(0),
			// pair.endpos_s2.get(0)));
			final char letter = scdawg.stringset.get(0).charAt(epos);
			// Logger.debug("ENDNODE: " + letter);
			if (spos != -1 && letter != '$' && scdawg.get_node_length(pair.node) < 3) {
				// Logger.debug(" -> label too short");
				continue;
			}
			// Logger.debug("LENGTH: " + scdawg.get_node_length(pair.node));
			// Logger.debug("LENGTH: " + scdawg.get_node_label(pair.node).length());
			result.add(pair);
		}

		// for (Endpos_Pair pair : result) {
		// Logger.debug("PAIR: " + scdawg.get_node_label(pair.node));
		// }

		/*
		 * BEGIN OLD
		 * 
		 * for(int i=0;i<quasi_max_nodes.size();i++) {
		 * 
		 * Endpos_Pair p1 = quasi_max_nodes.get(i);
		 * 
		 * for(int j=0;j<quasi_max_nodes.size();j++) {
		 * 
		 * Endpos_Pair p2 = quasi_max_nodes.get(j);
		 * 
		 * if(i==j) continue;
		 * 
		 * 
		 * for(int ik=0;ik<p1.endpos_s1.size();ik++) { int e_s1 = p1.endpos_s1.get(ik);
		 * int s_s1 = e_s1-scdawg.get_node_length(p1.node);
		 * 
		 * for(int jk=0;jk<p2.endpos_s1.size();jk++) { int e_s2 = p2.endpos_s1.get(jk);
		 * int s_s2 = e_s2-scdawg.get_node_length(p2.node);
		 * 
		 * if(s_s1>=s_s2&&s_s1<=e_s2&&e_s1>=s_s2&&e_s1<=e_s2) {
		 * System.out.printf("%d,%d,%d,%d ",s_s2,s_s1,e_s1,e_s2);
		 * Logger.debug("remove " + scdawg.get_node_label(p1.node) +" " +
		 * scdawg.get_node_label(p2.node)+ " -> " + p1.endpos_s1.get(ik) );
		 * p1.endpos_s1.remove(ik); }
		 * 
		 * } }
		 * 
		 * for(int ik=0;ik<p1.endpos_s2.size();ik++) { int e_s1 = p1.endpos_s2.get(ik);
		 * int s_s1 = e_s1-scdawg.get_node_length(p1.node);
		 * 
		 * for(int jk=0;jk<p2.endpos_s2.size();jk++) { int e_s2 = p2.endpos_s2.get(jk);
		 * int s_s2 = e_s2-scdawg.get_node_length(p2.node);
		 * 
		 * if(s_s1>=s_s2&&s_s1<=e_s2&&e_s1>=s_s2&&e_s1<=e_s2) { p1.endpos_s2.remove(ik);
		 * }
		 * 
		 * } }
		 * 
		 * 
		 * 
		 * }
		 * 
		 * 
		 * 
		 * }
		 * 
		 * for (Endpos_Pair p : quasi_max_nodes) {
		 * if(p.endpos_s1.size()>0&&p.endpos_s2.size()>0) { result.add(p); } }
		 * 
		 * // END_OLD
		 */

		return result;

	}

	public ArrayList[] build_greedy_cover(Node[] nodes_in_s1, HashMap<Node, ArrayList> nodes_endpos_s2,
			ArrayList<LCS_Triple> lcs_triples, HashMap<Node, Node> node_ancestors) {

		// build greedy cover

		ArrayList[] greedy_cover = new ArrayList[nodes_in_s1.length];

		Node prev = null;

		for (int i = 0; i < nodes_in_s1.length; i++)
			greedy_cover[i] = new ArrayList();

		boolean first_node_found = false;

		for (int i = 0; i < nodes_in_s1.length; i++) {

			if (nodes_in_s1[i] != null) {
				// Logger.debug(scdawg.get_node_label(nodes_in_s1[i])+ i+
				// nodes_endpos_s2.get(nodes_in_s1[i]) );

				if (nodes_endpos_s2.get(nodes_in_s1[i]) == null)
					continue; // Wenn keine Endpos im zweiten String dann nix
				// machen

				// Logger.debug("Size: " + nodes_endpos_s2.get(nodes_in_s1[i]).size());

				for (int j = nodes_endpos_s2.get(nodes_in_s1[i]).size() - 1; j >= 0; j--) { // iterieren
					// über
					// alle

					int dec_elem = (int) nodes_endpos_s2.get(nodes_in_s1[i]).get(j);

					// Logger.debug(scdawg.get_node_label(p.node) + " " +
					// elem_s1 + " :: " + dec_elem);

					lcs_triples.add(new LCS_Triple(i, dec_elem, nodes_in_s1[i]));
					int lcs_index = lcs_triples.size() - 1;

					node_ancestors.put(nodes_in_s1[i], prev);
					prev = nodes_in_s1[i];

					if ((j == nodes_endpos_s2.get(nodes_in_s1[i]).size() - 1) & !first_node_found) { // erstes
																										// element
																										// immer
																										// hinzufügen
																										// // HIER
																										// NUN DIE
																										// INDIZES
																										// ANSTATT
																										// DER WERTE
																										// ABER
																										// IMMER MIT
																										// TRIPEL

						greedy_cover[0].add(lcs_index);
						lcs_triples.get(lcs_index).column_number = 0;
						first_node_found = true;
					}

					else {

						for (int k = 0; k < greedy_cover.length; k++) { // alle
							// cover
							// listen
							// durchgehen

							if (greedy_cover[k].size() == 0) {
								greedy_cover[k].add(lcs_index);
								lcs_triples.get(lcs_index).column_number = k;

								break;
							}

							else if (dec_elem < lcs_triples
									.get((int) greedy_cover[k].get(greedy_cover[k].size() - 1)).endpos_s2) { // wenn
								// kleiner
								// als
								// das
								// letzte
								// dann
								// hinzufügen
								greedy_cover[k].add(lcs_index);
								lcs_triples.get(lcs_index).column_number = k;

								break;
							}

						}

					}

				} // for j

			}

		}

		return greedy_cover;
	}

	// *******************************************************************************
	// calculate_LCS()
	// *******************************************************************************

	public ArrayList<LCS_Triple> calculate_LCS(Node[] nodes_in_s1, HashMap<Node, ArrayList> nodes_endpos_s2) {

		ArrayList<LCS_Triple> lcs_triples = new ArrayList<LCS_Triple>();
		HashMap<Node, Node> node_ancestors = new HashMap<Node, Node>();
		ArrayList[] greedy_cover = build_greedy_cover(nodes_in_s1, nodes_endpos_s2, lcs_triples, node_ancestors);
		this.print_greedy_cover(greedy_cover, lcs_triples);
		// calculate lis

		int i_count = 0;
		for (int i = 0; i < greedy_cover.length; i++) {
			if (greedy_cover[i].size() > 0)
				i_count++;
		}

		ArrayList<LCS_Triple> lis = new ArrayList<LCS_Triple>();
		int x = lcs_triples.get((int) greedy_cover[i_count - 1].get(0)).endpos_s2; // pick
		// any??
		lis.add(lcs_triples.get((int) greedy_cover[i_count - 1].get(0)));

		i_count -= 2;

		while (i_count >= 0) {

			for (int j = 0; j < greedy_cover[i_count].size(); j++) {
				if (lcs_triples.get((int) greedy_cover[i_count].get(j)).endpos_s2 < x) {
					lis.add(0, lcs_triples.get((int) greedy_cover[i_count].get(j)));
					x = lcs_triples.get((int) greedy_cover[i_count].get(j)).endpos_s2;
					break;
				}
			}

			i_count--;
		}
		return lis;

	}
	
	 // *******************************************************************************
    // calculate_LCS_quadratic()
    // *******************************************************************************

    public ArrayList<LCS_Triple> calculate_LCS_quadratic(Node[] nodes_in_s1, HashMap<Node, ArrayList> nodes_endpos_s2) {

	ArrayList<LCS_Triple> lcs_triples = new ArrayList<LCS_Triple>();
	HashMap<Node, Node> node_ancestors = new HashMap<Node, Node>();
	ArrayList[] greedy_cover = build_greedy_cover(nodes_in_s1, nodes_endpos_s2, lcs_triples, node_ancestors);
	 this.print_greedy_cover(greedy_cover, lcs_triples);
	// calculate lis

	int i_count = 0;
	for (int i = 0; i < greedy_cover.length; i++) {
	    if (greedy_cover[i].size() > 0)
		i_count++;
	}

	ArrayList<LCS_Triple> lis = new ArrayList<LCS_Triple>();
	int x=0;
    for (int j = 0; j < greedy_cover[i_count-1].size(); j++) {
    	int e2 = lcs_triples.get((int) greedy_cover[i_count - 1].get(j)).endpos_s2; // pick
    	if(scdawg.stringset.get(1).charAt(e2-1)=='$') {
    		lis.add(lcs_triples.get((int) greedy_cover[i_count - 1].get(j)));
    		x= e2;
    		break;
    	}
    }

	i_count -= 2;

	while (i_count >= 0) {

	    for (int j = 0; j < greedy_cover[i_count].size(); j++) {
		if (lcs_triples.get((int) greedy_cover[i_count].get(j)).endpos_s2 < x) {
			int max_length = scdawg.get_node_length(lcs_triples.get((int) greedy_cover[i_count].get(j)).node);
			int arg_max = j;
			for (int k = j + 1;k<greedy_cover[i_count].size();k++) {
				int k_length = scdawg.get_node_length(lcs_triples.get((int) greedy_cover[i_count].get(k)).node);
				if(k_length>max_length) { // >= oder > ??
					max_length = k_length; 
					arg_max = k;
				}
			}
			x = lcs_triples.get((int) greedy_cover[i_count].get(arg_max)).endpos_s2;
		    lis.add(0, lcs_triples.get((int) greedy_cover[i_count].get(arg_max)));
		    break;
		}
	    }

	    i_count--;
	}
	return lis;

    }

	// *******************************************************************************
	// calculate_LCS_graph()
	// *******************************************************************************

	public ArrayList<ArrayList<LCS_Triple>> calculate_LCS_graph(Node[] nodes_in_s1, HashMap<Node, ArrayList> nodes_endpos_s2) {

		ArrayList<ArrayList<LCS_Triple>> result = new ArrayList<ArrayList<LCS_Triple>>();

		ArrayList<LCS_Triple> lcs_triples = new ArrayList<LCS_Triple>();
		HashMap<Node, Node> node_ancestors = new HashMap<Node, Node>();

		ArrayList[] greedy_cover = build_greedy_cover(nodes_in_s1, nodes_endpos_s2, lcs_triples, node_ancestors);
		this.print_greedy_cover(greedy_cover, lcs_triples);
		Iterator it = node_ancestors.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();
			Logger.debug("Knoten: " + scdawg.get_node_label((Node) pair.getKey()) + "vorgänger "
					+ scdawg.get_node_label((Node) pair.getValue()));
		}

		// calculate lis

		// LIS_Graph lis_graph = new LIS_Graph();

		// int i_count = 0;
		// for (int i = 0; i < greedy_cover.length; i++) {
		// if (greedy_cover[i].size() > 0)
		// i_count++;
		// }
		// determineLis(greedy_cover, lcs_triples, node_ancestors, lis_graph,
		// lis_graph.root, i_count - 1);
		//
		return result;

	}

	public void determineLis(ArrayList[] greedy_cover, ArrayList<LCS_Triple> lcs_triples,
			HashMap<Node, Node> node_ancestors, LIS_Graph lis_graph, Node node, int i) {

		Logger.debug(greedy_cover[i].size());
		for (int x = 0; x < greedy_cover[i].size(); x++) {
			Node child_node = lcs_triples.get((int) greedy_cover[i].get(x)).node;
			node.children.put(0, child_node);

			Logger.debug("Knoten: " + scdawg.get_node_label(child_node) + "  vorgänger "
					+ scdawg.get_node_label(node_ancestors.get(child_node)));

			Node ancestor = node_ancestors.get(child_node);

			if (ancestor == null)
				break;

			int next_column = 0;
			for (LCS_Triple t : lcs_triples) {
				if (t.node.equals(ancestor)) {
					next_column = t.column_number;
				}
			}
			Logger.debug("xxx " + next_column);
			Logger.debug("yyy " + lcs_triples.get((int) greedy_cover[i].get(x)).column_number);

			// wir müssen i berechenen = Spalte im cover in der k' vorkommt.

			determineLis(greedy_cover, lcs_triples, node_ancestors, lis_graph, child_node, next_column);

			// dann müssen alle knoten die unterhalb von k' also k'' usw. berechnet werden
			// und die rekursion mit jedem k''gestartet werden.

		}

		// int x = lcs_triples.get((int) greedy_cover[i_count - 1].get(0)).endpos_s2; //
		// pick any??/
		// lis.add(lcs_triples.get((int) greedy_cover[i_count - 1].get(0)));

	}

	public ArrayList<AlignmentPair> getAligmentPairs() {
		ArrayList<AlignmentPair> res = new ArrayList<AlignmentPair>();
		for (int u = 0; u < longest_common_subsequences.size(); u++) {
			ArrayList<LCS_Triple> lis = longest_common_subsequences.get(u);
			if (lis == null) {
				res.add(new AlignmentPair("", 0, 0));
				continue;
			}
			for (int i = 0; i < lis.size(); i++) {
				int e1 = lis.get(i).endpos_s1;
				int e2 = lis.get(i).endpos_s2;
				String nodelabel = scdawg.get_node_label(lis.get(i).node);
				res.add(new AlignmentPair(nodelabel, e1, e2));
			}
		}
		return res;
	}

	public void printLCS() {

		for (int u = 0; u < this.longest_common_subsequences.size(); u++) {
			ArrayList<LCS_Triple> lis = this.longest_common_subsequences.get(u);
			for (int i = 0; i < lis.size(); i++) {
				int e1 = lis.get(i).endpos_s1;
				int e2 = lis.get(i).endpos_s2;
				String nodelabel = scdawg.get_node_label(lis.get(i).node);
				Logger.debug("\"{}\":e1:\"{}\", e2:\"{}\"", nodelabel, e1, e2);
			}
		}

	}

	public void print_greedy_cover(ArrayList[] greedy_cover, ArrayList<LCS_Triple> lcs_triples) {

		for (int i = 0; i < greedy_cover.length; i++) {
			if (greedy_cover[i].size() == 0)
				continue;
			for (int j = 0; j < greedy_cover[i].size(); j++) {
				LCS_Triple t = lcs_triples.get((int) greedy_cover[i].get(j));
				Logger.debug(t.endpos_s2 + " (" + scdawg.get_node_label(t.node) + ")");
			}
			Logger.debug("-------------------------------");
		}

	}
	// *******************************************************************************
	// LCS_to_JSONString()
	// *******************************************************************************

	public String LCS_to_JSONString() {

		String result = "{";

		for (int u = 0; u < this.longest_common_subsequences.size(); u++) {

			ArrayList<LCS_Triple> lis = this.longest_common_subsequences.get(u);

			String alignment = "[";

			String d = "";

			for (int i = 0; i < lis.size(); i++) {
				// Logger.debug(lis.get(i).endpos_s1+" "+lis.get(i).endpos_s2+"
				// "+scdawg.get_node_label(lis.get(i).node));
				String nodelabel = scdawg.get_node_label(lis.get(i).node).replace("\"", "\\\"");
				alignment += d + "{\"endpos_s1\":\"" + lis.get(i).endpos_s1 + "\",\"endpos_s2\":\""
						+ lis.get(i).endpos_s2 + "\",\"nodelabel\":\"" + nodelabel + "\"}";
				d = ",";
			}

			alignment += "]";

			result += "\"alignment" + u + "\":" + alignment;
			if (u < scdawg.stringset.size() - 2)
				result += ",";

		} // for u

		return result + "}";

	}

	// *******************************************************************************
	// *******************************************************************************
	// New Methods find quasi maximal Nodes ::::=>
	// *******************************************************************************
	// *******************************************************************************

	public void align_new() {

		HashSet<Node> candidates = this.find_quasi_max_nodes();

		HashMap<Node, Integer> result = new HashMap<Node, Integer>();

		ArrayList<NavigableMap<Integer, Node>> filtered_candidates = this.filter_quasi_max_nodes(candidates);
		ArrayList<NavigableMap<Integer, Node>> reduced_candidates = this
				.remove_included_substrings(filtered_candidates);

		Logger.debug("---------------------------------");

		for (int i = 0; i < reduced_candidates.size(); i++) {

			for (Map.Entry<Integer, Node> entry : reduced_candidates.get(i).entrySet()) {

				Node currentNode = entry.getValue();
				int endpos_n1 = scdawg.get_node_length(currentNode) + entry.getKey() - 1;
				Logger.debug(scdawg.get_node_label((Node) entry.getValue()) + " " + entry.getKey() + " "
						+ (scdawg.get_node_length(currentNode) + entry.getKey() - 1));
			}
			Logger.debug("XXXXXXXXXXXXXXXXXXXXXXXXXXX");

		}

	}

	public boolean has_transitions_to_sinks(Node node) {
		Iterator it = node.children.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();
			Node child = (Node) pair.getValue();
			for (Node sink : scdawg.sinks) {
				if (child.equals(sink))
					return true;
			}
		}

		Iterator it2 = node.children_left.entrySet().iterator();
		while (it2.hasNext()) {
			Map.Entry pair = (Map.Entry) it2.next();
			Node child = (Node) pair.getValue();
			for (Node sink : scdawg.sinks) {
				if (child.equals(sink))
					return true;
			}
		}

		return false;
	}

	public HashSet<Node> find_quasi_max_nodes() {

		HashSet<Node> candidates = new HashSet<Node>();

		for (int i = 0; i < scdawg.all_nodes.size(); i++) {
			Node node = scdawg.all_nodes.get(i);
			if (node == scdawg.root)
				continue;
			if (has_transitions_to_sinks(node))
				candidates.add(node);

		}

		// for(Node n : candidates) {
		// Logger.debug(scdawg.get_node_label(n));
		// }

		return candidates;
	}

	public ArrayList<NavigableMap<Integer, Node>> filter_quasi_max_nodes(HashSet<Node> candidates) {
		ArrayList<NavigableMap<Integer, Node>> result = new ArrayList<NavigableMap<Integer, Node>>();

		for (int i = 0; i < scdawg.stringset.size(); i++) {
			result.add(new TreeMap<Integer, Node>());
		}

		for (Node n : candidates) {

			Iterator it2 = n.children.entrySet().iterator();
			while (it2.hasNext()) {
				Map.Entry pair = (Map.Entry) it2.next();

				for (Node sink : scdawg.sinks) {
					Node child = (Node) pair.getValue();
					if (sink.equals(child)) {

						int pos = scdawg.stringset.get(child.stringnr).length()
								- (scdawg.get_node_length(n) + scdawg.get_edge_length((int) pair.getKey(), n, child));

						if (!result.get(child.stringnr).containsKey(pos)) {
							result.get(child.stringnr).put(pos, n);
						} else {
							if (scdawg.get_node_length(result.get(child.stringnr).get(pos)) < scdawg
									.get_node_length(n)) {
								result.get(child.stringnr).put(pos, n);
							}
						}

					}
				}

			}

			// LEFT EDGES
			//
			Iterator it3 = n.children_left.entrySet().iterator();
			while (it3.hasNext()) {
				Map.Entry pair = (Map.Entry) it3.next();

				for (Node sink : scdawg.sinks) {
					Node child = (Node) pair.getValue();
					if (sink.equals(child)) {

						int pos = scdawg.get_edge_label_left((int) pair.getKey(), n, child).length();

						if (!result.get(child.stringnr).containsKey(pos)) {
							result.get(child.stringnr).put(pos, n);
						} else {
							if (scdawg.get_node_length(result.get(child.stringnr).get(pos)) < scdawg
									.get_node_length(n)) {
								result.get(child.stringnr).put(pos, n);
							}
						}

					}
				}

			}

			for (NavigableMap<Integer, Node> map : result) {
				map = (NavigableMap<Integer, Node>) Util.sortByKeys(map);
			}

		}

		return result;

	}

	public ArrayList<NavigableMap<Integer, Node>> remove_included_substrings(
			ArrayList<NavigableMap<Integer, Node>> filtered_candidates) {

		ArrayList<NavigableMap<Integer, Node>> result = new ArrayList<NavigableMap<Integer, Node>>();

		for (int i = 0; i < scdawg.stringset.size(); i++) {
			result.add(new TreeMap<Integer, Node>());
		}

		for (int i = 0; i < filtered_candidates.size(); i++) {

			Node n1 = null;
			boolean found = false;

			Map.Entry<Integer, Node> currentPair = null;
			Map.Entry<Integer, Node> comparePair = null;

			for (Map.Entry<Integer, Node> entry : filtered_candidates.get(i).entrySet()) {

				Map.Entry<Integer, Node> prev = filtered_candidates.get(i).lowerEntry(entry.getKey()); // previous

				if (prev == null) {
					currentPair = entry;

					result.get(i).put(currentPair.getKey(), currentPair.getValue());
					found = false;
				}

				else {

					comparePair = entry;
					int endpos_n2 = scdawg.get_node_length(comparePair.getValue()) + comparePair.getKey() - 1;
					int endpos_n1 = scdawg.get_node_length(currentPair.getValue()) + currentPair.getKey() - 1;

					if (endpos_n2 > endpos_n1) {
						currentPair = entry;
						result.get(i).put(currentPair.getKey(), currentPair.getValue());

					}
				}

			}

		}
		return result;
	}

}
