package de.lmu.cis.ocrd.ml.features;

import de.lmu.cis.ocrd.ml.FreqMap;
import de.lmu.cis.ocrd.profile.Profile;

public interface ArgumentFactory {
	FreqMap getMasterOCRUnigrams() throws Exception;

	FreqMap getOtherOCRUnigrams(int i) throws Exception;

	FreqMap getCharacterTrigrams() throws Exception;

	Profile getProfile() throws Exception;
}
