package de.lmu.cis.ocrd.train;

import de.lmu.cis.ocrd.Document;
import de.lmu.cis.ocrd.ml.CharacterNGrams;
import de.lmu.cis.ocrd.ml.FreqMap;
import de.lmu.cis.ocrd.ml.features.ArgumentFactory;
import de.lmu.cis.ocrd.profile.LocalProfiler;
import de.lmu.cis.ocrd.profile.Profile;
import de.lmu.cis.ocrd.profile.Profiler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Callable;

public class LazyArgumentFactory implements ArgumentFactory {
	private final Environment environment;
	private final Configuration configuration;
	private Document masterOCRDocument;
	private Profile profile;
	private FreqMap characterTrigrams;
	private FreqMap masterOCRUnigrams;
	private ArrayList<FreqMap> otherOCRUnigrams;

	public LazyArgumentFactory(Environment environment) throws IOException {
		this.environment = environment;
		this.configuration = environment.openConfiguration();
	}

	@Override
	public FreqMap getMasterOCRUnigrams() {
		if (masterOCRUnigrams == null) {
			masterOCRUnigrams = tryToLoad(() -> getFreqMap(getMasterOCRDocument()));
		}
		return masterOCRUnigrams;
	}

	@Override
	public FreqMap getOtherOCRUnigrams(int i) {
		while (otherOCRUnigrams.size() <= i) {
			otherOCRUnigrams.add(null);
		}
		if (otherOCRUnigrams.get(i) == null) {
			otherOCRUnigrams.set(i, tryToLoad(() -> {
				final Document document = environment.openOtherOCR(i);
				return getFreqMap(document);
			}));
		}
		return otherOCRUnigrams.get(i);
	}

	@Override
	public FreqMap getCharacterTrigrams() {
		if (characterTrigrams == null) {
			characterTrigrams = tryToLoad(() -> CharacterNGrams.fromCSV(configuration.getLanguageModel().getCharacterTrigrams()));
		}
		return characterTrigrams;
	}

	@Override
	public Profile getProfile() {
		if (profile == null) {
			profile = tryToLoad(() -> {
				final Profiler profiler = new LocalProfiler()
						.withArgs(configuration.getProfiler().getArguments())
						.withExecutable(configuration.getProfiler().getExecutable())
						.withLanguageDirectory(configuration.getProfiler().getLanguageDirectory())
						.withLanguage(configuration.getProfiler().getLanguage())
						.withInputDocument(getMasterOCRDocument());
				return profiler.profile();
			});
		}
		return profile;
	}

	private Document getMasterOCRDocument() {
		try {
			if (masterOCRDocument == null) {
				masterOCRDocument = environment.openMasterOCR();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return masterOCRDocument;
	}

	private static <T> T tryToLoad(Callable<T> f) {
		try {
			return f.call();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static FreqMap getFreqMap(Document document) throws Exception {
		FreqMap freqMap = new FreqMap();
		document.eachLine((line) -> {
			for (String token : line.line.getNormalized().split("\\s+")) {
				freqMap.add(token);
			}
		});
		return freqMap;
	}
}
