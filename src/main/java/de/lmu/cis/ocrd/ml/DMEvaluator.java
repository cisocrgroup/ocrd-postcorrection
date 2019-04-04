package de.lmu.cis.ocrd.ml;

import de.lmu.cis.ocrd.ml.features.OCRToken;
import de.lmu.cis.ocrd.ml.features.Ranking;
import de.lmu.cis.ocrd.pagexml.OCRTokenImpl;
import de.lmu.cis.ocrd.profile.Candidate;
import weka.core.Instance;
import weka.core.Instances;

import java.io.IOException;
import java.io.Writer;
import java.util.*;

public class DMEvaluator {



	private enum Classification {
		UNINTERPRETABLE,
		UNINTERPRETABLE_OCR_ERROR,
		UNINTERPRETABLE_OCR_CORRECT,
		INTERPRETABLE_OCR_CORRECT,
		INTERPRETABLE_OCR_ERROR_HAVE_NO_CANDIDATE,
		INTERPRETABLE_OCR_ERROR_HAVE_CANDIDATE_ON_FIRST_RANK,
		INTERPRETABLE_OCR_ERROR_HAVE_CANDIDATE_ON_OTHER_RANK,
		INTERPRETABLE_OCR_ERROR_TOKENIZATION,
	}

	private final Map<OCRToken, List<Ranking>> rankings;
	private final Map<OCRToken, Classification> classifications;
	private final Map<Classification, YesNo> counts;
	private final List<OCRToken> notInterpretableTokenList;
	private Instances instances;
	private LogisticClassifier classifier;
	private Writer writer;
	private final List<OCRToken> tokens;
	private final int i;

	private int interpretableTokens;
	private int interpretableCorrectTokens;
	private int interpretableNotCorrectTokens;
	private int profilerFirstRankTokens;
	private int missingPlacement;
	private int badPlacement;
	private int goodPlacement;
	private int notInterpretableNotCorrectTokens;
	private int notInterpretableCorrectTokens;
	private int doNotCare;
	private int goodNos;
	private int goodYes;
	private int badNos;
	private int badYes;
	private int correctOCRTokensBefore;
	private int badOCRTokensBefore;
	private int correctOCRTokensAfter;
	private int badOCRTokensAfter;
	private int postCorrectionRealImprovements;
	private int postCorrectionUninterpretable;
	private int postCorrectionBadRank;
	private int postCorrectionMissingCandidate;
	private int postCorrectionMissedOpportunities;
	private int postCorrectionDisimprovements;
	private int postCorrectionFalseFriends;
	private int tokenization;
	private int typeIerrors;
	private int typeIIerrors;

