package de.lmu.cis.ocrd.ml;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.functions.Logistic;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class LogisticClassifier implements Classifier {
	private final AbstractClassifier classifier;
	private final Instances structure;

	private LogisticClassifier(Instances structure) {
		this.classifier = new Logistic();
		this.structure = structure;
	}

	public static LogisticClassifier train(Instances train, Instances structure) throws Exception {
		LogisticClassifier classifier = new LogisticClassifier(structure);
		classifier.classifier.buildClassifier(train);
		classifier.structure.setClassIndex(train.classIndex());
		return classifier;
	}

	@Override
	public Prediction predict(FeatureSet.Vector features) throws Exception {
		final int n = features.size() - 1; // last feature is GT
		final Instance instance = new DenseInstance(n);
		instance.setDataset(structure);
		for (int i = 0; i < n; i++) {
			instance.setValue(i, features.get(i).toString());
		}
		final double res = classifier.classifyInstance(instance);
		final double[] xy = classifier.distributionForInstance(instance);
		return new Prediction(res, xy, instance.classAttribute().value((int) res));
	}
}
