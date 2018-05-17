package de.lmu.cis.ocrd.ml.features;

import de.lmu.cis.ocrd.ml.FreqMap;
import de.lmu.cis.ocrd.profile.Profile;

public interface ArgumentFactory {
    FreqMap<String> getMasterOCRUnigrams();
    FreqMap<String> getOtherOCRUnigrams(int i);
    FreqMap<String> getCharacterTrigrams();
    Profile getProfile();
}
