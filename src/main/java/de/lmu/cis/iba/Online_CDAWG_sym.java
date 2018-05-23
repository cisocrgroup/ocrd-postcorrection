package de.lmu.cis.iba;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import org.pmw.tinylog.Logger;


// import indexstructure.Neo4J_Handler;

public class Online_CDAWG_sym extends IndexStructure {

	ActivePoint ap;

	public Node root, sink, suffixstate, old_suffixstate, split;

	public ArrayList<Node> all_nodes;
	public ArrayList<Node> sinks = new ArrayList();

	int id_cnt;
	int split_cnt = 0;

	public String input_text;
	boolean letters_available;

	int stringcount;

	int pos = 0;

	boolean print = false;

	public Online_CDAWG_sym(ArrayList<String> _stringset, boolean print) {

		super();

		id_cnt = 0;
		stringset = _stringset;
		input_text = "";
		letters_available = true;

		this.print = print;

		all_nodes = new ArrayList();

	} // Online_CDAWG()

	public void build_cdawg() {

		long startTime = System.currentTimeMillis();
		Logger.debug(" ..building CDAWG ");

		// 1. Create a state root

		root = create_node(-1, 0, 0, 0);

		stringcount = 0;

		main_loop: while (stringcount < stringset.size()) {

			input_text = stringset.get(stringcount);

			// 1. Create a state sink

			for (int i = 0; i < sinks.size(); i++) {
				if (this.get_node_label(sinks.get(i)).equals(input_text)) {
					// sink.stringnumbers.add(stringcount);
					sinks.get(i).stringnumbers.add(stringcount);
					stringcount++;
					continue main_loop;
				}
			}

			sink = create_node(0, 0, 0, stringcount);
			sink.stringnumbers.add(stringcount);
			sink.is_endNode = true;
			sinks.add(sink);

			ap = new ActivePoint(root, -1, 0);

			pos = -1;

			// 2. For each letter a of w do:

			letters_available = true;
			while (letters_available) {
				update();
				if (pos >= input_text.length() - 1)
					letters_available = false;
			} // letters_available

			sink.suffixLink = ap.active_node;

			if (ap.active_node == root) {
				Node target_active_edge = root.children.get(ap.active_edge);
				if (!target_active_edge.is_endNode) {
					sink.suffixLink = target_active_edge;
				}
			}
			System.out.println(all_nodes.size());

			stringcount++;
		}

		// add left edges

		// add other left edges

		Logger.debug(" Creating left edges...");
		Logger.debug(" Step 1...");

		for (int i = 0; i < this.all_nodes.size(); i++) {

			Node node = all_nodes.get(i);

			if (node == root) {

				Iterator it = node.children_new.entrySet().iterator();

				while (it.hasNext()) {
					Map.Entry pair = (Map.Entry) it.next();
					int key = (int) pair.getKey();
					EdgeInfo edgeinfo = (EdgeInfo) pair.getValue();

					if (!node.children_left_new.containsKey(key)) {
						// System.out.println(this.get_edge_length_new(key, node, edgeinfo.child));

						// System.out.println("'" + this.get_node_label(node) + "' " +
						// this.get_letter_by_idx(key) + " "
						// + this.get_node_label(edgeinfo.child) + " " + edgeinfo.pos);

						int left_char_occ = edgeinfo.pos - this.get_node_length(node);
						// System.out.println("left char occ " + left_char_occ);

						// System.out.println("left char " + this.get_letter(left_char_occ,
						// edgeinfo.child.stringnr));
						create_edge_left(node, edgeinfo.child, this.get_letter(left_char_occ, edgeinfo.child.stringnr),
								left_char_occ); // create

					}

				}

			}

			else {

				Iterator it = node.children_new.entrySet().iterator();

				while (it.hasNext()) {
					Map.Entry pair = (Map.Entry) it.next();
					int key = (int) pair.getKey();
					EdgeInfo edgeinfo = (EdgeInfo) pair.getValue();

					// if(!node.children_left_new.containsKey(key)){
					// System.out.println(this.get_edge_length_new(key, node, edgeinfo.child));
					if (this.get_edge_length_new(key, node, edgeinfo.child) + this.get_node_length(node) == this
							.get_node_length(edgeinfo.child)) {
						continue;
					}
					// System.out.println("'" + this.get_node_label(node) + "' " +
					// this.get_letter_by_idx(key) + " "
					// + this.get_node_label(edgeinfo.child) + " " + edgeinfo.pos);
					////
					int left_char_occ = edgeinfo.pos - this.get_node_length(node) - 1;
					// System.out.println("left char occ " + left_char_occ);

					// System.out.println("left char " + this.get_letter(left_char_occ,
					// edgeinfo.child.stringnr));
					create_edge_left(node, edgeinfo.child, this.get_letter(left_char_occ, edgeinfo.child.stringnr),
							left_char_occ); // create

					// }

				}

			}

		}
		Logger.debug(" Step 2...");

		// reverse suffixlinks
		for (int i = 0; i < this.all_nodes.size(); i++) {

			Node suffixNode = all_nodes.get(i).suffixLink;
			Node node = all_nodes.get(i);

			if (suffixNode == null) {
				continue;
			}

			int left_char_occ = node.end - this.get_node_length(suffixNode);
			create_edge_left(suffixNode, node, this.get_letter(left_char_occ, node.stringnr), left_char_occ); // create

		}
		Logger.debug(" Step 3...");

		// copy prefix links
		for (int i = 0; i < this.all_nodes.size(); i++) {

			Node suffixNode = all_nodes.get(i).suffixLink;
			Node node = all_nodes.get(i);

			Iterator it = node.children_new.entrySet().iterator();

			while (it.hasNext()) {
				Map.Entry pair = (Map.Entry) it.next();
				int key = (int) pair.getKey();
				EdgeInfo edgeinfo = (EdgeInfo) pair.getValue();

				if (this.get_edge_length_new(key, node, edgeinfo.child) + this.get_node_length(node) == this
						.get_node_length(edgeinfo.child)) {

					Iterator it2 = edgeinfo.child.children_left.entrySet().iterator();
					while (it2.hasNext()) {
						Map.Entry pair2 = (Map.Entry) it2.next();
						int key2 = (int) pair2.getKey();
						Node child2 = (Node) pair2.getValue();
						if (!node.children_left.containsKey(key2)) {
							node.children_left.put(key2, child2);
						}
					}

				}

			}

		}

		long duration = System.currentTimeMillis() - startTime;
		Logger.debug(" ... took " + duration + " milliseconds");
		Logger.debug(" ... memory used:" + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
		Logger.debug(" ... memory free:" + (Runtime.getRuntime().freeMemory()));
		Logger.debug(" ... memory total:" + (Runtime.getRuntime().totalMemory()));

	}

	boolean canonize(Node node, int pos) {

		int edgelength_new = this.get_edge_length_new(ap.active_edge, ap.active_node,
				ap.active_node.children.get(ap.active_edge));

		if (ap.active_length >= edgelength_new) {

			if (print)
				System.out.println("CANONIZE");
			ap.active_length -= edgelength_new;

			int next_pos = pos - ap.active_length;

			if (ap.active_length > 0) {

				ap.active_edge = this.get_letter(next_pos, stringcount);
			}

			ap.active_node = node;

			return true;
		}
		return false;
	}

	private void add_suffixlink(Node node) {
		if (print)
			System.out.println("SUFFIX START <" + this.get_node_label(node) + ">");
		if (suffixstate != null) {
			suffixstate.suffixLink = node;
			old_suffixstate = suffixstate;

		}
		suffixstate = node;
	}

	// *******************************************************************************
	// update()
	// *******************************************************************************
	private void update() {

		pos++;
		int a = get_letter(pos, stringcount);
		suffixstate = null;
		old_suffixstate = null;
		split = null;
		Node active_child = null;

		sink.end = pos;
		sink.pathlength = pos + 1;

		while (true) {

			if (ap.active_length == 0)
				ap.active_edge = a;

			if (print)
				System.out.println(" pos: " + pos + " " + input_text.charAt(pos) + " (active_node: " + ap.active_node.id
						+ " <" + this.get_node_label(ap.active_node) + "> active_edge: " + ap.active_edge + " ["
						+ this.get_letter_by_idx(ap.active_edge) + "] active length: " + ap.active_length + ")");

			if (!(has_outgoing_edge(ap.active_node, ap.active_edge))) { // if no edge with label of
																		// active edge from active
																		// point

				create_edge(ap.active_node, sink, ap.active_edge, pos); // create new edge from active_node to sink

				add_suffixlink(ap.active_node); // rule 2

			}

			else {
				if (print)
					System.out.println("ELSE");

				Node next = ap.active_node.children.get(ap.active_edge);
				if (canonize(next, pos))
					continue; // observation 2
				String current_letter = get_letter_by_idx(a);

				int active_label_length_new = this.get_edge_length_new(ap.active_edge, ap.active_node, next);

				int last_char_pos = next.end + 1 - active_label_length_new + ap.active_length;
				Character last_char_new = stringset.get(next.stringnr).charAt(last_char_pos);
				String last_suffix_new = last_char_new.toString();

				if (last_suffix_new.equals(current_letter)) { // observation 1 current auf
																// active edge vorhanden =>
																// kein neuer rechtskontext.
					if (print)
						System.out.println("ACTIVE LENGTH ++");
					ap.active_length++;
					add_suffixlink(ap.active_node); // observation 3
					break;
				}

				// Redirect
				if (active_child == ap.active_node.children.get(ap.active_edge)) {
					redirect_edge(ap.active_node, split, ap.active_edge);
					if (print)
						System.out.println("REDIRECT");
				}

				// Split
				else {
					if (print)
						System.out.println("SPLIT");

					active_child = ap.active_node.children.get(ap.active_edge);

					int stringnr = next.stringnr;

					int end_new_node = ap.active_node.children_new.get(ap.active_edge).pos + ap.active_length - 1;
					int start_new_node = end_new_node - this.get_node_length(ap.active_node) + 1 - ap.active_length;

					int start_new = end_new_node + 1;

					split = create_node(start_new_node,
							start_new_node + ap.active_node.pathlength + ap.active_length - 1,
							ap.active_node.pathlength + ap.active_length, stringnr);

					// create_edge(ap.active_node, split, ap.active_edge, pos); // create edge from
					// active_node to split

					redirect_edge(ap.active_node, split, ap.active_edge);

					create_edge(split, sink, a, pos); // create edge from split to sink

					create_edge(split, next, this.get_letter(last_char_pos, next.stringnr), start_new); // create
					// edge from
					// split
					// to next
					add_suffixlink(split); // rule 2

					// split
				}
			}

			if (ap.active_node == root && ap.active_length > 0) { // rule 1
				if (print)
					System.out.println("RULE 1");
				ap.active_edge = get_letter(pos - ap.active_length + 1, stringcount); // find the next shortest
																						// suffix (e.g. after root ->
																						// ab ; root -> b)
				ap.active_length--;

			} else {
				if (ap.active_node.suffixLink != null) { // rule 3
					if (print)
						System.out.println("RULE 3");
					ap.active_node = ap.active_node.suffixLink;
				} else {
					ap.active_node = root;
					if (print)
						System.out.println("AP -> ROOT");

					break;
				}
			}

		} // while

		separate_node();

		if (print)
			System.out.println(
					"-----------------------------------------------------------------------------------------");
	}

	// *******************************************************************************
	// separate_node()
	// *******************************************************************************

	void separate_node() {

		Node next = ap.active_node.children.get(ap.active_edge);
		int ap_rep_length = 0;
		ap_rep_length = this.get_node_length(ap.active_node);

		int next_rep_length = this.get_node_length(next);

		// if(print) System.out.println(this.get_edge_label(ap.active_edge,
		// ap.active_node, next));

		int edgelength_new = this.get_edge_length_new(ap.active_edge, ap.active_node, next);

		if (ap.active_length == edgelength_new && next_rep_length > ap.active_length + ap_rep_length) {
			if (print)
				System.out.println("SEPARATE NODE" + next.pathlength);

			Node copy_node = this.create_node(next.end - (edgelength_new + this.get_node_length(ap.active_node)) + 1,
					next.end, this.get_node_length(ap.active_node) + edgelength_new, next.stringnr);

			Iterator it = next.children.entrySet().iterator();

			while (it.hasNext()) {
				Map.Entry pair = (Map.Entry) it.next();
				int key = (int) pair.getKey();
				Node child = (Node) pair.getValue();
				copy_node.children.put(key, child);

			}

			it = next.children_new.entrySet().iterator();

			while (it.hasNext()) {
				Map.Entry pair = (Map.Entry) it.next();
				int key = (int) pair.getKey();
				EdgeInfo edgeinfo_old = (EdgeInfo) pair.getValue();

				copy_node.children_new.put(key, new EdgeInfo(edgeinfo_old.child, edgeinfo_old.pos));

			}

			copy_node.suffixLink = next.suffixLink;
			next.suffixLink = copy_node;
			if (print)
				System.out.println("SEPARATE NODE ID " + copy_node.id + " REP " + this.get_node_label(copy_node));

			redirect_edge(ap.active_node, copy_node, ap.active_edge);

			Node old_next = next;

			if (print)
				System.out.println("ANFANG ACTIVE LENGTH " + ap.active_length);

			while (true) {

				if (ap.active_node.suffixLink != null) { // rule 3
					if (print)
						System.out.println("RULE 3");
					ap.active_node = ap.active_node.suffixLink;

				}

				else {

					if (print)
						System.out.println("RULE 1");
					ap.active_length--;

					if (ap.active_length == 0) {
						break;
					}

					ap.active_edge = get_letter(pos - ap.active_length + 1, stringcount); // find the next shortest
																							// suffix (e.g. after root
																							// -> ab ; root -> b)

					if (print)
						System.out.println("RULE1 suffix" + ap.active_edge);
				}

				next = ap.active_node.children.get(ap.active_edge);

				while (this.get_edge_length_new(ap.active_edge, ap.active_node, next)
						+ this.get_node_length(ap.active_node) >= this.get_node_length(next)) {
					canonize(next, pos + 1);

					if (ap.active_length == 0) {
						break;
					}
					next = ap.active_node.children.get(ap.active_edge);
				}

				// if(this.get_node_length(copy_node)>(this.get_edge_length(ap.active_edge,
				// ap.active_node,old_next)+this.get_node_length(ap.active_node))){
				if (old_next == next) {
					if (print)
						System.out.println("SEPARATE REDIRECT");
					redirect_edge(ap.active_node, copy_node, ap.active_edge);

				}
				if (print)
					System.out
							.println("APPPPP " + ap.active_node.id + " " + ap.active_edge + " al " + ap.active_length);
				if (print)
					System.out.println(" pos: " + pos + " " + input_text.charAt(pos) + " (active_node: "
							+ ap.active_node.id + " <" + this.get_node_label(ap.active_node) + "> active_edge: "
							+ ap.active_edge + " [" + this.get_letter_by_idx(ap.active_edge) + "] active length: "
							+ ap.active_length + ")");

				if (ap.active_length == 0) {
					break;
				}
			}

			ap.active_node = copy_node;
		}
	}

	// *******************************************************************************
	// redirect_edge()
	// *******************************************************************************

	void redirect_edge(Node start, Node target, int edge) {

		start.children.put(edge, target);

		EdgeInfo new_edge_destination = new EdgeInfo(target, start.children_new.get(edge).pos);
		start.children_new.put(edge, new_edge_destination);

		/// ** # # # ** ## ** #'**** ##** NEU NEU

		// KEY BLEIBT BUCHTSTABE dann Object Edge mit Position und Zielknoten.
	}

	// *******************************************************************************
	// get_letter()
	// *******************************************************************************

	int get_letter(int pos, int stringnr) {

		Character letter = stringset.get(stringnr).charAt(pos);
		return utf8_sequence_map.get(letter.toString());
	}

	// *******************************************************************************
	// get_node_label()
	// *******************************************************************************

	public String get_node_label(Node node) {

		String result;
		// System.out.println(stringset.get(0).substring(node.end-node.pathlength,node.end)+"
		// pathlength: "+node.pathlength);

		try {

			if (node == root)
				return "";

			else {
				return stringset.get(node.stringnr).substring(node.start, node.end + 1);
			}

		} catch (Exception e) {

			return "X";
		}

	} // get_node_label()

	// *******************************************************************************
	// get_node_length()
	// *******************************************************************************

	public int get_node_length(Node node) {

		return node.pathlength;

	} // get_node_label()

	// *******************************************************************************
	// get_edge_label()
	// *******************************************************************************

	public String get_edge_label(int letter_idx, Node parent, Node node) {

		String rep_parent = get_node_label(parent);
		String rep_child = get_node_label(node);
		if (rep_parent.equals("λ"))
			rep_parent = "";

		String letter = this.get_letter_by_idx(letter_idx);
		// System.out.println("rep_parent: "+rep_parent+" rep_child: "+rep_child+"
		// letter "+letter+" node_id "+node.id+ " node start "+node.start);

		try {

			int start = rep_child.indexOf(rep_parent + letter);

			return rep_child.substring(start + rep_parent.length(), rep_child.length());

		} catch (Exception e) {
			return "X";
		}

	} // get_edge_label()

	// *******************************************************************************
	// get_edge_label_left()
	// *******************************************************************************

	public String get_edge_label_left(int letter_idx, Node parent, Node node) {

		String rep_parent = new StringBuilder(get_node_label(parent)).toString();
		String rep_child = new StringBuilder(get_node_label(node)).toString();
		if (rep_parent.equals("λ"))
			rep_parent = "";

		String letter = this.get_letter_by_idx(letter_idx);
		// System.out.println("rep_parent: "+rep_parent+" rep_child: "+rep_child+"
		// letter "+letter+" node_id "+node.id+ " node start "+node.start);

		try {
			//
			//
			int end = rep_child.lastIndexOf(letter + rep_parent) + 1;
			//
			//
			String result = new StringBuilder(rep_child.substring(0, end)).reverse().toString();
			return result;
			// return this.get_letter_by_idx(letter_idx);
		} catch (Exception e) {
			return "X";
		}

	} // get_edge_label_left()

	// *******************************************************************************
	// get_edge_length()
	// *******************************************************************************

	public int get_edge_length(int letter_idx, Node parent, Node node) {

		String rep_parent = get_node_label(parent);
		String rep_child = get_node_label(node);
		if (rep_parent.equals("λ"))
			rep_parent = "";

		String letter = this.get_letter_by_idx(letter_idx);
		// System.out.println("rep_parent: "+rep_parent+" rep_child: "+rep_child+"
		// letter "+letter+" node_id "+node.id+ " node start "+node.start);

		try {

			int start = rep_child.indexOf(rep_parent + letter);
			// System.out.println("START OLD " + start);

			return rep_child.length() - (start + rep_parent.length());

		} catch (Exception e) {
			return -2;
		}

	} // get_edge_length()

	// *******************************************************************************
	// get_edge_length_new()
	// *******************************************************************************

	public int get_edge_length_new(int letter_idx, Node parent, Node node) {

		try {
			EdgeInfo edge_info = parent.children_new.get(letter_idx);
			int start = edge_info.pos;

			if (print)
				System.out.println("START NEW " + start + " node end " + node.end);

			return node.end - start + 1;

		} catch (Exception e) {
			return -2;
		}

	} // get_edge_length_new()

	private boolean has_outgoing_edge(Node node, int label) {
		Node n = node.children.get(label);
		if (n == null)
			return false;
		return true;
	}

	private boolean has_outgoing_left_edge(Node node, int label) {
		Node n = node.children_left.get(label);
		if (n == null)
			return false;
		return true;
	}

	public Node get_parent(Node node) {
		return node.suffixLink;
	} // get_parent()

	public void save_graph_to_db() {

		long startTime = System.currentTimeMillis();
		System.out.println("\n ..saving graph to database ");

		// Neo4J_Handler neo4j = new Neo4J_Handler("C:/Neo4j/Neo4CDAWG_test",this);

		// neo4j.connect_and_clear_graphDb();
		// neo4j.create_node_db(root);
		//
		// neo4j.link_children_db(root);
		// neo4j.link_suffixes_db(root);

		long duration = System.currentTimeMillis() - startTime;
		System.out.println(" ... took " + duration + " milliseconds \n");
	}

	// *******************************************************************************
	// create_node()
	// *******************************************************************************

	private Node create_node(int start, int end, int pathlength, int stringnr) {

		// if(pos==-2) node = new Node();
		id_cnt = id_cnt + 1;
		int new_idcnt = id_cnt;

		Node node = new Node(start, end, pathlength, stringnr, id_cnt);

		// Iterator it = utf8_sequence_map.entrySet().iterator();
		// while (it.hasNext()) {
		// Map.Entry pair = (Map.Entry)it.next();
		//
		// node.children.put((Integer) pair.getValue(),null);
		// }

		all_nodes.add(node);

		return node;
	}

	// *******************************************************************************
	// create_edge()
	// *******************************************************************************

	public void create_edge(Node parent, Node child, int label, int pos) {

		parent.children.put(label, child);
		parent.children_new.put(label, new EdgeInfo(child, pos));

	} // create_edge()

	// *******************************************************************************
	// create_edge_left()
	// *******************************************************************************

	public void create_edge_left(Node parent, Node child, int label, int pos) {

		// for(int i=0;i<sinks.size();i++) {
		// Node sink= sinks.get(i);
		// if(child==sink&&parent.right_end) {
		// quasi_candidates.get(sink.stringnr).put(parent, label);
		// }
		// }
		parent.children_left.put(label, child);
		parent.children_left_new.put(label, new EdgeInfo(child, pos));

	} // create_edge()

	public void print_automaton(String outputfile) {

		long startTime = System.currentTimeMillis();
		System.out.println(" ..printing CDAWG ");

		String filename = "scdawg.dot";

		StringBuilder sb = new StringBuilder();

		// dotfile
		sb.append("digraph cdawg_graph { ");
		sb.append("\n" + "labeljust=l");
		sb.append("\n" + "fontname=Vera");
		sb.append("\n" + "fontsize=20");
		sb.append("\n" + "labelloc=top");
		sb.append("\n" + "margin=.5");
		sb.append("\n" + "size=\"15,7\"");
		sb.append("\n" + "nodesep=.3");
		sb.append("\n"
				+ "node [width=0.5,height=auto,shape=record,fontsize=12,fontcolor=black,style=filled,fillcolor=sandybrown];");
		sb.append("\n" + "edge [minlen=1,constraint=true,fontsize=11,labelfontsize=11];");

		sb.append("\n");
		sb.append("\n");

		//
		String edge_list = "", node_list = "";
		// cout << endl << " Nodes" << endl << " -----" << endl;
		sb.append("/* Nodes */ \n \n");
		//
		String nodeslist = print_nodes();
		sb.append(nodeslist);
		// cout << endl << " Edges" << endl << " -----" << endl;
		sb.append("\n /* Edges */ \n \n");
		// print_edges(root,edge_list);
		String edgeslist = print_edges();
		sb.append(edgeslist + "}");

		String result = sb.toString();

		Util.writeFile(filename, result);

		// String[] cmd = { "konsole", "-c", "dot -Tsvg scdawg.dot -o " + outputfile +
		// "_CDAWG.svg" };
		String[] cmd = { "cmd.exe", "/c", "dot -Tsvg scdawg.dot -o " + outputfile + "_CDAWG.svg" };

		System.out.println(cmd[0]);
		System.out.println(cmd[1]);
		System.out.println(cmd[2]);

		try {

			Process p = Runtime.getRuntime().exec(cmd);

			BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));

			String temp = "";

			while ((temp = input.readLine()) != null)
				System.out.println(temp);

			input.close();

		} catch (IOException e) {
		}

