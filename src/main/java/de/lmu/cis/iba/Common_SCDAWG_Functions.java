package de.lmu.cis.iba;

import de.lmu.cis.ocrd.OCRLine;

import java.util.*;

/**
 * @author Tobias Englmeier (CIS)
 */

public class Common_SCDAWG_Functions {

	Online_CDAWG_sym scdawg = null;

	public Common_SCDAWG_Functions(Online_CDAWG_sym scdawg) {
		this.scdawg = scdawg;
	}

	/*********************************************************************************
	 * count_nodes()
	 * 
	 * counts all paths for alle nodes in the SCDAWG left and right transitions from
	 * one node are followed recursively
	 * 
	 * @return
	 **********************************************************************************/

	public HashMap<Node, Integer> count_nodes() {
		HashMap<Node, Integer> result = new HashMap<Node, Integer>();
		// Count all right transitions

		scdawg.eachNode_BFS(scdawg.root, false, true, new Online_CDAWG_sym.Visitor() {
			public void visit(Node n) {

				Iterator it = n.children.entrySet().iterator();

				while (it.hasNext()) {

					Map.Entry pair = (Map.Entry) it.next();
					Node child = (Node) pair.getValue();

					if (scdawg.sinks.contains(child))
						continue;

					if (result.containsKey(child))
						result.put(child, (int) result.get(child) + 1);
					else {
						result.put(child, 1);
					}
				}

				// Count all left transitions

				Iterator it2 = n.children_left.entrySet().iterator();

				while (it2.hasNext()) {

					Map.Entry pair = (Map.Entry) it2.next();
					Node child = (Node) pair.getValue();

					if (scdawg.sinks.contains(child))
						continue;

					if (result.containsKey(child))
						result.put(child, (int) result.get(child) + 1);
					else {
						result.put(child, 1);
					}
				}

			}
		});

		return result;
	}

	/*********************************************************************************
	 * get_quasi_maximal_nodes_pairwise
	 * 
	 * finds quasimaximal nodes für two strings simple quasi-max definition = direct
	 * left + direct right edge to sink for start and end two left or two right
	 * transitions
	 ***********************************************************************************/

	public ArrayList<Endpos_Pair> get_quasi_maximal_nodes_pairwise() {

		int[] marked_nodes = this.get_string_occurences();

		ArrayList<Endpos_Pair> result = new ArrayList();
		Node[] nodes_in_s1 = new Node[scdawg.stringset.get(0).length()];

		mainloop:
		for (int i = 0; i < scdawg.all_nodes.size(); i++) {

			Node node = scdawg.all_nodes.get(i);

			if (marked_nodes[node.id] != -1 || node == scdawg.root)
				continue;

			// all nodes except last alignment part
			ArrayList endpos_s1 = new ArrayList();
			ArrayList endpos_s2 = new ArrayList();

			HashSet<Integer> sinkshit = new HashSet<>();

			Iterator it = node.children_new.entrySet().iterator();

			while (it.hasNext()) {
				Map.Entry pair2 = (Map.Entry) it.next();
				EdgeInfo e = (EdgeInfo) pair2.getValue();
				int letter = (int) pair2.getKey();
				Node n2 = e.child;

				if (n2.is_endNode) {
					int start_in_sink = e.pos - scdawg.get_node_length(node) - 1;


					if (start_in_sink > -1) {
						// Logger.debug(scdawg.get_node_label(node)+" "+scdawg.get_node_label(n2)+" :"+scdawg.get_node_length(node)+" "+e.pos+" "+start_in_sink);

						int start_letter = scdawg.get_letter(start_in_sink, n2.stringnr);

						if (node.children_left.containsKey(start_letter)) {
							Node n_left = node.children_left.get(start_letter);

							if (n_left.is_endNode) {

								sinkshit.add(n2.stringnr);

								if (n2.stringnr == 0) {
									if (!endpos_s1.contains(e.pos)) {
										endpos_s1.add(e.pos);
									}
								} else {
									if (!endpos_s2.contains(e.pos)) {
										endpos_s2.add(e.pos);
									}
								}

							}

						}
					}
				}
			}
//			if (sinkshit.size() == 2 && (endpos_s1.size() == 1 && endpos_s2.size() == 1)) {
			if (sinkshit.size() == 2 && (endpos_s1.size() > 0 && endpos_s2.size() > 0)) {
				result.add(new Endpos_Pair(node, endpos_s1, endpos_s2));
				continue mainloop;
			}
			// check for start node
			if (node.children_new.size() == 2 && scdawg.get_node_label(node).startsWith("#")) {
				sinkshit = new HashSet<>();
				for (Map.Entry<Integer, EdgeInfo> e : node.children_new.entrySet()) {
					if (e.getValue().child.is_endNode) {
						sinkshit.add(e.getValue().child.stringnr);
					}
				}
				if (sinkshit.size() == 2) {
					endpos_s1.add(scdawg.get_node_length(node));
					endpos_s2.add(scdawg.get_node_length(node));
					result.add(new Endpos_Pair(node, endpos_s1, endpos_s2));
					continue mainloop;
				}
			}

			// check for end node
			if (node.children_left.size() == 2 && scdawg.get_node_label(node).endsWith("$")) {
				sinkshit = new HashSet<>();
				for (Map.Entry<Integer, Node> e : node.children_left.entrySet()) {
					if (e.getValue().is_endNode) {
						sinkshit.add(e.getValue().stringnr);
						if (e.getValue().stringnr == 0) {
							if (!endpos_s1.contains(scdawg.stringset.get(0).length())) {
								endpos_s1.add(scdawg.stringset.get(0).length());
							}
						} else {
							if (!endpos_s2.contains(scdawg.stringset.get(1).length())) {
								endpos_s2.add(scdawg.stringset.get(1).length());
							}
						}
					}
				}
				if (sinkshit.size() == 2) {
					result.add(new Endpos_Pair(node, endpos_s1, endpos_s2));
					continue mainloop;
				}
			}
		}
		return result;
	}