	public DMEvaluator(Map<OCRToken, List<Ranking>> rankings, int i) {
		this.rankings = rankings;
		this.classifications = new HashMap<>();
		this.counts = new HashMap<>();
		for (Classification c : Classification.values()) {
			counts.put(c, new YesNo());
		}
		notInterpretableTokenList = new ArrayList<>();
		tokens = new ArrayList<>();
		this.i = i;
		interpretableTokens = 0;
		interpretableCorrectTokens = 0;
		interpretableNotCorrectTokens = 0;
		profilerFirstRankTokens = 0;
		missingPlacement = 0;
		badPlacement = 0;
		goodPlacement = 0;
		notInterpretableNotCorrectTokens = 0;
		notInterpretableCorrectTokens = 0;
		doNotCare = 0;
		goodNos = 0;
		goodYes = 0;
		badNos = 0;
		badYes = 0;
		correctOCRTokensBefore = 0;
		badOCRTokensBefore = 0;
		correctOCRTokensAfter = 0;
		badOCRTokensAfter = 0;
		postCorrectionRealImprovements = 0;
		postCorrectionUninterpretable = 0;
		postCorrectionBadRank = 0;
		postCorrectionMissingCandidate = 0;
		postCorrectionMissedOpportunities = 0;
		postCorrectionFalseFriends = 0;
		tokenization = 0;
		typeIerrors = 0;
		typeIIerrors = 0;
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

	public void register(OCRToken token) throws Exception {
		tokens.add(token);
		final String gt = token.getGT().orElseThrow(() -> new Exception("missing ground-truth"));
		// is the token interpretable?
		// it is not interpretable if the Profiler did not return any correction suggestion
		// or if there are no rankings for the token because NaNs etc.
		if (token.getAllProfilerCandidates().isEmpty() || rankings.get(token) == null) {
			notInterpretableTokenList.add(token);
			if (token.ocrIsCorrect()) {
				classifications.put(token, Classification.UNINTERPRETABLE_OCR_CORRECT);
				notInterpretableCorrectTokens++;
			} else {
				notInterpretableNotCorrectTokens++;
				postCorrectionUninterpretable++;
				classifications.put(token, Classification.UNINTERPRETABLE_OCR_ERROR);
			}
			return;
		}
		interpretableTokens++;
		// we only care about tokens that we are going to correct
		// correct or incorrect uninterpretable tokens cannot be corrected anyway
		if (token.ocrIsCorrect()) {
			correctOCRTokensBefore++;
		} else {
			badOCRTokensBefore++;
		}
		if (token.ocrIsCorrect()) {
			interpretableCorrectTokens++;
			classifications.put(token, Classification.INTERPRETABLE_OCR_CORRECT);
			return;
		}
		// ocr error (we want to correct something)
		interpretableNotCorrectTokens++;
		if (token.getAllProfilerCandidates().get(0).Suggestion.equalsIgnoreCase(gt)) {
			profilerFirstRankTokens++;
		}
		int placement = getPlacement(token, gt);
		if (gt.matches(".*\\s+.*")) {
			classifications.put(token, Classification.INTERPRETABLE_OCR_ERROR_TOKENIZATION);
			tokenization++;
		} else if (placement == -1) {
			missingPlacement++;
			classifications.put(token, Classification.INTERPRETABLE_OCR_ERROR_HAVE_NO_CANDIDATE);
			updateErrorTypeCounts_I_II(token, gt); // update type i and type ii errors if no candidate in placements
		} else if (placement == 0) {
			goodPlacement++;
			classifications.put(token, Classification.INTERPRETABLE_OCR_ERROR_HAVE_CANDIDATE_ON_FIRST_RANK);
		} else {
			badPlacement++;
			classifications.put(token, Classification.INTERPRETABLE_OCR_ERROR_HAVE_CANDIDATE_ON_OTHER_RANK);
			updateErrorTypeCounts_I_II(token, gt); // update type i and type ii errors if candidate in other placement
		}
	}

	// getPlacement returns the placement of a correct suggestion or -1 if no correct suggestion is present.
	private int getPlacement(OCRToken token, String gt) {
		final List<Ranking> rs = rankings.get(token);
		double before = Double.MAX_VALUE;
		for (int i = 0; i < rs.size(); i++) {
			assert(rs.get(i).ranking <= before);
			before = rs.get(i).ranking;
			if (gt.equalsIgnoreCase(rs.get(i).candidate.Suggestion)) {
				return i;
			}
		}
		return -1;
	}

	// look at all candidates and count type(i) and type(ii) errors
	private void updateErrorTypeCounts_I_II(OCRToken token, String gt) {
		if (token instanceof OCRTokenImpl) {
			boolean found = false;
			for (Candidate candidate : ((OCRTokenImpl)token).getAllProfilerCandidatesNoLimit()) {
				if (gt.equalsIgnoreCase(candidate.Suggestion)) {
					found = true;
					break;
				}
			}
			if (found) {
				typeIIerrors++;
			} else {
				typeIerrors++;
			}
		}
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
		printTokenClassifications();
		printf("\ntotal\n");
		printf("=====\n");
		printf("number of tokens: %d\n", notInterpretableTokenList.size() + interpretableTokens);
		printf("number of uninterpretable tokens: %d\n", notInterpretableTokenList.size());
		printf("number of interpretable tokens: %d\n", interpretableTokens);
		printf("number of ocr correct tokens: %d\n", correctOCRTokensBefore);
		printf("number of ocr error tokens: %d\n", badOCRTokensBefore);

		printf("\nuninterpretable tokens\n");
		printf("======================\n");
		printf("number of uninterpretable tokens: %d\n", notInterpretableTokenList.size());
		printf("number of uninterpretable ocr correct tokens: %d\n", notInterpretableCorrectTokens);
		printf("number of uninterpretable ocr error tokens: %d\n", notInterpretableNotCorrectTokens);

		printf("\ninterpretable tokens\n");
		printf("====================\n");
		printf("number of interpretable tokens: %d\n", interpretableTokens);
		printf("number of interpretable ocr correct tokens: %d\n", interpretableCorrectTokens);
		printf("number of interpretable ocr error tokens: %d\n", interpretableNotCorrectTokens);

		printf("\nerror types\n");
		printf("===========\n");
		printf("type(i)   [no correction candidate]:       %d\n", typeIerrors);
		printf("type(ii)  [good correction not on rank 1]: %d\n", typeIIerrors);
		printf("type(iii) [missed opportunities]:          %d\n", postCorrectionMissedOpportunities);
		printf("type(iv)  [disimprovements]:               %d\n", postCorrectionDisimprovements);

		printf("\npost correction errors\n");
		printf("======================\n");
		printf("real improvements: %d\n", postCorrectionRealImprovements);
		printf("disimprovements: %d\n", postCorrectionDisimprovements);
		printf("do not care: %d\n", doNotCare);
		printf("uninterpretable token: %d\n", postCorrectionUninterpretable);
		printf("true correction not on rank 1: %d\n", postCorrectionBadRank);
		printf("missing correction candidate: %d\n", postCorrectionMissingCandidate);
		printf("false friends: %d\n", postCorrectionFalseFriends);
		printf("tokenization errors: %d\n", tokenization);
		printf("missed opportunities: %d\n", postCorrectionMissedOpportunities);


		printf("\ndecisions ocr correct interpretable tokens\n");
		printf("==========================================\n");
		printf("number of ocr correct interpretable tokens true yes decisions: %d\n",
				counts.get(Classification.INTERPRETABLE_OCR_CORRECT).goodYes);
		printf("number of ocr correct not lexical tokens false yes decisions: %d\n",
				counts.get(Classification.INTERPRETABLE_OCR_CORRECT).badYes);
		printf("number of ocr correct not lexical tokens true no decisions: %d\n",
				counts.get(Classification.INTERPRETABLE_OCR_CORRECT).goodNos);
		printf("number of ocr correct not lexical tokens false no decisions: %d\n",
				counts.get(Classification.INTERPRETABLE_OCR_CORRECT).badNos);

		printf("\nplacements of ocr errors interpretable tokens\n");
		printf("=============================================\n");
		printf("number of good placements (good correction on rank 1): %d\n", goodPlacement);
		printf("number of bad placements (bad correction on rank 1 but a good one was it the top 5): %d\n",
				badPlacement);
		printf("number of no good placement available (no correct correction suggestion in the top 5): %d\n",
				missingPlacement);
		printf("number of good profiler suggestions on rank 1: %d\n", profilerFirstRankTokens);

		printf("\ndecisions on good placement tokens\n");
		printf("==================================\n");
		printf("number of good placement true yes decisions: %d\n",
				counts.get(Classification.INTERPRETABLE_OCR_ERROR_HAVE_CANDIDATE_ON_FIRST_RANK).goodYes);
		printf("number of good placement false yes decisions: %d\n",
				counts.get(Classification.INTERPRETABLE_OCR_ERROR_HAVE_CANDIDATE_ON_FIRST_RANK).badYes);
		printf("number of good placement true no decisions: %d\n",
				counts.get(Classification.INTERPRETABLE_OCR_ERROR_HAVE_CANDIDATE_ON_FIRST_RANK).goodNos);
		printf("number of good placement false no decisions: %d\n",
				counts.get(Classification.INTERPRETABLE_OCR_ERROR_HAVE_CANDIDATE_ON_FIRST_RANK).badNos);

		printf("\ndecisions on bad placement tokens\n");
		printf("=================================\n");
		printf("number of bad placement true yes decisions: %d\n",
				counts.get(Classification.INTERPRETABLE_OCR_ERROR_HAVE_CANDIDATE_ON_OTHER_RANK).goodYes);
		printf("number of bad placement false yes decisions: %d\n",
				counts.get(Classification.INTERPRETABLE_OCR_ERROR_HAVE_CANDIDATE_ON_OTHER_RANK).badYes);
		printf("number of bad placement true no decisions: %d\n",
				counts.get(Classification.INTERPRETABLE_OCR_ERROR_HAVE_CANDIDATE_ON_OTHER_RANK).goodNos);
		printf("number of bad placement false no decisions: %d\n",
				counts.get(Classification.INTERPRETABLE_OCR_ERROR_HAVE_CANDIDATE_ON_OTHER_RANK).badNos);

		printf("\ndecisions on missing placement tokens\n");
		printf("=====================================\n");
		printf("number of missing placement true yes decisions: %d\n",
				counts.get(Classification.INTERPRETABLE_OCR_ERROR_HAVE_NO_CANDIDATE).goodYes);
		printf("number of missing placement false yes decisions: %d\n",
				counts.get(Classification.INTERPRETABLE_OCR_ERROR_HAVE_NO_CANDIDATE).badYes);
		printf("number of missing placement true no decisions: %d\n",
				counts.get(Classification.INTERPRETABLE_OCR_ERROR_HAVE_NO_CANDIDATE).goodNos);
		printf("number of missing placement false no decisions: %d\n",
				counts.get(Classification.INTERPRETABLE_OCR_ERROR_HAVE_NO_CANDIDATE).badNos);

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
		printf("number of do not care yes decisions: %d\n", doNotCare - badYes);


		final String title = String.format("===================\nResults (%d):\n", i+1);
		final String data = classifier.evaluate(title, instances);
		writer.write(data);
	}

	private void printTokenClassifications() {
        final List<Map.Entry<OCRToken, Classification>> entries = new ArrayList<>(classifications.entrySet());
	    entries.sort(Comparator.comparing(Map.Entry::getValue));
	    entries.forEach((entry)->{
            printf("%s: %s", entry.getValue().toString(), entry.getKey());
            if (entry.getValue() == Classification.INTERPRETABLE_OCR_ERROR_HAVE_NO_CANDIDATE ||
					entry.getValue() == Classification.INTERPRETABLE_OCR_ERROR_HAVE_CANDIDATE_ON_OTHER_RANK) {
                ((OCRTokenImpl) entry.getKey()).getAllProfilerCandidatesNoLimit().forEach((c) -> printf(
                		",suggestion:%s;dict:%s;nHist:%d;nOCR:%d", c.Suggestion, c.Dict,
						c.HistPatterns.length, c.OCRPatterns.length));
            }
			printf("\n");
        });
    }

	private void evaluate(OCRToken token, Instance instance) throws Exception {
		final String gt = token.getGT().orElseThrow(() -> new Exception("missing ground-truth"));
		final boolean yes = classify(instance);
		final boolean ocrCorrect = token.ocrIsCorrect();
		final String correction = rankings.get(token).get(0).candidate.Suggestion;
		final boolean correctionCorrect = gt.equalsIgnoreCase(correction);
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

		// post correction errors
		if (!ocrCorrect) {
			if (yes && correctionCorrect) {
				postCorrectionRealImprovements++;
			}
			if (!correctionCorrect) {
				switch (classifications.get(token)) {
					case INTERPRETABLE_OCR_ERROR_HAVE_CANDIDATE_ON_OTHER_RANK:
						postCorrectionBadRank++;
						break;
					case INTERPRETABLE_OCR_ERROR_HAVE_NO_CANDIDATE:
						postCorrectionMissingCandidate++;
						break;
				}
			}
			if (!yes && correctionCorrect) {
				postCorrectionMissedOpportunities++;
			}
			// count false friends
			if (token.getAllProfilerCandidates().size() == 1) {
				final Candidate c = token.getAllProfilerCandidates().get(0);
				if (c.Distance == 0 && c.HistPatterns.length == 0) {
					postCorrectionFalseFriends++;
				}
			}
		} else if(ocrCorrect && yes && !correctionCorrect) {
			postCorrectionDisimprovements++;
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
			if (yes) {
				if (correctionCorrect) {
					this.goodYes++;
				} else{
					this.badYes++;
				}
			} else {
				if (correctionCorrect) {
					badNos++;
				} else {
					goodNos++;
				}
			}
		}
	}

	private boolean classify(Instance instance) throws Exception {
		final Prediction p = classifier.predict(instance);
		return p.getPrediction();
	}

	private void printf(String fmt, Object...args) {
	    try {
            writer.write(String.format(fmt, args));
        } catch (IOException e) {
	        throw new RuntimeException(e);
        }
	}
}
