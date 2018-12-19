package de.lmu.cis.ocrd.cli;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import de.lmu.cis.ocrd.ml.ARFFWriter;
import de.lmu.cis.ocrd.ml.LM;
import de.lmu.cis.ocrd.ml.LogisticClassifier;
import de.lmu.cis.ocrd.ml.features.*;
import de.lmu.cis.ocrd.pagexml.*;
import de.lmu.cis.ocrd.profile.Candidate;
import org.apache.commons.io.IOUtils;
import org.pmw.tinylog.Logger;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.functions.SimpleLogistic;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class TrainCommand implements Command {

	static class TrainingResource {
		String model = "", training = "", features = "";
	}

	static class DLETrainingResource extends TrainingResource {
		public String dynamicLexicon = "";
	}

	static class Parameter {
		DLETrainingResource dleTraining;
		TrainingResource rrTraining, dmTraining;
		String trigrams = "";
		int nOCR;
	}

	private String[] ifgs; // input file groups
	private METS mets; // mets file
	private Parameter parameter;
	private LM lm;
	private FeatureSet dleFS, rrFS, dmFS;
	private ARFFWriter dlew, rrw, dmw;
	private boolean debug;

	@Override
	public String getName() {
		return "train";
	}

	@Override
	public void execute(CommandLineArguments config) throws Exception {
		this.ifgs = config.mustGetInputFileGrp();
		this.mets = METS.open(Paths.get(config.mustGetMETSFile()));
		this.parameter = config.mustGetParameter(Parameter.class);
		this.debug = "DEBUG".equals(config.getLogLevel());
		this.lm = new LM(true, Paths.get(parameter.trigrams));
		this.dleFS = FeatureFactory
				.getDefault()
				.withArgumentFactory(lm)
				.createFeatureSet(getFeatures(parameter.dleTraining.features))
				.add(new DynamicLexiconGTFeature());
		this.rrFS = FeatureFactory
				.getDefault()
				.withArgumentFactory(lm)
				.createFeatureSet(getFeatures(parameter.rrTraining.features))
				.add(new ReRankingGTFeature());
		// DM needs to be created separately (see below)
		for (int i = 0; i < parameter.nOCR; i++) {
			// DLE
			final Path dleTrain = tagPath(parameter.dleTraining.training,
					i+1);
			final Path dleModel = tagPath(parameter.dleTraining.model, i+1);
			dlew = ARFFWriter
					.fromFeatureSet(dleFS)
					.withWriter(getWriter(dleTrain))
					.withDebugToken(debug)
					.withRelation("dle-train-" + (i+1))
					.writeHeader(i+1);
			// RR
			final Path rrTrain = tagPath(parameter.rrTraining.training, i+1);
			final Path rrModel = tagPath(parameter.rrTraining.model, i+1);
			rrw = ARFFWriter
					.fromFeatureSet(rrFS)
					.withWriter(getWriter(rrTrain))
					.withDebugToken(debug)
					.withRelation("rr-train-" + (i+1))
					.writeHeader(i+1);
			// DM
			dmFS = FeatureFactory
					.getDefault()
					.withArgumentFactory(lm)
					.createFeatureSet(getFeatures(parameter.dmTraining.features))
					.add(getDMConfidenceFeature(rrModel, rrTrain, rrFS))
					.add(new DecisionMakerGTFeature());
			final Path dmTrain = tagPath(parameter.dmTraining.training, i+1);
			final Path dmModel = tagPath(parameter.dmTraining.model, i+1);
			dmw = ARFFWriter
					.fromFeatureSet(dmFS)
					.withWriter(getWriter(rrTrain))
					.withDebugToken(debug)
					.withRelation("dm-train-" + (i+1))
					.writeHeader(i+1);
			for (String ifg : ifgs) {
				Logger.info("input file group: {}", ifg);
				final List<METS.File> files = mets.findFileGrpFiles(ifg);
				prepare(files, i, parameter.nOCR);
			}
			// Train models
			dlew.close();
			rrw.close();
			dmw.close();
			train(dleTrain, dleModel);
			train(rrTrain, rrModel);
			train(dmTrain, dmModel);
		}
	}

	private void prepare(List<METS.File> files, int i, int n) throws Exception {
		Logger.info("prepare({}, {})", i, n);
		lm.setFiles(files);
		prepareDLE(files, i, n);
		prepareRR(files, i, n);
		prepareDM(files, i, n);
	}

	private void prepareDLE(List<METS.File> files, int i, int n) throws Exception {
		Logger.info("prepareDLE({}, {})", i, n);
		for (METS.File file : files) {
			prepareDLE(Page.parse(file.open()), i, n);
		}
	}

	private void prepareDLE(Page page, int i, int n) throws Exception {
		eachLongWord(page, (word, mOCR)-> {
			final OCRToken t = new OCRTokenImpl(word, n);
			Logger.debug("prepareDLE: adding {} (GT: {})",
					t.getMasterOCR().toString(),
					t.getGT().get());
			final FeatureSet.Vector values = dleFS.calculateFeatureVector(t, i+1);
			Logger.debug(values);
			dlew.writeFeatureVector(values);
		});
	}

	private void prepareRR(List<METS.File> files, int i, int n) throws Exception {
		Logger.info("prepareRR({}, {})", i, n);
		for (METS.File file : files) {
			prepareRR(Page.parse(file.open()), i, n);
		}
	}

	private void prepareRR(Page page, int i, int n) throws Exception {
		eachLongWord(page, (word, mOCR)->{
			OCRTokenImpl t = new OCRTokenImpl(word, n);
			for (Candidate c : t.getAllProfilerCandidates()) {
				OCRTokenWithCandidateImpl tc =
						new OCRTokenWithCandidateImpl(word, n, c);
				Logger.debug("prepareRR: adding {} (Candidate: {}, GT: {})",
						tc.getMasterOCR().toString(),
						c.Suggestion,
						tc.getGT().toString());
				final FeatureSet.Vector values =
						rrFS.calculateFeatureVector(tc, i+1);
				Logger.debug(values);
				rrw.writeFeatureVector(values);
			}
		});
	}

	private void prepareDM(List<METS.File> files, int i, int n) throws Exception {
		Logger.info("prepareDM({}, {})", i, n);
		for (METS.File file : files) {
			prepareDM(Page.parse(file.open()), i, n);
		}
	}

	private void prepareDM(Page page, int i, int n) throws Exception {
		eachLongWord(page, (word, mOCR)->{
			OCRTokenImpl t = new OCRTokenImpl(word, n);
			for (Candidate c : t.getAllProfilerCandidates()) {
				OCRTokenWithCandidateImpl tc =
						new OCRTokenWithCandidateImpl(word, n, c);
				Logger.debug("prepareDM: adding {} (Candidate: {}, GT: {})",
						tc.getMasterOCR().toString(),
						c.Suggestion,
						tc.getGT().toString());
				final FeatureSet.Vector values =
						dmFS.calculateFeatureVector(tc, i+1);
				Logger.debug(values);
				dmw.writeFeatureVector(values);
			}
		});
	}

	interface WordOperation {
		void apply(Word word, String mOCR) throws Exception;
	}

	private static void eachLongWord(Page page, WordOperation f) throws Exception {
		for (Line line : page.getLines()) {
			for (Word word : line.getWords()) {
				String mOCR = word.getUnicodeNormalized().get(0);
				if (mOCR.length() > 3) {
					f.apply(word, mOCR);
				}
			}
		}
	}

	private static Path tagPath(String path, int n) {
		return Paths.get(path.replaceFirst("(\\..*?)$", "_" + n + "$1"));
	}

	private static void train(Path src, Path dest) throws Exception {
		Logger.debug("training {} from {}", dest.toString(), src.toString());
		final ConverterUtils.DataSource ds = new ConverterUtils.DataSource(
				src.toString());
		final Instances train = ds.getDataSet();
		// gt is last class
		train.setClassIndex(train.numAttributes() - 1);
		final Instances structure = ds.getStructure();
		structure.setClassIndex(structure.numAttributes() - 1);
		final AbstractClassifier sl = new SimpleLogistic();
		sl.buildClassifier(train);
		try (ObjectOutputStream out = new ObjectOutputStream(
				new FileOutputStream(dest.toFile()))) {
			out.writeObject(sl);
			out.flush();
		}
	}

	private static Feature getDMConfidenceFeature(Path model,
	                                              Path dataSet,
	                                              FeatureSet fs) throws Exception {
		final ConverterUtils.DataSource ds =
				new ConverterUtils.DataSource(dataSet.toString());
		AbstractClassifier c;
		try (ObjectInputStream ois =
				     new ObjectInputStream(new FileInputStream(model.toFile()))) {
			c = (AbstractClassifier) ois.readObject();
		}
		BinaryPredictor p = new LogisticClassifier(ds.getStructure(), c);
		return new DecisionMakerConfidenceFeature("rr-confidence", p, fs);
	}

	private static JsonObject[] getFeatures(String features) throws Exception {
		final Path path = Paths.get(features);
		JsonObject[] os;
		try (InputStream is = new FileInputStream(path.toFile())) {
			final String json = IOUtils.toString(is, Charset.forName("UTF-8"));
			os = new Gson().fromJson(json, JsonObject[].class);
		}
		return os;
	}

	private static Writer getWriter(Path path) throws Exception {
		return new BufferedWriter(new FileWriter(path.toFile()));
	}
}
