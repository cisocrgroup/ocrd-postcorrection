package de.lmu.cis.iba;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

public class LIS_Graph {

	public LIS_Node root;
	private Online_CDAWG_sym scdawg = null;

	HashMap<String, LIS_Node> all_nodes = new HashMap<String, LIS_Node>();
	ArrayList<LCS_Triple> lcs_triples = new ArrayList<LCS_Triple>();

	public LIS_Graph(Online_CDAWG_sym scdawg) {
		this.root = new LIS_Node("root", 0, -1);
		this.scdawg = scdawg;
	}

	// *******************************************************************************
	// build_LCS_graph()
	// *******************************************************************************

	public void build_LCS_graph(Node[] nodes_in_s1, HashMap<Node, ArrayList> nodes_endpos_s2) {

	 		LCS_Algorithm lcs = new LCS_Algorithm(this.scdawg);
	 		
	 		ArrayList<LCS_Triple> lcs_triples = new ArrayList<LCS_Triple>();
	 		HashMap<Node, Node> node_ancestors = new HashMap<Node, Node>();

	 		ArrayList[] greedy_cover =  lcs.build_greedy_cover(nodes_in_s1, nodes_endpos_s2, lcs_triples, node_ancestors);
	 		lcs.print_greedy_cover(greedy_cover, lcs_triples);
	 		
	 		this.lcs_triples = lcs_triples;
	 		
	 		Iterator it = node_ancestors.entrySet().iterator();
	 		while (it.hasNext()) {
	 			Map.Entry pair = (Map.Entry) it.next();
	 			System.out.println("Knoten: " + scdawg.get_node_label((Node) pair.getKey()) + " vorgänger "
	 					+ scdawg.get_node_label((Node) pair.getValue()));
	 		}

	 		// calculate lis


	 		 int i_count = 0;
	 		 for (int i = 0; i < greedy_cover.length; i++) {
	 		 if (greedy_cover[i].size() > 0)
	 		 i_count++;
	 		 }
	 		 System.out.println("----------------------------------------");
	 		 
	 		 
	  		for (int x = 0; x < greedy_cover[i_count - 1].size(); x++) {
	  		  Node last_cover_node = lcs_triples.get((int) greedy_cover[i_count - 1].get(x)).node;
		    	  LIS_Node lis_start_node = new LIS_Node(scdawg.get_node_label(last_cover_node),0,(int) greedy_cover[i_count - 1].get(x));

			   int lis_start_node_epos_s2 = lcs_triples.get((int) greedy_cover[i_count - 1].get(x)).endpos_s2;   
	        	   this.addTransition(this.root, lis_start_node, lis_start_node_epos_s2);
	        	 
	 		 determineLis(greedy_cover, lcs_triples, node_ancestors, this , lis_start_node, i_count - 1);
	  		}
	 	}
	

	
	public void determineLis(ArrayList[] greedy_cover, ArrayList<LCS_Triple> lcs_triples,
			HashMap<Node, Node> node_ancestors, LIS_Graph lis_graph, LIS_Node lis_node, int i) {

		for (int x = 0; x < greedy_cover[i].size(); x++) {

			Node child_node = lcs_triples.get((int) greedy_cover[i].get(x)).node;

			int child_node_epos_s2 = lcs_triples.get((int) greedy_cover[i].get(x)).endpos_s2;

			
		
			
			
			
			int next_column = 0;
			Node ancestor = null;
			//
			// System.out.println("Knoten: " + scdawg.get_node_label(child_node) + "
			// vorgänger "
			// + scdawg.get_node_label(node_ancestors.get(child_node)));
			

			ancestor = node_ancestors.get(child_node);
			if (ancestor == null) {
				break;
			}
			
			
			LIS_Node lis_child = null;
			if (lis_node.children.containsKey(child_node_epos_s2)) {
				lis_child = lis_node.children.get(child_node_epos_s2);
			} else {
				lis_child = new LIS_Node(scdawg.get_node_label(child_node), i,
						(int) greedy_cover[i].get(x));
			}
		

			if (lis_graph.all_nodes.containsKey(lis_child.label)) {
				lis_child = lis_graph.all_nodes.get(lis_child.label);
			}
			

			// wir müssen i berechenen = Spalte im cover in der k' vorkommt.

			Node ancestor_node = null;
			int child_node_epos_s2_ancestor = -1;

			for (LCS_Triple t : lcs_triples) {
				if (t.node.equals(ancestor)) {
					next_column = t.column_number;
					child_node_epos_s2_ancestor = t.endpos_s2;

				}
			}

			for (int k = 0; k < greedy_cover[i - 1].size(); k++) {

				Node child_node_2 = lcs_triples.get((int) greedy_cover[i - 1].get(k)).node;
				int child_node_2_epos_s2 = lcs_triples.get((int) greedy_cover[i - 1].get(k)).endpos_s2;

				if (child_node_2_epos_s2 <= child_node_epos_s2) {

					LIS_Node lis_child_2 = null;
					if (lis_node.children.containsKey(child_node_2_epos_s2)) {
						lis_child_2 = lis_node.children.get(child_node_2_epos_s2);
					} else {
						lis_child_2 = new LIS_Node(scdawg.get_node_label(child_node_2), i - 1,
								(int) greedy_cover[i - 1].get(k));
					}
						
				

					if (lis_graph.all_nodes.containsKey(lis_child_2.label)) {
						lis_child_2 = lis_graph.all_nodes.get(lis_child_2.label);
					}
					

			
					
					if(lis_node.label.equals(" th")&&lis_child_2.label.equals("ir")) {
						System.out.println("HIERR");
						 System.out.println("i Knoten "+scdawg.get_node_label(child_node)+" i - 1 Knoten: " + scdawg.get_node_label(child_node_2)+" child_node_2_epos_s2 "+child_node_2_epos_s2
								 +" "+ child_node_epos_s2 );
									
									System.out.println(lis_child_2.label+" ::: "+scdawg.get_node_label(child_node_2));


					}
					lis_graph.addTransition(lis_child, lis_child_2, child_node_2_epos_s2);
					determineLis(greedy_cover, lcs_triples, node_ancestors, lis_graph, lis_child_2, next_column);

				}
			}

		}
		

	}

