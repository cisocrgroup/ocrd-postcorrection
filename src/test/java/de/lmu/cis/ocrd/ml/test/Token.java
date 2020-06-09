package de.lmu.cis.ocrd.ml.test;

import de.lmu.cis.ocrd.Word;
import de.lmu.cis.ocrd.ml.OCRToken;
import de.lmu.cis.ocrd.ml.Ranking;
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

	Token(Word masterOCR, int id) {
		assert masterOCR != null;
		this.masterOCR = masterOCR;
		this.id = id;
	}

	static Token create(String str, int id) {
		return new Token(Word.create(str), id);
	}

	public Token withGT(List<String> gtTokens) {
		this.gt = String.join("-", gtTokens);
		return this;
	}

	Token withGT(String gt) {
		this.gt = gt;
		return this;
	}

	@Override
	public String getID() {
		throw new RuntimeException("getID: not implemented");
	}

	@Override
	public Optional<String> getGT() {
		return Optional.ofNullable(gt);
	}

	@Override
	public List<Ranking> getRankings() {
		return new ArrayList<>();
	}

	@Override
	public int getNOCR() {
		return otherOCR.size() + 1;
	}

	@Override
	public Word getMasterOCR() {
		return this.masterOCR;
	}

	Token addOCR(Word ocr) {
		assert ocr != null;
		if (this.otherOCR == null) {
			this.otherOCR = new ArrayList<>();
		}
		this.otherOCR.add(ocr);
		return this;
	}

	int getNumberOfOtherOCRs() {
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
	public List<Candidate> getCandidates() {
		return new ArrayList<>();
	}

	@Override
	public void correct(String correction, double confidence, boolean take) {
		/* do nothing */
	}

	@Override
	public Optional<Candidate> getCandidate() {
		return Optional.empty();
	}
}
