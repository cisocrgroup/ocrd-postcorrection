package de.lmu.cis.ocrd.cli;

import de.lmu.cis.ocrd.ml.*;
import de.lmu.cis.ocrd.profile.*;
import org.pmw.tinylog.Logger;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class PostCorrectionCommand extends ParametersCommand {
	private LM lm;
	private ModelZIP model;
	private Profile profile;

	PostCorrectionCommand(String name) {super(name);}
	public PostCorrectionCommand() {
		this("post-correct");
	}

	@Override
	public void execute(CommandLineArguments config) throws Exception {
		init(config);
		config.setCommand(this); // logging
		// output file group
		final String ofg = config.maybeGetSingleOutputFileGroup(); // can be null
		this.model = ModelZIP.open(parameters.getModel());
		try (InputStream is = model.openLanguageModel()) {
			this.lm = new LM(is);
		}

		final String ifg = config.mustGetSingleInputFileGroup();
		if (config.isIterate()) {
			iterate((nOCR, runLE)->postCorrect(ifg, ofg, nOCR, runLE));
		} else {
			postCorrect(ifg, ofg, parameters.getNOCR(), parameters.isRunLE());
		}
	}

	interface Func {
		void apply(int nOCR, boolean runLE) throws Exception;
	}

	void iterate(Func f) throws Exception {
		final boolean[] runLE = new boolean[]{false, true};
		for (boolean b: runLE) {
			for (int n = 0; n < parameters.getNOCR(); n++) {
				f.apply(n+1, b);
			}
		}
	}

	private void postCorrect(String ifg, String ofg, int nOCR, boolean runLE) throws Exception {
		Logger.debug("postCorrect({}, {}, {}, {})", ifg, ofg, nOCR, runLE);
		AdditionalLexicon alex = new NoAdditionalLexicon();
		if (runLE) {
			alex = predictLexiconExtensions(ifg, nOCR);
		}
		if (parameters.isRunDM()) {
			decide(predictRankings(alex, ifg, nOCR), ifg, nOCR, runLE, ofg != null);
		}
		if (ofg != null) {
			workspace.write(ifg, ofg);
		}
	}

	private AdditionalLexicon predictLexiconExtensions(String ifg, int nOCR) throws Exception {
		Logger.debug("predictLexiconExtensions({}, {})", ifg, nOCR);
		// profile with no additional lexicon
		final AdditionalLexicon alex = new NoAdditionalLexicon();
		profile = getProfile(ifg, alex, nOCR);
		// predict lexicon entries and return them
		final Predictor predictor = getLEPredictor(ifg, nOCR);
		final AdditionalLexiconSet ret = new AdditionalLexiconSet();
		final Protocol protocol = new LEProtocol();
		for (OCRToken token: TokenFilter.filter(workspace.getNormalTokenReader(ifg, profile)).collect(Collectors.toList())){
			final Predictor.Result result = predictor.predict(token, nOCR);
			final boolean take = result.getPrediction().getPrediction();
			final double conf = result.getPrediction().getConfidence();
			protocol.protocol(result.getToken(), "", conf, take);
			if (take) {
				ret.add(result.getToken().getMasterOCR().getWordNormalized());
			}
		}
		saveProtocol(parameters.getLETraining().getProtocol(nOCR, true), protocol);
		saveAdditionalLexicon(ret, parameters.getLETraining().getLexicon(nOCR));
		return ret;
	}

	private Rankings predictRankings(AdditionalLexicon alex, String ifg, int nOCR) throws Exception {
		Logger.debug("predictRankings({}, {})", ifg, nOCR);
		// no protocol
		// if a profile exists, we need to overwrite it with an alexed profile
		// otherwise no profile exists and we need to create one anyway.
		profile = getProfile(ifg, alex, nOCR);
		workspace.resetProfile(ifg, profile);
		final Predictor predictor = getRRPredictor(ifg, nOCR);
		Rankings rankings = new Rankings();
		for (OCRToken token: TokenFilter.filter(workspace.getNormalTokenReader(ifg, profile)).collect(Collectors.toList())) {
			if (token.getCandidates().isEmpty()) { // skip token with no interpretation
				continue;
			}
			rankings.put(token, new ArrayList<>());
			for (Candidate candidate: token.getCandidates()) {
				final Predictor.Result result = predictor.predict(new CandidateOCRToken(token, candidate), nOCR);
				double conf = result.getPrediction().getConfidence();
				final boolean take = result.getPrediction().getPrediction();
				if (!take) {
					conf = -conf;
				}
				assert(take && conf >= 0 || !take && conf <= 0);
				rankings.get(token).add(new Ranking(candidate, conf));
			}
		}
		rankings.sortAllRankings();
		return rankings;
	}

	private void decide(Rankings rankings, String ifg, int nOCR, boolean runLE, boolean doCorrect) throws Exception {
		Logger.debug("decide({}, {}, {})", ifg, nOCR, runLE);
		// no profile
		final Predictor predictor = getDMPredictor(ifg, nOCR);
		final DMProtocol protocol = new DMProtocol(rankings);
		for (OCRToken token: rankings.keySet()) {
			final Predictor.Result result = predictor.predict(new RankingsOCRToken(token, rankings.get(token)), nOCR);
			final boolean take = result.getPrediction().getPrediction();
			final Ranking topRanking = rankings.get(token).get(0);
			protocol.protocol(token, topRanking.getCandidate().Suggestion, topRanking.getRanking(), take);
			if (take && doCorrect) {
				token.correct(topRanking.getCandidate().Suggestion, topRanking.getRanking());
			}
		}
		saveProtocol(parameters.getDMTraining().getProtocol(nOCR, runLE), protocol);
	}

	private Predictor getLEPredictor(String ifg, int nOCR) throws Exception {
		try (InputStream is = model.openLEModel(nOCR-1)) {
			return new Predictor()
					.withLanguageModel(lm)
					.withTokens(workspace.getNormalTokenReader(ifg, profile))
					.withOpenClassifier(is)
					.withFeatureSet(model.getLEFeatures(lm));
		}
	}

	private Predictor getRRPredictor(String ifg, int nOCR) throws Exception {
		try (InputStream is = model.openRRModel(nOCR-1)) {
			return new Predictor()
					.withLanguageModel(lm)
					.withTokens(workspace.getNormalTokenReader(ifg, profile))
					.withOpenClassifier(is)
					.withFeatureSet(model.getRRFeatures(lm));
		}
	}

	private Predictor getDMPredictor(String ifg, int nOCR) throws Exception {
		try (InputStream is = model.openDMModel(nOCR-1)) {
			return new Predictor()
					.withLanguageModel(lm)
					.withTokens(workspace.getNormalTokenReader(ifg, profile))
					.withOpenClassifier(is)
					.withFeatureSet(model.getDMFeatures(lm));
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