	public void addTransition(LIS_Node parent, LIS_Node child, int epos) {
		parent.children.put(epos, child);
		all_nodes.put(child.label, child);
	}
	
	public ArrayList<LCS_Triple> get_alignment_greedy(){
		
		ArrayList<LCS_Triple> result = new ArrayList<LCS_Triple>();
		
		ArrayList<ArrayList<LCS_Triple>> all_longest_common_subsequences = this.serialize_all_LCS();
		
		int max_sum = 0;
		ArrayList<LCS_Triple> arg_max = null;
		
		for (ArrayList<LCS_Triple> alignment : all_longest_common_subsequences) {
		int sum = 0;
	
			for (LCS_Triple t : alignment) {
				sum+= scdawg.get_node_length(t.node);
			}
		if(sum>max_sum) {
			max_sum = sum;
			arg_max = alignment;
		}
			
		}
		Collections.reverse(arg_max);
		return arg_max;
		
	}

	// *******************************************************************************
	// serialize_all_LCS_indices()
	// *******************************************************************************

	public ArrayList<ArrayList<LCS_Triple>> serialize_all_LCS() {

		ArrayList<ArrayList<LCS_Triple>> result = new ArrayList<ArrayList<LCS_Triple>>();
		ArrayList<ArrayList<Integer>> all_lcs_indices = serialize_all_LCS_indices();

		for (ArrayList<Integer> lcs_indices : all_lcs_indices) {

			ArrayList<LCS_Triple> t = new ArrayList();
			for (int idx : lcs_indices) {
				t.add(this.lcs_triples.get(idx));
			}

			result.add(t);

		}

		return result;

	}

