package de.lmu.cis.ocrd.ml.features;

import com.google.gson.JsonObject;
import de.lmu.cis.ocrd.align.Graph;
import de.lmu.cis.ocrd.util.JSON;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LineOverlapWithMasterOCRFeature extends NamedDoubleFeature {
	private static final long serialVersionUID = 1L;
	private final List<String> cache = new ArrayList<>();
	private final List<Double> values = new ArrayList<>();

	public LineOverlapWithMasterOCRFeature(JsonObject o,
			ArgumentFactory ignore) {
		this(JSON.mustGetNameOrType(o));
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
		return values.get(j);
	}

	private void update(OCRToken token, int i, int n) {
		while (cache.size() != n - 1) {
			cache.add(null);
		}
		while (values.size() != n -1) {
			values.add(0.0);
		}
		final String otherOCRLine = getWord(token, i, n).getLineNormalized();
		final int j = getIndex(i, n);
		if (Objects.equals(cache.get(j), otherOCRLine)) {
			return;
		}
		final String masterOCRLine = token.getMasterOCR().getLineNormalized();
		final Graph g = new Graph(otherOCRLine, masterOCRLine);
		cache.set(j, otherOCRLine);
		values.set(j, g.getStartNode().calculateOverlap());
	}
}