		long duration = System.currentTimeMillis() - startTime;
		System.out.println(" ... took " + duration + " milliseconds");

	} // print_automaton()

	public String get_dot() {

		long startTime = System.currentTimeMillis();

		StringBuilder sb = new StringBuilder();

		//
		String edge_list = "", node_list = "";
		// cout << endl << " Nodes" << endl << " -----" << endl;
		//
		String nodeslist = print_nodes();
		sb.append(nodeslist);
		// cout << endl << " Edges" << endl << " -----" << endl;
		// print_edges(root,edge_list);
		String edgeslist = print_edges();
		sb.append(edgeslist + "}");

		String result = sb.toString();

		return result;

	} // get_dot()

	String print_nodes() {

		String result = "";

		String node_str;
		StringBuilder node_sstr = new StringBuilder();

		// String[] levels = new String[stringset.get(0).length()];
		// for (int i=0;i<levels.length;i++) levels[i] = "{rank=same;";

		for (int r = 0; r < all_nodes.size(); r++) {

			Node node = all_nodes.get(r);

			int node_number;
			if (node.end == -1)
				node_number = node.end + 1;
			else
				node_number = node.end;

			String node_id_string = " n_" + node.id + "_" + node_number + " ";

			node_sstr.append(node_id_string);

			// Multimap<Integer,Integer> equivalence_classes =
			// get_equivalence_classes(node);

			//
			// ArrayList eq_strings_map =
			// equivalence_classes_to_strings(equivalence_classes);
			//
			//
			String rep = this.get_node_label(node);
			if (rep.length() == 0)
				rep = "λ";

			node_sstr.append("[label=< id=" + node.id + "<br/> Rep: \"" + rep + "\"<br/>");
			// node_sstr.append("[label=<\"" + rep + "\">");

			node_sstr.append("Start: " + node.start + " <br/> End: " + node.end + "<br/> Pathlength: " + node.pathlength
					+ "<br/> Stringnr: " + node.stringnr + "<br/>>");

			node_sstr.append("]; \n");
			// levels[node.pathlength] += node_id_string+"; ";
		}

		// for (int i=0;i<levels.length;i++) node_sstr.append(levels[i]+"}\n");

		result = node_sstr.toString();

		return result;

	} // print_nodes()

	// *******************************************************************************
	// print_nodes_rec()
	// *******************************************************************************
	String print_nodes_rec(Node node) {

		String result = "";

		StringBuilder node_sstr = new StringBuilder();

		String node_str = "";

		int node_number;
		if (node.end == -1)
			node_number = node.end + 1;
		else
			node_number = node.end;

		node_sstr.append(" n_" + node.id + "_" + node_number + " ");

		String rep = "";
		if (node != root)
			rep = this.get_node_label(node);
		else
			rep = "λ";

		node_sstr.append("[label=< id=" + node.id + "<br/> Rep: \"" + rep + "\"<br/>>");

		node_sstr.append("]; \n");

		StringBuilder childlevel = new StringBuilder();
		childlevel.append("{ rank= same; ");

		Iterator it = node.children.entrySet().iterator();
		while (it.hasNext()) {

			Map.Entry pair = (Map.Entry) it.next();

			Node child = (Node) pair.getValue();
			int i = (int) pair.getKey();

			String child_node_str = print_nodes_rec(child);
			node_sstr.append(child_node_str);

			int node_number_end;
			if (node.children.get(i).end == -1)
				node_number_end = 0;
			else
				node_number_end = node.children.get(i).end;

			childlevel.append(" n_" + node.children.get(i).id + "_" + node_number_end + "; ");
		}

		childlevel.append("}");

		node_sstr.append(childlevel.toString() + "\n");

		result = node_sstr.toString();

		return result;

	} // print_nodes_rec()

	// *******************************************************************************
	// print_edges()
	// *******************************************************************************
	public String print_edges() {

		String result = "";
		StringBuilder edge_sstr = new StringBuilder();

		for (int r = 0; r < all_nodes.size(); r++) {

			Node node = all_nodes.get(r);
			// if (node->ident_pointers.size()>0||node==root) {
			int label_count = 0;

			Iterator it = node.children.entrySet().iterator();
			while (it.hasNext()) {

				Map.Entry pair = (Map.Entry) it.next();

				int i = (int) pair.getKey();

				int node_number;
				if (node.end == -1)
					node_number = node.end + 1;
				else
					node_number = node.end;

				int node_number_end;
				if (node.children.get(i).end == -1)
					node_number_end = 0;
				else
					node_number_end = node.children.get(i).end;

				edge_sstr.append(" n_" + node.id + "_" + node_number + " -> n_" + node.children.get(i).id + "_"
						+ node_number_end + " ");

				String label = "";
				label = this.get_edge_label(i, node, node.children.get(i));

				String regex = "\\\"";

				edge_sstr.append(" [style=" + "solid" + "  label=\"" + label + "\"];" + "\n");

			} // for it node children

			it = node.children_left.entrySet().iterator();
			while (it.hasNext()) {

				Map.Entry pair = (Map.Entry) it.next();

				int i = (int) pair.getKey();

				int node_number;
				if (node.end == -1)
					node_number = node.end + 1;
				else
					node_number = node.end;

				int node_number_end;
				if (node.children_left.get(i).end == -1)
					node_number_end = 0;
				else
					node_number_end = node.children_left.get(i).end;

				edge_sstr.append(" n_" + node.id + "_" + node_number + " -> n_" + node.children_left.get(i).id + "_"
						+ node_number_end + " ");

				String label = "";
				label = this.get_edge_label_left(i, node, node.children_left.get(i));

				String regex = "\\\"";

				edge_sstr.append(" [color=blue" + "  label=\"" + label + "\"];" + "\n");

			} // for it node children_left

			Node suffixLink = node.suffixLink;

			if (suffixLink != null) {

				int node_number;
				if (node.end == -1)
					node_number = node.end + 1;
				else
					node_number = node.end;

				int node_number_end;
				if (node.suffixLink.end == -1)
					node_number_end = node.suffixLink.end + 1;
				else
					node_number_end = node.suffixLink.end;

				edge_sstr.append(" n_" + node.id + "_" + node_number + " -> n_" + node.suffixLink.id + "_"
						+ node_number_end + " ");
				edge_sstr.append(" [color=red];\n");
			}

			// edge_list+= edge_str;
			label_count++;

			// if (node.prefixLinks.size()>0)
			// {
			//
			// for (int k=0;k<node->prefixLinks.size();k++){
			// string edge_str;
			// stringstream edge_sstr;
			//
			// int node_number;
			// if (node->end==-1) node_number=node->end+1;
			// else node_number = node->end;
			//
			// int node_number_end;
			// if (node->prefixLinks[k]->end==-1)
			// node_number_end=node->prefixLinks[k]->end+1; else node_number_end =
			// node->prefixLinks[k]->end;
			//
			// edge_sstr << " n_" << node->id << "_" << node_number << " -> n_" <<
			// node->prefixLinks[k]->id << "_" << node_number_end << " "; edge_str =
			// edge_sstr.str();
			//
			// string label = node->prefixlabels[k];
			// string regex = "\\\"";
			// label = ReplaceAll(label,std::string("\""), std::string(regex));
			// // if ( edge_list.find(edge_str) != string::npos ) return;
			// // cout << edge_str << " ["<< "prefix_link label=" <<
			// node->prefixlabels[k] << "];" << endl; dotfile << edge_str << "
			// [color=blue label=\"" << label << "\"];" << endl; edge_list+=
			// edge_str;
			//
			// }
			// }

			// }
		} // for all nodes

		result = edge_sstr.toString();

		return result;

	} // print_edges()

	public void print_automaton_new(String outputfile) {

		long startTime = System.currentTimeMillis();
		System.out.println(" ..printing CDAWG ");

		String filename = "scdawg.dot";

		StringBuilder sb = new StringBuilder();

		// dotfile
		sb.append("digraph cdawg_graph { ");
		sb.append("\n" + "labeljust=l");
		sb.append("\n" + "fontname=Vera");
		sb.append("\n" + "fontsize=20");
		sb.append("\n" + "labelloc=top");
		sb.append("\n" + "margin=.5");
		sb.append("\n" + "size=\"15,7\"");
		sb.append("\n" + "nodesep=.3");
		sb.append("\n"
				+ "node [width=0.5,height=auto,shape=record,fontsize=12,fontcolor=black,style=filled,fillcolor=sandybrown];");
		sb.append("\n" + "edge [minlen=1,constraint=true,fontsize=11,labelfontsize=11];");

		sb.append("\n");
		sb.append("\n");

		//
		String edge_list = "", node_list = "";
		// cout << endl << " Nodes" << endl << " -----" << endl;
		sb.append("/* Nodes */ \n \n");
		//
		String nodeslist = print_nodes();
		sb.append(nodeslist);
		// cout << endl << " Edges" << endl << " -----" << endl;
		sb.append("\n /* Edges */ \n \n");
		// print_edges(root,edge_list);
		String edgeslist = print_edges_new();
		sb.append(edgeslist + "}");

		String result = sb.toString();

		Util.writeFile(filename, result);

		// String[] cmd = { "konsole", "-c", "dot -Tsvg scdawg.dot -o " + outputfile +
		// "_CDAWG.svg" };
		String[] cmd = { "cmd.exe", "/c", "dot -Tsvg scdawg.dot -o " + outputfile + "_CDAWG.svg" };

		System.out.println(cmd[0]);
		System.out.println(cmd[1]);
		System.out.println(cmd[2]);

		try {

			Process p = Runtime.getRuntime().exec(cmd);

			BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));

			String temp = "";

			while ((temp = input.readLine()) != null)
				System.out.println(temp);

			input.close();

		} catch (IOException e) {
		}

		long duration = System.currentTimeMillis() - startTime;
		System.out.println(" ... took " + duration + " milliseconds");

	} // print_automaton_new()

	// *******************************************************************************
	// print_edges()
	// *******************************************************************************
	public String print_edges_new() {

		String result = "";
		StringBuilder edge_sstr = new StringBuilder();

		for (int r = 0; r < all_nodes.size(); r++) {

			Node node = all_nodes.get(r);
			// if (node->ident_pointers.size()>0||node==root) {
			int label_count = 0;

			Iterator it = node.children_new.entrySet().iterator();
			while (it.hasNext()) {

				Map.Entry pair = (Map.Entry) it.next();

				int i = (int) pair.getKey();

				int node_number;
				if (node.end == -1)
					node_number = node.end + 1;
				else
					node_number = node.end;

				int node_number_end;
				if (node.children.get(i).end == -1)
					node_number_end = 0;
				else
					node_number_end = node.children.get(i).end;

				edge_sstr.append(" n_" + node.id + "_" + node_number + " -> n_" + node.children.get(i).id + "_"
						+ node_number_end + " ");

				String label = "";
				label = this.get_edge_label(i, node, node.children.get(i));

				String regex = "\\\"";

				edge_sstr.append(" [style=" + "solid" + "  label=\"" + label + "\"];" + "\n");

			} // for it node children

			it = node.children_left.entrySet().iterator();
			while (it.hasNext()) {

				Map.Entry pair = (Map.Entry) it.next();

				int i = (int) pair.getKey();

				int node_number;
				if (node.end == -1)
					node_number = node.end + 1;
				else
					node_number = node.end;

				int node_number_end;
				if (node.children_left.get(i).end == -1)
					node_number_end = 0;
				else
					node_number_end = node.children_left.get(i).end;

				edge_sstr.append(" n_" + node.id + "_" + node_number + " -> n_" + node.children_left.get(i).id + "_"
						+ node_number_end + " ");

				String label = "";
				label = this.get_edge_label_left(i, node, node.children_left.get(i));

				String regex = "\\\"";

				edge_sstr.append(" [color=blue" + "  label=\"" + label + "\"];" + "\n");

			} // for it node children_left

			Node suffixLink = node.suffixLink;

			if (suffixLink != null) {

				int node_number;
				if (node.end == -1)
					node_number = node.end + 1;
				else
					node_number = node.end;

				int node_number_end;
				if (node.suffixLink.end == -1)
					node_number_end = node.suffixLink.end + 1;
				else
					node_number_end = node.suffixLink.end;

				edge_sstr.append(" n_" + node.id + "_" + node_number + " -> n_" + node.suffixLink.id + "_"
						+ node_number_end + " ");
				edge_sstr.append(" [color=red];\n");
			}

			// edge_list+= edge_str;
			label_count++;

		} // for all nodes

		result = edge_sstr.toString();

		return result;

	} // print_edges()

}
