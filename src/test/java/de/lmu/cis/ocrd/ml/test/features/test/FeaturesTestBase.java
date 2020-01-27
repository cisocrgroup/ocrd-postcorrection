package de.lmu.cis.ocrd.ml.test.features.test;

import de.lmu.cis.ocrd.ml.CandidateOCRToken;
import de.lmu.cis.ocrd.ml.CandidatesOCRToken;
import de.lmu.cis.ocrd.ml.LM;
import de.lmu.cis.ocrd.ml.OCRToken;
import de.lmu.cis.ocrd.pagexml.BaseOCRToken;
import de.lmu.cis.ocrd.pagexml.Line;
import de.lmu.cis.ocrd.pagexml.Page;
import de.lmu.cis.ocrd.pagexml.Word;
import de.lmu.cis.ocrd.profile.Candidate;
import de.lmu.cis.ocrd.profile.Candidates;
import de.lmu.cis.ocrd.profile.FileProfiler;
import de.lmu.cis.ocrd.profile.Profile;
import org.junit.Before;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FeaturesTestBase {
	private static final Path pagexml = Paths.get(
			"src/test/resources/workspace/OCR-D-PROFILED" +
					"/aventinus_grammatica_1515" +
					"-aventinus_grammatica_1515_0006.xml");
	private static final Path trigrams = Paths.get("src/test/resources/nGrams.csv");
	private static final Path profilePath = Paths.get("src/test/resources/workspace/profile.json.gz");
	private List<OCRToken> tokens;
	protected LM lm;

	@Before
	public void init() throws Exception {
		lm = new LM(trigrams);
		tokens = new ArrayList<>();
		Profile profile = new FileProfiler(profilePath).profile();
		// Logger.info("profile size = " + profile.size());
		for (Line line : Page.open(pagexml).getLines()) {
			for (Word word : line.getWords()) {
				tokens.add(makeToken(word, 2, 10, profile));
				if (tokens.size() == 10) {
					lm.setTokens(tokens);
					return;
				}
			}
		}
	}

	private static OCRToken makeToken(Word word, int gtIndex, int maxCandidates, Profile profile) throws Exception {
		Candidates candidates = profile.get(word.getUnicodeNormalized().get(0)).orElse(new Candidates());
		List<Candidate> cands;
		if (candidates.Candidates == null) {
			cands = new ArrayList<>();
		} else {
			cands = candidates.Candidates.subList(0, Math.min(candidates.Candidates.size(), maxCandidates));
		}
		return new CandidatesOCRToken(new BaseOCRToken(word, gtIndex), cands);
	}

	OCRToken getToken(int i) {
        // Logger.info("token({}) = {}", i, tokens.get(i).toString());
        return tokens.get(i);
	}

	OCRToken getCandidateToken(int i, int j) {
		final OCRToken t = getToken(i);
        // Logger.info("token({}, {}) = {}", i, j, t.toString());
		try {
			Profile profile = new FileProfiler(profilePath).profile();
			// Logger.info("lookup = {}", t.getMasterOCR().toString());
			// Logger.info("profile: {}", new Gson().toJson(profile.get(t.getMasterOCR().toString()).get()));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		// Logger.info("CANDIATE: " + new Gson().toJson(t.getCandidates().get(j)));
		return new CandidateOCRToken(t, t.getCandidates().get(j));
	}
}