	/*********************************************************************************
	 * get_distinct_quasimaximal_nodes()
	 * 
	 * finds all quasimaximal Nodes with transitions to only one string quasimaximal
	 * defined as either left or right transitions to exactly one string
	 **********************************************************************************/

	public void get_distinct_quasimaximal_nodes() {

		HashMap<Node, Integer> distinct_nodes = new HashMap();

		scdawg.eachNode_BFS(scdawg.root, false, false, new Online_CDAWG_sym.Visitor() {
			public void visit(Node n) {

				// Logger.debug(get_node_label(n));
				ArrayList sinkshit = new ArrayList();

				Iterator it2 = n.children.entrySet().iterator();

				while (it2.hasNext()) {
					Map.Entry pair2 = (Map.Entry) it2.next();
					Node n2 = (Node) pair2.getValue();

					if (!n2.is_endNode)
						return;

					if (n2.is_endNode & !sinkshit.contains(n2))
						sinkshit.add(n2);
					// System.out.print(get_letter_by_idx((int) pair2.getKey())+" ");
				}

				it2 = n.children_left.entrySet().iterator();

				while (it2.hasNext()) {
					Map.Entry pair2 = (Map.Entry) it2.next();
					Node n2 = (Node) pair2.getValue();

					if (!n2.is_endNode)
						return;

					if (n2.is_endNode & !sinkshit.contains(n2))
						sinkshit.add(n2);
				}

				// Logger.debug("xxx " + sinkshit.size());

				if (sinkshit.size() == 1) {
					int count = 0;

					Iterator it3 = n.children.entrySet().iterator();

					while (it3.hasNext()) {
						Map.Entry pair3 = (Map.Entry) it3.next();
						Node n3 = (Node) pair3.getValue();
						if (n3.is_endNode)
							count++;
					}

					distinct_nodes.put(n, count);
				}

				// Logger.debug("---------------------------------------------------");

			}
		});
		// Logger.debug("Size " + distinct_nodes.size());

		HashMap distinct_nodes_sorted = Util.sortByValues(distinct_nodes, "DESC");

		Iterator it2 = distinct_nodes_sorted.entrySet().iterator();

		while (it2.hasNext()) {
			Map.Entry pair = (Map.Entry) it2.next();
			Node n = (Node) pair.getKey();
			Integer count = (Integer) pair.getValue();

			// Logger.debug(count + " " + scdawg.get_node_label(n));

		}

	}

	/*********************************************************************************
	 * childOf()
	 * 
	 * Checks if one node is the direct child of another
	 **********************************************************************************/

