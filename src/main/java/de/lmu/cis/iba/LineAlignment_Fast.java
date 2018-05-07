package de.lmu.cis.iba;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.pmw.tinylog.Logger;

import de.lmu.cis.ocrd.Document;
import de.lmu.cis.ocrd.OCRLine;


public class LineAlignment_Fast extends ArrayList<ArrayList<OCRLine>> {
	private static class pair {
		public HashSet<Integer> ids;
		public Node node;
	}

	static ArrayList<Node> sinks = new ArrayList<Node>();
	public ArrayList<String> stringset = new ArrayList<String>();

	public LineAlignment_Fast(Document doc, int nlines) throws Exception {
		super();

		if (nlines <= 0) {
			throw new Exception("cannot allign " + nlines + " lines");
		}

		ArrayList<String> stringset = new ArrayList<String>();
		ArrayList<OCRLine> ocrlines = new ArrayList<OCRLine>();

		doc.eachLine(new Document.Visitor() {
			@Override
			public void visit(OCRLine l) throws Exception {
				stringset.add("#" + l.line.getNormalized() + "$");
				ocrlines.add(l);
			}
		});

		Online_CDAWG_sym scdawg = new Online_CDAWG_sym(stringset, false);
		scdawg.determineAlphabet(false);
		scdawg.build_cdawg();
		// scdawg.print_automaton("svgs/scdawg");

		Common_SCDAWG_Functions scdawg_functions = new Common_SCDAWG_Functions(scdawg);

		HashMap<Node,HashSet<Integer>> nodes_with_n_occs = scdawg_functions.get_n_string_occurences(3);

		HashMap<Node,HashSet<Integer>> nodes_sorted = Util.sortByNodeLength(nodes_with_n_occs, "DESC",scdawg);
		ArrayList<pair> nodes_sink_set = new ArrayList<pair>();

		Iterator it3 = nodes_sorted.entrySet().iterator();

		Logger.info("starting main loop ...");
		HashSet<Integer> usedIDs = new HashSet<Integer>();
		main_loop: while (it3.hasNext()) {
			Map.Entry pair = (Map.Entry) it3.next();

			Node n = (Node) pair.getKey();

			HashSet<Integer> ids = (HashSet<Integer>) pair.getValue();

			if (ids.size() != nlines) {
				continue;
			}
			for (Integer id : ids) {
				if (usedIDs.contains(id)) {
					continue main_loop;
				}
			}
			for (Integer id : ids) {
				usedIDs.add(id);
			}
			pair p = new pair();
			p.ids = ids;
			p.node = n;
			nodes_sink_set.add(p);
		}
		Logger.info("done with main loop");
		Logger.info("starting sink loop");
		// handle final nodes (special case if all ocrs are identical)
		sinkloop: for (Node sink : scdawg.sinks) {
			if (sink.stringnumbers.size() == nlines) {
				// it is impossilbe (?) that this node was used before
				// System.out.println("got sink with " + N + " sinks");
				// System.out.println(sink.stringnumbers);

				// Special case if identical strings had an smaller quasi max node as their sink

				for (pair pn : nodes_sink_set) {
					HashSet<Integer> ids = pn.ids;
					HashSet<Integer> sink_ids = new HashSet<Integer>();

					for (Integer i : sink.stringnumbers) {
						sink_ids.add(i);
					}

					if (sink_ids.equals(ids)) {
						continue sinkloop;
					}
				}

				// end special case

				pair p = new pair();
				p.ids = new HashSet<Integer>();
				for (Integer id : sink.stringnumbers) {
					p.ids.add(id);
				}
				p.node = scdawg.root;
				nodes_sink_set.add(p);
			}
		}
		Logger.info("done with sink loop");

		// ArrayList<String> xyz = new ArrayList<String>(stringset.size());
		String[] xyz = new String[stringset.size()];
		for (pair p : nodes_sink_set) {
			// System.out.println(scdawg.get_node_label(p.node));
			// System.out.println(p.ids);
			ArrayList<OCRLine> linetupel = new ArrayList<OCRLine>();
			for (Integer id : p.ids) {
				int idx = id;

				linetupel.add(ocrlines.get(idx));

				// System.out.println("- " + stringset.get(idx) + ": " +
				// strids.get(idx));
				xyz[idx] = stringset.get(idx);
			}
			this.add(linetupel);
			// System.out.println();
		}
	}

}
