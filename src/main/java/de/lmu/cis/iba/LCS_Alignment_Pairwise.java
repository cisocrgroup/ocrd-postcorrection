package de.lmu.cis.iba;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.pmw.tinylog.Logger;



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
	public ArrayList<ArrayList<LCS_Triple>> longest_common_subsequences = new ArrayList<ArrayList<LCS_Triple>>();

	Common_SCDAWG_Functions scdawg_functions;

	public LCS_Alignment_Pairwise(String n1,String n2) {

		ArrayList<String> stringset = new ArrayList<String>();
		stringset.add("#" + n1 + "$");
		stringset.add("#" + n2 + "$");
		Online_CDAWG_sym scdawg = new Online_CDAWG_sym(stringset, false);
		scdawg.determineAlphabet(false);
		scdawg.build_cdawg();
		this.scdawg = scdawg;

		this.scdawg = scdawg;
		Common_SCDAWG_Functions scdawg_functions = new Common_SCDAWG_Functions(scdawg);
		this.scdawg_functions = scdawg_functions;

	}

	public void align() {
		Logger.debug("Searching quasi max nodes for s1 and s2 pairs...");
		ArrayList<Endpos_Pair> quasi_max_nodes = scdawg_functions.get_quasi_maximal_nodes_pairwise();
		if (quasi_max_nodes == null) {
			return;
		}
		ArrayList<Endpos_Pair> quasi_max_sorted = Util.sortEndposPair(quasi_max_nodes, "ASC", scdawg);		// nach dem ersten String sortieren.
	  	ArrayList<Endpos_Pair> quasi_max_nodes_reduced = reduce_nodes(quasi_max_sorted);

		
		for (Endpos_Pair pair : quasi_max_nodes_reduced) {
			Node x = pair.node;

			Logger.debug(scdawg.get_node_label(x) + ", s1= " + pair.endpos_s1 + " :: s2=" + pair.endpos_s2);

			Logger.debug("\n--------------------------------------------");
		}
		
		Logger.debug("Calculating LCS for s1 and s2 pairs...");

		ArrayList lcs = this.calculate_LCS(quasi_max_sorted);
		this.longest_common_subsequences.add(lcs);
		this.printLCS();

	}
	
	public ArrayList<Endpos_Pair> reduce_nodes (ArrayList<Endpos_Pair> quasi_max_nodes){
		
ArrayList<Endpos_Pair> result = new ArrayList<Endpos_Pair>();
		
		
		for(int i=0;i<quasi_max_nodes.size();i++) {
			
			Endpos_Pair p1 = quasi_max_nodes.get(i);
			
			for(int j=0;j<quasi_max_nodes.size();j++) {
				
				Endpos_Pair p2 = quasi_max_nodes.get(j);
				
				if(i==j)
					continue;
				
					
					for(int ik=0;ik<p1.endpos_s1.size();ik++) {
						int e_s1 = p1.endpos_s1.get(ik);
						int s_s1 = e_s1-scdawg.get_node_length(p1.node);

						for(int jk=0;jk<p2.endpos_s1.size();jk++) {
							int e_s2 = p2.endpos_s1.get(jk);
							int s_s2 = e_s2-scdawg.get_node_length(p2.node);
							
							if(s_s1>=s_s2&&s_s1<=e_s2&&e_s1>=s_s2&&e_s1<=e_s2) {
								System.out.printf("%d,%d,%d,%d ",s_s2,s_s1,e_s1,e_s2);
								System.out.println("remove " + scdawg.get_node_label(p1.node) +" " + scdawg.get_node_label(p2.node)+ " -> " + p1.endpos_s1.get(ik) );
								p1.endpos_s1.remove(ik);
							}
							
						}
					}
					
					for(int ik=0;ik<p1.endpos_s2.size();ik++) {
						int e_s1 = p1.endpos_s2.get(ik);
						int s_s1 = e_s1-scdawg.get_node_length(p1.node);

						for(int jk=0;jk<p2.endpos_s2.size();jk++) {
							int e_s2 = p2.endpos_s2.get(jk);
							int s_s2 = e_s2-scdawg.get_node_length(p2.node);

							if(s_s1>=s_s2&&s_s1<=e_s2&&e_s1>=s_s2&&e_s1<=e_s2) {
								p1.endpos_s2.remove(ik);
							}
							
						}
					}

				
				
			}
			
			
			
		}
		
		for (Endpos_Pair p : quasi_max_nodes) {
			if(p.endpos_s1.size()>0&&p.endpos_s2.size()>0) {
				result.add(p);
			}
		}
		
		return result;
		
	}

	public ArrayList[] build_greedy_cover(ArrayList<Endpos_Pair> quasi_max_nodes, ArrayList<LCS_Triple> lcs_triples,
			HashMap<Node, Node> node_ancestors) {

		// build greedy cover

		ArrayList[] greedy_cover = new ArrayList[scdawg.all_nodes.size()];

		Node prev = null;

		for (int i = 0; i < scdawg.all_nodes.size(); i++)
			greedy_cover[i] = new ArrayList();

		boolean first_node_found = false;

		for (int i = 0; i < quasi_max_nodes.size(); i++) {
			Endpos_Pair p = quasi_max_nodes.get(i);
			if (p.endpos_s1.size() > 0) {
				// System.out.println(scdawg.get_node_label(nodes_in_s1[i])+ i+
				// nodes_endpos_s2.get(nodes_in_s1[i]) );

				for (int l=0;l<p.endpos_s1.size(); l++) { // iterieren über alle

					int elem_s1 = (int) p.endpos_s1.get(l);

					
					for (int j = p.endpos_s2.size() - 1; j >= 0; j--) { // iterieren über alle
																		// dec sequences ;//
																		// HIER EIN TRIPEL! //
																		// erstes paar abc;4;15
																		// abc;4;4 mn;7;8
																		// abc;11;15 abc;11;4
						int dec_elem= (int) p.endpos_s2.get(j);

						System.out.println(scdawg.get_node_label(p.node)+" "+elem_s1 + " :: " + dec_elem);

						lcs_triples.add(new LCS_Triple(elem_s1, dec_elem, p.node));
						int lcs_index = lcs_triples.size() - 1;

						node_ancestors.put(p.node, prev);
						prev = p.node;

						if ((j == p.endpos_s2.size() - 1) & !first_node_found) { // erstes
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

							for (int k = 0; k < greedy_cover.length; k++) { // alle cover listen durchgehen

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

				} // for l

			}

		}

		return greedy_cover;
	}

	// *******************************************************************************
	// calculate_LCS()
	// *******************************************************************************

	public ArrayList<LCS_Triple> calculate_LCS(ArrayList<Endpos_Pair> quasi_max_nodes) {

		ArrayList<LCS_Triple> lcs_triples = new ArrayList<LCS_Triple>();
		HashMap<Node, Node> node_ancestors = new HashMap<Node, Node>();
		ArrayList[] greedy_cover = build_greedy_cover(quasi_max_nodes, lcs_triples, node_ancestors);
		this.print_greedy_cover(greedy_cover,lcs_triples);
		// calculate lis

		int i_count = 0;
		for (int i = 0; i < greedy_cover.length; i++) {
			if (greedy_cover[i].size() > 0)
				i_count++;
		}

		ArrayList<LCS_Triple> lis = new ArrayList<LCS_Triple>();
		int x = lcs_triples.get((int) greedy_cover[i_count - 1].get(0)).endpos_s2; // pick any??
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
	// calculate_LCS_New()
	// *******************************************************************************

	public ArrayList<ArrayList<LCS_Triple>> calculate_LCS_New(ArrayList<Endpos_Pair> quasi_max_nodes) {

		ArrayList<ArrayList<LCS_Triple>> result = new ArrayList<ArrayList<LCS_Triple>>();

		ArrayList<LCS_Triple> lcs_triples = new ArrayList<LCS_Triple>();
		HashMap<Node, Node> node_ancestors = new HashMap<Node, Node>();

		ArrayList[] greedy_cover = build_greedy_cover(quasi_max_nodes, lcs_triples, node_ancestors);
		this.print_greedy_cover(greedy_cover,lcs_triples);
		// Iterator it = node_ancestors.entrySet().iterator();
		// while (it.hasNext()) {
		// Map.Entry pair = (Map.Entry) it.next();
		// System.out.println("Knoten: "+scdawg.get_node_label((Node) pair.getKey())+ "
		// vorgänger "+scdawg.get_node_label((Node) pair.getValue()));
		// }
		//
		// calculate lis

		LIS_Graph lis_graph = new LIS_Graph();

		int i_count = 0;
		for (int i = 0; i < greedy_cover.length; i++) {
			if (greedy_cover[i].size() > 0)
				i_count++;
		}
		determineLis(greedy_cover, lcs_triples, node_ancestors, lis_graph, lis_graph.root, i_count - 1);

		return result;

	}

	public void determineLis(ArrayList[] greedy_cover, ArrayList<LCS_Triple> lcs_triples,
			HashMap<Node, Node> node_ancestors, LIS_Graph lis_graph, Node node, int i) {

		System.out.println(greedy_cover[i].size());
		for (int x = 0; x < greedy_cover[i].size(); x++) {
			Node child_node = lcs_triples.get((int) greedy_cover[i].get(x)).node;
			node.children.put(0, child_node);

			System.out.println("Knoten: " + scdawg.get_node_label(child_node) + "  vorgänger "
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
			System.out.println("xxx " + next_column);
			System.out.println("yyy " + lcs_triples.get((int) greedy_cover[i].get(x)).column_number);

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
				System.out.printf("\"%s\":e1:\"%s\", e2:\"%s\"\n", nodelabel, e1, e2);
			}
		}

	}
	
	

	public void print_greedy_cover(ArrayList[] greedy_cover,ArrayList<LCS_Triple> lcs_triples) {

		for (int i = 0; i < greedy_cover.length; i++) {
			for (int j = 0; j < greedy_cover[i].size(); j++) {
				LCS_Triple t = lcs_triples.get((int) greedy_cover[i].get(j));
				System.out.println(t.endpos_s2+" ("+scdawg.get_node_label(t.node)+")");
			}
			System.out.println("-------------------------------");
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
				// System.out.println(lis.get(i).endpos_s1+" "+lis.get(i).endpos_s2+"
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

		System.out.println("---------------------------------");

		for (int i = 0; i < reduced_candidates.size(); i++) {

			for (Map.Entry<Integer, Node> entry : reduced_candidates.get(i).entrySet()) {

				Node currentNode = entry.getValue();
				int endpos_n1 = scdawg.get_node_length(currentNode) + entry.getKey() - 1;
				System.out.println(scdawg.get_node_label((Node) entry.getValue()) + " " + entry.getKey() + " "
						+ (scdawg.get_node_length(currentNode) + entry.getKey() - 1));
			}
			System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXX");

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
		// System.out.println(scdawg.get_node_label(n));
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