	public boolean childOf(Node father, Node child) {

		Iterator it2 = father.children.entrySet().iterator();

		while (it2.hasNext()) {
			Map.Entry pair2 = (Map.Entry) it2.next();
			Node n2 = (Node) pair2.getValue();

			if (n2.equals(child))
				return true;

		}

		// it2 = father.children_left.entrySet().iterator();
		//
		// while (it2.hasNext()) {
		// Map.Entry pair2 = (Map.Entry) it2.next();
		// Node n2 = (Node) pair2.getValue();
		//
		// if (n2.equals(child))
		// return true;
		//
		// }

		return false;
	}

	/*********************************************************************************
	 * get_string_occurences()
	 * 
	 * Returns HashMap with nodes and a boolean value, whether the nodes occurs in
	 * one or more strings
	 **********************************************************************************/

	public int[] get_string_occurences() {

		int[] marked_nodes = new int[scdawg.all_nodes.size() + 1];

		scdawg.eachNode_DFS(scdawg.root, true, true, new Online_CDAWG_sym.Visitor() {

			public void visit(Node n) {

				// Logger.debug("Node " + scdawg.get_node_label(n));

				if (n.is_endNode) {
					marked_nodes[n.id] = n.stringnr;
					return;
				}

				int k = -2;

				Iterator it2 = n.children.entrySet().iterator();

				while (it2.hasNext()) {
					Map.Entry pair2 = (Map.Entry) it2.next();
					Node n2 = (Node) pair2.getValue();

					if (k == -2) {
						k = marked_nodes[n2.id];
					} else if (k != marked_nodes[n2.id]) {
						marked_nodes[n.id] = -1;
						return;
					}

				}

				it2 = n.children_left.entrySet().iterator();

				while (it2.hasNext()) {
					Map.Entry pair2 = (Map.Entry) it2.next();
					Node n2 = (Node) pair2.getValue();

					if (k == -2) {
						k = marked_nodes[n2.id];
					} else if (k != marked_nodes[n2.id]) {
						marked_nodes[n.id] = -1;
						return;
					}
				}

				marked_nodes[n.id] = k;

			}
		});

		// Logger.debug("Size " + marked_nodes.length);

		// for (int i = 0; i < scdawg.all_nodes.size(); i++) {
		// Node n = scdawg.all_nodes.get(i);
		// Logger.debug(scdawg.get_node_label(n) + " " + marked_nodes[n.id]);

		// if (scdawg.stringset.get(0).contains(scdawg.get_node_label(n))
		// && scdawg.stringset.get(0).contains(scdawg.get_node_label(n)) &&
		// marked_nodes[n.id] != -1) {
		// }
		// }

		return marked_nodes;

	}

	/*********************************************************************************
	 * get_n_string_occurences()
	 * 
	 * Returns HashMap with nodes and a HashSet value, whether the nodes occurs in
	 * one or more strings
	 **********************************************************************************/

	public HashMap<Node, HashSet<Integer>> get_n_string_occurences(int n, ArrayList<OCRLine> ocrlines) {

		HashMap<Node, HashSet<Integer>> marked_nodes = new HashMap();
		HashMap<Node, HashSet<Integer>> result = new HashMap();

		scdawg.eachNode_DFS(scdawg.root, true, true, new Online_CDAWG_sym.Visitor() {

			public void visit(Node n) {

				if (n.is_endNode) {
					return;
				}

				// Logger.debug(" node: " + scdawg.get_node_label(n) + " " +
				// marked_nodes.get(n));

				HashSet<Integer> child_occurrences = new HashSet();
				HashSet sinkshit = new HashSet();

				Iterator it2 = n.children.entrySet().iterator();

				while (it2.hasNext()) {
					Map.Entry pair2 = (Map.Entry) it2.next();
					Node n2 = (Node) pair2.getValue();

					if (n2.is_endNode)
						sinkshit.addAll(n2.stringnumbers);

					if (marked_nodes.containsKey(n2))
						child_occurrences = marked_nodes.get(n2);
				}

				it2 = n.children_left.entrySet().iterator();

				while (it2.hasNext()) {
					Map.Entry pair2 = (Map.Entry) it2.next();
					Node n2 = (Node) pair2.getValue();

					if (n2.is_endNode)
						sinkshit.addAll(n2.stringnumbers);

					if (marked_nodes.containsKey(n2))
						child_occurrences = marked_nodes.get(n2);
				}

				sinkshit.addAll(child_occurrences);

				marked_nodes.put(n, sinkshit);

			}
		});

		// for (Node sink : scdawg.sinks){
		// marked_nodes.put(sink, false);
		//
		// }
		// Logger.debug("Size " + marked_nodes.size());

		Iterator it2 = marked_nodes.entrySet().iterator();

		while (it2.hasNext()) {
			Map.Entry pair = (Map.Entry) it2.next();
			Node node = (Node) pair.getKey();
			HashSet count = (HashSet) pair.getValue();

			if (count.size() <= n) {
				Iterator it3 = count.iterator();
				HashSet<Integer> ids = new HashSet();

				HashSet<String> ocrEngines = new HashSet<>();

				while (it3.hasNext()) {
					Integer id = (Integer) it3.next();
					ids.add(id);
					//System.out.print(id + " ");
					ocrEngines.add(ocrlines.get(id).ocrEngine);

				}
				// Logger.debug(":" + ids.size() + " " + scdawg.get_node_label(node));
				// Logger.debug(ocrEngines);
				if (ids.size() == n && ocrEngines.size() == n) {
					result.put(node, ids);
				}
			}
		}

		return result;

	}

