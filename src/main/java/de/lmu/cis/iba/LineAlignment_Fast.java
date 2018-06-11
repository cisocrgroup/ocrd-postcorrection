package de.lmu.cis.iba;

import de.lmu.cis.ocrd.Document;
import de.lmu.cis.ocrd.OCRLine;
import org.pmw.tinylog.Logger;

import java.util.*;


public class LineAlignment_Fast extends ArrayList<ArrayList<OCRLine>> {
	private static class pair {
		public HashSet<Integer> ids;
		public Node node;
	}

	public LineAlignment_Fast(Document doc, int nlines) throws Exception {
		super();

		if (nlines <= 0) {
			throw new Exception("cannot allign " + nlines + " lines");
		}

		ArrayList<String> stringset = new ArrayList<>();
		ArrayList<OCRLine> ocrlines = new ArrayList<>();

		doc.eachLine(l -> {
			stringset.add("#" + l.line.getNormalized() + "$");
			ocrlines.add(l);
		});

		Online_CDAWG_sym scdawg = new Online_CDAWG_sym(stringset, false);
		scdawg.determineAlphabet(false);
		scdawg.build_cdawg();
		// scdawg.print_automaton("svgs/scdawg");

		Common_SCDAWG_Functions scdawg_functions = new Common_SCDAWG_Functions(scdawg);

		HashMap<Node, HashSet<Integer>> nodes_with_n_occs = scdawg_functions.get_n_string_occurences(nlines, ocrlines);

		HashMap<Node,HashSet<Integer>> nodes_sorted = Util.sortByNodeLength(nodes_with_n_occs, "DESC",scdawg);
		ArrayList<pair> nodes_sink_set = new ArrayList<>();

		Iterator it3 = nodes_sorted.entrySet().iterator();

		Logger.debug("starting main loop ...");
		HashSet<Integer> usedIDs = new HashSet<>();
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
			HashSet<String> ocrEngines = new HashSet<>();
			for (Integer id : ids) {
				ocrEngines.add(ocrlines.get(id).ocrEngine);
			}
			if (ocrEngines.size() != nlines) {
				continue main_loop;
			}
			usedIDs.addAll(ids);
			pair p = new pair();
			p.ids = ids;
			p.node = n;
			nodes_sink_set.add(p);
		}
		Logger.debug("done with main loop");
		Logger.debug("starting sink loop");
		// handle final nodes (special case if all ocrs are identical)
		sinkloop: for (Node sink : scdawg.sinks) {
			if (sink.stringnumbers.size() == nlines) {
				// it is impossilbe (?) that this node was used before
				// Logger.debug("got sink with " + N + " sinks");
				// Logger.debug(sink.stringnumbers);

				// Special case if identical strings had an smaller quasi max node as their sink

				for (pair pn : nodes_sink_set) {
					HashSet<Integer> ids = pn.ids;
					HashSet<Integer> sink_ids = new HashSet<>();

					sink_ids.addAll(sink.stringnumbers);

					if (sink_ids.equals(ids)) {
						continue sinkloop;
					}
				}

				// end special case

				pair p = new pair();
				p.ids = new HashSet<>();
				p.ids.addAll(sink.stringnumbers);
				p.node = scdawg.root;
				nodes_sink_set.add(p);
			}
		}
		Logger.debug("done with sink loop");

		for (pair p : nodes_sink_set) {
			Logger.debug(scdawg.get_node_label(p.node));
			Logger.debug(p.ids);
			ArrayList<OCRLine> linetupel = new ArrayList<>();
			for (Integer id : p.ids) {
				int idx = id;

				linetupel.add(ocrlines.get(idx));

				Logger.debug("- " + stringset.get(idx));
			}
			this.add(linetupel);
		}
	}

}
