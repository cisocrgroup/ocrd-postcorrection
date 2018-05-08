package de.lmu.cis.ocrd.ml.features;

import de.lmu.cis.ocrd.ml.FreqMap;
import de.lmu.cis.ocrd.profile.Profiler;

public interface ArgumentFactory {
    FreqMap<String> getOCRUnigrams();
    FreqMap<String> getCharacterTrigrams();
    Profiler getProfiler();
}
