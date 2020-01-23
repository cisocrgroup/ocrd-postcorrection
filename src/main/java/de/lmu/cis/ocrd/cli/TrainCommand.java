package de.lmu.cis.ocrd.cli;

import de.lmu.cis.ocrd.ml.LM;
import de.lmu.cis.ocrd.ml.ModelZIP;
import de.lmu.cis.ocrd.ml.Rankings;
import de.lmu.cis.ocrd.ml.Trainer;
import de.lmu.cis.ocrd.ml.features.*;
import de.lmu.cis.ocrd.pagexml.METS;
import de.lmu.cis.ocrd.pagexml.METSFileGroupProfiler;
import de.lmu.cis.ocrd.pagexml.METSFileGroupReader;
import de.lmu.cis.ocrd.profile.NoAdditionalLexicon;
import de.lmu.cis.ocrd.profile.Profile;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.file.Paths;

public class TrainCommand extends ParametersCommand {
	private String[] ifgs; // input file groups
	private METS mets; // mets file
	private LM lm;
	private METSFileGroupReader trCache;
	private boolean debug;

	public TrainCommand() {
		super("train");
	}

	@Override
	public void execute(CommandLineArguments config) throws Exception {
		init(config);
		config.setCommand(this); // logging
		this.lm = new LM(parameters.getTrigrams());
		this.mets = METS.open(Paths.get(config.mustGetMETSFile()));
		this.ifgs = config.mustGetInputFileGroups();
		this.trCache = new METSFileGroupReader(mets, parameters);
		this.debug = config.getLogLevel().equalsIgnoreCase("debug");

		for (int i = 0; i < parameters.getNOCR(); i++) {
			final Trainer leTrainer = openLETrainer(i+1);
			final Trainer rrTrainer = openRRTrainer(i+1);
			for (String ifg: ifgs) {
				final Profile profile = new METSFileGroupProfiler(parameters, trCache.getWordReader(ifg), ifg, new NoAdditionalLexicon(), i+1).profile();
				leTrainer.prepare(trCache.getNormalTokenReader(ifg, profile), i+1);
				rrTrainer.prepare(trCache.getCandidateTokenReader(ifg, null), i+1);
			}
			leTrainer.train(parameters.getLETraining().getTraining(i+1), parameters.getLETraining().getModel(i+1));
			rrTrainer.train(parameters.getRRTraining().getTraining(i+1), parameters.getRRTraining().getModel(i+1));
		}

		for (int i = 0; i < parameters.getNOCR(); i++) {
			final Trainer dmTrainer = openDMTrainer(i+1);
			for (String ifg: ifgs) {
				final Rankings rankings = Rankings.load(
						trCache.getNormalTokenReader(ifg, null), // this is OK, since we already loaded these tokens beforehand
						parameters.getRRTraining().getModel(i+1),
						parameters.getRRTraining().getTraining(i+1));
				dmTrainer.prepare(trCache.getRankedTokenReader(ifg, null, rankings), i+1);
			}
			dmTrainer.train(parameters.getDMTraining().getTraining(i+1), parameters.getDMTraining().getModel(i+1));
		}
		// write zipped model
		writeModelZIP();
	}

	private void writeModelZIP() throws Exception {
		final ModelZIP model = new ModelZIP();
		for (int i = 0; i < parameters.getNOCR(); i++) {
			model.addLEModel(parameters.getLETraining().getModel(i+1), i);
			model.addRRModel(parameters.getRRTraining().getModel(i+1), i);
			model.addDMModel(parameters.getDMTraining().getModel(i+1), i);
		}
		model.setLEFeatureSet(parameters.getLETraining().getFeatures());
		model.setRRFeatureSet(parameters.getRRTraining().getFeatures());
		model.setDMFeatureSet(parameters.getDMTraining().getFeatures());
		model.setLanguageModelPath(parameters.getTrigrams().toString());
		model.setCreated(System.currentTimeMillis());
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
								.createFeatureSet(parameters.getLETraining().getFeatures(), parameters.getFilterClasses())
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
								.createFeatureSet(parameters.getRRTraining().getFeatures(), parameters.getFilterClasses())
								.add(new ReRankingGTFeature())
				);
		// trainer.train() closes the writer
		trainer.openARFFWriter(new BufferedWriter(new FileWriter(parameters.getRRTraining().getTraining(n).toFile())), "rr", n);
		return trainer;
	}

	private Trainer openDMTrainer(int n) throws Exception {
		final Trainer trainer = new Trainer()
				.withLanguageModel(lm)
				.withDebug(debug)
				.withFeatureSet(
						FeatureFactory
								.getDefault()
								.withArgumentFactory(lm)
								.createFeatureSet(parameters.getDMTraining().getFeatures(), parameters.getFilterClasses())
								.add(new DMBestRankFeature("dm-best-rank"))
								.add(new DMDifferenceToNextRankFeature("dm-difference-to-next"))
								.add(new DMGTFeature("dm-gt"))
				);
        // trainer.train() closes the writer
		trainer.openARFFWriter(new BufferedWriter(new FileWriter(parameters.getDMTraining().getTraining(n).toFile())), "dm", n);
		return trainer;
	}
}
