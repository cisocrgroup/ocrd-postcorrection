package de.lmu.cis.ocrd.ml.features;

import de.lmu.cis.ocrd.ml.FreqMap;

public interface ArgumentFactory {
	FreqMap getMasterOCRUnigrams() throws Exception;
	FreqMap getOtherOCRUnigrams(int i) throws Exception;
	int getNumberOfOtherOCRs() throws Exception;
	FreqMap getCharacterTrigrams() throws Exception;
}
