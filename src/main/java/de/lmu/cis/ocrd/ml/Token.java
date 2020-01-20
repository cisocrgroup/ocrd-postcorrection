package de.lmu.cis.ocrd.ml;

import com.google.gson.Gson;
import de.lmu.cis.ocrd.SimpleLine;
import de.lmu.cis.ocrd.Word;
import de.lmu.cis.ocrd.ml.features.Ranking;
import de.lmu.cis.ocrd.profile.Candidate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// TODO: Merge with Alignment.Token
public class Token implements Serializable, OCRToken {
	private static final long serialVersionUID = -5699834740435847251L;
	private final Word masterOCR;
	private final int id;

	private String gt;
	private ArrayList<Word> otherOCR;

	public Token(Word masterOCR, int id) {
		assert masterOCR != null;
		this.masterOCR = masterOCR;
		this.id = id;
	}

	public static Token create(String str, int id) {
		return new Token(Word.create(str), id);
	}

	private int getID() {
		return id;
	}

	public Token withGT(List<String> gtTokens) {
		this.gt = String.join("-", gtTokens);
		return this;
	}

	public Token withGT(String gt) {
		this.gt = gt;
		return this;
	}

	@Override
	public Optional<String> getGT() {
		return Optional.ofNullable(gt);
	}

	@Override
	public List<Ranking> getRankings() {
		return new ArrayList<>();
	}

	public boolean isCorrectToken() {
		if (gt == null) {
			return false;
		}
		return gt.equals(masterOCR.toString());
	}

	@Override
	public int getNOCR() {
		return otherOCR.size() + 1;
	}

	@Override
	public Word getMasterOCR() {
		return this.masterOCR;
	}

	public Token addOCR(Word ocr) {
		assert ocr != null;
		if (this.otherOCR == null) {
			this.otherOCR = new ArrayList<>();
		}
		this.otherOCR.add(ocr);
		return this;
	}

	public int getNumberOfOtherOCRs() {
		if (otherOCR == null) {
			return 0;
		}
		return otherOCR.size();
	}

	@Override
	public Word getSlaveOCR(int i) {
		return otherOCR.get(i);
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder(masterOCR.toString());
		char sep = '|';
		if (otherOCR != null) {
			for (Word other : otherOCR) {
				str.append(sep);
				str.append(other.toString());
			}
		}
		if (gt != null) {
			str.append(sep);
			str.append(gt);
		}
		return str.toString();
	}

	@Override
	public List<Candidate> getAllProfilerCandidates() {
		return new ArrayList<>();
	}

	@Override
	public boolean isLexiconEntry() {
		return false;
	}

	@Override
	public void correct(String correction, double confidence) {
		/* do nothing */
	}

	@Override
	public boolean ocrIsCorrect() {
		return false;
	}

	public boolean hasGT() {
		return gt != null;
	}

	public String toJSON() {
		return new Gson().toJson(new Data(this));
	}

	static class Data {
		final String[] ocr;
		final String gt;
		final SimpleLine.Data line;
		final int tokenID;

		Data(Token token) {
			line = new SimpleLine.Data(token.getMasterOCR().getLine());
			tokenID = token.getID();
			gt = token.getGT().isPresent() ? token.getGT().get() : "";
			ocr = new String[1 + token.getNumberOfOtherOCRs()];
			ocr[0] = token.getMasterOCR().toString();
			for (int i = 0; i < token.getNumberOfOtherOCRs(); i++) {
				ocr[i + 1] = token.getSlaveOCR(i).toString();
			}
		}
	}

	@Override
	public Optional<Candidate> getProfilerCandidate() {
		return Optional.empty();
	}
}
