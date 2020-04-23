package de.lmu.cis.ocrd.cli;

import com.google.gson.GsonBuilder;
import de.lmu.cis.ocrd.config.Parameters;
import de.lmu.cis.ocrd.config.Profiler;

import java.util.ArrayList;
import java.util.Collections;

class DefaultConfigCommand implements Command{
    @Override
    public void execute(CommandLineArguments config) {
        final Parameters parameters = makeParameters(config);
        System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(parameters));
    }

    private static Parameters makeParameters(CommandLineArguments config) {
        Parameters parameters = new Parameters();
        parameters.setNOCR(config.maybeGetNOCR().orElse(2));
        parameters.setDir("train");
        parameters.setCourageous(true);
        parameters.setRunDM(true);
        parameters.setRunLE(true);
        parameters.setFilterClasses(Collections.singletonList("deactivate"));
        parameters.setMaxCandidates(10);
        parameters.setTrigrams("language-model/trigrams.csv");
        parameters.setLEFeatures(new ArrayList<>());
        parameters.setRRFeatures(new ArrayList<>());
        parameters.setDMFeatures(new ArrayList<>());
        parameters.setProfiler(makeProfiler());
        return parameters;
    }

    private static Profiler makeProfiler() {
        Profiler profiler = new Profiler();
        profiler.setConfig("/path/to/language-config.ini");
        profiler.setPath("/path/to/profiler-executable");
        profiler.setIsNoCache(false);
        return profiler;
    }

    @Override
    public String getName() {
        return "default-config";
    }
}
