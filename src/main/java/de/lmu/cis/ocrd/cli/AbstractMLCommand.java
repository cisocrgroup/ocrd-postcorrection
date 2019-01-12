package de.lmu.cis.ocrd.cli;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import de.lmu.cis.ocrd.ml.features.*;
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

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

	protected FeatureSet makeDMFeatureSet(List<Double> cs) {
		final FeatureSet fs = new FeatureSet();
		for (int i = 0; i < parameter.maxCandidates; i++) {
			fs.add(new DecisionMakerConfidenceFeature(
					"rr-confidence", cs, i));
		}
		fs.add(DecisionMakerGTFeature.create(parameter.maxCandidates));
		return fs;
	}

	protected Optional<FeatureSet.Vector> calculateDMFeatureVector(
			OCRToken token,
			FeatureSet fs,
			List<Double> confidences,
			BinaryPredictor p,
			Iterator<Instance> is,
			int i
	) throws Exception {
		final List<Candidate> candidates = token.getAllProfilerCandidates();
		if (candidates.isEmpty()) {
			return Optional.empty();
		}
		for (int j = 0; j < getParameter().maxCandidates; j++) {
			confidences.set(j, 0.0);
		}
		int j = 0;
		for (Candidate candidate : token.getAllProfilerCandidates()) {
			if (!is.hasNext()) {
				throw new Exception("instances and tokens out of sync");
			}
			final Instance instance = is.next();
			final BinaryPrediction prediction = p.predict(instance);
			final double confidence = prediction.getPrediction() ?
					prediction.getConfidence() :
					-prediction.getConfidence();
			Logger.debug("Prediction for dm training: {}", prediction);
			Logger.debug("confidence: {}", confidence);
			confidences.set(j, confidence);
			j++;
		}
		final FeatureSet.Vector values =
				fs.calculateFeatureVector(token, i+1);
		return Optional.of(values);
	}


	protected static Path tagPath(String path, int n) {
		return Paths.get(path.replaceFirst("(\\..*?)$", "_" + n + "$1"));
	}

	protected interface WordOperation {
		void apply(Word word, String mOCR) throws Exception;
	}

	protected static void eachLongWord(Page page, WordOperation f) throws Exception {
		for (Line line : page.getLines()) {
			for (Word word : line.getWords()) {
				String mOCR = word.getUnicodeNormalized().get(0);
				if (mOCR.length() > 3) {
					f.apply(word, mOCR);
				}
			}
		}
	}

	protected List<OCRToken> readTokens(List<METS.File> files) throws Exception {
		List<OCRToken> tokens = new ArrayList<>();
		for (METS.File file : files) {
			try (InputStream is = file.open()) {
				Page page = Page.parse(is);
				eachLongWord(page, (word, mOCR)->{
					tokens.add(new OCRTokenImpl(word, parameter.nOCR,
							parameter.maxCandidates));
				});
			}
		}
		return tokens;
	}
}
