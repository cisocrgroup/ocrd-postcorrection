package de.lmu.cis.ocrd.cli;

import de.lmu.cis.ocrd.ml.FreqMap;
import de.lmu.cis.ocrd.ml.features.ArgumentFactory;
import de.lmu.cis.ocrd.profile.Profile;
import de.lmu.cis.ocrd.profile.Profiler;
import de.lmu.cis.ocrd.train.Environment;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class AsyncArgumentFactory implements ArgumentFactory {

    private final Environment environment;
    private final ConfigurationJSON data;
    private final List<AsyncOCRUnigrams> ocrUnigrams = new ArrayList<>();
    private final AsyncCharTrigrams charTrigrams;
    private final Profiler profiler;

    public AsyncArgumentFactory(ConfigurationJSON data, Environment environment, Profiler profiler) {
        this.data = data;
        this.environment = environment;
        ocrUnigrams.add(new AsyncOCRUnigrams(environment.getMasterOCR()));
        for (int i = 0; i < environment.getNumberOfOtherOCR(); i++) {
            ocrUnigrams.add(new AsyncOCRUnigrams(environment.getOtherOCR(i)));
        }
        charTrigrams = new AsyncCharTrigrams(Paths.get(data.getLanguageModel().getCharacterTrigrams()));
        this.profiler = new AsyncProfiler(profiler);
    }

    @Override
    public FreqMap getMasterOCRUnigrams() {
        return doGetFreqMap(0);
    }

    @Override
    public FreqMap getOtherOCRUnigrams(int i) {
        return doGetFreqMap(i + 1);
    }

    private FreqMap doGetFreqMap(int i) {
        try {
            return ocrUnigrams.get(i).getFreqMap();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public FreqMap getCharacterTrigrams() {
        try {
            return charTrigrams.getFreqMap();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Profile getProfile() {
        try {
            return profiler.profile();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