	/*********************************************************************************
	 * mark_children_of_single_occ_nodes()
	 * 
	 * Marks children of marked_nodes (nodes only occuring) once
	 * 
	 * @param marked_nodes:
	 *            Result from mark_children_of_single_occ_nodes()
	 **********************************************************************************/

	public HashMap<Node, Boolean> mark_children_of_single_occ_nodes(int[] marked_nodes) {

		HashMap<Node, Boolean> result = new HashMap();
		scdawg.eachNode_DFS(scdawg.root, true, false, new Online_CDAWG_sym.Visitor() {

			public void visit(Node n) {
				if (n.is_endNode) {
					return;
				}

				if (marked_nodes[n.id] == -1)
					return;

				Iterator it2 = n.children.entrySet().iterator();

				while (it2.hasNext()) {
					Map.Entry pair2 = (Map.Entry) it2.next();
					Node n2 = (Node) pair2.getValue();

					if (marked_nodes[n2.id] > -1)
						result.put(n2, true);
//					if (scdawg.get_node_label(n2).equals(" gott")) {
//						Logger.debug("cxx " + scdawg.get_node_label(n));
//						Logger.debug(marked_nodes[-1]);
//					}

				}

				Iterator it3 = n.children_left.entrySet().iterator();

				while (it3.hasNext()) {
					Map.Entry pair3 = (Map.Entry) it3.next();
					Node n3 = (Node) pair3.getValue();

					if (marked_nodes[n3.id] > -1)
						result.put(n3, true);
//					if (scdawg.get_node_label(n3).equals(" gott")) {
//						Logger.debug("cxx " + scdawg.get_node_label(n));
//						Logger.debug(marked_nodes[-1]);
//					}

				}

			}
		});

		// for (Node sink : scdawg.sinks){
		// marked_nodes.put(sink, false);
		//
		// }
		// Logger.debug("-----------------------------------------------------------------------");

		// Logger.debug("Size " + result.size());

		Iterator it2 = result.entrySet().iterator();

		while (it2.hasNext()) {
			Map.Entry pair = (Map.Entry) it2.next();
			Node n = (Node) pair.getKey();
			Boolean count = (Boolean) pair.getValue();

			// Logger.debug(count + " " + scdawg.get_node_label(n));

		}

		return result;

	}

	/*********************************************************************************
	 * get_quasiminimal_nodes()
	 * 
	 * Finds all quasiminimal nodes in an SCDAWG Quasiminimal defined as all nodes
	 * with parent nodes occuring in more then one string while they only occur in
	 * one string
	 **********************************************************************************/

	public HashMap<Node, Integer> get_quasiminimal_nodes() {

		HashMap<Node, Integer> result = new HashMap<Node, Integer>();
		ArrayList<Node> quasiminimal_nodes = new ArrayList<Node>();

		int[] marked_nodes = this.get_string_occurences();
		HashMap<Node, Boolean> marked_nodes2 = this.mark_children_of_single_occ_nodes(marked_nodes);

		scdawg.eachNode_DFS(scdawg.root, true, false, new Online_CDAWG_sym.Visitor() {

			public void visit(Node n) {

				if (n.is_endNode || n == scdawg.root) {
					return;
				}

				if (marked_nodes[n.id] == -1)
					return;

				if (marked_nodes2.containsKey(n)) {
					if (marked_nodes2.get(n))
						return;
				} else {
					quasiminimal_nodes.add(n);
				}
			}

		});

		HashMap<Node, Integer> right_edges_count = this.count_quasiminimal_nodes(marked_nodes);

		// Logger.debug("QUASIMINIMAL Size " + quasiminimal_nodes.size());

		for (Node n : quasiminimal_nodes) {

			Iterator it2 = n.children.entrySet().iterator();

			result.put(n, right_edges_count.get(n));
		}

		return result;
	}

