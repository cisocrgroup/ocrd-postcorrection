package de.lmu.cis.ocrd.profile;

import org.apache.commons.lang.WordUtils;

import java.util.StringJoiner;

public class Candidate {
	public String Suggestion, Modern, Dict;
	public PosPattern[] HistPatterns, OCRPatterns;
	public int Distance;
	public double Weight;

	Candidate allToLowerCase() {
		this.Suggestion = Suggestion.toLowerCase();
		this.Modern = this.Modern.toLowerCase();
		this.Dict = this.Dict.toLowerCase();
		if (this.HistPatterns != null) {
			for (PosPattern h : this.HistPatterns) {
				h.toLowerCase();
			}
		}
		if (this.OCRPatterns != null) {
			for (PosPattern o : this.OCRPatterns) {
				o.toLowerCase();
			}
		}
		return this;
	}

	void adjustOCRPosPatterns() {
		if (OCRPatterns.length <= 1) {
			return;
		}
		// at least 2 pos patterns
		int adjustment = OCRPatterns[0].getAdjustment();
		for (int i = 1; i < OCRPatterns.length; i++) {
			OCRPatterns[i].Pos += adjustment;
			adjustment += OCRPatterns[i].getAdjustment();
			if (OCRPatterns[i].Pos < 0) { // clip to 0
				OCRPatterns[i].Pos = 0;
			}
		}
	}

	// returns true if the candidate is a lexicon entry,
	// i.e. has no historical and no ocr patterns.
	public boolean isLexiconEntry() {
		return Distance == 0 && (HistPatterns == null || HistPatterns.length == 0);
	}

	// returns the proper cased suggestion string for the given ocr token.
	public String getAsSuggestionFor(String ocr) {
		if (ocr == null || "".equals(ocr) || Suggestion == null || "".equals(Suggestion)) {
			return Suggestion;
		}
		if (ocr.codePoints().allMatch((c)-> Character.getType(c) == Character.UPPERCASE_LETTER)) {
			return Suggestion.toUpperCase();
		}
		if (Character.getType(ocr.codePointAt(0)) == Character.UPPERCASE_LETTER) {
			return WordUtils.capitalizeFully(Suggestion);
		}
		return Suggestion;
	}

	public String toString() {
		StringJoiner sj = new StringJoiner(",");
		sj.add(Suggestion);
		sj.add("ocr:" + patternsToString(OCRPatterns));
		sj.add("hist:" + patternsToString(HistPatterns));
		sj.add("dist:" + Distance);
		sj.add("weight:" + Weight);
		sj.add("dict:" + Dict);
		return sj.toString();
	}

	private static String patternsToString(PosPattern[] pats) {
		StringJoiner sj = new StringJoiner(",", "[", "]");
		if (pats == null) {
			return sj.toString();
		}
		for (PosPattern pat: pats) {
			sj.add("(" + pat.Left + ":" + pat.Right + ":" + pat.Pos + ")");
		}
		return sj.toString();
	}
}
