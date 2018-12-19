package de.lmu.cis.ocrd.ml.features;

public class DecisionMakerConfidenceFeature extends NamedDoubleFeature {
	private final BinaryPredictor predictor;
	private final FeatureSet fs;

	public DecisionMakerConfidenceFeature(String name,
	                                      BinaryPredictor predictor,
	                                      FeatureSet fs) {
		super(name);
		this.predictor = predictor;
		this.fs = fs;
	}

	@Override
	protected double doCalculate(OCRToken token, int i, int n) {
		try {
			final FeatureSet.Vector values = fs.calculateFeatureVector(token, n);
			final BinaryPrediction p = predictor.predict(values);
			final double confidence = p.getConfidence();
			return p.getPrediction() ? confidence : -confidence;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean handlesOCR(int i, int n) {
		return handlesOnlyMasterOCR(i, n);
	}
}
