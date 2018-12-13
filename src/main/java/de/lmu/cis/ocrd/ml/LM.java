package de.lmu.cis.ocrd.ml;

import de.lmu.cis.ocrd.ml.features.ArgumentFactory;
import de.lmu.cis.ocrd.pagexml.Line;
import de.lmu.cis.ocrd.pagexml.METS;
import de.lmu.cis.ocrd.pagexml.Page;
import de.lmu.cis.ocrd.pagexml.Word;
import org.pmw.tinylog.Logger;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

// step: Language Model
public class LM implements ArgumentFactory {
	private final boolean gt;
	private final Path trigrams;
	private final List<METS.File> files;
	private List<FreqMap> freqMaps;
	private FreqMap trigramFreqMap;

	public LM(boolean gt, Path trigrams, List<METS.File> files) {
		this.gt = gt;
		this.trigrams = trigrams;
		this.files = files;
		this.freqMaps = null;
		this.trigramFreqMap = null;
	}

	private void loadFreqMapsIfNotPresent() throws Exception {
		if (freqMaps != null) {
			return;
		}
		freqMaps = new ArrayList<>();
		int n = 0;
		for (METS.File file : this.files) {
			final Page page = Page.parse(file.open());
			for (final Line line : page.getLines()) {
				if (n == 0) {
					n = line.getTextEquivs().size();
				}
				for (final Word word : line.getWords()) {
					addToFreqMaps(word, n);
				}
			}
		}
	}

	private void addToFreqMaps(Word word, int n) {
		Logger.debug("addToFreqMaps({}, {})", word.toString(), n);
		final List<String> aligned = word.getUnicodeNormalized();
		if (gt) {
			n -= 1;
		}
		while (freqMaps.size() <= n) {
			freqMaps.add(new FreqMap());
		}
		for (int i = 0; i < n && i < aligned.size(); i++) {
			freqMaps.get(i).add(aligned.get(i));
		}
	}

	private void loadTrigramsIfNotPresent() throws Exception {
		if (trigramFreqMap != null) {
			return;
		}
		trigramFreqMap = CharacterNGrams.fromCSV(trigrams.toString());
	}

	@Override
	public FreqMap getMasterOCRUnigrams() throws Exception {
		loadFreqMapsIfNotPresent();
		return freqMaps.get(0);
	}

	@Override
	public FreqMap getOtherOCRUnigrams(int i) throws Exception {
		loadFreqMapsIfNotPresent();
		return freqMaps.get(i + 1);
	}

	@Override
	public int getNumberOfOtherOCRs() throws Exception {
		loadFreqMapsIfNotPresent();
		return freqMaps.size() - 1;
	}

	@Override
	public FreqMap getCharacterTrigrams() throws Exception {
		loadTrigramsIfNotPresent();
		return trigramFreqMap;
	}
}
