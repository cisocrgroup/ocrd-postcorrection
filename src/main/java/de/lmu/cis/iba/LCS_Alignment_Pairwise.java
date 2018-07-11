package de.lmu.cis.iba;

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

    ArrayList<LCS_Triple> pairs;
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

		System.out.println("Searching quasi max nodes for s1 and s2 pairs...");

		ArrayList<Endpos_Pair> quasi_max_nodes = scdawg_functions.get_quasi_maximal_nodes_pairwise();
		if (quasi_max_nodes.size() == 0) {
			return;
		}
		Node[] nodes_in_s1 = new Node[scdawg.stringset.get(0).length() + 1];
		HashMap<Node, ArrayList> nodes_endpos_s2 = new HashMap();

		for (Endpos_Pair pair : quasi_max_nodes) {
			Node x = pair.node;
			System.out.println("-----------------------------------");

			System.out.println(scdawg.get_node_label(x) + ", s1=" + pair.endpos_s1 + " :: s2=" + pair.endpos_s2);
			for (int e1 : pair.endpos_s1) {
				nodes_in_s1[e1] = pair.node;
			}

			ArrayList e2 = pair.endpos_s2;
			Collections.sort(e2);
			nodes_endpos_s2.put(x, e2);
		}

		System.out.println("Calculating LCS for s1 and s2 pairs...");

		LIS_Graph g = new LIS_Graph(scdawg);
		g.build_LCS_graph(nodes_in_s1, nodes_endpos_s2);
		
		ArrayList<LCS_Triple> greedy_alignment = g.get_alignment_greedy();
		this.pairs = greedy_alignment;

	}

	public ArrayList<AlignmentPair> getAligmentPairs() {
		ArrayList<AlignmentPair> res = new ArrayList<AlignmentPair>();
			if (pairs == null) {
				res.add(new AlignmentPair("", 0, 0));
				return res;
			}
			for (int i = 0; i < pairs.size(); i++) {
				int e1 = pairs.get(i).endpos_s1;
				int e2 = pairs.get(i).endpos_s2;
				String nodelabel = scdawg.get_node_label(pairs.get(i).node);
				res.add(new AlignmentPair(nodelabel, e1, e2));
			}
		return res;
	}

}
