package de.lmu.cis.ocrd.pagexml;

import de.lmu.cis.ocrd.ml.OCRToken;
import de.lmu.cis.ocrd.ml.OCRWord;
import de.lmu.cis.ocrd.ml.features.Ranking;
import de.lmu.cis.ocrd.profile.Candidate;

import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

public class OCRTokenWithRankingsImpl implements OCRToken {
	private final List<Ranking>rankings;
	private final Candidate candidate;
	private final OCRToken token;

	public OCRTokenWithRankingsImpl(OCRToken token, List<Ranking> rs) {
		assert(!rs.isEmpty());
		this.token = token;
		this.rankings = rs;
		this.candidate = rs.get(0).candidate;
	}

	@Override
	public int getNOCR() {
		return token.getNOCR();
	}

	@Override
	public OCRWord getMasterOCR() {
		return token.getMasterOCR();
	}

	@Override
	public OCRWord getSlaveOCR(int i) {
		return token.getSlaveOCR(i);
	}

	@Override
	public Optional<String> getGT() {
		return token.getGT();
	}

	@Override
	public List<Ranking> getRankings() {
		return rankings;
	}

	@Override
	public Optional<Candidate> getCandidate() {
		return Optional.of(candidate);
	}

	@Override
	public List<Candidate> getCandidates() {
		return token.getCandidates();
	}

	@Override
	public void correct(String correction, double confidence) {
		token.correct(correction, confidence);
	}

	@Override
	public String toString() {
		return String.format("%s,suggestion:%s,hist:%d,ocr:%d,rs:%s",
							 token.toString(), candidate.Suggestion,
							 candidate.HistPatterns.length, candidate.OCRPatterns.length,
							 rankingsToString());
	}

	private String rankingsToString() {
		StringJoiner sj = new StringJoiner(",", "[", "]");
		for (Ranking ranking : rankings) {
			sj.add(ranking.candidate + ":" + Double.toString(ranking.ranking));
		}
		return sj.toString();
	}
}
