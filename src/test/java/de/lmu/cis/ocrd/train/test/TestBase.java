package de.lmu.cis.ocrd.train.test;

import de.lmu.cis.ocrd.profile.Candidate;
import de.lmu.cis.ocrd.profile.Candidates;
import de.lmu.cis.ocrd.profile.PosPattern;
import de.lmu.cis.ocrd.profile.Profile;
import de.lmu.cis.ocrd.train.Environment;

import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;

class TestBase {
	private final String tmpDirProperty = "java.io.tmpdir";
	private final String path = System.getProperty(tmpDirProperty);
	private final String name = "environment-test";

	String getName() {
		return name;
	}

	String getPath() {
		return path;
	}

	Environment newEnvironment() throws IOException {
		// Configurator.currentConfig().level(Level.DEBUG).activate();
		return new MockEnvironment(path, name);
	}

	private static class MockEnvironment extends Environment {
		MockEnvironment(String base, String name) throws IOException {
			super(base, name);
		}

		@Override
		public Profile getProfile() {
			return new MockProfile();
		}
	}

	private static class MockProfile extends Profile {
		private MockProfile() {
			super(new HashMap<>());
		}

		@Override
		public Optional<Candidates> get(String key) {
			final Candidates candidates = new Candidates();
			candidates.OCR = key;
			final Candidate candidate = new Candidate();
			candidate.Modern = key;
			candidate.Suggestion = key;
			candidate.Dict = "mock";
			candidate.OCRPatterns = new PosPattern[0];
			candidate.HistPatterns = new PosPattern[0];
			candidates.Candidates = new Candidate[]{candidate};
			return Optional.of(candidates);
		}
	}
}
