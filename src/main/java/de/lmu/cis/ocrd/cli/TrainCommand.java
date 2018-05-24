package de.lmu.cis.ocrd.cli;

import de.lmu.cis.ocrd.ml.FeatureSet;
import de.lmu.cis.ocrd.ml.features.FeatureFactory;
import de.lmu.cis.ocrd.profile.LocalProfiler;
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

	public void execute(CommandLineArguments config) throws Exception {
    	if (config.getArgs().length < 3) {
            throw new Exception("usage: name gt master-ocr [other-ocr...]");
        }
		boolean ok = false;
		environment = newEnvironment(config);
		try {
			featureSet = newFeatureSet(config);
			final DynamicLexiconTrainer trainer = new DynamicLexiconTrainer(environment, featureSet);
			trainer.prepare().train().evaluate();
			ok = true;
		} finally {
			if (!ok) {
				environment.remove();
			}
		}
    }

	private FeatureSet newFeatureSet(CommandLineArguments commandLineArguments) throws Exception {
        return FeatureFactory.getDefault()
				.withArgumentFactory(new AsyncArgumentFactory(commandLineArguments.getParameters(), environment, new LocalProfiler()))
				.createFeatureSet(commandLineArguments.getParameters().getDynamicLexiconTrainig().getFeatures());
    }

	private static Environment newEnvironment(CommandLineArguments commandLineArguments) throws IOException {
		final String name = commandLineArguments.getArgs()[0];
		final String gt = commandLineArguments.getArgs()[1];
		final String masterOCR = commandLineArguments.getArgs()[2];
		final Environment environment = new Environment(commandLineArguments.getWorkDir(), name)
				.withDebugTokenAlignment(commandLineArguments.getParameters().getDynamicLexiconTrainig().isDebugTrainingTokens())
				.withCopyTrainingFiles(commandLineArguments.getParameters().getDynamicLexiconTrainig().isCopyTrainingFiles())
                .withGT(gt)
                .withMasterOCR(masterOCR);
		for (int i = 3; i < commandLineArguments.getArgs().length; i++) {
			environment.addOtherOCR(commandLineArguments.getArgs()[i]);
        }
        return environment;
    }
}
