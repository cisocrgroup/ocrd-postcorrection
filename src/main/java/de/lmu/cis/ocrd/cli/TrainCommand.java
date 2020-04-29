package de.lmu.cis.ocrd.cli;

import de.lmu.cis.ocrd.ml.*;
import de.lmu.cis.ocrd.ml.features.*;
import de.lmu.cis.ocrd.profile.NoAdditionalLexicon;
import de.lmu.cis.ocrd.profile.Profile;
import org.pmw.tinylog.Logger;

import java.io.BufferedWriter;
import java.io.FileWriter;

public class TrainCommand extends ParametersCommand {
	private LM lm;
	private DMGTFeature dmgtFeature;
	private boolean debug;

	public TrainCommand() {
		super("train");
	}

	@Override
	public void execute(CommandLineArguments config) throws Exception {
		init(config);
		config.setCommand(this); // logging
		this.lm = new LM(parameters.getTrigrams());
		// input file groups
		String[] ifgs = config.mustGetInputFileGroups();
		this.debug = config.getLogLevel().equalsIgnoreCase("debug");

		// train lexicon extension and ranking
		for (int i = 0; i < parameters.getNOCR(); i++) {
			final Trainer leTrainer = openLETrainer(i+1);
			final Trainer rrTrainer = openRRTrainer(i+1);
			for (String ifg: ifgs) {
				Logger.info("le/rr training for {}({})", ifg, i+1);
				final Profile profile = getProfile(ifg, new NoAdditionalLexicon(), i+1);
				leTrainer.prepare(workspace.getNormalTokenReader(ifg, profile), i+1);
				rrTrainer.prepare(workspace.getCandidateTokenReader(ifg, null), i+1);
			}
			leTrainer.train(parameters.getLETraining().getTraining(i+1), parameters.getLETraining().getModel(i+1), isDebug());
			rrTrainer.train(parameters.getRRTraining().getTraining(i+1), parameters.getRRTraining().getModel(i+1), isDebug());
		}

		// train decision maker
		for (int i = 0; i < parameters.getNOCR(); i++) {
			final Trainer dmTrainer = openDMTrainer(i+1);
			for (String ifg: ifgs) {
				Logger.info("dm training for {}({})", ifg, i+1);
				final Profile profile = getProfile(ifg, new NoAdditionalLexicon(), i+1);
				final Rankings rankings = Rankings.load(
						workspace.getNormalTokenReader(ifg, profile), // this is OK, since we already loaded these tokens beforehand
						parameters.getRRTraining().getModel(i+1),
						parameters.getRRTraining().getTraining(i+1));
				dmTrainer.prepare(workspace.getRankedTokenReader(ifg, profile, rankings), i+1, (dmgtFeature::isValidForTraining));
			}
			dmTrainer.train(parameters.getDMTraining().getTraining(i+1), parameters.getDMTraining().getModel(i+1), isDebug());
		}
		writeModelZIP();
	}

	private void writeModelZIP() throws Exception {
		final Model model = new Model();
		for (int i = 0; i < parameters.getNOCR(); i++) {
			model.addLEModel(parameters.getLETraining().getModel(i+1), i);
			model.addRRModel(parameters.getRRTraining().getModel(i+1), i);
			model.addDMModel(parameters.getDMTraining().getModel(i+1), i);
		}
		model.setFilterClasses(parameters.getFilterClasses());
		model.setLEFeatureSet(parameters.getLETraining().getFeatures());
		model.setRRFeatureSet(parameters.getRRTraining().getFeatures());
		model.setDMFeatureSet(parameters.getDMTraining().getFeatures());
		model.setLanguageModelPath(parameters.getTrigrams().toString());
		model.setCourageous(parameters.isCourageous());
		model.setNOCR(parameters.getNOCR());
		model.setMaxCandidates(parameters.getMaxCandidates());
		model.setCreated(System.currentTimeMillis());
		Logger.debug("saving model to {}", parameters.getModel().toString());
		model.save(parameters.getModel());
	}

	private Trainer openLETrainer(int n) throws Exception {
		final Trainer trainer = new Trainer()
				.withLanguageModel(lm)
				.withDebug(debug)
				.withFeatureSet(
						FeatureFactory
								.getDefault()
								.withArgumentFactory(lm)
								.createFeatureSet(parameters.getLETraining().getFeatures(), parameters.getClassFilter())
								.add(new DynamicLexiconGTFeature())
				);
		// trainer.train() closes the writer
		trainer.openARFFWriter(new BufferedWriter(new FileWriter(parameters.getLETraining().getTraining(n).toFile())), "le", n);
		return trainer;
	}

	private Trainer openRRTrainer(int n) throws Exception {
		final Trainer trainer = new Trainer()
				.withLanguageModel(lm)
				.withDebug(debug)
				.withFeatureSet(
						FeatureFactory
								.getDefault()
								.withArgumentFactory(lm)
								.createFeatureSet(parameters.getRRTraining().getFeatures(), parameters.getClassFilter())
								.add(new ReRankingGTFeature())
				);
		// trainer.train() closes the writer
		trainer.openARFFWriter(new BufferedWriter(new FileWriter(parameters.getRRTraining().getTraining(n).toFile())), "rr", n);
		return trainer;
	}

	private Trainer openDMTrainer(int n) throws Exception {
		dmgtFeature = parameters.isCourageous() ?
				new CourageousDMGTFeature("courageous-dm-gt"):
				new DMGTFeature("dm-gt");
		final Trainer trainer = new Trainer()
				.withLanguageModel(lm)
				.withDebug(debug)
				.withFeatureSet(
						FeatureFactory
								.getDefault()
								.withArgumentFactory(lm)
								.createFeatureSet(parameters.getDMTraining().getFeatures(), parameters.getClassFilter())
								.add(dmgtFeature)
				);
        // trainer.train() closes the writer
		trainer.openARFFWriter(new BufferedWriter(new FileWriter(parameters.getDMTraining().getTraining(n).toFile())), "dm", n);
		return trainer;
	}
}
