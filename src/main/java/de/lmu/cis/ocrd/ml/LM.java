package de.lmu.cis.ocrd.ml;

import de.lmu.cis.ocrd.ml.features.ArgumentFactory;
import de.lmu.cis.ocrd.ml.features.OCRToken;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

// step: Language Model
public class LM implements ArgumentFactory {
	private final boolean gt;
	private final Path trigrams;
	private List<OCRToken> tokens;
	private List<FreqMap> freqMaps;
	private FreqMap trigramFreqMap;

	public LM(boolean gt, Path trigrams) {
		this.gt = gt;
		this.trigrams = trigrams;
		this.freqMaps = null;
		this.trigramFreqMap = null;
	}

	private void loadFreqMapsIfNotPresent() {
		if (freqMaps != null) {
			return;
		}
		freqMaps = new ArrayList<>();
		if (tokens.size() > 0) {
			for (int i = 0; i < tokens.get(0).getNOCR(); i++) {
				freqMaps.add(new FreqMap());
			}
		}
		for (OCRToken token : tokens) {
			freqMaps.get(0).add(token.getMasterOCR().getWord());
			for (int i = 1; i < token.getNOCR(); i++) {
				freqMaps.get(1).add(token.getOtherOCR(i - 1).getWord());
			}
		}
	}

	private void loadTrigramsIfNotPresent() throws Exception {
		if (trigramFreqMap != null) {
			return;
		}
		// use a caching map
		final FreqMap map = new CachingFreqMap();
		trigramFreqMap = CharacterNGrams.addFromCSV(trigrams.toString(), map);
	}

	public void setTokens(List<OCRToken> tokens) {
		this.tokens = tokens;
		this.freqMaps = null;
	}

	@Override
	public FreqMap getMasterOCRUnigrams() {
		loadFreqMapsIfNotPresent();
		return freqMaps.get(0);
	}

	@Override
	public FreqMap getOtherOCRUnigrams(int i) {
		loadFreqMapsIfNotPresent();
		return freqMaps.get(i + 1);
	}

	@Override
	public int getNumberOfOtherOCRs() {
		loadFreqMapsIfNotPresent();
		return freqMaps.size() - 1;
	}

	@Override
	public FreqMap getCharacterTrigrams() throws Exception {
		loadTrigramsIfNotPresent();
		return trigramFreqMap;
	}
}
