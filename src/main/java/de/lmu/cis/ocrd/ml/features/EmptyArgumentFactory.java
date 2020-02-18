package de.lmu.cis.ocrd.ml.features;

import de.lmu.cis.ocrd.ml.FreqMap;
import de.lmu.cis.ocrd.profile.Profile;

// helper class for testing
public class EmptyArgumentFactory implements ArgumentFactory {
	@Override
	public FreqMap getMasterOCRUnigrams() {
		return new FreqMap();
	}

	@Override
	public FreqMap getOtherOCRUnigrams(int i) {
		return new FreqMap();
	}

	@Override
	public FreqMap getCharacterTrigrams() {
		return new FreqMap();
	}

	public Profile getProfile() {
		return Profile.empty();
	}

	@Override
	public int getNumberOfOtherOCRs() {
		return 0;
	}
}
