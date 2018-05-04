package de.lmu.cis.ocrd;

import de.lmu.cis.pocoweb.Token;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class SimpleLine implements Line {

	private static String normalize(String line) {
		return line.replaceAll("[^\\p{L}\\p{Nd}]+", " ").trim();
	}

	private int lineID, pageID;
	private String line;

	private ArrayList<Double> cs;

	public static SimpleLine normalized(String ocr, double c) {
	    NormalizerTransducer t = new NormalizerTransducer(ocr.length());
	    ocr.codePoints().forEach((letter)-> t.delta(letter, c));
	    SimpleLine line = new SimpleLine();
	    line.line = t.getNormalized();
	    line.cs = t.getConfidences();
	    return line;
	}

	public static SimpleLine normalized(String ocr, List<Double> cs) {
	    NormalizerTransducer t = new NormalizerTransducer(ocr.length());
	    Iterator<Double> it = cs.iterator();
	    ocr.codePoints().forEach((letter)->{
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
    public List<Token> getTokens() {
        return null;
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
	    final int pos = line.indexOf(word, offset);
	    if (pos < 0) {
	        return Optional.empty();
        }
        return Optional.of(new Word(pos, pos + word.length(), this));
    }
}
