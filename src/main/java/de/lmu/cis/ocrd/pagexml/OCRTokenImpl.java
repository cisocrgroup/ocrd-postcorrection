package de.lmu.cis.ocrd.pagexml;

import com.google.gson.Gson;
import de.lmu.cis.ocrd.ml.features.OCRToken;
import de.lmu.cis.ocrd.ml.features.OCRWord;
import de.lmu.cis.ocrd.profile.Candidate;
import de.lmu.cis.ocrd.profile.Candidates;
import de.lmu.cis.ocrd.profile.Profile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OCRTokenImpl implements OCRToken {

	private final Word word;
	private final List<OCRWordImpl> words;
	private final List<Candidate> candidates;
	private final int gtIndex;
	private final Profile profile;

	public OCRTokenImpl(Word word, int gtIndex, int maxCandidates, Profile profile) throws Exception {
		this.gtIndex = gtIndex;
		this.word = word;
		this.words = getWords(word, gtIndex);
		this.profile = profile;
		this.candidates = getCandidates(profile, maxCandidates);
	}

	private static List<OCRWordImpl> getWords(Word word, int gtIndex) throws Exception {
		List<OCRWordImpl> words = new ArrayList<>();
		final List<TextEquiv> tes = word.getTextEquivs();
		if (tes.isEmpty()) {
			throw new Exception("empty word");
		}
		final List<Double> mConfs = new ArrayList<>();
		for (Glyph g : word.getGlyphs()) {
			final List<TextEquiv> gtes = g.getTextEquivs();
			if (gtes == null || gtes.isEmpty()) {
				mConfs.add(0.0);
			} else {
				mConfs.add(gtes.get(0).getConfidence());
			}
		}
		final List<String> normLines =
				word.getParentLine().getUnicodeNormalized();
		for (int i = 0; i <= gtIndex && i < tes.size() && i < normLines.size(); i++) {
			words.add(new OCRWordImpl(tes.get(i), normLines.get(i), mConfs));
		}
		while (words.size() < gtIndex) {
			words.add(new OCRWordImpl(tes.get(0), normLines.get(0), mConfs));
		}
		return words;
	}

	@Override
	public int getNOCR() {
		return gtIndex;
	}

	@Override
	public OCRWord getMasterOCR() {
		if (0 < words.size()) {
			return words.get(0);
		}
		return EmptyWord.instance;
	}

	@Override
	public OCRWord getSlaveOCR(int i) {
		if (i+1 < words.size()) {
			return words.get(i+1);
		}
		return EmptyWord.instance;
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
		return this.candidates;
	}

	@Override
	public String toString() {
		return word.toString();
	}

	@Override
	public boolean ocrIsCorrect() {
		final String gt = getGT().orElseThrow(() -> new RuntimeException("missing ground-truth"));
		return gt.equalsIgnoreCase(getMasterOCR().toString());
	}

	public List<Candidate> getAllProfilerCandidatesNoLimit() {
		List<Candidate> cs = new ArrayList<>();
		Optional<Candidates> candidates = profile.get(getMasterOCR().toString().toLowerCase());
		if (!candidates.isPresent()) {
			return cs;
		}
		for (Candidate candidate : candidates.get().Candidates) {
			cs.add(candidate);
		}
		return cs;
	}

	private List<Candidate> getCandidates(Profile profile, int maxCandidates) {
		List<Candidate> cs = new ArrayList<>();
		Optional<Candidates> candidates = profile.get(getMasterOCR().toString().toLowerCase());
		if (!candidates.isPresent()) {
			return cs;
		}
		for (Candidate candidate : candidates.get().Candidates) {
			if (cs.size() == maxCandidates) {
				return cs;
			}
			cs.add(candidate);
		}
		return cs;
	}
}
