package de.lmu.cis.ocrd.pagexml;

import com.google.gson.Gson;
import de.lmu.cis.ocrd.ml.features.OCRToken;
import de.lmu.cis.ocrd.ml.features.OCRWord;
import de.lmu.cis.ocrd.profile.Candidate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EmptyToken implements OCRToken {
	final OCRToken instance = new EmptyToken();
	private List<OCRWord> words = new ArrayList<>();

	private EmptyToken() {}

	@Override
	public int getNOCR() {
		return 0;
	}

	@Override
	public OCRWord getMasterOCR() {
		return null;
	}

	@Override
	public OCRWord getOtherOCR(int i) {
		return words.get(i+1);
	}

	@Override
	public Optional<String> getGT() {
		if (gtIndex <= 0 || gtIndex >= words.size()) {
			return Optional.empty();
		}
		return Optional.of(words.get(gtIndex).getWord());
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
		assert(this.candidates.size() <= maxCandidates);
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
			cs.get(cs.size()-1).Suggestion = te.getUnicodeNormalized();
			if (cs.size() == maxCandidates) {
				return cs;
			}
		}
		return cs;
	}
}
