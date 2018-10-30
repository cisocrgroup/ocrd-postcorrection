package de.lmu.cis.ocrd.ml.features;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;

import de.lmu.cis.ocrd.json.JSONUtil;
import de.lmu.cis.ocrd.ml.FreqMap;

public class UnigramFeature extends NamedDoubleFeature {
	private static final long serialVersionUID = 1507211972850104646L;
	private final List<FreqMap> unigrams;

	public UnigramFeature(JsonObject o, ArgumentFactory args) throws Exception {
		this(args, JSONUtil.mustGetNameOrType(o));
	}

	private UnigramFeature(ArgumentFactory factory, String name) throws Exception {
		super(name);
		unigrams = new ArrayList<>();
		unigrams.add(factory.getMasterOCRUnigrams());
		for (int i = 0; i < factory.getNumberOfOtherOCRs(); i++) {
			unigrams.add(factory.getOtherOCRUnigrams(i));
		}
	}

	@Override
	public boolean handlesOCR(int i, int n) {
		return handlesAnyOCR(i, n);
	}

	@Override
	public double doCalculate(OCRToken token, int i, int n) {
		// System.out.println("doCalculate(" + token + "," + i + "," + n + "): " +
		// unigrams.size());
		return unigrams.get(i).getRelative(getWord(token, i, n).toString());
	}
}
