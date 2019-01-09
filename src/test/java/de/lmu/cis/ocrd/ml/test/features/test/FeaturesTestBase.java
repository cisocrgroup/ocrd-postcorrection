package de.lmu.cis.ocrd.ml.test.features.test;

import de.lmu.cis.ocrd.ml.LM;
import de.lmu.cis.ocrd.ml.features.OCRToken;
import de.lmu.cis.ocrd.pagexml.*;
import de.lmu.cis.ocrd.profile.Candidate;
import org.junit.Before;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

class FeaturesTestBase {
	private static final Path pagexml = Paths.get(
			"src/test/resources/workspace/OCR-D-PROFILED" +
					"/aventinus_grammatica_1515" +
					"-aventinus_grammatica_1515_0006.xml");
	private static final Path trigrams = Paths.get(
			"src/test/resources/nGrams.csv");
	private List<OCRToken> tokens;
	protected LM lm;

	@Before
	public void init() throws Exception {
		lm = new LM(true, trigrams);
		tokens = new ArrayList<>();
		for (Line line : Page.open(pagexml).getLines()) {
			for (Word word : line.getWords()) {
				tokens.add(new OCRTokenImpl(word, 2));
				if (tokens.size() == 10) {
					return;
				}
			}
		}
	}

	protected OCRToken getToken(int i) {
		return tokens.get(i);
	}

	protected OCRToken getCandidateToken(int i, int j) {
		final OCRToken t = getToken(i);
		final List<Candidate> cs = t.getAllProfilerCandidates(j+1);
		return new OCRTokenWithCandidateImpl(t, cs.get(j));
	}
}
