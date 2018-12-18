package de.lmu.cis.ocrd.ml.features;

public class DecisionMakerConfidenceFeature extends NamedDoubleFeature {
	private final BinaryPredictor predictor;

	public DecisionMakerConfidenceFeature(String name,
	                                      BinaryPredictor predictor) {
		super(name);
		this.predictor = predictor;
	}

	@Override
	protected double doCalculate(OCRToken token, int i, int n) {
		final BinaryPrediction p = predictor.predict(token);
		return p.getPrediction() ?
				p.getConfidenceTrue() :
				p.getConfidenceFalse();
	}

	@Override
	public boolean handlesOCR(int i, int n) {
		return handlesOnlyMasterOCR(i, n);
	}
}
