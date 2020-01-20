package de.lmu.cis.ocrd.cli;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import de.lmu.cis.ocrd.ml.OCRToken;
import de.lmu.cis.ocrd.ml.features.*;
import de.lmu.cis.ocrd.pagexml.*;
import de.lmu.cis.ocrd.profile.*;
import org.apache.commons.io.IOUtils;
import org.pmw.tinylog.Logger;
import weka.core.Instance;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public abstract class AbstractMLCommand extends AbstractIOCommand {
	public static class TrainingResource {
		public String evaluation = "", model = "", training = "", result = "";
		public List<JsonObject> features = new ArrayList<>();
	}

	@SuppressWarnings("WeakerAccess")
	public static class ProtocolTrainingResource extends TrainingResource {
		public String protocol;
	}

	public static class LETrainingResource extends ProtocolTrainingResource {
		public String lexicon = "";
	}

	// Parameter is the main configuration file for ml commands.
	// Users should set the fields marked with set.  Other fields are
	// there for legacy reasons.
	@SuppressWarnings("WeakerAccess")
	public static class Parameter {
		public LETrainingResource leTraining;
		public TrainingResource rrTraining;
		public ProtocolTrainingResource dmTraining;
		public String model;
		public boolean runLE = false; // set
		public boolean runDM = false; // set
		public ConfigProfiler profiler = new ConfigProfiler(); // set
		public String dir; // set
		public List<JsonObject> leFeatures = new ArrayList<>(); // set
		public List<JsonObject> rrFeatures = new ArrayList<>(); // set
		public List<JsonObject> dmFeatures = new ArrayList<>(); // set
		public String trigrams = ""; // set
		public List<String> filterClasses; // set
		public int nOCR = 0; // set
		public int maxCandidates = 0; // set
	}

	private Parameter parameter;
	private List<Page> pages;

	List<Page> getPages() {
		return pages;
	}

	public Parameter getParameter() {
		return parameter;
	}

	void setParameter(CommandLineArguments args) throws Exception {
		parameter = args.mustGetParameter(Parameter.class);
		// set internal parameters
		parameter.profiler.cacheDir = Paths.get(parameter.dir, "cache").toString();
		parameter.model = Paths.get(parameter.dir, "model.zip").toString();
		parameter.leTraining = new LETrainingResource();
		parameter.leTraining.lexicon = Paths.get(parameter.dir, "le.txt").toString();
		parameter.leTraining.evaluation = Paths.get(parameter.dir, "le_eval.arff").toString();
		parameter.leTraining.result = Paths.get(parameter.dir, "le_result.txt").toString();
		parameter.leTraining.training = Paths.get(parameter.dir, "le_training.arff").toString();
		parameter.leTraining.model = Paths.get(parameter.dir, "le_model.bin").toString();
		parameter.leTraining.protocol = Paths.get(parameter.dir, "le_protocol.json").toString();
		parameter.leTraining.features = parameter.leFeatures;
		parameter.rrTraining = new TrainingResource();
		parameter.rrTraining.evaluation = Paths.get(parameter.dir, "rr_eval.arff").toString();
		parameter.rrTraining.result = Paths.get(parameter.dir, "rr_result.txt").toString();
		parameter.rrTraining.training = Paths.get(parameter.dir, "rr_training.arff").toString();
		parameter.rrTraining.model = Paths.get(parameter.dir, "rr_model.bin").toString();
		parameter.rrTraining.features = parameter.rrFeatures;
		parameter.dmTraining = new ProtocolTrainingResource();
		parameter.dmTraining.evaluation = Paths.get(parameter.dir, "dm_eval.arff").toString();
		parameter.dmTraining.result = Paths.get(parameter.dir, "dm_result.txt").toString();
		parameter.dmTraining.training = Paths.get(parameter.dir, "dm_training.arff").toString();
		parameter.dmTraining.model = Paths.get(parameter.dir, "dm_model.bin").toString();
		parameter.dmTraining.protocol = Paths.get(parameter.dir, "dm_protocol.json").toString();
		parameter.dmTraining.features = parameter.dmFeatures;
	}

	void setParameter(Parameter parameter) {
		this.parameter = parameter;
	}

	FeatureClassFilter getFeatureClassFilter() {
		return new FeatureClassFilter(parameter.filterClasses);
	}

	static JsonObject[] getFeatures(String features) throws Exception {
		final Path path = Paths.get(features);
		JsonObject[] os;
		try (InputStream is = new FileInputStream(path.toFile())) {
			final String json = IOUtils.toString(is, StandardCharsets.UTF_8);
			os = new Gson().fromJson(json, JsonObject[].class);
		}
		return os;
	}

	Map<OCRToken, List<Ranking>> calculateRankings(List<OCRToken> tokens, Iterator<Instance> is, BinaryPredictor c) throws Exception {
		Map<OCRToken, List<Ranking>> rankings = new HashMap<>();
		for (OCRToken token : tokens) {
			boolean first = true;
			for (Candidate candidate : token.getCandidates()) {
				if (!is.hasNext()) {
					throw new Exception("instances and tokens out of sync");
				}
				final Instance instance = is.next();
				Logger.debug("instance of {}: {}", token.toString(), instanceToString(instance));
				final BinaryPrediction p = c.predict(instance);
				Logger.debug("prediction for {}: {}", token.toString(), p.toString());
				final double ranking = p.getPrediction() ? p.getConfidence() : -p.getConfidence();
				Logger.debug("confidence for {}: {}", token.toString(), p.getPrediction());
				Logger.debug("ranking for {}: {}", token.toString(), ranking);
				if (Double.isNaN(ranking)) {
					Logger.warn("ranking for {} is NaN; skipping", token.toString());
					continue;
				}
				if (first) {
					rankings.put(token, new ArrayList<>());
					first = false;
				}
				rankings.get(token).add(new Ranking(candidate, ranking));
			}
			if (rankings.containsKey(token)) {
				rankings.get(token).sort((lhs, rhs) -> Double.compare(rhs.ranking, lhs.ranking));
			}
		}
		return rankings;
	}

	private static String instanceToString(Instance instance) {
		StringJoiner sj = new StringJoiner(",");
		for (int i = 0; i < instance.numAttributes(); i++) {
			sj.add(Double.toString(instance.value(i)));
		}
		return "{" + sj.toString() + "}";
	}


	public static Path tagPath(String path, int n) {
		return Paths.get(path.replaceFirst("(\\..*?)$", "_" + n + "$1"));
	}

	protected interface WordOperation {
		void apply(Word word, String mOCR) throws Exception;
	}

	private static void eachLongWord(Page page, WordOperation f) throws Exception {
		Logger.debug("each long word in page {}", page.getPath().toString());
		int i = 0;
		for (Line line : page.getLines()) {
			for (Word word : line.getWords()) {
				final List<String> unicodeNormalized = word.getUnicodeNormalized();
				if (unicodeNormalized.isEmpty()) {
					continue;
				}
				final String mOCR = unicodeNormalized.get(0);
				if (mOCR.length() <= 3) {
					continue;
				}
				i++;
				f.apply(word, mOCR);
			}
		}
		Logger.info("processed {} long words in {}", i, page.getPath().toString());
	}

	List<OCRToken> readOCRTokens(METS mets, String ifg, AdditionalLexicon alex) throws Exception {
		return readTokens(mets, ifg, alex, false);
	}

	List<OCRToken> readTokensWithGT(METS mets, String ifg, AdditionalLexicon alex) throws Exception {
		return readTokens(mets, ifg, alex, true);
	}

	private List<OCRToken> readTokens(METS mets, String ifg, AdditionalLexicon alex, boolean ocr) throws Exception {
		Logger.debug("read tokens ifg = {}, additional lex {}", ifg, alex.toString());
		List<OCRToken> tokens = new ArrayList<>();
		final int gtIndex = parameter.nOCR;
		pages = openPages(openFiles(mets, ifg));
		final Profile profile = openProfile(ifg, pages, alex);
		for (Page page : pages) {
			eachLongWord(page, (word, mOCR) -> {
				if (ocr) {
					final List<TextEquiv> tes = word.getTextEquivs();
					if (gtIndex < tes.size()) {
						final OCRToken t = new OCRTokenImpl(word, parameter.nOCR, parameter.maxCandidates, profile);
						if (t.getGT().isPresent()) {
							tokens.add(t);
						}
					}
				} else {
					tokens.add(new OCRTokenImpl(word, parameter.nOCR, parameter.maxCandidates, profile));
				}
			});
		}
		return tokens;
	}

	private List<METS.File> openFiles(METS mets, String ifg) {
		return mets.findFileGrpFiles(ifg);
	}

	private List<Page> openPages(List<METS.File> files) throws Exception {
		List<Page> pages = new ArrayList<>(files.size());
		for (METS.File file : files) {
			try (InputStream is = file.openInputStream()) {
				pages.add(Page.parse(Paths.get(file.getFLocat()), is));
			}
		}
		return pages;
	}

	private Profile openProfile(String ifg, List<Page> pages, AdditionalLexicon additionalLex) throws Exception {
		parameter.profiler.setAlex(additionalLex);
		parameter.profiler.setPages(pages);
		parameter.profiler.setInputFileGroup(ifg);
		return parameter.profiler.profile();
	}
}
