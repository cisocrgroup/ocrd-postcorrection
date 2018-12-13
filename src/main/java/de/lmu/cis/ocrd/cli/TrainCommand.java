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
		DLETrainingResource dleTraining;
		String trigrams = "";
	}

	private String[] ifgs; // input file groups
	private METS mets; // mets file
	private Parameter parameter;
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

		prepareDLE();
	}

	private void prepareDLE() throws Exception {
		Logger.info("prepareDLE");
		final JsonObject[] features =
				getFeatures(Paths.get(parameter.dleTraining.features));
		for (String ifg : ifgs) {
			prepareDLE(ifg, features);
		}
	}

	private void prepareDLE(String ifg, JsonObject[] features) throws Exception {
		Logger.info("prepareDLE({})", ifg);
		final List<METS.File> files = mets.findFileGrpFiles(ifg);
		final LM lm = new LM(true, Paths.get(parameter.trigrams), files);
		final FeatureSet fs = FeatureFactory.getDefault()
				.withArgumentFactory(lm)
				.createFeatureSet(features)
				.add(new DynamicLexiconGTFeature());
		for (int i = 0; i < lm.getNumberOfOtherOCRs(); i++) {
			prepareDLE(files, fs, i, lm.getNumberOfOtherOCRs());
			trainDLE(i, lm.getNumberOfOtherOCRs() + 1);
		}
	}

	private void prepareDLE(List<METS.File> files, FeatureSet fs, int i,
	                        int n) throws Exception {
		Logger.info("prepareDLE({}, {})", i, n);
		final Path dest = tagPath(parameter.dleTraining.training, i);
		try (final ARFFWriter w =
				     ARFFWriter.fromFeatureSet(fs).withWriter(
				     		new BufferedWriter(new FileWriter(dest.toFile())))) {
			w.withDebugToken(debug);
			w.withRelation("dle-train-" + (i+1));
			w.writeHeader(i+1);
			for (METS.File file : files) {
				prepareDLE(w, fs, Page.parse(file.open()), i, n);
			}
		}
	}

	private void prepareDLE(ARFFWriter w, FeatureSet fs, Page page, int i,
	                        int n) throws Exception {
		eachLongWord(page, (word, mOCR)-> {
			final OCRToken t = new OCRTokenImpl(word, n);
			Logger.debug("adding {} | GT: {}", t.getMasterOCR().toString(),
					t.getGT().get());
			final FeatureSet.Vector vals = fs.calculateFeatureVector(t, i+1);
			Logger.debug(vals);
			w.writeFeatureVector(vals);
		});
	}

	private void trainDLE(int i, int n) throws Exception {
		Logger.info("trainDLE({}, {})", i, n);
		final Path src = tagPath(parameter.dleTraining.training, i);
		final Path dest = tagPath(parameter.dleTraining.model, i);
		train(src, dest);
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

	private static JsonObject[] getFeatures(Path features) throws Exception {
		JsonObject[] os;
		try (InputStream is = new FileInputStream(features.toFile())) {
			final String json = IOUtils.toString(is, Charset.forName("UTF-8"));
			os = new Gson().fromJson(json, JsonObject[].class);
		}
		return os;
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
}
