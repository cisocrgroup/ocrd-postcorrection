package de.lmu.cis.ocrd.cli;

import de.lmu.cis.ocrd.ml.*;
import de.lmu.cis.ocrd.ml.features.DMBestRankFeature;
import de.lmu.cis.ocrd.ml.features.DMDifferenceToNextRankFeature;
import de.lmu.cis.ocrd.ml.features.FeatureFactory;
import de.lmu.cis.ocrd.pagexml.METSFileGroupProfiler;
import de.lmu.cis.ocrd.profile.*;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;

public class PostCorrectionCommand extends ParametersCommand {
	private String ifg; // input file group
	private LM lm;
	private ModelZIP model;
	private Profile profile;

	public PostCorrectionCommand() {
		super("post-correct");
	}

	@Override
	public void execute(CommandLineArguments config) throws Exception {
		init(config);
		config.setCommand(this); // logging
		this.ifg = config.mustGetSingleInputFileGroup();
		// output file group
		String ofg = config.mustGetSingleOutputFileGroup();
		this.model = ModelZIP.open(parameters.getModel());
		try (InputStream is = model.openLanguageModel()) {
			this.lm = new LM(is);
		}

		AdditionalLexicon alex = new NoAdditionalLexicon();
		if (parameters.isRunLE()) {
			alex = predictLexiconExtensions();
		}
		if (parameters.isRunDM()) {
			final Rankings rankings = predictRankings(alex);
			decide(rankings);
		}
		workspace.write(workspace.getWordReader(ifg), ofg);
		workspace.save();
	}

	private AdditionalLexicon predictLexiconExtensions() throws Exception {
		final AdditionalLexicon alex = new NoAdditionalLexicon();
		profile = new METSFileGroupProfiler(parameters, workspace.getWordReader(ifg), ifg, alex, parameters.getNOCR()).profile();
		final Predictor predictor = getLEPredictor();
		final Protocol protocol = new LEProtocol();
		final AdditionalLexiconSet ret = new AdditionalLexiconSet();
		for (OCRToken token: TokenFilter.filter(workspace.getNormalTokenReader(ifg, profile)).collect(Collectors.toList())){
			final Predictor.Result result = predictor.predict(token, parameters.getNOCR());
			final boolean take = result.getPrediction().getPrediction();
			final double conf = result.getPrediction().getConfidence();
			protocol.protocol(result.getToken(), "", conf, take);
			if (take) {
				ret.add(result.getToken().getMasterOCR().getWordNormalized());
			}
		}
		saveProtocol(parameters.getLETraining().getProtocol(parameters.getNOCR()), protocol);
		return ret;
	}

	private Rankings predictRankings(AdditionalLexicon alex) throws Exception {
		// no protocol
		profile = new METSFileGroupProfiler(parameters, workspace.getWordReader(ifg), ifg, alex, parameters.getNOCR()).profile();
		final Predictor predictor = getRRPredictor();
		Rankings rankings = new Rankings();
		for (OCRToken token: TokenFilter.filter(workspace.getNormalTokenReader(ifg, profile)).collect(Collectors.toList())) {
			if (token.getCandidates().isEmpty()) { // skip token with no interpretation
				continue;
			}
			rankings.put(token, new ArrayList<>());
			for (Candidate candidate: token.getCandidates()) {
				final Predictor.Result result = predictor.predict(new CandidateOCRToken(token, candidate), parameters.getNOCR());
				double conf = result.getPrediction().getConfidence();
				final boolean take = result.getPrediction().getPrediction();
				if (!take) {
					conf = -conf;
				}
				rankings.get(token).add(new Ranking(candidate, conf));
			}
			rankings.get(token).sort(Comparator.comparingDouble(Ranking::getRanking));
		}
		return rankings;
	}

	private void decide(Rankings rankings) throws Exception {
		// no profile
		final Predictor predictor = getDMPredictor();
		final Protocol protocol = new DMProtocol(rankings);
		for (OCRToken token: rankings.keySet()) {
			final Predictor.Result result = predictor.predict(new RankingsOCRToken(token, rankings.get(token)), parameters.getNOCR());
			final boolean take = result.getPrediction().getPrediction();
			final String correction = rankings.get(token).get(0).getCandidate().Suggestion;
			final double conf = rankings.get(token).get(0).getRanking();
			protocol.protocol(token, correction,conf, take);
			if (take) {
				token.correct(correction, conf);
			}
		}
		saveProtocol(parameters.getDMTraining().getProtocol(parameters.getNOCR()), protocol);
	}

	private Predictor getLEPredictor() throws Exception {
		try (InputStream is = model.openLEModel(parameters.getNOCR()-1)) {
			return new Predictor()
					.withLanguageModel(lm)
					.withTokens(workspace.getNormalTokenReader(ifg, profile))
					.withOpenClassifier(is)
					.withFeatureSet(
							FeatureFactory
									.getDefault()
									.withArgumentFactory(lm)
									.createFeatureSet(model.getLEFeatureSet(), parameters.getFilterClasses())
					);
		}
	}

	private Predictor getRRPredictor() throws Exception {
		try (InputStream is = model.openRRModel(parameters.getNOCR()-1)) {
			return new Predictor()
					.withLanguageModel(lm)
					.withTokens(workspace.getNormalTokenReader(ifg, profile))
					.withOpenClassifier(is)
					.withFeatureSet(
							FeatureFactory
									.getDefault()
									.withArgumentFactory(lm)
									.createFeatureSet(model.getRRFeatureSet(), parameters.getFilterClasses())
					);
		}
	}

	private Predictor getDMPredictor() throws Exception {
		try (InputStream is = model.openDMModel(parameters.getNOCR()-1)) {
			return new Predictor()
					.withLanguageModel(lm)
					.withTokens(workspace.getNormalTokenReader(ifg, profile))
					.withOpenClassifier(is)
					.withFeatureSet(
							FeatureFactory
									.getDefault()
									.withArgumentFactory(lm)
									.createFeatureSet(model.getDMFeatureSet(), parameters.getFilterClasses())
									.add(new DMBestRankFeature("dm-best-rank"))
									.add(new DMDifferenceToNextRankFeature("dm-difference-to-next"))
					);
		}
	}

	private void saveProtocol(Path path, Protocol protocol) throws Exception {
		try (OutputStream os = new FileOutputStream(path.toFile())) {
			protocol.write(os);
		}
	}
}