	/*********************************************************************************
	 * count_quasiminimal_nodes()
	 * 
	 * 
	 * 
	 * @param marked_nodes:
	 *            Result from get_string_occurences()
	 **********************************************************************************/

	public HashMap<Node, Integer> count_quasiminimal_nodes(int[] marked_nodes) {

		HashMap<Node, Integer> result = new HashMap<Node, Integer>();
		// Logger.debug("-----------------------------------------------------------------------");

		scdawg.eachNode_DFS(scdawg.root, true, true, new Online_CDAWG_sym.Visitor() {

			public void visit(Node n) {

				if (n.is_endNode || n == scdawg.root) {
					return;
				}

				if (marked_nodes[n.id] == -1)
					return;

				int count = 0;

				Iterator it2 = n.children.entrySet().iterator();

				while (it2.hasNext()) {
					Map.Entry pair2 = (Map.Entry) it2.next();
					Node n2 = (Node) pair2.getValue();

					if (n2.is_endNode)
						count++;
					if (result.containsKey(n2))
						count += result.get(n2);

				}

				result.put(n, count);

			}

		});

		// Logger.debug("-----------------------------------------------------------------------");
		//
		// Logger.debug("COUNT");
		//
		// Iterator it2 = result.entrySet().iterator();
		//
		// while (it2.hasNext()) {
		// Map.Entry pair = (Map.Entry) it2.next();
		// Node n = (Node) pair.getKey();
		// Integer count = (Integer) pair.getValue();
		//
		// Logger.debug(count + " " + scdawg.get_node_label(n));
		//
		// }

		return result;
	}

	/*********************************************************************************
	 * get_quasimaximal_nodes()
	 * 
	 * Finds all quasimaximal nodes in an SCDAWG quasimaximal defined as longest
	 * nodes occuring in (n) strings
	 * 
	 * @param marked_nodes:
	 *            Result from get_string_occurences()
	 **********************************************************************************/

	public HashMap<Node, Integer> get_quasimaximal_nodes(HashMap<Node, Boolean> marked_nodes) {

		HashMap<Node, Integer> quasi_maximal_nodes = new HashMap<Node, Integer>();
		HashMap<Node, Integer> quasi_maximal_nodes_help = new HashMap<Node, Integer>();

		scdawg.eachNode_DFS(scdawg.root, true, false, new Online_CDAWG_sym.Visitor() {

			Node quasi_maximal_candidate = null;
			HashSet<Integer> sinkset = new HashSet<Integer>();

			int count = 0;

			public void visit(Node n) {
				// WELCHE SINKS STATT NUR ZÄHLEN!!?
				if (n.is_endNode || n == scdawg.root)
					return;

				// Logger.debug(" node: " + scdawg.get_node_label(n) + " " + marked_nodes.get(n));

				if (marked_nodes.get(n) && quasi_maximal_candidate == null) {
					quasi_maximal_candidate = n;
				}

				// if (quasi_maximal_candidate != null)
				// Logger.debug(" candidate: " + scdawg.get_node_label(quasi_maximal_candidate));
				// else
				// Logger.debug(" candidate: null");

				if (marked_nodes.get(n)) {

					Iterator it2 = n.children.entrySet().iterator();

					while (it2.hasNext()) {
						Map.Entry pair2 = (Map.Entry) it2.next();
						Node n2 = (Node) pair2.getValue();

						if (scdawg.sinks.contains(n2)) {
							sinkset.add(n2.stringnr);
						}
						if (quasi_maximal_nodes_help.containsKey(n2) && quasi_maximal_candidate != null) {

							// Logger.debug(" candidate: " + quasi_maximal_candidate.children.entrySet().iterator().hasNext());


							Iterator it3 = quasi_maximal_candidate.children.entrySet().iterator();
							int sinkshit_candidate = 0;

							while (it3.hasNext()) {
								Map.Entry pair3 = (Map.Entry) it3.next();
								Node n3 = (Node) pair3.getValue();

								if (scdawg.sinks.contains(n3)) {
									sinkshit_candidate++;
								}
							}

							it3 = n2.children.entrySet().iterator();
							int sinkshit_n2 = 0;

							while (it3.hasNext()) {
								Map.Entry pair3 = (Map.Entry) it3.next();
								Node n3 = (Node) pair3.getValue();

								if (scdawg.sinks.contains(n3)) {
									sinkshit_n2++;
								}
							}

							int sinkshit_total = sinkshit_candidate + sinkshit_n2;

							if (sinkshit_total == scdawg.sinks.size()) {
								// Logger.debug("NIMM IHN");
								quasi_maximal_nodes.put(quasi_maximal_candidate, count);
								quasi_maximal_candidate = null;
								sinkset = new HashSet<Integer>();

								return;
							}
						}
					}

					it2 = n.children_left.entrySet().iterator();

					while (it2.hasNext()) {
						Map.Entry pair2 = (Map.Entry) it2.next();
						Node n2 = (Node) pair2.getValue();

						if (scdawg.sinks.contains(n2)) {
							sinkset.add(n2.stringnr);

						}
					}

					// Logger.debug(sinkset.size() + " " + scdawg.sinks.size());

					if (sinkset.size() == scdawg.sinks.size()) { // wenn in allen sinks vorkommt
						if (count == 0)
							count += n.children.size();

						quasi_maximal_nodes.put(quasi_maximal_candidate, count);
						quasi_maximal_candidate = null;
						sinkset = new HashSet<Integer>();
						quasi_maximal_nodes_help.put(n, n.stringnr);
						count = 0;
					} else
						count += n.children.size();

				}

			}
		});
		// Logger.debug("Size maximal " + quasi_maximal_nodes.size());

		HashMap<Node, Integer> result = Util.sortByValues(quasi_maximal_nodes, "DESC");

		Iterator it2 = result.entrySet().iterator();

		while (it2.hasNext()) {
			Map.Entry pair = (Map.Entry) it2.next();
			Node n = (Node) pair.getKey();
			Integer count = (Integer) pair.getValue();

			// Logger.debug(count + " xxx " + scdawg.get_node_label(n));

		}

		return result;

	}

