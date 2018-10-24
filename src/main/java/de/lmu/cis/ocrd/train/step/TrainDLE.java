package de.lmu.cis.ocrd.train.step;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.pmw.tinylog.Logger;

import de.lmu.cis.ocrd.ml.ARFFWriter;
import de.lmu.cis.ocrd.ml.FeatureSet;
import de.lmu.cis.ocrd.ml.features.DynamicLexiconGTFeature;
import de.lmu.cis.ocrd.ml.features.FeatureFactory;
import de.lmu.cis.ocrd.ml.features.OCRToken;
import de.lmu.cis.ocrd.pagexml.Line;
import de.lmu.cis.ocrd.pagexml.OCRTokenImpl;
import de.lmu.cis.ocrd.pagexml.Page;
import de.lmu.cis.ocrd.pagexml.Word;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.functions.SimpleLogistic;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

// step: train dynamic lexicon extension.
public class TrainDLE extends Base {
	public TrainDLE(String[] args) throws Exception {
		this(args[0], args[1], args[2], args[3], args[4],
				Arrays.stream(args).skip(5).collect(Collectors.toList()));
	}

	public TrainDLE(String logLevel, String features, String profile,
			String trigrams, String dir, List<String> files)
			throws IOException {
		super(true, logLevel, profile, trigrams, dir, files);
		FileUtils.copyFile(Paths.get(features).toFile(),
				getFeatures().toFile());
	}

	public void run() throws Exception {
		prepare();
		train();
	}

	//
	// Prepare the different arff files for the actual model training.
	//
	private void prepare() throws Exception {
		final FeatureSet fs = FeatureFactory.getDefault()
				.withArgumentFactory(getLM())
				.createFeatureSet(getFeatures(this.getFeatures()))
				.add(new DynamicLexiconGTFeature());
		for (int i = 0; i < getLM().getNumberOfOtherOCRs() + 1; i++) {
			prepare(fs, i);
		}
	}

	private void prepare(FeatureSet fs, int i) throws Exception {
		Path tfile = getTrain(i + 1);
		Logger.info("preparing for {} OCR(s) to {}", i, tfile.toString());
		try (ARFFWriter w = ARFFWriter.fromFeatureSet(fs).withWriter(
				new BufferedWriter(new FileWriter(tfile.toFile())))) {
			w.withDebugToken(true);
			w.withRelation("dle-train-" + (i + 1));
			w.writeHeader(i+1);
			for (Path file : getFiles()) {
				Page page = Page.open(file);
				prepare(w, fs, i, page);
			}
		}
	}

	private void prepare(ARFFWriter w, FeatureSet fs, int i, Page page) {
		for (Line line : page.getLines()) {
			for (Word word : line.getWords()) {
				final OCRToken t = new OCRTokenImpl(word, true);
				Logger.debug("word({}): {} GT: {}", i + 1,
						word.getUnicodeNormalized().get(i), t.getGT().get());
				final FeatureSet.Vector values = fs.calculateFeatureVector(t,
						i + 1);
				Logger.debug(values);
				w.writeFeatureVector(values);
			}
		}
	}

	//
	// Train the actual models.
	//
	private void train() throws Exception {
		for (int i = 0; i < getLM().getNumberOfOtherOCRs() + 1; i++) {
			train(i);
		}
	}

	private void train(int i) throws Exception {
		final Path mfile = getModel(i + 1);
		final Path tfile = getTrain(i + 1);
		Logger.info("training for {} OCR(s) from {} to {}", i, tfile.toString(),
				mfile.toString());
		final ConverterUtils.DataSource ds = new ConverterUtils.DataSource(
				tfile.toString());
		final Instances train = ds.getDataSet();
		train.setClassIndex(train.numAttributes() - 1);
		final Instances structure = ds.getStructure();
		structure.setClassIndex(structure.numAttributes() - 1);
		final AbstractClassifier sl = new SimpleLogistic();
		sl.buildClassifier(train);
		try (ObjectOutputStream out = new ObjectOutputStream(
				new FileOutputStream(mfile.toFile()))) {
			out.writeObject(sl);
			out.flush();
		}
	}

	public Path getTrain(int n) {
		return Paths.get(getDir().toString(), "dle-train-" + n + ".arff");
	}

	public Path getModel(int n) {
		return Paths.get(getDir().toString(), "dle-model-" + n + ".ser");
	}

	public Path getFeatures() {
		return Paths.get(getDir().toString(), "dle-features.json");
	}

	public static void main(String[] args) throws Exception {
		if (args.length < 5) {
			throw new Exception(
					"Usage: logLevel features profile trigrams dir files...");
		}
		new TrainDLE(args).run();
	}
}
