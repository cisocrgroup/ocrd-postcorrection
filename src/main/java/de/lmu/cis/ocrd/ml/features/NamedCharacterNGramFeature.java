package de.lmu.cis.ocrd.ml.features;

import de.lmu.cis.ocrd.ml.FreqMap;

import java.util.ArrayList;
import java.util.List;

abstract class NamedCharacterNGramFeature extends NamedFeature {
	private static final long serialVersionUID = -8497838051734162573L;
	private final FreqMap ngrams;

	NamedCharacterNGramFeature(String name, FreqMap ngrams) {
		super(name);
		this.ngrams = ngrams;
	}

	@Override
	public final String getClasses() {
		return "REAL";
	}

	protected static List<String> splitIntoCharacterNGrams(String str, int n) {
		ArrayList<String> splits = new ArrayList<>();
		str = '$' + str + '$';
		final int max = str.length() - n;
		for (int i = 0; i < max; i++) {
			splits.add(str.substring(i, i + n));
		}
		return splits;
	}

	protected FreqMap getNgrams() {
		return ngrams;
	}
}
