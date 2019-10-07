package de.lmu.cis.ocrd.ml.test.features.test;

import com.google.gson.Gson;
import de.lmu.cis.ocrd.ml.LM;
import de.lmu.cis.ocrd.ml.features.OCRToken;
import de.lmu.cis.ocrd.pagexml.*;
import de.lmu.cis.ocrd.profile.Candidate;
import de.lmu.cis.ocrd.profile.FileProfiler;
import de.lmu.cis.ocrd.profile.Profile;
import org.junit.Before;
import org.pmw.tinylog.Logger;

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
		Logger.info("profile size = " + profile.size());
		for (Line line : Page.open(pagexml).getLines()) {
			for (Word word : line.getWords()) {
				tokens.add(new OCRTokenImpl(word, 2, 10, profile));
				if (tokens.size() == 10) {
					lm.setTokens(tokens);
					return;
				}
			}
		}
	}

	OCRToken getToken(int i) {
        Logger.info("token({}) = {}", i, tokens.get(i).toString());
        return tokens.get(i);
	}

	OCRToken getCandidateToken(int i, int j) {
		final OCRToken t = getToken(i);
        Logger.info("token({}, {}) = {}", i, j, t.toString());
		try {
			Profile profile = new FileProfiler(profilePath).profile();
			Logger.info("lookup = {}", t.getMasterOCR().toString());
			Logger.info("profile: {}", profile.get(t.getMasterOCR().toString()));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		final List<Candidate> cs = t.getAllProfilerCandidates();
		Logger.info("CANDIATE: " + new Gson().toJson(cs.get(j)));
		return new OCRTokenWithCandidateImpl(t, cs.get(j));
	}
}
