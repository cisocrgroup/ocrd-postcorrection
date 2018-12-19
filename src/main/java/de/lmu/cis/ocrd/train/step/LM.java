package de.lmu.cis.ocrd.train.step;

import de.lmu.cis.ocrd.ml.CharacterNGrams;
import de.lmu.cis.ocrd.ml.FreqMap;
import de.lmu.cis.ocrd.ml.features.ArgumentFactory;
import de.lmu.cis.ocrd.pagexml.Line;
import de.lmu.cis.ocrd.pagexml.Page;
import de.lmu.cis.ocrd.pagexml.Word;
import de.lmu.cis.ocrd.profile.Profile;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

// step: Language Model
public class LM implements ArgumentFactory {
	private final int startIndex;
	private final String trigramsPath;
	private final List<String> files;
	private List<FreqMap> freqMaps;
	private FreqMap trigrams;
	private Profile profile;

	LM(boolean gt, String trigrams, List<String> files) {
		this.startIndex = gt ? 1 : 0;
		this.trigramsPath = trigrams;
		this.files = files;
		this.freqMaps = null;
		this.profile = null;
		this.trigrams = null;
	}

	private void loadFreqMapsIfNotPresent() throws Exception {
		if (freqMaps != null) {
			return;
		}
		freqMaps = new ArrayList<>();
		for (String file : this.files) {
			final Page page = Page.open(Paths.get(file));
			for (final Line line : page.getLines()) {
				for (final Word word : line.getWords()) {
					addToFreqMaps(word);
				}
			}
		}
	}

	private void addToFreqMaps(Word word) throws Exception {
		final List<String> alligned = word.getUnicode();
		while (alligned.size() - startIndex != freqMaps.size()) {
			freqMaps.add(new FreqMap());
		}
		for (int i = startIndex; i < alligned.size(); i++) {
			freqMaps.get(i-startIndex).add(alligned.get(i));
		}
	}

	private void loadTrigramsIfNotPresent() throws Exception {
		if (trigrams != null) {
			return;
		}
		trigrams = CharacterNGrams.fromCSV(trigramsPath);
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
		return trigrams;
	}

	public void setProfile(Profile profile) {
		this.profile = profile;
	}

	public Profile getProfile() throws Exception {
		return profile;
	}
}
