package de.lmu.cis.ocrd.cli;

import de.lmu.cis.ocrd.ml.*;
import de.lmu.cis.ocrd.ml.features.FeatureFactory;
import de.lmu.cis.ocrd.profile.*;
import org.pmw.tinylog.Logger;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
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
			decide(predictRankings(alex));
		}
		workspace.write(ifg, ofg);
	}

	private AdditionalLexicon predictLexiconExtensions() throws Exception {
		// profile with no additional lexicon
		final AdditionalLexicon alex = new NoAdditionalLexicon();
		profile = getProfile(ifg, alex, parameters.getNOCR());
		// predict lexicon entries and return them
		final Predictor predictor = getLEPredictor();
		final AdditionalLexiconSet ret = new AdditionalLexiconSet();
		final Protocol protocol = new LEProtocol();
		for (OCRToken token: TokenFilter.filter(workspace.getNormalTokenReader(ifg, profile)).collect(Collectors.toList())){
			final Predictor.Result result = predictor.predict(token, parameters.getNOCR());
			final boolean take = result.getPrediction().getPrediction();
			final double conf = result.getPrediction().getConfidence();
			protocol.protocol(result.getToken(), "", conf, take);
			if (take) {
				ret.add(result.getToken().getMasterOCR().getWordNormalized());
			}
		}
		saveProtocol(parameters.getLETraining().getProtocol(parameters.getNOCR(), true), protocol);
		saveAdditionalLexicon(ret, parameters.getLETraining().getLexicon(parameters.getNOCR()));
		return ret;
	}

	private Rankings predictRankings(AdditionalLexicon alex) throws Exception {
		// no protocol
		// if a profile exists, we need to overwrite it with an alexed profile
		// otherwise no profile exists and we need to create one anyway.
		profile = getProfile(ifg, alex, parameters.getNOCR());
		workspace.resetProfile(ifg, profile);
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
				assert(take && conf >= 0 || !take && conf <= 0);
				rankings.get(token).add(new Ranking(candidate, conf));
			}
		}
		rankings.sort();
		return rankings;
	}

	private void decide(Rankings rankings) throws Exception {
		// no profile
		OCRToken token130 = null;
		final Predictor predictor = getDMPredictor();
		final DMProtocol protocol = new DMProtocol(rankings);
		for (OCRToken token: rankings.keySet()) {
			final Predictor.Result result = predictor.predict(new RankingsOCRToken(token, rankings.get(token)), parameters.getNOCR());
			final boolean take = result.getPrediction().getPrediction();
			final Ranking topRanking = rankings.get(token).get(0);
			protocol.protocol(token, topRanking.getCandidate().Suggestion, topRanking.getRanking(), take);
			if (take) {
				token.correct(topRanking.getCandidate().Suggestion, topRanking.getRanking());
			}
			if ("130".equals(token.getID())) {
				token130 = token;
			}
		}
		saveProtocol(parameters.getDMTraining().getProtocol(parameters.getNOCR(), parameters.isRunLE()), protocol);
		if (token130 != null) {
			Logger.debug("token130: {}", token130.toString());
			Logger.debug("protocol: {}", protocol.getProtocol().corrections.containsKey(token130.getID()) ? protocol.getProtocol().corrections.get(token130.getID()).id : "not found");
			throw new Exception("again this fucking token: " + token130.toString());
		}
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
					);
		}
	}

	private void saveAdditionalLexicon(AdditionalLexiconSet alex, Path path) throws Exception {
		try(Writer w = new BufferedWriter(new FileWriter(path.toFile()))) {
			for (String str: alex) {
				w.write(str);
				w.write('\n');
			}
		}
	}

	private void saveProtocol(Path path, Protocol protocol) throws Exception {
		Logger.debug("saving protocol to {}", path.toString());
		try (OutputStream os = new FileOutputStream(path.toFile())) {
			protocol.write(os);
		}
	}
}