	// *******************************************************************************
	// serialize_all_LCS_indices()
	// *******************************************************************************

	public ArrayList<ArrayList<Integer>> serialize_all_LCS_indices() {

		ArrayList<ArrayList<Integer>> result = new ArrayList<ArrayList<Integer>>();

		ArrayList<Integer> first_path = new ArrayList<Integer>();
		serializeGraphUtil(this.root, first_path, result);

		return result;

	}

	public void serializeGraphUtil(LIS_Node node, ArrayList<Integer> path, ArrayList<ArrayList<Integer>> result) {

		if (node.children == null || node.children.isEmpty()) {
			result.add(path);
			return;
		}

		ArrayList<Integer> copy_path = new ArrayList<Integer>();

		for (int idx : path) {
			int new_idx = idx;
			copy_path.add(new_idx);
		}

		int cnt = 0;
		for (Map.Entry<Integer, LIS_Node> child : node.children.entrySet()) {

			if (cnt == 0) {
				path.add(child.getValue().lcs_idx);
				serializeGraphUtil(child.getValue(), path, result);
			} else {

				ArrayList<Integer> new_path = new ArrayList<Integer>();

				for (int idx : copy_path) {
					int new_idx = idx;
					new_path.add(new_idx);
				}

				new_path.add(child.getValue().lcs_idx);
				serializeGraphUtil(child.getValue(), new_path, result);

			}
			cnt++;
		}

	}

	public void printLCS() {
		
		ArrayList<ArrayList<LCS_Triple>> longest_common_subsequences = this.serialize_all_LCS();
		
		for (int u = 0; u < longest_common_subsequences.size(); u++) {
			ArrayList<LCS_Triple> lis = longest_common_subsequences.get(u);
			for (int i = 0; i < lis.size(); i++) {
				int e1 = lis.get(i).endpos_s1;
				int e2 = lis.get(i).endpos_s2;
				String nodelabel = scdawg.get_node_label(lis.get(i).node);
				System.out.printf("\"%s\":e1:\"%s\", e2:\"%s\"\n", nodelabel, e1, e2);
			}
			System.out.println("---------------------------------------");
		}

	}

	public void printLCS(ArrayList<LCS_Triple> lis) {
		
			for (int i = 0; i < lis.size(); i++) {
				int e1 = lis.get(i).endpos_s1;
				int e2 = lis.get(i).endpos_s2;
				String nodelabel = scdawg.get_node_label(lis.get(i).node);
				System.out.printf("\"%s\":e1:\"%s\", e2:\"%s\"\n", nodelabel, e1, e2);
			}
			System.out.println("---------------------------------------");
		

	}
	
	public void print_graph(String outputfile) {

		long startTime = System.currentTimeMillis();
		System.out.println(" ..printing Graph ");

		String filename = "lis.dot";

		StringBuilder sb = new StringBuilder();

		// dotfile
		sb.append("digraph lis_graph { ");
		sb.append("\n" + "labeljust=l");
		sb.append("\n" + "fontname=Vera");
		sb.append("\n" + "fontsize=20");
		sb.append("\n" + "labelloc=top");
		sb.append("\n" + "margin=.5");
		sb.append("\n" + "size=\"15,7\"");
		sb.append("\n" + "nodesep=.3");
		sb.append("\n" + "rankdir=\"LR\"");
		sb.append("\n"
				+ "node [width=0.5,height=auto,shape=record,fontsize=12,fontcolor=black,style=filled,fillcolor=sandybrown];");
		sb.append("\n" + "edge [minlen=1,constraint=true,fontsize=11,labelfontsize=11];");

		sb.append("\n");
		sb.append("\n");

		//
		String edge_list = "", node_list = "";
		sb.append("/* Nodes */ \n \n");
		//
		String nodeslist = print_nodes();
		sb.append(nodeslist);
		sb.append("\n /* Edges */ \n \n");
		// print_edges(root,edge_list);
		String edgeslist = print_edges();
		sb.append(edgeslist + "}");

		String result = sb.toString();

		Util.writeFile(filename, result);

		String[] cmd = { "/usr/bin/dot -Tsvg lis.dot -o " + outputfile + "_.svg" };
//		String[] cmd = { "cmd.exe", "/c", "dot -Tsvg lis.dot -o " + outputfile + "_.svg" };

		try {

			Process p = Runtime.getRuntime().exec(cmd);

			BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));

