package de.lmu.cis.ocrd.cli;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import de.lmu.cis.ocrd.ml.features.*;
import de.lmu.cis.ocrd.pagexml.*;
import de.lmu.cis.ocrd.profile.*;
import org.apache.commons.io.IOUtils;
import org.pmw.tinylog.Logger;
import weka.core.Instance;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.GZIPOutputStream;

public abstract class AbstractMLCommand extends AbstractIOCommand {
	public static class Profiler {
		public String path = "", config = "", cacheDir = "";

		public Path getCacheFilePath(String ifg, AdditionalLexicon additionalLex) {
			String suffix = ".json.gz";
			if (additionalLex.use()) {
				suffix = "_" + additionalLex.toString() + suffix;
			}
			return Paths.get(cacheDir, ifg + suffix);
		}
	}

	public static class TrainingResource {
		public String evaluation = "", model = "", training = "", features = "", result = "";
	}

	public static class ProtocolTrainingResource extends TrainingResource {
		public String protocol;
	}

	public static class LETrainingResource extends ProtocolTrainingResource {
		public String lexicon = "";
	}

	public static class Parameter {
		public LETrainingResource leTraining;
		public TrainingResource rrTraining;
		public ProtocolTrainingResource dmTraining;
		public Profiler profiler = new Profiler(); // set
		public String model;
		public String dir; // set
		public String leFeatures = ""; // set
		public String rrFeatures = ""; // set
		public String dmFeatures = ""; // set
		public String trigrams = ""; // set
        public List<String> filterClasses; // set
		public int nOCR = 0; // set
		public int maxCandidates = 0; // set
        public boolean runLE = false; // set
        public boolean runDM = false; // set
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
		parameter.leTraining.evaluation = Paths.get(parameter.dir, "le-eval.arff").toString();
		parameter.leTraining.result = Paths.get(parameter.dir, "le-result.arff").toString();
		parameter.leTraining.training = Paths.get(parameter.dir, "le-training.arff").toString();
		parameter.leTraining.model = Paths.get(parameter.dir, "le-model.bin").toString();
		parameter.leTraining.protocol = Paths.get(parameter.dir, "le-protocol.json").toString();
		parameter.leTraining.features = parameter.leFeatures;
		parameter.rrTraining = new TrainingResource();
		parameter.rrTraining.evaluation = Paths.get(parameter.dir, "rr-eval.arff").toString();
		parameter.rrTraining.result = Paths.get(parameter.dir, "rr-result.arff").toString();
		parameter.rrTraining.training = Paths.get(parameter.dir, "rr-training.arff").toString();
		parameter.rrTraining.model = Paths.get(parameter.dir, "rr-model.bin").toString();
		parameter.rrTraining.features = parameter.rrFeatures;
		parameter.dmTraining = new ProtocolTrainingResource();
		parameter.dmTraining.evaluation = Paths.get(parameter.dir, "dm-eval.arff").toString();
		parameter.dmTraining.result = Paths.get(parameter.dir, "dm-result.arff").toString();
		parameter.dmTraining.training = Paths.get(parameter.dir, "dm-training.arff").toString();
		parameter.dmTraining.model = Paths.get(parameter.dir, "dm-model.bin").toString();
		parameter.dmTraining.protocol = Paths.get(parameter.dir, "dm-protocol.json").toString();
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
			for (Candidate candidate : token.getAllProfilerCandidates()) {
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

	List<OCRToken> readTokens(METS mets, String ifg, AdditionalLexicon additionalLex) throws Exception {
		Logger.debug("read tokens ifg = {}, additional lex {}", ifg, additionalLex.toString());
		List<OCRToken> tokens = new ArrayList<>();
		final int gtIndex = parameter.nOCR;
		pages = openPages(openFiles(mets, ifg));
		final Profile profile = openProfile(ifg, pages, additionalLex);
		for (Page page : pages) {
			eachLongWord(page, (word, mOCR) -> {
				final List<TextEquiv> tes = word.getTextEquivs();
				if (gtIndex < tes.size() &&
						tes.get(gtIndex).getDataTypeDetails().contains("OCR-D-GT")) {
					OCRTokenImpl t = new OCRTokenImpl(word, parameter.nOCR, parameter.maxCandidates, profile);
					if (t.getGT().isPresent()) {
						tokens.add(t);
					}
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
		Path cached = parameter.profiler.getCacheFilePath(ifg, additionalLex);
		if (parameter.profiler.cacheDir == null || "".equals(parameter.profiler.cacheDir)) {
			cached = null;
		}
		return openProfileMaybeCached(cached, pages, additionalLex);
	}

	private Profile openProfileMaybeCached(Path cached, List<Page> pages, AdditionalLexicon additionalLex) throws Exception {
		if (cached != null && cached.toFile().exists()) {
			Logger.debug("opening cached profile: {}", cached.toString());
			return new FileProfiler(cached).profile();
		}
		if (cached != null && cached.getParent().toFile().mkdirs()) {
			Logger.debug("created cache directory {}", cached.getParent().toString());
		}
		Profile profile = getProfiler(pages, additionalLex).profile();
		Charset utf8 = StandardCharsets.UTF_8;
		if (cached == null) {
			return profile;
		}
		Logger.debug("caching profile: {}", cached.toString());
		try (Writer w = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(cached.toFile())), utf8))) {
			w.write(profile.toJSON());
			w.write('\n');
		}
		return profile;
	}

	private de.lmu.cis.ocrd.profile.Profiler getProfiler(List<Page> pages, AdditionalLexicon additionalLex) throws Exception {
		if (parameter.profiler.path.toLowerCase().endsWith(".json") || parameter.profiler.path.toLowerCase().endsWith(".gz")) {
			Logger.debug("using a file profiler: {}", parameter.profiler.path);
			return new FileProfiler(Paths.get(parameter.profiler.path));
		}
		if (parameter.profiler.path.toLowerCase().startsWith("http://") || parameter.profiler.path.toLowerCase().startsWith("https://")) {
			throw new Exception("Profiler type url: not implemented");
		}
		Logger.debug("using a local profiler: {} {}", parameter.profiler.path, parameter.profiler.config);
		return new FileGrpProfiler(pages, new LocalProfilerProcess(
				Paths.get(parameter.profiler.path),
				Paths.get(parameter.profiler.config),
				additionalLex));
	}
}
