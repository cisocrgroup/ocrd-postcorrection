package de.lmu.cis.ocrd.train;

import de.lmu.cis.ocrd.ml.ARFFWriter;
import de.lmu.cis.ocrd.ml.features.FeatureSet;
import de.lmu.cis.ocrd.ml.Token;
import org.pmw.tinylog.Logger;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;

public class TrainingAndTestARFFWriter implements AutoCloseable {
	private final ARFFWriter training, test;
	private final FeatureSet featureSet;
	private final Writer trainingWriter, testWriter;
	private int n;

	TrainingAndTestARFFWriter(FeatureSet featureSet, Path training, Path test) throws IOException {
		this.featureSet = featureSet;
		this.trainingWriter = new BufferedWriter(new FileWriter(training.toFile()));
		this.testWriter = new BufferedWriter(new FileWriter(test.toFile()));
		this.training = ARFFWriter.fromFeatureSet(featureSet).withWriter(trainingWriter);
		this.test = ARFFWriter.fromFeatureSet(featureSet).withWriter(testWriter);
	}

	TrainingAndTestARFFWriter withRelationName(String relationName) {
		training.withRelation(relationName + "_training");
		test.withRelation(relationName + "_test");
		return this;
	}

	TrainingAndTestARFFWriter withDebug(boolean debug) {
		training.withDebugToken(debug);
		test.withDebugToken(debug);
		return this;
	}

	void writeHeader(int n) {
		this.n = n;
		training.writeHeader(n);
		test.writeHeader(n);
	}

	void add(Token token, boolean isTrain) {
		final FeatureSet.Vector features = featureSet.calculateFeatureVector(token, n);
		Logger.debug(features);
		if (isTrain) {
			training.writeFeatureVector(features);
		} else {
			test.writeFeatureVector(features);
		}
	}

	@Override
	public void close() throws Exception {
		trainingWriter.flush();
		trainingWriter.close();
		testWriter.flush();
		testWriter.close();
	}
}
