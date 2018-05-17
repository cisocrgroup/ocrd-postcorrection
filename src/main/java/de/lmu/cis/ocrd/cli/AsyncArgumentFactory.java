package de.lmu.cis.ocrd.cli;

import de.lmu.cis.ocrd.ml.FreqMap;
import de.lmu.cis.ocrd.ml.features.ArgumentFactory;
import de.lmu.cis.ocrd.profile.Profile;
import de.lmu.cis.ocrd.train.Environment;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class AsyncArgumentFactory implements ArgumentFactory {

    private final Environment environment;
    private final ConfigurationJSON data;
    private final List<AsyncOCRUnigrams> ocrUnigrams = new ArrayList<>();
    private final AsyncCharTrigrams charTrigrams;

    public AsyncArgumentFactory(ConfigurationJSON data, Environment environment) {
        this.data = data;
        this.environment = environment;
        ocrUnigrams.add(new AsyncOCRUnigrams(environment.getMasterOCR()));
        for (int i = 0; i < environment.getNumberOfOtherOCR(); i++) {
            ocrUnigrams.add(new AsyncOCRUnigrams(environment.getOtherOCR(i)));
        }
        charTrigrams = new AsyncCharTrigrams(Paths.get(data.getLanguageModel().getCharacterTrigrams()));
    }

    @Override
    public FreqMap<String> getMasterOCRUnigrams() {
        return doGetFreqMap(0);
    }

    @Override
    public FreqMap<String> getOtherOCRUnigrams(int i) {
        return doGetFreqMap(i + 1);
    }

    private FreqMap<String> doGetFreqMap(int i) {
        try {
            return ocrUnigrams.get(i).getFreqMap();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public FreqMap<String> getCharacterTrigrams() {
        try {
            return charTrigrams.getFreqMap();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Profile getProfile() {
        throw new RuntimeException("not implemented");
    }
}
