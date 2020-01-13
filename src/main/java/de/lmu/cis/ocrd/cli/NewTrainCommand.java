package de.lmu.cis.ocrd.cli;

import de.lmu.cis.ocrd.config.Parameters;
import de.lmu.cis.ocrd.ml.LM;
import de.lmu.cis.ocrd.ml.ModelZIP;
import de.lmu.cis.ocrd.ml.Trainer;
import de.lmu.cis.ocrd.ml.features.*;
import de.lmu.cis.ocrd.pagexml.CachingProfiler;
import de.lmu.cis.ocrd.pagexml.FileGroupTokenReaderCache;
import de.lmu.cis.ocrd.pagexml.METS;
import de.lmu.cis.ocrd.profile.NoAdditionalLexicon;
import de.lmu.cis.ocrd.profile.Profile;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class NewTrainCommand implements Command {
	private Parameters parameters;
	private String[] ifgs; // input file groups
	private METS mets; // mets file
	private LM lm;
	private FileGroupTokenReaderCache trCache;

	@Override
	public String getName() {
		return "train";
	}

	public Parameters getParameters() {
		return parameters;
	}

	@Override
	public void execute(CommandLineArguments config) throws Exception {
		config.setCommand(this); // logging
		this.parameters = config.mustGetParameter(Parameters.class);
		this.lm = new LM(parameters.getTrigrams());
		this.mets = METS.open(Paths.get(config.mustGetMETSFile()));
		this.ifgs = config.mustGetInputFileGroups();
		this.trCache = new FileGroupTokenReaderCache(mets, parameters);

		for (int i = 0; i < parameters.getNOCR(); i++) {
			final Trainer leTrainer = openLETrainer(i+1);
			final Trainer rrTrainer = openRRTrainer(i+1);
			for (String ifg: ifgs) {
				final Profile profile = new CachingProfiler(parameters, trCache.getWordReader(ifg), ifg, new NoAdditionalLexicon(), i+1).profile();
				leTrainer.prepare(trCache.getNormalTokenReader(ifg, profile), i+1);
				rrTrainer.prepare(trCache.getCandidateTokenReader(ifg, null), i+1);
			}
			leTrainer.train(parameters.getLETraining().getTraining(i+1), parameters.getLETraining().getModel(i+1));
			rrTrainer.train(parameters.getRRTraining().getTraining(i+1), parameters.getRRTraining().getModel(i+1));
		}

		for (int i = 0; i < parameters.getNOCR(); i++) {
			final Trainer dmTrainer = openDMTrainer(i+1);
			for (String ifg: ifgs) {
				Map<OCRToken, List<Ranking>> rankings = dmTrainer.getRankings(
						trCache.getNormalTokenReader(ifg, null),
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
				.withLM(lm)
				.withFeatureSet(
						FeatureFactory
								.getDefault()
								.withArgumentFactory(lm)
								.createFeatureSet(parameters.getLETraining().getFeatures(), parameters.getFilterClasses())
								.add(new DynamicLexiconGTFeature())
				);
		// trainer closes the writer
		trainer.openARFFWriter(new BufferedWriter(new FileWriter(parameters.getLETraining().getTraining(n).toFile())), "le", n);
		return trainer;
	}

	private Trainer openRRTrainer(int n) throws Exception {
		final Trainer trainer = new Trainer()
				.withLM(lm)
				.withFeatureSet(
						FeatureFactory
								.getDefault()
								.withArgumentFactory(lm)
								.createFeatureSet(parameters.getRRTraining().getFeatures(), parameters.getFilterClasses())
								.add(new ReRankingGTFeature())
				);
		// trainer closes the writer
		trainer.openARFFWriter(new BufferedWriter(new FileWriter(parameters.getRRTraining().getTraining(n).toFile())), "rr", n);
		return trainer;
	}

	private Trainer openDMTrainer(int n) throws Exception {
		final Trainer trainer = new Trainer()
				.withLM(lm)
				.withFeatureSet(
						FeatureFactory
								.getDefault()
								.withArgumentFactory(lm)
								.createFeatureSet(parameters.getDMTraining().getFeatures(), parameters.getFilterClasses())
								.add(new DMBestRankFeature("dm-best-rank"))
								.add(new DMDifferenceToNextRankFeature("dm-difference-to-next"))
								.add(new DMGTFeature("dm-gt"))
				);
        // trainer closes the writer
		trainer.openARFFWriter(new BufferedWriter(new FileWriter(parameters.getDMTraining().getTraining(n).toFile())), "dm", n);
		return trainer;
	}
}
