package de.lmu.cis.ocrd.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringCorrector {
	private static final Pattern p = Pattern.compile("^(\\p{P}*)(.*?)(\\p{P}*)$");
	private final String origin, prefix, suffix;

	public StringCorrector(String origin) {
		final Matcher m = p.matcher(origin);
		final boolean ok = m.matches();
		assert(ok);
		this.prefix = m.group(1);
		this.origin = m.group(2);
		this.suffix = m.group(3);
	}

	// correctWith corrects origin with the given correction.
	// Casing and any leading and/or subsequent punctuation
	// in origin is applied to the correction.
	public String correctWith(String correction) {
		return prefix + applyCasing(correction) + suffix;
	}

	private String applyCasing(String correction) {
		StringBuilder b = new StringBuilder();
		final int [] cCorrection = correction.codePoints().toArray();
		final int [] cOrigin = origin.codePoints().toArray();
		int i = 0;
		boolean allUpper = true;
		boolean allLower = true;
		for (; i < cCorrection.length && i < cOrigin.length; i++) {
			if (Character.isUpperCase(cOrigin[i])) {
				allLower = false;
				b.appendCodePoint(Character.toUpperCase(cCorrection[i]));
			} else if (Character.isLowerCase(cOrigin[i])) {
				allUpper = false;
				b.appendCodePoint(Character.toLowerCase(cCorrection[i]));
			} else {
				b.appendCodePoint(cCorrection[i]);
			}
		}
		for (; i < cCorrection.length; i++) {
			if (allUpper) {
				b.appendCodePoint(Character.toUpperCase(cCorrection[i]));
			} else if (allLower) {
				b.appendCodePoint(Character.toLowerCase(cCorrection[i]));
			} else {
				b.appendCodePoint(cCorrection[i]);
			}
		}
		return b.toString();
	}
}
