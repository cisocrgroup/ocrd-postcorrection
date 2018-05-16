package de.lmu.cis.ocrd.cli;

import de.lmu.cis.ocrd.Document;
import de.lmu.cis.ocrd.FileTypes;
import de.lmu.cis.ocrd.ml.CharacterNGrams;
import de.lmu.cis.ocrd.ml.FreqMap;
import de.lmu.cis.ocrd.profile.Profile;
import de.lmu.cis.ocrd.profile.Profiler;
import de.lmu.cis.ocrd.train.Environment;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

// Lazy ArgumentFactory that calculates unigram lists as needed.
public class ArgumentFactory implements de.lmu.cis.ocrd.ml.features.ArgumentFactory {

    private final Environment environment;
    private final ConfigurationJSON data;
    private Future<FreqMap<String>> ocrUnigrams;
    private FreqMap<String> theOCRUnigrams;
    private Future<FreqMap<String>> charTrigrams;
    private FreqMap<String> theCharTrigrams;
    private Future<Profile> profile;
    private Profile theProfile;
    private List<String> theTokens;
    private Future<List<String>> tokens;

    ArgumentFactory(ConfigurationJSON data, Environment environment) {
        this.data = data;
        this.environment = environment;
        setup();
    }

    @Override
    public FreqMap<String> getOCRUnigrams() {
        try {
            if (theOCRUnigrams == null) {
                theOCRUnigrams = ocrUnigrams.get();
            }
            return theOCRUnigrams;
        } catch (InterruptedException | ExecutionException e) {
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
        } catch (InterruptedException | ExecutionException e) {
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
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }


    private synchronized List<String> getTokens() {
        try {
            if (theTokens == null) {
                theTokens = tokens.get();
            }
            return theTokens;
        } catch (InterruptedException|ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private void setup() {
        startToReadTokens();
        startToReadOCRUnigrams();
        startToReadCharacterTrigrams();
        startToReadProfile();
    }

    private void startToReadOCRUnigrams() {
        ocrUnigrams = new FutureTask<>(()->{
            FreqMap<String> u = new FreqMap<>();
            for (String token : getTokens()) {
                u.add(token);
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
        for (String token : getTokens()) {
           str.append(token).append('\n');
        }
        return new StringReader(str.toString());
    }

    private void startToReadTokens() {
        tokens = new FutureTask<>(()->{
            Document document = FileTypes.openDocument(environment.fullPath(environment.getMasterOCR()).toString());
            ArrayList<String> tokens = new ArrayList<>();
            document.eachLine((line)-> tokens.addAll(Arrays.asList(line.line.getNormalized().split("\\s+"))));
            return tokens;
        });
    }
}
