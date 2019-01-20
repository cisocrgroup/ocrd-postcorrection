package de.lmu.cis.ocrd.ml;

import de.lmu.cis.ocrd.ml.features.OCRToken;
import de.lmu.cis.ocrd.ml.features.Ranking;
import weka.core.Instance;
import weka.core.Instances;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DMEvaluator {



	private enum Classification {
		LEXICAL,
		NON_LEXICAL_CORRECT,
		NON_LEXICAL_NOT_CORRECT_HAVE_NOT_CANDIDATE,
		NON_LEXICAL_NOT_CORRECT_HAVE_CANDIDATE_ON_FIRST_RANK,
		NON_LEXICAL_NOT_CORRECT_HAVE_CANDIDATE_ON_OTHER_RANK,
		LEXICAL_NOT_CORRECT,
		LEXICAL_CORRECT,
	}

	private final Map<OCRToken, List<Ranking>> rankings;
	private final Map<OCRToken, Classification> classifications;
	private final Map<Classification, YesNo> counts;
	private Instances instances;
	private LogisticClassifier classifier;
	private Writer writer;
	private List<OCRToken> tokens;
	private final int i;

	private int nonLexicalTokens;
	private int lexicalTokens;
	private int correctNonLexicalTokens;
	private int nonCorrectNonLexicalTokens;
	private int profilerFirstRankTokens;
	private int missingPlacement;
	private int badPlacement;
	private int goodPlacement;
	private int lexicalNotCorrectTokens;
	private int lexicalCorrectTokens;
	private int doNotCare;
	private int goodNos;
	private int goodYes;
	private int badNos;
	private int badYes;
	private int correctOCRTokensBefore;
	private int badOCRTokensBefore;
	private int correctOCRTokensAfter;
	private int badOCRTokensAfter;

	public DMEvaluator(Map<OCRToken, List<Ranking>> rankings, int i) {
		this.rankings = rankings;
		this.classifications = new HashMap<>();
		this.counts = new HashMap<>();
		for (Classification c : Classification.values()) {
			counts.put(c, new YesNo());
		}
		this.i = i;
		nonLexicalTokens = 0;
		lexicalTokens = 0;
		correctNonLexicalTokens = 0;
		nonCorrectNonLexicalTokens = 0;
		profilerFirstRankTokens = 0;
		missingPlacement = 0;
		badPlacement = 0;
		goodPlacement = 0;
		lexicalNotCorrectTokens = 0;
		lexicalCorrectTokens = 0;
		doNotCare = 0;
		goodNos = 0;
		goodYes = 0;
		badNos = 0;
		badYes = 0;
		correctOCRTokensBefore = 0;
		badOCRTokensBefore = 0;
		correctOCRTokensAfter = 0;
		badOCRTokensAfter = 0;
	}

	public void setInstances(Instances instances) {
		this.instances = instances;
	}

	public void setClassifier(LogisticClassifier classifier) {
		this.classifier = classifier;
	}

	public void setWriter(Writer writer) {
		this.writer = writer;
	}

	public void setTokens(List<OCRToken> tokens) {
		this.tokens = tokens;
	}

	public void addToken(OCRToken token) throws Exception {
		final String gt = token.getGT().orElseThrow(() -> new Exception("missing ground-truth"));
		// is token lexical?
		if (token.getAllProfilerCandidates().isEmpty()) {
			lexicalTokens++;
			if (gt.equals(token.getMasterOCR().toString())) {
				classifications.put(token, Classification.LEXICAL_CORRECT);
				lexicalCorrectTokens++;
			} else {
				lexicalNotCorrectTokens++;
				classifications.put(token, Classification.LEXICAL_NOT_CORRECT);
			}
			return;
		}
		nonLexicalTokens++;
		// we only care about tokens that we are going to correct
		// correct or incorrect lexical tokens cannot be corrected anyway
		if (gt.equals(token.getMasterOCR().toString())) {
			correctOCRTokensBefore++;
		} else {
			badOCRTokensBefore++;
		}
		if (gt.equals(token.getMasterOCR().toString())) {
			correctNonLexicalTokens++;
			classifications.put(token, Classification.NON_LEXICAL_CORRECT);
			return;
		}
		nonCorrectNonLexicalTokens++;
		if (token.getAllProfilerCandidates().get(0).Suggestion.equals(gt)) {
			profilerFirstRankTokens++;
		}
		int placement = getPlacement(token, gt);
		if (placement == -1) {
			missingPlacement++;
			classifications.put(token, Classification.NON_LEXICAL_NOT_CORRECT_HAVE_NOT_CANDIDATE);
		} else if (placement == 0) {
			goodPlacement++;
			classifications.put(token, Classification.NON_LEXICAL_NOT_CORRECT_HAVE_CANDIDATE_ON_FIRST_RANK);
		} else {
			badPlacement++;
			classifications.put(token, Classification.NON_LEXICAL_NOT_CORRECT_HAVE_CANDIDATE_ON_OTHER_RANK);
		}
	}

	// getPlacement returns the placement of a correct suggestion or -1 if no correct suggestion is present.
	private int getPlacement(OCRToken token, String gt) {
		final List<Ranking> rs = rankings.get(token);
		double before = Double.MAX_VALUE;
		for (int i = 0; i < rs.size(); i++) {
			assert(rs.get(i).ranking <= before);
			before = rs.get(i).ranking;
			if (gt.equals(rs.get(i).candidate.Suggestion)) {
				return i;
			}
		}
		return -1;
	}

	public void evaluate() throws Exception {
		final Iterator<OCRToken> is = tokens.iterator();
		for (Instance instance : instances) {
			OCRToken token = null;
			while (is.hasNext()) {
				token = is.next();
				if (rankings.containsKey(token)) {
					break;
				}
			}
			if (token == null) {
				throw new Exception("OCRTokens and instances out of sync");
			}
			evaluate(token, instance);
		}
		printf("total\n");
		printf("=====\n");
		printf("number of tokens: %d\n", lexicalTokens + nonLexicalTokens);

		printf("\nlexical tokens\n");
		printf("==============\n");
		printf("number of lexical tokens: %d\n", lexicalTokens);
		printf("number of correct lexical tokens: %d\n", lexicalCorrectTokens);
		printf("number of not correct lexical tokens: %d\n", lexicalNotCorrectTokens);
		printf("number of correct OCR tokens: %d\n", correctOCRTokensBefore);
		printf("number of incorrect OCR tokens: %d\n", badOCRTokensBefore);

		printf("\nnot lexical tokens\n");
		printf("==================\n");
		printf("number of not lexical tokens: %d\n", nonLexicalTokens);
		printf("number of correct not lexical tokens: %d\n", correctNonLexicalTokens);
		printf("number of not correct not lexical tokens: %d\n", nonCorrectNonLexicalTokens);

		printf("\ndecisions on correct not lexical tokens\n");
		printf("=======================================\n");
		printf("number of correct not lexical tokens true yes decisions: %d\n",
				counts.get(Classification.NON_LEXICAL_CORRECT).goodYes);
		printf("number of correct not lexical tokens false yes decisions: %d\n",
				counts.get(Classification.NON_LEXICAL_CORRECT).badYes);
		printf("number of correct not lexical tokens true no decisions: %d\n",
				counts.get(Classification.NON_LEXICAL_CORRECT).goodNos);
		printf("number of correct not lexical tokens false no decisions: %d\n",
				counts.get(Classification.NON_LEXICAL_CORRECT).badNos);

		printf("\nnot correct not lexical tokens\n");
		printf("==============================\n");
		printf("number of good placements (good correction on rank 1): %d\n", goodPlacement);
		printf("number of bad placements (bad correction on rank 1 but a good one was it the top 5): %d\n",
				badPlacement);
		printf("number of no good placement available (no correct correction suggestion in the top 5): %d\n",
				missingPlacement);
		printf("number of good profiler suggestions on rank 1: %d\n", profilerFirstRankTokens);

		printf("\ndecisions on good placement tokens\n");
		printf("==================================\n");
		printf("number of good placement true yes decisions: %d\n",
				counts.get(Classification.NON_LEXICAL_NOT_CORRECT_HAVE_CANDIDATE_ON_FIRST_RANK).goodYes);
		printf("number of good placement false yes decisions: %d\n",
				counts.get(Classification.NON_LEXICAL_NOT_CORRECT_HAVE_CANDIDATE_ON_FIRST_RANK).badYes);
		printf("number of good placement true no decisions: %d\n",
				counts.get(Classification.NON_LEXICAL_NOT_CORRECT_HAVE_CANDIDATE_ON_FIRST_RANK).goodNos);
		printf("number of good placement false no decisions: %d\n",
				counts.get(Classification.NON_LEXICAL_NOT_CORRECT_HAVE_CANDIDATE_ON_FIRST_RANK).badNos);

		printf("\ndecisions on bad placement tokens\n");
		printf("=================================\n");
		printf("number of bad placement true yes decisions: %d\n",
				counts.get(Classification.NON_LEXICAL_NOT_CORRECT_HAVE_CANDIDATE_ON_OTHER_RANK).goodYes);
		printf("number of bad placement false yes decisions: %d\n",
				counts.get(Classification.NON_LEXICAL_NOT_CORRECT_HAVE_CANDIDATE_ON_OTHER_RANK).badYes);
		printf("number of bad placement true no decisions: %d\n",
				counts.get(Classification.NON_LEXICAL_NOT_CORRECT_HAVE_CANDIDATE_ON_OTHER_RANK).goodNos);
		printf("number of bad placement false no decisions: %d\n",
				counts.get(Classification.NON_LEXICAL_NOT_CORRECT_HAVE_CANDIDATE_ON_OTHER_RANK).badNos);

		printf("\ndecisions on missing placement tokens\n");
		printf("=====================================\n");
		printf("number of missing placement true yes decisions: %d\n",
				counts.get(Classification.NON_LEXICAL_NOT_CORRECT_HAVE_NOT_CANDIDATE).goodYes);
		printf("number of missing placement false yes decisions: %d\n",
				counts.get(Classification.NON_LEXICAL_NOT_CORRECT_HAVE_NOT_CANDIDATE).badYes);
		printf("number of missing placement true no decisions: %d\n",
				counts.get(Classification.NON_LEXICAL_NOT_CORRECT_HAVE_NOT_CANDIDATE).goodNos);
		printf("number of missing placement false no decisions: %d\n",
				counts.get(Classification.NON_LEXICAL_NOT_CORRECT_HAVE_NOT_CANDIDATE).badNos);

		printf("\ncorrections\n");
		printf("===========\n");
		printf("number of correct OCR tokens: %d\n", correctOCRTokensBefore);
		printf("number of incorrect OCR tokens: %d\n", badOCRTokensBefore);
		printf("number of correct OCR tokens (after correction): %d\n", correctOCRTokensAfter);
		printf("number of incorrect OCR tokens (after correction): %d\n", badOCRTokensAfter);
		printf("number of true yes decisions: %d\n", goodYes);
		printf("number of false yes decisions: %d\n", badYes);
		printf("number of true no decisions: %d\n", goodNos);
		printf("number of false no decisions: %d\n", badNos);
		printf("number of do not care yes decisions: %d\n", doNotCare);


		final String title = String.format("===================\nResults (%d):\n", i+1);
		final String data = classifier.evaluate(title, instances);
		writer.write(data);
	}

	private void evaluate(OCRToken token, Instance instance) throws Exception {
		final String gt = token.getGT().orElseThrow(() -> new Exception("missing ground-truth"));
		final boolean yes = classify(instance);
		final boolean ocrCorrect = gt.equals(token.getMasterOCR().toString());
		final boolean correctionCorrect = rankings.get(token).get(0).candidate.Suggestion.equals(gt);
		counts.get(classifications.get(token)).add(yes, correctionCorrect);

		// we have an invalid ocr token and the correction is bad
		if (yes && !ocrCorrect && !correctionCorrect) {
			doNotCare++;
		}
		// the ocr is correct and we don't correct
		if (!yes && ocrCorrect) {
			goodNos++;
		}
		// the ocr is not correct but we could have corrected
		if (!yes && !ocrCorrect && correctionCorrect) {
			badNos++;
		}
		// the ocr is correct and we correct bad
		if (yes && ocrCorrect && !correctionCorrect) {
			badYes++;
		}
		// the ocr is not correct and we correct good
		if (yes && ocrCorrect && correctionCorrect) {
			goodYes++;
		}
		if ((yes && correctionCorrect) || (!yes && ocrCorrect)) {
			correctOCRTokensAfter++;
		}
		if ((yes && !correctionCorrect) || (!yes && !ocrCorrect)) {
			badOCRTokensAfter++;
		}
	}

	private static class YesNo {
		int goodYes, badYes, goodNos, badNos;
		YesNo() {
			goodYes = 0;
			goodNos = 0;
			badYes = 0;
			badNos = 0;
		}
		void add(boolean yes, boolean correctionCorrect) {
			if (yes && correctionCorrect) {
				this.goodYes++;
			} else if (yes && !correctionCorrect) {
				this.badYes++;
			} else if (!yes && correctionCorrect) {
				this.badNos++;
			} else {
				this.goodNos++;
			}
		}
	}

	private boolean classify(Instance instance) throws Exception {
		final Prediction p = classifier.predict(instance);
		return p.getPrediction();
	}

	private void printf(String fmt, Object...args) throws IOException {
		writer.write(String.format(fmt, args));
	}
}
