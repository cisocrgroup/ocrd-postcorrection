package de.lmu.cis.ocrd.cli;

import de.lmu.cis.ocrd.ml.CharacterNGrams;
import de.lmu.cis.ocrd.ml.FreqMap;
import de.lmu.cis.ocrd.ml.Token;
import de.lmu.cis.ocrd.profile.Profile;
import de.lmu.cis.ocrd.profile.Profiler;

import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

public class ArgumentFactory implements de.lmu.cis.ocrd.ml.features.ArgumentFactory {

    private final List<Token> tokens;
    private final ConfigurationJSON data;
    private Future<FreqMap<String>> ocrUnigrams;
    private FreqMap<String> theOCRUnigrams;
    private Future<FreqMap<String>> charTrigrams;
    private FreqMap<String> theCharTrigrams;
    private Future<Profile> profile;
    private Profile theProfile;

    public ArgumentFactory(ConfigurationJSON data, List<Token> tokens) {
        this.data = data;
        this.tokens = tokens;
        setup();
    }

    @Override
    public FreqMap<String> getOCRUnigrams() {
        try {
            if (theOCRUnigrams == null) {
                theOCRUnigrams = ocrUnigrams.get();
            }
            return theOCRUnigrams;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public FreqMap<String> getCharacterTrigrams() {
        try {
            if (theCharTrigrams == null) {
                theCharTrigrams = charTrigrams.get();
            }
            return theCharTrigrams;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Profile getProfile() {
        try {
            if (theProfile == null) {
                theProfile = profile.get();
            }
            return theProfile;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private void setup() {
        startToReadOCRUnigrams();
        startToReadCharacterTrigrams();
        startToReadProfile();
    }

    private void startToReadOCRUnigrams() {
        ocrUnigrams = new FutureTask<>(()->{
            FreqMap<String> u = new FreqMap<>();
            for (Token token : tokens) {
                u.add(token.getMasterOCR().toString());
            }
            return u;
        });
    }

    private void startToReadCharacterTrigrams() {
        charTrigrams = new FutureTask<>(()->{
            try {
                return CharacterNGrams.fromCSV(data.getLanguageModel().getCharacterTrigrams());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void startToReadProfile() {
        profile = new FutureTask<>(()-> new Profiler()
                .withArgs("--types", "--sourceFormat", "TXT")
                .withExecutable(data.getProfiler().getExecutable())
                .withLanguageDirectory(data.getProfiler().getLanguageDirectory())
                .withLanguage(data.getProfiler().getLanguage())
                .withStdin(makeTokensReader())
                .run());
    }

    private Reader makeTokensReader() {
        StringBuilder str = new StringBuilder();
        for (Token token : tokens) {
           str.append(token.getMasterOCR().toString()).append('\n');
        }
        return new StringReader(str.toString());
    }
}
