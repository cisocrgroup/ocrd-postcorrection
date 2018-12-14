package de.lmu.cis.ocrd.cli;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import de.lmu.cis.ocrd.ml.ARFFWriter;
import de.lmu.cis.ocrd.ml.FeatureSet;
import de.lmu.cis.ocrd.ml.LM;
import de.lmu.cis.ocrd.ml.features.DynamicLexiconGTFeature;
import de.lmu.cis.ocrd.ml.features.FeatureFactory;
import de.lmu.cis.ocrd.ml.features.OCRToken;
import de.lmu.cis.ocrd.pagexml.*;
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

	public static class TrainingResource {
		public String model = "", training = "", features = "";
	}

	public static class DLETrainingResource extends TrainingResource {
		public String dynamicLexicon = "";
	}

	public static class Parameter {
		public DLETrainingResource dleTraining;
		public TrainingResource rrTraining;
		public String trigrams = "";
		public int nOCR;
	}

	private String[] ifgs; // input file groups
	private METS mets; // mets file
	private Parameter parameter;
	private LM lm;
	private FeatureSet dleFS, rrFS;
	private ARFFWriter dlew, rrw;
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

		for (int i = 0; i < parameter.nOCR; i++) {
			final Path dleTrain = tagPath(parameter.dleTraining.training,
					i+1);
			final Path dleModel = tagPath(parameter.dleTraining.model, i+1);
			dlew = ARFFWriter
					.fromFeatureSet(dleFS)
					.withWriter(getWriter(dleTrain))
					.withDebugToken(debug)
					.withRelation("dle-train-" + (i+1))
					.writeHeader(i+1);
			for (String ifg : ifgs) {
				Logger.info("input file group: {}", ifg);
				final List<METS.File> files = mets.findFileGrpFiles(ifg);
				prepare(files, i, parameter.nOCR);
			}
			dlew.close();
			train(dleTrain, dleModel);
		}
	}

	private void prepare(List<METS.File> files, int i, int n) throws Exception {
		Logger.info("prepare({}, {})", i, n);
		lm.setFiles(files);
		prepareDLE(files, i, n);
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
			Logger.debug("adding {} (GT: {})", t.getMasterOCR().toString(),
					t.getGT().get());
			final FeatureSet.Vector vals = dleFS.calculateFeatureVector(t, i+1);
			Logger.debug(vals);
			dlew.writeFeatureVector(vals);
		});
	}

	private void prepareRR() throws Exception {

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
