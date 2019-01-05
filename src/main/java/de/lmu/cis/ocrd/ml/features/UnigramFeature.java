package de.lmu.cis.ocrd.ml.features;

import com.google.gson.JsonObject;
import de.lmu.cis.ocrd.ml.FreqMap;
import de.lmu.cis.ocrd.util.JSON;

public class UnigramFeature extends NamedDoubleFeature {
	private static final long serialVersionUID = 1507211972850104646L;
	private final ArgumentFactory args;

	public UnigramFeature(JsonObject o, ArgumentFactory args) throws Exception {
		this(args, JSON.mustGetNameOrType(o));
	}

	protected UnigramFeature(ArgumentFactory factory, String name) throws Exception {
		super(name);
		this.args = factory;
	}

	@Override
	public boolean handlesOCR(int i, int n) {
		return handlesAnyOCR(i, n);
	}

	@Override
	public double doCalculate(OCRToken token, int i, int n) {
		// System.out.println("doCalculate(" + token + "," + i + "," + n + "): " +
		// unigrams.size());
		return getUnigrams(i).getRelative(getWord(token, i, n).toString());
	}

	protected FreqMap getUnigrams(int i) {
		try {
			if (i == 0) {
				return this.args.getMasterOCRUnigrams();
			}
			return this.args.getOtherOCRUnigrams(i - 1);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
