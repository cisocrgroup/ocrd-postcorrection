package de.lmu.cis.ocrd;

import java.util.ArrayList;

public class NormalizerTransducer {
	private final StringBuilder str;
	private final ArrayList<Double> cs;
	private int state;
	private double prevConfidence;

	public NormalizerTransducer(int n) {
		this.prevConfidence = 0;
		this.state = 0;
		this.str = new StringBuilder(n);
		this.cs = new ArrayList<>(n);
	}

	public static String normalize(String str) {
		final NormalizerTransducer t = new NormalizerTransducer(str.length());
		str.codePoints().forEach((letter) -> t.delta(letter, 0));
		return t.getNormalized();
	}

	// state 0 -- alpha:alpha -> 1
	// state 0 -- !alpha:epsilon -> 0
	// state 1 -- alpha:alpha -> 1
	// state 1 -- !alpha:epsilon -> 2
	// state 2 -- alpha:<SP>alpha -> 1
	// state 2 -- !alpha:epsilon -> 2
	public void delta(int letter, double confidence) {
		final boolean isAlpha = Unicode.isLetter(letter);

		switch (state) {
			case 0:
				if (isAlpha) {
					str.appendCodePoint(letter);
					cs.add(confidence);
					state = 1;
				} else {
					state = 0;
				}
				break;
			case 1:
				if (isAlpha) {
					str.appendCodePoint(letter);
					cs.add(confidence);
					state = 1;
				} else {
					state = 2;
				}
				break;
			case 2:
				if (isAlpha) {
					str.append(' ');
					str.appendCodePoint(letter);
					cs.add(prevConfidence);
					cs.add(confidence);
					state = 1;
				} else {
					state = 2;
				}
				break;
			default:
				throw new RuntimeException("normalizing: invalid state encountered: " + state);
		}
		prevConfidence = confidence;
	}

	public String getNormalized() {
		return str.toString();
	}

	public ArrayList<Double> getConfidences() {
		return cs;
	}
}
