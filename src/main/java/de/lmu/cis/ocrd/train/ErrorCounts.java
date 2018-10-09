package de.lmu.cis.ocrd.train;

import de.lmu.cis.ocrd.ml.Prediction;
import de.lmu.cis.ocrd.ml.Token;

import java.util.*;

class ErrorCounts {
	private Map<ErrorType, List<Token>> counts = null;
	private HashSet<String> types = null;
	ErrorCounts(boolean typeBased) {
		if (typeBased) {
			this.types = new HashSet<>();
		}
	}

	ErrorCounts() {
		this(false);
	}

	private static double calculateRate(int a, int b) {
		if (a + b == 0) {
			return 1;
		}
		return (double) a / ((double) (a + b));
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

	int getTotalCount() {
		return getTruePositiveCount() + getTrueNegativeCount() + getFalsePositiveCount() + getFalseNegativeCount();
	}

	ErrorCounts add(Token token, Prediction prediction, Object gt) {
		if (counts == null) {
			counts = new HashMap<>();
			counts.put(ErrorType.TRUE_POSITIVE, new ArrayList<>());
			counts.put(ErrorType.TRUE_NEGATIVE, new ArrayList<>());
			counts.put(ErrorType.FALSE_POSITIVE, new ArrayList<>());
			counts.put(ErrorType.FALSE_NEGATIVE, new ArrayList<>());
		}
		if (types != null) {
			if (types.contains(token.getMasterOCR().toString())) {
				return this;
			}
			types.add(token.getMasterOCR().toString());
		}
		counts.get(getErrorType(prediction, gt)).add(token);
		return this;
	}

	int getTruePositiveCount() {
		return getCount(ErrorType.TRUE_POSITIVE);
	}

	int getFalsePositiveCount() {
		return getCount(ErrorType.FALSE_POSITIVE);
	}

	int getFalseNegativeCount() {
		return getCount(ErrorType.FALSE_NEGATIVE);
	}

	int getTrueNegativeCount() {
		return getCount(ErrorType.TRUE_NEGATIVE);
	}

	double getPrecision() {
		return calculateRate(getTruePositiveCount(), getFalsePositiveCount());
	}

	double getRecall() {
		return calculateRate(getTruePositiveCount(), getFalseNegativeCount());
	}

	double getF1() {
		return getFMeasure(1.0);
	}

	private double getFMeasure(double beta) {
		final double b = Math.pow(beta, 2);
		final double p = getPrecision();
		final double r = getRecall();
		return (1 + b) * ((p * r) / ((p * b) + r));
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

	private enum ErrorType {TRUE_POSITIVE, FALSE_POSITIVE, TRUE_NEGATIVE, FALSE_NEGATIVE}
}
