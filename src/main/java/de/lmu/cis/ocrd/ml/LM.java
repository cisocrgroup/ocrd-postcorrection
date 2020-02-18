package de.lmu.cis.ocrd.ml;

import de.lmu.cis.ocrd.ml.features.ArgumentFactory;

import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

// step: Language Model
public class LM implements ArgumentFactory {
	private final Path trigrams;
	private List<OCRToken> tokens;
	private List<FreqMap> freqMaps;
	private FreqMap trigramFreqMap;

	public LM(Path trigrams) {
		this.trigrams = trigrams;
		this.freqMaps = null;
		this.trigramFreqMap = null;
	}

	public LM(InputStream is) throws Exception {
		final FreqMap map = new CachingFreqMap();
		this.trigrams = Paths.get("");
		this.freqMaps = null;
		this.trigramFreqMap = CharacterNGrams.readFromCSV(is, map);
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
			freqMaps.get(0).add(token.getMasterOCR().getWordNormalized());
			for (int i = 1; i < token.getNOCR(); i++) {
				freqMaps.get(1).add(token.getSlaveOCR(i - 1).getWordNormalized());
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