	/*********************************************************************************
	 * find_n_transitions_to_sinks()
	 * 
	 * Method searches for all nodes with occurences in n strings
	 * 
	 * @param node:
	 *            node from which search will be started
	 **********************************************************************************/

	public static HashSet<Integer> find_n_transitions_to_sinks(Node node, Online_CDAWG_sym scdawg,
			HashSet<Integer> acc) {

		Iterator it = node.children.entrySet().iterator();
		HashSet<Integer> result = new HashSet<Integer>();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();
			Node child = (Node) pair.getValue();
			for (int j = 0; j < scdawg.sinks.size(); j++) {
				if (scdawg.sinks.get(j) == child) {
					// if (!sinks.contains(scdawg.sinks.get(j))) {
					for (int k = 0; k < scdawg.sinks.get(j).stringnumbers.size(); k++) {
						acc.add(scdawg.sinks.get(j).stringnumbers.get(k));
						// sinks.add(scdawg.sinks.get(j));
					}
					// }
				}
			}
		}

		Iterator it2 = node.children_left.entrySet().iterator();

		while (it2.hasNext()) {

			Map.Entry pair = (Map.Entry) it2.next();
			Node child = (Node) pair.getValue();

			for (int j = 0; j < scdawg.sinks.size(); j++) {
				if (scdawg.sinks.get(j) == child) {
					// if (!sinks.contains(scdawg.sinks.get(j))) {
					for (int k = 0; k < scdawg.sinks.get(j).stringnumbers.size(); k++) {
						acc.add(scdawg.sinks.get(j).stringnumbers.get(k));
						// sinks.add(scdawg.sinks.get(j));
					}
					// }
				}
			}
		}

		// REC AUFRUF der Funktion mit den Kindern
		Iterator it3 = node.children.entrySet().iterator();

		while (it3.hasNext()) {

			Map.Entry pair = (Map.Entry) it3.next();
			Node child = (Node) pair.getValue();
			find_n_transitions_to_sinks(child, scdawg, acc);
		}

		Iterator it4 = node.children_left.entrySet().iterator();

		while (it4.hasNext()) {
			Map.Entry pair = (Map.Entry) it4.next();
			Node child = (Node) pair.getValue();
			find_n_transitions_to_sinks(child, scdawg, acc);
		}
		return acc;
	}

}
