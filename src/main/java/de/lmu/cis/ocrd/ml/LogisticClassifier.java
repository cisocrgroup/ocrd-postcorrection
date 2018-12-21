package de.lmu.cis.ocrd.ml;

import de.lmu.cis.ocrd.ml.features.BinaryPredictor;
import de.lmu.cis.ocrd.ml.features.FeatureSet;
import org.pmw.tinylog.Logger;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.SimpleLogistic;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.io.*;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LogisticClassifier implements Classifier, BinaryPredictor, Serializable {
	private static final long serialVersionUID = -5403801469028720384L;
	private final AbstractClassifier classifier;
	private final Instances structure;
	private final Map<Integer, Instance> instances = new HashMap<>();

	public static LogisticClassifier train(Path path) throws Exception {
		Logger.debug("training logistic classifier from file {}", path);
		final ConverterUtils.DataSource ds =
				new ConverterUtils.DataSource(path.toString());
		final Instances train = ds.getDataSet();
		// gt is last class
		train.setClassIndex(train.numAttributes() - 1);
		final Instances structure = ds.getStructure();
		structure.setClassIndex(structure.numAttributes() - 1);
		final AbstractClassifier sl = new SimpleLogistic();
		sl.buildClassifier(train);
		return new LogisticClassifier(ds.getStructure(), sl);
	}

	public static LogisticClassifier load(Path path) throws Exception {
		Logger.debug("loading logistic classifier from file {}", path);
		try (ObjectInputStream ois =
				     new ObjectInputStream(new FileInputStream(path.toFile()))) {
			return (LogisticClassifier) ois.readObject();
		}
	}

	private LogisticClassifier(Instances structure,
	                           AbstractClassifier classifier) {
		this.classifier = classifier;
		this.structure = structure;
	}

	public String evaluate(String title, Path path) throws Exception {
		Logger.debug("evaluating {}", path);
		final ConverterUtils.DataSource ds =
				new ConverterUtils.DataSource(path.toString());
		final Instances toEvaluate = ds.getDataSet();
		final Evaluation evaluation = new Evaluation(structure);
		evaluation.evaluateModel(classifier, toEvaluate);
		return evaluation.toSummaryString(title, true);
	}

	@Override
	public Prediction classify(FeatureSet.Vector features) throws Exception {
		return predict(features);
	}

	@Override
	public Prediction predict(List<Object> features) throws Exception {
		final Instance instance = newInstance(features);
		return predict(instance);
	}

	public Prediction predict(Instance instance) throws Exception {
		final double res = classifier.classifyInstance(instance);
		final double[] xy = classifier.distributionForInstance(instance);
		return new Prediction(res, xy,
				instance.classAttribute().value((int) res));
	}

	public void save(Path path) throws Exception {
		Logger.debug("saving logistic classifier to {}", path);
		try (ObjectOutputStream oot =
				     new ObjectOutputStream(new FileOutputStream(path.toFile()))) {
			oot.writeObject(this);
			oot.flush();
			// close is redundant
		}
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
