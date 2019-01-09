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
	private final int gtIndex;

	public OCRTokenImpl(Word word, int gtIndex) throws Exception {
		this.gtIndex = gtIndex;
		this.word = word;
		this.words = getWords(word, gtIndex);
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
		return words.get(0);
	}

	@Override
	public OCRWord getOtherOCR(int i) {
		return words.get(i+1);
	}

	@Override
	public Optional<String> getGT() {
		if (gtIndex <= 0) {
			return Optional.empty();
		}
		return Optional.of(words.get(gtIndex).getWord());
	}

	@Override
	public Optional<Candidate> getProfilerCandidate() {
		return Optional.empty();
	}

	@Override
	public List<Candidate> getAllProfilerCandidates(int max) {
		if (this.candidates == null) {
			this.candidates = calculateAllCandidates(max);
		}
		return this.candidates;
	}

	@Override
	public String toString() {
		return word.toString();
	}

	private List<Candidate> calculateAllCandidates(int max) {
		List<Candidate> cs = new ArrayList<>();
		for (TextEquiv te : word.getTextEquivs()) {
			if (!te.getDataType().contains("profiler-candidate")) {
				continue;
			}
			cs.add(new Gson().fromJson(te.getDataTypeDetails(),
					Candidate.class));
			cs.get(cs.size()-1).Suggestion = te.getUnicodeNormalized();
			if (cs.size() == max) {
				return cs;
			}
		}
		return cs;
	}
}
