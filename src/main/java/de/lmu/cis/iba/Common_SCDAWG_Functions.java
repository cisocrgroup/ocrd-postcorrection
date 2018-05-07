package de.lmu.cis.iba;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;



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

		scdawg.eachNode_BFS(scdawg.root, new Online_CDAWG_sym.Visitor() {
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

		ArrayList<Endpos_Pair> result = new ArrayList();

		for (int u = 0; u < scdawg.stringset.size() - 1; u++) {

			// nur für ersten string??? Array länge aus allen positionen immer knoten

			// dann iterieren auf pos und wenn konten gefunden => pos in zweiten string =>
			// paare in s2 (s1,s2)..

			Node[] nodes_in_s1 = new Node[scdawg.stringset.get(u).length()];

			HashMap<Node, ArrayList> nodes_endpos_s2 = new HashMap();

			for (int i = 0; i < scdawg.all_nodes.size(); i++) {

				Node node = scdawg.all_nodes.get(i);

				// all nodes except first and last alignment part

				Iterator it = node.children.entrySet().iterator();

				while (it.hasNext()) {
					Map.Entry pair = (Map.Entry) it.next();
					Node child = (Node) pair.getValue();

					int letter = (int) pair.getKey();

					for (int j = 0; j < scdawg.sinks.size(); j++) {

						if (scdawg.sinks.get(j) == child) { // wenn rechtsübergang auf sink

							Iterator it2 = node.children_left.entrySet().iterator();

							while (it2.hasNext()) {

								Map.Entry pair_left = (Map.Entry) it2.next();
								Node child_left = (Node) pair_left.getValue();

								if (child_left == child) {
									int endpos = scdawg.get_node_length(child)
											- scdawg.get_edge_length(letter, node, child) - 1;

									if (node.start == -1)
										continue;

									if (scdawg.sinks.get(j).stringnr == u)
										nodes_in_s1[endpos] = node;
									if (scdawg.sinks.get(j).stringnr == u + 1) {

										if (nodes_endpos_s2.containsKey(node)) {
											if (!nodes_endpos_s2.get(node).contains(endpos)) {
												nodes_endpos_s2.get(node).add(endpos);
											}

										} else {
											nodes_endpos_s2.put(node, new ArrayList());

											nodes_endpos_s2.get(node).add(endpos);

										}

									}

								}

							}

						}
					}

				}

				// last alignment part

				if (scdawg.get_node_label(node).endsWith("$") & !scdawg.get_node_label(node).startsWith("#")) {

					for (int j = 0; j < scdawg.sinks.size(); j++) {

						Iterator it2 = node.children_left.entrySet().iterator();

						while (it2.hasNext()) {

							Map.Entry pair_left = (Map.Entry) it2.next();
							Node child_left = (Node) pair_left.getValue();

							int letter = (int) pair_left.getKey();

							if (child_left == scdawg.sinks.get(j)) {
								int endpos = scdawg.sinks.get(j).start
										+ scdawg.get_edge_label_left(letter, node, child_left).length()
										+ scdawg.get_node_length(node) - 1;

								if (scdawg.sinks.get(j).stringnr == u)
									nodes_in_s1[endpos] = node;
								if (scdawg.sinks.get(j).stringnr == u + 1) {

									if (nodes_endpos_s2.containsKey(node)) {
										if (!nodes_endpos_s2.get(node).contains(endpos)) {
											nodes_endpos_s2.get(node).add(endpos);
										}

									} else {
										nodes_endpos_s2.put(node, new ArrayList());

										nodes_endpos_s2.get(node).add(endpos);

									}

								}

							}

						} // while

					} // for sinks

				} // last alignment part

				// first alignment part

				if (scdawg.get_node_label(node).startsWith("#") & !scdawg.get_node_label(node).startsWith("$")) {

					for (int j = 0; j < scdawg.sinks.size(); j++) {

						Iterator it2 = node.children.entrySet().iterator();

						while (it2.hasNext()) {

							Map.Entry pair = (Map.Entry) it2.next();
							Node child = (Node) pair.getValue();

							int letter = (int) pair.getKey();

							if (child == scdawg.sinks.get(j)) {
								int endpos = scdawg.get_node_length(scdawg.sinks.get(j))
										- scdawg.get_edge_length(letter, node, scdawg.sinks.get(j)) - 1;

								if (scdawg.sinks.get(j).stringnr == u)
									nodes_in_s1[endpos] = node;
								if (scdawg.sinks.get(j).stringnr == u + 1) {

									if (nodes_endpos_s2.containsKey(node)) {
										if (!nodes_endpos_s2.get(node).contains(endpos)) {
											nodes_endpos_s2.get(node).add(endpos);
										}

									} else {
										nodes_endpos_s2.put(node, new ArrayList());

										nodes_endpos_s2.get(node).add(endpos);

									}

								}

							}

						} // while

					} // for sinks

				} // first alignment part

			} // all Nodes

			result.add(new Endpos_Pair(nodes_in_s1, nodes_endpos_s2));

		} // for u

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

		scdawg.eachNode_BFS(scdawg.root, new Online_CDAWG_sym.Visitor() {
			public void visit(Node n) {

				// System.out.println(get_node_label(n));
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

				// System.out.println("xxx " + sinkshit.size());

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

				// System.out.println("---------------------------------------------------");

			}
		});
		System.out.println("Size " + distinct_nodes.size());

		HashMap distinct_nodes_sorted = Util.sortByValues(distinct_nodes, "DESC");

		Iterator it2 = distinct_nodes_sorted.entrySet().iterator();

		while (it2.hasNext()) {
			Map.Entry pair = (Map.Entry) it2.next();
			Node n = (Node) pair.getKey();
			Integer count = (Integer) pair.getValue();

			System.out.println(count + " " + scdawg.get_node_label(n));

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
	 * get_quasiminimal_nodes_01()
	 * 
	 * @deprecated First try to find quasiminimal nodes, returns wrong result..
	 **********************************************************************************/
	public void get_quasiminimal_nodes_01() {

		HashMap<Node, Integer> quasi_minimal_nodes = new HashMap();

		scdawg.eachNode_DFS(scdawg.root, true, true, new Online_CDAWG_sym.Visitor() {

			Node quasi_minimal_candidate = null;

			public void visit(Node n) {

				if (n.is_endNode)
					return;

				ArrayList sinkshit = new ArrayList();

				Iterator it2 = n.children.entrySet().iterator();

				boolean start_node = true;

				while (it2.hasNext()) {
					Map.Entry pair2 = (Map.Entry) it2.next();
					Node n2 = (Node) pair2.getValue();

					if (!n2.is_endNode)
						start_node = false;

					if (n2.is_endNode & !sinkshit.contains(n2))
						sinkshit.add(n2);
					// System.out.print(get_letter_by_idx((int) pair2.getKey())+" ");
				}

				it2 = n.children_left.entrySet().iterator();

				while (it2.hasNext()) {
					Map.Entry pair2 = (Map.Entry) it2.next();
					Node n2 = (Node) pair2.getValue();

					if (!n2.is_endNode)
						start_node = false;

					if (n2.is_endNode & !sinkshit.contains(n2))
						sinkshit.add(n2);
				}

				System.out.println(" node: " + scdawg.get_node_label(n) + " " + sinkshit.size());

				if (!start_node && quasi_minimal_candidate == null)
					return;

				if (quasi_minimal_candidate == null && sinkshit.size() == 1) {
					quasi_minimal_candidate = n;
					System.out.println("quasi_minimal_candidate :" + scdawg.get_node_label(quasi_minimal_candidate));

					return;
				}

				else if (childOf(n, quasi_minimal_candidate)) {

					if (sinkshit.size() == 1) {
						quasi_minimal_candidate = n;
						System.out
								.println("quasi_minimal_candidate :" + scdawg.get_node_label(quasi_minimal_candidate));
					}

					else {
						System.out.println("ELSE CHILD ");
						System.out.println(" ---> TAKE: \"" + scdawg.get_node_label(quasi_minimal_candidate) + "\"");

						quasi_minimal_nodes.put(quasi_minimal_candidate, null);
						quasi_minimal_candidate = null;
					}
				}

				else if (sinkshit.size() == 1) {

					System.out.println("ELSE NO CHILD");
					System.out.println(" ---> TAKE: \"" + scdawg.get_node_label(quasi_minimal_candidate) + "\"");

					quasi_minimal_nodes.put(quasi_minimal_candidate, null);

					quasi_minimal_candidate = n;

					System.out.println("quasi_minimal_candidate :" + scdawg.get_node_label(quasi_minimal_candidate));

				}

				//
				// if (sinkshit.size() == 1) {
				// int count = 0;
				//
				// Iterator it3 = n.children.entrySet().iterator();
				//
				// while (it3.hasNext()) {
				// Map.Entry pair3 = (Map.Entry) it3.next();
				// Node n3 = (Node) pair3.getValue();
				// if (n3.is_endNode)
				// count++;
				// }
				//
				// distinct_nodes.put(n, count);
				// }

				// System.out.println("---------------------------------------------------");

			}
		});
		System.out.println("Size " + quasi_minimal_nodes.size());

		Iterator it2 = quasi_minimal_nodes.entrySet().iterator();

		while (it2.hasNext()) {
			Map.Entry pair = (Map.Entry) it2.next();
			Node n = (Node) pair.getKey();
			Integer count = (Integer) pair.getValue();

			System.out.println(count + " " + scdawg.get_node_label(n));

		}

	}

	/*********************************************************************************
	 * get_string_occurences()
	 * 
	 * Returns HashMap with nodes and a boolean value, whether the nodes occurs in
	 * one or more strings
	 **********************************************************************************/

	public HashMap<Node, Boolean> get_string_occurences() {

		HashMap<Node, Boolean> marked_nodes = new HashMap();

		scdawg.eachNode_DFS(scdawg.root, true, true, new Online_CDAWG_sym.Visitor() {

			public void visit(Node n) {

				if (n.is_endNode){
					return;
				}

				boolean child_has_multiple_occurrences = false;
				ArrayList sinkshit = new ArrayList();

				Iterator it2 = n.children.entrySet().iterator();

				while (it2.hasNext()) {
					Map.Entry pair2 = (Map.Entry) it2.next();
					Node n2 = (Node) pair2.getValue();

					if (n2.is_endNode & !sinkshit.contains(n2))
						sinkshit.add(n2);

					if (marked_nodes.containsKey(n2)&&marked_nodes.get(n2))
						child_has_multiple_occurrences = marked_nodes.get(n2);
				}

				it2 = n.children_left.entrySet().iterator();

				while (it2.hasNext()) {
					Map.Entry pair2 = (Map.Entry) it2.next();
					Node n2 = (Node) pair2.getValue();

					if (n2.is_endNode & !sinkshit.contains(n2))
						sinkshit.add(n2);

					if (marked_nodes.containsKey(n2)&&marked_nodes.get(n2))
						child_has_multiple_occurrences = marked_nodes.get(n2);
				}
				if (sinkshit.size() > 1 || child_has_multiple_occurrences)
					marked_nodes.put(n, true);
				else
					marked_nodes.put(n, false);

			}
		});
		
//		for (Node sink : scdawg.sinks){
//			marked_nodes.put(sink, false);
//
//		}
		 System.out.println("Size " + marked_nodes.size());

		 Iterator it2 = marked_nodes.entrySet().iterator();
		
		 while (it2.hasNext()) {
		 Map.Entry pair = (Map.Entry) it2.next();
		 Node n = (Node) pair.getKey();
		 Boolean count = (Boolean) pair.getValue();
		
		 System.out.println(count + " " + scdawg.get_node_label(n));
		
		 }

		return marked_nodes;

	}

	/*********************************************************************************
	 * get_n_string_occurences()
	 * 
	 * Returns HashMap with nodes and a HashSet value, whether the nodes occurs in
	 * one or more strings
	 **********************************************************************************/

	public HashMap<Node, HashSet<Integer>> get_n_string_occurences(int n) {

		HashMap<Node, HashSet<Integer>> marked_nodes = new HashMap();
		HashMap<Node, HashSet<Integer>> result = new HashMap();

		scdawg.eachNode_DFS(scdawg.root, true, true, new Online_CDAWG_sym.Visitor() {

			public void visit(Node n) {

				if (n.is_endNode){
					return;
				}

//				System.out.println(" node: " + scdawg.get_node_label(n) + " " + marked_nodes.get(n));

				
				HashSet<Integer> child_occurrences = new HashSet();
				HashSet sinkshit = new HashSet();

				Iterator it2 = n.children.entrySet().iterator();

				while (it2.hasNext()) {
					Map.Entry pair2 = (Map.Entry) it2.next();
					Node n2 = (Node) pair2.getValue();

					if (n2.is_endNode)
						sinkshit.add(n2);

					if (marked_nodes.containsKey(n2))
						child_occurrences = marked_nodes.get(n2);
				}

				it2 = n.children_left.entrySet().iterator();

				while (it2.hasNext()) {
					Map.Entry pair2 = (Map.Entry) it2.next();
					Node n2 = (Node) pair2.getValue();

					if (n2.is_endNode)
						sinkshit.add(n2);

					if (marked_nodes.containsKey(n2))
						child_occurrences = marked_nodes.get(n2);
				}
				
				sinkshit.addAll(child_occurrences);
				
				marked_nodes.put(n, sinkshit);

			}
		});
		
//		for (Node sink : scdawg.sinks){
//			marked_nodes.put(sink, false);
//
//		}
		 System.out.println("Size " + marked_nodes.size());

		 Iterator it2 = marked_nodes.entrySet().iterator();
		
		 while (it2.hasNext()) {
		 Map.Entry pair = (Map.Entry) it2.next();
		 Node node = (Node) pair.getKey();
		 HashSet count = (HashSet) pair.getValue();
		 
		 if(count.size()==n) {
		 Iterator it3 = count.iterator();
			HashSet<Integer> ids = new HashSet();	
			 while (it3.hasNext()) {
				  Node nx = (Node) it3.next();
				  ids.add(nx.stringnr);
				 System.out.print(nx.stringnr+" ");
			 }
		 System.out.println(count.size() + " " + scdawg.get_node_label(node));
		 result.put(node,ids);

		 }
		 }

		return result;

	}

	
	/*********************************************************************************
	 * get_quasiminimal_nodes()
	 * 
	 * Finds all quasiminimal nodes in an SCDAWG Quasiminimal defined as all nodes
	 * with parent nodes occuring in more then one string while they only occur in
	 * one string
	 * 
	 * @param marked_nodes:
	 *            Result from get_string_occurences()
	 **********************************************************************************/

	public HashMap<Node, Integer> get_quasiminimal_nodes(HashMap<Node, Boolean> marked_nodes) {

		HashMap<Node, Integer> quasi_minimal_nodes = new HashMap<Node, Integer>();
		HashMap<Node, Integer> quasi_maximal_nodes = new HashMap<Node, Integer>();

		scdawg.eachNode_DFS(scdawg.root, false, false, new Online_CDAWG_sym.Visitor() {

			Node quasi_minimal_candidate = null;
			int count = 0;

			public void visit(Node n) {

				System.out.println(" node: " + scdawg.get_node_label(n) + " " + marked_nodes.get(n));

				
				if (n.is_endNode||n==scdawg.root||marked_nodes.get(n))
					return;



				if (quasi_minimal_candidate == null &! quasi_minimal_nodes.containsKey(n.suffixLink)) {
					quasi_minimal_candidate = n;
				}
				System.out.println(" candidate : " + scdawg.get_node_label(quasi_minimal_candidate));


				if (!marked_nodes.get(n)) {

					Iterator it2 = n.children.entrySet().iterator();
					int sinkshit = 0;

					while (it2.hasNext()) {
						Map.Entry pair2 = (Map.Entry) it2.next();
						Node n2 = (Node) pair2.getValue();

						if (scdawg.sinks.contains(n2)) {
							sinkshit++;
						}
						if(quasi_maximal_nodes.containsKey(n2)&&quasi_minimal_candidate!=null){
							quasi_minimal_nodes.put(quasi_minimal_candidate, count);
							quasi_minimal_candidate = null;
						}
					}

					it2 = n.children_left.entrySet().iterator();

					while (it2.hasNext()) {
						Map.Entry pair2 = (Map.Entry) it2.next();
						Node n2 = (Node) pair2.getValue();

						if (scdawg.sinks.contains(n2)) {
							sinkshit++;
						}
						if(quasi_maximal_nodes.containsKey(n2)&&quasi_minimal_candidate!=null){
							quasi_minimal_nodes.put(quasi_minimal_candidate, count);
							quasi_minimal_candidate = null;
						}
					}
					
 				System.out.println((n.children.size()+n.children_left.size())+ " "+sinkshit);
					
										
    				if (sinkshit == (n.children.size())+n.children_left.size()) {
						if (count == 0)
							count += n.children.size();

						if(quasi_minimal_candidate!=null){
							quasi_minimal_nodes.put(quasi_minimal_candidate, count);
							quasi_minimal_candidate = null;
						}
					    quasi_maximal_nodes.put(n,n.stringnr);
						count = 0;
					} else
						count += n.children.size();

				}

			}
		});
		System.out.println("Size minimal " + quasi_minimal_nodes.size());

		HashMap<Node, Integer> result = Util.sortByValues(quasi_minimal_nodes, "DESC");

		Iterator it2 = result.entrySet().iterator();

		while (it2.hasNext()) {
			Map.Entry pair = (Map.Entry) it2.next();
			Node n = (Node) pair.getKey();
			Integer count = (Integer) pair.getValue();

			System.out.println(count + " xxx " + scdawg.get_node_label(n));

		}

		return result;

	}
	
	
	/*********************************************************************************
	 * get_quasiminimal_nodes_new()
	 * 
	 * Finds all quasiminimal nodes in an SCDAWG Quasiminimal defined as all nodes
	 * with parent nodes occuring in more then one string while they only occur in
	 * one string
	 * 
	 * @param marked_nodes:
	 *            Result from get_string_occurences()
	 **********************************************************************************/

	public HashMap<Node, Integer> get_quasiminimal_nodes_new(HashMap<Node, Boolean> marked_nodes) {

		HashMap<Node, Integer> quasi_minimal_nodes = new HashMap<Node, Integer>();

		scdawg.eachNode_DFS(scdawg.root, true, false, new Online_CDAWG_sym.Visitor() {

			Node quasi_minimal_candidate = null;
			int count = 0;
			HashSet<Integer> sinkset = new HashSet<Integer>();

			public void visit(Node n) {

				if (n.is_endNode||n==scdawg.root)
					return;

				Iterator it2 = n.children.entrySet().iterator();
				int sinkshit = 0;

				while (it2.hasNext()) {
					Map.Entry pair2 = (Map.Entry) it2.next();
					Node n2 = (Node) pair2.getValue();
					
					if(marked_nodes.get(n2)){
						return;
					}
					
					if (scdawg.sinks.contains(n2)) {
						sinkset.add(n2.stringnr);
					}
					
				}

				it2 = n.children_left.entrySet().iterator();

				while (it2.hasNext()) {
					Map.Entry pair2 = (Map.Entry) it2.next();
					Node n2 = (Node) pair2.getValue();

					if (scdawg.sinks.contains(n2)) {
						sinkset.add(n2.stringnr);
					}
					
					if(marked_nodes.get(n2)){
						return;
					}
					
				}
				
				if(n.suffixLink==scdawg.root&&marked_nodes.get(n)==false){				
				quasi_minimal_nodes.put(n, 0);
				}
				

			}
		});
		System.out.println("Size minimal " + quasi_minimal_nodes.size());

		HashMap<Node, Integer> result = Util.sortByValues(quasi_minimal_nodes, "DESC");

		Iterator it2 = result.entrySet().iterator();

		while (it2.hasNext()) {
			Map.Entry pair = (Map.Entry) it2.next();
			Node n = (Node) pair.getKey();
			Integer count = (Integer) pair.getValue();

			System.out.println(count + " xxx " + scdawg.get_node_label(n));

		}

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
				if (n.is_endNode||n==scdawg.root)
					return;

				System.out.println(" node: " + scdawg.get_node_label(n) + " " + marked_nodes.get(n));
				
			
				if (marked_nodes.get(n) && quasi_maximal_candidate == null) {
					quasi_maximal_candidate = n;
				}

				if(quasi_maximal_candidate!=null) System.out.println(" candidate: "+scdawg.get_node_label(quasi_maximal_candidate));
				else System.out.println(" candidate: null");
				
				if (marked_nodes.get(n)) {

					Iterator it2 = n.children.entrySet().iterator();

					while (it2.hasNext()) {
						Map.Entry pair2 = (Map.Entry) it2.next();
						Node n2 = (Node) pair2.getValue();

						if (scdawg.sinks.contains(n2)) {
							sinkset.add(n2.stringnr);
						}
						if(quasi_maximal_nodes_help.containsKey(n2)&&quasi_maximal_candidate!=null){
							
							
							System.out.println(" candidate: "+ quasi_maximal_candidate.children.entrySet().iterator().hasNext());

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
							
							
							
							if(sinkshit_total == scdawg.sinks.size()){
								System.out.println("NIMM IHN");
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
					
					System.out.println(sinkset.size()+ " "+scdawg.sinks.size());

					if (sinkset.size()==scdawg.sinks.size()) { // wenn in allen sinks vorkommt
						if (count == 0)
							count += n.children.size();

						quasi_maximal_nodes.put(quasi_maximal_candidate, count);
						quasi_maximal_candidate = null;
						sinkset = new HashSet<Integer>();
						quasi_maximal_nodes_help.put(n,n.stringnr);
						count = 0;
					} else
						count += n.children.size();

				}

			}
		});
		System.out.println("Size maximal " + quasi_maximal_nodes.size());

		HashMap<Node, Integer> result = Util.sortByValues(quasi_maximal_nodes, "DESC");

		Iterator it2 = result.entrySet().iterator();

		while (it2.hasNext()) {
			Map.Entry pair = (Map.Entry) it2.next();
			Node n = (Node) pair.getKey();
			Integer count = (Integer) pair.getValue();

			System.out.println(count + " xxx " + scdawg.get_node_label(n));

		}

		return result;

	}
	
	
	/*********************************************************************************
	 * find_n_transitions_to_sinks()
	 * 
	 * Method searches for all nodes with occurences in n strings
	 * @param node:
	 *           node from which search will be started
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
//					if (!sinks.contains(scdawg.sinks.get(j))) {
						for (int k = 0; k < scdawg.sinks.get(j).stringnumbers.size(); k++) {
							acc.add(scdawg.sinks.get(j).stringnumbers.get(k));
							// sinks.add(scdawg.sinks.get(j));
						}
//					}
				}
			}
		}

		Iterator it2 = node.children_left.entrySet().iterator();

		while (it2.hasNext()) {

			Map.Entry pair = (Map.Entry) it2.next();
			Node child = (Node) pair.getValue();

			for (int j = 0; j < scdawg.sinks.size(); j++) {
				if (scdawg.sinks.get(j) == child) {
//					if (!sinks.contains(scdawg.sinks.get(j))) {
						for (int k = 0; k < scdawg.sinks.get(j).stringnumbers.size(); k++) {
							acc.add(scdawg.sinks.get(j).stringnumbers.get(k));
							// sinks.add(scdawg.sinks.get(j));
						}
//					}
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
