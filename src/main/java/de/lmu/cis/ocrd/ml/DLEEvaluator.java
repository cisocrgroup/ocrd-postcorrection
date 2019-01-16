package de.lmu.cis.ocrd.ml;

import weka.core.Instance;
import weka.core.Instances;

import java.io.Writer;

public class DLEEvaluator {
	private final Instances instances;
	private final LogisticClassifier classifier;
	private final Writer writer;
	private final int i;

	private int good, bad, missed, total;

	public DLEEvaluator(Writer w, LogisticClassifier c, Instances is, int i) {
		this.writer = w;
		this.classifier = c;
		this.instances = is;
		this.i = i;
		this.good = 0;
		this.bad = 0;
		this.missed = 0;
		this.total = 0;
	}

	public void evaluate() throws Exception {
		for (Instance instance : instances) {
			evaluate(instance);
		}
		writer.write(String.format("total number of tokens: %d", total));
		writer.write(String.format("number of good extensions: %d\n", good));
		writer.write(String.format("number of bad extensions: %d\n", bad));
		writer.write(String.format("number of missed opportunities: %d\n",
				missed));
		final String title = String.format(
				"===================\nResults (%d):\n", i+1);
		final String data = classifier.evaluate(title, instances);
		writer.write(data);
	}

	private void evaluate(Instance instance) throws Exception {
		final Prediction p = classifier.predict(instance);
		final int gt = (int) instance.classValue();
		final int got = (int) p.getValue();

		total++;
		// 0 is true :(
		if (gt == 0) {
			if (got == 0) {
				good++;
			} else {
				missed++;
			}
		} else {
			if (got == 0) {
				bad++;
			}
		}
	}
}
