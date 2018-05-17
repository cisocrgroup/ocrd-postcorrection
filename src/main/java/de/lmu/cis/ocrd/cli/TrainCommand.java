package de.lmu.cis.ocrd.cli;

import de.lmu.cis.ocrd.ml.FeatureSet;
import de.lmu.cis.ocrd.ml.features.FeatureFactory;
import de.lmu.cis.ocrd.train.DynamicLexiconTrainer;
import de.lmu.cis.ocrd.train.Environment;

import java.io.IOException;

public class TrainCommand implements Command {
    private Environment environment;
    private FeatureSet featureSet;

    @Override
    public String getName() {
        return "train";
    }

    @Override
    public void execute(Configuration config) throws Exception {
        if (config.getArgs().length < 3) {
            throw new Exception("usage: name gt master-ocr [other-ocr...]");
        }
        environment = newEnvironment(config);
        featureSet = newFeatureSet(config);
        final DynamicLexiconTrainer trainer = new DynamicLexiconTrainer(environment, featureSet);
        trainer.prepare().train().evaluate();
    }

    private FeatureSet newFeatureSet(Configuration configuration) throws Exception {
        return new FeatureFactory()
                .withArgumentFactory(new ArgumentFactory(configuration.getParameters(), environment))
                .createFeatureSet(configuration.getParameters().getDynamicLexiconTrainig().getFeatures());
    }

    private static Environment newEnvironment(Configuration configuration) throws IOException {
        final String name = configuration.getArgs()[0];
        final String gt = configuration.getArgs()[1];
        final String masterOCR = configuration.getArgs()[2];
        final Environment environment = new Environment(configuration.getWorkDir(), name)
                .withDebugTokenAlignment(configuration.getParameters().getDynamicLexiconTrainig().isDebugTrainingTokens())
                .withCopyTrainingFiles(configuration.getParameters().getDynamicLexiconTrainig().isCopyTrainingFiles())
                .withGT(gt)
                .withMasterOCR(masterOCR);
        for (int i = 3; i < configuration.getArgs().length; i++) {
            environment.addOtherOCR(configuration.getArgs()[i]);
        }
        return environment;
    }
}
