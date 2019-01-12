package de.lmu.cis.ocrd.ml;

import de.lmu.cis.ocrd.ml.features.OCRToken;
import de.lmu.cis.ocrd.profile.Candidate;
import org.pmw.tinylog.Logger;
import weka.core.Instance;
import weka.core.Instances;

import java.io.Writer;
import java.util.Iterator;
import java.util.List;

public class DMEvaluator {
	private final Instances instances;
	private final LogisticClassifier classifier;
	private final Writer writer;
	private final List<OCRToken> tokens;
	private final int i;

	private int correctTokens,
			correctTokensAfterCorrection,
			falseCorrections,
			totalTokens;
	public DMEvaluator(Writer w, LogisticClassifier c, Instances is,
	                   List<OCRToken> tokens, int i) {
		this.writer = w;
		this.classifier = c;
		this.instances = is;
		this.tokens = tokens;
		this.i = i;
		this.correctTokens = 0;
		this.correctTokensAfterCorrection = 0;
		this.falseCorrections = 0;
		this.totalTokens = 0;
	}

	public void evaluate() throws Exception {
		final Iterator<OCRToken> is = tokens.iterator();
		for (Instance instance : instances) {
			if (!is.hasNext()) {
				throw new Exception("OCRTokens and instances out of sync");
			}
			evaluate(is.next(), instance);
		}
		writer.write(String.format("total number of tokens: %d", totalTokens));
		writer.write(String.format(
				"number of correct tokens (without corrections): %d\n",
				correctTokens));
		writer.write(String.format(
				"number of correct tokens (with corrections): %d\n",
				correctTokensAfterCorrection));
		writer.write(String.format(
				"number of false corrections (disimprovements): %d\n",
				falseCorrections));
		final String title = String.format(
				"===================\nResults (%d):\n", i+1);
		final String data = classifier.evaluate(title, instances);
		writer.write(data);
	}

	private void evaluate(OCRToken token, Instance instance) throws Exception {
		final Prediction p = classifier.predict(instance);
		final String gt = token.getGT().orElseThrow(() -> new Exception(
				"missing ground-truth"));
		boolean masterOCRCorrect = false;
		totalTokens++;
		if (gt.equals(token.getMasterOCR().toString())) {
			masterOCRCorrect = true;
			correctTokens++;
		}
		final String correction = getCorrection(token, p);
		if (gt.equals(correction)) {
			correctTokensAfterCorrection++;
		} else if (masterOCRCorrect) {
			falseCorrections++;
		}
	}

	private String getCorrection(OCRToken token, Prediction p) {
		final int index = (int) p.getValue();
		final List<Candidate> candidates = token.getAllProfilerCandidates();
		// index = 0 means no correction; keep master ocr token
		if (index == 0) {
			final String correction = token.getMasterOCR().toString();
			Logger.debug("token: {}, correction: {}, prediction: {}",
					token, correction, p);
			return correction;
		}
		// index = 1 -> first candidate, ...
		if (index <= candidates.size()) {
			final String correction = candidates.get(index-1).Suggestion;
			Logger.debug("token: {}, correction: {}, prediction: {}",
					token, correction, p);
			return correction;
		}
		return "**INVALID-TOKEN**";
	}
}
