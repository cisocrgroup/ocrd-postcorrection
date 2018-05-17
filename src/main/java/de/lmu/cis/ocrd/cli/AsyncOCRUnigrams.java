package de.lmu.cis.ocrd.cli;

import de.lmu.cis.ocrd.Document;
import de.lmu.cis.ocrd.FileTypes;
import de.lmu.cis.ocrd.ml.FreqMap;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

class AsyncOCRUnigrams {
    private final Path path;
    private final Future<List<String>> tokens;
    private FreqMap<String> freqMap;

    AsyncOCRUnigrams(Path path) {
        this.path = path;
        tokens = startToRead();
    }

    public List<String> getTokens() throws Exception {
        return tokens.get();
    }

    FreqMap<String> getFreqMap() throws Exception {
        if (freqMap == null) {
            freqMap = makeFreqMap(getTokens());
        }
        return freqMap;
    }

    private static FreqMap<String> makeFreqMap(List<String> tokens) {
        final FreqMap<String> freqMap = new FreqMap<>();
        for (String token : tokens) {
            freqMap.add(token);
        }
        return freqMap;
    }

    private Future<List<String>> startToRead() {
        final FutureTask<List<String>> future = new FutureTask<>(()->{
            final Document document = FileTypes.openDocument(path.toString());
            final ArrayList<String> tokens = new ArrayList<>();
            document.eachLine((line)-> tokens.addAll(Arrays.asList(line.line.getNormalized().split("\\s+"))));
            return tokens;
        });
        future.run();
        return future;
    }
}
