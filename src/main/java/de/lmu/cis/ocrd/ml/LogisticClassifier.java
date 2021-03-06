package de.lmu.cis.ocrd.ml;

import de.lmu.cis.ocrd.ml.features.BinaryPredictor;
import de.lmu.cis.ocrd.ml.features.FeatureSet;
import org.pmw.tinylog.Logger;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.Logistic;
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

	static LogisticClassifier train(Path path, boolean debug) throws Exception {
		Logger.debug("training logistic classifier from file {}", path);
		final ConverterUtils.DataSource ds =
				new ConverterUtils.DataSource(path.toString());
		final Instances train = ds.getDataSet();
		// gt is last class
		train.setClassIndex(train.numAttributes() - 1);
		final Instances structure = ds.getStructure();
		structure.setClassIndex(structure.numAttributes() - 1);
		final AbstractClassifier sl = new Logistic();
		//final AbstractClassifier sl = new SimpleLogistic();
		sl.setDebug(debug);
//		for (int i = 0; i < train.numAttributes(); i++) {
//			Logger.debug("train.attribute({}).numValues() = {}", i, train.attribute(i).numValues());
//			Logger.debug("attribute: {}", train.attribute(i).toString());
//			Logger.debug("attribute name: {}", train.attribute(i).name());
//		}
		Logger.debug("building classifier: nattr={}, nvals={}", train.numAttributes(), train.numClasses());
		sl.buildClassifier(train);
		return new LogisticClassifier(ds.getStructure(), sl);
	}

	static LogisticClassifier load(InputStream is) throws Exception {
		try (ObjectInputStream ois = new ObjectInputStream(is)) {
			final LogisticClassifier c =  (LogisticClassifier) ois.readObject();
			c.structure.setClassIndex(c.structure.numAttributes()-1);
			return c;
		}
	}

	static LogisticClassifier load(Path path) throws Exception {
		Logger.debug("loading logistic classifier from file {}", path);
		try (InputStream is = new FileInputStream(path.toFile())) {
			return load(is);
		}
	}

	private LogisticClassifier(Instances structure,
	                           AbstractClassifier classifier) {
		this.classifier = classifier;
		this.structure = structure;
	}

	String evaluate(String title, Instances instances) throws Exception {
		Logger.debug("self numAttributes: {}", this.structure.numAttributes());
		Logger.debug("toEvaluate numAttributes: {}", instances.numAttributes());
		final Evaluation evaluation = new Evaluation(structure);
		evaluation.evaluateModel(classifier, instances);
		return evaluation.toSummaryString(title, true);
	}

	public String evaluate(String title, Path path) throws Exception {
		Logger.debug("evaluating {}", path);
		final Instances instances =
				new ConverterUtils.DataSource(path.toString()).getDataSet();
		instances.setClassIndex(instances.numAttributes()-1);
		return evaluate(title, instances);
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

	@Override
	public Prediction predict(Instance instance) throws Exception {
		final double res = classifier.classifyInstance(instance);
		final double[] xy = classifier.distributionForInstance(instance);
		return new Prediction(res, xy, instance.classAttribute().value((int) res));
	}

	void save(Path path) throws Exception {
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
		Logger.debug("structure:\n{}", structure.toString());
		return setupInstance(instances.get(n), features);
	}
}
