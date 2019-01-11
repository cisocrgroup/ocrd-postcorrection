package de.lmu.cis.ocrd.cli;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import de.lmu.cis.ocrd.ml.LogisticClassifier;
import de.lmu.cis.ocrd.ml.features.DecisionMakerConfidenceFeature;
import de.lmu.cis.ocrd.ml.features.Feature;
import de.lmu.cis.ocrd.ml.features.FeatureSet;
import de.lmu.cis.ocrd.ml.features.OCRToken;
import de.lmu.cis.ocrd.pagexml.*;
import org.apache.commons.io.IOUtils;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

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

	protected static Feature getDMConfidenceFeature(Path model,
	                                                FeatureSet fs) throws Exception {
		final LogisticClassifier c = LogisticClassifier.load(model);
		return new DecisionMakerConfidenceFeature("rr-confidence", c, fs);
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
