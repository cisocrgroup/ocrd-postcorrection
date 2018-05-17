package de.lmu.cis.ocrd.ml.features;

import de.lmu.cis.ocrd.ml.FreqMap;
import de.lmu.cis.ocrd.profile.Profile;

public interface ArgumentFactory {
    FreqMap getMasterOCRUnigrams();

    FreqMap getOtherOCRUnigrams(int i);

    FreqMap getCharacterTrigrams();
    Profile getProfile();
}
