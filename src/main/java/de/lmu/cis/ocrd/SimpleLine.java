package de.lmu.cis.ocrd;

import java.io.Serializable;
import java.util.*;

public class SimpleLine implements Line, Serializable {

	private int lineID, pageID;
	private String line;
	private ArrayList<Double> cs;

	public static SimpleLine normalized(String ocr, double c) {
		NormalizerTransducer t = new NormalizerTransducer(ocr.length());
		ocr.codePoints().forEach((letter) -> t.delta(letter, c));
		SimpleLine line = new SimpleLine();
		line.line = t.getNormalized();
		line.cs = t.getConfidences();
		return line;
	}

	public static SimpleLine normalized(String ocr, Double... cs) {
		return normalized(ocr, Arrays.asList(cs));
	}

	public static SimpleLine normalized(String ocr, List<Double> cs) {
		NormalizerTransducer t = new NormalizerTransducer(ocr.length());
		Iterator<Double> it = cs.iterator();
		ocr.codePoints().forEach((letter) -> {
			if (!it.hasNext()) {
				throw new IndexOutOfBoundsException("too few confidences for: " + ocr + " (" + cs.size() + " vs. " + ocr.length() + ")");
			}
			t.delta(letter, it.next());
		});
		SimpleLine line = new SimpleLine();
		line.line = t.getNormalized();
		line.cs = t.getConfidences();
		return line;
	}

	public double getConfidenceAt(int i) {
		return cs.get(i);
	}

	@Override
	public int getLineId() {
		return this.lineID;
	}

	@Override
	public String getNormalized() {
		return this.line;
	}

	@Override
	public int getPageId() {
		return this.pageID;
	}

	public SimpleLine withLineID(int id) {
		this.lineID = id;
		return this;
	}

	public SimpleLine withPageID(int id) {
		this.pageID = id;
		return this;
	}

	public Optional<Word> getWord(String word) {
		return getWord(0, word);
	}

	public Optional<Word> getWord(int offset, String word) {
		int pos = line.indexOf(word, offset);
		if (pos >= 0) {
			return Optional.of(new Word(pos, pos + word.length(), this));
		}
		// Fallback: try whole line
		pos = line.indexOf(word);
		if (pos >= 0) {
			return Optional.of(new Word(pos, pos + word.length(), this));
		}
		return Optional.empty();
	}

	public Optional<Word> getWord(List<String> words) {
		return getWord(0, words);
	}

	public Optional<Word> getWord(int offset, List<String> words) {
		return getWord(offset, String.join(" ", words));
	}

	public static class Data {
		public final int pageID, lineID;
		final String normalized;

		public Data(SimpleLine line) {
			this.pageID = line.pageID;
			this.lineID = line.lineID;
			this.normalized = line.getNormalized();
		}
	}
}
