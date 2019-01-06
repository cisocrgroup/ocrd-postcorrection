package de.lmu.cis.ocrd.pagexml;

import com.google.gson.Gson;
import de.lmu.cis.ocrd.ml.features.OCRToken;
import de.lmu.cis.ocrd.ml.features.OCRWord;
import de.lmu.cis.ocrd.profile.Candidate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OCRTokenImpl implements OCRToken {

	private final Word word;
	private final List<OCRWordImpl> words;
	private List<Candidate> candidates;
	private final int gtindex;

	public OCRTokenImpl(Word word, int gtindex) {
		this.gtindex = gtindex;
		this.words = new ArrayList<>();
		this.word = word;
		for (int i = 0; i <= gtindex; i++) {
			words.add(new OCRWordImpl(i, word));
		}
	}

	@Override
	public int getNOCR() {
		return gtindex;
	}

	@Override
	public OCRWord getMasterOCR() {
		return words.get(0);
	}

	@Override
	public OCRWord getOtherOCR(int i) {
		return words.get(i+1);
	}

	@Override
	public Optional<String> getGT() {
		if (gtindex <= 0) {
			return Optional.empty();
		}
		return Optional.of(words.get(gtindex).getWord());
	}

	@Override
	public Optional<Candidate> getProfilerCandidate() {
		return Optional.empty();
	}

	@Override
	public List<Candidate> getAllProfilerCandidates() {
		if (this.candidates == null) {
			this.candidates = calculateAllCandidates();
		}
		return this.candidates;
	}

	@Override
	public String toString() {
		return word.toString();
	}

	private List<Candidate> calculateAllCandidates() {
		List<Candidate> cs = new ArrayList<>();
		for (TextEquiv te : word.getTextEquivs()) {
			if (!te.getDataType().contains("profiler-candidate")) {
				continue;
			}
			cs.add(new Gson().fromJson(te.getDataTypeDetails(),
					Candidate.class));
		}
		return cs;
	}
}
