package de.lmu.cis.ocrd;

import java.util.ArrayList;

public class Normalizer {
	private final StringBuilder str, xstr;
	private final ArrayList<Double> cs, xcs;
	private int state;
	private double prevConfidence;

	public Normalizer(int n) {
		this.prevConfidence = 0;
		this.state = 0;
		this.str = new StringBuilder(n);
		this.cs = new ArrayList<>(n);
		this.xstr = new StringBuilder(n);
		this.xcs = new ArrayList<>(n);
	}

	public static String normalize(String str) {
		final Normalizer t = new Normalizer(str.length());
		str.codePoints().forEach((letter) -> t.delta2(letter, 0));
		return t.getNormalized();
	}

	// state 0 -- a:a   -> 1
	// state 0 -- z,w:e -> 0
	// state 1 -- a:a   -> 1
	// state 1 -- z:x   -> 2
	// state 1 -- w:e   -> 3
	// state 2 -- z:x   -> 2
	// state 2 -- w:e   -> 3
	// state 2 -- a:xa  -> 1
	// state 3 -- w,z:e -> 3
	// state 3 -- a:sa    -> 1
	public void delta2(int c, double confidence) {
		final int sigma = getSigma(c); // sigma = _ | . | a
		// System.out.print("state " + state + " sigma " + (char)sigma);
		switch (state) {
			case 0:
				if (sigma == 'a') {
					emit(c, confidence);
					state = 1;
				}
				break;
			case 1:
				switch (sigma) {
					case 'a':
						emit(c, confidence);
						break;
					case '_':
						state = 3;
						break;
					default:
						push(c, confidence);
						state = 2;
						break;
				}
				break;
			case 2:
				switch (sigma) {
					case 'a':
						pop();
						emit(c, confidence);
						state = 1;
						break;
					case '_':
						empty();
						state = 3;
						break;
					default:
						push(c, confidence);
						break;
				}
				break;
			case 3:
				if (sigma == 'a') {
					emit(' ', 0);
					emit(c, confidence);
					state = 1;
				}
				break;
		}
		// System.out.println(" -> " + state);
	}

	private void push(int c, double confidence) {
		xstr.appendCodePoint(c);
		xcs.add(confidence);
	}

	private void pop() {
		str.append(xstr.toString());
		cs.addAll(xcs);
		empty();
	}

	private void empty() {
		xstr.setLength(0);
		xcs.clear();
	}

	private void emit(int c, double confidence) {
		str.appendCodePoint(c);
		cs.add(confidence);
	}

	// state 0 -- alpha:alpha -> 1
	// state 0 -- !alpha:epsilon -> 0
	// state 1 -- alpha:alpha -> 1
	// state 1 -- !alpha:epsilon -> 2
	// state 2 -- alpha:<SP>alpha -> 1
	// state 2 -- !alpha:epsilon -> 2
	public void delta(int letter, double confidence) {
		final boolean isAlpha = isLetter(letter);

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

	private static boolean isLetter(int c) {
		switch (c) {
			case 0x2e17: // DOUBLE OBLIQUE HYPHEN aka '⸗'
				return true;
			default:
				return Unicode.isLetter(c);
		}
	}

	private static int getSigma(int c) {
		return Unicode.isSpace(c) ? '_' : Unicode.isLetter(c) ? 'a' : '.';
	}

	public String getNormalized() {
		return str.toString();
	}

	public ArrayList<Double> getConfidences() {
		return cs;
	}
}
