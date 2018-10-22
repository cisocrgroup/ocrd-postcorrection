package de.lmu.cis.ocrd.ml.features;

import java.util.ArrayList;

import com.google.gson.JsonObject;

import de.lmu.cis.ocrd.SimpleLine;
import de.lmu.cis.ocrd.align.Graph;
import de.lmu.cis.ocrd.align.Node;
import de.lmu.cis.ocrd.json.JSONUtil;

public class LineOverlapWithMasterOCRFeature extends NamedDoubleFeature {
	private final ArrayList<SimpleLine> cache = new ArrayList<>();
	private final ArrayList<Node> nodes = new ArrayList<>();

	public LineOverlapWithMasterOCRFeature(JsonObject o, ArgumentFactory ignore) {
		this(JSONUtil.mustGetNameOrType(o));
	}

	public LineOverlapWithMasterOCRFeature(String name) {
		super(name);
	}

	private static int getIndex(int i, int n) {
		assert (i > 0);
		assert (i < n);
		return i - 1;
	}

	@Override
	public boolean handlesOCR(int i, int n) {
		return handlesEveryOtherOCR(i, n);
	}

	@Override
	public double doCalculate(OCRToken token, int i, int n) {
		assert (this.handlesOCR(i, n));
		update(token, i, n);
		final int j = getIndex(i, n);
		return nodes.get(j).calculateOverlap();
	}

	private void update(OCRToken token, int i, int n) {
		while (cache.size() != n - 2) {
			cache.add(null);
		}
		while (nodes.size() != n - 2) {
			nodes.add(null);
		}
		final SimpleLine otherOCRLine = getWord(token, i, n).getLine();
		final int j = getIndex(i, n);
		if (cache.get(j) == otherOCRLine) {
			return;
		}
		final SimpleLine masterOCRLine = token.getMasterOCR().getLine();
		final Graph g = new Graph(otherOCRLine.getNormalized(), masterOCRLine.getNormalized());
		nodes.set(j, g.getStartNode());
		cache.set(j, otherOCRLine);
	}
}
