package de.lmu.cis.ocrd.cli;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import de.lmu.cis.ocrd.ml.features.BinaryPrediction;
import de.lmu.cis.ocrd.ml.features.BinaryPredictor;
import de.lmu.cis.ocrd.ml.features.OCRToken;
import de.lmu.cis.ocrd.ml.features.Ranking;
import de.lmu.cis.ocrd.pagexml.*;
import de.lmu.cis.ocrd.profile.*;
import org.apache.commons.io.IOUtils;
import org.pmw.tinylog.Logger;
import weka.core.Instance;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.GZIPOutputStream;

public abstract class AbstractMLCommand extends AbstractIOCommand {
	public static class Profiler {
		String type = "", executable = "", config = "", cacheDir = "";

		public Path getCacheFilePath(String ifg, Optional<Path> additionalLex) {
			String suffix = ".json.gz";
			if (additionalLex.isPresent()) {
				suffix = "_" + additionalLex.get().getFileName().toString().replaceFirst("(\\..*?)$", suffix);
			}
			return Paths.get(cacheDir, ifg + suffix);
		}
	}

	public static class TrainingResource {
		public String evaluation = "", model = "", training = "", features = "", result = "";
	}

	public static class DLETrainingResource extends TrainingResource {
		public String dynamicLexicon = "";
	}

	public static class Parameter {
		public DLETrainingResource dleTraining;
		public TrainingResource rrTraining;
		public TrainingResource dmTraining;
		public Profiler profiler;
		String trigrams = "";
		int nOCR = 0;
		int maxCandidates = 0;
	}

	private Parameter parameter;

	public Parameter getParameter() {
		return parameter;
	}

	void setParameter(CommandLineArguments args) throws Exception {
		parameter = args.mustGetParameter(Parameter.class);
	}

	static JsonObject[] getFeatures(String features) throws Exception {
		final Path path = Paths.get(features);
		JsonObject[] os;
		try (InputStream is = new FileInputStream(path.toFile())) {
			final String json = IOUtils.toString(is, Charset.forName("UTF-8"));
			os = new Gson().fromJson(json, JsonObject[].class);
		}
		return os;
	}

	Map<OCRToken, List<Ranking>> calculateRankings(List<OCRToken> tokens, Iterator<Instance> is, BinaryPredictor c) throws Exception {
		final Map<OCRToken, List<Ranking>> rankings = new HashMap<>();
		for (OCRToken token : tokens) {
			boolean first = true;
			for (Candidate candidate : token.getAllProfilerCandidates()) {
				if (!is.hasNext()) {
					throw new Exception("instances and tokens out of sync");
				}
				if (first) {
					rankings.put(token, new ArrayList<>());
					first = false;
				}
				final Instance instance = is.next();
				final BinaryPrediction p = c.predict(instance);
				final double ranking = p.getPrediction() ?
						p.getConfidence() : -p.getConfidence();
				rankings.get(token).add(new Ranking(candidate, ranking));
			}
			if (rankings.containsKey(token)) {
				rankings.get(token).sort((lhs, rhs) -> Double.compare(rhs.ranking, lhs.ranking));
			}
		}
		return rankings;
	}


	public static Path tagPath(String path, int n) {
		return Paths.get(path.replaceFirst("(\\..*?)$", "_" + n + "$1"));
	}

	protected interface WordOperation {
		void apply(Word word, String mOCR) throws Exception;
	}

	private static void eachLongWord(Page page, WordOperation f) throws Exception {
		for (Line line : page.getLines()) {
			for (Word word : line.getWords()) {
				String mOCR = word.getUnicodeNormalized().get(0);
				if (mOCR.length() > 3) {
					f.apply(word, mOCR);
				} else {
					Logger.debug("word: {} too short", word);
				}
			}
		}
	}

	List<OCRToken> readTokens(METS mets, String ifg, Optional<Path> additionalLex) throws Exception {
		List<OCRToken> tokens = new ArrayList<>();
		final int gtIndex = parameter.nOCR;
		List<Page> pages = openPages(openFiles(mets, ifg));
		final Profile profile = openProfile(ifg, pages, additionalLex);
		for (Page page: pages) {
			eachLongWord(page, (word, mOCR)->{
				Logger.debug("word: {}", word);
				final List<TextEquiv> tes = word.getTextEquivs();
				if (gtIndex < tes.size() &&
						tes.get(gtIndex).getDataTypeDetails().contains("OCR-D-GT")) {
					OCRTokenImpl t = new OCRTokenImpl(word, parameter.nOCR, parameter.maxCandidates, profile);
					Logger.debug("using token: {}", t.toString());
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
		for (METS.File file: files) {
			try (InputStream is = file.open()) {
				pages.add(Page.parse(is));
			}
		}
		return pages;
	}

	private Profile openProfile(String ifg, List<Page> pages, Optional<Path> additionalLex) throws Exception {
		Path cached = parameter.profiler.getCacheFilePath(ifg, additionalLex);
		return openProfileMaybeCached(cached, pages, additionalLex);
	}

	private Profile openProfileMaybeCached(Path cached, List<Page> pages, Optional<Path> additionalLex) throws Exception {
		if (cached.toFile().exists()) {
			return new FileProfiler(cached).profile();
		}
		cached.getParent().toFile().mkdirs();
		Profile profile = getProfiler(pages, additionalLex).profile();
		Charset utf8 = Charset.forName("UTF-8");
		try (Writer w = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(cached.toFile())), utf8))) {
			w.write(profile.toJSON());
			w.write('\n');
		}
		return profile;
	}

	private de.lmu.cis.ocrd.profile.Profiler getProfiler(List<Page> pages, Optional<Path> additionalLex) throws Exception {
		switch (parameter.profiler.type) {
			case "local":
				return new FileGrpProfiler(pages, new LocalProfilerProcess(
						Paths.get(parameter.profiler.executable),
						Paths.get(parameter.profiler.config),
						additionalLex));
			case "file":
				return new FileProfiler(Paths.get(parameter.profiler.config));
			case "url":
				throw new Exception("Profiler type url: not implemented");
			default:
				throw new Exception ("Invalid profiler type: " + parameter.profiler.type);
		}
	}
}
