package de.lmu.cis.ocrd.cli;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import de.lmu.cis.ocrd.ml.LM;
import de.lmu.cis.ocrd.ml.LogisticClassifier;
import de.lmu.cis.ocrd.ml.features.FeatureFactory;
import de.lmu.cis.ocrd.ml.features.FeatureSet;
import de.lmu.cis.ocrd.ml.features.OCRToken;
import de.lmu.cis.ocrd.pagexml.METS;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class PostCorrectionCommand extends AbstractMLCommand {
    private ModelZIP model;
    private METS mets;
    private LM lm;

    @Override
    public String getName() {
        return "post-correct";
    }

    @Override
    public void execute(CommandLineArguments config) throws Exception {
        setParameter(config);
        final Parameter parameter = getParameter();
        model = ModelZIP.open(Paths.get(parameter.model));
        mets = METS.open(Paths.get(config.mustGetMETSFile()));
        lm = new LM(false, Paths.get(getParameter().trigrams));
        final String ifg = config.mustGetSingleInputFileGroup();
        final String ofg = config.mustGetSingleOutputFileGroup();
        final Set<String> alex = runDLE(ifg, parameter.nOCR);
    }

    private Set<String> runDLE(String ifg, int nOCR) throws Exception {
        final List<OCRToken> tokens = readTokens(mets, ifg, Optional.empty());
        lm.setTokens(tokens);
        final FeatureSet fs = makeFeatureSet(model.openDLEFeatureSet());
        final LogisticClassifier c = LogisticClassifier.load(model.openDLEModel(nOCR));
        Set<String> set = new HashSet<>();
        for (OCRToken token: tokens) {
           final FeatureSet.Vector values = fs.calculateFeatureVector(token, nOCR);
            if (c.predict(values).getPrediction()) {
                set.add(token.getMasterOCR().toString());
            }
        }
        return set;
    }

    private FeatureSet makeFeatureSet(InputStream is) throws Exception {
        try (InputStream iis = is) {
            final String json = IOUtils.toString(iis, Charset.forName("UTF-8"));
            final JsonObject[] os = new Gson().fromJson(json, JsonObject[].class);
            return FeatureFactory.getDefault().withArgumentFactory(lm).createFeatureSet(os);
        }
    }

}
