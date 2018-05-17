package de.lmu.cis.ocrd.cli;

import de.lmu.cis.ocrd.ml.CharacterNGrams;
import de.lmu.cis.ocrd.ml.FreqMap;

import java.nio.file.Path;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

class AsyncCharTrigrams {
    private final Path path;
    private final Future<FreqMap<String>> charTrigrams;

    AsyncCharTrigrams(Path path) {
        this.path = path;
        charTrigrams = startToRead();
    }

    FreqMap<String> getFreqMap() throws Exception {
        return charTrigrams.get();
    }

    private Future<FreqMap<String>> startToRead() {
        final FutureTask<FreqMap<String>> future = new FutureTask<>(()-> CharacterNGrams.fromCSV(path.toString()));
        future.run();
        return future;
    }
}
