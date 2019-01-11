package de.lmu.cis.ocrd.ml.features;

import java.util.List;

public class DecisionMakerConfidenceFeature extends NamedDoubleFeature {
	private final List<Double> confidences;
	private final int i;

	public DecisionMakerConfidenceFeature(String name, List<Double> cs, int i) {
		super(String.format("%s_%d", name, i));
		confidences = cs;
		this.i = i;
	}

	@Override
	protected double doCalculate(OCRToken token, int i, int n) {
		return confidences.get(this.i);
	}

	@Override
	public boolean handlesOCR(int i, int n) {
		return handlesOnlyMasterOCR(i, n);
	}
}
