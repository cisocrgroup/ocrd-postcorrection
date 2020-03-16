package de.lmu.cis.ocrd.ml.features;

import de.lmu.cis.ocrd.ml.FreqMap;

public interface ArgumentFactory {
	FreqMap getMasterOCRUnigrams() throws Exception;
	FreqMap getSlaveOCRUnigrams(int i) throws Exception;
	FreqMap getCharacterTrigrams() throws Exception;
}
