package de.lmu.cis.ocrd.cli;

import de.lmu.cis.ocrd.ml.LM;
import de.lmu.cis.ocrd.ml.Rankings;
import de.lmu.cis.ocrd.ml.Trainer;
import de.lmu.cis.ocrd.ml.features.FeatureFactory;
import de.lmu.cis.ocrd.profile.AdditionalFileLexicon;
import de.lmu.cis.ocrd.profile.AdditionalLexicon;
import de.lmu.cis.ocrd.profile.NoAdditionalLexicon;
import de.lmu.cis.ocrd.profile.Profile;

import java.io.OutputStreamWriter;

class ARFFCommand extends ParametersCommand {
    ARFFCommand() {
        super("arff");
    }

    @Override
    public void execute(CommandLineArguments config) throws Exception {
        init(config);
        config.setCommand(this);
        final CommandLineArguments.TrainingType trainingType = config.getTrainingType();
        final LM lm = new LM(parameters.getTrigrams());
        final Trainer trainer = new Trainer().withLanguageModel(lm).withDebug("DEBUG".equalsIgnoreCase(config.getLogLevel()));
        final String[] ifgs = config.mustGetInputFileGroups();
        final int nOCR = parameters.getNOCR();
        Profile profile;
        switch (trainingType) {
            case LE:
                trainer.withFeatureSet(
                        FeatureFactory.getDefault().withArgumentFactory(lm).createFeatureSet(
                                parameters.getLETraining().getFeatures(), parameters.getClassFilter()));
                trainer.openARFFWriter(new OutputStreamWriter(System.out), trainingType.toString(), parameters.getNOCR());
                for (String ifg: ifgs) {
                    profile = getProfile(ifg, new NoAdditionalLexicon(), nOCR);
                    trainer.prepare(workspace.getNormalTokenReader(ifg, profile), nOCR);
                }
                trainer.closeARFFWriter();
                break;
            case RR:
                trainer.withFeatureSet(
                        FeatureFactory.getDefault().withArgumentFactory(lm).createFeatureSet(
                                parameters.getRRTraining().getFeatures(), parameters.getClassFilter()));
                trainer.openARFFWriter(new OutputStreamWriter(System.out), trainingType.toString(), parameters.getNOCR());
                for (String ifg: ifgs) {
                    profile = getProfile(ifg, getAdditionalLexicon(), nOCR);
                    trainer.prepare(workspace.getCandidateTokenReader(ifg, profile), nOCR);
                }
                trainer.closeARFFWriter();
                break;
            case DM:
                trainer.withFeatureSet(
                        FeatureFactory.getDefault().withArgumentFactory(lm).createFeatureSet(
                                parameters.getDMTraining().getFeatures(), parameters.getClassFilter()));
                trainer.openARFFWriter(new OutputStreamWriter(System.out), trainingType.toString(), parameters.getNOCR());
                for (String ifg: ifgs) {
                    profile = getProfile(ifg, getAdditionalLexicon(), nOCR);
                    final Rankings rankings = Rankings.load(
                            workspace.getNormalTokenReader(ifg, profile),
                            parameters.getRRTraining().getModel(nOCR),
                            parameters.getRRTraining().getTraining(nOCR));
                    trainer.prepare(workspace.getRankedTokenReader(ifg, profile, rankings), parameters.getNOCR());
                }
                trainer.closeARFFWriter();
                break;
            default:
                throw new Exception("internal error: bad feature type: " + trainingType.toString());
        }
    }

    private AdditionalLexicon getAdditionalLexicon() {
        if (parameters.isRunLE()) {
            return new AdditionalFileLexicon(parameters.getLETraining().getLexicon(parameters.getNOCR()));
        }
        return new NoAdditionalLexicon();
    }
}
