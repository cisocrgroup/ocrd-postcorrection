package de.lmu.cis.ocrd.ml;

import de.lmu.cis.ocrd.ml.features.BinaryPredictor;
import de.lmu.cis.ocrd.ml.features.FeatureSet;
import weka.classifiers.AbstractClassifier;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LogisticClassifier implements Classifier, BinaryPredictor {
	private static final long serialVersionUID = -5403801469028720384L;
	private final AbstractClassifier classifier;
	private final Instances structure;
	private final Map<Integer, Instance> instances = new HashMap<>();

	public LogisticClassifier(
			Instances structure,
			AbstractClassifier classifier) {
		//this.classifier = new Logistic();
		this.classifier = classifier;
		this.structure = structure;
	}

	@Override
	public Prediction classify(FeatureSet.Vector features) throws Exception {
		return predict(features);
	}

	@Override
	public Prediction predict(List<Object> features) throws Exception {
		final Instance instance = newInstance(features);
		final double res = classifier.classifyInstance(instance);
		final double[] xy = classifier.distributionForInstance(instance);
		return new Prediction(res, xy, instance.classAttribute().value((int) res));
	}

	private static Instance setupInstance(Instance instance,
	                                      List<Object> features) throws Exception {
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

	private Instance newInstance(List<Object> features) throws Exception {
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
