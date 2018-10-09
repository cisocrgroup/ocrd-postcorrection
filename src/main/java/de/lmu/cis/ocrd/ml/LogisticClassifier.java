package de.lmu.cis.ocrd.ml;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.functions.SimpleLogistic;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import java.util.HashMap;
import java.util.Map;

public class LogisticClassifier implements Classifier {
	private final AbstractClassifier classifier;
	private final Instances structure;
	private final Map<Integer, Instance> instances = new HashMap<>();

	private LogisticClassifier(Instances structure) {
		//this.classifier = new Logistic();
		this.classifier = new SimpleLogistic();
		this.structure = structure;
	}

	public static LogisticClassifier train(Instances train, Instances structure) throws Exception {
		LogisticClassifier classifier = new LogisticClassifier(structure);
		classifier.classifier.buildClassifier(train);
		classifier.structure.setClassIndex(train.classIndex());
		return classifier;
	}

	private static Instance setupInstance(Instance instance, FeatureSet.Vector features) throws Exception {
		final int n = features.size() - 1; // last feature is GT
		for (int i = 0; i < n; i++) {
			final Object p = features.get(i);
			if (p instanceof Double) {
				instance.setValue(i, (double) p);
			} else if (p instanceof String) {
				instance.setValue(i, (String) p);
			} else if (p instanceof Boolean) {
				instance.setValue(i, (boolean) p ? "true" : "false");
			} else {
				throw new Exception("Invalid feature value of type: " + p.getClass());
			}
		}
		return instance;
	}

	@Override
	public Prediction predict(FeatureSet.Vector features) throws Exception {
		final Instance instance = newInstance(features);
		final double res = classifier.classifyInstance(instance);
		final double[] xy = classifier.distributionForInstance(instance);
		return new Prediction(res, xy, instance.classAttribute().value((int) res));
	}

	private Instance newInstance(FeatureSet.Vector features) throws Exception {
		final int n = features.size() - 1; // last feature is GT
		if (instances.containsKey(n)) {
			return setupInstance(instances.get(n), features);
		}
		final Instance instance = new DenseInstance(n);
		instance.setDataset(structure);
		instances.put(n, instance);
		return setupInstance(instances.get(n), features);
	}
}
