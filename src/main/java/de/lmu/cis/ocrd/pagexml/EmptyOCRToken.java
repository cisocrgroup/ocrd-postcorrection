package de.lmu.cis.ocrd.pagexml;

import de.lmu.cis.ocrd.ml.OCRToken;
import de.lmu.cis.ocrd.ml.OCRWord;
import de.lmu.cis.ocrd.ml.Ranking;
import de.lmu.cis.ocrd.profile.Candidate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EmptyOCRToken implements OCRToken {
	final OCRToken instance = new EmptyOCRToken();
	private final List<Candidate> candidates = new ArrayList<>();

	private EmptyOCRToken() {}

	@Override
	public int getNOCR() {
		return 0;
	}

	@Override
	public String getID() {return "**EMPTY**ID**";}

	@Override
	public OCRWord getMasterOCR() {
		return EmptyWord.instance;
	}

	@Override
	public OCRWord getSlaveOCR(int i) {
		return EmptyWord.instance;
	}

	@Override
	public Optional<String> getGT() {
		return Optional.empty();
	}

	@Override
	public List<Ranking> getRankings() {
		return new ArrayList<>();
	}

	@Override
	public Optional<Candidate> getCandidate() {
		return Optional.empty();
	}

	@Override
	public List<Candidate> getCandidates() {
		return candidates;
	}

	@Override
	public List<Candidate> getAllCandidates() {return candidates;}

	@Override
	public void correct(String correction, double confidence, boolean take) {
		/* do nothing */
	}

	@Override
	public String toString() {
		return "**EMPTY-TOKEN**";
	}
}
