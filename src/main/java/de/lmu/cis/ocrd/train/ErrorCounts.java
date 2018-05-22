package de.lmu.cis.ocrd.train;

import de.lmu.cis.ocrd.ml.Prediction;
import de.lmu.cis.ocrd.ml.Token;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ErrorCounts {
	private enum ErrorType {TRUE_POSITIVE, FALSE_POSITIVE, TRUE_NEGATIVE, FALSE_NEGATIVE}

	private Map<ErrorType, List<Token>> counts;

	ErrorCounts add(Token token, Prediction prediction, Object gt) {
		if (counts == null) {
			counts = new HashMap<>();
			counts.put(ErrorType.TRUE_POSITIVE, new ArrayList<>());
			counts.put(ErrorType.TRUE_NEGATIVE, new ArrayList<>());
			counts.put(ErrorType.FALSE_POSITIVE, new ArrayList<>());
			counts.put(ErrorType.FALSE_NEGATIVE, new ArrayList<>());
		}
		counts.get(getErrorType(prediction, gt)).add(token);
		return this;
	}


	private int getTruePositiveCount() {
		return getCount(ErrorType.TRUE_POSITIVE);
	}

	private int getFalsePositiveCount() {
		return getCount(ErrorType.FALSE_POSITIVE);
	}

	private int getFalseNegativeCount() {
		return getCount(ErrorType.FALSE_NEGATIVE);
	}

	double getPrecision() {
		final double tp = getTruePositiveCount();
		final double fp = getFalsePositiveCount();
		return tp / (tp + fp);
	}

	double getRecall() {
		final double tp = getTruePositiveCount();
		final double fn = getFalseNegativeCount();
		return tp / (tp + fn);
	}

	Iterable<Token> getTruePositives() {
		return getIterable(ErrorType.TRUE_POSITIVE);
	}

	Iterable<Token> getFalsePositives() {
		return getIterable(ErrorType.FALSE_POSITIVE);
	}

	Iterable<Token> getFalseNegatives() {
		return getIterable(ErrorType.FALSE_NEGATIVE);
	}

	private Iterable<Token> getIterable(ErrorType type) {
		if (counts == null) {
			return new ArrayList<>();
		}
		return counts.get(type);
	}

	private int getCount(ErrorType type) {
		if (counts == null) {
			return 0;
		}
		return counts.get(type).size();
	}

	private static ErrorType getErrorType(Prediction prediction, Object gt) {
		if (!(gt instanceof Boolean)) {
			throw new RuntimeException("invalid ground truth: " + gt);
		}
		final Boolean want = (Boolean) gt;
		final Boolean got = Boolean.valueOf(prediction.label);
		if (want.equals(got)) {
			if (want) {
				return ErrorType.TRUE_POSITIVE;
			} else {
				return ErrorType.TRUE_NEGATIVE;
			}
		} else {
			if (want) {
				return ErrorType.FALSE_NEGATIVE;
			} else {
				return ErrorType.FALSE_POSITIVE;
			}
		}
	}
}