			String temp = "";

			while ((temp = input.readLine()) != null)
				System.out.println(temp);

			input.close();
			p.waitFor();

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		long duration = System.currentTimeMillis() - startTime;
		System.out.println(" ... took " + duration + " milliseconds");

	} // print_automaton()

	public String print_nodes() {

		String result = "";

		String node_str;
		StringBuilder node_sstr = new StringBuilder();

		this.eachNode_DFS(this.root, true, false, new Visitor() {
			public void visit(LIS_Node node) {

				node_sstr.append("\"" + node.label + "\"");
				node_sstr.append("; \n");
			}

		});
		result = node_sstr.toString();

		return result;

	} // print_nodes()

	// *******************************************************************************
	// print_edges()
	// *******************************************************************************
	public String print_edges() {

		String result = "";
		StringBuilder edge_sstr = new StringBuilder();

		this.eachNode_DFS(this.root, true, false, new Visitor() {
			public void visit(LIS_Node node) {

				Iterator it = node.children.entrySet().iterator();
				while (it.hasNext()) {

					Map.Entry pair = (Map.Entry) it.next();

					int i = (int) pair.getKey();
					edge_sstr.append("\"" + node.label + "\" -> \"" + node.children.get(i).label + "\"");

					String label = "";
					label = "" + i;

					edge_sstr.append(" [style=" + "solid" + "  label=\"" + label + "\"];" + "\n");

				} // for it node children

			} // for all nodes

		});

		result = edge_sstr.toString();

		return result;

	} // print_edges()

	public void eachNode_BFS(LIS_Node s, boolean rev, boolean all, Visitor v) {

		HashMap<LIS_Node, Boolean> visited = new HashMap<LIS_Node, Boolean>();

		LinkedList<LIS_Node> queue = new LinkedList<LIS_Node>();

		visited.put(s, true);
		queue.add(s);

		while (queue.size() != 0) {
			s = queue.poll();
			if (!rev)
				v.visit(s);

			for (Map.Entry<Integer, LIS_Node> child : s.children.entrySet()) {

				if (all) {
					visited.put(child.getValue(), true);
					queue.add(child.getValue());
				}

				else if (!visited.containsKey(child.getValue())) {
					visited.put(child.getValue(), true);

					queue.add(child.getValue());
				}
			}

			if (rev)
				v.visit(s);

		}

	}

	void DFSUtil(LIS_Node s, boolean rev, boolean all, HashMap<LIS_Node, Boolean> visited, Visitor v) {
		visited.put(s, true);
		if (!rev) {
			v.visit(s);
		}

		for (Map.Entry<Integer, LIS_Node> child : s.children.entrySet()) {

			if (all) {
				DFSUtil(child.getValue(), rev, all, visited, v);
			}

			else if (!visited.containsKey(child.getValue())) {
				DFSUtil(child.getValue(), rev, all, visited, v);
			}
		}

		if (rev) {
			v.visit(s);
		}

	}

	public void eachNode_DFS(LIS_Node s, boolean rev, boolean all, Visitor v) {

		HashMap<LIS_Node, Boolean> visited = new HashMap<LIS_Node, Boolean>();

		DFSUtil(s, rev, all, visited, v);
	}

	public interface Visitor {
		void visit(LIS_Node n);
	}

}
