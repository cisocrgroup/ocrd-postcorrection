package de.lmu.cis.ocrd.cli;

import de.lmu.cis.ocrd.ml.*;
import de.lmu.cis.ocrd.ml.features.*;
import de.lmu.cis.ocrd.pagexml.METS;
import de.lmu.cis.ocrd.pagexml.OCRTokenWithCandidateImpl;
import de.lmu.cis.ocrd.pagexml.OCRTokenWithRankingsImpl;
import de.lmu.cis.ocrd.profile.AdditionalFileLexicon;
import de.lmu.cis.ocrd.profile.AdditionalLexicon;
import de.lmu.cis.ocrd.profile.Candidate;
import de.lmu.cis.ocrd.profile.NoAdditionalLexicon;
import org.apache.commons.io.FileUtils;
import org.pmw.tinylog.Logger;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class EvaluateRRDMCommand extends AbstractMLCommand {

	private FeatureSet rrFS;
	private LM lm;
	private METS mets;
	private boolean debug;
	private DMEvaluator dmEvaluator;

	@Override
	public String getName() {
		return "evaluate-rrdm";
	}

	@Override
	public void execute(CommandLineArguments config) throws Exception {
		config.setCommand(this);
		setParameter(config);
		debug = "debug".equals(config.getLogLevel().toLowerCase());
		lm = new LM(Paths.get(getParameter().trigrams));
		rrFS = FeatureFactory
				.getDefault()
				.withArgumentFactory(lm)
				.createFeatureSet(getParameter().rrTraining.features, getFeatureClassFilter())
				.add(new ReRankingGTFeature());
		mets = METS.open(Paths.get(config.mustGetMETSFile()));
		for (int i = 0; i < getParameter().nOCR; i++) {
			evaluateRR(config.mustGetInputFileGroups(), i, true);
			evaluateRR(config.mustGetInputFileGroups(), i, false);
			evaluateDM(config.mustGetInputFileGroups(), i, true);
			evaluateDM(config.mustGetInputFileGroups(), i, false);
        }
	}

	private void evaluateRR(String[] ifgs, int i, boolean useAlex) throws Exception {
		final String suffix = useAlex? "": "_no_dle";
        try (ARFFWriter w = ARFFWriter
                .fromFeatureSet(rrFS)
                .withRelation("evaluate-rr")
                .withDebugToken(debug)
                .withWriter(openTagged(getParameter().rrTraining.evaluation.replace(".arff", suffix + ".arff"), i+1))
                .writeHeader(i+1)) {
            for (String ifg : ifgs) {
				Logger.debug("input file group: {}", ifg);
				final List<OCRToken> tokens = readTokensWithGT(mets, ifg, getAlex(useAlex, i));
				Logger.debug("read {} OCR tokens with GT from input file group {}", tokens.size(), ifg);
                lm.setTokens(tokens);
                tokens.forEach((token)->{
                    final List<Candidate> cs = token.getAllProfilerCandidates();
                    Logger.debug("adding {} candidates (rr/{})", cs.size(), i +1);
                    cs.forEach((c)-> w.writeToken(new OCRTokenWithCandidateImpl(token, c), i +1));
                });
            }
        }
        evaluate(getParameter().rrTraining.evaluation.replace(".arff", suffix + ".arff"),
                getParameter().rrTraining.model,
                getParameter().rrTraining.result.replace(".txt", suffix + ".txt"),
				i);
    }

    private void evaluateDM(String[] ifgs, int i, boolean useAlex) throws Exception {
		FeatureSet dmFS = FeatureFactory.getDefault()
				.withArgumentFactory(lm)
				.createFeatureSet(getParameter().dmTraining.features, getFeatureClassFilter())
				.add(new DMBestRankFeature("dm-best-rank"))
				.add(new DMDifferenceToNextRankFeature("dm-difference-to-next"))
				.add(new DMGTFeature("dm-gt"));
		final String suffix = useAlex? "" : "_no_dle";
        try (ARFFWriter w = ARFFWriter
                .fromFeatureSet(dmFS)
                .withRelation("evaluate-dm")
                .withDebugToken(debug)
                .withWriter(openTagged(getParameter().dmTraining.evaluation.replace(".arff", suffix + ".arff"), i+1))
                .writeHeader(i+1)) {
            Logger.debug("evaluateDM({})", i+1);
			final Path rrTrain = tagPath(getParameter().rrTraining.evaluation.replace(".arff", suffix + ".arff"), i + 1);
			final Path rrModel = tagPath(getParameter().rrTraining.model, i + 1);
			final LogisticClassifier c = LogisticClassifier.load(rrModel);
			final Instances instances =
					new ConverterUtils.DataSource(rrTrain.toString()).getDataSet();
			instances.setClassIndex(instances.numAttributes() - 1);
			Iterator<Instance> is = instances.iterator();
            for (String ifg : ifgs) {
                Logger.debug("input file group: {}", ifg);
                final List<OCRToken> tokens = readTokensWithGT(mets, ifg, getAlex(useAlex, i));
				Logger.debug("read {} OCR tokens with GT from input file group {}", tokens.size(), ifg);
                lm.setTokens(tokens);
                Map<OCRToken, List<Ranking>> rankings = calculateRankings(tokens, is, c);
				// TODO: is this really needed?
                dmFS = FeatureFactory.getDefault()
						.withArgumentFactory(lm)
						.createFeatureSet(getParameter().dmTraining.features, getFeatureClassFilter())
						.add(new DMBestRankFeature("dm-best-rank"))
						.add(new DMDifferenceToNextRankFeature("dm-difference-to-next"))
						.add(new DMGTFeature("dm-gt"));
                dmEvaluator = new DMEvaluator(rankings, i);
				for (OCRToken token : tokens) {
					if (!rankings.containsKey(token) || rankings.get(token).isEmpty()) {
						continue;
					}
					dmEvaluator.register(token);
					final OCRToken rankedToken = new OCRTokenWithRankingsImpl(token, rankings.get(token));
					if (!DMGTFeature.isValidForTraining(rankedToken)) {
						continue;
					}
					w.writeTokenWithFeatureSet(rankedToken, dmFS, i + 1);
				}
            }
        }
        writeDMEvaluation(i, useAlex);
    }

	private void writeDMEvaluation(int i, boolean useAlex) throws Exception {
		final String suffix = useAlex? "" : "_no_dle";
		final Path evalPath = tagPath(getParameter().dmTraining.evaluation.replace(".arff", suffix + ".arff"), i+1);
		final Path resultPath = tagPath(getParameter().dmTraining.result.replace(".txt", suffix + ".txt"), i+1);
		final Path modelPath = tagPath(getParameter().dmTraining.model, i+1);

		final Instances instances = new ConverterUtils.DataSource(evalPath.toString()).getDataSet();
		instances.setClassIndex(instances.numAttributes()-1);
		final LogisticClassifier classifier = LogisticClassifier.load(modelPath);
		dmEvaluator.setClassifier(classifier);
		dmEvaluator.setInstances(instances);

		try (Writer w = new OutputStreamWriter(new FileOutputStream(
				resultPath.toFile()), StandardCharsets.UTF_8)) {
			dmEvaluator.setWriter(w);
			dmEvaluator.evaluate();
		}
	}

	private void evaluate(String eval, String model, String res, int i) throws Exception {
		final Path evalPath = tagPath(eval, i+1);
		final Path modelPath = tagPath(model, i+1);
		final Path resultPath = tagPath(res, i+1);
		Logger.debug("evaluating {} ({}) {}", evalPath, modelPath, resultPath);
		final LogisticClassifier c = LogisticClassifier.load(modelPath);
		final String title = String.format("\nResults (%d):\n=============\n", i+1);
		final String data = c.evaluate(title, evalPath);
		FileUtils.writeStringToFile(resultPath.toFile(), data, StandardCharsets.UTF_8);
	}

	private AdditionalLexicon getAlex(boolean useAlex, int i) {
		if (useAlex) {
			return new AdditionalFileLexicon(tagPath(getParameter().leTraining.lexicon, i + 1));
		}
		return new NoAdditionalLexicon();
	}

	private Writer openTagged(String path, int i) throws Exception {
		final Path tmp = tagPath(path, i);
		return new BufferedWriter(new FileWriter(tmp.toFile()));
	}
}
