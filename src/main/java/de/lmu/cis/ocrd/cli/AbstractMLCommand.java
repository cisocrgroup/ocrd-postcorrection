package de.lmu.cis.ocrd.cli;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import de.lmu.cis.ocrd.ml.features.BinaryPrediction;
import de.lmu.cis.ocrd.ml.features.BinaryPredictor;
import de.lmu.cis.ocrd.ml.features.OCRToken;
import de.lmu.cis.ocrd.ml.features.Ranking;
import de.lmu.cis.ocrd.pagexml.*;
import de.lmu.cis.ocrd.profile.Candidate;
import org.apache.commons.io.IOUtils;
import org.pmw.tinylog.Logger;
import weka.core.Instance;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public abstract class AbstractMLCommand extends AbstractIOCommand {

	static class TrainingResource {
		String evaluation = "", model = "", training = "", features = "",
				result = "";
	}

	static class DLETrainingResource extends TrainingResource {
		public String dynamicLexicon = "";
	}

	public static class Parameter {
		DLETrainingResource dleTraining;
		TrainingResource rrTraining, dmTraining;
		String trigrams = "";
		int nOCR;
		int maxCandidates;
	}

	private Parameter parameter;

	protected Parameter getParameter() {
		return parameter;
	}

	protected void setParameter(CommandLineArguments args) throws Exception {
		parameter = args.mustGetParameter(Parameter.class);
	}

	protected static JsonObject[] getFeatures(String features) throws Exception {
		final Path path = Paths.get(features);
		JsonObject[] os;
		try (InputStream is = new FileInputStream(path.toFile())) {
			final String json = IOUtils.toString(is, Charset.forName("UTF-8"));
			os = new Gson().fromJson(json, JsonObject[].class);
		}
		return os;
	}

	protected Map<OCRToken, List<Ranking>> calculateRankings(List<OCRToken> tokens, Iterator<Instance> is, BinaryPredictor c) throws Exception {
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
				rankings.get(token).sort((lhs, rhs) -> {
					if (lhs.ranking < rhs.ranking) {
						return 1;
					}
					if (lhs.ranking > rhs.ranking) {
						return -1;
					}
					return 0;
				});

			}
		}
		return rankings;
	}


	protected static Path tagPath(String path, int n) {
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

	protected List<OCRToken> readTokens(List<METS.File> files) throws Exception {
		List<OCRToken> tokens = new ArrayList<>();
		final int gtIndex = parameter.nOCR;
		for (METS.File file : files) {
			try (InputStream is = file.open()) {
				Page page = Page.parse(is);
				eachLongWord(page, (word, mOCR)->{
					Logger.debug("word: {}", word);
					final List<TextEquiv> tes = word.getTextEquivs();
					if (gtIndex < tes.size() &&
							tes.get(gtIndex).getDataTypeDetails().contains("OCR-D-GT")) {
						OCRTokenImpl t = new OCRTokenImpl(word, parameter.nOCR, parameter.maxCandidates);
						Logger.debug("using token: {}", t.toString());
						if (t.getGT().isPresent()) {
							tokens.add(t);
						}
					}
				});
			}
		}
		return tokens;
	}
}
