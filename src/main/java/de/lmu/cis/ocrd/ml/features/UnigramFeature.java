package de.lmu.cis.ocrd.ml.features;

import com.google.gson.JsonObject;
import de.lmu.cis.ocrd.ml.FreqMap;
import de.lmu.cis.ocrd.ml.OCRToken;
import de.lmu.cis.ocrd.util.JSON;

public class UnigramFeature extends NamedDoubleFeature {
	private static final long serialVersionUID = 1507211972850104646L;
	private final ArgumentFactory args;

	public UnigramFeature(JsonObject o, ArgumentFactory args) {
		this(args, JSON.mustGetNameOrType(o));
	}

	UnigramFeature(ArgumentFactory factory, String name) {
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

	FreqMap getUnigrams(int i) {
		try {
			if (i == 0) {
				return this.args.getMasterOCRUnigrams();
			}
			return this.args.getSlaveOCRUnigrams(i - 1);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
