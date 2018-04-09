package de.lmu.cis.iba;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

public abstract class IndexStructure {

	public HashMap<String, Integer> utf8_sequence_map;
	public HashMap<String, Integer> utf8_sequence_count;
	public ArrayList<ActivePoint> active_points = new ArrayList<ActivePoint>();

	public ArrayList<String> stringset;

	public int ALPHABET_LENGTH;
	public String id_cnt;

	public Node root;

	// public static enum RelTypes implements RelationshipType
	// {
	// CHILD_OF,
	// SUFFIX
	// }
	//

	public IndexStructure() {

		utf8_sequence_map = new HashMap<String, Integer>();
		utf8_sequence_count = new HashMap<String, Integer>();

	}

	public void determineAlphabet(boolean print) {

		for (int i = 0; i < stringset.size(); i++) {

			int pos = 0;
			String line = stringset.get(i);

			while (pos < line.length()) {
				Character letter = line.charAt(pos);
				String letter_str = letter.toString();
				// int seq_length = utf8_sequence_length((int)letter);

				Charset utf8 = Charset.forName("UTF-8");

				// char[] charsarray = letter_str.toCharArray();
				//
				// for (int j=0;j<charsarray.length;j++){
				// int utf_code = (int)charsarray[j];
				// System.out.println(letter_str+" "+utf_code);
				// }
				//
				int seq_length = new String(letter_str).getBytes(utf8).length;

				// System.out.println(pos +" " +letter_str + " " + seq_length + " | " );

				// utf8_sequence_map.put(line.substring(pos,pos+seq_length), 0);
				//
				// if(utf8_sequence_count.get(line.substring(pos,pos+seq_length))==null)
				// utf8_sequence_count.put(line.substring(pos,pos+seq_length),1);
				// else utf8_sequence_count.put(line.substring(pos,pos+seq_length),
				// utf8_sequence_count.get(line.substring(pos,pos+seq_length)) + 1);

				utf8_sequence_map.put(letter_str, 0);

				if (utf8_sequence_count.get(letter_str) == null)
					utf8_sequence_count.put(letter_str, 1);
				else
					utf8_sequence_count.put(letter_str, utf8_sequence_count.get(letter_str) + 1);

				pos++;
			} // while

		}

		int i = 0;
		Iterator it = utf8_sequence_map.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();
			pair.setValue(i++);
		}

		ALPHABET_LENGTH = utf8_sequence_count.size();

		// statistic

		if (print) {

			System.out.println(" alphabet statistic ");
			System.out.println(" --------- ");

			utf8_sequence_count = Util.sortByValues(utf8_sequence_count, "DESC");

			it = utf8_sequence_count.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry pair = (Map.Entry) it.next();
				System.out.println(" " + pair.getKey() + " : 	" + pair.getValue());
			}

			System.out.println(" alphabet length " + utf8_sequence_map.size());
			System.out.println(" --------- ");

			// alphabet mapping

			System.out.println(" alphabet mapping ");
			System.out.println(" --------- ");

			utf8_sequence_map = Util.sortByValues(utf8_sequence_map, "ASC");

			it = utf8_sequence_map.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry pair = (Map.Entry) it.next();
				System.out.println(" " + pair.getKey() + " :	 " + pair.getValue());
			}
		}

	}

	public String get_longest_member(Node node) {
		// TODO Auto-generated method stub
		return null;
	}

	// *******************************************************************************
	// get_letter_by_idx()
	// *******************************************************************************

	public String get_letter_by_idx(int idx) {
		String result = "";
		Iterator it = utf8_sequence_map.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();

			if ((int) pair.getValue() == idx) {
				result = (String) pair.getKey();
				break;
			}
		}

		return result;
	}

	// *******************************************************************************
	// get_node_label()
	// *******************************************************************************

	public String get_node_label(Node node) {

		String result;

		return "xyz";
	} // get_node_label()

	// *******************************************************************************
	// get_edge_label()
	// *******************************************************************************

	public String get_edge_label(int letter_idx, Node parent, Node node) {
		// return stringset.get(0).substring(node.end-1,node.end);
		return "ZZZZ";
	} // get_edge_label()

	public String get_edge_label_left(int key, Node root2, Node child) {
		return "ZZZZ";
	}

	public int get_node_length(Node n) {
		return -1;
	}

	public int get_edge_length(int letter, Node n1, Node n2) {
		return -1;
	}

	public int count_edges_rec(int count, Node node, HashSet<Node> used_nodes) {

		count += node.children.size();

		HashSet<Node> child_nodes = new HashSet<Node>();

		Iterator it = node.children.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();
			if (!used_nodes.contains(pair.getValue())) {
				child_nodes.add((Node) pair.getValue());
				used_nodes.add((Node) pair.getValue());
			}
		}

		for (Node child : child_nodes) {
			count = count_edges_rec(count, child, used_nodes);
		}

		return count;
	}

	public int count_edges(ArrayList<Node> allnodes) {

		int count = 0;

		for (Node n : allnodes) {
			count += n.children.size();
		}

		return count;
	}

	public int count_edges_left(ArrayList<Node> allnodes) {

		int count = 0;

		for (Node n : allnodes) {
			count += n.children_left.size();
		}

		return count;
	}

	public void eachNode_BFS(Node s, Visitor v) {

		HashMap<Node, Boolean> visited = new HashMap<Node, Boolean>();

		LinkedList<Node> queue = new LinkedList<Node>();

		visited.put(s, true);
		queue.add(s);

		while (queue.size() != 0) {
			s = queue.poll();
			v.visit(s);

			Iterator it = s.children.entrySet().iterator();
			while (it.hasNext()) {

				Map.Entry pair = (Map.Entry) it.next();
				int key = (int) pair.getKey();
				Node child = (Node) pair.getValue();

				if (!visited.containsKey(child)) {
					visited.put(child, true);

					queue.add(child);
				}

			}

		}

	}

	void DFSUtil(Node s, boolean sym, boolean rev, HashMap<Node, Boolean> visited, Visitor v) {
		visited.put(s, true);
		if (!rev)
			v.visit(s);

		Iterator it = s.children.entrySet().iterator();
		while (it.hasNext()) {

			Map.Entry pair = (Map.Entry) it.next();
			int key = (int) pair.getKey();
			Node child = (Node) pair.getValue();
			if (!visited.containsKey(child)) {
				DFSUtil(child, sym, rev, visited, v);
			}
		}

		if (sym) {

			Iterator it2 = s.children_left.entrySet().iterator();
			while (it.hasNext()) {

				Map.Entry pair = (Map.Entry) it2.next();
				int key = (int) pair.getKey();
				Node child = (Node) pair.getValue();
				if (!visited.containsKey(child)) {
					DFSUtil(child, sym, rev, visited, v);
				}
			}

		}

		if (rev)
			v.visit(s);

	}

	public void eachNode_DFS(Node s, boolean sym, boolean rev, Visitor v) {

		HashMap<Node, Boolean> visited = new HashMap<Node, Boolean>();

		DFSUtil(s, sym, rev, visited, v);
	}

	public interface Visitor {
		void visit(Node n);
	}

}
